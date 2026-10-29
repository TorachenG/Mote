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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.foundation.lazy.items
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.PrimaryTabRow as M3PrimaryTabRow
import androidx.compose.material3.Tab as M3Tab
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
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

private const val KEY_DOWNLOAD_PROTECTION = "web_download_protection"
private const val KEY_DISABLE_WEBRTC = "web_disable_webrtc"
private const val KEY_BLOCK_REDIRECTS = "web_block_redirects"
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
    val downloadProtectionEnabled: Boolean,
    val disableWebRtcEnabled: Boolean,
    val blockRedirectsEnabled: Boolean,
    val blockAdsGlobalEnabled: Boolean,
    val trackingProtectionGlobalEnabled: Boolean,
    val trackingLevel: Int,
)

@Immutable
private data class WebSecurityActions(
    val onRememberFormChange: (Boolean) -> Unit,
    val onAcceptCookieChange: (Boolean) -> Unit,
    val onClearIncognitoOnExitChange: (Boolean) -> Unit,
    val onDownloadProtectionChange: (Boolean) -> Unit,
    val onDisableWebRtcChange: (Boolean) -> Unit,
    val onBlockRedirectsChange: (Boolean) -> Unit,
    val onBlockAdsGlobalChange: (Boolean) -> Unit,
    val onTrackingProtectionGlobalChange: (Boolean) -> Unit,
    val onTrackingLevelChange: (Int) -> Unit,
    val onOpenWhitelist: () -> Unit,
    val onOpenSiteManager: () -> Unit,
    val onOpenDownloadWhitelist: () -> Unit,
    val onOpenAdWhitelist: () -> Unit,
    val onOpenWebPermissions: () -> Unit,
    val onBack: () -> Unit,
)

// ============================================================
// 入口
// ============================================================


/** 顶部盾牌与三张统计卡 */
@Composable
private fun SecurityShieldHeader() {
    val context = LocalContext.current
    val smart = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_SMART)
    val privacy = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_PRIVACY)
    val permission = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_PERMISSION)
    val total = smart + privacy + permission
    val primary = MiuixTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 14.dp)
                .size(148.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                        ),
                        CircleShape,
                    ),
            )
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(124.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = total.toString(), fontSize = 30.sp, color = Color.White)
                Text(text = "累计防护", fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecurityStatCard("$smart", "智能拦截", Modifier.weight(1f))
            SecurityStatCard("$privacy", "隐私保护", Modifier.weight(1f))
            SecurityStatCard("$permission", "权限管控", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SecurityStatCard(count: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$count 次",
                fontSize = MiuixTheme.textStyles.body1.fontSize,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
fun SettingsWebSecurityPage(
    onBack: () -> Unit,
    onOpenWhitelist: () -> Unit = {},
    onOpenSiteManager: () -> Unit = {},
    onOpenDownloadWhitelist: () -> Unit = {},
    onOpenAdWhitelist: () -> Unit = {},
    onOpenWebPermissions: () -> Unit = {},
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
    var downloadProtectionEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DOWNLOAD_PROTECTION, true))
    }
    var disableWebRtcEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DISABLE_WEBRTC, false))
    }
    var blockRedirectsEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BLOCK_REDIRECTS, false))
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
        downloadProtectionEnabled = downloadProtectionEnabled,
        disableWebRtcEnabled = disableWebRtcEnabled,
        blockRedirectsEnabled = blockRedirectsEnabled,
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
        onDownloadProtectionChange = {
            downloadProtectionEnabled = it
            prefs.edit().putBoolean(KEY_DOWNLOAD_PROTECTION, it).apply()
        },
        onDisableWebRtcChange = {
            disableWebRtcEnabled = it
            prefs.edit().putBoolean(KEY_DISABLE_WEBRTC, it).apply()
        },
        onBlockRedirectsChange = {
            blockRedirectsEnabled = it
            prefs.edit().putBoolean(KEY_BLOCK_REDIRECTS, it).apply()
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
        onOpenSiteManager = onOpenSiteManager,
        onOpenDownloadWhitelist = onOpenDownloadWhitelist,
        onOpenAdWhitelist = onOpenAdWhitelist,
        onOpenWebPermissions = onOpenWebPermissions,
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
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            SecurityShieldHeader()

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "网页管理",
                                    summary = "查看并单独管理网页",
                                    onClick = actions.onOpenSiteManager,
                                )
                            }

                            SmallTitle("权限")
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
                                        summary = "允许网站保存和读取 Cookie",
                                        checked = state.acceptCookieEnabled,
                                        onCheckedChange = actions.onAcceptCookieChange,
                                    )
                                }
                                SearchableItem(
                                    title = "网页权限",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_web_permissions",
                                ) {
                                    ArrowPreference(
                                        title = "网页权限",
                                        summary = "管理相机、定位、剪贴板等权限",
                                        onClick = actions.onOpenWebPermissions,
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
                                            itemKey = "settings_web_security_tracking_level_0",
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
                                            itemKey = "settings_web_security_tracking_level_1",
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
                                            itemKey = "settings_web_security_tracking_level_2",
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
                                SearchableItem(
                                    title = "下载防护",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_download_protection",
                                ) {
                                    SwitchPreference(
                                        title = "下载防护",
                                        summary = "拦截网页发起的可疑可执行文件下载",
                                        checked = state.downloadProtectionEnabled,
                                        onCheckedChange = actions.onDownloadProtectionChange,
                                    )
                                }
                                SearchableItem(
                                    title = "广告拦截",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_block_ads",
                                ) {
                                    SwitchPreference(
                                        title = "广告拦截",
                                        summary = "阻止网页中的广告请求",
                                        checked = state.blockAdsGlobalEnabled,
                                        onCheckedChange = actions.onBlockAdsGlobalChange,
                                    )
                                }
                                SearchableItem(
                                    title = "跳转拦截",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_block_redirects",
                                ) {
                                    SwitchPreference(
                                        title = "跳转拦截",
                                        summary = "阻止网页自动跳转到其它站点",
                                        checked = state.blockRedirectsEnabled,
                                        onCheckedChange = actions.onBlockRedirectsChange,
                                    )
                                }
                                SearchableItem(
                                    title = "禁用 WebRTC",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_disable_webrtc",
                                ) {
                                    SwitchPreference(
                                        title = "禁用 WebRTC",
                                        summary = "阻止网页通过 WebRTC 暴露本机 IP 地址",
                                        checked = state.disableWebRtcEnabled,
                                        onCheckedChange = actions.onDisableWebRtcChange,
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                                insideMargin = PaddingValues(0.dp),
                            ) {
                                ArrowPreference(
                                    title = "防护白名单",
                                    onClick = actions.onOpenWhitelist,
                                )
                            }

                            SmallTitle("辅助功能")
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "无痕浏览窗口清空",
                                    pageRoute = "settings_web_security",
                                    pageTitle = "网页安全中心",
                                    itemKey = "settings_web_security_clear_incognito",
                                ) {
                                    SwitchPreference(
                                        title = "无痕浏览窗口清空",
                                        summary = "退出无痕浏览时清空浏览数据",
                                        checked = state.clearIncognitoOnExitEnabled,
                                        onCheckedChange = actions.onClearIncognitoOnExitChange,
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
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                item(key = "shield_header") {
                    SecurityShieldHeaderMaterial()
                }

                item(key = "site_manager") {
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
                                    M3SegmentArrowItem(title = "网页管理", onClick = actions.onOpenSiteManager)
                                },
                            ),
                        )
                    }
                }

                item(key = "perm_header") {
                    WebSecuritySectionHeader("权限")
                }

                item(key = "perm_items") {
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
                                            M3SegmentSwitchItem(
                                                title = "记住表单数据",
                                                checked = state.rememberFormEnabled,
                                                onCheckedChange = actions.onRememberFormChange,
                                            )
                                        },
                                        {
                                M3SegmentArrowItem(
                                    title = "网页权限",
                                    onClick = actions.onOpenWebPermissions,
                                )
                            },
                            {
                                            M3SegmentSwitchItem(
                                                title = "接受 Cookie",
                                                checked = state.acceptCookieEnabled,
                                                onCheckedChange = actions.onAcceptCookieChange,
                                            )
                                        },
                            ),
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
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                        {
                                            M3SegmentSwitchItem(
                                                title = "跟踪防护",
                                                checked = state.trackingProtectionGlobalEnabled,
                                                onCheckedChange = actions.onTrackingProtectionGlobalChange,
                                            )
                                        },
                                        {
                                            if (state.trackingProtectionGlobalEnabled) {
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
                                        },
                                        {
                                            if (state.trackingProtectionGlobalEnabled) {
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
                                        },
                                        {
                                            if (state.trackingProtectionGlobalEnabled) {
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
                                        },
                                        {
                                            M3SegmentSwitchItem(
                                                title = "下载防护",
                                                checked = state.downloadProtectionEnabled,
                                                onCheckedChange = actions.onDownloadProtectionChange,
                                            )
                                        },
                                        {
                                            M3SegmentSwitchItem(
                                                title = "广告拦截",
                                                checked = state.blockAdsGlobalEnabled,
                                                onCheckedChange = actions.onBlockAdsGlobalChange,
                                            )
                                        },
                                        {
                                            M3SegmentSwitchItem(
                                                title = "跳转拦截",
                                                checked = state.blockRedirectsEnabled,
                                                onCheckedChange = actions.onBlockRedirectsChange,
                                            )
                                        },
                                        {
                                            M3SegmentSwitchItem(
                                                title = "禁用 WebRTC",
                                                checked = state.disableWebRtcEnabled,
                                                onCheckedChange = actions.onDisableWebRtcChange,
                                            )
                                        },
                            ),
                        )
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
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentArrowItem(
                                        title = "防护白名单",
                                        onClick = actions.onOpenWhitelist,
                                    )
                                },
                            ),
                        )
                    }
                }

                item(key = "aux_header") {
                    WebSecuritySectionHeader("辅助功能")
                }

                item(key = "aux_items") {
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
                                            M3SegmentSwitchItem(
                                                title = "无痕浏览窗口清空",
                                                checked = state.clearIncognitoOnExitEnabled,
                                                onCheckedChange = actions.onClearIncognitoOnExitChange,
                                            )
                                        },
                            ),
                        )
                    }
                }

            }
        }
    }
}


@Composable
private fun SecurityShieldHeaderMaterial() {
    val context = LocalContext.current
    val smart = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_SMART)
    val privacy = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_PRIVACY)
    val permission = WebSecurityStats.count(context, WebSecurityStats.CATEGORY_PERMISSION)
    val total = smart + privacy + permission
    val primary = M3MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 14.dp)
                .size(148.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(148.dp)
                    .background(
                        Brush.radialGradient(listOf(primary.copy(alpha = 0.20f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            M3Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(124.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                M3Text("$total", color = M3MaterialTheme.colorScheme.onPrimary, style = M3MaterialTheme.typography.headlineMedium)
                M3Text("累计防护", color = M3MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), style = M3MaterialTheme.typography.labelSmall)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecurityStatCardMaterial("$smart", "智能拦截", Modifier.weight(1f))
            SecurityStatCardMaterial("$privacy", "隐私保护", Modifier.weight(1f))
            SecurityStatCardMaterial("$permission", "权限管控", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SecurityStatCardMaterial(count: String, label: String, modifier: Modifier = Modifier) {
    M3Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = M3MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            M3Text("$count 次", style = M3MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(3.dp))
            M3Text(label, style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
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
// 白名单通用（跟踪 / 下载 / 广告）
// ============================================================

private const val WL_TRACKING = "tracking"
private const val WL_DOWNLOAD = "download"
private const val WL_AD = "ad"

private fun whitelistOf(kind: String): List<String> = when (kind) {
    WL_DOWNLOAD -> SecurityStateHolder.getDownloadWhitelist()
    WL_AD -> SecurityStateHolder.getAdWhitelist()
    else -> SecurityStateHolder.getTrackingWhitelist()
}

private fun whitelistAdd(kind: String, host: String) {
    when (kind) {
        WL_DOWNLOAD -> SecurityStateHolder.addToDownloadWhitelist(host)
        WL_AD -> SecurityStateHolder.addToAdWhitelist(host)
        else -> SecurityStateHolder.addToTrackingWhitelist(host)
    }
}

private fun whitelistRemove(kind: String, host: String) {
    when (kind) {
        WL_DOWNLOAD -> SecurityStateHolder.removeFromDownloadWhitelist(host)
        WL_AD -> SecurityStateHolder.removeFromAdWhitelist(host)
        else -> SecurityStateHolder.removeFromTrackingWhitelist(host)
    }
}

private fun whitelistTitle(kind: String): String = when (kind) {
    WL_DOWNLOAD -> "下载防护白名单"
    WL_AD -> "广告拦截白名单"
    else -> "跟踪防护白名单"
}

private fun whitelistEmptyText(kind: String): String = when (kind) {
    WL_DOWNLOAD -> "暂无下载防护白名单"
    WL_AD -> "暂无广告拦截白名单"
    else -> "暂无跟踪防护白名单"
}

// ============================================================
// 白名单页
// ============================================================

@Composable
fun TrackingWhitelistPage(onBack: () -> Unit) = WhitelistPage(WL_TRACKING, onBack)

@Composable
fun DownloadWhitelistPage(onBack: () -> Unit) = WhitelistPage(WL_DOWNLOAD, onBack)

@Composable
fun AdWhitelistPage(onBack: () -> Unit) = WhitelistPage(WL_AD, onBack)

@Composable
fun WhitelistPage(kind: String, onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> TrackingWhitelistPageMiuix(kind, onBack)
        UiMode.Material -> TrackingWhitelistPageMaterial(kind, onBack)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackingWhitelistPageMiuix(kind: String, onBack: () -> Unit) {
    var currentKind by remember(kind) { mutableStateOf(kind) }

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

    val whitelist = whitelistOf(currentKind)

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
                        top = contentTopPadding + 64.dp,
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
                                                text = whitelistEmptyText(currentKind),
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
                                                .heightIn(min = 56.dp)
                                                .padding(horizontal = 16.dp, vertical = 13.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = host,
                                                style = MiuixTheme.textStyles.headline1,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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
                    whitelistTitle(currentKind)
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

            Box(modifier = Modifier.fillMaxWidth().padding(top = contentTopPadding)
                    .padding(horizontal = 12.dp)) {
                TabRowWithContour(
                    tabs = listOf("跟踪防护", "下载防护", "广告拦截"),
                    selectedTabIndex = when (currentKind) {
                        WL_DOWNLOAD -> 1
                        WL_AD -> 2
                        else -> 0
                    },
                    onTabSelected = { idx ->
                        currentKind = when (idx) {
                            1 -> WL_DOWNLOAD
                            2 -> WL_AD
                            else -> WL_TRACKING
                        }
                        isMultiSelectMode = false
                        selectedHosts.clear()
                    },
                    modifier = Modifier.fillMaxWidth(),
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
                whitelistAdd(currentKind, host)
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
                                whitelistRemove(currentKind, it)
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
private fun TrackingWhitelistPageMaterial(kind: String, onBack: () -> Unit) {
    var currentKind by remember(kind) { mutableStateOf(kind) }

    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val whitelist = whitelistOf(currentKind)

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

    val pageBg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )
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
                                } else whitelistTitle(currentKind)
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
            M3PrimaryTabRow(
                selectedTabIndex = when (currentKind) {
                    WL_DOWNLOAD -> 1
                    WL_AD -> 2
                    else -> 0
                },
                containerColor = pageBg,
            ) {
                listOf("跟踪防护", "下载防护", "广告拦截").forEachIndexed { idx, title ->
                    M3Tab(
                        selected = when (currentKind) {
                            WL_DOWNLOAD -> idx == 1
                            WL_AD -> idx == 2
                            else -> idx == 0
                        },
                        onClick = {
                            currentKind = when (idx) {
                                1 -> WL_DOWNLOAD
                                2 -> WL_AD
                                else -> WL_TRACKING
                            }
                            isMultiSelectMode = false
                            selectedHosts.clear()
                        },
                        text = { M3Text(title) },
                    )
                }
            }
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
                                                    text = whitelistEmptyText(currentKind),
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
                    whitelistAdd(currentKind, host)
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
                                whitelistRemove(currentKind, it)
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


// ============================================================
// 网页管理
// ============================================================

internal object WebSiteNavArgs {
    var host: String? = null
}

@Composable
fun WebSiteManagerPage(onBack: () -> Unit, onOpenSite: (String) -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> WebSiteManagerPageMiuix(onBack, onOpenSite)
        UiMode.Material -> WebSiteManagerPageMaterial(onBack, onOpenSite)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WebSiteManagerPageMiuix(onBack: () -> Unit, onOpenSite: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val scrollBehavior = MiuixScrollBehavior()
    val surface = MiuixTheme.colorScheme.surface

    val version = SecurityStateHolder.whitelistVersion
    val sites = remember(version) { SecurityStateHolder.getManagedSites() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selecting by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    fun exitSelect() { selecting = false; selected.clear() }
    androidx.activity.compose.BackHandler(enabled = selecting) { exitSelect() }

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = if (selecting) {
                    if (selected.isEmpty()) "请选择项目" else "已选择 ${selected.size} 项"
                } else "网页管理",
                largeTitle = if (selecting) {
                    if (selected.isEmpty()) "请选择项目" else "已选择 ${selected.size} 项"
                } else "网页管理",
                color = Color.Transparent,
                defaultWindowInsetsPadding = true,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { if (selecting) exitSelect() else onBack() }) {
                        Icon(
                            imageVector = if (selecting) MiuixIcons.Close else MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                overscrollEffect = null,
            ) {
                if (sites.isEmpty()) {
                    item(key = "empty") {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(top = 12.dp)
                                .padding(bottom = 12.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
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
                                        text = "暂无单独设置的网站",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                    }
                }
                items(sites) { host ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (selecting) {
                                            if (selected.contains(host)) selected.remove(host) else selected.add(host)
                                        } else {
                                            onOpenSite(host)
                                        }
                                    },
                                    onLongClick = {
                                        selecting = true
                                        if (!selected.contains(host)) selected.add(host)
                                    },
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (selecting) {
                                Checkbox(
                                    state = if (selected.contains(host)) ToggleableState.On else ToggleableState.Off,
                                    onClick = null,
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(
                                text = host,
                                style = MiuixTheme.textStyles.headline1,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            if (!selecting) {
                                Icon(
                                    imageVector = MiuixIcons.ChevronForward,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(10f)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                .padding(horizontal = 24.dp)
                .height(56.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(28.dp)
                    shadowElevation = 24.dp.toPx()
                    ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                    spotShadowColor = Color.Transparent
                }
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selecting) {
                IconButton(onClick = { if (selected.isNotEmpty()) showDeleteDialog = true }) {
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

        TrackingWhitelistAddDialogMiuix(
            show = showAddDialog,
            prefs = prefs,
            onDismiss = { showAddDialog = false },
            onAdd = { host ->
                SecurityStateHolder.addManagedSite(host)
                showAddDialog = false
                Toast.makeText(context, "已添加网站", Toast.LENGTH_SHORT).show()
            },
        )

        WindowDialog(
            show = showDeleteDialog,
            title = "删除",
            onDismissRequest = { showDeleteDialog = false },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "确定要移除选中的 ${selected.size} 个网站吗？",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { showDeleteDialog = false }, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(
                        onClick = {
                            selected.toList().forEach { SecurityStateHolder.removeManagedSite(it) }
                            exitSelect()
                            showDeleteDialog = false
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
private fun WebSiteManagerPageMaterial(onBack: () -> Unit, onOpenSite: (String) -> Unit) {
    val context = LocalContext.current
    val bg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )
    val version = SecurityStateHolder.whitelistVersion
    val sites = remember(version) { SecurityStateHolder.getManagedSites() }

    var selecting by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    fun exitSelect() { selecting = false; selected.clear() }
    androidx.activity.compose.BackHandler(enabled = selecting) { exitSelect() }

    fun showAddDialog() {
        val input = android.widget.EditText(context).apply {
            hint = "example.com"
            setSingleLine()
        }
        android.app.AlertDialog.Builder(context)
            .setTitle("添加网站")
            .setView(input)
            .setPositiveButton("添加") { _, _ ->
                val host = input.text?.toString()?.trim().orEmpty()
                if (host.isNotBlank()) SecurityStateHolder.addManagedSite(host)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        M3Scaffold(
            containerColor = bg,
            topBar = {
                M3TopAppBar(
                    title = {
                        M3Text(
                            if (selecting) {
                                if (selected.isEmpty()) "请选择项目" else "已选择 ${selected.size} 项"
                            } else "网页管理"
                        )
                    },
                    navigationIcon = {
                        M3IconButton(onClick = { if (selecting) exitSelect() else onBack() }) {
                            M3Icon(
                                if (selecting) Icons.Filled.Close else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "返回",
                            )
                        }
                    },
                    actions = {
                        if (selecting) {
                            M3IconButton(onClick = {
                                selected.toList().forEach { SecurityStateHolder.removeManagedSite(it) }
                                exitSelect()
                            }) {
                                M3Icon(Icons.Filled.Delete, contentDescription = "删除")
                            }
                        } else {
                            M3IconButton(onClick = { showAddDialog() }) {
                                M3Icon(Icons.Filled.Add, contentDescription = "添加")
                            }
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
                )
            },
        ) { innerPadding ->
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .overScrollVertical(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                overscrollEffect = null,
            ) {
                if (sites.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                .padding(top = 12.dp, bottom = 12.dp)
                        ) {
                            M3Surface(
                                shape = RoundedCornerShape(25.dp),
                                color = lerp(
                                    M3MaterialTheme.colorScheme.surface,
                                    M3MaterialTheme.colorScheme.secondaryContainer,
                                    0.10f,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
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
                                            text = "暂无单独设置的网站",
                                            style = M3MaterialTheme.typography.bodyMedium,
                                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "site_list") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = sites.map { host ->
                                {
                                    Box(
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                if (selecting) {
                                                    if (selected.contains(host)) selected.remove(host) else selected.add(host)
                                                } else {
                                                    onOpenSite(host)
                                                }
                                            },
                                            onLongClick = {
                                                selecting = true
                                                if (!selected.contains(host)) selected.add(host)
                                            },
                                        ),
                                    ) {
                                        if (selecting) {
                                            androidx.compose.foundation.layout.Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                androidx.compose.material3.Checkbox(
                                                    checked = selected.contains(host),
                                                    onCheckedChange = null,
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                M3Text(host, style = M3MaterialTheme.typography.bodyLarge)
                                            }
                                        } else {
                                            M3SegmentArrowItem(title = host, onClick = { onOpenSite(host) })
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebSiteSettingsPage(host: String, onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> WebSiteSettingsPageMiuix(host, onBack)
        UiMode.Material -> WebSiteSettingsPageMaterial(host, onBack)
    }
}

@Composable
private fun WebSiteSettingsPageMiuix(host: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val scrollBehavior = MiuixScrollBehavior()
    val surface = MiuixTheme.colorScheme.surface

    val site = remember(host) { SecurityStateHolder.getSiteProtection(host) }
    var siteBlockAds by remember(host) { mutableStateOf(site.blockAds) }
    var siteTracking by remember(host) { mutableStateOf(site.trackingProtection) }
    val siteSettings = remember(host) { SecurityStateHolder.getSiteProtection(host) }
    var downloadProtection by remember(host) { mutableStateOf(siteSettings.downloadProtection) }
    var disableWebRtc by remember(host) { mutableStateOf(siteSettings.webrtcDisabled) }
    var blockRedirects by remember(host) { mutableStateOf(siteSettings.blockRedirects) }
    var rememberForm by remember { mutableStateOf(prefs.getBoolean(KEY_REMEMBER_FORM, true)) }
    var acceptCookie by remember { mutableStateOf(prefs.getBoolean(KEY_ACCEPT_COOKIE, true)) }

    Column(modifier = Modifier.fillMaxSize().background(surface)) {
        TopAppBar(
            title = "网站设置",
            largeTitle = "网站设置",
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = MiuixIcons.ChevronBackward, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                }
            },
        )
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            item(key = "host") {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 12.dp),
                ) {
                    top.yukonga.miuix.kmp.basic.BasicComponent(title = host)
                }
            }
            item(key = "protect") {
                SmallTitle("防护")
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                ) {
                    SwitchPreference(
                        title = "跟踪防护",
                        summary = "限制该网站跨站收集浏览数据",
                        checked = siteTracking,
                        onCheckedChange = {
                            siteTracking = it
                            SecurityStateHolder.setSiteTrackingProtection(host, it)
                        },
                    )
                    SwitchPreference(
                        title = "下载防护",
                        summary = "拦截网页发起的可疑可执行文件下载",
                        checked = downloadProtection,
                        onCheckedChange = {
                            downloadProtection = it
                            SecurityStateHolder.setSiteDownloadProtection(host, it)
                        },
                    )
                    SwitchPreference(
                        title = "广告拦截",
                        summary = "阻止该网站中的广告请求",
                        checked = siteBlockAds,
                        onCheckedChange = {
                            siteBlockAds = it
                            SecurityStateHolder.setSiteBlockAds(host, it)
                        },
                    )
                    SwitchPreference(
                        title = "跳转拦截",
                        summary = "阻止网页自动跳转到其它站点",
                        checked = blockRedirects,
                        onCheckedChange = {
                            blockRedirects = it
                            SecurityStateHolder.setSiteBlockRedirects(host, it)
                        },
                    )
                    SwitchPreference(
                        title = "禁用 WebRTC",
                        summary = "阻止网页通过 WebRTC 暴露本机 IP 地址",
                        checked = disableWebRtc,
                        onCheckedChange = {
                            disableWebRtc = it
                            SecurityStateHolder.setSiteWebrtc(host, it)
                        },
                    )
                }
            }
            item(key = "permission") {
                SmallTitle("网页权限")
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                ) {
                    SwitchPreference(
                        title = "记住表单数据",
                        summary = "记住表单中输入的数据以便以后使用",
                        checked = rememberForm,
                        onCheckedChange = {
                            rememberForm = it
                            prefs.edit().putBoolean(KEY_REMEMBER_FORM, it).apply()
                        },
                    )
                    SwitchPreference(
                        title = "接受 Cookie",
                        summary = "允许网站保存和读取 Cookie",
                        checked = acceptCookie,
                        onCheckedChange = {
                            acceptCookie = it
                            prefs.edit().putBoolean(KEY_ACCEPT_COOKIE, it).apply()
                        },
                    )

                    listOf(
                        WebPermissionStore.LOCATION to "定位",
                        WebPermissionStore.CAMERA to "相机",
                        WebPermissionStore.MICROPHONE to "麦克风",
                        WebPermissionStore.AUTOPLAY to "播放声音",
                        WebPermissionStore.CLIPBOARD to "剪贴板",
                        WebPermissionStore.POPUP_WINDOW to "新建窗口",
                        WebPermissionStore.DIALOG to "弹出提示",
                        WebPermissionStore.VIDEO_AUTOPLAY to "自动播放视频",
                    ).forEach { (kind, name) ->
                        val siteMode = WebPermissionStore.siteMode(context, host, kind) ?: WebPermissionStore.ASK
                        WindowDropdownPreference(
                            title = name,
                            entries = listOf(
                                DropdownEntry(
                                    items = listOf(
                                        DropdownItem("询问", selected = siteMode == WebPermissionStore.ASK, onClick = { WebPermissionStore.setSiteMode(context, host, kind, WebPermissionStore.ASK) }),
                                        DropdownItem("允许", selected = siteMode == WebPermissionStore.ALLOW, onClick = { WebPermissionStore.setSiteMode(context, host, kind, WebPermissionStore.ALLOW) }),
                                        DropdownItem("拒绝", selected = siteMode == WebPermissionStore.DENY, onClick = { WebPermissionStore.setSiteMode(context, host, kind, WebPermissionStore.DENY) }),
                                    ),
                                ),
                            ),
                            collapseOnSelection = true,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WebSiteSettingsPageMaterial(host: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val bg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    val site = remember(host) { SecurityStateHolder.getSiteProtection(host) }
    var siteBlockAds by remember(host) { mutableStateOf(site.blockAds) }
    var siteTracking by remember(host) { mutableStateOf(site.trackingProtection) }
    val siteSettings = remember(host) { SecurityStateHolder.getSiteProtection(host) }
    var downloadProtection by remember(host) { mutableStateOf(siteSettings.downloadProtection) }
    var disableWebRtc by remember(host) { mutableStateOf(siteSettings.webrtcDisabled) }
    var blockRedirects by remember(host) { mutableStateOf(siteSettings.blockRedirects) }
    var rememberForm by remember { mutableStateOf(prefs.getBoolean(KEY_REMEMBER_FORM, true)) }
    var acceptCookie by remember { mutableStateOf(prefs.getBoolean(KEY_ACCEPT_COOKIE, true)) }

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = { M3Text("网站设置") },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            item(key = "host") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                ) {
                    M3Text(
                        host,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        style = M3MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item(key = "permission") {
                WebSecuritySectionHeader("网页权限")
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
                                M3SegmentSwitchItem(
                                    title = "记住表单数据",
                                    checked = rememberForm,
                                    onCheckedChange = {
                                        rememberForm = it
                                        prefs.edit().putBoolean(KEY_REMEMBER_FORM, it).apply()
                                    },
                                )
                            },
                            {
                                M3SegmentSwitchItem(
                                    title = "接受 Cookie",
                                    checked = acceptCookie,
                                    onCheckedChange = {
                                        acceptCookie = it
                                        prefs.edit().putBoolean(KEY_ACCEPT_COOKIE, it).apply()
                                    },
                                )
                            },
                        ),
                    )
                }
            }
            item(key = "protect") {
                WebSecuritySectionHeader("防护")
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
                                M3SegmentSwitchItem(
                                    title = "跟踪防护",
                                    checked = siteTracking,
                                    onCheckedChange = {
                                        siteTracking = it
                                        SecurityStateHolder.setSiteTrackingProtection(host, it)
                                    },
                                )
                            },
                            {
                                M3SegmentSwitchItem(
                                    title = "下载防护",
                                    checked = downloadProtection,
                                    onCheckedChange = {
                                        downloadProtection = it
                                        prefs.edit().putBoolean(KEY_DOWNLOAD_PROTECTION, it).apply()
                                    },
                                )
                            },
                            {
                                M3SegmentSwitchItem(
                                    title = "广告拦截",
                                    checked = siteBlockAds,
                                    onCheckedChange = {
                                        siteBlockAds = it
                                        SecurityStateHolder.setSiteBlockAds(host, it)
                                    },
                                )
                            },
                            {
                                M3SegmentSwitchItem(
                                    title = "跳转拦截",
                                    checked = blockRedirects,
                                    onCheckedChange = {
                                        blockRedirects = it
                                        prefs.edit().putBoolean(KEY_BLOCK_REDIRECTS, it).apply()
                                    },
                                )
                            },
                            {
                                M3SegmentSwitchItem(
                                    title = "禁用 WebRTC",
                                    checked = disableWebRtc,
                                    onCheckedChange = {
                                        disableWebRtc = it
                                        prefs.edit().putBoolean(KEY_DISABLE_WEBRTC, it).apply()
                                    },
                                )
                            },
                        ),
                    )
                }
            }
            item(key = "site_perms") {
                WebSecuritySectionHeader("网页权限")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                ) {
                    var menuFor by remember { mutableStateOf<String?>(null) }
                    val perms = listOf(
                        WebPermissionStore.LOCATION to "定位",
                        WebPermissionStore.CAMERA to "相机",
                        WebPermissionStore.MICROPHONE to "麦克风",
                        WebPermissionStore.AUTOPLAY to "播放声音",
                        WebPermissionStore.CLIPBOARD to "剪贴板",
                        WebPermissionStore.POPUP_WINDOW to "新建窗口",
                        WebPermissionStore.DIALOG to "弹出提示",
                        WebPermissionStore.VIDEO_AUTOPLAY to "自动播放视频",
                    )
                    MoteSegmentedColumn(
                        color = M3MaterialTheme.colorScheme.surface,
                        content = perms.map { (kind, name) ->
                            {
                                val mode = WebPermissionStore.siteMode(context, host, kind)
                                    ?: WebPermissionStore.defaultMode(kind)
                                Box {
                                    M3SegmentArrowItem(
                                        title = "$name：${WebPermissionStore.label(mode)}",
                                        onClick = { menuFor = kind },
                                    )
                                    M3DropdownMenu(
                                        expanded = menuFor == kind,
                                        onDismissRequest = { menuFor = null },
                                    ) {
                                        listOf(
                                            "询问" to WebPermissionStore.ASK,
                                            "允许" to WebPermissionStore.ALLOW,
                                            "拒绝" to WebPermissionStore.DENY,
                                        ).forEach { (label, value) ->
                                            M3DropdownMenuItem(
                                                text = { M3Text(label) },
                                                onClick = {
                                                    WebPermissionStore.setSiteMode(context, host, kind, value)
                                                    menuFor = null
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}



// ============================================================
// 网页权限：三态（询问 / 允许 / 拒绝）与运行时询问
// ============================================================

internal object WebPermissionStore {
    const val ASK = "ask"
    const val ALLOW = "allow"
    const val DENY = "deny"

    const val CAMERA = "camera"
    const val MICROPHONE = "microphone"
    const val LOCATION = "location"
    const val AUTOPLAY = "autoplay"
    const val CLIPBOARD = "clipboard"
    const val POPUP_WINDOW = "popup_window"
    const val DIALOG = "dialog"
    const val VIDEO_AUTOPLAY = "video_autoplay"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)

    private fun key(kind: String) = "web_perm_mode_$kind"

    /** 默认值：播放声音与弹出提示默认允许，其余默认询问 */
    fun defaultMode(kind: String): String = when (kind) {
        AUTOPLAY, DIALOG, VIDEO_AUTOPLAY -> ALLOW
        else -> ASK
    }

    fun mode(context: Context, kind: String): String =
        prefs(context).getString(key(kind), defaultMode(kind)) ?: defaultMode(kind)

    fun setMode(context: Context, kind: String, mode: String) {
        prefs(context).edit().putString(key(kind), mode).apply()
    }

    private fun siteKey(context: Context, host: String, kind: String) =
        "web_site_perm_${'$'}{host}_${'$'}{kind}"

    fun siteMode(context: Context, host: String, kind: String): String? =
        prefs(context).getString(siteKey(context, host, kind), null)

    fun setSiteMode(context: Context, host: String, kind: String, mode: String) {
        prefs(context).edit().putString(siteKey(context, host, kind), mode).apply()
    }

    /** 生效模式：受管站点优先，否则全局 */
    fun effectiveMode(context: Context, host: String, kind: String): String {
        if (host.isNotBlank()) {
            val site = siteMode(context, host, kind)
            if (site != null) return site
        }
        return mode(context, kind)
    }

    fun label(mode: String): String = when (mode) {
        ALLOW -> "允许"
        DENY -> "拒绝"
        else -> "询问"
    }

    fun title(kind: String): String = when (kind) {
        CAMERA -> "相机权限"
        MICROPHONE -> "麦克风权限"
        LOCATION -> "定位权限"
        AUTOPLAY -> "播放声音权限"
        CLIPBOARD -> "剪贴板权限"
        POPUP_WINDOW -> "新建窗口权限"
        DIALOG -> "弹出提示权限"
        VIDEO_AUTOPLAY -> "自动播放视频权限"
        else -> "网页权限"
    }
}

/** 运行时询问：由浏览页触发，主界面负责弹出 */
internal object WebPermissionPrompt {
    data class Pending(
        val kind: String,
        val host: String,
        val onDecision: (allow: Boolean, remember: Boolean, applyAll: Boolean) -> Unit,
    )

    var pending by mutableStateOf<Pending?>(null)

    fun request(
        context: Context,
        kind: String,
        host: String,
        onDecision: (allow: Boolean, remember: Boolean, applyAll: Boolean) -> Unit,
    ): Boolean {
        val mode = WebPermissionStore.effectiveMode(context, host, kind)
        return when (mode) {
            WebPermissionStore.ALLOW -> { onDecision(true, false, false); true }
            WebPermissionStore.DENY -> { onDecision(false, false, false); true }
            else -> {
                pending = Pending(kind, host, onDecision)
                true
            }
        }
    }
}

@Composable
internal fun WebPermissionDialogHost() {
    val context = LocalContext.current
    val pending = WebPermissionPrompt.pending
    var last by remember { mutableStateOf<WebPermissionPrompt.Pending?>(null) }
    androidx.compose.runtime.LaunchedEffect(pending) {
        if (pending != null) last = pending
    }
    var applyAll by remember { mutableStateOf(false) }
    val current = pending ?: last
    val shown = pending != null

    fun decide(allow: Boolean, remember: Boolean) {
        val p = pending ?: return
        if (remember) {
            val value = if (allow) WebPermissionStore.ALLOW else WebPermissionStore.DENY
            if (applyAll) {
                WebPermissionStore.setMode(context, p.kind, value)
            } else if (p.host.isNotBlank()) {
                WebPermissionStore.setSiteMode(context, p.host, p.kind, value)
            }
        }
        p.onDecision(allow, remember, applyAll)
        WebPermissionPrompt.pending = null
        applyAll = false
    }

    WindowDialog(
        show = shown,
        title = WebPermissionStore.title(current?.kind ?: WebPermissionStore.CAMERA),
        onDismissRequest = {
            pending?.onDecision(false, false, false)
            WebPermissionPrompt.pending = null
            applyAll = false
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                text = "当前网页申请获取${WebPermissionStore.title(current?.kind ?: "").removeSuffix("权限")}权限",
                fontSize = MiuixTheme.textStyles.body1.fontSize,
                color = DialogDefaults.summaryColor(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    state = if (applyAll) ToggleableState.On else ToggleableState.Off,
                    onClick = { applyAll = !applyAll },
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "应用所有网站",
                    fontSize = MiuixTheme.textStyles.body1.fontSize,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { decide(true, false) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("仅本次允许")
            }
            Button(onClick = { decide(true, true) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("允许")
            }
            Button(onClick = { decide(false, false) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("拒绝")
            }
            Button(onClick = { decide(false, true) }, modifier = Modifier.fillMaxWidth()) {
                Text("拒绝并不再询问")
            }
        }
    }
}

// ============================================================
// 网页权限
// ============================================================

private const val KEY_PERM_CAMERA = "web_perm_camera"
private const val KEY_PERM_MIC = "web_perm_microphone"
private const val KEY_PERM_LOCATION = "web_perm_location"
private const val KEY_PERM_AUTOPLAY = "web_perm_autoplay"

@Composable
fun WebPermissionsPage(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> WebPermissionsPageMiuix(onBack)
        UiMode.Material -> WebPermissionsPageMaterial(onBack)
    }
}

@Composable
private fun WebPermissionsPageMiuix(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val surface = MiuixTheme.colorScheme.surface
    var version by remember { mutableIntStateOf(0) }
    val items = listOf(
        WebPermissionStore.LOCATION to "定位",
        WebPermissionStore.CAMERA to "相机",
        WebPermissionStore.MICROPHONE to "麦克风",
        WebPermissionStore.AUTOPLAY to "播放声音",
        WebPermissionStore.CLIPBOARD to "剪贴板",
        WebPermissionStore.POPUP_WINDOW to "新建窗口",
        WebPermissionStore.DIALOG to "弹出提示",
        WebPermissionStore.VIDEO_AUTOPLAY to "自动播放视频",
    )

    fun pick(kind: String) {
        val labels = arrayOf("询问", "允许", "拒绝")
        android.app.AlertDialog.Builder(context)
            .setTitle(WebPermissionStore.title(kind))
            .setItems(labels) { _, which ->
                WebPermissionStore.setMode(
                    context,
                    kind,
                    when (which) {
                        1 -> WebPermissionStore.ALLOW
                        2 -> WebPermissionStore.DENY
                        else -> WebPermissionStore.ASK
                    },
                )
                version++
            }
            .setNegativeButton("取消", null)
            .show()
    }

    Column(modifier = Modifier.fillMaxSize().background(surface)) {
        TopAppBar(
            title = "网页权限",
            largeTitle = "网页权限",
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(MiuixIcons.ChevronBackward, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                }
            },
        )
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            item(key = "perms") {
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    items.forEach { (kind, name) ->
                        val mode = WebPermissionStore.mode(context, kind)
                        WindowDropdownPreference(
                            title = name,
                            entries = listOf(
                                DropdownEntry(
                                    items = listOf(
                                        DropdownItem("询问", selected = mode == WebPermissionStore.ASK, onClick = { WebPermissionStore.setMode(context, kind, WebPermissionStore.ASK); version++ }),
                                        DropdownItem("允许", selected = mode == WebPermissionStore.ALLOW, onClick = { WebPermissionStore.setMode(context, kind, WebPermissionStore.ALLOW); version++ }),
                                        DropdownItem("拒绝", selected = mode == WebPermissionStore.DENY, onClick = { WebPermissionStore.setMode(context, kind, WebPermissionStore.DENY); version++ }),
                                    ),
                                ),
                            ),
                            collapseOnSelection = true,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WebPermissionsPageMaterial(onBack: () -> Unit) {
    val context = LocalContext.current
    val bg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )
    var version by remember { mutableIntStateOf(0) }
    val items = listOf(
        WebPermissionStore.LOCATION to "定位",
        WebPermissionStore.CAMERA to "相机",
        WebPermissionStore.MICROPHONE to "麦克风",
        WebPermissionStore.AUTOPLAY to "播放声音",
        WebPermissionStore.CLIPBOARD to "剪贴板",
        WebPermissionStore.POPUP_WINDOW to "新建窗口",
        WebPermissionStore.DIALOG to "弹出提示",
        WebPermissionStore.VIDEO_AUTOPLAY to "自动播放视频",
    )

    fun pick(kind: String) {
        val labels = arrayOf("询问", "允许", "拒绝")
        android.app.AlertDialog.Builder(context)
            .setTitle(WebPermissionStore.title(kind))
            .setItems(labels) { _, which ->
                WebPermissionStore.setMode(
                    context,
                    kind,
                    when (which) {
                        1 -> WebPermissionStore.ALLOW
                        2 -> WebPermissionStore.DENY
                        else -> WebPermissionStore.ASK
                    },
                )
                version++
            }
            .setNegativeButton("取消", null)
            .show()
    }

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = { M3Text("网页权限") },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            item(key = "perms") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                ) {
                    var menuFor by remember { mutableStateOf<String?>(null) }
                    MoteSegmentedColumn(
                        color = M3MaterialTheme.colorScheme.surface,
                        content = items.map { (kind, name) ->
                            {
                                @Suppress("UNUSED_EXPRESSION")
                                version
                                Box {
                                    M3SegmentArrowItem(
                                        title = "$name：${WebPermissionStore.label(WebPermissionStore.mode(context, kind))}",
                                        onClick = { menuFor = kind },
                                    )
                                    M3DropdownMenu(
                                        expanded = menuFor == kind,
                                        onDismissRequest = { menuFor = null },
                                    ) {
                                        listOf(
                                            "询问" to WebPermissionStore.ASK,
                                            "允许" to WebPermissionStore.ALLOW,
                                            "拒绝" to WebPermissionStore.DENY,
                                        ).forEach { (label, mode) ->
                                            M3DropdownMenuItem(
                                                text = { M3Text(label) },
                                                onClick = {
                                                    WebPermissionStore.setMode(context, kind, mode)
                                                    menuFor = null
                                                    version++
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
