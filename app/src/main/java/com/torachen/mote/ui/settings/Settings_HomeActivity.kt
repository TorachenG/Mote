@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
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
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.RadioButton as M3RadioButton
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Switch as M3Switch
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

// ---------------- 启动选项 ----------------
private val STARTUP_OPTIONS = listOf(
    "打开新标签页",
    "打开自定义网页",
    "打开上次退出时的网页",
)
private const val STARTUP_OPTION_CUSTOM = 1
private const val STARTUP_OPTION_LAST_SESSION = 2

// ---------------- 新标签页类型选项 ----------------
private val NEWTAB_PAGE_TYPE_OPTIONS = listOf(
    "默认",
    "自定义网页",
)
private const val NEWTAB_PAGE_TYPE_DEFAULT = 0
private const val NEWTAB_PAGE_TYPE_CUSTOM = 1

// ---------------- 持久化键 ----------------
private const val KEY_STARTUP_OPTION = "home_startup_option"
private const val KEY_STARTUP_CUSTOM_URL = "home_startup_custom_url"
private const val KEY_ASK_RESTORE_MEMORY = "home_ask_restore_memory_v2"
private const val KEY_NEWTAB_PAGE_TYPE = "home_newtab_page_type"
private const val KEY_NEWTAB_CUSTOM_URL = "home_newtab_custom_url"

// ★ 校验 + 规范化用户输入的主页 URL
internal fun normalizeHomeUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    if (trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
    ) {
        return try {
            val uri = Uri.parse(trimmed)
            if (uri.host.isNullOrBlank()) null else trimmed
        } catch (_: Exception) { null }
    }

    if (trimmed.any { it == ' ' || it.code in 0x4E00..0x9FFF }) return null

    val candidate = "https://$trimmed"
    return try {
        val uri = Uri.parse(candidate)
        val host = uri.host ?: return null
        if (host.isBlank()) return null
        if (!host.contains('.') && host != "localhost") return null
        candidate
    } catch (_: Exception) { null }
}

// ============================================================
// 状态
// ============================================================

@Immutable
private data class HomePageState(
    val startupOptionIndex: Int,
    val startupUrlDraft: String,
    val startupCustomUrl: String,
    val askRestoreMemory: Boolean,
    val newtabPageType: Int,
    val newtabUrlDraft: String,
    val newtabCustomUrl: String,
)

@Immutable
private data class HomePageActions(
    val onBack: () -> Unit,
    val onStartupOptionChange: (Int) -> Unit,
    val onStartupUrlDraftChange: (String) -> Unit,
    val onCommitStartupUrl: () -> Unit,
    val onAskRestoreMemoryChange: (Boolean) -> Unit,
    val onNewtabPageTypeChange: (Int) -> Unit,
    val onNewtabUrlDraftChange: (String) -> Unit,
    val onCommitNewtabUrl: () -> Unit,
    val onNavigateToNewTabCustomization: () -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsHomePage(
    onBack: () -> Unit,
    onNavigateToNewTabCustomization: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var startupOptionIndex by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_STARTUP_OPTION, 0).coerceIn(0, STARTUP_OPTIONS.lastIndex)
        )
    }
    var startupCustomUrl by remember {
        mutableStateOf(prefs.getString(KEY_STARTUP_CUSTOM_URL, "") ?: "")
    }
    var startupUrlDraft by remember { mutableStateOf(startupCustomUrl) }

    LaunchedEffect(startupOptionIndex) {
        if (startupOptionIndex == STARTUP_OPTION_CUSTOM) {
            startupUrlDraft = startupCustomUrl
        }
    }

    var askRestoreMemory by remember {
        mutableStateOf(prefs.getBoolean(KEY_ASK_RESTORE_MEMORY, true))
    }

    val isLastSessionSelected = startupOptionIndex == STARTUP_OPTION_LAST_SESSION
    val effectiveAskRestore = if (isLastSessionSelected) false else askRestoreMemory

    var newtabPageType by remember {
        mutableIntStateOf(
            prefs.getInt(KEY_NEWTAB_PAGE_TYPE, NEWTAB_PAGE_TYPE_DEFAULT)
                .coerceIn(0, NEWTAB_PAGE_TYPE_OPTIONS.lastIndex)
        )
    }
    var newtabCustomUrl by remember {
        mutableStateOf(prefs.getString(KEY_NEWTAB_CUSTOM_URL, "") ?: "")
    }
    var newtabUrlDraft by remember { mutableStateOf(newtabCustomUrl) }

    LaunchedEffect(newtabPageType) {
        if (newtabPageType == NEWTAB_PAGE_TYPE_CUSTOM) {
            newtabUrlDraft = newtabCustomUrl
        }
    }

    fun commitStartupCustomUrl() {
        val normalized = normalizeHomeUrl(startupUrlDraft)
        if (normalized == null) {
            Toast.makeText(
                context,
                "请输入完整合法的网址（如 https://example.com）",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        startupCustomUrl = normalized
        startupUrlDraft = normalized
        prefs.edit().putString(KEY_STARTUP_CUSTOM_URL, normalized).apply()
    }

    fun commitNewtabCustomUrl() {
        val normalized = normalizeHomeUrl(newtabUrlDraft)
        if (normalized == null) {
            Toast.makeText(
                context,
                "请输入完整合法的网址（如 https://example.com）",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        newtabCustomUrl = normalized
        newtabUrlDraft = normalized
        prefs.edit().putString(KEY_NEWTAB_CUSTOM_URL, normalized).apply()
    }

    val state = HomePageState(
        startupOptionIndex = startupOptionIndex,
        startupUrlDraft = startupUrlDraft,
        startupCustomUrl = startupCustomUrl,
        askRestoreMemory = effectiveAskRestore,
        newtabPageType = newtabPageType,
        newtabUrlDraft = newtabUrlDraft,
        newtabCustomUrl = newtabCustomUrl,
    )

    val actions = HomePageActions(
        onBack = onBack,
        onStartupOptionChange = { index ->
            startupOptionIndex = index
            prefs.edit().putInt(KEY_STARTUP_OPTION, index).apply()
        },
        onStartupUrlDraftChange = { startupUrlDraft = it },
        onCommitStartupUrl = { commitStartupCustomUrl() },
        onAskRestoreMemoryChange = { checked ->
            askRestoreMemory = checked
            prefs.edit().putBoolean(KEY_ASK_RESTORE_MEMORY, checked).apply()
        },
        onNewtabPageTypeChange = { index ->
            newtabPageType = index
            prefs.edit().putInt(KEY_NEWTAB_PAGE_TYPE, index).apply()
        },
        onNewtabUrlDraftChange = { newtabUrlDraft = it },
        onCommitNewtabUrl = { commitNewtabCustomUrl() },
        onNavigateToNewTabCustomization = onNavigateToNewTabCustomization,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsHomeMiuix(state, actions)
        UiMode.Material -> SettingsHomeMaterial(state, actions)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsHomeMiuix(
    state: HomePageState,
    actions: HomePageActions,
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

    val isLastSessionSelected = state.startupOptionIndex == STARTUP_OPTION_LAST_SESSION
    val effectiveAskRestore = if (isLastSessionSelected) false else state.askRestoreMemory

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
                        .overScrollVertical()
                        .imePadding(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("启动时")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                STARTUP_OPTIONS.forEachIndexed { index, label ->
                                    SearchableItem(
                                        title = label,
                                        pageRoute = "settings_home",
                                        pageTitle = "主页设置",
                                        itemKey = "settings_home_startup_$index",
                                    ) {
                                        RadioButtonPreference(
                                            title = label,
                                            selected = state.startupOptionIndex == index,
                                            onClick = {
                                                actions.onStartupOptionChange(index)
                                            },
                                        )
                                    }

                                    if (index == STARTUP_OPTION_CUSTOM) {
                                        AnimatedVisibility(
                                            visible = state.startupOptionIndex == STARTUP_OPTION_CUSTOM,
                                        ) {
                                            UrlEditRow(
                                                value = state.startupUrlDraft,
                                                onValueChange = actions.onStartupUrlDraftChange,
                                                showConfirm = state.startupUrlDraft.trim() != state.startupCustomUrl,
                                                onConfirm = actions.onCommitStartupUrl,
                                                label = "自定义网页地址",
                                            )
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "询问恢复上次退出时的网页",
                                    pageRoute = "settings_home",
                                    pageTitle = "主页设置",
                                    itemKey = "settings_home_ask_restore_memory",
                                ) {
                                    SwitchPreference(
                                        title = "询问恢复上次退出时的网页",
                                        checked = effectiveAskRestore,
                                        enabled = !isLastSessionSelected,
                                        onCheckedChange = actions.onAskRestoreMemoryChange,
                                    )
                                }
                            }

                            SmallTitle("新标签页")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                NEWTAB_PAGE_TYPE_OPTIONS.forEachIndexed { index, label ->
                                    SearchableItem(
                                        title = label,
                                        pageRoute = "settings_home",
                                        pageTitle = "主页设置",
                                        itemKey = "settings_home_newtab_type_$index",
                                    ) {
                                        RadioButtonPreference(
                                            title = label,
                                            selected = state.newtabPageType == index,
                                            onClick = {
                                                actions.onNewtabPageTypeChange(index)
                                            },
                                        )
                                    }

                                    if (index == NEWTAB_PAGE_TYPE_CUSTOM) {
                                        AnimatedVisibility(
                                            visible = state.newtabPageType == NEWTAB_PAGE_TYPE_CUSTOM,
                                        ) {
                                            UrlEditRow(
                                                value = state.newtabUrlDraft,
                                                onValueChange = actions.onNewtabUrlDraftChange,
                                                showConfirm = state.newtabUrlDraft.trim() != state.newtabCustomUrl,
                                                onConfirm = actions.onCommitNewtabUrl,
                                                label = "自定义网页地址",
                                            )
                                        }
                                    }
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
                                        .clickable { actions.onNavigateToNewTabCustomization() }
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "定制新标签页",
                                        color = MiuixTheme.colorScheme.primary,
                                        fontSize = MiuixTheme.textStyles.body1.fontSize,
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
                    pageRoute = "settings_home",
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
                    title = "主页设置",
                    largeTitle = "主页设置",
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
private fun SettingsHomeMaterial(
    state: HomePageState,
    actions: HomePageActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

    val pageBg = M3MaterialTheme.colorScheme.surface

    val isLastSessionSelected = state.startupOptionIndex == STARTUP_OPTION_LAST_SESSION

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("主页设置") },
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
                    .padding(innerPadding)
                    .imePadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "startup_header") {
                    HomeSectionHeader("启动时")
                }

                item(key = "startup_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        val startupItems: List<@Composable () -> Unit> = buildList {
                            STARTUP_OPTIONS.forEachIndexed { index, label ->
                                add {
                                    M3SegmentRadioItem(
                                        title = label,
                                        selected = state.startupOptionIndex == index,
                                        onClick = { actions.onStartupOptionChange(index) },
                                    )
                                }
                                if (index == STARTUP_OPTION_CUSTOM &&
                                    state.startupOptionIndex == STARTUP_OPTION_CUSTOM
                                ) {
                                    add {
                                        M3SegmentUrlEditItem(
                                            value = state.startupUrlDraft,
                                            onValueChange = actions.onStartupUrlDraftChange,
                                            label = "自定义网页地址",
                                            showConfirm = state.startupUrlDraft.trim() != state.startupCustomUrl,
                                            onConfirm = actions.onCommitStartupUrl,
                                        )
                                    }
                                }
                            }
                        }
                        MoteSegmentedColumn(content = startupItems)
                    }
                }

                item(key = "startup_ask_restore") {
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
                                        title = "询问恢复上次退出时的网页",
                                        summary = null,
                                        checked = if (isLastSessionSelected) false
                                        else state.askRestoreMemory,
                                        onCheckedChange = actions.onAskRestoreMemoryChange,
                                    )
                                }
                            )
                        )
                    }
                }

                item(key = "newtab_header") {
                    HomeSectionHeader("新标签页")
                }

                item(key = "newtab_items") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        val newtabItems: List<@Composable () -> Unit> = buildList {
                            NEWTAB_PAGE_TYPE_OPTIONS.forEachIndexed { index, label ->
                                add {
                                    M3SegmentRadioItem(
                                        title = label,
                                        selected = state.newtabPageType == index,
                                        onClick = { actions.onNewtabPageTypeChange(index) },
                                    )
                                }
                                if (index == NEWTAB_PAGE_TYPE_CUSTOM &&
                                    state.newtabPageType == NEWTAB_PAGE_TYPE_CUSTOM
                                ) {
                                    add {
                                        M3SegmentUrlEditItem(
                                            value = state.newtabUrlDraft,
                                            onValueChange = actions.onNewtabUrlDraftChange,
                                            label = "自定义网页地址",
                                            showConfirm = state.newtabUrlDraft.trim() != state.newtabCustomUrl,
                                            onConfirm = actions.onCommitNewtabUrl,
                                        )
                                    }
                                }
                            }
                        }
                        MoteSegmentedColumn(content = newtabItems)
                    }
                }

                item(key = "find_more_header") {
                    HomeSectionHeader("在查找其它设置吗？")
                }

                item(key = "find_more_item") {
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
                                                text = "定制新标签页",
                                                color = M3MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        onClick = {
                                            actions.onNavigateToNewTabCustomization()
                                        },
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

// ============================================================
// Material 专用小件
// ============================================================

@Composable
private fun HomeSectionHeader(text: String) {
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

@Composable
private fun M3SegmentRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    M3SegmentItem(
        headline = { M3Text(title) },
        leading = {
            M3RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun M3SegmentUrlEditItem(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showConfirm: Boolean,
    onConfirm: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.weight(1f),
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { M3Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = showConfirm,
            enter = androidx.compose.animation.expandHorizontally(
                animationSpec = tween(240, easing = SmoothEase),
                expandFrom = Alignment.Start,
            ) + fadeIn(tween(180, easing = SmoothEase)),
            exit = androidx.compose.animation.shrinkHorizontally(
                animationSpec = tween(200, easing = SmoothEase),
                shrinkTowards = Alignment.Start,
            ) + fadeOut(tween(140, easing = SmoothEase)),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(M3MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center,
            ) {
                M3Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "确定",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun UrlEditRow(
    value: String,
    onValueChange: (String) -> Unit,
    showConfirm: Boolean,
    onConfirm: () -> Unit,
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            singleLine = true,
            modifier = Modifier.weight(1f),
        )

        AnimatedVisibility(
            visible = showConfirm,
            enter = expandHorizontally(
                animationSpec = tween(240, easing = SmoothEase),
                expandFrom = Alignment.Start,
            ) + fadeIn(tween(180, easing = SmoothEase)),
            exit = shrinkHorizontally(
                animationSpec = tween(200, easing = SmoothEase),
                shrinkTowards = Alignment.Start,
            ) + fadeOut(tween(140, easing = SmoothEase)),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "确定",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
