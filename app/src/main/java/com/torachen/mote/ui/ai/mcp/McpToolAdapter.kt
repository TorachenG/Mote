package com.torachen.mote.ui.ai.mcp

import com.torachen.mote.ui.ai.AiImagePart
import com.torachen.mote.ui.ai.AiToolOutcome

/** 把 MCP 工具描述/结果适配成现有 Agent 的工具协议。 */
internal object McpToolAdapter {

    private const val MAX_RESULT_CHARS = 8_000
    private const val MAX_IMAGE_BASE64_CHARS = 2 * 1024 * 1024 * 4 / 3

    fun toSpec(
        descriptor: McpToolDescriptor,
        policy: McpToolPolicy,
        serverName: String,
    ): AiToolSpec = AiToolSpec(
        name = descriptor.publicName,
        title = descriptor.title,
        summary = descriptor.description.ifBlank { descriptor.title },
        params = McpSchema.summary(descriptor.inputSchema, 1_500),
        group = AiToolGroup.MCP,
        inputSchema = descriptor.inputSchema,
        source = AiToolSource.Mcp,
        requiresApproval = policy != McpToolPolicy.Allow,
        risky = descriptor.annotations.risky,
        untrustedOutput = true,
        serverId = descriptor.serverId,
        serverName = serverName,
        rawName = descriptor.rawName,
    )

    fun toOutcome(
        descriptor: McpToolDescriptor,
        result: McpCallResult,
        supportsVision: Boolean,
    ): AiToolOutcome {
        val body = buildString {
            append(result.text())
            result.structuredContent?.let { structured ->
                if (structured.length() > 0) {
                    append("\nstructuredContent:\n")
                    append(structured.toString().take(4_000))
                }
            }
        }.take(MAX_RESULT_CHARS)

        val wrapped = buildString {
            append("<untrusted_mcp_result server=\"")
            append(descriptor.namespace.take(64).replace('"', '\''))
            append("\" tool=\"")
            append(descriptor.rawName.take(80).replace('"', '\''))
            append("\">\n")
            append(body)
            append("\n</untrusted_mcp_result>")
        }

        val image = if (supportsVision) {
            result.content.filterIsInstance<McpContentBlock.Image>()
                .firstOrNull { isSupportedImage(it.mimeType) && it.data.length <= MAX_IMAGE_BASE64_CHARS }
                ?.let { AiImagePart(mimeType = it.mimeType, base64 = it.data) }
        } else {
            null
        }

        return AiToolOutcome(
            text = wrapped,
            title = if (result.isError) {
                "MCP 工具失败：${descriptor.title}"
            } else {
                "已调用 MCP：${descriptor.title}"
            },
            failed = result.isError,
            image = image,
        )
    }

    private fun isSupportedImage(mimeType: String): Boolean = mimeType.lowercase() in setOf(
        "image/png",
        "image/jpeg",
        "image/webp",
        "image/gif",
    )
}
