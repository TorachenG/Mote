@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val TEXT_SCALE_OPTIONS = listOf("超小", "小", "正常", "大", "超大")
private const val TEXT_SCALE_DEFAULT = 2

private val ZOOM_RATIO_OPTIONS = listOf("50%", "75%", "100%", "125%", "150%", "200%")
private const val ZOOM_RATIO_DEFAULT = 2

private val NIGHT_MODE_OPTIONS = listOf("跟随软件设置", "关闭", "开启")
private const val NIGHT_MODE_DEFAULT = 0

private val VIDEO_PLAYER_OPTIONS = listOf("内置播放器", "原生播放器")
private const val VIDEO_PLAYER_DEFAULT = 0

private val DYNAMIC_TOOL_OPTIONS = listOf(
    "关闭", "主页", "刷新", "全屏", "添加书签", "书签列表", "历史记录", "下载管理", "网页翻译", "资源嗅探"
)
private const val DYNAMIC_TOOL_DEFAULT = 1

private const val KEY_TEXT_SCALE = "web_text_scale"
private const val KEY_FORCE_ZOOM = "web_force_zoom_enabled"
private const val KEY_DEFAULT_ZOOM = "web_default_zoom"
private const val KEY_RESTORE_ZOOM = "web_restore_zoom_enabled"
private const val KEY_NIGHT_MODE = "web_night_mode"
private const val KEY_NIGHT_DIM = "web_night_mode_dim"
private const val KEY_DESKTOP_MODE = "web_desktop_mode"
private const val KEY_NO_IMAGE = "web_no_image"
private const val KEY_FULLSCREEN = "web_fullscreen"
private const val KEY_VIDEO_PLAYER = "web_video_player"
private const val KEY_PULL_REFRESH = "web_pull_refresh"
private const val KEY_SWIPE_NAV = "web_swipe_nav"
private const val KEY_DYNAMIC_TOOL = "web_dynamic_tool"
private const val KEY_INCOGNITO = "web_incognito"
private const val KEY_DYNAMIC_TOOL_SUGGEST = "web_dynamic_tool_suggest"

// ============================================================
// 状态
// ============================================================

@Immutable
private data class BrowserPageState(
    val textScaleIndex: Int,
    val forceZoomEnabled: Boolean,
    val defaultZoomIndex: Int,
    val restoreZoomEnabled: Boolean,
    val nightModeIndex: Int,
    val nightModeDimEnabled: Boolean,
    val desktopModeEnabled: Boolean,
    val noImageEnabled: Boolean,
    val fullscreenEnabled: Boolean,
    val videoPlayerIndex: Int,
    val pullRefreshEnabled: Boolean,
    val swipeNavEnabled: Boolean,
    val dynamicToolIndex: Int,
    val dynamicToolSuggestEnabled: Boolean,
    val incognitoEnabled: Boolean,
    val isDefaultBrowser: Boolean,
)

@Immutable
private data class BrowserPageActions(
    val onBack: () -> Unit,
    val onTextScaleChange: (Int) -> Unit,
    val onForceZoomChange: (Boolean) -> Unit,
    val onDefaultZoomChange: (Int) -> Unit,
    val onRestoreZoomChange: (Boolean) -> Unit,
    val onNightModeChange: (Int) -> Unit,
    val onNightModeDimChange: (Boolean) -> Unit,
    val onDesktopModeChange: (Boolean) -> Unit,
    val onNoImageChange: (Boolean) -> Unit,
    val onFullscreenChange: (Boolean) -> Unit,
    val onVideoPlayerChange: (Int) -> Unit,
    val onPullRefreshChange: (Boolean) -> Unit,
    val onSwipeNavChange: (Boolean) -> Unit,
    val onDynamicToolChange: (Int) -> Unit,
    val onDynamicToolSuggestChange: (Boolean) -> Unit,
    val onIncognitoChange: (Boolean) -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsWebPage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var textScaleIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_TEXT_SCALE, TEXT_SCALE_DEFAULT)
                .coerceIn(0, TEXT_SCALE_OPTIONS.lastIndex)
        )
    }
    var forceZoomEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_FORCE_ZOOM, false))
    }
    var defaultZoomIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_DEFAULT_ZOOM, ZOOM_RATIO_DEFAULT)
                .coerceIn(0, ZOOM_RATIO_OPTIONS.lastIndex)
        )
    }
    var restoreZoomEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_RESTORE_ZOOM, true))
    }
    var nightModeIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_NIGHT_MODE, NIGHT_MODE_DEFAULT)
                .coerceIn(0, NIGHT_MODE_OPTIONS.lastIndex)
        )
    }
    var nightModeDimEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NIGHT_DIM, true))
    }
    var desktopModeEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DESKTOP_MODE, false))
    }
    var noImageEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NO_IMAGE, false))
    }
    var fullscreenEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_FULLSCREEN, false))
    }

    var videoPlayerIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_VIDEO_PLAYER, VIDEO_PLAYER_DEFAULT)
                .coerceIn(0, VIDEO_PLAYER_OPTIONS.lastIndex)
        )
    }
    var pullRefreshEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_PULL_REFRESH, true))
    }
    var swipeNavEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_SWIPE_NAV, true))
    }
    var dynamicToolIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_DYNAMIC_TOOL, DYNAMIC_TOOL_DEFAULT)
                .coerceIn(0, DYNAMIC_TOOL_OPTIONS.lastIndex)
        )
    }
    var dynamicToolSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_TOOL_SUGGEST, true))
    }
    var incognitoEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_INCOGNITO, false))
    }

    var isDefaultBrowser by remember { mutableStateOf(checkIsDefaultBrowser(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultBrowser = checkIsDefaultBrowser(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state = BrowserPageState(
        textScaleIndex = textScaleIndex,
        forceZoomEnabled = forceZoomEnabled,
        defaultZoomIndex = defaultZoomIndex,
        restoreZoomEnabled = restoreZoomEnabled,
        nightModeIndex = nightModeIndex,
        nightModeDimEnabled = nightModeDimEnabled,
        desktopModeEnabled = desktopModeEnabled,
        noImageEnabled = noImageEnabled,
        fullscreenEnabled = fullscreenEnabled,
        videoPlayerIndex = videoPlayerIndex,
        pullRefreshEnabled = pullRefreshEnabled,
        swipeNavEnabled = swipeNavEnabled,
        dynamicToolIndex = dynamicToolIndex,
        dynamicToolSuggestEnabled = dynamicToolSuggestEnabled,
        incognitoEnabled = incognitoEnabled,
        isDefaultBrowser = isDefaultBrowser,
    )

    val actions = BrowserPageActions(
        onBack = onBack,
        onTextScaleChange = { textScaleIndex = it; prefs.edit().putInt(KEY_TEXT_SCALE, it).apply() },
        onForceZoomChange = { forceZoomEnabled = it; prefs.edit().putBoolean(KEY_FORCE_ZOOM, it).apply() },
        onDefaultZoomChange = { defaultZoomIndex = it; prefs.edit().putInt(KEY_DEFAULT_ZOOM, it).apply() },
        onRestoreZoomChange = { restoreZoomEnabled = it; prefs.edit().putBoolean(KEY_RESTORE_ZOOM, it).apply() },
        onNightModeChange = { nightModeIndex = it; prefs.edit().putInt(KEY_NIGHT_MODE, it).apply() },
        onNightModeDimChange = { nightModeDimEnabled = it; prefs.edit().putBoolean(KEY_NIGHT_DIM, it).apply() },
        onDesktopModeChange = { desktopModeEnabled = it; prefs.edit().putBoolean(KEY_DESKTOP_MODE, it).apply() },
        onNoImageChange = { noImageEnabled = it; prefs.edit().putBoolean(KEY_NO_IMAGE, it).apply() },
        onFullscreenChange = { fullscreenEnabled = it; prefs.edit().putBoolean(KEY_FULLSCREEN, it).apply() },
        onVideoPlayerChange = { videoPlayerIndex = it; prefs.edit().putInt(KEY_VIDEO_PLAYER, it).apply() },
        onPullRefreshChange = { pullRefreshEnabled = it; prefs.edit().putBoolean(KEY_PULL_REFRESH, it).apply() },
        onSwipeNavChange = { swipeNavEnabled = it; prefs.edit().putBoolean(KEY_SWIPE_NAV, it).apply() },
        onDynamicToolChange = { dynamicToolIndex = it; prefs.edit().putInt(KEY_DYNAMIC_TOOL, it).apply() },
        onDynamicToolSuggestChange = { dynamicToolSuggestEnabled = it; prefs.edit().putBoolean(KEY_DYNAMIC_TOOL_SUGGEST, it).apply() },
        onIncognitoChange = { incognitoEnabled = it; prefs.edit().putBoolean(KEY_INCOGNITO, it).apply() },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsWebMiuix(state, actions)
        UiMode.Material -> SettingsWebMaterial(state, actions)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsWebMiuix(
    state: BrowserPageState,
    actions: BrowserPageActions,
) {
    val context = LocalContext.current
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val browserRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { /* 结果可以忽略，返回页面时 ON_RESUME 会自动重新检测默认浏览器状态 */ }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

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
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = SmoothEase),
        label = "settingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val textScaleEntries = remember(state.textScaleIndex) {
        listOf(
            DropdownEntry(
                items = TEXT_SCALE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.textScaleIndex == index,
                        onClick = { actions.onTextScaleChange(index) },
                    )
                },
            ),
        )
    }

    val zoomRatioEntries = remember(state.defaultZoomIndex) {
        listOf(
            DropdownEntry(
                items = ZOOM_RATIO_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.defaultZoomIndex == index,
                        onClick = { actions.onDefaultZoomChange(index) },
                    )
                },
            ),
        )
    }

    val nightModeEntries = remember(state.nightModeIndex) {
        listOf(
            DropdownEntry(
                items = NIGHT_MODE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.nightModeIndex == index,
                        onClick = { actions.onNightModeChange(index) },
                    )
                },
            ),
        )
    }

    val videoPlayerEntries = remember(state.videoPlayerIndex) {
        listOf(
            DropdownEntry(
                items = VIDEO_PLAYER_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.videoPlayerIndex == index,
                        onClick = { actions.onVideoPlayerChange(index) },
                    )
                },
            ),
        )
    }

    val dynamicToolEntries = remember(state.dynamicToolIndex) {
        listOf(
            DropdownEntry(
                items = DYNAMIC_TOOL_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = state.dynamicToolIndex == index,
                        onClick = { actions.onDynamicToolChange(index) },
                    )
                },
            ),
        )
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else Modifier
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (listState.canScrollForward || listState.canScrollBackward) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            }
                        )
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("显示")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "网页文字大小",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_text_scale",
                                ) {
                                    WindowDropdownPreference(
                                        title = "网页文字大小",
                                        entries = textScaleEntries,
                                        collapseOnSelection = true,
                                    )
                                }
                                SearchableItem(
                                    title = "默认缩放比例",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_default_zoom",
                                ) {
                                    WindowDropdownPreference(
                                        title = "默认缩放比例",
                                        entries = zoomRatioEntries,
                                        collapseOnSelection = true,
                                    )
                                }
                                SearchableItem(
                                    title = "强制启用缩放",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_force_zoom",
                                ) {
                                    SwitchPreference(
                                        title = "强制启用缩放",
                                        summary = "开启后可强制通过双指捏合放大缩小网页",
                                        checked = state.forceZoomEnabled,
                                        onCheckedChange = actions.onForceZoomChange,
                                    )
                                }
                                SearchableItem(
                                    title = "恢复缩放比例",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_restore_zoom",
                                ) {
                                    SwitchPreference(
                                        title = "恢复缩放比例",
                                        summary = "开启后，下次浏览被修改过缩放比例的网页时自动恢复",
                                        checked = state.restoreZoomEnabled,
                                        onCheckedChange = actions.onRestoreZoomChange,
                                    )
                                }
                                SearchableItem(
                                    title = "夜间模式",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_night_mode",
                                ) {
                                    WindowDropdownPreference(
                                        title = "夜间模式",
                                        entries = nightModeEntries,
                                        collapseOnSelection = true,
                                    )
                                }
                                SearchableItem(
                                    title = "夜间模式调暗页面",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_night_mode_dim",
                                ) {
                                    SwitchPreference(
                                        title = "夜间模式调暗页面",
                                        checked = state.nightModeDimEnabled,
                                        onCheckedChange = actions.onNightModeDimChange,
                                    )
                                }
                                SearchableItem(
                                    title = "电脑模式",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_desktop_mode",
                                ) {
                                    SwitchPreference(
                                        title = "电脑模式",
                                        checked = state.desktopModeEnabled,
                                        onCheckedChange = actions.onDesktopModeChange,
                                    )
                                }
                                SearchableItem(
                                    title = "无图模式",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_no_image",
                                ) {
                                    SwitchPreference(
                                        title = "无图模式",
                                        checked = state.noImageEnabled,
                                        onCheckedChange = actions.onNoImageChange,
                                    )
                                }
                                SearchableItem(
                                    title = "全屏模式",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_fullscreen",
                                ) {
                                    SwitchPreference(
                                        title = "全屏模式",
                                        checked = state.fullscreenEnabled,
                                        onCheckedChange = actions.onFullscreenChange,
                                    )
                                }
                            }

                            SmallTitle("功能")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "网页视频播放器",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_video_player",
                                ) {
                                    WindowDropdownPreference(
                                        title = "网页视频播放器",
                                        entries = videoPlayerEntries,
                                        collapseOnSelection = true,
                                    )
                                }
                                SearchableItem(
                                    title = "网页顶部下拉刷新",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_pull_refresh",
                                ) {
                                    SwitchPreference(
                                        title = "网页顶部下拉刷新",
                                        checked = state.pullRefreshEnabled,
                                        onCheckedChange = actions.onPullRefreshChange,
                                    )
                                }
                                SearchableItem(
                                    title = "左右滑动后退/前进",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_swipe_nav",
                                ) {
                                    SwitchPreference(
                                        title = "左右滑动后退/前进",
                                        checked = state.swipeNavEnabled,
                                        onCheckedChange = actions.onSwipeNavChange,
                                    )
                                }
                                SearchableItem(
                                    title = "工具栏中间位",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_dynamic_tool",
                                ) {
                                    WindowDropdownPreference(
                                        title = "工具栏中间位",
                                        summary = "自定义工具栏中间功能",
                                        entries = dynamicToolEntries,
                                        collapseOnSelection = true,
                                    )
                                }
                                SearchableItem(
                                    title = "工具栏功能建议",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_dynamic_tool_suggest",
                                ) {
                                    SwitchPreference(
                                        title = "工具栏功能建议",
                                        summary = "开启后，会在合适的时候自动更换工具栏中间的功能",
                                        checked = state.dynamicToolSuggestEnabled,
                                        onCheckedChange = actions.onDynamicToolSuggestChange,
                                    )
                                }
                                SearchableItem(
                                    title = "无痕模式",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_incognito",
                                ) {
                                    SwitchPreference(
                                        title = "无痕模式",
                                        checked = state.incognitoEnabled,
                                        onCheckedChange = actions.onIncognitoChange,
                                    )
                                }
                            }

                            SmallTitle("其它")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "设置默认浏览器",
                                    pageRoute = "settings_web",
                                    pageTitle = "网页浏览设置",
                                    itemKey = "settings_web_default_browser",
                                ) {
                                    SwitchPreference(
                                        title = "设置默认浏览器",
                                        checked = state.isDefaultBrowser,
                                        onCheckedChange = { _ ->
                                            openSetDefaultBrowser(
                                                context = context,
                                                launcher = browserRoleLauncher,
                                                isCurrentlyDefault = state.isDefaultBrowser,
                                            )
                                        },
                                    )
                                }
                            }

                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }

                SettingsPageAutoScroll(
                    lazyListState = listState,
                    pageRoute = "settings_web",
                    contentTopPadding = contentTopPadding,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                Modifier.progressiveTextureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            }
                        } else {
                            Modifier.background(surfaceColor)
                        }
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    }
            ) {
                TopAppBar(
                    title = "网页浏览设置",
                    largeTitle = "网页浏览设置",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = actions.onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                )
            }
        }
    }
}

// ============================================================
// Material 3 版
// ============================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsWebMaterial(
    state: BrowserPageState,
    actions: BrowserPageActions,
) {
    val context = LocalContext.current
    val navOwner = rememberNavigationEventDispatcherOwner()

    val browserRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { /* 结果可以忽略，返回页面时 ON_RESUME 会自动重新检测默认浏览器状态 */ }

    val pageBg = M3MaterialTheme.colorScheme.surface
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("网页浏览设置") },
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
                item(key = "display_header") {
                    WebSectionHeader("显示")
                }

                item(key = "display_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            content = listOf(
                                {
                                    M3SegmentDropdownItem(
                                        title = "网页文字大小",
                                        summary = null,
                                        options = TEXT_SCALE_OPTIONS,
                                        selectedIndex = state.textScaleIndex,
                                        onSelected = actions.onTextScaleChange,
                                    )
                                },
                                {
                                    M3SegmentDropdownItem(
                                        title = "默认缩放比例",
                                        summary = null,
                                        options = ZOOM_RATIO_OPTIONS,
                                        selectedIndex = state.defaultZoomIndex,
                                        onSelected = actions.onDefaultZoomChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "强制启用缩放",
                                        summary = "开启后可强制通过双指捏合放大缩小网页",
                                        checked = state.forceZoomEnabled,
                                        onCheckedChange = actions.onForceZoomChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "恢复缩放比例",
                                        summary = "开启后，下次浏览被修改过缩放比例的网页时自动恢复",
                                        checked = state.restoreZoomEnabled,
                                        onCheckedChange = actions.onRestoreZoomChange,
                                    )
                                },
                                {
                                    M3SegmentDropdownItem(
                                        title = "夜间模式",
                                        summary = null,
                                        options = NIGHT_MODE_OPTIONS,
                                        selectedIndex = state.nightModeIndex,
                                        onSelected = actions.onNightModeChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "夜间模式调暗页面",
                                        summary = null,
                                        checked = state.nightModeDimEnabled,
                                        onCheckedChange = actions.onNightModeDimChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "电脑模式",
                                        summary = null,
                                        checked = state.desktopModeEnabled,
                                        onCheckedChange = actions.onDesktopModeChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "无图模式",
                                        summary = null,
                                        checked = state.noImageEnabled,
                                        onCheckedChange = actions.onNoImageChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "全屏模式",
                                        summary = null,
                                        checked = state.fullscreenEnabled,
                                        onCheckedChange = actions.onFullscreenChange,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "function_header") {
                    WebSectionHeader("功能")
                }

                item(key = "function_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            content = listOf(
                                {
                                    M3SegmentDropdownItem(
                                        title = "网页视频播放器",
                                        summary = null,
                                        options = VIDEO_PLAYER_OPTIONS,
                                        selectedIndex = state.videoPlayerIndex,
                                        onSelected = actions.onVideoPlayerChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "网页顶部下拉刷新",
                                        summary = null,
                                        checked = state.pullRefreshEnabled,
                                        onCheckedChange = actions.onPullRefreshChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "左右滑动后退/前进",
                                        summary = null,
                                        checked = state.swipeNavEnabled,
                                        onCheckedChange = actions.onSwipeNavChange,
                                    )
                                },
                                {
                                    M3SegmentDropdownItem(
                                        title = "工具栏中间位",
                                        summary = "自定义工具栏中间功能",
                                        options = DYNAMIC_TOOL_OPTIONS,
                                        selectedIndex = state.dynamicToolIndex,
                                        onSelected = actions.onDynamicToolChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "工具栏功能建议",
                                        summary = "开启后，会在合适的时候自动更换工具栏中间的功能",
                                        checked = state.dynamicToolSuggestEnabled,
                                        onCheckedChange = actions.onDynamicToolSuggestChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "无痕模式",
                                        summary = null,
                                        checked = state.incognitoEnabled,
                                        onCheckedChange = actions.onIncognitoChange,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "other_header") {
                    WebSectionHeader("其它")
                }

                item(key = "other_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            content = listOf(
                                {
                                    M3SegmentSwitchItem(
                                        title = "设置默认浏览器",
                                        checked = state.isDefaultBrowser,
                                        onCheckedChange = { _ ->
                                            openSetDefaultBrowser(
                                                context = context,
                                                launcher = browserRoleLauncher,
                                                isCurrentlyDefault = state.isDefaultBrowser,
                                            )
                                        },
                                    )
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Material 分组标题
// ============================================================

@Composable
private fun WebSectionHeader(text: String) {
    M3Text(
        text = text,
        color = M3MaterialTheme.colorScheme.primary,
        style = M3MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 32.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
    )
}

// ============================================================
// 默认浏览器检测
// ============================================================

private fun checkIsDefaultBrowser(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
            }
        } catch (_: Exception) {
        }
    }
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))
        val resolveInfo = context.packageManager.resolveActivity(
            intent, PackageManager.MATCH_DEFAULT_ONLY,
        )
        resolveInfo?.activityInfo?.packageName == context.packageName
    } catch (_: Exception) {
        false
    }
}

// ============================================================
// 设为默认浏览器
// ============================================================

private fun openSetDefaultBrowser(
    context: Context,
    launcher: ActivityResultLauncher<Intent>,
    isCurrentlyDefault: Boolean,
) {
    if (isCurrentlyDefault) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(detailIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "无法打开默认应用设置", Toast.LENGTH_SHORT).show()
            }
        }
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                launcher.launch(intent)
                return
            }
        } catch (_: Exception) {
        }
    }
    try {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(detailIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "无法打开默认应用设置", Toast.LENGTH_SHORT).show()
        }
    }
}
