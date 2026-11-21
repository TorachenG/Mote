@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.os.SystemClock
import android.widget.Toast
import com.torachen.mote.ui.ai.mcp.AiToolApproval
import com.torachen.mote.ui.ai.preset.AiPresetResolver
import com.torachen.mote.ui.ai.preset.AiPresetStore
import com.torachen.mote.ui.ai.usage.AiUsageRecorder
import com.torachen.mote.ui.ai.security.AiProviderSecretMigrator
import com.torachen.mote.ui.ai.security.AiSecretStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.torachen.mote.MoteAutoSync
import com.torachen.mote.service.MoteAccessibilityService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * AI 相关数据的本机持久化与运行时状态。
 *
 * 全部使用项目既有的 `mote_prefs`，不新增数据库、不引入序列化依赖。
 */

internal const val AI_PREFS_NAME = "mote_prefs"

internal fun aiPrefs(context: Context): SharedPreferences =
    context.getSharedPreferences(AI_PREFS_NAME, Context.MODE_PRIVATE)

// ============================================================
// 服务列表
// ============================================================

object AiProviderStore {

    private const val KEY_PROVIDERS = "ai_providers"
    private const val KEY_ACTIVE_PROVIDER = "ai_active_provider"

    private var loaded = false

    /** 已保存的服务（可观察） */
    val providers = mutableStateListOf<AiProviderConfig>()

    /** 当前使用的服务 id（可观察） */
    var activeId by mutableStateOf<String?>(null)
        private set

    val active: AiProviderConfig?
        get() = providers.firstOrNull { it.id == activeId }

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        // 先把旧版明文 Key 迁到 Keystore；失败时仍可走 decodeProviders 的明文回退，
        // 保证升级后本次仍能正常请求，下次启动继续重试迁移。
        AiProviderSecretMigrator.migrate(context)
        val prefs = aiPrefs(context)
        providers.clear()
        providers.addAll(
            decodeProviders(context, prefs.getString(KEY_PROVIDERS, null)).map { provider ->
                val missingModel = provider.model.isBlank() || provider.model !in provider.models
                val legacyDeepSeekName = provider.presetId == "deepseek" &&
                        provider.model == "deepseek-chat" &&
                        provider.models.any { it != "deepseek-chat" }
                if (provider.models.isNotEmpty() && (missingModel || legacyDeepSeekName)) {
                    provider.copy(
                        model = preferredModelId(provider.models, provider.presetId),
                    )
                } else {
                    provider
                }
            },
        )
        val savedActive = prefs.getString(KEY_ACTIVE_PROVIDER, null)
        activeId = savedActive?.takeIf { id -> providers.any { it.id == id } }
            ?: providers.firstOrNull()?.id
    }

    fun byId(id: String?): AiProviderConfig? =
        providers.firstOrNull { it.id == id }

    /** 新增或更新一个服务；默认同时设为当前服务。 */
    fun upsert(context: Context, config: AiProviderConfig, makeActive: Boolean = true) {
        val index = providers.indexOfFirst { it.id == config.id }
        val existing = providers.getOrNull(index)
        val ref = config.keyRef
            ?: existing?.keyRef
            ?: "provider:${config.id}:apiKey"
        // 编辑服务时表单可能只提交了非密钥字段；密钥为空时保留已经保存的 Key。
        val effectiveKey = config.apiKey.ifBlank { existing?.apiKey.orEmpty() }
        val secured = effectiveKey.isBlank() || AiSecretStore.put(context, ref, effectiveKey)
        val stored = config.copy(
            apiKey = effectiveKey,
            keyRef = if (secured) ref else config.keyRef,
        )
        if (index >= 0) {
            providers[index] = stored
        } else {
            providers.add(stored)
        }
        if (makeActive || activeId == null) {
            activeId = stored.id
        }
        persist(context)
        AiFloatingBubbleService.refreshIcon()
    }

    fun remove(context: Context, id: String) {
        val index = providers.indexOfFirst { it.id == id }
        if (index < 0) return
        val removed = providers.removeAt(index)
        removed.keyRef?.let { AiSecretStore.delete(context, it) }
        if (activeId == id) {
            activeId = providers.firstOrNull()?.id
        }
        persist(context)
        AiFloatingBubbleService.refreshIcon()
    }

    fun setActive(context: Context, id: String) {
        if (providers.none { it.id == id }) return
        activeId = id
        aiPrefs(context).edit().putString(KEY_ACTIVE_PROVIDER, id).apply()
        AiFloatingBubbleService.refreshIcon()
    }

    fun newCustomId(): String = "custom_" + System.currentTimeMillis().toString(36)

    private fun persist(context: Context) {
        // 兜底：任何没有 ref 的旧对象，在第一次写入时补一个 Keystore ref。
        // 如果 Keystore 不可用，宁可不写配置，也不把明文 Key 落盘。
        var canPersist = true
        providers.indices.forEach { index ->
            val provider = providers[index]
            if (provider.keyRef.isNullOrBlank() && provider.apiKey.isNotBlank()) {
                val ref = "provider:${provider.id}:apiKey"
                if (AiSecretStore.put(context, ref, provider.apiKey)) {
                    providers[index] = provider.copy(keyRef = ref)
                } else {
                    canPersist = false
                }
            }
        }
        if (!canPersist) return
        aiPrefs(context).edit()
            .putString(KEY_PROVIDERS, encodeProviders(providers))
            .putString(KEY_ACTIVE_PROVIDER, activeId.orEmpty())
            .apply()
    }

    private fun encodeProviders(list: List<AiProviderConfig>): String {
        val array = JSONArray()
        list.forEach { provider ->
            array.put(
                JSONObject().apply {
                    put("id", provider.id)
                    put("presetId", provider.presetId.orEmpty())
                    put("name", provider.name)
                    put("protocol", provider.protocol.name)
                    put("baseUrl", provider.baseUrl)
                    // 永远不写 apiKey；只写 Keystore 引用。
                    provider.keyRef?.takeIf { it.isNotBlank() }?.let { put("apiKeyRef", it) }
                    put("model", provider.model)
                    put("supportsVision", provider.supportsVision)
                    put("supportsReasoning", provider.supportsReasoning)
                    put("models", JSONArray(provider.models))
                    put(
                        "modelAliases",
                        JSONObject().apply {
                            provider.modelAliases.forEach { (id, alias) -> put(id, alias) }
                        },
                    )
                    put(
                        "modelSpecs",
                        JSONObject().apply {
                            provider.modelSpecs.forEach { (id, spec) ->
                                put(
                                    id,
                                    JSONObject().apply {
                                        put("context", spec.contextWindow)
                                        put("maxOutput", spec.maxOutputTokens)
                                        put("text", spec.supportsText)
                                        put("image", spec.supportsImage)
                                    },
                                )
                            }
                        },
                    )
                },
            )
        }
        return array.toString()
    }

    private fun decodeProviders(context: Context, raw: String?): List<AiProviderConfig> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
                    val presetId = obj.optString("presetId").takeIf { it.isNotBlank() }
                    val keyRef = obj.optString("apiKeyRef").takeIf { it.isNotBlank() }
                    // 迁移未完成或 Keystore 失效时，回退到旧明文字段，避免升级后服务直接不可用。
                    val apiKey = keyRef?.let { AiSecretStore.get(context, it) }
                        ?: obj.optString("apiKey")
                    val preset = AiPresets.byId(presetId)
                    val protocol = runCatching {
                        AiProtocol.valueOf(obj.optString("protocol"))
                    }.getOrDefault(preset?.protocol ?: AiProtocol.OpenAi)
                    val models = mutableListOf<String>()
                    obj.optJSONArray("models")?.let { modelArray ->
                        for (i in 0 until modelArray.length()) {
                            modelArray.optString(i).takeIf { it.isNotBlank() }?.let(models::add)
                        }
                    }
                    val aliases = mutableMapOf<String, String>()
                    obj.optJSONObject("modelAliases")?.let { aliasObject ->
                        aliasObject.keys().forEach { key ->
                            val value = aliasObject.optString(key)
                            if (value.isNotBlank()) aliases[key] = value
                        }
                    }
                    val specs = mutableMapOf<String, AiModelSpec>()
                    obj.optJSONObject("modelSpecs")?.let { specObject ->
                        specObject.keys().forEach { key ->
                            val specJson = specObject.optJSONObject(key) ?: return@forEach
                            specs[key] = AiModelSpec(
                                contextWindow = specJson.optInt("context", 1_048_576),
                                maxOutputTokens = specJson.optInt("maxOutput", 131_072),
                                supportsText = specJson.optBoolean("text", true),
                                supportsImage = specJson.optBoolean(
                                    "image",
                                    preset?.supportsVision == true,
                                ),
                            )
                        }
                    }
                    add(
                        AiProviderConfig(
                            id = id,
                            presetId = presetId,
                            name = obj.optString("name").ifBlank { preset?.name ?: "AI 服务" },
                            protocol = protocol,
                            baseUrl = obj.optString("baseUrl").ifBlank { preset?.baseUrl.orEmpty() },
                            apiKey = apiKey,
                            model = obj.optString("model"),
                            models = models,
                            supportsVision = obj.optBoolean("supportsVision", preset?.supportsVision == true),
                            supportsReasoning = obj.optBoolean(
                                "supportsReasoning",
                                preset?.supportsReasoning == true,
                            ),
                            modelAliases = aliases,
                            modelSpecs = specs,
                            keyRef = keyRef,
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}

// ============================================================
// 设置
// ============================================================

object AiSettings {

    private const val KEY_SYSTEM_PROMPT = "ai_system_prompt"
    private const val KEY_CONTEXT_TURNS = "ai_context_turns"
    private const val KEY_STREAM = "ai_stream_enabled"
    private const val KEY_VISION = "ai_vision_enabled"
    private const val KEY_MAX_FILE_CHARS = "ai_max_file_chars"
    private const val KEY_SHOW_THINKING = "ai_show_thinking"
    private const val KEY_AGENT_ENABLED = "ai_agent_enabled"
    private const val KEY_TITLE_FROM_CONTENT = "ai_title_from_content"
    private const val KEY_NEW_CONVERSATION_ON_OPEN = "ai_new_conversation_on_open"
    private const val KEY_WEB_SEARCH = "ai_web_search_enabled"
    private const val KEY_REASONING_LEVEL = "ai_reasoning_level"

    val DEFAULT_SYSTEM_PROMPT: String = """
        你是 Mote AI，Mote 浏览器内置的智能助手，面向中文用户提供专业、准确、可靠的回答。

        ## 回答原则
        - 先理解用户真正的问题、上下文和目的，再组织答案；先给出结论，再解释依据、步骤和例外情况。
        - 优先保证准确和完整，不必刻意追求简短；用清晰的标题、列表、表格和代码块让信息易读。
        - 不确定的内容要明确说明，不编造事实、数据、来源或链接；涉及新闻、价格、赛程等实时信息时，先搜索核对再回答。
        - 引用网页内容时保留来源链接，并区分事实、推测和个人建议。
        - 代码和技术方案给出可运行的示例、关键参数和注意事项。
        - 涉及医疗、法律、金融等高风险话题时保持谨慎，说明适用条件和风险，建议用户咨询专业人士。

        ## 表达方式
        - 使用简体中文，语气自然、专业、克制，不堆砌营销话术，不滥用表情。
        - 复杂问题分点说明；比较用表格；流程用步骤；代码用代码块。
        - 默认使用 Markdown：标题、列表、表格、引用、代码块、公式和 Mermaid 图表。
        - 可以指出用户问题中的歧义或风险，但不要过度说教。

        ## 工具与能力
        - 可以联网搜索、打开网页、打开应用、生成 Word/Excel/PPT；用户开启“自动操作”后还可以读屏、点击、滑动和输入。
        - 需要工具时自主调用；不要凭猜测回答手机屏幕、网页或文件里的内容。
        - 生成文件后告知保存位置，用户可以在对话底部查看和打开。

        ## 边界
        - 不代替用户完成支付、转账、输入密码或验证码等敏感操作。
        - 不伪造工具执行结果；工具失败时说明原因并给出替代方案。
        - 用户提供文件时先理解文件再回答，需要修改时遵守当前文件权限。
    """.trimIndent()

    private var loaded = false

    var systemPrompt by mutableStateOf(DEFAULT_SYSTEM_PROMPT)
        private set
    var contextTurns by mutableIntStateOf(12)
        private set
    var streamEnabled by mutableStateOf(true)
        private set
    var visionEnabled by mutableStateOf(true)
        private set
    var maxFileChars by mutableIntStateOf(60_000)
        private set
    var showThinking by mutableStateOf(true)
        private set
    var agentEnabled by mutableStateOf(false)
        private set
    var titleFromContent by mutableStateOf(true)
        private set
    var newConversationOnOpen by mutableStateOf(false)
        private set
    var webSearchEnabled by mutableStateOf(true)
        private set
    var reasoningLevel by mutableStateOf(AiReasoningLevel.Standard)
        private set

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = aiPrefs(context)
        systemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT)
            ?.takeIf { it.isNotBlank() } ?: DEFAULT_SYSTEM_PROMPT
        contextTurns = prefs.getInt(KEY_CONTEXT_TURNS, 12).coerceIn(0, 40)
        streamEnabled = prefs.getBoolean(KEY_STREAM, true)
        visionEnabled = prefs.getBoolean(KEY_VISION, true)
        maxFileChars = prefs.getInt(KEY_MAX_FILE_CHARS, 60_000).coerceIn(4_000, 400_000)
        showThinking = prefs.getBoolean(KEY_SHOW_THINKING, true)
        agentEnabled = prefs.getBoolean(KEY_AGENT_ENABLED, false)
        titleFromContent = prefs.getBoolean(KEY_TITLE_FROM_CONTENT, true)
        newConversationOnOpen = prefs.getBoolean(KEY_NEW_CONVERSATION_ON_OPEN, false)
        webSearchEnabled = prefs.getBoolean(KEY_WEB_SEARCH, true)
        reasoningLevel = AiReasoningLevel.entries.firstOrNull {
            it.name == prefs.getString(KEY_REASONING_LEVEL, null)
        } ?: AiReasoningLevel.Standard
    }

    fun setSystemPrompt(context: Context, value: String) {
        systemPrompt = value.ifBlank { DEFAULT_SYSTEM_PROMPT }
        aiPrefs(context).edit().putString(KEY_SYSTEM_PROMPT, systemPrompt).apply()
    }

    fun setContextTurns(context: Context, value: Int) {
        contextTurns = value.coerceIn(0, 40)
        aiPrefs(context).edit().putInt(KEY_CONTEXT_TURNS, contextTurns).apply()
    }

    fun setStreamEnabled(context: Context, value: Boolean) {
        streamEnabled = value
        aiPrefs(context).edit().putBoolean(KEY_STREAM, value).apply()
    }

    fun setVisionEnabled(context: Context, value: Boolean) {
        visionEnabled = value
        aiPrefs(context).edit().putBoolean(KEY_VISION, value).apply()
    }

    fun setMaxFileChars(context: Context, value: Int) {
        maxFileChars = value.coerceIn(4_000, 400_000)
        aiPrefs(context).edit().putInt(KEY_MAX_FILE_CHARS, maxFileChars).apply()
    }

    fun setShowThinking(context: Context, value: Boolean) {
        showThinking = value
        aiPrefs(context).edit().putBoolean(KEY_SHOW_THINKING, value).apply()
    }

    fun setAgentEnabled(context: Context, value: Boolean) {
        agentEnabled = value
        aiPrefs(context).edit().putBoolean(KEY_AGENT_ENABLED, value).apply()
    }

    fun setTitleFromContent(context: Context, value: Boolean) {
        titleFromContent = value
        aiPrefs(context).edit().putBoolean(KEY_TITLE_FROM_CONTENT, value).apply()
    }

    fun setNewConversationOnOpen(context: Context, value: Boolean) {
        newConversationOnOpen = value
        aiPrefs(context).edit().putBoolean(KEY_NEW_CONVERSATION_ON_OPEN, value).apply()
    }

    fun setWebSearchEnabled(context: Context, value: Boolean) {
        webSearchEnabled = value
        aiPrefs(context).edit().putBoolean(KEY_WEB_SEARCH, value).apply()
    }

    fun setReasoningLevel(context: Context, value: AiReasoningLevel) {
        reasoningLevel = value
        aiPrefs(context).edit().putString(KEY_REASONING_LEVEL, value.name).apply()
    }
}

// ============================================================
// 文件处理权限
// ============================================================

enum class AiFilePermission(val label: String, val summary: String) {
    ViewOnly("仅查看", "只能读取附件内容，不能修改文件"),
    EditWithConfirm("查看并修改", "每次修改文件前都需要确认"),
    FullAccess("完全权限", "常规修改自动执行，敏感操作仍需确认"),
}

object AiPermissionStore {

    private const val KEY_PERMISSION = "ai_file_permission"

    private var loaded = false

    var permission by mutableStateOf(AiFilePermission.ViewOnly)
        private set

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        permission = runCatching {
            AiFilePermission.valueOf(
                aiPrefs(context).getString(KEY_PERMISSION, AiFilePermission.ViewOnly.name)
                    ?: AiFilePermission.ViewOnly.name,
            )
        }.getOrDefault(AiFilePermission.ViewOnly)
    }

    fun setPermission(context: Context, value: AiFilePermission) {
        permission = value
        aiPrefs(context).edit().putString(KEY_PERMISSION, value.name).apply()
    }
}

// ============================================================
// 文件修改确认
// ============================================================

data class AiFileEditRequest(
    val fileName: String,
    val summary: String,
)

object AiFileEditConfirmation {

    var request by mutableStateOf<AiFileEditRequest?>(null)
        private set

    private var deferred: kotlinx.coroutines.CompletableDeferred<Boolean>? = null

    suspend fun ask(request: AiFileEditRequest): Boolean {
        this.request = request
        val waiter = kotlinx.coroutines.CompletableDeferred<Boolean>()
        deferred = waiter
        val result = waiter.await()
        if (deferred === waiter) deferred = null
        this.request = null
        return result
    }

    fun resolve(approved: Boolean) {
        val waiter = deferred
        deferred = null
        request = null
        waiter?.complete(approved)
    }
}

// ============================================================
// 对话消息
// ============================================================

/** 单条对话（UI 状态，流式过程中会被频繁更新）。 */
class AiUiMessage(
    val id: Long,
    val role: String,
    initialContent: String = "",
    initialReasoning: String = "",
    val attachments: List<AiAttachmentMeta> = emptyList(),
    val fileContext: String = "",
    initialReasoningElapsedMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    initialUsageTokens: Int = 0,
    initialUsageEstimated: Boolean = false,
    initialError: String? = null,
    initialReasoningDone: Boolean = initialReasoning.isNotBlank(),
    initialSteps: List<AiAgentStep> = emptyList(),
    initialGeneratedFiles: List<AiGeneratedFile> = emptyList(),
    initialSources: List<AiSource> = emptyList(),
    var parentId: Long? = null,
    initialSelectedChildId: Long? = null,
    initialContinuable: Boolean = false,
    initialErrorAction: AiErrorAction? = null,
) {
    var content by mutableStateOf(initialContent)
    var reasoning by mutableStateOf(initialReasoning)
    var reasoningElapsedMs by mutableStateOf(initialReasoningElapsedMs)
        private set
    var usageTokens by mutableIntStateOf(initialUsageTokens)
    var usageEstimated by mutableStateOf(initialUsageEstimated)
    var reasoningStartedAt by mutableLongStateOf(0L)
        private set
    var reasoningDone by mutableStateOf(initialReasoningDone)
        private set
    var streaming by mutableStateOf(false)
    var error by mutableStateOf(initialError)
    var errorAction by mutableStateOf(initialErrorAction)

    /** 被用户停止或达到输出上限后，可以基于当前正文继续生成。 */
    var continuable by mutableStateOf(initialContinuable)

    /** 思考区里的工具动作时间线（正在搜索 / 正在打开 / 正在滑动…）。 */
    val steps = mutableStateListOf<AiAgentStep>().apply { addAll(initialSteps) }

    /** 这条回答生成的文件，汇总显示在对话底部。 */
    val generatedFiles = mutableStateListOf<AiGeneratedFile>().apply {
        addAll(initialGeneratedFiles)
    }

    /** 联网来源：正文引用角标 [citation:N] 的编号与这里一一对应。 */
    val sources = mutableStateListOf<AiSource>().apply { addAll(initialSources) }

    var selectedChildId by mutableStateOf(initialSelectedChildId)

    fun mergeSources(items: List<AiSource>) {
        items.forEach { source ->
            val index = sources.indexOfFirst { it.url == source.url }
            if (index >= 0) {
                val old = sources[index]
                sources[index] = old.copy(
                    title = source.title.ifBlank { old.title },
                    snippet = source.snippet.ifBlank { old.snippet },
                    siteName = source.siteName.ifBlank { old.siteName },
                    publishedAt = source.publishedAt ?: old.publishedAt,
                )
            } else {
                sources.add(source)
            }
        }
    }

    val isUser: Boolean get() = role == "user"

    fun addGeneratedFile(file: AiGeneratedFile) {
        if (file.path.isBlank()) return
        val index = generatedFiles.indexOfFirst { it.path == file.path }
        if (index >= 0) {
            generatedFiles[index] = file
        } else {
            generatedFiles.add(file)
        }
    }

    fun startReasoning(startedAt: Long) {
        if (reasoningStartedAt == 0L) {
            reasoningStartedAt = startedAt
        }
        reasoningDone = false
    }

    fun appendReasoning(delta: String) {
        reasoning += delta
    }

    fun finishReasoning(elapsedMs: Long) {
        if (reasoningStartedAt <= 0L) return
        reasoningElapsedMs = elapsedMs.coerceAtLeast(0L)
        reasoningDone = true
    }

    /** 新的一步（或更新同一步的状态）。 */
    fun upsertStep(step: AiAgentStep) {
        val index = steps.indexOfFirst { it.id == step.id }
        if (index >= 0) {
            steps[index] = step
        } else {
            steps.add(step)
        }
    }

    /** 结束仍在进行中的步骤，避免界面一直转圈。 */
    fun finishPendingSteps(cancelled: Boolean = false) {
        for (index in steps.indices) {
            val step = steps[index]
            if (step.state == AiStepState.Running) {
                steps[index] = step.copy(
                    state = if (cancelled) AiStepState.Failed else AiStepState.Done,
                    title = if (cancelled) "${step.title}（已中断）" else step.title,
                )
            }
        }
    }

    fun finishStreaming(finalElapsedMs: Long) {
        if (reasoningStartedAt > 0L) {
            if (!reasoningDone || reasoningElapsedMs <= 0L) {
                reasoningElapsedMs = finalElapsedMs.coerceAtLeast(0L)
            }
            reasoningDone = true
        }
        streaming = false
    }
}

// ============================================================
// 对话状态
// ============================================================

data class AiConversationMeta(
    val id: String,
    val title: String,
    val updatedAt: Long,
)

object AiChatStore {

    private var loaded = false
    private var nextId = 1L
    private var appContext: Context? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var runningJob: Job? = null
    private var saveJob: Job? = null
    private var persistPending = false
    private var activeCall: okhttp3.Call? = null

    val messages = mutableStateListOf<AiUiMessage>()
    val conversations = mutableStateListOf<AiConversationMeta>()

    /** 当前会话的完整消息树（含未选中的分支）；messages 只是当前选中的路径。 */
    private val allMessages = mutableStateListOf<AiUiMessage>()

    var rootMessageId by mutableStateOf<Long?>(null)
        private set

    var activeConversationId by mutableStateOf<String?>(null)
        private set

    /** 同一父节点下的兄弟版本，供消息气泡下方的 1/N 切换器使用。 */
    data class AiVersionInfo(val ids: List<Long>, val index: Int)

    fun versionInfo(messageId: Long): AiVersionInfo? {
        val ids = aiVersionIds(allMessages, messageId)
        if (ids.size <= 1) return null
        val index = ids.indexOf(messageId)
        if (index < 0) return null
        return AiVersionInfo(ids, index)
    }

    fun switchVersion(messageId: Long) {
        if (streaming) return
        val message = allMessages.firstOrNull { it.id == messageId } ?: return
        val parentId = message.parentId
        if (parentId == null) {
            rootMessageId = messageId
        } else {
            val parent = allMessages.firstOrNull { it.id == parentId } ?: return
            parent.selectedChildId = messageId
        }
        rebuildVisible()
        persist()
    }

    private fun rebuildVisible() {
        messages.clear()
        messages.addAll(aiVisiblePath(allMessages, rootMessageId))
    }

    private fun loadConversation(context: Context, id: String) {
        val loaded = AiConversationStore.load(context, id)
        allMessages.clear()
        allMessages.addAll(loaded)
        val storedRoot = AiConversationStore.loadRoot(context, id)
        // 旧文件没有 rootMessageId：按线性链迁移，保证老会话还能正常打开。
        if (storedRoot == null && loaded.size > 1) {
            var previous: AiUiMessage? = null
            loaded.forEach { message ->
                message.parentId = previous?.id
                previous?.selectedChildId = message.id
                previous = message
            }
            rootMessageId = loaded.firstOrNull()?.id
        } else {
            rootMessageId = storedRoot
                ?.takeIf { root -> loaded.any { it.id == root } }
                ?: loaded.firstOrNull { it.parentId == null }?.id
                ?: loaded.firstOrNull()?.id
        }
        nextId = (allMessages.maxOfOrNull { it.id } ?: 0L) + 1L
        rebuildVisible()
    }


    var streaming by mutableStateOf(false)
        private set

    var notice by mutableStateOf<String?>(null)
        private set

    fun ensureLoaded(context: Context) {
        appContext = context.applicationContext
        if (loaded) return
        loaded = true
        AiProviderStore.ensureLoaded(context)
        AiSettings.ensureLoaded(context)
        AiPermissionStore.ensureLoaded(context)

        val index = AiConversationIndex.load(context)
        if (index != null && index.conversations.isNotEmpty()) {
            conversations.addAll(index.conversations.sortedByDescending { it.updatedAt })
            activeConversationId = index.activeId
                ?.takeIf { id -> conversations.any { it.id == id } }
                ?: conversations.firstOrNull()?.id
        } else {
            val legacy = AiConversationStore.load(context, "legacy")
            if (legacy.isNotEmpty()) {
                val id = "legacy"
                conversations.add(
                    AiConversationMeta(
                        id = id,
                        title = titleFor(legacy),
                        updatedAt = legacy.lastOrNull()?.createdAt ?: System.currentTimeMillis(),
                    ),
                )
                activeConversationId = id
                AiConversationIndex.save(context, id, conversations.toList())
            }
        }
        if (activeConversationId != null) {
            loadConversation(context, activeConversationId!!)
        }

        AiDraftStore.ensureLoaded(context)
        AiPresetStore.ensureLoaded(context)
        AiConversationSettings.ensureLoaded(context)
        AiConversationSettings.ensureDefaultsForAll(context, conversations.map { it.id })
    }

    fun clearNotice() {
        notice = null
    }

    /** 导出/备份前把当前内存状态立即落盘。 */
    fun flush() {
        saveCurrentNow()
    }

    /** 导入备份前停止生成并取消待落盘任务，避免旧会话在导入后被写回。 */
    fun prepareForImport() {
        if (streaming) stop()
        saveJob?.cancel()
        saveJob = null
        persistPending = false
    }

    /** 导入备份后重新从磁盘加载会话索引和当前会话。 */
    fun reloadFromDisk(context: Context) {
        saveJob?.cancel()
        persistPending = false
        loaded = false
        messages.clear()
        allMessages.clear()
        conversations.clear()
        activeConversationId = null
        rootMessageId = null
        nextId = 1L
        ensureLoaded(context)
    }

    fun continueReply(messageId: Long) {
        if (streaming) return
        val context = appContext ?: return
        val assistant = messages.firstOrNull { it.id == messageId && !it.isUser } ?: return
        if (assistant.content.isBlank()) return
        val conversationId = activeConversationId
        val provider = AiConversationSettings.providerFor(conversationId) ?: return
        val permission = AiConversationSettings.permissionFor(conversationId)
        val reasoningLevel = AiConversationSettings.reasoningFor(conversationId)
        val history = historyBefore(assistant.id)
        assistant.continuable = false
        assistant.error = null
        startGeneration(
            context = context,
            provider = provider,
            conversationId = conversationId,
            assistant = assistant,
            history = history,
            requestText = "",
            attachments = emptyList(),
            userImages = emptyList(),
            permission = permission,
            reasoningLevel = reasoningLevel,
            materializeRequest = history.lastOrNull { it.role == "user" }?.text.orEmpty(),
            continuePrefix = assistant.content,
        )
    }

    fun regenerate(assistantId: Long) {
        if (streaming) return
        val context = appContext ?: return
        val oldAssistant = messages.firstOrNull { it.id == assistantId && !it.isUser } ?: return
        val user = allMessages.firstOrNull { it.id == oldAssistant.parentId } ?: return
        val conversationId = activeConversationId
        val provider = AiConversationSettings.providerFor(conversationId) ?: return
        val permission = AiConversationSettings.permissionFor(conversationId)
        val reasoningLevel = AiConversationSettings.reasoningFor(conversationId)
        val history = historyBefore(user.id)

        val assistant = AiUiMessage(
            id = nextId++,
            role = "assistant",
            parentId = user.id,
        )
        user.selectedChildId = assistant.id
        allMessages.add(assistant)
        rebuildVisible()
        startGeneration(
            context = context,
            provider = provider,
            conversationId = conversationId,
            assistant = assistant,
            history = history,
            requestText = buildPromptText(user.content, emptyList(), user.fileContext),
            attachments = emptyList(),
            userImages = emptyList(),
            permission = permission,
            reasoningLevel = reasoningLevel,
            materializeRequest = user.content,
        )
    }

    fun editUserMessage(context: Context, messageId: Long, newText: String) {
        if (streaming) return
        val oldUser = messages.firstOrNull { it.id == messageId && it.isUser } ?: return
        val parentId = oldUser.parentId
        val conversationId = activeConversationId
        val provider = AiConversationSettings.providerFor(conversationId) ?: return
        val permission = AiConversationSettings.permissionFor(conversationId)
        val reasoningLevel = AiConversationSettings.reasoningFor(conversationId)
        val history = historyBefore(oldUser.id)
        val trimmed = newText.trim()
        if (trimmed.isEmpty()) return

        val newUser = AiUiMessage(
            id = nextId++,
            role = "user",
            initialContent = trimmed,
            attachments = oldUser.attachments,
            fileContext = oldUser.fileContext,
            parentId = parentId,
        )
        newUser.usageTokens = estimateTokens(trimmed) + estimateTokens(oldUser.fileContext)
        newUser.usageEstimated = true
        val assistant = AiUiMessage(
            id = nextId++,
            role = "assistant",
            parentId = newUser.id,
        )
        newUser.selectedChildId = assistant.id
        if (parentId == null) {
            rootMessageId = newUser.id
        } else {
            allMessages.firstOrNull { it.id == parentId }?.selectedChildId = newUser.id
        }
        allMessages.add(newUser)
        allMessages.add(assistant)
        rebuildVisible()
        startGeneration(
            context = context,
            provider = provider,
            conversationId = conversationId,
            assistant = assistant,
            history = history,
            requestText = buildPromptText(trimmed, emptyList(), newUser.fileContext),
            attachments = emptyList(),
            userImages = emptyList(),
            permission = permission,
            reasoningLevel = reasoningLevel,
            materializeRequest = trimmed,
        )
    }

    /** 新建一个会话；当前已经是空对话时复用，避免堆积空会话。 */
    fun newConversation(presetId: String? = null) {
        if (streaming) stop()
        // 每次新建都生成独立 conversationId。即使当前对话还没有消息，也不能复用，
        // 否则两个“新对话”的输入框会共用同一份内容。
        saveCurrentNow()
        val id = "conv_" + System.currentTimeMillis().toString(36)
        conversations.add(0, AiConversationMeta(id, "新对话", System.currentTimeMillis()))
        activeConversationId = id
        messages.clear()
        allMessages.clear()
        rootMessageId = null
        nextId = 1L
        notice = null
        appContext?.let { AiConversationSettings.ensureDefaults(it, id, presetId) }
        saveIndex()
    }

    fun switchConversation(id: String) {
        if (id == activeConversationId) return
        if (conversations.none { it.id == id }) return
        if (streaming) stop()
        saveCurrentNow()
        val context = appContext ?: return
        activeConversationId = id
        loadConversation(context, id)
        notice = null
        AiConversationSettings.ensureDefaults(context, id)
        saveIndex()
    }

    fun clearAllConversations() {
        if (streaming) stop()
        saveJob?.cancel()
        val context = appContext ?: return
        conversations.forEach { meta ->
            AiConversationStore.delete(context, meta.id)
            AiDraftStore.remove(context, meta.id)
        }
        AiConversationStore.delete(context, "legacy")
        AiConversationIndex.delete(context)
        conversations.clear()
        messages.clear()
        allMessages.clear()
        rootMessageId = null
        activeConversationId = null
        nextId = 1L
        notice = null
        AiConversationSettings.clearAll(context)
        AiDraftStore.clearAll(context)
        runCatching { MoteAutoSync.requestSync(context) }
    }

    fun deleteConversations(ids: Set<String>) {
        if (ids.isEmpty()) return
        if (streaming && activeConversationId in ids) stop()
        saveJob?.cancel()
        val context = appContext ?: return
        ids.forEach { id ->
            AiConversationStore.delete(context, id)
            AiDraftStore.remove(context, id)
        }
        AiConversationSettings.remove(context, ids)
        conversations.removeAll { it.id in ids }
        if (activeConversationId in ids) {
            activeConversationId = conversations.firstOrNull()?.id
            messages.clear()
            allMessages.clear()
            rootMessageId = null
            if (activeConversationId != null) {
                loadConversation(context, activeConversationId!!)
                AiConversationSettings.ensureDefaults(context, activeConversationId!!)
            }
        }
        AiConversationIndex.save(context, activeConversationId, conversations.toList())
        runCatching { MoteAutoSync.requestSync(context) }
    }

    /** 普通对话和自动操作统一用 Markdown 追加错误，保留已生成内容。 */
    private fun AiUiMessage.appendGenerationError(
        message: String,
        action: AiErrorAction = AiErrorAction.None,
    ) {
        val block = "> **生成出错**：$message"
        content = if (content.isBlank()) block else "$content\n\n$block"
        error = null
        errorAction = action.takeIf { it != AiErrorAction.None }
    }

    fun send(context: Context, text: String, attachments: List<AiAttachment>) {
        appContext = context.applicationContext
        if (streaming) return
        if (activeConversationId == null) {
            newConversation()
        }

        val conversationId = activeConversationId
        val resolved = AiPresetResolver.resolve(context, conversationId)
        val provider = resolved?.provider ?: AiConversationSettings.providerFor(conversationId)
        if (provider == null || !provider.isConfigured) {
            notice = "还没有可用的 AI 服务，请先在设置中绑定 API Key 与模型"
            return
        }
        val permission = resolved?.filePermission
            ?: AiConversationSettings.permissionFor(conversationId)
        val reasoningLevel = resolved?.reasoning
            ?: AiConversationSettings.reasoningFor(conversationId)
        val systemPrompt = resolved?.systemPrompt ?: AiSettings.systemPrompt

        val trimmed = text.trim()
        if (trimmed.isEmpty() && attachments.isEmpty()) return
        val modelSpec = provider.specFor(provider.model)
        if (!modelSpec.supportsText && trimmed.isNotEmpty()) {
            notice = "当前模型未开启文本输入，请在服务设置 → 模型 → 文本 中开启"
            return
        }

        val history = buildHistory()
        val fileContext = buildFileContext(attachments)
        val requestText = buildPromptText(trimmed, attachments, fileContext)

        val parentMessage = messages.lastOrNull()
        val userMessage = AiUiMessage(
            id = nextId++,
            role = "user",
            initialContent = trimmed,
            attachments = attachments.map { it.meta },
            fileContext = fileContext,
            parentId = parentMessage?.id,
        )
        userMessage.usageTokens = estimateTokens(trimmed) + estimateTokens(fileContext)
        userMessage.usageEstimated = true
        val assistant = AiUiMessage(
            id = nextId++,
            role = "assistant",
            parentId = userMessage.id,
        )
        userMessage.selectedChildId = assistant.id
        if (parentMessage == null) {
            rootMessageId = userMessage.id
        } else {
            parentMessage.selectedChildId = userMessage.id
        }
        allMessages.add(userMessage)
        allMessages.add(assistant)
        messages.add(userMessage)
        messages.add(assistant)
        notice = null
        persist()

        val userImages = if (modelSpec.supportsImage) {
            attachments.mapNotNull { attachment ->
                val mime = attachment.imageMime
                val base64 = attachment.imageBase64
                if (mime != null && base64 != null) AiImagePart(mime, base64) else null
            }
        } else {
            emptyList()
        }

        val startedAt = SystemClock.elapsedRealtime()
        var cancelled = false
        streaming = true
        assistant.streaming = true
        assistant.error = null
        assistant.errorAction = null
        assistant.startReasoning(startedAt)

        // 所有请求都走同一个 Agent 循环：由模型自己判断要不要搜索、打开网页、
        // 打开应用或操控手机，不再依赖固定触发词，也不会因为一句话里有「打开」
        // 就抢先把整段请求当成“打开某个应用”执行。
        runningJob = scope.launch {
            try {
                val result = AiAgentRunner.run(
                    context = context,
                    provider = provider,
                    systemPrompt = systemPrompt + "\n\n" +
                            reasoningLevel.instruction,
                    history = history,
                    userRequest = requestText,
                    userImages = userImages,
                    files = attachments,
                    permission = permission,
                    reasoning = reasoningLevel,
                    onThought = { delta ->
                        assistant.startReasoning(startedAt)
                        assistant.appendReasoning(delta)
                        // 边生成边落盘：即使渲染层异常退出，重新进入也能恢复已生成内容
                        persist()
                    },
                    onStep = { step ->
                        assistant.upsertStep(step)
                        persist()
                    },
                    onContent = { delta ->
                        if (assistant.reasoning.isNotBlank() && !assistant.reasoningDone) {
                            assistant.finishReasoning(SystemClock.elapsedRealtime() - startedAt)
                        }
                        assistant.content += delta
                        persist()
                    },
                    onCall = { activeCall = it },
                    onConfirmEdit = { request -> AiFileEditConfirmation.ask(request) },
                    onNotice = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    onGeneratedFile = { file ->
                        assistant.addGeneratedFile(file)
                        persist()
                    },
                    onSources = { sources ->
                        assistant.mergeSources(sources)
                        persist()
                    },
                    onApproveTool = { request -> AiToolApproval.ask(context, request) },
                    deviceControlEnabled = resolved?.deviceControl ?: AiSettings.agentEnabled,
                    allowedTools = resolved?.allowedTools,
                    mcpServerIds = resolved?.mcpServerIds,
                    onRequestUsage = { request ->
                        AiUsageRecorder.record(
                            context = context,
                            request = request,
                            conversationId = conversationId,
                            messageId = assistant.id,
                            presetId = resolved?.presetId,
                            provider = provider,
                        )
                    },
                )
                assistant.content = cleanFinalAnswer(result.text)
                assistant.mergeSources(result.sources)
                assistant.continuable = result.stopReason
                    ?.lowercase()
                    ?.let { it == "max_tokens" || it == "length" } == true
                val materialized = runCatching {
                    withContext(Dispatchers.IO) {
                        AiDocumentWriter.materialize(
                            context = context,
                            assistant = assistant,
                            directRequest = if (result.documentCreated) "" else trimmed,
                        )
                    }
                }.getOrNull()
                materialized?.let { assistant.addGeneratedFile(it) }
            } catch (cancel: CancellationException) {
                cancelled = true
                if (assistant.content.isBlank() && assistant.reasoning.isBlank()) {
                    assistant.content = "（已停止生成）"
                }
                throw cancel
            } catch (throwable: Throwable) {
                assistant.appendGenerationError(aiErrorMessage(throwable), aiErrorAction(throwable))
            } finally {
                val endedAt = SystemClock.elapsedRealtime()
                assistant.finishPendingSteps(cancelled = cancelled)
                assistant.finishReasoning(endedAt - startedAt)
                assistant.finishStreaming(endedAt - startedAt)
                if (assistant.usageTokens <= 0) {
                    assistant.usageTokens = estimateTokens(systemPrompt) +
                            history.sumOf { estimateTokens(it.text) } +
                            estimateTokens(requestText) +
                            assistant.steps.sumOf { estimateTokens(it.title + it.detail) } +
                            estimateTokens(assistant.reasoning + assistant.content)
                    assistant.usageEstimated = true
                }
                streaming = false
                runningJob = null
                activeCall = null
                val hadControl = runCatching {
                    AiFloatingBubbleService.isAgentBusy()
                }.getOrDefault(false)
                runCatching {
                    AiFloatingBubbleService.setAgentBusy(false)
                    if (hadControl) {
                        // 任务结束后让悬浮球多停留一会儿，方便用户回到 Mote AI
                        AiFloatingController.stopTemporaryDelayed(context, 6_000L)
                        Toast.makeText(
                            context,
                            if (cancelled) "已停止自动操作" else "自动操作已完成",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        AiFloatingController.stopTemporary(context)
                    }
                }
                persist()
                runCatching { MoteAutoSync.requestSync(context) }
            }
        }
    }

    private fun startGeneration(
        context: Context,
        provider: AiProviderConfig,
        conversationId: String?,
        assistant: AiUiMessage,
        history: List<AiPromptMessage>,
        requestText: String,
        attachments: List<AiAttachment>,
        userImages: List<AiImagePart>,
        permission: AiFilePermission,
        reasoningLevel: AiReasoningLevel,
        materializeRequest: String,
        continuePrefix: String? = null,
    ) {
        val resolved = AiPresetResolver.resolve(context, conversationId)
        val effectiveProvider = resolved?.provider ?: provider
        val effectivePermission = resolved?.filePermission ?: permission
        val effectiveReasoning = resolved?.reasoning ?: reasoningLevel
        val systemPrompt = resolved?.systemPrompt ?: AiSettings.systemPrompt
        val startedAt = SystemClock.elapsedRealtime()
        var cancelled = false
        streaming = true
        assistant.streaming = true
        assistant.error = null
        assistant.errorAction = null
        assistant.startReasoning(startedAt)

        runningJob = scope.launch {
            try {
                val result = AiAgentRunner.run(
                    context = context,
                    provider = effectiveProvider,
                    systemPrompt = systemPrompt + "\n\n" +
                            effectiveReasoning.instruction,
                    history = history,
                    userRequest = requestText,
                    userImages = userImages,
                    files = attachments,
                    permission = effectivePermission,
                    reasoning = effectiveReasoning,
                    prefixAssistant = continuePrefix,
                    onThought = { delta ->
                        assistant.startReasoning(startedAt)
                        assistant.appendReasoning(delta)
                        persist()
                    },
                    onStep = { step ->
                        assistant.upsertStep(step)
                        persist()
                    },
                    onContent = { delta ->
                        if (assistant.reasoning.isNotBlank() && !assistant.reasoningDone) {
                            assistant.finishReasoning(SystemClock.elapsedRealtime() - startedAt)
                        }
                        assistant.content += delta
                        persist()
                    },
                    onCall = { activeCall = it },
                    onConfirmEdit = { request -> AiFileEditConfirmation.ask(request) },
                    onNotice = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    onGeneratedFile = { file ->
                        assistant.addGeneratedFile(file)
                        persist()
                    },
                    onSources = { sources ->
                        assistant.mergeSources(sources)
                        persist()
                    },
                    onApproveTool = { request -> AiToolApproval.ask(context, request) },
                    deviceControlEnabled = resolved?.deviceControl ?: AiSettings.agentEnabled,
                    allowedTools = resolved?.allowedTools,
                    mcpServerIds = resolved?.mcpServerIds,
                    onRequestUsage = { request ->
                        AiUsageRecorder.record(
                            context = context,
                            request = request,
                            conversationId = conversationId,
                            messageId = assistant.id,
                            presetId = resolved?.presetId,
                            provider = effectiveProvider,
                        )
                    },
                )
                if (continuePrefix == null) {
                    assistant.content = cleanFinalAnswer(result.text)
                }
                assistant.mergeSources(result.sources)
                assistant.continuable = result.stopReason
                    ?.lowercase()
                    ?.let { it == "max_tokens" || it == "length" } == true
                val materialized = runCatching {
                    withContext(Dispatchers.IO) {
                        AiDocumentWriter.materialize(
                            context = context,
                            assistant = assistant,
                            directRequest = if (result.documentCreated) "" else materializeRequest,
                        )
                    }
                }.getOrNull()
                materialized?.let { assistant.addGeneratedFile(it) }
            } catch (cancel: CancellationException) {
                cancelled = true
                if (assistant.content.isBlank() && assistant.reasoning.isBlank()) {
                    assistant.content = "（已停止生成）"
                }
                throw cancel
            } catch (throwable: Throwable) {
                assistant.appendGenerationError(aiErrorMessage(throwable), aiErrorAction(throwable))
            } finally {
                val endedAt = SystemClock.elapsedRealtime()
                assistant.finishPendingSteps(cancelled = cancelled)
                assistant.finishReasoning(endedAt - startedAt)
                assistant.finishStreaming(endedAt - startedAt)
                if (assistant.usageTokens <= 0) {
                    assistant.usageTokens = estimateTokens(systemPrompt) +
                            history.sumOf { estimateTokens(it.text) } +
                            estimateTokens(requestText) +
                            assistant.steps.sumOf { estimateTokens(it.title + it.detail) } +
                            estimateTokens(assistant.reasoning + assistant.content)
                    assistant.usageEstimated = true
                }
                streaming = false
                runningJob = null
                activeCall = null
                val hadControl = runCatching {
                    AiFloatingBubbleService.isAgentBusy()
                }.getOrDefault(false)
                runCatching {
                    AiFloatingBubbleService.setAgentBusy(false)
                    if (hadControl) {
                        AiFloatingController.stopTemporaryDelayed(context, 6_000L)
                        Toast.makeText(
                            context,
                            if (cancelled) "已停止自动操作" else "自动操作已完成",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        AiFloatingController.stopTemporary(context)
                    }
                }
                persist()
                runCatching { MoteAutoSync.requestSync(context) }
            }
        }
    }

    fun stop() {
        activeCall?.cancel()
        runningJob?.cancel()
        activeCall = null
        runningJob = null
        streaming = false
        messages.lastOrNull { !it.isUser && it.streaming }?.let { message ->
            message.finishStreaming(0L)
            message.continuable = message.content.isNotBlank()
        }
        persist()
    }

    private fun buildHistory(): List<AiPromptMessage> {
        val turns = AiConversationSettings.contextTurnsFor(activeConversationId).coerceIn(0, 40)
        if (messages.isEmpty()) return emptyList()
        val limit = if (turns == 0) 0 else turns * 2
        val source = if (limit <= 0) {
            emptyList()
        } else {
            messages.takeLast(limit).dropWhile { it.role == "assistant" }
        }
        return promptHistory(source)
    }

    private fun promptHistory(source: List<AiUiMessage>): List<AiPromptMessage> =
        source.mapNotNull { message ->
            val text = when {
                message.isUser -> buildString {
                    if (message.fileContext.isNotBlank()) {
                        append(message.fileContext).append("\n\n")
                    }
                    append(message.content)
                }

                else -> message.content
            }
            if (text.isBlank()) null else AiPromptMessage(message.role, text)
        }

    private fun historyBefore(messageId: Long?): List<AiPromptMessage> {
        if (messageId == null) return emptyList()
        val index = messages.indexOfFirst { it.id == messageId }
        if (index < 0) return emptyList()
        val before = messages.subList(0, index)
        val turns = AiConversationSettings.contextTurnsFor(activeConversationId).coerceIn(0, 40)
        val limit = if (turns == 0) 0 else turns * 2
        val source = if (limit <= 0) {
            emptyList()
        } else {
            before.takeLast(limit).dropWhile { it.role == "assistant" }
        }
        return promptHistory(source)
    }

    private fun buildFileContext(attachments: List<AiAttachment>): String {
        if (attachments.isEmpty()) return ""
        val builder = StringBuilder()
        attachments.forEach { attachment ->
            if (attachment.text.isBlank()) return@forEach
            builder.append("【附件：").append(attachment.name).append("】\n")
            builder.append(attachment.text.trim())
            builder.append("\n\n")
        }
        return builder.toString().trim()
    }

    private fun buildPromptText(
        text: String,
        attachments: List<AiAttachment>,
        fileContext: String,
    ): String {
        if (attachments.isEmpty()) return text
        return buildString {
            if (fileContext.isNotBlank()) append(fileContext).append("\n\n")
            if (text.isNotBlank()) append("用户的问题：").append(text)
        }.trim().ifBlank { text }
    }

    private fun titleFor(messages: List<AiUiMessage>): String {
        val source = if (AiSettings.titleFromContent) {
            messages.firstOrNull { it.isUser && it.content.isNotBlank() }
        } else {
            messages.lastOrNull { it.isUser && it.content.isNotBlank() }
        }
        return source
            ?.content
            ?.replace('\n', ' ')
            ?.trim()
            ?.take(28)
            ?.takeIf { it.isNotBlank() }
            ?: "新对话"
    }

    private fun updateCurrentMeta() {
        val id = activeConversationId ?: return
        val index = conversations.indexOfFirst { it.id == id }
        if (index < 0) return
        val title = titleFor(messages)
        val updatedAt = messages.lastOrNull()?.createdAt ?: System.currentTimeMillis()
        conversations[index] = conversations[index].copy(title = title, updatedAt = updatedAt)
    }

    private fun snapshotMessages(): List<SavedMessage> =
        allMessages.map { message ->
            SavedMessage(
                id = message.id,
                parentId = message.parentId,
                selectedChildId = message.selectedChildId,
                role = message.role,
                content = message.content,
                reasoning = message.reasoning,
                reasoningElapsedMs = message.reasoningElapsedMs,
                attachments = message.attachments,
                fileContext = message.fileContext,
                createdAt = message.createdAt,
                usageTokens = message.usageTokens,
                usageEstimated = message.usageEstimated,
                error = message.error,
                errorAction = message.errorAction,
                steps = message.steps.toList(),
                generatedFiles = message.generatedFiles.toList(),
                sources = message.sources.toList(),
                continuable = message.continuable,
            )
        }

    private fun persist() {
        val context = appContext ?: return
        val id = activeConversationId ?: return
        updateCurrentMeta()
        // 流式生成时 persist 会被高频调用：用节流而不是取消重排，
        // 否则持续输出时永远等不到 600ms 的落盘窗口。
        if (saveJob?.isActive == true) {
            persistPending = true
            return
        }
        saveJob = scope.launch {
            delay(600L)
            val snapshot = snapshotMessages()
            val conversationMetas = conversations.toList()
            withContext(Dispatchers.IO) {
                AiConversationStore.save(context, id, snapshot, rootMessageId)
                AiConversationIndex.save(context, id, conversationMetas)
            }
            if (persistPending) {
                persistPending = false
                persist()
            }
        }
    }

    private fun saveCurrentNow() {
        saveJob?.cancel()
        persistPending = false
        val context = appContext ?: return
        val id = activeConversationId ?: return
        updateCurrentMeta()
        runCatching {
            AiConversationStore.save(context, id, snapshotMessages(), rootMessageId)
            AiConversationIndex.save(context, activeConversationId, conversations.toList())
        }
    }

    private fun saveIndex() {
        val context = appContext ?: return
        runCatching {
            AiConversationIndex.save(context, activeConversationId, conversations.toList())
        }
    }
}

// ============================================================
// 对话记录文件
// ============================================================

internal data class SavedMessage(
    val id: Long = 0L,
    val parentId: Long? = null,
    val selectedChildId: Long? = null,
    val role: String,
    val content: String,
    val reasoning: String,
    val reasoningElapsedMs: Long,
    val attachments: List<AiAttachmentMeta>,
    val fileContext: String,
    val createdAt: Long,
    val usageTokens: Int,
    val usageEstimated: Boolean,
    val error: String?,
    val errorAction: AiErrorAction? = null,
    val steps: List<AiAgentStep> = emptyList(),
    val generatedFiles: List<AiGeneratedFile> = emptyList(),
    val sources: List<AiSource> = emptyList(),
    val continuable: Boolean = false,
)

/** 旧记录里只有「已生成文件：Download/Mote/xxx」文字，这里补回结构化文件卡片。 */
private val generatedFileMarker = Regex("已生成文件：Download/Mote/([^\\s，。；、）)\"']+)")

private fun recoverGeneratedFiles(content: String): List<AiGeneratedFile> {
    if (!content.contains("已生成文件：Download/Mote/")) return emptyList()
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Mote",
    )
    return generatedFileMarker.findAll(content).mapNotNull { match ->
        val name = match.groupValues[1].trim()
        if (name.isBlank()) return@mapNotNull null
        val file = File(dir, name)
        if (!file.isFile) return@mapNotNull null
        AiGeneratedFile(
            path = file.absolutePath,
            name = name,
            sizeBytes = file.length(),
            kind = aiGeneratedFileKindOf(name),
        )
    }.distinctBy { it.path }.toList()
}

internal object AiConversationStore {

    private const val BASE_FILE_NAME = "ai_conversation.json"
    private const val MAX_FILE_CONTEXT_CHARS = 50_000
    private const val MAX_CONTENT_CHARS = 120_000

    private fun file(context: Context, id: String): File {
        val name = if (id.isBlank() || id == "legacy") {
            BASE_FILE_NAME
        } else {
            "ai_conversation_${id.replace(Regex("[^A-Za-z0-9_-]"), "_")}.json"
        }
        return File(context.filesDir, name)
    }

    fun load(context: Context, id: String): List<AiUiMessage> {
        val raw = runCatching { file(context, id).readText() }.getOrNull()
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val root = JSONObject(raw)
            val array = root.optJSONArray("messages") ?: return emptyList()
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val role = obj.optString("role").takeIf { it == "user" || it == "assistant" }
                        ?: continue
                    val attachments = mutableListOf<AiAttachmentMeta>()
                    obj.optJSONArray("attachments")?.let { attachmentArray ->
                        for (i in 0 until attachmentArray.length()) {
                            val item = attachmentArray.optJSONObject(i) ?: continue
                            attachments.add(
                                AiAttachmentMeta(
                                    name = item.optString("name"),
                                    sizeBytes = item.optLong("size"),
                                    chars = item.optInt("chars"),
                                    imagePath = item.optString("imagePath")
                                        .takeIf { it.isNotBlank() },
                                ),
                            )
                        }
                    }
                    val steps = mutableListOf<AiAgentStep>()
                    obj.optJSONArray("steps")?.let { stepArray ->
                        for (i in 0 until stepArray.length()) {
                            val item = stepArray.optJSONObject(i) ?: continue
                            val title = item.optString("title")
                            if (title.isBlank()) continue
                            steps.add(
                                AiAgentStep(
                                    id = item.optLong("id", i + 1L),
                                    title = title,
                                    detail = item.optString("detail"),
                                    // 记录里不会留下“进行中”，避免重新打开会话后一直转圈。
                                    state = runCatching {
                                        AiStepState.valueOf(item.optString("state"))
                                    }.getOrDefault(AiStepState.Done)
                                        .let { if (it == AiStepState.Running) AiStepState.Done else it },
                                    kind = runCatching {
                                        AiStepKind.valueOf(item.optString("kind"))
                                    }.getOrDefault(AiStepKind.Other),
                                    sourceIds = item.optJSONArray("sourceIds")?.let { ids ->
                                        (0 until ids.length()).map { ids.optInt(it) }
                                    }.orEmpty(),
                                    url = item.optString("url").takeIf { it.isNotBlank() },
                                ),
                            )
                        }
                    }
                    val generatedFiles = mutableListOf<AiGeneratedFile>()
                    obj.optJSONArray("generatedFiles")?.let { fileArray ->
                        for (i in 0 until fileArray.length()) {
                            val item = fileArray.optJSONObject(i) ?: continue
                            val path = item.optString("path")
                            val name = item.optString("name").ifBlank {
                                path.substringAfterLast('/')
                            }
                            if (path.isBlank() || name.isBlank()) continue
                            generatedFiles.add(
                                AiGeneratedFile(
                                    path = path,
                                    name = name,
                                    sizeBytes = item.optLong("size"),
                                    kind = aiGeneratedFileKindOf(name),
                                ),
                            )
                        }
                    }
                    val content = obj.optString("content")
                    val recovered = if (generatedFiles.isEmpty() && role == "assistant") {
                        recoverGeneratedFiles(content)
                    } else {
                        emptyList()
                    }
                    val sources = mutableListOf<AiSource>()
                    obj.optJSONArray("sources")?.let { sourceArray ->
                        for (i in 0 until sourceArray.length()) {
                            val item = sourceArray.optJSONObject(i) ?: continue
                            val url = item.optString("url")
                            if (url.isBlank()) continue
                            sources.add(
                                AiSource(
                                    index = item.optInt("index", i + 1),
                                    url = url,
                                    title = item.optString("title").ifBlank { url },
                                    siteName = item.optString("siteName"),
                                    snippet = item.optString("snippet"),
                                    publishedAt = item.optString("publishedAt")
                                        .takeIf { it.isNotBlank() },
                                ),
                            )
                        }
                    }
                    add(
                        AiUiMessage(
                            id = obj.optLong("id", index.toLong() + 1L),
                            role = role,
                            initialContent = content,
                            initialReasoning = obj.optString("reasoning"),
                            attachments = attachments,
                            fileContext = obj.optString("fileContext"),
                            initialReasoningElapsedMs = obj.optLong("reasoningElapsedMs"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            initialUsageTokens = obj.optInt("usageTokens"),
                            initialUsageEstimated = obj.optBoolean("usageEstimated"),
                            initialError = obj.optString("error").takeIf { it.isNotBlank() },
                            initialErrorAction = runCatching {
                                AiErrorAction.valueOf(obj.optString("errorAction"))
                            }.getOrNull(),
                            initialReasoningDone = role == "assistant",
                            initialSteps = steps,
                            initialGeneratedFiles = generatedFiles + recovered,
                            initialSources = sources,
                            initialContinuable = obj.optBoolean("continuable"),
                            parentId = if (obj.has("parentId") && !obj.isNull("parentId")) {
                                obj.optLong("parentId")
                            } else {
                                null
                            },
                            initialSelectedChildId =
                            if (obj.has("selectedChildId") && !obj.isNull("selectedChildId")) {
                                obj.optLong("selectedChildId")
                            } else {
                                null
                            },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun loadRoot(context: Context, id: String): Long? {
        val raw = runCatching { file(context, id).readText() }.getOrNull() ?: return null
        return runCatching {
            val root = JSONObject(raw)
            if (root.has("rootMessageId") && !root.isNull("rootMessageId")) {
                root.optLong("rootMessageId")
            } else {
                null
            }
        }.getOrNull()
    }

    fun raw(context: Context, id: String): String? =
        runCatching { file(context, id).takeIf { it.isFile }?.readText() }.getOrNull()

    fun writeRaw(context: Context, id: String, raw: String) {
        runCatching { file(context, id).writeText(raw) }
    }

    fun save(
        context: Context,
        id: String,
        messages: List<SavedMessage>,
        rootMessageId: Long? = null,
    ) {
        val target = file(context, id)
        if (messages.isEmpty()) {
            if (id != "legacy") target.delete()
            return
        }
        runCatching {
            val array = JSONArray()
            messages.forEach { message ->
                array.put(
                    JSONObject().apply {
                        put("id", message.id)
                        message.parentId?.let { put("parentId", it) }
                        message.selectedChildId?.let { put("selectedChildId", it) }
                        put("role", message.role)
                        put("content", message.content.take(MAX_CONTENT_CHARS))
                        put("reasoning", message.reasoning.take(MAX_CONTENT_CHARS))
                        put("reasoningElapsedMs", message.reasoningElapsedMs)
                        put("fileContext", message.fileContext.take(MAX_FILE_CONTEXT_CHARS))
                        put("createdAt", message.createdAt)
                        put("usageTokens", message.usageTokens)
                        put("usageEstimated", message.usageEstimated)
                        if (message.continuable) put("continuable", true)
                        if (!message.error.isNullOrBlank()) {
                            put("error", message.error.take(4000))
                        }
                        message.errorAction?.let { put("errorAction", it.name) }
                        if (message.steps.isNotEmpty()) {
                            put(
                                "steps",
                                JSONArray().apply {
                                    message.steps.forEach { step ->
                                        put(
                                            JSONObject().apply {
                                                put("id", step.id)
                                                put("title", step.title.take(200))
                                                put("detail", step.detail.take(300))
                                                put("state", step.state.name)
                                                put("kind", step.kind.name)
                                                if (step.sourceIds.isNotEmpty()) {
                                                    put(
                                                        "sourceIds",
                                                        JSONArray().apply {
                                                            step.sourceIds.forEach { put(it) }
                                                        },
                                                    )
                                                }
                                                step.url?.let { put("url", it.take(1000)) }
                                            },
                                        )
                                    }
                                },
                            )
                        }
                        if (message.generatedFiles.isNotEmpty()) {
                            put(
                                "generatedFiles",
                                JSONArray().apply {
                                    message.generatedFiles.forEach { file ->
                                        put(
                                            JSONObject().apply {
                                                put("path", file.path)
                                                put("name", file.name)
                                                put("size", file.sizeBytes)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                        put(
                            "attachments",
                            JSONArray().apply {
                                message.attachments.forEach { attachment ->
                                    put(
                                        JSONObject().apply {
                                            put("name", attachment.name)
                                            put("size", attachment.sizeBytes)
                                            put("chars", attachment.chars)
                                            attachment.imagePath?.let {
                                                put("imagePath", it)
                                            }
                                        },
                                    )
                                }
                            },
                        )
                        if (message.sources.isNotEmpty()) {
                            put(
                                "sources",
                                JSONArray().apply {
                                    message.sources.forEach { source ->
                                        put(
                                            JSONObject().apply {
                                                put("index", source.index)
                                                put("url", source.url)
                                                put("title", source.title.take(300))
                                                put("siteName", source.siteName.take(120))
                                                put("snippet", source.snippet.take(500))
                                                source.publishedAt?.let {
                                                    put("publishedAt", it.take(120))
                                                }
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    },
                )
            }
            val root = JSONObject().put("messages", array)
            rootMessageId?.let { root.put("rootMessageId", it) }
            target.writeText(root.toString())
        }
    }

    fun delete(context: Context, id: String) {
        runCatching { file(context, id).delete() }
    }
}

/** 会话索引：active 会话 + 侧边栏元数据。 */
internal object AiConversationIndex {

    private const val FILE_NAME = "ai_conversations.json"

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    fun load(context: Context): StoredConversationIndex? {
        val raw = runCatching { file(context).readText() }.getOrNull() ?: return null
        return runCatching {
            val root = JSONObject(raw)
            val array = root.optJSONArray("conversations") ?: return null
            val conversations = buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
                    add(
                        AiConversationMeta(
                            id = id,
                            title = obj.optString("title").ifBlank { "新对话" },
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        ),
                    )
                }
            }
            StoredConversationIndex(
                activeId = root.optString("active").takeIf { it.isNotBlank() },
                conversations = conversations,
            )
        }.getOrNull()
    }

    fun save(context: Context, activeId: String?, conversations: List<AiConversationMeta>) {
        runCatching {
            val array = JSONArray()
            conversations.forEach { meta ->
                array.put(
                    JSONObject().apply {
                        put("id", meta.id)
                        put("title", meta.title)
                        put("updatedAt", meta.updatedAt)
                    },
                )
            }
            val root = JSONObject()
                .put("active", activeId.orEmpty())
                .put("conversations", array)
            file(context).writeText(root.toString())
        }
    }

    fun delete(context: Context) {
        runCatching { file(context).delete() }
    }
}

internal data class StoredConversationIndex(
    val activeId: String?,
    val conversations: List<AiConversationMeta>,
)
