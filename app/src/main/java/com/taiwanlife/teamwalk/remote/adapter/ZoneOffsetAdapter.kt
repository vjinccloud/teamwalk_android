package com.taiwanlife.teamwalk.remote.adapter

import androidx.annotation.Keep
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.ZoneOffset
import java.time.format.DateTimeParseException


@Keep
class ZoneOffsetAdapter : JsonSerializer<ZoneOffset>, JsonDeserializer<ZoneOffset> {
    override fun serialize(
        src: ZoneOffset?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == null) {
            JsonPrimitive("")
        } else {
            JsonPrimitive(src.id) // ZoneOffset 的 ID 格式如 "+08:00" 或 "Z"
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ZoneOffset {
        if (json == null || json.isJsonNull || json.asString.isBlank()) {
            return ZoneOffset.UTC // 或其他預設值，或拋出異常
        }
        try {
            return ZoneOffset.of(json.asString)
        } catch (e: DateTimeParseException) {
            throw JsonParseException("無法解析 ZoneOffset 字串: ${json.asString}", e)
        }
    }
}