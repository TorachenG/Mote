package com.torachen.mote.ui.ai.mcp

import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong

/** JSON-RPC 2.0 的最小实现；MCP 只需要 request / notification / error。 */
internal object McpJsonRpc {

    private val ids = AtomicLong(1L)

    fun nextId(): Long = ids.getAndIncrement()

    fun request(id: Long, method: String, params: Any? = null): JSONObject =
        JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("method", method)
            if (params != null) put("params", params)
        }

    fun notification(method: String, params: Any? = null): JSONObject =
        JSONObject().apply {
            put("jsonrpc", "2.0")
            put("method", method)
            if (params != null) put("params", params)
        }

    fun result(id: Long, result: Any?): JSONObject =
        JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", result ?: JSONObject.NULL)
        }

    fun error(id: Long, code: Int, message: String, data: Any? = null): JSONObject =
        JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put(
                "error",
                JSONObject().apply {
                    put("code", code)
                    put("message", message)
                    if (data != null) put("data", data)
                },
            )
        }

    fun isResponse(obj: JSONObject): Boolean =
        obj.has("id") && !obj.isNull("id") && (obj.has("result") || obj.has("error"))

    fun isNotification(obj: JSONObject): Boolean =
        obj.has("method") && (!obj.has("id") || obj.isNull("id"))

    fun responseId(obj: JSONObject): Long? {
        if (!obj.has("id") || obj.isNull("id")) return null
        return when (val value = obj.opt("id")) {
            null -> null
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    fun errorOf(obj: JSONObject): McpRpcError? {
        val error = obj.optJSONObject("error") ?: return null
        return McpRpcError(
            code = error.optInt("code", -32603),
            message = error.optString("message").ifBlank { "MCP 协议错误" },
            data = error.opt("data").let { it as? JSONObject },
        )
    }

    fun resultOf(obj: JSONObject): JSONObject? = obj.optJSONObject("result")

    fun arrayResultOf(obj: JSONObject): JSONArray? = obj.optJSONArray("result")
}

internal data class McpRpcError(
    val code: Int,
    val message: String,
    val data: JSONObject? = null,
)
