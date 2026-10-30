@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ============================================================
// 法律文档：开放源代码许可 / 第三方源代码许可
// ============================================================

private const val LEGAL_DOC_TOP_BAR_BLUR_RADIUS = 25f
private const val LEGAL_DOC_TOP_BAR_BLEND_ALPHA = 0.8f
private val LEGAL_DOC_TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val LegalDocSmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ---------------- 二级页路由（与列表页同文件） ----------------

const val LEGAL_ROUTE_OSS_COMPONENTS = "legal_oss_components"
const val LEGAL_ROUTE_OSS_APACHE = "legal_oss_apache"
const val LEGAL_ROUTE_OSS_MIT = "legal_oss_mit"

const val LEGAL_ROUTE_THIRD_PARTY_COMPONENTS = "legal_tp_components"
const val LEGAL_ROUTE_THIRD_PARTY_MLKIT = "legal_tp_mlkit"
const val LEGAL_ROUTE_THIRD_PARTY_WEBVIEW = "legal_tp_webview"

/** 法律信息页里所有可打开的文档路由（统一由 [LegalDocumentPage] 渲染）。 */
internal val LEGAL_DOC_ROUTES: Set<String> = setOf(
    LEGAL_ROUTE_OPEN_SOURCE_LICENSE,
    LEGAL_ROUTE_THIRD_PARTY_LICENSE,
    LEGAL_ROUTE_PRIVACY_POLICY,
    LEGAL_ROUTE_USER_AGREEMENT,
    LEGAL_ROUTE_UX_IMPROVEMENT,
    LEGAL_ROUTE_OSS_COMPONENTS,
    LEGAL_ROUTE_OSS_APACHE,
    LEGAL_ROUTE_OSS_MIT,
    LEGAL_ROUTE_THIRD_PARTY_COMPONENTS,
    LEGAL_ROUTE_THIRD_PARTY_MLKIT,
    LEGAL_ROUTE_THIRD_PARTY_WEBVIEW,
)

/** 一条第三方组件 / 开源组件声明。 */
internal data class LegalComponent(
    val name: String,
    val version: String = "",
    val license: String,
)

/**
 * 开放源代码组件清单。
 * 只列直接依赖；传递依赖的许可证与直接依赖一致（绝大多数为 Apache-2.0）。
 */
internal val OSS_COMPONENTS: List<LegalComponent> = listOf(
    LegalComponent("AndroidX Core KTX", "1.17.0", "Apache-2.0"),
    LegalComponent("AndroidX Lifecycle Runtime KTX", "2.8.7", "Apache-2.0"),
    LegalComponent("AndroidX Activity Compose", "1.12.0", "Apache-2.0"),
    LegalComponent("AndroidX WebKit", "1.17.1", "Apache-2.0"),
    LegalComponent("AndroidX ProfileInstaller", "1.4.1", "Apache-2.0"),
    LegalComponent("AndroidX Camera", "1.4.2", "Apache-2.0"),
    LegalComponent("AndroidX Media3 / ExoPlayer", "1.11.1", "Apache-2.0"),
    LegalComponent("AndroidX Media", "1.7.0", "Apache-2.0"),
    LegalComponent("AndroidX NavigationEvent", "1.1.2", "Apache-2.0"),
    LegalComponent("Compose Multiplatform", "1.12.0", "Apache-2.0"),
    LegalComponent("Compose Material3 (JetBrains)", "1.9.0", "Apache-2.0"),
    LegalComponent("Compose Material Icons Extended", "1.7.3", "Apache-2.0"),
    LegalComponent("Navigation Compose (JetBrains)", "2.9.0-beta05", "Apache-2.0"),
    LegalComponent("Kotlin Standard Library", "2.4.10", "Apache-2.0"),
    LegalComponent("Kotlin Coroutines", "", "Apache-2.0"),
    LegalComponent("MiuiX for Compose Multiplatform", "0.9.4", "Apache-2.0"),
    LegalComponent("Material Kolor", "5.0.1", "MIT"),
)

/**
 * 第三方（非 Mote 自有、非本仓库分发）组件。
 * 这些组件适用各自的服务条款，不属于开放源代码许可范围。
 */
internal val THIRD_PARTY_COMPONENTS: List<LegalComponent> = listOf(
    LegalComponent("Google ML Kit 条码扫描", "17.3.0", "Google ML Kit 服务条款"),
    LegalComponent("Google ML Kit 文字识别（中文）", "16.0.1", "Google ML Kit 服务条款"),
    LegalComponent("Android System WebView / Chromium", "随系统版本", "BSD-3-Clause 等"),
)

// ============================================================
// 入口
// ============================================================

@Composable
fun LegalDocumentPage(
    route: String,
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
) {
    when (route) {
        // 开放源代码许可
        LEGAL_ROUTE_OPEN_SOURCE_LICENSE -> OpenSourceLicenseListPage(onBack, onOpenDoc)
        LEGAL_ROUTE_OSS_COMPONENTS -> LegalComponentsDetailPage("开源组件清单", OSS_COMPONENTS, onBack)
        LEGAL_ROUTE_OSS_APACHE -> LegalTextDetailPage("Apache License 2.0", "licenses/apache-2.0.txt", onBack)
        LEGAL_ROUTE_OSS_MIT -> LegalTextDetailPage("The MIT License", "licenses/mit.txt", onBack)

        // 第三方源代码许可
        LEGAL_ROUTE_THIRD_PARTY_LICENSE -> ThirdPartyLicenseListPage(onBack, onOpenDoc)
        LEGAL_ROUTE_THIRD_PARTY_COMPONENTS ->
            LegalComponentsDetailPage("第三方组件清单", THIRD_PARTY_COMPONENTS, onBack)
        LEGAL_ROUTE_THIRD_PARTY_MLKIT ->
            LegalMarkdownDetailPage("Google ML Kit", "legal/third-party-mlkit.md", onBack)
        LEGAL_ROUTE_THIRD_PARTY_WEBVIEW ->
            LegalMarkdownDetailPage("Android System WebView", "legal/third-party-webview.md", onBack)

        // 云端文档
        LEGAL_ROUTE_PRIVACY_POLICY -> RemoteLegalDocPage(RemoteLegalDoc.PRIVACY_POLICY, onBack)
        LEGAL_ROUTE_USER_AGREEMENT -> RemoteLegalDocPage(RemoteLegalDoc.USER_AGREEMENT, onBack)
        LEGAL_ROUTE_UX_IMPROVEMENT -> RemoteLegalDocPage(RemoteLegalDoc.UX_IMPROVEMENT, onBack)

        else -> SettingsSubPage(title = "法律信息", onBack = onBack)
    }
}

// ============================================================
// 列表页（Miuix 设置项样式，右侧带箭头）
// ============================================================

@Composable
private fun OpenSourceLicenseListPage(
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
) {
    LegalDocumentScaffold(title = "开放源代码许可", onBack = onBack) {
        item(key = "components_title") {
            SmallTitle("开源组件")
        }
        item(key = "components_card") {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "开源组件清单",
                    onClick = { onOpenDoc(LEGAL_ROUTE_OSS_COMPONENTS) },
                )
            }
        }

        item(key = "licenses_title") {
            SmallTitle("许可证全文")
        }
        item(key = "licenses_card") {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "Apache License 2.0",
                    onClick = { onOpenDoc(LEGAL_ROUTE_OSS_APACHE) },
                )
                ArrowPreference(
                    title = "The MIT License",
                    onClick = { onOpenDoc(LEGAL_ROUTE_OSS_MIT) },
                )
            }
        }

        item(key = "bottom_spacer") {
            LegalBottomSpacer()
        }
    }
}

@Composable
private fun ThirdPartyLicenseListPage(
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
) {
    LegalDocumentScaffold(title = "第三方源代码许可", onBack = onBack) {
        item(key = "components_title") {
            SmallTitle("第三方组件")
        }
        item(key = "components_card") {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "第三方组件清单",
                    onClick = { onOpenDoc(LEGAL_ROUTE_THIRD_PARTY_COMPONENTS) },
                )
            }
        }

        item(key = "terms_title") {
            SmallTitle("条款与说明")
        }
        item(key = "terms_card") {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "Google ML Kit",
                    onClick = { onOpenDoc(LEGAL_ROUTE_THIRD_PARTY_MLKIT) },
                )
                ArrowPreference(
                    title = "Android System WebView",
                    onClick = { onOpenDoc(LEGAL_ROUTE_THIRD_PARTY_WEBVIEW) },
                )
            }
        }

        item(key = "bottom_spacer") {
            LegalBottomSpacer()
        }
    }
}

// ============================================================
// 二级页：组件清单 / 许可证全文 / 第三方说明
// ============================================================

@Composable
private fun LegalComponentsDetailPage(
    title: String,
    components: List<LegalComponent>,
    onBack: () -> Unit,
) {
    LegalDocumentScaffold(title = title, onBack = onBack) {
        item(key = "components_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                components.forEachIndexed { index, component ->
                    LegalComponentRow(component)
                    if (index != components.lastIndex) {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
        item(key = "bottom_spacer") {
            LegalBottomSpacer()
        }
    }
}

/** 许可证全文：等宽字体，逐字保留原文。 */
@Composable
private fun LegalTextDetailPage(
    title: String,
    assetPath: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val text = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrDefault("")
    }

    LegalDocumentScaffold(title = title, onBack = onBack) {
        item(key = "text_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
        item(key = "bottom_spacer") {
            LegalBottomSpacer()
        }
    }
}

/** 第三方说明：复用更新日志那套 Markdown 渲染。 */
@Composable
private fun LegalMarkdownDetailPage(
    title: String,
    assetPath: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val text = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrDefault("")
    }

    LegalDocumentScaffold(title = title, onBack = onBack) {
        item(key = "md_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                MarkdownText(
                    text = text,
                    baseColor = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        item(key = "bottom_spacer") {
            LegalBottomSpacer()
        }
    }
}

// ============================================================
// 通用行 / 页面骨架
// ============================================================

@Composable
private fun LegalBottomSpacer() {
    Spacer(
        Modifier.height(
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
        )
    )
}

@Composable
private fun LegalComponentRow(component: LegalComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = component.name,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MiuixTheme.colorScheme.onSurface,
            )
            if (component.version.isNotBlank()) {
                Text(
                    text = component.version,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = component.license,
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
internal fun LegalDocumentScaffold(
    title: String,
    onBack: () -> Unit,
    actions: (@Composable () -> Unit)? = null,
    listContent: LazyListScope.() -> Unit,
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
        animationSpec = tween(400, easing = LegalDocSmoothEase),
        label = "legalDocBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = LEGAL_DOC_TOP_BAR_BLEND_ALPHA)))
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding =
        if (topBarBottomPx > 0) measuredTop else statusBarHeight + LEGAL_DOC_TOP_BAR_FALLBACK_HEIGHT

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
                    content = listContent,
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
                                    blurRadius = LEGAL_DOC_TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = LEGAL_DOC_TOP_BAR_BLUR_RADIUS,
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
                    actions = { actions?.invoke() },
                )
            }
        }
    }
}
