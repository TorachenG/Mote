package com.torachen.mote.ui.filemanager

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.UiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.io.FileOutputStream

/**
 * PDF 查看页：Android WebView 本身不渲染 PDF，
 * 这里先用 PdfRenderer 把每一页渲染成图片，再生成一个 HTML 索引页交给 WebView 展示。
 */
@Composable
internal fun LocalPdfViewerPage(
    request: LocalFileViewerRequest.Pdf,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val surface = MiuixTheme.colorScheme.surface
    var htmlFile by remember(request.path) { mutableStateOf<File?>(null) }
    var error by remember(request.path) { mutableStateOf<String?>(null) }
    var progress by remember(request.path) { mutableIntStateOf(0) }
    var total by remember(request.path) { mutableIntStateOf(0) }

    LaunchedEffect(request.path) {
        htmlFile = null
        error = null
        progress = 0
        total = 0
        val result = runCatching {
            withContext(Dispatchers.IO) {
                buildPdfHtml(
                    context = context,
                    file = File(request.path),
                    onPage = { done, count ->
                        withContext(Dispatchers.Main) {
                            progress = done
                            total = count
                        }
                    },
                )
            }
        }
        result.onSuccess { htmlFile = it }
            .onFailure { error = it.message ?: "无法打开 PDF" }
    }

    Column(modifier = Modifier.fillMaxSize().then(Modifier)) {
        if (LocalUiMode.current == UiMode.Material) {
            FileViewerMaterialTopBar(
                title = request.title,
                onBack = onBack,
            )
        } else {
            SmallTopAppBar(
                title = request.title,
                color = MiuixTheme.colorScheme.surface,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = error ?: "无法打开 PDF",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }
                htmlFile == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(progress = 0f, size = 28.dp)
                            Text(
                                text = if (total > 0) "正在渲染 PDF… $progress/$total" else "正在加载 PDF…",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
                else -> {
                    val html = htmlFile ?: return@Box
                    PdfWebView(html = html, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun PdfWebView(html: File, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webView = remember(html.absolutePath) {
        @Suppress("SetJavaScriptEnabled")
        WebView(context).apply {
            setBackgroundColor(AndroidColor.rgb(232, 232, 232))
            settings.javaScriptEnabled = false
            settings.allowFileAccess = true
            @Suppress("DEPRECATION")
            settings.allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            settings.allowUniversalAccessFromFileURLs = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = WebViewClient()
            loadUrl("file://${html.absolutePath}")
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    }
    AndroidView(factory = { webView }, modifier = modifier)
}

private suspend fun buildPdfHtml(
    context: android.content.Context,
    file: File,
    onPage: suspend (Int, Int) -> Unit,
): File {
    if (!file.exists()) error("文件不存在")
    val cacheDir = File(
        context.cacheDir,
        "pdf_preview/${file.name.hashCode()}_${file.length()}_${file.lastModified()}",
    )
    if (cacheDir.exists()) cacheDir.deleteRecursively()
    cacheDir.mkdirs()

    val images = mutableListOf<String>()
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
            val count = renderer.pageCount
            for (index in 0 until count) {
                renderer.openPage(index).use { page ->
                    val targetWidth = 1240
                    val scale = targetWidth.toFloat() / page.width
                    val height = (page.height * scale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(AndroidColor.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val name = "page_$index.jpg"
                    FileOutputStream(File(cacheDir, name)).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                    }
                    bitmap.recycle()
                    images.add(name)
                }
                onPage(index + 1, count)
            }
        }
    }

    val html = buildString {
        append("<!doctype html><html><head><meta charset=\"utf-8\">")
        append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">")
        append("<style>")
        append("html,body{margin:0;padding:0;background:#e8e8e8;}")
        append("img{display:block;width:100%;height:auto;margin:0 0 10px;background:#fff;}")
        append("</style></head><body>")
        images.forEach { append("<img src=\"").append(it).append("\">") }
        append("</body></html>")
    }
    return File(cacheDir, "index.html").apply { writeText(html, Charsets.UTF_8) }
}
