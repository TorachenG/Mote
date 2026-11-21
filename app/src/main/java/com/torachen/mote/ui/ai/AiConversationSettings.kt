package com.torachen.mote.ui.ai

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.torachen.mote.ui.ai.preset.AiAgentPreset
import com.torachen.mote.ui.ai.preset.AiPresetStore
import org.json.JSONObject

/**
 * 每个对话独立选择的模型 / 文件权限 / 推理等级。
 *
 * 其它开关（联网搜索、自动操作、悬浮球、界面样式等）仍然是全局通用设置。
 * 新建对话时会从当前全局默认值复制一份；之后各对话互不影响。
 */
internal object AiConversationSettings {

    data class Config(
        val presetId: String? = null,
        val providerId: String? = null,
        val modelId: String? = null,
        val permission: AiFilePermission? = null,
        val reasoning: AiReasoningLevel? = null,
    )

    private const val KEY = "ai_conversation_settings"

    private var loaded = false
    private val configs = mutableStateMapOf<String, Config>()

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        AiPresetStore.ensureLoaded(context)
        val raw = aiPrefs(context).getString(KEY, null).orEmpty()
        if (raw.isBlank()) return
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { id ->
                val obj = json.optJSONObject(id) ?: return@forEach
                configs[id] = Config(
                    presetId = obj.optString("preset").takeIf { it.isNotBlank() },
                    providerId = obj.optString("provider").takeIf { it.isNotBlank() },
                    modelId = obj.optString("model").takeIf { it.isNotBlank() },
                    permission = runCatching {
                        AiFilePermission.valueOf(obj.optString("permission"))
                    }.getOrNull(),
                    reasoning = runCatching {
                        AiReasoningLevel.valueOf(obj.optString("reasoning"))
                    }.getOrNull(),
                )
            }
        }
    }

    /** 为新建对话绑定预设；历史对话保留旧的 provider/model 覆盖。 */
    fun ensureDefaults(
        context: Context,
        conversationId: String,
        presetId: String? = null,
    ) {
        ensureLoaded(context)
        val selected = presetId ?: AiPresetStore.defaultId()
        val existing = configs[conversationId]
        if (existing != null) {
            // 旧版本没有 preset 字段：补默认预设，但保留用户已选的 provider/model/权限。
            if (existing.presetId == null) {
                configs[conversationId] = existing.copy(presetId = selected)
                persist(context)
            }
            return
        }
        configs[conversationId] = Config(presetId = selected)
        persist(context)
    }

    /** 给还没有配置的历史对话补一份默认值，保证它们彼此独立。 */
    fun ensureDefaultsForAll(context: Context, conversationIds: Iterable<String>) {
        ensureLoaded(context)
        var changed = false
        conversationIds.forEach { id ->
            val existing = configs[id]
            if (existing == null) {
                configs[id] = Config(presetId = AiPresetStore.defaultId())
                changed = true
            } else if (existing.presetId == null) {
                configs[id] = existing.copy(presetId = AiPresetStore.defaultId())
                changed = true
            }
        }
        if (changed) persist(context)
    }

    fun configFor(conversationId: String?): Config? =
        conversationId?.let { configs[it] }

    fun presetFor(conversationId: String?): AiAgentPreset? {
        val config = configFor(conversationId)
        return AiPresetStore.byId(config?.presetId)
            ?: AiPresetStore.byId(AiPresetStore.defaultId())
    }

    fun providerFor(conversationId: String?): AiProviderConfig? {
        val config = conversationId?.let { configs[it] }
        val preset = presetFor(conversationId)
        val storedProvider = config?.providerId?.let { AiProviderStore.byId(it) }
        val presetProvider = preset?.providerId?.let { AiProviderStore.byId(it) }
        val provider = storedProvider ?: presetProvider ?: AiProviderStore.active ?: return null
        val modelId = config?.modelId?.takeIf { it.isNotBlank() }
            ?: preset?.modelId?.takeIf { it.isNotBlank() }
        // 模型可能已经在服务设置里被删除，失效时回退到该服务当前选择的模型。
        val usableModelId = modelId?.takeIf { id ->
            provider.models.isEmpty() || id in provider.models || id == provider.model
        }
        return if (usableModelId != null && usableModelId != provider.model) {
            provider.copy(model = usableModelId)
        } else {
            provider
        }
    }

    fun permissionFor(conversationId: String?): AiFilePermission =
        conversationId?.let { configs[it]?.permission }
            ?: presetFor(conversationId)?.filePermission
            ?: AiPermissionStore.permission

    fun reasoningFor(conversationId: String?): AiReasoningLevel =
        conversationId?.let { configs[it]?.reasoning }
            ?: presetFor(conversationId)?.reasoning
            ?: AiSettings.reasoningLevel

    fun contextTurnsFor(conversationId: String?): Int =
        (presetFor(conversationId)?.contextTurns ?: AiSettings.contextTurns).coerceIn(0, 40)

    fun setProvider(
        context: Context,
        conversationId: String?,
        provider: AiProviderConfig,
        modelId: String,
    ) {
        ensureLoaded(context)
        AiProviderStore.upsert(context, provider.copy(model = modelId), makeActive = true)
        val id = conversationId ?: return
        configs[id] = (configs[id] ?: Config(presetId = AiPresetStore.defaultId()))
            .copy(providerId = provider.id, modelId = modelId)
        persist(context)
    }

    /** 切换当前对话的预设；清空模型/权限覆盖，让新预设的能力边界真正生效。 */
    fun setPreset(context: Context, conversationId: String?, presetId: String) {
        ensureLoaded(context)
        val id = conversationId ?: return
        if (AiPresetStore.byId(presetId) == null) return
        configs[id] = (configs[id] ?: Config()).copy(
            presetId = presetId,
            providerId = null,
            modelId = null,
            permission = null,
            reasoning = null,
        )
        persist(context)
    }

    fun setPermission(
        context: Context,
        conversationId: String?,
        permission: AiFilePermission,
    ) {
        ensureLoaded(context)
        AiPermissionStore.setPermission(context, permission)
        val id = conversationId ?: return
        configs[id] = (configs[id] ?: Config(presetId = AiPresetStore.defaultId()))
            .copy(permission = permission)
        persist(context)
    }

    fun setReasoning(
        context: Context,
        conversationId: String?,
        level: AiReasoningLevel,
    ) {
        ensureLoaded(context)
        AiSettings.setReasoningLevel(context, level)
        val id = conversationId ?: return
        configs[id] = (configs[id] ?: Config(presetId = AiPresetStore.defaultId()))
            .copy(reasoning = level)
        persist(context)
    }

    fun remove(context: Context, conversationIds: Set<String>) {
        ensureLoaded(context)
        var changed = false
        conversationIds.forEach { id ->
            if (configs.remove(id) != null) changed = true
        }
        if (changed) persist(context)
    }

    fun clearAll(context: Context) {
        ensureLoaded(context)
        configs.clear()
        aiPrefs(context).edit().remove(KEY).apply()
    }

    private fun persist(context: Context) {
        runCatching {
            val json = JSONObject()
            configs.forEach { (id, config) ->
                json.put(
                    id,
                    JSONObject().apply {
                        config.presetId?.let { put("preset", it) }
                        config.providerId?.let { put("provider", it) }
                        config.modelId?.let { put("model", it) }
                        config.permission?.let { put("permission", it.name) }
                        config.reasoning?.let { put("reasoning", it.name) }
                    },
                )
            }
            aiPrefs(context).edit().putString(KEY, json.toString()).apply()
        }
    }
}
