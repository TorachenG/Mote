package com.torachen.mote.ui.ai.mcp

/**
 * 增量 SSE 解析器。
 *
 * 按行 feed，空行触发一次事件；支持：

 * - `data:` 多行拼接（用 \n）
 * - `event:`、`id:`、`retry:`
 * - `:` 注释行
 * - CRLF / LF
 */
internal class McpSseParser {

    private var eventName: String = ""
    private val data = StringBuilder()
    private var hasData = false
    private var eventId: String? = null
    private var retryMs: Long? = null

    fun feed(line: String): McpSseEvent? {
        val normalized = line.removeSuffix("\r")
        if (normalized.isEmpty()) return dispatch()
        if (normalized.startsWith(":")) return null

        val colon = normalized.indexOf(':')
        val field = if (colon >= 0) normalized.substring(0, colon) else normalized
        var value = if (colon >= 0) normalized.substring(colon + 1) else ""
        if (value.startsWith(" ")) value = value.substring(1)

        when (field) {
            "event" -> eventName = value
            "data" -> {
                if (hasData) data.append('\n')
                data.append(value)
                hasData = true
            }

            "id" -> eventId = value
            "retry" -> retryMs = value.toLongOrNull()?.takeIf { it >= 0L }
        }
        return null
    }

    /** 流结束时把未以空行收尾的最后一个事件交出去。 */
    fun flush(): McpSseEvent? = dispatch()

    private fun dispatch(): McpSseEvent? {
        if (!hasData) {
            reset()
            return null
        }
        val event = McpSseEvent(
            event = eventName.ifBlank { "message" },
            data = data.toString(),
            id = eventId,
            retryMs = retryMs,
        )
        reset()
        return event
    }

    private fun reset() {
        eventName = ""
        data.setLength(0)
        hasData = false
        eventId = null
        retryMs = null
    }
}

internal data class McpSseEvent(
    val event: String,
    val data: String,
    val id: String? = null,
    val retryMs: Long? = null,
)
