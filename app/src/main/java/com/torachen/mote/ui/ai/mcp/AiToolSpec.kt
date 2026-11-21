package com.torachen.mote.ui.ai.mcp

import org.json.JSONObject

internal enum class AiToolGroup {
    WEB,
    BROWSER,
    APP,
    DOCUMENT,
    FILE,
    DEVICE,
    MCP,
    OTHER,
}

internal enum class AiToolSource {
    BuiltIn,
    Mcp,
    Plugin,
    Remote,
}

/**
 * 面向模型的工具描述。
 *
 * 内置工具继续用 params 文本；MCP/插件工具可以带 inputSchema，
 * 由本地校验器在调用前检查参数。
 */
internal data class AiToolSpec(
    val name: String,
    val title: String,
    val summary: String,
    val params: String,
    val group: AiToolGroup,
    val needsWeb: Boolean = false,
    val needsDevice: Boolean = false,
    val inputSchema: JSONObject? = null,
    val source: AiToolSource = AiToolSource.BuiltIn,
    val requiresApproval: Boolean = false,
    val risky: Boolean = false,
    val untrustedOutput: Boolean = false,
    val serverId: String? = null,
    val serverName: String = "",
    val rawName: String? = null,
)

/** 一次需要用户确认的工具调用（当前用于 MCP 工具）。 */
internal data class AiToolApprovalRequest(
    val serverId: String?,
    val serverName: String,
    val toolName: String,
    val description: String,
    val argumentsPreview: String,
    val risky: Boolean,
)

internal enum class AiToolApprovalDecision {
    Deny,
    AllowOnce,
    AllowAlways,
    DenyAlways,
}
