@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

import com.torachen.mote.ui.component.miuix.effect.BgEffectBackground

import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.87f


private const val APP_DISPLAY_NAME = "Mote"

private val LOGO_TOP_OFFSET = 92.dp
private val LOGO_BOTTOM_GAP = 126.dp
private val LOGO_FALLBACK_HEIGHT = 180.dp
private val TOP_BAR_FALLBACK_HEIGHT = 64.dp

private const val CARD_ALPHA = 0.5f

private val LOGO_SIZE = 100.dp

private val AUTO_CHECK_UPDATE_OPTIONS = listOf("每次启动", "每周启动", "关闭")
private const val AUTO_CHECK_UPDATE_DEFAULT = 0

private val DOWNLOAD_SOURCE_OPTIONS = listOf(
    "Gitee" to "国内访问更快更稳定",
    "Github" to "国际访问，适合海外用户",
)
private const val DOWNLOAD_SOURCE_DEFAULT = 0

private const val KEY_AUTO_CHECK_UPDATE = "about_auto_check_update"
private const val KEY_DOWNLOAD_SOURCE = "about_download_source"
private const val KEY_LAST_CHECK_TIME = "about_last_check_time"

private const val GITEE_REPO = "TorachenG/Mote"
private const val GITHUB_REPO = "TorachenG/Mote"
private const val APK_ASSET_NAME = "app-release.apk"
private const val GITEE_TOKEN = "6effe3228a3311f0784e19a1216a1229"

// ============================================================
// 更新状态
// ============================================================

internal sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class HasUpdate(
        val version: String,
        val url: String,
        val changelog: String,
        val sizeBytes: Long,
    ) : UpdateState
    data class Failed(val message: String) : UpdateState
}

internal object PendingUpdateStore {
    var pending by mutableStateOf<UpdateState.HasUpdate?>(null)
}

// ============================================================
// 页面状态 / 动作
// ============================================================

@Immutable
private data class AboutPageState(
    val appName: String,
    val versionName: String,
    val updateStateText: String,
    val autoCheckUpdateOptions: List<String>,
    val autoCheckUpdateIndex: Int,
    val downloadSourceOptions: List<Pair<String, String>>,
    val downloadSourceIndex: Int,
)

@Immutable
private data class AboutPageActions(
    val onBack: () -> Unit,
    val onCheckUpdate: () -> Unit,
    val onAutoCheckUpdateChange: (Int) -> Unit,
    val onDownloadSourceChange: (Int) -> Unit,
    val onOpenContact: () -> Unit,
    val onOpenWebsite: () -> Unit,
)

// ============================================================
// ★ 星星 Logo（Canvas 绘制，四角星 sparkle）
// ============================================================

@Composable
private fun MoteStarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(
                w * 0.5f, h * 0.30f,
                w * 0.70f, h * 0.5f,
                w, h * 0.5f
            )
            cubicTo(
                w * 0.70f, h * 0.5f,
                w * 0.5f, h * 0.70f,
                w * 0.5f, h
            )
            cubicTo(
                w * 0.5f, h * 0.70f,
                w * 0.30f, h * 0.5f,
                0f, h * 0.5f
            )
            cubicTo(
                w * 0.30f, h * 0.5f,
                w * 0.5f, h * 0.30f,
                w * 0.5f, 0f
            )
            close()
        }

        val brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFB5A7),
                Color(0xFFC5A0DD),
                Color(0xFF3B72FF),
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(w, h),
        )

        drawPath(path = path, brush = brush)
    }
}

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsAboutPage(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val currentVersionRaw = remember {
        readCurrentVersionNameFromContext(context)
    }

    var autoCheckUpdateIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_AUTO_CHECK_UPDATE, AUTO_CHECK_UPDATE_DEFAULT)
                .coerceIn(0, AUTO_CHECK_UPDATE_OPTIONS.lastIndex)
        )
    }
    var downloadSourceIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_DOWNLOAD_SOURCE, DOWNLOAD_SOURCE_DEFAULT)
                .coerceIn(0, DOWNLOAD_SOURCE_OPTIONS.lastIndex)
        )
    }

    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var showContactSheet by remember { mutableStateOf(false) }

    fun openInMote(url: String) {
        try {
            onOpenUrl(url)
        } catch (_: Exception) {
        }
    }

    fun performCheck() {
        if (updateState is UpdateState.Checking) return
        updateState = UpdateState.Checking
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                fetchLatestRelease(downloadSourceIndex)
            }
            updateState = result
            if (result is UpdateState.HasUpdate) {
                showUpdateDialog = true
            } else if (result is UpdateState.Failed) {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            } else if (result is UpdateState.UpToDate) {
                Toast.makeText(context, "已经是最新版本", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val updateStateText = when (val s = updateState) {
        UpdateState.Idle -> "检查版本"
        UpdateState.Checking -> "正在检查中"
        UpdateState.UpToDate -> "已经是最新版本"
        is UpdateState.HasUpdate -> "发现新版本"
        is UpdateState.Failed -> "检查失败"
    }

    val state = AboutPageState(
        appName = APP_DISPLAY_NAME,
        versionName = currentVersionRaw,
        updateStateText = updateStateText,
        autoCheckUpdateOptions = AUTO_CHECK_UPDATE_OPTIONS,
        autoCheckUpdateIndex = autoCheckUpdateIndex,
        downloadSourceOptions = DOWNLOAD_SOURCE_OPTIONS,
        downloadSourceIndex = downloadSourceIndex,
    )

    val actions = AboutPageActions(
        onBack = onBack,
        onCheckUpdate = { performCheck() },
        onAutoCheckUpdateChange = { index ->
            autoCheckUpdateIndex = index
            prefs.edit().putInt(KEY_AUTO_CHECK_UPDATE, index).apply()
        },
        onDownloadSourceChange = { index ->
            downloadSourceIndex = index
            prefs.edit().putInt(KEY_DOWNLOAD_SOURCE, index).apply()
            updateState = UpdateState.Idle
        },
        onOpenContact = { showContactSheet = true },
        onOpenWebsite = { openInMote("https://toracheng.github.io/Mote") },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsAboutMiuix(state, actions)
        UiMode.Material -> SettingsAboutMaterial(state, actions)
    }

    // ★ UpdateDialog 里的 WindowBottomSheet 需要 LocalNavigationEventDispatcherOwner
    val dialogNavOwner = rememberNavigationEventDispatcherOwner()
    val updateInfo = updateState as? UpdateState.HasUpdate
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides dialogNavOwner
    ) {
        ContactBottomSheet(
            show = showContactSheet,
            onDismiss = { showContactSheet = false },
            onOpenGithub = {
                showContactSheet = false
                openInMote("https://github.com/TorachenG/Mote/issues")
            },
            onOpenGitee = {
                showContactSheet = false
                openInMote("https://gitee.com/TorachenG/Mote/issues")
            },
            onOpenWebsite = {
                showContactSheet = false
                openInMote("https://toracheng.github.io/Mote")
            },
        )

        UpdateDialog(
            show = showUpdateDialog && updateInfo != null,
            info = updateInfo,
            currentVersion = currentVersionRaw,
            isDownloading = isDownloading,
            onDismiss = { showUpdateDialog = false },
            onDownload = {
                if (updateInfo != null && !isDownloading) {
                    isDownloading = true
                    showUpdateDialog = false
                    startApkDownload(
                        context = context,
                        downloadUrl = updateInfo.url,
                        onComplete = { isDownloading = false },
                        onFailed = { msg ->
                            isDownloading = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                    )
                }
            },
        )
    }
}

// ============================================================
// MIUIX 版 —— BgEffectBackground 动态流光 + 自绘四角星
// ============================================================

@Composable
private fun SettingsAboutMiuix(
    state: AboutPageState,
    actions: AboutPageActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()

    val contentBackdrop = rememberLayerBackdrop()

    val barBlurBackdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true))
    }
    var materialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val lazyListState = rememberLazyListState()
    var logoHeightPx by remember { mutableIntStateOf(0) }

    val scrollProgress by remember {
        derivedStateOf {
            if (logoHeightPx <= 0) 0f
            else {
                val index = lazyListState.firstVisibleItemIndex
                val offset = lazyListState.firstVisibleItemScrollOffset
                if (index > 0) 1f else (offset.toFloat() / logoHeightPx).coerceIn(0f, 1f)
            }
        }
    }

    val isAtTop = scrollProgress == 0f
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(400, easing = androidx.compose.animation.core.CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "settingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, barBlurBackdrop, themeIsDark)
    } else {
        Modifier
    }

    val blurActive = barBlurBackdrop != null && scrollProgress == 1f
    val barColor = if (blurActive) {
        Color.Transparent
    } else {
        if (scrollProgress == 1f) surfaceColor else Color.Transparent
    }

    val effectiveLogoHeight = with(density) {
        if (logoHeightPx > 0) logoHeightPx.toDp() else LOGO_FALLBACK_HEIGHT
    }

    val logoBlend = remember(themeIsDark) {
        if (themeIsDark) {
            listOf(
                BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab),
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab),
            )
        }
    }

    var cardInitialY by remember { mutableFloatStateOf(0f) }
    var cardCurrentY by remember { mutableFloatStateOf(0f) }

    val cardProgress by remember {
        derivedStateOf {
            val topBarY = topBarBottomPx.toFloat()
            if (cardInitialY <= 0f || topBarY <= 0f) 0f
            else {
                val range = cardInitialY - topBarY
                if (range <= 0f) 0f
                else ((cardInitialY - cardCurrentY) / range).coerceIn(0f, 1f)
            }
        }
    }

    val darkT = cardProgress
    val darkAlphaProgress = darkT * darkT * darkT * darkT
    val darkCardColor = lerp(
        Color.White,
        MiuixTheme.colorScheme.surfaceContainer,
        darkT,
    ).copy(alpha = 0.1f + 0.9f * darkAlphaProgress)

    val cardColor = if (surfaceColor.luminance() >= 0.5f) {
        lerp(
            MiuixTheme.colorScheme.surfaceContainer.copy(alpha = CARD_ALPHA),
            MiuixTheme.colorScheme.surfaceContainer,
            cardProgress,
        )
    } else {
        darkCardColor
    }

    var logoAreaY by remember { mutableFloatStateOf(0f) }
    var iconY by remember { mutableFloatStateOf(0f) }
    var appNameY by remember { mutableFloatStateOf(0f) }
    var versionY by remember { mutableFloatStateOf(0f) }
    var initialLogoAreaY by remember { mutableFloatStateOf(0f) }

    var iconProgress by remember { mutableFloatStateOf(0f) }
    var appNameProgress by remember { mutableFloatStateOf(0f) }
    var versionProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(lazyListState) {
        try {
            snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
                .collect { offset ->
                    if (lazyListState.firstVisibleItemIndex > 0) {
                        if (iconProgress != 1f) iconProgress = 1f
                        if (appNameProgress != 1f) appNameProgress = 1f
                        if (versionProgress != 1f) versionProgress = 1f
                        return@collect
                    }

                    if (initialLogoAreaY == 0f && logoAreaY > 0f) {
                        initialLogoAreaY = logoAreaY
                    }
                    val refLogoAreaY = if (initialLogoAreaY > 0f) initialLogoAreaY else logoAreaY

                    val stage1TotalLength = refLogoAreaY - versionY
                    val stage2TotalLength = versionY - appNameY
                    val stage3TotalLength = appNameY - iconY

                    val versionDelay = stage1TotalLength * 0.5f
                    versionProgress = ((offset.toFloat() - versionDelay) /
                            (stage1TotalLength - versionDelay).coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                    appNameProgress = ((offset.toFloat() - stage1TotalLength) /
                            stage2TotalLength.coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                    iconProgress = ((offset.toFloat() - stage1TotalLength - stage2TotalLength) /
                            stage3TotalLength.coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                }
        } catch (_: Exception) {}
    }

    val autoCheckUpdateEntries = remember(
        state.autoCheckUpdateOptions, state.autoCheckUpdateIndex
    ) {
        listOf(
            DropdownEntry(
                items = state.autoCheckUpdateOptions.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.autoCheckUpdateIndex == index,
                        onClick = { actions.onAutoCheckUpdateChange(index) },
                    )
                },
            ),
        )
    }

    val downloadSourceEntries = remember(
        state.downloadSourceOptions, state.downloadSourceIndex
    ) {
        listOf(
            DropdownEntry(
                items = state.downloadSourceOptions.mapIndexed { index, (label, summary) ->
                    DropdownItem(
                        text = label,
                        summary = summary,
                        selected = state.downloadSourceIndex == index,
                        onClick = { actions.onDownloadSourceChange(index) },
                    )
                },
            ),
        )
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (barBlurBackdrop != null) {
                            Modifier.layerBackdrop(barBlurBackdrop)
                        } else Modifier
                    )
            ) {
                BgEffectBackground(
                    dynamicBackground = true,
                    modifier = Modifier.fillMaxSize(),
                    bgModifier = Modifier.layerBackdrop(contentBackdrop),
                    isFullSize = true,
                    effectBackground = true,
                    alpha = { 1f - scrollProgress },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = contentTopPadding + LOGO_TOP_OFFSET)
                            .onGloballyPositioned { coords ->
                                if (coords.size.height != logoHeightPx) {
                                    logoHeightPx = coords.size.height
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(LOGO_SIZE)
                                .graphicsLayer {
                                    alpha = 1f - iconProgress
                                    scaleX = 1f - (iconProgress * 0.05f)
                                    scaleY = 1f - (iconProgress * 0.05f)
                                }
                                .onGloballyPositioned { coordinates ->
                                    if (iconY != 0f) return@onGloballyPositioned
                                    try {
                                        iconY = coordinates.positionInWindow().y +
                                                coordinates.size.height
                                    } catch (_: Exception) {}
                                },
                        ) {
                            MoteStarCanvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (blurSupported) {
                                            Modifier.textureBlur(
                                                backdrop = contentBackdrop,
                                                shape = RoundedCornerShape(0.dp),
                                                blurRadius = 150f,
                                                colors = BlurColors(blendColors = logoBlend),
                                                contentBlendMode = ComposeBlendMode.DstIn,
                                                enabled = true,
                                            )
                                        } else Modifier
                                    ),
                            )
                        }

                        val appNameStrokePx = with(density) { 0.6.dp.toPx() }

                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 5.dp)
                                .onGloballyPositioned { coordinates ->
                                    if (appNameY != 0f) return@onGloballyPositioned
                                    try {
                                        appNameY = coordinates.positionInWindow().y +
                                                coordinates.size.height
                                    } catch (_: Exception) {}
                                }
                                .graphicsLayer {
                                    alpha = 1f - appNameProgress
                                    scaleX = 1f - (appNameProgress * 0.05f)
                                    scaleY = 1f - (appNameProgress * 0.05f)
                                }
                                .then(
                                    if (blurSupported) {
                                        Modifier.textureBlur(
                                            backdrop = contentBackdrop,
                                            shape = RoundedCornerShape(0.dp),
                                            blurRadius = 150f,
                                            colors = BlurColors(blendColors = logoBlend),
                                            contentBlendMode = ComposeBlendMode.DstIn,
                                            enabled = true,
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = state.appName,
                                color = MiuixTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 35.sp,
                                style = TextStyle(
                                    drawStyle = Stroke(
                                        width = appNameStrokePx,
                                        join = StrokeJoin.Round,
                                        cap = StrokeCap.Round,
                                    ),
                                ),
                            )
                            Text(
                                text = state.appName,
                                color = MiuixTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 35.sp,
                            )
                        }

                        Text(
                            text = state.versionName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = 1f - versionProgress
                                    scaleX = 1f - (versionProgress * 0.05f)
                                    scaleY = 1f - (versionProgress * 0.05f)
                                }
                                .onGloballyPositioned { coordinates ->
                                    if (versionY != 0f) return@onGloballyPositioned
                                    try {
                                        versionY = coordinates.positionInWindow().y +
                                                coordinates.size.height
                                    } catch (_: Exception) {}
                                },
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .scrollEndHaptic()
                            .overScrollVertical(),
                        contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                        overscrollEffect = null,
                    ) {
                        item(key = "logoSpacer") {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(LOGO_TOP_OFFSET + effectiveLogoHeight + LOGO_BOTTOM_GAP)
                                    .onGloballyPositioned { coordinates ->
                                        try {
                                            logoAreaY = coordinates.positionInWindow().y +
                                                    coordinates.size.height
                                        } catch (_: Exception) {}
                                    }
                            )
                        }

                        item(key = "cards") {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .padding(bottom = 12.dp)
                                    .onGloballyPositioned { coords ->
                                        try {
                                            val y = coords.positionInWindow().y
                                            if (y > 0f) {
                                                if (cardInitialY == 0f) cardInitialY = y
                                                cardCurrentY = y
                                            }
                                        } catch (_: Exception) {}
                                    },
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.defaultColors(
                                        color = cardColor,
                                        contentColor = MiuixTheme.colorScheme.onSurface,
                                    ),
                                ) {
                                    SearchableItem(
                                        title = "检查更新",
                                        pageRoute = "settings_about",
                                        pageTitle = "关于",
                                        itemKey = "settings_about_check_update",
                                    ) {
                                        ArrowPreference(
                                            title = "检查更新",
                                            endActions = {
                                                Text(
                                                    text = state.updateStateText,
                                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                                )
                                            },
                                            onClick = { actions.onCheckUpdate() },
                                        )
                                    }
                                    SearchableItem(
                                        title = "自动检查更新",
                                        pageRoute = "settings_about",
                                        pageTitle = "关于",
                                        itemKey = "settings_about_auto_check_update",
                                    ) {
                                        WindowDropdownPreference(
                                            title = "自动检查更新",
                                            entries = autoCheckUpdateEntries,
                                            collapseOnSelection = true,
                                        )
                                    }
                                    SearchableItem(
                                        title = "更新来源",
                                        pageRoute = "settings_about",
                                        pageTitle = "关于",
                                        itemKey = "settings_about_download_source",
                                    ) {
                                        WindowSpinnerPreference(
                                            title = "更新来源",
                                            entries = downloadSourceEntries,
                                            collapseOnSelection = true,
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.defaultColors(
                                        color = cardColor,
                                        contentColor = MiuixTheme.colorScheme.onSurface,
                                    ),
                                ) {
                                    ArrowPreference(
                                        title = "联系与反馈",
                                        onClick = { actions.onOpenContact() },
                                    )
                                    ArrowPreference(
                                        title = "官方网站",
                                        onClick = { actions.onOpenWebsite() },
                                    )
                                }

                                Spacer(Modifier.weight(1f))

                                Spacer(
                                    Modifier.height(
                                        WindowInsets.navigationBars.asPaddingValues()
                                            .calculateBottomPadding() +
                                                WindowInsets.captionBar.asPaddingValues()
                                                    .calculateBottomPadding()
                                    )
                                )
                            }
                        }
                    }
                }

                SettingsPageAutoScroll(
                    lazyListState = lazyListState,
                    pageRoute = "settings_about",
                    contentTopPadding = contentTopPadding,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurActive && barBlurBackdrop != null) {
                            if (progressiveTitleBlur) {
                                // ★ 修复：开启渐进模糊时，使用 progressiveTextureBlur
                                Modifier.progressiveTextureBlur(
                                    backdrop = barBlurBackdrop,
                                    shape = RectangleShape,
                                    blurRadius = 10f,
                                    gradient = ProgressiveBlur.Top.copy(
                                        endFraction = 1.35f,
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = 0.3f)
                                            ),
                                        ),
                                    ),
                                )
                            } else {
                                // ★ 关闭时保持原来的普通模糊
                                Modifier.textureBlur(
                                    backdrop = barBlurBackdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)
                                            ),
                                        ),
                                    ),
                                )
                            }
                        } else Modifier
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    }
            ) {
                SmallTopAppBar(
                    title = "关于",
                    scrollBehavior = scrollBehavior,
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(
                        alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f),
                    ),
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = actions.onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                )
            }
        }
    }
}

// ============================================================
// Material 3 版 —— 页面背景 lerp(surface, White, 0.5f)
// ============================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsAboutMaterial(
    state: AboutPageState,
    actions: AboutPageActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

    // ★ 页面背景：直接跟 M3 主题 surface（跟新标签页保持一致）
    val pageBg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("关于") },
                    navigationIcon = {
                        M3IconButton(onClick = actions.onBack) {
                            M3Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                            )
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(
                        containerColor = pageBg,
                    ),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "about_logo") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(top = 24.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            MoteStarCanvas(modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.height(12.dp))
                        M3Text(
                            text = state.appName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        M3Text(
                            text = state.versionName,
                            fontSize = 14.sp,
                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item(key = "about_cards") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(M3_SEGMENT_GROUP_GAP),
                    ) {
                        // ★ 用 MoteSegmentedColumn 包裹 —— 圆角才会被注入
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentItem(
                                        headline = { M3Text("检查更新") },
                                        supporting = {
                                            M3Text(
                                                text = state.updateStateText,
                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = M3MaterialTheme.typography.bodyMedium,
                                            )
                                        },
                                        onClick = { actions.onCheckUpdate() },
                                    )
                                },
                                {
                                    M3SegmentDropdownItem(
                                        title = "自动检查更新",
                                        summary = null,
                                        options = state.autoCheckUpdateOptions,
                                        selectedIndex = state.autoCheckUpdateIndex,
                                        onSelected = actions.onAutoCheckUpdateChange,
                                    )
                                },
                                {
                                    M3SegmentDropdownItem(
                                        title = "更新来源",
                                        summary = null,
                                        options = state.downloadSourceOptions.map { it.first },
                                        selectedIndex = state.downloadSourceIndex,
                                        onSelected = actions.onDownloadSourceChange,
                                    )
                                },
                            )
                        )

                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentItem(
                                        headline = { M3Text("联系与反馈") },
                                        onClick = { actions.onOpenContact() },
                                    )
                                },
                                {
                                    M3SegmentItem(
                                        headline = { M3Text("官方网站") },
                                        onClick = { actions.onOpenWebsite() },
                                    )
                                },
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 更新弹窗
// ============================================================

@Composable
private fun ContactBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onOpenGithub: () -> Unit,
    onOpenGitee: () -> Unit,
    onOpenWebsite: () -> Unit,
) {
    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "联系与反馈",
        backgroundColor = MiuixTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "GitHub Issues",
                        summary = "提交问题、提出建议",
                        onClick = onOpenGithub,
                    )
                    ArrowPreference(
                        title = "Gitee Issues",
                        summary = "国内访问更稳定",
                        onClick = onOpenGitee,
                    )
                }
            }

            top.yukonga.miuix.kmp.basic.Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("关闭") }
        }
    }
}

@Composable
internal fun UpdateDialog(
    show: Boolean,
    info: UpdateState.HasUpdate?,
    currentVersion: String,
    isDownloading: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
) {
    val sheetTitle = if (info != null) {
        "发现新版本：$currentVersion → ${info.version}"
    } else "发现新版本"

    WindowBottomSheet(
        show = show,
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = sheetTitle,
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = !isDownloading,
    ) {
        if (info != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 260))
                    .padding(bottom = 24.dp),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        if (info.changelog.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 380.dp)
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                MarkdownText(
                                    text = info.changelog,
                                    baseColor = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { if (!isDownloading) onDismiss() },
                        modifier = Modifier.weight(1f),
                        enabled = !isDownloading,
                    ) { Text("暂时忽略") }
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        enabled = !isDownloading,
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text(if (isDownloading) "下载中…" else "立即更新") }
                }
            }
        }
    }
}

// ============================================================
// Markdown 渲染
// ============================================================

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Bullet(val text: String) : MdBlock
    data class Ordered(val number: Int, val text: String) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
}

@Composable
internal fun MarkdownText(
    text: String,
    baseColor: Color,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 22.sp
                        2 -> 19.sp
                        3 -> 17.sp
                        else -> 15.sp
                    }
                    Text(
                        text = buildInlineMarkdown(block.text),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = baseColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
                is MdBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text("•", color = MiuixTheme.colorScheme.primary, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = buildInlineMarkdown(block.text),
                            color = baseColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Ordered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            "${block.number}.",
                            color = MiuixTheme.colorScheme.primary,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = buildInlineMarkdown(block.text),
                            color = baseColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .heightIn(min = 20.dp)
                                .background(
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(2.dp),
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = buildInlineMarkdown(block.text),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Code -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MiuixTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = block.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = baseColor,
                        )
                    }
                }
                is MdBlock.Paragraph -> {
                    if (block.text.isBlank()) {
                        Spacer(Modifier.height(4.dp))
                    } else {
                        Text(
                            text = buildInlineMarkdown(block.text),
                            color = baseColor,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun parseMarkdownBlocks(text: String): List<MdBlock> {
    val result = mutableListOf<MdBlock>()
    val lines = text.lines()
    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val trimmed = raw.trimStart()

        if (trimmed.startsWith("```")) {
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i]); i++
            }
            if (i < lines.size) i++
            result.add(MdBlock.Code(codeLines.joinToString("\n")))
            continue
        }

        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            if (level in 1..6 && trimmed.getOrNull(level) == ' ') {
                result.add(MdBlock.Heading(level, trimmed.substring(level + 1).trimEnd()))
                i++; continue
            }
        }

        if (trimmed.startsWith("> ")) {
            result.add(MdBlock.Quote(trimmed.substring(2).trimEnd()))
            i++; continue
        }

        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            result.add(MdBlock.Bullet(trimmed.substring(2).trimEnd()))
            i++; continue
        }

        val ordered = Regex("^(\\d+)\\.\\s+(.*)$").find(trimmed)
        if (ordered != null) {
            val num = ordered.groupValues[1].toIntOrNull() ?: 1
            result.add(MdBlock.Ordered(num, ordered.groupValues[2].trimEnd()))
            i++; continue
        }

        if (trimmed.isBlank()) {
            result.add(MdBlock.Paragraph(""))
            i++; continue
        }

        result.add(MdBlock.Paragraph(raw.trimEnd()))
        i++
    }
    return result
}

private fun buildInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end > i) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else { append(text[i]); i++ }
                }
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end > i) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x1F000000),
                            )
                        ) { append(text.substring(i + 1, end)) }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                text[i] == '[' -> {
                    val cb = text.indexOf(']', i + 1)
                    val op = if (cb > i) text.indexOf('(', cb) else -1
                    val cp = if (op > 0) text.indexOf(')', op) else -1
                    if (cb > i && op == cb + 1 && cp > op) {
                        withStyle(SpanStyle(color = Color(0xFF3B82F6))) {
                            append(text.substring(i + 1, cb))
                        }
                        i = cp + 1
                    } else { append(text[i]); i++ }
                }
                (text[i] == '*' || text[i] == '_') -> {
                    val marker = text[i]
                    val end = text.indexOf(marker, i + 1)
                    if (end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                else -> { append(text[i]); i++ }
            }
        }
    }
}

// ============================================================
// 网络请求
// ============================================================

internal fun fetchLatestRelease(sourceIndex: Int): UpdateState {
    val apiUrl = when (sourceIndex) {
        0 -> "https://gitee.com/api/v5/repos/$GITEE_REPO/releases/latest" +
                "?access_token=$GITEE_TOKEN"
        1 -> "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
        else -> return UpdateState.Failed("未知下载来源")
    }

    return try {
        val conn = URL(apiUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        )
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Referer", "https://gitee.com/")

        try {
            if (conn.responseCode != 200) {
                return UpdateState.Failed("请求失败：HTTP ${conn.responseCode}")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)

            val tagName = json.optString("tag_name", "").removePrefix("v")
            val changelog = json.optString("body", "")
            val assets = json.optJSONArray("assets")

            if (tagName.isBlank()) return UpdateState.Failed("未找到版本信息")
            if (assets == null) return UpdateState.Failed("未找到发布附件")

            var downloadUrl: String? = null
            var sizeBytes: Long = 0L
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name", "")
                if (name.equals(APK_ASSET_NAME, ignoreCase = true)) {
                    downloadUrl = asset.optString("browser_download_url", "")
                    sizeBytes = asset.optLong("size", 0L)
                    break
                }
            }

            if (downloadUrl.isNullOrBlank()) return UpdateState.Failed("未找到 APK 下载链接")

            val currentVersion = readCurrentVersionName()
            if (isNewerVersion(tagName, currentVersion)) {
                UpdateState.HasUpdate(
                    version = tagName, url = downloadUrl,
                    changelog = changelog, sizeBytes = sizeBytes,
                )
            } else UpdateState.UpToDate
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        UpdateState.Failed("网络错误：${e.message ?: "未知"}")
    }
}

private fun isNewerVersion(remote: String, local: String): Boolean {
    val r = remote.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val l = local.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val maxLen = maxOf(r.size, l.size)
    for (i in 0 until maxLen) {
        val rv = r.getOrElse(i) { 0 }
        val lv = l.getOrElse(i) { 0 }
        if (rv > lv) return true
        if (rv < lv) return false
    }
    return false
}

internal fun readCurrentVersionNameFromContext(context: Context): String {
    return try {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        pkgInfo.versionName ?: "?"
    } catch (_: Exception) { "?" }
}

private fun readCurrentVersionName(): String {
    val context = MoteAppContextHolder.appContext ?: return "0"
    return readCurrentVersionNameFromContext(context)
}

// ============================================================
// 下载 APK 并触发安装
// ============================================================

internal fun startApkDownload(
    context: Context,
    downloadUrl: String,
    onComplete: () -> Unit,
    onFailed: (String) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(
                    context, "请允许安装未知应用后重新点击下载", Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
                Toast.makeText(context, "无法打开安装权限设置", Toast.LENGTH_LONG).show()
            }
            onFailed("未授予安装权限")
            return
        }
    }

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    if (dm == null) { onFailed("系统下载服务不可用"); return }

    val fileName = "mote_update_${System.currentTimeMillis()}.apk"

    try {
        val request = DownloadManager.Request(downloadUrl.toUri()).apply {
            setTitle("Mote新版本")
            setDescription("正在下载新版本…")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val downloadId = dm.enqueue(request)
        Toast.makeText(context, "开始下载新版本…", Toast.LENGTH_SHORT).show()

        Thread {
            while (true) {
                Thread.sleep(1200)
                val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor == null) continue
                try {
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        )
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                launchInstall(context, dm, downloadId)
                                onComplete(); return@Thread
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reason = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                                )
                                onFailed("下载失败 (code=$reason)"); return@Thread
                            }
                        }
                    }
                } finally { cursor.close() }
            }
        }.start()
    } catch (e: Exception) {
        onFailed("下载失败：${e.message ?: "未知"}")
    }
}

private fun launchInstall(context: Context, dm: DownloadManager, downloadId: Long) {
    try {
        val uri = dm.getUriForDownloadedFile(downloadId)
        if (uri == null) {
            Toast.makeText(context, "无法获取安装包", Toast.LENGTH_LONG).show(); return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法启动安装：${e.message}", Toast.LENGTH_LONG).show()
    }
}

object MoteAppContextHolder {
    var appContext: Context? = null
}

internal fun shouldAutoCheck(prefs: SharedPreferences): Boolean {
    val mode = prefs.getInt(KEY_AUTO_CHECK_UPDATE, AUTO_CHECK_UPDATE_DEFAULT)
    return when (mode) {
        0 -> true
        1 -> {
            val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
            System.currentTimeMillis() - lastCheck >= 7L * 24 * 3600 * 1000
        }
        else -> false
    }
}

internal fun markChecked(prefs: SharedPreferences) {
    prefs.edit()
        .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
        .apply()
}

@Composable
fun AutoUpdateChecker() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        // 延迟 0.5 秒，等首帧渲染完成后尽快发起网络检查。
        kotlinx.coroutines.delay(500)

        if (!shouldAutoCheck(prefs)) return@LaunchedEffect
        val sourceIndex = prefs
            .getInt(KEY_DOWNLOAD_SOURCE, DOWNLOAD_SOURCE_DEFAULT)
            .coerceIn(0, DOWNLOAD_SOURCE_OPTIONS.lastIndex)
        val result = withContext(Dispatchers.IO) { fetchLatestRelease(sourceIndex) }
        markChecked(prefs)
        if (result is UpdateState.HasUpdate) {
            PendingUpdateStore.pending = result
        }
    }
}
