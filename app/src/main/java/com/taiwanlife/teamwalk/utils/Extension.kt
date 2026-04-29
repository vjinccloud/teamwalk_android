package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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

    return try {
        val uri = url.toUri()
        val chromePackage = "com.android.chrome"
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)

        browserIntent.setPackage(chromePackage)

        val resolveInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                browserIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        if (resolveInfo != null) {
            // Chrome 存在且可用
            browserIntent
        } else {
            // Chrome 未安裝/停用 通用選擇器
            browserIntent.setPackage(null)
            Intent.createChooser(browserIntent, "請選擇瀏覽器")
        }
    } catch (e: Exception) {
        // 萬一發生意外 返回最基本的 Intent
        Intent(Intent.ACTION_VIEW, url.toUri())
    }
}

/**
 * 0003006: WebView 載入 URL 時加 cache buster timestamp
 * 範例：https://x.com/bridge → https://x.com/bridge?_=1714117200000
 *      https://x.com/bridge?id=5 → https://x.com/bridge?id=5&_=1714117200000
 * 確保每次載入都不讀快取。
 */
fun String.appendCacheBuster(): String {
    if (this.isBlank()) return this
    val separator = if (this.contains('?')) '&' else '?'
    return "$this${separator}_=${System.currentTimeMillis()}"
}

/**
 * 0003008: 跳到本 App 的「應用程式詳細頁」
 * 使用者已永久拒絕權限時的引導入口。從這頁可以再點「權限」進入細項調整。
 * 適用：CAMERA、ACTIVITY_RECOGNITION、WRITE_STORAGE 等一般 runtime 權限。
 */
fun Context.openAppSettings() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
            addCategory(Intent.CATEGORY_DEFAULT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "openAppSettings failed")
        Toast.makeText(this, "無法開啟設定頁", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 0003008: 直接跳到本 App 的「通知設定頁」
 * 比 openAppSettings 更深一層 — 直達通知開關。
 * Android 8.0+ 適用。
 */
fun Context.openAppNotificationSettings() {
    try {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "openAppNotificationSettings failed; fallback to app details")
        openAppSettings()
    }
}

/**
 * 0003008: 跳到 Health Connect 設定，最終目的是讓使用者切換本 App 的權限。
 *
 * 嘗試順序（依不同 Android 版本 / HC 形式 fallback）：
 * 1. Android 14+ 系統 framework 的 per-app 權限管理 action
 * 2. AndroidX 的 per-app 權限管理 action（HC 獨立 App / 較新版本）
 * 3. HC 設定主畫面 action（使用者再點「應用程式權限」→ 找到本 App）
 * 4. 直接 launch HC 獨立 App
 * 5. fallback 到本 App 詳細頁
 */
fun Context.openHealthConnectSettings() {
    val candidates = listOf(
        // 1. Android 14+ 系統 HC framework — 直達 per-app 權限頁
        Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS").apply {
            putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
        },
        // 2. AndroidX HC（一些版本支援）
        Intent("androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS").apply {
            putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
        },
        // 3. HC 設定主畫面（使用者再點兩下到權限頁）
        Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"),
    )

    for (intent in candidates) {
        try {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                return
            }
        } catch (e: Exception) {
            Timber.w(e, "Try intent ${intent.action} failed")
        }
    }

    // 4. 直接 launch HC 獨立 App
    try {
        val launchIntent = packageManager.getLaunchIntentForPackage(Config.GOOGLE_HEALTH_CONNECT_PACKAGE_NAME)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(launchIntent)
            return
        }
    } catch (e: Exception) {
        Timber.w(e, "Launch HC app failed")
    }

    // 5. 最終 fallback：本 App 詳細頁
    Timber.i("Falling back to app details settings")
    openAppSettings()
}