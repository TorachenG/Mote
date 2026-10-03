@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

// ---------------- 持久化键 ----------------
private const val KEY_REMEMBER_FORM = "web_remember_form"
private const val KEY_ACCEPT_COOKIE = "web_accept_cookie"
private const val KEY_CLEAR_INCOGNITO_ON_EXIT = "web_clear_incognito_on_exit"

// ============================================================
// 权限设置页
// ============================================================

@Composable
fun SettingsPermissionsPage(
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

    // ---------------- 持久化 ----------------
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    // ---------------- 一、网页权限 ----------------

    var rememberFormEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_REMEMBER_FORM, true))
    }
    var acceptCookieEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_ACCEPT_COOKIE, true))
    }
    var clearIncognitoOnExitEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_CLEAR_INCOGNITO_ON_EXIT, true))
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            val viewportHeight = maxHeight
            var contentHeightPx by remember { mutableIntStateOf(0) }
            val contentHeight = with(density) { contentHeightPx.toDp() }

            // ★ 动态 filler
            val fillerHeight = (
                    viewportHeight - contentTopPadding - contentHeight - 12.dp + 1.dp
                    ).coerceAtLeast(0.dp)

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
                    // ==================================================
                    // 内容区（合并成单个 item 以便测量整体高度）
                    // ==================================================
                    item(key = "content") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { contentHeightPx = it.size.height }
                        ) {
                            // ---------- 一、网页权限 ----------
                            SmallTitle("网页权限")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "记住表单数据",
                                    summary = "记住表单中输入的数据以便以后使用",
                                    checked = rememberFormEnabled,
                                    onCheckedChange = {
                                        rememberFormEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_REMEMBER_FORM, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "接受 Cookie",
                                    summary = "允许网页保存和读取 Cookie 数据",
                                    checked = acceptCookieEnabled,
                                    onCheckedChange = {
                                        acceptCookieEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_ACCEPT_COOKIE, it)
                                            .apply()
                                    },
                                )
                                SwitchPreference(
                                    title = "无痕浏览窗口清空",
                                    summary = "退出浏览器时清空所有无痕浏览窗口",
                                    checked = clearIncognitoOnExitEnabled,
                                    onCheckedChange = {
                                        clearIncognitoOnExitEnabled = it
                                        prefs.edit()
                                            .putBoolean(KEY_CLEAR_INCOGNITO_ON_EXIT, it)
                                            .apply()
                                    },
                                )
                            }

                            // ---------- 二、软件权限 ----------
                            SmallTitle("软件权限")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "读取手机存储",
                                    onClick = { openAppPermissionSettings(context) },
                                )
                                ArrowPreference(
                                    title = "相机",
                                    onClick = { openAppPermissionSettings(context) },
                                )
                                ArrowPreference(
                                    title = "录音",
                                    onClick = { openAppPermissionSettings(context) },
                                )
                                ArrowPreference(
                                    title = "定位",
                                    onClick = { openAppPermissionSettings(context) },
                                )
                                ArrowPreference(
                                    title = "读取或写入剪切板",
                                    onClick = { openAppPermissionSettings(context) },
                                )
                            }
                        }
                    }

                    // --------------------------------------------------
                    // 底部填充（动态高度）
                    // --------------------------------------------------
                    item(key = "bottom_filler") {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fillerHeight)
                        )
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
                    title = "权限设置",
                    largeTitle = "权限设置",
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
// 辅助函数：跳转系统权限管理页面
//
// 优先尝试各厂商的权限管理页；失败则回退到系统标准的
// 「应用详情」页面（其中包含权限入口）；再失败回退到系统设置首页。
// ============================================================

private fun openAppPermissionSettings(context: Context) {
    val packageName = context.packageName

    // ---------- 1. 厂商权限管理页 ----------
    val vendorIntents = listOf(
        // 小米 MIUI
        Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            putExtra("extra_pkgname", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // 华为 EMUI
        Intent("com.huawei.permissionmanager.ui.MainActivity").apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // 魅族 Flyme
        Intent("com.meizu.safe.security.SHOW_APPSEC").apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // OPPO / OnePlus ColorOS
        Intent("com.coloros.securitypermission.permission.PermissionGroupsActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // vivo FuntouchOS
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
            // 该厂商 Action 不可用，继续尝试下一个
        }
    }

    // ---------- 2. 兜底：当前应用的系统应用设置页 ----------
    try {
        val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(detailIntent)
        return
    } catch (_: Exception) {
        // 继续兜底
    }

    // ---------- 3. 最后兜底：系统设置首页 ----------
    try {
        val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(settingsIntent)
    } catch (_: Exception) {
        // 忽略
    }
}