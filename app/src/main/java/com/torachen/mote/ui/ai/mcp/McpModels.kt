package com.torachen.mote.ui.ai.mcp

import org.json.JSONObject

/** MCP 传输方式。stdio 仅通过 Shizuku 的 shell 通道启用。 */
enum class McpTransport(val wireName: String) {
    StreamableHttp("streamable-http"),
    LegacySse("sse"),
    Stdio("stdio");

    companion object {
        fun fromWire(value: String): McpTransport? =
            entries.firstOrNull { it.wireName == value || it.name.equals(value, ignoreCase = true) }
    }
}

sealed interface McpAuth {
    data object None : McpAuth
    data class Bearer(val secretRef: String) : McpAuth
    data class Header(val name: String, val secretRef: String) : McpAuth
}

data class McpHeader(
    val name: String,
    val value: String = "",
    val secretRef: String? = null,
)

data class McpReconnect(
    val enabled: Boolean = true,
    val initialDelayMs: Long = 500L,
    val maxDelayMs: Long = 30_000L,
    val maxAttempts: Int = 10,
)

/** stdio server 的启动参数。命令本身不加密，env 里的密钥通过 secretRef 解析。 */
data class McpStdioConfig(
    val command: String,
    val args: List<String> = emptyList(),
    val env: Map<String, String> = emptyMap(),
    val envSecretRefs: Map<String, String> = emptyMap(),
    val cwd: String = "",
)

enum class McpToolPolicy {
    /** 每次调用都询问；后台任务中等价于拒绝。 */
    Ask,
    /** 用户已明确允许；后台仍受预设能力开关约束。 */
    Allow,
    /** 永远拒绝。 */
    Deny,
}

data class McpServerConfig(
    val id: String,
    val displayName: String,
    val namespace: String,
    val url: String,
    val transport: McpTransport = McpTransport.StreamableHttp,
    val auth: McpAuth = McpAuth.None,
    val headers: List<McpHeader> = emptyList(),
    val stdio: McpStdioConfig? = null,
    val enabled: Boolean = true,
    val toolCallTimeoutMs: Long = 60_000L,
    val startupTimeoutMs: Long = 30_000L,
    val reconnect: McpReconnect = McpReconnect(),
    val toolPolicies: Map<String, McpToolPolicy> = emptyMap(),
    val disabledTools: Set<String> = emptySet(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val isHttp: Boolean get() = transport != McpTransport.Stdio
}

data class McpServerInfo(
    val name: String = "",
    val version: String = "",
    val title: String = "",
)

data class McpToolAnnotations(
    val readOnlyHint: Boolean? = null,
    val destructiveHint: Boolean? = null,
    val idempotentHint: Boolean? = null,
    val openWorldHint: Boolean? = null,
) {
    val risky: Boolean get() = destructiveHint == true || readOnlyHint == false
}

data class McpToolDescriptor(
    val serverId: String,
    val namespace: String,
    val rawName: String,
    val publicName: String,
    val title: String,
    val description: String,
    val inputSchema: JSONObject,
    val outputSchema: JSONObject? = null,
    val annotations: McpToolAnnotations = McpToolAnnotations(),
)

data class McpInitializeResult(
    val protocolVersion: String,
    val serverInfo: McpServerInfo,
    val instructions: String,
    val capabilities: JSONObject,
)

sealed interface McpContentBlock {
    data class Text(val text: String) : McpContentBlock
    data class Image(val mimeType: String, val data: String) : McpContentBlock
    data class ResourceLink(val name: String, val uri: String) : McpContentBlock
    data class Audio(val mimeType: String) : McpContentBlock
    data class EmbeddedResource(val uri: String?) : McpContentBlock
    data class Unknown(val type: String) : McpContentBlock
}

data class McpResourceDescriptor(
    val uri: String,
    val name: String,
    val mimeType: String = "",
    val description: String = "",
)

data class McpCallResult(
    val content: List<McpContentBlock>,
    val structuredContent: JSONObject? = null,
    val isError: Boolean = false,
    val raw: JSONObject? = null,
) {
    /** 把 MCP content 转成模型可读文本；图片/音频单独由调用方处理。 */
    fun text(): String = content.joinToString("\n") { block ->
        when (block) {
            is McpContentBlock.Text -> block.text
            is McpContentBlock.ResourceLink -> "Resource link: ${block.name} (${block.uri})"
            is McpContentBlock.Image -> "[image ${block.mimeType}]"
            is McpContentBlock.Audio -> "[audio ${block.mimeType} unsupported]"
            is McpContentBlock.EmbeddedResource ->
                "[embedded resource ${block.uri ?: "(unknown)"} unsupported]"
            is McpContentBlock.Unknown -> "[unsupported MCP content type: ${block.type}]"
        }
    }.ifBlank { "(tool returned no model-visible content)" }
}

sealed interface McpServerStatus {
    data object Disabled : McpServerStatus
    data class Connecting(val attempt: Int = 0) : McpServerStatus
    data class Ready(
        val toolCount: Int,
        val connectedAt: Long,
        val serverInfo: McpServerInfo,
        val protocolVersion: String,
    ) : McpServerStatus

    data class Reconnecting(
        val attempt: Int,
        val maxAttempts: Int,
        val nextRetryAt: Long,
        val lastError: String,
    ) : McpServerStatus

    data class Failed(
        val reason: McpFailureReason,
        val message: String,
        val retryable: Boolean = true,
    ) : McpServerStatus
}

enum class McpFailureReason {
    Config,
    Auth,
    Network,
    Timeout,
    Protocol,
    Server,
    ToolError,
    RateLimit,
    Cancelled,
    Unsupported,
    ReconnectExhausted,
    ShizukuUnavailable,
}

data class McpProbeResult(
    val ok: Boolean,
    val serverInfo: McpServerInfo? = null,
    val protocolVersion: String = "",
    val toolNames: List<String> = emptyList(),
    val instructions: String = "",
    val durationMs: Long = 0L,
    val error: String? = null,
    val failureReason: McpFailureReason? = null,
)

/** MCP 层统一异常；调用方根据 kind 决定是否重试/展示。 */
class McpException(
    val kind: McpFailureReason,
    message: String,
    val retryable: Boolean = kind in setOf(
        McpFailureReason.Network,
        McpFailureReason.Timeout,
        McpFailureReason.Server,
        McpFailureReason.RateLimit,
    ),
    cause: Throwable? = null,
) : Exception(message, cause)
