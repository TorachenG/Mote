package com.torachen.mote.ui.ai.mcp

import org.json.JSONArray
import org.json.JSONObject

/**
 * MCP inputSchema 的本地子集校验。
 *
 * 目标不是实现完整 JSON Schema，而是在调用远端前挡住最常见的参数错误；
 * 遇到 $ref/allOf 等未支持关键字时跳过本地校验，让 server 给出权威错误。
 */
internal object McpSchema {

    fun validate(schema: JSONObject?, args: JSONObject): String? {
        if (schema == null) return null
        return validateNode(schema, args, "arguments", 0)
    }

    /** 给模型看的 schema 摘要；超长时只保留 required + properties 名称/类型。 */
    fun summary(schema: JSONObject?, maxChars: Int = 2_000): String {
        if (schema == null) return "{}"
        val raw = schema.toString()
        if (raw.length <= maxChars) return raw
        val properties = schema.optJSONObject("properties")
        val required = schema.optJSONArray("required")
        val builder = StringBuilder("{\"type\":\"object\"")
        if (required != null && required.length() > 0) {
            builder.append(",\"required\":").append(required.toString())
        }
        if (properties != null) {
            builder.append(",\"properties\":{")
            val keys = properties.keys().asSequence().take(24).toList()
            keys.forEachIndexed { index, key ->
                if (index > 0) builder.append(',')
                val node = properties.optJSONObject(key)
                builder.append(JSONObject.quote(key)).append(":{")
                builder.append("\"type\":").append(JSONObject.quote(node?.optString("type").orEmpty()))
                node?.optString("description")?.take(80)?.takeIf { it.isNotBlank() }?.let {
                    builder.append(",\"description\":").append(JSONObject.quote(it))
                }
                builder.append('}')
            }
            if (properties.length() > keys.size) builder.append(",\"…\":\"…\"")
            builder.append('}')
        }
        builder.append('}')
        return builder.toString().take(maxChars)
    }

    private fun validateNode(
        schema: JSONObject,
        value: Any?,
        path: String,
        depth: Int,
    ): String? {
        if (depth > 8) return null
        if (schema.has("\$ref") || schema.has("allOf")) return null

        val types = when (val type = schema.opt("type")) {
            null -> emptyList()
            is String -> listOf(type)
            is JSONArray -> buildList {
                for (index in 0 until type.length()) add(type.optString(index))
            }

            else -> emptyList()
        }
        if (types.isNotEmpty() && types.none { matchesType(it, value) }) {
            return "$path 类型应为 ${types.joinToString("/")}，实际为 ${typeName(value)}"
        }

        schema.optJSONArray("enum")?.let { enum ->
            var found = false
            for (index in 0 until enum.length()) {
                if (jsonEquals(enum.opt(index), value)) {
                    found = true
                    break
                }
            }
            if (!found) return "$path 必须是枚举值之一：${enum.toString().take(300)}"
        }

        schema.optJSONArray("oneOf")?.let { oneOf ->
            var matches = 0
            for (index in 0 until oneOf.length()) {
                val child = oneOf.optJSONObject(index) ?: continue
                if (validateNode(child, value, path, depth + 1) == null) matches++
            }
            if (matches != 1) return "$path 必须匹配 oneOf 中的一个 schema（当前匹配 $matches 个）"
        }

        return when {
            value is JSONObject -> validateObject(schema, value, path, depth)
            value is JSONArray -> validateArray(schema, value, path, depth)
            value is String -> validateString(schema, value, path)
            value is Number -> validateNumber(schema, value, path)
            else -> null
        }
    }

    private fun validateObject(
        schema: JSONObject,
        value: JSONObject,
        path: String,
        depth: Int,
    ): String? {
        val properties = schema.optJSONObject("properties")
        val required = schema.optJSONArray("required")
        if (required != null) {
            for (index in 0 until required.length()) {
                val key = required.optString(index)
                if (key.isBlank()) continue
                if (!value.has(key) || value.isNull(key)) return "$path.$key 是必填参数"
            }
        }
        val additional = schema.opt("additionalProperties")
        if (additional == false && properties != null) {
            val allowed = properties.keys().asSequence().toSet()
            for (key in value.keys()) {
                if (key !in allowed) return "$path.$key 不是 schema 允许的参数"
            }
        }
        if (properties != null) {
            for (key in properties.keys()) {
                if (!value.has(key) || value.isNull(key)) continue
                val child = properties.optJSONObject(key) ?: continue
                validateNode(child, value.opt(key), "$path.$key", depth + 1)?.let { return it }
            }
        }
        return null
    }

    private fun validateArray(
        schema: JSONObject,
        value: JSONArray,
        path: String,
        depth: Int,
    ): String? {
        schema.optInt("minItems", -1).takeIf { it >= 0 }?.let {
            if (value.length() < it) return "$path 至少需要 $it 项"
        }
        schema.optInt("maxItems", -1).takeIf { it >= 0 }?.let {
            if (value.length() > it) return "$path 最多允许 $it 项"
        }
        val itemSchema = schema.optJSONObject("items") ?: return null
        for (index in 0 until value.length()) {
            validateNode(itemSchema, value.opt(index), "$path[$index]", depth + 1)?.let { return it }
        }
        return null
    }

    private fun validateString(schema: JSONObject, value: String, path: String): String? {
        schema.optInt("minLength", -1).takeIf { it >= 0 }?.let {
            if (value.length < it) return "$path 长度至少为 $it"
        }
        schema.optInt("maxLength", -1).takeIf { it >= 0 }?.let {
            if (value.length > it) return "$path 长度不能超过 $it"
        }
        val pattern = schema.optString("pattern")
        if (pattern.isNotBlank() && runCatching { Regex(pattern).containsMatchIn(value) }.getOrDefault(true).not()) {
            return "$path 不符合 pattern"
        }
        return null
    }

    private fun validateNumber(schema: JSONObject, value: Number, path: String): String? {
        val number = value.toDouble()
        if (schema.has("minimum") && number < schema.optDouble("minimum")) {
            return "$path 不能小于 ${schema.opt("minimum")}"
        }
        if (schema.has("maximum") && number > schema.optDouble("maximum")) {
            return "$path 不能大于 ${schema.opt("maximum")}"
        }
        return null
    }

    private fun matchesType(type: String, value: Any?): Boolean = when (type) {
        "object" -> value is JSONObject
        "array" -> value is JSONArray
        "string" -> value is String
        "integer" -> value is Number && value.toDouble() == value.toLong().toDouble()
        "number" -> value is Number
        "boolean" -> value is Boolean
        "null" -> value == null || value == JSONObject.NULL
        else -> true
    }

    private fun typeName(value: Any?): String = when (value) {
        null, JSONObject.NULL -> "null"
        is JSONObject -> "object"
        is JSONArray -> "array"
        is String -> "string"
        is Boolean -> "boolean"
        is Number -> "number"
        else -> value.javaClass.simpleName
    }

    private fun jsonEquals(left: Any?, right: Any?): Boolean {
        if (left is Number && right is Number) return left.toDouble() == right.toDouble()
        if (left == JSONObject.NULL) return right == null || right == JSONObject.NULL
        if (right == JSONObject.NULL) return left == null
        return left == right
    }
}
