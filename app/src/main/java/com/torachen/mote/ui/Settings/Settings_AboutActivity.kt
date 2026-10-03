// Settings_AboutActivity.kt
@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// ★★★ MIUIX 模糊 ★★★
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

// ★★★ MIUIX 图标 ★★★
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

// ★★★ 应用图标 ★★★
import com.torachen.mote.R

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f

private const val APP_DISPLAY_NAME = "微元"

/**
 * 版本名前缀 / 后缀。
 * 预留空字符串，方便后续添加文字。
 * 例如：前缀改为 "v" → "v0.2.1"；后缀改为 " 正式版" → "0.2.1 正式版"。
 */
private const val VERSION_PREFIX = ""
private const val VERSION_SUFFIX = ""

private val LOGO_TOP_OFFSET = 92.dp
private val LOGO_BOTTOM_GAP = 126.dp
private val LOGO_FALLBACK_HEIGHT = 180.dp
private val TOP_BAR_FALLBACK_HEIGHT = 64.dp

private val LIST_BOTTOM_PADDING = 12.dp
private val CARD_SPACING = 12.dp

// ---------------- 自动检查更新选项 ----------------
private val AUTO_CHECK_UPDATE_OPTIONS = listOf(
    "每次启动",
    "每过七天",
    "关闭",
)
private const val AUTO_CHECK_UPDATE_DEFAULT = 0

// ---------------- 下载来源选项 ----------------
private val DOWNLOAD_SOURCE_OPTIONS = listOf(
    "Gitee" to "国内访问更快更稳定",
    "Github" to "国际访问，适合海外用户",
)
private const val DOWNLOAD_SOURCE_DEFAULT = 0

// ---------------- 持久化键 ----------------
private const val KEY_AUTO_CHECK_UPDATE = "about_auto_check_update"
private const val KEY_DOWNLOAD_SOURCE = "about_download_source"

// ============================================================
// 关于微元
// ============================================================

@Composable
fun SettingsAboutPage(
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

    // ★ 顶栏容器底边（像素）。容器从 Y=0 开始，底边 = statusBar + AppBar 高
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val lazyListState = rememberLazyListState()

    var logoHeightPx by remember { mutableIntStateOf(0) }
    val logoHeightDp = with(density) { logoHeightPx.toDp() }
    val effectiveLogoHeight = if (logoHeightPx > 0) logoHeightDp else LOGO_FALLBACK_HEIGHT

    var cardsHeightPx by remember { mutableIntStateOf(0) }
    val cardsHeight = with(density) { cardsHeightPx.toDp() }

    // ---------------- 持久化 ----------------
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 动态读取当前应用版本名 ----------------
    val versionText = remember {
        try {
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            "$VERSION_PREFIX${pkgInfo.versionName}$VERSION_SUFFIX"
        } catch (_: Exception) {
            "$VERSION_PREFIX?$VERSION_SUFFIX"
        }
    }

    var autoCheckUpdateIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_AUTO_CHECK_UPDATE, AUTO_CHECK_UPDATE_DEFAULT)
                .coerceIn(0, AUTO_CHECK_UPDATE_OPTIONS.lastIndex)
        )
    }
    var downloadSourceIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_DOWNLOAD_SOURCE, DOWNLOAD_SOURCE_DEFAULT)
                .coerceIn(0, DOWNLOAD_SOURCE_OPTIONS.lastIndex)
        )
    }

    // ---------------- 下拉条目构建 ----------------

    val autoCheckUpdateEntries = remember(autoCheckUpdateIndex) {
        listOf(
            DropdownEntry(
                items = AUTO_CHECK_UPDATE_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = autoCheckUpdateIndex == index,
                        onClick = {
                            autoCheckUpdateIndex = index
                            prefs.edit()
                                .putInt(KEY_AUTO_CHECK_UPDATE, index)
                                .apply()
                        },
                    )
                },
            ),
        )
    }

    val downloadSourceEntries = remember(downloadSourceIndex) {
        listOf(
            DropdownEntry(
                items = DOWNLOAD_SOURCE_OPTIONS.mapIndexed { index, (label, summary) ->
                    DropdownItem(
                        text = label,
                        summary = summary,
                        selected = downloadSourceIndex == index,
                        onClick = {
                            downloadSourceIndex = index
                            prefs.edit()
                                .putInt(KEY_DOWNLOAD_SOURCE, index)
                                .apply()
                        },
                    )
                },
            ),
        )
    }

    val scrollProgress by remember {
        derivedStateOf {
            when {
                lazyListState.firstVisibleItemIndex > 0 -> 1f
                else -> {
                    val spacer = lazyListState.layoutInfo.visibleItemsInfo
                        .firstOrNull { it.key == "logoSpacer" }
                    if (spacer != null && spacer.size > 0) {
                        (lazyListState.firstVisibleItemScrollOffset.toFloat() / spacer.size)
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                }
            }
        }
    }

    // 顶栏背景 alpha：随滚动渐变
    val barAlpha by animateFloatAsState(
        targetValue = if (scrollProgress > 0.35f) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "barAlpha",
    )
    val titleAlpha = ((scrollProgress - 0.35f) / 0.4f).coerceIn(0f, 1f)

    val logoVisualTop = contentTopPadding + LOGO_TOP_OFFSET
    val logoSpacerHeight = LOGO_TOP_OFFSET + effectiveLogoHeight + LOGO_BOTTOM_GAP

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            val viewportHeight = maxHeight

            val fillerHeight = (
                    viewportHeight
                            - contentTopPadding
                            - cardsHeight
                            - LIST_BOTTOM_PADDING
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
                // 固定 Logo 区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = logoVisualTop)
                        .onGloballyPositioned { coords ->
                            val h = coords.size.height
                            if (h != logoHeightPx) logoHeightPx = h
                        }
                        .graphicsLayer { alpha = 1f - scrollProgress },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White),
                    ) {
                        Image(
                            modifier = Modifier.size(74.dp),
                            painter = painterResource(id = R.mipmap.ic_logo),
                            contentDescription = null,
                        )
                    }

                    Text(
                        text = APP_DISPLAY_NAME,
                        modifier = Modifier.padding(top = 12.dp, bottom = 5.dp),
                        color = MiuixTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 35.sp,
                    )

                    Text(
                        text = versionText,
                        modifier = Modifier.fillMaxWidth(),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = LIST_BOTTOM_PADDING,
                    ),
                ) {
                    item(key = "logoSpacer") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(logoSpacerHeight),
                        )
                    }

                    item(key = "cards") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    val h = coords.size.height
                                    if (h != cardsHeightPx) cardsHeightPx = h
                                },
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = CARD_SPACING),
                            ) {
                                ArrowPreference(
                                    title = "检查更新",
                                    endActions = {
                                        Text(
                                            text = "正在检查中",
                                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                        )
                                    },
                                    onClick = {},
                                )
                                WindowDropdownPreference(
                                    title = "自动检查更新",
                                    entries = autoCheckUpdateEntries,
                                    collapseOnSelection = true,
                                )
                                WindowSpinnerPreference(
                                    title = "下载来源",
                                    entries = downloadSourceEntries,
                                    collapseOnSelection = true,
                                )
                            }

                            Card(
                                modifier = Modifier.padding(horizontal = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "开源许可证",
                                    endActions = {
                                        Text(
                                            text = "Apache-2.0",
                                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                        )
                                    },
                                    onClick = {},
                                )
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

            // ========== 顶栏层 ==========
            //
            // ★ 关键修复：
            //   - 外层容器从屏幕 Y=0 开始（不再用 statusBarsPadding）
            //   - 背景层 matchParentSize 覆盖整个容器（含状态栏区域）
            //   - 内容层用 statusBarsPadding 把 AppBar 内容往下推
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) topBarBottomPx = bottomPx
                    }
            ) {
                // ---- 背景层（含状态栏区域）----
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = barAlpha }
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
                )

                // ---- 内容层（用 statusBarsPadding 保护）----
                Box(modifier = Modifier.statusBarsPadding()) {
                    SmallTopAppBar(
                        title = "关于",
                        scrollBehavior = scrollBehavior,
                        color = Color.Transparent,
                        titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
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
}