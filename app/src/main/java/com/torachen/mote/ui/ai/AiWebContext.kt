package com.torachen.mote.ui.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.torachen.mote.ui.BrowserWebViewRegistry
import com.torachen.mote.ui.BrowserWindowStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONTokener
import kotlin.coroutines.resume

/** 浏览器标签页在 AI 侧的可读快照。 */
data class AiWebPageInfo(
    val id: String,
    val title: String,
    val url: String,
    val isActive: Boolean,
)

/**
 * 浏览器 ↔ AI 的共享上下文。
 *
 * BrowserScreen 定期刷新；主对话、悬浮面板和悬浮球下的 AI 都能读取。
 */
object AiWebContext {

    var pages by mutableStateOf<List<AiWebPageInfo>>(emptyList())
        private set
    var activeUrl by mutableStateOf("")
        private set
    var activeTitle by mutableStateOf("")
        private set
    var activeText by mutableStateOf("")
        private set
    var pageTexts by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    private var activeId: String? = null

    fun updatePages(list: List<AiWebPageInfo>) {
        pages = list
        val active = list.firstOrNull { it.isActive }
        activeId = active?.id
        if (active != null) {
            activeUrl = active.url
            activeTitle = active.title
            activeText = pageTexts[active.id].orEmpty()
        }
    }

    fun updateActiveText(text: String) {
        val id = activeId ?: return
        updatePageText(id, text)
    }

    fun updatePageText(id: String, text: String) {
        pageTexts = pageTexts + (id to text.take(12_000))
        if (id == activeId) activeText = text.take(24_000)
    }

    fun isDefaultPage(): Boolean {
        val url = activeUrl.trim()
        return url.isBlank() ||
                url.startsWith("about:", ignoreCase = true) ||
                url.startsWith("chrome://", ignoreCase = true) ||
                url.startsWith("mote://", ignoreCase = true) ||
                url.startsWith("file:///android_asset", ignoreCase = true)
    }

    fun withContext(request: String): String {
        val context = promptContext()
        return if (context.isBlank()) request else "$context\n\n用户请求：$request"
    }

    /** 仅在用户明确要求时抓取所有窗口正文，避免每次对话都读取网页。 */
    suspend fun captureCurrentPages() {
        val windows = BrowserWindowStateHolder.windows.toList()
        val activeWindowId = BrowserWindowStateHolder.activeWindowId
        val infos = windows.map { window ->
            AiWebPageInfo(
                id = window.id,
                title = window.title,
                url = window.url,
                isActive = window.id == activeWindowId,
            )
        }
        updatePages(infos)
        withContext(Dispatchers.Main) {
            windows.forEach { window ->
                val webView = BrowserWebViewRegistry.webViews[window.id] ?: return@forEach
                withTimeoutOrNull(1_800L) {
                    suspendCancellableCoroutine { continuation ->
                        runCatching {
                            webView.evaluateJavascript(
                                """
                                (function(){
                                  try {
                                    var body = document.body;
                                    if (!body) return '';
                                    var text = body.innerText || body.textContent || '';
                                    return text.replace(/\s+/g, ' ').slice(0, 24000);
                                  } catch (e) { return ''; }
                                })()
                                """.trimIndent(),
                            ) { raw ->
                                val text = runCatching {
                                    (JSONTokener(raw).nextValue() as? String).orEmpty()
                                }.getOrDefault("")
                                if (text.isNotBlank()) updatePageText(window.id, text)
                                if (continuation.isActive) continuation.resume(Unit)
                            }
                        }.onFailure {
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    }
                }
            }
        }
    }

    fun promptContext(): String {        if (pages.isEmpty() && activeText.isBlank()) return ""
        return buildString {
            append("【浏览器上下文】\n")
            append("当前共 ").append(pages.size).append(" 个网页窗口。\n")
            pages.take(12).forEach { page ->
                append(if (page.isActive) "- [当前] " else "- ")
                append(page.title.ifBlank { page.url })
                append("（").append(page.url).append("）\n")
                val text = pageTexts[page.id].orEmpty()
                if (text.isNotBlank()) {
                    append("  页面内容摘要：")
                    append(text.take(4_000))
                    append("\n")
                }
            }
            if (activeTitle.isNotBlank() || activeUrl.isNotBlank()) {
                append("\n当前网页：").append(activeTitle.ifBlank { activeUrl }).append("\n")
                append("URL：").append(activeUrl).append("\n")
            }
            append("【上下文结束】")
        }
    }
}
