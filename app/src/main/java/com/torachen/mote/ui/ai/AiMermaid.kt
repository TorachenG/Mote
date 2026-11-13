@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy

/** 语言标记为 mermaid 的代码块交给图形渲染器，而不是普通代码块。 */
internal fun isMermaidLanguage(language: String): Boolean =
    language.trim()
        .substringBefore(' ')
        .substringBefore('{')
        .equals("mermaid", ignoreCase = true)

/**
 * 老设备的系统 WebView 可能太旧，跑不动 mermaid（需要较新的 Chromium）。
 * Chrome/80 以下直接退回代码块，避免出现空白图。
 */
internal object AiMermaidSupport {
    private var cached: Boolean? = null

    fun isSupported(context: Context): Boolean {
        cached?.let { return it }
        val userAgent = runCatching { WebSettings.getDefaultUserAgent(context) }.getOrDefault("")
        val major = Regex("Chrome/(\\d+)").find(userAgent)?.groupValues?.get(1)?.toIntOrNull()
        val supported = major == null || major >= 80
        cached = supported
        return supported
    }
}

/**
 * Mermaid 图形渲染。
 *
 * 使用内置 WebView + assets 里的 mermaid.min.js 离线渲染，不访问网络。
 * 渲染完成后由 JS 把图表高度回传给 Compose，卡片会自适应内容高度。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun AiMermaidDiagram(
    code: String,
    dark: Boolean,
    palette: AiMarkdownPalette,
    modifier: Modifier = Modifier,
) {
    // 流式输出时先等代码稳定，避免每个 token 都重建 WebView、反复渲染半个图。
    var stableCode by remember { mutableStateOf("") }
    var stabilized by remember { mutableStateOf(false) }
    LaunchedEffect(code) {
        delay(350L)
        stableCode = code
        stabilized = true
    }

    var diagramHeight by remember { mutableStateOf(180.dp) }
    LaunchedEffect(stableCode, dark) {
        diagramHeight = 180.dp
    }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val html = remember(stableCode, dark) {
        buildMermaidHtml(context, stableCode, dark)
    }
    val maxHeight = 1_600.dp
    val displayHeight = if (diagramHeight > maxHeight) maxHeight else diagramHeight
    val needsInnerScroll = diagramHeight > maxHeight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.codeBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Mermaid 图表",
                fontSize = 10.sp,
                color = palette.secondary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        clipboard.setText(AnnotatedString(code))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = "复制图表代码",
                    tint = palette.secondary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        if (!stabilized) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "正在生成图表…",
                    fontSize = 12.sp,
                    color = palette.secondary,
                )
            }
        } else {
            key(stableCode, dark) {
                AndroidView(
                    factory = { viewContext ->
                        WebView(viewContext).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                            overScrollMode = View.OVER_SCROLL_NEVER
                            isVerticalScrollBarEnabled = false
                            isHorizontalScrollBarEnabled = false
                            isLongClickable = false
                            setOnLongClickListener { true }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?,
                                ): Boolean = true

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean = true
                            }
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = false
                                allowFileAccess = true
                                allowContentAccess = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                                blockNetworkLoads = true
                                setSupportZoom(false)
                                builtInZoomControls = false
                                displayZoomControls = false
                                useWideViewPort = false
                                loadWithOverviewMode = false
                                javaScriptCanOpenWindowsAutomatically = false
                            }
                            addJavascriptInterface(
                                MermaidBridge { reportedHeight ->
                                    mainHandler.post {
                                        val height = reportedHeight.coerceIn(26, 20_000)
                                        diagramHeight = height.dp
                                    }
                                },
                                "MermaidBridge",
                            )
                            loadDataWithBaseURL(
                                "file:///android_asset/mermaid/",
                                html,
                                "text/html",
                                "UTF-8",
                                null,
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(displayHeight),
                    update = { webView ->
                        // 图不高时禁用交互，让手指拖动交给外层对话滚动；
                        // 超过上限时允许在图内滚动。
                        webView.isEnabled = needsInnerScroll
                    },
                    onRelease = { webView ->
                        runCatching { webView.loadUrl("about:blank") }
                        runCatching { webView.destroy() }
                    },
                )
            }
        }
    }
}

internal class MermaidBridge(
    private val onHeight: (Int) -> Unit,
) {
    @JavascriptInterface
    fun onRendered(height: Int) {
        onHeight(height)
    }
}

private fun buildMermaidHtml(context: Context, source: String, dark: Boolean): String {
    val template = runCatching {
        context.assets.open("mermaid/diagram.html")
            .bufferedReader()
            .use { it.readText() }
    }.getOrNull()
    if (template.isNullOrBlank()) {
        return """
            <!DOCTYPE html><html><body style="margin:0;padding:10px;color:#d9534f;
            font-family:monospace;font-size:12px;">Mermaid 资源缺失</body></html>
        """.trimIndent()
    }
    // JSON 字符串就是合法的 JS 字符串字面量；再把 </ 转义，避免提前闭合 script 标签。
    val sourceJson = JSONObject.quote(source).replace("</", "<\\/")
    return template
        .replace("__SOURCE_JSON__", sourceJson)
        .replace("__DARK__", if (dark) "true" else "false")
}
