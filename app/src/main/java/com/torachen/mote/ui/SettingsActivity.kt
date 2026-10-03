@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ★★★ NavigationEvent ★★★
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// ★★★ MIUIX 组件 ★★★
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
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
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Update

// ============================================================
// 数据模型
// ============================================================

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val iconInnerSize: Dp = DEFAULT_ICON_INNER_SIZE,
    val iconBgColor: Color? = null,
)

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

// ============================================================
// 常量
// ============================================================

private val ICON_CONTAINER_SIZE = 27.5.dp
private val ICON_CORNER_RADIUS = 8.25.dp
private val ICON_TO_TITLE_GAP = 6.dp
private val DEFAULT_ICON_INNER_SIZE = 23.dp
private val SMALL_ICON_INNER_SIZE = 20.5.dp
private val MEDIUM_ICON_INNER_SIZE = 21.75.dp

// ★ 档位 A：效果优先，MIUIX 官方默认参数
private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f

// 首帧测量前的兜底 topBar 高度（不含 statusBars）
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ============================================================
// 图标
// ============================================================

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    innerSize: Dp,
    bgColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ICON_CONTAINER_SIZE)
            .clip(RoundedCornerShape(ICON_CORNER_RADIUS))
            .background(bgColor ?: MiuixTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(innerSize),
        )
    }
}

// ============================================================
// 设置分区
// ============================================================

private val settingsSections = listOf(
    SettingsSection(
        title = "通用",
        items = listOf(
            SettingsItem("主页设置", MiuixIcons.Home, "settings_home", SMALL_ICON_INNER_SIZE),
            SettingsItem("搜索设置", MiuixIcons.Search, "settings_search", SMALL_ICON_INNER_SIZE),
            SettingsItem("网页浏览设置", MiuixIcons.Months, "settings_web"),
        )
    ),
    SettingsSection(
        title = "定制",
        items = listOf(
            SettingsItem("图标", MiuixIcons.Photos, "settings_icon", iconBgColor = Color(0xFF2AD200)),
            SettingsItem("颜色", MiuixIcons.Background, "settings_color", iconBgColor = Color(0xFFFF7439)),
            SettingsItem("布局", MiuixIcons.All, "settings_layout"),
        )
    ),
    SettingsSection(
        title = "隐私",
        items = listOf(
            SettingsItem("权限设置", MiuixIcons.Link, "settings_permission", MEDIUM_ICON_INNER_SIZE, Color(0xFFA4A4A4)),
            SettingsItem("通知设置", MiuixIcons.Community, "settings_notification", MEDIUM_ICON_INNER_SIZE, Color(0xFFFF1C3E)),
            SettingsItem("法律信息", MiuixIcons.Contacts, "settings_legal", MEDIUM_ICON_INNER_SIZE),
        )
    ),
    SettingsSection(
        title = "其它",
        items = listOf(
            SettingsItem("关于", MiuixIcons.Info, "settings_about", iconBgColor = Color(0xFFA4A4A4)),
        )
    ),
)

// ============================================================
// 设置主页
// ============================================================

@Composable
fun SettingsMainPage(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
    val scrollBehavior = MiuixScrollBehavior()

    var searchValue by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }

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

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
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
                    item(key = "searchbar") {
                        SearchBar(
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            inputField = {
                                InputField(
                                    query = searchValue,
                                    onQueryChange = { searchValue = it },
                                    onSearch = { searchExpanded = false },
                                    expanded = searchExpanded,
                                    onExpandedChange = { searchExpanded = it },
                                    label = "搜索设置",
                                )
                            },
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            outsideEndAction = {
                                Text(
                                    text = "取消",
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                        ) {
                                            searchValue = ""
                                            searchExpanded = false
                                        },
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MiuixTheme.colorScheme.primary,
                                )
                            },
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {}
                        }
                    }

                    item(key = "settings_content") {
                        AnimatedVisibility(
                            visible = !searchExpanded,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200)),
                        ) {
                            Column {
                                settingsSections.forEach { section ->
                                    SmallTitle(section.title)
                                    Card(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .padding(bottom = 12.dp),
                                    ) {
                                        section.items.forEach { item ->
                                            ArrowPreference(
                                                title = item.title,
                                                startAction = {
                                                    Box(modifier = Modifier.padding(end = ICON_TO_TITLE_GAP)) {
                                                        SettingsIcon(
                                                            icon = item.icon,
                                                            innerSize = item.iconInnerSize,
                                                            bgColor = item.iconBgColor,
                                                        )
                                                    }
                                                },
                                                onClick = { onItemClick(item.route) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                    title = "设置",
                    largeTitle = "设置",
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
// 设置子页面（通用兜底）
//
// ★ 现在只用于尚未实现的其它设置项（例如 settings_icon、
//   settings_background …）。settings_home 与 settings_search
//   已由 MainActivity.AppRoot 单独路由到对应实现。
// ============================================================

@Composable
fun SettingsSubPage(
    title: String,
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

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
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
                    contentPadding = PaddingValues(top = contentTopPadding),
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(title, color = MiuixTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "该页面正在开发中",
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
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
                    title = title,
                    largeTitle = title,
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