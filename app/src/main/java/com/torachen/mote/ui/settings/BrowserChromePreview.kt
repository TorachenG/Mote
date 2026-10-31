package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.RecordingTape
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val PreviewPageTop = Color(0xFF2F5D8A)
private val PreviewPageBottom = Color(0xFFE9DCC8)
private val PreviewPageBackground = Color(0xFFF7F3EC)
private val PreviewPageCard = Color(0xFFFFFFFF)
private val PreviewPageLine = Color(0xFFB9B2A7)

private data class PreviewThemeColors(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
)

@Composable
private fun previewThemeColors(): PreviewThemeColors {
    return if (LocalUiMode.current == UiMode.Material) {
        val c = M3MaterialTheme.colorScheme
        PreviewThemeColors(
            surface = c.surface,
            surfaceContainer = c.surfaceContainer,
            surfaceContainerHigh = c.surfaceContainerHigh,
            onSurface = c.onSurface,
        )
    } else {
        val c = MiuixTheme.colorScheme
        PreviewThemeColors(
            surface = c.surface,
            surfaceContainer = c.surfaceContainer,
            surfaceContainerHigh = c.surfaceContainerHigh,
            onSurface = c.onSurface,
        )
    }
}

private fun previewContrastColor(background: Color): Color =
    if (background.luminance() < 0.5f) Color.White else Color.Black

private fun previewOverlayColor(background: Color): Color =
    if (background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.16f).compositeOver(background)
    } else {
        Color.Black.copy(alpha = 0.07f).compositeOver(background)
    }

/**
 * 浏览器页面的实时预览：布局、标题栏位置与沉浸式配色都按当前设置渲染。
 * 预览中不显示文本，所有文本区域用圆角矩形占位。
 */
@Composable
internal fun BrowserChromePreview() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var headerAtBottom by remember {
        mutableStateOf(prefs.getInt("browser_header_position", 0) == 1)
    }
    var immersiveEnabled by remember {
        mutableStateOf(prefs.getBoolean("web_immersive_page", true))
    }
    var toolIndex by remember {
        mutableIntStateOf(
            prefs.getInt("web_dynamic_tool", 1).coerceIn(0, 9)
        )
    }
    var fusedDualPane by remember {
        mutableStateOf(prefs.getBoolean("web_fused_dual_pane", false))
    }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "browser_header_position" ->
                    headerAtBottom = prefs.getInt(key, 0) == 1
                "web_immersive_page" ->
                    immersiveEnabled = prefs.getBoolean(key, true)
                "web_dynamic_tool" ->
                    toolIndex = prefs.getInt(key, 1).coerceIn(0, 9)
                "web_fused_dual_pane" ->
                    fusedDualPane = prefs.getBoolean(key, false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val colors = previewThemeColors()
    val frameShape = RoundedCornerShape(24.dp)
    val topChrome = if (immersiveEnabled) PreviewPageTop else colors.surface
    val bottomChrome = if (immersiveEnabled) PreviewPageBottom else colors.surface
    val topContent = if (immersiveEnabled) {
        previewContrastColor(PreviewPageTop)
    } else {
        colors.onSurface
    }
    val bottomContent = if (immersiveEnabled) {
        previewContrastColor(PreviewPageBottom)
    } else {
        colors.onSurface
    }
    val titleChrome = if (headerAtBottom) bottomChrome else topChrome
    val titleContent = if (headerAtBottom) bottomContent else topContent
    val inputPill = if (immersiveEnabled) {
        previewOverlayColor(titleChrome)
    } else {
        colors.surfaceContainerHigh
    }
    val inputPillContent = if (immersiveEnabled) {
        previewContrastColor(inputPill)
    } else {
        colors.onSurface
    }

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
                .clip(frameShape)
                .background(colors.surfaceContainer)
                .border(
                    width = 1.dp,
                    color = colors.onSurface.copy(alpha = 0.08f),
                    shape = frameShape,
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                PreviewStatusBar(background = topChrome)

                if (!headerAtBottom) {
                    PreviewTitleBar(
                        barColor = topChrome,
                        contentColor = topContent,
                        pillColor = inputPill,
                        pillContentColor = inputPillContent,
                        fused = fusedDualPane,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    PreviewPageContent()
                }

                if (headerAtBottom) {
                    PreviewTitleBar(
                        barColor = bottomChrome,
                        contentColor = bottomContent,
                        pillColor = inputPill,
                        pillContentColor = inputPillContent,
                        fused = fusedDualPane,
                    )
                }

                // 融合式双栏把工具栏并进了标题栏，预览里不再画独立工具栏
                if (!fusedDualPane) {
                    PreviewBottomBar(
                        barColor = bottomChrome,
                        contentColor = bottomContent,
                        toolIndex = toolIndex,
                    )
                }
                PreviewNavBar(
                    background = bottomChrome,
                    contentColor = bottomContent,
                )
            }
        }
    }
}

@Composable
private fun PreviewStatusBar(background: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(background),
    )
}

@Composable
private fun PreviewTitleBar(
    barColor: Color,
    contentColor: Color,
    pillColor: Color,
    pillContentColor: Color,
    fused: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(barColor)
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (fused) {
            // 融合式双栏：左侧是后退 + 自定义功能，隐藏网页图标
            PreviewActionDot(contentColor)
            Spacer(Modifier.width(3.dp))
            PreviewActionDot(contentColor)
            Spacer(Modifier.width(4.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(previewOverlayColor(barColor)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(9.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(CircleShape)
                .background(pillColor)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.Search,
                contentDescription = null,
                tint = pillContentColor.copy(alpha = 0.62f),
                modifier = Modifier.size(9.dp),
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(pillContentColor.copy(alpha = 0.55f)),
            )
        }

        if (fused) {
            // 融合式双栏：右侧是窗口 + 工具栏，隐藏扫码
            Spacer(Modifier.width(4.dp))
            PreviewActionDot(contentColor)
            Spacer(Modifier.width(3.dp))
            PreviewActionDot(contentColor)
        } else {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = MiuixIcons.Scan,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** 预览里的无文本动作图标占位 */
@Composable
private fun PreviewActionDot(contentColor: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(contentColor.copy(alpha = 0.55f)),
    )
}

@Composable
private fun PreviewPageContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PreviewPageBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PreviewPageTop)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(Color.White.copy(alpha = 0.9f)),
                )
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.55f)),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PreviewPageCard)
                    .padding(7.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    PreviewTextLine(widthFraction = 0.62f, color = Color(0xFF4A4A4A))
                    PreviewTextLine(widthFraction = 0.92f, color = PreviewPageLine)
                    PreviewTextLine(widthFraction = 0.78f, color = PreviewPageLine)
                }
            }
            PreviewTextLine(widthFraction = 0.88f, color = PreviewPageLine)
            PreviewTextLine(widthFraction = 0.72f, color = PreviewPageLine)
            PreviewTextLine(widthFraction = 0.94f, color = PreviewPageLine)
            PreviewTextLine(widthFraction = 0.55f, color = PreviewPageLine)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(PreviewPageBottom)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        previewContrastColor(PreviewPageBottom).copy(alpha = 0.55f)
                    ),
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        previewContrastColor(PreviewPageBottom).copy(alpha = 0.32f)
                    ),
            )
        }
    }
}

@Composable
private fun PreviewTextLine(
    widthFraction: Float,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}

@Composable
private fun PreviewBottomBar(
    barColor: Color,
    contentColor: Color,
    toolIndex: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(barColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewBarSlot {
            Icon(
                imageVector = MiuixIcons.ChevronBackward,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
        PreviewBarSlot {
            Icon(
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp),
            )
        }
        PreviewBarSlot {
            val toolIcon = previewToolIcon(toolIndex)
            if (toolIcon != null) {
                Icon(
                    imageVector = toolIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(contentColor.copy(alpha = 0.35f)),
                )
            }
        }
        PreviewBarSlot {
            PreviewWindowIcon(tint = contentColor)
        }
        PreviewBarSlot {
            Icon(
                imageVector = MiuixIcons.GridView,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun RowScope.PreviewBarSlot(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun PreviewWindowIcon(tint: Color) {
    Box(
        modifier = Modifier.size(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.5.dp),
        ) {
            drawRoundRect(
                color = tint,
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(width = 1.1.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(1.5.dp)
                .clip(RoundedCornerShape(0.75.dp))
                .background(tint.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun PreviewNavBar(
    background: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.78f)),
        )
    }
}

private fun previewToolIcon(index: Int): ImageVector? = when (index) {
    1 -> MiuixIcons.Home
    2 -> MiuixIcons.Refresh
    3 -> MiuixIcons.ExpandMore
    4 -> MiuixIcons.Favorites
    5 -> MiuixIcons.FavoritesFill
    6 -> MiuixIcons.Recent
    7 -> MiuixIcons.Download
    8 -> MiuixIcons.Translate
    9 -> MiuixIcons.RecordingTape
    else -> null
}
