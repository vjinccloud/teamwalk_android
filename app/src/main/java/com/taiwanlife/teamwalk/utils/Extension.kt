package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config.SHOW_DEBUG_TOAST
import com.taiwanlife.teamwalk.remote.adapter.InstantAdapter
import com.taiwanlife.teamwalk.remote.adapter.ZoneOffsetAdapter
import com.taiwanlife.teamwalk.ui.common.model.TeamWalkRecordModel
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

fun Uri.toOrigin():String {
    return "${scheme}://${host}"
}