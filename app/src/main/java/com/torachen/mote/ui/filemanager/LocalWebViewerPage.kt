package com.torachen.mote.ui.filemanager

import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.produceState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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

    val webView = if (request.isImage) null else remember(request.path) {
        WebView(context).apply {
            // HTML 预览用网页默认的白底：跟随主题会在深色模式下把
            // 没有自带背景色的网页染成深色，预览结果就和原文件不一致了
            setBackgroundColor(AndroidColor.WHITE)
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
            // 关闭 WebView 的算法暗化：系统深色模式下它会自行把网页反色，
            // 导致预览出来的颜色和原文件不一致
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settings.isAlgorithmicDarkeningAllowed = false
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                settings.forceDark = android.webkit.WebSettings.FORCE_DARK_OFF
            }
            webViewClient = WebViewClient()

            val file = File(request.path)
            loadUrl(Uri.fromFile(file).toString())
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.destroy()
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
                            shape = RoundedCornerShape(16.dp),
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
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
        if (request.isImage) {
            LocalImageViewerContent(request.path)
        } else {
            AndroidView(factory = { webView!! }, modifier = Modifier.fillMaxSize())
        }
    }
}


@Composable
private fun LocalImageViewerContent(path: String) {
    var loadFailed by remember(path) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                var sample = 1
                val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                while (maxDim / sample > 3000) sample *= 2
                BitmapFactory.decodeFile(
                    path,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )
            }.getOrNull()
        }
        if (value == null) loadFailed = true
    }

    val scaleAnim = remember { Animatable(1f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()
    val pageBg = if (LocalUiMode.current == UiMode.Material) {
        androidx.compose.material3.MaterialTheme.colorScheme.surface
    } else {
        MiuixTheme.colorScheme.surface
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(pageBg)) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()

        fun clampOffset(raw: Offset, s: Float): Offset {
            val maxX = (viewW * (s - 1f)) / 2f
            val maxY = (viewH * (s - 1f)) / 2f
            return Offset(raw.x.coerceIn(-maxX, maxX), raw.y.coerceIn(-maxY, maxY))
        }

        val bmp = bitmap
        if (bmp == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (loadFailed) {
                    androidx.compose.material3.Text(
                        text = "无法打开该图片",
                        color = Color.White.copy(alpha = 0.8f),
                    )
                } else {
                    androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                }
            }
        } else {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        translationX = offsetAnim.value.x
                        translationY = offsetAnim.value.y
                    }
                    .pointerInput(path) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                val newScale = (scaleAnim.value * zoom).coerceIn(1f, 6f)
                                scaleAnim.snapTo(newScale)
                                offsetAnim.snapTo(
                                    if (newScale <= 1.01f) Offset.Zero
                                    else clampOffset(offsetAnim.value + pan, newScale)
                                )
                            }
                        }
                    }
                    .pointerInput(path) {
                        detectTapGestures(
                            onDoubleTap = {
                                val zoomIn = scaleAnim.value <= 1.2f
                                scope.launch {
                                    val ease = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
                                    if (zoomIn) {
                                        scaleAnim.animateTo(2.5f, tween(300, easing = ease))
                                        offsetAnim.animateTo(
                                            clampOffset(offsetAnim.value, 2.5f),
                                            tween(300, easing = ease),
                                        )
                                    } else {
                                        offsetAnim.animateTo(Offset.Zero, tween(300, easing = ease))
                                        scaleAnim.animateTo(1f, tween(300, easing = ease))
                                    }
                                }
                            },
                        )
                    },
            )
        }
    }
}
