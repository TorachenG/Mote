package com.torachen.mote.ui.ai.preset

import android.content.Context
import com.torachen.mote.ui.ai.AiConversationSettings
import com.torachen.mote.ui.ai.AiFilePermission
import com.torachen.mote.ui.ai.AiProviderConfig
import com.torachen.mote.ui.ai.AiReasoningLevel
import com.torachen.mote.ui.ai.AiSettings

/** 解析后的、可直接交给 Agent 的一次对话配置。 */
internal data class ResolvedAiAgent(
    val presetId: String,
    val presetName: String,
    val provider: AiProviderConfig,
    val systemPrompt: String,
    val reasoning: AiReasoningLevel,
    val filePermission: AiFilePermission,
    val deviceControl: Boolean,
    val contextTurns: Int,
    val allowedTools: Set<String>?,
    val mcpServerIds: Set<String>?,
    val backgroundAllowed: Boolean,
)

internal object AiPresetResolver {

    fun resolve(context: Context, conversationId: String?): ResolvedAiAgent? {
        AiPresetStore.ensureLoaded(context)
        val preset = AiConversationSettings.presetFor(conversationId)
        val provider = AiConversationSettings.providerFor(conversationId) ?: return null
        val presetId = preset?.id ?: AiPresetStore.DEFAULT_ID
        val prompt = preset?.systemPrompt?.takeIf { it.isNotBlank() } ?: AiSettings.systemPrompt
        return ResolvedAiAgent(
            presetId = presetId,
            presetName = preset?.name ?: "通用",
            provider = provider,
            systemPrompt = prompt,
            reasoning = AiConversationSettings.reasoningFor(conversationId),
            filePermission = AiConversationSettings.permissionFor(conversationId),
            deviceControl = preset?.deviceControl ?: AiSettings.agentEnabled,
            contextTurns = AiConversationSettings.contextTurnsFor(conversationId),
            allowedTools = preset?.allowedTools,
            mcpServerIds = preset?.mcpServerIds,
            backgroundAllowed = preset?.backgroundAllowed ?: false,
        )
    }

    /** 任务没有对话上下文，按 presetId 直接解析。 */
    fun resolvePreset(context: Context, presetId: String): ResolvedAiAgent? {
        AiPresetStore.ensureLoaded(context)
        val preset = AiPresetStore.byId(presetId) ?: AiPresetStore.defaultPreset(context)
        val provider = preset?.providerId?.let { com.torachen.mote.ui.ai.AiProviderStore.byId(it) }
            ?: com.torachen.mote.ui.ai.AiProviderStore.active
            ?: return null
        val model = preset?.modelId?.takeIf { it.isNotBlank() } ?: provider.model
        val usableProvider = if (model != provider.model && provider.models.isNotEmpty() &&
            model !in provider.models
        ) {
            provider.copy(model = provider.model)
        } else {
            provider.copy(model = model)
        }
        return ResolvedAiAgent(
            presetId = preset?.id ?: AiPresetStore.DEFAULT_ID,
            presetName = preset?.name ?: "通用",
            provider = usableProvider,
            systemPrompt = preset?.systemPrompt?.takeIf { it.isNotBlank() } ?: AiSettings.systemPrompt,
            reasoning = preset?.reasoning ?: AiSettings.reasoningLevel,
            filePermission = preset?.filePermission
                ?: com.torachen.mote.ui.ai.AiPermissionStore.permission,
            deviceControl = preset?.deviceControl ?: AiSettings.agentEnabled,
            contextTurns = (preset?.contextTurns ?: AiSettings.contextTurns).coerceIn(0, 40),
            allowedTools = preset?.allowedTools,
            mcpServerIds = preset?.mcpServerIds,
            backgroundAllowed = preset?.backgroundAllowed ?: false,
        )
    }
}
