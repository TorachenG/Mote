package com.torachen.mote.ui.ai.mcp

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal object McpErrors {

    fun http(code: Int, detail: String): McpException = when (code) {
        401, 403 -> McpException(McpFailureReason.Auth, "鉴权失败（HTTP $code）：请检查 Token / Header")
        404 -> McpException(McpFailureReason.Config, "MCP 地址不存在（HTTP 404）：请检查 URL 是否指向 /mcp")
        408 -> McpException(McpFailureReason.Timeout, "MCP 请求超时（HTTP 408）")
        429 -> McpException(McpFailureReason.RateLimit, "MCP 请求过于频繁（HTTP 429），请稍后重试")
        in 500..599 -> McpException(McpFailureReason.Server, "MCP 服务端错误（HTTP $code）")
        else -> McpException(
            McpFailureReason.Protocol,
            "MCP 请求失败（HTTP $code）：${detail.take(300)}",
        )
    }

    fun rpc(error: McpRpcError): McpException = when (error.code) {
        -32700, -32600, -32602 -> McpException(
            McpFailureReason.Protocol,
            "MCP 请求参数错误：${error.message.take(300)}",
            retryable = false,
        )

        -32601 -> McpException(
            McpFailureReason.Unsupported,
            "MCP 服务不支持该能力：${error.message.take(300)}",
            retryable = false,
        )

        -32000, -32001 -> McpException(
            McpFailureReason.ToolError,
            "MCP 工具执行失败：${error.message.take(300)}",
            retryable = false,
        )

        else -> McpException(
            McpFailureReason.Protocol,
            "MCP 协议错误（${error.code}）：${error.message.take(300)}",
            retryable = false,
        )
    }

    fun wrap(throwable: Throwable): McpException {
        if (throwable is McpException) return throwable
        return when (throwable) {
            is SocketTimeoutException -> McpException(
                McpFailureReason.Timeout,
                "MCP 请求超时",
                cause = throwable,
            )

            is UnknownHostException, is ConnectException -> McpException(
                McpFailureReason.Network,
                "无法连接 MCP 服务：${throwable.message ?: "网络不可用"}",
                cause = throwable,
            )

            is IOException -> McpException(
                McpFailureReason.Network,
                "MCP 网络错误：${throwable.message ?: "连接中断"}",
                cause = throwable,
            )

            else -> McpException(
                McpFailureReason.Protocol,
                "MCP 调用失败：${throwable.message ?: throwable.javaClass.simpleName}",
                cause = throwable,
            )
        }
    }

    /** 把已知 secret 值从诊断文本里移除，避免 Token 进入 UI/日志。 */
    fun redact(text: String, secrets: Collection<String>): String {
        var result = text
        secrets.filter { it.length >= 4 }.forEach { secret ->
            result = result.replace(secret, "[redacted]")
        }
        return result.take(8_000)
    }
}
