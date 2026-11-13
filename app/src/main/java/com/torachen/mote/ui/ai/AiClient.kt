@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 轻量 AI 客户端：
 * - OpenAI Chat Completions（含 DeepSeek / 通义 / Kimi / 智谱 / 硅基流动 / OpenRouter 等兼容服务）
 * - Anthropic Messages
 * - Google Gemini
 *
 * 只依赖项目已有的 OkHttp + org.json，不额外引入网络库或序列化库。
 */
object AiClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.SECONDS)
        .build()

    // ============================================================
    // 对话
    // ============================================================

    fun streamChat(
        provider: AiProviderConfig,
        systemPrompt: String,
        messages: List<AiPromptMessage>,
        stream: Boolean = true,
        reasoning: AiReasoningLevel = AiSettings.reasoningLevel,
        onCall: (Call) -> Unit = {},
    ): Flow<AiStreamEvent> = flow {
        if (stream) {
            when (provider.protocol) {
                AiProtocol.OpenAi -> streamOpenAi(provider, systemPrompt, messages, reasoning, onCall)
                AiProtocol.Anthropic -> streamAnthropic(provider, systemPrompt, messages, reasoning, onCall)
                AiProtocol.Gemini -> streamGemini(provider, systemPrompt, messages, reasoning, onCall)
            }
        } else {
            val result = completeChat(provider, systemPrompt, messages, reasoning, onCall)
            if (result.reasoning.isNotBlank()) {
                emit(AiStreamEvent.Reasoning(result.reasoning))
            }
            if (result.content.isNotBlank()) {
                emit(AiStreamEvent.Content(result.content))
            }
            emit(AiStreamEvent.Done)
        }
    }.flowOn(Dispatchers.IO)

    private data class Completion(val reasoning: String, val content: String)

    // ============================================================
    // 模型列表 / 连通性测试
    // ============================================================

    suspend fun fetchModels(provider: AiProviderConfig): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = when (provider.protocol) {
                    AiProtocol.OpenAi -> endpoint(provider.baseUrl, "/models")
                    AiProtocol.Anthropic -> endpoint(provider.baseUrl, "/models")
                    AiProtocol.Gemini ->
                        endpoint(provider.baseUrl, "/models") + "?key=" + provider.apiKey
                }
                val request = baseRequest(provider, url)
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = readBody(response)
                    ensureSuccessful(response, body)
                    when (provider.protocol) {
                        AiProtocol.OpenAi, AiProtocol.Anthropic -> {
                            val array = JSONObject(body).optJSONArray("data") ?: JSONArray()
                            buildList {
                                for (index in 0 until array.length()) {
                                    val obj = array.optJSONObject(index) ?: continue
                                    obj.stringOrNull("id")?.let(::add)
                                }
                            }.distinct()
                        }

                        AiProtocol.Gemini -> {
                            val array = JSONObject(body).optJSONArray("models") ?: JSONArray()
                            buildList {
                                for (index in 0 until array.length()) {
                                    val obj = array.optJSONObject(index) ?: continue
                                    val name = obj.stringOrNull("name") ?: continue
                                    add(name.removePrefix("models/"))
                                }
                            }.distinct()
                        }
                    }
                }
            }
        }

    // ============================================================
    // OpenAI 兼容
    // ============================================================

    private suspend fun FlowCollector<AiStreamEvent>.streamOpenAi(
        provider: AiProviderConfig,
        systemPrompt: String,
        messages: List<AiPromptMessage>,
        reasoning: AiReasoningLevel,
        onCall: (Call) -> Unit,
    ) {
        val body = JSONObject().apply {
            put("model", provider.model)
            put("messages", openAiMessages(systemPrompt, messages))
            put("stream", true)
            applyOpenAiReasoning(provider, reasoning)
        }
        val response = execute(
            provider = provider,
            url = endpoint(provider.baseUrl, "/chat/completions"),
            body = body,
            accept = "text/event-stream",
            onCall = onCall,
        )
        response.use { res ->
            val raw = if (res.isSuccessful) null else readBody(res)
            ensureSuccessful(res, raw)
            val source = res.body?.source() ?: throw IOException("接口未返回内容")
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") break
                if (data.isEmpty()) continue
                val obj = runCatching { JSONObject(data) }.getOrNull() ?: continue
                obj.optJSONObject("error")?.let { error ->
                    throw IOException(error.stringOrNull("message") ?: "接口返回错误")
                }
                val delta = obj.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("delta")
                    ?: continue
                val reasoning = delta.stringOrNull("reasoning_content")
                    ?: delta.stringOrNull("reasoning")
                if (!reasoning.isNullOrEmpty()) {
                    emit(AiStreamEvent.Reasoning(reasoning))
                }
                val content = delta.stringOrNull("content")
                if (!content.isNullOrEmpty()) {
                    emit(AiStreamEvent.Content(content))
                }
            }
        }
        emit(AiStreamEvent.Done)
    }

    private fun openAiMessages(systemPrompt: String, messages: List<AiPromptMessage>): JSONArray {
        val array = JSONArray()
        if (systemPrompt.isNotBlank()) {
            array.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        messages.forEach { message ->
            if (message.text.isBlank() && message.images.isEmpty()) return@forEach
            val obj = JSONObject().put("role", message.role)
            if (message.images.isEmpty()) {
                obj.put("content", message.text)
            } else {
                val parts = JSONArray()
                if (message.text.isNotBlank()) {
                    parts.put(JSONObject().put("type", "text").put("text", message.text))
                }
                message.images.forEach { image ->
                    parts.put(
                        JSONObject()
                            .put("type", "image_url")
                            .put(
                                "image_url",
                                JSONObject().put(
                                    "url",
                                    "data:${image.mimeType};base64,${image.base64}",
                                ),
                            ),
                    )
                }
                obj.put("content", parts)
            }
            array.put(obj)
        }
        return array
    }

    // ============================================================
    // Anthropic
    // ============================================================

    private suspend fun FlowCollector<AiStreamEvent>.streamAnthropic(
        provider: AiProviderConfig,
        systemPrompt: String,
        messages: List<AiPromptMessage>,
        reasoning: AiReasoningLevel,
        onCall: (Call) -> Unit,
    ) {
        val body = JSONObject().apply {
            put("model", provider.model)
            put("max_tokens", provider.specFor(provider.model).maxOutputTokens.coerceIn(256, 200_000))
            put("stream", true)
            if (systemPrompt.isNotBlank()) put("system", systemPrompt)
            if (provider.supportsReasoning) {
                if (isDeepSeekProvider(provider)) {
                    put(
                        "reasoning",
                        JSONObject().put("effort", reasoning.effort),
                    )
                } else {
                    put(
                        "thinking",
                        JSONObject()
                            .put(
                                "type",
                                if (reasoning == AiReasoningLevel.Off) "disabled" else "enabled",
                            )
                            .put("budget_tokens", reasoning.budgetTokens.coerceAtMost((provider.specFor(provider.model).maxOutputTokens - 1024).coerceAtLeast(1024))),
                    )
                }
            }
            put("messages", anthropicMessages(messages))
        }
        val response = execute(
            provider = provider,
            url = endpoint(provider.baseUrl, "/messages"),
            body = body,
            accept = "text/event-stream",
            onCall = onCall,
        )
        response.use { res ->
            val raw = if (res.isSuccessful) null else readBody(res)
            ensureSuccessful(res, raw)
            val source = res.body?.source() ?: throw IOException("接口未返回内容")
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data.isEmpty() || data == "[DONE]") continue
                val obj = runCatching { JSONObject(data) }.getOrNull() ?: continue
                when (obj.stringOrNull("type")) {
                    "content_block_delta" -> {
                        val delta = obj.optJSONObject("delta") ?: continue
                        when (delta.stringOrNull("type")) {
                            "thinking_delta" -> delta.stringOrNull("thinking")
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { emit(AiStreamEvent.Reasoning(it)) }

                            "text_delta" -> delta.stringOrNull("text")
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { emit(AiStreamEvent.Content(it)) }
                        }
                    }

                    "message_stop" -> break
                    "error" -> {
                        val error = obj.optJSONObject("error")
                        throw IOException(error?.stringOrNull("message") ?: "接口返回错误")
                    }
                }
            }
        }
        emit(AiStreamEvent.Done)
    }

    private fun anthropicMessages(messages: List<AiPromptMessage>): JSONArray {
        val array = JSONArray()
        mergeConsecutiveRoles(messages).forEach { message ->
            if (message.role == "system") return@forEach
            if (message.text.isBlank() && message.images.isEmpty()) return@forEach
            val obj = JSONObject().put(
                "role",
                if (message.role == "assistant") "assistant" else "user",
            )
            if (message.images.isEmpty()) {
                obj.put("content", message.text)
            } else {
                val blocks = JSONArray()
                message.images.forEach { image ->
                    blocks.put(
                        JSONObject()
                            .put("type", "image")
                            .put(
                                "source",
                                JSONObject()
                                    .put("type", "base64")
                                    .put("media_type", image.mimeType)
                                    .put("data", image.base64),
                            ),
                    )
                }
                if (message.text.isNotBlank()) {
                    blocks.put(JSONObject().put("type", "text").put("text", message.text))
                }
                obj.put("content", blocks)
            }
            array.put(obj)
        }
        return array
    }

    // ============================================================
    // Gemini
    // ============================================================

    private suspend fun FlowCollector<AiStreamEvent>.streamGemini(
        provider: AiProviderConfig,
        systemPrompt: String,
        messages: List<AiPromptMessage>,
        reasoning: AiReasoningLevel,
        onCall: (Call) -> Unit,
    ) {
        val model = geminiModel(provider.model)
        val body = JSONObject().apply {
            put("contents", geminiContents(messages))
            if (systemPrompt.isNotBlank()) {
                put(
                    "systemInstruction",
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", systemPrompt)),
                    ),
                )
            }
            val generationConfig = JSONObject().apply {
                put(
                    "maxOutputTokens",
                    provider.specFor(provider.model).maxOutputTokens.coerceIn(256, 200_000),
                )
                if (provider.supportsReasoning) {
                    put(
                        "thinkingConfig",
                        JSONObject().apply {
                            put("includeThoughts", reasoning != AiReasoningLevel.Off)
                            put("thinkingBudget", reasoning.budgetTokens)
                        },
                    )
                }
            }
            put("generationConfig", generationConfig)
        }
        val url = endpoint(provider.baseUrl, "/models/$model:streamGenerateContent") +
                "?alt=sse&key=" + provider.apiKey
        val response = execute(
            provider = provider,
            url = url,
            body = body,
            accept = "text/event-stream",
            onCall = onCall,
        )
        response.use { res ->
            val raw = if (res.isSuccessful) null else readBody(res)
            ensureSuccessful(res, raw)
            val source = res.body?.source() ?: throw IOException("接口未返回内容")
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data.isEmpty()) continue
                val obj = runCatching { JSONObject(data) }.getOrNull() ?: continue
                obj.optJSONObject("error")?.let { error ->
                    throw IOException(error.stringOrNull("message") ?: "接口返回错误")
                }
                emitGeminiParts(obj)
            }
        }
        emit(AiStreamEvent.Done)
    }

    private suspend fun FlowCollector<AiStreamEvent>.emitGeminiParts(obj: JSONObject) {
        val candidates = obj.optJSONArray("candidates") ?: return
        val parts = candidates.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?: return
        for (index in 0 until parts.length()) {
            val part = parts.optJSONObject(index) ?: continue
            val text = part.stringOrNull("text") ?: continue
            if (text.isEmpty()) continue
            if (part.optBoolean("thought", false)) {
                emit(AiStreamEvent.Reasoning(text))
            } else {
                emit(AiStreamEvent.Content(text))
            }
        }
    }

    private fun geminiModel(model: String): String = model.trim().removePrefix("models/")

    private fun geminiContents(messages: List<AiPromptMessage>): JSONArray {
        val array = JSONArray()
        mergeConsecutiveRoles(messages).forEach { message ->
            if (message.text.isBlank() && message.images.isEmpty()) return@forEach
            val parts = JSONArray()
            if (message.text.isNotBlank()) {
                parts.put(JSONObject().put("text", message.text))
            }
            message.images.forEach { image ->
                parts.put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject()
                            .put("mimeType", image.mimeType)
                            .put("data", image.base64),
                    ),
                )
            }
            array.put(
                JSONObject()
                    .put("role", if (message.role == "assistant") "model" else "user")
                    .put("parts", parts),
            )
        }
        return array
    }

    // ============================================================
    // 非流式
    // ============================================================

    private fun completeChat(
        provider: AiProviderConfig,
        systemPrompt: String,
        messages: List<AiPromptMessage>,
        reasoning: AiReasoningLevel,
        onCall: (Call) -> Unit,
    ): Completion {
        val (url, body) = when (provider.protocol) {
            AiProtocol.OpenAi -> {
                val json = JSONObject().apply {
                    put("model", provider.model)
                    put("messages", openAiMessages(systemPrompt, messages))
                    put("stream", false)
                    applyOpenAiReasoning(provider, reasoning)
                }
                endpoint(provider.baseUrl, "/chat/completions") to json
            }

            AiProtocol.Anthropic -> {
                val json = JSONObject().apply {
                    put("model", provider.model)
                    put("max_tokens", provider.specFor(provider.model).maxOutputTokens.coerceIn(256, 200_000))
                    if (systemPrompt.isNotBlank()) put("system", systemPrompt)
                    if (provider.supportsReasoning) {
                        if (isDeepSeekProvider(provider)) {
                            put(
                                "reasoning",
                                JSONObject().put("effort", reasoning.effort),
                            )
                        } else {
                            put(
                                "thinking",
                                JSONObject()
                                    .put(
                                        "type",
                                        if (reasoning == AiReasoningLevel.Off) {
                                            "disabled"
                                        } else {
                                            "enabled"
                                        },
                                    )
                                    .put("budget_tokens", reasoning.budgetTokens.coerceAtMost((provider.specFor(provider.model).maxOutputTokens - 1024).coerceAtLeast(1024))),
                            )
                        }
                    }
                    put("messages", anthropicMessages(messages))
                    put("stream", false)
                }
                endpoint(provider.baseUrl, "/messages") to json
            }

            AiProtocol.Gemini -> {
                val model = geminiModel(provider.model)
                val json = JSONObject().apply {
                    put("contents", geminiContents(messages))
                    if (systemPrompt.isNotBlank()) {
                        put(
                            "systemInstruction",
                            JSONObject().put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", systemPrompt)),
                            ),
                        )
                    }
                    val generationConfig = JSONObject().apply {
                        put(
                            "maxOutputTokens",
                            provider.specFor(provider.model).maxOutputTokens.coerceIn(256, 200_000),
                        )
                        if (provider.supportsReasoning) {
                            put(
                                "thinkingConfig",
                                JSONObject().apply {
                                    put("includeThoughts", reasoning != AiReasoningLevel.Off)
                                    put("thinkingBudget", reasoning.budgetTokens)
                                },
                            )
                        }
                    }
                    put("generationConfig", generationConfig)
                }
                (endpoint(provider.baseUrl, "/models/$model:generateContent") +
                        "?key=" + provider.apiKey) to json
            }
        }

        val response = execute(
            provider = provider,
            url = url,
            body = body,
            accept = "application/json",
            onCall = onCall,
        )
        response.use { res ->
            val raw = readBody(res)
            ensureSuccessful(res, raw)
            val json = JSONObject(raw)
            return when (provider.protocol) {
                AiProtocol.OpenAi -> {
                    val message = json.optJSONArray("choices")
                        ?.optJSONObject(0)
                        ?.optJSONObject("message")
                    val reasoning = message?.stringOrNull("reasoning_content")
                        ?: message?.stringOrNull("reasoning")
                        ?: ""
                    Completion(
                        reasoning = reasoning,
                        content = message?.stringOrNull("content") ?: "",
                    )
                }

                AiProtocol.Anthropic -> {
                    val builder = StringBuilder()
                    val reasoning = StringBuilder()
                    val content = json.optJSONArray("content") ?: JSONArray()
                    for (index in 0 until content.length()) {
                        val block = content.optJSONObject(index) ?: continue
                        when (block.stringOrNull("type")) {
                            "text" -> builder.append(block.stringOrNull("text").orEmpty())
                            "thinking" -> reasoning.append(block.stringOrNull("thinking").orEmpty())
                        }
                    }
                    Completion(reasoning.toString(), builder.toString())
                }

                AiProtocol.Gemini -> {
                    val builder = StringBuilder()
                    val reasoning = StringBuilder()
                    val parts = json.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                    if (parts != null) {
                        for (index in 0 until parts.length()) {
                            val part = parts.optJSONObject(index) ?: continue
                            val text = part.stringOrNull("text") ?: continue
                            if (part.optBoolean("thought", false)) {
                                reasoning.append(text)
                            } else {
                                builder.append(text)
                            }
                        }
                    }
                    Completion(reasoning.toString(), builder.toString())
                }
            }
        }
    }

    // ============================================================
    // 网络工具
    // ============================================================

    /** Anthropic / Gemini 要求 user / assistant 交替，把连续同角色消息合并成一条。 */
    private fun mergeConsecutiveRoles(messages: List<AiPromptMessage>): List<AiPromptMessage> {
        val result = mutableListOf<AiPromptMessage>()
        messages.forEach { message ->
            val last = result.lastOrNull()
            if (last != null && last.role == message.role) {
                result[result.lastIndex] = last.copy(
                    text = listOf(last.text, message.text)
                        .filter { it.isNotBlank() }
                        .joinToString("\n\n"),
                    images = last.images + message.images,
                )
            } else {
                result.add(message)
            }
        }
        return result
    }

    private fun endpoint(baseUrl: String, path: String): String {
        val base = baseUrl.trim().trimEnd('/')
        return if (base.isEmpty()) path else base + path
    }

    private fun baseRequest(provider: AiProviderConfig, url: String): Request.Builder {
        val builder = Request.Builder().url(url)
        when (provider.protocol) {
            AiProtocol.OpenAi -> {
                if (provider.apiKey.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer ${provider.apiKey.trim()}")
                }
            }

            AiProtocol.Anthropic -> {
                if (provider.apiKey.isNotBlank()) {
                    builder.addHeader("x-api-key", provider.apiKey.trim())
                }
                builder.addHeader("anthropic-version", "2023-06-01")
            }

            AiProtocol.Gemini -> Unit
        }
        if (url.contains("openrouter.ai")) {
            builder.addHeader("HTTP-Referer", "https://mote.torachen.com")
            builder.addHeader("X-Title", "Mote Browser")
        }
        builder.addHeader("User-Agent", "Mote/Android")
        return builder
    }

    private fun execute(
        provider: AiProviderConfig,
        url: String,
        body: JSONObject,
        accept: String,
        onCall: (Call) -> Unit,
    ): Response {
        val request = baseRequest(provider, url)
            .addHeader("Accept", accept)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        val call = client.newCall(request)
        onCall(call)
        return call.execute()
    }

    private fun readBody(response: Response): String {
        val body = response.body ?: return ""
        return runCatching {
            val source = body.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            buffer.readUtf8()
        }.getOrElse { "" }.take(16_000)
    }

    private fun ensureSuccessful(response: Response, rawBody: String?) {
        if (response.isSuccessful) return
        val detail = parseErrorDetail(rawBody.orEmpty())
        throw AiHttpException(response.code, detail)
    }

    private fun parseErrorDetail(raw: String): String {
        if (raw.isBlank()) return "服务端未返回错误详情"
        val trimmed = raw.trim()
        return runCatching {
            val json = JSONObject(trimmed)
            json.optJSONObject("error")?.let { error ->
                return error.stringOrNull("message")
                    ?: error.stringOrNull("type")
                    ?: trimmed.take(200)
            }
            json.stringOrNull("error")
                ?: json.stringOrNull("message")
                ?: json.stringOrNull("detail")
                ?: trimmed.take(200)
        }.getOrElse {
            if (trimmed.startsWith("<")) {
                trimmed.replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(200)
            } else {
                trimmed.take(200)
            }
        }
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotEmpty() }
    }
}

/** 带 HTTP 状态码的接口异常，便于给出中文错误提示。 */
internal class AiHttpException(val code: Int, val detail: String) :
    IOException("HTTP $code: $detail")

/** 把异常翻译成适合直接展示给用户的中文提示。 */
internal fun aiErrorMessage(throwable: Throwable): String {
    if (throwable is AiHttpException) {
        return when (throwable.code) {
            400 ->
                "请求参数有误（HTTP 400）：${throwable.detail.take(200)}\n" +
                        "请检查模型名称是否有效，可回到服务设置点击「获取模型列表」重新选择。"

            401, 403 ->
                "鉴权失败（HTTP ${throwable.code}）：API Key 无效或没有访问该模型的权限"

            404 ->
                "接口地址或模型不存在（HTTP 404）：请检查 API 地址是否包含 /v1，以及模型名是否正确"

            408 -> "请求超时（HTTP 408），请稍后重试"
            413 -> "请求内容过大（HTTP 413），请减少附件或上下文"
            429 -> "请求过于频繁或额度不足（HTTP 429），请稍后重试"
            in 500..599 -> "服务端错误（HTTP ${throwable.code}），请稍后重试"
            else -> "请求失败（HTTP ${throwable.code}）：${throwable.detail.take(240)}"
        }
    }
    val message = throwable.message.orEmpty()
    return when {
        message.contains("timeout", ignoreCase = true) ->
            "请求超时，请检查网络连接或接口地址"

        message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("Failed to connect", ignoreCase = true) ||
                throwable is java.net.UnknownHostException ||
                throwable is java.net.ConnectException ->
            "无法连接服务器，请检查网络、接口地址或代理设置"

        message.contains("Canceled", ignoreCase = true) -> "请求已取消"
        message.isBlank() -> "请求失败，请稍后重试"
        else -> message
    }
}

private fun isDeepSeekProvider(provider: AiProviderConfig): Boolean =
    provider.presetId == "deepseek" ||
            provider.model.contains("deepseek", ignoreCase = true)

private fun JSONObject.applyOpenAiReasoning(
    provider: AiProviderConfig,
    reasoning: AiReasoningLevel,
) {
    if (!provider.supportsReasoning || !isDeepSeekProvider(provider)) return
    put(
        "thinking",
        JSONObject().put(
            "type",
            if (reasoning == AiReasoningLevel.Off) "disabled" else "enabled",
        ),
    )
    if (reasoning != AiReasoningLevel.Off) {
        put("reasoning_effort", reasoning.effort)
        when (reasoning) {
            AiReasoningLevel.High -> put("max_tokens", 8_192)
            AiReasoningLevel.Extreme -> put("max_tokens", 16_384)
            else -> Unit
        }
    }
}
