package com.torachen.mote.ui.filemanager

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 文件管理里打开本地 html / 图片用的独立浏览器页面（不影响主页浏览器） */
internal object LocalFileBrowserRequest {
    var url: String? = null
    var title: String? = null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun LocalFileBrowserPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val url = LocalFileBrowserRequest.url ?: return
    val title = LocalFileBrowserRequest.title ?: "预览"
    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(androidx.compose.ui.graphics.Color.White.toArgb())
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            // 关闭 WebView 算法暗化，避免系统深色模式把网页颜色改掉
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                settings.isAlgorithmicDarkeningAllowed = false
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                settings.forceDark = android.webkit.WebSettings.FORCE_DARK_OFF
            }
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        ) {
            TopAppBar(
                title = title,
                largeTitle = title,
                color = MiuixTheme.colorScheme.surface,
                defaultWindowInsetsPadding = false,
                navigationIcon = {
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            imageVector = top.yukonga.miuix.kmp.icon.MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
