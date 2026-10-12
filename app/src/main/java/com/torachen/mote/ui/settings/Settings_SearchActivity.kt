@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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

// ★★★ 协程 ★★★
import kotlinx.coroutines.delay

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ★ 按压反馈动画时长（参考 MIUIX 按压反馈节奏）
private const val PRESS_FADE_IN_MS = 220    // 灰色淡入
private const val PRESS_HOLD_MS = 120       // 灰色保持
private const val PRESS_FADE_OUT_MS = 320   // 灰色淡出
private const val PRESS_GAP_MS = 140        // 两次脉冲之间的间隔

/** 前六个内置搜索引擎（与主页引擎列表保持一致，不含"自定义"） */
private val BUILT_IN_SEARCH_ENGINES = listOf(
    "必应",
    "抖音",
    "百度",
    "神马",
    "搜狗",
    "谷歌",
)

private const val CUSTOM_ENGINE_LABEL = "自定义引擎"
private const val CUSTOM_ENGINE_INDEX = 6

// ---------------- 持久化键 ----------------
private const val KEY_DEFAULT_ENGINE = "default_search_engine"
private const val KEY_DEFAULT_ENGINE_DISPLAY = "default_search_engine_display"

private const val KEY_ENGINE_SWITCHER_BAR = "engine_switcher_bar_enabled"
private const val KEY_QUICK_INPUT_BAR = "quick_input_bar_enabled"
private const val KEY_SCAN_MODULE = "scan_module_enabled"
private const val KEY_HISTORY_MODULE = "history_module_enabled"

// ============================================================
// 搜索设置页
// ============================================================

@Composable
fun SettingsSearchPage(
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
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var defaultEngineDisplayIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_DEFAULT_ENGINE_DISPLAY, 0).coerceIn(0, CUSTOM_ENGINE_INDEX)
        )
    }

    var engineSwitcherBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_ENGINE_SWITCHER_BAR, true))
    }

    var quickInputBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_QUICK_INPUT_BAR, true))
    }
    var scanModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_SCAN_MODULE, true))
    }
    var historyModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_HISTORY_MODULE, true))
    }

    // ★ 从界面设置跳转过来时，闪烁搜索历史模块（灰色背景按压反馈）
    val flashProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (SettingsLayoutNavArgs.flashSearchHistoryOnEnter) {
            SettingsLayoutNavArgs.flashSearchHistoryOnEnter = false
            // 等 push 动画基本结束再闪
            delay(320)

            // ★ 按压反馈：完整的一次「淡入 → 保持 → 淡出」，重复 2 次
            repeat(2) { idx ->
                if (idx > 0) delay(PRESS_GAP_MS.toLong())

                // 淡入
                flashProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = PRESS_FADE_IN_MS,
                        easing = SmoothEase,
                    ),
                )
                // 保持
                delay(PRESS_HOLD_MS.toLong())
                // 淡出
                flashProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = PRESS_FADE_OUT_MS,
                        easing = SmoothEase,
                    ),
                )
            }
        }
    }

    fun selectDefaultEngine(displayIndex: Int) {
        defaultEngineDisplayIndex = displayIndex
        val actualIndex = if (displayIndex >= BUILT_IN_SEARCH_ENGINES.size) 0 else displayIndex
        prefs.edit()
            .putInt(KEY_DEFAULT_ENGINE_DISPLAY, displayIndex)
            .putInt(KEY_DEFAULT_ENGINE, actualIndex)
            .apply()
    }

    val engineEntries = remember(defaultEngineDisplayIndex) {
        listOf(
            DropdownEntry(
                items = BUILT_IN_SEARCH_ENGINES.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = defaultEngineDisplayIndex == index,
                        onClick = { selectDefaultEngine(index) },
                    )
                },
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = CUSTOM_ENGINE_LABEL,
                        selected = defaultEngineDisplayIndex == CUSTOM_ENGINE_INDEX,
                        onClick = { selectDefaultEngine(CUSTOM_ENGINE_INDEX) },
                    ),
                ),
            ),
        )
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            val viewportHeight = maxHeight
            var contentHeightPx by remember { mutableIntStateOf(0) }
            val contentHeight = with(density) { contentHeightPx.toDp() }

            val fillerHeight = (
                    viewportHeight - contentTopPadding - contentHeight - 12.dp + 1.dp
                    ).coerceAtLeast(0.dp)

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
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = 12.dp,
                    ),
                ) {
                    item(key = "content") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { contentHeightPx = it.size.height }
                        ) {
                            // ---------- 一、搜索引擎 ----------
                            SmallTitle("搜索引擎")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                WindowDropdownPreference(
                                    title = "默认搜索引擎",
                                    entries = engineEntries,
                                    collapseOnSelection = true,
                                )
                                SwitchPreference(
                                    title = "搜索引擎切换栏",
                                    checked = engineSwitcherBarEnabled,
                                    onCheckedChange = {
                                        engineSwitcherBarEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_ENGINE_SWITCHER_BAR, it)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 二、辅助功能 ----------
                            SmallTitle("辅助功能")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "快捷输入栏",
                                    summary = "在主页搜索栏输入时，键盘上方显示常用字符",
                                    checked = quickInputBarEnabled,
                                    onCheckedChange = {
                                        quickInputBarEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_QUICK_INPUT_BAR, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "扫一扫模块",
                                    checked = scanModuleEnabled,
                                    onCheckedChange = {
                                        scanModuleEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_SCAN_MODULE, it)
                                            .apply()
                                    },
                                )

                                // ★ 搜索历史模块：按压反馈（灰色背景脉冲）
                                //   外层 Box 是定位容器，内层 matchParentSize 的 Box 是灰色覆盖层
                                Box {
                                    SwitchPreference(
                                        title = "搜索历史模块",
                                        checked = historyModuleEnabled,
                                        onCheckedChange = {
                                            historyModuleEnabled = it
                                            prefs.edit()
                                                .putBoolean(KEY_HISTORY_MODULE, it)
                                                .apply()
                                        },
                                    )
                                    // 覆盖层：alpha 由 flashProgress 驱动，随动画平滑变化
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                color = MiuixTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.08f * flashProgress.value
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    item(key = "bottom_filler") {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fillerHeight)
                        )
                    }
                }
            }

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
                    title = "搜索设置",
                    largeTitle = "搜索设置",
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