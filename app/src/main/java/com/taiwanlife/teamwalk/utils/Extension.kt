package com.taiwanlife.teamwalk.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.taiwanlife.teamwalk.remote.adapter.InstantAdapter
import com.taiwanlife.teamwalk.remote.adapter.ZoneOffsetAdapter
import java.time.Instant
import java.time.ZoneOffset

fun String.enableToBoolean(): Boolean {
    return equals("Y")
}

fun getGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter())
        .registerTypeAdapter(ZoneOffset::class.java, ZoneOffsetAdapter()) // 註冊 ZoneOffset 適配器
        .setPrettyPrinting()
        .create()
}