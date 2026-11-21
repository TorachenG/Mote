package com.torachen.mote.ui.ai.preset

import android.content.Context
import com.torachen.mote.ui.ai.AiFilePermission
import com.torachen.mote.ui.ai.AiReasoningLevel
import com.torachen.mote.ui.ai.security.AiAtomicJsonFile
import java.io.File

/** Agent 预设的本地存储；内置预设固定 id，用户预设可编辑/删除。 */
internal object AiPresetStore {

    const val DEFAULT_ID = "builtin_default"

    private const val FILE_NAME = "ai_presets.json"
    private val lock = Any()
    private var loaded = false
    private val presets = mutableListOf<AiAgentPreset>()
    private var defaultIdValue = DEFAULT_ID

    private fun file(context: Context): File = File(context.applicationContext.filesDir, FILE_NAME)

    @Synchronized
    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val (storedDefault, stored) = AiPresetCodec.decodeList(
            AiAtomicJsonFile.read(file(context)),
        )
        presets.clear()
        presets.addAll(stored)
        var changed = stored.isEmpty()
        // 版本升级新增的内置预设自动补齐；旧版内置名称做一次轻量迁移。
        BUILT_INS.forEach { builtIn ->
            val index = presets.indexOfFirst { it.id == builtIn.id }
            if (index < 0) {
                presets.add(builtIn)
                changed = true
            } else {
                val existing = presets[index]
                if (existing.builtIn) {
                    var updated = existing
                    if (existing.name in LEGACY_NAMES[builtIn.id].orEmpty()) {
                        updated = updated.copy(name = builtIn.name)
                    }
                    if (existing.description in LEGACY_DESCRIPTIONS[builtIn.id].orEmpty()) {
                        updated = updated.copy(description = builtIn.description)
                    }
                    if (updated != existing) {
                        presets[index] = updated
                        changed = true
                    }
                }
            }
        }
        defaultIdValue = storedDefault?.takeIf { id -> presets.any { it.id == id } }
            ?: DEFAULT_ID
        if (changed) persistLocked(context)
    }

    fun all(context: Context): List<AiAgentPreset> = synchronized(lock) {
        ensureLoaded(context)
        // 内置预设在前，用户预设按更新时间倒序。
        val builtIns = presets.filter { it.builtIn }
        val users = presets.filterNot { it.builtIn }.sortedByDescending { it.updatedAt }
        builtIns + users
    }

    fun byId(id: String?): AiAgentPreset? = synchronized(lock) {
        if (id.isNullOrBlank()) null else presets.firstOrNull { it.id == id }
    }

    fun defaultId(): String = synchronized(lock) { defaultIdValue }

    fun defaultPreset(context: Context): AiAgentPreset? = synchronized(lock) {
        ensureLoaded(context)
        presets.firstOrNull { it.id == defaultIdValue } ?: presets.firstOrNull()
    }

    fun upsert(context: Context, preset: AiAgentPreset): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val now = System.currentTimeMillis()
        val existing = presets.firstOrNull { it.id == preset.id }
        val stored = preset.copy(
            createdAt = existing?.createdAt?.takeIf { it > 0L } ?: preset.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now,
        )
        if (existing != null) {
            presets[presets.indexOf(existing)] = stored
        } else {
            presets.add(stored)
        }
        persistLocked(context)
    }

    fun remove(context: Context, id: String): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val target = presets.firstOrNull { it.id == id } ?: return false
        if (target.builtIn) return false
        presets.remove(target)
        if (defaultIdValue == id) defaultIdValue = DEFAULT_ID
        persistLocked(context)
    }

    fun setDefault(context: Context, id: String): Boolean = synchronized(lock) {
        ensureLoaded(context)
        if (presets.none { it.id == id }) return false
        defaultIdValue = id
        persistLocked(context)
    }

    fun duplicate(context: Context, id: String): AiAgentPreset? = synchronized(lock) {
        ensureLoaded(context)
        val source = presets.firstOrNull { it.id == id } ?: return null
        val now = System.currentTimeMillis()
        val copy = source.copy(
            id = "preset_" + now.toString(36),
            name = source.name + " 副本",
            builtIn = false,
            createdAt = now,
            updatedAt = now,
        )
        presets.add(copy)
        persistLocked(context)
        copy
    }

    private fun persistLocked(context: Context): Boolean =
        AiAtomicJsonFile.write(
            file(context),
            AiPresetCodec.encodeList(presets.toList(), defaultIdValue),
        )

    private val LEGACY_NAMES: Map<String, Set<String>> = mapOf(
        DEFAULT_ID to setOf("默认"),
        "builtin_phone" to setOf("手机助手"),
        "builtin_minimal" to setOf("极简"),
    )

    private val LEGACY_DESCRIPTIONS: Map<String, Set<String>> = mapOf(
        DEFAULT_ID to setOf("跟随全局模型、提示词与权限"),
        "builtin_research" to setOf("联网搜索、读取网页，适合资料整理与事实核查"),
        "builtin_phone" to setOf("读屏、点击、输入和打开应用；敏感应用会自动拦截"),
        "builtin_coding" to setOf("查资料、读文档、生成 Office 文件；不自动操作手机"),
        "builtin_minimal" to setOf("不调用工具，只做纯文本问答，响应最快"),
    )

    private val BUILT_INS: List<AiAgentPreset> = listOf(
        AiAgentPreset(
            id = DEFAULT_ID,
            name = "通用",
            description = "跟随全局 AI 设置",
            builtIn = true,
        ),
        AiAgentPreset(
            id = "builtin_research",
            name = "研究",
            description = "搜索、读网页、整理资料",
            systemPrompt = "你是研究助手。先搜索再回答，区分事实、推测与来源；" +
                    "引用网页时保留可追溯链接，不要编造来源。",
            reasoning = AiReasoningLevel.High,
            allowedTools = setOf(
                "web_search", "fetch_page", "read_page", "open_url", "create_document",
            ),
            filePermission = AiFilePermission.ViewOnly,
            deviceControl = false,
            contextTurns = 20,
            builtIn = true,
        ),
        AiAgentPreset(
            id = "builtin_phone",
            name = "手机",
            description = "读屏并操作手机应用",
            systemPrompt = "你是手机操作助手。每次操作前先读屏，动作后等待并重新确认界面；" +
                    "遇到登录、验证码、支付、密码必须停下让用户本人完成。",
            reasoning = AiReasoningLevel.Standard,
            allowedTools = setOf(
                "open_app", "read_screen", "tap", "long_press", "swipe", "scroll",
                "input_text", "press_key", "wait", "read_page", "open_url",
            ),
            filePermission = AiFilePermission.ViewOnly,
            deviceControl = true,
            contextTurns = 12,
            builtIn = true,
        ),
        AiAgentPreset(
            id = "builtin_coding",
            name = "编程",
            description = "写代码、查文档、生成文件",
            systemPrompt = "你是严谨的软件工程助手。给出可运行的代码、关键参数和注意事项；" +
                    "不确定的 API 先搜索或读取文档，不要编造。",
            reasoning = AiReasoningLevel.High,
            allowedTools = setOf(
                "web_search", "fetch_page", "read_page", "open_url",
                "create_document", "edit_attachment",
            ),
            filePermission = AiFilePermission.EditWithConfirm,
            deviceControl = false,
            contextTurns = 20,
            builtIn = true,
        ),
        AiAgentPreset(
            id = "builtin_minimal",
            name = "纯聊天",
            description = "纯聊天，不调用工具",
            reasoning = AiReasoningLevel.Off,
            allowedTools = emptySet(),
            mcpServerIds = emptySet(),
            filePermission = AiFilePermission.ViewOnly,
            deviceControl = false,
            contextTurns = 6,
            builtIn = true,
        ),
    )
}
