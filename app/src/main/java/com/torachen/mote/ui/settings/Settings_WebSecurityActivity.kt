@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import org.json.JSONArray
import org.json.JSONObject

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.state.ToggleableState
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
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
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog

import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.RadioButton as M3RadioButton
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.ui.zIndex
// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private const val KEY_REMEMBER_FORM = "web_remember_form"
private const val KEY_ACCEPT_COOKIE = "web_accept_cookie"
private const val KEY_CLEAR_INCOGNITO_ON_EXIT = "web_clear_incognito_on_exit"

private const val KEY_BLOCK_ADS_GLOBAL = "web_block_ads_global"
private const val KEY_TRACKING_PROTECTION_GLOBAL = "web_tracking_protection_global"

// ============================================================
// 状态
// ============================================================

@Immutable
private data class WebSecurityUiState(
    val rememberFormEnabled: Boolean,
    val acceptCookieEnabled: Boolean,
    val clearIncognitoOnExitEnabled: Boolean,
    val blockAdsGlobalEnabled: Boolean,
    val trackingProtectionGlobalEnabled: Boolean,
    val trackingLevel: Int,
)

@Immutable
private data class WebSecurityActions(
    val onRememberFormChange: (Boolean) -> Unit,
    val onAcceptCookieChange: (Boolean) -> Unit,
    val onClearIncognitoOnExitChange: (Boolean) -> Unit,
    val onBlockAdsGlobalChange: (Boolean) -> Unit,
    val onTrackingProtectionGlobalChange: (Boolean) -> Unit,
    val onTrackingLevelChange: (Int) -> Unit,
    val onOpenWhitelist: () -> Unit,
    val onBack: () -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsWebSecurityPage(
    onBack: () -> Unit,
    onOpenWhitelist: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var rememberFormEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_REMEMBER_FORM, true))
    }
    var acceptCookieEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_ACCEPT_COOKIE, true))
    }
    var clearIncognitoOnExitEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_CLEAR_INCOGNITO_ON_EXIT, true))
    }
    var blockAdsGlobalEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BLOCK_ADS_GLOBAL, true))
    }
    var trackingProtectionGlobalEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TRACKING_PROTECTION_GLOBAL, true))
    }
    var trackingLevel by remember {
        mutableIntStateOf(SecurityStateHolder.trackingLevel)
    }

    val state = WebSecurityUiState(
        rememberFormEnabled = rememberFormEnabled,
        acceptCookieEnabled = acceptCookieEnabled,
        clearIncognitoOnExitEnabled = clearIncognitoOnExitEnabled,
        blockAdsGlobalEnabled = blockAdsGlobalEnabled,
        trackingProtectionGlobalEnabled = trackingProtectionGlobalEnabled,
        trackingLevel = trackingLevel,
    )

    val actions = WebSecurityActions(
        onRememberFormChange = {
            rememberFormEnabled = it
            prefs.edit().putBoolean(KEY_REMEMBER_FORM, it).apply()
        },
        onAcceptCookieChange = {
            acceptCookieEnabled = it
            prefs.edit().putBoolean(KEY_ACCEPT_COOKIE, it).apply()
        },
        onClearIncognitoOnExitChange = {
            clearIncognitoOnExitEnabled = it
            prefs.edit().putBoolean(KEY_CLEAR_INCOGNITO_ON_EXIT, it).apply()
        },
        onBlockAdsGlobalChange = { enabled ->
            blockAdsGlobalEnabled = enabled
            prefs.edit().putBoolean(KEY_BLOCK_ADS_GLOBAL, enabled).apply()
            SecurityStateHolder.globalBlockAdsEnabled = enabled
        },
        onTrackingProtectionGlobalChange = { enabled ->
            trackingProtectionGlobalEnabled = enabled
            prefs.edit().putBoolean(KEY_TRACKING_PROTECTION_GLOBAL, enabled).apply()
            SecurityStateHolder.globalTrackingProtectionEnabled = enabled
        },
        onTrackingLevelChange = { level ->
            val clamped = level.coerceIn(0, 2)
            trackingLevel = clamped
            SecurityStateHolder.updateTrackingLevel(clamped)
        },
        onOpenWhitelist = onOpenWhitelist,
        onBack = onBack,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsWebSecurityMiuix(state, actions)
        UiMode.Material -> SettingsWebSecurityMaterial(state, actions)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsWebSecurityMiuix(
    state: WebSecurityUiState,
    actions: WebSecurityActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

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
                    contentPadding = PaddingValues(top = contentTopPadding + 12.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            SmallTitle("网页权限")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "记住表单数据",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_remember_form",
                                ) {
                                    SwitchPreference(
                                        title = "记住表单数据",
                                        summary = "记住表单中输入的数据以便以后使用",
                                        checked = state.rememberFormEnabled,
                                        onCheckedChange = actions.onRememberFormChange,
                                    )
                                }
                                SearchableItem(
                                    title = "接受 Cookie",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_accept_cookie",
                                ) {
                                    SwitchPreference(
                                        title = "接受 Cookie",
                                        summary = "允许网页保存和读取 Cookie 数据",
                                        checked = state.acceptCookieEnabled,
                                        onCheckedChange = actions.onAcceptCookieChange,
                                    )
                                }
                                SearchableItem(
                                    title = "无痕浏览窗口清空",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_clear_incognito_on_exit",
                                ) {
                                    SwitchPreference(
                                        title = "无痕浏览窗口清空",
                                        summary = "退出浏览器时清空所有无痕浏览窗口",
                                        checked = state.clearIncognitoOnExitEnabled,
                                        onCheckedChange = actions.onClearIncognitoOnExitChange,
                                    )
                                }
                            }

                            SmallTitle("防护")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "拦截广告",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_block_ads",
                                ) {
                                    SwitchPreference(
                                        title = "拦截广告",
                                        summary = "阻止网页中的广告请求",
                                        checked = state.blockAdsGlobalEnabled,
                                        onCheckedChange = actions.onBlockAdsGlobalChange,
                                    )
                                }
                                SearchableItem(
                                    title = "跟踪防护",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_tracking_protection",
                                ) {
                                    SwitchPreference(
                                        title = "跟踪防护",
                                        summary = "限制跟踪器跨站收集浏览数据",
                                        checked = state.trackingProtectionGlobalEnabled,
                                        onCheckedChange = actions.onTrackingProtectionGlobalChange,
                                    )
                                }

                                AnimatedVisibility(visible = state.trackingProtectionGlobalEnabled) {
                                    Column {
                                        SearchableItem(
                                            title = "基本",
                                            pageRoute = "settings_web_security",
                                            pageTitle = "网页安全中心",
                                            itemKey = "settings_web_security_tracking_level_basic",
                                        ) {
                                            RadioButtonPreference(
                                                title = "基本",
                                                summary = "允许所有站点中的大部分跟踪器，内容与广告的个性化程度可能较高，网站能够正常运行",
                                                selected = state.trackingLevel == 0,
                                                onClick = { actions.onTrackingLevelChange(0) },
                                            )
                                        }
                                        SearchableItem(
                                            title = "均衡（推荐）",
                                            pageRoute = "settings_web_security",
                                            pageTitle = "网页安全中心",
                                            itemKey = "settings_web_security_tracking_level_balanced",
                                        ) {
                                            RadioButtonPreference(
                                                title = "均衡（推荐）",
                                                summary = "阻止陌生站点中的大部分跟踪器，内容与广告的个性化程度可能降低，网站能够正常运行",
                                                selected = state.trackingLevel == 1,
                                                onClick = { actions.onTrackingLevelChange(1) },
                                            )
                                        }
                                        SearchableItem(
                                            title = "严格",
                                            pageRoute = "settings_web_security",
                                            pageTitle = "网页安全中心",
                                            itemKey = "settings_web_security_tracking_level_strict",
                                        ) {
                                            RadioButtonPreference(
                                                title = "严格",
                                                summary = "阻止所有站点中的大部分跟踪器，内容与广告的个性化程度降到最低，部分网站可能无法正常运行",
                                                selected = state.trackingLevel == 2,
                                                onClick = { actions.onTrackingLevelChange(2) },
                                            )
                                        }
                                    }
                                }
                            }

                            SmallTitle("防护白名单")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "跟踪防护白名单",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_tracking_whitelist",
                                ) {
                                    ArrowPreference(
                                        title = "跟踪防护白名单",
                                        onClick = actions.onOpenWhitelist,
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
                    pageRoute = "settings_web_security",
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
                TopAppBar(
                    title = "网页安全中心",
                    largeTitle = "网页安全中心",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = actions.onBack,
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
private fun SettingsWebSecurityMaterial(
    state: WebSecurityUiState,
    actions: WebSecurityActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

    val pageBg = M3MaterialTheme.colorScheme.surface

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("网页安全中心") },
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
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "perm_header") {
                    WebSecuritySectionHeader("网页权限")
                }

                item(key = "perm_items") {
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
                                        title = "记住表单数据",
                                        summary = "记住表单中输入的数据以便以后使用",
                                        checked = state.rememberFormEnabled,
                                        onCheckedChange = actions.onRememberFormChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "接受 Cookie",
                                        summary = "允许网页保存和读取 Cookie 数据",
                                        checked = state.acceptCookieEnabled,
                                        onCheckedChange = actions.onAcceptCookieChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "无痕浏览窗口清空",
                                        summary = "退出浏览器时清空所有无痕浏览窗口",
                                        checked = state.clearIncognitoOnExitEnabled,
                                        onCheckedChange = actions.onClearIncognitoOnExitChange,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "protect_header") {
                    WebSecuritySectionHeader("防护")
                }

                item(key = "protect_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        val items: List<@Composable () -> Unit> = buildList {
                            add {
                                M3SegmentSwitchItem(
                                    title = "拦截广告",
                                    summary = "阻止网页中的广告请求",
                                    checked = state.blockAdsGlobalEnabled,
                                    onCheckedChange = actions.onBlockAdsGlobalChange,
                                )
                            }
                            add {
                                M3SegmentSwitchItem(
                                    title = "跟踪防护",
                                    summary = "限制跟踪器跨站收集浏览数据",
                                    checked = state.trackingProtectionGlobalEnabled,
                                    onCheckedChange = actions.onTrackingProtectionGlobalChange,
                                )
                            }
                            if (state.trackingProtectionGlobalEnabled) {
                                add {
                                    M3SegmentItem(
                                        headline = { M3Text("基本") },
                                        supporting = {
                                            M3Text(
                                                "允许所有站点中的大部分跟踪器，内容与广告的个性化程度可能较高，网站能够正常运行",
                                                style = M3MaterialTheme.typography.bodyMedium,
                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        leading = {
                                            M3RadioButton(
                                                selected = state.trackingLevel == 0,
                                                onClick = null,
                                            )
                                        },
                                        onClick = { actions.onTrackingLevelChange(0) },
                                    )
                                }
                                add {
                                    M3SegmentItem(
                                        headline = { M3Text("均衡（推荐）") },
                                        supporting = {
                                            M3Text(
                                                "阻止陌生站点中的大部分跟踪器，内容与广告的个性化程度可能降低，网站能够正常运行",
                                                style = M3MaterialTheme.typography.bodyMedium,
                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        leading = {
                                            M3RadioButton(
                                                selected = state.trackingLevel == 1,
                                                onClick = null,
                                            )
                                        },
                                        onClick = { actions.onTrackingLevelChange(1) },
                                    )
                                }
                                add {
                                    M3SegmentItem(
                                        headline = { M3Text("严格") },
                                        supporting = {
                                            M3Text(
                                                "阻止所有站点中的大部分跟踪器，内容与广告的个性化程度降到最低，部分网站可能无法正常运行",
                                                style = M3MaterialTheme.typography.bodyMedium,
                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        leading = {
                                            M3RadioButton(
                                                selected = state.trackingLevel == 2,
                                                onClick = null,
                                            )
                                        },
                                        onClick = { actions.onTrackingLevelChange(2) },
                                    )
                                }
                            }
                        }
                        MoteSegmentedColumn(content = items)
                    }
                }

                item(key = "whitelist_entry") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            content = listOf(
                                {
                                    M3SegmentArrowItem(
                                        title = "跟踪防护白名单",
                                        onClick = actions.onOpenWhitelist,
                                    )
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WebSecuritySectionHeader(text: String) {
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

// ============================================================
// 跟踪防护白名单页
// ============================================================

@Composable
fun TrackingWhitelistPage(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> TrackingWhitelistPageMiuix(onBack)
        UiMode.Material -> TrackingWhitelistPageMaterial(onBack)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackingWhitelistPageMiuix(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

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

    val whitelist = SecurityStateHolder.getTrackingWhitelist()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedHosts = remember { mutableStateListOf<String>() }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedHosts.clear()
    }

    fun toggleSelect(host: String) {
        if (host in selectedHosts) selectedHosts.remove(host)
        else selectedHosts.add(host)
    }

    androidx.activity.compose.BackHandler(enabled = isMultiSelectMode) {
        exitMultiSelect()
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

    val navBarBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

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
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = navBarBottom + 120.dp,
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (whitelist.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 12.dp)
                                        .padding(bottom = 12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 56.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = MiuixIcons.Link,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = "暂无跟踪防护白名单",
                                                fontSize = 14.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 12.dp, bottom = 12.dp),
                                ) {
                                    whitelist.forEachIndexed { idx, host ->
                                        val isSelected = host in selectedHosts
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isMultiSelectMode) toggleSelect(host)
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(host)
                                                        }
                                                    }
                                                )
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = host,
                                                style = MiuixTheme.textStyles.body1,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f),
                                            )

                                            if (isMultiSelectMode) {
                                                Spacer(Modifier.width(16.dp))
                                                Checkbox(
                                                    state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                                                    onClick = null,
                                                )
                                            }
                                        }
                                    }
                                }
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
                            if (progressiveTitleBlur) {
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
                val barTitle = if (isMultiSelectMode) {
                    if (selectedHosts.isEmpty()) "请选择项目" else "已选择 ${selectedHosts.size} 项"
                } else {
                    "跟踪防护白名单"
                }
                TopAppBar(
                    title = barTitle,
                    largeTitle = barTitle,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        if (isMultiSelectMode) {
                            IconButton(onClick = { exitMultiSelect() }) {
                                Icon(
                                    imageVector = MiuixIcons.Close,
                                    contentDescription = "退出多选",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        } else {
                            SettingsBackButton(
                                onBack = onBack,
                                progressiveTitleBlur = progressiveTitleBlur,
                                backCardAlpha = backCardAlpha,
                                backCardMaterialModifier = backCardMaterialModifier,
                                iconTint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        if (isMultiSelectMode) {
                            val allSelected = whitelist.isNotEmpty() && selectedHosts.size == whitelist.size
                            IconButton(onClick = {
                                if (allSelected) selectedHosts.clear() else {
                                    selectedHosts.clear()
                                    selectedHosts.addAll(whitelist)
                                }
                            }) {
                                Icon(
                                    imageVector = MiuixIcons.SelectAll,
                                    contentDescription = if (allSelected) "取消全选" else "全选",
                                    tint = if (allSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f) // ★ 修复阴影被卡片盖住的问题
                    .padding(bottom = navBarBottom + 24.dp)
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(28.dp)
                        shadowElevation = 24.dp.toPx()
                        ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                        spotShadowColor = Color.Transparent
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .then(
                        buildBackCardMaterialModifier(
                            materialStyle = materialStyle,
                            blurSupported = blurSupported,
                            backdrop = backdrop,
                            themeIsDark = themeIsDark,
                            shape = RoundedCornerShape(28.dp)
                        )
                    )
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isMultiSelectMode) {
                    IconButton(onClick = {
                        if (selectedHosts.isNotEmpty()) {
                            showDeleteConfirmDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = MiuixIcons.Add,
                            contentDescription = "添加",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        TrackingWhitelistAddDialogMiuix(
            show = showAddDialog,
            prefs = prefs,
            onDismiss = { showAddDialog = false },
            onAdd = { host ->
                SecurityStateHolder.addToTrackingWhitelist(host)
                showAddDialog = false
                Toast.makeText(context, "已添加到白名单", Toast.LENGTH_SHORT).show()
            },
        )

        WindowDialog(
            show = showDeleteConfirmDialog,
            title = "删除",
            onDismissRequest = { showDeleteConfirmDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "确定要移除选中的 ${selectedHosts.size} 条白名单吗？",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { showDeleteConfirmDialog = false },
                        modifier = Modifier.weight(1f),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            selectedHosts.toList().forEach {
                                SecurityStateHolder.removeFromTrackingWhitelist(it)
                            }
                            Toast.makeText(context, "已移除选中的白名单", Toast.LENGTH_SHORT).show()
                            exitMultiSelect()
                            showDeleteConfirmDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("删除") }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TrackingWhitelistPageMaterial(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val whitelist = SecurityStateHolder.getTrackingWhitelist()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedHosts = remember { mutableStateListOf<String>() }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedHosts.clear()
    }

    fun toggleSelect(host: String) {
        if (host in selectedHosts) selectedHosts.remove(host)
        else selectedHosts.add(host)
    }

    androidx.activity.compose.BackHandler(enabled = isMultiSelectMode) {
        exitMultiSelect()
    }

    val pageBg = M3MaterialTheme.colorScheme.surface
    val navBarBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    M3TopAppBar(
                        title = {
                            M3Text(
                                if (isMultiSelectMode) {
                                    if (selectedHosts.isEmpty()) "请选择项目" else "已选择 ${selectedHosts.size} 项"
                                } else "跟踪防护白名单"
                            )
                        },
                        navigationIcon = {
                            M3IconButton(onClick = {
                                if (isMultiSelectMode) exitMultiSelect() else onBack()
                            }) {
                                M3Icon(
                                    imageVector = if (isMultiSelectMode) MiuixIcons.Close else MiuixIcons.Back,
                                    contentDescription = if (isMultiSelectMode) "退出多选" else "返回",
                                )
                            }
                        },
                        actions = {
                            if (isMultiSelectMode) {
                                val allSelected = whitelist.isNotEmpty() && selectedHosts.size == whitelist.size
                                M3IconButton(onClick = {
                                    if (allSelected) selectedHosts.clear() else {
                                        selectedHosts.clear()
                                        selectedHosts.addAll(whitelist)
                                    }
                                }) {
                                    M3Icon(
                                        imageVector = MiuixIcons.SelectAll,
                                        contentDescription = if (allSelected) "取消全选" else "全选",
                                        tint = if (allSelected) M3MaterialTheme.colorScheme.primary else M3MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        },
                        colors = M3TopAppBarDefaults.topAppBarColors(
                            containerColor = pageBg,
                        ),
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = navBarBottom + 120.dp,
                    ),
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (whitelist.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(top = 12.dp, bottom = 12.dp)
                                ) {
                                    M3Surface(
                                        shape = RoundedCornerShape(25.dp),
                                        color = lerp(
                                            M3MaterialTheme.colorScheme.surfaceContainerLow,
                                            M3MaterialTheme.colorScheme.secondaryContainer,
                                            0.10f,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 56.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                M3Icon(
                                                    imageVector = MiuixIcons.Link,
                                                    contentDescription = null,
                                                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(48.dp),
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                M3Text(
                                                    text = "暂无跟踪防护白名单",
                                                    style = M3MaterialTheme.typography.bodyMedium,
                                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                                ) {
                                    val items: List<@Composable () -> Unit> = whitelist.map { host ->
                                        {
                                            val isSelected = host in selectedHosts
                                            M3SegmentItem(
                                                headline = { M3Text(host) },
                                                trailing = if (isMultiSelectMode) {
                                                    {
                                                        androidx.compose.material3.Checkbox(
                                                            checked = isSelected,
                                                            onCheckedChange = null,
                                                        )
                                                    }
                                                } else null,
                                                onClick = null,
                                                modifier = Modifier.combinedClickable(
                                                    onClick = {
                                                        if (isMultiSelectMode) toggleSelect(host)
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(host)
                                                        }
                                                    }
                                                )
                                            )
                                        }
                                    }
                                    MoteSegmentedColumn(content = items)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarBottom + 24.dp)
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(28.dp)
                        shadowElevation = 8.dp.toPx()
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(M3MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isMultiSelectMode) {
                    M3IconButton(onClick = {
                        if (selectedHosts.isNotEmpty()) {
                            showDeleteConfirmDialog = true
                        }
                    }) {
                        M3Icon(
                            imageVector = MiuixIcons.Delete,
                            contentDescription = "删除",
                            tint = M3MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    M3IconButton(onClick = { showAddDialog = true }) {
                        M3Icon(
                            imageVector = MiuixIcons.Add,
                            contentDescription = "添加",
                            tint = M3MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            TrackingWhitelistAddDialogMaterial(
                prefs = prefs,
                onDismiss = { showAddDialog = false },
                onAdd = { host ->
                    SecurityStateHolder.addToTrackingWhitelist(host)
                    showAddDialog = false
                    Toast.makeText(context, "已添加到白名单", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showDeleteConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { M3Text("删除") },
                text = { M3Text("确定要移除选中的 ${selectedHosts.size} 条白名单吗？") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            selectedHosts.toList().forEach {
                                SecurityStateHolder.removeFromTrackingWhitelist(it)
                            }
                            Toast.makeText(context, "已移除选中的白名单", Toast.LENGTH_SHORT).show()
                            exitMultiSelect()
                            showDeleteConfirmDialog = false
                        }
                    ) { M3Text("删除") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showDeleteConfirmDialog = false }
                    ) { M3Text("取消") }
                },
            )
        }
    }
}

// ============================================================
// 添加白名单对话框
// ============================================================

@Composable
private fun TrackingWhitelistAddDialogMiuix(
    show: Boolean,
    prefs: SharedPreferences,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var urlInput by remember { mutableStateOf("") }
    var showBookmarkPicker by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            urlInput = ""
            showBookmarkPicker = false
        }
    }

    WindowDialog(
        show = show && !showBookmarkPicker,
        title = "添加网址",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = "网址",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showBookmarkPicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "从书签添加",
                    fontSize = 15.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val url = urlInput.trim()
                        if (url.isNotBlank()) onAdd(url)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("添加") }
            }
        }
    }

    WindowDialog(
        show = show && showBookmarkPicker,
        title = "选择书签",
        onDismissRequest = { showBookmarkPicker = false },
    ) {
        val bookmarks = remember { loadFlatBookmarksForWhitelist(prefs) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无书签",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                ) {
                    items(
                        count = bookmarks.size,
                        key = { idx -> "bm_${bookmarks[idx].second}_$idx" },
                    ) { idx ->
                        val (title, url) = bookmarks[idx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    urlInput = url
                                    showBookmarkPicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title.ifBlank { url },
                                    fontSize = 15.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = url,
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { showBookmarkPicker = false },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("取消") }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TrackingWhitelistAddDialogMaterial(
    prefs: SharedPreferences,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var urlInput by remember { mutableStateOf("") }
    var showBookmarkPicker by remember { mutableStateOf(false) }

    if (!showBookmarkPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { M3Text("添加网址") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { M3Text("网址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(M3MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { showBookmarkPicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        M3Text(
                            text = "从书签添加",
                            style = M3MaterialTheme.typography.bodyMedium,
                            color = M3MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val url = urlInput.trim()
                        if (url.isNotBlank()) onAdd(url)
                    },
                    enabled = urlInput.isNotBlank(),
                ) { M3Text("添加") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    M3Text("取消")
                }
            },
        )
    } else {
        val bookmarks = remember { loadFlatBookmarksForWhitelist(prefs) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBookmarkPicker = false },
            title = { M3Text("选择书签") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                ) {
                    if (bookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            M3Text(
                                text = "暂无书签",
                                style = M3MaterialTheme.typography.bodyMedium,
                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(
                                count = bookmarks.size,
                                key = { idx -> "bm_${bookmarks[idx].second}_$idx" },
                            ) { idx ->
                                val (title, url) = bookmarks[idx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            urlInput = url
                                            showBookmarkPicker = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        M3Text(
                                            text = title.ifBlank { url },
                                            style = M3MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        M3Text(
                                            text = url,
                                            style = M3MaterialTheme.typography.bodySmall,
                                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showBookmarkPicker = false }
                ) { M3Text("取消") }
            },
        )
    }
}

private fun loadFlatBookmarksForWhitelist(
    prefs: SharedPreferences,
): List<Pair<String, String>> {
    val raw = prefs.getString("bookmarks_v2", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        val result = mutableListOf<Pair<String, String>>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            collectFlatBookmarkForWhitelist(obj, result)
        }
        result
    } catch (_: Exception) {
        emptyList()
    }
}

private fun collectFlatBookmarkForWhitelist(
    obj: JSONObject,
    out: MutableList<Pair<String, String>>,
) {
    if (obj.optBoolean("isFolder", false)) {
        val children = obj.optJSONArray("children") ?: return
        for (i in 0 until children.length()) {
            val c = children.optJSONObject(i) ?: continue
            collectFlatBookmarkForWhitelist(c, out)
        }
    } else {
        val url = obj.optString("url", "")
        if (url.isNotBlank()) {
            out.add(obj.optString("title", url) to url)
        }
    }
}
