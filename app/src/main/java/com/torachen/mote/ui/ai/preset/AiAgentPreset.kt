package com.torachen.mote.ui.ai.preset

import com.torachen.mote.ui.ai.AiFilePermission
import com.torachen.mote.ui.ai.AiReasoningLevel
import org.json.JSONArray
import org.json.JSONObject

/**
 * 一个 Agent 预设。
 *
 * 字段是“可继承”的：null 表示跟随全局/当前服务，非 null 表示该预设明确覆盖。
 * 这样内置“通用”预设可以保持跟随全局，而“研究/手机助手”等可以固定能力边界。
 */
internal data class AiAgentPreset(
    val id: String,
    val name: String,
    val description: String = "",
    val systemPrompt: String? = null,
    val providerId: String? = null,
    val modelId: String? = null,
    val reasoning: AiReasoningLevel? = null,
    /** null = 全部内置工具；emptySet = 不使用工具。 */
    val allowedTools: Set<String>? = null,
    /** null = 全部已启用 MCP；emptySet = 不使用 MCP。 */
    val mcpServerIds: Set<String>? = null,
    val filePermission: AiFilePermission? = null,
    val deviceControl: Boolean? = null,
    val contextTurns: Int? = null,
    val backgroundAllowed: Boolean = false,
    val builtIn: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

internal object AiPresetCodec {

    fun encodeList(
        presets: List<AiAgentPreset>,
        defaultId: String,
    ): String {
        val array = JSONArray()
        presets.forEach { array.put(encode(it)) }
        return JSONObject()
            .put("version", 1)
            .put("defaultId", defaultId)
            .put("presets", array)
            .toString()
    }

    fun decodeList(raw: String?): Pair<String?, List<AiAgentPreset>> {
        if (raw.isNullOrBlank()) return null to emptyList()
        val root = runCatching { JSONObject(raw) }.getOrNull() ?: return null to emptyList()
        val defaultId = root.optString("defaultId").takeIf { it.isNotBlank() }
        val array = root.optJSONArray("presets") ?: return defaultId to emptyList()
        val presets = buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                decode(obj)?.let(::add)
            }
        }
        return defaultId to presets
    }

    fun encode(preset: AiAgentPreset): JSONObject = JSONObject().apply {
        put("id", preset.id)
        put("name", preset.name)
        put("description", preset.description)
        preset.systemPrompt?.let { put("systemPrompt", it) }
        preset.providerId?.let { put("providerId", it) }
        preset.modelId?.let { put("modelId", it) }
        preset.reasoning?.let { put("reasoning", it.name) }
        preset.allowedTools?.let { put("allowedTools", JSONArray(it.toList())) }
        preset.mcpServerIds?.let { put("mcpServerIds", JSONArray(it.toList())) }
        preset.filePermission?.let { put("filePermission", it.name) }
        preset.deviceControl?.let { put("deviceControl", it) }
        preset.contextTurns?.let { put("contextTurns", it) }
        put("backgroundAllowed", preset.backgroundAllowed)
        put("builtIn", preset.builtIn)
        put("createdAt", preset.createdAt)
        put("updatedAt", preset.updatedAt)
    }

    fun decode(obj: JSONObject): AiAgentPreset? {
        val id = obj.optString("id").trim().ifBlank { return null }
        val name = obj.optString("name").trim().ifBlank { "未命名预设" }
        val allowedTools = obj.optJSONArray("allowedTools")?.let { array ->
            buildSet {
                for (index in 0 until array.length()) {
                    array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
        val mcpServerIds = obj.optJSONArray("mcpServerIds")?.let { array ->
            buildSet {
                for (index in 0 until array.length()) {
                    array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
        return AiAgentPreset(
            id = id,
            name = name,
            description = obj.optString("description").take(500),
            systemPrompt = obj.optString("systemPrompt").takeIf { it.isNotBlank() },
            providerId = obj.optString("providerId").takeIf { it.isNotBlank() },
            modelId = obj.optString("modelId").takeIf { it.isNotBlank() },
            reasoning = runCatching {
                AiReasoningLevel.valueOf(obj.optString("reasoning"))
            }.getOrNull(),
            allowedTools = allowedTools,
            mcpServerIds = mcpServerIds,
            filePermission = runCatching {
                AiFilePermission.valueOf(obj.optString("filePermission"))
            }.getOrNull(),
            deviceControl = if (obj.has("deviceControl") && !obj.isNull("deviceControl")) {
                obj.optBoolean("deviceControl")
            } else {
                null
            },
            contextTurns = if (obj.has("contextTurns") && !obj.isNull("contextTurns")) {
                obj.optInt("contextTurns", 12).coerceIn(0, 40)
            } else {
                null
            },
            backgroundAllowed = obj.optBoolean("backgroundAllowed", false),
            builtIn = obj.optBoolean("builtIn", false),
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
        )
    }
}
