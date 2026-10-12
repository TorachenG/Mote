@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ★★★ NavigationEvent ★★★
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

// ★★★ MIUIX 组件 ★★★
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// ★★★ MIUIX 模糊 ★★★
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Ok

// ★★★ MIUIX 工具（overscroll 回弹） ★★★
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

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

// ★ 校验 + 规范化用户输入的主页 URL：
//   - 空白 → null
//   - 已带 http:// / https:// 且 host 有效 → 原样返回
//   - 未带协议 → 自动补 https://，再校验 host
//   - 含空格 / 中文字符 → null
//   - host 不含"."且不是 localhost → null
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

    // 不允许空格 / 中文字符（避免用户误填搜索词）
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
// 主页设置页
// ============================================================

@Composable
fun SettingsHomePage(
    onBack: () -> Unit,
    onNavigateToNewTabCustomization: () -> Unit = {},
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

    // ---------------- 一、启动时 ----------------
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

    // ---------------- 二、新标签页 ----------------
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
        // ★ 校验 + 规范化
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
        // ★ 校验 + 规范化
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
                        .overScrollVertical()
                        .imePadding(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // ==================================================
                            // 一、启动时
                            // ==================================================
                            SmallTitle("启动时")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                STARTUP_OPTIONS.forEachIndexed { index, label ->
                                    RadioButtonPreference(
                                        title = label,
                                        selected = startupOptionIndex == index,
                                        onClick = {
                                            startupOptionIndex = index
                                            prefs.edit()
                                                .putInt(KEY_STARTUP_OPTION, index)
                                                .apply()
                                        },
                                    )

                                    if (index == STARTUP_OPTION_CUSTOM) {
                                        AnimatedVisibility(
                                            visible = startupOptionIndex == STARTUP_OPTION_CUSTOM,
                                            enter = expandVertically(
                                                animationSpec = tween(260, easing = SmoothEase),
                                                expandFrom = Alignment.Top,
                                            ) + fadeIn(tween(200, easing = SmoothEase)),
                                            exit = shrinkVertically(
                                                animationSpec = tween(220, easing = SmoothEase),
                                                shrinkTowards = Alignment.Top,
                                            ) + fadeOut(tween(140, easing = SmoothEase)),
                                        ) {
                                            UrlEditRow(
                                                value = startupUrlDraft,
                                                onValueChange = { startupUrlDraft = it },
                                                showConfirm = startupUrlDraft.trim() != startupCustomUrl,
                                                onConfirm = { commitStartupCustomUrl() },
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
                                SwitchPreference(
                                    title = "询问恢复上次退出时的网页",
                                    checked = effectiveAskRestore,
                                    enabled = !isLastSessionSelected,
                                    onCheckedChange = { checked ->
                                        askRestoreMemory = checked
                                        prefs.edit()
                                            .putBoolean(KEY_ASK_RESTORE_MEMORY, checked)
                                            .apply()
                                    },
                                )
                            }

                            // ==================================================
                            // 二、新标签页
                            // ==================================================
                            SmallTitle("新标签页")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                NEWTAB_PAGE_TYPE_OPTIONS.forEachIndexed { index, label ->
                                    RadioButtonPreference(
                                        title = label,
                                        selected = newtabPageType == index,
                                        onClick = {
                                            newtabPageType = index
                                            prefs.edit()
                                                .putInt(KEY_NEWTAB_PAGE_TYPE, index)
                                                .apply()
                                        },
                                    )

                                    if (index == NEWTAB_PAGE_TYPE_CUSTOM) {
                                        AnimatedVisibility(
                                            visible = newtabPageType == NEWTAB_PAGE_TYPE_CUSTOM,
                                            enter = expandVertically(
                                                animationSpec = tween(260, easing = SmoothEase),
                                                expandFrom = Alignment.Top,
                                            ) + fadeIn(tween(200, easing = SmoothEase)),
                                            exit = shrinkVertically(
                                                animationSpec = tween(220, easing = SmoothEase),
                                                shrinkTowards = Alignment.Top,
                                            ) + fadeOut(tween(140, easing = SmoothEase)),
                                        ) {
                                            UrlEditRow(
                                                value = newtabUrlDraft,
                                                onValueChange = { newtabUrlDraft = it },
                                                showConfirm = newtabUrlDraft.trim() != newtabCustomUrl,
                                                onConfirm = { commitNewtabCustomUrl() },
                                                label = "自定义网页地址",
                                            )
                                        }
                                    }
                                }
                            }

                            // ==================================================
                            // ★ 新增分类：在查找其它设置吗？
                            // ==================================================
                            SmallTitle("在查找其它设置吗？")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToNewTabCustomization() }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "定制新标签页",
                                        color = MiuixTheme.colorScheme.primary,
                                        fontSize = 16.sp,
                                    )
                                }
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
                    title = "主页设置",
                    largeTitle = "主页设置",
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
// URL 输入行
// ============================================================

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