package com.taiwanlife.teamwalk.remote.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Gson TypeAdapter for serializing and deserializing java.time.Instant objects.
 * Serializes Instant to ISO 8601 string format (e.g., "2025-06-30T15:37:34.000Z").
 */
class InstantAdapter : JsonSerializer<Instant>, JsonDeserializer<Instant> {

    // 當將 Instant 物件序列化為 JSON 字串時調用
    override fun serialize(
        src: Instant?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == null) {
            JsonPrimitive("") // 如果 Instant 為空，可以返回空字串或 JsonNull.INSTANCE
        } else {
            JsonPrimitive(src.toString()) // Instant 的 toString() 預設就是 ISO 8601 格式
        }
    }

    // 當將 JSON 字串反序列化為 Instant 物件時調用
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Instant {
        if (json == null || json.isJsonNull || json.asString.isBlank()) {
            // 如果 JSON 元素為空、null 或空白字串，返回 null
            return Instant.EPOCH // 或者您也可以拋出 JsonParseException，或返回 null (如果您的屬性是可空的)
        }
        try {
            return Instant.parse(json.asString) // 嘗試解析 ISO 8601 字串
        } catch (e: DateTimeParseException) {
            throw JsonParseException("無法解析 Instant 字串: ${json.asString}", e)
        }
    }
}