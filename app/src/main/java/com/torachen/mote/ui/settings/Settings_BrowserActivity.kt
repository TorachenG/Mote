@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// ★★★ NavigationEvent ★★★
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// ★★★ MIUIX 组件 ★★★
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// ★★★ MIUIX 模糊 ★★★
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

// ★★★ MIUIX 图标 ★★★
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

// ★★★ MIUIX 工具（overscroll 回弹） ★★★
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ---------------- 一、显示 ----------------
private val TEXT_SCALE_OPTIONS = listOf("超小", "小", "正常", "大", "超大")
private const val TEXT_SCALE_DEFAULT = 2

private val ZOOM_RATIO_OPTIONS = listOf("50%", "75%", "100%", "125%", "150%", "200%")
private const val ZOOM_RATIO_DEFAULT = 2

private val NIGHT_MODE_OPTIONS = listOf("跟随软件设置", "关闭", "开启")
private const val NIGHT_MODE_DEFAULT = 0

// ---------------- 二、功能 ----------------
private val VIDEO_PLAYER_OPTIONS = listOf("内置播放器", "原生播放器")
private const val VIDEO_PLAYER_DEFAULT = 0

private val DYNAMIC_TOOL_OPTIONS = listOf(
    "关闭", "主页", "刷新", "全屏", "添加书签", "网页翻译", "资源嗅探", "开发工具"
)
private const val DYNAMIC_TOOL_DEFAULT = 1

// ---------------- 持久化键 ----------------
private const val KEY_TEXT_SCALE = "web_text_scale"
private const val KEY_FORCE_ZOOM = "web_force_zoom_enabled"
private const val KEY_DEFAULT_ZOOM = "web_default_zoom"
private const val KEY_RESTORE_ZOOM = "web_restore_zoom_enabled"
private const val KEY_NIGHT_MODE = "web_night_mode"
private const val KEY_DESKTOP_MODE = "web_desktop_mode"
private const val KEY_NO_IMAGE = "web_no_image"
private const val KEY_FULLSCREEN = "web_fullscreen"
private const val KEY_VIDEO_PLAYER = "web_video_player"
private const val KEY_PULL_REFRESH = "web_pull_refresh"
private const val KEY_SWIPE_NAV = "web_swipe_nav"
private const val KEY_DYNAMIC_TOOL = "web_dynamic_tool"
private const val KEY_INCOGNITO = "web_incognito"
// ★ 需求 2：工具栏功能建议开关
private const val KEY_DYNAMIC_TOOL_SUGGEST = "web_dynamic_tool_suggest"

// ============================================================
// 网页浏览设置
// ============================================================

@Composable
fun SettingsWebPage(
    onBack: () -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
    val scrollBehavior = MiuixScrollBehavior()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else {
        null
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 一、显示 ----------------
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
    var desktopModeEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DESKTOP_MODE, false))
    }
    var noImageEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NO_IMAGE, false))
    }
    var fullscreenEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_FULLSCREEN, false))
    }

    // ---------------- 二、功能 ----------------
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
    // ★ 需求 2：工具栏功能建议（动态功能位自动切换）开关
    var dynamicToolSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_TOOL_SUGGEST, true))
    }
    var incognitoEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_INCOGNITO, false))
    }

    // ---------------- 下拉条目构建 ----------------
    val textScaleEntries = remember(textScaleIndex) {
        listOf(
            DropdownEntry(
                items = TEXT_SCALE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = textScaleIndex == index,
                        onClick = {
                            textScaleIndex = index
                            prefs.edit().putInt(KEY_TEXT_SCALE, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val zoomRatioEntries = remember(defaultZoomIndex) {
        listOf(
            DropdownEntry(
                items = ZOOM_RATIO_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = defaultZoomIndex == index,
                        onClick = {
                            defaultZoomIndex = index
                            prefs.edit().putInt(KEY_DEFAULT_ZOOM, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val nightModeEntries = remember(nightModeIndex) {
        listOf(
            DropdownEntry(
                items = NIGHT_MODE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = nightModeIndex == index,
                        onClick = {
                            nightModeIndex = index
                            prefs.edit().putInt(KEY_NIGHT_MODE, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val videoPlayerEntries = remember(videoPlayerIndex) {
        listOf(
            DropdownEntry(
                items = VIDEO_PLAYER_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = videoPlayerIndex == index,
                        onClick = {
                            videoPlayerIndex = index
                            prefs.edit().putInt(KEY_VIDEO_PLAYER, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val dynamicToolEntries = remember(dynamicToolIndex) {
        listOf(
            DropdownEntry(
                items = DYNAMIC_TOOL_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = dynamicToolIndex == index,
                        onClick = {
                            dynamicToolIndex = index
                            prefs.edit().putInt(KEY_DYNAMIC_TOOL, index).apply()
                        },
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
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // ---------- 一、显示 ----------
                            SmallTitle("显示")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                WindowDropdownPreference(
                                    title = "网页文字大小",
                                    entries = textScaleEntries,
                                    collapseOnSelection = true,
                                )
                                WindowDropdownPreference(
                                    title = "默认缩放比例",
                                    entries = zoomRatioEntries,
                                    collapseOnSelection = true,
                                )
                                SwitchPreference(
                                    title = "强制启用缩放",
                                    summary = "开启后可强制通过双指捏合放大缩小网页",
                                    checked = forceZoomEnabled,
                                    onCheckedChange = {
                                        forceZoomEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_FORCE_ZOOM, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "恢复缩放比例",
                                    summary = "开启后，下次浏览被修改过缩放比例的网页时自动恢复",
                                    checked = restoreZoomEnabled,
                                    onCheckedChange = {
                                        restoreZoomEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_RESTORE_ZOOM, it)
                                            .apply()
                                    },
                                )
                                WindowDropdownPreference(
                                    title = "夜间模式",
                                    entries = nightModeEntries,
                                    collapseOnSelection = true,
                                )
                                SwitchPreference(
                                    title = "电脑模式",
                                    checked = desktopModeEnabled,
                                    onCheckedChange = {
                                        desktopModeEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_DESKTOP_MODE, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "无图模式",
                                    checked = noImageEnabled,
                                    onCheckedChange = {
                                        noImageEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_NO_IMAGE, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "全屏模式",
                                    checked = fullscreenEnabled,
                                    onCheckedChange = {
                                        fullscreenEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_FULLSCREEN, it)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 二、功能 ----------
                            SmallTitle("功能")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                WindowDropdownPreference(
                                    title = "网页视频播放器",
                                    entries = videoPlayerEntries,
                                    collapseOnSelection = true,
                                )
                                SwitchPreference(
                                    title = "网页顶部下拉刷新",
                                    checked = pullRefreshEnabled,
                                    onCheckedChange = {
                                        pullRefreshEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_PULL_REFRESH, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "左右滑动后退/前进",
                                    checked = swipeNavEnabled,
                                    onCheckedChange = {
                                        swipeNavEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_SWIPE_NAV, it)
                                            .apply()
                                    },
                                )

                                // ★ 需求 1：标题改为"工具栏最右侧"，提示文字改为"自定义第五项功能"
                                WindowDropdownPreference(
                                    title = "工具栏最右侧",
                                    summary = "自定义第五项功能",
                                    entries = dynamicToolEntries,
                                    collapseOnSelection = true,
                                )
                                // ★ 需求 2：下方新增"工具栏功能建议"开关
                                SwitchPreference(
                                    title = "工具栏功能建议",
                                    summary = "开启后，会在合适的时候自动更换工具栏最右侧的功能",
                                    checked = dynamicToolSuggestEnabled,
                                    onCheckedChange = {
                                        dynamicToolSuggestEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_DYNAMIC_TOOL_SUGGEST, it)
                                            .apply()
                                    },
                                )

                                SwitchPreference(
                                    title = "无痕模式",
                                    checked = incognitoEnabled,
                                    onCheckedChange = {
                                        incognitoEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_INCOGNITO, it)
                                            .apply()
                                    },
                                )
                            }

                            // ★ 底部导航栏占位
                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }

            // ========== 顶栏层 ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = TOP_BAR_BLUR_RADIUS,
                                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(
                                            color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA),
                                        ),
                                    ),
                                ),
                            )
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
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            }
        }
    }
}