@file:Suppress("UseKtx", "SpellCheckingInspection")

package com.torachen.mote.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarDuration

import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.layout.DialogDefaults

import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.HorizontalSplit
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.RecordingTape
import top.yukonga.miuix.kmp.icon.extended.AppRecording
import top.yukonga.miuix.kmp.icon.extended.Close2
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Weeks
import top.yukonga.miuix.kmp.icon.extended.Forward

import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ============================================================
// 常量
// ============================================================

private const val MOBILE_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

private const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

private const val HOME_PAGE = "https://cn.bing.com"

private const val STYLE_ID_NIGHT = "__mote_night__"
private const val STYLE_ID_NO_IMG = "__mote_noimg__"

private const val MAX_HISTORY_SIZE = 500
private const val MAX_BOOKMARK_SIZE = 1000

private const val SHAKE_G_FORCE_THRESHOLD = 2.5f
private const val SHAKE_COOLDOWN_MS = 800L

private const val FULLSCREEN_ANIM_MS = 320
private val FULLSCREEN_EASING = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val BOTTOM_BAR_HEIGHT = 50.dp

private val JS_NIGHT_ON = """
(function(){
  var id = '$STYLE_ID_NIGHT';
  var s = document.getElementById(id);
  if (!s) {
    s = document.createElement('style');
    s.id = id;
    (document.head || document.documentElement).appendChild(s);
  }
  s.textContent = 'html{filter:invert(100%) hue-rotate(180deg) !important;background-color:#000 !important;}' +
    'img,video,picture,canvas,svg,iframe,' +
    '[style*="background-image"],[style*="background: url"]{' +
    'filter:invert(100%) hue-rotate(180deg) !important;}';
})();
""".trimIndent()

private val JS_NIGHT_OFF = """
(function(){
  var s = document.getElementById('$STYLE_ID_NIGHT');
  if (s) s.remove();
})();
""".trimIndent()

private val JS_NO_IMG_ON = """
(function(){
  var id = '$STYLE_ID_NO_IMG';
  var s = document.getElementById(id);
  if (!s) {
    s = document.createElement('style');
    s.id = id;
    (document.head || document.documentElement).appendChild(s);
  }
  s.textContent = 'img,picture,svg,canvas,' +
    '[style*="background-image"],[style*="background: url"]{' +
    'visibility:hidden !important;}';
})();
""".trimIndent()

private val JS_NO_IMG_OFF = """
(function(){
  var s = document.getElementById('$STYLE_ID_NO_IMG');
  if (s) s.remove();
})();
""".trimIndent()

// ============================================================
// 数据模型
// ============================================================
data class PendingDownload(
    val url: String,
    val userAgent: String,
    val contentDisposition: String?,
    val mimetype: String?,
    val fileName: String
)

data class PageEntry(val id: String, val route: String)

data class ToolDef(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val isToggle: Boolean = false,
)

data class BrowserWindow(
    val id: String,
    var title: String,
    var url: String,
    var favicon: Bitmap? = null,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var progress: Int = 0,
    var nightMode: Boolean = false,
    var noImage: Boolean = false,
    var desktopMode: Boolean = false,
    var incognito: Boolean = false,
    var translated: Boolean = false,
)

data class HistoryEntry(
    val title: String,
    val url: String,
    val timestamp: Long,
)

data class BookmarkEntry(
    val title: String,
    val url: String,
    val timestamp: Long,
)

private class ToolSheetState(initial: List<ToolDef>) {
    var tools by mutableStateOf(initial)
    var isEditMode by mutableStateOf(false)
    var draggingId by mutableStateOf<String?>(null)
    val activeTools = mutableStateListOf<String>()

    private var snapshot: List<ToolDef>? = null

    fun enterEditMode() {
        if (!isEditMode) snapshot = tools
        isEditMode = true
    }

    fun commitEdit() {
        snapshot = null
        isEditMode = false
    }

    fun cancelEdit() {
        snapshot?.let { tools = it }
        snapshot = null
        isEditMode = false
    }

    fun toggleActive(toolId: String): Boolean {
        return if (toolId in activeTools) {
            activeTools.remove(toolId); false
        } else {
            activeTools.add(toolId); true
        }
    }

    fun setActive(toolId: String, active: Boolean) {
        if (active && toolId !in activeTools) {
            activeTools.add(toolId)
        } else if (!active && toolId in activeTools) {
            activeTools.remove(toolId)
        }
    }
}

// ============================================================
// 圆形图标按钮
// ============================================================
@Composable
fun MiuixCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ============================================================
// 屏幕 R 角识别
// ============================================================
@Composable
fun rememberScreenCornerRadius(fallback: Dp = 28.dp): Dp {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(context, density) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val display = context.display
                val tl = display.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                val tr = display.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
                val radiusPx = maxOf(tl?.radius ?: 0, tr?.radius ?: 0).toFloat()
                if (radiusPx > 0f) with(density) { radiusPx.toDp() } else fallback
            } else fallback
        } catch (_: Exception) { fallback }
    }
}

// ============================================================
// 工具栏默认功能列表
// ============================================================
private fun defaultTools(): List<ToolDef> = listOf(
    ToolDef("add_bookmark", "添加书签", MiuixIcons.Favorites),
    ToolDef("bookmarks", "书签列表", MiuixIcons.FavoritesFill),
    ToolDef("history", "历史记录", MiuixIcons.Recent),
    ToolDef("downloads", "下载管理", MiuixIcons.Download),
    ToolDef("refresh", "刷新网页", MiuixIcons.Refresh),
    ToolDef("night_mode", "夜间模式", MiuixIcons.Show, isToggle = true),
    ToolDef("incognito", "无痕模式", MiuixIcons.Hide, isToggle = true),
    ToolDef("desktop_mode", "电脑模式", MiuixIcons.HorizontalSplit, isToggle = true),
    ToolDef("fullscreen", "全屏模式", MiuixIcons.ExpandMore, isToggle = true),
    ToolDef("no_image", "无图模式", MiuixIcons.Photos, isToggle = true),
    ToolDef("home", "主页", MiuixIcons.Home),
    ToolDef("share", "分享网页", MiuixIcons.Forward),
    ToolDef("find_in_page", "页面查找", MiuixIcons.Search),
    ToolDef("translate", "网页翻译", MiuixIcons.Translate, isToggle = true),
    ToolDef("print", "网页打印", MiuixIcons.Rename),
    ToolDef("sniff", "资源嗅探", MiuixIcons.RecordingTape),
    ToolDef("tts", "语音朗读", MiuixIcons.AppRecording),
    ToolDef("save_page", "保存网页", MiuixIcons.File),
    ToolDef("dev_tools", "开发工具", MiuixIcons.Weeks),
    ToolDef("exit", "退出", MiuixIcons.Close2),
)

// ============================================================
// 窗口管理图标
// ============================================================
@Composable
private fun WindowManagerIcon(count: Int, tint: Color) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.5.dp),
        ) {
            val cornerRadius = 5.dp.toPx()
            val strokeWidth = 1.5.dp.toPx()
            drawRoundRect(
                color = tint,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth),
            )
        }
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = tint,
        )
    }
}

// ============================================================
// MainActivity
// ============================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeController = remember {
                ThemeController(colorSchemeMode = ColorSchemeMode.System)
            }
            MiuixTheme(controller = themeController) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }

        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        webViewRef.get()?.destroy()
        webViewRef.clear()
        super.onDestroy()
    }

    companion object {
        var webViewRef: WeakReference<WebView> = WeakReference(null)
    }
}

// ============================================================
// 顶层路由
// ============================================================

@Composable
fun AppRoot() {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    val screenCornerRadius = rememberScreenCornerRadius(fallback = 28.dp)

    val pageStack = remember {
        mutableStateListOf(PageEntry(id = "browser_0", route = "browser"))
    }

    val progress = remember { Animatable(1f) }

    val ease = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }
    val pushSpec = remember { tween<Float>(durationMillis = 520, easing = ease) }
    val popSpec = remember { tween<Float>(durationMillis = 480, easing = ease) }
    val cancelSpec = remember { tween<Float>(durationMillis = 320, easing = ease) }

    var isAnimating by remember { mutableStateOf(false) }
    val gestureLock = remember { AtomicBoolean(false) }

    val isSettled by remember { derivedStateOf { progress.value >= 1f } }

    fun pushPage(route: String) {
        if (isAnimating || gestureLock.get()) return
        isAnimating = true
        scope.launch {
            try {
                pageStack.add(PageEntry(id = UUID.randomUUID().toString(), route = route))
                progress.snapTo(0f)
                withFrameNanos { }
                withFrameNanos { }
                progress.animateTo(1f, pushSpec)
            } catch (_: CancellationException) {
                try { withContext(NonCancellable) { progress.snapTo(1f) } } catch (_: Exception) { }
            } finally { isAnimating = false }
        }
    }

    fun popPage() {
        if (isAnimating || gestureLock.get() || pageStack.size <= 1) return
        isAnimating = true
        scope.launch {
            try {
                val target = pageStack.lastOrNull() ?: return@launch
                try { progress.animateTo(0f, popSpec) } catch (_: CancellationException) { }
                withContext(NonCancellable) {
                    if (pageStack.lastOrNull()?.id == target.id) {
                        pageStack.removeAt(pageStack.lastIndex)
                    }
                    progress.snapTo(1f)
                }
            } catch (_: Exception) {
            } finally { isAnimating = false }
        }
    }

    BackHandler(enabled = isAnimating) { }

    PredictiveBackHandler(enabled = pageStack.size > 1 && !isAnimating) { backEvents ->
        if (!gestureLock.compareAndSet(false, true)) {
            backEvents.collect { }
            return@PredictiveBackHandler
        }
        val target = pageStack.lastOrNull()
        var completed = false
        try {
            var firstEvent = true
            backEvents.collect { backEvent ->
                if (firstEvent && backEvent.progress >= 1f) {
                    firstEvent = false
                    return@collect
                }
                firstEvent = false
                progress.snapTo(1f - backEvent.progress)
            }
            completed = true
        } catch (_: CancellationException) {
            completed = false
        } finally {
            try {
                withContext(NonCancellable) {
                    if (completed && target != null && pageStack.lastOrNull()?.id == target.id) {
                        try { progress.animateTo(0f, popSpec) } catch (_: Exception) { }
                        if (pageStack.lastOrNull()?.id == target.id) {
                            pageStack.removeAt(pageStack.lastIndex)
                        }
                    } else {
                        try { progress.animateTo(1f, cancelSpec) } catch (_: Exception) { }
                    }
                    try { progress.snapTo(1f) } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
            gestureLock.set(false)
        }
    }

    val openSettings: () -> Unit = remember { { pushPage("settings_main") } }

    Box(modifier = Modifier.fillMaxSize()) {
        pageStack.forEachIndexed { index, entry ->
            val isTop = index == pageStack.lastIndex
            val isSecondTop = index == pageStack.size - 2
            val isDimmedLayer = !isTop && index == pageStack.lastIndex - 1
            val surfaceIsSettled = if (isTop) isSettled else true

            key(entry.id) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = when {
                                isTop -> screenWidthPx * (1f - progress.value)
                                isSecondTop -> -screenWidthPx / 6f * progress.value
                                else -> 0f
                            }
                        }
                        .zIndex(index.toFloat())
                ) {
                    when (entry.route) {
                        "browser" -> {
                            val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
                            CompositionLocalProvider(
                                LocalNavigationEventDispatcherOwner provides navOwner
                            ) {
                                BrowserScreen(
                                    onOpenSettings = openSettings,
                                    isTop = isTop
                                )
                            }
                        }
                        "settings_main" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsMainPage(
                                onBack = { popPage() },
                                onItemClick = { r -> pushPage(r) }
                            )
                        }
                        "settings_home" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsHomePage(
                                onBack = { popPage() }
                            )
                        }
                        "settings_search" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsSearchPage(
                                onBack = { popPage() }
                            )
                        }
                        "settings_web" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsWebPage(
                                onBack = { popPage() }
                            )
                        }
                        "settings_permission" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsPermissionsPage(onBack = { popPage() })
                        }
                        "settings_notification" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsNotificationPage(onBack = { popPage() })
                        }
                        "settings_legal" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsLegalPage(
                                onBack = { popPage() },
                                onOpenDoc = { docRoute -> pushPage(docRoute) },
                            )
                        }
                        "settings_about" -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsAboutPage(
                                onBack = { popPage() }
                            )
                        }
                        else -> PageSurface(
                            isSettled = surfaceIsSettled,
                            cornerRadius = screenCornerRadius
                        ) {
                            SettingsSubPage(
                                title = routeTitle(entry.route),
                                onBack = { popPage() }
                            )
                        }
                    }
                    if (isDimmedLayer) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawRect(
                                        color = Color.Black,
                                        alpha = progress.value * 0.55f
                                    )
                                }
                        )
                    }
                }
            }
        }

        if (isAnimating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(9999f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            )
        }
    }
}

private fun routeTitle(route: String): String = when (route) {
    "settings_home" -> "主页设置"
    "settings_search" -> "搜索设置"
    "settings_web" -> "网页浏览设置"
    "settings_icon" -> "图标"
    "settings_color" -> "颜色"
    "settings_layout" -> "布局"
    "settings_permission" -> "权限设置"
    "settings_notification" -> "通知设置"
    "settings_legal" -> "法律信息"
    "settings_about" -> "关于"
    else -> ""
}

@Composable
fun PageSurface(
    isSettled: Boolean,
    cornerRadius: Dp,
    content: @Composable () -> Unit
) {
    val shape: Shape = if (isSettled) RectangleShape else RoundedCornerShape(cornerRadius)
    val elevation = if (isSettled) 0.dp else 32.dp
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = shape,
        shadowElevation = elevation,
        color = MiuixTheme.colorScheme.surface
    ) {
        content()
    }
}

// ============================================================
// 浏览器主界面（多窗口）
// ============================================================

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    onOpenSettings: () -> Unit,
    isTop: Boolean
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // ---------------- 多窗口状态 ----------------
    val windows = remember { mutableStateListOf<BrowserWindow>() }
    var activeWindowId by remember { mutableStateOf<String?>(null) }
    val webViewMap = remember { mutableMapOf<String, WebView>() }

    val activeWindow: BrowserWindow? = windows.firstOrNull { it.id == activeWindowId }

    // ---------------- favicon 缓存 ----------------
    val faviconCache = remember { mutableStateMapOf<String, Bitmap>() }
    val pendingFaviconRequests = remember { mutableSetOf<String>() }

    // ---------------- 弹层状态 ----------------
    var showToolSheet by remember { mutableStateOf(false) }
    var showWindowSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearBookmarkDialog by remember { mutableStateOf(false) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkTitleInput by remember { mutableStateOf("") }
    var bookmarkUrlInput by remember { mutableStateOf("") }

    // ---------------- 搜索栏状态 ----------------
    var urlInput by remember { mutableStateOf(TextFieldValue("")) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchBarExpanded by remember { mutableStateOf(false) }

    // ---------------- 下载状态 ----------------
    var pendingDownload by remember { mutableStateOf<PendingDownload?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    // ---------------- 页面查找状态 ----------------
    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var findResultCurrent by remember { mutableIntStateOf(0) }
    var findResultTotal by remember { mutableIntStateOf(0) }

    // ---------------- 全屏状态 ----------------
    var isFullscreen by remember { mutableStateOf(false) }

    // ---------------- Snackbar ----------------
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarJob = remember { mutableStateOf<Job?>(null) }
    val translateJob = remember { mutableStateOf<Job?>(null) }

    // ---------------- 偏好设置 ----------------
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 历史记录 ----------------
    val history = remember { mutableStateListOf<HistoryEntry>() }

    fun saveHistory() {
        try {
            val json = JSONArray()
            history.forEach { entry ->
                json.put(JSONObject().apply {
                    put("title", entry.title)
                    put("url", entry.url)
                    put("time", entry.timestamp)
                })
            }
            prefs.edit().putString("history", json.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadHistory() {
        val raw = prefs.getString("history", null) ?: return
        try {
            val json = JSONArray(raw)
            history.clear()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                val url = obj.optString("url", "")
                if (url.isBlank()) continue
                history.add(
                    HistoryEntry(
                        title = obj.optString("title", "无标题"),
                        url = url,
                        timestamp = obj.optLong("time", 0L),
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    fun recordHistory(url: String, title: String) {
        if (url.isBlank() || url == "about:blank") return
        if (url.startsWith("data:") || url.startsWith("javascript:")) return

        val existingIndex = history.indexOfFirst { it.url == url }
        if (existingIndex == 0) {
            history[0] = history[0].copy(
                title = title.ifBlank { history[0].title },
                timestamp = System.currentTimeMillis(),
            )
            saveHistory()
            return
        }

        val entry: HistoryEntry = if (existingIndex > 0) {
            val old = history.removeAt(existingIndex)
            old.copy(
                title = title.ifBlank { old.title },
                timestamp = System.currentTimeMillis(),
            )
        } else {
            HistoryEntry(
                title = title.ifBlank { "无标题" },
                url = url,
                timestamp = System.currentTimeMillis(),
            )
        }

        history.add(0, entry)
        while (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }
        saveHistory()
    }

    // ---------------- 书签 ----------------
    val bookmarks = remember { mutableStateListOf<BookmarkEntry>() }

    fun saveBookmarks() {
        try {
            val json = JSONArray()
            bookmarks.forEach { entry ->
                json.put(JSONObject().apply {
                    put("title", entry.title)
                    put("url", entry.url)
                    put("time", entry.timestamp)
                })
            }
            prefs.edit().putString("bookmarks", json.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadBookmarks() {
        val raw = prefs.getString("bookmarks", null) ?: return
        try {
            val json = JSONArray(raw)
            bookmarks.clear()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                val url = obj.optString("url", "")
                if (url.isBlank()) continue
                bookmarks.add(
                    BookmarkEntry(
                        title = obj.optString("title", "无标题"),
                        url = url,
                        timestamp = obj.optLong("time", 0L),
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    fun openAddBookmarkDialog(prefillCurrent: Boolean) {
        if (prefillCurrent) {
            val win = activeWindow
            bookmarkTitleInput = win?.title?.ifBlank { "无标题" } ?: ""
            bookmarkUrlInput = win?.url ?: ""
        } else {
            bookmarkTitleInput = ""
            bookmarkUrlInput = ""
        }
        showAddBookmarkDialog = true
    }

    // ---------------- 搜索引擎 ----------------
    val searchEngines = remember {
        listOf(
            "必应" to "https://cn.bing.com/search?q=%s",
            "抖音" to "https://www.douyin.com/search/%s",
            "百度" to "https://www.baidu.com/s?wd=%s",
            "神马" to "https://m.sm.cn/s?q=%s",
            "搜狗" to "https://www.sogou.com/web?query=%s",
            "谷歌" to "https://www.google.com/search?q=%s"
        )
    }

    // ---------------- 文件选择器 ----------------
    val fileChooserCallback = remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cb = fileChooserCallback.value
        fileChooserCallback.value = null
        if (cb == null) return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uris: Array<Uri>? = when {
                data?.clipData != null -> {
                    val n = data.clipData!!.itemCount
                    Array(n) { i -> data.clipData!!.getItemAt(i).uri }
                }
                data?.data != null -> arrayOf(data.data!!)
                else -> null
            }
            cb.onReceiveValue(uris)
        } else {
            cb.onReceiveValue(null)
        }
    }

    // ---------------- 相机/麦克风权限 ----------------
    val pendingPermission = remember { mutableStateOf<PermissionRequest?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val req = pendingPermission.value
        pendingPermission.value = null
        if (req == null) return@rememberLauncherForActivityResult
        if (result.values.all { it }) req.grant(req.resources) else req.deny()
    }

    // ========================================================
    // 全屏模式
    // ========================================================

    LaunchedEffect(isFullscreen) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            Toast.makeText(context, "摇一摇退出全屏", Toast.LENGTH_SHORT).show()
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity ?: return@onDispose
            val window = activity.window
            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(isFullscreen) {
        if (!isFullscreen) {
            return@DisposableEffect onDispose { }
        }
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose { }
        }

        var lastShakeTime = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val gForce =
                    sqrt((x * x + y * y + z * z).toDouble()).toFloat() /
                            SensorManager.GRAVITY_EARTH
                if (gForce > SHAKE_G_FORCE_THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                        lastShakeTime = now
                        isFullscreen = false
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI,
        )
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ---------------- 工具函数 ----------------

    fun updateWindow(windowId: String, block: (BrowserWindow) -> Unit) {
        val idx = windows.indexOfFirst { it.id == windowId }
        if (idx >= 0) {
            val updated = windows[idx].copy().also(block)
            windows[idx] = updated
        }
    }

    fun applyIncognitoMode(windowId: String, enable: Boolean) {
        updateWindow(windowId) { it.incognito = enable }
        val wv = webViewMap[windowId] ?: return
        if (enable) {
            try {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } catch (_: Exception) {}
            try {
                wv.clearCache(true)
                wv.clearHistory()
                wv.clearFormData()
                wv.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            } catch (_: Exception) {}
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "无痕模式已开启",
                    duration = SnackbarDuration.Short,
                )
            }
        } else {
            try {
                wv.settings.cacheMode = WebSettings.LOAD_DEFAULT
            } catch (_: Exception) {}
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "无痕模式已关闭",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    fun createWindow(url: String = HOME_PAGE): String {
        val inheritIncognito = windows.firstOrNull { it.id == activeWindowId }?.incognito == true
        val id = UUID.randomUUID().toString()
        windows.add(
            BrowserWindow(
                id = id,
                title = "新标签页",
                url = url,
                incognito = inheritIncognito,
            )
        )
        activeWindowId = id
        urlInput = TextFieldValue(url)
        return id
    }

    fun closeWindow(id: String) {
        if (windows.size <= 1) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "至少要保留一个窗口",
                    duration = SnackbarDuration.Short,
                )
            }
            return
        }
        val index = windows.indexOfFirst { it.id == id }
        if (index < 0) return

        webViewMap.remove(id)?.let { wv ->
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.destroy()
        }
        windows.removeAt(index)

        if (activeWindowId == id) {
            val newIdx = index.coerceAtMost(windows.lastIndex)
            val newWin = windows.getOrNull(newIdx)
            activeWindowId = newWin?.id
            newWin?.let { urlInput = TextFieldValue(it.url) }
        }
    }

    fun switchToWindow(id: String) {
        if (id == activeWindowId) return
        activeWindowId = id
        windows.firstOrNull { it.id == id }?.let { win ->
            urlInput = TextFieldValue(win.url)
        }
    }

    fun loadUrlInActive(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return
        // ★ 每次搜索时实时读取引擎索引，保证设置页改动立即生效
        val engineIndex = prefs.getInt("default_search_engine", 0)
            .coerceIn(0, searchEngines.lastIndex)
        val url = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) ||
                    trimmed.startsWith("ftp://", ignoreCase = true) -> trimmed
            isWebsite(trimmed) -> "https://$trimmed"
            else -> searchEngines[engineIndex].second
                .replace("%s", URLEncoder.encode(trimmed, "UTF-8"))
        }
        wv.loadUrl(url)
    }

    fun insertText(text: String) {
        val current = urlInput.text
        val isPrefix = text.startsWith("http") || text.startsWith("ftp") || text == "www."
        val newText = if (isPrefix) text + current else current + text
        urlInput = TextFieldValue(newText)
    }

    fun loadFaviconFromDomain(windowId: String, pageUrl: String) {
        val uri = pageUrl.toUri()
        val host = uri.host ?: return
        val scheme = uri.scheme ?: "https"
        val faviconUrl = "$scheme://$host/favicon.ico"

        faviconCache[host]?.let { cached ->
            val target = windows.firstOrNull { it.id == windowId }
            if (target != null && target.favicon !== cached) {
                updateWindow(windowId) { it.favicon = cached }
            }
            return
        }

        if (faviconUrl in pendingFaviconRequests) return
        pendingFaviconRequests.add(faviconUrl)

        Thread {
            try {
                val connection = URL(faviconUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connect()
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                connection.disconnect()
                if (bitmap != null) {
                    faviconCache[host] = bitmap
                    scope.launch {
                        val target = windows.firstOrNull { it.id == windowId }
                        if (target != null && target.favicon !== bitmap) {
                            updateWindow(windowId) { it.favicon = bitmap }
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingFaviconRequests.remove(faviconUrl)
            }
        }.start()
    }

    // ---------------- 网页翻译 ----------------

    fun setPageTranslated(enable: Boolean) {
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return

        if (!enable) {
            translateJob.value?.cancel()
            translateJob.value = null
            snackbarJob.value?.cancel()
            snackbarJob.value = null

            wv.evaluateJavascript(JS_RESTORE_ORIGINAL, null)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "已恢复原文",
                    duration = SnackbarDuration.Short,
                )
            }
            return
        }

        wv.evaluateJavascript(JS_EXTRACT_TEXT_NODES) { raw ->
            val payload = try {
                JSONObject(decodeJsString(raw))
            } catch (_: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "无法读取页面内容",
                        duration = SnackbarDuration.Short,
                    )
                }
                return@evaluateJavascript
            }

            val already = payload.optBoolean("already", false)
            if (already) return@evaluateJavascript

            val arr = payload.optJSONArray("texts") ?: return@evaluateJavascript
            val texts = ArrayList<String>(arr.length())
            for (i in 0 until arr.length()) {
                texts.add(arr.optString(i, ""))
            }
            if (texts.isEmpty()) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "页面没有可翻译的文本",
                        duration = SnackbarDuration.Short,
                    )
                }
                return@evaluateJavascript
            }

            snackbarJob.value?.cancel()
            snackbarJob.value = scope.launch {
                snackbarHostState.showSnackbar(
                    message = "正在翻译…",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
            }

            translateJob.value?.cancel()
            translateJob.value = scope.launch {
                val translated = withContext(Dispatchers.IO) {
                    translateTextBatch(texts, TRANSLATE_TARGET_LANG)
                }
                if (!isActive) return@launch

                val json = JSONArray().apply {
                    translated.forEach { put(it) }
                }
                withContext(Dispatchers.Main) {
                    if (!isActive) return@withContext
                    val winNow = windows.firstOrNull { it.id == id }
                    if (winNow?.translated != true) return@withContext

                    wv.evaluateJavascript(
                        buildJsApplyTranslation(json.toString()),
                        null
                    )
                    snackbarJob.value?.cancel()
                    snackbarJob.value = null
                    delay(80)
                    if (!isActive) return@withContext
                    snackbarHostState.showSnackbar(
                        message = "翻译完成",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    // ---------------- 生命周期 ----------------
    LaunchedEffect(Unit) {
        loadHistory()
        loadBookmarks()
        if (windows.isEmpty()) {
            createWindow()
        }
    }

    LaunchedEffect(activeWindowId) {
        activeWindow?.let { win ->
            if (!isSearchFocused) {
                urlInput = TextFieldValue(win.url)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            translateJob.value?.cancel()
            translateJob.value = null
            snackbarJob.value?.cancel()
            snackbarJob.value = null

            webViewMap.values.forEach { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.destroy()
            }
            webViewMap.clear()
        }
    }

    // ---------------- 返回键处理 ----------------
    BackHandler(
        enabled = isTop && (activeWindow?.canGoBack == true) &&
                !showToolSheet && !showWindowSheet && !showHistorySheet &&
                !showBookmarkSheet &&
                !showAddBookmarkDialog && !showClearBookmarkDialog && !showClearHistoryDialog
    ) {
        activeWindowId?.let { id -> webViewMap[id]?.goBack() }
    }

    // ★ 后声明的 BackHandler 优先级更高；先关闭查找栏
    BackHandler(enabled = isTop && showFindBar) {
        showFindBar = false
        findQuery = ""
        findResultCurrent = 0
        findResultTotal = 0
        activeWindowId?.let { id ->
            webViewMap[id]?.clearMatches()
            webViewMap[id]?.findAllAsync("")
        }
    }

    // ==================== UI ====================
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---------- 顶部区域：SearchBar（左 favicon + 中间输入框） ----------
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = expandVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    expandFrom = Alignment.Top,
                ) + fadeIn(tween(FULLSCREEN_ANIM_MS)),
                exit = shrinkVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(tween(FULLSCREEN_ANIM_MS)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    SearchBar(
                        inputField = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FaviconOrFallback(
                                    favicon = activeWindow?.favicon,
                                    size = 22.dp,
                                )
                                Spacer(Modifier.width(8.dp))

                                InputField(
                                    query = urlInput.text,
                                    onQueryChange = { urlInput = TextFieldValue(it) },
                                    onSearch = {
                                        loadUrlInActive(urlInput.text)
                                        focusManager.clearFocus()
                                        searchBarExpanded = false
                                    },
                                    expanded = searchBarExpanded,
                                    onExpandedChange = { searchBarExpanded = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState: FocusState ->
                                            isSearchFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                activeWindow?.let { win ->
                                                    if (urlInput.text == win.title) {
                                                        urlInput = TextFieldValue(
                                                            text = win.url,
                                                            selection = TextRange(win.url.length)
                                                        )
                                                    }
                                                }
                                            } else {
                                                searchBarExpanded = false
                                                activeWindow?.let { win ->
                                                    if (urlInput.text.isBlank()) {
                                                        urlInput = TextFieldValue(win.url)
                                                    }
                                                }
                                            }
                                        },
                                    label = "输入网址或搜索内容"
                                )
                            }
                        },
                        onExpandedChange = { searchBarExpanded = it },
                        expanded = searchBarExpanded,
                        outsideEndAction = {
                            Text(
                                text = "取消",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable(
                                        interactionSource = null,
                                        indication = null,
                                    ) {
                                        searchBarExpanded = false
                                        focusManager.clearFocus()
                                        isSearchFocused = false
                                        activeWindow?.let { win ->
                                            urlInput = TextFieldValue(win.url)
                                        }
                                    },
                                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.primary,
                            )
                        }
                    ) { }

                    Spacer(modifier = Modifier.height(6.dp))

                    val prog = activeWindow?.progress ?: 0
                    if (prog in 1..99) {
                        LinearProgressIndicator(
                            progress = prog / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ---------- 页面内查找面板 ----------
            FindInPageBar(
                show = showFindBar && !isFullscreen,
                query = findQuery,
                resultCurrent = findResultCurrent,
                resultTotal = findResultTotal,
                onQueryChange = { findQuery = it },
                onSearch = {
                    val wv = activeWindowId?.let { webViewMap[it] }
                    if (wv != null) {
                        if (findQuery.isBlank()) {
                            wv.clearMatches()
                            wv.findAllAsync("")
                            findResultTotal = 0
                            findResultCurrent = 0
                        } else {
                            wv.findAllAsync(findQuery)
                        }
                    }
                },
                onNext = {
                    activeWindowId?.let { webViewMap[it]?.findNext(true) }
                },
                onPrev = {
                    activeWindowId?.let { webViewMap[it]?.findNext(false) }
                },
                onClose = {
                    showFindBar = false
                    findQuery = ""
                    findResultCurrent = 0
                    findResultTotal = 0
                    activeWindowId?.let { id ->
                        webViewMap[id]?.clearMatches()
                        webViewMap[id]?.findAllAsync("")
                    }
                },
            )

            // ---------- WebView 区域 ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.changedToDown() }) {
                                    focusManager.clearFocus()
                                    isSearchFocused = false
                                    searchBarExpanded = false
                                    activeWindow?.let { win ->
                                        if (urlInput.text.isBlank()) {
                                            urlInput = TextFieldValue(win.url)
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { container ->
                        val id = activeWindowId
                        if (id == null) {
                            container.removeAllViews()
                        } else {
                            val existing = webViewMap[id]
                            val alreadyAttached = existing != null &&
                                    container.childCount == 1 &&
                                    container.getChildAt(0) === existing

                            if (!alreadyAttached) {
                                container.removeAllViews()

                                val wv = existing ?: run {
                                    val created = createWebView(
                                        context = container.context,
                                        initialDesktopMode = windows
                                            .firstOrNull { it.id == id }
                                            ?.desktopMode ?: false,
                                        onWebViewCreated = { },
                                        onProgressChanged = { p ->
                                            updateWindow(id) { it.progress = p }
                                        },
                                        onPageStateChanged = { url, title, back, fwd ->
                                            val win0 = windows.firstOrNull { it.id == id }
                                            val urlChanged = win0?.url != url
                                            updateWindow(id) {
                                                if (urlChanged) {
                                                    it.translated = false
                                                }
                                                it.url = url
                                                it.title = title
                                                it.canGoBack = back
                                                it.canGoForward = fwd
                                            }
                                            val win = windows.firstOrNull { it.id == id }
                                            if (win?.incognito != true) {
                                                recordHistory(url, title)
                                            }
                                            urlInput = TextFieldValue(url)
                                        },
                                        onFaviconReceived = { host, bmp ->
                                            faviconCache[host] = bmp
                                            updateWindow(id) { it.favicon = bmp }
                                        },
                                        onFaviconFromDomain = { url ->
                                            loadFaviconFromDomain(id, url)
                                        },
                                        onDownloadRequest = { dl ->
                                            pendingDownload = dl
                                            showDownloadDialog = true
                                        },
                                        onFindResult = { total, current ->
                                            findResultTotal = total
                                            findResultCurrent = current + 1
                                        },
                                        onPageFinishedExtra = { wv2 ->
                                            val win = windows.firstOrNull { it.id == id }
                                            if (win?.nightMode == true) {
                                                wv2.evaluateJavascript(JS_NIGHT_ON, null)
                                            }
                                            if (win?.noImage == true) {
                                                wv2.evaluateJavascript(JS_NO_IMG_ON, null)
                                            }
                                        },
                                        fileChooserCallback = fileChooserCallback,
                                        fileChooserLauncher = fileChooserLauncher,
                                        pendingPermission = pendingPermission,
                                        permissionLauncher = permissionLauncher
                                    )
                                    webViewMap[id] = created

                                    val win = windows.firstOrNull { it.id == id }
                                    if (win?.incognito == true) {
                                        try {
                                            created.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                        } catch (_: Exception) {}
                                    }

                                    val initialUrl = win?.url ?: HOME_PAGE
                                    created.loadUrl(initialUrl)

                                    created
                                }

                                (wv.parent as? ViewGroup)?.removeView(wv)
                                container.addView(wv)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ---------- 底部工具栏 ----------
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = expandVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    expandFrom = Alignment.Bottom,
                ) + fadeIn(tween(FULLSCREEN_ANIM_MS)),
                exit = shrinkVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    shrinkTowards = Alignment.Bottom,
                ) + fadeOut(tween(FULLSCREEN_ANIM_MS)),
            ) {
                val navBarBottomPadding = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()

                Surface(color = MiuixTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = navBarBottomPadding)
                            .height(BOTTOM_BAR_HEIGHT),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val onSurface = MiuixTheme.colorScheme.onSurface

                        BottomBarButton(
                            onClick = { activeWindowId?.let { webViewMap[it]?.goBack() } },
                            enabled = activeWindow?.canGoBack == true,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "后退",
                                tint = if (activeWindow?.canGoBack == true) onSurface
                                else onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        BottomBarButton(
                            onClick = { activeWindowId?.let { webViewMap[it]?.goForward() } },
                            enabled = activeWindow?.canGoForward == true,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.ChevronForward,
                                contentDescription = "前进",
                                tint = if (activeWindow?.canGoForward == true) onSurface
                                else onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        BottomBarButton(onClick = { showToolSheet = true }) {
                            Icon(
                                imageVector = MiuixIcons.GridView,
                                contentDescription = "工具栏",
                                tint = onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        BottomBarButton(onClick = { showWindowSheet = true }) {
                            WindowManagerIcon(
                                count = windows.size,
                                tint = onSurface,
                            )
                        }
                        BottomBarButton(
                            onClick = {
                                activeWindowId?.let { id ->
                                    webViewMap[id]?.loadUrl(HOME_PAGE)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Home,
                                contentDescription = "主页",
                                tint = onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }

        // ---------- Snackbar ----------
        val navBarBottomPadding = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        val bottomBarHeight = if (isFullscreen) 0.dp else BOTTOM_BAR_HEIGHT
        SnackbarHost(
            state = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarBottomPadding + bottomBarHeight)
                .imePadding(),
        )

        // ---------- 输入法建议：全屏时隐藏 ----------
        if (isSearchFocused && !isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MiuixTheme.colorScheme.surface)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("https://", "www.", "/", ".com", ".cn", ".net", ".org").forEach { text ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MiuixTheme.colorScheme.secondaryContainer)
                                .clickable { insertText(text) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text)
                        }
                    }
                }
            }
        }
    }

    // ---------------- 工具栏弹出层 ----------------
    BrowserToolSheet(
        show = showToolSheet,
        pageTitle = activeWindow?.title ?: "新标签页",
        favicon = activeWindow?.favicon,
        isFullscreen = isFullscreen,
        onOpenSettings = {
            showToolSheet = false
            onOpenSettings()
        },
        onDismiss = { showToolSheet = false },
        onToolAction = { toolId, isActive ->
            val id = activeWindowId
            when (toolId) {
                "home" -> {
                    if (id != null) {
                        webViewMap[id]?.loadUrl(HOME_PAGE)
                    }
                }
                "night_mode" -> {
                    if (id != null) {
                        updateWindow(id) { it.nightMode = isActive }
                        webViewMap[id]?.evaluateJavascript(
                            if (isActive) JS_NIGHT_ON else JS_NIGHT_OFF, null
                        )
                    }
                }
                "no_image" -> {
                    if (id != null) {
                        updateWindow(id) { it.noImage = isActive }
                        webViewMap[id]?.evaluateJavascript(
                            if (isActive) JS_NO_IMG_ON else JS_NO_IMG_OFF, null
                        )
                    }
                }
                "desktop_mode" -> {
                    if (id != null) {
                        updateWindow(id) { it.desktopMode = isActive }
                        webViewMap[id]?.let { wv ->
                            wv.settings.userAgentString =
                                if (isActive) DESKTOP_UA else MOBILE_UA
                            wv.reload()
                        }
                    }
                }
                "incognito" -> {
                    if (id != null) {
                        applyIncognitoMode(id, isActive)
                    }
                }
                "translate" -> {
                    if (id != null) {
                        updateWindow(id) { it.translated = isActive }
                        setPageTranslated(isActive)
                    }
                }
                "fullscreen" -> {
                    isFullscreen = isActive
                }
                "refresh" -> id?.let { webViewMap[it]?.reload() }
                "settings" -> onOpenSettings()
                "history" -> {
                    scope.launch {
                        delay(280)
                        showHistorySheet = true
                    }
                }
                "add_bookmark" -> {
                    val win = activeWindow
                    if (win != null && win.url.isNotBlank()) {
                        scope.launch {
                            delay(280)
                            openAddBookmarkDialog(prefillCurrent = true)
                        }
                    }
                }
                "bookmarks" -> {
                    scope.launch {
                        delay(280)
                        showBookmarkSheet = true
                    }
                }
                "share" -> {
                    val title = activeWindow?.title?.ifBlank { "无标题" } ?: "无标题"
                    val url = activeWindow?.url?.ifBlank { "about:blank" } ?: "about:blank"
                    val text = "【$title】$url"
                    try {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                            putExtra(Intent.EXTRA_SUBJECT, title)
                        }
                        context.startActivity(Intent.createChooser(send, "分享网页"))
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "分享失败：${e.message}",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                }
                "save_page" -> {
                    if (id != null) {
                        val wv = webViewMap[id]
                        val pageUrl = wv?.url
                        if (!pageUrl.isNullOrBlank() &&
                            (pageUrl.startsWith("http://") || pageUrl.startsWith("https://"))
                        ) {
                            val rawTitle = activeWindow?.title?.ifBlank { "page" } ?: "page"
                            val safeName = rawTitle
                                .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
                                .take(80)
                                .ifBlank { "page" }
                            val fileName =
                                if (safeName.endsWith(".html", ignoreCase = true)) safeName
                                else "$safeName.html"
                            pendingDownload = PendingDownload(
                                url = pageUrl,
                                userAgent = wv.settings.userAgentString ?: MOBILE_UA,
                                contentDisposition = null,
                                mimetype = "text/html",
                                fileName = fileName,
                            )
                            showDownloadDialog = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "当前页面无法保存",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        }
                    }
                }
                "print" -> {
                    if (id != null) {
                        val wv = webViewMap[id]
                        if (wv != null) {
                            try {
                                val printManager = context.getSystemService(
                                    Context.PRINT_SERVICE
                                ) as PrintManager
                                val printAdapter = wv.createPrintDocumentAdapter(
                                    activeWindow?.title?.ifBlank { "网页" } ?: "网页"
                                )
                                val jobName = "Mote-${System.currentTimeMillis()}"
                                printManager.print(
                                    jobName,
                                    printAdapter,
                                    PrintAttributes.Builder().build(),
                                )
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "打印失败：${e.message}",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            }
                        }
                    }
                }
                "find_in_page" -> {
                    showFindBar = true
                }
                "dev_tools" -> { /* TODO: 开发工具 */ }
                "exit" -> (context as? Activity)?.finish()
                "downloads" -> { /* TODO */ }
                "sniff" -> { /* TODO */ }
                "tts" -> { /* TODO */ }
            }
            showToolSheet = false
        },
    )

    // ---------------- 窗口管理弹出层 ----------------
    val isIncognitoMode = activeWindow?.incognito == true

    WindowManagerSheet(
        show = showWindowSheet,
        windows = windows,
        activeWindowId = activeWindowId,
        isIncognitoMode = isIncognitoMode,
        onSwitch = { id ->
            switchToWindow(id)
            showWindowSheet = false
        },
        onClose = { id -> closeWindow(id) },
        onNewWindow = {
            createWindow()
            showWindowSheet = false
        },
        onToggleIncognito = {
            val id = activeWindowId
            if (id != null) {
                applyIncognitoMode(id, !isIncognitoMode)
            }
        },
        onDismiss = { showWindowSheet = false },
    )

    // ---------------- 历史记录弹出层 ----------------
    HistorySheet(
        show = showHistorySheet,
        history = history,
        onOpenUrl = { url ->
            val id = activeWindowId
            if (id != null) {
                webViewMap[id]?.loadUrl(url)
            }
            showHistorySheet = false
        },
        onDelete = { url ->
            val idx = history.indexOfFirst { it.url == url }
            if (idx >= 0) {
                history.removeAt(idx)
                saveHistory()
            }
        },
        onClearAll = {
            showClearHistoryDialog = true
        },
        onDismiss = { showHistorySheet = false },
    )

    // ---------------- 书签列表弹出层 ----------------
    BookmarkSheet(
        show = showBookmarkSheet,
        bookmarks = bookmarks,
        onAddBookmark = {
            openAddBookmarkDialog(prefillCurrent = false)
        },
        onOpenUrl = { url ->
            val id = activeWindowId
            if (id != null) {
                webViewMap[id]?.loadUrl(url)
            }
            showBookmarkSheet = false
        },
        onDelete = { url ->
            val idx = bookmarks.indexOfFirst { it.url == url }
            if (idx >= 0) {
                bookmarks.removeAt(idx)
                saveBookmarks()
            }
        },
        onClearAll = {
            showClearBookmarkDialog = true
        },
        onDismiss = { showBookmarkSheet = false },
    )

    // ---------------- 清空历史记录确认弹窗 ----------------
    WindowDialog(
        show = showClearHistoryDialog,
        title = "清空历史记录",
        onDismissRequest = { showClearHistoryDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确定要清空全部历史记录吗？此操作无法撤销。",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MiuixTheme.textStyles.body1,
                color = DialogDefaults.summaryColor(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showClearHistoryDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        history.clear()
                        saveHistory()
                        showClearHistoryDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "历史记录已清空",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("清空") }
            }
        }
    }

    // ---------------- 清空书签确认弹窗 ----------------
    WindowDialog(
        show = showClearBookmarkDialog,
        title = "清空书签",
        onDismissRequest = { showClearBookmarkDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确定要清空全部书签吗？此操作无法撤销。",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MiuixTheme.textStyles.body1,
                color = DialogDefaults.summaryColor(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showClearBookmarkDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        bookmarks.clear()
                        saveBookmarks()
                        showClearBookmarkDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "书签已清空",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("清空") }
            }
        }
    }

    // ---------------- 添加书签弹窗 ----------------
    WindowDialog(
        show = showAddBookmarkDialog,
        title = "添加书签",
        onDismissRequest = {
            showAddBookmarkDialog = false
            bookmarkTitleInput = ""
            bookmarkUrlInput = ""
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = bookmarkTitleInput,
                onValueChange = { bookmarkTitleInput = it },
                label = "书签名称",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = bookmarkUrlInput,
                onValueChange = { bookmarkUrlInput = it },
                label = "保存网址",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        showAddBookmarkDialog = false
                        bookmarkTitleInput = ""
                        bookmarkUrlInput = ""
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val url = bookmarkUrlInput.trim()
                        if (url.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "请输入网址",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                            return@Button
                        }
                        val title = bookmarkTitleInput.ifBlank { url }
                        val existing = bookmarks.indexOfFirst { it.url == url }
                        if (existing >= 0) {
                            bookmarks[existing] = BookmarkEntry(
                                title = title,
                                url = url,
                                timestamp = System.currentTimeMillis(),
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "书签已更新",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        } else {
                            bookmarks.add(
                                0,
                                BookmarkEntry(
                                    title = title,
                                    url = url,
                                    timestamp = System.currentTimeMillis(),
                                )
                            )
                            while (bookmarks.size > MAX_BOOKMARK_SIZE) {
                                bookmarks.removeAt(bookmarks.size - 1)
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "书签已添加",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        }
                        saveBookmarks()
                        showAddBookmarkDialog = false
                        bookmarkTitleInput = ""
                        bookmarkUrlInput = ""
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("添加") }
            }
        }
    }

    // ---------------- 下载文件弹窗 ----------------
    val downloadInfo = pendingDownload
    WindowDialog(
        show = showDownloadDialog,
        title = "下载文件",
        onDismissRequest = { showDownloadDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = downloadInfo?.fileName ?: "",
                onValueChange = { },
                label = "文件名",
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = downloadInfo?.url?.toUri()?.host ?: "未知",
                onValueChange = { },
                label = "来源",
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showDownloadDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        downloadInfo?.let { enqueueDownload(context, it) }
                        showDownloadDialog = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("下载") }
            }
        }
    }
}

// ============================================================
// 页面内查找面板
// ============================================================
@Composable
private fun FindInPageBar(
    show: Boolean,
    query: String,
    resultCurrent: Int,
    resultTotal: Int,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter = expandVertically(
            animationSpec = tween(220),
            expandFrom = Alignment.Top,
        ) + fadeIn(tween(160)),
        exit = shrinkVertically(
            animationSpec = tween(200),
            shrinkTowards = Alignment.Top,
        ) + fadeOut(tween(120)),
    ) {
        Surface(color = MiuixTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = "页面内查找",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (resultTotal == 0) "0/0" else "$resultCurrent/$resultTotal",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.width(4.dp))
                MiuixCircleIconButton(onClick = onPrev, size = 36.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronBackward,
                        contentDescription = "上一个",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                MiuixCircleIconButton(onClick = onNext, size = 36.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "下一个",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                MiuixCircleIconButton(onClick = onClose, size = 36.dp) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ============================================================
// 网站图标（带默认兜底）
// ============================================================
@Composable
private fun FaviconOrFallback(favicon: Bitmap?, size: Dp) {
    if (favicon != null) {
        Icon(
            bitmap = favicon.asImageBitmap(),
            contentDescription = "网站图标",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(5.dp)),
            tint = Color.Unspecified,
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Public,
            contentDescription = "默认图标",
            tint = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.size(size),
        )
    }
}

// ============================================================
// 底部栏按钮
// ============================================================
@Composable
private fun RowScope.BottomBarButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ============================================================
// 窗口管理弹出层
// ============================================================

@Composable
private fun WindowManagerSheet(
    show: Boolean,
    windows: List<BrowserWindow>,
    activeWindowId: String?,
    isIncognitoMode: Boolean,
    onSwitch: (String) -> Unit,
    onClose: (String) -> Unit,
    onNewWindow: () -> Unit,
    onToggleIncognito: () -> Unit,
    onDismiss: () -> Unit,
) {
    val visibleWindows = windows.filter { it.incognito == isIncognitoMode }

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "已打开的页面",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,

        startAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { onToggleIncognito() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Hide,
                    contentDescription = if (isIncognitoMode) "退出无痕模式" else "进入无痕模式",
                    tint = if (isIncognitoMode) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },

        endAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { onNewWindow() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "新建窗口",
                    tint = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = TOOL_SHEET_NON_EDIT_BOTTOM),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                if (visibleWindows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无窗口",
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp),
                    ) {
                        items(
                            count = visibleWindows.size,
                            key = { index -> visibleWindows[index].id },
                        ) { index ->
                            val win = visibleWindows[index]
                            val isActive = win.id == activeWindowId
                            val isFirst = index == 0
                            val isLast = index == visibleWindows.lastIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clickable { onSwitch(win.id) }
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = if (isFirst) 14.dp else 10.dp,
                                        bottom = if (isLast) 14.dp else 10.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val fav = win.favicon
                                    if (fav != null) {
                                        Icon(
                                            bitmap = fav.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            tint = Color.Unspecified,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Public,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = win.title.ifBlank { "无标题" },
                                        fontSize = 15.sp,
                                        fontWeight = if (isActive) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = win.url,
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 1,
                                    )
                                }

                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MiuixTheme.colorScheme.primary),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { onClose(win.id) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Close,
                                        contentDescription = "关闭窗口",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 历史记录弹出层
// ============================================================

@Composable
private fun HistorySheet(
    show: Boolean,
    history: List<HistoryEntry>,
    onOpenUrl: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "历史记录",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,

        endAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { if (history.isNotEmpty()) onClearAll() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Delete,
                    contentDescription = "清空历史记录",
                    tint = if (history.isNotEmpty()) MiuixTheme.colorScheme.onSurface
                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = TOOL_SHEET_NON_EDIT_BOTTOM),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无历史记录",
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp),
                    ) {
                        items(
                            count = history.size,
                            key = { index -> history[index].url },
                        ) { index ->
                            val entry = history[index]
                            val isFirst = index == 0
                            val isLast = index == history.lastIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clickable { onOpenUrl(entry.url) }
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = if (isFirst) 14.dp else 10.dp,
                                        bottom = if (isLast) 14.dp else 10.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Recent,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title.ifBlank { "无标题" },
                                        fontSize = 15.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = entry.url,
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 1,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { onDelete(entry.url) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Close,
                                        contentDescription = "删除该条",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 书签列表弹出层
// ============================================================

@Composable
private fun BookmarkSheet(
    show: Boolean,
    bookmarks: List<BookmarkEntry>,
    onAddBookmark: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "书签列表",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,

        startAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { onAddBookmark() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加书签",
                    tint = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },

        endAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { if (bookmarks.isNotEmpty()) onClearAll() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Delete,
                    contentDescription = "清空书签",
                    tint = if (bookmarks.isNotEmpty()) MiuixTheme.colorScheme.onSurface
                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = TOOL_SHEET_NON_EDIT_BOTTOM),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无书签",
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp),
                    ) {
                        items(
                            count = bookmarks.size,
                            key = { index -> bookmarks[index].url },
                        ) { index ->
                            val entry = bookmarks[index]
                            val isFirst = index == 0
                            val isLast = index == bookmarks.lastIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clickable { onOpenUrl(entry.url) }
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = if (isFirst) 14.dp else 10.dp,
                                        bottom = if (isLast) 14.dp else 10.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Favorites,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title.ifBlank { "无标题" },
                                        fontSize = 15.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = entry.url,
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 1,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { onDelete(entry.url) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Close,
                                        contentDescription = "删除该条",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 工具栏底部弹出层
// ============================================================

private val TOOL_SHEET_CELL_HEIGHT = 68.dp
private val TOOL_SHEET_EDITBAR_BOTTOM = 24.dp
private val TOOL_SHEET_NON_EDIT_BOTTOM = 24.dp

@Composable
private fun BrowserToolSheet(
    show: Boolean,
    pageTitle: String,
    favicon: Bitmap?,
    isFullscreen: Boolean,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    onToolAction: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    val allTools = remember { defaultTools() }
    val state = remember {
        ToolSheetState(
            run {
                val saved = prefs.getString("tool_order", null)
                if (saved.isNullOrEmpty()) {
                    allTools
                } else {
                    val ids = saved.split(",")
                    val ordered = ids.mapNotNull { id -> allTools.find { it.id == id } }
                    val missing = allTools.filter { it.id !in ids }
                    ordered + missing
                }
            }
        )
    }

    LaunchedEffect(isFullscreen) {
        state.setActive("fullscreen", isFullscreen)
    }

    val scrollState = rememberScrollState()
    val isDragging = state.draggingId != null

    WindowBottomSheet(
        show = show,
        onDismissRequest = {
            onDismiss()
        },
        onDismissFinished = {
            state.cancelEdit()
        },
        title = pageTitle.ifBlank { "新标签页" },
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,
        startAction = {
            Box(
                modifier = Modifier.size(width = 54.dp, height = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                FaviconOrFallback(favicon = favicon, size = 24.dp)
            }
        },
        endAction = {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(CircleShape)
                    .clickable { onOpenSettings() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Settings,
                    contentDescription = "设置",
                    tint = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState, enabled = !isDragging)
                .animateContentSize(animationSpec = tween(durationMillis = 260)),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    ToolGrid(
                        state = state,
                        onToolClick = { toolId ->
                            val tool = allTools.find { it.id == toolId }
                            val active = if (tool?.isToggle == true) {
                                state.toggleActive(toolId)
                            } else {
                                false
                            }
                            onToolAction(toolId, active)
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isEditMode,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 260),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 260),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(durationMillis = 140)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = TOOL_SHEET_EDITBAR_BOTTOM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MiuixTheme.colorScheme.secondaryContainer)
                            .clickable { state.tools = allTools }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "重置",
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "编辑模式",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MiuixTheme.colorScheme.primary)
                            .clickable {
                                val order = state.tools.map { it.id }.joinToString(",")
                                prefs.edit().putString("tool_order", order).apply()
                                state.commitEdit()
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "确定",
                            fontSize = 14.sp,
                            color = Color.White,
                        )
                    }
                }
            }

            val bottomSpacer by animateDpAsState(
                targetValue = if (state.isEditMode) 0.dp else TOOL_SHEET_NON_EDIT_BOTTOM,
                animationSpec = tween(durationMillis = 260),
                label = "bottomSpacer",
            )
            Spacer(Modifier.height(bottomSpacer))
        }
    }
}

// ============================================================
// 工具栏网格
// ============================================================
@Composable
private fun ToolGrid(
    state: ToolSheetState,
    onToolClick: (String) -> Unit,
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val cellWidth = maxWidth / 5
        val cellHeight = TOOL_SHEET_CELL_HEIGHT
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        val rows = (state.tools.size + 4) / 5
        val gridHeight = cellHeight * rows

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
        ) {
            state.tools.forEachIndexed { index, tool ->
                key(tool.id) {
                    val isDragging = state.draggingId == tool.id

                    val col = index % 5
                    val row = index / 5
                    val targetX = cellWidthPx * col
                    val targetY = cellHeightPx * row

                    var dragOffset by remember(tool.id) {
                        mutableStateOf(Offset.Zero)
                    }

                    val displayX by animateFloatAsState(
                        targetValue = if (isDragging) dragOffset.x else targetX,
                        animationSpec = tween(
                            durationMillis = if (isDragging) 0 else 260,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                        label = "displayX_${tool.id}",
                    )
                    val displayY by animateFloatAsState(
                        targetValue = if (isDragging) dragOffset.y else targetY,
                        animationSpec = tween(
                            durationMillis = if (isDragging) 0 else 260,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                        label = "displayY_${tool.id}",
                    )

                    val displayScale by animateFloatAsState(
                        targetValue = if (isDragging) 1.15f else 1f,
                        animationSpec = tween(
                            durationMillis = if (isDragging) 180 else 220,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                        label = "scale_${tool.id}",
                    )

                    val displayAlpha by animateFloatAsState(
                        targetValue = if (isDragging) 0.92f else 1f,
                        animationSpec = tween(
                            durationMillis = if (isDragging) 180 else 220,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                        label = "alpha_${tool.id}",
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(displayX.roundToInt(), displayY.roundToInt()) }
                            .size(cellWidth, cellHeight)
                            .zIndex(if (isDragging) 10f else 0f)
                            .graphicsLayer {
                                scaleX = displayScale
                                scaleY = displayScale
                                alpha = displayAlpha
                            }
                            .pointerInput(tool.id) {
                                detectTapGestures(
                                    onTap = {
                                        if (!state.isEditMode) onToolClick(tool.id)
                                    },
                                )
                            }
                            .pointerInput(tool.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        val idxNow = state.tools.indexOfFirst { it.id == tool.id }
                                        val c = idxNow % 5
                                        val r = idxNow / 5
                                        dragOffset = Offset(
                                            cellWidthPx * c,
                                            cellHeightPx * r,
                                        )
                                        state.draggingId = tool.id
                                        state.enterEditMode()
                                    },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        val draggingId = state.draggingId
                                            ?: return@detectDragGesturesAfterLongPress

                                        dragOffset += Offset(delta.x, delta.y)

                                        val centerX = dragOffset.x + cellWidthPx / 2f
                                        val centerY = dragOffset.y + cellHeightPx / 2f
                                        val targetCol = (centerX / cellWidthPx)
                                            .toInt().coerceIn(0, 4)
                                        val targetRow = (centerY / cellHeightPx)
                                            .toInt().coerceAtLeast(0)
                                        val targetIndex = (targetRow * 5 + targetCol)
                                            .coerceIn(0, state.tools.lastIndex)

                                        val currentIndex = state.tools
                                            .indexOfFirst { it.id == draggingId }

                                        if (currentIndex >= 0 && targetIndex != currentIndex) {
                                            val newList = state.tools.toMutableList()
                                            val item = newList.removeAt(currentIndex)
                                            newList.add(targetIndex, item)
                                            state.tools = newList
                                        }
                                    },
                                    onDragEnd = { state.draggingId = null },
                                    onDragCancel = { state.draggingId = null },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        ToolGridItem(
                            tool = tool,
                            isActive = tool.id in state.activeTools,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolGridItem(tool: ToolDef, isActive: Boolean) {
    val iconColor = if (tool.isToggle && isActive) {
        MiuixTheme.colorScheme.primary
    } else {
        MiuixTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = tool.title,
            tint = iconColor,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = tool.title,
            fontSize = 10.sp,
            color = MiuixTheme.colorScheme.onSurface,
        )
    }
}

private fun enqueueDownload(context: Context, dl: PendingDownload) {
    try {
        val request = DownloadManager.Request(dl.url.toUri()).apply {
            setMimeType(dl.mimetype)
            addRequestHeader("User-Agent", dl.userAgent)
            addRequestHeader("Cookie", CookieManager.getInstance().getCookie(dl.url) ?: "")
            setTitle(dl.fileName)
            setDescription("正在下载…")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dl.fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "开始下载：${dl.fileName}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "下载失败：${e.message}", Toast.LENGTH_LONG).show()
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    initialDesktopMode: Boolean,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageStateChanged: (url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onFaviconReceived: (host: String, bmp: Bitmap) -> Unit,
    onFaviconFromDomain: (url: String) -> Unit,
    onDownloadRequest: (PendingDownload) -> Unit,
    onFindResult: (total: Int, current: Int) -> Unit,
    onPageFinishedExtra: (WebView) -> Unit,
    fileChooserCallback: MutableState<ValueCallback<Array<Uri>>?>,
    fileChooserLauncher: ActivityResultLauncher<Intent>,
    pendingPermission: MutableState<PermissionRequest?>,
    permissionLauncher: ActivityResultLauncher<Array<String>>
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.apply {
            userAgentString = if (initialDesktopMode) DESKTOP_UA else MOBILE_UA
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            textZoom = 100
            loadsImagesAutomatically = true
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            allowContentAccess = true
            defaultTextEncodingName = "UTF-8"
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            mediaPlaybackRequiresUserGesture = false
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        setFindListener { total, current, _ ->
            onFindResult(total, current)
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) } catch (_: Exception) {}
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null) onFaviconFromDomain(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { u ->
                    onPageStateChanged(
                        u,
                        view?.title ?: "无标题",
                        view?.canGoBack() == true,
                        view?.canGoForward() == true
                    )
                    onFaviconFromDomain(u)

                    val jsHook = buildString {
                        append("(function(){")
                        append("const _pushState = history.pushState;")
                        append("history.pushState = function(state, title, url){")
                        append("_pushState.call(this, state, title, url);")
                        append("window.androidBridge.onUrlChanged(location.href);")
                        append("};")
                        append("window.addEventListener('popstate', function(){")
                        append("window.androidBridge.onUrlChanged(location.href);")
                        append("});")
                        append("})();")
                    }

                    view?.addJavascriptInterface(object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onUrlChanged(newUrl: String) {
                            view.post { onFaviconFromDomain(newUrl) }
                        }
                    }, "androidBridge")
                    view?.evaluateJavascript(jsHook, null)

                    view?.let { onPageFinishedExtra(it) }
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                val primaryError = when (error.primaryError) {
                    SslError.SSL_DATE_INVALID -> "证书日期无效"
                    SslError.SSL_EXPIRED -> "证书已过期"
                    SslError.SSL_IDMISMATCH -> "证书域名不匹配"
                    SslError.SSL_NOTYETVALID -> "证书尚未生效"
                    SslError.SSL_UNTRUSTED -> "证书来自未知授权中心"
                    SslError.SSL_INVALID -> "证书无效"
                    else -> "证书验证失败"
                }
                try {
                    AlertDialog.Builder(context)
                        .setTitle("安全警告")
                        .setMessage(
                            "$primaryError\n\n" +
                                    "该网站的安全证书无法验证，继续访问可能存在风险。\n\n" +
                                    "你可以选择“继续访问”暂时忽略此警告。"
                        )
                        .setPositiveButton("继续访问") { _, _ ->
                            try { handler.proceed() } catch (_: Exception) {}
                        }
                        .setNegativeButton("返回") { _, _ ->
                            try { handler.cancel() } catch (_: Exception) {}
                            if (view.canGoBack()) view.goBack()
                        }
                        .setCancelable(false)
                        .show()
                } catch (_: Exception) {
                    try { handler.cancel() } catch (_: Exception) {}
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                icon ?: return
                val url = view?.url ?: return
                val host = url.toUri().host ?: return
                if (host.contains("baidu.com") && isSolidColor(icon)) return
                onFaviconReceived(host, icon)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged(newProgress)
            }

            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val parent = view.parent as? ViewGroup ?: return false
                val popup = WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                    alpha = 0f
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                }
                var handled = false
                fun consume(url: String?) {
                    if (handled || url.isNullOrEmpty() || url == "about:blank") return
                    handled = true
                    view.loadUrl(url)
                    (popup.parent as? ViewGroup)?.removeView(popup)
                    popup.destroy()
                }
                popup.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        v: WebView?, req: WebResourceRequest?
                    ): Boolean {
                        consume(req?.url?.toString()); return true
                    }
                    override fun onPageStarted(v: WebView?, url: String?, f: Bitmap?) {
                        super.onPageStarted(v, url, f); consume(url)
                    }
                    @SuppressLint("WebViewClientOnReceivedSslError")
                    override fun onReceivedSslError(
                        v: WebView, h: SslErrorHandler, e: SslError
                    ) { try { h.proceed() } catch (_: Exception) {} }
                }
                parent.addView(popup)
                transport.webView = popup
                resultMsg.sendToTarget()
                return true
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback.value?.onReceiveValue(null)
                fileChooserCallback.value = filePathCallback
                val intent = try { fileChooserParams?.createIntent() } catch (_: Exception) { null }
                    ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE)
                    }
                return try {
                    fileChooserLauncher.launch(intent); true
                } catch (_: Exception) {
                    fileChooserCallback.value = null
                    filePathCallback?.onReceiveValue(null); false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) return
                val resources = request.resources
                val needed = mutableListOf<String>()
                for (r in resources) {
                    when (r) {
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                            needed.add(Manifest.permission.CAMERA)
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                            needed.add(Manifest.permission.RECORD_AUDIO)
                    }
                }
                val granted = needed.all {
                    ContextCompat.checkSelfPermission(context, it) ==
                            PackageManager.PERMISSION_GRANTED
                }
                when {
                    granted -> request.grant(resources)
                    needed.isNotEmpty() -> {
                        pendingPermission.value = request
                        permissionLauncher.launch(needed.toTypedArray())
                    }
                    else -> request.grant(resources)
                }
            }
        }

        setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            onDownloadRequest(
                PendingDownload(
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = contentDisposition,
                    mimetype = mimetype,
                    fileName = fileName
                )
            )
        }

        onWebViewCreated(this)
        MainActivity.webViewRef = WeakReference(this)
    }
}

// ============================================================
// 辅助函数
// ============================================================

private fun isWebsite(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return false
    if (trimmed.any { it.code in 0x4E00..0x9FFF || it == ' ' }) return false
    val lower = trimmed.lowercase()
    if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://")) return true
    val domainPart = lower.substringBefore('/').substringBefore('?').substringBefore('#')
    val host = domainPart.substringBefore(':')
    if (host.isEmpty() || '.' !in host) return false
    val seg = host.split('.')
    if (seg.size == 4 && seg.all { it.all { c -> c.isDigit() } && it.toIntOrNull() in 0..255 }) return true
    val suffixes = listOf(
        ".com", ".org", ".net", ".edu", ".gov", ".mil",
        ".tech", ".art", ".shop", ".app", ".game", ".video",
        ".store", ".movie", ".tv", ".int", ".arpa", ".city",
        ".town", ".io", ".cc", ".xyz", ".top", ".club",
        ".cn", ".hk", ".tw", ".us", ".uk", ".jp", ".kr", ".de"
    )
    return suffixes.any { host.endsWith(it) && host.length > it.length }
}

private fun isSolidColor(bmp: Bitmap): Boolean {
    val w = bmp.width
    val h = bmp.height
    if (w < 2 || h < 2) return true
    val p0 = bmp.getPixel(0, 0)
    return p0 == bmp.getPixel(w - 1, 0) &&
            p0 == bmp.getPixel(0, h - 1) &&
            p0 == bmp.getPixel(w - 1, h - 1)
}

// ============================================================
// 网页翻译
// ============================================================

private const val TRANSLATE_TARGET_LANG = "zh-CN"

private val JS_EXTRACT_TEXT_NODES = """
(function(){
  if (window.__mote_translate_nodes && window.__mote_translate_original) {
    return JSON.stringify({
      already: true,
      texts: window.__mote_translate_original
    });
  }
  var nodes = [];
  var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
    acceptNode: function(node) {
      var parent = node.parentElement;
      if (!parent) return NodeFilter.FILTER_REJECT;
      var tag = parent.tagName;
      if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'NOSCRIPT' ||
          tag === 'TEXTAREA' || tag === 'CODE' || tag === 'PRE') {
        return NodeFilter.FILTER_REJECT;
      }
      var text = node.nodeValue;
      if (!text || !text.trim()) return NodeFilter.FILTER_REJECT;
      try {
        var style = window.getComputedStyle(parent);
        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') {
          return NodeFilter.FILTER_REJECT;
        }
      } catch(e) {}
      return NodeFilter.FILTER_ACCEPT;
    }
  });
  while (walker.nextNode()) { nodes.push(walker.currentNode); }
  window.__mote_translate_nodes = nodes;
  window.__mote_translate_original = nodes.map(function(n){ return n.nodeValue; });
  return JSON.stringify({
    already: false,
    texts: nodes.map(function(n){ return n.nodeValue; })
  });
})();
""".trimIndent()

private fun buildJsApplyTranslation(translationsJson: String): String = """
(function(payload){
  var nodes = window.__mote_translate_nodes;
  if (!nodes) return;
  var translations = payload;
  for (var i = 0; i < nodes.length && i < translations.length; i++) {
    if (translations[i] != null && translations[i] !== '') {
      nodes[i].nodeValue = translations[i];
    }
  }
})($translationsJson);
""".trimIndent()

private val JS_RESTORE_ORIGINAL = """
(function(){
  if (!window.__mote_translate_nodes || !window.__mote_translate_original) return;
  var nodes = window.__mote_translate_nodes;
  var originals = window.__mote_translate_original;
  for (var i = 0; i < nodes.length && i < originals.length; i++) {
    nodes[i].nodeValue = originals[i];
  }
  delete window.__mote_translate_nodes;
  delete window.__mote_translate_original;
})();
""".trimIndent()

private fun googleTranslateOne(text: String, targetLang: String): String {
    val encoded = URLEncoder.encode(text, "UTF-8")
    val url = "https://translate.googleapis.com/translate_a/single" +
            "?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encoded"
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 5000
    conn.readTimeout = 8000
    conn.requestMethod = "GET"
    conn.setRequestProperty("User-Agent", MOBILE_UA)
    try {
        if (conn.responseCode != 200) throw RuntimeException("HTTP ${conn.responseCode}")
        val body = conn.inputStream.bufferedReader().readText()
        val arr = JSONArray(body)
        val inner = arr.getJSONArray(0)
        val sb = StringBuilder()
        for (i in 0 until inner.length()) {
            val seg = inner.getJSONArray(i)
            if (!seg.isNull(0)) sb.append(seg.getString(0))
        }
        if (sb.isEmpty()) throw RuntimeException("empty translation")
        return sb.toString()
    } finally {
        conn.disconnect()
    }
}

private fun myMemoryTranslateOne(text: String, targetLang: String): String {
    val encoded = URLEncoder.encode(text, "UTF-8")
    val url = "https://api.mymemory.translated.net/get" +
            "?q=$encoded&langpair=autodetect|$targetLang"
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 5000
    conn.readTimeout = 8000
    conn.requestMethod = "GET"
    conn.setRequestProperty("User-Agent", MOBILE_UA)
    try {
        if (conn.responseCode != 200) throw RuntimeException("HTTP ${conn.responseCode}")
        val body = conn.inputStream.bufferedReader().readText()
        val obj = JSONObject(body)
        val data = obj.optJSONObject("responseData") ?: throw RuntimeException("no data")
        val translated = data.optString("translatedText", "")
        if (translated.isBlank()) throw RuntimeException("empty")
        return translated
    } finally {
        conn.disconnect()
    }
}

private fun translateTextBatch(
    texts: List<String>,
    targetLang: String,
    maxRequests: Int = 60
): List<String> {
    val result = ArrayList<String>(texts.size)
    var requestCount = 0

    for (raw in texts) {
        val text = raw.trim()
        if (text.isEmpty() || text.length <= 1) {
            result.add(raw)
            continue
        }
        if (requestCount >= maxRequests) {
            result.add(raw)
            continue
        }
        requestCount++

        var translated: String? = null
        try {
            translated = googleTranslateOne(text, targetLang)
        } catch (_: Exception) {
            try {
                translated = myMemoryTranslateOne(text, targetLang)
            } catch (_: Exception) {
                translated = null
            }
        }
        val leading = raw.takeWhile { it.isWhitespace() }
        val trailing = raw.takeLastWhile { it.isWhitespace() }
        result.add(
            if (translated != null) "$leading$translated$trailing" else raw
        )
    }
    return result
}

private fun decodeJsString(raw: String?): String {
    if (raw.isNullOrBlank() || raw == "null") return ""
    val s = raw.trim()
    return try {
        JSONArray("[$s]").optString(0)
    } catch (_: Exception) {
        s.trim('"')
    }
}