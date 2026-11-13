@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.Context
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import com.torachen.mote.ui.KEY_PROGRESSIVE_TITLE_BLUR
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.SettingsBackButton
import com.torachen.mote.ui.SettingsPageAutoScroll
import com.torachen.mote.ui.UiMode
import com.torachen.mote.ui.buildBackCardMaterialModifier

private const val AI_TOP_BAR_BLUR_RADIUS = 25f
private const val AI_TOP_BAR_BLEND_ALPHA = 0.8f
private val AI_TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val AiSmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/**
 * AI 页面统一主题。
 *
 * 应用默认使用 MIUIX 主题（由根节点提供）；如果用户把全局风格切到了 Material，
 * 这里会补一层 MIUIX 亮 / 暗色方案，保证 AI 页面在两种模式下都有正确的配色。
 */
@Composable
internal fun AiPageTheme(content: @Composable () -> Unit) {
    if (LocalUiMode.current != UiMode.Material) {
        content()
        return
    }
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var colorMode by remember { mutableIntStateOf(prefs.getInt("ui_color_mode", 0)) }
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "ui_color_mode") colorMode = prefs.getInt(key, 0)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val systemDark = isSystemInDarkTheme()
    val isDark = when (colorMode) {
        1 -> false
        2, 3 -> true
        else -> systemDark
    }
    val colors = remember(isDark) {
        if (isDark) darkColorScheme() else lightColorScheme()
    }
    MiuixTheme(colors = colors) { content() }
}

/**
 * AI 设置类页面的统一骨架。
 *
 * 与「法律信息」「搜索设置」等页面完全一致：
 * 大标题 + 渐进模糊标题栏 + 返回卡片 + 卡片分组 + 搜索定位/高亮。
 */
@Composable
internal fun AiSettingsScaffold(
    title: String,
    pageRoute: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    floatingBottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    AiPageTheme {
        AiSettingsScaffoldInner(
            title = title,
            pageRoute = pageRoute,
            onBack = onBack,
            actions = actions,
            floatingBottomBar = floatingBottomBar,
            content = content,
        )
    }
}

@Composable
private fun AiSettingsScaffoldInner(
    title: String,
    pageRoute: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    floatingBottomBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
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
    } else {
        null
    }

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
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR ->
                    progressiveTitleBlur = prefs.getBoolean(key, true)

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
        animationSpec = tween(400, easing = AiSmoothEase),
        label = "aiSettingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = AI_TOP_BAR_BLEND_ALPHA)))
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding =
        if (topBarBottomPx > 0) measuredTop else statusBarHeight + AI_TOP_BAR_FALLBACK_HEIGHT

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            content()
                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding() +
                                            WindowInsets.captionBar
                                                .asPaddingValues()
                                                .calculateBottomPadding(),
                                ),
                            )
                        }
                    }
                }

                SettingsPageAutoScroll(
                    lazyListState = listState,
                    pageRoute = pageRoute,
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
                                    blurRadius = 10f,
                                    gradient = ProgressiveBlur.Top.copy(
                                        startFraction = 0.625f,
                                        curve = 1f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = 0.3f),
                                            ),
                                        ),
                                    ),
                                ).background(
                                    Brush.verticalGradient(
                                        listOf(surfaceColor, Color.Transparent),
                                    ),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = AI_TOP_BAR_BLUR_RADIUS,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = titleBarBlendColorEntries,
                                    ),
                                )
                            }
                        } else {
                            Modifier.background(surfaceColor)
                        },
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coordinates ->
                        val bottomPx = coordinates.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    },
            ) {
                TopAppBar(
                    title = title,
                    largeTitle = title,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    },
                    actions = actions,
                )
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                floatingBottomBar()
            }
        }
    }
}
