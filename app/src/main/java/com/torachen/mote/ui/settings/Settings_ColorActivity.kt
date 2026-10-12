@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// MIUIX 组件
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// MIUIX 模糊
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

// MIUIX 图标
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Search

// MIUIX 工具
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ---------------- 持久化键 ----------------
internal const val KEY_COLOR_MODE = "ui_color_mode"              // 0=跟随系统 1=关闭 2=开启
internal const val KEY_MONET_ENABLED = "ui_color_monet_enabled"  // Boolean
internal const val KEY_KEY_COLOR = "ui_color_key_color"          // Int（0=默认）
internal const val KEY_PALETTE_STYLE = "ui_color_palette_style"  // String
internal const val KEY_COLOR_SPEC = "ui_color_spec"              // String

// ---------------- 深色模式 ----------------
private val DARK_MODE_OPTIONS = listOf("跟随系统设置", "关闭", "开启")
private const val DARK_MODE_DEFAULT = 0

// ---------------- 强调色 ----------------
private data class KeyColorOption(val label: String, val argb: Int)

private val KEY_COLOR_OPTIONS: List<KeyColorOption> = listOf(
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
private val PALETTE_STYLE_NAMES = listOf(
    "TonalSpot",
    "Vibrant",
    "Expressive",
    "Neutral",
    "Fidelity",
    "Content",
    "Monochrome",
)
private const val PALETTE_STYLE_DEFAULT = 0

// ---------------- 色彩标准 ----------------
private val COLOR_SPEC_NAMES = listOf("Spec2021", "Spec2025")
private const val COLOR_SPEC_DEFAULT = 0

// ============================================================
// 颜色设置页
// ============================================================

@Composable
fun SettingsColorPage(
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
    } else null

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

    // ---------------- 状态 ----------------
    var darkModeIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_COLOR_MODE, DARK_MODE_DEFAULT)
                .coerceIn(0, DARK_MODE_OPTIONS.lastIndex)
        )
    }
    var monetEnabled by remember {
        // ★ 默认关闭
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

    // ★ 强调色默认（索引 0）时，隐藏"色彩风格"和"色彩标准"
    val showAdvancedColorOptions = keyColorIndex != 0

    // ---------------- 下拉条目 ----------------
    val darkModeEntries = remember(darkModeIndex) {
        listOf(
            DropdownEntry(
                items = DARK_MODE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = darkModeIndex == index,
                        onClick = {
                            darkModeIndex = index
                            prefs.edit().putInt(KEY_COLOR_MODE, index).apply()
                        },
                    )
                },
            ),
        )
    }

    val keyColorEntries = remember(keyColorIndex) {
        listOf(
            DropdownEntry(
                items = KEY_COLOR_OPTIONS.mapIndexed { index, option ->
                    DropdownItem(
                        text = option.label,
                        selected = keyColorIndex == index,
                        onClick = {
                            keyColorIndex = index
                            prefs.edit().putInt(KEY_KEY_COLOR, option.argb).apply()
                        },
                    )
                },
            ),
        )
    }

    val paletteStyleEntries = remember(paletteStyleIndex) {
        listOf(
            DropdownEntry(
                items = PALETTE_STYLE_NAMES.mapIndexed { index, name ->
                    DropdownItem(
                        text = name,
                        selected = paletteStyleIndex == index,
                        onClick = {
                            paletteStyleIndex = index
                            prefs.edit().putString(KEY_PALETTE_STYLE, name).apply()
                        },
                    )
                },
            ),
        )
    }

    val colorSpecEntries = remember(colorSpecIndex) {
        listOf(
            DropdownEntry(
                items = COLOR_SPEC_NAMES.mapIndexed { index, name ->
                    DropdownItem(
                        text = name,
                        selected = colorSpecIndex == index,
                        onClick = {
                            colorSpecIndex = index
                            prefs.edit().putString(KEY_COLOR_SPEC, name).apply()
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
                        if (blurSupported && backdrop != null)
                            Modifier.layerBackdrop(backdrop) else Modifier
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

                            // ★ 预览画面（X 轴居中）
                            BrowserHomePreview()

                            // 卡片一：深色模式
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                WindowDropdownPreference(
                                    title = "深色模式",
                                    entries = darkModeEntries,
                                    collapseOnSelection = true,
                                )
                            }

                            // 卡片二：壁纸取色 + 强调色 + 色彩风格 + 色彩标准
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "启用系统壁纸取色",
                                    summary = "开启后全局使用 Monet 取色",
                                    checked = monetEnabled,
                                    onCheckedChange = { enabled ->
                                        // ★ 完全对标参考项目：直接同步更新，无延迟
                                        monetEnabled = enabled
                                        prefs.edit()
                                            .putBoolean(KEY_MONET_ENABLED, enabled)
                                            .apply()
                                    },
                                )

                                // ★ 参考代码：AnimatedVisibility(visible = ...) 不传 enter/exit
                                AnimatedVisibility(visible = monetEnabled) {
                                    Column {
                                        WindowDropdownPreference(
                                            title = "强调色",
                                            entries = keyColorEntries,
                                            collapseOnSelection = true,
                                        )

                                        // ★ 强调色选"默认"时隐藏后两项
                                        AnimatedVisibility(visible = showAdvancedColorOptions) {
                                            Column {
                                                WindowDropdownPreference(
                                                    title = "色彩风格",
                                                    entries = paletteStyleEntries,
                                                    collapseOnSelection = true,
                                                )
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

                            // 底部导航栏占位
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
                        } else Modifier.background(surfaceColor)
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) topBarBottomPx = bottomPx
                    }
            ) {
                TopAppBar(
                    title = "颜色",
                    largeTitle = "颜色",
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

// ============================================================
// 新标签页预览画面
// ============================================================

@Composable
private fun BrowserHomePreview() {
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
                .width(200.dp)
                .height(380.dp)
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