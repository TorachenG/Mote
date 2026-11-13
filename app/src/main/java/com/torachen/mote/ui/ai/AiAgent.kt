@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.Context
import com.torachen.mote.service.MoteAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONArray
import org.json.JSONObject

/**
 * Mote AI 的 Agent 循环。
 *
 * 与旧实现（固定触发词 + 单步动作协议）不同，这里把「要不要用工具、用哪个工具」
 * 完全交给模型推理决定：
 * 1. 把当前可用工具清单写进 system prompt，模型因此知道自己能搜索、打开网页、
 *    打开应用、生成文档，也能在开启自动操作后读屏、点击、滑动、输入；
 * 2. 模型每一步可以调用 0~N 个工具（JSON 动作协议，兼容所有服务商）；
 * 3. 工具执行结果回传给模型，模型可以继续调用工具（分批、多轮），直到给出最终回答；
 * 4. 过程中通过 [onStep] 把「正在联网搜索」「正在打开微信」「正在滑动屏幕」这类
 *    可读状态分批推给界面，原始 JSON 永远不会出现在界面上。
 */
internal object AiAgentRunner {

    private const val MAX_ROUNDS = 20
    private const val MAX_CALLS_PER_ROUND = 4
    private const val MAX_RESULT_CHARS = 7_000
    private const val MAX_TOOL_MESSAGES = 30
    private const val TOOL_FENCE = "```mote-tool"

    data class Result(
        val text: String,
        val documentCreated: Boolean,
        val toolsUsed: List<String>,
    )

    fun isServiceConnected(): Boolean = MoteAccessibilityService.instance != null

    suspend fun run(
        context: Context,
        provider: AiProviderConfig,
        systemPrompt: String,
        history: List<AiPromptMessage>,
        userRequest: String,
        userImages: List<AiImagePart>,
        files: List<AiAttachment>,
        permission: AiFilePermission,
        reasoning: AiReasoningLevel,
        onThought: (String) -> Unit,
        onStep: (AiAgentStep) -> Unit,
        onContent: (String) -> Unit,
        onCall: (Call) -> Unit,
        onConfirmEdit: suspend (AiFileEditRequest) -> Boolean,
        onNotice: (String) -> Unit,
        onGeneratedFile: (AiGeneratedFile) -> Unit = {},
    ): Result {
        val env = AiToolEnv(
            context = context,
            files = files,
            permission = permission,
            webSearchEnabled = AiSettings.webSearchEnabled,
            deviceControlEnabled = AiSettings.agentEnabled,
            accessibilityReady = MoteAccessibilityService.instance != null,
            supportsVision = provider.specFor(provider.model).supportsImage,
        )
        val system = buildString {
            if (systemPrompt.isNotBlank()) append(systemPrompt.trim()).append("\n\n")
            append(aiToolSystemPrompt(env))
        }

        val messages = ArrayList<AiPromptMessage>()
        messages.addAll(history)
        messages.add(
            AiPromptMessage(
                role = "user",
                text = buildUserText(userRequest, files),
                images = userImages,
            ),
        )

        var documentCreated = false
        val toolsUsed = LinkedHashSet<String>()
        var stepId = 0L
        var parseFailures = 0

        // 先让用户看到「它查了一遍自己会什么」，这也是模型本轮真实拿到的工具清单。
        val availableTitles = aiAvailableToolTitles(env)
        val toolStepId = ++stepId
        onStep(
            AiAgentStep(
                id = toolStepId,
                title = "正在查询可用工具",
                state = AiStepState.Running,
            ),
        )
        onStep(
            AiAgentStep(
                id = toolStepId,
                title = "已加载 ${availableTitles.size} 个可用工具",
                detail = availableTitles.joinToString("、"),
                state = AiStepState.Done,
            ),
        )

        suspend fun streamRound(allowTools: Boolean): String {
            val gate = ContentGate(
                onDelta = onContent,
                holdToolCalls = allowTools,
            )
            val answer = StringBuilder()
            AiClient.streamChat(
                provider = provider,
                systemPrompt = system,
                messages = messages,
                stream = AiSettings.streamEnabled,
                reasoning = reasoning,
                onCall = onCall,
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Reasoning -> onThought(event.delta)
                    is AiStreamEvent.Content -> {
                        answer.append(event.delta)
                        gate.feed(event.delta)
                    }

                    AiStreamEvent.Done -> Unit
                }
            }
            val text = answer.toString().trim()
            if (text.isEmpty()) {
                gate.discard()
                return ""
            }
            val parse = if (allowTools) extractToolCalls(text) else ToolParse.None
            when (parse) {
                is ToolParse.Calls, is ToolParse.Failure -> gate.discard()
                is ToolParse.None -> gate.finish()
            }
            return text
        }

        for (round in 0 until MAX_ROUNDS) {
            val answer = streamRound(allowTools = true)
            if (answer.isEmpty()) {
                throw IllegalStateException("模型没有返回内容，请稍后重试")
            }
            when (val parse = extractToolCalls(answer)) {
                is ToolParse.None -> {
                    return Result(
                        text = answer,
                        documentCreated = documentCreated,
                        toolsUsed = toolsUsed.toList(),
                    )
                }

                is ToolParse.Failure -> {
                    parseFailures++
                    if (parseFailures > 2) {
                        throw IllegalStateException("模型连续返回了无法解析的工具调用，请重试或换一个模型")
                    }
                    messages.add(AiPromptMessage(role = "assistant", text = answer))
                    messages.add(
                        AiPromptMessage(
                            role = "user",
                            text = "上一轮的工具调用格式有误：${parse.message}。\n" +
                                    "请只输出一个 mote-tool 代码块，块内是 JSON 数组，" +
                                    "元素形如 {\"tool\":\"web_search\",\"arguments\":{\"query\":\"关键词\"}}；" +
                                    "或者直接用文字回答。",
                        ),
                    )
                    continue
                }

                is ToolParse.Calls -> {
                    if (parse.calls.isEmpty()) {
                        throw IllegalStateException("模型返回了空的工具调用")
                    }
                    val calls = parse.calls.take(MAX_CALLS_PER_ROUND)
                    if (parse.calls.size > MAX_CALLS_PER_ROUND) {
                        onNotice("本轮工具调用过多，只执行前 $MAX_CALLS_PER_ROUND 个")
                    }
                    val results = StringBuilder()
                    val images = mutableListOf<AiImagePart>()
                    calls.forEach { call ->
                        val id = ++stepId
                        onStep(
                            AiAgentStep(
                                id = id,
                                title = aiToolRunningTitle(call),
                                state = AiStepState.Running,
                            ),
                        )
                        val outcome = withContext(Dispatchers.Main.immediate) {
                            aiExecuteTool(
                                env = env,
                                call = call,
                                onConfirmEdit = onConfirmEdit,
                                onNotice = onNotice,
                            )
                        }
                        documentCreated = documentCreated || outcome.documentCreated
                        outcome.generatedFile?.let(onGeneratedFile)
                        toolsUsed.add(call.name)
                        onStep(
                            AiAgentStep(
                                id = id,
                                title = outcome.title,
                                detail = outcome.text.firstLine(80),
                                state = if (outcome.failed) AiStepState.Failed else AiStepState.Done,
                            ),
                        )
                        results.append("\n【").append(call.name).append("】")
                            .append(if (outcome.failed) "失败" else "成功")
                            .append('\n')
                            .append(outcome.text.take(MAX_RESULT_CHARS))
                            .append('\n')
                        outcome.image?.let(images::add)
                    }
                    messages.add(AiPromptMessage(role = "assistant", text = answer))
                    messages.add(
                        AiPromptMessage(
                            role = "user",
                            text = buildString {
                                append("工具执行结果：")
                                append(results)
                                append("\n请继续：如果还需要工具（例如补充搜索、读取具体网页、继续操作界面），")
                                append("按格式再调用；信息足够时直接用文字回答，不要输出工具代码块。")
                            },
                            images = images,
                        ),
                    )
                    trimToolHistory(messages)
                }
            }
        }

        // 达到工具轮数上限：再给模型一次纯回答机会。
        messages.add(
            AiPromptMessage(
                role = "user",
                text = "工具调用次数已达上限。请不要调用任何工具，直接用已经获得的信息回答用户，" +
                        "并说明还有哪些内容没能完成。",
            ),
        )
        val finalAnswer = streamRound(allowTools = false)
        if (finalAnswer.isEmpty()) {
            throw IllegalStateException("模型没有返回内容，请稍后重试")
        }
        return Result(
            text = finalAnswer,
            documentCreated = documentCreated,
            toolsUsed = toolsUsed.toList(),
        )
    }

    private fun buildUserText(request: String, files: List<AiAttachment>): String = buildString {
        if (files.isNotEmpty()) {
            append("用户附件：")
            files.forEachIndexed { index, file ->
                append("\n").append(index + 1).append(". ").append(file.name)
                if (file.uriString.isBlank()) append("（只读）")
            }
            append("\n\n")
        }
        append("用户请求：").append(request)
    }

    /** 工具结果会迅速占满上下文，只保留首条用户请求和最近的交换。 */
    private fun trimToolHistory(messages: MutableList<AiPromptMessage>) {
        while (messages.size > MAX_TOOL_MESSAGES && messages.size > 3) {
            messages.removeAt(1)
            if (messages.size > 3) messages.removeAt(1)
        }
    }

    // ============================================================
    // 解析
    // ============================================================

    private sealed interface ToolParse {
        data class Calls(val calls: List<AiToolCall>) : ToolParse
        data class Failure(val message: String) : ToolParse
        data object None : ToolParse
    }

    private fun extractToolCalls(answer: String): ToolParse {
        val text = answer.trim()
        if (text.isEmpty()) return ToolParse.None

        // 优先找工具代码块，允许模型在前面带一句“好的，我来查一下”之类的过渡语。
        val firstFence = text.indexOf(TOOL_FENCE)
        if (firstFence >= 0) {
            val calls = mutableListOf<AiToolCall>()
            var cursor = firstFence
            while (true) {
                val rest = text.substring(cursor).trimStart()
                if (!rest.startsWith(TOOL_FENCE)) break
                val bodyStart = TOOL_FENCE.length
                val end = rest.indexOf("```", bodyStart)
                if (end < 0) return ToolParse.Failure("工具代码块没有闭合")
                val body = rest.substring(bodyStart, end).trim()
                val parsed = parseCallPayload(body)
                    ?: return ToolParse.Failure("代码块内不是有效的 JSON 数组")
                calls.addAll(parsed)
                cursor = text.length - rest.length + end + 3
            }
            return if (calls.isEmpty()) {
                ToolParse.Failure("工具代码块为空")
            } else {
                ToolParse.Calls(calls)
            }
        }

        // 兼容模型把工具调用放进普通 JSON 代码块（例如 ```json）的情况。
        if (text.startsWith("```")) {
            val bodyStart = text.indexOf('\n')
            if (bodyStart in 1 until text.length) {
                val end = text.indexOf("```", bodyStart)
                if (end > bodyStart) {
                    val body = text.substring(bodyStart + 1, end).trim()
                    if (body.startsWith("{") || body.startsWith("[")) {
                        val parsed = parseCallPayload(body)
                        if (parsed != null) return ToolParse.Calls(parsed)
                    }
                }
            }
        }

        // 兼容部分模型直接输出 JSON（不带代码块）的情况，只接受回复开头的 JSON。
        if (text.startsWith("{") || text.startsWith("[")) {
            if (!text.take(400).contains("\"tool\"")) return ToolParse.None
            val payload = extractJsonPayload(text) ?: return ToolParse.None
            val parsed = parseCallPayload(payload) ?: return ToolParse.None
            return ToolParse.Calls(parsed)
        }
        return ToolParse.None
    }

    private fun parseCallPayload(raw: String): List<AiToolCall>? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val array = runCatching {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                JSONArray().put(JSONObject(trimmed))
            }
        }.getOrNull() ?: return null
        val calls = mutableListOf<AiToolCall>()
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val name = obj.optString("tool").trim()
                .ifBlank { obj.optString("name").trim() }
                .ifBlank { obj.optString("action").trim() }
            if (name.isBlank()) continue
            val args = obj.optJSONObject("arguments")
                ?: obj.optJSONObject("args")
                ?: obj.optJSONObject("parameters")
                ?: JSONObject()
            calls.add(AiToolCall(name, args))
        }
        return calls.takeIf { it.isNotEmpty() }
    }

    /** 从字符串开头截取一个完整的 JSON 对象或数组。 */
    private fun extractJsonPayload(text: String): String? {
        val first = text.firstOrNull() ?: return null
        if (first != '{' && first != '[') return null
        var depth = 0
        var inString = false
        var escaped = false
        for (index in text.indices) {
            val ch = text[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    ch == '\\' -> escaped = true
                    ch == '"' -> inString = false
                }
                continue
            }
            when (ch) {
                '"' -> inString = true
                '{', '[' -> depth++
                '}', ']' -> {
                    depth--
                    if (depth == 0) return text.substring(0, index + 1)
                }
            }
        }
        return null
    }

    /**
     * 流式内容闸门：普通回答边生成边显示；疑似工具调用时先压住，
     * 避免把 JSON 闪到用户眼前。
     */
    private class ContentGate(
        private val onDelta: (String) -> Unit,
        private val holdToolCalls: Boolean,
    ) {
        private val buffered = StringBuilder()
        private var released = false

        fun feed(delta: String) {
            if (released) {
                onDelta(delta)
                return
            }
            buffered.append(delta)
            if (shouldRelease()) release()
        }

        fun finish() {
            if (!released) release()
        }

        fun discard() {
            buffered.clear()
            released = true
        }

        private fun release() {
            if (released) return
            released = true
            if (buffered.isNotEmpty()) {
                val text = buffered.toString()
                buffered.clear()
                onDelta(text)
            }
        }

        private fun shouldRelease(): Boolean {
            if (!holdToolCalls) return true
            val text = buffered.toString().trimStart()
            if (text.isEmpty()) return false
            if (text.startsWith("`")) {
                return !TOOL_FENCE.startsWith(text.take(TOOL_FENCE.length))
            }
            if (text.startsWith("{") || text.startsWith("[")) {
                return !text.take(120).contains("\"tool\"")
            }
            return true
        }
    }

    private fun String.firstLine(maxChars: Int): String {
        val line = lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return if (line.length <= maxChars) line else line.take(maxChars) + "…"
    }
}

/** 去掉可能混进正文的工具代码块，避免把 JSON 展示给用户。 */
internal fun cleanFinalAnswer(text: String): String {
    val trimmed = text.trim()
    if (!trimmed.contains("```mote-tool")) return trimmed
    val stripped = Regex("(?s)```mote-tool.*?```").replace(trimmed, "").trim()
    return stripped.ifBlank { "（模型只返回了工具调用，没有给出回答，请重试）" }
}
