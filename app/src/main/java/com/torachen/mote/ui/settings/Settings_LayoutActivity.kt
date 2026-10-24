@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.graphics.Bitmap
import android.os.Build
import android.net.Uri
import android.widget.Toast
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.SharedPreferences

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Slider
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
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Search

import top.yukonga.miuix.kmp.utils.springAnimateToPage
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

import com.torachen.mote.service.loadBitmapForCrop

import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.ListItem as M3ListItem
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.PrimaryTabRow as M3PrimaryTabRow
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Tab as M3Tab
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.rounded.Check

import kotlinx.coroutines.Dispatchers
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


private val M3_COLOR_MODE_BUTTON_HEIGHT = 48.dp
private val TAB_ROW_CORNER = 8.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ---------------- 界面风格 ----------------
private val UI_STYLE_OPTIONS = listOf("Xiaomi HyperOS", "Material Design")
private const val UI_STYLE_MIUIX = 0
private const val UI_STYLE_MATERIAL = 1
private const val KEY_UI_STYLE = "ui_style"
private const val UI_STYLE_VALUE_MIUIX = "miuix"
private const val UI_STYLE_VALUE_MATERIAL = "material"

// ---------------- 颜色相关持久化键 ----------------
internal const val KEY_COLOR_MODE = "ui_color_mode"
internal const val KEY_MONET_ENABLED = "ui_color_monet_enabled"
internal const val KEY_KEY_COLOR = "ui_color_key_color"
internal const val KEY_PALETTE_STYLE = "ui_color_palette_style"
internal const val KEY_COLOR_SPEC = "ui_color_spec"

// ---------------- 深色模式 ----------------
internal val DARK_MODE_OPTIONS = listOf("跟随系统设置", "浅色", "深色", "纯黑")
internal const val DARK_MODE_DEFAULT = 0

// ---------------- 强调色 ----------------
internal data class KeyColorOption(val label: String, val argb: Int)

internal val KEY_COLOR_OPTIONS: List<KeyColorOption> = listOf(
    KeyColorOption("默认", 0),
    KeyColorOption("红", 0xFFF44336.toInt()),
    KeyColorOption("粉", 0xFFE91E63.toInt()),
    KeyColorOption("紫", 0xFF9C27B0.toInt()),
    KeyColorOption("深紫", 0xFF673AB7.toInt()),
    KeyColorOption("靛蓝", 0xFF3F51B5.toInt()),
    KeyColorOption("蓝", 0xFF2196F3.toInt()),
    KeyColorOption("青", 0xFF00BCD4.toInt()),
    KeyColorOption("蓝绿", 0xFF009688.toInt()),
    KeyColorOption("绿", 0xFF4FAF50.toInt()),
    KeyColorOption("黄", 0xFFFFEB3B.toInt()),
    KeyColorOption("琥珀", 0xFFFFC107.toInt()),
    KeyColorOption("橙", 0xFFFF9800.toInt()),
    KeyColorOption("棕", 0xFF795548.toInt()),
    KeyColorOption("灰蓝", 0xFF607D8F.toInt()),
    KeyColorOption("浅粉", 0xFFFF9CA8.toInt()),
)

// ---------------- 色彩风格 ----------------
internal val PALETTE_STYLE_NAMES = listOf(
    "TonalSpot",
    "Vibrant",
    "Expressive",
    "Neutral",
    "Fidelity",
    "Content",
    "Monochrome",
)
internal const val PALETTE_STYLE_DEFAULT = 0

// ---------------- 色彩标准 ----------------
internal val COLOR_SPEC_NAMES = listOf("Spec2021", "Spec2025")
internal const val COLOR_SPEC_DEFAULT = 0

// ---------------- 新标签页相关 ----------------
private val BANNER_CONTENT_OPTIONS = listOf(
    "关闭",
    "图片",
    "时间",
    "天气",
    "问候语",
)

/** 「图片」横幅在 [BANNER_CONTENT_OPTIONS] 中的下标。 */
private const val BANNER_CONTENT_IMAGE_INDEX = 1

private val BACKGROUND_CONTENT_OPTIONS = listOf(
    "默认",
    "必应壁纸",
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

private const val BACKGROUND_CONTENT_DIVIDER_AFTER_INDEX = 2
private const val BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX = 2
private const val BACKGROUND_CONTENT_BING_INDEX = 1

private val CONTROL_COLOR_OPTIONS = listOf(
    "跟随软件设置",
    "跟随背景调整",
    "黑色",
    "白色",
)
private const val CONTROL_COLOR_DEFAULT = 0
private const val CONTROL_COLOR_DIVIDER_AFTER_INDEX = 1
private const val CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX = 1

private val MATERIAL_COLOR_OPTIONS = listOf("跟随软件设置", "浅色", "深色")
private const val MATERIAL_COLOR_DEFAULT = 0

private val MATERIAL_STYLE_OPTIONS = listOf("普通材质", "高级材质", "轻透磨砂")
private const val KEY_MATERIAL_STYLE = "ui_material_style"

private val TAB_TITLES = listOf("通用", "新标签页")

private const val KEY_BANNER_CONTENT = "home_banner_content"
private const val KEY_BACKGROUND_CONTENT = "home_background_content"
private const val KEY_SHOW_QUICK_URLS = "home_show_quick_urls"
private const val KEY_CONTROL_COLOR_MODE = "ui_control_color_mode"
private const val KEY_MATERIAL_COLOR_MODE = "ui_material_color_mode"
private const val KEY_BACKGROUND_VIGNETTE = "home_background_vignette"
private const val KEY_CONTROL_COLOR_SUGGEST_DISMISSED = "control_color_suggest_dismissed"

// ============================================================
// 共享状态 / 动作
// ============================================================

@Immutable
private data class LayoutGeneralState(
    val uiStyleIndex: Int,
    val darkModeIndex: Int,
    val monetEnabled: Boolean,
    val keyColorIndex: Int,
    val paletteStyleIndex: Int,
    val colorSpecIndex: Int,
    val progressiveTitleBlur: Boolean,
)

@Immutable
private data class LayoutNewTabState(
    val bannerContentIndex: Int,
    val showQuickUrls: Boolean,
    val controlColorIndex: Int,
    val materialColorIndex: Int,
    val materialStyleIndex: Int,
    val backgroundContentIndex: Int,
    val hasCustomImage: Boolean,
    val hasCustomBanner: Boolean,
    val backgroundVignetteEnabled: Boolean,
)

@Immutable
private data class LayoutActions(
    val onBack: () -> Unit,
    val onUiStyleChange: (Int) -> Unit,
    val onProgressiveTitleBlurChange: (Boolean) -> Unit,
    val onDarkModeChange: (Int) -> Unit,
    val onMonetChange: (Boolean) -> Unit,
    val onKeyColorChange: (Int) -> Unit,
    val onPaletteStyleChange: (Int) -> Unit,
    val onColorSpecChange: (Int) -> Unit,
    val onBannerContentChange: (Int) -> Unit,
    val onShowQuickUrlsChange: (Boolean) -> Unit,
    val onControlColorChange: (Int) -> Unit,
    val onMaterialColorChange: (Int) -> Unit,
    val onMaterialStyleChange: (Int) -> Unit,
    val onBackgroundContentChange: (Int) -> Unit,
    val onBackgroundVignetteChange: (Boolean) -> Unit,
    val onPickWallpaper: () -> Unit,
    val onPickBanner: () -> Unit,
    val onResetBanner: () -> Unit,
    val onNavigateToSearchSettings: () -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsLayoutPage(
    onBack: () -> Unit,
    onOpenCrop: () -> Unit,
    onNavigateToSearchSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val initialTabFromArgs = remember {
        SettingsLayoutNavArgs.initialTab.also { SettingsLayoutNavArgs.initialTab = 0 }
    }
    var selectedTab by remember { mutableIntStateOf(initialTabFromArgs) }

    // ---------------- 通用页：界面风格 ----------------
    var uiStyleIndex by remember {
        mutableIntStateOf(
            if ((prefs.getString(KEY_UI_STYLE, UI_STYLE_VALUE_MIUIX)
                    ?: UI_STYLE_VALUE_MIUIX) == UI_STYLE_VALUE_MATERIAL
            ) UI_STYLE_MATERIAL else UI_STYLE_MIUIX
        )
    }

    // ★ 渐进模糊标题栏开关
    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true))
    }

    // ---------------- 通用页：颜色 ----------------
    var darkModeIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_COLOR_MODE, DARK_MODE_DEFAULT)
                .coerceIn(0, DARK_MODE_OPTIONS.lastIndex)
        )
    }
    var monetEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_MONET_ENABLED, false))
    }
    var keyColorIndex by remember {
        mutableIntStateOf(
            KEY_COLOR_OPTIONS.indexOfFirst {
                it.argb == prefs.getInt(KEY_KEY_COLOR, 0)
            }.takeIf { it >= 0 } ?: 0
        )
    }
    var paletteStyleIndex by remember {
        mutableIntStateOf(
            PALETTE_STYLE_NAMES.indexOf(
                prefs.getString(KEY_PALETTE_STYLE, PALETTE_STYLE_NAMES[PALETTE_STYLE_DEFAULT])
            ).takeIf { it >= 0 } ?: PALETTE_STYLE_DEFAULT
        )
    }
    var colorSpecIndex by remember {
        mutableIntStateOf(
            COLOR_SPEC_NAMES.indexOf(
                prefs.getString(KEY_COLOR_SPEC, COLOR_SPEC_NAMES[COLOR_SPEC_DEFAULT])
            ).takeIf { it >= 0 } ?: COLOR_SPEC_DEFAULT
        )
    }

    // ---------------- 新标签页 ----------------
    var bannerContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BANNER_CONTENT, 2).coerceIn(0, BANNER_CONTENT_OPTIONS.lastIndex)
        )
    }
    var showQuickUrls by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_QUICK_URLS, true))
    }
    var controlColorIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_CONTROL_COLOR_MODE, CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX)
                .coerceIn(0, CONTROL_COLOR_OPTIONS.lastIndex)
        )
    }
    var materialColorIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_MATERIAL_COLOR_MODE, MATERIAL_COLOR_DEFAULT)
                .coerceIn(0, MATERIAL_COLOR_OPTIONS.lastIndex)
        )
    }
    var materialStyleIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_MATERIAL_STYLE, 2).coerceIn(0, MATERIAL_STYLE_OPTIONS.lastIndex)
        )
    }
    var backgroundContentIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_BACKGROUND_CONTENT, BACKGROUND_CONTENT_BING_INDEX)
                .coerceIn(0, BACKGROUND_CONTENT_OPTIONS.lastIndex)
        )
    }
    var hasCustomImage by remember { mutableStateOf(hasCustomWallpaper(context)) }
    var hasCustomBanner by remember { mutableStateOf(hasCustomBannerImage(context)) }
    var backgroundVignetteEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BACKGROUND_VIGNETTE, true))
    }

    // ---------------- 弹窗 ----------------
    var showControlColorSuggestDialog by remember { mutableStateOf(false) }
    var noMoreSuggest by remember { mutableStateOf(false) }

    fun checkAndShowControlColorSuggest() {
        val dismissed = prefs.getBoolean(KEY_CONTROL_COLOR_SUGGEST_DISMISSED, false)
        val currentColor = prefs.getInt(KEY_CONTROL_COLOR_MODE, CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX)
        if (!dismissed && currentColor != CONTROL_COLOR_FOLLOW_BACKGROUND_INDEX) {
            scope.launch {
                kotlinx.coroutines.delay(400)
                noMoreSuggest = false
                showControlColorSuggestDialog = true
            }
        }
    }

    val wallpaperSavedPending = WallpaperSavedEventStore.pending
    LaunchedEffect(wallpaperSavedPending) {
        if (!wallpaperSavedPending) return@LaunchedEffect
        checkAndShowControlColorSuggest()
        WallpaperSavedEventStore.pending = false
    }

    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
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
                KEY_HOME_CUSTOM_BANNER_PATH -> {
                    hasCustomBanner = hasCustomBannerImage(context)
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

    // ★ 自定义横幅：直接保存为横幅图片（不需要裁剪，页面按 16:9 裁切显示）
    val pickBannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { saveCustomBanner(context, uri) }
                if (ok) {
                    hasCustomBanner = true
                    Toast.makeText(context, "已设置自定义图片横幅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "无法读取图片", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ---------------- 组装 state / actions ----------------
    val generalState = LayoutGeneralState(
        uiStyleIndex = uiStyleIndex,
        darkModeIndex = darkModeIndex,
        monetEnabled = monetEnabled,
        keyColorIndex = keyColorIndex,
        paletteStyleIndex = paletteStyleIndex,
        colorSpecIndex = colorSpecIndex,
        progressiveTitleBlur = progressiveTitleBlur,
    )

    val newTabState = LayoutNewTabState(
        bannerContentIndex = bannerContentIndex,
        showQuickUrls = showQuickUrls,
        controlColorIndex = controlColorIndex,
        materialColorIndex = materialColorIndex,
        materialStyleIndex = materialStyleIndex,
        backgroundContentIndex = backgroundContentIndex,
        hasCustomImage = hasCustomImage,
        hasCustomBanner = hasCustomBanner,
        backgroundVignetteEnabled = backgroundVignetteEnabled,
    )

    val actions = LayoutActions(
        onBack = onBack,
        onUiStyleChange = { index ->
            uiStyleIndex = index
            val value = if (index == UI_STYLE_MATERIAL) {
                UI_STYLE_VALUE_MATERIAL
            } else {
                UI_STYLE_VALUE_MIUIX
            }
            prefs.edit().putString(KEY_UI_STYLE, value).apply()
        },
        onProgressiveTitleBlurChange = { enabled ->
            progressiveTitleBlur = enabled
            prefs.edit().putBoolean(KEY_PROGRESSIVE_TITLE_BLUR, enabled).apply()
        },
        onDarkModeChange = { index ->
            darkModeIndex = index
            prefs.edit().putInt(KEY_COLOR_MODE, index).apply()
        },
        onMonetChange = { enabled ->
            monetEnabled = enabled
            prefs.edit().putBoolean(KEY_MONET_ENABLED, enabled).apply()
        },
        onKeyColorChange = { index ->
            keyColorIndex = index
            prefs.edit().putInt(KEY_KEY_COLOR, KEY_COLOR_OPTIONS[index].argb).apply()
        },
        onPaletteStyleChange = { index ->
            paletteStyleIndex = index
            prefs.edit().putString(KEY_PALETTE_STYLE, PALETTE_STYLE_NAMES[index]).apply()
        },
        onColorSpecChange = { index ->
            colorSpecIndex = index
            prefs.edit().putString(KEY_COLOR_SPEC, COLOR_SPEC_NAMES[index]).apply()
        },
        onBannerContentChange = { index ->
            bannerContentIndex = index
            prefs.edit().putInt(KEY_BANNER_CONTENT, index).apply()
        },
        onShowQuickUrlsChange = { enabled ->
            showQuickUrls = enabled
            prefs.edit().putBoolean(KEY_SHOW_QUICK_URLS, enabled).apply()
        },
        onControlColorChange = { index ->
            controlColorIndex = index
            prefs.edit().putInt(KEY_CONTROL_COLOR_MODE, index).apply()
        },
        onMaterialColorChange = { index ->
            materialColorIndex = index
            prefs.edit().putInt(KEY_MATERIAL_COLOR_MODE, index).apply()
        },
        onMaterialStyleChange = { index ->
            materialStyleIndex = index
            prefs.edit().putInt(KEY_MATERIAL_STYLE, index).apply()
        },
        onBackgroundContentChange = { index ->
            if (index == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX) {
                if (hasCustomImage) {
                    backgroundContentIndex = index
                    prefs.edit().putInt(KEY_BACKGROUND_CONTENT, index).apply()
                    checkAndShowControlColorSuggest() // 切换已有自定义图片时触发
                } else {
                    pickWallpaperLauncher.launch("image/*")
                }
            } else {
                backgroundContentIndex = index
                prefs.edit().putInt(KEY_BACKGROUND_CONTENT, index).apply()
                if (index == BACKGROUND_CONTENT_BING_INDEX) {
                    checkAndShowControlColorSuggest() // 切换必应壁纸时触发
                }
            }
        },
        onBackgroundVignetteChange = { enabled ->
            backgroundVignetteEnabled = enabled
            prefs.edit().putBoolean(KEY_BACKGROUND_VIGNETTE, enabled).apply()
        },
        onPickWallpaper = { pickWallpaperLauncher.launch("image/*") },
        onPickBanner = { pickBannerLauncher.launch("image/*") },
        onResetBanner = {
            scope.launch {
                withContext(Dispatchers.IO) { clearCustomBanner(context) }
                hasCustomBanner = false
                Toast.makeText(context, "已重置图片横幅", Toast.LENGTH_SHORT).show()
            }
        },
        onNavigateToSearchSettings = onNavigateToSearchSettings,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsLayoutMiuix(
            generalState = generalState,
            newTabState = newTabState,
            actions = actions,
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
        )
        UiMode.Material -> SettingsLayoutMaterial(
            generalState = generalState,
            newTabState = newTabState,
            actions = actions,
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
        )
    }

    // ---------------- 控件颜色建议弹窗 ----------------
        val dialogNavOwner = rememberNavigationEventDispatcherOwner()
        CompositionLocalProvider(
            LocalNavigationEventDispatcherOwner provides dialogNavOwner
        ) {
            when (LocalUiMode.current) {
                UiMode.Miuix -> {
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
                                    Text("确定")
                                }
                            }
                        }
                    }
                }

                UiMode.Material -> {
                    if (showControlColorSuggestDialog) {
                    M3AlertDialog(
                        onDismissRequest = {
                            showControlColorSuggestDialog = false
                            noMoreSuggest = false
                        },
                        title = { M3Text("更改控件颜色") },
                        text = {
                            Column {
                                M3Text("是否设置为“跟随背景调整”？")
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { noMoreSuggest = !noMoreSuggest },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    androidx.compose.material3.Checkbox(
                                        checked = noMoreSuggest,
                                        onCheckedChange = { noMoreSuggest = it },
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    M3Text("不再弹出此提示")
                                }
                            }
                        },
                        confirmButton = {
                            M3TextButton(
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
                            ) { M3Text("确定") }
                        },
                        dismissButton = {
                            M3TextButton(
                                onClick = {
                                    if (noMoreSuggest) {
                                        prefs.edit()
                                            .putBoolean(KEY_CONTROL_COLOR_SUGGEST_DISMISSED, true)
                                            .apply()
                                    }
                                    showControlColorSuggestDialog = false
                                    noMoreSuggest = false
                                },
                            ) { M3Text("取消") }
                        },
                    )
                
                    }}
            }
        }
    }

// ============================================================
// MIUIX 版实现
// ============================================================

@Composable
private fun SettingsLayoutMiuix(
    generalState: LayoutGeneralState,
    newTabState: LayoutNewTabState,
    actions: LayoutActions,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var materialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "ui_material_style") {
                materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
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

    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { TAB_TITLES.size },
    )

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.springAnimateToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            onTabChange(pagerState.currentPage)
        }
    }

    val listState0 = rememberLazyListState()
    val listState1 = rememberLazyListState()

    val currentListState = if (pagerState.currentPage == 0) listState0 else listState1
    val isAtTop by remember(currentListState) {
        derivedStateOf {
            currentListState.firstVisibleItemIndex == 0 &&
                    currentListState.firstVisibleItemScrollOffset == 0 &&
                    !currentListState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !generalState.progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = SmoothEase),
        label = "settingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (generalState.progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    val showAdvancedColorOptions = generalState.keyColorIndex != 0

    val uiStyleEntries = remember(generalState.uiStyleIndex) {
        listOf(
            DropdownEntry(
                items = UI_STYLE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = generalState.uiStyleIndex == index,
                        onClick = { actions.onUiStyleChange(index) },
                    )
                },
            ),
        )
    }

    val darkModeEntries = remember(generalState.darkModeIndex) {
        listOf(
            DropdownEntry(
                items = DARK_MODE_OPTIONS.take(3).mapIndexed { index, label ->
                    val selected = if (generalState.darkModeIndex >= 3) {
                        index == 2
                    } else {
                        generalState.darkModeIndex == index
                    }
                    DropdownItem(
                        text = label,
                        selected = selected,
                        onClick = { actions.onDarkModeChange(index) },
                    )
                },
            ),
        )
    }

    val keyColorEntries = remember(generalState.keyColorIndex) {
        listOf(
            DropdownEntry(
                items = KEY_COLOR_OPTIONS.mapIndexed { index, option ->
                    DropdownItem(
                        text = option.label,
                        selected = generalState.keyColorIndex == index,
                        onClick = { actions.onKeyColorChange(index) },
                    )
                },
            ),
        )
    }

    val paletteStyleEntries = remember(generalState.paletteStyleIndex) {
        listOf(
            DropdownEntry(
                items = PALETTE_STYLE_NAMES.mapIndexed { index, name ->
                    DropdownItem(
                        text = name,
                        selected = generalState.paletteStyleIndex == index,
                        onClick = { actions.onPaletteStyleChange(index) },
                    )
                },
            ),
        )
    }

    val colorSpecEntries = remember(generalState.colorSpecIndex) {
        listOf(
            DropdownEntry(
                items = COLOR_SPEC_NAMES.mapIndexed { index, name ->
                    DropdownItem(
                        text = name,
                        selected = generalState.colorSpecIndex == index,
                        onClick = { actions.onColorSpecChange(index) },
                    )
                },
            ),
        )
    }

    val bannerContentEntries = remember(newTabState.bannerContentIndex) {
        listOf(
            DropdownEntry(
                items = BANNER_CONTENT_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = newTabState.bannerContentIndex == index,
                        onClick = { actions.onBannerContentChange(index) },
                    )
                },
            ),
        )
    }

    val controlColorEntries = remember(newTabState.controlColorIndex) {
        listOf(
            DropdownEntry(
                items = CONTROL_COLOR_OPTIONS
                    .take(CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        DropdownItem(
                            text = label,
                            selected = newTabState.controlColorIndex == index,
                            onClick = { actions.onControlColorChange(index) },
                        )
                    },
            ),
            DropdownEntry(
                items = CONTROL_COLOR_OPTIONS
                    .drop(CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1)
                    .mapIndexed { index, label ->
                        val actualIndex = index + CONTROL_COLOR_DIVIDER_AFTER_INDEX + 1
                        DropdownItem(
                            text = label,
                            selected = newTabState.controlColorIndex == actualIndex,
                            onClick = { actions.onControlColorChange(actualIndex) },
                        )
                    },
            ),
        )
    }

    val materialColorEntries = remember(newTabState.materialColorIndex) {
        listOf(
            DropdownEntry(
                items = MATERIAL_COLOR_OPTIONS.take(1).mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = newTabState.materialColorIndex == index,
                        onClick = { actions.onMaterialColorChange(index) },
                    )
                },
            ),
            DropdownEntry(
                items = MATERIAL_COLOR_OPTIONS.drop(1).mapIndexed { index, label ->
                    val actualIndex = index + 1
                    DropdownItem(
                        text = label,
                        selected = newTabState.materialColorIndex == actualIndex,
                        onClick = { actions.onMaterialColorChange(actualIndex) },
                    )
                },
            ),
        )
    }

    val materialStyleEntries = remember(newTabState.materialStyleIndex) {
        listOf(
            DropdownEntry(
                items = MATERIAL_STYLE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = newTabState.materialStyleIndex == index,
                        onClick = { actions.onMaterialStyleChange(index) },
                    )
                },
            ),
        )
    }

    val backgroundContentEntries = remember(
        newTabState.backgroundContentIndex, newTabState.hasCustomImage
    ) {
        listOf(
            DropdownEntry(
                items = BACKGROUND_CONTENT_OPTIONS.take(3).mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = newTabState.backgroundContentIndex == index,
                        onClick = { actions.onBackgroundContentChange(index) },
                    )
                }
            ),
            DropdownEntry(
                items = BACKGROUND_CONTENT_OPTIONS.drop(3).mapIndexed { index, label ->
                    val actualIndex = index + 3
                    DropdownItem(
                        text = label,
                        selected = newTabState.backgroundContentIndex == actualIndex,
                        onClick = { actions.onBackgroundContentChange(actualIndex) },
                    )
                }
            )
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
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                    ) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(
                                    state = listState0,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                                        .scrollEndHaptic()
                                        .overScrollVertical(),
                                    contentPadding = PaddingValues(top = contentTopPadding),
                                    overscrollEffect = null,
                                ) {
                                    item(key = "general_content") {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            ThemePreview()

                                            SmallTitle("外观")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                SearchableItem(
                                                    title = "界面风格",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_ui_style",
                                                    tabIndex = 0,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "界面风格",
                                                        entries = uiStyleEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }
                                                SearchableItem(
                                                    title = "材质风格",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_material_style",
                                                    tabIndex = 0,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "材质风格",
                                                        entries = materialStyleEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }
                                                SearchableItem(
                                                    title = "渐进模糊标题栏",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_progressive_title_blur",
                                                    tabIndex = 0,
                                                ) {
                                                    SwitchPreference(
                                                        title = "渐进模糊标题栏",
                                                        checked = generalState.progressiveTitleBlur,
                                                        onCheckedChange = actions.onProgressiveTitleBlurChange,
                                                    )
                                                }
                                            }

                                            SmallTitle("颜色")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                SearchableItem(
                                                    title = "深色模式",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_dark_mode",
                                                    tabIndex = 0,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "深色模式",
                                                        entries = darkModeEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }
                                            }

                                            // ★ 新增：强调色横向滑动卡片集（启用系统壁纸取色时隐藏）
                                            AnimatedVisibility(
                                                visible = !generalState.monetEnabled,
                                                enter = fadeIn(tween(300, easing = SmoothEase)) + expandVertically(
                                                    animationSpec = tween(300, easing = SmoothEase),
                                                    expandFrom = Alignment.Top,
                                                ),
                                                exit = fadeOut(tween(300, easing = SmoothEase)) + shrinkVertically(
                                                    animationSpec = tween(300, easing = SmoothEase),
                                                    shrinkTowards = Alignment.Top,
                                                ),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 12.dp)
                                                ) {
                                                    MiuixColorSwatchRow(
                                                        keyColorIndex = generalState.keyColorIndex,
                                                        onKeyColorChange = actions.onKeyColorChange,
                                                    )
                                                }
                                            }

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                SearchableItem(
                                                    title = "系统壁纸取色",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_monet_enabled",
                                                    tabIndex = 0,
                                                ) {
                                                    SwitchPreference(
                                                        title = "系统壁纸取色",
                                                        summary = "开启后全局使用 Monet 取色",
                                                        checked = generalState.monetEnabled,
                                                        onCheckedChange = actions.onMonetChange,
                                                    )
                                                }

                                                AnimatedVisibility(visible = generalState.monetEnabled) {
                                                    Column {
                                                        SearchableItem(
                                                            title = "强调色",
                                                            pageRoute = "settings_layout",
                                                            pageTitle = "主题与界面",
                                                            itemKey = "settings_layout_key_color",
                                                            tabIndex = 0,
                                                        ) {
                                                            WindowDropdownPreference(
                                                                title = "强调色",
                                                                entries = keyColorEntries,
                                                                collapseOnSelection = true,
                                                            )
                                                        }

                                                        AnimatedVisibility(visible = showAdvancedColorOptions) {
                                                            Column {
                                                                SearchableItem(
                                                                    title = "色彩风格",
                                                                    pageRoute = "settings_layout",
                                                                    pageTitle = "主题与界面",
                                                                    itemKey = "settings_layout_palette_style",
                                                                    tabIndex = 0,
                                                                ) {
                                                                    WindowDropdownPreference(
                                                                        title = "色彩风格",
                                                                        entries = paletteStyleEntries,
                                                                        collapseOnSelection = true,
                                                                    )
                                                                }
                                                                SearchableItem(
                                                                    title = "色彩标准",
                                                                    pageRoute = "settings_layout",
                                                                    pageTitle = "主题与界面",
                                                                    itemKey = "settings_layout_color_spec",
                                                                    tabIndex = 0,
                                                                ) {
                                                                    WindowDropdownPreference(
                                                                        title = "色彩标准",
                                                                        entries = colorSpecEntries,
                                                                        collapseOnSelection = true,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item(key = "general_bottom") {
                                        Spacer(
                                            Modifier.height(
                                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                            )
                                        )
                                    }
                                }

                                SettingsPageAutoScroll(
                                    lazyListState = listState0,
                                    pageRoute = "settings_layout",
                                    contentTopPadding = contentTopPadding,
                                    tabIndex = 0,
                                )
                            }

                            1 -> {
                                LazyColumn(
                                    state = listState1,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                                        .scrollEndHaptic()
                                        .overScrollVertical(),
                                    contentPadding = PaddingValues(top = contentTopPadding),
                                    overscrollEffect = null,
                                ) {
                                    item(key = "newtab_content") {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            BrowserHomePreview()

                                            SmallTitle("主题")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                val showBgDependentOptions =
                                                    newTabState.backgroundContentIndex == BACKGROUND_CONTENT_BING_INDEX ||
                                                            newTabState.backgroundContentIndex == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX

                                                SearchableItem(
                                                    title = "控件颜色",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_control_color",
                                                    tabIndex = 1,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "控件颜色",
                                                        entries = controlColorEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }

                                                SearchableItem(
                                                    title = "自定义背景",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_background_content",
                                                    tabIndex = 1,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "自定义背景",
                                                        entries = backgroundContentEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }

                                                AnimatedVisibility(
                                                    visible = newTabState.backgroundContentIndex ==
                                                            BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX &&
                                                            newTabState.hasCustomImage,
                                                ) {
                                                    SearchableItem(
                                                        title = "更换背景图片",
                                                        pageRoute = "settings_layout",
                                                        pageTitle = "主题与界面",
                                                        itemKey = "settings_layout_reselect_image",
                                                        tabIndex = 1,
                                                    ) {
                                                        ArrowPreference(
                                                            title = "更换背景图片",
                                                            onClick = actions.onPickWallpaper,
                                                        )
                                                    }
                                                }

                                                AnimatedVisibility(visible = showBgDependentOptions) {
                                                    SearchableItem(
                                                        title = "背景暗角滤镜",
                                                        pageRoute = "settings_layout",
                                                        pageTitle = "主题与界面",
                                                        itemKey = "settings_layout_background_vignette",
                                                        tabIndex = 1,
                                                    ) {
                                                        SwitchPreference(
                                                            title = "背景暗角滤镜",
                                                            checked = newTabState.backgroundVignetteEnabled,
                                                            onCheckedChange = actions.onBackgroundVignetteChange,
                                                        )
                                                    }
                                                }

                                                AnimatedVisibility(visible = showBgDependentOptions) {
                                                    SearchableItem(
                                                        title = "页面材质颜色",
                                                        pageRoute = "settings_layout",
                                                        pageTitle = "主题与界面",
                                                        itemKey = "settings_layout_page_material_color",
                                                        tabIndex = 1,
                                                    ) {
                                                        WindowDropdownPreference(
                                                            title = "页面材质颜色",
                                                            summary = "仅新标签页生效",
                                                            entries = materialColorEntries,
                                                            collapseOnSelection = true,
                                                        )
                                                    }
                                                }
                                            }

                                            SmallTitle("内容")

                                            Card(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 12.dp),
                                            ) {
                                                SearchableItem(
                                                    title = "横幅内容",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_banner_content",
                                                    tabIndex = 1,
                                                ) {
                                                    WindowDropdownPreference(
                                                        title = "横幅内容",
                                                        entries = bannerContentEntries,
                                                        collapseOnSelection = true,
                                                    )
                                                }

                                                // ★ 仅在「图片」横幅下显示自定义横幅相关项
                                                AnimatedVisibility(
                                                    visible = newTabState.bannerContentIndex == BANNER_CONTENT_IMAGE_INDEX,
                                                ) {
                                                    Column {
                                                        SearchableItem(
                                                            title = "自定义图片横幅",
                                                            pageRoute = "settings_layout",
                                                            pageTitle = "主题与界面",
                                                            itemKey = "settings_layout_custom_banner",
                                                            tabIndex = 1,
                                                        ) {
                                                            ArrowPreference(
                                                                title = "自定义图片横幅",
                                                                onClick = actions.onPickBanner,
                                                            )
                                                        }
                                                        AnimatedVisibility(visible = newTabState.hasCustomBanner) {
                                                            SearchableItem(
                                                                title = "重置图片横幅",
                                                                pageRoute = "settings_layout",
                                                                pageTitle = "主题与界面",
                                                                itemKey = "settings_layout_reset_banner",
                                                                tabIndex = 1,
                                                            ) {
                                                                ArrowPreference(
                                                                    title = "重置图片横幅",
                                                                    onClick = actions.onResetBanner,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                SearchableItem(
                                                    title = "快捷网址",
                                                    pageRoute = "settings_layout",
                                                    pageTitle = "主题与界面",
                                                    itemKey = "settings_layout_show_quick_urls",
                                                    tabIndex = 1,
                                                ) {
                                                    SwitchPreference(
                                                        title = "快捷网址",
                                                        checked = newTabState.showQuickUrls,
                                                        onCheckedChange = actions.onShowQuickUrlsChange,
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
                                                        .clickable { actions.onNavigateToSearchSettings() }
                                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = "显示/隐藏搜索历史",
                                                        color = MiuixTheme.colorScheme.primary,
                                                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item(key = "newtab_bottom") {
                                        Spacer(
                                            Modifier.height(
                                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                            )
                                        )
                                    }
                                }

                                SettingsPageAutoScroll(
                                    lazyListState = listState1,
                                    pageRoute = "settings_layout",
                                    contentTopPadding = contentTopPadding,
                                    tabIndex = 1,
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (generalState.progressiveTitleBlur) {
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = "主题与界面",
                        largeTitle = "主题与界面",
                        scrollBehavior = scrollBehavior,
                        color = Color.Transparent,
                        defaultWindowInsetsPadding = false,
                        navigationIcon = {
                            SettingsBackButton(
                                onBack = actions.onBack,
                                progressiveTitleBlur = generalState.progressiveTitleBlur,
                                backCardAlpha = backCardAlpha,
                                backCardMaterialModifier = backCardMaterialModifier,
                                iconTint = MiuixTheme.colorScheme.onSurface,
                            )
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
                            onTabSelected = { onTabChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (blurSupported && backdrop != null && materialStyle != 0) {
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
    }
}

// ============================================================
// MIUIX —— 强调色横向色卡
// ============================================================

@Composable
private fun MiuixColorSwatchRow(
    keyColorIndex: Int,
    onKeyColorChange: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val primaryColor = MiuixTheme.colorScheme.primary
    // 使用统一的圆角形状，与普通卡片视觉保持一致
    val outerShape = RoundedCornerShape(16.dp)
    val innerShape = RoundedCornerShape(12.dp)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(KEY_COLOR_OPTIONS) { index, option ->
            val isSelected = keyColorIndex == index
            // 第一位（默认）展示固定的 MIUIX 默认蓝色
            val swatchColor = if (option.argb == 0) {
                Color(0xFF3482FF)
            } else {
                Color(option.argb)
            }

            // ★ 新增：边框粗细的过渡动画
            val animatedBorderWidth by animateDpAsState(
                targetValue = if (isSelected) 3.dp else 1.dp,
                animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                label = "swatchBorderWidth_$index"
            )

            // ★ 新增：边框颜色的过渡动画
            val animatedBorderColor by animateColorAsState(
                targetValue = if (isSelected) primaryColor else Color.Transparent,
                animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                label = "swatchBorderColor_$index"
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(outerShape)
                    .border(
                        width = animatedBorderWidth,
                        color = animatedBorderColor,
                        shape = outerShape
                    )
                    .padding(5.dp)
                    .clip(innerShape)
                    .background(swatchColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onKeyColorChange(index)
                    }
            )
        }
    }
}

// ============================================================
// Material 3 版实现
// ============================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsLayoutMaterial(
    generalState: LayoutGeneralState,
    newTabState: LayoutNewTabState,
    actions: LayoutActions,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

    val pageBg = M3MaterialTheme.colorScheme.surface
    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { TAB_TITLES.size },
    )

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            onTabChange(pagerState.currentPage)
        }
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    M3TopAppBar(
                        title = { M3Text("主题与界面") },
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
                    M3PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = pageBg,
                    ) {
                        TAB_TITLES.forEachIndexed { index, title ->
                            M3Tab(
                                selected = selectedTab == index,
                                onClick = { onTabChange(index) },
                                text = { M3Text(title) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                beyondViewportPageCount = 1,
            ) { page ->
                when (page) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            item(key = "theme_preview_material") {
                                ThemePreviewMaterial()
                            }

                            item(key = "appearance_header") {
                                LayoutSectionHeader("外观")
                            }

                            item(key = "ui_style_card") {
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
                                                    title = "界面风格",
                                                    summary = null,
                                                    options = UI_STYLE_OPTIONS,
                                                    selectedIndex = generalState.uiStyleIndex,
                                                    onSelected = actions.onUiStyleChange,
                                                )
                                            }
                                        )
                                    )
                                }
                            }

                            item(key = "color_header") {
                                LayoutSectionHeader("颜色")
                            }

                            // ★ 深色模式：横向图标按钮（无卡片、无标题）
                            item(key = "dark_mode_row") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    M3ColorModeToggleRow(
                                        options = DARK_MODE_OPTIONS,
                                        selectedIndex = generalState.darkModeIndex,
                                        onSelected = actions.onDarkModeChange,
                                    )
                                }
                            }

                            // ★ 系统壁纸取色（Material 下强制开启、不可关闭）
                            item(key = "monet_card") {
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
                                                    title = "系统壁纸取色",
                                                    summary = "Material 主题下默认开启，无法关闭",
                                                    checked = true,
                                                    onCheckedChange = { /* 强制开启 */ },
                                                    enabled = false,
                                                )
                                            }
                                        )
                                    )
                                }
                            }

                            item(key = "key_color_swatches") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    M3ColorSwatchRow(
                                        keyColorIndex = generalState.keyColorIndex,
                                        onKeyColorChange = actions.onKeyColorChange,
                                    )
                                }
                            }

                            // ★ 色彩风格 + 色彩标准（常显）
                            item(key = "advanced_color_card") {
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
                                                    title = "色彩风格",
                                                    summary = null,
                                                    options = PALETTE_STYLE_NAMES,
                                                    selectedIndex = generalState.paletteStyleIndex,
                                                    onSelected = actions.onPaletteStyleChange,
                                                )
                                            },
                                            {
                                                M3SegmentDropdownItem(
                                                    title = "色彩标准",
                                                    summary = null,
                                                    options = COLOR_SPEC_NAMES,
                                                    selectedIndex = generalState.colorSpecIndex,
                                                    onSelected = actions.onColorSpecChange,
                                                )
                                            },
                                        )
                                    )
                                }
                            }

                            item(key = "general_bottom_material") {
                                Spacer(
                                    Modifier.height(
                                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                    )
                                )
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            item(key = "browser_preview_material") {
                                BrowserHomePreviewMaterial()
                            }

                            item(key = "theme_header_material") {
                                LayoutSectionHeader("主题")
                            }

                            item(key = "theme_items_material") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    val showBgDependentOptions =
                                        newTabState.backgroundContentIndex == BACKGROUND_CONTENT_BING_INDEX ||
                                                newTabState.backgroundContentIndex == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX

                                    val themeItems: List<@Composable () -> Unit> = buildList {
                                        add {
                                            M3SegmentDropdownItem(
                                                title = "控件颜色",
                                                summary = null,
                                                options = CONTROL_COLOR_OPTIONS,
                                                selectedIndex = newTabState.controlColorIndex,
                                                onSelected = actions.onControlColorChange,
                                            )
                                        }
                                        add {
                                            M3SegmentDropdownItem(
                                                title = "自定义背景",
                                                summary = null,
                                                options = BACKGROUND_CONTENT_OPTIONS,
                                                selectedIndex = newTabState.backgroundContentIndex,
                                                onSelected = actions.onBackgroundContentChange,
                                            )
                                        }
                                        if (newTabState.backgroundContentIndex ==
                                            BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX &&
                                            newTabState.hasCustomImage
                                        ) {
                                            add {
                                                M3SegmentArrowItem(
                                                    title = "更换背景图片",
                                                    onClick = actions.onPickWallpaper,
                                                )
                                            }
                                        }
                                        if (showBgDependentOptions) {
                                            add {
                                                M3SegmentSwitchItem(
                                                    title = "背景暗角滤镜",
                                                    summary = null,
                                                    checked = newTabState.backgroundVignetteEnabled,
                                                    onCheckedChange = actions.onBackgroundVignetteChange,
                                                )
                                            }
                                            // ★ Material 主题下隐藏「材质风格」「材质颜色」
                                        }
                                    }
                                    MoteSegmentedColumn(content = themeItems)
                                }
                            }

                            item(key = "content_header_material") {
                                LayoutSectionHeader("内容")
                            }

                            item(key = "content_items_material") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    MoteSegmentedColumn(
                                        content = buildList<@Composable () -> Unit> {
                                            add {
                                                M3SegmentDropdownItem(
                                                    title = "横幅内容",
                                                    summary = null,
                                                    options = BANNER_CONTENT_OPTIONS,
                                                    selectedIndex = newTabState.bannerContentIndex,
                                                    onSelected = actions.onBannerContentChange,
                                                )
                                            }
                                            // ★ 仅在「图片」横幅下显示自定义横幅相关项
                                            if (newTabState.bannerContentIndex == BANNER_CONTENT_IMAGE_INDEX) {
                                                add {
                                                    M3SegmentArrowItem(
                                                        title = "自定义图片横幅",
                                                        onClick = actions.onPickBanner,
                                                    )
                                                }
                                                if (newTabState.hasCustomBanner) {
                                                    add {
                                                        M3SegmentArrowItem(
                                                            title = "重置图片横幅",
                                                            onClick = actions.onResetBanner,
                                                        )
                                                    }
                                                }
                                            }
                                            add {
                                                M3SegmentSwitchItem(
                                                    title = "快捷网址",
                                                    summary = null,
                                                    checked = newTabState.showQuickUrls,
                                                    onCheckedChange = actions.onShowQuickUrlsChange,
                                                )
                                            }
                                        }
                                    )
                                }
                            }

                            item(key = "find_more_header_material") {
                                LayoutSectionHeader("在查找其它设置吗？")
                            }

                            item(key = "find_more_item_material") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    MoteSegmentedColumn(
                                        content = listOf(
                                            {
                                                M3SegmentItem(
                                                    headline = {
                                                        M3Text(
                                                            text = "显示/隐藏搜索历史",
                                                            color = M3MaterialTheme.colorScheme.primary,
                                                        )
                                                    },
                                                    onClick = {
                                                        actions.onNavigateToSearchSettings()
                                                    },
                                                )
                                            }
                                        )
                                    )
                                }
                            }

                            item(key = "newtab_bottom_material") {
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
            }
        }
    }
}

// ============================================================
// Material —— 深色模式横向图标选择组
// ============================================================

@Composable
private fun M3ColorModeToggleRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val cs = M3MaterialTheme.colorScheme

    // 与 M3SegmentItem / M3SegmentDropdownItem 的卡片色完全一致
    val unifiedCardColor = lerp(
        cs.surfaceContainerLow,
        cs.secondaryContainer,
        0.10f,
    )

    val icons = listOf(
        Icons.Filled.Brightness4,   // 跟随系统
        Icons.Filled.Brightness7,   // 浅色
        Icons.Filled.Brightness3,   // 深色
        Icons.Filled.Brightness1,   // 纯黑
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = selectedIndex == index

            val cornerDp by animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 260, easing = SmoothEase),
                label = "m3ColorModeCorner",
            )
            val bgColor by animateColorAsState(
                targetValue = if (selected) cs.primary else unifiedCardColor,
                animationSpec = tween(durationMillis = 260, easing = SmoothEase),
                label = "m3ColorModeBg",
            )
            val fgColor by animateColorAsState(
                targetValue = if (selected) cs.onPrimary else cs.onSurfaceVariant,
                animationSpec = tween(durationMillis = 260, easing = SmoothEase),
                label = "m3ColorModeFg",
            )

            androidx.compose.material3.Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSelected(index)
                },
                shape = RoundedCornerShape(cornerDp),
                color = bgColor,
                modifier = Modifier
                    .weight(1f)
                    .height(M3_COLOR_MODE_BUTTON_HEIGHT)
                    .semantics { role = Role.RadioButton },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    M3Icon(
                        imageVector = icons.getOrNull(index) ?: icons[0],
                        contentDescription = label,
                        tint = fgColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
// ============================================================
// Material —— 强调色横向色卡
// ============================================================

@Composable
private fun M3ColorSwatchRow(
    keyColorIndex: Int,
    onKeyColorChange: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(KEY_COLOR_OPTIONS) { index, option ->
            M3ColorSwatch(
                argb = option.argb,
                isSelected = keyColorIndex == index,
                onClick = { onKeyColorChange(index) },
            )
        }
    }
}


@Composable
private fun M3ColorSwatch(
    argb: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val cs = M3MaterialTheme.colorScheme
    val context = LocalContext.current

    // ★ 默认卡（argb == 0）：用系统 dynamic 配色（莫奈取色）的 primary，
    //   不走 cs.primary，因为 cs.primary 会被用户选的强调色覆盖。
    val isDark = cs.surface.luminance() < 0.5f
    val systemDynamicPrimary = remember(isDark, context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context).primary
            else dynamicLightColorScheme(context).primary
        } else null
    }

    val swatchColor = if (argb == 0) {
        systemDynamicPrimary ?: cs.primary
    } else {
        Color(argb)
    }

    val upperHalfColor = lerp(swatchColor, Color.White, 0.55f)
    val lowerHalfColor = lerp(swatchColor, Color.White, 0.80f)
    val cardBgColor = lerp(swatchColor, cs.surface, 0.80f).copy(alpha = 0.5f)

    androidx.compose.material3.Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        modifier = Modifier.size(72.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    color = upperHalfColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                )
                drawArc(
                    color = lowerHalfColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                )
            }

            AnimatedVisibility(
                visible = !isSelected,
                enter = fadeIn(tween(durationMillis = 180, easing = SmoothEase)) +
                        androidx.compose.animation.scaleIn(
                            animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                            initialScale = 0.6f,
                        ),
                exit = fadeOut(tween(durationMillis = 140, easing = SmoothEase)) +
                        androidx.compose.animation.scaleOut(
                            animationSpec = tween(durationMillis = 180, easing = SmoothEase),
                            targetScale = 0.6f,
                        ),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(swatchColor),
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(durationMillis = 200, easing = SmoothEase)) +
                        androidx.compose.animation.scaleIn(
                            animationSpec = tween(durationMillis = 240, easing = SmoothEase),
                            initialScale = 0.6f,
                        ),
                exit = fadeOut(tween(durationMillis = 140, easing = SmoothEase)) +
                        androidx.compose.animation.scaleOut(
                            animationSpec = tween(durationMillis = 180, easing = SmoothEase),
                            targetScale = 0.6f,
                        ),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, cs.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(cs.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        M3Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = cs.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 新标签页预览（MIUIX 版）
// ============================================================

@Composable
internal fun BrowserHomePreview() {
    val colorScheme = MiuixTheme.colorScheme
    val primary = colorScheme.primary
    val surface = colorScheme.surface
    val surfaceContainer = colorScheme.surfaceContainer
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariantSummary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(304.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceContainer)
                .border(
                    width = 1.dp,
                    color = onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(44.dp))

                Text(
                    text = "12:34",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                    color = onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "2025年1月1日 周三",
                    fontSize = 8.sp,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .width(160.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Search,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "搜索",
                        fontSize = 10.sp,
                        color = onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = MiuixIcons.Scan,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }

                Spacer(Modifier.height(22.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            repeat(4) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(primary.copy(alpha = 0.55f)),
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(onSurfaceVariant.copy(alpha = 0.4f)),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(surface),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(onSurface.copy(alpha = 0.5f)),
                    )
                }
            }
        }
    }
}

// ============================================================
// 主题色预览（MIUIX 版）
// ============================================================

@Composable
internal fun ThemePreview() {
    val colorScheme = MiuixTheme.colorScheme
    val primary = colorScheme.primary
    val onPrimary = colorScheme.onPrimary
    val secondaryContainer = colorScheme.secondaryContainer
    val surface = colorScheme.surface
    val surfaceContainer = colorScheme.surfaceContainer
    val surfaceContainerHigh = colorScheme.surfaceContainerHigh
    val onSurface = colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(304.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceContainer)
                .border(
                    width = 1.dp,
                    color = onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MiuixPreviewSwatchDot(primary)
                    MiuixPreviewSwatchDot(secondaryContainer)
                    MiuixPreviewSwatchDot(surfaceContainerHigh)
                    MiuixPreviewSwatchDot(surface)
                    MiuixPreviewSwatchDot(onSurface)
                }

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(primary),
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(secondaryContainer),
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(primary),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(onPrimary),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(onSurface.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(surface),
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = 0.6f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )

                Spacer(Modifier.height(12.dp))

                Slider(
                    value = 0.5f,
                    onValueChange = {},
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(surfaceContainerHigh),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(onSurface.copy(alpha = 0.7f)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(onSurface.copy(alpha = 0.35f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixPreviewSwatchDot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 1.dp,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = CircleShape,
            ),
    )
}

// ============================================================
// 主题色预览（Material 版）
// ★ 上下左右统一 14dp，项与项之间统一 14dp
// ============================================================

@Composable
private fun ThemePreviewMaterial() {
    val cs = M3MaterialTheme.colorScheme
    val primary = cs.primary
    val onPrimary = cs.onPrimary
    val secondaryContainer = cs.secondaryContainer
    val surface = cs.surface
    val surfaceContainer = cs.surfaceContainer
    val surfaceContainerHigh = cs.surfaceContainerHigh
    val onSurface = cs.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(304.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceContainer)
                .border(
                    width = 1.dp,
                    color = onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(
                    14.dp,
                    Alignment.CenterVertically,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MiuixPreviewSwatchDot(primary)
                    MiuixPreviewSwatchDot(secondaryContainer)
                    MiuixPreviewSwatchDot(surfaceContainerHigh)
                    MiuixPreviewSwatchDot(surface)
                    MiuixPreviewSwatchDot(onSurface)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(primary),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(secondaryContainer),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(primary),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(onPrimary),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(onSurface.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(surface),
                        )
                    }
                }

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )

                androidx.compose.material3.Slider(
                    value = 0.5f,
                    onValueChange = {},
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(surfaceContainerHigh),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(onSurface.copy(alpha = 0.7f)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(onSurface.copy(alpha = 0.35f)),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 新标签页预览（Material 版）
// ============================================================

@Composable
private fun BrowserHomePreviewMaterial() {
    val cs = M3MaterialTheme.colorScheme
    val primary = cs.primary
    val surface = cs.surface
    val surfaceContainer = cs.surfaceContainer
    val onSurface = cs.onSurface
    val onSurfaceVariant = cs.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(304.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceContainer)
                .border(
                    width = 1.dp,
                    color = onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(44.dp))

                M3Text(
                    text = "12:34",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                    color = onSurface,
                )
                Spacer(Modifier.height(4.dp))
                M3Text(
                    text = "2025年1月1日 周三",
                    fontSize = 8.sp,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .width(160.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    M3Icon(
                        imageVector = MiuixIcons.Search,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    M3Text(
                        text = "搜索",
                        fontSize = 10.sp,
                        color = onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    M3Icon(
                        imageVector = MiuixIcons.Scan,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }

                Spacer(Modifier.height(22.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            repeat(4) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(primary.copy(alpha = 0.55f)),
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(onSurfaceVariant.copy(alpha = 0.4f)),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(surface),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(onSurface.copy(alpha = 0.5f)),
                    )
                }
            }
        }
    }
}

// ============================================================
// Material 通用小件
// ============================================================

@Composable
private fun LayoutSectionHeader(text: String) {
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