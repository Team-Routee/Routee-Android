package com.routee.android.core.network.extension

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val PrettyJson = Json { prettyPrint = true }

/**
 * JSON 문자열이면 들여쓰기된 형태로, 아니면 원본을 그대로 반환한다
 */
internal fun String.toPrettyJsonOrSelf(): String {
    val trimmed = trim()
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return this
    return runCatching {
        PrettyJson.encodeToString(JsonElement.serializer(), PrettyJson.parseToJsonElement(trimmed))
    }.getOrDefault(this)
}
