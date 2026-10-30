@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.union
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
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
import top.yukonga.miuix.kmp.preference.ArrowPreference
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

// ---------------- 法律文档路由常量 ----------------
const val LEGAL_ROUTE_USER_AGREEMENT = "legal_user_agreement"
const val LEGAL_ROUTE_PRIVACY_POLICY = "legal_privacy_policy"
const val LEGAL_ROUTE_OPEN_SOURCE_LICENSE = "legal_open_source_license"
const val LEGAL_ROUTE_THIRD_PARTY_LICENSE = "legal_third_party_license"
const val LEGAL_ROUTE_UX_IMPROVEMENT = "legal_ux_improvement"

// ============================================================
// 法律信息页
// ============================================================

@Composable
fun SettingsLegalPage(
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsLegalMiuix(onBack, onOpenDoc)
        UiMode.Material -> SettingsLegalMaterial(onBack, onOpenDoc)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsLegalMiuix(
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
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
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            SmallTitle("协议")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "用户协议",
                                    pageRoute = "settings_legal",
                                    pageTitle = "法律信息",
                                    itemKey = "settings_legal_user_agreement",
                                ) {
                                    ArrowPreference(
                                        title = "用户协议",
                                        onClick = { onOpenDoc(LEGAL_ROUTE_USER_AGREEMENT) },
                                    )
                                }
                                SearchableItem(
                                    title = "隐私政策",
                                    pageRoute = "settings_legal",
                                    pageTitle = "法律信息",
                                    itemKey = "settings_legal_privacy_policy",
                                ) {
                                    ArrowPreference(
                                        title = "隐私政策",
                                        onClick = { onOpenDoc(LEGAL_ROUTE_PRIVACY_POLICY) },
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
                                    title = "开放源代码许可",
                                    pageRoute = "settings_legal",
                                    pageTitle = "法律信息",
                                    itemKey = "settings_legal_open_source_license",
                                ) {
                                    ArrowPreference(
                                        title = "开放源代码许可",
                                        onClick = { onOpenDoc(LEGAL_ROUTE_OPEN_SOURCE_LICENSE) },
                                    )
                                }
                                SearchableItem(
                                    title = "第三方源代码许可",
                                    pageRoute = "settings_legal",
                                    pageTitle = "法律信息",
                                    itemKey = "settings_legal_third_party_license",
                                ) {
                                    ArrowPreference(
                                        title = "第三方源代码许可",
                                        onClick = { onOpenDoc(LEGAL_ROUTE_THIRD_PARTY_LICENSE) },
                                    )
                                }
                                SearchableItem(
                                    title = "用户体验改进计划",
                                    pageRoute = "settings_legal",
                                    pageTitle = "法律信息",
                                    itemKey = "settings_legal_ux_improvement",
                                ) {
                                    ArrowPreference(
                                        title = "用户体验改进计划",
                                        onClick = { onOpenDoc(LEGAL_ROUTE_UX_IMPROVEMENT) },
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
                    pageRoute = "settings_legal",
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
                                        startFraction = 0.5f,
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
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
                    title = "法律信息",
                    largeTitle = "法律信息",
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
private fun SettingsLegalMaterial(
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

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
                    title = { M3Text("法律信息") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
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
                item(key = "agreement_header") {
                    LegalSectionHeader("协议")
                }

                item(key = "agreement_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentArrowItem(
                                        title = "用户协议",
                                        onClick = {
                                            onOpenDoc(LEGAL_ROUTE_USER_AGREEMENT)
                                        },
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "隐私政策",
                                        onClick = {
                                            onOpenDoc(LEGAL_ROUTE_PRIVACY_POLICY)
                                        },
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "other_header") {
                    LegalSectionHeader("其它")
                }

                item(key = "other_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentArrowItem(
                                        title = "开放源代码许可",
                                        onClick = {
                                            onOpenDoc(LEGAL_ROUTE_OPEN_SOURCE_LICENSE)
                                        },
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "第三方源代码许可",
                                        onClick = {
                                            onOpenDoc(LEGAL_ROUTE_THIRD_PARTY_LICENSE)
                                        },
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "用户体验改进计划",
                                        onClick = {
                                            onOpenDoc(LEGAL_ROUTE_UX_IMPROVEMENT)
                                        },
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

@Composable
private fun LegalSectionHeader(text: String) {
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
