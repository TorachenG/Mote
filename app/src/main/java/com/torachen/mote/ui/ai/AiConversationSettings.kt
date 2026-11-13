package com.torachen.mote.ui.ai

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONObject

/**
 * 每个对话独立选择的模型 / 文件权限 / 推理等级。
 *
 * 其它开关（联网搜索、自动操作、悬浮球、界面样式等）仍然是全局通用设置。
 * 新建对话时会从当前全局默认值复制一份；之后各对话互不影响。
 */
internal object AiConversationSettings {

    data class Config(
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
        val raw = aiPrefs(context).getString(KEY, null).orEmpty()
        if (raw.isBlank()) return
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { id ->
                val obj = json.optJSONObject(id) ?: return@forEach
                configs[id] = Config(
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

    /** 为新建的对话复制一份当前全局默认值。 */
    fun ensureDefaults(context: Context, conversationId: String) {
        ensureLoaded(context)
        if (configs.containsKey(conversationId)) return
        configs[conversationId] = currentDefaults()
        persist(context)
    }

    /** 给还没有配置的历史对话补一份默认值，保证它们彼此独立。 */
    fun ensureDefaultsForAll(context: Context, conversationIds: Iterable<String>) {
        ensureLoaded(context)
        var changed = false
        conversationIds.forEach { id ->
            if (!configs.containsKey(id)) {
                configs[id] = currentDefaults()
                changed = true
            }
        }
        if (changed) persist(context)
    }

    fun providerFor(conversationId: String?): AiProviderConfig? {
        val config = conversationId?.let { configs[it] }
        val storedProvider = config?.providerId?.let { AiProviderStore.byId(it) }
        val provider = storedProvider ?: AiProviderStore.active ?: return null
        val modelId = if (storedProvider != null) {
            config?.modelId?.takeIf { it.isNotBlank() }
        } else {
            null
        }
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
        conversationId?.let { configs[it]?.permission } ?: AiPermissionStore.permission

    fun reasoningFor(conversationId: String?): AiReasoningLevel =
        conversationId?.let { configs[it]?.reasoning } ?: AiSettings.reasoningLevel

    fun setProvider(
        context: Context,
        conversationId: String?,
        provider: AiProviderConfig,
        modelId: String,
    ) {
        ensureLoaded(context)
        AiProviderStore.upsert(context, provider.copy(model = modelId), makeActive = true)
        val id = conversationId ?: return
        configs[id] = (configs[id] ?: Config()).copy(providerId = provider.id, modelId = modelId)
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
        configs[id] = (configs[id] ?: Config()).copy(permission = permission)
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
        configs[id] = (configs[id] ?: Config()).copy(reasoning = level)
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

    private fun currentDefaults(): Config {
        val active = AiProviderStore.active
        return Config(
            providerId = active?.id,
            modelId = active?.model,
            permission = AiPermissionStore.permission,
            reasoning = AiSettings.reasoningLevel,
        )
    }

    private fun persist(context: Context) {
        runCatching {
            val json = JSONObject()
            configs.forEach { (id, config) ->
                json.put(
                    id,
                    JSONObject().apply {
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
