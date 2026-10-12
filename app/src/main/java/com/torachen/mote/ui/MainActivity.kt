@file:Suppress("UseKtx", "SpellCheckingInspection")
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.torachen.mote.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
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
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.foundation.layout.ime
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
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
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem
import top.yukonga.miuix.kmp.basic.BreadcrumbBarDefaults
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarResult

import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.layout.DialogDefaults

import top.yukonga.miuix.kmp.preference.RadioButtonPreference

import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.AddFolder
import top.yukonga.miuix.kmp.icon.extended.All
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
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.RecordingTape
import top.yukonga.miuix.kmp.icon.extended.AppRecording
import top.yukonga.miuix.kmp.icon.extended.Close2
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Weeks
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Filter

import com.torachen.mote.service.HomeWallpaperCropOverlay
import com.torachen.mote.service.saveCroppedWallpaper

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
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.abs

// ============================================================
// 常量
// ============================================================

private const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

// ★ 已删除 HOME_PAGE 常量（不再硬编码内置主页 limestart.cn）

private const val STYLE_ID_NIGHT = "__mote_night__"
private const val STYLE_ID_NO_IMG = "__mote_noimg__"

private const val MAX_HISTORY_SIZE = 500
private const val MAX_BOOKMARK_SIZE = 1000
private const val MAX_SEARCH_HISTORY_SIZE = 20

private const val SHAKE_G_FORCE_THRESHOLD = 2.5f
private const val SHAKE_COOLDOWN_MS = 800L

private const val FULLSCREEN_ANIM_MS = 320
private val FULLSCREEN_EASING = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val BOTTOM_BAR_HEIGHT = 50.dp

private const val MAX_NETWORK_SNIFF = 300

private val TEXT_ZOOM_VALUES = listOf(70, 85, 100, 120, 140)
private const val TEXT_ZOOM_DEFAULT_INDEX = 2

private val ZOOM_RATIO_PERCENTAGES = listOf(50, 75, 100, 125, 150, 200)
private const val ZOOM_RATIO_DEFAULT_INDEX = 2

private val FALLBACK_MOBILE_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

internal object PendingScanResultStore {
    var pending by mutableStateOf<String?>(null)
}

// ★ 待裁剪的壁纸 Bitmap（从设置页 → 裁剪页传递）
internal object PendingCropBitmapStore {
    var pending: Bitmap? = null
}

// ★ 需求 1：壁纸保存完成事件（裁剪页 → 界面设置页，用于弹"控件颜色"建议弹窗）
internal object WallpaperSavedEventStore {
    var pending by mutableStateOf(false)
}

// ★ 需求 2：SSL 证书错误弹窗（WebViewClient → BrowserScreen）
internal object PendingSslErrorStore {
    var pending by mutableStateOf<PendingSslError?>(null)
}

internal data class PendingSslError(
    val errorText: String,
    val proceed: () -> Unit,
    val cancel: () -> Unit,
)

// ★ 界面设置导航参数（跨页面传递）
internal object SettingsLayoutNavArgs {
    // 界面设置初始选中 Tab（0=新标签页, 1=敬请期待）
    var initialTab: Int = 0
    // 从界面设置跳转到搜索设置时，是否要闪烁搜索历史模块
    var flashSearchHistoryOnEnter: Boolean = false
}

// ★ 会话快照：用于判断上次是否非正常关闭
internal object SessionSnapshotStore {
    // 应用启动时读一次，用来判断是否需要提示恢复
    var pendingRestore: List<String>? = null
    // 用户主动退出中（true 时不更新 snapshot）
    var userExiting: Boolean = false
}

internal object InitialLaunchUrlStore {
    var pending by mutableStateOf<String?>(null)
}

internal object InitialInsetsFallback {
    var navBarBottomPx: Int = 0
}

private class SearchQueryHolder {
    var value: String = ""
}

// ============================================================
// 下拉刷新守卫状态
// ============================================================

internal object PullRefreshStateHolder {
    private val map = java.util.concurrent.ConcurrentHashMap<
            String,
            java.util.concurrent.atomic.AtomicBoolean
            >()

    fun get(windowId: String): Boolean {
        return map[windowId]?.get() ?: true
    }

    fun set(windowId: String, allowed: Boolean) {
        map.getOrPut(windowId) {
            java.util.concurrent.atomic.AtomicBoolean(true)
        }.set(allowed)
    }

    fun remove(windowId: String) {
        map.remove(windowId)
    }
}

// ============================================================
// 左右滑动导航守卫状态
// ============================================================

internal object SwipeNavStateHolder {
    private val map = java.util.concurrent.ConcurrentHashMap<
            String,
            java.util.concurrent.atomic.AtomicBoolean
            >()

    fun get(windowId: String): Boolean {
        return map[windowId]?.get() ?: true
    }

    fun set(windowId: String, allowed: Boolean) {
        map.getOrPut(windowId) {
            java.util.concurrent.atomic.AtomicBoolean(true)
        }.set(allowed)
    }

    fun remove(windowId: String) {
        map.remove(windowId)
    }
}

// ============================================================
// 最近嗅探到的视频 URL（供 onShowCustomView 兜底）
// ============================================================

internal object RecentVideoUrlStore {
    private val map = java.util.concurrent.ConcurrentHashMap<String, String>()
    // ★ 记录每个窗口最后一次存 URL 的时间戳，超过 30 秒视为过期
    private val timestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private const val TTL_MS = 30_000L

    fun set(windowId: String, url: String) {
        if (url.contains(".m3u8", true) ||
            url.contains(".mpd", true) ||
            url.contains(".mp4", true)
        ) {
            map[windowId] = url
            timestamps[windowId] = System.currentTimeMillis()
        }
    }

    fun get(windowId: String): String? {
        val url = map[windowId] ?: return null
        val ts = timestamps[windowId] ?: 0L
        if (System.currentTimeMillis() - ts > TTL_MS) {
            map.remove(windowId)
            timestamps.remove(windowId)
            return null
        }
        return url
    }

    fun remove(windowId: String) {
        map.remove(windowId)
        timestamps.remove(windowId)
    }
}

private fun getMobileUserAgent(context: Context): String {
    return try {
        val systemUa = WebSettings.getDefaultUserAgent(context)
        systemUa.replace("; wv", "").replace(" wv", "")
    } catch (_: Exception) {
        FALLBACK_MOBILE_UA
    }
}

private fun getBaseDomain(host: String): String {
    if (host.isBlank()) return ""
    val parts = host.split(".")
    return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
}

private fun extractSearchQueryFromUrl(url: String): String? {
    return try {
        val uri = url.toUri()
        val params = listOf(
            "q", "query", "wd", "word", "keyword",
            "search_query", "kw", "w", "text", "k"
        )
        for (param in params) {
            val v = uri.getQueryParameter(param)
            if (!v.isNullOrBlank()) return v
        }
        null
    } catch (_: Exception) {
        null
    }
}

private fun formatHistoryTime(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return String.format(Locale.US, "%02d:%02d", h, m)
}

private fun getHistoryDayLabel(timestamp: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance()
    target.timeInMillis = timestamp

    val nowDay = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val targetDay = Calendar.getInstance().apply {
        timeInMillis = target.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((nowDay.timeInMillis - targetDay.timeInMillis) / (24L * 3600L * 1000L)).toInt()
    return when (diffDays) {
        0 -> "今天"
        1 -> "昨天"
        2 -> "前天"
        else -> {
            val y = target.get(Calendar.YEAR)
            val mo = target.get(Calendar.MONTH) + 1
            val d = target.get(Calendar.DAY_OF_MONTH)
            String.format(Locale.US, "%d年%d月%d日", y, mo, d)
        }
    }
}

private val JS_INTERCEPT_VIDEO = """
(function(){
  if (window.__mote_video_tracker) return;
  window.__mote_video_tracker = true;

  function getVideoSrc(video) {
    var src = video.currentSrc || video.src;
    if (!src) {
      var source = video.querySelector('source');
      if (source) src = source.src;
    }
    return src || '';
  }

  setInterval(function() {
    try {
      var videos = document.querySelectorAll('video');
      for (var i = 0; i < videos.length; i++) {
        var v = videos[i];
        if (!v.__mote_attached) {
          v.__mote_attached = true;
          v.addEventListener('play', function() {
            window.__mote_current_video_url = getVideoSrc(this);
          }, true);
        }
      }
      for (var i = 0; i < videos.length; i++) {
        var v = videos[i];
        if (!v.paused) {
          var s = getVideoSrc(v);
          if (s) window.__mote_current_video_url = s;
        }
      }
    } catch(e) {}
  }, 500);
})();
""".trimIndent()

private val JS_GET_CURRENT_VIDEO_URL = """
(function(){
  try {
    var videos = document.querySelectorAll('video');
    for (var i = 0; i < videos.length; i++) {
      var v = videos[i];
      if (!v.paused) {
        var src = v.currentSrc || v.src;
        if (!src) {
          var source = v.querySelector('source');
          if (source) src = source.src;
        }
        if (src) return src;
      }
    }
  } catch(e) {}
  return window.__mote_current_video_url || '';
})();
""".trimIndent()

internal val JS_EXIT_WEB_FULLSCREEN = """
(function(){
  try { if (document.exitFullscreen) document.exitFullscreen(); } catch(e) {}
  try { if (document.webkitExitFullscreen) document.webkitExitFullscreen(); } catch(e) {}
  try { if (document.webkitCancelFullScreen) document.webkitCancelFullScreen(); } catch(e) {}
  try { if (document.mozCancelFullScreen) document.mozCancelFullScreen(); } catch(e) {}
  try { if (document.msExitFullscreen) document.msExitFullscreen(); } catch(e) {}

  try {
    var videos = document.querySelectorAll('video');
    for (var i = 0; i < videos.length; i++) {
      var v = videos[i];
      try { if (v.webkitExitFullscreen) v.webkitExitFullscreen(); } catch(e) {}
      try { if (v.webkitExitFullScreen) v.webkitExitFullScreen(); } catch(e) {}
      try { if (v.webkitDisplayingFullscreen) v.webkitDisplayingFullscreen = false; } catch(e) {}
      try { if (v.exitFullscreen) v.exitFullscreen(); } catch(e) {}
    }
  } catch(e) {}

  try { window.__mote_current_video_url = ''; } catch(e) {}
  return 'ok';
})();
""".trimIndent()

private val JS_DETECT_ENGLISH_PAGE = """
(function(){
  try {
    var lang = (document.documentElement.lang || '').toLowerCase();
    if (lang.indexOf('en') === 0) return 'en';

    var nonEnLangs = ['zh', 'ja', 'ko', 'fr', 'de', 'es', 'ru', 'ar', 'pt', 'it'];
    for (var i = 0; i < nonEnLangs.length; i++) {
      if (lang.indexOf(nonEnLangs[i]) === 0) return 'other';
    }

    var text = '';
    try {
      if (document.body) text = document.body.innerText || document.body.textContent || '';
    } catch(e) {}
    if (!text) return 'unknown';
    text = text.substring(0, 3000);

    var cjk = 0;
    var ascii = 0;
    for (var i = 0; i < text.length; i++) {
      var c = text.charCodeAt(i);
      if (c >= 0x4E00 && c <= 0x9FFF) cjk++;
      else if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) ascii++;
    }
    if (ascii < 100) return 'unknown';
    if (cjk === 0) return 'en';
    if (ascii / (cjk + 1) > 20) return 'en';
    return 'other';
  } catch(e) { return 'unknown'; }
})();
""".trimIndent()

private val JS_HOOK_XHR_FETCH = """
(function(){
  if (window.__mote_xhr_hooked) return;
  window.__mote_xhr_hooked = true;
  window.__mote_xhr_urls = window.__mote_xhr_urls || [];

  function push(u) {
    try {
      if (!u) return;
      u = String(u);
      if (!u) return;
      if (window.__mote_xhr_urls.length >= 500) return;
      if (window.__mote_xhr_urls.indexOf(u) >= 0) return;
      window.__mote_xhr_urls.push(u);
    } catch(e) {}
  }

  try {
    var _open = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url) {
      try { push(url); } catch(e) {}
      return _open.apply(this, arguments);
    };
  } catch(e) {}

  try {
    var _fetch = window.fetch;
    if (_fetch) {
      window.fetch = function(input, init) {
        try {
          var url = (typeof input === 'string') ? input : (input && input.url);
          push(url);
        } catch(e) {}
        return _fetch.apply(this, arguments);
      };
    }
  } catch(e) {}

  try {
    var _send = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function() {
      try { push(this.__mote_url); } catch(e) {}
      return _send.apply(this, arguments);
    };
    var _open2 = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url) {
      try { this.__mote_url = url; } catch(e) {}
      try { push(url); } catch(e) {}
      return _open2.apply(this, arguments);
    };
  } catch(e) {}

  try {
    if (window.PerformanceObserver) {
      var obs = new PerformanceObserver(function(list){
        try {
          list.getEntries().forEach(function(entry){
            if (entry && entry.name) push(entry.name);
          });
        } catch(e) {}
      });
      obs.observe({ entryTypes: ['resource'] });
    }
  } catch(e) {}
})();
""".trimIndent()

private val JS_PULL_REFRESH_GUARD = """
(function(){
  if (window.__mote_pull_guard) return;
  window.__mote_pull_guard = true;

  function checkAllowed(x, y) {
    try {
      var doc = document.documentElement;
      var body = document.body;
      var topScroll = Math.max(
        window.pageYOffset || 0,
        (doc && doc.scrollTop) || 0,
        (body && body.scrollTop) || 0
      );
      if (topScroll > 2) return false;

      var el = document.elementFromPoint(x, y);
      if (!el) return true;

      var node = el;
      var depth = 0;
      while (node && node !== body && node !== doc && depth < 50) {
        var style = window.getComputedStyle(node);
        var oy = style.overflowY;
        if ((oy === 'auto' || oy === 'scroll' || oy === 'overlay') &&
            node.scrollHeight > node.clientHeight + 1 &&
            node.scrollTop > 2) {
          return false;
        }
        node = node.parentElement;
        depth++;
      }
      return true;
    } catch(e) {
      return true;
    }
  }

  function notify(allowed) {
    try {
      if (window.moteBridge && window.moteBridge.setPullRefreshAllowed) {
        window.moteBridge.setPullRefreshAllowed(!!allowed);
      }
    } catch(e) {}
  }

  document.addEventListener('touchstart', function(e){
    if (!e.touches || e.touches.length !== 1) return;
    var t = e.touches[0];
    notify(checkAllowed(t.clientX, t.clientY));
  }, { passive: true, capture: true });

  document.addEventListener('touchend', function(){
    notify(true);
  }, { passive: true, capture: true });

  document.addEventListener('touchcancel', function(){
    notify(true);
  }, { passive: true, capture: true });
})();
""".trimIndent()

private val JS_HORIZONTAL_SCROLL_GUARD = """
(function(){
  if (window.__mote_hscroll_guard) return;
  window.__mote_hscroll_guard = true;

  function checkAllowed(x, y) {
    try {
      var doc = document.documentElement;
      var body = document.body;

      var el = document.elementFromPoint(x, y);
      if (!el) return true;

      // ★ 需求 1：如果触摸点在 <video> / <audio> 或它们的祖先内 → 不允许横滑
      var videoCheck = el;
      var vDepth = 0;
      while (videoCheck && videoCheck !== body && videoCheck !== doc && vDepth < 12) {
        var tag = (videoCheck.tagName || '').toLowerCase();
        if (tag === 'video' || tag === 'audio') return false;
        var cls = (videoCheck.className || '').toString().toLowerCase();
        if (cls.indexOf('video') >= 0 || cls.indexOf('player') >= 0 ||
            cls.indexOf('xgplayer') >= 0 || cls.indexOf('dplayer') >= 0 ||
            cls.indexOf('artplayer') >= 0 || cls.indexOf('prism') >= 0 ||
            cls.indexOf('jwplayer') >= 0 || cls.indexOf('video-js') >= 0 ||
            cls.indexOf('plyr') >= 0 || cls.indexOf('vjs-') >= 0 ||
            cls.indexOf('mpegts') >= 0) {
          if (videoCheck.querySelector && videoCheck.querySelector('video')) {
            return false;
          }
        }
        videoCheck = videoCheck.parentElement;
        vDepth++;
      }

      var node = el;
      var depth = 0;
      while (node && node !== body && node !== doc && depth < 60) {
        var style = window.getComputedStyle(node);
        var ox = style.overflowX;
        if (ox === 'auto' || ox === 'scroll' || ox === 'overlay') {
          if (node.scrollWidth > node.clientWidth + 2) {
            var atLeft = node.scrollLeft <= 2;
            var atRight = node.scrollLeft + node.clientWidth >= node.scrollWidth - 2;
            if (!atLeft && !atRight) {
              return false;
            }
          }
        }
        node = node.parentElement;
        depth++;
      }
      return true;
    } catch(e) {
      return true;
    }
  }

  function notify(allowed) {
    try {
      if (window.moteBridge && window.moteBridge.setSwipeNavAllowed) {
        window.moteBridge.setSwipeNavAllowed(!!allowed);
      }
    } catch(e) {}
  }

  document.addEventListener('touchstart', function(e){
    if (!e.touches || e.touches.length !== 1) return;
    var t = e.touches[0];
    notify(checkAllowed(t.clientX, t.clientY));
  }, { passive: true, capture: true });

  document.addEventListener('touchend', function(){
    notify(true);
  }, { passive: true, capture: true });

  document.addEventListener('touchcancel', function(){
    notify(true);
  }, { passive: true, capture: true });
})();
""".trimIndent()

private fun buildJsSmartPreconnect(maxHosts: Int): String = """
(function(){
  if (window.__mote_preconnect_installed) return;
  window.__mote_preconnect_installed = true;

  var MAX_HOSTS = $maxHosts;
  var DELAY_MS = 500;

  function extractHosts() {
    var current = location.hostname || '';
    var count = {};
    var links = document.querySelectorAll('a[href]');
    for (var i = 0; i < links.length; i++) {
      var href = links[i].href;
      if (!href) continue;
      if (href.indexOf('http://') !== 0 && href.indexOf('https://') !== 0) continue;
      var host;
      try { host = new URL(href).hostname; } catch(e) { continue; }
      if (!host) continue;
      if (host === current) continue;
      if (host.indexOf('.') < 0) continue;
      count[host] = (count[host] || 0) + 1;
    }
    var arr = [];
    for (var k in count) {
      if (Object.prototype.hasOwnProperty.call(count, k)) arr.push([k, count[k]]);
    }
    arr.sort(function(a, b){ return b[1] - a[1]; });
    var result = [];
    for (var j = 0; j < arr.length && j < MAX_HOSTS; j++) {
      result.push(arr[j][0]);
    }
    return result;
  }

  function inject(hosts) {
    var head = document.head || document.documentElement;
    if (!head) return;
    for (var i = 0; i < hosts.length; i++) {
      var host = hosts[i];

      var l1 = document.createElement('link');
      l1.rel = 'dns-prefetch';
      l1.href = '//' + host;
      head.appendChild(l1);

      var l2 = document.createElement('link');
      l2.rel = 'preconnect';
      l2.href = '//' + host;
      l2.crossOrigin = 'anonymous';
      head.appendChild(l2);
    }
  }

  function run() {
    try {
      var hosts = extractHosts();
      if (hosts.length > 0) inject(hosts);
    } catch(e) {}
  }

  if (document.readyState === 'complete') {
    setTimeout(run, DELAY_MS);
  } else {
    window.addEventListener('load', function(){
      setTimeout(run, DELAY_MS);
    }, { once: true });
  }
})();
""".trimIndent()

private val JS_NIGHT_ON = """
(function(){
  var id = '$STYLE_ID_NIGHT';
  var s = document.getElementById(id);
  if (s) s.remove();

  var isDark = false;
  try {
    var candidates = [document.body, document.documentElement];
    for (var i = 0; i < candidates.length; i++) {
      var el = candidates[i];
      if (!el) continue;
      var bg = window.getComputedStyle(el).backgroundColor || '';
      var m = bg.match(/rgba?\(\s*(\d+)[,\s]+(\d+)[,\s]+(\d+)(?:[,\s\/]+([\d.]+))?/);
      if (m) {
        var r = +m[1], g = +m[2], b = +m[3];
        var a = (m[4] === undefined) ? 1 : parseFloat(m[4]);
        if (a < 0.1) continue;
        var lum = 0.299 * r + 0.587 * g + 0.114 * b;
        if (lum < 110) { isDark = true; break; }
      }
    }
  } catch(e) {}

  if (isDark) {
    s = document.createElement('style');
    s.id = id;
    s.textContent = 'html{background-color:#0a0a0a !important;filter:brightness(1.05) contrast(1.02) !important;}';
    (document.head || document.documentElement).appendChild(s);
    return;
  }

  s = document.createElement('style');
  s.id = id;
  s.textContent =
    'html{filter:invert(100%) hue-rotate(180deg) !important;background-color:#000 !important;}' +
    'img,video,picture,canvas,svg,iframe,' +
    '[style*="background-image"],[style*="background: url"]{' +
    'filter:invert(100%) hue-rotate(180deg) !important;}';
  (document.head || document.documentElement).appendChild(s);
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

private val JS_FORCE_ZOOM_ON = """
(function(){
  try {
    var m = document.querySelector('meta[name="viewport"]');
    if (m) {
      var c = m.getAttribute('content') || '';
      c = c.replace(/user-scalable\s*=\s*(no|0)/gi, 'user-scalable=yes');
      c = c.replace(/maximum-scale\s*=\s*[\d.]+/gi, 'maximum-scale=10');
      c = c.replace(/minimum-scale\s*=\s*[\d.]+/gi, 'minimum-scale=0.25');
      if (c.indexOf('user-scalable') < 0) c += ', user-scalable=yes';
      if (c.indexOf('maximum-scale') < 0) c += ', maximum-scale=10';
      m.setAttribute('content', c);
    } else {
      var nm = document.createElement('meta');
      nm.name = 'viewport';
      nm.content = 'width=device-width, initial-scale=1.0, minimum-scale=0.25, maximum-scale=10.0, user-scalable=yes';
      (document.head || document.documentElement).appendChild(nm);
    }
  } catch(e) {}
})();
""".trimIndent()

private val JS_FIND_FAVICON_URL = """
(function(){
  try {
    var sels = [
      'link[rel~="icon"]',
      'link[rel="shortcut icon"]',
      'link[rel="apple-touch-icon"]',
      'link[rel="apple-touch-icon-precomposed"]'
    ];
    for (var i = 0; i < sels.length; i++) {
      var els = document.querySelectorAll(sels[i]);
      for (var j = 0; j < els.length; j++) {
        if (els[j].href) return els[j].href;
      }
    }
  } catch(e) {}
  return '';
})();
""".trimIndent()

private val JS_SNIFF_RESOURCES = """
(function(){
  var out = [];
  var seen = {};
  var MAX_HTML_SCAN = 500000;

  function push(url, cat) {
    if (!url) return;
    url = String(url).trim();
    if (!url) return;
    if (url.indexOf('data:') === 0) return;
    if (url.indexOf('blob:') === 0) return;
    if (url.indexOf('javascript:') === 0) return;
    if (url.indexOf('about:') === 0) return;
    if (url.indexOf('mailto:') === 0) return;
    if (url.indexOf('tel:') === 0) return;
    if (url.charAt(0) === '#') return;
    try {
      if (url.indexOf('//') === 0) url = location.protocol + url;
      else if (url.indexOf('http') !== 0) url = new URL(url, location.href).href;
    } catch(e) { return; }
    url = url.replace(/[.,;:!?)\]]+${'$'}/, '');
    if (seen[url]) return;
    seen[url] = true;
    out.push({url: url, category: cat || 'other'});
  }

  function catByExt(url) {
    var clean = url.replace(/[#?].*${'$'}/, '').toLowerCase();
    if (/\.(jpg|jpeg|png|gif|webp|bmp|svg|ico|tif|tiff|avif|heic|heif|jfif|apng)${'$'}/.test(clean)) return 'image';
    if (/\.(mp4|webm|mkv|mov|avi|flv|wmv|ts|m4v|m3u8|mpd|3gp|ogv|rm|rmvb|asf)${'$'}/.test(clean)) return 'video';
    if (/\.(mp3|wav|flac|aac|m4a|ogg|opus|wma|ape|mka|amr|mid|midi)${'$'}/.test(clean)) return 'audio';
    if (/\.(zip|rar|7z|tar|gz|tgz|bz2|apk|ipa|exe|dmg|iso|pdf|doc|docx|xls|xlsx|ppt|pptx|epub|mobi|azw3|txt|md|json|xml|csv|torrent|swf|crx|deb|rpm|msi|jar)${'$'}/.test(clean)) return 'file';
    return null;
  }

  document.querySelectorAll('img').forEach(function(el){
    [el.currentSrc, el.src,
     el.getAttribute('data-src'), el.getAttribute('data-original'),
     el.getAttribute('data-lazy-src'), el.getAttribute('data-echo'),
     el.getAttribute('data-url'), el.getAttribute('data-image'),
     el.getAttribute('data-img'), el.getAttribute('data-lazyload'),
     el.getAttribute('data-original-src')
    ].forEach(function(u){ push(u, 'image'); });
    var ss = el.getAttribute('srcset');
    if (ss) ss.split(',').forEach(function(part){
      var u = part.trim().split(/\s+/)[0];
      push(u, 'image');
    });
  });

  document.querySelectorAll('picture source').forEach(function(el){
    var ss = el.getAttribute('srcset');
    if (ss) ss.split(',').forEach(function(part){
      var u = part.trim().split(/\s+/)[0];
      push(u, 'image');
    });
    push(el.src, 'image');
  });

  document.querySelectorAll('video').forEach(function(el){
    push(el.src, 'video');
    push(el.currentSrc, 'video');
    var poster = el.getAttribute('poster');
    if (poster) push(poster, 'image');
    el.querySelectorAll('source').forEach(function(s){ push(s.src, 'video'); });
  });

  document.querySelectorAll('audio').forEach(function(el){
    push(el.src, 'audio');
    push(el.currentSrc, 'audio');
    el.querySelectorAll('source').forEach(function(s){ push(s.src, 'audio'); });
  });

  document.querySelectorAll('svg image').forEach(function(el){
    push(el.getAttribute('href'), 'image');
    push(el.getAttributeNS('http://www.w3.org/1999/xlink', 'href'), 'image');
  });

  document.querySelectorAll('[style]').forEach(function(el){
    var s = el.getAttribute('style') || '';
    var re = /url\((['"]?)(.*?)\1\)/g;
    var m;
    while ((m = re.exec(s))) {
      push(m[2], catByExt(m[2]) || 'image');
    }
  });

  function scanRules(rules) {
    for (var j = 0; j < rules.length; j++) {
      var rule = rules[j];
      var text = rule.cssText || '';
      if (text.indexOf('url(') >= 0) {
        var re = /url\((['"]?)(.*?)\1\)/g;
        var m;
        while ((m = re.exec(text))) {
          push(m[2], catByExt(m[2]) || 'image');
        }
      }
      if (rule.cssRules && rule.cssRules.length) scanRules(rule.cssRules);
    }
  }
  try {
    for (var i = 0; i < document.styleSheets.length; i++) {
      var sheet = document.styleSheets[i];
      var rules;
      try { rules = sheet.cssRules || sheet.rules; } catch(e) { continue; }
      if (rules) scanRules(rules);
    }
  } catch(e) {}

  document.querySelectorAll('a[href]').forEach(function(el){
    var href = el.href;
    if (!href) return;
    var c = catByExt(href);
    if (c) push(href, c);
    if (href.indexOf('magnet:') === 0) push(href, 'file');
  });

  document.querySelectorAll('link[href]').forEach(function(el){
    var rel = (el.getAttribute('rel') || '').toLowerCase();
    var href = el.href;
    if (!href) return;
    if (rel.indexOf('stylesheet') >= 0) {
      push(href, 'file');
    } else if (rel.indexOf('icon') >= 0) {
      push(href, 'image');
    } else if (rel.indexOf('preload') >= 0 || rel.indexOf('prefetch') >= 0) {
      var as = (el.getAttribute('as') || '').toLowerCase();
      var c = catByExt(href);
      if (as === 'image') c = 'image';
      else if (as === 'video') c = 'video';
      else if (as === 'audio') c = 'audio';
      else if (as === 'font') c = 'file';
      else if (!c) c = 'file';
      push(href, c);
    }
  });

  document.querySelectorAll('script[src]').forEach(function(el){
    push(el.src, 'file');
  });

  document.querySelectorAll('iframe[src]').forEach(function(el){ push(el.src, 'file'); });
  document.querySelectorAll('embed[src]').forEach(function(el){ push(el.src, 'file'); });
  document.querySelectorAll('object[data]').forEach(function(el){ push(el.getAttribute('data'), 'file'); });

  document.querySelectorAll('meta[content]').forEach(function(el){
    var prop = (el.getAttribute('property') || el.getAttribute('name') || el.getAttribute('itemprop') || '').toLowerCase();
    var content = el.getAttribute('content');
    if (!content || content.length < 8) return;
    if (prop.indexOf('image') >= 0 || prop.indexOf('thumbnail') >= 0 || prop.indexOf('poster') >= 0) {
      push(content, 'image');
    } else if (prop.indexOf('video') >= 0) {
      push(content, 'video');
    } else if (prop.indexOf('audio') >= 0) {
      push(content, 'audio');
    } else if (prop.indexOf('url') >= 0) {
      var c = catByExt(content);
      if (c) push(content, c);
    }
  });

  try {
    var html = document.documentElement.outerHTML || '';
    if (html.length > MAX_HTML_SCAN) html = html.substring(0, MAX_HTML_SCAN);
    var reAll = /(?:https?:)?\/\/[^\s"'<>\\)]+/gi;
    var mMatch;
    var budget = 1000;
    while ((mMatch = reAll.exec(html)) && budget-- > 0) {
      var u = mMatch[0];
      if (!u || u.length < 10) continue;
      var c = catByExt(u);
      if (c) push(u, c);
    }
  } catch(e) {}

  try {
    var entries = performance.getEntriesByType ? performance.getEntriesByType('resource') : [];
    entries.forEach(function(entry){
      var u = entry.name;
      if (!u) return;
      var init = (entry.initiatorType || '').toLowerCase();
      var c = catByExt(u);
      if (c) { push(u, c); return; }
      if (init === 'img' || init === 'image') push(u, 'image');
      else if (init === 'video') push(u, 'video');
      else if (init === 'audio') push(u, 'audio');
      else if (init === 'xmlhttprequest' || init === 'fetch') {
        if (/\.(m3u8|mpd|mp4|webm|mkv|ts)(${'$'}|[?#])/i.test(u)) push(u, 'video');
      }
    });
  } catch(e) {}
  try {
    var xhrUrls = window.__mote_xhr_urls || [];
    for (var x = 0; x < xhrUrls.length; x++) {
      var ux = xhrUrls[x];
      if (ux) push(ux, catByExt(ux) || 'other');
    }
  } catch(e) {}

  try {
    document.querySelectorAll('video').forEach(function(el){
      var candidates = [
        el.currentSrc, el.src,
        el.getAttribute('data-src'),
        el.getAttribute('data-url'),
        el.getAttribute('data-video-src'),
        el.getAttribute('data-source'),
        el.getAttribute('data-play-url')
      ];
      candidates.forEach(function(u){ if (u) push(u, 'video'); });
      el.querySelectorAll('source').forEach(function(s){
        push(s.src, 'video');
        push(s.getAttribute('data-src'), 'video');
      });
    });
  } catch(e) {}

  try {
    var all = performance.getEntriesByType('resource') || [];
    for (var k = 0; k < all.length; k++) {
      var e = all[k];
      if (!e || !e.name) continue;
      var nm = e.name;
      if (nm.indexOf('/video/tos/') >= 0 ||
          nm.indexOf('/alisg/') >= 0 ||
          nm.indexOf('/tos-alisg-') >= 0 ||
          nm.indexOf('mime=video') >= 0) {
        push(nm, 'video');
      }
    }
  } catch(e) {}

  return JSON.stringify(out);
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
    val customIcon: (@Composable (isActive: Boolean) -> Unit)? = null,
    val customItem: (@Composable (isActive: Boolean) -> Unit)? = null,
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

data class BookmarkItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String? = null,
    val isFolder: Boolean = url == null,
    val children: MutableList<BookmarkItem> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
)

enum class SniffCategory(val label: String) {
    VIDEO("视频"),
    AUDIO("音频"),
    IMAGE("图片"),
    FILE("文件"),
    OTHER("其它"),
}

data class SniffedResource(
    val url: String,
    val category: SniffCategory,
    val filename: String,
    val fromNetwork: Boolean = false,
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
    ToolDef("zoom", "网页缩放", MiuixIcons.VerticalSplit),
    ToolDef("translate", "网页翻译", MiuixIcons.Translate, isToggle = true),
    ToolDef("print", "网页打印", MiuixIcons.Rename),
    ToolDef("sniff", "资源嗅探", MiuixIcons.RecordingTape),
    ToolDef("play_in_player", "播放视频", MiuixIcons.Play),
    ToolDef("tts", "语音朗读", MiuixIcons.AppRecording),
    ToolDef("save_page", "保存网页", MiuixIcons.File),
    ToolDef("dev_tools", "开发工具", MiuixIcons.Weeks),
    ToolDef("exit", "退出", MiuixIcons.Close2),
)

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

private fun sniffCategoryFromNetwork(url: String): SniffCategory? {
    val lower = url.lowercase()
    val clean = lower.substringBefore('#').substringBefore('?')

    val host: String = try {
        val s = lower.substringAfter("://", "")
        s.substringBefore('/').substringBefore(':')
    } catch (_: Exception) { "" }

    if (clean.endsWith(".mp4") || clean.endsWith(".webm") || clean.endsWith(".mkv") ||
        clean.endsWith(".mov") || clean.endsWith(".m4v") || clean.endsWith(".flv") ||
        clean.endsWith(".avi") || clean.endsWith(".wmv") || clean.endsWith(".3gp") ||
        clean.endsWith(".ogv") || clean.endsWith(".ts") ||
        clean.endsWith(".m3u8") || clean.endsWith(".mpd")
    ) return SniffCategory.VIDEO

    if (lower.contains("mime=video") || lower.contains("mime%3dvideo") ||
        lower.contains("type=video") || lower.contains("format=mp4") ||
        lower.contains("/videoplayback") ||
        lower.contains("/video/tos/") ||
        lower.contains("/tos-alisg-") ||
        lower.contains("/alisg/") ||
        (lower.contains(".mp4") && lower.contains("range="))
    ) return SniffCategory.VIDEO

    val videoCdnHosts = listOf(
        "muscdn.com", "tiktokcdn", "bytecdn", "byteoversea", "bytegoofy",
        "douyinvod", "ixigua", "huoshan",
        "kwaicdn", "ks-cdn", "static.yximgs",
        "bilivideo", "hdslb", "bilibili",
        "youku", "iqiyi", "qiyipic", "letv", "pptv",
        "video.qq.com", "vd.l.qq.com", "vv.video.qq.com",
        "mgtv.com", "mgtvcdn", "hunantv",
        "cloudfront.net", "akamaized.net", "fastly", "cloudflare",
        "b-cdn.net", "uplynk", "streamable",
    )
    if (videoCdnHosts.any { host.contains(it) }) {
        if (lower.contains("/video/") || lower.contains("/tos/") ||
            lower.contains("/vs/") || lower.contains("/mp4/") ||
            lower.contains("/hls/") || lower.contains("/dash/") ||
            lower.contains("/vod/") || lower.contains("/play/") ||
            lower.contains("/stream/") || lower.contains("/media/") ||
            lower.contains("/object/") || lower.contains("/origin/")
        ) return SniffCategory.VIDEO

        if (clean.length > 80 &&
            !clean.endsWith(".js") && !clean.endsWith(".css") &&
            !clean.endsWith(".html") && !clean.endsWith(".htm") &&
            !clean.endsWith(".jpg") && !clean.endsWith(".jpeg") &&
            !clean.endsWith(".png") && !clean.endsWith(".gif") &&
            !clean.endsWith(".svg") && !clean.endsWith(".webp") &&
            !clean.endsWith(".woff") && !clean.endsWith(".woff2") &&
            !clean.endsWith(".ttf") && !clean.endsWith(".otf") &&
            !clean.endsWith(".json")
        ) return SniffCategory.VIDEO
    }

    if (clean.endsWith(".mp3") || clean.endsWith(".wav") || clean.endsWith(".flac") ||
        clean.endsWith(".aac") || clean.endsWith(".m4a") || clean.endsWith(".ogg") ||
        clean.endsWith(".opus") || clean.endsWith(".wma") || clean.endsWith(".ape") ||
        lower.contains("mime=audio") || lower.contains("mime%3daudio") ||
        lower.contains("type=audio") || lower.contains("format=mp3")
    ) return SniffCategory.AUDIO

    if (clean.endsWith(".jpg") || clean.endsWith(".jpeg") || clean.endsWith(".png") ||
        clean.endsWith(".gif") || clean.endsWith(".webp") || clean.endsWith(".bmp") ||
        clean.endsWith(".svg") || clean.endsWith(".ico") || clean.endsWith(".avif") ||
        clean.endsWith(".heic") || clean.endsWith(".heif")
    ) return SniffCategory.IMAGE

    if (clean.endsWith(".zip") || clean.endsWith(".rar") || clean.endsWith(".7z") ||
        clean.endsWith(".tar") || clean.endsWith(".gz") || clean.endsWith(".apk") ||
        clean.endsWith(".pdf") || clean.endsWith(".doc") || clean.endsWith(".docx") ||
        clean.endsWith(".xls") || clean.endsWith(".xlsx") || clean.endsWith(".ppt") ||
        clean.endsWith(".pptx") || clean.endsWith(".epub") || clean.endsWith(".torrent") ||
        clean.endsWith(".iso") || clean.endsWith(".dmg") || clean.endsWith(".exe") ||
        clean.endsWith(".msi") || clean.endsWith(".deb") || clean.endsWith(".rpm")
    ) return SniffCategory.FILE

    return null
}

private fun looksLikeMediaByHeaders(request: WebResourceRequest?): Boolean {
    val headers = request?.requestHeaders ?: return false
    val range = headers.entries.firstOrNull { it.key.equals("Range", ignoreCase = true) }?.value
    val accept = headers.entries.firstOrNull { it.key.equals("Accept", ignoreCase = true) }?.value

    if (accept != null && (accept.contains("video/", true) || accept.contains("audio/", true))) {
        return true
    }

    if (range != null) {
        val clean = request.url.toString().lowercase().substringBefore('#').substringBefore('?')
        val hasExt = clean.substringAfterLast('/').contains(".")
        if (!hasExt) return true

        val m = Regex("bytes=(\\d+)-(\\d*)").find(range)
        if (m != null) {
            val start = m.groupValues[1].toLongOrNull() ?: 0
            if (start > 0) return true
        }
    }

    return false
}

// ============================================================
// 系统原生文本选择工具栏
// ============================================================

internal class SystemTextToolbar(private val view: View) : TextToolbar {

    private var actionMode: ActionMode? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        actionMode?.finish()

        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()

                if (onSelectAllRequested != null) {
                    menu?.add(Menu.NONE, MENU_ID_SELECT_ALL, 1, android.R.string.selectAll)
                        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
                if (onCutRequested != null) {
                    menu?.add(Menu.NONE, MENU_ID_CUT, 2, android.R.string.cut)
                        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
                if (onCopyRequested != null) {
                    menu?.add(Menu.NONE, MENU_ID_COPY, 3, android.R.string.copy)
                        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
                if (onPasteRequested != null) {
                    menu?.add(Menu.NONE, MENU_ID_PASTE, 4, android.R.string.paste)
                        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
                return true
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    MENU_ID_SELECT_ALL -> onSelectAllRequested?.invoke()
                    MENU_ID_CUT -> onCutRequested?.invoke()
                    MENU_ID_COPY -> onCopyRequested?.invoke()
                    MENU_ID_PASTE -> onPasteRequested?.invoke()
                    else -> return false
                }
                mode?.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                actionMode = null
                status = TextToolbarStatus.Hidden
            }
        }

        status = TextToolbarStatus.Shown
        actionMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        } else {
            view.startActionMode(callback)
        }
    }

    override fun hide() {
        actionMode?.finish()
        actionMode = null
        status = TextToolbarStatus.Hidden
    }

    companion object {
        private const val MENU_ID_SELECT_ALL = 1
        private const val MENU_ID_CUT = 2
        private const val MENU_ID_COPY = 3
        private const val MENU_ID_PASTE = 4
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MoteAppContextHolder.appContext = applicationContext

        enableEdgeToEdge()

        try {
            val p = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            if (p.getBoolean("web_fullscreen", false)) {
                p.edit().putBoolean("web_fullscreen", false).apply()
            }
        } catch (_: Exception) {}

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
        } catch (_: Exception) {}

        try {
            InitialInsetsFallback.navBarBottomPx =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    windowManager.currentWindowMetrics.windowInsets
                        .getInsets(android.view.WindowInsets.Type.navigationBars())
                        .bottom
                } else {
                    @Suppress("DEPRECATION")
                    val resId = resources.getIdentifier(
                        "navigation_bar_height", "dimen", "android"
                    )
                    if (resId > 0) resources.getDimensionPixelSize(resId) else 0
                }
        } catch (_: Exception) {
            InitialInsetsFallback.navBarBottomPx = 0
        }

        window.decorView.requestApplyInsets()

        run {
            val p = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            if (!p.getBoolean("web_saved_zooms_cleaned_v2", false)) {
                p.edit()
                    .remove("web_saved_zooms")
                    .putBoolean("web_saved_zooms_cleaned_v2", true)
                    .apply()
            }
        }

        run {
            val p = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            if (!p.getBoolean("home_banner_content_migrated_v1", false)) {
                val oldContent = p.getInt("home_banner_content", 0)
                val oldShow = p.getBoolean("home_show_banner", true)
                val newContent = if (!oldShow) 0 else oldContent + 1
                p.edit()
                    .putInt("home_banner_content", newContent)
                    .putBoolean("home_banner_content_migrated_v1", true)
                    .apply()
            }
        }

        run {
            val prefs = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            val snapshotRaw = prefs.getString("session_snapshot", null)
            if (!snapshotRaw.isNullOrBlank()) {
                try {
                    val arr = org.json.JSONArray(snapshotRaw)
                    val urls = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        val u = arr.optString(i, "")
                        if (u.isNotBlank()) urls.add(u)
                    }
                    if (urls.isNotEmpty()) {
                        SessionSnapshotStore.pendingRestore = urls
                    }
                } catch (_: Exception) {}
            }
            prefs.edit().putString("session_snapshot", "").apply()
            SessionSnapshotStore.userExiting = false
        }

        try {
            val launchUrl = intent?.data?.toString()
            if (!launchUrl.isNullOrBlank()) {
                InitialLaunchUrlStore.pending = launchUrl
            }
        } catch (_: Exception) {}
        try {
            val playingUrl = PlaybackSessionHolder.currentUrl
            val player = PlaybackSessionHolder.player
            val isLauncherIntent = intent?.action == Intent.ACTION_MAIN &&
                    intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
            if (playingUrl.isNotBlank() && player != null && isLauncherIntent) {
                NativePlayerActivity.start(
                    context = this,
                    url = playingUrl,
                    title = PlaybackSessionHolder.currentTitle.ifBlank { "视频播放" },
                    startPositionMs = player.currentPosition,
                )
            }
        } catch (_: Exception) {}
        setContent {
            MoteThemeWrapper {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }

        window.decorView.post {
            try { window.decorView.requestApplyInsets() } catch (_: Exception) {}
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        try {
            val launchUrl = intent.data?.toString()
            if (!launchUrl.isNullOrBlank()) {
                PendingOpenInNewWindowStore.pending = launchUrl
            }
        } catch (_: Exception) {}

        try {
            val playingUrl = PlaybackSessionHolder.currentUrl
            val player = PlaybackSessionHolder.player
            val isLauncherIntent = intent.action == Intent.ACTION_MAIN &&
                    intent.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
            if (playingUrl.isNotBlank() && player != null && isLauncherIntent) {
                NativePlayerActivity.start(
                    context = this,
                    url = playingUrl,
                    title = PlaybackSessionHolder.currentTitle.ifBlank { "视频播放" },
                    startPositionMs = player.currentPosition,
                )
            }
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        try {
            window.decorView.requestApplyInsets()

            val prefs = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            val fullscreen = prefs.getBoolean("web_fullscreen", false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (fullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            pendingWebFullscreenView = WeakReference(null)
            pendingWebFullscreenCallback = WeakReference(null)
            if (needsExitWebFullscreen) {
                needsExitWebFullscreen = false
                webViewRef.get()?.evaluateJavascript(JS_EXIT_WEB_FULLSCREEN, null)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        try {
            val prefs = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("web_fullscreen", false)) {
                prefs.edit().putBoolean("web_fullscreen", false).apply()
            }

            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        var webViewRef: WeakReference<WebView> = WeakReference(null)
        var pendingWebFullscreenView: WeakReference<View> = WeakReference(null)
        var pendingWebFullscreenCallback: WeakReference<WebChromeClient.CustomViewCallback> = WeakReference(null)
        var needsExitWebFullscreen: Boolean = false
        var suppressOrientationRestore: Boolean = false
    }
}

@Composable
fun AppRoot() {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    val screenCornerRadius = rememberScreenCornerRadius(fallback = 28.dp)

    val maxBlurPx = with(density) { 5.dp.toPx() }
    val supportBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val rootView = LocalView.current
    val systemTextToolbar = remember(rootView) { SystemTextToolbar(rootView) }

    val pageStack = remember {
        mutableStateListOf(PageEntry(id = "browser_0", route = "browser"))
    }

    var exitingPageId by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(1f) }
    var animJob by remember { mutableStateOf<Job?>(null) }

    val ease = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }
    val pushSpec = remember { tween<Float>(durationMillis = 400, easing = ease) }
    val popSpec = remember { tween<Float>(durationMillis = 350, easing = ease) }
    val cancelSpec = remember { tween<Float>(durationMillis = 250, easing = ease) }

    val isSettled by remember { derivedStateOf { progress >= 1f } }

    fun pushPage(route: String) {
        // ★ 修复：不用时间节流，改为"栈顶已经是这个 route 才忽略"
        //   之前的时间节流（350ms）会把动画期间的所有 push 操作都吞掉，
        //   导致用户感觉"打开后要等动画结束才能操作"
        if (pageStack.lastOrNull()?.route == route) return

        animJob?.cancel()
        animJob = null

        if (exitingPageId != null) {
            if (pageStack.isNotEmpty()) {
                pageStack.removeAt(pageStack.lastIndex)
            }
            exitingPageId = null
        }

        progress = 0f
        pageStack.add(PageEntry(id = UUID.randomUUID().toString(), route = route))
        animJob = scope.launch {
            try {
                withFrameNanos { }
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = pushSpec,
                ) { value, _ -> progress = value }
            } catch (_: CancellationException) {
            }
        }
    }

    fun popPage() {
        if (pageStack.size <= 1) return
        if (exitingPageId != null) return

        animJob?.cancel()
        animJob = null

        val target = pageStack.last()
        exitingPageId = target.id
        progress = 1f

        animJob = scope.launch {
            try {
                animate(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = popSpec,
                ) { value, _ -> progress = value }
            } catch (_: CancellationException) {
                return@launch
            }
            withContext(NonCancellable) {
                if (exitingPageId == target.id) {
                    progress = 1f
                    pageStack.removeAt(pageStack.lastIndex)
                    exitingPageId = null
                }
            }
        }
    }

    PredictiveBackHandler(enabled = pageStack.size > 1 && exitingPageId == null) { backEvents ->
        animJob?.cancel()
        animJob = null

        val target = pageStack.lastOrNull()
        if (target == null) {
            backEvents.collect { }
            return@PredictiveBackHandler
        }

        var completed = false
        try {
            var firstEvent = true
            backEvents.collect { backEvent ->
                if (firstEvent && backEvent.progress >= 1f) {
                    firstEvent = false
                    return@collect
                }
                firstEvent = false
                progress = 1f - backEvent.progress
            }
            completed = true
        } catch (_: CancellationException) {
            completed = false
        } finally {
            try {
                withContext(NonCancellable) {
                    if (completed) {
                        exitingPageId = target.id
                        try {
                            animate(
                                initialValue = progress,
                                targetValue = 0f,
                                animationSpec = popSpec,
                            ) { value, _ -> progress = value }
                        } catch (_: Exception) { }
                        if (exitingPageId == target.id) {
                            progress = 1f
                            pageStack.removeAt(pageStack.lastIndex)
                            exitingPageId = null
                        }
                    } else {
                        try {
                            animate(
                                initialValue = progress,
                                targetValue = 1f,
                                animationSpec = cancelSpec,
                            ) { value, _ -> progress = value }
                        } catch (_: Exception) { }
                        progress = 1f
                    }
                }
            } catch (_: Exception) { }
        }
    }

    BackHandler(enabled = pageStack.size > 1 && exitingPageId != null) {
    }

    val openSettings: () -> Unit = remember { { pushPage("settings_main") } }
    val openScanner: () -> Unit = remember { { pushPage("scanner") } }
    val handlePop: () -> Unit = remember { { popPage() } }
    val handlePush: (String) -> Unit = remember { { r -> pushPage(r) } }

    CompositionLocalProvider(
        LocalTextToolbar provides systemTextToolbar
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val hasExiting = exitingPageId != null

            pageStack.forEachIndexed { index, entry ->
                val isExiting = entry.id == exitingPageId
                val isBelowExiting = hasExiting && !isExiting && index == pageStack.lastIndex - 1
                val isTop = !hasExiting && index == pageStack.lastIndex
                val isSecondTop = !hasExiting && index == pageStack.size - 2

                val surfaceIsSettled = when {
                    isExiting -> true
                    hasExiting -> true
                    isTop -> isSettled
                    else -> true
                }

                val shouldBlur = when {
                    isExiting -> false
                    isBelowExiting -> !isSettled
                    isSecondTop -> !isSettled
                    else -> false
                }

                val zi = if (isExiting) 9999f else index.toFloat()

                // ★ 用 Modifier.offset（layout 阶段）代替 graphicsLayer.translationX，
                //   让 hit test 位置与视觉位置一致
                val translationXf = when {
                    isExiting -> screenWidthPx * (1f - progress)
                    isBelowExiting -> -screenWidthPx / 6f * progress
                    isTop -> screenWidthPx * (1f - progress)
                    isSecondTop -> -screenWidthPx / 6f * progress
                    else -> 0f
                }
                val translationXpx = translationXf.roundToInt()

                key(entry.id) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(zi)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(translationXpx, 0) }
                                .graphicsLayer {
                                    if (supportBlur && shouldBlur) {
                                        val blurPx = maxBlurPx * progress
                                        renderEffect = if (blurPx > 0.5f) {
                                            RenderEffect.createBlurEffect(
                                                blurPx,
                                                blurPx,
                                                Shader.TileMode.CLAMP,
                                            ).asComposeRenderEffect()
                                        } else {
                                            null
                                        }
                                    } else {
                                        renderEffect = null
                                    }
                                }
                        ) {
                            RenderPageContent(
                                entry = entry,
                                surfaceIsSettled = surfaceIsSettled,
                                screenCornerRadius = screenCornerRadius,
                                isTop = (index == pageStack.lastIndex) && !hasExiting,
                                openSettings = openSettings,
                                openScanner = openScanner,
                                onPop = handlePop,
                                onPush = handlePush,
                            )
                        }

                        if (shouldBlur) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset { IntOffset(translationXpx, 0) }
                                    .graphicsLayer { alpha = 0.2f * progress }
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }

            AutoUpdateChecker()
        }
    }
}

@Composable
private fun RenderPageContent(
    entry: PageEntry,
    surfaceIsSettled: Boolean,
    screenCornerRadius: Dp,
    isTop: Boolean,
    openSettings: () -> Unit,
    openScanner: () -> Unit,
    onPop: () -> Unit,
    onPush: (String) -> Unit,
) {
    PageSurface(
        isSettled = surfaceIsSettled,
        cornerRadius = screenCornerRadius,
    ) {
        when (entry.route) {
            "browser" -> {
                val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
                CompositionLocalProvider(
                    LocalNavigationEventDispatcherOwner provides navOwner
                ) {
                    BrowserScreen(
                        onOpenSettings = openSettings,
                        onOpenScanner = openScanner,
                        onOpenDownloads = { onPush("downloads") },
                        isTop = isTop,
                    )
                }
            }
            "scanner" -> ScannerScreen(
                onCodeScanned = { raw ->
                    PendingScanResultStore.pending = raw
                    onPop()
                },
                onClose = { onPop() },
            )
            "settings_main" -> SettingsMainPage(
                onBack = { onPop() },
                onItemClick = { r -> onPush(r) },
            )
            "settings_home" -> SettingsHomePage(
                onBack = { onPop() },
                onNavigateToNewTabCustomization = {
                    SettingsLayoutNavArgs.initialTab = 0
                    onPush("settings_layout")
                }
            )
            "settings_search" -> SettingsSearchPage(onBack = { onPop() })
            "settings_web" -> SettingsWebPage(onBack = { onPop() })
            "settings_icon" -> SettingsIconPage(onBack = { onPop() })
            "settings_color" -> SettingsColorPage(onBack = { onPop() })
            "settings_layout" -> SettingsLayoutPage(
                onBack = { onPop() },
                onOpenCrop = { onPush("crop") },
                onNavigateToSearchSettings = {
                    SettingsLayoutNavArgs.flashSearchHistoryOnEnter = true
                    onPush("settings_search")
                }
            )
            "crop" -> CropScreenPage(onPop = { onPop() })
            "settings_permission" -> SettingsPermissionsPage(onBack = { onPop() })
            "settings_notification" -> SettingsNotificationPage(onBack = { onPop() })
            "settings_legal" -> SettingsLegalPage(
                onBack = { onPop() },
                onOpenDoc = { docRoute -> onPush(docRoute) },
            )
            "downloads" -> DownloadManagerScreen(
                onBack = { onPop() },
            )
            "settings_about" -> SettingsAboutPage(onBack = { onPop() })
            else -> SettingsSubPage(
                title = routeTitle(entry.route),
                onBack = { onPop() },
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
    "settings_layout" -> "界面"
    "settings_permission" -> "权限设置"
    "settings_notification" -> "通知设置"
    "settings_legal" -> "法律信息"
    "settings_about" -> "关于"
    else -> ""
}

// ============================================================
// ★ 壁纸裁剪页（作为页面栈里的一个普通页面）
// ============================================================

@Composable
private fun CropScreenPage(
    onPop: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap = PendingCropBitmapStore.pending

    if (bitmap == null) {
        LaunchedEffect(Unit) { onPop() }
        return
    }

    HomeWallpaperCropOverlay(
        sourceBitmap = bitmap,
        onConfirm = { cropped ->
            val ok = saveCroppedWallpaper(context, cropped)
            if (ok) {
                context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("home_background_content", 1)
                    .apply()
                // ★ 需求 1：通知界面设置页弹"控件颜色"建议弹窗
                WallpaperSavedEventStore.pending = true
            } else {
                Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show()
            }
            try { cropped.recycle() } catch (_: Exception) {}
            try { bitmap.recycle() } catch (_: Exception) {}
            PendingCropBitmapStore.pending = null
            onPop()
        },
        onCancel = {
            try { bitmap.recycle() } catch (_: Exception) {}
            PendingCropBitmapStore.pending = null
            onPop()
        },
    )
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
// 底部工具栏
// ============================================================

private class BrowserBottomBarItem(
    val id: String,
    val visible: Boolean = true,
    val content: @Composable (isEnabled: Boolean) -> Unit,
)

@Composable
private fun BrowserBottomBar(
    items: List<BrowserBottomBarItem>,
    barVisible: Boolean,
    onItemClick: (String) -> Unit,
    navBarBottomPadding: Dp,
    backgroundColor: Color,
) {
    val smoothEase = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }
    val weightSpec = remember {
        tween<Float>(durationMillis = 300, easing = smoothEase)
    }
    val alphaSpec = remember {
        tween<Float>(durationMillis = 220, easing = smoothEase)
    }

    AnimatedVisibility(
        visible = barVisible,
        enter = expandVertically(
            animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
            expandFrom = Alignment.Bottom,
        ) + fadeIn(tween(FULLSCREEN_ANIM_MS)),
        exit = shrinkVertically(
            animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
            shrinkTowards = Alignment.Bottom,
        ) + fadeOut(tween(FULLSCREEN_ANIM_MS)),
    ) {
        Surface(color = backgroundColor) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = navBarBottomPadding)
                    .height(BOTTOM_BAR_HEIGHT),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    key(item.id) {
                        val weightAnim by animateFloatAsState(
                            targetValue = if (item.visible) 1f else 0f,
                            animationSpec = weightSpec,
                            label = "bbWeight_${item.id}",
                        )
                        val alphaAnim by animateFloatAsState(
                            targetValue = if (item.visible) 1f else 0f,
                            animationSpec = alphaSpec,
                            label = "bbAlpha_${item.id}",
                        )

                        if (weightAnim > 0.001f) {
                            Box(
                                modifier = Modifier
                                    .weight(weightAnim)
                                    .fillMaxHeight()
                                    .graphicsLayer { alpha = alphaAnim }
                                    .clipToBounds()
                                    .clickable(
                                        enabled = alphaAnim > 0.5f,
                                        onClick = { onItemClick(item.id) },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                item.content(item.visible)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    onOpenSettings: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenDownloads: () -> Unit,
    isTop: Boolean
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val isUiDarkMode = surfaceColor.luminance() < 0.5f
    val isLightTheme = !isUiDarkMode
    val buttonBg = if (isLightTheme) Color.White
    else MiuixTheme.colorScheme.secondaryContainer

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val initialWindowId = remember { UUID.randomUUID().toString() }
    val windows = remember {
        mutableStateListOf<BrowserWindow>().apply {
            val pending = InitialLaunchUrlStore.pending
            val finalUrl: String = if (!pending.isNullOrBlank()) {
                InitialLaunchUrlStore.pending = null
                pending
            } else {
                val startupOption = prefs.getInt("home_startup_option", 0)
                when (startupOption) {
                    0 -> ""
                    1 -> prefs.getString("home_startup_custom_url", "") ?: ""
                    2 -> prefs.getString("last_session_url", "") ?: ""
                    else -> ""
                }
            }
            val nightIdx = try {
                prefs.getInt("web_night_mode", 0)
            } catch (_: Exception) {
                0
            }
            val nightMode = when (nightIdx) {
                1 -> false
                2 -> true
                else -> isUiDarkMode
            }
            add(
                BrowserWindow(
                    id = initialWindowId,
                    title = if (finalUrl.isBlank()) "新标签页" else "加载中…",
                    url = finalUrl,
                    nightMode = nightMode,
                    noImage = prefs.getBoolean("web_no_image", false),
                    desktopMode = prefs.getBoolean("web_desktop_mode", false),
                )
            )
        }
    }
    var activeWindowId by remember { mutableStateOf<String?>(initialWindowId) }
    val webViewMap = remember { mutableMapOf<String, WebView>() }

    val activeWindow: BrowserWindow? = windows.firstOrNull { it.id == activeWindowId }

    val faviconCache = remember { mutableStateMapOf<String, Bitmap>() }
    val pendingFaviconRequests = remember { mutableSetOf<String>() }

    var showToolSheet by remember { mutableStateOf(false) }
    var showWindowSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearBookmarkDialog by remember { mutableStateOf(false) }
    var showClearSearchHistoryDialog by remember { mutableStateOf(false) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkTitleInput by remember { mutableStateOf("") }
    var bookmarkUrlInput by remember { mutableStateOf("") }
    var bookmarkTargetFolderId by remember { mutableStateOf<String?>(null) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderTitleInput by remember { mutableStateOf("") }
    var pendingDeleteBookmarkId by remember { mutableStateOf<String?>(null) }
    var showDeleteBookmarkDialog by remember { mutableStateOf(false) }
    var addFromBookmarkSheet by remember { mutableStateOf(false) }
    var addFolderFromAddBookmark by remember { mutableStateOf(false) }

    var showNightModeConfirmDialog by remember { mutableStateOf(false) }
    var pendingNightModeTarget by remember { mutableStateOf<Boolean?>(null) }

    var showSniffSheet by remember { mutableStateOf(false) }
    var isSniffing by remember { mutableStateOf(false) }
    val sniffedResources = remember { mutableStateListOf<SniffedResource>() }
    val networkSniffedResources = remember { mutableStateListOf<SniffedResource>() }

    var urlInput by remember { mutableStateOf(TextFieldValue("")) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchBarExpanded by remember { mutableStateOf(false) }
    var forceHideHistory by remember { mutableStateOf(false) }

    var pendingDownload by remember { mutableStateOf<PendingDownload?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var findResultCurrent by remember { mutableIntStateOf(0) }
    var findResultTotal by remember { mutableIntStateOf(0) }

    var showZoomBar by remember { mutableStateOf(false) }
    var zoomFactor by remember { mutableStateOf(1.0f) }
    var pendingDefaultZoomChange by remember { mutableStateOf<Int?>(null) }

    var isFullscreen by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableFloatStateOf(0f) }
    var pullOffsetTarget by remember { mutableFloatStateOf(0f) }
    var isDraggingPull by remember { mutableStateOf(false) }
    val animatedPullOffset by animateFloatAsState(
        targetValue = pullOffsetTarget,
        animationSpec = if (isDraggingPull) snap<Float>() else tween(durationMillis = 280),
        label = "pullOffset",
    )

    var swipeEdgeOffset by remember { mutableFloatStateOf(0f) }
    var swipeEdgeDirection by remember { mutableIntStateOf(0) }
    var isDraggingSwipe by remember { mutableStateOf(false) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeEdgeOffset,
        animationSpec = if (isDraggingSwipe) snap<Float>() else tween(durationMillis = 220),
        label = "swipeEdge",
    )

    var htmlFullscreenView by remember { mutableStateOf<View?>(null) }
    var htmlFullscreenCallback by remember {
        mutableStateOf<WebChromeClient.CustomViewCallback?>(null)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarJob = remember { mutableStateOf<Job?>(null) }
    val translateJob = remember { mutableStateOf<Job?>(null) }

    var pendingDialogInfo by remember { mutableStateOf<UpdateState.HasUpdate?>(null) }
    var showPendingDialog by remember { mutableStateOf(false) }
    var isPendingDownloading by remember { mutableStateOf(false) }
    val currentVersionName = remember { readCurrentVersionNameFromContext(context) }

    var pendingFullscreenSync by remember { mutableStateOf<Boolean?>(null) }
    var pendingIncognitoSync by remember { mutableStateOf<Boolean?>(null) }

    var pullRefreshEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_pull_refresh", true))
    }

    var swipeNavEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_swipe_nav", true))
    }

    var dynamicToolIndex by remember {
        mutableIntStateOf(prefs.getInt("web_dynamic_tool", 1).coerceIn(0, 7))
    }
    var dynamicToolSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_dynamic_tool_suggest", true))
    }
    var isEnglishPage by remember { mutableStateOf(false) }

    var controlColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_control_color_mode", 0).coerceIn(0, 3))
    }

    var hasActiveDownloads by remember { mutableStateOf(false) }

    var useNativePlayerEnabled by remember {
        mutableStateOf(prefs.getInt("web_video_player", 0) == 0)
    }
    val useNativePlayerEnabledRef = remember { mutableStateOf(useNativePlayerEnabled) }
    LaunchedEffect(useNativePlayerEnabled) {
        useNativePlayerEnabledRef.value = useNativePlayerEnabled
    }

    val currentProgress = activeWindow?.progress ?: 0
    val smoothProgress by animateFloatAsState(
        targetValue = if (currentProgress in 1..99) currentProgress / 100f else 1f,
        animationSpec = tween(
            durationMillis = 260,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
        ),
        label = "pageProgress",
    )

    fun updateWindow(windowId: String, block: (BrowserWindow) -> Unit) {
        val idx = windows.indexOfFirst { it.id == windowId }
        if (idx >= 0) {
            val updated = windows[idx].copy().also(block)
            windows[idx] = updated
        }
    }

    fun applyCssZoomToWebView(wv: WebView, factor: Float) {
        val pct = (factor * 100).toInt()
        val js = if (pct == 100) {
            """
            (function(){
              var el = document.getElementById('__mote_zoom_style__');
              if (el) el.textContent = '';
            })();
            """.trimIndent()
        } else {
            """
            (function(){
              var el = document.getElementById('__mote_zoom_style__');
              if (!el) {
                el = document.createElement('style');
                el.id = '__mote_zoom_style__';
                (document.head || document.documentElement).appendChild(el);
              }
              el.textContent = 'html{zoom: ${pct}% !important;}';
            })();
            """.trimIndent()
        }
        try { wv.evaluateJavascript(js, null) } catch (_: Exception) {}
    }

    fun readNightModeFromPrefs(): Boolean {
        val idx = try { prefs.getInt("web_night_mode", 0) } catch (_: Exception) { 0 }
        return when (idx) {
            1 -> false
            2 -> true
            else -> isUiDarkMode
        }
    }

    var quickInputBarEnabled by remember {
        mutableStateOf(prefs.getBoolean("quick_input_bar_enabled", true))
    }
    var engineSwitcherBarEnabled by remember {
        mutableStateOf(prefs.getBoolean("engine_switcher_bar_enabled", true))
    }
    var scanModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean("scan_module_enabled", true))
    }
    var historyModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean("history_module_enabled", true))
    }

    var textZoomValue by remember {
        mutableIntStateOf(
            TEXT_ZOOM_VALUES.getOrElse(
                prefs.getInt("web_text_scale", TEXT_ZOOM_DEFAULT_INDEX)
                    .coerceIn(0, TEXT_ZOOM_VALUES.lastIndex)
            ) { 100 }
        )
    }
    var forceZoomEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_force_zoom_enabled", false))
    }

    var defaultZoomPercent by remember {
        mutableIntStateOf(
            ZOOM_RATIO_PERCENTAGES.getOrElse(
                prefs.getInt("web_default_zoom", ZOOM_RATIO_DEFAULT_INDEX)
                    .coerceIn(0, ZOOM_RATIO_PERCENTAGES.lastIndex)
            ) { 100 }
        )
    }

    var zoomRestoreEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_restore_zoom_enabled", true))
    }
    val savedZooms = remember { mutableStateMapOf<String, Float>() }

    val zoomRestoreEnabledRef = remember { mutableStateOf(zoomRestoreEnabled) }
    val savedZoomsRef = remember { mutableStateOf(savedZooms) }
    LaunchedEffect(zoomRestoreEnabled) { zoomRestoreEnabledRef.value = zoomRestoreEnabled }
    LaunchedEffect(savedZooms.size) { savedZoomsRef.value = savedZooms }

    fun loadSavedZooms() {
        try {
            val raw = prefs.getString("web_saved_zooms_v2", null) ?: return
            val json = JSONObject(raw)
            savedZooms.clear()
            json.keys().forEach { key ->
                val v = json.optDouble(key, 1.0).toFloat()
                if (v in 0.25f..3.0f) {
                    savedZooms[key] = v
                }
            }
        } catch (_: Exception) {}
    }

    fun persistSavedZooms() {
        try {
            val json = JSONObject()
            savedZooms.entries.take(200).forEach { (k, v) ->
                if (v in 0.25f..3.0f) {
                    json.put(k, v.toDouble())
                }
            }
            prefs.edit().putString("web_saved_zooms_v2", json.toString()).apply()
        } catch (_: Exception) {}
    }

    fun computeZoomForHost(host: String?): Float {
        if (host != null && zoomRestoreEnabled) {
            savedZooms[host]?.let { return it }
        }
        return defaultZoomPercent / 100f
    }

    fun applyZoomToActiveWebView(factor: Float, record: Boolean) {
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return
        zoomFactor = factor
        applyCssZoomToWebView(wv, factor)
        if (record && zoomRestoreEnabled) {
            val host = try { wv.url?.toUri()?.host } catch (_: Exception) { null }
            if (host != null) {
                savedZooms[host] = factor
                persistSavedZooms()
            }
        }
    }

    var globalNightMode by remember {
        mutableStateOf(readNightModeFromPrefs())
    }
    var globalNoImage by remember {
        mutableStateOf(prefs.getBoolean("web_no_image", false))
    }
    var globalDesktopMode by remember {
        mutableStateOf(prefs.getBoolean("web_desktop_mode", false))
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "quick_input_bar_enabled" ->
                    quickInputBarEnabled = prefs.getBoolean(key, true)
                "engine_switcher_bar_enabled" ->
                    engineSwitcherBarEnabled = prefs.getBoolean(key, true)
                "scan_module_enabled" ->
                    scanModuleEnabled = prefs.getBoolean(key, true)
                "history_module_enabled" ->
                    historyModuleEnabled = prefs.getBoolean(key, true)
                "web_text_scale" -> {
                    val idx = prefs.getInt(key, TEXT_ZOOM_DEFAULT_INDEX)
                        .coerceIn(0, TEXT_ZOOM_VALUES.lastIndex)
                    textZoomValue = TEXT_ZOOM_VALUES[idx]
                    webViewMap.values.forEach { wv ->
                        try { wv.settings.textZoom = textZoomValue } catch (_: Exception) {}
                    }
                }
                "web_force_zoom_enabled" -> {
                    val enabled = prefs.getBoolean(key, false)
                    if (enabled != forceZoomEnabled) {
                        forceZoomEnabled = enabled
                        webViewMap.values.forEach { wv ->
                            if (enabled) {
                                wv.evaluateJavascript(JS_FORCE_ZOOM_ON, null)
                            } else {
                                wv.reload()
                            }
                        }
                    }
                }
                "web_default_zoom" -> {
                    val idx = prefs.getInt(key, ZOOM_RATIO_DEFAULT_INDEX)
                        .coerceIn(0, ZOOM_RATIO_PERCENTAGES.lastIndex)
                    pendingDefaultZoomChange = ZOOM_RATIO_PERCENTAGES[idx]
                }
                "web_restore_zoom_enabled" -> {
                    zoomRestoreEnabled = prefs.getBoolean(key, true)
                }
                "web_night_mode" -> {
                    val enabled = readNightModeFromPrefs()
                    if (enabled != globalNightMode) {
                        globalNightMode = enabled
                        val id = activeWindowId
                        if (id != null) {
                            updateWindow(id) { it.nightMode = enabled }
                            webViewMap[id]?.evaluateJavascript(
                                if (enabled) JS_NIGHT_ON else JS_NIGHT_OFF, null
                            )
                        }
                    }
                }
                "web_no_image" -> {
                    val enabled = prefs.getBoolean(key, false)
                    if (enabled != globalNoImage) {
                        globalNoImage = enabled
                        val id = activeWindowId
                        if (id != null) {
                            updateWindow(id) { it.noImage = enabled }
                            webViewMap[id]?.evaluateJavascript(
                                if (enabled) JS_NO_IMG_ON else JS_NO_IMG_OFF, null
                            )
                        }
                    }
                }
                "web_desktop_mode" -> {
                    val enabled = prefs.getBoolean(key, false)
                    if (enabled != globalDesktopMode) {
                        globalDesktopMode = enabled
                        val id = activeWindowId
                        if (id != null) {
                            updateWindow(id) { it.desktopMode = enabled }
                            webViewMap[id]?.let { wv ->
                                val ua = if (enabled) DESKTOP_UA
                                else getMobileUserAgent(context)
                                wv.settings.userAgentString = ua
                                wv.reload()
                            }
                        }
                    }
                }
                "web_fullscreen" -> {
                    pendingFullscreenSync = prefs.getBoolean(key, false)
                }
                "web_incognito" -> {
                    pendingIncognitoSync = prefs.getBoolean(key, false)
                }
                "web_pull_refresh" -> {
                    pullRefreshEnabled = prefs.getBoolean(key, true)
                }
                "web_swipe_nav" -> {
                    swipeNavEnabled = prefs.getBoolean(key, true)
                }
                "web_dynamic_tool" -> {
                    dynamicToolIndex = prefs.getInt(key, 1).coerceIn(0, 7)
                }
                "web_dynamic_tool_suggest" -> {
                    dynamicToolSuggestEnabled = prefs.getBoolean(key, true)
                }
                "web_video_player" -> {
                    useNativePlayerEnabled = prefs.getInt(key, 0) == 0
                }
                "ui_control_color_mode" -> {
                    controlColorMode = prefs.getInt(key, 0).coerceIn(0, 3)
                }
                "web_remember_form" -> {
                    val enabled = prefs.getBoolean(key, true)
                    webViewMap.values.forEach { wv ->
                        try { wv.settings.saveFormData = enabled } catch (_: Exception) {}
                    }
                }
                "web_accept_cookie" -> {
                    val enabled = prefs.getBoolean(key, true)
                    try {
                        CookieManager.getInstance().setAcceptCookie(enabled)
                        CookieManager.getInstance().flush()
                    } catch (_: Exception) {}
                    webViewMap.values.forEach { wv ->
                        try {
                            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, enabled)
                        } catch (_: Exception) {}
                    }
                }
                "home_newtab_page_type",
                "home_newtab_custom_url",
                "home_startup_option",
                "home_startup_custom_url" -> {
                    val id = activeWindowId
                    if (id != null) {
                        val win = windows.firstOrNull { it.id == id }
                        val u = win?.url.orEmpty()
                        if (u.isBlank() || u == "about:blank") {
                            val newtabType = prefs.getInt("home_newtab_page_type", 0)
                            val target = if (newtabType == 1) {
                                prefs.getString("home_newtab_custom_url", "") ?: ""
                            } else ""
                            if (target.isNotBlank()) {
                                updateWindow(id) { it.url = target; it.title = "新标签页" }
                                webViewMap[id]?.loadUrl(target)
                            } else {
                                updateWindow(id) { it.url = ""; it.title = "新标签页" }
                                webViewMap[id]?.loadUrl("about:blank")
                            }
                        }
                    }
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val searchQueryHolder = remember { SearchQueryHolder() }
    var isSearchResultPage by remember { mutableStateOf(false) }

    val searchHistory = remember { mutableStateListOf<String>() }

    fun saveSearchHistory() {
        try {
            prefs.edit()
                .putString("search_history", searchHistory.joinToString("|||"))
                .apply()
        } catch (_: Exception) {
        }
    }

    fun loadSearchHistory() {
        val raw = prefs.getString("search_history", "") ?: ""
        searchHistory.clear()
        if (raw.isNotBlank()) {
            searchHistory.addAll(raw.split("|||").filter { it.isNotBlank() })
        }
    }

    fun addToSearchHistory(query: String) {
        if (activeWindow?.incognito == true) return
        val q = query.trim()
        if (q.isBlank()) return
        searchHistory.remove(q)
        searchHistory.add(0, q)
        while (searchHistory.size > MAX_SEARCH_HISTORY_SIZE) {
            searchHistory.removeAt(searchHistory.size - 1)
        }
        saveSearchHistory()
    }

    var customEngineUrl by remember {
        mutableStateOf(prefs.getString("custom_search_engine_url", "") ?: "")
    }

    val searchEngines: List<Pair<String, String>> = remember(customEngineUrl) {
        buildList {
            add("必应" to "https://cn.bing.com/search?q=%s")
            add("抖音" to "https://www.douyin.com/search/%s")
            add("百度" to "https://m.baidu.com/s?word=%s")
            add("神马" to "https://m.sm.cn/s?q=%s")
            add("搜狗" to "https://m.sogou.com/web/searchList.jsp?keyword=%s")
            add("谷歌" to "https://www.google.com/search?q=%s")
            if (customEngineUrl.isNotBlank()) {
                add("自定义" to customEngineUrl)
            }
        }
    }

    val history = remember { mutableStateListOf<HistoryEntry>() }

    fun isHomeUrl(url: String): Boolean {
        if (url.isBlank()) return true
        if (url == "about:blank") return true

        val newtabType = prefs.getInt("home_newtab_page_type", 0)
        if (newtabType == 1) {
            val custom = prefs.getString("home_newtab_custom_url", "") ?: ""
            if (custom.isNotBlank() && url == custom) return true
        }

        val startupType = prefs.getInt("home_startup_option", 0)
        if (startupType == 1) {
            val startup = prefs.getString("home_startup_custom_url", "") ?: ""
            if (startup.isNotBlank() && url == startup) return true
        }

        return false
    }

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
        if (isHomeUrl(url)) return

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

    val bookmarks = remember { mutableStateListOf<BookmarkItem>() }
    val bookmarkFolderPath = remember { mutableStateListOf<BookmarkItem>() }

    fun bookmarkItemToJson(item: BookmarkItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("url", item.url ?: "")
            put("isFolder", item.isFolder)
            put("time", item.timestamp)
            if (item.isFolder) {
                val childrenArray = JSONArray()
                item.children.forEach { child ->
                    childrenArray.put(bookmarkItemToJson(child))
                }
                put("children", childrenArray)
            }
        }
    }

    fun jsonToBookmarkItem(obj: JSONObject): BookmarkItem {
        val isFolder = obj.optBoolean("isFolder", false)
        val url = obj.optString("url", "").ifBlank { null }
        val children = mutableListOf<BookmarkItem>()
        if (isFolder) {
            val arr = obj.optJSONArray("children")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val childObj = arr.optJSONObject(i) ?: continue
                    children.add(jsonToBookmarkItem(childObj))
                }
            }
        }
        return BookmarkItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            title = obj.optString("title", "无标题"),
            url = if (isFolder) null else url,
            isFolder = isFolder,
            children = children,
            timestamp = obj.optLong("time", System.currentTimeMillis()),
        )
    }

    fun saveBookmarks() {
        try {
            val json = JSONArray()
            bookmarks.forEach { item ->
                json.put(bookmarkItemToJson(item))
            }
            prefs.edit().putString("bookmarks_v2", json.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadBookmarks() {
        val raw = prefs.getString("bookmarks_v2", null) ?: run {
            val oldRaw = prefs.getString("bookmarks", null) ?: return
            try {
                val oldJson = JSONArray(oldRaw)
                bookmarks.clear()
                for (i in 0 until oldJson.length()) {
                    val obj = oldJson.getJSONObject(i)
                    val url = obj.optString("url", "")
                    if (url.isBlank()) continue
                    bookmarks.add(
                        BookmarkItem(
                            title = obj.optString("title", "无标题"),
                            url = url,
                            timestamp = obj.optLong("time", 0L),
                        )
                    )
                }
                saveBookmarks()
                prefs.edit().remove("bookmarks").apply()
            } catch (_: Exception) {}
            return
        }
        try {
            val json = JSONArray(raw)
            bookmarks.clear()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                bookmarks.add(jsonToBookmarkItem(obj))
            }
        } catch (_: Exception) {
        }
    }

    fun currentBookmarkLevel(): MutableList<BookmarkItem> {
        return bookmarkFolderPath.lastOrNull()?.children ?: bookmarks
    }

    fun getAllBookmarkFolders(): List<Pair<BookmarkItem, String?>> {
        val result = mutableListOf<Pair<BookmarkItem, String?>>()
        fun collect(items: List<BookmarkItem>, sourcePath: String?) {
            items.forEach { item ->
                if (item.isFolder) {
                    result.add(item to sourcePath)
                    val newSource = if (sourcePath == null) item.title
                    else "$sourcePath / ${item.title}"
                    collect(item.children, newSource)
                }
            }
        }
        collect(bookmarks, null)
        return result
    }

    fun findFolderById(items: List<BookmarkItem>, id: String): BookmarkItem? {
        for (item in items) {
            if (item.id == id && item.isFolder) return item
            if (item.isFolder) {
                findFolderById(item.children, id)?.let { return it }
            }
        }
        return null
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
        bookmarkTargetFolderId = bookmarkFolderPath.lastOrNull()?.id
        showAddBookmarkDialog = true
    }

    fun openAddFolderDialog() {
        folderTitleInput = ""
        showAddFolderDialog = true
    }

    fun openDeleteBookmarkDialog(item: BookmarkItem) {
        pendingDeleteBookmarkId = item.id
        showDeleteBookmarkDialog = true
    }

    fun removeBookmarkById(items: MutableList<BookmarkItem>, id: String): Boolean {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            items.removeAt(idx)
            return true
        }
        for (item in items) {
            if (item.isFolder && removeBookmarkById(item.children, id)) {
                return true
            }
        }
        return false
    }

    fun findBookmarkById(items: MutableList<BookmarkItem>, id: String): BookmarkItem? {
        for (item in items) {
            if (item.id == id) return item
            if (item.isFolder) {
                findBookmarkById(item.children, id)?.let { return it }
            }
        }
        return null
    }

    fun getTargetFolderItems(): MutableList<BookmarkItem> {
        val folderId = bookmarkTargetFolderId ?: return bookmarks
        val folder = findFolderById(bookmarks, folderId)
        return folder?.children ?: bookmarks
    }

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

    val pendingPermission = remember { mutableStateOf<PermissionRequest?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val req = pendingPermission.value
        pendingPermission.value = null
        if (req == null) return@rememberLauncherForActivityResult
        if (result.values.all { it }) req.grant(req.resources) else req.deny()
    }

    LaunchedEffect(isFullscreen, isTop) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullscreen && isTop) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            Toast.makeText(context, "摇一摇退出全屏", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity ?: return@onDispose
            val window = activity.window
            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
            htmlFullscreenView?.let { v -> (v.parent as? ViewGroup)?.removeView(v) }
            htmlFullscreenView = null
            htmlFullscreenCallback = null
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
                        prefs.edit().putBoolean("web_fullscreen", false).apply()
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

    fun clearSearchFocus() {
        focusManager.clearFocus()
        isSearchFocused = false
        searchBarExpanded = false
    }

    fun navigateToUrl(id: String, url: String) {
        val wv = webViewMap[id] ?: return
        updateWindow(id) { it.url = url }
        searchQueryHolder.value = ""
        isSearchResultPage = false
        urlInput = TextFieldValue(url)
        wv.loadUrl(url)
    }

    fun goToHomePage() {
        val id = activeWindowId ?: return
        searchQueryHolder.value = ""
        isSearchResultPage = false
        urlInput = TextFieldValue("")

        val newtabType = prefs.getInt("home_newtab_page_type", 0)
        if (newtabType == 1) {
            val customUrl = prefs.getString("home_newtab_custom_url", "") ?: ""
            if (customUrl.isNotBlank()) {
                updateWindow(id) { it.url = customUrl; it.title = "新标签页" }
                webViewMap[id]?.loadUrl(customUrl)
                return
            }
        }
        updateWindow(id) { it.url = ""; it.title = "新标签页" }
        webViewMap[id]?.loadUrl("about:blank")
    }

    val isAtHomePage: () -> Boolean = {
        val win = activeWindow
        val url = win?.url ?: ""
        if (url.isBlank() || url == "about:blank") {
            true
        } else {
            var result = false
            val newtabType = prefs.getInt("home_newtab_page_type", 0)
            if (newtabType == 1) {
                val customUrl = prefs.getString("home_newtab_custom_url", "") ?: ""
                if (customUrl.isNotBlank() && url == customUrl) result = true
            }
            if (!result) {
                val startupType = prefs.getInt("home_startup_option", 0)
                if (startupType == 1) {
                    val startupUrl = prefs.getString("home_startup_custom_url", "") ?: ""
                    if (startupUrl.isNotBlank() && url == startupUrl) result = true
                }
            }
            if (!result && isHomeUrl(url)) result = true
            result
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

    LaunchedEffect(pendingFullscreenSync) {
        val v = pendingFullscreenSync ?: return@LaunchedEffect
        isFullscreen = v
        pendingFullscreenSync = null
    }

    LaunchedEffect(pendingIncognitoSync) {
        val v = pendingIncognitoSync ?: return@LaunchedEffect
        val id = activeWindowId
        if (id != null) {
            applyIncognitoMode(id, v)
        }
        pendingIncognitoSync = null
    }

    LaunchedEffect(isUiDarkMode) {
        val mode = prefs.getInt("web_night_mode", 0)
        if (mode != 0) return@LaunchedEffect
        if (isUiDarkMode == globalNightMode) return@LaunchedEffect
        globalNightMode = isUiDarkMode
        val id = activeWindowId ?: return@LaunchedEffect
        updateWindow(id) { it.nightMode = isUiDarkMode }
        webViewMap[id]?.evaluateJavascript(
            if (isUiDarkMode) JS_NIGHT_ON else JS_NIGHT_OFF, null
        )
    }

    LaunchedEffect(pendingDefaultZoomChange) {
        val v = pendingDefaultZoomChange ?: return@LaunchedEffect
        defaultZoomPercent = v
        webViewMap.forEach { (winId, wv) ->
            val host = try { wv.url?.toUri()?.host } catch (_: Exception) { null }
            val newZoom = computeZoomForHost(host)
            if (winId == activeWindowId) {
                zoomFactor = newZoom
            }
            if (newZoom != 1.0f) {
                applyCssZoomToWebView(wv, newZoom)
            } else {
                applyCssZoomToWebView(wv, 1.0f)
            }
        }
        pendingDefaultZoomChange = null
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            val id = activeWindowId
            if (id != null) {
                webViewMap[id]?.reload()
            }
            delay(1500)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (dm == null) return@LaunchedEffect
        while (true) {
            val active = withContext(Dispatchers.IO) {
                try {
                    val q = DownloadManager.Query().setFilterByStatus(
                        DownloadManager.STATUS_RUNNING or
                                DownloadManager.STATUS_PENDING or
                                DownloadManager.STATUS_PAUSED
                    )
                    dm.query(q).use { cursor -> cursor.count > 0 }
                } catch (_: Exception) {
                    false
                }
            }
            hasActiveDownloads = active
            delay(1500L)
        }
    }

    fun createWindow(url: String = ""): String {
        val inheritIncognito = windows.firstOrNull { it.id == activeWindowId }?.incognito == true
        val id = UUID.randomUUID().toString()

        val finalUrl: String = if (url.isBlank()) {
            val newtabType = prefs.getInt("home_newtab_page_type", 0)
            if (newtabType == 1) {
                prefs.getString("home_newtab_custom_url", "") ?: ""
            } else {
                ""
            }
        } else {
            url
        }

        windows.add(
            BrowserWindow(
                id = id,
                title = "新标签页",
                url = finalUrl,
                incognito = inheritIncognito,
                nightMode = globalNightMode,
                noImage = globalNoImage,
                desktopMode = globalDesktopMode,
            )
        )
        activeWindowId = id
        urlInput = TextFieldValue("")
        searchQueryHolder.value = ""
        isSearchResultPage = false
        clearSearchFocus()
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
        PullRefreshStateHolder.remove(id)
        SwipeNavStateHolder.remove(id)
        RecentVideoUrlStore.remove(id)
        windows.removeAt(index)

        if (activeWindowId == id) {
            val newIdx = index.coerceAtMost(windows.lastIndex)
            val newWin = windows.getOrNull(newIdx)
            activeWindowId = newWin?.id
            searchQueryHolder.value = ""
            isSearchResultPage = false
            val newWinUrl = newWin?.url.orEmpty()
            urlInput = if (isHomeUrl(newWinUrl) || newWinUrl.isBlank()) {
                TextFieldValue("")
            } else {
                TextFieldValue(newWinUrl)
            }
        }
    }

    fun switchToWindow(id: String) {
        if (id == activeWindowId) return
        activeWindowId = id
        searchQueryHolder.value = ""
        isSearchResultPage = false
        val win = windows.firstOrNull { it.id == id }
        val winUrl = win?.url.orEmpty()
        urlInput = if (isHomeUrl(winUrl) || winUrl.isBlank()) {
            TextFieldValue("")
        } else {
            TextFieldValue(winUrl)
        }
        clearSearchFocus()
    }

    fun loadUrlInActive(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return

        val rawIndex = prefs.getInt("default_search_engine", 0)
        val engineIndex = rawIndex.coerceIn(0, searchEngines.lastIndex)

        val isDirectUrl = trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("ftp://", ignoreCase = true) ||
                isWebsite(trimmed)

        val url = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) ||
                    trimmed.startsWith("ftp://", ignoreCase = true) -> trimmed
            isWebsite(trimmed) -> "https://$trimmed"
            else -> {
                val engine = searchEngines.getOrElse(engineIndex) { searchEngines[0] }
                engine.second.replace("%s", URLEncoder.encode(trimmed, "UTF-8"))
            }
        }

        if (isDirectUrl) {
            searchQueryHolder.value = ""
            isSearchResultPage = false
        } else {
            searchQueryHolder.value = trimmed
            isSearchResultPage = true
            addToSearchHistory(trimmed)
        }
        updateWindow(id) { it.url = url }
        urlInput = if (isDirectUrl) {
            TextFieldValue(url)
        } else {
            TextFieldValue(trimmed)
        }
        wv.loadUrl(url)
    }

    fun switchEngineAndSearch(newEngineIndex: Int) {
        val query = searchQueryHolder.value
        if (query.isBlank()) return
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return
        val index = newEngineIndex.coerceIn(0, searchEngines.lastIndex)
        prefs.edit()
            .putInt("default_search_engine", index)
            .putInt("default_search_engine_display", index)
            .apply()
        urlInput = TextFieldValue(query)
        val engine = searchEngines.getOrElse(index) { searchEngines[0] }
        val url = engine.second.replace("%s", URLEncoder.encode(query, "UTF-8"))
        updateWindow(id) { it.favicon = null; it.url = url }
        wv.loadUrl(url)
    }

    fun insertText(text: String) {
        val current = urlInput.text
        val isPrefix = text.startsWith("http") || text.startsWith("ftp") || text == "www."
        val newText = if (isPrefix) text + current else current + text
        urlInput = TextFieldValue(newText)
    }

    fun loadFaviconFromExplicitUrl(windowId: String, faviconUrl: String, expectedHost: String) {
        if (faviconUrl.isBlank() || !faviconUrl.startsWith("http")) return
        if (faviconUrl in pendingFaviconRequests) return
        pendingFaviconRequests.add(faviconUrl)
        Thread {
            try {
                val conn = URL(faviconUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.connect()
                val bmp = BitmapFactory.decodeStream(conn.inputStream)
                conn.disconnect()
                if (bmp != null) {
                    scope.launch {
                        val t = windows.firstOrNull { it.id == windowId } ?: return@launch
                        val currentHost = try { t.url.toUri().host } catch (_: Exception) { null }
                        if (currentHost != expectedHost) return@launch
                        val existing = faviconCache[expectedHost]
                        if (existing == null ||
                            bmp.width * bmp.height > existing.width * existing.height
                        ) {
                            faviconCache[expectedHost] = bmp
                            updateWindow(windowId) { it.favicon = bmp }
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingFaviconRequests.remove(faviconUrl)
            }
        }.start()
    }

    fun loadFaviconFromDomain(windowId: String, pageUrl: String) {
        val uri = pageUrl.toUri()
        val host = uri.host ?: return
        val scheme = uri.scheme ?: "https"
        val faviconUrl = "$scheme://$host/favicon.ico"

        val targetNow = windows.firstOrNull { it.id == windowId }

        faviconCache[host]?.let { cached ->
            if (targetNow != null && targetNow.favicon !== cached) {
                updateWindow(windowId) { it.favicon = cached }
            }
            return
        }

        val currentHost = try { targetNow?.url?.toUri()?.host } catch (_: Exception) { null }
        if (targetNow?.favicon != null && currentHost == host) return

        loadFaviconFromExplicitUrl(windowId, faviconUrl, host)
    }

    fun performSniff(windowId: String) {
        val wv = webViewMap[windowId] ?: return
        isSniffing = true
        wv.evaluateJavascript(JS_SNIFF_RESOURCES) { raw ->
            val parsed = parseSniffedResources(raw)
            sniffedResources.clear()
            sniffedResources.addAll(parsed)
            isSniffing = false
            showSniffSheet = true
            val totalCount = parsed.size + networkSniffedResources.size
            if (totalCount == 0) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "未发现可嗅探的资源",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

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

    DisposableEffect(Unit) {
        val externalListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "bookmarks_v2" -> {
                    loadBookmarks()
                    bookmarkFolderPath.clear()
                }
                "history" -> {
                    loadHistory()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(externalListener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(externalListener)
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
        loadBookmarks()
        loadSearchHistory()
        loadSavedZooms()
    }

    LaunchedEffect(windows.toList()) {
        if (SessionSnapshotStore.userExiting) return@LaunchedEffect

        val snapshotUrls = windows
            .map { it.url }
            .filter { it.isNotBlank() && it != "about:blank" }
            .filter { url ->
                val startupCustomUrl = prefs.getString("home_startup_custom_url", "") ?: ""
                val newtabCustomUrl = prefs.getString("home_newtab_custom_url", "") ?: ""
                url != startupCustomUrl && url != newtabCustomUrl
            }

        if (snapshotUrls.isNotEmpty()) {
            try {
                val json = JSONArray().apply { snapshotUrls.forEach { put(it) } }
                prefs.edit().putString("session_snapshot", json.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    val pendingRestore = SessionSnapshotStore.pendingRestore
    LaunchedEffect(pendingRestore) {
        val urls = pendingRestore ?: return@LaunchedEffect
        SessionSnapshotStore.pendingRestore = null

        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        delay(200)

        val result = snackbarHostState.showSnackbar(
            message = "上次打开过 ${urls.size} 个页面",
            actionLabel = "打开",
            duration = SnackbarDuration.Long,
        )

        if (result == SnackbarResult.ActionPerformed) {
            urls.forEach { url ->
                try { createWindow(url) } catch (_: Exception) {}
            }
        }
    }

    val pendingUpdate = PendingUpdateStore.pending
    LaunchedEffect(pendingUpdate) {
        val info = pendingUpdate ?: return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = "发现新版本 ${info.version}",
            actionLabel = "查看",
            duration = SnackbarDuration.Long,
        )

        PendingUpdateStore.pending = null

        if (result == SnackbarResult.ActionPerformed) {
            pendingDialogInfo = info
            showPendingDialog = true
        }
    }

    val pendingScan = PendingScanResultStore.pending
    LaunchedEffect(pendingScan) {
        val raw = pendingScan ?: return@LaunchedEffect
        PendingScanResultStore.pending = null
        val text = raw.trim()
        if (text.isNotBlank()) {
            loadUrlInActive(text)
        }
    }

    val pendingInitialUrl = InitialLaunchUrlStore.pending
    LaunchedEffect(pendingInitialUrl) {
        val url = pendingInitialUrl ?: return@LaunchedEffect
        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        val id = activeWindowId
        if (id != null) {
            delay(100)
            try { navigateToUrl(id, url) } catch (_: Exception) {}
        }
        InitialLaunchUrlStore.pending = null
    }

    val pendingOpenNew = PendingOpenInNewWindowStore.pending
    LaunchedEffect(pendingOpenNew) {
        val url = pendingOpenNew ?: return@LaunchedEffect
        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        if (activeWindowId != null) {
            delay(100)
            try { createWindow(url) } catch (_: Exception) {}
        }
        PendingOpenInNewWindowStore.pending = null
    }

    LaunchedEffect(activeWindowId) {
        activeWindow?.let { win ->
            if (!isSearchFocused) {
                val query = searchQueryHolder.value
                urlInput = when {
                    query.isNotBlank() && isSearchResultPage -> TextFieldValue(query)
                    isHomeUrl(win.url) || win.url.isBlank() -> TextFieldValue("")
                    else -> TextFieldValue(win.url)
                }
            }
        }
    }

    LaunchedEffect(activeWindowId) {
        val id = activeWindowId ?: return@LaunchedEffect
        while (true) {
            delay(2000L)
            val wv = webViewMap[id] ?: continue
            try {
                wv.evaluateJavascript(JS_SNIFF_RESOURCES) { raw ->
                    val parsed = parseSniffedResources(raw)
                    val existingUrls = sniffedResources.map { it.url }.toSet()
                    val newUrls = parsed.map { it.url }.toSet()
                    if (existingUrls != newUrls) {
                        sniffedResources.clear()
                        sniffedResources.addAll(parsed)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isTop) {
        if (!isTop) {
            focusManager.clearFocus()
            isSearchFocused = false
            searchBarExpanded = false
        }
    }

    LaunchedEffect(findQuery, showFindBar, activeWindowId) {
        if (!showFindBar) return@LaunchedEffect
        val wv = activeWindowId?.let { webViewMap[it] } ?: return@LaunchedEffect

        delay(200)

        if (findQuery.isBlank()) {
            wv.clearMatches()
            findResultTotal = 0
            findResultCurrent = 0
        } else {
            wv.findAllAsync(findQuery)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            translateJob.value?.cancel()
            translateJob.value = null
            snackbarJob.value?.cancel()
            snackbarJob.value = null

            // ★ 需求 2：退出浏览器时清空无痕浏览数据
            val clearIncognito = try {
                prefs.getBoolean("web_clear_incognito_on_exit", true)
            } catch (_: Exception) { true }
            if (clearIncognito) {
                val hasIncognito = windows.any { it.incognito }
                if (hasIncognito) {
                    try {
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                    } catch (_: Exception) {}
                    windows.filter { it.incognito }.forEach { win ->
                        webViewMap[win.id]?.let { wv ->
                            try {
                                wv.clearCache(true)
                                wv.clearHistory()
                                wv.clearFormData()
                            } catch (_: Exception) {}
                        }
                    }
                }
            }

            webViewMap.values.forEach { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.destroy()
            }
            webViewMap.clear()
        }
    }

    val showNewTabPage = activeWindow?.let { win ->
        win.url.isBlank() || win.url == "about:blank"
    } ?: run {
        val startupOption = prefs.getInt("home_startup_option", 0)
        when (startupOption) {
            0 -> true
            1 -> {
                val customUrl = prefs.getString("home_startup_custom_url", "") ?: ""
                customUrl.isBlank()
            }
            2 -> {
                val lastSession = prefs.getString("last_session_url", "") ?: ""
                lastSession.isBlank()
            }
            else -> true
        }
    }

    BackHandler(
        enabled = isTop && windows.size > 1 &&
                activeWindow?.canGoBack != true &&
                isAtHomePage() &&
                !showFindBar &&
                !showToolSheet && !showWindowSheet && !showHistorySheet &&
                !showBookmarkSheet && !showSniffSheet &&
                !showAddBookmarkDialog && !showClearBookmarkDialog && !showClearHistoryDialog &&
                !showClearSearchHistoryDialog && !showAddFolderDialog && !showDeleteBookmarkDialog
    ) {
        activeWindowId?.let { id -> closeWindow(id) }
    }

    BackHandler(
        enabled = isTop &&
                activeWindow?.canGoBack != true &&
                !isAtHomePage() &&
                !showFindBar &&
                !showToolSheet && !showWindowSheet && !showHistorySheet &&
                !showBookmarkSheet && !showSniffSheet &&
                !showAddBookmarkDialog && !showClearBookmarkDialog && !showClearHistoryDialog &&
                !showClearSearchHistoryDialog && !showAddFolderDialog && !showDeleteBookmarkDialog
    ) {
        goToHomePage()
    }

    BackHandler(
        enabled = isTop && (activeWindow?.canGoBack == true) &&
                !showToolSheet && !showWindowSheet && !showHistorySheet &&
                !showBookmarkSheet && !showSniffSheet &&
                !showAddBookmarkDialog && !showClearBookmarkDialog && !showClearHistoryDialog &&
                !showClearSearchHistoryDialog && !showAddFolderDialog && !showDeleteBookmarkDialog
    ) {
        activeWindowId?.let { id -> webViewMap[id]?.goBack() }
    }

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

    BackHandler(enabled = isTop && showZoomBar) {
        showZoomBar = false
    }

    var historyMultiSelect by remember { mutableStateOf(false) }
    val historySelected = remember { mutableStateListOf<String>() }
    var bookmarkMultiSelect by remember { mutableStateOf(false) }
    val bookmarkSelected = remember { mutableStateListOf<String>() }

    BackHandler(enabled = isTop && historyMultiSelect) {
        historyMultiSelect = false
        historySelected.clear()
    }
    BackHandler(enabled = isTop && bookmarkMultiSelect) {
        bookmarkMultiSelect = false
        bookmarkSelected.clear()
    }

    var toolbarWallpaperIndex by remember {
        mutableIntStateOf(prefs.getInt("home_background_content", 0))
    }
    var toolbarCustomColor by remember {
        mutableLongStateOf(prefs.getLong("home_custom_wallpaper_color", 0L))
    }

    DisposableEffect(Unit) {
        val wallpaperListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "home_background_content" -> {
                    toolbarWallpaperIndex = prefs.getInt("home_background_content", 0)
                }
                "home_custom_wallpaper_color" -> {
                    toolbarCustomColor = prefs.getLong("home_custom_wallpaper_color", 0L)
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(wallpaperListener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(wallpaperListener)
        }
    }

    val themeIsDarkForToolbar = surfaceColor.luminance() < 0.5f
    val toolbarColor = if (showNewTabPage) {
        Color.Transparent
    } else {
        surfaceColor
    }

    val bottomBarIconTint: Color = if (showNewTabPage) {
        val mode = controlColorMode
        val bgIndex = toolbarWallpaperIndex
        val wallpaperIsLight = when (bgIndex) {
            0 -> !themeIsDarkForToolbar
            1 -> {
                val colorLong = toolbarCustomColor
                if (colorLong != 0L) Color(colorLong.toInt()).luminance() > 0.5f
                else !themeIsDarkForToolbar
            }
            4 -> false
            else -> true
        }
        when (mode) {
            0 -> MiuixTheme.colorScheme.onSurface
            1 -> if (wallpaperIsLight) Color(0xFF000000) else Color(0xFFFFFFFF)
            2 -> Color(0xFF000000)
            3 -> Color(0xFFFFFFFF)
            else -> if (wallpaperIsLight) Color(0xFF000000) else Color(0xFFFFFFFF)
        }
    } else {
        MiuixTheme.colorScheme.onSurface
    }

    val hasVideoResource =
        networkSniffedResources.any { it.category == SniffCategory.VIDEO } ||
                sniffedResources.any { it.category == SniffCategory.VIDEO }
    val effectiveDynamicTool: String? = when {
        dynamicToolSuggestEnabled && hasActiveDownloads -> "downloads"
        dynamicToolSuggestEnabled && hasVideoResource -> "play_in_player"
        dynamicToolSuggestEnabled && isEnglishPage -> "translate"
        else -> when (dynamicToolIndex) {
            1 -> "home"
            2 -> "refresh"
            3 -> "fullscreen"
            4 -> "add_bookmark"
            5 -> "translate"
            6 -> "sniff"
            7 -> "dev_tools"
            else -> null
        }
    }
    fun runDynamicToolAction(toolId: String) {
        val id = activeWindowId
        when (toolId) {
            "home" -> {
                clearSearchFocus()
                goToHomePage()
            }
            "refresh" -> id?.let { webViewMap[it]?.reload() }
            "fullscreen" -> {
                val newVal = !isFullscreen
                isFullscreen = newVal
                prefs.edit().putBoolean("web_fullscreen", newVal).apply()
            }
            "add_bookmark" -> {
                val win = activeWindow
                if (win == null || win.url.isBlank() || win.url == "about:blank") {
                    Toast.makeText(
                        context,
                        "无法将新标签页加入书签",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    addFromBookmarkSheet = false
                    scope.launch {
                        delay(280)
                        openAddBookmarkDialog(prefillCurrent = true)
                    }
                }
            }
            "translate" -> {
                if (id != null) {
                    val newState = activeWindow?.translated != true
                    updateWindow(id) { it.translated = newState }
                    setPageTranslated(newState)
                }
            }
            "sniff" -> {
                if (id != null) {
                    scope.launch {
                        delay(280)
                        performSniff(id)
                    }
                }
            }
            "play_in_player" -> {
                val firstVideo = networkSniffedResources.firstOrNull {
                    it.category == SniffCategory.VIDEO
                } ?: sniffedResources.firstOrNull {
                    it.category == SniffCategory.VIDEO
                }
                if (firstVideo != null) {
                    clearSearchFocus()
                    try {
                        NativePlayerActivity.start(
                            context = context,
                            url = firstVideo.url,
                            title = activeWindow?.title?.ifBlank { "视频播放" } ?: "视频播放",
                            fromWeb = true,
                            startPositionMs = 0L,
                            referer = activeWindow?.url ?: "",
                        )
                    } catch (_: Exception) {}
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "未嗅探到视频资源，请稍后再试",
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            }
            "downloads" -> {
                clearSearchFocus()
                onOpenDownloads()
            }
            "dev_tools" -> { }
        }
    }

    val swipeTriggerPx = with(LocalDensity.current) { 72.dp.toPx() }
    val swipePanelWidthPx = with(LocalDensity.current) { 80.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            AnimatedVisibility(
                visible = !isFullscreen && htmlFullscreenView == null && !showNewTabPage,
                enter = expandVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    expandFrom = Alignment.Top,
                ) + fadeIn(tween(FULLSCREEN_ANIM_MS)) + scaleIn(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(tween(FULLSCREEN_ANIM_MS)) + scaleOut(
                    animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                    targetScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
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
                                                    if (urlInput.text.isBlank() &&
                                                        !isHomeUrl(win.url)
                                                    ) {
                                                        urlInput = TextFieldValue(win.url)
                                                    }
                                                }
                                            }
                                        },
                                    label = "请输入关键词或网址"
                                )

                                AnimatedVisibility(
                                    visible = scanModuleEnabled && !isSearchFocused,
                                    enter = expandHorizontally(
                                        animationSpec = tween(durationMillis = 220),
                                        expandFrom = Alignment.Start,
                                    ) + fadeIn(tween(durationMillis = 160)),
                                    exit = shrinkHorizontally(
                                        animationSpec = tween(durationMillis = 220),
                                        shrinkTowards = Alignment.Start,
                                    ) + fadeOut(tween(durationMillis = 160)),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Spacer(Modifier.width(4.dp))
                                        MiuixCircleIconButton(
                                            onClick = {
                                                clearSearchFocus()
                                                onOpenScanner()
                                            },
                                            size = 32.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Scan,
                                                contentDescription = "扫一扫",
                                                tint = MiuixTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(25.dp),
                                            )
                                        }
                                    }
                                }
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
                                        focusManager.clearFocus()
                                        isSearchFocused = false
                                        forceHideHistory = true
                                        activeWindow?.let { win ->
                                            val query = searchQueryHolder.value
                                            urlInput = when {
                                                query.isNotBlank() && isSearchResultPage ->
                                                    TextFieldValue(query)

                                                isHomeUrl(win.url) ->
                                                    TextFieldValue("")

                                                else -> TextFieldValue(win.url)
                                            }
                                        }
                                        scope.launch {
                                            delay(200)
                                            searchBarExpanded = false
                                            forceHideHistory = false
                                        }
                                    },
                                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.primary,
                            )
                        }
                    ) {
                        AnimatedVisibility(
                            visible = !forceHideHistory &&
                                    historyModuleEnabled &&
                                    searchHistory.isNotEmpty() &&
                                    urlInput.text.isBlank() &&
                                    activeWindow?.incognito != true,
                            enter = expandVertically(
                                animationSpec = tween(220),
                                expandFrom = Alignment.Top,
                            ) + fadeIn(tween(160)),
                            exit = shrinkVertically(
                                animationSpec = tween(200),
                                shrinkTowards = Alignment.Top,
                            ) + fadeOut(tween(120)),
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp)
                                    .animateContentSize(),
                            ) {
                                item(key = "search_history_header") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "搜索历史",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = "清空",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.clickable(
                                                interactionSource = null,
                                                indication = null,
                                            ) {
                                                searchBarExpanded = false
                                                clearSearchFocus()
                                                scope.launch {
                                                    delay(220)
                                                    showClearSearchHistoryDialog = true
                                                }
                                            },
                                        )
                                    }
                                }
                                items(
                                    count = searchHistory.size,
                                    key = { idx -> "search_his_${searchHistory[idx]}" },
                                ) { idx ->
                                    val item = searchHistory[idx]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem(
                                                fadeOutSpec = null,
                                            )
                                            .clickable {
                                                urlInput = TextFieldValue(item)
                                                loadUrlInActive(item)
                                                clearSearchFocus()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Recent,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = item,
                                            fontSize = 14.sp,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = null,
                                                    indication = null,
                                                ) {
                                                    if (idx in searchHistory.indices) {
                                                        searchHistory.removeAt(idx)
                                                        saveSearchHistory()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = "删除",
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val currentEngineIndex = prefs.getInt("default_search_engine", 0)
                        .coerceIn(0, searchEngines.lastIndex)
                    AnimatedVisibility(
                        visible = isSearchResultPage && engineSwitcherBarEnabled,
                        enter = expandVertically(
                            animationSpec = tween(200),
                            expandFrom = Alignment.Top,
                        ) + fadeIn(tween(160)),
                        exit = shrinkVertically(
                            animationSpec = tween(180),
                            shrinkTowards = Alignment.Top,
                        ) + fadeOut(tween(120)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MiuixTheme.colorScheme.surface)
                                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 0.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            searchEngines.forEachIndexed { index, (name, _) ->
                                val selected = index == currentEngineIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (selected) MiuixTheme.colorScheme.primary
                                            else buttonBg
                                        )
                                        .clickable {
                                            if (!selected) {
                                                switchEngineAndSearch(index)
                                            }
                                        }
                                        .padding(horizontal = 18.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        color = if (selected) Color.White
                                        else MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            val density = LocalDensity.current
            val pullTriggerPx = with(density) { 74.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(swipeNavEnabled, activeWindowId) {
                        if (!swipeNavEnabled) return@pointerInput
                        val wv = activeWindowId?.let { webViewMap[it] }
                        val guardWindowId = activeWindowId
                        awaitPointerEventScope {
                            while (true) {
                                val downEv = awaitPointerEvent(PointerEventPass.Initial)
                                val downCh = downEv.changes.firstOrNull { it.changedToDown() }
                                    ?: continue
                                val startX = downCh.position.x
                                val startY = downCh.position.y
                                val w = size.width.toFloat()
                                if (w <= 0f) continue

                                val canBack = wv?.canGoBack() == true || !isAtHomePage()
                                val canForward = wv?.canGoForward() == true

                                var direction = 0
                                var consumed = false
                                var guardChecked = false
                                var guardAllowed = true

                                while (true) {
                                    val ev = awaitPointerEvent(PointerEventPass.Initial)
                                    val ch = ev.changes.firstOrNull { it.id == downCh.id } ?: break
                                    if (!ch.pressed) break
                                    val dx = ch.position.x - startX
                                    val dy = ch.position.y - startY

                                    if (direction == 0 &&
                                        (kotlin.math.abs(dx) > 14f || kotlin.math.abs(dy) > 14f)
                                    ) {
                                        if (!guardChecked) {
                                            guardChecked = true
                                            guardAllowed = if (guardWindowId != null) {
                                                SwipeNavStateHolder.get(guardWindowId)
                                            } else {
                                                true
                                            }
                                        }
                                        if (!guardAllowed) break

                                        if (kotlin.math.abs(dx) >= kotlin.math.abs(dy) * 1.5f) {
                                            direction = when {
                                                dx > 0 && canBack -> -1
                                                dx < 0 && canForward -> 1
                                                else -> 0
                                            }
                                            if (direction == 0) break
                                            isDraggingSwipe = true
                                            swipeEdgeDirection = direction
                                        } else {
                                            break
                                        }
                                    }

                                    if (direction != 0) {
                                        ch.consume()
                                        consumed = true
                                        swipeEdgeOffset = kotlin.math.abs(dx)
                                    }
                                }

                                isDraggingSwipe = false
                                val didTrigger = consumed && direction != 0 &&
                                        swipeEdgeOffset >= swipeTriggerPx
                                if (didTrigger) {
                                    if (direction == -1) {
                                        if (wv?.canGoBack() == true) {
                                            wv.goBack()
                                        } else {
                                            goToHomePage()
                                        }
                                    }
                                    if (direction == 1) wv?.goForward()
                                }
                                swipeEdgeOffset = 0f
                            }
                        }
                    }
                    .pointerInput(pullRefreshEnabled, activeWindowId) {
                        if (!pullRefreshEnabled) return@pointerInput
                        val wv = activeWindowId?.let { webViewMap[it] }
                        val guardWindowId = activeWindowId
                        awaitPointerEventScope {
                            while (true) {
                                val downEv = awaitPointerEvent(PointerEventPass.Initial)
                                val downCh = downEv.changes.firstOrNull { it.changedToDown() }
                                    ?: continue
                                val startX = downCh.position.x
                                val startY = downCh.position.y
                                var pulled = false
                                var maxPull = 0f

                                while (true) {
                                    val ev = awaitPointerEvent(PointerEventPass.Initial)
                                    val ch = ev.changes.firstOrNull { it.id == downCh.id } ?: break
                                    if (!ch.pressed) break
                                    val dx = ch.position.x - startX
                                    val dy = ch.position.y - startY

                                    if (!pulled && dy > 5f &&
                                        kotlin.math.abs(dy) > kotlin.math.abs(dx) * 1.2f
                                    ) {
                                        val jsAllowed = if (guardWindowId != null) {
                                            PullRefreshStateHolder.get(guardWindowId)
                                        } else {
                                            true
                                        }
                                        if (!jsAllowed) break

                                        val atTop = try {
                                            wv == null || !wv.canScrollVertically(-1)
                                        } catch (_: Exception) { false }
                                        if (atTop) {
                                            pulled = true
                                            isDraggingPull = true
                                        } else {
                                            break
                                        }
                                    }
                                    if (pulled) {
                                        maxPull = dy.coerceAtLeast(0f)
                                        pullDistance = maxPull
                                        pullOffsetTarget = maxPull * 0.5f
                                        ch.consume()
                                    }
                                }

                                if (pulled) {
                                    isDraggingPull = false
                                    pullOffsetTarget = 0f
                                    if (maxPull * 0.5f >= pullTriggerPx && !isRefreshing) {
                                        isRefreshing = true
                                    }
                                    pullDistance = 0f
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.changedToDown() }) {
                                    clearSearchFocus()
                                }
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MiuixTheme.colorScheme.surface),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (pullDistance > 0f || isRefreshing) {
                        val indicatorProgress = if (isRefreshing) 1f
                        else (animatedPullOffset / pullTriggerPx).coerceIn(0f, 1f)
                        val indicatorScale = 1f + indicatorProgress * 0.6f
                        val indicatorColor = if (indicatorProgress >= 1f) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            Color(0xFF666666)
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 28.dp)
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = indicatorScale
                                    scaleY = indicatorScale
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            InfiniteProgressIndicator(
                                color = indicatorColor,
                                size = 18.dp,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }

                val webViewNavBarPad = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
                    .takeIf { it > 0.dp }
                    ?: with(density) { InitialInsetsFallback.navBarBottomPx.toDp() }

                val webViewBottomPadding by animateDpAsState(
                    targetValue = when {
                        isFullscreen || htmlFullscreenView != null -> 0.dp
                        showNewTabPage -> 0.dp
                        else -> webViewNavBarPad + BOTTOM_BAR_HEIGHT
                    },
                    animationSpec = tween(
                        durationMillis = FULLSCREEN_ANIM_MS,
                        easing = FULLSCREEN_EASING,
                    ),
                    label = "webViewBottomPadding",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = webViewBottomPadding)
                        .graphicsLayer { translationY = animatedPullOffset }
                ) {
                    WebViewLayer(
                        context = context,
                        activeWindowId = activeWindowId,
                        windows = windows,
                        webViewMap = webViewMap,
                        textZoomValue = textZoomValue,
                        forceZoomEnabled = forceZoomEnabled,
                        defaultZoomPercent = defaultZoomPercent,
                        zoomRestoreEnabledRef = zoomRestoreEnabledRef,
                        savedZoomsRef = savedZoomsRef,
                        savedZooms = savedZooms,
                        sniffedResources = sniffedResources,
                        networkSniffedResources = networkSniffedResources,
                        faviconCache = faviconCache,
                        searchEngines = searchEngines,
                        searchQueryHolder = searchQueryHolder,
                        prefs = prefs,
                        useNativePlayerEnabledRef = useNativePlayerEnabledRef,
                        isHomeUrl = { u -> isHomeUrl(u) },
                        onUpdateWindow = { wid, block -> updateWindow(wid, block) },
                        onRecordHistory = { u, t -> recordHistory(u, t) },
                        onAddToSearchHistory = { q -> addToSearchHistory(q) },
                        onIsSearchResultPage = { b -> isSearchResultPage = b },
                        onUrlInput = { tv -> urlInput = tv },
                        isSearchFocused = { isSearchFocused },
                        onZoomForHost = { h -> computeZoomForHost(h) },
                        onZoomFactor = { f -> zoomFactor = f },
                        onPersistSavedZooms = { persistSavedZooms() },
                        onApplyCssZoom = { wv, f -> applyCssZoomToWebView(wv, f) },
                        onFaviconLoad = { wid, url -> loadFaviconFromDomain(wid, url) },
                        onFaviconExplicit = { wid, furl, host -> loadFaviconFromExplicitUrl(wid, furl, host) },
                        onPendingDownload = { dl -> pendingDownload = dl; showDownloadDialog = true },
                        onFindResult = { total, current ->
                            findResultTotal = total; findResultCurrent = current + 1
                        },
                        onSniffResult = { res ->
                            if (networkSniffedResources.none { it.url == res.url }) {
                                if (networkSniffedResources.size >= MAX_NETWORK_SNIFF) {
                                    networkSniffedResources.removeAt(0)
                                }
                                networkSniffedResources.add(res)
                            }
                            if (res.category == SniffCategory.VIDEO && res.fromNetwork) {
                                activeWindowId?.let { RecentVideoUrlStore.set(it, res.url) }
                            }
                        },
                        onShowHtmlFullscreen = { v, cb ->
                            val activity = context as? Activity
                            if (v == null || activity == null) {
                                cb?.onCustomViewHidden()
                            } else if (htmlFullscreenView != null) {
                                cb?.onCustomViewHidden()
                            } else {
                                htmlFullscreenView = v
                                htmlFullscreenCallback = cb
                                v.setBackgroundColor(android.graphics.Color.BLACK)
                                val decor = activity.window.decorView as ViewGroup
                                decor.addView(v, ViewGroup.LayoutParams(-1, -1))
                                val controller = WindowCompat.getInsetsController(
                                    activity.window, activity.window.decorView
                                )
                                controller.hide(WindowInsetsCompat.Type.systemBars())
                                controller.systemBarsBehavior =
                                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        },
                        onHideHtmlFullscreen = {
                            htmlFullscreenView?.let { v -> (v.parent as? ViewGroup)?.removeView(v) }
                            htmlFullscreenView = null
                            htmlFullscreenCallback?.onCustomViewHidden()
                            htmlFullscreenCallback = null
                        },
                        onEnglishPageDetected = { isEn -> isEnglishPage = isEn },
                        fileChooserCallback = fileChooserCallback,
                        fileChooserLauncher = fileChooserLauncher,
                        pendingPermission = pendingPermission,
                        permissionLauncher = permissionLauncher,
                    )
                }

                if (currentProgress in 1..99 || smoothProgress < 0.999f) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .zIndex(10f)
                    ) {
                        val fullWidth = maxWidth
                        LinearProgressIndicator(
                            progress = smoothProgress,
                            modifier = Modifier
                                .requiredWidth(fullWidth + 24.dp)
                                .offset(x = (-12).dp)
                        )
                    }
                }
            }
        }

        val floatDensity = LocalDensity.current
        val navBarBottomPaddingReal = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        val navBarBottomPaddingFallback = with(floatDensity) {
            InitialInsetsFallback.navBarBottomPx.toDp()
        }
        val navBarBottomPadding = if (navBarBottomPaddingReal > 0.dp) {
            navBarBottomPaddingReal
        } else {
            navBarBottomPaddingFallback
        }
        val bottomBarHeight = if (isFullscreen || htmlFullscreenView != null) 0.dp
        else BOTTOM_BAR_HEIGHT
        val imeBottomPadding = WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding()
        val floatingBottomInset = maxOf(
            imeBottomPadding,
            navBarBottomPadding + bottomBarHeight,
        )

        SnackbarHost(
            state = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarBottomPadding + bottomBarHeight)
                .imePadding(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = floatingBottomInset),
        ) {
            FindInPageBar(
                show = showFindBar && !isFullscreen && htmlFullscreenView == null,
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
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = floatingBottomInset),
        ) {
            ZoomBar(
                show = showZoomBar && !isFullscreen && htmlFullscreenView == null,
                percent = zoomFactor,
                onZoomIn = {
                    val newFactor = (zoomFactor + 0.25f).coerceAtMost(3.0f)
                    if (newFactor != zoomFactor) {
                        applyZoomToActiveWebView(newFactor, record = true)
                    }
                },
                onZoomOut = {
                    val newFactor = (zoomFactor - 0.25f).coerceAtLeast(0.25f)
                    if (newFactor != zoomFactor) {
                        applyZoomToActiveWebView(newFactor, record = true)
                    }
                },
                onReset = {
                    applyZoomToActiveWebView(1.0f, record = true)
                },
                onClose = { showZoomBar = false },
            )
        }

        val dialogInfo = pendingDialogInfo
        if (dialogInfo != null) {
            val dialogNavOwner = rememberNavigationEventDispatcherOwner(parent = null)
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides dialogNavOwner
            ) {
                UpdateDialog(
                    show = showPendingDialog,
                    info = dialogInfo,
                    currentVersion = currentVersionName,
                    isDownloading = isPendingDownloading,
                    onDismiss = {
                        if (!isPendingDownloading) {
                            showPendingDialog = false
                            pendingDialogInfo = null
                        }
                    },
                    onDownload = {
                        if (!isPendingDownloading) {
                            isPendingDownloading = true
                            showPendingDialog = false
                            val url = dialogInfo.url
                            scope.launch {
                                delay(250)
                                startApkDownload(
                                    context = context,
                                    downloadUrl = url,
                                    onComplete = {
                                        isPendingDownloading = false
                                        pendingDialogInfo = null
                                    },
                                    onFailed = { msg ->
                                        isPendingDownloading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = msg,
                                                duration = SnackbarDuration.Short,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = floatingBottomInset),
        ) {
            AnimatedVisibility(
                visible = isSearchFocused && !isFullscreen && htmlFullscreenView == null && quickInputBarEnabled,
                enter = expandVertically(
                    animationSpec = tween(200),
                    expandFrom = Alignment.Bottom,
                ) + fadeIn(tween(160)),
                exit = shrinkVertically(
                    animationSpec = tween(180),
                    shrinkTowards = Alignment.Bottom,
                ) + fadeOut(tween(120)),
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
                                .background(buttonBg)
                                .clickable { insertText(text) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showNewTabPage,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                ),
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                ),
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            NewTabPage(
                prefs = prefs,
                searchEngines = searchEngines,
                searchHistory = searchHistory,
                isTop = isTop,
                historyModuleEnabled = historyModuleEnabled,
                onClearSearchHistory = {
                    searchHistory.clear()
                    saveSearchHistory()
                },
                onDeleteSearchHistoryItem = { item ->
                    searchHistory.remove(item)
                    saveSearchHistory()
                },
                onNavigate = { input -> loadUrlInActive(input) },
                onOpenScanner = onOpenScanner,
                windowCount = windows.size,
                onSwipeWindow = { direction ->
                    val currentIdx = windows.indexOfFirst { it.id == activeWindowId }
                    if (currentIdx >= 0) {
                        val targetIdx = currentIdx + direction
                        if (targetIdx in windows.indices) {
                            switchToWindow(windows[targetIdx].id)
                        }
                    }
                },
                onSwipeNav = { direction ->
                    val id = activeWindowId
                    if (id != null) {
                        val wv = webViewMap[id]
                        if (direction < 0) {
                            if (wv?.canGoBack() == true) wv.goBack()
                        } else if (direction > 0) {
                            if (wv?.canGoForward() == true) wv.goForward()
                        }
                    }
                },
                onSwipeVisual = { direction, offsetPx, dragging ->
                    if (direction != 0) {
                        swipeEdgeDirection = direction
                    }
                    swipeEdgeOffset = offsetPx
                    isDraggingSwipe = dragging
                },
                canSwipeBack = activeWindow?.canGoBack == true,
                canSwipeForward = activeWindow?.canGoForward == true,
            )
        }

        LaunchedEffect(animatedSwipeOffset) {
            if (animatedSwipeOffset < 0.5f && swipeEdgeDirection != 0) {
                swipeEdgeDirection = 0
            }
        }

        if (animatedSwipeOffset > 1f && swipeEdgeDirection != 0) {
            val progress = (animatedSwipeOffset / swipeTriggerPx).coerceIn(0f, 1f)
            val iconScale = 1f + progress * 0.6f
            val iconColor = Color.White

            val shape = if (swipeEdgeDirection == -1) {
                RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
            } else {
                RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(
                        if (swipeEdgeDirection == -1) Alignment.CenterStart
                        else Alignment.CenterEnd
                    )
                    .offset {
                        val inScreenPx = (swipePanelWidthPx * progress).roundToInt()
                        val offScreenPx = swipePanelWidthPx.roundToInt() - inScreenPx
                        if (swipeEdgeDirection == -1) {
                            IntOffset(-offScreenPx, 0)
                        } else {
                            IntOffset(offScreenPx, 0)
                        }
                    }
                    .width(40.dp)
                    .padding(vertical = 360.dp)
                    .clip(shape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (swipeEdgeDirection == -1) {
                        MiuixIcons.ChevronBackward
                    } else {
                        MiuixIcons.ChevronForward
                    },
                    contentDescription = if (swipeEdgeDirection == -1) "后退" else "前进",
                    tint = iconColor,
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                )
            }
        }

        val bottomBarDensity = LocalDensity.current
        val navBarBottomPaddingReal2 = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        val navBarBottomPaddingFallback2 = with(bottomBarDensity) {
            InitialInsetsFallback.navBarBottomPx.toDp()
        }
        val navBarBottomPadding2 = if (navBarBottomPaddingReal2 > 0.dp) {
            navBarBottomPaddingReal2
        } else {
            navBarBottomPaddingFallback2
        }

        val bottomBarItems = buildList {
            val onSurface = bottomBarIconTint
            val canGoBack = activeWindow?.canGoBack == true || !isAtHomePage()
            val canGoForward = activeWindow?.canGoForward == true
            val dynamicTool = effectiveDynamicTool?.let { tid ->
                defaultTools().find { it.id == tid }
            }

            add(
                BrowserBottomBarItem(
                    id = "back",
                    visible = true,
                    content = { _ ->
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "后退",
                            tint = if (canGoBack) onSurface
                            else onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            )
            add(
                BrowserBottomBarItem(
                    id = "forward",
                    visible = true,
                    content = { _ ->
                        Icon(
                            imageVector = MiuixIcons.ChevronForward,
                            contentDescription = "前进",
                            tint = if (canGoForward) onSurface
                            else onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            )
            add(
                BrowserBottomBarItem(
                    id = "tools",
                    visible = true,
                    content = { _ ->
                        Icon(
                            imageVector = MiuixIcons.GridView,
                            contentDescription = "工具栏",
                            tint = onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            )
            add(
                BrowserBottomBarItem(
                    id = "windows",
                    visible = true,
                    content = { _ ->
                        WindowManagerIcon(
                            count = windows.size,
                            tint = onSurface,
                        )
                    },
                )
            )
            add(
                BrowserBottomBarItem(
                    id = "dynamic",
                    visible = dynamicTool != null,
                    content = { _ ->
                        AnimatedContent(
                            targetState = dynamicTool,
                            transitionSpec = {
                                val ease = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
                                val enterAlpha = tween<Float>(durationMillis = 200, easing = ease)
                                val enterScale = tween<Float>(durationMillis = 240, easing = ease)
                                val exitAlpha = tween<Float>(durationMillis = 160, easing = ease)
                                val exitScale = tween<Float>(durationMillis = 200, easing = ease)
                                (
                                        fadeIn(animationSpec = enterAlpha) +
                                                scaleIn(
                                                    animationSpec = enterScale,
                                                    initialScale = 0.4f,
                                                )
                                        ) togetherWith (
                                        fadeOut(animationSpec = exitAlpha) +
                                                scaleOut(
                                                    animationSpec = exitScale,
                                                    targetScale = 0.4f,
                                                )
                                        )
                            },
                            label = "dynamicToolIcon",
                        ) { tool ->
                            if (tool != null) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.title,
                                    tint = onSurface,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Spacer(Modifier.size(24.dp))
                            }
                        }
                    },
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            BrowserBottomBar(
                items = bottomBarItems,
                barVisible = !isFullscreen && htmlFullscreenView == null,
                onItemClick = { id ->
                    clearSearchFocus()
                    val windowId = activeWindowId
                    when (id) {
                        "back" -> {
                            val wv = windowId?.let { webViewMap[it] }
                            if (wv?.canGoBack() == true) {
                                wv.goBack()
                            } else if (!isAtHomePage()) {
                                goToHomePage()
                            }
                        }
                        "forward" -> windowId?.let { webViewMap[it]?.goForward() }
                        "tools" -> showToolSheet = true
                        "windows" -> showWindowSheet = true
                        "dynamic" -> effectiveDynamicTool?.let { runDynamicToolAction(it) }
                    }
                },
                navBarBottomPadding = navBarBottomPadding2,
                backgroundColor = toolbarColor,
            )
        }
    }

    BrowserToolSheet(
        show = showToolSheet,
        pageTitle = if (showNewTabPage) "新标签页"
        else (activeWindow?.title?.ifBlank { "无标题" } ?: "新标签页"),
        favicon = activeWindow?.favicon,
        isFullscreen = isFullscreen,
        isNightMode = activeWindow?.nightMode == true,
        isIncognito = activeWindow?.incognito == true,
        isDesktopMode = activeWindow?.desktopMode == true,
        isNoImage = activeWindow?.noImage == true,
        isTranslated = activeWindow?.translated == true,
        onOpenSettings = {
            showToolSheet = false
            onOpenSettings()
        },
        onDismiss = { showToolSheet = false },
        onToolAction = { toolId, isActive ->
            val id = activeWindowId
            when (toolId) {
                "home" -> {
                    goToHomePage()
                }
                "night_mode" -> {
                    if (id != null) {
                        val currentSetting = prefs.getInt("web_night_mode", 0)
                        if (currentSetting == 0) {
                            pendingNightModeTarget = isActive
                            showNightModeConfirmDialog = true
                        } else {
                            updateWindow(id) { it.nightMode = isActive }
                            webViewMap[id]?.evaluateJavascript(
                                if (isActive) JS_NIGHT_ON else JS_NIGHT_OFF, null
                            )
                            prefs.edit()
                                .putInt("web_night_mode", if (isActive) 2 else 1)
                                .apply()
                        }
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
                            val ua = if (isActive) DESKTOP_UA
                            else getMobileUserAgent(context)
                            wv.settings.userAgentString = ua
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
                    prefs.edit().putBoolean("web_fullscreen", isActive).apply()
                }
                "refresh" -> id?.let { webViewMap[it]?.reload() }
                "settings" -> onOpenSettings()
                "history" -> {
                    scope.launch {
                        delay(280)
                        historyMultiSelect = false
                        historySelected.clear()
                        showHistorySheet = true
                    }
                }
                "add_bookmark" -> {
                    val win = activeWindow
                    if (win == null || win.url.isBlank() || win.url == "about:blank") {
                        Toast.makeText(
                            context,
                            "无法将新标签页加入书签",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        addFromBookmarkSheet = false
                        scope.launch {
                            delay(280)
                            openAddBookmarkDialog(prefillCurrent = true)
                        }
                    }
                }
                "bookmarks" -> {
                    scope.launch {
                        delay(280)
                        bookmarkMultiSelect = false
                        bookmarkSelected.clear()
                        bookmarkFolderPath.clear()
                        showBookmarkSheet = true
                    }
                }
                "sniff" -> {
                    if (id != null) {
                        scope.launch {
                            delay(280)
                            performSniff(id)
                        }
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
                                userAgent = wv.settings.userAgentString ?: FALLBACK_MOBILE_UA,
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
                    clearSearchFocus()
                    showZoomBar = false
                    showFindBar = true
                }
                "zoom" -> {
                    clearSearchFocus()
                    scope.launch {
                        delay(280)
                        zoomFactor = 1.0f
                        showFindBar = false
                        showZoomBar = true
                    }
                }
                "dev_tools" -> { }
                "exit" -> {
                    SessionSnapshotStore.userExiting = true
                    prefs.edit().putString("session_snapshot", "").apply()
                    (context as? Activity)?.finish()
                }
                "downloads" -> onOpenDownloads()
                "tts" -> { }
            }
            showToolSheet = false
        },
    )

    val mergedSniffed = remember(
        sniffedResources.toList(),
        networkSniffedResources.toList(),
    ) {
        (networkSniffedResources + sniffedResources).distinctBy { it.url }
    }

    var showAllSniffResources by remember {
        mutableStateOf(
            try {
                prefs.getBoolean("web_sniff_show_all", false)
            } catch (_: Exception) { false }
        )
    }

    SniffSheet(
        show = showSniffSheet,
        isSniffing = isSniffing,
        resources = mergedSniffed,
        showAllResources = showAllSniffResources,
        onOpen = { res ->
            showSniffSheet = false
            if (res.category == SniffCategory.VIDEO) {
                try {
                    NativePlayerActivity.start(
                        context = context,
                        url = res.url,
                        title = res.filename.ifBlank { "视频播放" },
                        fromWeb = true,
                    )
                } catch (_: Exception) {
                    createWindow(res.url)
                }
            } else {
                val id = activeWindowId
                if (id != null) {
                    navigateToUrl(id, res.url)
                } else {
                    createWindow(res.url)
                }
            }
        },
        onDownload = { res ->
            pendingDownload = PendingDownload(
                url = res.url,
                userAgent = getMobileUserAgent(context),
                contentDisposition = null,
                mimetype = guessMimeFromUrl(res.url),
                fileName = res.filename,
            )
            showSniffSheet = false
            showDownloadDialog = true
        },
        onToggleShowAll = { enabled ->
            showAllSniffResources = enabled
            prefs.edit().putBoolean("web_sniff_show_all", enabled).apply()
        },
        onDismiss = { showSniffSheet = false },
    )

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

    HistorySheet(
        show = showHistorySheet,
        history = history,
        multiSelect = historyMultiSelect,
        selectedUrls = historySelected,
        onEnterMultiSelect = { url ->
            historyMultiSelect = true
            historySelected.clear()
            historySelected.add(url)
        },
        onToggleSelect = { url ->
            if (url in historySelected) historySelected.remove(url)
            else historySelected.add(url)
        },
        onCancelMultiSelect = {
            historyMultiSelect = false
            historySelected.clear()
        },
        onToggleSelectAll = {
            if (historySelected.size == history.size && history.isNotEmpty()) {
                historySelected.clear()
            } else {
                historySelected.clear()
                historySelected.addAll(history.map { it.url })
            }
        },
        onDeleteSelected = {
            val toRemove = historySelected.toSet()
            history.removeAll { it.url in toRemove }
            saveHistory()
            historyMultiSelect = false
            historySelected.clear()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "已删除 ${toRemove.size} 条历史",
                    duration = SnackbarDuration.Short,
                )
            }
        },
        onOpenUrl = { url ->
            val id = activeWindowId
            if (id != null) navigateToUrl(id, url)
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
            showHistorySheet = false
            scope.launch {
                delay(220)
                showClearHistoryDialog = true
            }
        },
        onDismiss = {
            showHistorySheet = false
            historyMultiSelect = false
            historySelected.clear()
        },
    )

    BookmarkSheet(
        show = showBookmarkSheet,
        bookmarks = bookmarks,
        folderPath = bookmarkFolderPath,
        currentItems = currentBookmarkLevel(),
        multiSelect = bookmarkMultiSelect,
        selectedIds = bookmarkSelected,
        onEnterMultiSelect = { id ->
            bookmarkMultiSelect = true
            bookmarkSelected.clear()
            bookmarkSelected.add(id)
        },
        onToggleSelect = { id ->
            if (id in bookmarkSelected) bookmarkSelected.remove(id)
            else bookmarkSelected.add(id)
        },
        onCancelMultiSelect = {
            bookmarkMultiSelect = false
            bookmarkSelected.clear()
        },
        onToggleSelectAll = {
            val items = currentBookmarkLevel()
            if (bookmarkSelected.size == items.size && items.isNotEmpty()) {
                bookmarkSelected.clear()
            } else {
                bookmarkSelected.clear()
                bookmarkSelected.addAll(items.map { it.id })
            }
        },
        onDeleteSelected = {
            val toRemove = bookmarkSelected.toSet()
            val items = currentBookmarkLevel()
            items.removeAll { it.id in toRemove }
            saveBookmarks()
            bookmarkMultiSelect = false
            bookmarkSelected.clear()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "已删除 ${toRemove.size} 项",
                    duration = SnackbarDuration.Short,
                )
            }
        },
        onAddBookmark = {
            addFromBookmarkSheet = true
            showBookmarkSheet = false
            bookmarkMultiSelect = false
            bookmarkSelected.clear()
            scope.launch {
                delay(280)
                openAddBookmarkDialog(prefillCurrent = false)
            }
        },
        onAddFolder = {
            addFromBookmarkSheet = true
            addFolderFromAddBookmark = false
            showBookmarkSheet = false
            bookmarkMultiSelect = false
            bookmarkSelected.clear()
            scope.launch {
                delay(280)
                openAddFolderDialog()
            }
        },
        onOpenUrl = { url ->
            val id = activeWindowId
            if (id != null) navigateToUrl(id, url)
            showBookmarkSheet = false
        },
        onEnterFolder = { folder ->
            bookmarkFolderPath.add(folder)
        },
        onNavigateToBreadcrumb = { index ->
            val targetSize = index + 1
            while (bookmarkFolderPath.size > targetSize) {
                bookmarkFolderPath.removeAt(bookmarkFolderPath.lastIndex)
            }
        },
        onDelete = { item ->
            openDeleteBookmarkDialog(item)
        },
        onClearAll = {
            showBookmarkSheet = false
            scope.launch {
                delay(220)
                showClearBookmarkDialog = true
            }
        },
        onDismiss = {
            showBookmarkSheet = false
            bookmarkMultiSelect = false
            bookmarkSelected.clear()
            bookmarkFolderPath.clear()
        },
    )

    WindowDialog(
        show = showClearSearchHistoryDialog,
        title = "清空搜索历史",
        onDismissRequest = { showClearSearchHistoryDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确定要清空全部搜索历史吗？此操作无法撤销。",
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
                    onClick = { showClearSearchHistoryDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        searchHistory.clear()
                        saveSearchHistory()
                        showClearSearchHistoryDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "搜索历史已清空",
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
                        bookmarkFolderPath.clear()
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

    WindowDialog(
        show = showAddBookmarkDialog,
        title = "添加书签",
        onDismissRequest = {
            showAddBookmarkDialog = false
            bookmarkTitleInput = ""
            bookmarkUrlInput = ""
            bookmarkTargetFolderId = null
            if (addFromBookmarkSheet) {
                addFromBookmarkSheet = false
                scope.launch {
                    delay(280)
                    showBookmarkSheet = true
                }
            }
        }
    ) {
        val allFolders = remember(bookmarks.toList(), showAddBookmarkDialog) {
            getAllBookmarkFolders()
        }

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .padding(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainer),
                    ) {
                        CompactRadioRow(
                            title = "根目录",
                            selected = bookmarkTargetFolderId == null,
                            onClick = { bookmarkTargetFolderId = null },
                        )
                    }

                    allFolders.forEach { (folder, source) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MiuixTheme.colorScheme.surfaceContainer),
                        ) {
                            CompactRadioRow(
                                title = folder.title,
                                selected = bookmarkTargetFolderId == folder.id,
                                onClick = { bookmarkTargetFolderId = folder.id },
                                trailing = if (source != null) {
                                    {
                                        Text(
                                            text = "来源于：$source",
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            maxLines = 1,
                                        )
                                    }
                                } else null,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .clickable {
                        addFolderFromAddBookmark = true
                        showAddBookmarkDialog = false
                        showAddFolderDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "创建文件夹",
                    fontSize = 15.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }

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
                        bookmarkTargetFolderId = null
                        if (addFromBookmarkSheet) {
                            addFromBookmarkSheet = false
                            scope.launch {
                                delay(280)
                                showBookmarkSheet = true
                            }
                        }
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
                        val targetItems = getTargetFolderItems()
                        val existing = targetItems.indexOfFirst { it.url == url }
                        if (existing >= 0) {
                            val old = targetItems[existing]
                            targetItems[existing] = old.copy(
                                title = title,
                                timestamp = System.currentTimeMillis(),
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "书签已更新",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        } else {
                            targetItems.add(
                                0,
                                BookmarkItem(
                                    title = title,
                                    url = url,
                                    timestamp = System.currentTimeMillis(),
                                )
                            )
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
                        bookmarkTargetFolderId = null
                        if (addFromBookmarkSheet) {
                            addFromBookmarkSheet = false
                            scope.launch {
                                delay(280)
                                showBookmarkSheet = true
                            }
                        }
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

    WindowDialog(
        show = showAddFolderDialog,
        title = "新建文件夹",
        onDismissRequest = {
            showAddFolderDialog = false
            folderTitleInput = ""
            if (addFolderFromAddBookmark) {
                addFolderFromAddBookmark = false
                showAddBookmarkDialog = true
            } else if (addFromBookmarkSheet) {
                addFromBookmarkSheet = false
                scope.launch {
                    delay(280)
                    showBookmarkSheet = true
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = folderTitleInput,
                onValueChange = { folderTitleInput = it },
                label = "文件夹名称",
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
                        showAddFolderDialog = false
                        folderTitleInput = ""
                        if (addFolderFromAddBookmark) {
                            addFolderFromAddBookmark = false
                            showAddBookmarkDialog = true
                        } else if (addFromBookmarkSheet) {
                            addFromBookmarkSheet = false
                            scope.launch {
                                delay(280)
                                showBookmarkSheet = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val name = folderTitleInput.trim()
                        if (name.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "请输入文件夹名称",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                            return@Button
                        }
                        val items = currentBookmarkLevel()
                        val newFolder = BookmarkItem(
                            title = name,
                            url = null,
                            isFolder = true,
                        )
                        items.add(0, newFolder)
                        saveBookmarks()
                        bookmarkTargetFolderId = newFolder.id
                        showAddFolderDialog = false
                        folderTitleInput = ""
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "文件夹已创建",
                                duration = SnackbarDuration.Short,
                            )
                            delay(220)
                            if (addFolderFromAddBookmark) {
                                addFolderFromAddBookmark = false
                                showAddBookmarkDialog = true
                            } else if (addFromBookmarkSheet) {
                                addFromBookmarkSheet = false
                                showBookmarkSheet = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("创建") }
            }
        }
    }

    WindowDialog(
        show = showDeleteBookmarkDialog,
        title = "删除确认",
        onDismissRequest = {
            showDeleteBookmarkDialog = false
            pendingDeleteBookmarkId = null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val item = pendingDeleteBookmarkId?.let { findBookmarkById(bookmarks, it) }
            Text(
                text = if (item?.isFolder == true) {
                    "确定要删除文件夹「${item.title}」吗？其中的所有内容也会被删除。"
                } else {
                    "确定要删除书签「${item?.title ?: ""}」吗？"
                },
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
                    onClick = {
                        showDeleteBookmarkDialog = false
                        pendingDeleteBookmarkId = null
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val id = pendingDeleteBookmarkId
                        if (id != null) {
                            removeBookmarkById(bookmarks, id)
                            saveBookmarks()
                        }
                        showDeleteBookmarkDialog = false
                        pendingDeleteBookmarkId = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "已删除",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("删除") }
            }
        }
    }

    if (pendingNightModeTarget != null) {
        WindowDialog(
            show = showNightModeConfirmDialog,
            title = "夜间模式设置",
            onDismissRequest = {
                showNightModeConfirmDialog = false
                pendingNightModeTarget = null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (pendingNightModeTarget == true) {
                        "主动切换为夜间模式后，将关闭跟随软件设置的功能"
                    } else {
                        "主动关闭夜间模式后，将关闭跟随软件设置的功能"
                    },
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
                        onClick = {
                            showNightModeConfirmDialog = false
                            pendingNightModeTarget = null
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            val target = pendingNightModeTarget
                            val id = activeWindowId
                            if (target != null && id != null) {
                                prefs.edit()
                                    .putInt("web_night_mode", if (target) 2 else 1)
                                    .apply()
                                updateWindow(id) { it.nightMode = target }
                                webViewMap[id]?.evaluateJavascript(
                                    if (target) JS_NIGHT_ON else JS_NIGHT_OFF, null
                                )
                            }
                            showNightModeConfirmDialog = false
                            pendingNightModeTarget = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("确定") }
                }
            }
        }
    }

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
    val sslError = PendingSslErrorStore.pending
    if (sslError != null) {
        WindowDialog(
            show = true,
            title = "安全警告",
            onDismissRequest = {
                sslError.cancel()
                PendingSslErrorStore.pending = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "${sslError.errorText}\n\n" +
                            "该网站的安全证书无法验证，继续访问可能存在风险。\n\n" +
                            "你可以选择“继续访问”暂时忽略此警告。",
                    modifier = Modifier.fillMaxWidth(),
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            sslError.cancel()
                            PendingSslErrorStore.pending = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("返回")
                    }
                    Button(
                        onClick = {
                            sslError.proceed()
                            PendingSslErrorStore.pending = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("继续访问")
                    }
                }
            }
        }
    }
}

// ============================================================
// WebView 容器
// ============================================================

@Suppress("UNUSED_PARAMETER")
@Composable
private fun WebViewLayer(
    context: Context,
    activeWindowId: String?,
    windows: MutableList<BrowserWindow>,
    webViewMap: MutableMap<String, WebView>,
    textZoomValue: Int,
    forceZoomEnabled: Boolean,
    defaultZoomPercent: Int,
    zoomRestoreEnabledRef: MutableState<Boolean>,
    savedZoomsRef: State<Map<String, Float>>,
    savedZooms: MutableMap<String, Float>,
    sniffedResources: MutableList<SniffedResource>,
    networkSniffedResources: MutableList<SniffedResource>,
    faviconCache: MutableMap<String, Bitmap>,
    searchEngines: List<Pair<String, String>>,
    searchQueryHolder: SearchQueryHolder,
    prefs: SharedPreferences,
    useNativePlayerEnabledRef: MutableState<Boolean>,
    isHomeUrl: (String) -> Boolean,
    onUpdateWindow: (String, (BrowserWindow) -> Unit) -> Unit,
    onRecordHistory: (String, String) -> Unit,
    onAddToSearchHistory: (String) -> Unit,
    onIsSearchResultPage: (Boolean) -> Unit,
    onUrlInput: (TextFieldValue) -> Unit,
    isSearchFocused: () -> Boolean,
    onZoomForHost: (String?) -> Float,
    onZoomFactor: (Float) -> Unit,
    onPersistSavedZooms: () -> Unit,
    onApplyCssZoom: (WebView, Float) -> Unit,
    onFaviconLoad: (String, String) -> Unit,
    onFaviconExplicit: (String, String, String) -> Unit,
    onPendingDownload: (PendingDownload) -> Unit,
    onFindResult: (Int, Int) -> Unit,
    onSniffResult: (SniffedResource) -> Unit,
    onShowHtmlFullscreen: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideHtmlFullscreen: () -> Unit,
    onEnglishPageDetected: (Boolean) -> Unit,
    fileChooserCallback: MutableState<ValueCallback<Array<Uri>>?>,
    fileChooserLauncher: ActivityResultLauncher<Intent>,
    pendingPermission: MutableState<PermissionRequest?>,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
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
                            windowId = id,
                            initialDesktopMode = windows.firstOrNull { it.id == id }?.desktopMode ?: false,
                            initialTextZoom = textZoomValue,
                            initialForceZoom = forceZoomEnabled,
                            initialDefaultZoom = defaultZoomPercent,
                            initialUrlProvider = { windows.firstOrNull { it.id == id }?.url ?: "" },
                            isZoomRestoreEnabled = { zoomRestoreEnabledRef.value },
                            getSavedZoom = { host -> savedZoomsRef.value[host] },
                            onZoomChanged = { host, scale ->
                                savedZooms[host] = scale
                                onPersistSavedZooms()
                            },
                            onScaleChanged = { _ -> },
                            onWebViewCreated = { },
                            isNativePlayerEnabled = { useNativePlayerEnabledRef.value },
                            onProgressChanged = { p ->
                                onUpdateWindow(id) { it.progress = p }
                            },
                            onPageStateChanged = { url, title, back, fwd ->
                                val win0 = windows.firstOrNull { it.id == id }
                                val urlChanged = win0?.url != url
                                if (urlChanged) {
                                    sniffedResources.clear()
                                    networkSniffedResources.clear()
                                }
                                val oldHost = try { win0?.url?.toUri()?.host } catch (_: Exception) { null }
                                val newHost = try { url.toUri().host } catch (_: Exception) { null }
                                val hostChanged = oldHost != newHost
                                onUpdateWindow(id) {
                                    if (urlChanged) { it.translated = false }
                                    if (hostChanged && newHost != null) {
                                        val cached = faviconCache[newHost]
                                        it.favicon = cached
                                    }
                                    it.url = url
                                    it.title = title
                                    it.canGoBack = back
                                    it.canGoForward = fwd
                                }
                                if (hostChanged) {
                                    val targetZoom = onZoomForHost(newHost)
                                    if (activeWindowId == id) {
                                        onZoomFactor(targetZoom)
                                    }
                                }
                                val win = windows.firstOrNull { it.id == id }
                                if (win?.incognito != true) {
                                    onRecordHistory(url, title)
                                }

                                if (url.isNotBlank() && url != "about:blank") {
                                    prefs.edit().putString("last_session_url", url).apply()
                                }

                                val urlObj = try { url.toUri() } catch (_: Exception) { null }

                                val engineBase = searchEngines.firstOrNull { (_, pattern) ->
                                    val p = pattern.substringAfter("://").substringBefore("/")
                                    val pBase = getBaseDomain(p)
                                    val uBase = getBaseDomain(urlObj?.host ?: "")
                                    pBase.isNotBlank() && pBase == uBase
                                }

                                val urlQuery = extractSearchQueryFromUrl(url)
                                var isOnSearchResultPage = false
                                if (engineBase != null) {
                                    val path = (urlObj?.path ?: "").lowercase()
                                    isOnSearchResultPage = when {
                                        urlQuery != null && urlQuery.isNotBlank() -> true
                                        engineBase.first == "百度" -> path.startsWith("/s")
                                        engineBase.first == "必应" -> path.startsWith("/search")
                                        engineBase.first == "抖音" -> path.startsWith("/search")
                                        engineBase.first == "神马" -> path.startsWith("/s")
                                        engineBase.first == "搜狗" ->
                                            path.startsWith("/web/searchList") || path.startsWith("/s")
                                        engineBase.first == "谷歌" -> path.startsWith("/search")
                                        engineBase.first == "自定义" ->
                                            extractSearchQueryFromUrl(url) != null
                                        else -> false
                                    }
                                }

                                if (isOnSearchResultPage) {
                                    val urlQuery2 = extractSearchQueryFromUrl(url)
                                    if (urlQuery2 != null && urlQuery2.isNotBlank() &&
                                        urlQuery2 != searchQueryHolder.value
                                    ) {
                                        searchQueryHolder.value = urlQuery2
                                        if (win?.incognito != true) {
                                            onAddToSearchHistory(urlQuery2)
                                        }
                                    }
                                }

                                val query = searchQueryHolder.value
                                val showQueryInBar = isOnSearchResultPage && query.isNotBlank()
                                onIsSearchResultPage(showQueryInBar)

                                if (!isSearchFocused()) {
                                    onUrlInput(
                                        when {
                                            showQueryInBar -> TextFieldValue(query)
                                            isHomeUrl(url) || url.isBlank() -> TextFieldValue("")
                                            else -> TextFieldValue(url)
                                        }
                                    )
                                }
                            },
                            onFaviconReceived = { host, bmp ->
                                val win = windows.firstOrNull { it.id == id }
                                val currentHost = try { win?.url?.toUri()?.host } catch (_: Exception) { null }
                                if (currentHost == host) {
                                    val existingFav = faviconCache[host]
                                    if (existingFav == null ||
                                        bmp.width * bmp.height > existingFav.width * existingFav.height
                                    ) {
                                        faviconCache[host] = bmp
                                        onUpdateWindow(id) { it.favicon = bmp }
                                    }
                                }
                            },
                            onFaviconFromDomain = { url -> onFaviconLoad(id, url) },
                            onDownloadRequest = { dl -> onPendingDownload(dl) },
                            onFindResult = { total, current -> onFindResult(total, current) },
                            onPageFinishedExtra = { wv2 ->
                                val win = windows.firstOrNull { it.id == id }

                                val targetZoom = if (activeWindowId == id) {
                                    onZoomForHost(try { wv2.url?.toUri()?.host } catch (_: Exception) { null })
                                } else {
                                    onZoomForHost(try { wv2.url?.toUri()?.host } catch (_: Exception) { null })
                                }
                                if (targetZoom != 1.0f) {
                                    onApplyCssZoom(wv2, targetZoom)
                                }

                                if (win?.nightMode == true) {
                                    wv2.evaluateJavascript(JS_NIGHT_ON, null)
                                }
                                if (win?.noImage == true) {
                                    wv2.evaluateJavascript(JS_NO_IMG_ON, null)
                                }
                                if (forceZoomEnabled) {
                                    wv2.evaluateJavascript(JS_FORCE_ZOOM_ON, null)
                                }
                                try { wv2.settings.textZoom = textZoomValue } catch (_: Exception) {}

                                wv2.evaluateJavascript(JS_PULL_REFRESH_GUARD, null)
                                wv2.evaluateJavascript(JS_HORIZONTAL_SCROLL_GUARD, null)

                                if (prefs.getBoolean("web_smart_preload_enabled", true)) {
                                    val maxHosts = prefs.getInt("web_smart_preload_max_hosts", 5)
                                        .coerceIn(1, 10)
                                    wv2.evaluateJavascript(buildJsSmartPreconnect(maxHosts), null)
                                }

                                wv2.evaluateJavascript(JS_SNIFF_RESOURCES) { raw ->
                                    val parsed = parseSniffedResources(raw)
                                    sniffedResources.clear()
                                    sniffedResources.addAll(parsed)
                                }
                                val currentHost = try { win?.url?.toUri()?.host } catch (_: Exception) { null }
                                if (currentHost != null) {
                                    wv2.evaluateJavascript(JS_FIND_FAVICON_URL) { raw ->
                                        val href = decodeJsString(raw)
                                        if (href.isNotBlank() && href.startsWith("http")) {
                                            onFaviconExplicit(id, href, currentHost)
                                        }
                                    }
                                }

                                wv2.evaluateJavascript(JS_DETECT_ENGLISH_PAGE) { raw ->
                                    val result = decodeJsString(raw)
                                    onEnglishPageDetected(result == "en")
                                }
                            },
                            onResourceSniffed = { resource -> onSniffResult(resource) },
                            onPageStartedExtra = {
                                sniffedResources.clear()
                                networkSniffedResources.clear()
                            },
                            onShowCustomView = { v, cb -> onShowHtmlFullscreen(v, cb) },
                            onHideCustomView = { onHideHtmlFullscreen() },
                            onEnglishPageDetected = { isEn -> onEnglishPageDetected(isEn) },
                            fileChooserCallback = fileChooserCallback,
                            fileChooserLauncher = fileChooserLauncher,
                            pendingPermission = pendingPermission,
                            permissionLauncher = permissionLauncher,
                            onRenderProcessGoneCallback = { dead ->
                                webViewMap.entries.firstOrNull { it.value === dead }?.let {
                                    webViewMap.remove(it.key)
                                }
                            },
                        )
                        webViewMap[id] = created

                        val win = windows.firstOrNull { it.id == id }
                        if (win?.incognito == true) {
                            try { created.settings.cacheMode = WebSettings.LOAD_NO_CACHE } catch (_: Exception) {}
                        }

                        val initialUrl = win?.url.orEmpty().ifBlank { "about:blank" }
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

// ============================================================
// 资源嗅探解析
// ============================================================

private fun parseSniffedResources(raw: String?): List<SniffedResource> {
    val jsonStr = decodeJsString(raw)
    if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
    return try {
        val arr = JSONArray(jsonStr)
        val result = ArrayList<SniffedResource>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val url = obj.optString("url", "")
            if (url.isBlank()) continue
            val catName = obj.optString("category", "other").lowercase()
            val cat = when (catName) {
                "image" -> SniffCategory.IMAGE
                "video" -> SniffCategory.VIDEO
                "audio" -> SniffCategory.AUDIO
                "file" -> SniffCategory.FILE
                else -> SniffCategory.OTHER
            }
            result.add(
                SniffedResource(
                    url = url,
                    category = cat,
                    filename = extractFilename(url),
                    fromNetwork = false,
                )
            )
        }
        result
    } catch (_: Exception) {
        emptyList()
    }
}

private fun extractFilename(url: String): String {
    return try {
        val uri = url.toUri()

        val query = uri.query
        if (!query.isNullOrBlank()) {
            for (p in query.split("&")) {
                val kv = p.split("=", limit = 2)
                if (kv.size == 2) {
                    val k = kv[0].lowercase()
                    if (k == "filename" || k == "name" || k == "file" || k == "title") {
                        val v = URLDecoder.decode(kv[1], "UTF-8")
                        if (v.isNotBlank()) return v
                    }
                }
            }
        }

        val path = uri.path ?: return url
        val last = path.substringAfterLast('/', "")
        if (last.isNotBlank() && last != "/") {
            val decoded = URLDecoder.decode(last, "UTF-8")
            if (decoded.isNotBlank()) {
                if ('.' !in decoded) {
                    val ext = guessExtFromUrl(url)
                    return "$decoded$ext"
                }
                return decoded
            }
        }

        val host = uri.host ?: "file"
        val hash = path.hashCode().toString(16).takeLast(8)
        val ext = guessExtFromUrl(url)
        "$host-$hash$ext"
    } catch (_: Exception) {
        url
    }
}

private fun guessExtFromUrl(url: String): String {
    val lower = url.lowercase()
    val clean = lower.substringBefore('#').substringBefore('?')
    return when {
        clean.endsWith(".mp4") -> ".mp4"
        clean.endsWith(".m3u8") -> ".m3u8"
        clean.endsWith(".ts") -> ".ts"
        clean.endsWith(".webm") -> ".webm"
        clean.endsWith(".mkv") -> ".mkv"
        clean.endsWith(".mov") -> ".mov"
        clean.endsWith(".mp3") -> ".mp3"
        clean.endsWith(".m4a") -> ".m4a"
        clean.endsWith(".jpg") || clean.endsWith(".jpeg") -> ".jpg"
        clean.endsWith(".png") -> ".png"
        clean.endsWith(".gif") -> ".gif"
        clean.endsWith(".webp") -> ".webp"
        clean.endsWith(".pdf") -> ".pdf"
        clean.endsWith(".zip") -> ".zip"
        else -> ""
    }
}

private fun guessMimeFromUrl(url: String): String? {
    val lower = url.lowercase()
    return when {
        lower.contains(".jpg") || lower.contains(".jpeg") -> "image/jpeg"
        lower.contains(".png") -> "image/png"
        lower.contains(".gif") -> "image/gif"
        lower.contains(".webp") -> "image/webp"
        lower.contains(".svg") -> "image/svg+xml"
        lower.contains(".bmp") -> "image/bmp"
        lower.contains(".mp4") -> "video/mp4"
        lower.contains(".webm") -> "video/webm"
        lower.contains(".mkv") -> "video/x-matroska"
        lower.contains(".mov") -> "video/quicktime"
        lower.contains(".avi") -> "video/x-msvideo"
        lower.contains(".flv") -> "video/x-flv"
        lower.contains(".m3u8") -> "application/vnd.apple.mpegurl"
        lower.contains(".mp3") -> "audio/mpeg"
        lower.contains(".wav") -> "audio/wav"
        lower.contains(".ogg") -> "audio/ogg"
        lower.contains(".m4a") -> "audio/mp4"
        lower.contains(".flac") -> "audio/flac"
        lower.contains(".aac") -> "audio/aac"
        lower.contains(".pdf") -> "application/pdf"
        lower.contains(".zip") -> "application/zip"
        lower.contains(".rar") -> "application/x-rar-compressed"
        lower.contains(".7z") -> "application/x-7z-compressed"
        lower.contains(".apk") -> "application/vnd.android.package-archive"
        lower.contains(".doc") || lower.contains(".docx") -> "application/msword"
        lower.contains(".xls") || lower.contains(".xlsx") -> "application/vnd.ms-excel"
        lower.contains(".ppt") || lower.contains(".pptx") -> "application/vnd.ms-powerpoint"
        lower.contains(".txt") -> "text/plain"
        lower.contains(".epub") -> "application/epub+zip"
        else -> null
    }
}

@Composable
private fun SniffSheet(
    show: Boolean,
    isSniffing: Boolean,
    resources: List<SniffedResource>,
    showAllResources: Boolean,
    onOpen: (SniffedResource) -> Unit,
    onDownload: (SniffedResource) -> Unit,
    onToggleShowAll: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var filter by remember { mutableStateOf<SniffCategory?>(null) }

    LaunchedEffect(show) {
        if (!show) {
            filter = null
        }
    }

    // ★ 需求 3：过滤不重要资源
    val importantResources = remember(resources, showAllResources) {
        if (showAllResources) resources
        else resources.filter { isImportantResource(it) }
    }

    val availableCategories = remember(importantResources) {
        SniffCategory.entries.filter { cat -> importantResources.any { it.category == cat } }
    }

    val filteredResources = remember(importantResources, filter) {
        if (filter == null) importantResources
        else importantResources.filter { it.category == filter }
    }

    val filterEntries = remember(importantResources, filter, showAllResources) {
        listOf(
            DropdownEntry(
                items = buildList {
                    add(
                        DropdownItem(
                            text = "全部（${importantResources.size}）",
                            selected = filter == null,
                            onClick = { filter = null },
                        )
                    )
                    availableCategories.forEach { cat ->
                        val count = importantResources.count { it.category == cat }
                        add(
                            DropdownItem(
                                text = "${cat.label}（$count）",
                                selected = filter == cat,
                                onClick = { filter = cat },
                            )
                        )
                    }
                },
            ),
            // ★ 需求 3：显示所有资源开关
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = if (showAllResources) "✓ 显示所有资源" else "显示所有资源",
                        selected = showAllResources,
                        onClick = { onToggleShowAll(!showAllResources) },
                    ),
                ),
            ),
        )
    }

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "资源嗅探",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,

        // ★ 需求 3：删除刷新按钮

        endAction = {
            WindowIconCascadingDropdownMenu(entries = filterEntries) {
                Icon(
                    imageVector = MiuixIcons.Filter,
                    contentDescription = "筛选",
                    tint = when {
                        filter != null || showAllResources -> MiuixTheme.colorScheme.primary
                        importantResources.isNotEmpty() -> MiuixTheme.colorScheme.onSurface
                        else -> MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
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
                when {
                    isSniffing && importantResources.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "正在嗅探资源…",
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                    filteredResources.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (filter != null) "当前筛选下暂无资源" else "未发现可嗅探的资源",
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                    else -> {
                        val grouped = filteredResources.groupBy { it.category }
                        val ordered = SniffCategory.entries.toList()
                            .mapNotNull { cat -> grouped[cat]?.let { cat to it } }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ordered.forEach { (cat, list) ->
                                item(key = "header_${cat.name}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 12.dp,
                                                bottom = 6.dp,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = cat.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "(${list.size})",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                }

                                items(
                                    count = list.size,
                                    key = { idx -> "${cat.name}_${list[idx].url}" },
                                ) { idx ->
                                    val res = list[idx]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .clickable { onOpen(res) }
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 10.dp,
                                                bottom = 10.dp,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = sniffIconFor(cat),
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = res.filename.ifBlank { res.url },
                                                fontSize = 14.sp,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = res.url,
                                                fontSize = 11.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .clickable { onDownload(res) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Download,
                                                contentDescription = "下载",
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
    }
}

// ★ 需求 3：判断资源是否"重要"
private fun isImportantResource(res: SniffedResource): Boolean {
    val url = res.url.lowercase()
    val clean = url.substringBefore('#').substringBefore('?')

    return when (res.category) {
        SniffCategory.VIDEO, SniffCategory.AUDIO -> true
        SniffCategory.IMAGE -> {
            val blacklist = listOf(
                "favicon", "/logo", "logo.", "/icon", "icon_", "icon-",
                "sprite", "button_", "button-", "/btn", "/bg_", "/bg-",
                "loading", "/ad_", "/ad-", "/ads/", "/advert",
                "avatar_default", "default_avatar", "placeholder",
                "transparent", "blank.gif", "spacer", "pixel.gif",
                "tracking", "/stat", "/count", "/analytics", "beacon"
            )
            if (blacklist.any { clean.contains(it) }) return false
            if (clean.contains("1x1") || clean.contains("0x0")) return false
            true
        }
        SniffCategory.FILE -> {
            if (clean.endsWith(".js") || clean.endsWith(".css") ||
                clean.endsWith(".map") || clean.endsWith(".json") ||
                clean.endsWith(".woff") || clean.endsWith(".woff2") ||
                clean.endsWith(".ttf") || clean.endsWith(".otf") ||
                clean.endsWith(".eot")
            ) return false
            true
        }
        SniffCategory.OTHER -> false
    }
}

private fun sniffIconFor(category: SniffCategory): ImageVector = when (category) {
    SniffCategory.VIDEO -> MiuixIcons.Play
    SniffCategory.AUDIO -> MiuixIcons.Music
    SniffCategory.IMAGE -> MiuixIcons.Photos
    SniffCategory.FILE -> MiuixIcons.File
    SniffCategory.OTHER -> MiuixIcons.File
}

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
            expandFrom = Alignment.Bottom,
        ) + fadeIn(tween(160)),
        exit = shrinkVertically(
            animationSpec = tween(200),
            shrinkTowards = Alignment.Bottom,
        ) + fadeOut(tween(120)),
    ) {
        Surface(
            color = MiuixTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MiuixTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "页面内查找",
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (resultTotal == 0) "0/0" else "$resultCurrent/$resultTotal",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.animateContentSize(
                        animationSpec = tween(durationMillis = 180)
                    ),
                )

                Spacer(Modifier.width(10.dp))
                MiuixCircleIconButton(onClick = onPrev, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronBackward,
                        contentDescription = "上一个",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                MiuixCircleIconButton(onClick = onNext, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "下一个",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                MiuixCircleIconButton(onClick = onClose, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomBar(
    show: Boolean,
    percent: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter = expandVertically(
            animationSpec = tween(220),
            expandFrom = Alignment.Bottom,
        ) + fadeIn(tween(160)),
        exit = shrinkVertically(
            animationSpec = tween(200),
            shrinkTowards = Alignment.Bottom,
        ) + fadeOut(tween(120)),
    ) {
        Surface(
            color = MiuixTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiuixCircleIconButton(onClick = onZoomOut, size = 32.dp) {
                    Text(
                        text = "−",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "${(percent * 100).roundToInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .width(60.dp)
                        .animateContentSize(animationSpec = tween(150)),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.width(12.dp))

                MiuixCircleIconButton(onClick = onZoomIn, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.Add,
                        contentDescription = "放大",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MiuixTheme.colorScheme.secondaryContainer)
                        .clickable { onReset() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "重置",
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.width(8.dp))

                MiuixCircleIconButton(onClick = onClose, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭",
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

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
                        modifier = Modifier.fillMaxWidth(),
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
                                    val winIsNewTab = win.url.isBlank() || win.url == "about:blank"
                                    Text(
                                        text = if (winIsNewTab) "新标签页"
                                        else win.title.ifBlank { "无标题" },
                                        fontSize = 15.sp,
                                        fontWeight = if (isActive) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (winIsNewTab) "新标签页" else win.url,
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

@Composable
private fun MultiSelectBottomBar(
    selectedCount: Int,
    totalCount: Int,
    onToggleAll: () -> Unit,
    onDelete: () -> Unit,
) {
    val allSelected = totalCount > 0 && selectedCount == totalCount
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
                .clickable { onToggleAll() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (allSelected) "取消" else "全选",
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "多选模式",
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MiuixTheme.colorScheme.primary)
                .clickable(enabled = selectedCount > 0) { onDelete() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "删除",
                fontSize = 14.sp,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun HistorySheet(
    show: Boolean,
    history: List<HistoryEntry>,
    multiSelect: Boolean,
    selectedUrls: List<String>,
    onEnterMultiSelect: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onCancelMultiSelect: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
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
        allowDismiss = !multiSelect,

        endAction = {
            Crossfade(
                targetState = multiSelect,
                animationSpec = tween(220),
                label = "historyEndAction",
            ) { isMulti ->
                if (isMulti) {
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 36.dp)
                            .clip(CircleShape)
                            .clickable { onCancelMultiSelect() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "退出多选",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
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
                }
            }
        },
    ) {
        val bottomPadding by animateDpAsState(
            targetValue = if (multiSelect) 0.dp else TOOL_SHEET_NON_EDIT_BOTTOM,
            animationSpec = tween(durationMillis = 260),
            label = "historyBottomPadding",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = bottomPadding),
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
                    val grouped = remember(history.toList()) {
                        val map = LinkedHashMap<String, MutableList<HistoryEntry>>()
                        history.forEach { entry ->
                            val key = getHistoryDayLabel(entry.timestamp)
                            map.getOrPut(key) { mutableListOf() }.add(entry)
                        }
                        map
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                    ) {
                        grouped.forEach { (dayLabel, entries) ->
                            item(key = "h_day_$dayLabel") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 12.dp,
                                            bottom = 4.dp,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = dayLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "(${entries.size})",
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }

                            items(
                                count = entries.size,
                                key = { idx -> "h_${entries[idx].url}_${entries[idx].timestamp}" },
                            ) { idx ->
                                val entry = entries[idx]
                                val isSelected = entry.url in selectedUrls
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .combinedClickable(
                                            onClick = {
                                                if (multiSelect) {
                                                    onToggleSelect(entry.url)
                                                } else {
                                                    onOpenUrl(entry.url)
                                                }
                                            },
                                            onLongClick = {
                                                if (!multiSelect) {
                                                    onEnterMultiSelect(entry.url)
                                                }
                                            },
                                        )
                                        .background(
                                            if (isSelected)
                                                MiuixTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 10.dp,
                                            bottom = 10.dp,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (multiSelect) {
                                        Checkbox(
                                            state = if (isSelected) ToggleableState.On
                                            else ToggleableState.Off,
                                            onClick = { onToggleSelect(entry.url) },
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    } else {
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
                                    }

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

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = formatHistoryTime(entry.timestamp),
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )

                                    if (!multiSelect) {
                                        Spacer(Modifier.width(8.dp))
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

            AnimatedVisibility(
                visible = multiSelect,
                enter = expandVertically(
                    animationSpec = tween(260),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(
                    animationSpec = tween(260),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(140)),
            ) {
                MultiSelectBottomBar(
                    selectedCount = selectedUrls.size,
                    totalCount = history.size,
                    onToggleAll = onToggleSelectAll,
                    onDelete = {
                        if (selectedUrls.isNotEmpty()) onDeleteSelected()
                    },
                )
            }
        }
    }
}

// ============================================================
// 书签列表
// ============================================================

private data class BookmarkLevelKey(
    val depth: Int,
    val folderId: String?,
)

private fun findBookmarkByIdInList(items: List<BookmarkItem>, id: String): BookmarkItem? {
    for (item in items) {
        if (item.id == id) return item
        if (item.isFolder) {
            findBookmarkByIdInList(item.children, id)?.let { return it }
        }
    }
    return null
}

@Composable
private fun CompactRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val primary = MiuixTheme.colorScheme.primary
    val inactiveRing = MiuixTheme.colorScheme.onSurfaceVariantSummary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) primary else inactiveRing,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(primary),
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )

        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun BookmarkSheet(
    show: Boolean,
    bookmarks: List<BookmarkItem>,
    folderPath: List<BookmarkItem>,
    currentItems: List<BookmarkItem>,
    multiSelect: Boolean,
    selectedIds: List<String>,
    onEnterMultiSelect: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onCancelMultiSelect: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddBookmark: () -> Unit,
    onAddFolder: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onEnterFolder: (BookmarkItem) -> Unit,
    onNavigateToBreadcrumb: (Int) -> Unit,
    onDelete: (BookmarkItem) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val breadcrumbItems = remember(bookmarks.toList(), folderPath.size) {
        buildList {
            add(BreadcrumbItem(path = "root", text = "根目录"))
            folderPath.forEach { folder ->
                add(BreadcrumbItem(path = folder.id, text = folder.title))
            }
        }
    }
    val highlightIndex = breadcrumbItems.lastIndex

    val currentLevelKey = BookmarkLevelKey(
        depth = folderPath.size,
        folderId = folderPath.lastOrNull()?.id,
    )

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "书签列表",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = !multiSelect,

        startAction = {
            Crossfade(
                targetState = multiSelect,
                animationSpec = tween(220),
                label = "bookmarkStartAction",
            ) { isMulti ->
                if (!isMulti) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 36.dp)
                                .clip(CircleShape)
                                .clickable { onAddBookmark() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Favorites,
                                contentDescription = "添加书签",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        if (folderPath.size < 2) {
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .clip(CircleShape)
                                    .clickable { onAddFolder() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.AddFolder,
                                    contentDescription = "新建文件夹",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.size(width = 54.dp, height = 36.dp))
                }
            }
        },

        endAction = {
            Crossfade(
                targetState = multiSelect,
                animationSpec = tween(220),
                label = "bookmarkEndAction",
            ) { isMulti ->
                if (isMulti) {
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 36.dp)
                            .clip(CircleShape)
                            .clickable { onCancelMultiSelect() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "退出多选",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
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
                }
            }
        },
    ) {
        val bottomPadding by animateDpAsState(
            targetValue = if (multiSelect) 0.dp else TOOL_SHEET_NON_EDIT_BOTTOM,
            animationSpec = tween(durationMillis = 260),
            label = "bookmarkBottomPadding",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = bottomPadding),
        ) {
            if (breadcrumbItems.isNotEmpty()) {
                BreadcrumbBar(
                    items = breadcrumbItems,
                    highlightIndex = highlightIndex,
                    onItemClick = { index ->
                        onNavigateToBreadcrumb(index - 1)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                )
            }

            AnimatedContent(
                targetState = currentLevelKey,
                transitionSpec = {
                    val isForward = targetState.depth > initialState.depth
                    val slideSpec = tween<IntOffset>(
                        durationMillis = 340,
                        easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                    )
                    val fadeSpec = tween<Float>(durationMillis = 220)
                    if (isForward) {
                        (
                                slideInHorizontally(
                                    animationSpec = slideSpec,
                                    initialOffsetX = { fullWidth -> fullWidth },
                                ) + fadeIn(animationSpec = fadeSpec)
                                ) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = slideSpec,
                                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                ) + fadeOut(animationSpec = fadeSpec)
                                )
                    } else {
                        (
                                slideInHorizontally(
                                    animationSpec = slideSpec,
                                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                                ) + fadeIn(animationSpec = fadeSpec)
                                ) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = slideSpec,
                                    targetOffsetX = { fullWidth -> fullWidth },
                                ) + fadeOut(animationSpec = fadeSpec)
                                )
                    }
                },
                label = "bookmarkLevel",
            ) { levelKey ->
                val levelItems: List<BookmarkItem> =
                    if (levelKey.depth == 0 || levelKey.folderId == null) {
                        bookmarks.toList()
                    } else {
                        findBookmarkByIdInList(bookmarks, levelKey.folderId)?.children?.toList()
                            ?: emptyList()
                    }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    if (levelItems.isEmpty()) {
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
                        val sortedItems = remember(levelItems) {
                            val folders = levelItems.filter { it.isFolder }
                            val files = levelItems.filter { !it.isFolder }
                            folders + files
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp),
                        ) {
                            items(
                                count = sortedItems.size,
                                key = { index -> sortedItems[index].id },
                            ) { index ->
                                val entry = sortedItems[index]
                                val isSelected = entry.id in selectedIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .combinedClickable(
                                            onClick = {
                                                if (multiSelect) {
                                                    onToggleSelect(entry.id)
                                                } else if (entry.isFolder) {
                                                    onEnterFolder(entry)
                                                } else {
                                                    entry.url?.let { onOpenUrl(it) }
                                                }
                                            },
                                            onLongClick = {
                                                if (!multiSelect) {
                                                    onEnterMultiSelect(entry.id)
                                                }
                                            },
                                        )
                                        .background(
                                            if (isSelected)
                                                MiuixTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 10.dp,
                                            bottom = 10.dp,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (multiSelect) {
                                        Checkbox(
                                            state = if (isSelected) ToggleableState.On
                                            else ToggleableState.Off,
                                            onClick = { onToggleSelect(entry.id) },
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier.size(32.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (entry.isFolder) {
                                                    MiuixIcons.Folder
                                                } else {
                                                    MiuixIcons.Favorites
                                                },
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.title.ifBlank { "无标题" },
                                            fontSize = 15.sp,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                        if (!entry.isFolder) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = entry.url.orEmpty(),
                                                fontSize = 12.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                            )
                                        } else {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "${entry.children.size} 项",
                                                fontSize = 12.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                            )
                                        }
                                    }

                                    if (!multiSelect) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .clickable { onDelete(entry) },
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

            AnimatedVisibility(
                visible = multiSelect,
                enter = expandVertically(
                    animationSpec = tween(260),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(
                    animationSpec = tween(260),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(140)),
            ) {
                MultiSelectBottomBar(
                    selectedCount = selectedIds.size,
                    totalCount = currentItems.size,
                    onToggleAll = onToggleSelectAll,
                    onDelete = {
                        if (selectedIds.isNotEmpty()) onDeleteSelected()
                    },
                )
            }
        }
    }
}

private val TOOL_SHEET_CELL_HEIGHT = 68.dp
private val TOOL_SHEET_EDITBAR_BOTTOM = 24.dp
private val TOOL_SHEET_NON_EDIT_BOTTOM = 24.dp


@Composable
private fun rememberEllipsizedSheetTitle(
    title: String,
    // 两侧保留宽度：startAction 54dp + endAction 54dp + 内边距
    reservedWidth: Dp = 176.dp,
): String {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val availableWidthPx = remember(
        configuration.screenWidthDp, reservedWidth, density
    ) {
        with(density) {
            (configuration.screenWidthDp.dp - reservedWidth).toPx().toInt()
        }.coerceAtLeast(1)
    }

    // 使用 MIUIX 标题样式（如果编译报错，改成 title1 / title3 / 或自定义 TextStyle）
    val titleStyle = MiuixTheme.textStyles.title2

    return remember(title, availableWidthPx, titleStyle) {
        if (title.isEmpty()) return@remember title

        // 完整文本宽度
        val fullWidth = textMeasurer.measure(
            text = AnnotatedString(title),
            style = titleStyle,
        ).size.width

        if (fullWidth <= availableWidthPx) return@remember title

        // 省略号宽度
        val ellipsisWidth = textMeasurer.measure(
            text = AnnotatedString("…"),
            style = titleStyle,
        ).size.width
        val maxContentWidth = (availableWidthPx - ellipsisWidth).coerceAtLeast(0)

        // 二分查找最长可放下的前缀
        var lo = 0
        var hi = title.length
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            val w = textMeasurer.measure(
                text = AnnotatedString(title.substring(0, mid)),
                style = titleStyle,
            ).size.width
            if (w <= maxContentWidth) lo = mid else hi = mid - 1
        }

        if (lo <= 0) "…" else title.substring(0, lo).trimEnd() + "…"
    }
}

@Composable
private fun BrowserToolSheet(
    show: Boolean,
    pageTitle: String,
    favicon: Bitmap?,
    isFullscreen: Boolean,
    isNightMode: Boolean,
    isIncognito: Boolean,
    isDesktopMode: Boolean,
    isNoImage: Boolean,
    isTranslated: Boolean,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    onToolAction: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    val allTools = remember {
        defaultTools().filter { it.id != "play_in_player" }
    }
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

    LaunchedEffect(show) {
        if (show) {
            state.setActive("fullscreen", isFullscreen)
            state.setActive("night_mode", isNightMode)
            state.setActive("incognito", isIncognito)
            state.setActive("desktop_mode", isDesktopMode)
            state.setActive("no_image", isNoImage)
            state.setActive("translate", isTranslated)
        }
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
        title = rememberEllipsizedSheetTitle(pageTitle.ifBlank { "新标签页" }),
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
    if (tool.customItem != null) {
        tool.customItem(isActive)
        return
    }

    val iconColorTarget = if (tool.isToggle && isActive) {
        MiuixTheme.colorScheme.primary
    } else {
        MiuixTheme.colorScheme.onSurface
    }
    val animatedIconColor by animateColorAsState(
        targetValue = iconColorTarget,
        animationSpec = tween(
            durationMillis = 220,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
        ),
        label = "toolIconColor_${tool.id}",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (tool.customIcon != null) {
            tool.customIcon(isActive)
        } else {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = animatedIconColor,
                modifier = Modifier.size(26.dp),
            )
        }
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

// ============================================================
// WebView 工厂
// ============================================================

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    windowId: String,
    initialDesktopMode: Boolean,
    initialTextZoom: Int,
    initialForceZoom: Boolean,
    initialDefaultZoom: Int,
    initialUrlProvider: () -> String,
    isZoomRestoreEnabled: () -> Boolean,
    getSavedZoom: (String) -> Float?,
    onZoomChanged: (host: String, scale: Float) -> Unit,
    onScaleChanged: (scale: Float) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    isNativePlayerEnabled: () -> Boolean,
    onProgressChanged: (Int) -> Unit,
    onPageStateChanged: (url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onFaviconReceived: (host: String, bmp: Bitmap) -> Unit,
    onFaviconFromDomain: (url: String) -> Unit,
    onDownloadRequest: (PendingDownload) -> Unit,
    onFindResult: (total: Int, current: Int) -> Unit,
    onPageFinishedExtra: (WebView) -> Unit,
    onResourceSniffed: (SniffedResource) -> Unit,
    onPageStartedExtra: () -> Unit,
    onShowCustomView: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideCustomView: () -> Unit,
    onEnglishPageDetected: (Boolean) -> Unit,
    fileChooserCallback: MutableState<ValueCallback<Array<Uri>>?>,
    fileChooserLauncher: ActivityResultLauncher<Intent>,
    pendingPermission: MutableState<PermissionRequest?>,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onRenderProcessGoneCallback: (WebView) -> Unit,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val mobileUa = getMobileUserAgent(context)

        // ★ 需求 2：读取权限开关
        val pmPrefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val rememberFormEnabled = try {
            pmPrefs.getBoolean("web_remember_form", true)
        } catch (_: Exception) { true }
        val acceptCookieEnabled = try {
            pmPrefs.getBoolean("web_accept_cookie", true)
        } catch (_: Exception) { true }

        settings.apply {
            userAgentString = if (initialDesktopMode) DESKTOP_UA else mobileUa
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            textZoom = initialTextZoom
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
            // ★ 需求 2：记住表单数据
            saveFormData = rememberFormEnabled
        }

        // ★ 需求 2：接受 Cookie
        CookieManager.getInstance().setAcceptCookie(acceptCookieEnabled)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, acceptCookieEnabled)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        addJavascriptInterface(object {
            @JavascriptInterface
            fun setPullRefreshAllowed(allowed: Boolean) {
                PullRefreshStateHolder.set(windowId, allowed)
            }

            @JavascriptInterface
            fun setSwipeNavAllowed(allowed: Boolean) {
                SwipeNavStateHolder.set(windowId, allowed)
            }
        }, "moteBridge")

        addJavascriptInterface(object {
            @Suppress("unused")
            @JavascriptInterface
            fun onUrlChanged(newUrl: String) {
                post {
                    onPageStateChanged(
                        newUrl,
                        title ?: "无标题",
                        canGoBack(),
                        canGoForward(),
                    )
                    onFaviconFromDomain(newUrl)
                }
            }
        }, "androidBridge")

        setFindListener { activeOrdinal, totalMatches, _ ->
            post { onFindResult(totalMatches, activeOrdinal) }
        }

        webViewClient = object : WebViewClient() {

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                super.onScaleChanged(view, oldScale, newScale)
                onScaleChanged(newScale)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                try {
                    if (request != null) {
                        val url = request.url?.toString()
                        if (!url.isNullOrBlank() &&
                            !url.startsWith("data:") &&
                            !url.startsWith("blob:") &&
                            !url.startsWith("about:")
                        ) {
                            var cat = sniffCategoryFromNetwork(url)
                            if (cat == null && looksLikeMediaByHeaders(request)) {
                                cat = SniffCategory.VIDEO
                            }
                            if (cat != null) {
                                val res = SniffedResource(
                                    url = url,
                                    category = cat,
                                    filename = extractFilename(url),
                                    fromNetwork = true,
                                )
                                post { onResourceSniffed(res) }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                return super.shouldInterceptRequest(view, request)
            }

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
                onEnglishPageDetected(false)
                onPageStartedExtra()
                try { view?.evaluateJavascript(JS_HOOK_XHR_FETCH, null) } catch (_: Exception) {}
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

                    if (initialForceZoom) {
                        view?.evaluateJavascript(JS_FORCE_ZOOM_ON, null)
                    }

                    val jsHook = buildString {
                        append("(function(){")
                        append("if (window.__mote_history_hooked) return;")
                        append("window.__mote_history_hooked = true;")
                        append("const _pushState = history.pushState;")
                        append("history.pushState = function(state, title, url){")
                        append("_pushState.call(this, state, title, url);")
                        append("window.androidBridge.onUrlChanged(location.href);")
                        append("};")
                        append("const _replaceState = history.replaceState;")
                        append("history.replaceState = function(state, title, url){")
                        append("_replaceState.call(this, state, title, url);")
                        append("window.androidBridge.onUrlChanged(location.href);")
                        append("};")
                        append("window.addEventListener('popstate', function(){")
                        append("window.androidBridge.onUrlChanged(location.href);")
                        append("});")
                        append("})();")
                    }

                    view?.evaluateJavascript(JS_INTERCEPT_VIDEO, null)

                    view?.evaluateJavascript(JS_PULL_REFRESH_GUARD, null)
                    view?.evaluateJavascript(JS_HORIZONTAL_SCROLL_GUARD, null)

                    view?.evaluateJavascript(jsHook, null)

                    view?.let { onPageFinishedExtra(it) }
                }
                try {
                    CookieManager.getInstance().flush()
                    android.util.Log.d("MoteCookie", "Cookie flushed for URL: $url")
                } catch (e: Exception) {
                    e.printStackTrace()
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
                // ★ 需求 2：改用 MIUIX WindowDialog（由 BrowserScreen 统一渲染）
                PendingSslErrorStore.pending = PendingSslError(
                    errorText = primaryError,
                    proceed = {
                        try { handler.proceed() } catch (_: Exception) {}
                    },
                    cancel = {
                        try { handler.cancel() } catch (_: Exception) {}
                        try {
                            if (view.canGoBack()) view.goBack()
                        } catch (_: Exception) {}
                    },
                )
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                try {
                    (view?.parent as? ViewGroup)?.removeView(view)
                    view?.destroy()
                    view?.let { onRenderProcessGoneCallback(it) }
                } catch (_: Exception) {}
                return true
            }
        }

        val externalOnShowCustomView = onShowCustomView

        webChromeClient = object : WebChromeClient() {

            override fun onShowCustomView(
                view: View?,
                callback: CustomViewCallback?
            ) {
                val webViewRef = this@apply

                if (!isNativePlayerEnabled()) {
                    externalOnShowCustomView(view, callback)
                    (context as? Activity)?.let { act ->
                        act.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                    return
                }

                webViewRef.evaluateJavascript(
                    "(function(){try{var v=document.querySelector('video');" +
                            "if(!v)return '{}';" +
                            "var t=(v.currentTime||0)*1000;" +
                            "var u=v.currentSrc||v.src;" +
                            "if(!u){var s=v.querySelector('source');if(s)u=s.src;}" +
                            "return JSON.stringify({time:Math.round(t),url:u||''});" +
                            "}catch(e){return '{}';}})()"
                ) { progressRaw ->
                    val payload = try {
                        JSONObject(decodeJsString(progressRaw))
                    } catch (_: Exception) { JSONObject() }

                    val startPositionMs = payload.optLong("time", 0L)
                    var videoUrl = payload.optString("url", "")

                    if (videoUrl.isBlank() || !videoUrl.startsWith("http")) {
                        val sniffed = RecentVideoUrlStore.get(windowId)
                        if (!sniffed.isNullOrBlank()) {
                            videoUrl = sniffed
                        }
                    }

                    val currentPlaying = PlaybackSessionHolder.currentUrl
                    val isFromRecentStore = videoUrl == RecentVideoUrlStore.get(windowId)

                    val urlIsFresh = videoUrl.isNotBlank() &&
                            videoUrl.startsWith("http") &&
                            videoUrl != currentPlaying

                    val urlBelongsToPage = if (isFromRecentStore) {
                        try {
                            val pageHost = webViewRef.url?.toUri()?.host ?: ""
                            val videoHost = videoUrl.toUri().host ?: ""
                            pageHost.isNotBlank() && videoHost.isNotBlank() &&
                                    (videoHost == pageHost ||
                                            videoHost.endsWith("." + pageHost.substringAfter(".")))
                        } catch (_: Exception) { false }
                    } else true

                    val canHandoff = urlIsFresh && urlBelongsToPage

                    if (canHandoff) {
                        webViewRef.evaluateJavascript(
                            "(function(){try{var v=document.querySelector('video');" +
                                    "if(v)v.pause();}catch(e){}})()",
                            null
                        )

                        MainActivity.suppressOrientationRestore = true
                        externalOnShowCustomView(view, callback)
                        webViewRef.post {
                            try { onHideCustomView() } catch (_: Exception) {}
                            MainActivity.suppressOrientationRestore = false
                            try {
                                NativePlayerActivity.start(
                                    context = context,
                                    url = videoUrl,
                                    title = webViewRef.title?.ifBlank { "视频播放" } ?: "视频播放",
                                    fromWeb = true,
                                    startPositionMs = startPositionMs,
                                    referer = webViewRef.url ?: "",
                                )
                            } catch (_: Exception) {}
                        }
                    } else {
                        externalOnShowCustomView(view, callback)
                        (context as? Activity)?.let { act ->
                            act.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                    }
                }
            }

            override fun onHideCustomView() {
                if (!MainActivity.suppressOrientationRestore) {
                    (context as? Activity)?.let { act ->
                        act.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        WindowCompat.getInsetsController(act.window, act.window.decorView)
                            .show(WindowInsetsCompat.Type.systemBars())
                    }
                }
                MainActivity.suppressOrientationRestore = false
                onHideCustomView()
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                icon ?: return
                val url = view?.url ?: return
                val host = url.toUri().host ?: return
                if (icon.width <= 2 && icon.height <= 2) return
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

    // 含中文或空格 → 当搜索词
    if (trimmed.any { it.code in 0x4E00..0x9FFF || it == ' ' }) return false

    // ★ 需求 1：邮箱格式 → 当搜索词（避免用户输入邮箱时误打开邮箱域名）
    if (Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$").matches(trimmed)) return false

    val lower = trimmed.lowercase()

    // 显式协议 → 直接当 URL
    if (lower.startsWith("http://") ||
        lower.startsWith("https://") ||
        lower.startsWith("ftp://") ||
        lower.startsWith("file://") ||
        lower.startsWith("content://") ||
        lower.startsWith("about:") ||
        lower.startsWith("data:")
    ) return true

    // 去掉路径 / 查询 / fragment / userinfo，只看 host:port
    val authorityPart = lower
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    val hostPort = authorityPart.substringAfterLast('@')
    val host = hostPort.substringBefore(':')
    val port = hostPort.substringAfter(':', "")

    if (host.isEmpty()) return false

    // 端口必须是数字
    if (port.isNotEmpty() && port.any { !it.isDigit() }) return false

    // 本地地址
    if (host == "localhost" || host == "127.0.0.1" || host == "::1") return true

    // IPv4
    val seg = host.split('.')
    if (seg.size == 4 && seg.all { s ->
            s.isNotEmpty() && s.all { it.isDigit() } &&
                    (s.toIntOrNull()?.let { it in 0..255 } == true)
        }
    ) return true

    // 必须有至少一个点（顶级域名）
    val dotIdx = host.lastIndexOf('.')
    if (dotIdx <= 0 || dotIdx == host.length - 1) return false

    val tld = host.substring(dotIdx + 1)

    // 顶级域名：2~24 位，纯字母 / 字母+数字+连字符
    if (tld.length < 2 || tld.length > 24) return false
    if (!tld.all { it.isLetterOrDigit() || it == '-' }) return false
    // TLD 不能纯数字（排除 1.2.3.4.5 这种）
    if (tld.all { it.isDigit() }) return false

    // host 每一段不能为空
    if (host.split('.').any { it.isEmpty() }) return false

    // host 长度限制
    if (host.length > 253) return false

    return true
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
    conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)
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
    conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)
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

// ============================================================
// 主题包装器（响应颜色设置）
// ============================================================

@Composable
private fun MoteThemeWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var colorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_color_mode", 0).coerceIn(0, 2))
    }
    var monetEnabled by remember {
        mutableStateOf(prefs.getBoolean("ui_color_monet_enabled", false))
    }
    var keyColor by remember {
        mutableIntStateOf(prefs.getInt("ui_color_key_color", 0))
    }
    var paletteStyleName by remember {
        mutableStateOf(prefs.getString("ui_color_palette_style", "TonalSpot") ?: "TonalSpot")
    }
    var colorSpecName by remember {
        mutableStateOf(prefs.getString("ui_color_spec", "Spec2021") ?: "Spec2021")
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "ui_color_mode" -> colorMode = prefs.getInt(key, 0).coerceIn(0, 2)
                "ui_color_monet_enabled" -> monetEnabled = prefs.getBoolean(key, false)
                "ui_color_key_color" -> keyColor = prefs.getInt(key, 0)
                "ui_color_palette_style" -> paletteStyleName =
                    prefs.getString(key, "TonalSpot") ?: "TonalSpot"
                "ui_color_spec" -> colorSpecName =
                    prefs.getString(key, "Spec2021") ?: "Spec2021"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // ★ 深色模式 + 壁纸取色 → ColorSchemeMode
    val schemeMode = when (colorMode) {
        0 -> if (monetEnabled) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        1 -> if (monetEnabled) ColorSchemeMode.MonetLight else ColorSchemeMode.Light
        2 -> if (monetEnabled) ColorSchemeMode.MonetDark else ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }

    val paletteStyle = try {
        ThemePaletteStyle.valueOf(paletteStyleName)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }
    val colorSpec = try {
        ThemeColorSpec.valueOf(colorSpecName)
    } catch (_: Exception) {
        ThemeColorSpec.Spec2021
    }

    val themeController = remember(schemeMode, keyColor, paletteStyle, colorSpec) {
        ThemeController(
            colorSchemeMode = schemeMode,
            keyColor = if (keyColor == 0) null else Color(keyColor),
            paletteStyle = paletteStyle,
            colorSpec = colorSpec,
        )
    }

    MiuixTheme(controller = themeController) {
        content()
    }
}