package com.taiwanlife.teamwalk.utils

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

fun SleepSessionRecord.toTeamWalkRecord(): TeamWalkRecordModel {
    val zoneId =
        endZoneOffset?.let { ZoneOffset.ofTotalSeconds(it.totalSeconds) } ?: ZoneId.of("UTC")
    val startMillis = startTime.toEpochMilli().toString()
    val endMillis = endTime.toEpochMilli().toString()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"))
    val localFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd").withLocale(Locale.US).withZone(zoneId)

    val duration = (Duration.between(startTime, endTime).toMillis() / 1000) // 總睡眠時間（秒）

    return TeamWalkRecordModel(
        startTimestamp = startMillis,
        endTimestamp = endMillis,
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = duration
    )
}

fun StepsRecord.toTeamWalkRecord(): TeamWalkRecordModel {
    val zoneId = endZoneOffset?.let { ZoneId.ofOffset("UTC", it) } ?: ZoneId.of("UTC")

    val startMillis = startTime.toEpochMilli()
    val endMillis = endTime.toEpochMilli()

    val utcFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"))

    val localFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        .withLocale(Locale.US)
        .withZone(zoneId)

    return TeamWalkRecordModel(
        startTimestamp = startMillis.toString(),
        endTimestamp = endMillis.toString(),
        utcDate = utcFormatter.format(startTime),
        localDate = localFormatter.format(startTime),
        data = count // 使用者的步數
    )
}