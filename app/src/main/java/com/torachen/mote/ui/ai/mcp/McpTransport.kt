package com.torachen.mote.ui.ai.mcp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.json.JSONObject
import java.io.Closeable

/**
 * MCP 传输抽象。
 *
 * request 负责发出一个带 id 的 JSON-RPC 请求并等待同 id 响应；
 * notify 只发送通知（如 notifications/initialized），不等待响应。
 */
internal interface McpChannel : Closeable {

    suspend fun start() {}

    suspend fun request(payload: JSONObject, timeoutMs: Long): JSONObject?

    suspend fun notify(payload: JSONObject)

    /** 服务端主动通知（tools/list_changed 等）；没有长连接的实现返回 emptyFlow。 */
    fun notifications(): Flow<JSONObject> = emptyFlow()

    var sessionId: String?

    var protocolVersion: String?

    /** 最近一次 stderr/诊断（stdio 用）。 */
    fun diagnostics(): String = ""

    override fun close()
}
