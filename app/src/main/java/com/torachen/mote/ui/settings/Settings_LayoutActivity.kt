@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ★★★ NavigationEvent ★★★
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// ★★★ MIUIX 组件 ★★★
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

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

// ★★★ MIUIX 工具（Pager 弹簧动画 + overscroll 回弹） ★★★
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ★ 裁剪页在 service 包
import com.torachen.mote.service.loadBitmapForCrop

// ★★★ 协程 ★★★
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TAB_BLUR_RADIUS = 15f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val TAB_ROW_FALLBACK_HEIGHT = 60.dp

private val TAB_ROW_CORNER = 8.dp

private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val BANNER_CONTENT_OPTIONS = listOf(
    "关闭",
    "LOGO",
    "时间",
    "天气",
    "问候语",
)

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

private const val BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX = 1
private const val BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX = 1

private val CONTROL_COLOR_OPTIONS = listOf(
    "跟随软件设置",
    "跟随背景调整",
    "黑色",
    "白色",
)
private const val CONTROL_COLOR_DEFAULT = 0
// ★ 在"跟随背景调整"（index 1）之后插入分割条
private const val CONTROL_COLOR_DIVIDER_AFTER_INDEX = 1
// ★ "跟随背景调整"的 index
private const val CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX = 1

private val TAB_TITLES = listOf("新标签页", "敬请期待")

private const val KEY_BANNER_CONTENT = "home_banner_content"
private const val KEY_BACKGROUND_CONTENT = "home_background_content"
private const val KEY_SHOW_QUICK_URLS = "home_show_quick_urls"
private const val KEY_CONTROL_COLOR_MODE = "ui_control_color_mode"
// ★ 需求 1：勾选"不再弹出此提示"后持久化
private const val KEY_CONTROL_COLOR_SUGGEST_DISMISSED = "control_color_suggest_dismissed"

// ============================================================
// 界面设置页
// ============================================================

@Composable
fun SettingsLayoutPage(
    onBack: () -> Unit,
    onOpenCrop: () -> Unit,
    onNavigateToSearchSettings: () -> Unit = {},
) {
    val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
    val scrollBehavior = MiuixScrollBehavior()
    val scope = rememberCoroutineScope()

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
    val contentTopPadding = if (topBarBottomPx > 0) {
        measuredTop
    } else {
        statusBarHeight + TOP_BAR_FALLBACK_HEIGHT + TAB_ROW_FALLBACK_HEIGHT
    }

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val initialTabFromArgs = remember {
        SettingsLayoutNavArgs.initialTab.also { SettingsLayoutNavArgs.initialTab = 0 }
    }

    var selectedTab by remember { mutableIntStateOf(initialTabFromArgs) }
    val pagerState = rememberPagerState(
        initialPage = initialTabFromArgs,
        pageCount = { TAB_TITLES.size },
    )

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.springAnimateToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            selectedTab = pagerState.currentPage
        }
    }

    // ---------------- 一、内容 ----------------
    var bannerContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BANNER_CONTENT, 1).coerceIn(0, BANNER_CONTENT_OPTIONS.lastIndex)
        )
    }
    var showQuickUrls by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_QUICK_URLS, true))
    }

    // ---------------- 二、主题 ----------------
    var controlColorIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_CONTROL_COLOR_MODE, CONTROL_COLOR_DEFAULT)
                .coerceIn(0, CONTROL_COLOR_OPTIONS.lastIndex)
        )
    }
    var backgroundContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BACKGROUND_CONTENT, 0)
                .coerceIn(0, BACKGROUND_CONTENT_OPTIONS.lastIndex)
        )
    }
    var hasCustomImage by remember { mutableStateOf(hasCustomWallpaper(context)) }

    // ★ 需求 1：控件颜色建议弹窗
    var showControlColorSuggestDialog by remember { mutableStateOf(false) }
    var noMoreSuggest by remember { mutableStateOf(false) }

    // ★ 需求 1：监听"壁纸刚保存"事件 → 弹建议 dialog
    val wallpaperSavedPending = WallpaperSavedEventStore.pending
    LaunchedEffect(wallpaperSavedPending) {
        if (!wallpaperSavedPending) return@LaunchedEffect

        // 判断条件：未勾选"不再弹出" + 当前控件颜色不是"跟随背景调整"
        val dismissed = prefs.getBoolean(KEY_CONTROL_COLOR_SUGGEST_DISMISSED, false)
        val currentColor = prefs.getInt(KEY_CONTROL_COLOR_MODE, CONTROL_COLOR_DEFAULT)

        if (!dismissed && currentColor != CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX) {
            // 等 crop 页 pop 动画结束
            delay(400)
            noMoreSuggest = false
            showControlColorSuggestDialog = true
        }

        // 事件已消费，重置 flag
        WallpaperSavedEventStore.pending = false
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_BACKGROUND_CONTENT -> {
                    backgroundContentIndex = prefs.getInt(key, 0)
                        .coerceIn(0, BACKGROUND_CONTENT_OPTIONS.lastIndex)
                    hasCustomImage = hasCustomWallpaper(context)
                }
                "home_custom_wallpaper_path",
                "home_custom_wallpaper_color" -> {
                    hasCustomImage = hasCustomWallpaper(context)
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val pickWallpaperLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    loadBitmapForCrop(context, uri)
                }
                if (bmp != null) {
                    PendingCropBitmapStore.pending = bmp
                    onOpenCrop()
                } else {
                    Toast.makeText(context, "无法读取图片", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ---------------- 下拉条目 ----------------

    val bannerContentEntries = remember(bannerContentIndex) {
        listOf(
            DropdownEntry(
                items = BANNER_CONTENT_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = bannerContentIndex == index,
                        onClick = {
                            bannerContentIndex = index
                            prefs.edit().putInt(KEY_BANNER_CONTENT, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val controlColorEntries = remember(controlColorIndex) {
        listOf(
            // 第一段：跟随软件设置 / 跟随背景调整
            DropdownEntry(
                items = CONTROL_COLOR_OPTIONS
                    .take(CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        DropdownItem(
                            text = label,
                            selected = controlColorIndex == index,
                            onClick = {
                                controlColorIndex = index
                                prefs.edit().putInt(KEY_CONTROL_COLOR_MODE, index).apply()
                            },
                        )
                    },
            ),
            // 第二段：黑色 / 白色
            DropdownEntry(
                items = CONTROL_COLOR_OPTIONS
                    .drop(CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        val actualIndex = index + CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1
                        DropdownItem(
                            text = label,
                            selected = controlColorIndex == actualIndex,
                            onClick = {
                                controlColorIndex = actualIndex
                                prefs.edit().putInt(KEY_CONTROL_COLOR_MODE, actualIndex).apply()
                            },
                        )
                    },
            ),
        )
    }

    val backgroundContentEntries = remember(backgroundContentIndex, hasCustomImage) {
        listOf(
            DropdownEntry(
                items = BACKGROUND_CONTENT_OPTIONS
                    .take(BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        DropdownItem(
                            text = label,
                            selected = backgroundContentIndex == index,
                            onClick = {
                                if (index == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX) {
                                    if (hasCustomImage) {
                                        backgroundContentIndex = index
                                        prefs.edit()
                                            .putInt(KEY_BACKGROUND_CONTENT, index)
                                            .apply()
                                    } else {
                                        pickWallpaperLauncher.launch("image/*")
                                    }
                                } else {
                                    backgroundContentIndex = index
                                    prefs.edit()
                                        .putInt(KEY_BACKGROUND_CONTENT, index)
                                        .apply()
                                }
                            },
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
                            onClick = {
                                backgroundContentIndex = actualIndex
                                prefs.edit()
                                    .putInt(KEY_BACKGROUND_CONTENT, actualIndex)
                                    .apply()
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
                    item(key = "pager") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                            ) { page ->
                                when (page) {
                                    // ==================================================
                                    // 第一页：新标签页
                                    // ==================================================
                                    0 -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            SmallTitle("内容")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                WindowDropdownPreference(
                                                    title = "横幅内容",
                                                    entries = bannerContentEntries,
                                                    collapseOnSelection = true,
                                                )
                                                SwitchPreference(
                                                    title = "快捷网址",
                                                    checked = showQuickUrls,
                                                    onCheckedChange = {
                                                        showQuickUrls = it
                                                        prefs.edit()
                                                            .putBoolean(KEY_SHOW_QUICK_URLS, it)
                                                            .apply()
                                                    },
                                                )
                                            }

                                            SmallTitle("主题")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                WindowDropdownPreference(
                                                    title = "控件颜色",
                                                    entries = controlColorEntries,
                                                    collapseOnSelection = true,
                                                )

                                                WindowDropdownPreference(
                                                    title = "自定义背景",
                                                    entries = backgroundContentEntries,
                                                    collapseOnSelection = true,
                                                )

                                                AnimatedVisibility(
                                                    visible = backgroundContentIndex ==
                                                            BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX &&
                                                            hasCustomImage,
                                                    enter = expandVertically(
                                                        animationSpec = tween(220, easing = SmoothEase),
                                                        expandFrom = Alignment.Top,
                                                    ) + fadeIn(tween(180, easing = SmoothEase)),
                                                    exit = shrinkVertically(
                                                        animationSpec = tween(220, easing = SmoothEase),
                                                        shrinkTowards = Alignment.Top,
                                                    ) + fadeOut(tween(140, easing = SmoothEase)),
                                                ) {
                                                    ArrowPreference(
                                                        title = "重新选择图片",
                                                        onClick = {
                                                            pickWallpaperLauncher.launch("image/*")
                                                        },
                                                    )
                                                }
                                            }

                                            SmallTitle("在查找其它设置吗？")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { onNavigateToSearchSettings() }
                                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = "显示/隐藏搜索历史",
                                                        color = MiuixTheme.colorScheme.primary,
                                                        fontSize = 16.sp,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // ==================================================
                                    // 第二页：敬请期待
                                    // ==================================================
                                    1 -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 64.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                Text(
                                                    text = "敬请期待",
                                                    fontSize = 14.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                )
                                            }
                                        }
                                    }
                                }
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = "界面",
                        largeTitle = "界面",
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        TabRowWithContour(
                            tabs = TAB_TITLES,
                            selectedTabIndex = selectedTab,
                            onTabSelected = { selectedTab = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (blurSupported && backdrop != null) {
                                        Modifier.textureBlur(
                                            backdrop = backdrop,
                                            shape = RoundedCornerShape(TAB_ROW_CORNER),
                                            blurRadius = TAB_BLUR_RADIUS,
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
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }
        }

        // ============================================================
        // ★ 需求 1：控件颜色建议弹窗（与下载管理删除弹窗格式一致）
        // ============================================================
        WindowDialog(
            show = showControlColorSuggestDialog,
            title = "更改控件颜色",
            onDismissRequest = {
                showControlColorSuggestDialog = false
                noMoreSuggest = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "是否设置为“跟随背景调整”？",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { noMoreSuggest = !noMoreSuggest }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        state = if (noMoreSuggest) ToggleableState.On
                        else ToggleableState.Off,
                        onClick = { noMoreSuggest = !noMoreSuggest },
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "不再弹出此提示",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            if (noMoreSuggest) {
                                prefs.edit()
                                    .putBoolean(KEY_CONTROL_COLOR_SUGGEST_DISMISSED, true)
                                    .apply()
                            }
                            showControlColorSuggestDialog = false
                            noMoreSuggest = false
                        },
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
                            controlColorIndex = CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX
                            prefs.edit()
                                .putInt(KEY_CONTROL_COLOR_MODE, CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX)
                                .apply()
                            if (noMoreSuggest) {
                                prefs.edit()
                                    .putBoolean(KEY_CONTROL_COLOR_SUGGEST_DISMISSED, true)
                                    .apply()
                            }
                            showControlColorSuggestDialog = false
                            noMoreSuggest = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("设置")
                    }
                }
            }
        }
    }
}