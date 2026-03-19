package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.SHOW_DEBUG_TOAST
import com.taiwanlife.teamwalk.remote.adapter.InstantAdapter
import com.taiwanlife.teamwalk.remote.adapter.ZoneOffsetAdapter
import com.taiwanlife.teamwalk.ui.common.model.TeamWalkRecordModel
import org.json.JSONObject
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries.zoneId
import java.util.Locale

fun String.enableToBoolean(): Boolean {
    return equals("Y")
}

fun Boolean.enableToString(): String {
    return if (this) "Y" else "N"
}

fun String.quoteJS(): String {
    return JSONObject.quote(this)
}

fun getGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter())
        .registerTypeAdapter(ZoneOffset::class.java, ZoneOffsetAdapter()) // 註冊 ZoneOffset 適配器
//        .setPrettyPrinting()
        .create()
}

/**
 * 只有在Debug模式會出現的Toast
 */
fun Context.debugToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG && SHOW_DEBUG_TOAST) {
        toast(message, duration)
    }
}

/**
 * 只有在Debug模式會出現的Toast
 */
fun Context.debugToast(@StringRes stringId: Int, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG && SHOW_DEBUG_TOAST) {
        toast(stringId, duration)
    }
}

/**
 * Toast
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Toast
 */
fun Context.toast(@StringRes stringId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, this.getString(stringId), Toast.LENGTH_SHORT).show()
}

/**
 * 將HealthConnect的類別轉為給Javascript頁面的Record類別
 */
fun SleepSessionRecord.toTeamWalkRecord(): TeamWalkRecordModel {
    val zoneId =
        endZoneOffset?.let { ZoneOffset.ofTotalSeconds(it.totalSeconds) } ?: ZoneId.of("UTC")
    val startSeconds = startTime.epochSecond.toString()
    val endSeconds = endTime.epochSecond.toString()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"))
    val localFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd").withLocale(Locale.US).withZone(zoneId)

    val duration = (Duration.between(startTime, endTime).toMillis() / 1000) // 總睡眠時間（秒）

    return TeamWalkRecordModel(
        startTimestamp = startSeconds,
        endTimestamp = endSeconds,
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = duration
    )
}

/**
 * 將HealthConnect的類別轉為給Javascript頁面的Record類別
 */
fun StepsRecord.toTeamWalkRecord(): TeamWalkRecordModel {
    val zoneId = endZoneOffset?.let { ZoneId.ofOffset("UTC", it) } ?: ZoneId.of("UTC")

    val startSeconds = startTime.epochSecond.toString()
    val endSeconds = endTime.epochSecond.toString()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"))

    val localFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        .withLocale(Locale.US)
        .withZone(zoneId)

    return TeamWalkRecordModel(
        startTimestamp = startSeconds,
        endTimestamp = endSeconds,
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = count // 使用者的步數
    )
}

fun AggregationResultGroupedByDuration.toStepTeamWalkRecord(): TeamWalkRecordModel {
    val startSeconds = startTime.epochSecond.toString()
    val endSeconds = endTime.epochSecond.toString()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withZone(ZoneId.of("UTC"))

    // 匯總資料通常建議使用系統預設時區，因為匯總物件本身不帶 zoneOffset
    val localFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        .withZone(Config.TAIWAN_ZONE_ID)

    // 從 result 中提取步數總和
    val totalSteps = result[StepsRecord.COUNT_TOTAL] ?: 0L

    return TeamWalkRecordModel(
        startTimestamp = startSeconds,
        endTimestamp = endSeconds,
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = totalSteps
    )
}

fun AggregationResultGroupedByDuration.toSleepTeamWalkRecord(): TeamWalkRecordModel {
    val startSeconds = startTime.epochSecond.toString()
    val endSeconds = endTime.epochSecond.toString()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withZone(ZoneId.of("UTC"))

    val localFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        .withZone(Config.TAIWAN_ZONE_ID)

    // 提取睡眠總時長，並轉為秒數 (Long)
    val sleepDurationInSeconds = result[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.seconds ?: 0L

    return TeamWalkRecordModel(
        startTimestamp = startSeconds,
        endTimestamp = endSeconds,
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = sleepDurationInSeconds
    )
}

fun Uri.toOrigin():String {
    return "${scheme}://${host}"
}


/**
 * 強制使用 Chrome 開啟網頁，若無 Chrome 則顯示瀏覽器選擇器
 */
fun Context.getChromeIntent(url: String): Intent? {
    if (url.isBlank()) return null

    try {
        val uri = url.toUri()
        val chromePackage = "com.android.chrome"
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)

        // 檢查是否有安裝 Chrome
        val isChromeInstalled = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(chromePackage, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(chromePackage, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        val finalIntent = if (isChromeInstalled) {
            // 強制指定 Chrome 開啟 (Explicit Intent)
            browserIntent.setPackage(chromePackage)
            browserIntent
        } else {
            // 如果沒有 Chrome，清除 package 設定並建立選擇器 (Intent Chooser)
            browserIntent.setPackage(null)
            Intent.createChooser(browserIntent, "請選擇瀏覽器")
        }

        return finalIntent

    } catch (e: Exception) {
        // 防呆
    }

    return null
}