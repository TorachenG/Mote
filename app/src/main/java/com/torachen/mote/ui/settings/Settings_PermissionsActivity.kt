@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import com.torachen.mote.ui.MoteSegmentedColumn
import com.torachen.mote.ui.M3SegmentItem
import com.torachen.mote.ui.M3_SEGMENT_LIST_HORIZONTAL_PADDING
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

private const val KEY_TOAST_INCOGNITO = "toast_incognito_enabled"
private const val KEY_TOAST_TRANSLATE = "toast_translate_enabled"
private const val KEY_TOAST_FULLSCREEN = "toast_fullscreen_enabled"
private const val KEY_NOTIFICATION_ENABLED = "notification_bar_enabled"
internal const val KEY_CLIPBOARD_READ = "clipboard_read_enabled"

// ============================================================
// 状态
// ============================================================

@Immutable
private data class PermissionsUiState(
    val notificationBarEnabled: Boolean,
    val toastIncognitoEnabled: Boolean,
    val toastTranslateEnabled: Boolean,
    val toastFullscreenEnabled: Boolean,
)

@Immutable
private data class PermissionsActions(
    val onNotificationBarChange: (Boolean) -> Unit,
    val onToastIncognitoChange: (Boolean) -> Unit,
    val onToastTranslateChange: (Boolean) -> Unit,
    val onToastFullscreenChange: (Boolean) -> Unit,
    val onOpenNotificationSettings: () -> Unit,
    val onOpenPermissionSettings: () -> Unit,
    val onOpenClipboardSetting: () -> Unit,
    val onBack: () -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsAppPermissionsPage(
    onBack: () -> Unit,
    onOpenClipboardSetting: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var notificationBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true))
    }
    var toastIncognitoEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_INCOGNITO, true))
    }
    var toastTranslateEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_TRANSLATE, true))
    }
    var toastFullscreenEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_FULLSCREEN, true))
    }

    val state = PermissionsUiState(
        notificationBarEnabled = notificationBarEnabled,
        toastIncognitoEnabled = toastIncognitoEnabled,
        toastTranslateEnabled = toastTranslateEnabled,
        toastFullscreenEnabled = toastFullscreenEnabled,
    )

    val actions = PermissionsActions(
        onNotificationBarChange = {
            notificationBarEnabled = it
            prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, it).apply()
        },
        onToastIncognitoChange = {
            toastIncognitoEnabled = it
            prefs.edit().putBoolean(KEY_TOAST_INCOGNITO, it).apply()
        },
        onToastTranslateChange = {
            toastTranslateEnabled = it
            prefs.edit().putBoolean(KEY_TOAST_TRANSLATE, it).apply()
        },
        onToastFullscreenChange = {
            toastFullscreenEnabled = it
            prefs.edit().putBoolean(KEY_TOAST_FULLSCREEN, it).apply()
        },
        onOpenNotificationSettings = { openNotificationSettings(context) },
        onOpenPermissionSettings = { openAppPermissionSettings(context) },
        onOpenClipboardSetting = onOpenClipboardSetting,
        onBack = onBack,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsPermissionsMiuix(state, actions)
        UiMode.Material -> SettingsPermissionsMaterial(state, actions)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsPermissionsMiuix(
    state: PermissionsUiState,
    actions: PermissionsActions,
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

    var hasStorage by remember { mutableStateOf(false) }
    var hasCamera by remember { mutableStateOf(false) }
    var hasAudio by remember { mutableStateOf(false) }
    var hasLocation by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
                hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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

                            SmallTitle("通知")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "接收通知栏消息",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_notification_bar",
                                ) {
                                    SwitchPreference(
                                        title = "接收通知栏消息",
                                        summary = "开启后，允许系统通知栏收到来自应用的新消息",
                                        checked = state.notificationBarEnabled,
                                        onCheckedChange = actions.onNotificationBarChange,
                                    )
                                }
                                SearchableItem(
                                    title = "调整通知栏信息",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_adjust_notification",
                                ) {
                                    ArrowPreference(
                                        title = "调整通知栏信息",
                                        onClick = actions.onOpenNotificationSettings,
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "无痕模式提示",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_toast_incognito",
                                ) {
                                    SwitchPreference(
                                        title = "无痕模式提示",
                                        checked = state.toastIncognitoEnabled,
                                        onCheckedChange = actions.onToastIncognitoChange,
                                    )
                                }
                                SearchableItem(
                                    title = "网页翻译提示",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_toast_translate",
                                ) {
                                    SwitchPreference(
                                        title = "网页翻译提示",
                                        checked = state.toastTranslateEnabled,
                                        onCheckedChange = actions.onToastTranslateChange,
                                    )
                                }
                                SearchableItem(
                                    title = "全屏模式提示",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_toast_fullscreen",
                                ) {
                                    SwitchPreference(
                                        title = "全屏模式提示",
                                        checked = state.toastFullscreenEnabled,
                                        onCheckedChange = actions.onToastFullscreenChange,
                                    )
                                }
                            }

                            SmallTitle("权限")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "读取手机存储",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_storage",
                                ) {
                                    ArrowPreference(
                                        title = "读取手机存储",
                                        endActions = {
                                            Text(
                                                text = if (hasStorage) "已开启" else "去设置",
                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                            )
                                        },
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                }
                                SearchableItem(
                                    title = "相机",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_camera",
                                ) {
                                    ArrowPreference(
                                        title = "相机",
                                        endActions = {
                                            Text(
                                                text = if (hasCamera) "已开启" else "去设置",
                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                            )
                                        },
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                }
                                SearchableItem(
                                    title = "录音",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_record_audio",
                                ) {
                                    ArrowPreference(
                                        title = "录音",
                                        endActions = {
                                            Text(
                                                text = if (hasAudio) "已开启" else "去设置",
                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                            )
                                        },
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                }
                                SearchableItem(
                                    title = "定位",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_location",
                                ) {
                                    ArrowPreference(
                                        title = "定位",
                                        endActions = {
                                            Text(
                                                text = if (hasLocation) "已开启" else "去设置",
                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                            )
                                        },
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                }
                                SearchableItem(
                                    title = "读取剪贴板",
                                    pageRoute = "settings_app_permissions",
                                    pageTitle = "通知与权限",
                                    itemKey = "settings_app_permissions_clipboard",
                                ) {
                                    ArrowPreference(
                                        title = "读取剪贴板",
                                        onClick = actions.onOpenClipboardSetting,
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
                    pageRoute = "settings_app_permissions",
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
                                        startFraction = 0.75f,
                                        curve = 1f,
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
                    title = "通知与权限",
                    largeTitle = "通知与权限",
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
private fun SettingsPermissionsMaterial(
    state: PermissionsUiState,
    actions: PermissionsActions,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current

    var hasStorage by remember { mutableStateOf(false) }
    var hasCamera by remember { mutableStateOf(false) }
    var hasAudio by remember { mutableStateOf(false) }
    var hasLocation by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
                hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    title = { M3Text("通知与权限") },
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
                item(key = "notif_header") {
                    PermissionsSectionHeader("通知")
                }

                item(key = "notif_items") {
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
                                        title = "接收通知栏消息",
                                        summary = "开启后，允许系统通知栏收到来自应用的新消息",
                                        checked = state.notificationBarEnabled,
                                        onCheckedChange = actions.onNotificationBarChange,
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "调整通知栏信息",
                                        onClick = actions.onOpenNotificationSettings,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "toast_items") {
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
                                        title = "无痕模式提示",
                                        summary = null,
                                        checked = state.toastIncognitoEnabled,
                                        onCheckedChange = actions.onToastIncognitoChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "网页翻译提示",
                                        summary = null,
                                        checked = state.toastTranslateEnabled,
                                        onCheckedChange = actions.onToastTranslateChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "全屏模式提示",
                                        summary = null,
                                        checked = state.toastFullscreenEnabled,
                                        onCheckedChange = actions.onToastFullscreenChange,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "perm_header") {
                    PermissionsSectionHeader("权限")
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
                                    M3SegmentArrowItem(
                                        title = "读取手机存储",
                                        endText = if (hasStorage) "已开启" else "去设置",
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "相机",
                                        endText = if (hasCamera) "已开启" else "去设置",
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "录音",
                                        endText = if (hasAudio) "已开启" else "去设置",
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "定位",
                                        endText = if (hasLocation) "已开启" else "去设置",
                                        onClick = actions.onOpenPermissionSettings,
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "读取剪贴板",
                                        onClick = actions.onOpenClipboardSetting,
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
private fun PermissionsSectionHeader(text: String) {
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
// 辅助函数：跳转系统权限管理页面
// ============================================================

private fun openAppPermissionSettings(context: Context) {
    val packageName = context.packageName

    val vendorIntents = listOf(
        Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            putExtra("extra_pkgname", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("com.huawei.permissionmanager.ui.MainActivity").apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("com.meizu.safe.security.SHOW_APPSEC").apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("com.coloros.securitypermission.permission.PermissionGroupsActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("com.vivo.permissionmanager.activity.PurviewTabActivity").apply {
            putExtra("packagename", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )

    for (intent in vendorIntents) {
        try {
            context.startActivity(intent)
            return
        } catch (_: Exception) {
        }
    }

    try {
        val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(detailIntent)
        return
    } catch (_: Exception) {
    }

    try {
        val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(settingsIntent)
    } catch (_: Exception) {
    }
}

// ============================================================
// 辅助函数：跳转系统通知设置页面
// ============================================================

private fun openNotificationSettings(context: Context) {
    val packageName = context.packageName

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {
        }
    }

    try {
        val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(detailIntent)
        return
    } catch (_: Exception) {
    }

    try {
        val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(settingsIntent)
    } catch (_: Exception) {
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClipboardAccessPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var enabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_CLIPBOARD_READ, true))
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        prefs.edit().putBoolean(KEY_CLIPBOARD_READ, value).apply()
    }

    if (LocalUiMode.current == UiMode.Material) {
        val pageBg = lerp(
            M3MaterialTheme.colorScheme.surfaceContainerLow,
            M3MaterialTheme.colorScheme.secondaryContainer,
            0.10f,
        )
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("读取剪贴板") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
                            M3Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "返回",
                            )
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg),
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING),
                    ) {
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentItem(
                                        headline = { M3Text("开启") },
                                        supporting = {
                                            M3Text("开启后允许 Mote 通过读取剪贴板提供快捷跳转等功能服务")
                                        },
                                        trailing = {
                                            androidx.compose.material3.RadioButton(
                                                selected = enabled,
                                                onClick = null,
                                            )
                                        },
                                        onClick = { setEnabled(true) },
                                    )
                                },
                                {
                                    M3SegmentItem(
                                        headline = { M3Text("关闭") },
                                        trailing = {
                                            androidx.compose.material3.RadioButton(
                                                selected = !enabled,
                                                onClick = null,
                                            )
                                        },
                                        onClick = { setEnabled(false) },
                                    )
                                },
                            ),
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
            TopAppBar(
                title = "读取剪贴板",
                largeTitle = "读取剪贴板",
                color = Color.Transparent,
                defaultWindowInsetsPadding = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().overScrollVertical(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                overscrollEffect = null,
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        RadioButtonPreference(
                            title = "开启",
                            summary = "开启后允许 Mote 通过读取剪贴板提供快捷跳转等功能服务",
                            selected = enabled,
                            onClick = { setEnabled(true) },
                        )
                        RadioButtonPreference(
                            title = "关闭",
                            selected = !enabled,
                            onClick = { setEnabled(false) },
                        )
                    }
                }
            }
        }
    }
}
