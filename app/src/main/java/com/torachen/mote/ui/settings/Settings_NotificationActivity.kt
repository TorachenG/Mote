@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// ★★★ NavigationEvent ★★★
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// ★★★ MIUIX 组件 ★★★
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
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
import top.yukonga.miuix.kmp.icon.extended.Back

// ★★★ MIUIX 工具（overscroll 回弹） ★★★
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ---------------- 一、底部提示 ----------------
private const val KEY_TOAST_INCOGNITO = "toast_incognito_enabled"
private const val KEY_TOAST_TRANSLATE = "toast_translate_enabled"
private const val KEY_TOAST_FULLSCREEN = "toast_fullscreen_enabled"

// ---------------- 二、角标提示 ----------------
private const val KEY_BADGE_UPDATE = "badge_update_enabled"
private const val KEY_BADGE_SNIFF = "badge_sniff_enabled"
private const val KEY_BADGE_TRANSLATE = "badge_translate_enabled"

// ---------------- 三、通知栏 ----------------
private const val KEY_NOTIFICATION_ENABLED = "notification_bar_enabled"

// ============================================================
// 通知设置页
// ============================================================

@Composable
fun SettingsNotificationPage(
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
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 一、底部提示 ----------------
    var toastIncognitoEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_INCOGNITO, true))
    }
    var toastTranslateEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_TRANSLATE, true))
    }
    var toastFullscreenEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_TOAST_FULLSCREEN, true))
    }

    // ---------------- 二、角标提示 ----------------
    var badgeUpdateEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BADGE_UPDATE, true))
    }
    var badgeSniffEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BADGE_SNIFF, true))
    }
    var badgeTranslateEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BADGE_TRANSLATE, true))
    }

    // ---------------- 三、通知栏 ----------------
    var notificationBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true))
    }

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
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // ---------- 一、底部提示 ----------
                            SmallTitle("底部提示")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "无痕模式提示",
                                    checked = toastIncognitoEnabled,
                                    onCheckedChange = {
                                        toastIncognitoEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_TOAST_INCOGNITO, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "网页翻译提示",
                                    checked = toastTranslateEnabled,
                                    onCheckedChange = {
                                        toastTranslateEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_TOAST_TRANSLATE, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "全屏模式提示",
                                    checked = toastFullscreenEnabled,
                                    onCheckedChange = {
                                        toastFullscreenEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_TOAST_FULLSCREEN, it)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 二、角标提示 ----------
                            SmallTitle("角标提醒")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "应用更新提醒",
                                    summary = "发现新版本时将在相关入口显示红点",
                                    checked = badgeUpdateEnabled,
                                    onCheckedChange = {
                                        badgeUpdateEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_BADGE_UPDATE, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "资源嗅探提醒",
                                    summary = "发现网页资源时将显示红点",
                                    checked = badgeSniffEnabled,
                                    onCheckedChange = {
                                        badgeSniffEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_BADGE_SNIFF, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "网页翻译提醒",
                                    summary = "发现网页可翻译时将显示红点",
                                    checked = badgeTranslateEnabled,
                                    onCheckedChange = {
                                        badgeTranslateEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_BADGE_TRANSLATE, it)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 三、通知栏 ----------
                            SmallTitle("通知栏")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "接收通知栏消息",
                                    summary = "开启后，允许系统通知栏收到来自应用的新消息",
                                    checked = notificationBarEnabled,
                                    onCheckedChange = {
                                        notificationBarEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_NOTIFICATION_ENABLED, it)
                                            .apply()
                                    },
                                )
                                ArrowPreference(
                                    title = "调整通知栏信息",
                                    onClick = { openNotificationSettings(context) },
                                )
                            }

                            // ★ 底部导航栏占位
                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }

            // ========== 顶栏层 ==========
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
                    title = "通知设置",
                    largeTitle = "通知设置",
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