@file:Suppress("SpellCheckingInspection", "MissingPermission")
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.torachen.mote.ui

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Rational
import android.view.TextureView
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.torachen.mote.service.PlaybackService
import com.torachen.mote.service.PlaybackCoverHolder
import com.torachen.mote.service.EpisodeItem
import com.torachen.mote.service.EpisodeListResult
import com.torachen.mote.service.HiddenEpisodeWebView
import com.torachen.mote.service.EpisodeSheet
import com.torachen.mote.service.generateEpisodesFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.RotateLeft
import top.yukonga.miuix.kmp.icon.extended.ScreenCapture
import top.yukonga.miuix.kmp.icon.extended.Unlock
import top.yukonga.miuix.kmp.icon.extended.ZoomOut
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import java.io.File
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private val DialogEasingIn = CubicBezierEasing(0.05f, 0f, 0.135f, 1f)
private val DialogEasingOut = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private val AspectEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private val LockMorphEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private const val UI_SCRIM_ALPHA = 0.5f
private const val MENU_ITEM_BG_ALPHA = 0.35f
private const val UI_SPRING_DAMPING = 0.85f
private const val UI_SPRING_STIFFNESS = 800f
private const val SIDE_UI_SLIDE_DISTANCE_DP = 400f
private const val CONTROL_BLUR_RADIUS = 15f
private const val PANEL_BLUR_RADIUS = 25f
private const val ASPECT_ANIM_MS = 450

private const val UI_TOGGLE_ANIM_MS = 220

// ★ 微调模式 —— 每 dp 对应多少毫秒视频
private const val FINE_TUNE_MS_PER_DP = 50f
// ★ 向上滑多少 dp 进入微调模式
private const val FINE_TUNE_TRIGGER_DP = 60f
// ★ 需求 2：向下滑回触发距离的百分比时退出精调模式
private const val FINE_TUNE_EXIT_RATIO = 0.4f

// ★ 底部迷你进度条高度
private val MINI_PROGRESS_BAR_HEIGHT = 3.dp

// ★ 需求 2/3：快速唤出菜单双击判定窗口
//   参考 YouTube / VLC / MX Player：250~300ms 是舒适区间
private const val QUICK_UI_TAP_DELAY_MS = 280L

private val CONTROL_BUTTON_SIZE = 44.dp
private val DOWNLOAD_DIALOG_CORNER = 28.dp
private val OVERLAY_CORNER = 14.dp

private const val LOG_TAG = "MotePlayer"

private const val DESKTOP_PLAYER_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/130.0.0.0 Safari/537.36"

private const val PREVIEW_CACHE_BUCKET_MS = 200L
private const val PREVIEW_CACHE_MAX = 800

private fun previewBucket(timeMs: Long): Long = timeMs / PREVIEW_CACHE_BUCKET_MS

// ============================================================
// 缓存
// ============================================================

private object MediaCacheHolder {
    @Volatile private var instance: SimpleCache? = null
    private const val MAX_CACHE_BYTES = 200L * 1024 * 1024

    @Suppress("DEPRECATION")
    fun get(context: Context): SimpleCache {
        return instance ?: synchronized(this) {
            instance ?: SimpleCache(
                File(context.cacheDir, "mote_media_cache"),
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
            ).also { instance = it }
        }
    }
}

internal object PlaybackSessionHolder {
    @Volatile var player: ExoPlayer? = null
    @Volatile var currentUrl: String = ""
    @Volatile var currentTitle: String = ""
}

// ★ PiP 退出 / Activity 重建时用来恢复播放位置，避免从头开始。
internal object PlaybackPositionHolder {
    @Volatile var positionMs: Long = 0L
    @Volatile var savedUrl: String = ""
}

// ============================================================
// Activity
// ============================================================

class NativePlayerActivity : ComponentActivity() {

    private var videoUrl: String = ""
    private var videoTitle: String = "视频播放"
    private var startPositionMs: Long = 0L
    private var refererUrl: String = ""
    private var playerInitialized: Boolean = false
    private var shouldResumeOnResume: Boolean = false

    // ★ 即将进入 PiP 的短时窗口（onUserLeaveHint / onPause 触发时用）
    @Volatile private var pendingEnterPip: Boolean = false

    private val pipModeState = mutableStateOf(false)

    // ★ 当前是否处于 PiP 或正在进入 PiP
    private fun isPipOrEntering(): Boolean {
        if (pendingEnterPip) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInPictureInPictureMode
        } else false
    }

    fun markEnteringPip() {
        pendingEnterPip = true
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w(LOG_TAG, "Storage permission denied, trying to play anyway")
            Toast.makeText(this, "未授予存储权限，可能无法播放本地视频", Toast.LENGTH_LONG).show()
        } else {
            Log.d(LOG_TAG, "Storage permission granted")
        }
        if (!playerInitialized) {
            playerInitialized = true
            initializePlayerAndUi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
            ?.takeIf { it.isNotBlank() }
            ?: intent.data?.toString()?.takeIf { it.isNotBlank() }
                    ?: run { finish(); return }

        videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE)
            .orEmpty()
            .ifBlank {
                intent.data?.lastPathSegment
                    ?.substringBeforeLast('.')
                    ?.takeIf { it.isNotBlank() }
                    ?: "视频播放"
            }

        startPositionMs = intent.getLongExtra(EXTRA_START_POSITION, 0L)
        refererUrl = intent.getStringExtra(EXTRA_REFERER).orEmpty()

        try {
            val oldPlayer = PlaybackSessionHolder.player
            if (oldPlayer != null) {
                PlaybackSessionHolder.player = null
                oldPlayer.release()
            }
        } catch (_: Exception) {}

        PlaybackSessionHolder.currentUrl = videoUrl
        PlaybackSessionHolder.currentTitle = videoTitle

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val c = WindowCompat.getInsetsController(window, window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (needStoragePermission()) {
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            playerInitialized = true
            initializePlayerAndUi()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipModeState.value = isInPictureInPictureMode
        // ★ 进入/退出 PiP 后清除"即将进入"标记
        if (isInPictureInPictureMode) {
            pendingEnterPip = false
        }
    }

    private fun needStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return false
        val scheme = try { Uri.parse(videoUrl).scheme?.lowercase() } catch (_: Exception) { null }
        if (scheme == "http" || scheme == "https") return false
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun initializePlayerAndUi() {
        setContent {
            val tc = remember { ThemeController(colorSchemeMode = ColorSchemeMode.Dark) }
            val navOwner = rememberNavigationEventDispatcherOwner()
            MiuixTheme(controller = tc) {
                CompositionLocalProvider(
                    LocalNavigationEventDispatcherOwner provides navOwner
                ) {
                    NativePlayerScreen(
                        videoUrl = videoUrl,
                        videoTitle = videoTitle,
                        startPositionMs = startPositionMs,
                        refererUrlInitial = refererUrl,
                        isInPipMode = pipModeState.value,
                        onBack = {
                            try { PlaybackSessionHolder.player?.pause() } catch (_: Exception) {}
                            try { PlaybackSessionHolder.player?.stop() } catch (_: Exception) {}
                            try { PlaybackSessionHolder.player?.release() } catch (_: Exception) {}
                            PlaybackSessionHolder.player = null
                            PlaybackSessionHolder.currentUrl = ""
                            PlaybackSessionHolder.currentTitle = ""
                            try {
                                stopService(Intent(this@NativePlayerActivity, PlaybackService::class.java))
                            } catch (_: Exception) {}
                            finish()
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newUrl = intent.getStringExtra(EXTRA_VIDEO_URL)?.takeIf { it.isNotBlank() }
            ?: intent.data?.toString()?.takeIf { it.isNotBlank() }
        if (!newUrl.isNullOrBlank() && newUrl != videoUrl) {
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val c = WindowCompat.getInsetsController(window, window.decorView)
            c.hide(WindowInsetsCompat.Type.systemBars())
            c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } catch (_: Exception) {}

        if (shouldResumeOnResume) {
            shouldResumeOnResume = false
            try {
                if (!isFinishing) {
                    PlaybackSessionHolder.player?.play()
                }
            } catch (_: Exception) {}
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // ★ PiP 进入不算"用户离开"，跳过暂停逻辑
        if (isPipOrEntering()) return
        if (!isChangingConfigurations) {
            val bgEnabled = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                .getBoolean("player_background_play", false)
            if (!bgEnabled) {
                try {
                    val p = PlaybackSessionHolder.player
                    if (p != null && p.isPlaying) {
                        p.pause()
                        shouldResumeOnResume = true
                    }
                } catch (_: Exception) {}
            }
        }
    }

    override fun onPause() {
        // ★ PiP 进入/处于 PiP 时不走"后台播放关闭则暂停"的分支
        val skipForPip = isPipOrEntering()
        if (!isChangingConfigurations && !skipForPip) {
            val bgEnabled = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                .getBoolean("player_background_play", false)
            if (!bgEnabled) {
                try {
                    val p = PlaybackSessionHolder.player
                    if (p != null && p.isPlaying) {
                        p.pause()
                        shouldResumeOnResume = true
                    }
                } catch (_: Exception) {}
            } else {
                try {
                    val p = PlaybackSessionHolder.player
                    if (p != null && p.isPlaying) {
                        val i = Intent(this, PlaybackService::class.java).apply {
                            action = PlaybackService.ACTION_START
                            putExtra(PlaybackService.EXTRA_TITLE, videoTitle)
                            putExtra(PlaybackService.EXTRA_IS_PLAYING, true)
                            putExtra(PlaybackService.EXTRA_POSITION, p.currentPosition)
                            putExtra(PlaybackService.EXTRA_DURATION, p.duration)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(i)
                        } else {
                            startService(i)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        try {
            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (isChangingConfigurations) return
        // ★ PiP 场景跳过
        if (isPipOrEntering()) return
        val bgEnabled = getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .getBoolean("player_background_play", false)
        if (!bgEnabled) {
            try { PlaybackSessionHolder.player?.pause() } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        try {
            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
        const val EXTRA_FROM_WEB = "extra_from_web"
        const val EXTRA_START_POSITION = "extra_start_position"
        const val EXTRA_REFERER = "extra_referer"

        fun start(
            context: Context,
            url: String,
            title: String,
            fromWeb: Boolean = false,
            startPositionMs: Long = 0L,
            referer: String = "",
        ) {
            val i = Intent(context, NativePlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, url)
                putExtra(EXTRA_VIDEO_TITLE, title)
                putExtra(EXTRA_FROM_WEB, fromWeb)
                putExtra(EXTRA_START_POSITION, startPositionMs)
                if (referer.isNotBlank()) {
                    putExtra(EXTRA_REFERER, referer)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val scheme = Uri.parse(url).scheme
                if (scheme == "content" || scheme == "file") {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(i)
        }
    }
}

// ============================================================
// 工具
// ============================================================

@Composable
private fun buildPlayerMaterialModifier(
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
    shape: Shape,
    advancedBlurRadius: Float = CONTROL_BLUR_RADIUS,
): Modifier {
    val isBlurActive = blurSupported && backdrop != null && materialStyle != 0

    return if (isBlurActive && backdrop != null) {
        if (materialStyle == 2) {
            // 轻透磨砂：与新标签页深色模式参数一致
            val highlight = Highlight.GlassStrokeSmallDark
            val baseSurface = Color(0xFF121212)
            val blendColor = lerp(baseSurface, Color.Black, 0.5f).copy(alpha = 0.5f)

            val blurColors = BlurDefaults.blurColors(
                blendColors = listOf(
                    BlendColorEntry(
                        color = blendColor,
                        mode = BlurBlendMode.SrcOver,
                    ),
                ),
                contrast = 1.25f,
                saturation = 1.25f,
            )
            Modifier
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                    ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                    spotShadowColor = Color.Transparent
                }
                .textureBlur(
                    backdrop = backdrop,
                    shape = shape,
                    blurRadius = 25f,
                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                    colors = blurColors,
                    highlight = highlight,
                )
        } else {
            // 高级材质：保持原有半透明黑
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = shape,
                blurRadius = advancedBlurRadius,
                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = Color.Black.copy(alpha = UI_SCRIM_ALPHA)),
                    ),
                ),
            )
        }
    } else {
        // 普通材质 或 不支持模糊
        Modifier.background(Color.Black.copy(alpha = UI_SCRIM_ALPHA), shape)
    }
}

private fun fmtTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val t = ms / 1000
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

private fun guessStreamMimeType(url: String): String? {
    val clean = url.lowercase().substringBefore('#').substringBefore('?')
    return when {
        clean.endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
        clean.endsWith(".mpd") -> MimeTypes.APPLICATION_MPD
        clean.endsWith(".ism") || clean.endsWith(".isml") -> MimeTypes.APPLICATION_SS
        else -> null
    }
}

private fun buildMediaItem(url: String): MediaItem {
    val builder = MediaItem.Builder().setUri(url)
    guessStreamMimeType(url)?.let { builder.setMimeType(it) }
    return builder.build()
}

private suspend fun captureFrame(ctx: Context, url: String, timeUs: Long): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val r = MediaMetadataRetriever()
            try {
                val uri = Uri.parse(url)
                val scheme = uri.scheme
                if (scheme == "content" || scheme == "file") {
                    r.setDataSource(ctx, uri)
                } else {
                    r.setDataSource(url, emptyMap<String, String>())
                }
                r.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            } finally { try { r.release() } catch (_: Exception) {} }
        } catch (e: OutOfMemoryError) {
            Log.w(LOG_TAG, "captureFrame OOM: ${e.message}")
            null
        } catch (e: Exception) {
            e.printStackTrace(); null
        }
    }

private fun openReusableRetriever(ctx: Context, url: String): MediaMetadataRetriever? {
    return try {
        val uri = Uri.parse(url)
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "file" && scheme != "content") return null
        val r = MediaMetadataRetriever()
        try {
            if (scheme == "content") {
                r.setDataSource(ctx, uri)
            } else {
                val path = uri.path ?: return null
                r.setDataSource(path)
            }
        } catch (e: Exception) {
            try { r.release() } catch (_: Exception) {}
            return null
        }
        r
    } catch (_: Exception) {
        null
    }
}

private suspend fun captureFrameScaledReuse(
    ctx: Context,
    url: String,
    timeUs: Long,
    targetWidth: Int,
    reusable: MediaMetadataRetriever?,
    mutex: Mutex,
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (reusable != null) {
            mutex.withLock {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        reusable.getScaledFrameAtTime(
                            timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST,
                            targetWidth,
                            targetWidth * 9 / 16,
                        )
                    } else {
                        val full = reusable.getFrameAtTime(
                            timeUs, MediaMetadataRetriever.OPTION_CLOSEST
                        )
                        if (full == null) null
                        else if (full.width > targetWidth) {
                            val ratio = targetWidth.toFloat() / full.width
                            Bitmap.createScaledBitmap(
                                full, targetWidth, (full.height * ratio).toInt(), true,
                            )
                        } else full
                    }
                } catch (e: OutOfMemoryError) {
                    Log.w(LOG_TAG, "captureFrameScaledReuse(reuse) OOM: ${e.message}")
                    null
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            val r = MediaMetadataRetriever()
            try {
                val uri = Uri.parse(url)
                val scheme = uri.scheme
                if (scheme == "content" || scheme == "file") {
                    r.setDataSource(ctx, uri)
                } else {
                    r.setDataSource(url, emptyMap<String, String>())
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    r.getScaledFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST,
                        targetWidth,
                        targetWidth * 9 / 16,
                    )
                } else {
                    val full = r.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                    if (full == null) null
                    else if (full.width > targetWidth) {
                        val ratio = targetWidth.toFloat() / full.width
                        Bitmap.createScaledBitmap(
                            full, targetWidth, (full.height * ratio).toInt(), true,
                        )
                    } else full
                }
            } finally {
                try { r.release() } catch (_: Exception) {}
            }
        }
    } catch (e: OutOfMemoryError) {
        Log.w(LOG_TAG, "captureFrameScaledReuse OOM: ${e.message}")
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun captureCoverBytes(
    ctx: Context,
    url: String,
    timeUs: Long,
    targetW: Int = 512,
): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val bmp = captureFrameScaledReuse(
            ctx, url, timeUs, targetW, null, Mutex()
        ) ?: return@withContext null
        if (isSolidColorBitmap(bmp)) return@withContext null
        val baos = java.io.ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        baos.toByteArray()
    } catch (e: OutOfMemoryError) {
        Log.w(LOG_TAG, "captureCoverBytes OOM: ${e.message}")
        null
    } catch (_: Exception) {
        null
    }
}

private fun isSolidColorBitmap(bmp: Bitmap): Boolean {
    val w = bmp.width
    val h = bmp.height
    if (w < 4 || h < 4) return true
    val points = listOf(
        bmp.getPixel(1, 1),
        bmp.getPixel(w - 2, 1),
        bmp.getPixel(1, h - 2),
        bmp.getPixel(w - 2, h - 2),
        bmp.getPixel(w / 2, h / 2),
    )
    val first = points[0]
    return points.all { it == first }
}

private fun saveToGallery(ctx: Context, bmp: Bitmap): Boolean {
    val name = "Mote_${System.currentTimeMillis()}.png"
    val v = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Mote")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val res = ctx.contentResolver
    val uri = try { res.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v) ?: return false }
    catch (e: Exception) { e.printStackTrace(); return false }
    return try {
        val ok = res.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: false
        if (!ok) { try { res.delete(uri, null, null) } catch (_: Exception) {}; return false }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            res.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        }
        true
    } catch (e: Exception) { e.printStackTrace(); try { res.delete(uri, null, null) } catch (_: Exception) {}; false }
}

private fun vibrateShort(ctx: Context) {
    try {
        val v = ctx.getSystemService(Vibrator::class.java) ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(30)
        }
    } catch (_: Exception) {}
}

private fun copyLocalToDownloads(ctx: Context, uri: Uri, name: String) {
    val resolver = ctx.contentResolver
    val collection: Uri
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }
    val dest = resolver.insert(collection, values)
        ?: throw IllegalStateException("无法创建下载文件")
    resolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "无法读取源文件" }
        resolver.openOutputStream(dest).use { output ->
            requireNotNull(output) { "无法写入目标文件" }
            input.copyTo(output, 8192)
        }
    }
}

private fun 内部工具Video(ctx: Context, url: String, name: String) {
    try {
        val safeName = name
            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .take(80)
            .let { if (it.contains(".")) it else "$it.mp4" }
        val uri = Uri.parse(url)
        val scheme = uri.scheme
        if (scheme != "http" && scheme != "https") {
            copyLocalToDownloads(ctx, uri, safeName)
            Toast.makeText(ctx, "已保存到下载目录：$safeName", Toast.LENGTH_SHORT).show()
            return
        }
        val request = DownloadManager.Request(uri).apply {
            setTitle(safeName)
            setDescription("正在下载视频…")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val dm = ctx.applicationContext
            .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(ctx, "开始下载：$safeName", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(ctx, "下载失败：${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun shareVideo(ctx: Context, url: String, title: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(send, "分享视频").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.applicationContext.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(ctx, "分享失败：${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ============================================================
// 书签
// ============================================================

internal fun isVideoBookmarked(ctx: Context, url: String): Boolean {
    return try {
        val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("bookmarks_v2", null) ?: return false
        val json = org.json.JSONArray(raw)
        for (i in 0 until json.length()) {
            val o = json.optJSONObject(i) ?: continue
            if (o.optString("url") == url) return true
        }
        false
    } catch (_: Exception) { false }
}

internal fun removeVideoBookmark(ctx: Context, url: String): Boolean {
    return try {
        val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("bookmarks_v2", null) ?: return false
        val json = org.json.JSONArray(raw)
        val newJson = org.json.JSONArray()
        var removed = false
        for (i in 0 until json.length()) {
            val o = json.optJSONObject(i) ?: continue
            if (o.optString("url") == url) {
                removed = true
            } else {
                newJson.put(o)
            }
        }
        if (removed) {
            prefs.edit().putString("bookmarks_v2", newJson.toString()).apply()
        }
        removed
    } catch (_: Exception) { false }
}

internal fun addBookmarkToRoot(ctx: Context, title: String, url: String) {
    try {
        val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("bookmarks_v2", null)
        val json = if (raw.isNullOrEmpty()) org.json.JSONArray() else org.json.JSONArray(raw)
        val newItem = org.json.JSONObject().apply {
            put("id", java.util.UUID.randomUUID().toString())
            put("title", title.ifBlank { url })
            put("url", url)
            put("isFolder", false)
            put("time", System.currentTimeMillis())
        }
        var exists = false
        for (i in 0 until json.length()) {
            val o = json.optJSONObject(i) ?: continue
            if (o.optString("url") == url) { exists = true; break }
        }
        if (!exists) json.put(newItem)
        prefs.edit().putString("bookmarks_v2", json.toString()).apply()
    } catch (e: Exception) { e.printStackTrace() }
}

// ============================================================
// 系统状态
// ============================================================

@Composable
private fun rememberTimeText(): String {
    var t by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val c = Calendar.getInstance()
            t = String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
            delay(1000)
        }
    }
    return t
}

@Composable
private fun rememberBattery(ctx: Context): Pair<Int, Boolean> {
    var info by remember { mutableStateOf(100 to false) }
    DisposableEffect(ctx) {
        val r = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                val l = i?.getIntExtra("level", -1) ?: -1
                val s = i?.getIntExtra("scale", -1) ?: -1
                val st = i?.getIntExtra("status", -1) ?: -1
                if (l >= 0 && s > 0) info = (l * 100 / s) to (st == 2 || st == 5)
            }
        }
        ctx.registerReceiver(r, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { try { ctx.unregisterReceiver(r) } catch (_: Exception) {} }
    }
    return info
}

@Composable
private fun rememberNetwork(ctx: Context): String {
    var t by remember { mutableStateOf("离线") }
    DisposableEffect(ctx) {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fun detect(): String = try {
            val n = cm.activeNetwork; val c = n?.let { cm.getNetworkCapabilities(it) }
            when {
                c == null -> "离线"
                c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WLAN"
                c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "有线"
                c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    try {
                        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        @Suppress("DEPRECATION") when (tm.dataNetworkType) {
                            TelephonyManager.NETWORK_TYPE_NR -> "5G"
                            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                            TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_CDMA -> "2G"
                            else -> "移动网络"
                        }
                    } catch (_: Exception) { "移动网络" }
                }
                else -> "离线"
            }
        } catch (_: Exception) { "离线" }
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: Network) { t = detect() }
            override fun onLost(n: Network) { t = detect() }
            override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) { t = detect() }
        }
        try { cm.registerDefaultNetworkCallback(cb) } catch (_: Exception) {}
        t = detect()
        onDispose { try { cm.unregisterNetworkCallback(cb) } catch (_: Exception) {} }
    }
    return t
}

// ============================================================
// 主界面
// ============================================================

@Composable
private fun NativePlayerScreen(
    videoUrl: String,
    videoTitle: String,
    startPositionMs: Long = 0L,
    refererUrlInitial: String = "",
    isInPipMode: Boolean = false,
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val prefs = remember { ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    var playerMaterialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "ui_material_style") {
                playerMaterialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val audioManager = remember {
        ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val activity = ctx as? Activity
    val density = LocalDensity.current

    var refererUrlState by remember { mutableStateOf(refererUrlInitial) }
    val refererUrl = refererUrlState

    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null

    // 滑动避让区（很小，为了滑动生效面积大）
    val systemGesturesTopPx = with(density) {
        WindowInsets.systemGestures.asPaddingValues().calculateTopPadding().toPx()
    }
    val systemGesturesBottomPx = with(density) {
        WindowInsets.systemGestures.asPaddingValues().calculateBottomPadding().toPx()
    }
    val topReservedPx = maxOf(systemGesturesTopPx, with(density) { 24.dp.toPx() })
    val bottomReservedPx = maxOf(systemGesturesBottomPx, with(density) { 24.dp.toPx() })

    // ★ 点击避让区（更大，用于屏蔽顶部下拉状态栏的误触）
    val tapReservedTopPx = with(density) { 40.dp.toPx() }

    val mediaSourceFactory = remember(videoUrl, refererUrl) {
        val ctxApp = ctx.applicationContext
        val parsedUri = Uri.parse(videoUrl)
        val scheme = parsedUri.scheme?.lowercase() ?: ""
        val host = parsedUri.host ?: ""
        val effectiveReferer: String = when {
            refererUrl.isNotBlank() -> refererUrl
            scheme.startsWith("http") && host.isNotBlank() -> "$scheme://$host/"
            else -> ""
        }

        val effectiveOrigin: String = try {
            if (effectiveReferer.isNotBlank()) {
                val r = Uri.parse(effectiveReferer)
                if (r.scheme != null && r.host != null) "${r.scheme}://${r.host}" else ""
            } else ""
        } catch (_: Exception) { "" }

        val cookie: String = try {
            CookieManager.getInstance().getCookie(videoUrl) ?: ""
        } catch (_: Exception) { "" }

        val headers = HashMap<String, String>()
        if (effectiveReferer.isNotBlank()) headers["Referer"] = effectiveReferer
        if (effectiveOrigin.isNotBlank()) headers["Origin"] = effectiveOrigin
        if (cookie.isNotBlank()) headers["Cookie"] = cookie

        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
        val any = DefaultDataSource.Factory(ctxApp, http)
        val isRemote = scheme.startsWith("http")
        val source = if (isRemote) {
            CacheDataSource.Factory()
                .setCache(MediaCacheHolder.get(ctxApp))
                .setUpstreamDataSourceFactory(any)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            any
        }
        DefaultMediaSourceFactory(source)
    }

    val mediaSourceFactoryRef = rememberUpdatedState(mediaSourceFactory)
    val switchMutex = remember { Mutex() }

    val loadControl = remember {
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(30_000, 90_000, 1_500, 3_000)
            .setBackBuffer(30_000, true)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    val renderersFactory = remember {
        DefaultRenderersFactory(ctx).apply {
            setEnableDecoderFallback(true)
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableAudioTrackPlaybackParams(true)
        }
    }

    val exo = remember {
        ExoPlayer.Builder(ctx, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(LOG_TAG, "===== Playback Error =====")
                        Log.e(LOG_TAG, "errorCodeName: ${error.errorCodeName}")
                        Log.e(LOG_TAG, "errorCode: ${error.errorCode}")
                        Log.e(LOG_TAG, "message: ${error.message}")
                        error.printStackTrace()
                        val cause = error.cause
                        if (cause is HttpDataSource.InvalidResponseCodeException) {
                            Log.e(LOG_TAG, "HTTP responseCode: ${cause.responseCode}")
                            Log.e(LOG_TAG, "HTTP responseMessage: ${cause.responseMessage}")
                            Log.e(LOG_TAG, "HTTP dataSpec uri: ${cause.dataSpec.uri}")
                        }
                    }
                })
                setMediaSource(mediaSourceFactory.createMediaSource(buildMediaItem(videoUrl)))
                prepare()
                playWhenReady = true
                // ★ 优先用 intent 传入的位置；
                //   否则如果 holder 里保存的是同一个 URL 的位置，恢复它，
                //   避免 PiP 退出回到全屏后从头播放。
                val effectiveStartPos: Long = when {
                    startPositionMs > 0L -> startPositionMs
                    PlaybackPositionHolder.savedUrl == videoUrl &&
                            PlaybackPositionHolder.positionMs > 0L ->
                        PlaybackPositionHolder.positionMs
                    else -> 0L
                }
                if (effectiveStartPos > 0L) {
                    seekTo(effectiveStartPos)
                }
            }
    }

    DisposableEffect(exo) {
        PlaybackSessionHolder.player = exo
        onDispose {}
    }

    DisposableEffect(Unit) {
        onDispose {
            // ★ 先保存播放位置（不论后续怎么释放 exo）
            try {
                PlaybackPositionHolder.positionMs = exo.currentPosition
                PlaybackPositionHolder.savedUrl = videoUrl
            } catch (_: Exception) {}

            if (PlaybackSessionHolder.player !== exo) return@onDispose
            val bgEnabled = prefs.getBoolean("player_background_play", false)
            val changingConfig = activity?.isChangingConfigurations == true
            if (bgEnabled && !changingConfig) {
                try { exo.clearVideoSurface() } catch (_: Exception) {}
                PlaybackSessionHolder.player = exo
            } else {
                try { exo.pause() } catch (_: Exception) {}
                try { exo.stop() } catch (_: Exception) {}
                try { exo.release() } catch (_: Exception) {}
                PlaybackSessionHolder.player = null
            }
        }
    }

    val playerContainerRef = remember { mutableStateOf<AspectRatioFrameLayout?>(null) }
    val textureViewRef = remember { mutableStateOf<TextureView?>(null) }

    var isPlaying by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var duration by remember { mutableLongStateOf(0L) }
    var position by remember { mutableLongStateOf(startPositionMs) }
    var buffered by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var isSwipeSeeking by remember { mutableStateOf(false) }
    var swipeTarget by remember { mutableLongStateOf(0L) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmapBucket by remember { mutableLongStateOf(-1L) }
    var lastPreviewFetchMs by remember { mutableLongStateOf(-1000L) }
    val previewJob = remember { mutableStateOf<Job?>(null) }
    val latestSeekTime = remember { mutableLongStateOf(0L) }

    // ★ tap 手势相关
    var tapStartY by remember { mutableFloatStateOf(0f) }
    var wasLongPressInThisGesture by remember { mutableStateOf(false) }
    var controlsVisibleBeforeLongPress by remember { mutableStateOf(true) }

    // ★ 快速唤出菜单（默认关闭）
    var quickUiToggle by remember {
        mutableStateOf(prefs.getBoolean("player_quick_ui_toggle", false))
    }
    val tapToggleJob = remember { mutableStateOf<Job?>(null) }
    // ★ 用时间戳判定双击（比 Job.isActive 更可靠）
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val reusableRetriever = remember(videoUrl) { mutableStateOf<MediaMetadataRetriever?>(null) }
    val retrieverMutex = remember { Mutex() }

    DisposableEffect(videoUrl) {
        val job = scope.launch(Dispatchers.IO) {
            val r = openReusableRetriever(ctx, videoUrl)
            withContext(Dispatchers.Main) {
                reusableRetriever.value = r
            }
        }
        onDispose {
            job.cancel()
            try { reusableRetriever.value?.release() } catch (_: Exception) {}
            reusableRetriever.value = null
        }
    }

    val frameDispatcher = remember { Dispatchers.IO.limitedParallelism(2) }

    val previewCache = remember {
        Collections.synchronizedMap(
            object : LinkedHashMap<Long, Bitmap>(PREVIEW_CACHE_MAX, 0.75f, true) {
                override fun removeEldestEntry(
                    eldest: MutableMap.MutableEntry<Long, Bitmap>?
                ): Boolean = size > PREVIEW_CACHE_MAX
            }
        )
    }

    LaunchedEffect(videoUrl) {
        try { previewCache.clear() } catch (_: Exception) {}
        previewBitmap = null
        previewBitmapBucket = -1L
        lastPreviewFetchMs = -1000L
        latestSeekTime.value = 0L
    }

    var isEnded by remember { mutableStateOf(false) }
    var uiTick by remember { mutableIntStateOf(0) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var isLongPressSpeed by remember { mutableStateOf(false) }
    var pendingCapture by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var frontCameraAreaEnabled by remember {
        mutableStateOf(prefs.getBoolean("player_front_area", true))
    }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var pendingDownloadName by remember { mutableStateOf("") }

    var backgroundPlayEnabled by remember {
        mutableStateOf(prefs.getBoolean("player_background_play", false))
    }

    var bottomProgressBarEnabled by remember {
        mutableStateOf(prefs.getBoolean("player_bottom_progress_bar", false))
    }

    var isCurrentVideoBookmarked by remember { mutableStateOf(false) }
    LaunchedEffect(videoUrl) {
        isCurrentVideoBookmarked = isVideoBookmarked(ctx, videoUrl)
    }

    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }

    var brightnessOverlayVisible by remember { mutableStateOf(false) }
    var brightnessOverlayValue by remember { mutableFloatStateOf(0.5f) }
    var volumeOverlayVisible by remember { mutableStateOf(false) }
    var volumeOverlayValue by remember { mutableFloatStateOf(0.5f) }

    val userManuallyRotatedState = remember { mutableStateOf(false) }

    var showEpisodeSheet by remember { mutableStateOf(false) }

    val isWebPageUrl = (refererUrl.startsWith("http://") ||
            refererUrl.startsWith("https://")) &&
            !refererUrl.endsWith(".m3u8", true) &&
            !refererUrl.endsWith(".mp4", true) &&
            !refererUrl.endsWith(".webm", true) &&
            refererUrl.length > "https://x.x/".length

    var episodeList by remember(refererUrlInitial) {
        val isInitialWebPage = (refererUrlInitial.startsWith("http://") ||
                refererUrlInitial.startsWith("https://")) &&
                !refererUrlInitial.endsWith(".m3u8", true) &&
                !refererUrlInitial.endsWith(".mp4", true) &&
                !refererUrlInitial.endsWith(".webm", true) &&
                refererUrlInitial.length > "https://x.x/".length
        mutableStateOf(
            if (isInitialWebPage) generateEpisodesFromUrl(refererUrlInitial)
            else null
        )
    }

    // ★ 只有 DOM 真实抓取到 > 1 集时，才把选集按钮视为"可信"并显示。
    var episodeListTrusted by remember(refererUrlInitial) { mutableStateOf(false) }

    val timeText = rememberTimeText()
    val (battery, charging) = rememberBattery(ctx)
    val network = rememberNetwork(ctx)

    fun bump() { uiTick++ }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(ctx, "Android 8.0 及以上才支持小窗", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val act = ctx as? Activity ?: return
            // ★ 提前标记"即将进入 PiP"，让 onPause / onUserLeaveHint 跳过暂停逻辑
            (act as? NativePlayerActivity)?.markEnteringPip()
            val ar = if (videoAspectRatio > 1f) {
                Rational(16, 9)
            } else {
                Rational(9, 16)
            }
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(ar)
                .build()
            act.enterPictureInPictureMode(params)
        } catch (e: Exception) {
            Toast.makeText(ctx, "无法进入小窗：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentBrightness(): Float {
        val w = activity?.window ?: return 0.5f
        val cur = w.attributes.screenBrightness
        if (cur >= 0f) return cur
        return try {
            Settings.System.getInt(
                ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS,
            ) / 255f
        } catch (_: Exception) { 0.5f }
    }

    fun applyBrightness(v: Float) {
        val w = activity?.window ?: return
        val attrs = w.attributes
        attrs.screenBrightness = v.coerceIn(0.01f, 1f)
        w.attributes = attrs
    }

    fun buildServiceIntent(): Intent = Intent(ctx, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_START
        putExtra(PlaybackService.EXTRA_TITLE, videoTitle)
        putExtra(PlaybackService.EXTRA_IS_PLAYING, isPlaying)
        putExtra(PlaybackService.EXTRA_POSITION, position)
        putExtra(PlaybackService.EXTRA_DURATION, duration)
    }

    fun startPlaybackService() {
        val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notification_bar_enabled", true)) return
        try {
            val intent = buildServiceIntent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (_: Exception) {}
    }

    fun notifyServiceRefresh() {
        if (!backgroundPlayEnabled) return
        val prefs = ctx.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notification_bar_enabled", true)) return
        val intent = buildServiceIntent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (_: Exception) {
            try { ctx.startService(intent) } catch (_: Exception) {}
        }
    }

    fun stopPlaybackService() {
        try {
            ctx.stopService(Intent(ctx, PlaybackService::class.java))
        } catch (_: Exception) {}
    }

    fun captureCoverAsync() {
        scope.launch(Dispatchers.IO) {
            var waitCount = 0
            while (duration <= 0 && waitCount < 50) {
                delay(100)
                waitCount++
            }

            val positions = buildList {
                val d = duration
                if (d > 0) {
                    add(d / 10)
                    add(d / 4)
                    add(d / 2)
                    add(d * 2 / 3)
                } else {
                    add(10_000L)
                }
            }
            var cover: ByteArray? = null
            for (p in positions) {
                val bytes = captureCoverBytes(ctx, videoUrl, p * 1000L, 512)
                if (bytes != null) {
                    cover = bytes
                    break
                }
            }
            if (cover == null) {
                cover = captureCoverBytes(ctx, videoUrl, 0L, 512)
            }
            PlaybackCoverHolder.coverBytes = cover
            if (cover != null) {
                PlaybackCoverHolder.coverVersion++
                withContext(Dispatchers.Main) {
                    notifyServiceRefresh()
                }
            }
        }
    }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            backgroundPlayEnabled = true
            prefs.edit().putBoolean("player_background_play", true).apply()
            startPlaybackService()
            captureCoverAsync()
        } else {
            backgroundPlayEnabled = false
            prefs.edit().putBoolean("player_background_play", false).apply()
            Toast.makeText(ctx, "未授予通知权限，无法开启后台播放", Toast.LENGTH_LONG).show()
        }
    }

    fun toggleBackgroundPlay(enable: Boolean) {
        if (!enable) {
            backgroundPlayEnabled = false
            prefs.edit().putBoolean("player_background_play", false).apply()
            stopPlaybackService()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                backgroundPlayEnabled = true
                prefs.edit().putBoolean("player_background_play", true).apply()
                startPlaybackService()
                captureCoverAsync()
            } else {
                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            backgroundPlayEnabled = true
            prefs.edit().putBoolean("player_background_play", true).apply()
            startPlaybackService()
            captureCoverAsync()
        }
    }

    DisposableEffect(exo) {
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { isPlaying = p }
            override fun onPlaybackStateChanged(s: Int) {
                if (s == Player.STATE_READY || s == Player.STATE_BUFFERING) duration = exo.duration.coerceAtLeast(0L)
                if (s == Player.STATE_ENDED) {
                    isEnded = true
                    controlsVisible = true
                    bump()
                }
                if (s == Player.STATE_READY && exo.currentPosition < exo.duration - 500) isEnded = false
            }
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val newAspect = videoSize.width.toFloat() / videoSize.height.toFloat()
                    if (abs(newAspect - videoAspectRatio) > 0.001f) {
                        videoAspectRatio = newAspect
                        playerContainerRef.value?.setAspectRatio(newAspect)
                    }
                    if (!userManuallyRotatedState.value) {
                        val act = activity ?: return
                        val wantPortrait = videoSize.height >= videoSize.width
                        val wantOrientation = if (wantPortrait) {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                        if (act.requestedOrientation != wantOrientation) {
                            try {
                                act.requestedOrientation = wantOrientation
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
        exo.addListener(l); onDispose { exo.removeListener(l) }
    }

    LaunchedEffect(exo) {
        while (true) {
            if (!isSeeking) position = exo.currentPosition.coerceAtLeast(0L)
            buffered = exo.bufferedPosition.coerceAtLeast(0L)
            val d = exo.duration; if (d > 0) duration = d
            delay(400)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, isLocked, isSeeking, isSwipeSeeking, showMoreMenu, showSpeedMenu, isEnded, uiTick) {
        if (controlsVisible && isPlaying && !isEnded && !isSeeking && !isSwipeSeeking && !showMoreMenu && !showSpeedMenu && !isLongPressSpeed && !isLocked) {
            delay(3000); controlsVisible = false
        }
    }

    LaunchedEffect(Unit) {
        val a = ctx as? Activity ?: return@LaunchedEffect
        val c = WindowCompat.getInsetsController(a.window, a.window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    DisposableEffect(Unit) {
        onDispose {
            previewJob.value?.cancel()
            previewJob.value = null
            try { previewCache.clear() } catch (_: Exception) {}
            tapToggleJob.value?.cancel()
            tapToggleJob.value = null
        }
    }

    val writePermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val b = pendingCapture; pendingCapture = null
        if (granted && b != null) scope.launch {
            val ok = withContext(Dispatchers.IO) { saveToGallery(ctx, b) }
            Toast.makeText(ctx, if (ok) "已保存到相册" else "保存失败", Toast.LENGTH_SHORT).show()
        } else if (!granted) Toast.makeText(ctx, "未授予存储权限", Toast.LENGTH_SHORT).show()
    }

    fun doCapture() {
        if (isCapturing) return
        isCapturing = true
        Toast.makeText(ctx, "正在截屏…", Toast.LENGTH_SHORT).show()
        val us = exo.currentPosition * 1000L
        scope.launch {
            val bmp = captureFrame(ctx, videoUrl, us); isCapturing = false
            if (bmp == null) { Toast.makeText(ctx, "截图失败", Toast.LENGTH_SHORT).show(); return@launch }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val ok = withContext(Dispatchers.IO) { saveToGallery(ctx, bmp) }
                Toast.makeText(ctx, if (ok) "已保存到相册" else "保存失败", Toast.LENGTH_SHORT).show()
            } else {
                val g = ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                if (g) { val ok = withContext(Dispatchers.IO) { saveToGallery(ctx, bmp) }; Toast.makeText(ctx, if (ok) "已保存到相册" else "保存失败", Toast.LENGTH_SHORT).show() }
                else { pendingCapture = bmp; writePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
            }
        }
    }

    fun setSpeed(s: Float) { speed = s; exo.playbackParameters = PlaybackParameters(s) }

    fun fetchPreviewFrame(timeMs: Long) {
        latestSeekTime.value = timeMs
        val bucket = previewBucket(timeMs)
        val cached = try { previewCache[bucket] } catch (_: Exception) { null }
        if (cached != null) {
            previewBitmap = cached
            previewBitmapBucket = bucket
            return
        }
        if (previewJob.value?.isActive == true) return

        previewJob.value = scope.launch(frameDispatcher) {
            while (isActive) {
                val target = latestSeekTime.value
                if (target <= 0L) break
                val targetBucket = previewBucket(target)
                val c = try { previewCache[targetBucket] } catch (_: Exception) { null }
                if (c != null) {
                    withContext(Dispatchers.Main) {
                        previewBitmap = c
                        previewBitmapBucket = targetBucket
                    }
                    break
                }
                val bmp = captureFrameScaledReuse(
                    ctx = ctx, url = videoUrl, timeUs = target * 1000L,
                    targetWidth = 160,
                    reusable = reusableRetriever.value,
                    mutex = retrieverMutex,
                )
                if (bmp != null) {
                    try { previewCache[targetBucket] = bmp } catch (_: Exception) {}
                    withContext(Dispatchers.Main) {
                        if (abs(swipeTarget - target) < 800L) {
                            previewBitmap = bmp
                            previewBitmapBucket = targetBucket
                        }
                    }
                }
                if (latestSeekTime.value == target) break
            }
        }
    }

    fun clearPreview() {
        previewJob.value?.cancel()
        previewJob.value = null
        previewBitmap = null
        previewBitmapBucket = -1L
        lastPreviewFetchMs = -1000L
        latestSeekTime.value = 0L
    }

    LaunchedEffect(isSwipeSeeking) {
        if (!isSwipeSeeking) return@LaunchedEffect
        val maxMs = duration
        if (maxMs <= 0) return@LaunchedEffect
        var lastCenter = -1L
        while (isActive) {
            val centerMs = swipeTarget
            val centerBucket = centerMs / 500L
            if (centerBucket == lastCenter) {
                delay(150)
                continue
            }
            lastCenter = centerBucket
            val batch = listOf(
                (centerMs - 500L).coerceAtLeast(0L),
                centerMs,
                (centerMs + 500L).coerceAtMost(maxMs),
            ).distinct()
            val missing = batch.filter {
                try { previewCache[previewBucket(it)] } catch (_: Exception) { null } == null
            }
            if (missing.isEmpty()) {
                delay(150)
                continue
            }
            try {
                withContext(frameDispatcher) {
                    missing.map { t ->
                        async {
                            if (!isActive) return@async
                            if (abs(swipeTarget - centerMs) > 3_000L) return@async
                            val bmp = captureFrameScaledReuse(
                                ctx = ctx, url = videoUrl, timeUs = t * 1000L,
                                targetWidth = 160,
                                reusable = reusableRetriever.value,
                                mutex = retrieverMutex,
                            )
                            if (bmp != null) {
                                try { previewCache[previewBucket(t)] = bmp } catch (_: Exception) {}
                            }
                        }
                    }.awaitAll()
                }
            } catch (_: Exception) {}
        }
    }

    BackHandler {
        when {
            showDownloadDialog -> showDownloadDialog = false
            showSpeedMenu -> showSpeedMenu = false
            showMoreMenu -> showMoreMenu = false
            isLocked -> { isLocked = false; controlsVisible = true }
            else -> onBack()
        }
    }

    val cutoutL = WindowInsets.displayCutout.asPaddingValues()
        .calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
    val cutoutR = WindowInsets.displayCutout.asPaddingValues()
        .calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
    val statusL = WindowInsets.statusBars.asPaddingValues()
        .calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
    val navR = WindowInsets.navigationBars.asPaddingValues()
        .calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
    val hPad: Dp = maxOf(cutoutL, cutoutR, statusL, navR, 12.dp)

    val statusBarTopPad = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topInsetHeight: Dp = maxOf(statusBarTopPad, 24.dp)
    val bottomInsetHeight: Dp = maxOf(navBarBottomPad, 12.dp)

    val frontAreaPaddingTarget: Dp = if (!frontCameraAreaEnabled) {
        maxOf(statusL, cutoutL, 24.dp)
    } else 0.dp
    val animatedFrontAreaPadding by animateDpAsState(
        targetValue = frontAreaPaddingTarget,
        animationSpec = tween(durationMillis = ASPECT_ANIM_MS, easing = AspectEasing),
        label = "frontAreaPadding",
    )

    val topUiVisible = controlsVisible && !isLocked
    val bottomUiVisible = controlsVisible && !isLocked
    val rightUiVisible = controlsVisible || isLocked

    val topUiProgress by animateFloatAsState(
        targetValue = if (topUiVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = UI_TOGGLE_ANIM_MS,
            easing = AspectEasing,
        ),
        label = "topUiProgress",
    )
    val bottomUiProgress by animateFloatAsState(
        targetValue = if (bottomUiVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = UI_TOGGLE_ANIM_MS,
            easing = AspectEasing,
        ),
        label = "bottomUiProgress",
    )
    val rightUiProgress by animateFloatAsState(
        targetValue = if (rightUiVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = UI_TOGGLE_ANIM_MS,
            easing = AspectEasing,
        ),
        label = "rightUiProgress",
    )

    val topUiAlpha = (topUiProgress * 4f).coerceIn(0f, 1f)
    val topUiOffset = (1f - topUiProgress) * (-56f)
    val bottomUiAlpha = (bottomUiProgress * 4f).coerceIn(0f, 1f)
    val bottomUiOffset = (1f - bottomUiProgress) * 56f
    val rightUiAlpha = (rightUiProgress * 4f).coerceIn(0f, 1f)
    val rightUiOffset = (1f - rightUiProgress) * SIDE_UI_SLIDE_DISTANCE_DP

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (blurSupported && backdrop != null) {
                        Modifier.layerBackdrop(backdrop)
                    } else Modifier
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = animatedFrontAreaPadding)
                    // ============================================================
                    // ★ 点击手势
                    // ============================================================
                    .pointerInput(isLocked, tapReservedTopPx, quickUiToggle) {
                        detectTapGestures(
                            onTap = { _ ->
                                if (tapStartY < tapReservedTopPx) {
                                    return@detectTapGestures
                                }
                                if (wasLongPressInThisGesture) {
                                    wasLongPressInThisGesture = false
                                    return@detectTapGestures
                                }
                                if (showMoreMenu || showSpeedMenu) {
                                    tapToggleJob.value?.cancel()
                                    tapToggleJob.value = null
                                    lastTapTime = 0L
                                    showMoreMenu = false
                                    showSpeedMenu = false
                                    controlsVisible = false
                                    return@detectTapGestures
                                }
                                if (isLocked) {
                                    tapToggleJob.value?.cancel()
                                    tapToggleJob.value = null
                                    lastTapTime = 0L
                                    return@detectTapGestures
                                }
                                if (quickUiToggle) {
                                    tapToggleJob.value?.cancel()
                                    tapToggleJob.value = null
                                    lastTapTime = 0L
                                    controlsVisible = !controlsVisible
                                    if (controlsVisible) bump()
                                } else {
                                    val now = System.currentTimeMillis()
                                    val elapsed = now - lastTapTime
                                    if (elapsed in 1..QUICK_UI_TAP_DELAY_MS) {
                                        // ★ 双击：暂停 / 播放
                                        lastTapTime = 0L
                                        tapToggleJob.value?.cancel()
                                        tapToggleJob.value = null
                                        when {
                                            isEnded -> {
                                                exo.seekTo(0L); exo.play()
                                                isEnded = false; position = 0L
                                            }
                                            isPlaying -> exo.pause()
                                            else -> exo.play()
                                        }
                                        vibrateShort(ctx)
                                        bump()
                                    } else {
                                        // ★ 单击：等判定窗口后 toggle UI
                                        lastTapTime = now
                                        tapToggleJob.value?.cancel()
                                        tapToggleJob.value = scope.launch {
                                            delay(QUICK_UI_TAP_DELAY_MS)
                                            controlsVisible = !controlsVisible
                                            if (controlsVisible) bump()
                                            tapToggleJob.value = null
                                        }
                                    }
                                }
                            },
                            onPress = { offset ->
                                tapStartY = offset.y
                                wasLongPressInThisGesture = false

                                if (isLocked) {
                                    tryAwaitRelease()
                                    return@detectTapGestures
                                }
                                val longJob = scope.launch {
                                    delay(500)
                                    if (!isLongPressSpeed) {
                                        isLongPressSpeed = true
                                        wasLongPressInThisGesture = true
                                        controlsVisibleBeforeLongPress = controlsVisible
                                        exo.playbackParameters = PlaybackParameters(3.0f)
                                        vibrateShort(ctx)
                                        controlsVisible = false
                                    }
                                }
                                val released = tryAwaitRelease()
                                longJob.cancel()
                                if (released && isLongPressSpeed) {
                                    isLongPressSpeed = false
                                    exo.playbackParameters = PlaybackParameters(speed)
                                    controlsVisible = controlsVisibleBeforeLongPress
                                }
                            },
                        )
                    }
                    // ============================================================
                    // ★ 滑动手势（进度 / 亮度 / 音量）
                    // ============================================================
                    .pointerInput(isLocked, duration, topReservedPx, bottomReservedPx) {
                        if (isLocked) return@pointerInput
                        awaitPointerEventScope {
                            while (true) {
                                val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                                val down = downEvent.changes.firstOrNull { it.changedToDown() }
                                    ?: continue
                                val startX = down.position.x
                                val startY = down.position.y
                                val viewWidth = size.width.toFloat()
                                val viewHeight = size.height.toFloat()

                                if (startY < topReservedPx || startY > viewHeight - bottomReservedPx) {
                                    while (true) {
                                        val ev = awaitPointerEvent(PointerEventPass.Initial)
                                        if (ev.changes.none { it.pressed }) break
                                    }
                                    continue
                                }

                                var mode = 0
                                var seekStartPos = 0L
                                var seekTarget = 0L
                                var startBrightness = 0f
                                var startVolumeNorm = 0f
                                val threshold = 22f
                                var longPressDetected = false

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val dx = change.position.x - startX
                                    val dy = change.position.y - startY

                                    if (isLongPressSpeed) {
                                        longPressDetected = true
                                        continue
                                    }

                                    if (mode == 0) {
                                        if (abs(dx) > threshold || abs(dy) > threshold) {
                                            mode = when {
                                                abs(dx) >= abs(dy) * 1.2f -> 1
                                                startX < viewWidth * 0.4f -> 2
                                                startX > viewWidth * 0.6f -> 3
                                                else -> 0
                                            }
                                            when (mode) {
                                                1 -> {
                                                    if (duration > 0) {
                                                        isSwipeSeeking = true
                                                        seekStartPos = exo.currentPosition.coerceAtLeast(0L)
                                                        seekTarget = seekStartPos
                                                        swipeTarget = seekTarget
                                                        fetchPreviewFrame(seekTarget)
                                                    } else mode = 0
                                                }
                                                2 -> {
                                                    startBrightness = getCurrentBrightness()
                                                    brightnessOverlayValue = startBrightness
                                                    brightnessOverlayVisible = true
                                                }
                                                3 -> {
                                                    val maxV = audioManager
                                                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                                        .coerceAtLeast(1)
                                                    val curV = audioManager
                                                        .getStreamVolume(AudioManager.STREAM_MUSIC)
                                                    startVolumeNorm = curV.toFloat() / maxV
                                                    volumeOverlayValue = startVolumeNorm
                                                    volumeOverlayVisible = true
                                                }
                                            }
                                        }
                                    }

                                    if (mode != 0) change.consume()

                                    when (mode) {
                                        1 -> {
                                            if (duration > 0 && viewWidth > 0f) {
                                                // ★ 滑动范围按视频时长动态调整
                                                //   ≤20min  → 1 屏滑完
                                                //   ≤60min  → 2 屏
                                                //   ≤120min → 3 屏
                                                //   >120min → 4 屏
                                                val screensForSeek = when {
                                                    duration <= 20L * 60_000L -> 1f
                                                    duration <= 60L * 60_000L -> 2f
                                                    duration <= 120L * 60_000L -> 3f
                                                    else -> 4f
                                                }
                                                seekTarget = (seekStartPos +
                                                        (dx * (1f / screensForSeek) / viewWidth * duration).toLong())
                                                    .coerceIn(0L, duration)
                                                swipeTarget = seekTarget
                                                fetchPreviewFrame(seekTarget)
                                            }
                                        }
                                        2 -> {
                                            if (viewHeight > 0f) {
                                                val newB = (startBrightness - dy / viewHeight).coerceIn(0.01f, 1f)
                                                brightnessOverlayValue = newB
                                                applyBrightness(newB)
                                                brightnessOverlayVisible = true
                                            }
                                        }
                                        3 -> {
                                            if (viewHeight > 0f) {
                                                val newV = (startVolumeNorm - dy / viewHeight).coerceIn(0f, 1f)
                                                volumeOverlayValue = newV
                                                val maxV = audioManager
                                                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                                    .coerceAtLeast(1)
                                                val newVol = (newV * maxV).roundToInt().coerceIn(0, maxV)
                                                audioManager.setStreamVolume(
                                                    AudioManager.STREAM_MUSIC, newVol, 0
                                                )
                                                volumeOverlayVisible = true
                                            }
                                        }
                                    }
                                }

                                if (longPressDetected) {
                                    if (isSwipeSeeking) {
                                        isSwipeSeeking = false
                                        clearPreview()
                                    }
                                    brightnessOverlayVisible = false
                                    volumeOverlayVisible = false
                                    continue
                                }

                                if (isLongPressSpeed) {
                                    isLongPressSpeed = false
                                    exo.playbackParameters = PlaybackParameters(speed)
                                }
                                if (mode == 1 && isSwipeSeeking) {
                                    exo.seekTo(seekTarget)
                                    position = seekTarget
                                    isSwipeSeeking = false
                                    clearPreview()
                                    bump()
                                }
                                if (mode == 2) brightnessOverlayVisible = false
                                if (mode == 3) volumeOverlayVisible = false
                            }
                        }
                    },
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val containerAspectRatio: Float =
                        if (constraints.maxHeight > 0 && constraints.maxWidth > 0) {
                            constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat()
                        } else 16f / 9f

                    val wideVideo = videoAspectRatio > containerAspectRatio
                    val fillRatio = if (wideVideo) {
                        videoAspectRatio / containerAspectRatio
                    } else {
                        containerAspectRatio / videoAspectRatio
                    }

                    val (targetScaleX, targetScaleY) = when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> 1f to 1f
                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                            if (wideVideo) 1f to fillRatio else fillRatio to 1f
                        }
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> fillRatio to fillRatio
                        else -> 1f to 1f
                    }

                    val animatedScaleX by animateFloatAsState(
                        targetValue = targetScaleX,
                        animationSpec = tween(durationMillis = ASPECT_ANIM_MS, easing = AspectEasing),
                        label = "aspectScaleX",
                    )
                    val animatedScaleY by animateFloatAsState(
                        targetValue = targetScaleY,
                        animationSpec = tween(durationMillis = ASPECT_ANIM_MS, easing = AspectEasing),
                        label = "aspectScaleY",
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = animatedScaleX
                                scaleY = animatedScaleY
                            },
                    ) {
                        AndroidView(
                            factory = { c ->
                                val arfl = AspectRatioFrameLayout(c).apply {
                                    setBackgroundColor(android.graphics.Color.BLACK)
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    setAspectRatio(videoAspectRatio)
                                    layoutParams = FrameLayout.LayoutParams(-1, -1)
                                }
                                val tv = TextureView(c).apply {
                                    layoutParams = FrameLayout.LayoutParams(-1, -1)
                                }
                                arfl.addView(tv)
                                exo.setVideoTextureView(tv)
                                playerContainerRef.value = arfl
                                textureViewRef.value = tv
                                arfl
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        if (!isInPipMode) {

            // ★ 需求 2/3：滑动临时进度条的动画驱动（提升作用域，供滑动临时进度条 + 迷你进度条共用）
            //   - isSwipeSeeking 变 true → morphProgress 0→1（入场）
            //   - isSwipeSeeking 变 false → morphProgress 1→0（出场，还原动画）
            val morphProgress = remember { Animatable(0f) }
            LaunchedEffect(isSwipeSeeking, isLocked, controlsVisible) {
                val shouldShow = isSwipeSeeking && !isLocked && !controlsVisible
                if (shouldShow) {
                    morphProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 220, easing = AspectEasing),
                    )
                } else if (morphProgress.value > 0.01f) {
                    // ★ 需求 3：松手后播还原动画，滑回迷你进度条位置并淡出
                    morphProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 240, easing = AspectEasing),
                    )
                }
            }

            if (isLongPressSpeed) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 34.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            buildPlayerMaterialModifier(
                                materialStyle = playerMaterialStyle,
                                blurSupported = blurSupported,
                                backdrop = backdrop,
                                shape = RoundedCornerShape(20.dp),
                                advancedBlurRadius = CONTROL_BLUR_RADIUS,
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("加速中 3.0x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (isSwipeSeeking && !isLocked) {
                val currentBucket = previewBucket(swipeTarget)
                val previewMatches = previewBitmap != null &&
                        previewBitmapBucket >= 0 &&
                        abs(previewBitmapBucket - currentBucket) <= 1L
                val hasPreview = previewMatches

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = hasPreview,
                        enter = fadeIn(tween(160)) + expandVertically(
                            animationSpec = tween(220, easing = AspectEasing),
                            expandFrom = Alignment.Top,
                        ),
                        exit = fadeOut(tween(120)) + shrinkVertically(
                            animationSpec = tween(180, easing = AspectEasing),
                            shrinkTowards = Alignment.Top,
                        ),
                    ) {
                        previewBitmap?.let { bmp ->
                            Box(
                                Modifier
                                    .fillMaxWidth(0.22f)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.Unspecified,
                                )
                            }
                        }
                    }

                    if (hasPreview) Spacer(Modifier.height(5.dp))

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                buildPlayerMaterialModifier(
                                    materialStyle = playerMaterialStyle,
                                    blurSupported = blurSupported,
                                    backdrop = backdrop,
                                    shape = RoundedCornerShape(12.dp),
                                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            "${fmtTime(swipeTarget)} / ${fmtTime(duration)}",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // ★ 需求 2/3：滑动时的临时进度条
            //   - 未启用底部进度条：从底部 60dp 处淡入
            //   - 启用底部进度条：从迷你进度条位置（导航栏上方）上移 + 放大到 60dp 处
            //   入场 + 出场动画由 morphProgress 双向驱动
            run {
                val miniNavBarBottomPad = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
                val slideStartPad = if (bottomProgressBarEnabled) miniNavBarBottomPad else 60.dp
                val slideEndPad = 60.dp

                if (morphProgress.value > 0.01f || isSwipeSeeking) {
                    val currentBottomPad = slideStartPad +
                            (slideEndPad - slideStartPad) * morphProgress.value

                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = hPad, end = hPad, bottom = currentBottomPad)
                            .graphicsLayer { alpha = morphProgress.value },
                    ) {
                        PlayerProgressBar(
                            position = swipeTarget, duration = duration, buffered = buffered,
                            onSeekStart = {}, onSeek = {}, onSeekEnd = {},
                            interactive = false, instant = true, enabled = false,
                        )
                    }
                }
            }

            if (exo.playbackState == Player.STATE_BUFFERING) {
                Box(Modifier.align(Alignment.Center)) {
                    InfiniteProgressIndicator(color = Color.White, size = 40.dp, strokeWidth = 3.dp)
                }
            }

            Box(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().height(140.dp)
                    .graphicsLayer { alpha = topUiAlpha }
                    .background(Brush.verticalGradient(colors = listOf(
                        Color.Black.copy(alpha = 0.4f), Color.Transparent,
                    )))
            )

            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(160.dp)
                    .graphicsLayer { alpha = bottomUiAlpha }
                    .background(Brush.verticalGradient(colors = listOf(
                        Color.Transparent, Color.Black.copy(alpha = 0.4f),
                    )))
            )

            Box(
                Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .graphicsLayer { alpha = topUiAlpha }
                    .offset(y = topUiOffset.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(topInsetHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        TopStatusBar(timeText, network, battery, charging, hPad)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(start = hPad, end = hPad),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.Back, "返回", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(videoTitle, color = Color.White, fontSize = 16.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))

                        Box(
                            Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable { enterPipMode(); bump() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.ZoomOut,
                                contentDescription = "小窗播放",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))

                        Box(
                            Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable {
                                    val opening = !showMoreMenu
                                    showMoreMenu = opening
                                    showSpeedMenu = false
                                    if (opening) controlsVisible = false
                                    bump()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.More, "更多", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { alpha = rightUiAlpha }
                    .offset(x = rightUiOffset.dp)
                    .padding(end = hPad),
                contentAlignment = Alignment.CenterEnd,
            ) {
                val sideButtonSpacing = 12.dp
                val sideColumnHeight = CONTROL_BUTTON_SIZE * 2 + sideButtonSpacing

                Box(
                    modifier = Modifier.height(sideColumnHeight),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    AnimatedVisibility(
                        visible = !isLocked,
                        enter = fadeIn(tween(200)) + scaleIn(
                            animationSpec = tween(220, easing = AspectEasing),
                            initialScale = 0.6f,
                        ),
                        exit = fadeOut(tween(160)) + scaleOut(
                            animationSpec = tween(200, easing = AspectEasing),
                            targetScale = 0.6f,
                        ),
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        Box(
                            Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable(enabled = !isCapturing) { doCapture(); bump() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.ScreenCapture,
                                contentDescription = "截图",
                                tint = Color.White.copy(alpha = if (isCapturing) 0.5f else 1f),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    val lockButtonY by animateDpAsState(
                        targetValue = if (isLocked) {
                            (sideColumnHeight - CONTROL_BUTTON_SIZE) / 2
                        } else {
                            CONTROL_BUTTON_SIZE + sideButtonSpacing
                        },
                        animationSpec = tween(
                            durationMillis = 420,
                            easing = LockMorphEasing,
                        ),
                        label = "lockButtonY",
                    )

                    val lockMorph by animateFloatAsState(
                        targetValue = if (isLocked) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 340,
                            easing = LockMorphEasing,
                        ),
                        label = "lockIconMorph",
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = lockButtonY),
                    ) {
                        Box(
                            Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable {
                                    isLocked = !isLocked
                                    controlsVisible = true
                                    bump()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (lockMorph < 0.99f) {
                                Icon(
                                    imageVector = MiuixIcons.Lock,
                                    contentDescription = "锁定",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            alpha = 1f - lockMorph
                                            rotationZ = lockMorph * 90f
                                            val s = 1f - lockMorph * 0.4f
                                            scaleX = s
                                            scaleY = s
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        },
                                )
                            }
                            if (lockMorph > 0.01f) {
                                Icon(
                                    imageVector = MiuixIcons.Unlock,
                                    contentDescription = "解锁",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            alpha = lockMorph
                                            rotationZ = -90f + lockMorph * 90f
                                            val s = 0.6f + lockMorph * 0.4f
                                            scaleX = s
                                            scaleY = s
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        },
                                )
                            }
                        }
                    }
                }
            }

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(start = hPad, end = hPad, bottom = bottomInsetHeight),
            ) {
                val progressBarCallbacks = @Composable { barH: Dp ->
                    PlayerProgressBar(
                        position = if (isSwipeSeeking) swipeTarget else position,
                        duration = duration, buffered = buffered,
                        onSeekStart = {
                            isSeeking = true
                            isSwipeSeeking = true
                            swipeTarget = position
                            fetchPreviewFrame(position)
                            bump()
                        },
                        onSeek = { t ->
                            swipeTarget = t
                            fetchPreviewFrame(t)
                            bump()
                        },
                        onSeekEnd = {
                            exo.seekTo(swipeTarget)
                            position = swipeTarget
                            isSeeking = false
                            isSwipeSeeking = false
                            clearPreview()
                            bump()
                        },
                        instant = isSwipeSeeking || isSeeking,
                        enabled = bottomUiVisible && !isLongPressSpeed,
                        barHeight = barH,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (bottomProgressBarEnabled) {
                                    val shiftPx = (1f - bottomUiProgress) * 60.dp.toPx()
                                    translationY = shiftPx
                                    alpha = bottomUiAlpha
                                } else {
                                    alpha = bottomUiAlpha
                                }
                            }
                    ) {
                        progressBarCallbacks(28.dp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = bottomUiAlpha
                            translationY = bottomUiOffset.dp.toPx()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(CONTROL_BUTTON_SIZE).clip(CircleShape)
                            .then(
                                buildPlayerMaterialModifier(
                                    materialStyle = playerMaterialStyle,
                                    blurSupported = blurSupported,
                                    backdrop = backdrop,
                                    shape = CircleShape,
                                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                )
                            )
                            .clickable(enabled = bottomUiVisible) {
                                when {
                                    isEnded -> { exo.seekTo(0L); exo.play(); isEnded = false; position = 0L }
                                    isPlaying -> exo.pause()
                                    else -> exo.play()
                                }
                                bump()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color.White, modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${fmtTime(position)} / ${fmtTime(duration)}",
                        color = Color.White, fontSize = 13.sp, maxLines = 1,
                        modifier = Modifier.width(100.dp),
                    )
                    Spacer(Modifier.weight(1f))

                    // ★ 仅在 DOM 抓取到真实剧集时才显示按钮
                    val hasEpisodes = episodeListTrusted && (episodeList?.total ?: 0) > 1
                    if (hasEpisodes) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .height(CONTROL_BUTTON_SIZE)
                                .clip(RoundedCornerShape(CONTROL_BUTTON_SIZE / 2))
                                .then(
                                    buildPlayerMaterialModifier(
                                        materialStyle = playerMaterialStyle,
                                        blurSupported = blurSupported,
                                        backdrop = backdrop,
                                        shape = RoundedCornerShape(CONTROL_BUTTON_SIZE / 2),
                                        advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                    )
                                )
                                .clickable(enabled = bottomUiVisible) { showEpisodeSheet = true }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "选集",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Box(
                        Modifier.height(CONTROL_BUTTON_SIZE)
                            .clip(RoundedCornerShape(CONTROL_BUTTON_SIZE / 2))
                            .then(
                                buildPlayerMaterialModifier(
                                    materialStyle = playerMaterialStyle,
                                    blurSupported = blurSupported,
                                    backdrop = backdrop,
                                    shape = RoundedCornerShape(CONTROL_BUTTON_SIZE / 2),
                                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                )
                            )
                            .clickable(enabled = bottomUiVisible) {
                                val opening = !showSpeedMenu
                                showSpeedMenu = opening
                                showMoreMenu = false
                                if (opening) controlsVisible = false
                                bump()
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (speed == 1.0f) "1.0x" else "${speed}x", color = Color.White,
                            fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.size(CONTROL_BUTTON_SIZE)
                            .clip(CircleShape)
                            .then(
                                buildPlayerMaterialModifier(
                                    materialStyle = playerMaterialStyle,
                                    blurSupported = blurSupported,
                                    backdrop = backdrop,
                                    shape = CircleShape,
                                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                                )
                            )
                            .clickable(enabled = bottomUiVisible) {
                                userManuallyRotatedState.value = true
                                val act = ctx as? Activity
                                if (act != null) {
                                    val isCurrentlyPortrait =
                                        act.resources.configuration.orientation ==
                                                Configuration.ORIENTATION_PORTRAIT
                                    act.requestedOrientation = if (isCurrentlyPortrait) {
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    }
                                }
                                bump()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MiuixIcons.RotateLeft,
                            contentDescription = "旋转屏幕",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            if ((showMoreMenu || showSpeedMenu) && !isLocked) {
                Box(
                    Modifier.fillMaxSize()
                        .clickable(interactionSource = null, indication = null) {
                            showMoreMenu = false
                            showSpeedMenu = false
                        }
                )
            }

            AnimatedVisibility(
                visible = showMoreMenu && !isLocked,
                enter = fadeIn(tween(200)) + slideInHorizontally(tween(260), initialOffsetX = { it }),
                exit = fadeOut(tween(160)) + slideOutHorizontally(tween(220), targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            ) {
                MoreMenu(
                    resizeMode = resizeMode,
                    frontCameraAreaEnabled = frontCameraAreaEnabled,
                    isBookmarked = isCurrentVideoBookmarked,
                    backgroundPlayEnabled = backgroundPlayEnabled,
                    bottomProgressBarEnabled = bottomProgressBarEnabled,
                    quickUiToggle = quickUiToggle,
                    onResizeModeChange = { resizeMode = it },
                    onFrontCameraAreaChange = {
                        frontCameraAreaEnabled = it
                        prefs.edit().putBoolean("player_front_area", it).apply()
                    },
                    onBackgroundPlayChange = { toggleBackgroundPlay(it) },
                    onBottomProgressBarChange = { enabled ->
                        bottomProgressBarEnabled = enabled
                        prefs.edit().putBoolean("player_bottom_progress_bar", enabled).apply()
                    },
                    onQuickUiToggleChange = { enabled ->
                        quickUiToggle = enabled
                        prefs.edit().putBoolean("player_quick_ui_toggle", enabled).apply()
                        if (enabled) {
                            Toast.makeText(
                                ctx,
                                "开启后将屏蔽双击播放/暂停",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    onCopyLink = {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("url", videoUrl))
                        Toast.makeText(ctx, "链接已复制", Toast.LENGTH_SHORT).show()
                        showMoreMenu = false
                    },
                    onShare = { shareVideo(ctx, videoUrl, videoTitle); showMoreMenu = false },
                    onAddBookmark = {
                        addBookmarkToRoot(ctx, videoTitle, videoUrl)
                        isCurrentVideoBookmarked = true
                        Toast.makeText(ctx, "已添加到书签", Toast.LENGTH_SHORT).show()
                        showMoreMenu = false
                    },
                    onRemoveBookmark = {
                        removeVideoBookmark(ctx, videoUrl)
                        isCurrentVideoBookmarked = false
                        Toast.makeText(ctx, "已取消书签", Toast.LENGTH_SHORT).show()
                        showMoreMenu = false
                    },
                    onDownload = {
                        val safeName = videoTitle
                            .ifBlank { "video_${System.currentTimeMillis()}" }
                            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
                            .take(80)
                            .let { if (it.contains(".")) it else "$it.mp4" }
                        pendingDownloadName = safeName
                        showMoreMenu = false
                        showDownloadDialog = true
                    },
                    endPadding = hPad,
                )
            }

            AnimatedVisibility(
                visible = showSpeedMenu && !isLocked,
                enter = fadeIn(tween(200)) + slideInHorizontally(tween(260), initialOffsetX = { it }),
                exit = fadeOut(tween(160)) + slideOutHorizontally(tween(220), targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            ) {
                SpeedMenu(
                    currentSpeed = speed,
                    onSpeedSelected = { setSpeed(it); showSpeedMenu = false },
                    endPadding = hPad,
                )
            }

            AnimatedVisibility(
                visible = showDownloadDialog,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(200)),
            ) {
                Box(
                    Modifier.fillMaxSize()
                        .clickable(interactionSource = null, indication = null) {
                            showDownloadDialog = false
                        }
                )
            }
            AnimatedVisibility(
                visible = showDownloadDialog,
                enter = fadeIn(tween(180)) + slideInVertically(
                    animationSpec = tween(280, easing = DialogEasingIn),
                    initialOffsetY = { fullHeight -> fullHeight + 64 },
                ),
                exit = fadeOut(tween(160)) + slideOutVertically(
                    animationSpec = tween(220, easing = DialogEasingOut),
                    targetOffsetY = { fullHeight -> fullHeight + 64 },
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.width(360.dp)
                            .clip(RoundedCornerShape(DOWNLOAD_DIALOG_CORNER))
                            .then(
                                buildPlayerMaterialModifier(
                                    materialStyle = playerMaterialStyle,
                                    blurSupported = blurSupported,
                                    backdrop = backdrop,
                                    shape = RoundedCornerShape(DOWNLOAD_DIALOG_CORNER),
                                    advancedBlurRadius = PANEL_BLUR_RADIUS,
                                )
                            )
                            .clickable(interactionSource = null, indication = null) { }
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("下载视频", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("确定要下载该视频吗？", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(pendingDownloadName, color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = { showDownloadDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    color = Color.White.copy(alpha = 0.2f), contentColor = Color.White,
                                ),
                            ) { Text("取消") }
                            Button(
                                onClick = {
                                    内部工具Video(ctx, videoUrl, pendingDownloadName)
                                    showDownloadDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.primary, contentColor = Color.White,
                                ),
                            ) { Text("下载") }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = brightnessOverlayVisible && !isLocked,
                enter = fadeIn(tween(120)), exit = fadeOut(tween(220)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                BrightnessOverlay(
                    brightness = brightnessOverlayValue,
                    backdrop = backdrop, blurSupported = blurSupported,
                    materialStyle = playerMaterialStyle,
                )
            }

            AnimatedVisibility(
                visible = volumeOverlayVisible && !isLocked,
                enter = fadeIn(tween(120)), exit = fadeOut(tween(220)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                VolumeOverlay(
                    volume = volumeOverlayValue,
                    backdrop = backdrop, blurSupported = blurSupported,
                    materialStyle = playerMaterialStyle,
                )
            }

            if (isWebPageUrl) {
                HiddenEpisodeWebView(
                    context = ctx,
                    pageUrl = refererUrl,
                    onEpisodeListExtracted = { result ->
                        if (result.total > 1) {
                            episodeList = result
                            episodeListTrusted = true
                        }
                    },
                    onVideoUrlExtracted = { newUrl ->
                        if (newUrl.isNotBlank()) {
                            scope.launch {
                                switchMutex.withLock {
                                    if (newUrl == PlaybackSessionHolder.currentUrl) {
                                        return@withLock
                                    }
                                    try {
                                        val factory = mediaSourceFactoryRef.value
                                        val newMediaSource: MediaSource =
                                            factory.createMediaSource(buildMediaItem(newUrl))
                                        exo.setMediaSource(newMediaSource)
                                        exo.prepare()
                                        exo.playWhenReady = true
                                        PlaybackSessionHolder.currentUrl = newUrl
                                        PlaybackPositionHolder.savedUrl = newUrl
                                        PlaybackPositionHolder.positionMs = 0L
                                        val ep = episodeList
                                        if (ep != null) {
                                            val idx = ep.episodes.indexOfFirst {
                                                newUrl.contains("/${it.episodeNumber}/") ||
                                                        newUrl.contains("/${it.episodeNumber}.")
                                            }
                                            if (idx >= 0) {
                                                episodeList = ep.copy(currentIndex = idx)
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    },
                )
            }

            val epList = episodeList
            if (epList != null && epList.episodes.isNotEmpty()) {
                EpisodeSheet(
                    show = showEpisodeSheet,
                    episodes = epList.episodes,
                    currentIndex = epList.currentIndex,
                    onSelect = { ep ->
                        val targetUrl = when {
                            ep.url.startsWith("http://") || ep.url.startsWith("https://") -> ep.url
                            ep.url.startsWith("/") -> {
                                val origin = refererUrl.substringBefore("/vodplay/")
                                    .ifBlank { refererUrl.substringBefore("/play/") }
                                    .ifBlank {
                                        val s = refererUrl.substringBefore("://")
                                        val h = refererUrl.substringAfter("://").substringBefore("/")
                                        if (s.isNotBlank() && h.isNotBlank()) "$s://$h" else ""
                                    }
                                if (origin.isNotBlank()) "$origin${ep.url}" else ep.url
                            }
                            ep.url.startsWith("#") -> {
                                val base = refererUrl.substringBefore("#").trimEnd('/')
                                "$base${ep.url}"
                            }
                            else -> {
                                val base = refererUrl.substringBefore("#").trimEnd('/')
                                "$base/${ep.url}"
                            }
                        }
                        refererUrlState = targetUrl
                        showEpisodeSheet = false
                    },
                    onDismiss = { showEpisodeSheet = false },
                )
            }

            // ★ 需求 2/3：迷你进度条（仅启用底部进度条时）。
            //   - 滑动进度时隐藏，由滑动的临时进度条接管
            //   - 出场动画期间（morphProgress 从 1→0），迷你进度条渐显，形成过渡
            if (bottomProgressBarEnabled) {
                val miniNavBarBottomPad = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
                val miniAlphaRaw = (1f - bottomUiProgress).coerceIn(0f, 1f)
                val miniAlpha = if (isSwipeSeeking) {
                    0f
                } else {
                    // 出场动画期间 morphProgress 从 1→0，迷你进度条从 0→miniAlphaRaw 渐显
                    val morphFactor = if (morphProgress.value > 0.01f) {
                        1f - morphProgress.value
                    } else 1f
                    (miniAlphaRaw * morphFactor).coerceIn(0f, 1f)
                }
                if (miniAlpha > 0.01f) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(
                                start = hPad,
                                end = hPad,
                                bottom = miniNavBarBottomPad,
                            )
                            .height(MINI_PROGRESS_BAR_HEIGHT)
                            .graphicsLayer {
                                val shiftPx = (1f - miniAlpha) * (-20.dp.toPx())
                                translationY = shiftPx
                                alpha = miniAlpha
                            },
                    ) {
                        MiniProgressBar(
                            position = position,
                            duration = duration,
                        )
                    }
                }
            }
        } // end if (!isInPipMode)
    }
}

// ============================================================
// 底部迷你进度条
// ============================================================

@Composable
private fun MiniProgressBar(
    position: Long,
    duration: Long,
) {
    val primaryColor = MiuixTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val progress = if (duration > 0L) {
            (position.toFloat() / duration).coerceIn(0f, 1f)
        } else 0f
        val cr = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            size = size,
            cornerRadius = cr,
        )
        if (progress > 0f) {
            drawRoundRect(
                color = primaryColor,
                size = Size(size.width * progress, size.height),
                cornerRadius = cr,
            )
        }
    }
}

// ============================================================
// 亮度提示（太阳）
// ============================================================

@Composable
private fun BrightnessOverlay(
    brightness: Float, backdrop: LayerBackdrop?, blurSupported: Boolean,
    materialStyle: Int,
) {
    val v = brightness.coerceIn(0f, 1f)
    val sunColor = if (v >= 0.99f) Color(0xFFFFB74D) else Color.White

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(OVERLAY_CORNER))
            .then(
                buildPlayerMaterialModifier(
                    materialStyle = materialStyle,
                    blurSupported = blurSupported,
                    backdrop = backdrop,
                    shape = RoundedCornerShape(OVERLAY_CORNER),
                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val minDim = size.minDimension
            val coreRadius = minDim * 0.21f
            val gap = minDim * 0.05f
            val rayInner = coreRadius + gap
            val minRayLen = minDim * 0.10f
            val maxRayLen = minDim * 0.26f
            val rayLen = minRayLen + (maxRayLen - minRayLen) * v
            val rayWidth = minDim * 0.07f
            val rayCorner = rayWidth / 2f
            for (i in 0 until 8) {
                val angleDeg = i * 45f
                rotate(degrees = angleDeg, pivot = Offset(cx, cy)) {
                    drawRoundRect(
                        color = sunColor,
                        topLeft = Offset(cx + rayInner, cy - rayWidth / 2f),
                        size = Size(rayLen, rayWidth),
                        cornerRadius = CornerRadius(rayCorner, rayCorner),
                    )
                }
            }
            drawCircle(color = sunColor, radius = coreRadius, center = Offset(cx, cy))
        }
        Box(
            modifier = Modifier.width(60.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f)),
        ) {
            Box(Modifier.fillMaxWidth(v).fillMaxHeight().background(Color.White))
        }
    }
}

// ============================================================
// 音量提示
// ============================================================

@Composable
private fun VolumeOverlay(
    volume: Float, backdrop: LayerBackdrop?, blurSupported: Boolean,
    materialStyle: Int,
) {
    val v = volume.coerceIn(0f, 1f)
    val isFull = v >= 0.99f
    val tint = if (isFull) MiuixTheme.colorScheme.primary else Color.White

    val waveCount = when {
        v <= 0f -> 0
        v < 0.34f -> 1
        v < 0.67f -> 2
        else -> 3
    }

    val targetIconSize = (60f + 15f * v).dp
    val iconSize by animateDpAsState(
        targetValue = targetIconSize,
        animationSpec = tween(durationMillis = 120, easing = AspectEasing),
        label = "volumeIconSize",
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(OVERLAY_CORNER))
            .then(
                buildPlayerMaterialModifier(
                    materialStyle = materialStyle,
                    blurSupported = blurSupported,
                    backdrop = backdrop,
                    shape = RoundedCornerShape(OVERLAY_CORNER),
                    advancedBlurRadius = CONTROL_BLUR_RADIUS,
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(iconSize)) {
                val w = size.width
                val h = size.height
                val cy = h / 2f
                val speakerLeft = w * 0.06f
                val speakerTop = cy - h * 0.15f
                val speakerBottom = cy + h * 0.15f
                val fanLeft = w * 0.18f
                val fanTop = cy - h * 0.28f
                val fanBottom = cy + h * 0.28f
                val fanRight = w * 0.38f
                val waveStartX = fanRight + w * 0.01f
                val waveStroke = 2.2.dp.toPx()
                val waveWidthStep = w * 0.07f
                val maxWaveR = if (waveCount > 0) {
                    h * 0.12f + (waveCount - 1) * waveWidthStep
                } else 0f
                val totalWidth = (waveStartX + maxWaveR) - speakerLeft
                val offsetX = (w - totalWidth) / 2f - speakerLeft
                val speakerPath = Path().apply {
                    moveTo(speakerLeft + offsetX, speakerTop)
                    lineTo(fanLeft + offsetX, speakerTop)
                    lineTo(fanRight + offsetX, fanTop)
                    lineTo(fanRight + offsetX, fanBottom)
                    lineTo(fanLeft + offsetX, speakerBottom)
                    lineTo(speakerLeft + offsetX, speakerBottom)
                    close()
                }
                drawPath(speakerPath, tint)
                if (waveCount > 0) {
                    for (i in 0 until waveCount) {
                        val r = h * 0.12f + i * waveWidthStep
                        drawArc(
                            color = tint, startAngle = -50f, sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(waveStartX + offsetX - r, cy - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(width = waveStroke, cap = StrokeCap.Round),
                        )
                    }
                } else {
                    drawLine(
                        color = tint,
                        start = Offset(speakerLeft + offsetX - w * 0.02f, cy + h * 0.26f),
                        end = Offset(fanRight + offsetX + w * 0.08f, cy - h * 0.26f),
                        strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round,
                    )
                }
            }
        }
        val barColor = if (isFull) MiuixTheme.colorScheme.primary else Color.White
        Box(
            modifier = Modifier.width(60.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f)),
        ) {
            Box(Modifier.fillMaxWidth(v).fillMaxHeight().background(barColor))
        }
    }
}

// ============================================================
// 顶部状态栏
// ============================================================

@Composable
private fun TopStatusBar(time: String, network: String, battery: Int, charging: Boolean, hPad: Dp) {
    val low = battery < 20 && !charging
    val bc = if (low) Color(0xFFFF3B30) else Color.White
    Row(
        Modifier.fillMaxWidth().padding(start = hPad, end = hPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            time, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, modifier = Modifier.width(48.dp),
        )
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.width(110.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (network == "WLAN") {
                Icon(Icons.Filled.Wifi, "WLAN", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
            } else if (network != "离线") {
                Text(network, color = Color.White, fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1,
                    modifier = Modifier.width(32.dp))
                Spacer(Modifier.width(6.dp))
            }
            Spacer(Modifier.width(4.dp))
            BatteryIcon(battery, bc)
            Spacer(Modifier.width(3.dp))
            Text("$battery%", color = bc, fontSize = 12.sp,
                fontWeight = FontWeight.Medium, maxLines = 1,
                modifier = Modifier.width(36.dp))
        }
    }
}

@Composable
private fun BatteryIcon(level: Int, color: Color) {
    Canvas(Modifier.size(22.dp, 11.dp)) {
        val st = 1.dp.toPx(); val cr = 2.dp.toPx()
        val cw = 1.5.dp.toPx(); val ch = 4.dp.toPx()
        drawRoundRect(color, Offset(st/2f, st/2f), Size(size.width - cw - st, size.height - st),
            CornerRadius(cr, cr),
            style = androidx.compose.ui.graphics.drawscope.Stroke(st))
        drawRoundRect(color, Offset(size.width - cw, (size.height - ch)/2f),
            Size(cw, ch), CornerRadius(1.dp.toPx(), 1.dp.toPx()))
        val ip = st * 2f; val im = size.width - cw - st - ip * 2f
        val iw = im * (level.coerceIn(0, 100) / 100f)
        if (iw > 0f) drawRoundRect(color, Offset(ip, ip), Size(iw, size.height - ip * 2f),
            CornerRadius(1.dp.toPx(), 1.dp.toPx()))
    }
}

// ============================================================
// 侧边菜单背景
// ============================================================

@Composable
private fun GradientScrimPanel(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.15f to Color.Black.copy(alpha = 0.35f),
                        0.55f to Color.Black.copy(alpha = 0.72f),
                        1.0f to Color.Black.copy(alpha = 0.95f),
                    )
                )
            )
        )
        content()
    }
}

// ============================================================
// 更多菜单
// ============================================================

@Composable
private fun MoreMenu(
    resizeMode: Int, frontCameraAreaEnabled: Boolean, isBookmarked: Boolean,
    backgroundPlayEnabled: Boolean,
    bottomProgressBarEnabled: Boolean,
    quickUiToggle: Boolean,
    onResizeModeChange: (Int) -> Unit, onFrontCameraAreaChange: (Boolean) -> Unit,
    onBackgroundPlayChange: (Boolean) -> Unit,
    onBottomProgressBarChange: (Boolean) -> Unit,
    onQuickUiToggleChange: (Boolean) -> Unit,
    onCopyLink: () -> Unit, onShare: () -> Unit,
    onAddBookmark: () -> Unit, onRemoveBookmark: () -> Unit, onDownload: () -> Unit,
    endPadding: Dp,
) {
    val funcs = listOf(
        "复制链接" to MiuixIcons.Copy,
        "分享视频" to Icons.Rounded.Share,
        (if (isBookmarked) "取消书签" else "添加书签") to
                (if (isBookmarked) MiuixIcons.FavoritesFill else MiuixIcons.Favorites),
        "下载视频" to MiuixIcons.Download,
    )
    val actions: List<() -> Unit> = listOf(
        onCopyLink, onShare,
        if (isBookmarked) onRemoveBookmark else onAddBookmark,
        onDownload,
    )
    val itemPadding: Dp = maxOf(20.dp, endPadding)
    val panelContentWidth: Dp = 240.dp

    Box(Modifier.fillMaxHeight().width(panelContentWidth + itemPadding * 2)) {
        GradientScrimPanel {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(start = itemPadding, end = itemPadding, top = itemPadding, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    funcs.chunked(4).forEachIndexed { rowIdx, row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEachIndexed { colIdx, (label, icon) ->
                                val idx = rowIdx * 4 + colIdx
                                Column(
                                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .clickable { actions[idx]() }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(icon, label, tint = Color.White, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(10.dp))
                                    Text(label, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                Text("画面比例", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "适应" to AspectRatioFrameLayout.RESIZE_MODE_FIT,
                        "拉伸" to AspectRatioFrameLayout.RESIZE_MODE_FILL,
                        "填充" to AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                    ).forEach { (n, m) ->
                        val selected = resizeMode == m
                        Box(
                            Modifier.weight(1f).height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) MiuixTheme.colorScheme.primary
                                else Color.Black.copy(alpha = MENU_ITEM_BG_ALPHA))
                                .clickable { onResizeModeChange(m) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(n, color = Color.White, fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Text("其它设置", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = MENU_ITEM_BG_ALPHA)),
                ) {
                    CompactSwitchRow(
                        title = "前摄区域显示",
                        checked = frontCameraAreaEnabled,
                        onCheckedChange = onFrontCameraAreaChange,
                    )
                    CompactSwitchRow(
                        title = "后台继续播放",
                        checked = backgroundPlayEnabled,
                        onCheckedChange = onBackgroundPlayChange,
                    )
                    CompactSwitchRow(
                        title = "底部进度条",
                        checked = bottomProgressBarEnabled,
                        onCheckedChange = onBottomProgressBarChange,
                    )
                    CompactSwitchRow(
                        title = "快速唤出菜单",
                        checked = quickUiToggle,
                        onCheckedChange = onQuickUiToggleChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSwitchRow(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(46.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        CompactSwitch(checked)
    }
}

@Composable
private fun CompactSwitch(checked: Boolean) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 16.dp else 0.dp,
        animationSpec = tween(200), label = "thumbOffset",
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) MiuixTheme.colorScheme.primary
        else Color.White.copy(alpha = 0.3f),
        animationSpec = tween(200), label = "trackColor",
    )
    Box(
        Modifier.size(width = 36.dp, height = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(trackColor).padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier.offset(x = thumbOffset).size(16.dp)
                .clip(CircleShape).background(Color.White)
        )
    }
}

// ============================================================
// 倍速菜单
// ============================================================

@Composable
private fun SpeedMenu(
    currentSpeed: Float, onSpeedSelected: (Float) -> Unit, endPadding: Dp,
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f, 4.0f, 5.0f)
    val itemPadding: Dp = maxOf(20.dp, endPadding)
    val panelContentWidth: Dp = 130.dp
    Box(Modifier.fillMaxHeight().width(panelContentWidth + itemPadding * 2)) {
        GradientScrimPanel {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(start = itemPadding, end = itemPadding, top = itemPadding, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                speeds.forEach { s ->
                    val selected = currentSpeed == s
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MiuixTheme.colorScheme.primary
                            else Color.Black.copy(alpha = MENU_ITEM_BG_ALPHA))
                            .clickable { onSpeedSelected(s) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            if (s == 1.0f) "1.0x（默认）" else "${s}x",
                            color = Color.White, fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 进度条（含微调模式）
// ============================================================

@Composable
private fun PlayerProgressBar(
    position: Long, duration: Long, buffered: Long,
    onSeekStart: () -> Unit, onSeek: (Long) -> Unit, onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    instant: Boolean = false,
    enabled: Boolean = true,
    barHeight: Dp = 28.dp,
) {
    val currentPosition by rememberUpdatedState(position)

    var dragPos by remember { mutableLongStateOf(position) }
    var dragging by remember { mutableStateOf(false) }

    // ★ 微调模式
    var fineTuneMode by remember { mutableStateOf(false) }

    val disp = if (dragging) dragPos else position

    val pfRaw = if (duration > 0) (disp.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val bfRaw = if (duration > 0) (buffered.toFloat() / duration).coerceIn(0f, 1f) else 0f

    val pfAnim = remember { Animatable(pfRaw) }
    val bfAnim = remember { Animatable(bfRaw) }

    LaunchedEffect(pfRaw, dragging, instant) {
        if (dragging || instant) pfAnim.snapTo(pfRaw)
        else pfAnim.animateTo(pfRaw, tween(300))
    }
    LaunchedEffect(bfRaw, dragging, instant) {
        if (dragging || instant) bfAnim.snapTo(bfRaw)
        else bfAnim.animateTo(bfRaw, tween(300))
    }

    val pf = if (dragging || instant) pfRaw else pfAnim.value
    val bf = if (dragging || instant) bfRaw else bfAnim.value

    val den = LocalDensity.current

    // ★ 需求 2：拖动进度条变高 = 原 8dp × 2 = 16dp；精调 = 原 16dp × 2 = 32dp
    val trackHeightDp by animateDpAsState(
        targetValue = when {
            fineTuneMode -> 32.dp
            dragging -> 16.dp
            else -> 4.dp
        },
        animationSpec = tween(180, easing = AspectEasing),
        label = "trackHeight",
    )
    val trackHeightPx = with(den) { trackHeightDp.toPx() }

    val progressColor = MiuixTheme.colorScheme.primary

    // 微调模式下每 px 对应多少毫秒视频
    val fineTuneMsPerPx = with(den) {
        FINE_TUNE_MS_PER_DP / density
    }

    val fineTuneTriggerPx = with(den) { FINE_TUNE_TRIGGER_DP.dp.toPx() }
    // ★ 需求 2：向下滑回触发距离的 40% 时退出精调
    val fineTuneExitPx = fineTuneTriggerPx * FINE_TUNE_EXIT_RATIO

    Box(modifier.fillMaxWidth().height(barHeight)) {
        val canvasModifier = if (interactive && enabled) {
            Modifier
                .fillMaxSize()
                .pointerInput(duration, fineTuneMsPerPx, fineTuneTriggerPx, fineTuneExitPx) {
                    if (duration <= 0) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val startX = down.position.x
                            val startY = down.position.y
                            val viewWidth = size.width.toFloat()
                            if (viewWidth <= 0f) continue

                            // ★ 需求 2：用局部变量追踪锚点，方便退出精调时重新锚定
                            var localStartX = startX
                            var localSeekStartPos = currentPosition
                            var isDragActive = false
                            var movedEnough = false
                            val slop = 8f
                            var localFineTune = false
                            var localAnchorX = 0f
                            var localAnchorTime = 0L

                            while (true) {
                                val ev = awaitPointerEvent()
                                val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                                if (!ch.pressed) break

                                val dx = ch.position.x - startX
                                val dy = ch.position.y - startY
                                val localDx = ch.position.x - localStartX

                                if (!isDragActive && abs(dx) > slop) {
                                    isDragActive = true
                                    dragging = true
                                    dragPos = localSeekStartPos
                                    fineTuneMode = false
                                    onSeekStart()
                                }

                                if (isDragActive) {
                                    ch.consume()

                                    // ★ 向上滑 → 进入精调
                                    if (!localFineTune && dy < -fineTuneTriggerPx) {
                                        localFineTune = true
                                        fineTuneMode = true
                                        localAnchorX = ch.position.x
                                        localAnchorTime = dragPos
                                    }
                                    // ★ 需求 2：向下滑回进度条附近（触发距离的 40%）→ 退出精调
                                    else if (localFineTune && dy > -fineTuneExitPx) {
                                        localFineTune = false
                                        fineTuneMode = false
                                        // 重新锚定，避免退出后位置跳变
                                        localStartX = ch.position.x
                                        localSeekStartPos = dragPos
                                    }

                                    val target = if (localFineTune) {
                                        // 精调：按时间精调
                                        val fineDx = ch.position.x - localAnchorX
                                        localAnchorTime + (fineDx * fineTuneMsPerPx).toLong()
                                    } else {
                                        // 普通：全进度
                                        (localSeekStartPos + (localDx / viewWidth * duration).toLong())
                                    }
                                    dragPos = target.coerceIn(0L, duration)
                                    onSeek(dragPos)
                                    movedEnough = true
                                }
                            }

                            if (isDragActive) {
                                dragging = false
                                fineTuneMode = false
                                onSeekEnd()
                            } else if (!movedEnough) {
                                val tapRatio = (startX / viewWidth).coerceIn(0f, 1f)
                                val target = (tapRatio * duration).toLong()
                                onSeek(target)
                                onSeekEnd()
                            }
                        }
                    }
                }
        } else {
            Modifier.fillMaxSize()
        }

        Canvas(canvasModifier) {
            val w = size.width
            val h = size.height
            val cy = h / 2f
            val cr = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)

            drawRoundRect(
                Color.White.copy(alpha = 0.25f),
                Offset(0f, cy - trackHeightPx / 2f),
                Size(w, trackHeightPx), cr,
            )
            drawRoundRect(
                Color.White.copy(alpha = 0.45f),
                Offset(0f, cy - trackHeightPx / 2f),
                Size(w * bf, trackHeightPx), cr,
            )
            drawRoundRect(
                progressColor,
                Offset(0f, cy - trackHeightPx / 2f),
                Size(w * pf, trackHeightPx), cr,
            )
        }
    }
}