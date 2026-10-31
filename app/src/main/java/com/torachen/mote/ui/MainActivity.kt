@file:Suppress("UseKtx", "SpellCheckingInspection")
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.torachen.mote.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.ContentValues
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaMetadataRetriever
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.ActionMode
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.RoundedCorner
import android.view.ViewConfiguration
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.JsPromptResult
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import top.yukonga.miuix.kmp.basic.SearchBarDefaults
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import androidx.webkit.ScriptHandler
import androidx.webkit.WebViewCompat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarResult

import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.layout.DialogDefaults

import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference

import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.AddFolder
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
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
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.RecordingTape
import top.yukonga.miuix.kmp.icon.extended.AppRecording
import top.yukonga.miuix.kmp.icon.extended.Close2
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Weeks
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Link

import com.torachen.mote.MoteApplication
import com.torachen.mote.service.HomeWallpaperCropOverlay
import com.torachen.mote.service.saveCroppedWallpaper
import com.torachen.mote.service.TrackerBlockList
import com.torachen.mote.service.UserScriptStore
import com.torachen.mote.ui.scripts.ScriptEditorPage
import com.torachen.mote.ui.scripts.ScriptsNavArgs
import com.torachen.mote.ui.scripts.ScriptsPage
import com.torachen.mote.ui.filemanager.FileManagerSettingsPage
import com.torachen.mote.ui.filemanager.LocalFileViewerRequest
import com.torachen.mote.ui.filemanager.LocalFileViewerState
import com.torachen.mote.ui.filemanager.LocalPdfViewerPage
import com.torachen.mote.ui.filemanager.LocalTextEditorPage
import com.torachen.mote.ui.filemanager.LocalWebViewerPage

import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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

private const val STYLE_ID_NIGHT = "__mote_night__"
private const val STYLE_ID_NO_IMG = "__mote_noimg__"

private const val MAX_HISTORY_SIZE = 500
private const val MAX_BOOKMARK_SIZE = 1000
private const val MAX_SEARCH_HISTORY_SIZE = 20

private const val SHAKE_G_FORCE_THRESHOLD = 2.1f
private const val SHAKE_COOLDOWN_MS = 700L

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

internal object PendingCropBitmapStore {
    var pending: Bitmap? = null
}

internal object WallpaperSavedEventStore {
    var pending by mutableStateOf(false)
}

internal object PendingSslErrorStore {
    var pending by mutableStateOf<PendingSslError?>(null)
}

internal data class PendingSslError(
    val errorText: String,
    val proceed: () -> Unit,
    val cancel: () -> Unit,
)

internal enum class JsDialogType {
    ALERT, CONFIRM, PROMPT
}

internal data class PendingJsDialog(
    val message: String,
    val type: JsDialogType,
    val defaultValue: String = "",
    val confirm: (String) -> Unit,
    val cancel: () -> Unit
)

internal object PendingJsDialogStore {
    var pending by mutableStateOf<PendingJsDialog?>(null)
}

internal object SettingsLayoutNavArgs {
    var initialTab: Int = 0
    var flashSearchHistoryOnEnter: Boolean = false
}

internal object SessionSnapshotStore {
    var pendingRestore: List<String>? = null
    var userExiting: Boolean = false
}

internal object InitialLaunchUrlStore {
    var pending by mutableStateOf<String?>(null)
}

internal data class PendingPassword(
    val url: String,
    val username: String,
    val password: String,
)

internal object PasswordCaptureStore {
    var pending by mutableStateOf<PendingPassword?>(null)
}

internal object InitialInsetsFallback {
    var navBarBottomPx: Int = 0
}

internal object GlobalOverlayState {
    var isOpen by mutableStateOf(false)
}

// ★ 新增：用于在 Activity 重建时保留页面栈和窗口数据
internal object PageNavigationStateHolder {
    val pageStack = mutableStateListOf(PageEntry(id = "browser_0", route = "browser"))
}

internal object BrowserWindowStateHolder {
    val windows = mutableStateListOf<BrowserWindow>()
    var activeWindowId: String? = null
}
private class SearchQueryHolder {
    var value: String = ""
}

internal object PullRefreshStateHolder {
    private val map = java.util.concurrent.ConcurrentHashMap<
            String,
            java.util.concurrent.atomic.AtomicBoolean
            >()

    fun get(windowId: String): Boolean = map[windowId]?.get() ?: true

    fun set(windowId: String, allowed: Boolean) {
        map.getOrPut(windowId) {
            java.util.concurrent.atomic.AtomicBoolean(true)
        }.set(allowed)
    }

    fun remove(windowId: String) {
        map.remove(windowId)
    }
}

internal object SwipeNavStateHolder {
    private val map = java.util.concurrent.ConcurrentHashMap<
            String,
            java.util.concurrent.atomic.AtomicBoolean
            >()

    fun get(windowId: String): Boolean = map[windowId]?.get() ?: true

    fun set(windowId: String, allowed: Boolean) {
        map.getOrPut(windowId) {
            java.util.concurrent.atomic.AtomicBoolean(true)
        }.set(allowed)
    }

    fun remove(windowId: String) {
        map.remove(windowId)
    }
}

internal object RecentVideoUrlStore {
    private val map = java.util.concurrent.ConcurrentHashMap<String, String>()
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

internal object LongPressSkipStore {
    private val map = java.util.Collections.synchronizedMap(
        java.util.WeakHashMap<WebView, Boolean>()
    )

    fun setSkip(wv: WebView) {
        map[wv] = true
    }

    fun consumeSkip(wv: WebView): Boolean {
        synchronized(map) {
            if (map[wv] == true) {
                map.remove(wv)
                return true
            }
            return false
        }
    }
}

// ============================================================
// 网页安全中心 - 数据模型
// ============================================================

internal data class SecurityInfo(
    val url: String = "",
    val host: String = "",
    val isHttps: Boolean = false,
    val hasSslError: Boolean = false,
    val sslErrorText: String? = null,
    val certIssuer: String = "",
    val certSubject: String = "",
    val certValidFrom: String = "",
    val certValidTo: String = "",
    val isCertExpired: Boolean = false,
    val hasMixedContent: Boolean = false,
    val siteManaged: Boolean = false,
    val siteBlockAdsEnabled: Boolean = true,
    val siteTrackingProtectionEnabled: Boolean = true,
    val globalBlockAdsEnabled: Boolean = true,
    val globalTrackingProtectionEnabled: Boolean = true,
    val blockedTrackers: Int = 0,
    val blockedAds: Int = 0,
    val blockedTotal: Int = 0,
) {
    val effectiveBlockAdsEnabled: Boolean
        get() = if (siteManaged) siteBlockAdsEnabled else globalBlockAdsEnabled

    val effectiveTrackingProtectionEnabled: Boolean
        get() = if (siteManaged) siteTrackingProtectionEnabled else globalTrackingProtectionEnabled
}

internal object SecurityStateHolder {

    internal data class SiteProtection(
        val blockAds: Boolean = true,
        val trackingProtection: Boolean = true,
    val downloadProtection: Boolean = true,
    val webrtcDisabled: Boolean = false,
    val blockRedirects: Boolean = false,
    )

    var info by mutableStateOf(SecurityInfo())

    var globalBlockAdsEnabled by mutableStateOf(true)
    var globalTrackingProtectionEnabled by mutableStateOf(true)

    var trackingLevel by mutableIntStateOf(1)
        private set
    var whitelistVersion by mutableIntStateOf(0)
        private set

    private val siteProtectionMap =
        java.util.concurrent.ConcurrentHashMap<String, SiteProtection>()

    private var prefsRef: SharedPreferences? = null

    fun attachPrefs(prefs: SharedPreferences) {
        if (prefsRef === prefs) return
        prefsRef = prefs
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val p = prefsRef ?: return
        globalBlockAdsEnabled = p.getBoolean("web_block_ads_global", true)
        globalTrackingProtectionEnabled = p.getBoolean("web_tracking_protection_global", true)
        trackingLevel = p.getInt("web_tracking_level", 1).coerceIn(0, 2)
        try {
            val raw = p.getString("web_site_protection", null)
            if (!raw.isNullOrBlank()) {
                val json = JSONObject(raw)
                siteProtectionMap.clear()
                json.keys().forEach { host ->
                    val o = json.optJSONObject(host) ?: return@forEach
                    siteProtectionMap[host] = SiteProtection(
                        blockAds = o.optBoolean("ads", true),
                        trackingProtection = o.optBoolean("tracking", true),
                        downloadProtection = o.optBoolean("download", true),
                        webrtcDisabled = o.optBoolean("webrtc", false),
                        blockRedirects = o.optBoolean("redirects", false),
                    )
                }
            }
        } catch (_: Exception) {}
    }

    private fun persistSiteProtection() {
        try {
            val json = JSONObject()
            siteProtectionMap.forEach { (host, sp) ->
                json.put(host, JSONObject().apply {
                    put("ads", sp.blockAds)
                    put("tracking", sp.trackingProtection)
            put("download", sp.downloadProtection)
            put("webrtc", sp.webrtcDisabled)
            put("redirects", sp.blockRedirects)
                })
            }
            prefsRef?.edit()?.putString("web_site_protection", json.toString())?.apply()
        } catch (_: Exception) {}
    }

    private fun getSite(host: String): SiteProtection {
        val base = getBaseDomain(host)
        return siteProtectionMap[base] ?: SiteProtection()
    }

    fun setSiteBlockAds(host: String, enabled: Boolean) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        val cur = siteProtectionMap[base] ?: SiteProtection()
        siteProtectionMap[base] = cur.copy(blockAds = enabled)
        persistSiteProtection()
        whitelistVersion++
        info = info.copy(siteManaged = true, siteBlockAdsEnabled = enabled)
    }

    fun setSiteTrackingProtection(host: String, enabled: Boolean) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        val cur = siteProtectionMap[base] ?: SiteProtection()
        siteProtectionMap[base] = cur.copy(trackingProtection = enabled)
        persistSiteProtection()
        whitelistVersion++
        info = info.copy(siteManaged = true, siteTrackingProtectionEnabled = enabled)
        whitelistVersion++
    }

    fun updateTrackingLevel(level: Int) {
        trackingLevel = level.coerceIn(0, 2)
        prefsRef?.edit()?.putInt("web_tracking_level", trackingLevel)?.apply()
    }

    /** 所有被单独改动过设置的站点（含广告、跟踪等任意一项与默认不同） */
    fun getAllSiteProtections(): Map<String, SiteProtection> = siteProtectionMap.toMap()

    fun getSiteProtection(host: String): SiteProtection =
        siteProtectionMap[getBaseDomain(host)] ?: SiteProtection()

    fun getTrackingWhitelist(): List<String> {
        @Suppress("UNUSED_VARIABLE")
        val v = whitelistVersion
        return siteProtectionMap
            .filter { it.value.trackingProtection == false }
            .keys
            .toList()
            .sorted()
    }

    private val managedSites = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun addManagedSite(host: String) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        whitelistVersion++
    }

    fun removeManagedSite(host: String) {
        val base = getBaseDomain(host)
        managedSites.remove(base)
        siteProtectionMap.remove(base)
        persistSiteProtection()
        whitelistVersion++
    }

    fun getManagedSites(): List<String> =
        (managedSites + siteProtectionMap
            .filter {
                !it.value.blockAds || !it.value.trackingProtection ||
                    !it.value.downloadProtection || it.value.webrtcDisabled || it.value.blockRedirects
            }
            .keys)
            .sorted()

    fun isManagedSite(host: String): Boolean {
        val base = getBaseDomain(host)
        if (base.isBlank()) return false
        if (managedSites.contains(base)) return true
        val sp = siteProtectionMap[base] ?: return false
        return !sp.blockAds || !sp.trackingProtection || !sp.downloadProtection ||
            sp.webrtcDisabled || sp.blockRedirects
    }

    fun effectiveDownloadProtection(host: String): Boolean {
        val base = getBaseDomain(host)
        val global = prefsRef?.getBoolean("web_download_protection", true) ?: true
        return if (base.isNotBlank() && isManagedSite(base)) getSiteProtection(base).downloadProtection else global
    }

    fun effectiveWebrtc(host: String): Boolean {
        val base = getBaseDomain(host)
        val global = prefsRef?.getBoolean("web_disable_webrtc", false) ?: false
        return if (base.isNotBlank() && isManagedSite(base)) getSiteProtection(base).webrtcDisabled else global
    }

    fun effectiveRedirects(host: String): Boolean {
        val base = getBaseDomain(host)
        val global = prefsRef?.getBoolean("web_block_redirects", false) ?: false
        return if (base.isNotBlank() && isManagedSite(base)) getSiteProtection(base).blockRedirects else global
    }

    fun setSiteWebrtc(host: String, enabled: Boolean) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        val cur = siteProtectionMap[base] ?: SiteProtection()
        siteProtectionMap[base] = cur.copy(webrtcDisabled = enabled)
        persistSiteProtection()
        whitelistVersion++
    }

    fun setSiteBlockRedirects(host: String, enabled: Boolean) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        val cur = siteProtectionMap[base] ?: SiteProtection()
        siteProtectionMap[base] = cur.copy(blockRedirects = enabled)
        persistSiteProtection()
        whitelistVersion++
    }

    fun setSiteDownloadProtection(host: String, enabled: Boolean) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        managedSites.add(base)
        val cur = siteProtectionMap[base] ?: SiteProtection()
        siteProtectionMap[base] = cur.copy(downloadProtection = enabled)
        persistSiteProtection()
        whitelistVersion++
    }

    fun getDownloadWhitelist(): List<String> = siteProtectionMap
        .filter { it.value.downloadProtection == false }
        .keys.sorted()

    fun addToDownloadWhitelist(host: String) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        val cur = siteProtectionMap[base] ?: SiteProtection()
        if (cur.downloadProtection == false) return
        siteProtectionMap[base] = cur.copy(downloadProtection = false)
        persistSiteProtection()
        whitelistVersion++
    }

    fun removeFromDownloadWhitelist(host: String) {
        val base = getBaseDomain(host)
        val cur = siteProtectionMap[base] ?: return
        siteProtectionMap[base] = cur.copy(downloadProtection = true)
        persistSiteProtection()
        whitelistVersion++
    }

    fun getAdWhitelist(): List<String> = siteProtectionMap
        .filter { it.value.blockAds == false }
        .keys.sorted()

    fun addToAdWhitelist(host: String) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        setSiteBlockAds(base, false)
    }

    fun removeFromAdWhitelist(host: String) {
        val base = getBaseDomain(host)
        setSiteBlockAds(base, true)
    }

    fun addToTrackingWhitelist(host: String) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        val cur = siteProtectionMap[base] ?: SiteProtection()
        if (cur.trackingProtection == false) return
        siteProtectionMap[base] = cur.copy(trackingProtection = false)
        persistSiteProtection()
        whitelistVersion++
    }

    fun removeFromTrackingWhitelist(host: String) {
        val base = getBaseDomain(host)
        if (base.isBlank()) return
        val cur = siteProtectionMap[base] ?: return
        if (cur.trackingProtection == true) return
        siteProtectionMap[base] = cur.copy(trackingProtection = true)
        persistSiteProtection()
        whitelistVersion++
    }

    fun reset(url: String) {
        val isHttps = url.startsWith("https://", ignoreCase = true)
        val host = try { Uri.parse(url).host ?: "" } catch (_: Exception) { "" }
        val base = getBaseDomain(host)
        val site = siteProtectionMap[base] ?: SiteProtection()
        info = SecurityInfo(
            url = url,
            host = host,
            isHttps = isHttps,
            siteManaged = isManagedSite(host),
            siteBlockAdsEnabled = site.blockAds,
            siteTrackingProtectionEnabled = site.trackingProtection,
            globalBlockAdsEnabled = globalBlockAdsEnabled,
            globalTrackingProtectionEnabled = globalTrackingProtectionEnabled,
        )
    }

    fun updateCertInfo(
        issuer: String,
        subject: String,
        validFrom: String,
        validTo: String,
        isExpired: Boolean,
    ) {
        info = info.copy(
            certIssuer = issuer,
            certSubject = subject,
            certValidFrom = validFrom,
            certValidTo = validTo,
            isCertExpired = isExpired,
            hasSslError = isExpired,
        )
    }

    fun markSslError(errorText: String) {
        info = info.copy(
            hasSslError = true,
            sslErrorText = errorText,
        )
    }

    fun onTrackerBlocked(url: String, category: String) {
        info = info.copy(
            blockedAds = if (category == "ad") info.blockedAds + 1 else info.blockedAds,
            blockedTrackers = if (category != "ad") info.blockedTrackers + 1 else info.blockedTrackers,
            blockedTotal = info.blockedTotal + 1,
        )
        try {
            val ctx = MoteAppContextHolder.appContext ?: return
            val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                .format(java.util.Date())
            val key = if (category == "ad") {
                "blocker_daily_${today}_ads"
            } else {
                "blocker_daily_${today}_trackers"
            }
            prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
        } catch (_: Exception) {}
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

// ============================================================
// JS 脚本
// ============================================================

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

// 分别读取网页顶部 / 底部区域颜色：
// 顶部优先 meta[name=theme-color]；顶部和底部都优先采样对应视口区域元素背景，
// 最后回退 body/html 背景色。返回 "#rrggbb|#rrggbb"，无法识别的一侧为空。
private val JS_DETECT_PAGE_THEME_COLOR = """
(function(){
  try {
    function parseColor(css) {
      if (!css) return null;
      css = String(css).trim();
      var m = css.match(/^rgba?\(([^)]+)\)\s*$/i);
      if (m) {
        var p = m[1].split(',').map(function(x){ return parseFloat(x); });
        if (p.length >= 3 && !isNaN(p[0]) && !isNaN(p[1]) && !isNaN(p[2])) {
          var a = p.length > 3 ? p[3] : 1;
          if (isNaN(a) || a <= 0.03) return null;
          return [p[0], p[1], p[2], a];
        }
      }
      m = css.match(/^#([0-9a-f]{3,8})$/i);
      if (m) {
        var h = m[1];
        if (h.length === 3 || h.length === 4) {
          h = h.split('').map(function(c){ return c + c; }).join('');
        }
        if (h.length === 6 || h.length === 8) {
          var a2 = h.length === 8 ? parseInt(h.substring(6, 8), 16) / 255 : 1;
          if (a2 <= 0.03) return null;
          return [parseInt(h.substring(0, 2), 16),
                  parseInt(h.substring(2, 4), 16),
                  parseInt(h.substring(4, 6), 16), a2];
        }
      }
      return null;
    }
    function toHex(c) {
      if (!c) return '';
      var a = c[3];
      var r = Math.round(c[0] * a + 255 * (1 - a));
      var g = Math.round(c[1] * a + 255 * (1 - a));
      var b = Math.round(c[2] * a + 255 * (1 - a));
      return '#' + [r, g, b].map(function(v){
        v = Math.max(0, Math.min(255, v));
        return ('0' + v.toString(16)).slice(-2);
      }).join('');
    }
    function effectiveBg(el) {
      while (el && el !== document.body && el !== document.documentElement) {
        var c = parseColor(getComputedStyle(el).backgroundColor);
        if (c) return c;
        el = el.parentElement;
      }
      return null;
    }
    function sampleAt(yRatio) {
      try {
        var node = document.elementFromPoint(
          Math.floor(window.innerWidth * 0.5),
          Math.floor(window.innerHeight * yRatio)
        );
        return effectiveBg(node);
      } catch (e) { return null; }
    }
    var bodyColor = null;
    try {
      var bodyEl = document.body;
      bodyColor = bodyEl ? parseColor(getComputedStyle(bodyEl).backgroundColor) : null;
      if (!bodyColor) {
        bodyEl = document.documentElement;
        bodyColor = bodyEl ? parseColor(getComputedStyle(bodyEl).backgroundColor) : null;
      }
    } catch (e) {}

    var topColor = null;
    var metaFallback = null;
    var metas = document.querySelectorAll('meta[name="theme-color"]');
    for (var i = 0; i < metas.length; i++) {
      var content = metas[i].content;
      if (!content) continue;
      var metaColor = parseColor(content);
      if (!metaColor) continue;
      var media = metas[i].getAttribute('media');
      if (media) {
        try {
          if (!window.matchMedia(media).matches) {
            if (!metaFallback) metaFallback = metaColor;
            continue;
          }
        } catch (e) {}
      }
      topColor = metaColor;
      break;
    }
    if (!topColor) topColor = metaFallback;
    if (!topColor) {
      var topPoints = [0.02, 0.12, 0.25];
      for (var t = 0; t < topPoints.length; t++) {
        var tc = sampleAt(topPoints[t]);
        if (tc) { topColor = tc; break; }
      }
    }
    if (!topColor) topColor = bodyColor;

    var bottomColor = null;
    var bottomPoints = [0.98, 0.88, 0.75];
    for (var b = 0; b < bottomPoints.length; b++) {
      var bc = sampleAt(bottomPoints[b]);
      if (bc) { bottomColor = bc; break; }
    }
    if (!bottomColor) bottomColor = bodyColor;
    if (!bottomColor) bottomColor = topColor;

    return toHex(topColor) + '|' + toHex(bottomColor);
  } catch (e) { return ''; }
})();
""".trimIndent()

private fun parsePageThemeColors(raw: String?): Pair<Int?, Int?> {
    if (raw.isNullOrBlank()) return null to null
    val value = raw.trim().trim('"').trim()
    if (value.isEmpty()) return null to null

    fun parseHex(part: String?): Int? {
        val hex = part?.trim().orEmpty()
        if (hex.length != 7 || !hex.startsWith("#")) return null
        return try {
            (0xFF000000L or hex.substring(1).toLong(16)).toInt()
        } catch (_: Exception) {
            null
        }
    }

    val parts = value.split("|", limit = 2)
    return parseHex(parts.getOrNull(0)) to parseHex(parts.getOrNull(1))
}


private val JS_CONTEXTMENU_TRACKER = """
(function(){
  if (window.__mote_lp_hooked) return;
  window.__mote_lp_hooked = true;
  window.__mote_lp_x = -1;
  window.__mote_lp_y = -1;
  document.addEventListener('contextmenu', function(e){
    try {
      window.__mote_lp_x = e.clientX;
      window.__mote_lp_y = e.clientY;
    } catch(err) {}
  }, true);
  document.addEventListener('touchstart', function(e){
    try {
      if (e.touches && e.touches.length === 1) {
        window.__mote_lp_x = e.touches[0].clientX;
        window.__mote_lp_y = e.touches[0].clientY;
      }
    } catch(err) {}
  }, { passive: true, capture: true });
})();
""".trimIndent()

/** 禁用 WebRTC：覆盖 RTCPeerConnection，避免网页通过 ICE 收集本机 IP */
/** 剪贴板 JS 钩子：接管 navigator.clipboard 的读写 */
/** 禁止网页自动播放视频：覆盖 HTMLMediaElement.play */
private val JS_BLOCK_VIDEO_AUTOPLAY = """
(function() {
  try {
    var orig = HTMLMediaElement.prototype.play;
    HTMLMediaElement.prototype.play = function() {
      try { this.pause && this.pause(); } catch (e) {}
      return Promise.reject(new DOMException('Autoplay blocked by Mote', 'NotAllowedError'));
    };
  } catch (e) {}
})();
"""

/** 记录 document.cookie 写入，用于自行维护 Cookie 属性 */
private val JS_COOKIE_HOOK = """
(function() {
  try {
    var desc = Object.getOwnPropertyDescriptor(Document.prototype, 'cookie');
    if (!desc || !desc.set || desc.set.__mote) return;
    var origSet = desc.set;
    var wrapped = function(v) {
      try { MoteCookie.set(String(v), location.host); } catch (e) {}
      return origSet.call(this, v);
    };
    wrapped.__mote = true;
    Object.defineProperty(Document.prototype, 'cookie', {
      get: function() { return desc.get.call(this); },
      set: wrapped,
      configurable: true,
    });
  } catch (e) {}
})();
"""

private val JS_CLIPBOARD_HOOK = """
(function() {
  try {
    if (!navigator.clipboard) return;
    window.__moteClipRes = null;
    window.__moteClipRej = null;
    window.__moteClipResult = function(ok, data) {
      try {
        if (ok) { window.__moteClipRes && window.__moteClipRes(data || ''); }
        else { window.__moteClipRej && window.__moteClipRej(new Error('denied')); }
      } catch (e) {}
    };
    navigator.clipboard.readText = function() {
      return new Promise(function(res, rej) {
        window.__moteClipRes = res; window.__moteClipRej = rej;
        try { MotePermission.request('read', ''); } catch (e) { rej(e); }
      });
    };
    navigator.clipboard.writeText = function(t) {
      return new Promise(function(res, rej) {
        window.__moteClipRes = res; window.__moteClipRej = rej;
        try { MotePermission.request('write', String(t == null ? '' : t)); } catch (e) { rej(e); }
      });
    };
  } catch (e) {}
})();
"""

private val JS_DISABLE_WEBRTC = """
(function() {
  try {
    var block = function() { throw new Error('WebRTC disabled by Mote'); };
    try { Object.defineProperty(window, 'RTCPeerConnection', { value: block, writable: false }); } catch (e) {}
    try { Object.defineProperty(window, 'webkitRTCPeerConnection', { value: block, writable: false }); } catch (e) {}
    try { Object.defineProperty(window, 'mozRTCPeerConnection', { value: block, writable: false }); } catch (e) {}
    try { if (navigator.mediaDevices) { navigator.mediaDevices.getUserMedia = block; } } catch (e) {}
    try { if (window.RTCDataChannel) { window.RTCDataChannel = block; } } catch (e) {}
  } catch (e) {}
})();
"""

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

// 统一手势守卫：方向锁定 + 滚动容器边界检测。
// 在 touchstart 时根据起点坐标检测当前位置是否允许触发原生下拉刷新 / 左右切页，
// 并在 touchmove 早期锁定手势方向，避免后续手指轻微抖动导致方向误判。
private val JS_GESTURE_GUARD = """
(function(){
  if (window.__mote_gesture_guard__) return;
  window.__mote_gesture_guard__ = true;

  var DIRECTION_THRESHOLD = 6; // 方向判定阈值（px）

  var touchStartX = 0, touchStartY = 0;
  var lockedDirection = null; // 'horizontal' | 'vertical' | null

    function isScrollableX(node) {
    var ox = window.getComputedStyle(node).overflowX;
    if ((ox === 'auto' || ox === 'scroll' || ox === 'overlay') && node.scrollWidth > node.clientWidth + 2) {
      return true;
    }
    // ★ 识别图片放大后的拖拽行为（解决百度图片横滑冲突）
    var transform = window.getComputedStyle(node).transform;
    if (transform && transform !== 'none' && transform !== 'matrix(1, 0, 0, 1, 0, 0)') {
      if (node.tagName === 'IMG' || node.tagName === 'CANVAS' || (node.querySelector && node.querySelector('img'))) {
        return true;
      }
    }
    return false;
  }

  function isScrollableY(node) {
    var oy = window.getComputedStyle(node).overflowY;
    if (oy !== 'auto' && oy !== 'scroll' && oy !== 'overlay') return false;
    return node.scrollHeight > node.clientHeight + 2;
  }

  function isAtXBoundary(node, dir) {
    if (dir === 'right') return node.scrollLeft <= 2;
    return node.scrollLeft + node.clientWidth >= node.scrollWidth - 2;
  }

  function isAtYBoundary(node) {
    return node.scrollTop <= 2;
  }

  function notify(name, allowed) {
    try {
      if (window.moteBridge && window.moteBridge[name]) {
        window.moteBridge[name](!!allowed);
      }
    } catch(e) {}
  }

  function notifyAll(allowed) {
    notify('setPullRefreshAllowed', allowed);
    notify('setSwipeNavAllowed', allowed);
  }

      function checkAtStart(x, y) {
    var doc = document.documentElement;
    var body = document.body;

    var topScroll = Math.max(
      window.pageYOffset || 0,
      (doc && doc.scrollTop) || 0,
      (body && body.scrollTop) || 0
    );
    
    if (topScroll > 2) {
      notify('setPullRefreshAllowed', false);
    } else {
      var el = document.elementFromPoint(x, y);
      var hasScrollableY = false;
      if (el) {
        var node = el;
        var depth = 0;
        while (node && node !== body && node !== doc && depth < 60) {
          if (isScrollableY(node)) {
            hasScrollableY = true;
            break;
          }
          node = node.parentElement;
          depth++;
        }
      }
      
      var docHeight = Math.max(doc ? doc.scrollHeight : 0, body ? body.scrollHeight : 0);
      var winHeight = window.innerHeight || (doc ? doc.clientHeight : 0);
      
      // ★ 精确判断：如果没有内部滚动区域，且整个文档刚好一屏，说明是全屏 H5，禁止触发原生下拉
      // 这样不会误杀包含内部滚动容器的正常 SPA
      if (!hasScrollableY && docHeight <= winHeight + 10) {
        notify('setPullRefreshAllowed', false);
      } else {
        notify('setPullRefreshAllowed', true);
      }
    }

    var el = document.elementFromPoint(x, y);
    if (!el) {
      notify('setSwipeNavAllowed', true);
      return;
    }

    var hasScrollableX = false;
    var node = el;
    var depth = 0;
    while (node && node !== body && node !== doc && depth < 60) {
      if (isScrollableX(node)) {
        hasScrollableX = true;
        break;
      }
      node = node.parentElement;
      depth++;
    }
    if (hasScrollableX) {
      notify('setSwipeNavAllowed', false);
    } else {
      notify('setSwipeNavAllowed', true);
    }
  }

  document.addEventListener('touchstart', function(e){
    if (!e.touches || e.touches.length !== 1) return;
    var t = e.touches[0];
    touchStartX = t.clientX;
    touchStartY = t.clientY;
    lockedDirection = null;
    checkAtStart(t.clientX, t.clientY);
  }, { passive: true, capture: true });

  document.addEventListener('touchmove', function(e){
    if (!e.touches || e.touches.length !== 1) return;
    var t = e.touches[0];
    var dx = t.clientX - touchStartX;
    var dy = t.clientY - touchStartY;

    if (!lockedDirection) {
      if (Math.abs(dx) > DIRECTION_THRESHOLD || Math.abs(dy) > DIRECTION_THRESHOLD) {
        lockedDirection = Math.abs(dx) >= Math.abs(dy) ? 'horizontal' : 'vertical';
      }
      return;
    }

    if (lockedDirection === 'horizontal') {
      notify('setPullRefreshAllowed', false);
      notify('setSwipeNavAllowed', true);
      return;
    }

    if (lockedDirection === 'vertical') {
      notify('setSwipeNavAllowed', false);

      var el = document.elementFromPoint(t.clientX, t.clientY);
      if (!el) {
        notify('setPullRefreshAllowed', true);
        return;
      }

      var doc = document.documentElement;
      var body = document.body;
      var hasScrollableY = false;
      var node = el;
      var depth = 0;
      while (node && node !== body && node !== doc && depth < 60) {
        if (isScrollableY(node) && !isAtYBoundary(node)) {
          hasScrollableY = true;
          break;
        }
        node = node.parentElement;
        depth++;
      }
      if (hasScrollableY) {
        notify('setPullRefreshAllowed', false);
      } else {
        notify('setPullRefreshAllowed', true);
      }
    }
  }, { passive: true, capture: true });

  document.addEventListener('touchend', function(){
    lockedDirection = null;
    notifyAll(true);
  }, { passive: true, capture: true });

  document.addEventListener('touchcancel', function(){
    lockedDirection = null;
    notifyAll(true);
  }, { passive: true, capture: true });
})();
""".trimIndent()

private val JS_PASSWORD_DETECT = """
(function(){
    if (window.__mote_pwd_hooked) return;
    window.__mote_pwd_hooked = true;

    var lastReport = { url: '', username: '', password: '', time: 0 };

    function findPasswordAndUsername() {
        try {
            var pwdInputs = document.querySelectorAll('input[type="password"]');
            if (!pwdInputs || pwdInputs.length === 0) return null;

            var pwdInput = null;
            var pwdValue = '';
            for (var i = 0; i < pwdInputs.length; i++) {
                var v = pwdInputs[i].value || '';
                if (v.length > 0) {
                    pwdInput = pwdInputs[i];
                    pwdValue = v;
                    break;
                }
            }
            if (!pwdInput || !pwdValue) return null;

            var form = pwdInput.form;
            var username = '';
            var candidates;
            if (form) {
                candidates = form.querySelectorAll(
                    'input[type="text"],input[type="email"],input[type="tel"],input:not([type])'
                );
            } else {
                candidates = document.querySelectorAll(
                    'input[type="text"],input[type="email"],input[type="tel"]'
                );
            }
            for (var j = 0; j < candidates.length; j++) {
                var c = candidates[j];
                if (c === pwdInput) continue;
                var cv = c.value || '';
                if (cv.length > 0) {
                    username = cv;
                    break;
                }
            }

            return { username: username, password: pwdValue };
        } catch (e) { return null; }
    }

    function reportIfNeeded() {
        try {
            var result = findPasswordAndUsername();
            if (!result || !result.password || result.password.length === 0) return;

            var now = Date.now();
            if (
                lastReport.url === location.href &&
                lastReport.username === result.username &&
                lastReport.password === result.password &&
                now - lastReport.time < 60000
            ) return;

            lastReport.url = location.href;
            lastReport.username = result.username || '';
            lastReport.password = result.password;
            lastReport.time = now;

            if (window.moteBridge && window.moteBridge.onPasswordSubmit) {
                window.moteBridge.onPasswordSubmit(
                    location.href,
                    result.username || '',
                    result.password
                );
            }
        } catch (e) {}
    }

    document.addEventListener('submit', function(e) {
        try { reportIfNeeded(); } catch (err) {}
    }, true);

    document.addEventListener('click', function(e) {
        try {
            var t = e.target;
            if (!t) return;
            var tag = t.tagName;
            if (tag !== 'BUTTON' && tag !== 'INPUT' && tag !== 'A') return;
            var type = (t.getAttribute('type') || '').toLowerCase();
            var text = (t.innerText || t.value || '').trim().toLowerCase();
            var isSubmitLike =
                type === 'submit' ||
                (type === 'button' && (
                    text.indexOf('登录') >= 0 || text.indexOf('登陆') >= 0 ||
                    text.indexOf('login') >= 0 || text.indexOf('sign in') >= 0 ||
                    text.indexOf('signin') >= 0 || text.indexOf('提交') >= 0 ||
                    text.indexOf('确定') >= 0 || text.indexOf('确认') >= 0 ||
                    text.indexOf('搜索') >= 0 || text.indexOf('search') >= 0
                )) ||
                (text.indexOf('登录') >= 0 && text.length < 10) ||
                (text.indexOf('login') >= 0 && text.length < 20);
            if (isSubmitLike) {
                setTimeout(reportIfNeeded, 150);
            }
        } catch (err) {}
    }, true);
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
    'img,video,picture,canvas,svg,input[type="image"]{' +
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

// Dark Reader 风格动态主题引擎：遍历 DOM，按色相保留 + 亮度反转生成覆盖式深色 CSS。
// 能处理白色横幅、卡片、未适配 prefers-color-scheme 的页面，效果远好于简单 invert。
// 刷新策略：不再使用 MutationObserver（会造成 style 反复重建、页面闪白/闪灰），
// 改为固定时机刷新（立即 / DOMContentLoaded / load / 若干延迟兜底）。
// 关闭时调用 engine.stop() 清理所有 pending setTimeout，避免「关闭后又变回深色」。
// dim = true 时，html/body 底色更深，且所有反转后的背景色额外乘以暗度系数。
private fun buildDarkReaderScript(dim: Boolean): String {
    val htmlBg = if (dim) "#080808" else "#111111"
    val bodyBg = if (dim) "#0d0d0d" else "#1a1a1a"
    val dimFactor = if (dim) "0.72" else "1.0"
    return """
(function(){
  if (window.__mote_dark_engine__) {
    try { window.__mote_dark_engine__.refresh(); } catch(e) {}
    return;
  }

  var DIM_FACTOR = $dimFactor;

  function parseColor(str) {
    if (!str) return null;
    str = String(str).trim();
    if (str === 'transparent' || str === 'rgba(0, 0, 0, 0)' || str === 'rgba(0,0,0,0)') return null;
    var m = str.match(/^rgba?\(\s*([\d.]+)[,\s]+([\d.]+)[,\s]+([\d.]+)(?:[,\s\/]+([\d.]+))?/);
    if (m) {
      var a = m[4] === undefined ? 1 : parseFloat(m[4]);
      if (a < 0.05) return null;
      return { r: +m[1], g: +m[2], b: +m[3], a: a };
    }
    return null;
  }

  function luminance(c) {
    return (0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b) / 255;
  }

  function rgbToHsl(r, g, b) {
    r /= 255; g /= 255; b /= 255;
    var max = Math.max(r, g, b), min = Math.min(r, g, b);
    var h, s, l = (max + min) / 2;
    if (max === min) { h = s = 0; }
    else {
      var d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      if (max === r) h = (g - b) / d + (g < b ? 6 : 0);
      else if (max === g) h = (b - r) / d + 2;
      else h = (r - g) / d + 4;
      h /= 6;
    }
    return [h, s, l];
  }

  function hslToRgb(h, s, l) {
    var r, g, b;
    if (s === 0) { r = g = b = l; }
    else {
      function hue2rgb(p, q, t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1/6) return p + (q - p) * 6 * t;
        if (t < 1/2) return q;
        if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
        return p;
      }
      var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      var p = 2 * l - q;
      r = hue2rgb(p, q, h + 1/3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1/3);
    }
    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
  }

  function invertColor(c) {
    var hsl = rgbToHsl(c.r, c.g, c.b);
    var h = hsl[0], s = hsl[1], l = hsl[2];
    var newS = Math.min(1, s * 0.9);
    var newL = 1 - l;
    if (newL < 0.05) newL = 0.05;
    if (newL > 0.95) newL = 0.95;
    var rgb = hslToRgb(h, newS, newL);
    return { r: rgb[0], g: rgb[1], b: rgb[2], a: c.a };
  }

  function toRgba(c) {
    if (c.a >= 0.99) return 'rgb(' + c.r + ',' + c.g + ',' + c.b + ')';
    return 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',' + c.a + ')';
  }

  function clearMarks() {
    try {
      var old = document.querySelectorAll('[data-mote-dr]');
      for (var i = 0; i < old.length; i++) {
        old[i].removeAttribute('data-mote-dr');
      }
    } catch(e) {}
  }

  function buildStyle() {
    var parts = [];
    parts.push('*,*::before,*::after{transition:none !important;}');
    parts.push('html{background-color:$htmlBg !important;color-scheme:dark !important;}');
    parts.push('body{background-color:$bodyBg !important;color:#dcdcdc !important;}');

    var els = document.querySelectorAll('*');
    var count = 0;
    for (var i = 0; i < els.length && count < 3000; i++) {
      var el = els[i];
      var tag = el.tagName;
      if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'LINK' ||
          tag === 'META' || tag === 'TITLE' || tag === 'HEAD') continue;

      var style;
      try { style = window.getComputedStyle(el); } catch(e) { continue; }

      var bg = parseColor(style.backgroundColor);
      var fg = parseColor(style.color);
      var bgImage = style.backgroundImage;

      var rules = [];

      if (bg && luminance(bg) > 0.35) {
        var newBg = invertColor(bg);
        if (newBg.a >= 0.99) {
          var bl = luminance(newBg);
          if (bl < 0.08) {
            newBg.r = Math.min(255, newBg.r + 20);
            newBg.g = Math.min(255, newBg.g + 20);
            newBg.b = Math.min(255, newBg.b + 20);
          }
        }
        if (DIM_FACTOR < 1.0) {
          newBg.r = Math.round(newBg.r * DIM_FACTOR);
          newBg.g = Math.round(newBg.g * DIM_FACTOR);
          newBg.b = Math.round(newBg.b * DIM_FACTOR);
        }
        rules.push('background-color:' + toRgba(newBg) + ' !important');
      }

      if (fg && luminance(fg) < 0.45) {
        var newFg = invertColor(fg);
        var fl = luminance(newFg);
        if (fl < 0.6) {
          newFg.r = Math.min(255, Math.round(newFg.r + 40));
          newFg.g = Math.min(255, Math.round(newFg.g + 40));
          newFg.b = Math.min(255, Math.round(newFg.b + 40));
        }
        rules.push('color:' + toRgba(newFg) + ' !important');
      }

      var borderColor = parseColor(style.borderTopColor);
      if (borderColor && luminance(borderColor) > 0.5) {
        var nb = invertColor(borderColor);
        rules.push('border-color:' + toRgba(nb) + ' !important');
      }

      if (bgImage && bgImage !== 'none') {
        rules.push('background-blend-mode:multiply !important');
      }

      if (rules.length > 0) {
        var id = '__mote_dr_' + count;
        el.setAttribute('data-mote-dr', id);
        parts.push('[data-mote-dr="' + id + '"]{' + rules.join(';') + '}');
        count++;
      }
    }

    parts.push('img,video,picture,canvas,svg,[style*="background-image"],iframe{filter:none !important;}');
    return parts.join('\n');
  }

  function ensureStyleEl() {
    var style = document.getElementById('__mote_dark_style__');
    if (!style) {
      style = document.createElement('style');
      style.id = '__mote_dark_style__';
      (document.head || document.documentElement).appendChild(style);
    }
    return style;
  }

  function refresh() {
    try {
      clearMarks();
      var css = buildStyle();
      ensureStyleEl().textContent = css;
    } catch(e) {}
  }

  var scheduledTimeouts = [];
  function scheduleRefresh(delayMs) {
    var t = setTimeout(function() {
      try { refresh(); } catch(e) {}
    }, delayMs);
    scheduledTimeouts.push(t);
  }

  function stop() {
    for (var i = 0; i < scheduledTimeouts.length; i++) {
      try { clearTimeout(scheduledTimeouts[i]); } catch(e) {}
    }
    scheduledTimeouts.length = 0;
  }

  window.__mote_dark_engine__ = { refresh: refresh, stop: stop };

  function start() {
    refresh();
    scheduleRefresh(200);
    scheduleRefresh(1000);
    scheduleRefresh(2500);
    scheduleRefresh(5000);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      try { refresh(); } catch(e) {}
    }, { once: true });
    document.addEventListener('load', function() {
      try { refresh(); } catch(e) {}
    }, { once: true });
    start();
  } else {
    start();
  }
})();
""".trimIndent()
}

private val JS_DARK_READER_OFF = """
(function(){
  try {
    var engine = window.__mote_dark_engine__;
    if (engine && engine.stop) engine.stop();
  } catch(e) {}
  try { delete window.__mote_dark_engine__; } catch(e) { window.__mote_dark_engine__ = null; }
  try {
    var s = document.getElementById('__mote_dark_style__');
    if (s) s.remove();
    var els = document.querySelectorAll('[data-mote-dr]');
    for (var i = 0; i < els.length; i++) {
      els[i].removeAttribute('data-mote-dr');
    }
  } catch(e) {}
})();
""".trimIndent()

/**
 * WebView 是否支持原生算法暗色（效果远好于手动 invert）。
 * 现代网页大多声明了 prefers-color-scheme，原生算法暗色会直接触发
 * 网页自身的深色主题，不会出现「半黑半白」「白横幅」这类问题。
 */
private fun isNativeDarkModeSupported(): Boolean {
    return try {
        WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)
    } catch (_: Exception) { false }
}

// 管理每个 WebView 的 DocumentStartScript 句柄。
// DocumentStartScript 在页面 HTML 开始解析的最早期注入 Dark Reader，
// 彻底消除「先浅后深」的白闪；WebView 被 GC 后 WeakHashMap 自动回收。
// 同时缓存注册时使用的 dim 状态，dim 变化时自动重注册并返回 true。
internal object NightModeScriptHandlers {
    private val handlers = java.util.Collections.synchronizedMap(
        java.util.WeakHashMap<android.webkit.WebView, Pair<androidx.webkit.ScriptHandler, Boolean>>()
    )

    /**
     * 确保已注册。返回 true 表示本次注册/重新注册了（调用方应重建当前页面）。
     * 返回 false 表示 dim 未变、句柄已存在，无需重建。
     */
    fun ensure(
        wv: android.webkit.WebView,
        dim: Boolean,
        provider: () -> androidx.webkit.ScriptHandler?,
    ): Boolean {
        synchronized(handlers) {
            val existing = handlers[wv]
            if (existing != null && existing.second == dim) return false
            try { existing?.first?.remove() } catch (_: Exception) {}
            val handler = try { provider() } catch (_: Exception) { null }
            if (handler == null) {
                handlers.remove(wv)
                return false
            }
            handlers[wv] = handler to dim
            return true
        }
    }

    fun remove(wv: android.webkit.WebView) {
        synchronized(handlers) {
            try {
                handlers.remove(wv)?.first?.remove()
            } catch (_: Exception) {}
        }
    }
}

/**
 * 给单个 WebView 应用夜间模式。
 * 优先使用 Android WebView 原生算法暗色（跟随 CSS prefers-color-scheme）；
 * 不支持时回退到 JS invert。
 */
private fun isSystemDarkMode(context: Context): Boolean {
    val uiMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
}


private fun applyNightModeToWebView(wv: WebView, enabled: Boolean) {
    val nativeSupported = isNativeDarkModeSupported()
    val systemDark = isSystemDarkMode(wv.context)

    if (!enabled) {
        NightModeScriptHandlers.remove(wv)
        try {
            if (nativeSupported) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(wv.settings, false)
            }
        } catch (_: Exception) {}
        try { wv.evaluateJavascript(JS_NIGHT_OFF, null) } catch (_: Exception) {}
        try { wv.evaluateJavascript(JS_DARK_READER_OFF, null) } catch (_: Exception) {}
        try { wv.setBackgroundColor(android.graphics.Color.WHITE) } catch (_: Exception) {}
        return
    }

    // 立刻把 WebView 底层背景设为深色，消除加载过程中的白屏
    try {
        wv.setBackgroundColor(android.graphics.Color.parseColor("#1a1a1a"))
    } catch (_: Exception) {}

    // 系统深色 + 原生支持：无条件走原生算法暗化
    if (nativeSupported && systemDark) {
        NightModeScriptHandlers.remove(wv)
        try { wv.evaluateJavascript(JS_NIGHT_OFF, null) } catch (_: Exception) {}
        try { wv.evaluateJavascript(JS_DARK_READER_OFF, null) } catch (_: Exception) {}
        try {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(wv.settings, true)
        } catch (_: Exception) {}
        return
    }

    // Dark Reader 动态主题引擎（恒传 false，全局调暗交给 UI 蒙版）
    val script = buildDarkReaderScript(false)

    val reRegistered = NightModeScriptHandlers.ensure(wv, false) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(wv, script, setOf("*"))
        } else null
    }

    if (reRegistered) {
        try { wv.evaluateJavascript(JS_DARK_READER_OFF, null) } catch (_: Exception) {}
        try { wv.evaluateJavascript(script, null) } catch (_: Exception) {}
    } else {
        try {
            wv.evaluateJavascript(
                "(function(){try{return !!window.__mote_dark_engine__;}catch(e){return false;}})()"
            ) { raw ->
                try {
                    if (raw == "true") {
                        wv.evaluateJavascript("window.__mote_dark_engine__.refresh();", null)
                    } else {
                        wv.evaluateJavascript(script, null)
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}

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
    function applyZoomUnlock() {
      var metas = document.querySelectorAll('meta[name="viewport"]');
      if (metas.length > 0) {
        for (var i = 0; i < metas.length; i++) {
          metas[i].remove();
        }
      }
      var nm = document.createElement('meta');
      nm.name = 'viewport';
      nm.content = 'width=device-width, initial-scale=1.0, minimum-scale=0.1, maximum-scale=20.0, user-scalable=yes';
      (document.head || document.documentElement).appendChild(nm);
    }

    applyZoomUnlock();

    // 针对 SPA 动态渲染：在 DOM 变化后再次确保缩放未被重新锁死
    if (window.__mote_force_zoom_observer__) {
      try { window.__mote_force_zoom_observer__.disconnect(); } catch(e) {}
    }
    var observer = new MutationObserver(function() {
      var metas = document.querySelectorAll('meta[name="viewport"]');
      for (var i = 0; i < metas.length; i++) {
        var c = (metas[i].getAttribute('content') || '').toLowerCase();
        if (c.indexOf('user-scalable=no') >= 0 || c.indexOf('user-scalable=0') >= 0 || c.indexOf('maximum-scale=1') >= 0) {
          applyZoomUnlock();
          break;
        }
      }
    });
    observer.observe(document.documentElement, { childList: true, subtree: true });
    window.__mote_force_zoom_observer__ = observer;
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

private val JS_EXTRACT_ARTICLE_TEXT = """
(function(){
  try {
    var body = document.body;
    if (!body) return '[]';
    var walker = document.createTreeWalker(body, NodeFilter.SHOW_TEXT, {
      acceptNode: function(node) {
        var parent = node.parentElement;
        if (!parent) return NodeFilter.FILTER_REJECT;
        var tag = parent.tagName;
        if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'NOSCRIPT' || tag === 'TEXTAREA' || tag === 'CODE' || tag === 'PRE') {
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
    var paragraphs = [];
    var currentText = '';
    while (walker.nextNode()) {
      var text = walker.currentNode.nodeValue.trim();
      if (text) {
        currentText += text;
        // 遇到标点符号或换行时切分，方便 TTS 朗读
        if (/[.。!！?？\n]/.test(text)) {
          paragraphs.push(currentText);
          currentText = '';
        }
      }
    }
    if (currentText) paragraphs.push(currentText);
    return JSON.stringify(paragraphs);
  } catch(e) {
    return '[]';
  }
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

private data class LongPressInfo(
    val imageUrl: String = "",
    val linkUrl: String = "",
    val text: String = "",
    val pageUrl: String = "",
    val filename: String = "",
    val xPx: Int = 0,
    val yPx: Int = 0,
    val selectable: Boolean = false,
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
    var isProgrammatic: Boolean = false,
    var isDirectOpen: Boolean = false,
    // 网页顶部/底部区域主题色（沉浸式页面使用），ARGB；null 表示尚未探测到
    var themeColor: Int? = null,
    var bottomThemeColor: Int? = null,
)

data class HistoryEntry(
    val title: String,
    val url: String,
    val timestamp: Long,
    val isClosed: Boolean = false,
    val isRemote: Boolean = false
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
    val activeTools = mutableStateListOf<String>()

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

internal fun defaultTools(): List<ToolDef> = listOf(
    ToolDef("bookmarks", "书签列表", MiuixIcons.FavoritesFill),
    ToolDef("history", "历史记录", MiuixIcons.Recent),
    ToolDef("downloads", "下载/文件", MiuixIcons.Download),
    ToolDef("refresh", "刷新网页", MiuixIcons.Refresh),
    ToolDef("night_mode", "夜间模式", MiuixIcons.Show, isToggle = true),
    ToolDef("incognito", "无痕模式", MiuixIcons.Hide, isToggle = true),
    ToolDef("desktop_mode", "电脑模式", MiuixIcons.HorizontalSplit, isToggle = true),
    ToolDef("fullscreen", "全屏模式", MiuixIcons.ExpandMore, isToggle = true),
    ToolDef("no_image", "无图模式", MiuixIcons.Photos, isToggle = true),
    ToolDef("home", "主页", MiuixIcons.Home),
    ToolDef("share", "分享网页", MiuixIcons.Share),
    ToolDef("find_in_page", "页面查找", MiuixIcons.Search),
    ToolDef("zoom", "网页缩放", MiuixIcons.VerticalSplit),
    ToolDef("translate", "网页翻译", MiuixIcons.Translate, isToggle = true),
    ToolDef("print", "打印/PDF", MiuixIcons.Rename),
    ToolDef("sniff", "资源嗅探", MiuixIcons.RecordingTape),
    ToolDef("view_source", "网页源码", MiuixIcons.Weeks),
    ToolDef("run_script", "脚本", MiuixIcons.Notes),
    ToolDef("play_in_player", "播放视频", MiuixIcons.Play),
    ToolDef("tts", "语音朗读", MiuixIcons.AppRecording),
    ToolDef("save_page", "保存网页", MiuixIcons.File),
)

@Composable
private fun WindowManagerIcon(count: Int, tint: Color, pulseTrigger: Int = 0) {
    val iconScale = remember { Animatable(1f) }
    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger > 0) {
            iconScale.snapTo(1f)
            iconScale.animateTo(
                targetValue = 1.35f,
                animationSpec = tween(160, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
            )
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(220, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
            )
        }
    }
    Box(
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = iconScale.value
                scaleY = iconScale.value
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.5.dp),
        ) {
            val cornerRadius = 5.dp.toPx()
            val strokeWidth = 1.7.dp.toPx()
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

    // 1. 明确声明是 video/audio
    if (accept != null && (accept.contains("video/", true) || accept.contains("audio/", true))) {
        return true
    }

    // 2. 带了 Range 头，要严格排查是否是伪装的大文件/API
    if (range != null) {
        val clean = request.url.toString().lowercase().substringBefore('#').substringBefore('?')
        if (clean.endsWith(".js") || clean.endsWith(".css") || clean.endsWith(".json") ||
            clean.endsWith(".html") || clean.endsWith(".htm") ||
            clean.endsWith(".jpg") || clean.endsWith(".jpeg") || clean.endsWith(".png") ||
            clean.endsWith(".gif") || clean.endsWith(".webp") || clean.endsWith(".svg") ||
            clean.endsWith(".woff") || clean.endsWith(".woff2") || clean.endsWith(".ttf")
        ) {
            return false
        }

        if (sniffCategoryFromNetwork(request.url.toString()) != null) {
            return true
        }

        val hasExt = clean.substringAfterLast('/').contains(".")
        if (!hasExt) {
            val m = Regex("bytes=(\\d+)-(\\d*)").find(range)
            if (m != null) {
                val start = m.groupValues[1].toLongOrNull() ?: 0
                if (start > 0) return true
            }
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

// ============================================================
// MainActivity
// ============================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ 方案4：尽早启动 WebView 后台预热，主线程不等待
        prewarmWebView()

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
            val prefs = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            lifecycleScope.launch(Dispatchers.IO) {
                AutoCacheCleaner.checkAndClean(applicationContext)
            }

            // ★ 方案3：数据迁移移到 IO 线程，不阻塞首帧。
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    if (!prefs.getBoolean("web_saved_zooms_cleaned_v2", false)) {
                        prefs.edit()
                            .remove("web_saved_zooms")
                            .putBoolean("web_saved_zooms_cleaned_v2", true)
                            .apply()
                    }
                    if (!prefs.getBoolean("home_banner_content_migrated_v1", false)) {
                        val oldContent = prefs.getInt("home_banner_content", 0)
                        val oldShow = prefs.getBoolean("home_show_banner", true)
                        val newContent = if (!oldShow) 0 else oldContent + 1
                        prefs.edit()
                            .putInt("home_banner_content", newContent)
                            .putBoolean("home_banner_content_migrated_v1", true)
                            .apply()
                    }
                } catch (_: Exception) {}
            }

            // ★ session_snapshot 仍需同步读取（BrowserScreen 初始化依赖它）
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
            if (intent?.getBooleanExtra("open_downloads", false) == true) {
                intent.removeExtra("open_downloads")
                PendingOpenDownloadsStore.pending = true
            }
        } catch (_: Exception) {}

        try {
            val playingUrl = PlaybackSessionHolder.currentUrl
            val isLauncherIntent = intent?.action == Intent.ACTION_MAIN &&
                    intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
            if (playingUrl.isNotBlank() && isLauncherIntent) {
                NativePlayerActivity.start(
                    context = this,
                    url = playingUrl,
                    title = PlaybackSessionHolder.currentTitle.ifBlank { "视频播放" },
                    startPositionMs = PlaybackPositionHolder.positionMs,
                )
            }
        } catch (_: Exception) {}

        setContent {
            MoteThemeWrapper {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppRoot()
                        PrivacyPolicyDialog()
                        WebPermissionDialogHost()
                    }
                }
            }
        }

        window.decorView.post {
            try { window.decorView.requestApplyInsets() } catch (_: Exception) {}
        }
    }

    // ★ 方案4：预热 WebView 的方法，加在 onCreate 方法的下方
    @androidx.annotation.OptIn(androidx.webkit.WebViewCompat.ExperimentalAsyncStartUp::class)
    @Suppress("DEPRECATION")
    private fun prewarmWebView() {
        try {
            val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
            val config = androidx.webkit.WebViewStartUpConfig.Builder(executor).build()

            // WebKit 1.17.1：使用 WebViewOutcomeReceiver 配合 WebViewStartupException
            androidx.webkit.WebViewCompat.startUpWebView(
                applicationContext,
                config,
                object : androidx.webkit.WebViewOutcomeReceiver<
                        androidx.webkit.WebViewStartUpResult,
                        androidx.webkit.WebViewStartupException> {
                    override fun onResult(result: androidx.webkit.WebViewStartUpResult) {
                        // 预热成功，无需操作
                    }

                    override fun onError(error: androidx.webkit.WebViewStartupException) {
                        // 预热失败，静默处理
                    }
                }
            )
        } catch (_: Throwable) {
            // 老版本 / 系统 WebView 缺失时静默跳过
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        try {
            val launchUrl = intent.data?.toString()
            if (!launchUrl.isNullOrBlank()) {
                // 第三方通过 VIEW 唤起时创建的是「直接打开」窗口：无历史时返回应关闭它，
                // 而不是回到新标签页（forceNewWindow=true 会让 createWindow 标记 isDirectOpen）
                PendingOpenInNewWindowStore.forceNewWindow = true
                PendingOpenInNewWindowStore.pending = launchUrl
            }
            if (intent.getBooleanExtra("open_downloads", false) == true) {
                intent.removeExtra("open_downloads")
                PendingOpenDownloadsStore.pending = true
            }
        } catch (_: Exception) {}

        try {
            val playingUrl = PlaybackSessionHolder.currentUrl
            val isLauncherIntent = intent.action == Intent.ACTION_MAIN &&
                    intent.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
            if (playingUrl.isNotBlank() && isLauncherIntent) {
                NativePlayerActivity.start(
                    context = this,
                    url = playingUrl,
                    title = PlaybackSessionHolder.currentTitle.ifBlank { "视频播放" },
                    startPositionMs = PlaybackPositionHolder.positionMs,
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

// ============================================================
// 主题包装器
// ============================================================

@Composable
internal fun MoteThemeWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var uiMode by remember {
        mutableStateOf(
            UiMode.fromValue(
                prefs.getString("ui_style", UiMode.DEFAULT_VALUE) ?: UiMode.DEFAULT_VALUE
            )
        )
    }

    var colorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_color_mode", 0).coerceIn(0, 3))
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
                "ui_style" -> uiMode = UiMode.fromValue(
                    prefs.getString(key, UiMode.DEFAULT_VALUE) ?: UiMode.DEFAULT_VALUE
                )
                "ui_color_mode" -> colorMode = prefs.getInt(key, 0).coerceIn(0, 3)
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

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isAmoled = colorMode == 3
    val isDark = when (colorMode) {
        1 -> false
        2 -> true
        3 -> true
        else -> systemDark
    }

    val currentContent by rememberUpdatedState(content)
    val movableContent = remember { movableContentOf { currentContent() } }

    CompositionLocalProvider(LocalUiMode provides uiMode) {
        when (uiMode) {
            UiMode.Miuix -> {
                val useMonet = monetEnabled
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

                if (useMonet) {
                    // 开启系统壁纸取色时：走原生 Monet 动态取色
                    val schemeMode = when (colorMode) {
                        0 -> ColorSchemeMode.MonetSystem
                        1 -> ColorSchemeMode.MonetLight
                        2, 3 -> ColorSchemeMode.MonetDark
                        else -> ColorSchemeMode.MonetSystem
                    }
                    val themeController = ThemeController(
                        colorSchemeMode = schemeMode,
                        keyColor = null,
                        isDark = isDark,
                        paletteStyle = paletteStyle,
                        colorSpec = colorSpec,
                    )
                    MiuixTheme(controller = themeController) {
                        movableContent()
                    }
                } else {
                    // 1. 先用全限定名显式获取基础颜色，防止和 Material 的同名函数混淆
                    val baseColors = if (isDark) {
                        top.yukonga.miuix.kmp.theme.darkColorScheme()
                    } else {
                        top.yukonga.miuix.kmp.theme.lightColorScheme()
                    }

                    // 2. 显式指定类型为 MIUIX 的 Colors，彻底解决 Any 类型推断报错
                    val finalScheme: top.yukonga.miuix.kmp.theme.Colors = if (keyColor != 0) {
                        val customPrimary = Color(keyColor)
                        val customOnPrimary = if (customPrimary.luminance() > 0.5f) Color.Black else Color.White
                        val customPrimaryVariant = customPrimary.copy(alpha = 0.8f)
                        val customOnPrimaryVariant = customOnPrimary.copy(alpha = 0.9f)

                        baseColors.copy(
                            primary = customPrimary,
                            onPrimary = customOnPrimary,
                            primaryVariant = customPrimaryVariant,
                            onPrimaryVariant = customOnPrimaryVariant,
                            primaryContainer = customPrimary.copy(alpha = 0.15f),
                            onPrimaryContainer = customOnPrimary
                        )
                    } else {
                        baseColors
                    }

                    // 3. 应用配色
                    MiuixTheme(colors = finalScheme) {
                        movableContent()
                    }
                }
            }

            UiMode.Material -> {
                val paletteStyle = try {
                    PaletteStyle.valueOf(paletteStyleName)
                } catch (_: Exception) {
                    PaletteStyle.TonalSpot
                }

                val baseScheme: androidx.compose.material3.ColorScheme = if (keyColor == 0) {
                    remember(isDark, context) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (isDark) dynamicDarkColorScheme(context)
                            else dynamicLightColorScheme(context)
                        } else {
                            if (isDark) darkColorScheme()
                            else lightColorScheme()
                        }
                    }
                } else {
                    rememberDynamicColorScheme(
                        seedColor = Color(keyColor),
                        isDark = isDark,
                        isAmoled = isAmoled,
                        style = paletteStyle,
                    )
                }

                val m3Scheme = remember(baseScheme, isAmoled) {
                    if (isAmoled) {
                        baseScheme.copy(
                            background = Color.Black,
                            surface = Color.Black,
                            surfaceContainerLowest = Color(0xFF000000),
                            surfaceContainerLow = Color(0xFF0A0A0A),
                            surfaceContainer = Color(0xFF121212),
                            surfaceContainerHigh = Color(0xFF1A1A1A),
                            surfaceContainerHighest = Color(0xFF222222),
                        )
                    } else baseScheme
                }

                androidx.compose.material3.MaterialTheme(
                    colorScheme = m3Scheme,
                    content = { movableContent() },
                )
            }
        }
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

    val pageStack = PageNavigationStateHolder.pageStack

    var exitingPageId by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(1f) }
    var animJob by remember { mutableStateOf<Job?>(null) }
    // ★ 返回是否已经"在飞"：手势 pop 与按钮 pop 共用同一把锁，
    //   防止两者在 exitingPageId 置位前的那几毫秒同时删栈（会多掉一层 → 再返回直接回桌面）。
    var backInFlight by remember { mutableStateOf(false) }

    // 与 miuix 官方导航对齐的时间曲线：欠阻尼弹簧阶跃（前段起速、尾段长而柔），
    // 程序化进出场 500ms；手势松手后的续接用更短的一档，避免半程继续时拖沓。
    val pushSpec = remember { tween<Float>(durationMillis = 500, easing = PageTransitionEasing) }
    val popSpec = remember { tween<Float>(durationMillis = 500, easing = PageTransitionEasing) }
    val cancelSpec = remember { tween<Float>(durationMillis = 320, easing = PageGestureSettleEasing) }

    val isSettled by remember { derivedStateOf { progress >= 1f } }

    /**
     * 立即结算正在飞行的退场动画：把正在退场的页面从栈里摘掉、复位进度与锁。
     * 用于连续返回——退场动画没播完时再按一次返回，不必等它结束。
     */
    fun settlePendingExit() {
        val exiting = exitingPageId
        animJob?.cancel()
        animJob = null
        if (exiting != null) {
            pageStack.removeAll { it.id == exiting }
        }
        exitingPageId = null
        backInFlight = false
        progress = 1f
    }

    fun pushPage(route: String) {
        // ★ 只有"当前页面且没有在退场"才拦截重复 push。
        //   退场动画期间，栈顶是正在消失的那一页，若用户点的正是它同名的入口
        //   （例如从「搜索设置」返回时立刻再点「搜索设置」），旧判断会直接 return，
        //   表现为"有触摸反馈但不跳转"。
        val topRoute = pageStack.lastOrNull()?.route
        if (exitingPageId == null && topRoute == route) return

        animJob?.cancel()
        animJob = null

        if (exitingPageId != null) {
            // ★ 按 id 移除正在退场的页面（原来按 lastIndex 删，栈变化时会删错层）
            val exitingId = exitingPageId
            pageStack.removeAll { it.id == exitingId }
            exitingPageId = null
            backInFlight = false
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
        // 上一次退场还没播完就再返回：先立即结算，再退下一层（连续返回不排队）
        if (exitingPageId != null || backInFlight) {
            settlePendingExit()
            if (pageStack.size <= 1) return
        }

        val target = pageStack.lastOrNull() ?: return

        animJob?.cancel()
        animJob = null

        backInFlight = true
        exitingPageId = target.id
        progress = 1f

        animJob = scope.launch {
            try {
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
                        // ★ 按 id 删，不按 lastIndex 删：栈在此期间变化也不会删错
                        pageStack.removeAll { it.id == target.id }
                        exitingPageId = null
                    }
                }
            } finally {
                backInFlight = false
            }
        }
    }

    // ★ 只要栈里还有页面就保持注册（不再随 exitingPageId 反注册），
    //   否则状态翻转的那一帧会有"回调空窗"，返回事件落到系统默认 → 直接 finish() 回桌面。
    PredictiveBackHandler(enabled = pageStack.size > 1 && !GlobalOverlayState.isOpen) { backEvents ->
        // 已经有一次返回在飞：先立即结算，让新手势从干净的落定状态开始
        if (exitingPageId != null || backInFlight) {
            settlePendingExit()
            if (pageStack.size <= 1) {
                backEvents.collect { }
                return@PredictiveBackHandler
            }
        }

        animJob?.cancel()
        animJob = null

        val target = pageStack.lastOrNull()
        if (target == null) {
            backEvents.collect { }
            return@PredictiveBackHandler
        }

        backInFlight = true
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
                val stillTop = pageStack.lastOrNull()?.id == target.id
                if (completed && stillTop) {
                    // ★ 关键：在进入任何挂起之前同步置位，堵住 popPage() 看到的竞态窗口
                    exitingPageId = target.id
                    withContext(NonCancellable) {
                        try {
                            // 手势松手后的收尾：用更短的手势曲线，避免从半程继续时拖沓
                            animate(
                                initialValue = progress,
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = 350,
                                    easing = PageGestureSettleEasing,
                                ),
                            ) { value, _ -> progress = value }
                        } catch (_: Exception) { }
                        if (exitingPageId == target.id) {
                            progress = 1f
                            // ★ 按 id 删，不按 lastIndex 删
                            pageStack.removeAll { it.id == target.id }
                            exitingPageId = null
                        }
                    }
                } else if (!completed) {
                    try {
                        animate(
                            initialValue = progress,
                            targetValue = 1f,
                            animationSpec = cancelSpec,
                        ) { value, _ -> progress = value }
                    } catch (_: Exception) { }
                    progress = 1f
                } else {
                    // 手势完成但栈已变化（例如期间点了返回按钮）：不重复删栈，只恢复位移
                    progress = 1f
                }
            } catch (_: Exception) {
            } finally {
                backInFlight = false
            }
        }
    }

    // 退场动画期间系统返回：不再吞掉事件，而是立即结算当前退场并继续退下一层
    BackHandler(enabled = pageStack.size > 1 && exitingPageId != null) {
        popPage()
    }

    /**
     * 从应用内页面跳到浏览器时，直接收掉所有中间页面。
     * 这类跳转不会再回到文件管理/设置页，不能依赖退场动画或 backInFlight 竞态，
     * 否则浏览器返回时会露出残留页面。
     */
    fun resetToRootImmediate() {
        val root = pageStack.firstOrNull() ?: return
        animJob?.cancel()
        animJob = null
        exitingPageId = null
        backInFlight = false
        progress = 1f
        if (pageStack.size > 1) {
            pageStack.removeAll { it.id != root.id }
        }
    }

    fun openInBrowserFromApp(url: String) {
        PendingOpenInNewWindowStore.forceNewWindow = true
        // 先让浏览器在页面栈底层准备好，再播放当前页面的退场动画，避免“闪回主页”
        PendingOpenInNewWindowStore.pending = url

        if (pageStack.size <= 1) return
        if (exitingPageId != null || backInFlight) {
            resetToRootImmediate()
            return
        }
        val topId = pageStack.last().id
        val rootId = pageStack.first().id
        if (topId == rootId) return
        popPage()
        pageStack.removeAll { it.id != rootId && it.id != topId }
        // 动画结束后兜底清掉仍在栈里的页面（正常情况 popPage 已移除顶层）
        val job = animJob
        scope.launch {
            job?.join()
            if (pageStack.size > 1) resetToRootImmediate()
        }
    }

    // 本地 PDF / 文本 / HTML / 图片查看器走正式页面栈，复用统一的进出场和预见式返回
    val viewerRequest = LocalFileViewerState.current
    LaunchedEffect(viewerRequest) {
        if (viewerRequest != null && pageStack.none { it.route == "local_file_viewer" }) {
            pushPage("local_file_viewer")
        }
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

                // ★ 位移不在 composition 阶段读 progress（否则动画期间整个页面栈每帧重组），
                //   改到 offset 的 placement lambda 里读；像素结果完全一致，只是省掉每帧重组。
                key(entry.id) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(zi)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset {
                                    val f = when {
                                        isExiting -> screenWidthPx * (1f - progress)
                                        isBelowExiting -> -screenWidthPx / 6f * progress
                                        isTop -> screenWidthPx * (1f - progress)
                                        isSecondTop -> -screenWidthPx / 6f * progress
                                        else -> 0f
                                    }
                                    IntOffset(f.roundToInt(), 0)
                                }
                                // ★ 触摸拦截必须放在"带位移的这一层"：
                                //   它只覆盖退场页当前可见的区域，页面滑走露出的部分立刻可以操作下层页面。
                                //   放在外层（不位移的整屏 Box）会把全屏触摸吃掉，导致必须等动画结束才能操作。
                                .then(
                                    if (isExiting) {
                                        Modifier.pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    awaitPointerEvent(PointerEventPass.Initial)
                                                        .changes
                                                        .forEach { it.consume() }
                                                }
                                            }
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
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
                                onOpenInBrowserFromApp = { url -> openInBrowserFromApp(url) },
                            )
                        }

                        if (shouldBlur) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    // 只有 isBelowExiting / isSecondTop 会走到这里，位移公式与主内容一致
                                    .offset {
                                        IntOffset((-screenWidthPx / 6f * progress).roundToInt(), 0)
                                    }
                                    .graphicsLayer { alpha = 0.2f * progress }
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }

            AutoUpdateChecker()

            val openDownloads = PendingOpenDownloadsStore.pending
            LaunchedEffect(openDownloads) {
                if (openDownloads) {
                    if (pageStack.lastOrNull()?.route != "downloads") {
                        pushPage("downloads")
                    }
                    PendingOpenDownloadsStore.pending = false
                }
            }
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
    onOpenInBrowserFromApp: (String) -> Unit,
) {
    val openInBrowserFromApp = onOpenInBrowserFromApp
    PageSurface(
        isSettled = surfaceIsSettled,
        cornerRadius = screenCornerRadius,
    ) {
        when (entry.route) {
            "browser" -> {
                BrowserScreen(
                    onOpenSettings = openSettings,
                    onOpenToolCustomize = { onPush("settings_tool_menu") },
                    onOpenScanner = openScanner,
                    onOpenDownloads = { onPush("downloads") },
                    onOpenScripts = { onPush("scripts") },
                    onOpenNewTabCustomization = {
                        SettingsLayoutNavArgs.initialTab = 1
                        onPush("settings_layout")
                    },
                    onOpenCrop = { onPush("crop") },
                    isTop = isTop,
                )
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
                    SettingsLayoutNavArgs.initialTab = 1
                    onPush("settings_layout")
                }
            )
            "settings_search" -> SettingsSearchPage(
                onBack = { onPop() },
                onOpenCustomEngineManager = { onPush("settings_custom_engines") },
            )
            "settings_custom_engines" -> CustomSearchEnginesPage(
                onBack = { onPop() },
            )
            "settings_web" -> SettingsWebPage(
                onBack = { onPop() },
                onOpenUaSettings = { onPush("settings_ua") },
            )
            "settings_ua" -> UaSettingsPage(onBack = { onPop() })
            "settings_icon" -> SettingsIconPage(onBack = { onPop() })
            "settings_layout" -> SettingsLayoutPage(
                onBack = { onPop() },
                onOpenCrop = { onPush("crop") },
                onNavigateToSearchSettings = {
                    SettingsLayoutNavArgs.flashSearchHistoryOnEnter = true
                    onPush("settings_search")
                },
                onOpenToolMenu = { onPush("settings_tool_menu") },
            )
            "settings_tool_menu" -> ToolMenuCustomizePage(onBack = { onPop() })
            "crop" -> CropScreenPage(onPop = { onPop() })
            "settings_web_security" -> SettingsWebSecurityPage(
                onBack = { onPop() },
                onOpenWhitelist = { onPush("settings_tracking_whitelist") },
                onOpenSiteManager = { onPush("settings_web_sites") },
                onOpenDownloadWhitelist = { onPush("settings_download_whitelist") },
                onOpenAdWhitelist = { onPush("settings_ad_whitelist") },
                onOpenWebPermissions = { onPush("settings_web_permissions") },
                onOpenCookieManager = { onPush("settings_cookie_manager") },
            )
            "settings_web_sites" -> WebSiteManagerPage(
                onBack = { onPop() },
                onOpenSite = { host ->
                    WebSiteNavArgs.host = host
                    onPush("settings_web_site")
                },
            )
            "settings_web_site" -> WebSiteSettingsPage(
                host = WebSiteNavArgs.host ?: "",
                onBack = { onPop() },
            )
            "settings_tracking_whitelist" -> TrackingWhitelistPage(
                onBack = { onPop() },
                onOpenSite = { host ->
                    WebSiteNavArgs.host = host
                    onPush("settings_web_site")
                },
            )
            "settings_download_whitelist" -> DownloadWhitelistPage(
                onBack = { onPop() },
                onOpenSite = { host ->
                    WebSiteNavArgs.host = host
                    onPush("settings_web_site")
                },
            )
            "settings_ad_whitelist" -> AdWhitelistPage(
                onBack = { onPop() },
                onOpenSite = { host ->
                    WebSiteNavArgs.host = host
                    onPush("settings_web_site")
                },
            )
            "settings_web_permissions" -> WebPermissionsPage(onBack = { onPop() })
            "settings_cookie_manager" -> CookieManagerPage(
                onBack = { onPop() },
                onOpenOrigin = { origin, host ->
                    CookieDetailNav.origin = origin
                    CookieDetailNav.host = host
                    onPush("settings_cookie_origin")
                },
            )
            "settings_cookie_origin" -> CookieDetailPage(onBack = { onPop() })
            "settings_app_permissions" -> SettingsAppPermissionsPage(
                onBack = { onPop() },
                onOpenClipboardSetting = { onPush("settings_clipboard_access") },
            )
            "settings_clipboard_access" -> ClipboardAccessPage(onBack = { onPop() })
            "settings_personal_info" -> SettingsPersonalInfoPage(
                onBack = { onPop() },
                onOpenPasswordManager = { onPush("settings_password_manager") },
                onOpenUxImprovement = { onPush("legal_ux_improvement") },
            )
            "settings_password_manager" -> PasswordManagerPage(
                onBack = { onPop() },
            )
            "settings_sync" -> SettingsSyncPage(
                onBack = { onPop() },
                onOpenBrowserSync = { onPush("settings_sync_browsers") },
            )
            "settings_legal" -> SettingsLegalPage(
                onBack = { onPop() },
                onOpenDoc = { docRoute -> onPush(docRoute) },
            )
            // ★ 法律文档：开源许可 / 第三方许可 / 隐私政策 / 用户协议 / 体验改进计划
            in LEGAL_DOC_ROUTES -> LegalDocumentPage(
                route = entry.route,
                onBack = { onPop() },
                onOpenDoc = { docRoute -> onPush(docRoute) },
            )
            "scripts" -> ScriptsPage(
                onBack = { onPop() },
                onOpenEditor = { onPush("script_editor") },
            )
            "script_editor" -> ScriptEditorPage(
                onBack = { onPop() },
                onSaved = { onPop() },
            )
            "downloads" -> DownloadsShell(
                onBack = { onPop() },
                onOpenDownloadSettings = { onPush("download_settings") },
                onOpenTaskDetail = { onPush("download_task_detail") },
                onOpenInBrowser = { uri -> openInBrowserFromApp(uri) },
                onOpenScanner = { onPush("scanner") },
                onOpenDir = { path, category, title, fromShortcut ->
                    FileManagerDirRequest.path = path
                    FileManagerDirRequest.category = category
                    FileManagerDirRequest.title = title
                    FileManagerDirRequest.fromShortcut = fromShortcut
                    FileManagerDirRequest.token = System.nanoTime()
                    onPush("file_manager_dir")
                },
                onOpenFileSettings = { onPush("file_manager_settings") },
            )
            "file_manager_dir" -> FileManagerDirPage(
                onBack = { onPop() },
                onOpenInBrowser = { uri -> openInBrowserFromApp(uri) },
                initialPath = FileManagerDirRequest.path,
                initialCategory = FileManagerDirRequest.category,
                pageTitle = FileManagerDirRequest.title ?: "手机存储目录",
                requestToken = FileManagerDirRequest.token,
                fromShortcut = FileManagerDirRequest.fromShortcut,
                onOpenFileBrowser = { onPush("file_browser") },
            )
            "file_manager_settings" -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                FileManagerSettingsPage(
                    onBack = { onPop() },
                    onOpenRecycleBin = {
                        FileManagerDirRequest.path = java.io.File(context.filesDir, "recycle").absolutePath
                        FileManagerDirRequest.category = null
                        FileManagerDirRequest.title = "回收站"
                        FileManagerDirRequest.fromShortcut = true
                        FileManagerDirRequest.token = System.nanoTime()
                        onPush("file_manager_dir")
                    },
                )
            }
            "local_file_viewer" -> {
                // 用首次组合时的请求快照，保证退场动画期间内容不会先变空
                val request = remember { LocalFileViewerState.current }
                if (request == null) {
                    LaunchedEffect(Unit) { onPop() }
                } else {
                    DisposableEffect(request) {
                        onDispose {
                            if (LocalFileViewerState.current === request) {
                                LocalFileViewerState.current = null
                            }
                        }
                    }
                    when (request) {
                        is LocalFileViewerRequest.Pdf -> LocalPdfViewerPage(
                            request = request,
                            onBack = onPop,
                        )
                        is LocalFileViewerRequest.Text -> LocalTextEditorPage(
                            request = request,
                            onBack = onPop,
                        )
                        is LocalFileViewerRequest.Web -> LocalWebViewerPage(
                            request = request,
                            onBack = onPop,
                            onOpenInBrowser = { url ->
                                LocalFileViewerState.current = null
                                openInBrowserFromApp(url)
                            },
                        )
                    }
                }
            }
            "file_browser" -> BrowserScreen(                onOpenSettings = { onPush("settings_main") },
                onOpenToolCustomize = { onPush("settings_tool_menu") },
                onOpenScanner = { onPush("scanner") },
                onOpenDownloads = { onPush("downloads") },
                onOpenScripts = { onPush("scripts") },
                onOpenNewTabCustomization = {
                    SettingsLayoutNavArgs.initialTab = 1
                    onPush("settings_layout")
                },
                onOpenCrop = { onPush("crop") },
                initialUrl = com.torachen.mote.ui.filemanager.LocalFileBrowserRequest.url,
                isTop = isTop,
            )
            "download_task_detail" -> DownloadTaskDetailScreen(
                onBack = { onPop() }
            )
            "download_settings" -> DownloadSettingsScreen(
                onBack = { onPop() }
            )
            "settings_space_management" -> SettingsSpaceManagementPage(
                onBack = { onPop() },
                onOpenCacheSettings = { onPush("settings_cache_settings") }
            )
            "settings_sync_browsers" -> SettingsSyncBrowsersPage(
                onBack = { onPop() },
                onOpenUrl = { url -> openInBrowserFromApp(url) },
            )
            "settings_cache_settings" -> CacheSettingsPage(onBack = { onPop() })
            "settings_about" -> SettingsAboutPage(
                onBack = { onPop() },
                onOpenUrl = { url -> openInBrowserFromApp(url) },
            )
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
    "settings_custom_engines" -> "默认搜索引擎"
    "settings_web" -> "网页浏览设置"
    "settings_icon" -> "图标"
    "settings_layout" -> "主题与界面"
    "settings_web_security" -> "网页安全中心"
    "settings_app_permissions" -> "通知与权限"
    "settings_personal_info" -> "个人信息"
    "settings_password_manager" -> "密码管理器"
    "settings_legal" -> "法律信息"
    "settings_about" -> "关于"
    "file_manager_settings" -> "文件管理设置"
    else -> ""
}

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
                    // ★ 修复：自定义背景图索引是 2，不是 1（1 是每日壁纸）
                    .putInt("home_background_content", 2)
                    .apply()
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
// 标题栏（顶部 / 底部）
// ============================================================

/**
 * 浏览器标题栏/工具栏的配色组（沉浸式页面使用）。
 */
private data class ChromePalette(
    val active: Boolean,
    val backgroundColor: Color,
    val isDark: Boolean,
    val contentColor: Color,
    val secondaryContentColor: Color,
    val chipColor: Color,
    val accentColor: Color,
    val selectedChipColor: Color,
    val selectedChipTextColor: Color,
)

private fun buildChromePalette(
    active: Boolean,
    backgroundColor: Color,
    surfaceContentColor: Color,
    surfaceSecondaryContentColor: Color,
    surfacePrimaryColor: Color,
    surfaceChipColor: Color,
): ChromePalette {
    val isDark = backgroundColor.luminance() < 0.5f
    val contentColor = if (active) {
        if (isDark) Color.White else Color.Black
    } else {
        surfaceContentColor
    }
    return ChromePalette(
        active = active,
        backgroundColor = backgroundColor,
        isDark = isDark,
        contentColor = contentColor,
        secondaryContentColor = if (active) {
            contentColor.copy(alpha = 0.62f)
        } else {
            surfaceSecondaryContentColor
        },
        chipColor = if (active) {
            if (isDark) {
                Color.White.copy(alpha = 0.16f).compositeOver(backgroundColor)
            } else {
                Color.Black.copy(alpha = 0.07f).compositeOver(backgroundColor)
            }
        } else {
            surfaceChipColor
        },
        accentColor = if (active) contentColor else surfacePrimaryColor,
        // 选中态始终使用主题色（如 MIUIX 蓝），不随沉浸式取色改变
        selectedChipColor = surfacePrimaryColor,
        selectedChipTextColor = Color.White,
    )
}

/**
 * 浏览器标题栏容器。
 *
 * [atBottom] 为 true 时，整个标题栏上下排序取反：
 * 输入框在最下方，搜索建议/历史、搜索引擎切换栏依次位于其上方。
 */
@Composable
private fun BrowserHeader(
    atBottom: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    inputField: @Composable () -> Unit,
    outsideEndAction: @Composable () -> Unit,
    engineSwitcher: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    trailing: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    containerColor: Color = Color.Transparent,
) {
    if (atBottom) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(bottom = bottomInset),
        ) {
            Spacer(Modifier.height(4.dp))
            engineSwitcher()
            BottomSearchBar(
                inputField = inputField,
                outsideEndAction = outsideEndAction,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                content = content,
            )
            trailing()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(containerColor)
                .statusBarsPadding(),
        ) {
            SearchBar(
                inputField = inputField,
                onExpandedChange = onExpandedChange,
                expanded = expanded,
                outsideEndAction = outsideEndAction,
                content = content,
            )
            engineSwitcher()
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * [SearchBar] 的上下翻转版本：展开内容位于输入框上方，且自下而上展开。
 */
@Composable
private fun BottomSearchBar(
    inputField: @Composable () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    expanded: Boolean,
    outsideEndAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)

    Column {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            Column {
                content()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 0.dp, horizontal = 12.dp),
            ) {
                inputField()
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it }),
            ) {
                outsideEndAction?.invoke()
            }
        }
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = expanded,
        onBackCompleted = {
            currentOnExpandedChange(false)
        },
    )
}

/**
 * 快捷输入栏（网址前缀按钮），供标题栏底部模式内嵌使用。
 */
@Composable
private fun QuickInputBarRow(
    backgroundColor: Color,
    buttonBackground: Color,
    textColor: Color,
    onInsert: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("https://", "www.", "/", ".com", ".cn", ".net", ".org").forEach { text ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(buttonBackground)
                    .clickable { onInsert(text) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text, color = textColor)
            }
        }
    }
}

/**
 * 沉浸式页面：让系统状态栏/导航栏图标明暗跟随顶部/底部控件配色。
 */
@Composable
private fun ImmersiveSystemBarsEffect(
    topActive: Boolean,
    topIsDark: Boolean,
    bottomActive: Boolean,
    bottomIsDark: Boolean,
    showNewTabPage: Boolean,
    isTop: Boolean,
    isFullscreen: Boolean,
    themeIsDark: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun applySystemBarIcons() {
        if (isFullscreen || showNewTabPage) return
        val activity = context as? Activity ?: return
        val controller = WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView,
        )
        if (!isTop) {
            // 浏览器不在最上层（如进入设置页）时恢复主题色系统栏图标
            controller.isAppearanceLightStatusBars = !themeIsDark
            controller.isAppearanceLightNavigationBars = !themeIsDark
            return
        }
        controller.isAppearanceLightStatusBars =
            if (topActive) !topIsDark else !themeIsDark
        controller.isAppearanceLightNavigationBars =
            if (bottomActive) !bottomIsDark else !themeIsDark
    }

    LaunchedEffect(
        topActive,
        topIsDark,
        bottomActive,
        bottomIsDark,
        showNewTabPage,
        isTop,
        isFullscreen,
        themeIsDark,
    ) {
        if (isFullscreen || showNewTabPage) return@LaunchedEffect
        // 延迟一点，避开新标签页/上一个页面退出时对系统栏图标的还原
        if (isTop) delay(260L)
        applySystemBarIcons()
    }

    DisposableEffect(
        lifecycleOwner,
        topActive,
        topIsDark,
        bottomActive,
        bottomIsDark,
        showNewTabPage,
        isTop,
        isFullscreen,
        themeIsDark,
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                applySystemBarIcons()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/**
 * 网页安全弹窗。顶部标题栏时挂在标题栏下方并向下展开；
 * 底部标题栏时挂在标题栏上方并向上展开。
 */
@Composable
private fun BrowserSecuritySheetPopup(
    visible: Boolean,
    page: Int,
    info: SecurityInfo,
    atBottom: Boolean,
    modifier: Modifier = Modifier,
    onBoundsChanged: (Rect) -> Unit,
    onPageChange: (Int) -> Unit,
    onOpenSiteSettings: (String) -> Unit,
    onToggleBlockAds: (Boolean) -> Unit,
    onToggleTrackingProtection: (Boolean) -> Unit,
    onOpenCookieSettings: () -> Unit,
) {
    val menuEase = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(240, easing = menuEase),
            expandFrom = if (atBottom) Alignment.Bottom else Alignment.Top,
        ) + fadeIn(tween(240, easing = menuEase)),
        exit = shrinkVertically(
            animationSpec = tween(240, easing = menuEase),
            shrinkTowards = if (atBottom) Alignment.Bottom else Alignment.Top,
        ) + fadeOut(tween(240, easing = menuEase)),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    onBoundsChanged(coords.boundsInWindow())
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
            shape = if (atBottom) {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp,
                )
            } else {
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp,
                )
            },
            color = MiuixTheme.colorScheme.surface,
        ) {
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = tween(300, easing = menuEase),
                            initialOffsetX = { it },
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(300, easing = menuEase),
                            targetOffsetX = { -it },
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(300, easing = menuEase),
                            initialOffsetX = { -it },
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(300, easing = menuEase),
                            targetOffsetX = { it },
                        )
                    }
                },
                label = "securitySheetPage",
            ) { currentPage ->
                when (currentPage) {
                    0 -> SecurityMenuContent(
                        info = info,
                        onOpenCertDetail = { onPageChange(1) },
                        onOpenCookies = { onPageChange(2) },
                        onToggleBlockAds = onToggleBlockAds,
                        onToggleTrackingProtection = onToggleTrackingProtection,
                        onOpenSiteSettings = onOpenSiteSettings,
                    )
                    1 -> CertDetailContent(
                        info = info,
                        onBack = { onPageChange(0) },
                    )
                    2 -> CookieDetailContent(
                        info = info,
                        onBack = { onPageChange(0) },
                        onOpenSettings = onOpenCookieSettings,
                    )
                }
            }
        }
    }
}

// ============================================================
// 底部工具栏
// ============================================================

private class BrowserBottomBarItem(
    val id: String,
    val visible: Boolean = true,
    val enabled: Boolean = true,
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
                                        enabled = alphaAnim > 0.5f && item.enabled,
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

// ============================================================
// 浏览器主界面
// ============================================================

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    onOpenSettings: () -> Unit,
    onOpenToolCustomize: () -> Unit = {},
    onOpenScanner: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenScripts: () -> Unit = {},
    onOpenNewTabCustomization: () -> Unit,
    onOpenCrop: () -> Unit = {},
    onOpenSiteSettings: (String) -> Unit = {},
    initialUrl: String? = null,
    isTop: Boolean
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textToolbar = LocalTextToolbar.current
    val scope = rememberCoroutineScope()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val isUiDarkMode = surfaceColor.luminance() < 0.5f
    val isLightTheme = !isUiDarkMode
    val buttonBg = if (isLightTheme) Color.White
    else MiuixTheme.colorScheme.secondaryContainer

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    LaunchedEffect(Unit) {
        SecurityStateHolder.attachPrefs(prefs)
    }
    val windows = BrowserWindowStateHolder.windows
    if (windows.isEmpty()) {
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
        windows.add(
            BrowserWindow(
                id = UUID.randomUUID().toString(),
                title = if (finalUrl.isBlank()) "新标签页" else "加载中…",
                url = finalUrl,
                nightMode = nightMode,
                noImage = prefs.getBoolean("web_no_image", false),
                desktopMode = prefs.getBoolean("web_desktop_mode", false),
            )
        )
    }

    var activeWindowId by remember {
        mutableStateOf<String?>(BrowserWindowStateHolder.activeWindowId ?: windows.first().id)
    }
    LaunchedEffect(activeWindowId) {
        BrowserWindowStateHolder.activeWindowId = activeWindowId
    }
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
    var editingBookmarkId by remember { mutableStateOf<String?>(null) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderTitleInput by remember { mutableStateOf("") }

    var showSourceDialog by remember { mutableStateOf(false) }
    var showScriptDialog by remember { mutableStateOf(false) }
    var sourceContent by remember { mutableStateOf("") }
    var sourceSearchQuery by remember { mutableStateOf("") }
    var sourceMatchCurrent by remember { mutableIntStateOf(0) }
    var sourceMatchTotal by remember { mutableIntStateOf(0) }
    var sourceWrapEnabled by remember { mutableStateOf(true) }
    var sourceFontSize by remember { mutableIntStateOf(13) }
    var sourceWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var isSourceSearchActive by remember { mutableStateOf(false) }

    var scriptContent by remember { mutableStateOf("") }
    var pendingDeleteBookmarkId by remember { mutableStateOf<String?>(null) }
    var showDeleteBookmarkDialog by remember { mutableStateOf(false) }
    var showMoveBookmarksDialog by remember { mutableStateOf(false) }
    var addFromBookmarkSheet by remember { mutableStateOf(false) }
    var addFolderFromAddBookmark by remember { mutableStateOf(false) }

    var showNightModeConfirmDialog by remember { mutableStateOf(false) }
    var pendingNightModeTarget by remember { mutableStateOf<Boolean?>(null) }

    var showSniffSheet by remember { mutableStateOf(false) }
    var isSniffing by remember { mutableStateOf(false) }
    val sniffedResources = remember { mutableStateListOf<SniffedResource>() }
    val networkSniffedResources = remember { mutableStateListOf<SniffedResource>() }

    var showSecuritySheet by remember { mutableStateOf(false) }
    var securityPage by remember { mutableIntStateOf(0) }
    var securitySheetBounds by remember { mutableStateOf<Rect?>(null) }
    var searchBarBounds by remember { mutableStateOf<Rect?>(null) }
    var inputBounds by remember { mutableStateOf(Rect.Zero) }
    var bottomHeaderContentHeightPx by remember { mutableIntStateOf(0) }
    val secInfo = SecurityStateHolder.info

    LaunchedEffect(showSecuritySheet) {
        if (!showSecuritySheet && securityPage != 0) {
            delay(280)
            securityPage = 0
        }
    }

    var urlInput by remember { mutableStateOf(TextFieldValue("")) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchBarExpanded by remember { mutableStateOf(false) }
    var forceHideHistory by remember { mutableStateOf(false) }

    var pendingDownload by remember { mutableStateOf<PendingDownload?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var longPressInfo by remember { mutableStateOf<LongPressInfo?>(null) }
    var longPressMenuVisible by remember { mutableStateOf(false) }
    var windowIconPulse by remember { mutableIntStateOf(0) }
    var longPressCounter by remember { mutableIntStateOf(0) }

    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var findResultCurrent by remember { mutableIntStateOf(0) }
    var findResultTotal by remember { mutableIntStateOf(0) }

    var showZoomBar by remember { mutableStateOf(false) }
    var zoomFactor by remember { mutableStateOf(1.0f) }
    var pendingDefaultZoomChange by remember { mutableStateOf<Int?>(null) }

    var isFullscreen by remember { mutableStateOf(false) }
    var pendingExternalSchemeUrl by remember { mutableStateOf<String?>(null) }

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

    var showTtsBar by remember { mutableStateOf(false) }
    var isTtsPlaying by remember { mutableStateOf(false) }
    var ttsTextChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    var ttsCurrentIndex by remember { mutableIntStateOf(0) }

    var ttsReady by remember { mutableStateOf(false) }
    var ttsInitFailed by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var showTtsErrorDialog by remember { mutableStateOf(false) }
    var ttsRetryKey by remember { mutableIntStateOf(0) }

    DisposableEffect(ttsRetryKey) {
        ttsReady = false
        ttsInitFailed = false
        var isDisposed = false
        val mainHandler = Handler(Looper.getMainLooper())

        try {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
        } catch (_: Exception) {}
        ttsEngine = null

        val initListener = TextToSpeech.OnInitListener { status ->
            mainHandler.post {
                if (isDisposed) return@post
                if (status == TextToSpeech.SUCCESS) {
                    ttsEngine?.let { engine ->
                        try {
                            engine.setLanguage(Locale.CHINESE)
                        } catch (_: Exception) {}
                        ttsReady = true
                        ttsInitFailed = false
                    } ?: run {
                        ttsInitFailed = true
                    }
                } else {
                    ttsReady = false
                    ttsInitFailed = true
                }
            }
        }

        // 使用 applicationContext 避免 Activity Context 泄漏
        val engine = TextToSpeech(context.applicationContext, initListener)
        ttsEngine = engine

        // 5 秒超时兜底，给小米系统留出足够拉起引擎的时间
        val timeoutRunnable = Runnable {
            if (!ttsReady && !ttsInitFailed) {
                ttsInitFailed = true
            }
        }
        mainHandler.postDelayed(timeoutRunnable, 5000L)

        onDispose {
            isDisposed = true
            mainHandler.removeCallbacks(timeoutRunnable)
            try {
                engine.stop()
                engine.shutdown()
            } catch (_: Exception) {}
            ttsEngine = null
        }
    }

    fun speakChunk(index: Int) {
        val engine = ttsEngine ?: return
        if (index !in ttsTextChunks.indices) {
            isTtsPlaying = false
            return
        }
        if (!ttsReady) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "语音引擎未就绪",
                    duration = SnackbarDuration.Short,
                )
            }
            return
        }
        val text = ttsTextChunks[index]
        ttsCurrentIndex = index
        isTtsPlaying = true
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    if (isTtsPlaying) {
                        speakChunk(index + 1)
                    }
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post { isTtsPlaying = false }
            }
        })
        try {
            val params = Bundle()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, index.toString())
        } catch (_: Exception) {
            isTtsPlaying = false
        }
    }

    fun stopTts() {
        try {
            isTtsPlaying = false
            ttsEngine?.stop()
        } catch (_: Exception) {}
    }

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
        mutableIntStateOf(prefs.getInt("web_dynamic_tool", 1).coerceIn(0, 9))
    }
    var dynamicToolSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_dynamic_tool_suggest", true))
    }
    var isEnglishPage by remember { mutableStateOf(false) }

    fun getActiveCustomEngineUrl(): String {
        val engines = loadCustomSearchEngines(prefs)
        val activeId = prefs.getString("active_custom_engine_id", null)
        return engines.find { it.id == activeId }?.url
            ?: engines.firstOrNull()?.url
            ?: ""
    }

    var customEngineUrl by remember { mutableStateOf(getActiveCustomEngineUrl()) }

    var controlColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_control_color_mode", 1).coerceIn(0, 3))
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
    var headerAtBottom by remember {
        mutableStateOf(prefs.getInt("browser_header_position", 0) == 1)
    }
    var headerTextMode by remember {
        mutableStateOf(prefs.getInt("browser_header_text_mode", 1))
    }
    var immersivePageEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_immersive_page", true))
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

    var searchSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean("search_suggest_enabled", true))
    }
    var currentSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

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
    var globalNightDim by remember {
        mutableStateOf(prefs.getBoolean("web_night_mode_dim", true))
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
                "browser_header_position" ->
                    headerAtBottom = prefs.getInt(key, 0) == 1
                "browser_header_text_mode" ->
                    headerTextMode = prefs.getInt(key, 1)
                "web_immersive_page" ->
                    immersivePageEnabled = prefs.getBoolean(key, true)
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
                            webViewMap[id]?.let { applyNightModeToWebView(it, enabled) }
                        }
                    }
                    val mode = prefs.getInt(key, 0)
                    if (mode == 1 || mode == 2) {
                        MoteApplication.trackEventSafe(
                            "night_mode_toggled",
                            mapOf("enabled" to (mode == 2))
                        )
                    }
                }
                "web_night_mode_dim" -> {
                    globalNightDim = prefs.getBoolean(key, true)
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
                                val ua = UaStore.effectiveUa(context, enabled)
                                    ?: if (enabled) DESKTOP_UA else getMobileUserAgent(context)
                                wv.settings.userAgentString = ua
                                applyDesktopViewportSettings(wv, enabled)
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
                    dynamicToolIndex = prefs.getInt(key, 1).coerceIn(0, 9)
                }
                "web_dynamic_tool_suggest" -> {
                    dynamicToolSuggestEnabled = prefs.getBoolean(key, true)
                }
                "search_suggest_enabled" -> {
                    searchSuggestEnabled = prefs.getBoolean(key, true)
                }
                "web_video_player" -> {
                    useNativePlayerEnabled = prefs.getInt(key, 0) == 0
                }
                "custom_search_engines", "active_custom_engine_id" -> {
                    customEngineUrl = getActiveCustomEngineUrl()
                }
                "ui_control_color_mode" -> {
                    controlColorMode = prefs.getInt(key, 1).coerceIn(0, 3)
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

    val customEngines = remember(customEngineUrl) {
        loadCustomSearchEngines(prefs)
    }
    val searchEngines: List<Pair<String, String>> = remember(customEngines) {
        buildList {
            add("必应" to "https://cn.bing.com/search?q=%s")
            add("抖音" to "https://www.douyin.com/search/%s")
            add("百度" to "https://m.baidu.com/s?word=%s")
            add("神马" to "https://m.sm.cn/s?q=%s")
            add("搜狗" to "https://m.sogou.com/web/searchList.jsp?keyword=%s")
            add("谷歌" to "https://www.google.com/search?q=%s")
            customEngines.forEach {
                add(it.name to it.url)
            }
        }
    }

    val history = remember { mutableStateListOf<HistoryEntry>() }
    val closedHistory = remember { mutableStateListOf<HistoryEntry>() }

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
                    put("isRemote", entry.isRemote)
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
                        isRemote = obj.optBoolean("isRemote", false)
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
                isRemote = false
            )
            saveHistory()
            return
        }

        val entry: HistoryEntry = if (existingIndex > 0) {
            val old = history.removeAt(existingIndex)
            old.copy(
                title = title.ifBlank { old.title },
                timestamp = System.currentTimeMillis(),
                isRemote = false
            )
        } else {
            HistoryEntry(
                title = title.ifBlank { "无标题" },
                url = url,
                timestamp = System.currentTimeMillis(),
                isRemote = false
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

    fun findParentList(items: MutableList<BookmarkItem>, id: String): MutableList<BookmarkItem>? {
        if (items.any { it.id == id }) return items
        for (item in items) {
            if (item.isFolder) {
                findParentList(item.children, id)?.let { return it }
            }
        }
        return null
    }

    fun findParentFolderId(items: List<BookmarkItem>, id: String, parentId: String? = null): String? {
        for (item in items) {
            if (item.id == id) return parentId
            if (item.isFolder) {
                findParentFolderId(item.children, id, item.id)?.let { return it }
            }
        }
        return null
    }

    fun collectAllFolders(
        items: List<BookmarkItem>,
        depth: Int = 0,
        out: MutableList<Pair<BookmarkItem, Int>> = mutableListOf(),
    ): List<Pair<BookmarkItem, Int>> {
        items.forEach { item ->
            if (item.isFolder) {
                out.add(item to depth)
                collectAllFolders(item.children, depth + 1, out)
            }
        }
        return out
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
            if (prefs.getBoolean("toast_fullscreen_enabled", true)) {
                Toast.makeText(context, "摇一摇退出全屏", Toast.LENGTH_SHORT).show()
            }
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
        updateWindow(id) { it.url = ""; it.title = "新标签页"; it.favicon = null }
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
            if (prefs.getBoolean("toast_incognito_enabled", true)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "无痕模式已开启",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        } else {
            try {
                wv.settings.cacheMode = WebSettings.LOAD_DEFAULT
            } catch (_: Exception) {}
            if (prefs.getBoolean("toast_incognito_enabled", true)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "无痕模式已关闭",
                        duration = SnackbarDuration.Short,
                    )
                }
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
        webViewMap[id]?.let { applyNightModeToWebView(it, isUiDarkMode) }
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

    val hasActiveMoteDownloads = MoteDownloader.tasks.any {
        it.status == MoteDownloadStatus.RUNNING ||
                it.status == MoteDownloadStatus.PENDING ||
                it.status == MoteDownloadStatus.PAUSED
    }

    fun createWindow(url: String = "", directOpen: Boolean = false): String {
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
                isProgrammatic = finalUrl.isNotBlank(),
                isDirectOpen = directOpen && finalUrl.isNotBlank(),
            )
        )
        activeWindowId = id
        urlInput = TextFieldValue("")
        searchQueryHolder.value = ""
        isSearchResultPage = false
        clearSearchFocus()
        return id
    }

    fun createBackgroundWindow(url: String) {
        if (url.isBlank()) return
        val inheritIncognito =
            windows.firstOrNull { it.id == activeWindowId }?.incognito == true
        val id = UUID.randomUUID().toString()
        val initialTitle: String = try {
            val host = Uri.parse(url).host ?: ""
            if (host.isNotBlank()) host.removePrefix("www.") else "加载中…"
        } catch (_: Exception) {
            "加载中…"
        }
        windows.add(
            BrowserWindow(
                id = id,
                title = initialTitle,
                url = url,
                incognito = inheritIncognito,
                nightMode = globalNightMode,
                noImage = globalNoImage,
                desktopMode = globalDesktopMode,
                isProgrammatic = true,
            )
        )
        // 不切换 activeWindowId
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

        val closingWin = windows[index]
        if (closingWin.url.isNotBlank() && closingWin.url != "about:blank") {
            closedHistory.add(0, HistoryEntry(closingWin.title, closingWin.url, System.currentTimeMillis(), isClosed = true))
        }

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
            } else if (headerTextMode == 1 && newWin?.title?.isNotBlank() == true) {
                TextFieldValue(newWin.title)
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
        } else if (headerTextMode == 1 && win?.title?.isNotBlank() == true) {
            TextFieldValue(win.title)
        } else {
            TextFieldValue(winUrl)
        }
        clearSearchFocus()
    }

    // ★ 统一导航判断（供底部工具栏与横滑手势使用）
    //   返回：WebView 有历史 → goBack
    //         无历史 + 不在 home → 直接跳转窗口关闭，普通窗口回主页
    fun canNavigateBack(): Boolean {
        val wv = activeWindowId?.let { webViewMap[it] } ?: return false
        if (wv.canGoBack()) return true
        if (!isAtHomePage()) return true
        return false
    }

    fun performNavigateBack() {
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return
        if (wv.canGoBack()) {
            wv.goBack()
            return
        }
        if (!isAtHomePage()) {
            // 由文件管理/设置等页面直接新建并加载的窗口从未经过主页，返回应直接关闭它
            if (activeWindow?.isDirectOpen == true && windows.size > 1) {
                closeWindow(id)
            } else {
                goToHomePage()
            }
        }
    }

    fun canNavigateForward(): Boolean {
        val wv = activeWindowId?.let { webViewMap[it] } ?: return false
        return wv.canGoForward()
    }

    fun performNavigateForward() {
        val wv = activeWindowId?.let { webViewMap[it] } ?: return
        if (wv.canGoForward()) wv.goForward()
    }

    fun loadUrlInActive(input: String, forceSearch: Boolean = false) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val id = activeWindowId ?: return
        val wv = webViewMap[id] ?: return

        val rawIndex = prefs.getInt("default_search_engine", 0)
        val engineIndex = rawIndex.coerceIn(0, searchEngines.lastIndex)

        val isDirectUrl = !forceSearch && (
                trimmed.startsWith("http://", ignoreCase = true) ||
                        trimmed.startsWith("https://", ignoreCase = true) ||
                        trimmed.startsWith("ftp://", ignoreCase = true) ||
                        isWebsite(trimmed))

        val url = when {
            !forceSearch && (trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) ||
                    trimmed.startsWith("ftp://", ignoreCase = true)) -> trimmed
            !forceSearch && isWebsite(trimmed) -> "https://$trimmed"
            else -> {
                val engine = searchEngines.getOrElse(engineIndex) { searchEngines[0] }
                MoteApplication.trackEventSafe(
                    "search_performed",
                    mapOf("engine" to engine.first)
                )
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
        val value = urlInput
        val current = value.text
        // 快捷输入栏：内容只添加在光标左侧，不替换选区
        val pos = value.selection.min.coerceIn(0, current.length)
        val newText = current.substring(0, pos) + text + current.substring(pos)
        urlInput = TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(pos + text.length),
        )
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
                            // 持久化，供书签列表 / 历史记录使用
                            FaviconStore.save(context, expectedHost, bmp)
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
        networkSniffedResources.clear()
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
            if (prefs.getBoolean("toast_translate_enabled", true)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "已恢复原文",
                        duration = SnackbarDuration.Short,
                    )
                }
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


            if (payload.optBoolean("already", false)) {
                return@evaluateJavascript
            }

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

            if (prefs.getBoolean("toast_translate_enabled", true)) {
                snackbarJob.value?.cancel()
                snackbarJob.value = scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "正在翻译…",
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite,
                    )
                }
            }

            translateJob.value?.cancel()
            translateJob.value = scope.launch {
                try {
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

                        // ★ 修正：MIUIX 库使用 newestSnackbarData() 来获取当前最新并 dismiss
                        snackbarHostState.newestSnackbarData()?.dismiss()
                        snackbarJob.value?.cancel()
                        snackbarJob.value = null
                    }

                    // 留出动画时间，让旧 Snackbar 彻底滑出
                    delay(250)
                    if (!isActive) return@launch

                    if (prefs.getBoolean("toast_translate_enabled", true)) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "翻译完成",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.newestSnackbarData()?.dismiss()
                        snackbarJob.value?.cancel()
                        snackbarJob.value = null
                    }

                    delay(150)

                    if (prefs.getBoolean("toast_translate_enabled", true)) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "翻译失败：${e.message ?: "网络错误"}",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                } finally {
                    // 确保在退出页面或意外取消时，立刻清理掉悬而未决的 Indefinite Snackbar
                    withContext(NonCancellable + Dispatchers.Main) {
                        if (snackbarJob.value != null) {
                            snackbarHostState.newestSnackbarData()?.dismiss()
                            snackbarJob.value?.cancel()
                            snackbarJob.value = null
                        }
                    }
                }
            }
        }
    }

    val prefsReloadScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val externalListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "bookmarks_v2" -> prefsReloadScope.launch(Dispatchers.Main) {
                    loadBookmarks()
                    bookmarkFolderPath.clear()
                }
                "history" -> prefsReloadScope.launch(Dispatchers.Main) {
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
        // 这些 load 会写 Compose 状态列表，必须在主线程执行，否则界面可能收不到刷新
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
                // ★ 核心修复：只要页面一发生变化，立刻同步写入磁盘！
                prefs.edit().putString("session_snapshot", json.toString()).commit()
            } catch (_: Exception) {}
        } else {
            prefs.edit().putString("session_snapshot", "").commit()
        }
    }

    var hasHandledRestore by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (hasHandledRestore) return@LaunchedEffect
        val pendingRestore = SessionSnapshotStore.pendingRestore ?: return@LaunchedEffect
        hasHandledRestore = true
        SessionSnapshotStore.pendingRestore = null

        val urls = pendingRestore
        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        delay(500) // 延迟半秒，等界面完全渲染，防止被 NewTabPage 遮挡

        val startupOption = prefs.getInt("home_startup_option", 0)
        val askRestore = prefs.getBoolean("home_ask_restore_memory_v2", true)

        fun restoreSession() {
            if (urls.isNotEmpty()) {
                val currentWinId = activeWindowId
                if (currentWinId != null) {
                    updateWindow(currentWinId) { it.url = urls[0]; it.title = "加载中…" }
                    webViewMap[currentWinId]?.loadUrl(urls[0])
                }
                for (i in 1 until urls.size) {
                    try {
                        createBackgroundWindow(urls[i])
                    } catch (_: Exception) {}
                }
            }
        }

        if (startupOption == 2) {
            restoreSession()
            return@LaunchedEffect
        }

        if (!askRestore) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = "上次打开过 ${urls.size} 个页面",
            actionLabel = "恢复",
            duration = SnackbarDuration.Long,
        )

        if (result == SnackbarResult.ActionPerformed) {
            restoreSession()
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

    LaunchedEffect(initialUrl) {
        val url = initialUrl ?: return@LaunchedEffect
        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        activeWindowId?.let { id ->
            delay(120)
            runCatching { navigateToUrl(id, url) }
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
        val forceNewWindow = PendingOpenInNewWindowStore.forceNewWindow
        var waited = 0
        while (activeWindowId == null && waited < 3000) {
            delay(50); waited += 50
        }
        if (activeWindowId != null) {
            delay(100)
            try {
                val current = windows.firstOrNull { it.id == activeWindowId }
                if (!forceNewWindow && current != null && (current.url.isBlank() || isHomeUrl(current.url))) {
                    // 当前窗口还是空白页时直接复用，避免多出一个新标签页历史
                    navigateToUrl(current.id, url)
                } else {
                    createWindow(url, directOpen = forceNewWindow)
                }
            } catch (_: Exception) {}
        }
        PendingOpenInNewWindowStore.forceNewWindow = false
        PendingOpenInNewWindowStore.pending = null
    }

    LaunchedEffect(activeWindowId) {
        activeWindow?.let { win ->
            if (!isSearchFocused) {
                val query = searchQueryHolder.value
                urlInput = when {
                    query.isNotBlank() && isSearchResultPage -> TextFieldValue(query)
                    isHomeUrl(win.url) || win.url.isBlank() -> TextFieldValue("")
                    headerTextMode == 1 && win.title.isNotBlank() -> TextFieldValue(win.title)
                    else -> TextFieldValue(win.url)
                }
            }
        }
    }

    val pendingPassword = PasswordCaptureStore.pending
    LaunchedEffect(pendingPassword) {
        val p = pendingPassword ?: return@LaunchedEffect
        PasswordCaptureStore.pending = null

        val existing = loadSavedPasswords(prefs)
        val alreadySaved = existing.any {
            it.url == p.url && it.username == p.username && it.password == p.password
        }
        if (alreadySaved) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = "是否保存该密码？",
            actionLabel = "保存",
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            val newEntry = PasswordEntry(
                url = p.url,
                username = p.username,
                password = p.password,
            )
            saveSavedPasswords(prefs, existing + newEntry)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "密码已保存",
                    duration = SnackbarDuration.Short,
                )
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

    LaunchedEffect(urlInput.text, isSearchFocused, searchSuggestEnabled) {
        val text = urlInput.text
        val engineIndex = prefs.getInt("default_search_engine", 0)
        if (searchSuggestEnabled && isSearchFocused && text.isNotBlank()) {
            delay(150)
            try {
                currentSuggestions = withContext(Dispatchers.IO) {
                    fetchSearchSuggestions(text, engineIndex)
                }.filter { !it.equals(text, ignoreCase = true) }
            } catch (_: Exception) {
                currentSuggestions = emptyList()
            }
        } else {
            currentSuggestions = emptyList()
        }
    }

    LaunchedEffect(isTop) {
        if (!isTop) {
            focusManager.clearFocus()
            isSearchFocused = false
            searchBarExpanded = false
            showSecuritySheet = false
        }
    }
    LaunchedEffect(activeWindowId, isEnglishPage) {
        val id = activeWindowId ?: return@LaunchedEffect
        val win = windows.firstOrNull { it.id == id } ?: return@LaunchedEffect
        if (win.translated && isEnglishPage) {
            setPageTranslated(true)
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

    val isNoDialogOpen = !showToolSheet && !showWindowSheet && !showHistorySheet &&
            !showBookmarkSheet && !showSniffSheet && !showSecuritySheet &&
            !showAddBookmarkDialog && !showClearBookmarkDialog && !showClearHistoryDialog &&
            !showClearSearchHistoryDialog && !showAddFolderDialog && !showDeleteBookmarkDialog

    val canSystemGoBack = canNavigateBack() || (windows.size > 1)
    BackHandler(
        enabled = isTop && canSystemGoBack && !showFindBar && isNoDialogOpen
    ) {
        if (canNavigateBack()) {
            performNavigateBack()
        } else if (windows.size > 1) {
            activeWindowId?.let { closeWindow(it) }
        }
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

    val hasActiveOverlay = showToolSheet || showWindowSheet || showHistorySheet ||
            showBookmarkSheet || showClearHistoryDialog || showClearBookmarkDialog ||
            showClearSearchHistoryDialog || showAddBookmarkDialog || showAddFolderDialog ||
            showDeleteBookmarkDialog || showNightModeConfirmDialog || showSniffSheet ||
            showSecuritySheet || showDownloadDialog || pendingExternalSchemeUrl != null ||
            PendingSslErrorStore.pending != null || PendingJsDialogStore.pending != null ||
            longPressMenuVisible

    LaunchedEffect(hasActiveOverlay) {
        GlobalOverlayState.isOpen = hasActiveOverlay
    }

    // 只有不受 Dialog 保护的非弹窗层（菜单、安全面板），才需要手动拦截返回键
    BackHandler(enabled = isTop && showSecuritySheet) {
        showSecuritySheet = false
    }

    BackHandler(enabled = isTop && longPressMenuVisible) {
        longPressMenuVisible = false
    }

    var historyMultiSelect by remember { mutableStateOf(false) }
    val historySelected = remember { mutableStateListOf<String>() }
    var bookmarkMultiSelect by remember { mutableStateOf(false) }
    // 打开书签 / 历史时先同步一次，尽量把其它设备的新数据带回来
    LaunchedEffect(showBookmarkSheet) {
        if (showBookmarkSheet) com.torachen.mote.MoteAutoSync.requestSync(context)
    }
    LaunchedEffect(showHistorySheet) {
        if (showHistorySheet) com.torachen.mote.MoteAutoSync.requestSync(context)
    }
    val bookmarkSelected = remember { mutableStateListOf<String>() }
    var bookmarkFaviconReload by remember { mutableIntStateOf(0) }
    LaunchedEffect(showBookmarkSheet) {
        if (showBookmarkSheet) bookmarkFaviconReload++
    }


    fun moveSelectedBookmarks(targetFolderId: String?): Int {
        if (bookmarkSelected.isEmpty()) return 0
        val targetChildren: MutableList<BookmarkItem> = if (targetFolderId == null) {
            bookmarks
        } else {
            findBookmarkById(bookmarks, targetFolderId)?.children ?: return 0
        }
        val moved = mutableListOf<BookmarkItem>()
        bookmarkSelected.toList().forEach { id ->
            val item = findBookmarkById(bookmarks, id) ?: return@forEach
            if (item.isFolder) return@forEach
            if (removeBookmarkById(bookmarks, id)) moved.add(item)
        }
        if (moved.isNotEmpty()) {
            targetChildren.addAll(moved)
            saveBookmarks()
        }
        bookmarkMultiSelect = false
        bookmarkSelected.clear()
        return moved.size
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

    // ---------------- 沉浸式页面（顶部取网页顶部色，底部取网页底部色） ----------------
    val pageTopThemeColor = activeWindow?.themeColor?.let { Color(it) }
    val pageBottomThemeColor = activeWindow?.bottomThemeColor?.let { Color(it) }
    val immersivePageActive = immersivePageEnabled && !showNewTabPage
    val topImmersiveActive = immersivePageActive && pageTopThemeColor != null
    val bottomImmersiveActive =
        immersivePageActive && (pageBottomThemeColor != null || pageTopThemeColor != null)

    val topChromeColor = if (topImmersiveActive) {
        pageTopThemeColor ?: surfaceColor
    } else {
        surfaceColor
    }
    val bottomChromeColor = if (bottomImmersiveActive) {
        pageBottomThemeColor ?: pageTopThemeColor ?: surfaceColor
    } else {
        surfaceColor
    }
    val topChrome = buildChromePalette(
        active = topImmersiveActive,
        backgroundColor = topChromeColor,
        surfaceContentColor = MiuixTheme.colorScheme.onSurface,
        surfaceSecondaryContentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        surfacePrimaryColor = MiuixTheme.colorScheme.primary,
        surfaceChipColor = buttonBg,
    )
    val bottomChrome = buildChromePalette(
        active = bottomImmersiveActive,
        backgroundColor = bottomChromeColor,
        surfaceContentColor = MiuixTheme.colorScheme.onSurface,
        surfaceSecondaryContentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        surfacePrimaryColor = MiuixTheme.colorScheme.primary,
        surfaceChipColor = buttonBg,
    )
    val titleChrome = if (headerAtBottom) bottomChrome else topChrome

    val toolbarColor = if (showNewTabPage) {
        Color.Transparent
    } else {
        bottomChrome.backgroundColor
    }

    val bottomBarIconTint: Color = if (showNewTabPage) {
        NewTabControlColorStore.color
            ?: if (LocalUiMode.current == UiMode.Material) {
                M3MaterialTheme.colorScheme.onSurface
            } else {
                MiuixTheme.colorScheme.onSurface
            }
    } else {
        bottomChrome.contentColor
    }

    // 沉浸式页面：同步系统状态栏/导航栏图标明暗
    ImmersiveSystemBarsEffect(
        topActive = topImmersiveActive,
        topIsDark = topChrome.isDark,
        bottomActive = bottomImmersiveActive,
        bottomIsDark = bottomChrome.isDark,
        showNewTabPage = showNewTabPage,
        isTop = isTop,
        isFullscreen = isFullscreen,
        themeIsDark = isUiDarkMode,
    )

    val videoResourceCount = (networkSniffedResources + sniffedResources)
        .distinctBy { it.url }
        .count { it.category == SniffCategory.VIDEO }
    val baseTool = when (dynamicToolIndex) {
        1 -> "home"
        2 -> "refresh"
        3 -> "fullscreen"
        4 -> "add_bookmark"
        5 -> "bookmarks"
        6 -> "history"
        7 -> "downloads"
        8 -> "translate"
        9 -> "sniff"
        else -> null
    }

    val isAnyHomePage = showNewTabPage || isAtHomePage()
    val effectiveDynamicTool: String? = when {
        dynamicToolSuggestEnabled && (hasActiveDownloads || hasActiveMoteDownloads) -> "downloads"
        dynamicToolSuggestEnabled && videoResourceCount > 1 -> "sniff"
        dynamicToolSuggestEnabled && videoResourceCount == 1 -> "play_in_player"
        dynamicToolSuggestEnabled && isEnglishPage -> "translate"
        dynamicToolSuggestEnabled && isAnyHomePage && baseTool == "home" -> "bookmarks"
        dynamicToolSuggestEnabled && showNewTabPage && baseTool != "bookmarks" && baseTool != "history" -> "bookmarks"
        else -> baseTool
    }
    fun runDynamicToolAction(toolId: String) {
        val id = activeWindowId
        MoteApplication.trackEventSafe("tool_used", mapOf("tool_id" to toolId))
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
                    openAddBookmarkDialog(prefillCurrent = true)
                }
            }
            "bookmarks" -> {
                clearSearchFocus()
                bookmarkMultiSelect = false
                bookmarkSelected.clear()
                bookmarkFolderPath.clear()
                showBookmarkSheet = true
            }
            "history" -> {
                clearSearchFocus()
                historyMultiSelect = false
                historySelected.clear()
                showHistorySheet = true
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
                    performSniff(id)
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
        }
    }

    val swipeTriggerPx = with(LocalDensity.current) { 72.dp.toPx() }
    val swipePanelWidthPx = with(LocalDensity.current) { 80.dp.toPx() }

    val headerDensity = LocalDensity.current
    val statusBarTopPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()
    val headerNavBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
        .takeIf { it > 0.dp }
        ?: with(headerDensity) { InitialInsetsFallback.navBarBottomPx.toDp() }
    val headerImeBottomPadding = WindowInsets.ime
        .asPaddingValues()
        .calculateBottomPadding()
    val headerBottomBarHeight =
        if (isFullscreen || htmlFullscreenView != null) 0.dp else BOTTOM_BAR_HEIGHT
    // 底部标题栏需要避让的高度：输入法 > 底部工具栏
    val headerBottomInset = maxOf(
        headerImeBottomPadding,
        headerNavBarBottomPadding + headerBottomBarHeight,
    )
    val headerVisible = !isFullscreen && htmlFullscreenView == null && !showNewTabPage
    val contentAnimAnchor: Alignment.Vertical =
        if (headerAtBottom) Alignment.Bottom else Alignment.Top
    val bottomHeaderContentHeight = with(headerDensity) {
        bottomHeaderContentHeightPx.toDp()
    }
    val bottomHeaderLift =
        if (headerAtBottom && headerVisible) bottomHeaderContentHeight else 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showSecuritySheet, searchBarBounds, securitySheetBounds) {
                if (!showSecuritySheet) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val down = event.changes.firstOrNull { it.changedToDown() }
                        if (down != null) {
                            val sb = searchBarBounds
                            val ssb = securitySheetBounds
                            val inSearchBar = sb != null && sb.contains(down.position)
                            val inSheet = ssb != null && ssb.contains(down.position)
                            if (!inSearchBar && !inSheet) {
                                showSecuritySheet = false
                            }
                        }
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            val browserHeaderInput: @Composable () -> Unit = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val faviconWindowState = rememberUpdatedState(activeWindow)
                    Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    val w = faviconWindowState.value
                                                    val u = w?.url.orEmpty()
                                                    if (u.isNotBlank() && u != "about:blank") {
                                                        showSecuritySheet = !showSecuritySheet
                                                    }
                                                },
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    FaviconOrFallback(
                                        favicon = activeWindow?.favicon,
                                        size = 22.dp,
                                        fallbackTint = titleChrome.contentColor,
                                    )
                                }
                                Spacer(Modifier.width(8.dp))

                                MoteUrlInputField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    onSearch = {
                                        loadUrlInActive(urlInput.text)
                                        focusManager.clearFocus()
                                        searchBarExpanded = false
                                    },
                                    expanded = searchBarExpanded,
                                    onExpandedChange = { searchBarExpanded = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .onGloballyPositioned { coords ->
                                            val b = coords.boundsInWindow()
                                            inputBounds = Rect(b.left, b.top, b.right, b.bottom)
                                        }
                                        .onFocusChanged { focusState: FocusState ->
                                            isSearchFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                showSecuritySheet = false
                                                activeWindow?.let { win ->
                                                    // 点击聚焦时总是显示当前网址（无论之前输入框显示什么），
                                                    // 再整体选中；没有网址时才退回原文本
                                                    val target = win.url.ifBlank { urlInput.text }
                                                    if (target.isNotEmpty()) {
                                                        // 聚焦时整体选中，并唤起文本工具栏
                                                        urlInput = TextFieldValue(
                                                            text = target,
                                                            selection = TextRange(0, target.length),
                                                        )
                                                        scope.launch {
                                                            delay(80)
                                                            runCatching {
                                                                textToolbar.showMenu(
                                                                    rect = inputBounds,
                                                                    onCopyRequested = {
                                                                        val sel = urlInput.selection
                                                                        val start = minOf(sel.start, sel.end)
                                                                        val end = maxOf(sel.start, sel.end)
                                                                        if (end > start) {
                                                                            copyToClipboard(
                                                                                context,
                                                                                urlInput.text.substring(start, end),
                                                                            )
                                                                        }
                                                                    },
                                                                    onCutRequested = {
                                                                        val sel = urlInput.selection
                                                                        val start = minOf(sel.start, sel.end)
                                                                        val end = maxOf(sel.start, sel.end)
                                                                        if (end > start) {
                                                                            copyToClipboard(
                                                                                context,
                                                                                urlInput.text.substring(start, end),
                                                                            )
                                                                            val next = urlInput.text.removeRange(start, end)
                                                                            urlInput = TextFieldValue(
                                                                                next,
                                                                                TextRange(start.coerceAtMost(next.length)),
                                                                            )
                                                                        }
                                                                    },
                                                                    onPasteRequested = {
                                                                        pastedText(context)?.let { text ->
                                                                            val sel = urlInput.selection
                                                                            val start = minOf(sel.start, sel.end)
                                                                            val end = maxOf(sel.start, sel.end)
                                                                            val next = urlInput.text.replaceRange(start, end, text)
                                                                            urlInput = TextFieldValue(
                                                                                next,
                                                                                TextRange(start + text.length),
                                                                            )
                                                                        }
                                                                    },
                                                                    onSelectAllRequested = {
                                                                        urlInput = TextFieldValue(
                                                                            urlInput.text,
                                                                            TextRange(0, urlInput.text.length),
                                                                        )
                                                                    },
                                                                )
                                                            }
                                                        }
                                                    } else if (urlInput.text == win.title) {
                                                        urlInput = TextFieldValue(win.url)
                                                    }
                                                }
                                            } else {
                                                searchBarExpanded = false
                                                activeWindow?.let { win ->
                                                    if (urlInput.text.isBlank() &&
                                                        !isHomeUrl(win.url)
                                                    ) {
                                                        urlInput = if (
                                                            headerTextMode == 1 &&
                                                            win.title.isNotBlank() &&
                                                            win.title != "无标题"
                                                        ) {
                                                            TextFieldValue(win.title)
                                                        } else {
                                                            TextFieldValue(win.url)
                                                        }
                                                    } else if (
                                                        headerTextMode == 1 &&
                                                        urlInput.text == win.url &&
                                                        win.title.isNotBlank() &&
                                                        win.title != "无标题"
                                                    ) {
                                                        // 聚焦时把标题换成了地址，未编辑就失焦则还原标题
                                                        urlInput = TextFieldValue(win.title)
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
                                                tint = titleChrome.contentColor,
                                                modifier = Modifier.size(25.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
            val browserHeaderCancel: @Composable () -> Unit = {
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

                                                else -> if (
                                                    headerTextMode == 1 &&
                                                    win.title.isNotBlank()
                                                ) {
                                                    TextFieldValue(win.title)
                                                } else {
                                                    TextFieldValue(win.url)
                                                }
                                            }
                                        }
                                        scope.launch {
                                            delay(200)
                                            searchBarExpanded = false
                                            forceHideHistory = false
                                        }
                                    },
                                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = titleChrome.accentColor,
                            )
            }
            val browserHeaderExpanded: @Composable ColumnScope.() -> Unit = {
                        var clipboardUrl by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(isSearchFocused) {
                            clipboardUrl = if (isSearchFocused) {
                                withContext(Dispatchers.IO) { readClipboardUrl(context, prefs) }
                            } else null
                        }
                        val clipUrl = clipboardUrl
                        val clipboardJumpSuggestion: @Composable () -> Unit = {
                        AnimatedVisibility(
                            visible = isSearchFocused && urlInput.text.isBlank() && clipUrl != null,
                            enter = expandVertically(
                                animationSpec = tween(220),
                                expandFrom = contentAnimAnchor,
                            ) + fadeIn(tween(160)),
                            exit = shrinkVertically(
                                animationSpec = tween(200),
                                shrinkTowards = contentAnimAnchor,
                            ) + fadeOut(tween(120)),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                        .clickable {
                                            clipUrl?.let {
                                                urlInput = TextFieldValue(it)
                                                loadUrlInActive(it)
                                                clearSearchFocus()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Link,
                                        contentDescription = null,
                                        tint = titleChrome.secondaryContentColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "复制的链接",
                                            fontSize = 14.sp,
                                            color = titleChrome.contentColor,
                                        )
                                        Text(
                                            text = clipUrl.orEmpty(),
                                            fontSize = 12.sp,
                                            color = titleChrome.secondaryContentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                        }
                        // 顶部标题栏：跳转建议在上，搜索历史在下避让
                        // 底部标题栏：跳转建议贴近输入框（最后渲染）
                        if (!headerAtBottom) clipboardJumpSuggestion()
                        AnimatedVisibility(
                            visible = isSearchFocused && urlInput.text.isNotBlank() && currentSuggestions.isNotEmpty(),
                            enter = expandVertically(
                                animationSpec = tween(220),
                                expandFrom = contentAnimAnchor,
                            ) + fadeIn(tween(160)),
                            exit = shrinkVertically(
                                animationSpec = tween(200),
                                shrinkTowards = contentAnimAnchor,
                            ) + fadeOut(tween(120)),
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp)
                                    .animateContentSize(),
                            ) {
                                items(
                                    count = currentSuggestions.size,
                                    key = { idx -> "suggest_${currentSuggestions[idx]}" },
                                ) { idx ->
                                    val item = currentSuggestions[idx]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem(fadeOutSpec = null)
                                            .clickable {
                                                urlInput = TextFieldValue(item)
                                                loadUrlInActive(item, forceSearch = true)
                                                clearSearchFocus()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Search,
                                            contentDescription = null,
                                            tint = titleChrome.secondaryContentColor,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = item,
                                            fontSize = 14.sp,
                                            color = titleChrome.contentColor,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = !forceHideHistory &&
                                    historyModuleEnabled &&
                                    searchHistory.isNotEmpty() &&
                                    urlInput.text.isBlank() &&
                                    activeWindow?.incognito != true,
                            enter = expandVertically(
                                animationSpec = tween(220),
                                expandFrom = contentAnimAnchor,
                            ) + fadeIn(tween(160)),
                            exit = shrinkVertically(
                                animationSpec = tween(200),
                                shrinkTowards = contentAnimAnchor,
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
                                            color = titleChrome.secondaryContentColor,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = "清空",
                                            fontSize = 12.sp,
                                            color = titleChrome.accentColor,
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
                                                loadUrlInActive(item, forceSearch = true)
                                                clearSearchFocus()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Recent,
                                            contentDescription = null,
                                            tint = titleChrome.secondaryContentColor,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = item,
                                            fontSize = 14.sp,
                                            color = titleChrome.contentColor,
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
                                                tint = titleChrome.secondaryContentColor,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (headerAtBottom) clipboardJumpSuggestion()
            }
            val browserHeaderEngine: @Composable () -> Unit = {
                val currentEngineIndex = prefs.getInt("default_search_engine", 0)
                    .coerceIn(0, searchEngines.lastIndex)
                AnimatedVisibility(
                    visible = isSearchResultPage && engineSwitcherBarEnabled,
                    enter = expandVertically(
                        animationSpec = tween(200),
                        expandFrom = contentAnimAnchor,
                    ) + fadeIn(tween(160)),
                    exit = shrinkVertically(
                        animationSpec = tween(180),
                        shrinkTowards = contentAnimAnchor,
                    ) + fadeOut(tween(120)),
                ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(titleChrome.backgroundColor)
                                .padding(
                                    start = 6.dp,
                                    end = 6.dp,
                                    top = if (headerAtBottom) 0.dp else 4.dp,
                                    bottom = if (headerAtBottom) 4.dp else 0.dp,
                                )
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
                                            if (selected) titleChrome.selectedChipColor
                                            else titleChrome.chipColor
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
                                        color = if (selected) titleChrome.selectedChipTextColor
                                        else titleChrome.contentColor,
                                    )
                                }
                            }
                        }
            }
            }
            val browserHeaderTrailing: @Composable () -> Unit = {
                AnimatedVisibility(
                    visible = isSearchFocused &&
                            quickInputBarEnabled &&
                            !isFullscreen &&
                            htmlFullscreenView == null,
                    enter = expandVertically(
                        animationSpec = tween(200),
                        expandFrom = Alignment.Bottom,
                    ) + fadeIn(tween(160)),
                    exit = shrinkVertically(
                        animationSpec = tween(180),
                        shrinkTowards = Alignment.Bottom,
                    ) + fadeOut(tween(120)),
                ) {
                    QuickInputBarRow(
                        backgroundColor = titleChrome.backgroundColor,
                        buttonBackground = titleChrome.chipColor,
                        textColor = titleChrome.contentColor,
                        onInsert = { insertText(it) },
                    )
                }
            }

            if (!headerAtBottom) {
                AnimatedVisibility(
                    visible = headerVisible,
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
                    BrowserHeader(
                        atBottom = false,
                        expanded = searchBarExpanded,
                        onExpandedChange = { searchBarExpanded = it },
                        inputField = browserHeaderInput,
                        outsideEndAction = browserHeaderCancel,
                        engineSwitcher = browserHeaderEngine,
                        content = browserHeaderExpanded,
                        containerColor = topChrome.backgroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                searchBarBounds = coords.boundsInWindow()
                            },
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = expandVertically(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(tween(FULLSCREEN_ANIM_MS)),
                    exit = shrinkVertically(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        shrinkTowards = Alignment.Top,
                    ) + fadeOut(tween(FULLSCREEN_ANIM_MS)),
                ) {
                    // 底部标题栏模式下，顶部只为系统状态栏留空（沉浸式取网页顶部色）
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(statusBarTopPadding)
                            .background(topChrome.backgroundColor)
                    )
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

                                val canBack = canNavigateBack()
                                val canForward = canNavigateForward()

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
                                    if (direction == -1) performNavigateBack()
                                    if (direction == 1) performNavigateForward()
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
                        headerAtBottom -> 0.dp
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
                        onLongPress = { imageUrl, linkUrl, text, pageUrl, xPx, yPx, selectable ->
                            val filename = if (imageUrl.isNotBlank()) {
                                URLUtil.guessFileName(imageUrl, null, null)
                                    .ifBlank { "image_${System.currentTimeMillis()}.jpg" }
                            } else ""
                            longPressInfo = LongPressInfo(
                                imageUrl = imageUrl,
                                linkUrl = linkUrl,
                                text = text,
                                pageUrl = pageUrl,
                                filename = filename,
                                xPx = xPx,
                                yPx = yPx,
                                selectable = selectable,
                            )
                            longPressMenuVisible = true
                            longPressCounter++
                        },
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
                        onExternalSchemeRequest = { pendingExternalSchemeUrl = it },
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

            if (headerAtBottom) {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = expandVertically(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        expandFrom = Alignment.Bottom,
                    ) + fadeIn(tween(FULLSCREEN_ANIM_MS)) + scaleIn(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        initialScale = 0.92f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        shrinkTowards = Alignment.Bottom,
                    ) + fadeOut(tween(FULLSCREEN_ANIM_MS)) + scaleOut(
                        animationSpec = tween(FULLSCREEN_ANIM_MS, easing = FULLSCREEN_EASING),
                        targetScale = 0.92f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                    ),
                ) {
                    BrowserHeader(
                        atBottom = true,
                        bottomInset = headerBottomInset,
                        expanded = searchBarExpanded,
                        onExpandedChange = { searchBarExpanded = it },
                        inputField = browserHeaderInput,
                        outsideEndAction = browserHeaderCancel,
                        engineSwitcher = browserHeaderEngine,
                        content = browserHeaderExpanded,
                        trailing = browserHeaderTrailing,
                        containerColor = bottomChrome.backgroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                searchBarBounds = coords.boundsInWindow()
                                val insetPx = with(headerDensity) {
                                    headerBottomInset.roundToPx()
                                }
                                bottomHeaderContentHeightPx =
                                    (coords.size.height - insetPx).coerceAtLeast(0)
                            },
                    )
                }
            }
        }

        val navBarBottomPadding = headerNavBarBottomPadding
        val bottomBarHeight = headerBottomBarHeight
        val floatingBottomInset = headerBottomInset
        // 底部标题栏模式下，悬浮栏需要让开标题栏本身
        val bottomBarOverlayOffset = floatingBottomInset + bottomHeaderLift

        SnackbarHost(
            state = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(999f)
                .padding(
                    bottom = navBarBottomPadding + bottomBarHeight + bottomHeaderLift
                )
                .imePadding(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarOverlayOffset),
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
                            containerColor = bottomChrome.backgroundColor,
                contentColor = bottomChrome.contentColor,
)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarOverlayOffset),
        ) {
            TtsBar(
                show = showTtsBar,
                isPlaying = isTtsPlaying,
                currentIndex = ttsCurrentIndex,
                totalCount = ttsTextChunks.size,
                currentText = ttsTextChunks.getOrNull(ttsCurrentIndex) ?: "",
                onPlayPause = {
                    if (isTtsPlaying) {
                        stopTts()
                    } else {
                        speakChunk(ttsCurrentIndex)
                    }
                },
                onPrev = {
                    if (ttsCurrentIndex > 0) {
                        stopTts()
                        speakChunk(ttsCurrentIndex - 1)
                    }
                },
                onNext = {
                    if (ttsCurrentIndex < ttsTextChunks.lastIndex) {
                        stopTts()
                        speakChunk(ttsCurrentIndex + 1)
                    }
                },
                onClose = {
                    showTtsBar = false
                    stopTts()
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarOverlayOffset),
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
                            containerColor = bottomChrome.backgroundColor,
                contentColor = bottomChrome.contentColor,
)
        }

        val dialogInfo = pendingDialogInfo
        if (dialogInfo != null) {
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarOverlayOffset),
        ) {
            AnimatedVisibility(
                visible = isSearchFocused &&
                        !isFullscreen &&
                        htmlFullscreenView == null &&
                        quickInputBarEnabled &&
                        !headerAtBottom,
                enter = expandVertically(
                    animationSpec = tween(200),
                    expandFrom = Alignment.Bottom,
                ) + fadeIn(tween(160)),
                exit = shrinkVertically(
                    animationSpec = tween(180),
                    shrinkTowards = Alignment.Bottom,
                ) + fadeOut(tween(120)),
            ) {
                QuickInputBarRow(
                    backgroundColor = bottomChrome.backgroundColor,
                    buttonBackground = bottomChrome.chipColor,
                    textColor = bottomChrome.contentColor,
                    onInsert = { insertText(it) },
                )
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
        onSearchSuggestion = { input -> loadUrlInActive(input, forceSearch = true) },
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
                    if (direction < 0) performNavigateBack()
                    else if (direction > 0) performNavigateForward()
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
                onOpenNewTabCustomization = onOpenNewTabCustomization,
                onOpenCrop = onOpenCrop,
                onRequestDownload = { dl ->
                    pendingDownload = dl
                    showDownloadDialog = true
                },
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
            val canGoBack = canNavigateBack()
            val canGoForward = canNavigateForward()
            val dynamicTool = effectiveDynamicTool?.let { tid ->
                defaultTools().find { it.id == tid }
                    ?: if (tid == "add_bookmark") {
                        ToolDef("add_bookmark", "添加书签", MiuixIcons.Favorites)
                    } else {
                        null
                    }
            }

            val disabledInNewTab = setOf("translate", "zoom", "find_in_page", "tts", "sniff", "save_page", "print", "share", "play_in_player")
            val dynamicToolEnabled = !(showNewTabPage && effectiveDynamicTool in disabledInNewTab)

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
                    id = "dynamic",
                    visible = dynamicTool != null,
                    enabled = dynamicToolEnabled,
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
                                    tint = if (dynamicToolEnabled) onSurface else onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Spacer(Modifier.size(24.dp))
                            }
                        }
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
                            pulseTrigger = windowIconPulse,
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
                    when (id) {
                        "back" -> performNavigateBack()
                        "forward" -> performNavigateForward()
                        "tools" -> showToolSheet = true
                        "windows" -> showWindowSheet = true
                        "dynamic" -> effectiveDynamicTool?.let { runDynamicToolAction(it) }
                    }
                },
                navBarBottomPadding = navBarBottomPadding2,
                backgroundColor = toolbarColor,
            )
        }

        val sb = searchBarBounds
        if (!headerAtBottom && sb != null) {
            val dd = LocalDensity.current
            val menuTopDp = with(dd) { sb.bottom.toDp() }
            BrowserSecuritySheetPopup(
                visible = showSecuritySheet,
                page = securityPage,
                info = secInfo,
                atBottom = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(201f)
                    .offset(y = menuTopDp),
                onBoundsChanged = { securitySheetBounds = it },
                onPageChange = { securityPage = it },
                onOpenSiteSettings = onOpenSiteSettings,
                onToggleBlockAds = { enabled ->
                    val host = secInfo.host
                    if (host.isNotBlank()) {
                        SecurityStateHolder.setSiteBlockAds(host, enabled)
                    }
                },
                onToggleTrackingProtection = { enabled ->
                    val host = secInfo.host
                    if (host.isNotBlank()) {
                        SecurityStateHolder.setSiteTrackingProtection(host, enabled)
                    }
                },
                onOpenCookieSettings = {
                    showSecuritySheet = false
                    onOpenSettings()
                },
            )
        }

        if (headerAtBottom) {
            BrowserSecuritySheetPopup(
                visible = showSecuritySheet && headerVisible,
                page = securityPage,
                info = secInfo,
                atBottom = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomBarOverlayOffset)
                    .zIndex(201f),
                onBoundsChanged = { securitySheetBounds = it },
                onPageChange = { securityPage = it },
                onOpenSiteSettings = onOpenSiteSettings,
                onToggleBlockAds = { enabled ->
                    val host = secInfo.host
                    if (host.isNotBlank()) {
                        SecurityStateHolder.setSiteBlockAds(host, enabled)
                    }
                },
                onToggleTrackingProtection = { enabled ->
                    val host = secInfo.host
                    if (host.isNotBlank()) {
                        SecurityStateHolder.setSiteTrackingProtection(host, enabled)
                    }
                },
                onOpenCookieSettings = {
                    showSecuritySheet = false
                    onOpenSettings()
                },
            )
        }

        LaunchedEffect(longPressMenuVisible) {
            if (!longPressMenuVisible && longPressInfo != null) {
                delay(400L)
                if (!longPressMenuVisible) {
                    longPressInfo = null
                }
            }
        }

        BackHandler(enabled = longPressMenuVisible) {
            longPressMenuVisible = false
        }

        val lpInfo = longPressInfo
        if (lpInfo != null) {
            val longPressMenuContent: @Composable () -> Unit = {
                val currentEngineIndex = prefs.getInt("default_search_engine", 0)

                key("lp_${longPressCounter}") {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(lpInfo.xPx, lpInfo.yPx) }
                        .size(1.dp)
                ) {
                    WindowCascadingListPopup(
                        show = longPressMenuVisible,
                        entries = buildList {
                            // ===== 组1：跳转链接 =====
                            val openItems = mutableListOf<DropdownItem>()
                            if (lpInfo.linkUrl.isNotBlank()) {
                                openItems.add(
                                    DropdownItem(
                                        text = "新窗口中打开",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Forward,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            createWindow(lpInfo.linkUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                                openItems.add(
                                    DropdownItem(
                                        text = "后台窗口打开",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Add,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            createBackgroundWindow(lpInfo.linkUrl)
                                            windowIconPulse++
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (openItems.isNotEmpty()) {
                                add(DropdownEntry(items = openItems))
                            }

                            // ===== 组2：图片操作 + 搜索 + 选中文字（同一组，内部无分割线）=====
                            val imageItems = mutableListOf<DropdownItem>()
                            if (lpInfo.imageUrl.isNotBlank()) {
                                imageItems.add(
                                    DropdownItem(
                                        text = "查看图片",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Photos,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            createWindow(lpInfo.imageUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                                imageItems.add(
                                    DropdownItem(
                                        text = "下载图片",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Download,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            val mime = guessMimeFromUrl(lpInfo.imageUrl)
                                                ?: "image/*"
                                            pendingDownload = PendingDownload(
                                                url = lpInfo.imageUrl,
                                                userAgent = getMobileUserAgent(context),
                                                contentDisposition = null,
                                                mimetype = mime,
                                                fileName = lpInfo.filename.ifBlank { "image.jpg" },
                                            )
                                            longPressMenuVisible = false
                                            showDownloadDialog = true
                                        },
                                    )
                                )
                            }
                            if (lpInfo.imageUrl.isNotBlank() || lpInfo.text.isNotBlank()) {
                                imageItems.add(
                                    DropdownItem(
                                        text = "搜索",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Search,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            val imgUrl = lpInfo.imageUrl
                                            val searchText = lpInfo.text
                                            val refUrl = activeWindow?.url ?: ""
                                            val engIdx = currentEngineIndex
                                            longPressMenuVisible = false
                                            if (imgUrl.isNotBlank()) {
                                                scope.launch {
                                                    val resultUrl = performReverseImageSearch(
                                                        context = context,
                                                        imageUrl = imgUrl,
                                                        referer = refUrl,
                                                        engineIndex = engIdx,
                                                    )
                                                    if (!resultUrl.isNullOrBlank()) {
                                                        createWindow(resultUrl)
                                                    } else {
                                                        snackbarHostState.showSnackbar(
                                                            message = "以图搜图失败，请稍后重试",
                                                            duration = SnackbarDuration.Short,
                                                        )
                                                    }
                                                }
                                            } else {
                                                val engine = searchEngines.getOrElse(engIdx) {
                                                    searchEngines[0]
                                                }
                                                val url = engine.second.replace(
                                                    "%s",
                                                    URLEncoder.encode(searchText, "UTF-8"),
                                                )
                                                createWindow(url)
                                            }
                                        },
                                    )
                                )
                            }
                            if (lpInfo.text.isNotBlank() && lpInfo.selectable) {
                                imageItems.add(
                                    DropdownItem(
                                        text = "选中文字",
                                        icon = { m ->
                                            Icon(
                                                imageVector = MiuixIcons.Rename,
                                                contentDescription = null,
                                                modifier = m,
                                            )
                                        },
                                        onClick = {
                                            longPressMenuVisible = false
                                            val wv = activeWindowId?.let { webViewMap[it] }
                                            if (wv != null) {
                                                try {
                                                    val loc = IntArray(2)
                                                    wv.getLocationOnScreen(loc)
                                                    val localX = (lpInfo.xPx - loc[0]).toFloat()
                                                    val localY = (lpInfo.yPx - loc[1]).toFloat()
                                                    LongPressSkipStore.setSkip(wv)
                                                    wv.evaluateJavascript(
                                                        "(function(){try{var s=window.getSelection();if(s)s.removeAllRanges();}catch(e){}})()",
                                                        null
                                                    )
                                                    wv.requestFocus()
                                                    val downTime = SystemClock.uptimeMillis()
                                                    val downEv = MotionEvent.obtain(
                                                        downTime, downTime,
                                                        MotionEvent.ACTION_DOWN,
                                                        localX, localY, 0,
                                                    )
                                                    wv.dispatchTouchEvent(downEv)
                                                    downEv.recycle()
                                                    val upDelay = ViewConfiguration.getLongPressTimeout().toLong() + 150L
                                                    wv.postDelayed({
                                                        try {
                                                            val upEv = MotionEvent.obtain(
                                                                downTime, SystemClock.uptimeMillis(),
                                                                MotionEvent.ACTION_UP,
                                                                localX, localY, 0,
                                                            )
                                                            wv.dispatchTouchEvent(upEv)
                                                            upEv.recycle()
                                                        } catch (_: Exception) {}
                                                    }, upDelay)
                                                } catch (_: Exception) {}
                                            }
                                        },
                                    )
                                )
                            }
                            if (imageItems.isNotEmpty()) {
                                add(DropdownEntry(items = imageItems))
                            }

                            // ===== 组4：复制 / 分享（二级菜单）=====
                            val copyShareChildren = mutableListOf<DropdownItem>()
                            if (lpInfo.linkUrl.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "复制跳转链接",
                                        onClick = {
                                            copyTextToClipboard(context, lpInfo.linkUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.imageUrl.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "复制图片链接",
                                        onClick = {
                                            copyTextToClipboard(context, lpInfo.imageUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.text.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "复制文本",
                                        onClick = {
                                            copyTextToClipboard(context, lpInfo.text)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.linkUrl.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "分享跳转链接",
                                        onClick = {
                                            shareTextUrl(context, lpInfo.linkUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.imageUrl.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "分享图片链接",
                                        onClick = {
                                            shareTextUrl(context, lpInfo.imageUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.text.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "分享文本",
                                        onClick = {
                                            sharePlainText(context, lpInfo.text)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (lpInfo.imageUrl.isNotBlank()) {
                                copyShareChildren.add(
                                    DropdownItem(
                                        text = "分享图片",
                                        onClick = {
                                            shareImageUrl(context, lpInfo.imageUrl)
                                            longPressMenuVisible = false
                                        },
                                    )
                                )
                            }
                            if (copyShareChildren.isNotEmpty()) {
                                add(
                                    DropdownEntry(
                                        items = listOf(
                                            DropdownItem(
                                                text = "复制 / 分享",
                                                icon = { m ->
                                                    Icon(
                                                        imageVector = MiuixIcons.Copy,
                                                        contentDescription = null,
                                                        modifier = m,
                                                    )
                                                },
                                                children = copyShareChildren,
                                            )
                                        )
                                    )
                                )
                            }
                        },
                        onDismissRequest = {
                            longPressMenuVisible = false
                            // ★ 不立即清 longPressInfo，保留关闭动画
                        },
                        onDismissFinished = { longPressInfo = null },
                        minWidth = 180.dp,
                        enableWindowDim = false,
                        dropdownColors = DropdownDefaults.dropdownColors(),
                    )
                }
            }
            }
            longPressMenuContent()
        }
    }

    val currentUrl = activeWindow?.url ?: ""
    val isCurrentPageBookmarked = remember(currentUrl, bookmarks.toList()) {
        bookmarks.any { !it.isFolder && it.url == currentUrl }
    }

    BrowserToolSheet(
        show = showToolSheet,
        pageTitle = if (showNewTabPage) "新标签页"
        else (activeWindow?.title?.ifBlank { "无标题" } ?: "新标签页"),
        isFullscreen = isFullscreen,
        isNightMode = activeWindow?.nightMode == true,
        isIncognito = activeWindow?.incognito == true,
        isDesktopMode = activeWindow?.desktopMode == true,
        isNoImage = activeWindow?.noImage == true,
        isTranslated = activeWindow?.translated == true,
        isNewTabPage = showNewTabPage,
        isBookmarked = isCurrentPageBookmarked,
        onAddBookmark = {
            val win = activeWindow
            if (win != null && win.url.isNotBlank() && win.url != "about:blank") {
                val title = win.title.ifBlank { win.url }
                if (bookmarks.none { it.url == win.url }) {
                    bookmarks.add(0, BookmarkItem(title = title, url = win.url, timestamp = System.currentTimeMillis()))
                    saveBookmarks()
                    val host = try { win.url.toUri().host } catch (_: Exception) { null }
                    if (!host.isNullOrBlank()) {
                        val bmp = faviconCache[host] ?: win.favicon
                        if (bmp != null) {
                            scope.launch(Dispatchers.IO) {
                                FaviconStore.save(context, host, bmp)
                            }
                        }
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar(message = "已添加到书签", duration = SnackbarDuration.Short)
                    }
                }
            }
        },
        onRemoveBookmark = {
            val win = activeWindow
            if (win != null && win.url.isNotBlank()) {
                val existingIndex = bookmarks.indexOfFirst { !it.isFolder && it.url == win.url }
                if (existingIndex >= 0) {
                    bookmarks.removeAt(existingIndex)
                    saveBookmarks()
                    scope.launch {
                        snackbarHostState.showSnackbar(message = "已删除书签", duration = SnackbarDuration.Short)
                    }
                }
            }
        },
        onOpenSettings = {
            showToolSheet = false
            onOpenSettings()
        },
        onOpenToolCustomize = {
            showToolSheet = false
            onOpenToolCustomize()
        },
        onDismiss = { showToolSheet = false },
        onToolAction = { toolId, isActive ->
            val id = activeWindowId
            MoteApplication.trackEventSafe("tool_used", mapOf("tool_id" to toolId))
            when (toolId) {
                "home" -> {
                    goToHomePage()
                }
                "night_mode" -> id?.let { windowId ->
                    if (prefs.getInt("web_night_mode", 0) == 0) {
                        pendingNightModeTarget = isActive
                        showNightModeConfirmDialog = true
                    } else {
                        updateWindow(windowId) { it.nightMode = isActive }
                        webViewMap[windowId]?.let { applyNightModeToWebView(it, isActive) }
                        prefs.edit().putInt("web_night_mode", if (isActive) 2 else 1).apply()
                    }
                }
                "no_image" -> id?.let { windowId ->
                    updateWindow(windowId) { it.noImage = isActive }
                    webViewMap[windowId]?.evaluateJavascript(if (isActive) JS_NO_IMG_ON else JS_NO_IMG_OFF, null)
                }
                "desktop_mode" -> id?.let { windowId ->
                    updateWindow(windowId) { it.desktopMode = isActive }
                    webViewMap[windowId]?.let { wv ->
                        wv.settings.userAgentString = UaStore.effectiveUa(context, isActive)
                            ?: if (isActive) DESKTOP_UA else getMobileUserAgent(context)
                        wv.reload()
                    }
                }
                "incognito" -> id?.let { applyIncognitoMode(it, isActive) }
                "translate" -> id?.let { windowId ->
                    updateWindow(windowId) { it.translated = isActive }
                    setPageTranslated(isActive)
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
                "view_source" -> {
                    if (id != null && !showNewTabPage) {
                        webViewMap[id]?.evaluateJavascript("document.documentElement.outerHTML") { raw ->
                            sourceContent = decodeJsString(raw)
                            showSourceDialog = true
                        }
                    }
                }
                "run_script" -> {
                    ScriptsNavArgs.editingId = null
                    onOpenScripts()
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
                "exit" -> {
                    SessionSnapshotStore.userExiting = true
                    prefs.edit().putString("session_snapshot", "").commit()
                    (context as? Activity)?.finish()
                }
                "downloads" -> onOpenDownloads()
                "tts" -> id?.let { windowId ->
                    if (ttsInitFailed) {
                        showTtsErrorDialog = true
                        ttsRetryKey++
                        return@let
                    }
                    if (!ttsReady) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "语音引擎正在初始化，请稍后再试",
                                duration = SnackbarDuration.Short,
                            )
                        }
                        return@let
                    }
                    val wv = webViewMap[windowId]
                    if (wv != null) {
                        wv.evaluateJavascript(JS_EXTRACT_ARTICLE_TEXT) { raw ->
                            val jsonStr = decodeJsString(raw)
                            if (jsonStr.isBlank() || jsonStr == "[]") {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "未找到可朗读的文字",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                                return@evaluateJavascript
                            }
                            try {
                                val arr = JSONArray(jsonStr)
                                val chunks = mutableListOf<String>()
                                for (i in 0 until arr.length()) {
                                    val s = arr.optString(i, "").trim()
                                    if (s.isNotBlank()) chunks.add(s)
                                }
                                if (chunks.isEmpty()) return@evaluateJavascript

                                ttsTextChunks = chunks
                                showTtsBar = true
                                speakChunk(0)
                            } catch (_: Exception) {}
                        }
                    }
                }
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
        closedHistory = closedHistory,
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
        onToggleSelectAllFiltered = { filteredUrls ->
            if (historySelected.size == filteredUrls.size && filteredUrls.isNotEmpty()) {
                historySelected.clear()
            } else {
                historySelected.clear()
                historySelected.addAll(filteredUrls)
            }
        },
        onDeleteSelected = {
            val toRemove = historySelected.toSet()
            history.removeAll { it.url in toRemove }
            closedHistory.removeAll { it.url in toRemove }
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
        isNewTabPage = showNewTabPage,
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
        onToggleSelectAllFiltered = { filteredIds ->
            if (bookmarkSelected.size == filteredIds.size && filteredIds.isNotEmpty()) {
                bookmarkSelected.clear()
            } else {
                bookmarkSelected.clear()
                bookmarkSelected.addAll(filteredIds)
            }
        },
        onDeleteSelected = {
            val toRemove = bookmarkSelected.toSet()
            toRemove.forEach { id ->
                removeBookmarkById(bookmarks, id)
            }
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
        onShareSelected = { selectedIds ->
            val items = mutableListOf<String>()
            fun collectUrls(itemsList: List<BookmarkItem>) {
                itemsList.forEach {
                    if (it.id in selectedIds && !it.isFolder) {
                        items.add("【${it.title.ifBlank { "无标题" }}】${it.url}")
                    }
                    if (it.isFolder) {
                        collectUrls(it.children)
                    }
                }
            }
            collectUrls(bookmarks)
            if (items.isNotEmpty()) {
                val text = items.joinToString("\n")
                try {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(
                        Intent.createChooser(send, "分享网页").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                } catch (_: Exception) {}
            }
        },
        onMoveSelected = { showMoveBookmarksDialog = true },
        onEditSelected = { id ->
            val item = findBookmarkById(bookmarks, id)
            if (item != null) {
                editingBookmarkId = id
                bookmarkTitleInput = item.title
                bookmarkUrlInput = item.url ?: ""
                bookmarkTargetFolderId = findParentFolderId(bookmarks, id)
                addFromBookmarkSheet = true
                showBookmarkSheet = false
                bookmarkMultiSelect = false
                bookmarkSelected.clear()
                scope.launch {
                    delay(280)
                    showAddBookmarkDialog = true
                }
            }
        },
        canMove = collectAllFolders(bookmarks).isNotEmpty(),
        faviconReload = bookmarkFaviconReload,
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
        title = if (editingBookmarkId != null) "编辑书签" else "添加书签",
        onDismissRequest = {
            showAddBookmarkDialog = false
            editingBookmarkId = null
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
                        val editId = editingBookmarkId
                        if (editId != null) {
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
                            val item = findBookmarkById(bookmarks, editId)
                            if (item != null) {
                                val target = getTargetFolderItems()
                                val parent = findParentList(bookmarks, editId)
                                val updated = item.copy(
                                    title = title,
                                    url = url,
                                    timestamp = System.currentTimeMillis(),
                                )
                                if (parent != null && parent === target) {
                                    val idx = parent.indexOfFirst { it.id == editId }
                                    if (idx >= 0) parent[idx] = updated
                                } else {
                                    removeBookmarkById(bookmarks, editId)
                                    target.add(0, updated)
                                }
                                saveBookmarks()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "书签已保存",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            }
                            showAddBookmarkDialog = false
                            editingBookmarkId = null
                            bookmarkTitleInput = ""
                            bookmarkUrlInput = ""
                            bookmarkTargetFolderId = null
                            return@Button
                        }
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

                            // ★ 把当前窗口已拿到的图标落到 FaviconStore 磁盘缓存。
                            //   手动输入网址时 faviconCache 里可能没有（从没在当前窗口加载过），
                            //   这时若 host 与当前窗口一致，退回用窗口自身的 favicon。
                            val urlHost = try { url.toUri().host } catch (_: Exception) { null }
                            if (!urlHost.isNullOrBlank()) {
                                val winHost = try { activeWindow?.url?.toUri()?.host } catch (_: Exception) { null }
                                val bmp = faviconCache[urlHost]
                                    ?: activeWindow?.favicon?.takeIf { winHost == urlHost }
                                if (bmp != null) {
                                    scope.launch(Dispatchers.IO) {
                                        FaviconStore.save(context, urlHost, bmp)
                                    }
                                }
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
                ) { Text(if (editingBookmarkId != null) "保存" else "添加") }
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

    WindowDialog(
        show = showMoveBookmarksDialog,
        title = "移动到",
        onDismissRequest = { showMoveBookmarksDialog = false }
    ) {
        val folders = collectAllFolders(bookmarks)
        val options = buildList {
            add("根目录" to 0)
            folders.forEach { (folder, depth) -> add(folder.title to depth) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            options.forEachIndexed { index, option ->
                val title = option.first
                val depth = option.second
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer)
                        .clickable {
                            val targetId = if (index == 0) null else folders.getOrNull(index - 1)?.first?.id
                            val moved = moveSelectedBookmarks(targetId)
                            showMoveBookmarksDialog = false
                            if (moved > 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "已移动 $moved 项",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            }
                        }
                        .padding(
                            start = 16.dp + (depth * 16).dp,
                            end = 16.dp,
                            top = 14.dp,
                            bottom = 14.dp,
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Folder,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }

    var lastNightModeTarget by remember { mutableStateOf<Boolean?>(null) }
    if (pendingNightModeTarget != null) {
        lastNightModeTarget = pendingNightModeTarget
    }
    WindowDialog(
        show = pendingNightModeTarget != null && showNightModeConfirmDialog,
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
                text = if (lastNightModeTarget == true) {
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
                        val target = lastNightModeTarget
                        val id = activeWindowId
                        if (target != null && id != null) {
                            prefs.edit()
                                .putInt("web_night_mode", if (target) 2 else 1)
                                .apply()
                            updateWindow(id) { it.nightMode = target }
                            webViewMap[id]?.let { applyNightModeToWebView(it, target) }
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

    WindowBottomSheet(
        show = showSourceDialog,
        onDismissRequest = {
            showSourceDialog = false
            isSourceSearchActive = false
            sourceWebViewRef?.clearMatches()
            sourceSearchQuery = ""
            sourceMatchCurrent = 0
            sourceMatchTotal = 0
        },
        title = "网页源码",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSourceSearchActive) {
                    // 搜索激活状态：页面查找同款输入框 + Close 按钮
                    BasicTextField(
                        value = sourceSearchQuery,
                        onValueChange = { query ->
                            sourceSearchQuery = query
                            val wv = sourceWebViewRef ?: return@BasicTextField
                            if (query.isBlank()) {
                                wv.clearMatches()
                                sourceMatchCurrent = 0
                                sourceMatchTotal = 0
                            } else {
                                wv.findAllAsync(query)
                            }
                        },
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
                                if (sourceSearchQuery.isEmpty()) {
                                    Text(
                                        text = "搜索源码",
                                        fontSize = 13.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            isSourceSearchActive = false
                            sourceSearchQuery = ""
                            sourceWebViewRef?.clearMatches()
                            sourceMatchCurrent = 0
                            sourceMatchTotal = 0
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭搜索",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    // 默认状态：搜索按钮在左，所有选项居中
                    IconButton(
                        onClick = { isSourceSearchActive = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Search,
                            contentDescription = "搜索源码",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (sourceMatchTotal > 0) "${sourceMatchCurrent}/${sourceMatchTotal}" else "0/0",
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.Center,
                            )
                            IconButton(
                                onClick = {
                                    val wv = sourceWebViewRef ?: return@IconButton
                                    if (sourceSearchQuery.isNotBlank()) wv.findNext(false)
                                },
                                enabled = sourceMatchTotal > 0,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.ChevronBackward,
                                    contentDescription = "上一个",
                                    tint = if (sourceMatchTotal > 0) MiuixTheme.colorScheme.onSurface
                                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(
                                onClick = {
                                    val wv = sourceWebViewRef ?: return@IconButton
                                    if (sourceSearchQuery.isNotBlank()) wv.findNext(true)
                                },
                                enabled = sourceMatchTotal > 0,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.ChevronForward,
                                    contentDescription = "下一个",
                                    tint = if (sourceMatchTotal > 0) MiuixTheme.colorScheme.onSurface
                                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(
                                onClick = {
                                    sourceWrapEnabled = !sourceWrapEnabled
                                    sourceWebViewRef?.evaluateJavascript(
                                        "window.moteSetWrap($sourceWrapEnabled);", null
                                    )
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.VerticalSplit,
                                    contentDescription = if (sourceWrapEnabled) "关闭自动换行" else "开启自动换行",
                                    tint = if (sourceWrapEnabled) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newSize = (sourceFontSize + 1).coerceAtMost(20)
                                    sourceFontSize = newSize
                                    sourceWebViewRef?.evaluateJavascript(
                                        "window.moteSetFontSize($newSize);", null
                                    )
                                },
                                enabled = sourceFontSize < 20,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.ExpandMore,
                                    contentDescription = "增大字号",
                                    tint = if (sourceFontSize < 20) MiuixTheme.colorScheme.onSurface
                                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newSize = (sourceFontSize - 1).coerceAtLeast(9)
                                    sourceFontSize = newSize
                                    sourceWebViewRef?.evaluateJavascript(
                                        "window.moteSetFontSize($newSize);", null
                                    )
                                },
                                enabled = sourceFontSize > 9,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.ExpandLess,
                                    contentDescription = "减小字号",
                                    tint = if (sourceFontSize > 9) MiuixTheme.colorScheme.onSurface
                                    else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    // 右侧占位，与左侧搜索按钮宽度一致，确保中间按钮组绝对居中
                    Spacer(Modifier.size(32.dp))
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 20.dp), // ★ 增加一点下间距
            ) {
                SourceWebView(
                    html = sourceContent,
                    wrapEnabled = sourceWrapEnabled,
                    fontSizeSp = sourceFontSize,
                    onWebViewCreated = { wv ->
                        sourceWebViewRef = wv
                        wv.setFindListener { activeOrdinal, totalMatches, _ ->
                            sourceMatchCurrent = activeOrdinal + 1
                            sourceMatchTotal = totalMatches
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showSourceDialog = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("关闭") }
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("html", sourceContent))
                        Toast.makeText(context, "源码已复制", Toast.LENGTH_SHORT).show()
                        showSourceDialog = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("复制源码") }
            }
        }
    }

    WindowDialog(
        show = showScriptDialog,
        title = "执行脚本",
        onDismissRequest = { showScriptDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = scriptContent,
                onValueChange = { scriptContent = it },
                label = "JavaScript 代码",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showScriptDialog = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        if (scriptContent.isNotBlank()) {
                            activeWindowId?.let { wid ->
                                webViewMap[wid]?.evaluateJavascript(scriptContent, null)
                                Toast.makeText(context, "脚本已执行", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showScriptDialog = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("运行") }
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

    val extUrl = pendingExternalSchemeUrl
    var lastExtUrl by remember { mutableStateOf("") }
    if (extUrl != null) lastExtUrl = extUrl
    WindowDialog(
        show = extUrl != null,
        title = "打开外部应用",
        onDismissRequest = { pendingExternalSchemeUrl = null },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "该网页试图打开第三方应用",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
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
                    onClick = { pendingExternalSchemeUrl = null },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        try {
                            if (lastExtUrl.startsWith("intent://")) {
                                val intent = Intent.parseUri(lastExtUrl, Intent.URI_INTENT_SCHEME)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW, lastExtUrl.toUri())
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } catch (e: android.content.ActivityNotFoundException) {
                            Toast.makeText(context, "未安装该应用，无法打开", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "打开失败：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        pendingExternalSchemeUrl = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("打开")
                }
            }
        }
    }

    val sslError = PendingSslErrorStore.pending
    var lastSslError by remember { mutableStateOf<PendingSslError?>(null) }
    if (sslError != null) lastSslError = sslError
    WindowDialog(
        show = sslError != null,
        title = "安全警告",
        onDismissRequest = {
            lastSslError?.cancel()
            PendingSslErrorStore.pending = null
        },
    ) {
        if (lastSslError != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "${lastSslError!!.errorText}\n\n" +
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
                            lastSslError?.cancel()
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
                            lastSslError?.proceed()
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

    val jsDialog = PendingJsDialogStore.pending
    var lastJsDialog by remember { mutableStateOf<PendingJsDialog?>(null) }
    if (jsDialog != null) lastJsDialog = jsDialog

    var jsPromptInput by remember { mutableStateOf("") }
    LaunchedEffect(jsDialog) {
        if (jsDialog?.type == JsDialogType.PROMPT) {
            jsPromptInput = jsDialog.defaultValue
        }
    }

    WindowDialog(
        show = jsDialog != null,
        title = if (jsDialog?.type == JsDialogType.ALERT) "提示" else "网页消息",
        onDismissRequest = {
            lastJsDialog?.cancel()
            PendingJsDialogStore.pending = null
        },
    ) {
        if (lastJsDialog != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = lastJsDialog!!.message,
                    modifier = Modifier.fillMaxWidth(),
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )

                if (lastJsDialog!!.type == JsDialogType.PROMPT) {
                    TextField(
                        value = jsPromptInput,
                        onValueChange = { jsPromptInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (lastJsDialog!!.type != JsDialogType.ALERT) {
                        Button(
                            onClick = {
                                lastJsDialog?.cancel()
                                PendingJsDialogStore.pending = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                contentColor = MiuixTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text("取消")
                        }
                    }
                    Button(
                        onClick = {
                            lastJsDialog?.confirm(jsPromptInput)
                            PendingJsDialogStore.pending = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }

    WindowDialog(
        show = showTtsErrorDialog,
        title = "语音引擎未就绪",
        onDismissRequest = { showTtsErrorDialog = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "当前设备未安装系统语音引擎，或未设置默认引擎。",
                modifier = Modifier.fillMaxWidth(),
                style = MiuixTheme.textStyles.body1,
                color = DialogDefaults.summaryColor(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showTtsErrorDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        showTtsErrorDialog = false
                        openTtsSettings(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("去设置") }
            }
        }
    }

    // ★ 全局夜间模式半透明黑色蒙版
    if (activeWindow?.nightMode == true && globalNightDim && !showNewTabPage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .zIndex(998f) // 在普通 UI 层之上，但在弹窗和 Snackbar(999f) 之下，且不拦截触摸
        )
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
    onLongPress: (imageUrl: String, linkUrl: String, text: String, pageUrl: String, xPx: Int, yPx: Int, selectable: Boolean) -> Unit,
    onFindResult: (Int, Int) -> Unit,
    onSniffResult: (SniffedResource) -> Unit,
    onShowHtmlFullscreen: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideHtmlFullscreen: () -> Unit,
    onEnglishPageDetected: (Boolean) -> Unit,
    fileChooserCallback: MutableState<ValueCallback<Array<Uri>>?>,
    fileChooserLauncher: ActivityResultLauncher<Intent>,
    pendingPermission: MutableState<PermissionRequest?>,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onExternalSchemeRequest: (String) -> Unit,
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
                                val isBlankUrl = url.isBlank() || url == "about:blank"
                                onUpdateWindow(id) {
                                    if (isBlankUrl) {
                                        it.favicon = null
                                    } else if (hostChanged && newHost != null) {
                                        val cached = faviconCache[newHost]
                                        it.favicon = cached
                                    }
                                    it.url = url
                                    it.title = title
                                    it.canGoBack = back
                                    it.canGoForward = fwd
                                }

                                // 沉浸式页面：探测网页主题色（页面加载完成 / SPA 路由变化时刷新）
                                try {
                                    webViewMap[id]?.evaluateJavascript(
                                        JS_DETECT_PAGE_THEME_COLOR
                                    ) { raw ->
                                        val currentUrl = windows
                                            .firstOrNull { it.id == id }
                                            ?.url
                                        if (currentUrl == url) {
                                            val (topColor, bottomColor) =
                                                parsePageThemeColors(raw)
                                            onUpdateWindow(id) {
                                                it.themeColor = topColor
                                                it.bottomThemeColor = bottomColor
                                            }
                                        }
                                    }
                                } catch (_: Exception) {}

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
                                val isOnSearchResultPage = if (engineBase != null) {
                                    val path = (urlObj?.path ?: "").lowercase()
                                    when {
                                        !urlQuery.isNullOrBlank() -> true
                                        engineBase.first == "百度" || engineBase.first == "神马" -> path.startsWith("/s")
                                        engineBase.first == "必应" || engineBase.first == "抖音" || engineBase.first == "谷歌" -> path.startsWith("/search")
                                        engineBase.first == "搜狗" -> path.startsWith("/web/searchList") || path.startsWith("/s")
                                        engineBase.first == "自定义" -> urlQuery != null
                                        else -> false
                                    }
                                } else false

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
                                            prefs.getInt("browser_header_text_mode", 1) == 1 &&
                                                title.isNotBlank() && title != "无标题" ->
                                                TextFieldValue(title)
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
                                        // 持久化，供书签列表 / 历史记录使用
                                        FaviconStore.save(context, host, bmp)
                                        onUpdateWindow(id) { it.favicon = bmp }
                                    }
                                }
                            },
                            onFaviconFromDomain = { url -> onFaviconLoad(id, url) },
                            onDownloadRequest = { dl -> onPendingDownload(dl) },
                            onLongPress = { imageUrl, linkUrl, text, pageUrl, xPx, yPx, selectable ->
                                onLongPress(imageUrl, linkUrl, text, pageUrl, xPx, yPx, selectable)
                            },
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
                                    applyNightModeToWebView(wv2, true)
                                }
                                if (win?.noImage == true) {
                                    wv2.evaluateJavascript(JS_NO_IMG_ON, null)
                                }
                                if (forceZoomEnabled) {
                                    wv2.evaluateJavascript(JS_FORCE_ZOOM_ON, null)
                                }
                                try { wv2.settings.textZoom = textZoomValue } catch (_: Exception) {}

                                wv2.evaluateJavascript(JS_GESTURE_GUARD, null)

                                // ★ 注入已启用的用户脚本
                                UserScriptStore.injectInto(wv2)

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

                                val cert = try { wv2.certificate } catch (_: Exception) { null }
                                if (cert != null) {
                                    val issuedTo = cert.issuedTo?.cName ?: ""
                                    val issuedBy = cert.issuedBy?.cName ?: ""
                                    val validFromDate = cert.validNotBeforeDate
                                    val validToDate = cert.validNotAfterDate
                                    val now = java.util.Date()
                                    val isExpired = validToDate != null && validToDate.before(now)
                                    val dateFormat = java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm", java.util.Locale.US,
                                    )
                                    SecurityStateHolder.updateCertInfo(
                                        issuer = issuedBy,
                                        subject = issuedTo,
                                        validFrom = validFromDate?.let { dateFormat.format(it) } ?: "未知",
                                        validTo = validToDate?.let { dateFormat.format(it) } ?: "未知",
                                        isExpired = isExpired,
                                    )
                                }
                            },
                            onResourceSniffed = { resource -> onSniffResult(resource) },
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
                            onExternalSchemeRequest = onExternalSchemeRequest,
                        )
                        webViewMap[id] = created

                        val win = windows.firstOrNull { it.id == id }
                        if (win?.incognito == true) {
                            try { created.settings.cacheMode = WebSettings.LOAD_NO_CACHE } catch (_: Exception) {}
                        }

                        if (win?.nightMode == true) {
                            applyNightModeToWebView(created, true)
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
// WebView 工厂
// ============================================================

@SuppressLint("SetJavaScriptEnabled")
private fun applyDesktopViewportSettings(wv: WebView, desktopMode: Boolean) {
    val s = wv.settings
    if (desktopMode) {
        s.useWideViewPort = false
        s.loadWithOverviewMode = false
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
    } else {
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
    }
}

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
    onLongPress: (imageUrl: String, linkUrl: String, text: String, pageUrl: String, xPx: Int, yPx: Int, selectable: Boolean) -> Unit,
    onFindResult: (total: Int, current: Int) -> Unit,
    onPageFinishedExtra: (WebView) -> Unit,
    onResourceSniffed: (SniffedResource) -> Unit,
    onShowCustomView: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideCustomView: () -> Unit,
    onEnglishPageDetected: (Boolean) -> Unit,
    fileChooserCallback: MutableState<ValueCallback<Array<Uri>>?>,
    fileChooserLauncher: ActivityResultLauncher<Intent>,
    pendingPermission: MutableState<PermissionRequest?>,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onRenderProcessGoneCallback: (WebView) -> Unit,
    onExternalSchemeRequest: (String) -> Unit,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val mobileUa = getMobileUserAgent(context)

        val pmPrefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val rememberFormEnabled = try {
            pmPrefs.getBoolean("web_remember_form", true)
        } catch (_: Exception) { true }
        val acceptCookieEnabled = try {
            pmPrefs.getBoolean("web_accept_cookie", true)
        } catch (_: Exception) { true }

        settings.apply {
            userAgentString = UaStore.effectiveUa(context, initialDesktopMode)
            ?: if (initialDesktopMode) DESKTOP_UA else mobileUa
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
            saveFormData = rememberFormEnabled
        }
        applyDesktopViewportSettings(this, initialDesktopMode)

        CookieManager.getInstance().setAcceptCookie(acceptCookieEnabled)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, acceptCookieEnabled)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        isHapticFeedbackEnabled = false

        val selfRef: WebView = this
        addJavascriptInterface(object {
            @JavascriptInterface
            fun setPullRefreshAllowed(allowed: Boolean) {
                PullRefreshStateHolder.set(windowId, allowed)
            }

            @JavascriptInterface
            fun setSwipeNavAllowed(allowed: Boolean) {
                SwipeNavStateHolder.set(windowId, allowed)
            }

            @JavascriptInterface
            fun onPasswordSubmit(url: String, username: String, password: String) {
                if (url.isBlank() || password.isBlank()) return
                post {
                    PasswordCaptureStore.pending = PendingPassword(url, username, password)
                }
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

        var lastTouchRawX = 0
        var lastTouchRawY = 0
        var lastTouchViewX = 0f
        var lastTouchViewY = 0f
        setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                lastTouchRawX = event.rawX.toInt()
                lastTouchRawY = event.rawY.toInt()
                lastTouchViewX = event.x
                lastTouchViewY = event.y
            }
            false
        }
        setOnLongClickListener { v ->
            val wv = v as? WebView ?: return@setOnLongClickListener false
            if (LongPressSkipStore.consumeSkip(wv)) {
                return@setOnLongClickListener false
            }
            val result = wv.hitTestResult ?: return@setOnLongClickListener false
            val type = result.type
            val hitExtra = result.extra.orEmpty()

            performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
            )

            val density = resources.displayMetrics.density
            val scale = try { wv.scale } catch (_: Exception) { 1f }
            val safeScale = if (scale > 0.05f) scale else 1f
            val cssX = (lastTouchViewX / (density * safeScale)).toDouble()
            val cssY = (lastTouchViewY / (density * safeScale)).toDouble()

            val js = "(function(fx,fy){" +
                    "try{" +
                    "var x=(window.__mote_lp_x>=0)?window.__mote_lp_x:fx;" +
                    "var y=(window.__mote_lp_y>=0)?window.__mote_lp_y:fy;" +
                    "window.__mote_lp_x=-1;" +
                    "window.__mote_lp_y=-1;" +
                    "var els = document.elementsFromPoint ? document.elementsFromPoint(x,y) : [document.elementFromPoint(x,y)];" +
                    "if(!els || els.length===0)return '{}';" +
                    "var el = els[0];" +

                    // 图片查找
                    "var img=null;" +
                    "for(var k=0;k<els.length;k++){" +
                    "  var e=els[k];" +
                    "  if(!e)continue;" +
                    "  if(e.tagName==='IMG'){img=e;break;}" +
                    "  if(e.tagName==='PICTURE'&&e.querySelector){img=e.querySelector('img');if(img)break;}" +
                    "}" +
                    "if(!img){" +
                    "  var _best=null,_bestD=1e18;" +
                    "  for(var k=0;k<els.length;k++){" +
                    "    var _p=els[k];" +
                    "    if(!_p||!_p.querySelectorAll)continue;" +
                    "    var _list=_p.querySelectorAll('img');" +
                    "    for(var _i=0;_i<_list.length;_i++){" +
                    "      var _im=_list[_i];" +
                    "      var _r=_im.getBoundingClientRect();" +
                    "      if(_r.width>0&&_r.height>0&&x>=_r.left-16&&x<=_r.right+16&&y>=_r.top-16&&y<=_r.bottom+16){" +
                    "        var _cx=_r.left+_r.width/2,_cy=_r.top+_r.height/2;" +
                    "        var _dist=(_cx-x)*(_cx-x)+(_cy-y)*(_cy-y);" +
                    "        if(_dist<_bestD){_bestD=_dist;_best=_im;}" +
                    "      }" +
                    "    }" +
                    "    if(_best)break;" +
                    "  }" +
                    "  if(_best)img=_best;" +
                    "}" +
                    "if(!img && el){" +
                    "  var _p2=el,_depth=0;" +
                    "  while(_p2&&_depth<10){" +
                    "    if(_p2.tagName==='IMG'){img=_p2;break;}" +
                    "    _p2=_p2.parentElement;_depth++;" +
                    "  }" +
                    "}" +

                    // 链接查找
                    "var anchor=null;" +
                    // ★ 改动 1：不再只看最上层节点 el，而是遍历所有重叠的 els 查找 a[href]
                    "for(var k=0;k<els.length;k++){" +
                    "  var cur=els[k];" +
                    "  if(!cur)continue;" +
                    "  try{var a=cur.closest?cur.closest('a[href]'):null;if(a){anchor=a;break;}}catch(e){}" +
                    "  if(!anchor){" +
                    "    var p=cur,d2=0;" +
                    "    while(p&&d2<15){" +
                    "      if(p.tagName==='A'&&p.getAttribute&&p.getAttribute('href')){anchor=p;break;}" +
                    "      p=p.parentElement;d2++;" +
                    "    }" +
                    "  }" +
                    "}" +
                    // 假链接兜底
                    "if(!anchor){" +
                    "  var cur=el,dd=0;" +
                    "  while(cur&&dd<15){" +
                    "    try{" +
                    "      var oc=cur.getAttribute&&cur.getAttribute('onclick');" +
                    "      var rl=cur.getAttribute&&cur.getAttribute('role');" +
                    "      var dh=cur.getAttribute&&cur.getAttribute('data-href');" +
                    "      var du=cur.getAttribute&&cur.getAttribute('data-url');" +
                    "      var dl=cur.getAttribute&&cur.getAttribute('data-link');" +
                    "      if(oc||rl==='link'||dh||du||dl){anchor=cur;break;}" +
                    "    }catch(e){}" +
                    "    cur=cur.parentElement;dd++;" +
                    "  }" +
                    "}" +
                    // 图片 URL
                    "function getImgUrl(im){" +
                    "  if(!im)return '';" +
                    "  var u=im.currentSrc||im.src||'';" +
                    "  if(!u||u.indexOf('data:')===0){" +
                    "    var attrs=['data-src','data-original','data-lazy-src','data-echo','data-url','data-image','data-img','data-lazyload','data-original-src','data-origin'];" +
                    "    for(var i=0;i<attrs.length;i++){" +
                    "      var v=im.getAttribute&&im.getAttribute(attrs[i]);" +
                    "      if(v&&v.indexOf('data:')!==0){u=v;break;}" +
                    "    }" +
                    "  }" +
                    "  if(!u){" +
                    "    var s=im.querySelector&&im.querySelector('source');" +
                    "    if(s)u=s.src||'';" +
                    "  }" +
                    "  if(u&&u.indexOf('//')===0)u=location.protocol+u;" +
                    "  else if(u&&u.indexOf('http')!==0){" +
                    "    try{u=new URL(u,location.href).href;}catch(e){}" +
                    "  }" +
                    "  return u;" +
                    "}" +
                    // 链接 URL
                    "function getLinkUrl(a){" +
                    "  if(!a)return '';" +
                    "  var u='';" +
                    "  try{u=a.href||'';}catch(e){}" +
                    "  if(!u){" +
                    "    var attrs=['data-href','data-url','data-link','data-target','data-src'];" +
                    "    for(var i=0;i<attrs.length;i++){" +
                    "      var v=a.getAttribute&&a.getAttribute(attrs[i]);" +
                    "      if(v){u=v;break;}" +
                    "    }" +
                    "  }" +
                    "  if(!u){" +
                    "    try{" +
                    "      var oc=a.getAttribute&&a.getAttribute('onclick');" +
                    "      if(oc){" +
                    "        var m1=oc.match(/location\\.href\\s*=\\s*['\"]([^'\"]+)['\"]/);" +
                    "        if(m1&&m1[1])u=m1[1];" +
                    "        if(!u){" +
                    "          var m2=oc.match(/window\\.open\\s*\\(\\s*['\"]([^'\"]+)['\"]/);" +
                    "          if(m2&&m2[1])u=m2[1];" +
                    "        }" +
                    "      }" +
                    "    }catch(e){}" +
                    "  }" +
                    "  if(u){" +
                    "    if(u==='#'||u.indexOf('javascript:')===0||u.indexOf('void(')===0){u='';}" +
                    "  }" +
                    "  if(u&&u.indexOf('http')!==0&&u.indexOf('//')!==0){" +
                    "    try{u=new URL(u,location.href).href;}catch(e){}" +
                    "  }" +
                    "  return u;" +
                    "}" +
                    // 文字提取
                    "var text='';" +
                    "try{" +
                    "  var textNode=null;" +
                    "  var anchorEl=null;" +
                    "  if(document.caretRangeFromPoint){" +
                    "    var range=document.caretRangeFromPoint(x,y);" +
                    "    if(range&&range.startContainer){" +
                    // ★ 改动 2：如果翻译插件把文本包裹在子节点中，这里也能剥开取到文字
                    "      if(range.startContainer.nodeType===3){" +
                    "        textNode=range.startContainer;" +
                    "        anchorEl=textNode.parentElement;" +
                    "      } else if(range.startContainer.firstChild&&range.startContainer.firstChild.nodeType===3){" +
                    "        textNode=range.startContainer.firstChild;" +
                    "        anchorEl=range.startContainer;" +
                    "      }" +
                    "    }" +
                    "  }" +
                    "  if(!anchorEl)anchorEl=el;" +
                    "  if(anchorEl){" +
                    "    var block=anchorEl.closest?anchorEl.closest('a,button,h1,h2,h3,h4,h5,h6,p,li,td,th,dt,dd,blockquote,pre'):null;" +
                    "    if(!block){" +
                    "      var cur=anchorEl,d=0;" +
                    "      while(cur&&d<8){" +
                    "        var txt=(cur.innerText||'').trim();" +
                    "        if(txt.length>=2){" +
                    "          var ptxt=(cur.parentElement?(cur.parentElement.innerText||''):'').trim();" +
                    "          if(ptxt.length===0||txt.length<=ptxt.length*0.8){" +
                    "            block=cur;break;" +
                    "          }" +
                    "        }" +
                    "        cur=cur.parentElement;d++;" +
                    "      }" +
                    "    }" +
                    "    if(block){" +
                    "      text=(block.innerText||block.textContent||'').trim();" +
                    "    }" +
                    "  }" +
                    "  if(!text&&textNode){" +
                    "    text=(textNode.nodeValue||'').trim();" +
                    "  }" +
                    "}catch(e){}" +
                    "text=text.replace(/[\\s\\n\\r]+/g,' ').trim();" +
                    "if(text.length>1000)text=text.substring(0,1000);" +
                    // user-select 检测
                    "var selectable=true;" +
                    "var cur2=el,d4=0;" +
                    "while(cur2&&d4<15){" +
                    "  try{" +
                    "    var st=window.getComputedStyle(cur2);" +
                    "    var us=st.userSelect||st.webkitUserSelect||'';" +
                    "    if(us==='none'){selectable=false;break;}" +
                    "  }catch(e){}" +
                    "  cur2=cur2.parentElement;" +
                    "  d4++;" +
                    "}" +
                    "return JSON.stringify({" +
                    "  imgSrc:getImgUrl(img)," +
                    "  anchorHref:getLinkUrl(anchor)," +
                    "  text:text," +
                    "  selectable:selectable" +
                    "});" +
                    "}catch(e){return '{}';}" +
                    "})($cssX,$cssY)"

            wv.evaluateJavascript(js) { raw ->
                val decoded = decodeJsString(raw)
                var jsImgSrc = ""
                var jsAnchorHref = ""
                var jsText = ""
                var jsSelectable = true
                try {
                    val obj = JSONObject(decoded)
                    jsImgSrc = obj.optString("imgSrc", "")
                    jsAnchorHref = obj.optString("anchorHref", "")
                    jsText = obj.optString("text", "")
                    jsSelectable = obj.optBoolean("selectable", true)
                } catch (_: Exception) {}

                val imageUrl: String
                val linkUrl: String
                val text: String
                when (type) {
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                        linkUrl = jsAnchorHref
                        imageUrl = hitExtra.ifBlank { jsImgSrc }
                        text = ""
                    }
                    WebView.HitTestResult.IMAGE_TYPE -> {
                        imageUrl = hitExtra.ifBlank { jsImgSrc }
                        linkUrl = ""
                        text = ""
                    }
                    WebView.HitTestResult.ANCHOR_TYPE -> {
                        linkUrl = hitExtra.ifBlank { jsAnchorHref }
                        imageUrl = jsImgSrc
                        text = jsText
                    }
                    else -> {
                        if (jsAnchorHref.isNotBlank()) {
                            linkUrl = jsAnchorHref
                            imageUrl = jsImgSrc
                            text = jsText
                        } else {
                            if (jsText.isBlank() && jsImgSrc.isBlank()) {
                                return@evaluateJavascript
                            }
                            linkUrl = ""
                            imageUrl = jsImgSrc
                            text = jsText
                        }
                    }
                }

                if (imageUrl.isBlank() && linkUrl.isBlank() && text.isBlank()) {
                    return@evaluateJavascript
                }

                val pageUrl = wv.url ?: ""
                onLongPress(imageUrl, linkUrl, text, pageUrl, lastTouchRawX, lastTouchRawY, jsSelectable)
            }

            true
        }

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
                            val trackerCategory = TrackerBlockList.classifyWithLevel(
                                url,
                                SecurityStateHolder.trackingLevel,
                            )
                            if (trackerCategory != null) {
                                val secInfo = SecurityStateHolder.info
                                val shouldBlock = when (trackerCategory) {
                                    "ad" -> secInfo.effectiveBlockAdsEnabled
                                    else -> secInfo.effectiveTrackingProtectionEnabled
                                }
                                if (shouldBlock) {
                                    post {
                                        SecurityStateHolder.onTrackerBlocked(url, trackerCategory)
                                        WebSecurityStats.bump(
                                            context,
                                            if (trackerCategory == "ad") WebSecurityStats.CATEGORY_SMART
                                            else WebSecurityStats.CATEGORY_PRIVACY,
                                        )
                                    }
                                    return WebResourceResponse(
                                        "text/plain", "utf-8",
                                        java.io.ByteArrayInputStream(ByteArray(0)),
                                    )
                                }
                            }

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
                val lower = url.lowercase()
                try {
                    val secPrefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                    if (SecurityStateHolder.effectiveRedirects(SecurityStateHolder.info.host) &&
                        request != null &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        request.isRedirect
                    ) {
                        val currentHost = view?.url?.let { Uri.parse(it).host.orEmpty() }.orEmpty()
                        val targetHost = request.url.host.orEmpty()
                        if (currentHost.isNotBlank() && targetHost.isNotBlank() && currentHost != targetHost) {
                            WebSecurityStats.bump(context, WebSecurityStats.CATEGORY_SMART)
                            Toast.makeText(context, "网页重定向已拦截：$targetHost", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    }
                } catch (_: Exception) {}

                if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
                    val ignoredSchemes = listOf("about:", "data:", "file:", "content:", "javascript:", "blob:")
                    if (ignoredSchemes.any { lower.startsWith(it) }) {
                        return false
                    }

                    // ★ 需求：遇到系统快应用链接就拦截
                    val isQuickApp = lower.startsWith("hap://") ||
                            lower.startsWith("hwfastapp://") ||
                            lower.startsWith("mfastapp://") ||
                            lower.startsWith("opfastapp://") ||
                            lower.startsWith("fastapp://") ||
                            lower.contains("fastapp")

                    if (isQuickApp) {
                        onExternalSchemeRequest(url)
                        return true
                    }

                    // 2. 针对 intent:// 协议处理（常见于知乎等应用）
                    if (lower.startsWith("intent://")) {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // 唤起失败，尝试网页自带的 fallback 链接
                            try {
                                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                if (!fallbackUrl.isNullOrBlank() && view != null) {
                                    view.loadUrl(fallbackUrl)
                                }
                            } catch (_: Exception) {}
                        }
                        // 无论如何返回 true，防止 WebView 崩溃变成错误页，但不弹任何提示
                        return true
                    }

                    // 3. 其他自定义 Scheme (如 zhihu://, bilibili://)
                    return try {
                        val intent = Intent(Intent.ACTION_VIEW, request?.url)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        true
                    } catch (e: Exception) {
                        // ★ 静默处理，绝不弹 Toast 阻拦用户的后续操作
                        true
                    }
                }
                return false
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                try { view?.tag = System.currentTimeMillis() } catch (_: Exception) {}
                onEnglishPageDetected(false)
                if (url != null) {
                    SecurityStateHolder.reset(url)
                }
                try { view?.evaluateJavascript(JS_HOOK_XHR_FETCH, null) } catch (_: Exception) {}
                try { view?.evaluateJavascript(JS_CLIPBOARD_HOOK, null) } catch (_: Exception) {}
                try { view?.evaluateJavascript(JS_COOKIE_HOOK, null) } catch (_: Exception) {}
                try {
                    if (WebPermissionStore.mode(context, WebPermissionStore.VIDEO_AUTOPLAY) == WebPermissionStore.DENY) {
                        view?.evaluateJavascript(JS_BLOCK_VIDEO_AUTOPLAY, null)
                    }
                } catch (_: Exception) {}
                try {
                    val secPrefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                    if (SecurityStateHolder.effectiveWebrtc(SecurityStateHolder.info.host)) {
                        view?.evaluateJavascript(JS_DISABLE_WEBRTC, null)
                        WebSecurityStats.bump(context, WebSecurityStats.CATEGORY_PRIVACY)
                    }
                    var permissionBlocks = 0
                    if (!secPrefs.getBoolean("web_accept_cookie", true)) permissionBlocks++
                    if (!secPrefs.getBoolean("web_remember_form", true)) permissionBlocks++
                    if (permissionBlocks > 0) {
                        WebSecurityStats.bump(context, WebSecurityStats.CATEGORY_PERMISSION, permissionBlocks)
                    }
                } catch (_: Exception) {}
                if (url != null) onFaviconFromDomain(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                try {
                    val start = (view?.tag as? Long) ?: 0L
                    if (start > 0L) {
                        val duration = System.currentTimeMillis() - start
                        MoteApplication.trackEventSafe(
                            "page_load_finished",
                            mapOf("duration_ms" to duration)
                        )
                    }
                } catch (_: Exception) {}
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

                    view?.evaluateJavascript(JS_CONTEXTMENU_TRACKER, null)

                    view?.evaluateJavascript(JS_INTERCEPT_VIDEO, null)

                    view?.evaluateJavascript(JS_GESTURE_GUARD, null)
                    view?.evaluateJavascript(jsHook, null)
                    view?.evaluateJavascript(JS_PASSWORD_DETECT, null)

                    view?.let { onPageFinishedExtra(it) }
                }
                try {
                    CookieManager.getInstance().flush()
                } catch (_: Exception) {
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                // 用户已开启「忽略 SSL 证书警告」时直接放行，不弹窗
                val ignoreSsl = runCatching {
                    view.context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                        .getBoolean("web_ignore_ssl_warnings", false)
                }.getOrDefault(false)
                if (ignoreSsl) {
                    try { handler.proceed() } catch (_: Exception) {}
                    return
                }
                val primaryError = when (error.primaryError) {
                    SslError.SSL_DATE_INVALID -> "证书日期无效"
                    SslError.SSL_EXPIRED -> "证书已过期"
                    SslError.SSL_IDMISMATCH -> "证书域名不匹配"
                    SslError.SSL_NOTYETVALID -> "证书尚未生效"
                    SslError.SSL_UNTRUSTED -> "证书来自未知授权中心"
                    SslError.SSL_INVALID -> "证书无效"
                    else -> "证书验证失败"
                }
                SecurityStateHolder.markSslError(primaryError)
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
        addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun request(kind: String, text: String) {
                post {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val web = selfRef
                    fun reply(ok: Boolean, data: String) {
                        val escaped = data.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
                        web.evaluateJavascript("window.__moteClipResult && window.__moteClipResult($ok, '$escaped')", null)
                    }
                    WebPermissionPrompt.request(context, WebPermissionStore.CLIPBOARD, SecurityStateHolder.info.host) { allow, _, _ ->
                        if (!allow) {
                            reply(false, "")
                        } else if (kind == "write") {
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Mote", text))
                            reply(true, "")
                        } else {
                            val read = cm.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                            reply(true, read)
                        }
                    }
                }
            }
        }, "MotePermission")
        addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun set(raw: String, host: String) {
                post { CookieMetaStore.recordSetCookie(context, raw, host) }
            }
        }, "MoteCookie")
        val permSettings = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        settings.mediaPlaybackRequiresUserGesture =
            WebPermissionStore.mode(context, WebPermissionStore.AUTOPLAY) != WebPermissionStore.ALLOW &&
            WebPermissionStore.mode(context, WebPermissionStore.VIDEO_AUTOPLAY) != WebPermissionStore.ALLOW
        settings.setGeolocationEnabled(
            WebPermissionStore.mode(context, WebPermissionStore.LOCATION) != WebPermissionStore.DENY
        )
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

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: android.webkit.GeolocationPermissions.Callback?,
            ) {
                if (callback == null) return
                WebPermissionPrompt.request(context, WebPermissionStore.LOCATION, SecurityStateHolder.info.host) { allow, _, _ ->
                    callback.invoke(origin, allow, false)
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) return
                val resources = request.resources
                val camera = resources.any { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                val mic = resources.any { it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }
                val kind = when {
                    camera -> WebPermissionStore.CAMERA
                    mic -> WebPermissionStore.MICROPHONE
                    else -> return
                }
                WebPermissionPrompt.request(context, kind, SecurityStateHolder.info.host) { allow, _, _ ->
                    post {
                        if (!allow) {
                            request.deny()
                        } else {
                            val needed = buildList {
                                if (camera) add(Manifest.permission.CAMERA)
                                if (mic) add(Manifest.permission.RECORD_AUDIO)
                            }
                            val granted = needed.all {
                                ContextCompat.checkSelfPermission(context, it) ==
                                    PackageManager.PERMISSION_GRANTED
                            }
                            if (granted || needed.isEmpty()) {
                                request.grant(resources)
                            } else {
                                pendingPermission.value = request
                                permissionLauncher.launch(needed.toTypedArray())
                            }
                        }
                    }
                }
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                if (WebPermissionStore.mode(context, WebPermissionStore.DIALOG) == WebPermissionStore.DENY) {
                    result.cancel()
                    return true
                }
                post {
                    PendingJsDialogStore.pending = PendingJsDialog(
                        message = message ?: "",
                        type = JsDialogType.ALERT,
                        confirm = { result.confirm() },
                        cancel = { result.cancel() }
                    )
                }
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                post {
                    PendingJsDialogStore.pending = PendingJsDialog(
                        message = message ?: "",
                        type = JsDialogType.CONFIRM,
                        confirm = { result.confirm() },
                        cancel = { result.cancel() }
                    )
                }
                return true
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                if (result == null) return false
                post {
                    PendingJsDialogStore.pending = PendingJsDialog(
                        message = message ?: "",
                        type = JsDialogType.PROMPT,
                        defaultValue = defaultValue ?: "",
                        confirm = { input -> result.confirm(input) },
                        cancel = { result.cancel() }
                    )
                }
                return true
            }
        }

        setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            val secPrefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            val pageHost = SecurityStateHolder.info.host
            if (SecurityStateHolder.effectiveDownloadProtection(pageHost)) {
                val lowerName = fileName.lowercase()
                val riskyExt = listOf(".apk", ".exe", ".bat", ".cmd", ".sh", ".scr", ".msi", ".jar", ".vbs")
                if (riskyExt.any { lowerName.endsWith(it) } ||
                    mimetype.contains("vnd.android.package-archive", true)
                ) {
                    WebSecurityStats.bump(context, WebSecurityStats.CATEGORY_SMART)
                    Toast.makeText(
                        context,
                        "下载防护已拦截可疑文件：$fileName",
                        Toast.LENGTH_LONG,
                    ).show()
                    return@setDownloadListener
                }
            }
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
// 网页安全中心 - 菜单内容
// ============================================================

@Composable
private fun SecurityMenuContent(
    info: SecurityInfo,
    onOpenSiteSettings: (String) -> Unit,
    onOpenCertDetail: () -> Unit,
    onOpenCookies: () -> Unit,
    onToggleBlockAds: (Boolean) -> Unit,
    onToggleTrackingProtection: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        ArrowPreference(
            title = when {
                info.hasSslError -> "连接危险"
                info.isHttps && info.isCertExpired -> "连接有风险"
                info.isHttps -> "连接安全"
                else -> "连接未加密"
            },
            summary = when {
                info.hasSslError -> "${info.host} | ${info.sslErrorText ?: "证书验证失败"}"
                info.isHttps && info.isCertExpired -> "${info.host} | 证书已过期"
                info.isHttps -> "${info.host} | 已建立加密连接"
                else -> "${info.host} | 此网站未使用 HTTPS 加密"
            },
            onClick = onOpenCertDetail,
        )

        ArrowPreference(
            title = "Cookie 和网站数据",
            onClick = onOpenCookies,
        )

        SwitchPreference(
            title = "此网站的跟踪防护",
            summary = if (info.blockedTrackers > 0) {
                "已拦截 ${info.blockedTrackers} 个跟踪器"
            } else {
                "限制跟踪器跨站收集浏览数据"
            },
            checked = info.effectiveTrackingProtectionEnabled,
            onCheckedChange = onToggleTrackingProtection,
        )

        ArrowPreference(
            title = "设置该网页",
            summary = info.host,
            onClick = { if (info.host.isNotBlank()) onOpenSiteSettings(info.host) },
        )

        SwitchPreference(
            title = "此网站的广告拦截",
            summary = if (info.blockedAds > 0) {
                "已拦截 ${info.blockedAds} 个广告"
            } else {
                "阻止网页中的广告请求"
            },
            checked = info.effectiveBlockAdsEnabled,
            onCheckedChange = onToggleBlockAds,
        )
    }
}

@Composable
private fun CertDetailContent(
    info: SecurityInfo,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.ChevronBackward,
                contentDescription = "返回",
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "证书详情",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CertInfoRow(label = "颁发给", value = info.certSubject.ifBlank { "未知" })
            CertInfoRow(label = "颁发者", value = info.certIssuer.ifBlank { "未知" })
            CertInfoRow(label = "生效时间", value = info.certValidFrom.ifBlank { "未知" })
            CertInfoRow(
                label = "过期时间",
                value = info.certValidTo.ifBlank { "未知" },
                valueColor = if (info.isCertExpired) Color(0xFFFF3B30)
                else MiuixTheme.colorScheme.onSurface,
            )
            CertInfoRow(
                label = "证书状态",
                value = if (info.isCertExpired) "已过期" else "有效",
                valueColor = if (info.isCertExpired) Color(0xFFFF3B30)
                else Color(0xFF34C759),
            )
        }
    }
}

@Composable
private fun CookieDetailContent(
    info: SecurityInfo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val host = info.host

    val cookieCount = remember(host, info.isHttps) {
        if (host.isBlank()) return@remember 0
        try {
            val scheme = if (info.isHttps) "https" else "http"
            val raw = CookieManager.getInstance().getCookie("$scheme://$host")
            if (raw.isNullOrBlank()) 0
            else raw.split(";").count { it.isNotBlank() }
        } catch (_: Exception) {
            0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.ChevronBackward,
                contentDescription = "返回",
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Cookie 和网站数据",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Column {
                Text(
                    text = "Cookie 和其他站点数据用于记住你，例如帮助你登录或设置个性化广告。若要管理所有站点的 Cookie，请参阅设置。",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "设置",
                    fontSize = 13.sp,
                    color = Color(0xFF3B82F6),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onOpenSettings() })
                        },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Text(
                text = if (cookieCount > 0) "正在使用 $cookieCount 个 Cookie"
                else "暂未使用 Cookie",
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun BlockedStatItem(
    label: String,
    count: Int,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label $count",
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun CertInfoRow(
    label: String,
    value: String,
    valueColor: Color = MiuixTheme.colorScheme.onSurface,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = DialogDefaults.summaryColor(),
            modifier = Modifier.width(76.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            modifier = Modifier.weight(1f),
        )
    }
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

// ============================================================
// ★ 新增：MIME ↔ 扩展名映射（修复 bin 扩展名问题）
// ============================================================

private val MIME_TO_EXT: Map<String, String> = mapOf(
    "image/jpeg" to ".jpg",
    "image/jpg" to ".jpg",
    "image/pjpeg" to ".jpg",
    "image/png" to ".png",
    "image/gif" to ".gif",
    "image/webp" to ".webp",
    "image/bmp" to ".bmp",
    "image/x-ms-bmp" to ".bmp",
    "image/svg+xml" to ".svg",
    "image/x-icon" to ".ico",
    "image/vnd.microsoft.icon" to ".ico",
    "image/tiff" to ".tiff",
    "image/avif" to ".avif",
    "image/heic" to ".heic",
    "image/heif" to ".heif",
    "image/apng" to ".apng",
    "video/mp4" to ".mp4",
    "video/webm" to ".webm",
    "video/x-matroska" to ".mkv",
    "video/quicktime" to ".mov",
    "video/x-msvideo" to ".avi",
    "video/x-flv" to ".flv",
    "video/mpeg" to ".mpeg",
    "audio/mpeg" to ".mp3",
    "audio/mp4" to ".m4a",
    "audio/wav" to ".wav",
    "audio/x-wav" to ".wav",
    "audio/ogg" to ".ogg",
    "audio/flac" to ".flac",
    "audio/aac" to ".aac",
    "audio/opus" to ".opus",
    "application/pdf" to ".pdf",
    "application/zip" to ".zip",
    "application/x-rar-compressed" to ".rar",
    "application/x-7z-compressed" to ".7z",
    "application/vnd.android.package-archive" to ".apk",
)

private fun extFromMime(mime: String?): String {
    if (mime.isNullOrBlank()) return ""
    return MIME_TO_EXT[mime.substringBefore(';').trim().lowercase()] ?: ""
}

/**
 * 探测远程 URL 的 MIME 类型（HEAD 优先，失败走 GET 只读响应头）。
 */
private fun probeMimeType(
    url: String,
    userAgent: String = FALLBACK_MOBILE_UA,
    referer: String = "",
): String? {
    fun tryMethod(method: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.instanceFollowRedirects = true
            conn.requestMethod = method
            conn.setRequestProperty("User-Agent", userAgent)
            if (referer.isNotBlank()) {
                conn.setRequestProperty("Referer", referer)
            }
            try {
                val cookies = CookieManager.getInstance().getCookie(url)
                if (!cookies.isNullOrBlank()) {
                    conn.setRequestProperty("Cookie", cookies)
                }
            } catch (_: Exception) {}
            conn.connect()
            if (conn.responseCode in 200..299) {
                val ct = conn.contentType
                conn.disconnect()
                if (!ct.isNullOrBlank() && !ct.startsWith("text/")) {
                    ct.substringBefore(';').trim().lowercase()
                } else null
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) {
            null
        }
    }
    return tryMethod("HEAD") ?: tryMethod("GET")
}

/**
 * 下载远程字节 + 返回 Content-Type。
 */
private fun downloadImageWithMime(
    imageUrl: String,
    referer: String,
): Pair<ByteArray, String?>? {
    return try {
        val conn = URL(imageUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
        )
        if (referer.isNotBlank()) {
            conn.setRequestProperty("Referer", referer)
        }
        try {
            val cookies = CookieManager.getInstance().getCookie(imageUrl)
            if (!cookies.isNullOrBlank()) {
                conn.setRequestProperty("Cookie", cookies)
            }
        } catch (_: Exception) {}
        conn.connect()
        if (conn.responseCode !in 200..299) {
            conn.disconnect()
            return null
        }
        val ct = conn.contentType?.substringBefore(';')?.trim()?.lowercase()
        val bytes = conn.inputStream.use { it.readBytes() }
        conn.disconnect()
        if (bytes.isEmpty()) null else bytes to ct
    } catch (_: Exception) {
        null
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
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MiuixTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (cat == SniffCategory.IMAGE || (cat == SniffCategory.VIDEO && (res.url.contains(".mp4") || res.url.contains(".webm")))) {
                                                SniffThumbnail(
                                                    url = res.url,
                                                    fallbackIcon = sniffIconFor(cat),
                                                    category = cat
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = sniffIconFor(cat),
                                                    contentDescription = null,
                                                    tint = MiuixTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
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
                "transparent", "blank.gif", "spacer", "pixel.gif", "pixel.png",
                "tracking", "/stat", "/count", "/analytics", "beacon", "/ping", "/log",
                "1x1", "0x0", "base64"
            )
            if (blacklist.any { clean.contains(it) }) return false
            true
        }
        SniffCategory.FILE -> {
            val blacklist = listOf(
                ".js", ".css", ".map", ".json", ".woff", ".woff2", ".ttf", ".otf", ".eot",
                ".php", ".asp", ".aspx", ".jsp", ".cgi", ".do", ".action"
            )
            if (blacklist.any { clean.endsWith(it) }) return false
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
private fun TtsBar(
    show: Boolean,
    isPlaying: Boolean,
    currentIndex: Int,
    totalCount: Int,
    currentText: String,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                if (currentText.isNotBlank()) {
                    Text(
                        text = currentText,
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiuixCircleIconButton(onClick = onPrev, size = 32.dp) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "上一段",
                            tint = if (currentIndex > 0) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    MiuixCircleIconButton(onClick = onPlayPause, size = 36.dp) {
                        Icon(
                            imageVector = if (isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                            contentDescription = if (isPlaying) "暂停" else "继续",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    MiuixCircleIconButton(onClick = onNext, size = 32.dp) {
                        Icon(
                            imageVector = MiuixIcons.ChevronForward,
                            contentDescription = "下一段",
                            tint = if (currentIndex < totalCount - 1) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "${currentIndex + 1} / $totalCount",
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )

                    Spacer(Modifier.weight(1f))

                    MiuixCircleIconButton(onClick = onClose, size = 32.dp) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭朗读",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
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
    containerColor: Color,
    contentColor: Color,
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
            color = containerColor,
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
                        color = contentColor,
                    ),
                    cursorBrush = SolidColor(contentColor),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(contentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "页面内查找",
                                    fontSize = 13.sp,
                                    color = contentColor.copy(alpha = 0.6f),
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
                    color = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.animateContentSize(
                        animationSpec = tween(durationMillis = 180)
                    ),
                )

                Spacer(Modifier.width(10.dp))
                MiuixCircleIconButton(onClick = onPrev, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronBackward,
                        contentDescription = "上一个",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                MiuixCircleIconButton(onClick = onNext, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "下一个",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                MiuixCircleIconButton(onClick = onClose, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭",
                        tint = contentColor,
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
    containerColor: Color,
    contentColor: Color,
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
            color = containerColor,
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
                        color = contentColor,
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "${(percent * 100).roundToInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
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
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(contentColor.copy(alpha = 0.12f))
                        .clickable { onReset() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "重置",
                        fontSize = 13.sp,
                        color = contentColor,
                    )
                }

                Spacer(Modifier.width(8.dp))

                MiuixCircleIconButton(onClick = onClose, size = 32.dp) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FaviconOrFallback(
    favicon: Bitmap?,
    size: Dp,
    fallbackTint: Color = MiuixTheme.colorScheme.onSurface,
) {
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
            tint = fallbackTint,
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

private enum class HistoryFilter { ALL, LAST_7_DAYS, LAST_30_DAYS }
private enum class HistorySort { OLDEST_FIRST, NEWEST_FIRST }

@Composable
private fun HistorySheet(
    show: Boolean,
    history: List<HistoryEntry>,
    closedHistory: List<HistoryEntry>,
    multiSelect: Boolean,
    selectedUrls: List<String>,
    onEnterMultiSelect: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onCancelMultiSelect: () -> Unit,
    onToggleSelectAllFiltered: (List<String>) -> Unit,
    onDeleteSelected: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var sort by remember { mutableStateOf(HistorySort.NEWEST_FIRST) }
    var currentTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(show) {
        if (show) {
            filter = HistoryFilter.ALL
            sort = HistorySort.NEWEST_FIRST
            currentTab = 0
        }
    }

    // 根据 Tab 选出基础列表
    val currentSourceList = remember(history.toList(), closedHistory.toList(), currentTab) {
        when (currentTab) {
            0 -> history
            1 -> closedHistory
            2 -> history.filter { it.isRemote }
            else -> history
        }
    }

    val filteredSorted = remember(currentSourceList.toList(), filter, sort) {
        val now = System.currentTimeMillis()
        val filtered = when (filter) {
            HistoryFilter.ALL -> currentSourceList
            HistoryFilter.LAST_7_DAYS -> currentSourceList.filter {
                now - it.timestamp <= 7L * 24 * 3600 * 1000
            }
            HistoryFilter.LAST_30_DAYS -> currentSourceList.filter {
                now - it.timestamp <= 30L * 24 * 3600 * 1000
            }
        }
        when (sort) {
            HistorySort.NEWEST_FIRST -> filtered.sortedByDescending { it.timestamp }
            HistorySort.OLDEST_FIRST -> filtered.sortedBy { it.timestamp }
        }
    }

    val grouped = remember(filteredSorted) {
        val map = LinkedHashMap<String, MutableList<HistoryEntry>>()
        filteredSorted.forEach { entry ->
            val key = getHistoryDayLabel(entry.timestamp)
            map.getOrPut(key) { mutableListOf() }.add(entry)
        }
        map
    }

    val filteredUrls = remember(filteredSorted) {
        filteredSorted.map { it.url }
    }

    val filterEntries = remember(filter, sort) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(text = "全部", selected = filter == HistoryFilter.ALL, onClick = { filter = HistoryFilter.ALL }),
                    DropdownItem(text = "近7日", selected = filter == HistoryFilter.LAST_7_DAYS, onClick = { filter = HistoryFilter.LAST_7_DAYS }),
                    DropdownItem(text = "近30日", selected = filter == HistoryFilter.LAST_30_DAYS, onClick = { filter = HistoryFilter.LAST_30_DAYS }),
                ),
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(text = "从晚到早", selected = sort == HistorySort.NEWEST_FIRST, onClick = { sort = HistorySort.NEWEST_FIRST }),
                    DropdownItem(text = "从早到晚", selected = sort == HistorySort.OLDEST_FIRST, onClick = { sort = HistorySort.OLDEST_FIRST }),
                ),
            ),
        )
    }

    val allSelected = filteredUrls.isNotEmpty() && selectedUrls.size == filteredUrls.size

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = if (multiSelect) {
            if (selectedUrls.isEmpty()) "请选择项目" else "已选择 ${selectedUrls.size} 项"
        } else {
            "历史记录"
        },
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = !multiSelect,

        startAction = {
            if (multiSelect) {
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
            }
        },

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
                            .clickable { onToggleSelectAllFiltered(filteredUrls) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.SelectAll,
                            contentDescription = if (allSelected) "取消全选" else "全选",
                            tint = if (allSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    WindowIconCascadingDropdownMenu(entries = filterEntries) {
                        Icon(
                            imageVector = MiuixIcons.Filter,
                            contentDescription = "筛选",
                            tint = if (filter != HistoryFilter.ALL || sort != HistorySort.NEWEST_FIRST) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        },
    ) {
        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                return@PredictiveBackHandler
            }
            if (multiSelect) onCancelMultiSelect() else onDismiss()
        }

        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val normalBottomPadding = if (navBarBottom > 12.dp) navBarBottom else 12.dp

        Box(modifier = Modifier.fillMaxWidth()) {
            val bottomPadding by animateDpAsState(
                targetValue = if (multiSelect) 80.dp + navBarBottom else normalBottomPadding,
                animationSpec = tween(durationMillis = 260),
                label = "historyBottomPadding",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 260))
                    .padding(bottom = bottomPadding),
            ) {
                // 主题与界面同款 TabRow
                TabRowWithContour(
                    tabs = listOf("全部", "最近关闭", "其它设备"),
                    selectedTabIndex = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    if (filteredSorted.isEmpty()) {
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
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            grouped.forEach { (dayLabel, entries) ->
                                item(key = "h_day_$dayLabel") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
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
                                    HistoryRow(
                                        modifier = Modifier.animateItem(),
                                        entry = entry,
                                        multiSelect = multiSelect,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (multiSelect) onToggleSelect(entry.url)
                                            else onOpenUrl(entry.url)
                                        },
                                        onLongClick = {
                                            if (!multiSelect) onEnterMultiSelect(entry.url)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = multiSelect,
                enter = slideInVertically(animationSpec = tween(220), initialOffsetY = { it + 40 }) + fadeIn(tween(180)),
                exit = slideOutVertically(animationSpec = tween(200), targetOffsetY = { it + 40 }) + fadeOut(tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navBarBottom + 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .graphicsLayer {
                                shape = RoundedCornerShape(28.dp)
                                shadowElevation = 24.dp.toPx()
                                ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                                spotShadowColor = Color.Transparent
                            }
                            .background(color = MiuixTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            val selectedEntries = (history + closedHistory).filter { it.url in selectedUrls }
                            if (selectedEntries.isNotEmpty()) {
                                val text = selectedEntries.distinctBy { it.url }.joinToString("\n") {
                                    "【${it.title.ifBlank { "无标题" }}】${it.url}"
                                }
                                try {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(send, "分享网页").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                } catch (_: Exception) {}
                            }
                        }) {
                            Icon(imageVector = MiuixIcons.Share, contentDescription = "分享", tint = MiuixTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { if (selectedUrls.isNotEmpty()) onDeleteSelected() }) {
                            Icon(imageVector = MiuixIcons.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    modifier: Modifier = Modifier,
    entry: HistoryEntry,
    multiSelect: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val host = remember(entry.url) {
        try {
            Uri.parse(entry.url).host ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    var favicon by remember(host) {
        mutableStateOf(if (host.isNotBlank()) FaviconStore.get(context, host) else null)
    }
    LaunchedEffect(host) {
        if (host.isNotBlank() && favicon == null) {
            favicon = FaviconStore.fetchFromHost(context, host)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(
                if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.08f)
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (favicon != null) {
                Image(
                    bitmap = favicon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = MiuixIcons.Recent,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(18.dp),
                )
            }
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

        Spacer(Modifier.width(8.dp))

        Text(
            text = when {
                entry.isRemote -> "云端 · " + formatHistoryTime(entry.timestamp)
                else -> formatHistoryTime(entry.timestamp)
            },
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        AnimatedVisibility(
            visible = multiSelect,
            enter = fadeIn(tween(160)) + expandHorizontally(
                animationSpec = tween(220),
                expandFrom = Alignment.End,
            ),
            exit = fadeOut(tween(140)) + shrinkHorizontally(
                animationSpec = tween(200),
                shrinkTowards = Alignment.End,
            ),
        ) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                Checkbox(
                    state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                    onClick = { onClick() },
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
    isNewTabPage: Boolean,
    onEnterMultiSelect: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onCancelMultiSelect: () -> Unit,
    onToggleSelectAllFiltered: (List<String>) -> Unit,
    onDeleteSelected: () -> Unit,
    onShareSelected: (List<String>) -> Unit,
    onMoveSelected: () -> Unit,
    onEditSelected: (String) -> Unit,
    canMove: Boolean,
    faviconReload: Int,
    onAddBookmark: () -> Unit,
    onAddFolder: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onEnterFolder: (BookmarkItem) -> Unit,
    onNavigateToBreadcrumb: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
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

    val selectableItems = currentItems.filter { !it.isFolder }
    val selectableIds = selectableItems.map { it.id }
    val allSelected = selectableIds.isNotEmpty() && selectedIds.containsAll(selectableIds)

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = if (multiSelect) {
            if (selectedIds.isEmpty()) "请选择项目" else "已选择 ${selectedIds.size} 项"
        } else {
            "书签列表"
        },
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = !multiSelect && folderPath.isEmpty(),

        startAction = {
            if (multiSelect) {
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
                            .clickable { onToggleSelectAllFiltered(selectableIds) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.SelectAll,
                            contentDescription = if (allSelected) "取消全选" else "全选",
                            tint = if (allSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 36.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isNewTabPage) { onAddBookmark() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Favorites,
                                contentDescription = "添加书签",
                                tint = if (!isNewTabPage) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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
                }
            }
        },
    ) {
        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                return@PredictiveBackHandler
            }
            if (folderPath.isNotEmpty()) {
                onNavigateToBreadcrumb(folderPath.size - 2)
            } else if (multiSelect) {
                onCancelMultiSelect()
            } else {
                onDismiss()
            }
        }

        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val normalBottomPadding = if (navBarBottom > 12.dp) navBarBottom else 12.dp

        Box(modifier = Modifier.fillMaxWidth()) {
            val bottomPadding by animateDpAsState(
                targetValue = if (multiSelect) 80.dp + navBarBottom else normalBottomPadding,
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
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                items(
                                    count = sortedItems.size,
                                    key = { index -> sortedItems[index].id },
                                ) { index ->
                                    val entry = sortedItems[index]
                                    val isSelected = entry.id in selectedIds
                                    val host = remember(entry.url) {
                                        try {
                                            if (entry.url.isNullOrBlank()) "" else Uri.parse(entry.url).host ?: ""
                                        } catch (_: Exception) {
                                            ""
                                        }
                                    }
                                    var favicon by remember(host) {
                                        mutableStateOf(if (host.isNotBlank()) FaviconStore.get(context, host) else null)
                                    }
                                    // 每次打开书签列表都会重试加载尚未拿到的网站图标
                                    LaunchedEffect(faviconReload, host) {
                                        if (host.isNotBlank() && favicon == null) {
                                            favicon = FaviconStore.get(context, host)
                                                ?: FaviconStore.fetchFromUrl(
                                                    context,
                                                    entry.url ?: "https://$host/",
                                                    host,
                                                )
                                        }
                                    }

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
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (entry.isFolder) {
                                                Icon(
                                                    imageVector = MiuixIcons.Folder,
                                                    contentDescription = null,
                                                    tint = MiuixTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            } else {
                                                if (favicon != null) {
                                                    Image(
                                                        bitmap = favicon!!.asImageBitmap(),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = MiuixIcons.Favorites,
                                                        contentDescription = null,
                                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
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
                                            if (!entry.isFolder) {
                                                Text(
                                                    text = entry.url.orEmpty(),
                                                    fontSize = 12.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                    maxLines = 1,
                                                )
                                            } else {
                                                Text(
                                                    text = "${entry.children.size} 项",
                                                    fontSize = 12.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                    maxLines = 1,
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        AnimatedVisibility(
                                            visible = multiSelect,
                                            enter = fadeIn(tween(160)) + expandHorizontally(
                                                animationSpec = tween(220),
                                                expandFrom = Alignment.End,
                                            ),
                                            exit = fadeOut(tween(140)) + shrinkHorizontally(
                                                animationSpec = tween(200),
                                                shrinkTowards = Alignment.End,
                                            ),
                                        ) {
                                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                                Checkbox(
                                                    state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                                                    onClick = { onToggleSelect(entry.id) },
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

            AnimatedVisibility(
                visible = multiSelect,
                enter = slideInVertically(
                    animationSpec = tween(220),
                    initialOffsetY = { it + 40 },
                ) + fadeIn(tween(180)),
                exit = slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it + 40 },
                ) + fadeOut(tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navBarBottom + 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .graphicsLayer {
                                shape = RoundedCornerShape(28.dp)
                                shadowElevation = 24.dp.toPx()
                                ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                                spotShadowColor = Color.Transparent
                            }
                            .background(
                                color = MiuixTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(28.dp),
                            )
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onShareSelected(selectedIds) }) {
                            Icon(
                                imageVector = MiuixIcons.Share,
                                contentDescription = "分享",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(onClick = {
                            selectedIds.firstOrNull()?.let { onEditSelected(it) }
                        }) {
                            Icon(
                                imageVector = MiuixIcons.Edit,
                                contentDescription = "编辑",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(
                            onClick = { if (selectedIds.isNotEmpty() && canMove) onMoveSelected() },
                            enabled = canMove,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.MoveFile,
                                contentDescription = "移动",
                                tint = if (canMove) MiuixTheme.colorScheme.onSurface
                                else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(onClick = {
                            if (selectedIds.isNotEmpty()) onDeleteSelected()
                        }) {
                            Icon(
                                imageVector = MiuixIcons.Delete,
                                contentDescription = "删除",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val TOOL_SHEET_CELL_HEIGHT = 68.dp
private val TOOL_SHEET_EDITBAR_BOTTOM = 24.dp
private val TOOL_SHEET_NON_EDIT_BOTTOM = 24.dp

// ============================================================
// 标题截断辅助函数
// ============================================================

@Composable
private fun rememberEllipsizedSheetTitle(
    title: String,
    reservedWidth: Dp = 176.dp,
): String {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val availableWidthPx = remember(
        configuration.screenWidthDp, reservedWidth, density
    ) {
        with(density) {
            (configuration.screenWidthDp.dp - reservedWidth).toPx().toInt()
        }.coerceAtLeast(1)
    }

    val titleStyle = MiuixTheme.textStyles.title2

    return remember(title, availableWidthPx, titleStyle) {
        if (title.isEmpty()) return@remember title

        val fullWidth = textMeasurer.measure(
            text = androidx.compose.ui.text.AnnotatedString(title),
            style = titleStyle,
        ).size.width

        if (fullWidth <= availableWidthPx) return@remember title

        val ellipsisWidth = textMeasurer.measure(
            text = androidx.compose.ui.text.AnnotatedString("…"),
            style = titleStyle,
        ).size.width
        val maxContentWidth = (availableWidthPx - ellipsisWidth).coerceAtLeast(0)

        var lo = 0
        var hi = title.length
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            val w = textMeasurer.measure(
                text = androidx.compose.ui.text.AnnotatedString(title.substring(0, mid)),
                style = titleStyle,
            ).size.width
            if (w <= maxContentWidth) lo = mid else hi = mid - 1
        }

        if (lo <= 0) "…" else title.substring(0, lo).trimEnd() + "…"
    }
}

// ============================================================
// 浏览器工具弹窗
// ============================================================

@Composable
private fun BrowserToolSheet(
    show: Boolean,
    pageTitle: String,
    isFullscreen: Boolean,
    isNightMode: Boolean,
    isIncognito: Boolean,
    isDesktopMode: Boolean,
    isNoImage: Boolean,
    isTranslated: Boolean,
    isNewTabPage: Boolean,
    isBookmarked: Boolean,
    onOpenSettings: () -> Unit,
    onOpenToolCustomize: () -> Unit,
    onAddBookmark: () -> Unit,
    onRemoveBookmark: () -> Unit,
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
    val disabledIdSet = remember { toolMenuDisabledIds(prefs).toSet() }
    val state = remember {
        ToolSheetState(
            allToolMenuIds(prefs)
                .filter { it !in disabledIdSet }
                .mapNotNull { id -> allTools.find { it.id == id } }
        )
    }

    var showDeleteBookmarkDialog by remember { mutableStateOf(false) }

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

    WindowBottomSheet(
        show = show,
        onDismissRequest = {
            onDismiss()
        },
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .animateContentSize(animationSpec = tween(durationMillis = 260)),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(24.dp)),
            ) {
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    ToolGrid(
                        state = state,
                        isNewTabPage = isNewTabPage,
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

            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomPadding = if (navBarHeight > 12.dp) navBarHeight else 12.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = bottomPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bookmarkButtonEnabled = !isNewTabPage || isBookmarked
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                        .combinedClickable(
                            enabled = bookmarkButtonEnabled,
                            onClick = {
                                if (isBookmarked) {
                                    showDeleteBookmarkDialog = true
                                } else {
                                    onAddBookmark()
                                }
                            },
                            onLongClick = { onAddBookmark() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isBookmarked) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                        contentDescription = "书签",
                        tint = if (bookmarkButtonEnabled) MiuixTheme.colorScheme.onSurface
                        else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = pageTitle.ifBlank { "新标签页" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // ★ 改动点：新增左侧退出按钮
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                onDismiss()
                                onToolAction("exit", false)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close2,
                            contentDescription = "退出",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onOpenToolCustomize() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "工具弹窗自定义",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = "设置",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
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

    // ★ 改动点：内置的删除书签弹窗
    if (showDeleteBookmarkDialog) {
        WindowDialog(
            show = showDeleteBookmarkDialog,
            title = "删除书签",
            onDismissRequest = { showDeleteBookmarkDialog = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "确认删除该书签？",
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
                        onClick = { showDeleteBookmarkDialog = false },
                        modifier = Modifier.weight(1f),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            onRemoveBookmark()
                            showDeleteBookmarkDialog = false
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
    }
}

@Composable
private fun ToolGrid(
    state: ToolSheetState,
    isNewTabPage: Boolean = false,
    onToolClick: (String) -> Unit,
) {
    val disabledTools = remember {
        setOf(
            "translate", "zoom", "find_in_page", "tts", "sniff",
            "save_page", "print", "share", "play_in_player", "view_source",
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        state.tools.chunked(5).forEach { rowTools ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TOOL_SHEET_CELL_HEIGHT),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowTools.forEach { tool ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .combinedClickable(
                                onClick = {
                                    if (isNewTabPage && tool.id in disabledTools) return@combinedClickable
                                    onToolClick(tool.id)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        ToolGridItem(
                            tool = tool,
                            isActive = tool.id in state.activeTools,
                            isEnabled = !(isNewTabPage && tool.id in disabledTools),
                        )
                    }
                }
                repeat(5 - rowTools.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun ToolGridItem(tool: ToolDef, isActive: Boolean, isEnabled: Boolean = true) {
    if (tool.customItem != null) {
        tool.customItem(isActive)
        return
    }

    val iconColorTarget = if (!isEnabled) {
        MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    } else if (tool.isToggle && isActive) {
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
            color = if (isEnabled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
    }
}

// ============================================================
// ★ 替换：enqueueDownload（探测 MIME 修正扩展名，写入公共目录）
// ============================================================
@android.annotation.SuppressLint("NewApi")
private fun mediaStoreFileNameExists(context: Context, name: String): Boolean {
    return try {
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val args = arrayOf(name, "Download/Mote/")
        context.contentResolver.query(collection, projection, selection, args, null)
            ?.use { it.count > 0 } ?: false
    } catch (_: Exception) {
        false
    }
}

private fun enqueueDownload(context: Context, dl: PendingDownload) {
    Thread {
        val mainHandler = Handler(Looper.getMainLooper())
        try {
            val probedMime = probeMimeType(dl.url, dl.userAgent)

            val finalMime: String = when {
                !probedMime.isNullOrBlank() && !probedMime.startsWith("text/") -> probedMime
                !dl.mimetype.isNullOrBlank() && dl.mimetype != "application/octet-stream" -> dl.mimetype
                else -> "application/octet-stream"
            }

            val ext = extFromMime(finalMime)
            val originalExt = dl.fileName.substringAfterLast('.', "")
            val originalNameNoExt = dl.fileName.substringBeforeLast('.', dl.fileName)
            var finalFileName: String = when {
                dl.fileName.isBlank() -> "download_${System.currentTimeMillis()}$ext"
                originalExt.isBlank() -> "$originalNameNoExt$ext"
                originalExt.equals("bin", ignoreCase = true) -> "$originalNameNoExt$ext"
                ext.isNotBlank() && originalExt.length !in 2..5 -> "$originalNameNoExt$ext"
                else -> dl.fileName
            }

            val useMediaStore = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

            var mediaUriStr: String? = null
            var filePath: String = ""

            if (useMediaStore) {
                // 重名处理：手动查 MediaStore，避免文件名与 task 不一致
                val baseName = finalFileName.substringBeforeLast('.', finalFileName)
                val targetExt = finalFileName.substringAfterLast('.', "")
                var index = 1
                while (mediaStoreFileNameExists(context, finalFileName)) {
                    finalFileName = if (targetExt.isBlank()) {
                        "${baseName}($index)"
                    } else {
                        "${baseName}($index).$targetExt"
                    }
                    index++
                }

                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, finalFileName)
                    put(MediaStore.Downloads.MIME_TYPE, finalMime)
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/Mote/")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = context.contentResolver.insert(collection, values)
                    ?: throw RuntimeException("无法在系统下载目录创建文件")
                mediaUriStr = uri.toString()
                filePath = ""
            } else {
                // Android 9 及以下：老逻辑写入公共 Download/Mote
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Mote",
                )
                if (!dir.exists()) dir.mkdirs()

                val baseName = finalFileName.substringBeforeLast('.', finalFileName)
                val targetExt = finalFileName.substringAfterLast('.', "")
                var index = 1
                while (File(dir, finalFileName).exists()) {
                    finalFileName = if (targetExt.isBlank()) {
                        "${baseName}($index)"
                    } else {
                        "${baseName}($index).$targetExt"
                    }
                    index++
                }

                filePath = File(dir, finalFileName).absolutePath
            }

            val task = MoteDownloadTask(
                url = dl.url,
                fileName = finalFileName,
                mimeType = finalMime,
                filePath = filePath,
                uri = mediaUriStr,
            )

            MoteDownloader.start(context, task)

            mainHandler.post {
                Toast.makeText(context, "开始下载：$finalFileName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            mainHandler.post {
                Toast.makeText(context, "下载失败：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}
// ============================================================
// ★ 替换：shareImageUrl（后台下载 + FileProvider + 正确扩展名）
// ============================================================

private fun shareImageUrl(context: Context, imageUrl: String) {
    Thread {
        val mainHandler = Handler(Looper.getMainLooper())
        try {
            // 1. 下载字节 + 拿服务端 Content-Type
            val result = downloadImageWithMime(imageUrl, "")
            if (result == null || result.first.isEmpty()) {
                mainHandler.post {
                    Toast.makeText(context, "无法下载图片", Toast.LENGTH_SHORT).show()
                }
                return@Thread
            }
            val (bytes, serverMime) = result

            // 2. 决定 MIME
            val mime: String = when {
                !serverMime.isNullOrBlank() && serverMime.startsWith("image/") -> serverMime
                else -> guessMimeFromUrl(imageUrl) ?: "image/jpeg"
            }

            // 3. 决定扩展名（★ 修复 bin 扩展名的关键）
            val ext: String = extFromMime(mime)
                .ifBlank { guessExtFromUrl(imageUrl) }
                .ifBlank { ".jpg" }

            // 4. 写入 cacheDir/images/
            val dir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val fileName = "share_${System.currentTimeMillis()}$ext"
            val file = File(dir, fileName)
            file.writeBytes(bytes)

            // 5. FileProvider → content:// URI
            val authority = "${context.packageName}.fileprovider"
            val uri = try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                mainHandler.post {
                    Toast.makeText(
                        context,
                        "无法生成分享 URI：${e.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                return@Thread
            }

            // 6. 回主线程分享
            mainHandler.post {
                try {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(
                        Intent.createChooser(send, "分享图片").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "分享失败：${e.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        } catch (e: Exception) {
            mainHandler.post {
                Toast.makeText(context, "分享失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun openTtsSettings(context: Context) {
    try {
        val intent = Intent("com.android.settings.TTS_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

private fun copyTextToClipboard(context: Context, text: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as? android.content.ClipboardManager ?: return
        cm.setPrimaryClip(android.content.ClipData.newPlainText("url", text))
        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {}
}
private fun shareTextUrl(context: Context, url: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(send, "分享链接").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePlainText(context: Context, text: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(send, "分享文本").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}
private fun downloadImageBytes(
    context: Context,
    imageUrl: String,
    referer: String,
): ByteArray? {
    return try {
        val conn = URL(imageUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
        )
        if (referer.isNotBlank()) {
            conn.setRequestProperty("Referer", referer)
        }
        try {
            val cookies = CookieManager.getInstance().getCookie(imageUrl)
            if (!cookies.isNullOrBlank()) {
                conn.setRequestProperty("Cookie", cookies)
            }
        } catch (_: Exception) {}
        conn.connect()
        if (conn.responseCode !in 200..299) {
            conn.disconnect()
            return null
        }
        val bytes = conn.inputStream.use { it.readBytes() }
        conn.disconnect()
        if (bytes.isEmpty()) null else bytes
    } catch (_: Exception) {
        null
    }
}

private fun baiduImageSearchUpload(
    context: Context,
    imageUrl: String,
    referer: String,
): String? {
    val imgBytes = downloadImageBytes(context, imageUrl, referer) ?: return null
    return try {
        val boundary = "----MoteBoundary${System.currentTimeMillis()}"
        val conn = URL("https://graph.baidu.com/upload").openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=$boundary"
        )
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
        )
        conn.setRequestProperty("Origin", "https://graph.baidu.com")
        conn.setRequestProperty("Referer", "https://graph.baidu.com/")

        conn.outputStream.use { out ->
            out.write("--$boundary\r\n".toByteArray(Charsets.UTF_8))
            out.write(
                "Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n"
                    .toByteArray(Charsets.UTF_8)
            )
            out.write("Content-Type: image/jpeg\r\n\r\n".toByteArray(Charsets.UTF_8))
            out.write(imgBytes)
            out.write("\r\n".toByteArray(Charsets.UTF_8))
            out.write("--$boundary--\r\n".toByteArray(Charsets.UTF_8))
        }

        if (conn.responseCode !in 200..299) {
            conn.disconnect()
            return null
        }
        val body = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        val json = JSONObject(body)
        val data = json.optJSONObject("data")
        val resultUrl = data?.optString("url", "") ?: ""
        if (resultUrl.isBlank()) null else resultUrl
    } catch (_: Exception) {
        null
    }
}

private fun reverseImageSearchUrl(engineIndex: Int, imageUrl: String): String {
    val encoded = try {
        URLEncoder.encode(imageUrl, "UTF-8")
    } catch (_: Exception) {
        imageUrl
    }
    return when (engineIndex) {
        0 -> "https://www.bing.com/images/search?view=detailv2&iss=sbi&FORM=SBIIDP" +
                "&sbisrc=UrlPaste&q=imgurl:$encoded&idpbck=1"
        2 -> "https://graph.baidu.com/s?tn=pc&image=$encoded"
        4 -> "https://pic.sogou.com/ris?query=$encoded"
        5 -> "https://lens.google.com/uploadbyurl?url=$encoded"
        else -> "https://www.bing.com/images/search?view=detailv2&iss=sbi&FORM=SBIIDP" +
                "&sbisrc=UrlPaste&q=imgurl:$encoded&idpbck=1"
    }
}

private suspend fun performReverseImageSearch(
    context: Context,
    imageUrl: String,
    referer: String,
    engineIndex: Int,
): String? = withContext(Dispatchers.IO) {
    if (imageUrl.isBlank()) return@withContext null

    if (engineIndex == 2) {
        val uploadResult = baiduImageSearchUpload(context, imageUrl, referer)
        if (!uploadResult.isNullOrBlank()) return@withContext uploadResult
    }

    reverseImageSearchUrl(engineIndex, imageUrl)
}

// ============================================================
// 辅助函数
// ============================================================

private fun isWebsite(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return false

    if (trimmed.any { it.code in 0x4E00..0x9FFF || it == ' ' }) return false

    if (Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$").matches(trimmed)) return false

    val lower = trimmed.lowercase()

    if (lower.startsWith("http://") ||
        lower.startsWith("https://") ||
        lower.startsWith("ftp://") ||
        lower.startsWith("file://") ||
        lower.startsWith("content://") ||
        lower.startsWith("about:") ||
        lower.startsWith("data:")
    ) return true

    val authorityPart = lower
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    val hostPort = authorityPart.substringAfterLast('@')
    val host = hostPort.substringBefore(':')
    val port = hostPort.substringAfter(':', "")

    if (host.isEmpty()) return false

    if (port.isNotEmpty() && port.any { !it.isDigit() }) return false

    if (host == "localhost" || host == "127.0.0.1" || host == "::1") return true

    val seg = host.split('.')
    if (seg.size == 4 && seg.all { s ->
            s.isNotEmpty() && s.all { it.isDigit() } &&
                    (s.toIntOrNull()?.let { it in 0..255 } == true)
        }
    ) return true

    val dotIdx = host.lastIndexOf('.')
    if (dotIdx <= 0 || dotIdx == host.length - 1) return false

    val tld = host.substring(dotIdx + 1)

    if (tld.length < 2 || tld.length > 24) return false
    if (!tld.all { it.isLetterOrDigit() || it == '-' }) return false
    if (tld.all { it.isDigit() }) return false

    if (host.split('.').any { it.isEmpty() }) return false

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

private fun tencentTranslateOne(text: String, targetLang: String): String {
    val encoded = URLEncoder.encode(text, "UTF-8")
    val url = "https://api.jaxing.cc/v2/Translate/Tencent" +
            "?sourceText=$encoded&target=$targetLang"
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 5000
    conn.readTimeout = 8000
    conn.requestMethod = "GET"
    conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)
    try {
        if (conn.responseCode != 200) throw RuntimeException("HTTP ${conn.responseCode}")
        val body = conn.inputStream.bufferedReader().readText()
        val obj = JSONObject(body)
        val data = obj.optJSONObject("data") ?: obj
        val translated = data.optString("targetText", "")
            .ifBlank { data.optString("translation", "") }
            .ifBlank { data.optString("result", "") }
        if (translated.isBlank()) throw RuntimeException("empty translation")
        return translated
    } finally {
        conn.disconnect()
    }
}

private fun bilibiliTranslateOne(text: String, targetLang: String): String {
    val url = "https://index-translate.bilibili.com/v1/chat/completions"
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 6000
    conn.readTimeout = 10000
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)

    val langMap = mapOf(
        "zh-CN" to "Chinese", "zh" to "Chinese",
        "en" to "English", "ja" to "Japanese",
        "ko" to "Korean", "fr" to "French",
        "de" to "German", "es" to "Spanish",
    )
    val targetName = langMap[targetLang] ?: targetLang

    val prompt = "Translate the following text to $targetName. " +
            "Only output the translation, no explanation.\n\n$text"

    val payload = JSONObject().apply {
        put("model", "index-translate-35b-a3b")
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        })
        put("temperature", 0.3)
        put("max_tokens", 2048)
    }

    try {
        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        if (conn.responseCode != 200) throw RuntimeException("HTTP ${conn.responseCode}")
        val body = conn.inputStream.bufferedReader().readText()
        val obj = JSONObject(body)
        val choices = obj.optJSONArray("choices") ?: throw RuntimeException("no choices")
        val first = choices.optJSONObject(0) ?: throw RuntimeException("no choice")
        val message = first.optJSONObject("message") ?: throw RuntimeException("no message")
        val translated = message.optString("content", "").trim()
        if (translated.isBlank()) throw RuntimeException("empty translation")
        return translated
    } finally {
        conn.disconnect()
    }
}

private fun bilibiliTranslateBatch(texts: List<String>): List<String>? {
    if (texts.isEmpty()) return emptyList()
    return try {
        val conn = URL("https://index-translate.bilibili.com/v1/chat/completions")
            .openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)
        conn.connectTimeout = 10000
        conn.readTimeout = 15000

        val separator = "\n<<<MOTE_SPLIT>>>\n"
        val combined = texts.joinToString(separator)
        val prompt = "Translate each segment below into zh-CN. " +
                "Keep the separator '<<<MOTE_SPLIT>>>' exactly as is between segments. " +
                "Output only the translated segments with the same separator, no extra text.\n\n" +
                combined

        val payload = JSONObject().apply {
            put("model", "Index-Translate-35B-A3B")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.2)
            put("max_tokens", 4096)
        }
        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode != 200) {
            conn.disconnect()
            return null
        }
        val respBody = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val obj = JSONObject(respBody)
        val choices = obj.optJSONArray("choices") ?: return null
        val content = choices.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content", "")
            ?.trim() ?: return null

        val translated = content.split(separator.trim()).map { it.trim() }
        if (translated.size != texts.size) return null
        translated
    } catch (_: Exception) {
        null
    }
}
private fun translateOneWithFallback(text: String, targetLang: String): String {
    try {
        return tencentTranslateOne(text, targetLang)
    } catch (_: Exception) {}

    try {
        return bilibiliTranslateOne(text, targetLang)
    } catch (_: Exception) {}

    try {
        return myMemoryTranslateOne(text, targetLang)
    } catch (_: Exception) {}

    try {
        return googleTranslateOne(text, targetLang)
    } catch (_: Exception) {}

    throw RuntimeException("all translation sources failed")
}

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

// ============================================================
// ★ 全新翻译引擎：微软 Edge 批量翻译（免授权直连版）
// ============================================================

private fun edgeTranslateTexts(texts: List<String>): List<String>? {
    if (texts.isEmpty()) return emptyList()
    return try {
        val conn = URL(
            "https://edge.microsoft.com/translate/translatetext" +
                    "?from=&to=zh-Hans&isEnterpriseClient=false"
        ).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", FALLBACK_MOBILE_UA)
        conn.connectTimeout = 8000
        conn.readTimeout = 12000

        val body = JSONArray().apply { texts.forEach { put(it) } }
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode == 200) {
            val respText = conn.inputStream.bufferedReader().readText()
            val resArr = JSONArray(respText)
            val result = mutableListOf<String>()
            for (i in 0 until resArr.length()) {
                val obj = resArr.getJSONObject(i)
                val transArr = obj.optJSONArray("translations")
                if (transArr != null && transArr.length() > 0) {
                    result.add(transArr.getJSONObject(0).optString("text", texts[i]))
                } else {
                    result.add(texts[i])
                }
            }
            conn.disconnect()
            result
        } else {
            conn.disconnect()
            null
        }
    } catch (_: Exception) {
        null
    }
}

internal fun translateTextBatch(
    texts: List<String>,
    targetLang: String,
    maxRequests: Int = 600
): List<String> {
    val indicesToTranslate = mutableListOf<Int>()
    val textsToTranslate = mutableListOf<String>()

    texts.forEachIndexed { index, s ->
        val t = s.trim()
        // 过滤掉空白、纯数字、单字符和明显的符号，减少无用请求
        if (t.length > 1 && t.any { it.isLetter() }) {
            indicesToTranslate.add(index)
            textsToTranslate.add(t)
        }
    }

    if (textsToTranslate.isEmpty()) return texts

    val result = texts.toMutableList()

    // 1. 首选：Edge 批量翻译
    var successCount = 0
    for (i in textsToTranslate.indices step 40) {
        val end = minOf(i + 40, textsToTranslate.size)
        val batch = textsToTranslate.subList(i, end)
        val translatedBatch = edgeTranslateTexts(batch)
        if (translatedBatch != null && translatedBatch.size == batch.size) {
            for (j in batch.indices) {
                val originalRaw = texts[indicesToTranslate[i + j]]
                val leading = originalRaw.takeWhile { it.isWhitespace() }
                val trailing = originalRaw.takeLastWhile { it.isWhitespace() }
                result[indicesToTranslate[i + j]] = "$leading${translatedBatch[j]}$trailing"
            }
            successCount += batch.size
        } else {
            break
        }
    }
    if (successCount == textsToTranslate.size) {
        return result
    }

    // 2. 第一兜底：Bilibili 批量翻译（OpenAI 兼容，免费无 Key）
    var bilibiliSuccess = 0
    for (i in textsToTranslate.indices step 20) {
        val end = minOf(i + 20, textsToTranslate.size)
        val batch = textsToTranslate.subList(i, end)
        try {
            val translatedBatch = bilibiliTranslateBatch(batch)
            if (translatedBatch != null && translatedBatch.size == batch.size) {
                for (j in batch.indices) {
                    val originalRaw = texts[indicesToTranslate[i + j]]
                    val leading = originalRaw.takeWhile { it.isWhitespace() }
                    val trailing = originalRaw.takeLastWhile { it.isWhitespace() }
                    result[indicesToTranslate[i + j]] = "$leading${translatedBatch[j]}$trailing"
                }
                bilibiliSuccess += batch.size
            } else {
                break
            }
        } catch (_: Exception) {
            break
        }
    }
    if (bilibiliSuccess == textsToTranslate.size) {
        return result
    }

    // 3. 最终兜底：逐个请求（限制 20 条）
    var requestCount = 0
    for (index in indicesToTranslate) {
        if (requestCount >= 20) break
        requestCount++

        val raw = texts[index]
        val text = textsToTranslate[indicesToTranslate.indexOf(index)]
        try {
            val translated = translateOneWithFallback(text, targetLang)
            val leading = raw.takeWhile { it.isWhitespace() }
            val trailing = raw.takeLastWhile { it.isWhitespace() }
            result[index] = "$leading$translated$trailing"
        } catch (_: Exception) {}
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
// 资源嗅探缩略图加载组件
// ============================================================

@Composable
private fun SniffThumbnail(
    url: String,
    fallbackIcon: ImageVector,
    category: SniffCategory,
) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        isLoading = true
        bitmap = withContext(Dispatchers.IO) {
            try {
                if (!url.startsWith("http")) return@withContext null
                val lower = url.lowercase()
                if (category == SniffCategory.VIDEO) {
                    if (lower.contains(".mp4") || lower.contains(".webm")) {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(url, emptyMap())
                            val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            if (frame != null) {
                                val targetWidth = 72
                                val ratio = targetWidth.toFloat() / frame.width
                                val targetHeight = (frame.height * ratio).toInt().coerceAtLeast(1)
                                Bitmap.createScaledBitmap(frame, targetWidth, targetHeight, true)
                            } else null
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }
                    } else null
                } else if (category == SniffCategory.IMAGE) {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.instanceFollowRedirects = true
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                    conn.connect()
                    if (conn.responseCode in 200..299) {
                        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                        val bmp = BitmapFactory.decodeStream(conn.inputStream, null, options)
                        conn.disconnect()
                        if (bmp != null) {
                            val targetWidth = 72
                            val ratio = targetWidth.toFloat() / bmp.width
                            val targetHeight = (bmp.height * ratio).toInt().coerceAtLeast(1)
                            Bitmap.createScaledBitmap(bmp, targetWidth, targetHeight, true)
                        } else null
                    } else {
                        conn.disconnect()
                        null
                    }
                } else null
            } catch (e: Exception) {
                null
            }
        }
        isLoading = false
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else if (isLoading && (category == SniffCategory.IMAGE || category == SniffCategory.VIDEO)) {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(22.dp),
        )
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
    }
}
// ============================================================
// 网页源码查看（BottomSheet 内嵌 WebView 渲染源码）
// ============================================================

@Composable
private fun SourceWebView(
    html: String,
    wrapEnabled: Boolean,
    fontSizeSp: Int,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val bgHex = if (isDark) "transparent" else "transparent"
    val textHex = if (isDark) "#E6E6E6" else "#1C1C1E"
    val lineNumHex = if (isDark) "#6B6B6B" else "#8E8E93"
    val tagHex = if (isDark) "#7FB3FF" else "#0A66C2"
    val attrHex = if (isDark) "#C792EA" else "#7B30B5"
    val valueHex = if (isDark) "#A8D08D" else "#2E7D32"
    val commentHex = if (isDark) "#7A7A7A" else "#8E8E93"

    val tag = Triple(html, textHex, isDark)
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                this.tag = tag
                loadDataWithBaseURL(
                    null,
                    buildSourceHtml(
                        source = html,
                        textHex = textHex,
                        lineNumHex = lineNumHex,
                        tagHex = tagHex,
                        attrHex = attrHex,
                        valueHex = valueHex,
                        commentHex = commentHex,
                        wrapEnabled = wrapEnabled,
                        fontSizeSp = fontSizeSp,
                    ),
                    "text/html",
                    "utf-8",
                    null,
                )
                onWebViewCreated(this)
            }
        },
        update = { wv ->
            if (wv.tag != tag) {
                wv.tag = tag
                wv.loadDataWithBaseURL(
                    null,
                    buildSourceHtml(
                        source = html,
                        textHex = textHex,
                        lineNumHex = lineNumHex,
                        tagHex = tagHex,
                        attrHex = attrHex,
                        valueHex = valueHex,
                        commentHex = commentHex,
                        wrapEnabled = wrapEnabled,
                        fontSizeSp = fontSizeSp,
                    ),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
        onRelease = { wv ->
            try {
                wv.stopLoading()
                wv.destroy()
            } catch (_: Exception) {
            }
        },
    )
}

private fun buildSourceHtml(
    source: String,
    textHex: String,
    lineNumHex: String,
    tagHex: String,
    attrHex: String,
    valueHex: String,
    commentHex: String,
    wrapEnabled: Boolean,
    fontSizeSp: Int,
): String {
    val lines = source.split("\n")
    val lineHtml = StringBuilder()
    lines.forEachIndexed { index, line ->
        val escaped = line
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        lineHtml.append(
            "<div class=\"line\"><span class=\"ln\">${index + 1}</span>" +
                    "<span class=\"code\">$escaped</span></div>"
        )
    }

    val wrapClass = if (wrapEnabled) "wrap" else "nowrap"

    return """<!DOCTYPE html>
<html><head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  * { box-sizing: border-box; }
  html, body {
    margin: 0; padding: 0;
    background: transparent;
    color: $textHex;
    font: ${fontSizeSp}px/1.55 ui-monospace, Menlo, Consolas, monospace;
  }
  #viewer { padding: 10px 12px 10px 0; }
  .line { display: flex; align-items: flex-start; }
  .ln {
    flex: 0 0 auto;
    width: 44px;
    text-align: right;
    padding-right: 10px;
    color: $lineNumHex;
    user-select: none;
    -webkit-user-select: none;
  }
  .code {
    flex: 1 1 auto;
    white-space: pre-wrap;
    word-break: break-word;
    min-width: 0;
  }
  .nowrap .code {
    white-space: pre;
    word-break: normal;
    overflow-x: auto;
  }
  .tag { color: $tagHex; }
  .attr { color: $attrHex; }
  .value { color: $valueHex; }
  .comment { color: $commentHex; font-style: italic; }
  .mote-find-highlight { background: rgba(255, 200, 0, 0.45); border-radius: 2px; }
  ::-webkit-scrollbar { width: 4px; height: 4px; }
  ::-webkit-scrollbar-thumb { background: rgba(128,128,128,0.4); border-radius: 2px; }
</style>
</head>
<body>
<div id="viewer" class="$wrapClass">$lineHtml</div>
<script>
(function(){
  var root = document.getElementById('viewer');

  function escapeReg(s) {
    return s.replace(/[.*+?^${'$'}{}()|[\]\\]/g, '\\${'$'}&');
  }

  function highlightHtml(text) {
    var esc = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
    esc = esc.replace(/&lt;!--[\s\S]*?--&gt;/g, function(m) {
      return '<span class="comment">' + m + '</span>';
    });
    esc = esc.replace(/(&lt;\/?)([a-zA-Z][a-zA-Z0-9:-]*)([^&]*?)(\/?&gt;)/g, function(_, open, tag, attrs, close) {
      var attrHtml = attrs.replace(/([a-zA-Z_:][-a-zA-Z0-9_:.]*)(\s*=\s*)("[^"]*"|'[^']*'|[^\s>]+)/g,
        '<span class="attr">${'$'}1</span>${'$'}2<span class="value">${'$'}3</span>');
      return open + '<span class="tag">' + tag + '</span>' + attrHtml + close;
    });
    return esc;
  }

  var codeNodes = document.querySelectorAll('.code');
  for (var i = 0; i < codeNodes.length; i++) {
    var raw = codeNodes[i].textContent;
    codeNodes[i].innerHTML = highlightHtml(raw);
  }

  window.moteSetWrap = function(enabled) {
    if (enabled) root.classList.remove('nowrap');
    else root.classList.add('nowrap');
  };
  window.moteSetFontSize = function(px) {
    document.body.style.fontSize = px + 'px';
  };
})();
</script>
</body></html>"""
}


@Composable
fun PrivacyPolicyDialog() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    var showDialog by remember { mutableStateOf(!prefs.getBoolean("privacy_agreed", false)) }
    // 0: 用户协议, 1: 隐私政策
    var currentDocIndex by remember { mutableIntStateOf(0) }

    // ★ 移除了 if (!showDialog) return，保证 Miuix 退场动画能正常执行

    // 1. 不可被返回操作关闭
    androidx.activity.compose.BackHandler(enabled = showDialog) { }

    val onAgree: () -> Unit = {
        prefs.edit().putBoolean("privacy_agreed", true).apply()
        showDialog = false
        val appKey = "A-US-1587001410"
        (context.applicationContext as? com.torachen.mote.MoteApplication)?.initAptabase(appKey)
    }

    // 2. 拒绝后退出应用（加入延迟等待退场动画）
    val onExit: () -> Unit = {
        showDialog = false
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            (context as? android.app.Activity)?.finishAndRemoveTask()
        }, 300) // 300ms 后关闭 Activity
    }

    // 3. 拼接带有下划线、主题蓝的富文本
    val annotatedString = buildAnnotatedString {
        append("欢迎使用 Mote！在开始使用之前，请阅读并同意")

        pushStringAnnotation(tag = "LINK", annotation = "user_agreement")
        withStyle(SpanStyle(color = MiuixTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("《用户协议》")
        }
        pop()

        append("和")

        pushStringAnnotation(tag = "LINK", annotation = "privacy_policy")
        withStyle(SpanStyle(color = MiuixTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("《隐私政策》")
        }
        pop()
        append("。")
    }

    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.window.WindowDialog(
                show = showDialog, // ★ 传入状态，让组件自己播放退场动画
                title = "阅读用户协议与隐私政策",
                onDismissRequest = { /* 禁止外部点击关闭 */ },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 第一段正文：富文本点击
                    Text(
                        text = annotatedString,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        onTextLayout = { textLayoutResult.value = it },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { pos ->
                                textLayoutResult.value?.let { layout ->
                                    val offset = layout.getOffsetForPosition(pos)
                                    annotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset).firstOrNull()?.let {
                                        if (it.item == "user_agreement") currentDocIndex = 0
                                        else if (it.item == "privacy_policy") currentDocIndex = 1
                                    }
                                }
                            }
                        }
                    )

                    // 灰底自适应高度区 + 左右平滑过渡动画 + Markdown渲染
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 400.dp)
                            .animateContentSize(),
                        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                    ) {
                        AnimatedContent(
                            targetState = currentDocIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn()) togetherWith
                                            (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn()) togetherWith
                                            (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut())
                                }
                            },
                            label = "docTransition"
                        ) { targetIndex ->
                            val doc = if (targetIndex == 0) {
                                RemoteLegalDocs.loadInitial(context, RemoteLegalDoc.USER_AGREEMENT)
                            } else {
                                RemoteLegalDocs.loadInitial(context, RemoteLegalDoc.PRIVACY_POLICY)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                MarkdownText(
                                    text = doc,
                                    baseColor = MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = onExit,
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                contentColor = MiuixTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text("拒绝并退出")
                        }
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = onAgree,
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("同意并继续")
                        }
                    }
                }
            }
        }
        UiMode.Material -> {
            // M3 分支保留 if 包裹
            if (showDialog) {
                val m3AnnotatedString = buildAnnotatedString {
                    append("欢迎使用 Mote！在开始使用之前，请阅读并同意")
                    pushStringAnnotation(tag = "LINK", annotation = "user_agreement")
                    withStyle(SpanStyle(color = androidx.compose.material3.MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                        append("《用户协议》")
                    }
                    pop()
                    append("和")
                    pushStringAnnotation(tag = "LINK", annotation = "privacy_policy")
                    withStyle(SpanStyle(color = androidx.compose.material3.MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                        append("《隐私政策》")
                    }
                    pop()
                    append("。")
                }

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { },
                    title = { androidx.compose.material3.Text("阅读用户协议与隐私政策") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            androidx.compose.material3.Text(
                                text = m3AnnotatedString,
                                onTextLayout = { textLayoutResult.value = it },
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures { pos ->
                                        textLayoutResult.value?.let { layout ->
                                            val offset = layout.getOffsetForPosition(pos)
                                            m3AnnotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset).firstOrNull()?.let {
                                                if (it.item == "user_agreement") currentDocIndex = 0
                                                else if (it.item == "privacy_policy") currentDocIndex = 1
                                            }
                                        }
                                    }
                                }
                            )

                            androidx.compose.material3.Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 400.dp)
                                    .animateContentSize(),
                                shape = RoundedCornerShape(12.dp),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                AnimatedContent(
                                    targetState = currentDocIndex,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn()) togetherWith
                                                    (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut())
                                        } else {
                                            (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn()) togetherWith
                                                    (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut())
                                        }
                                    },
                                    label = "docTransition"
                                ) { targetIndex ->
                                    val doc = if (targetIndex == 0) {
                                        RemoteLegalDocs.loadInitial(context, RemoteLegalDoc.USER_AGREEMENT)
                                    } else {
                                        RemoteLegalDocs.loadInitial(context, RemoteLegalDoc.PRIVACY_POLICY)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        MiuixTheme {
                                            MarkdownText(
                                                text = doc,
                                                baseColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = onAgree) {
                            androidx.compose.material3.Text("同意并继续")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = onExit) {
                            androidx.compose.material3.Text("拒绝并退出")
                        }
                    },
                )
            }
        }
    }
}


/**
 * 与 Miuix InputField 外观完全一致的网址输入框，仅把状态换成 TextFieldValue，
 * 以便在获得焦点时可以整体选中内容并唤起文本工具栏。
 */
@Composable
private fun MoteUrlInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnSearch by rememberUpdatedState(onSearch)
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val interactionSource = remember { MutableInteractionSource() }
    val capsuleShape = CircleShape
    val focused = interactionSource.collectIsFocusedAsState().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val textColor = LocalContentColor.current

    val inputTextStyle = MiuixTheme.textStyles.main
        .copy(fontWeight = FontWeight.Medium)
        .copy(color = textColor)

    val labelText by remember(value.text, expanded, label) {
        derivedStateOf { if (!(value.text.isNotEmpty() || expanded)) label else "" }
    }

    BasicTextField(
        value = value,
        onValueChange = currentOnValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) currentOnExpandedChange(true) }
            .semantics {
                onClick {
                    focusRequester.requestFocus()
                    true
                }
            },
        singleLine = true,
        textStyle = inputTextStyle,
        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { currentOnSearch() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.background(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    shape = capsuleShape,
                ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.Search,
                        contentDescription = "搜索",
                        tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                        modifier = Modifier.padding(
                            start = SearchBarDefaults.LeadingIconStartPadding,
                            end = SearchBarDefaults.LeadingIconEndPadding,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = SearchBarDefaults.InputFieldMinHeight),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = labelText,
                            style = TextStyle(
                                fontSize = SearchBarDefaults.InputFieldFontSize,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                        )
                        Box(modifier = Modifier.graphicsLayer { }) {
                            innerTextField()
                        }
                    }
                    AnimatedVisibility(
                        visible = value.text.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier.padding(
                                start = SearchBarDefaults.TrailingIconStartPadding,
                                end = SearchBarDefaults.TrailingIconEndPadding,
                            ),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Basic.SearchCleanup,
                                contentDescription = "清除",
                                tint = MiuixTheme.colorScheme.onSurfaceContainerHighest,
                                modifier = Modifier
                                    .clip(capsuleShape)
                                    .clickable { currentOnValueChange(TextFieldValue("")) },
                            )
                        }
                    }
                }
            }
        },
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
        } else if (focused) {
            focusManager.clearFocus()
        }
    }
}


/** 复制文本到系统剪贴板 */
private fun copyToClipboard(context: Context, text: String) {
    runCatching {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE)
            as? android.content.ClipboardManager ?: return
        manager.setPrimaryClip(android.content.ClipData.newPlainText("Mote", text))
    }
}

/** 读取系统剪贴板纯文本 */
private fun pastedText(context: Context): String? = runCatching {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE)
        as? android.content.ClipboardManager ?: return null
    manager.primaryClip?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)?.coerceToText(context)?.toString()
}.getOrNull()?.takeIf { it.isNotEmpty() }
