@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

/** 服务商接口协议。 */
enum class AiProtocol(val id: String, val label: String) {
    OpenAi("openai", "OpenAI 兼容"),
    Anthropic("anthropic", "Anthropic"),
    Gemini("gemini", "Google Gemini"),
}

/** 内置预设。 */
data class AiProviderPreset(
    val id: String,
    val name: String,
    val protocol: AiProtocol,
    val baseUrl: String,
    val defaultModel: String,
    val models: List<String>,
    val brandColor: Long,
    val iconUrl: String? = null,
    val keyHint: String = "",
    val helpUrl: String? = null,
    val supportsVision: Boolean = false,
    val supportsReasoning: Boolean = false,
    val needsEndpointId: Boolean = false,
)

/** 单个模型的运行参数与输入能力。 */
data class AiModelSpec(
    val contextWindow: Int = 1_048_576,
    val maxOutputTokens: Int = 131_072,
    val supportsText: Boolean = true,
    val supportsImage: Boolean = false,
)

/** 一条用户已保存的服务配置。 */
data class AiProviderConfig(
    val id: String,
    val presetId: String?,
    val name: String,
    val protocol: AiProtocol,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val models: List<String> = emptyList(),
    val supportsVision: Boolean = false,
    val supportsReasoning: Boolean = false,
    val modelAliases: Map<String, String> = emptyMap(),
    val modelSpecs: Map<String, AiModelSpec> = emptyMap(),
) {
    val isLocalEndpoint: Boolean
        get() = baseUrl.contains("127.0.0.1") ||
                baseUrl.contains("localhost") ||
                baseUrl.contains("0.0.0.0") ||
                baseUrl.contains("[::1]")

    val isConfigured: Boolean
        get() = model.isNotBlank() && (apiKey.isNotBlank() || isLocalEndpoint)

    fun displayModelName(modelId: String): String {
        if (modelId.isBlank()) return ""
        modelAliases[modelId]?.takeIf { it.isNotBlank() }?.let { return it }
        return autoModelDisplayName(presetId, modelId)
    }

    fun specFor(modelId: String): AiModelSpec =
        modelSpecs[modelId] ?: AiModelSpec(supportsImage = supportsVision)

    val summary: String
        get() {
            val modelLabel = if (model.isBlank()) {
                ""
            } else {
                "${displayModelName(model)} ($model)"
            }
            return listOf(modelLabel, protocol.label)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
        }
}

/** 解析完成、可以直接放进上下文的附件。 */
data class AiAttachment(
    val id: Long,
    val name: String,
    val mime: String,
    val sizeBytes: Long,
    val text: String,
    val imageMime: String? = null,
    val imageBase64: String? = null,
    val note: String? = null,
    val uriString: String = "",
    val imagePath: String? = null,
) {
    val meta: AiAttachmentMeta
        get() = AiAttachmentMeta(
            name = name,
            sizeBytes = sizeBytes,
            chars = text.length,
            imagePath = imagePath,
        )
}

/** 保存到对话记录里的附件摘要。 */
data class AiAttachmentMeta(
    val name: String,
    val sizeBytes: Long,
    val chars: Int,
    val imagePath: String? = null,
)

/** 发给模型的一条消息。 */
data class AiPromptMessage(
    val role: String,
    val text: String,
    val images: List<AiImagePart> = emptyList(),
)

/** 图片输入。 */
data class AiImagePart(
    val mimeType: String,
    val base64: String,
)

/** 流式事件。 */
sealed interface AiStreamEvent {
    data class Reasoning(val delta: String) : AiStreamEvent
    data class Content(val delta: String) : AiStreamEvent
    data object Done : AiStreamEvent
}

/** 思考区里一条工具动作的进度状态。 */
enum class AiStepState {
    Running,
    Done,
    Failed,
}

/**
 * 思考区的一步：只包含给人看的自然语言，例如
 * 「正在联网搜索「今天的新闻」」「已打开「微信」」「正在滑动屏幕」。
 * 工具调用的原始 JSON 不会进入这里。
 */
data class AiAgentStep(
    val id: Long,
    val title: String,
    val detail: String = "",
    val state: AiStepState = AiStepState.Running,
)

/** AI 在一次对话里生成的文件，用于对话底部的文件区展示与打开。 */
enum class AiGeneratedFileKind {
    Word,
    Excel,
    Ppt,
    Image,
    Other,
}

data class AiGeneratedFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val kind: AiGeneratedFileKind = AiGeneratedFileKind.Other,
)

internal fun aiGeneratedFileKindOf(fileName: String): AiGeneratedFileKind =
    when (fileName.substringAfterLast('.', "").lowercase()) {
        "docx" -> AiGeneratedFileKind.Word
        "xlsx" -> AiGeneratedFileKind.Excel
        "pptx" -> AiGeneratedFileKind.Ppt
        "png", "jpg", "jpeg", "webp", "gif", "bmp", "heic", "heif" -> AiGeneratedFileKind.Image
        else -> AiGeneratedFileKind.Other
    }

/** 内置预设列表：按模型综合能力与国内使用情况排序。 */
object AiPresets {

    val all: List<AiProviderPreset> = listOf(
        AiProviderPreset(
            id = "openai",
            name = "OpenAI",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.openai.com/v1",
            defaultModel = "gpt-4o-mini",
            models = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1", "o4-mini", "o3"),
            brandColor = 0xFF10A37F,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/openai.png",
            keyHint = "在 platform.openai.com 创建 API Key（sk- 开头）",
            helpUrl = "https://platform.openai.com/api-keys",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "anthropic",
            name = "Anthropic Claude",
            protocol = AiProtocol.Anthropic,
            baseUrl = "https://api.anthropic.com/v1",
            defaultModel = "claude-sonnet-4-5",
            models = listOf(
                "claude-sonnet-4-5",
                "claude-opus-4-1",
                "claude-3-7-sonnet-latest",
                "claude-3-5-haiku-latest",
            ),
            brandColor = 0xFFD97757,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/anthropic.png",
            keyHint = "在 console.anthropic.com 创建 API Key（sk-ant- 开头）",
            helpUrl = "https://console.anthropic.com/settings/keys",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "gemini",
            name = "Google Gemini",
            protocol = AiProtocol.Gemini,
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            defaultModel = "gemini-2.5-flash",
            models = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash"),
            brandColor = 0xFF4285F4,
            iconUrl = "https://www.gstatic.com/lamda/images/gemini_favicon_f069958c85030456e93de685481c559f160ea06b.png",
            keyHint = "在 Google AI Studio 创建 API Key（AIza 开头）",
            helpUrl = "https://aistudio.google.com/app/apikey",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "deepseek",
            name = "DeepSeek",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.deepseek.com/v1",
            defaultModel = "deepseek-v4-pro",
            models = listOf("deepseek-flash", "deepseek-v4-pro", "deepseek-reasoner"),
            brandColor = 0xFF4D6BFE,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/deepseek.png",
            keyHint = "在 platform.deepseek.com 创建 API Key（sk- 开头）",
            helpUrl = "https://platform.deepseek.com/api_keys",
            supportsVision = false,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "qwen",
            name = "通义千问",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            defaultModel = "qwen-plus",
            models = listOf("qwen-plus", "qwen-max", "qwen-turbo", "qwen3-max"),
            brandColor = 0xFF615CED,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/qwen.png",
            keyHint = "在阿里云百炼控制台创建 API Key（sk- 开头）",
            helpUrl = "https://bailian.console.aliyun.com/",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "mimo",
            name = "小米 MiMo",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.xiaomimimo.com/v1",
            defaultModel = "mimo-v2.6-flash",
            models = listOf(
                "mimo-v2.6-pro",
                "mimo-v2.6-flash",
                "mimo-v2.6-pro-ultraspeed",
            ),
            brandColor = 0xFFFF6900,
            iconUrl = null,
            keyHint = "在 platform.xiaomimimo.com 创建 API Key（sk- 开头）",
            helpUrl = "https://platform.xiaomimimo.com/#/console/api-keys",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "kimi",
            name = "月之暗面 Kimi",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.moonshot.cn/v1",
            defaultModel = "kimi-k2-0905-preview",
            models = listOf(
                "kimi-k2-0905-preview",
                "moonshot-v1-128k",
                "moonshot-v1-32k",
                "moonshot-v1-8k",
            ),
            brandColor = 0xFF1F1F1F,
            iconUrl = null,
            keyHint = "在 platform.moonshot.cn 创建 API Key（sk- 开头）",
            helpUrl = "https://platform.moonshot.cn/console/api-keys",
            supportsVision = true,
            supportsReasoning = false,
        ),
        AiProviderPreset(
            id = "zhipu",
            name = "智谱 GLM",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://open.bigmodel.cn/api/paas/v4",
            defaultModel = "glm-4.5",
            models = listOf("glm-4.5", "glm-4.5-air", "glm-4-plus", "glm-4-flash"),
            brandColor = 0xFF3859FF,
            iconUrl = null,
            keyHint = "在 bigmodel.cn 控制台创建 API Key",
            helpUrl = "https://bigmodel.cn/console/api-keys",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "doubao",
            name = "火山方舟 豆包",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3",
            defaultModel = "",
            models = emptyList(),
            brandColor = 0xFF1664FF,
            iconUrl = null,
            keyHint = "填入方舟控制台创建的 API Key，模型处填写「推理接入点 ID」（ep- 开头）",
            helpUrl = "https://console.volcengine.com/ark",
            supportsVision = true,
            supportsReasoning = false,
            needsEndpointId = true,
        ),
        AiProviderPreset(
            id = "hunyuan",
            name = "腾讯混元",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.hunyuan.cloud.tencent.com/v1",
            defaultModel = "hunyuan-turbos-latest",
            models = listOf("hunyuan-turbos-latest", "hunyuan-plus-latest", "hunyuan-t1-latest"),
            brandColor = 0xFF0052D9,
            iconUrl = null,
            keyHint = "在腾讯云混元控制台创建 API Key",
            helpUrl = "https://console.cloud.tencent.com/hunyuan",
            supportsVision = false,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "ernie",
            name = "百度文心",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://qianfan.baidubce.com/v2",
            defaultModel = "ernie-4.5-turbo-128k",
            models = listOf("ernie-4.5-turbo-128k", "ernie-4.5-8k-preview", "ernie-speed-128k"),
            brandColor = 0xFF2932E1,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/baidu.png",
            keyHint = "在百度智能云千帆控制台创建 API Key",
            helpUrl = "https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application",
            supportsVision = false,
            supportsReasoning = false,
        ),
        AiProviderPreset(
            id = "minimax",
            name = "MiniMax",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.minimax.chat/v1",
            defaultModel = "MiniMax-M2",
            models = listOf("MiniMax-M2", "MiniMax-Text-01", "abab6.5s-chat"),
            brandColor = 0xFFE73562,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/minimax.png",
            keyHint = "在 MiniMax 开放平台创建 API Key",
            helpUrl = "https://platform.minimaxi.com/",
            supportsVision = false,
            supportsReasoning = false,
        ),
        AiProviderPreset(
            id = "siliconflow",
            name = "硅基流动",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://api.siliconflow.cn/v1",
            defaultModel = "deepseek-ai/DeepSeek-V3",
            models = listOf(
                "deepseek-ai/DeepSeek-V3",
                "deepseek-ai/DeepSeek-R1",
                "Qwen/Qwen3-235B-A22B",
                "moonshotai/Kimi-K2-Instruct",
            ),
            brandColor = 0xFF6E56CF,
            iconUrl = null,
            keyHint = "在 cloud.siliconflow.cn 创建 API Key（sk- 开头）",
            helpUrl = "https://cloud.siliconflow.cn/account/ak",
            supportsVision = true,
            supportsReasoning = true,
        ),
        AiProviderPreset(
            id = "openrouter",
            name = "OpenRouter",
            protocol = AiProtocol.OpenAi,
            baseUrl = "https://openrouter.ai/api/v1",
            defaultModel = "openai/gpt-4o-mini",
            models = listOf(
                "openai/gpt-4o-mini",
                "anthropic/claude-sonnet-4.5",
                "google/gemini-2.5-flash",
                "deepseek/deepseek-chat",
            ),
            brandColor = 0xFF6366F1,
            iconUrl = "https://fastly.jsdelivr.net/gh/homarr-labs/dashboard-icons/png/open-router.png",
            keyHint = "在 openrouter.ai/keys 创建 API Key（sk-or- 开头）",
            helpUrl = "https://openrouter.ai/keys",
            supportsVision = true,
            supportsReasoning = true,
        ),
    )

    fun byId(id: String?): AiProviderPreset? = all.firstOrNull { it.id == id }
}

/** 推理/思考强度。 */
enum class AiReasoningLevel(
    val label: String,
    val effort: String,
    val budgetTokens: Int,
    val instruction: String,
) {
    Extreme(
        label = "极致",
        effort = "max",
        budgetTokens = 24_000,
        instruction = "进入极致推理模式，像 DSH Max 一样不要因为 token 消耗而压缩思考，" +
                "充分探索、交叉验证后再给出最终答案。",
    ),
    High(
        label = "高级",
        effort = "max",
        budgetTokens = 12_000,
        instruction = "请进行更充分的推理，在性能与 token 消耗之间适当偏向性能，" +
                "不要过早下结论。",
    ),
    Standard(
        label = "标准",
        effort = "high",
        budgetTokens = 4_096,
        instruction = "按正常强度思考，兼顾回答质量和响应速度。",
    ),
    Off(
        label = "关闭",
        effort = "none",
        budgetTokens = 0,
        instruction = "不要进行深度思考或输出思维链，直接给出最终答案，优先快速响应。",
    ),
}

/** 按服务商选择列表中最合适的最新模型。 */
fun preferredModelId(models: List<String>, presetId: String?): String {
    if (models.isEmpty()) return ""
    if (presetId == "deepseek") {
        return models.firstOrNull { it.contains("v4-pro", ignoreCase = true) }
            ?: models.firstOrNull { it.contains("pro", ignoreCase = true) }
            ?: models.first()
    }
    return models.firstOrNull { it.contains("latest", ignoreCase = true) }
        ?: models.firstOrNull { it.contains("pro", ignoreCase = true) }
        ?: models.first()
}

/** 按 DSH 等自动操作应用的版本展示规则，把原始模型 ID 映射成更直观的版本名。 */
fun autoModelDisplayName(presetId: String?, modelId: String): String {
    val id = modelId.trim()
    if (id.isEmpty()) return id
    val lower = id.lowercase()
    if (presetId == "deepseek" || lower.startsWith("deepseek")) {
        return when (lower) {
            "deepseek-flash" -> "deepseek-v4.1-flash"
            else -> id
        }
    }
    return id
}

/** 用量展示：123 tok / 1.2K tok / 1.2M tok。 */
fun formatTokenCount(tokens: Int): String = when {
    tokens >= 1_000_000 -> String.format("%.1fM", tokens / 1_000_000.0)
    tokens >= 1_000 -> String.format("%.1fK", tokens / 1_000.0)
    else -> tokens.toString()
}

/** 粗略估算 token 数：中日韩字符约 1 token，拉丁字符约 4 字符 1 token。 */
fun estimateTokens(text: String): Int {
    if (text.isBlank()) return 0
    var cjk = 0
    var other = 0
    text.forEach { ch ->
        if (ch.code > 0x2E80) cjk++ else other++
    }
    return (cjk + other / 4).coerceAtLeast(1)
}

/** 把毫秒格式化成「X 秒 / X 分 X 秒 / X 时 X 分 X 秒」。 */
fun formatAiElapsed(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(1L)
    return when {
        totalSeconds < 60L -> "${totalSeconds} 秒"
        totalSeconds < 3600L -> {
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            if (seconds == 0L) "$minutes 分" else "$minutes 分 $seconds 秒"
        }

        else -> {
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            val seconds = totalSeconds % 60L
            buildString {
                append(hours).append(" 时")
                if (minutes > 0L) append(" ").append(minutes).append(" 分")
                if (seconds > 0L) append(" ").append(seconds).append(" 秒")
            }
        }
    }
}

/** 文件大小展示。 */
fun formatAiFileSize(bytes: Long): String = when {
    bytes <= 0L -> ""
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0)
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
