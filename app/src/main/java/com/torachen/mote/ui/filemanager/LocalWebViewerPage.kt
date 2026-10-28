package com.torachen.mote.ui.filemanager

import android.graphics.Color as AndroidColor
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.UiMode
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import java.io.File

/** 文件管理里打开的本地 HTML / 图片简易 WebView 查看页 */
@Composable
internal fun LocalWebViewerPage(
    request: LocalFileViewerRequest.Web,
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
) {
    val context = LocalContext.current
    val surface = MiuixTheme.colorScheme.surface
    var showMore by remember { mutableStateOf(false) }

    val webView = remember(request.path, request.isImage) {
        WebView(context).apply {
            setBackgroundColor(AndroidColor.rgb(232, 232, 232))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
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

            val file = File(request.path)
            if (request.isImage) {
                val imageUri = Uri.fromFile(file)
                val baseUrl = Uri.fromFile(file.parentFile ?: File("/")).toString().trimEnd('/') + "/"
                val html = """
                    <!doctype html>
                    <html><head>
                    <meta name="viewport" content="width=device-width,initial-scale=1.0">
                    <style>
                    html,body{margin:0;padding:0;background:#e8e8e8;}
                    img{display:block;max-width:100%;height:auto;margin:0 auto;}
                    </style></head>
                    <body><img src="$imageUri"></body></html>
                """.trimIndent()
                loadDataWithBaseURL(
                    baseUrl,
                    html,
                    "text/html",
                    "utf-8",
                    null,
                )
            } else {
                loadUrl(Uri.fromFile(file).toString())
            }
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (LocalUiMode.current == UiMode.Material) {
            FileViewerMaterialTopBar(
                title = request.title,
                onBack = onBack,
                actions = {
                    Box {
                        M3IconButton(onClick = { showMore = true }) {
                            M3Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMore,
                            onDismissRequest = { showMore = false },
                        ) {
                            DropdownMenuItem(
                                text = { M3Text("返回浏览器打开") },
                                onClick = {
                                    showMore = false
                                    LocalFileViewerState.current = null
                                    onOpenInBrowser(request.browserUrl)
                                },
                            )
                        }
                    }
                },
            )
        } else {
            SmallTopAppBar(
                title = request.title,
                color = surface,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMore = true }) {
                            Icon(
                                imageVector = MiuixIcons.More,
                                contentDescription = "更多",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                        WindowCascadingListPopup(
                            show = showMore,
                            entries = listOf(
                                DropdownEntry(
                                    items = listOf(
                                        DropdownItem(
                                            text = "返回浏览器打开",
                                            onClick = {
                                                showMore = false
                                                LocalFileViewerState.current = null
                                                onOpenInBrowser(request.browserUrl)
                                            },
                                        ),
                                    ),
                                ),
                            ),
                            onDismissRequest = { showMore = false },
                        )
                    }
                },
            )
        }
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
    }
}
