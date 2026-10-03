@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
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

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ---------------- 启动选项 ----------------
private val STARTUP_OPTIONS = listOf(
    "打开新标签页",
    "打开自定义网页",
    "打开上次退出时的网页",
)

private const val STARTUP_OPTION_LAST_SESSION = 2

// ---------------- 横幅内容选项 ----------------
private val BANNER_CONTENT_OPTIONS = listOf(
    "LOGO",
    "时间",
    "天气",
    "问候语",
)

// ---------------- 背景内容选项 ----------------
private val BACKGROUND_CONTENT_OPTIONS = listOf(
    "契合当前主题",
    "自定义图片",
    "极简白",
    "素雅灰",
    "极夜黑",
    "浅灰蓝",
    "极客紫",
    "嫩芽绿",
    "咸蛋黄",
    "蜜桃粉",
    "元气红",
)

// 背景内容分割线位置：自定义图片（索引 1）之后
private const val BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX = 1

// ---------------- 持久化键 ----------------
private const val KEY_STARTUP_OPTION = "home_startup_option"
// ★ 升级 key，让旧版本可能存在的 false 值失效，确保新装/升级后为默认开启
private const val KEY_ASK_RESTORE_MEMORY = "home_ask_restore_memory_v2"
private const val KEY_SHOW_BANNER = "home_show_banner"
private const val KEY_BANNER_CONTENT = "home_banner_content"
private const val KEY_BACKGROUND_CONTENT = "home_background_content"
private const val KEY_SHOW_QUICK_URLS = "home_show_quick_urls"

// ============================================================
// 主页设置页
// ============================================================

@Composable
fun SettingsHomePage(
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

    // ---------------- 持久化 ----------------
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 一、启动时 ----------------

    var startupOptionIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_STARTUP_OPTION, 0).coerceIn(0, STARTUP_OPTIONS.lastIndex)
        )
    }

    // 默认开启 true
    var askRestoreMemory by remember {
        mutableStateOf(prefs.getBoolean(KEY_ASK_RESTORE_MEMORY, true))
    }

    val isLastSessionSelected = startupOptionIndex == STARTUP_OPTION_LAST_SESSION
    val effectiveAskRestore = if (isLastSessionSelected) false else askRestoreMemory

    // ---------------- 二、新标签页 ----------------

    var showBanner by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_BANNER, true))
    }
    var bannerContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BANNER_CONTENT, 0).coerceIn(0, BANNER_CONTENT_OPTIONS.lastIndex)
        )
    }
    var backgroundContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BACKGROUND_CONTENT, 0).coerceIn(0, BACKGROUND_CONTENT_OPTIONS.lastIndex)
        )
    }
    var showQuickUrls by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_QUICK_URLS, true))
    }

    // ---------------- 下拉条目构建 ----------------

    val bannerContentEntries = remember(bannerContentIndex) {
        listOf(
            DropdownEntry(
                items = BANNER_CONTENT_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = bannerContentIndex == index,
                        onClick = { bannerContentIndex = index },
                    )
                },
            ),
        )
    }

    val backgroundContentEntries = remember(backgroundContentIndex) {
        listOf(
            DropdownEntry(
                items = BACKGROUND_CONTENT_OPTIONS
                    .take(BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        DropdownItem(
                            text = label,
                            selected = backgroundContentIndex == index,
                            onClick = { backgroundContentIndex = index },
                        )
                    },
            ),
            DropdownEntry(
                items = BACKGROUND_CONTENT_OPTIONS
                    .drop(BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        val actualIndex = index + BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX + 1
                        DropdownItem(
                            text = label,
                            selected = backgroundContentIndex == actualIndex,
                            onClick = { backgroundContentIndex = actualIndex },
                        )
                    },
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

            // ★ 动态 filler：内容不足一屏时补足到刚好一屏 + 1dp，其余情况为 0
            val fillerHeight = (
                    viewportHeight - contentTopPadding - contentHeight - 12.dp + 1.dp
                    ).coerceAtLeast(0.dp)

            // ========== 内容层 ==========
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
                    // ==================================================
                    // 内容区（合并成单个 item 以便测量整体高度）
                    // ==================================================
                    item(key = "content") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { contentHeightPx = it.size.height }
                        ) {
                            // ---------- 一、启动时 ----------
                            SmallTitle("启动时")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                STARTUP_OPTIONS.forEachIndexed { index, label ->
                                    RadioButtonPreference(
                                        title = label,
                                        selected = startupOptionIndex == index,
                                        onClick = {
                                            startupOptionIndex = index
                                            prefs.edit()
                                                .putInt(KEY_STARTUP_OPTION, index)
                                                .apply()
                                        },
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "询问恢复上次退出时的网页",
                                    checked = effectiveAskRestore,
                                    enabled = !isLastSessionSelected,
                                    onCheckedChange = { checked ->
                                        askRestoreMemory = checked
                                        prefs.edit()
                                            .putBoolean(KEY_ASK_RESTORE_MEMORY, checked)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 二、新标签页 ----------
                            SmallTitle("新标签页")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "显示横幅",
                                    checked = showBanner,
                                    onCheckedChange = {
                                        showBanner = it
                                        prefs.edit()
                                            .putBoolean(KEY_SHOW_BANNER, it)
                                            .apply()
                                    },
                                )

                                AnimatedVisibility(
                                    visible = showBanner,
                                    enter = expandVertically(
                                        animationSpec = tween(260),
                                        expandFrom = Alignment.Top,
                                    ) + fadeIn(tween(200)),
                                    exit = shrinkVertically(
                                        animationSpec = tween(260),
                                        shrinkTowards = Alignment.Top,
                                    ) + fadeOut(tween(140)),
                                ) {
                                    WindowDropdownPreference(
                                        title = "横幅内容",
                                        entries = bannerContentEntries,
                                        collapseOnSelection = true,
                                    )
                                }

                                WindowDropdownPreference(
                                    title = "背景内容",
                                    entries = backgroundContentEntries,
                                    collapseOnSelection = false,
                                )

                                SwitchPreference(
                                    title = "显示快捷网址",
                                    checked = showQuickUrls,
                                    onCheckedChange = {
                                        showQuickUrls = it
                                        prefs.edit()
                                            .putBoolean(KEY_SHOW_QUICK_URLS, it)
                                            .apply()
                                    },
                                )
                            }
                        }
                    }

                    // --------------------------------------------------
                    // 底部填充（动态高度）
                    // --------------------------------------------------
                    item(key = "bottom_filler") {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fillerHeight)
                        )
                    }
                }
            }

            // ========== 顶栏层（兄弟节点）==========
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
                    title = "主页设置",
                    largeTitle = "主页设置",
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