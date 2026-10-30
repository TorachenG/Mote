@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
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
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog
import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api as M3ExperimentalApi
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.torachen.mote.MoteApplication

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

internal const val KEY_SAVED_PASSWORDS = "saved_passwords_v1"
private const val KEY_UX_IMPROVEMENT = "ux_improvement_enabled"

// ============================================================
// 数据模型 & 持久化
// ============================================================

internal data class PasswordEntry(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val username: String = "",
    val password: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

internal fun loadSavedPasswords(prefs: SharedPreferences): List<PasswordEntry> {
    val raw = prefs.getString(KEY_SAVED_PASSWORDS, null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        val list = mutableListOf<PasswordEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val url = obj.optString("url", "")
            val password = obj.optString("password", "")
            if (url.isBlank() || password.isBlank()) continue
            list.add(
                PasswordEntry(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    url = url,
                    username = obj.optString("username", ""),
                    password = password,
                    note = obj.optString("note", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun saveSavedPasswords(prefs: SharedPreferences, entries: List<PasswordEntry>) {
    try {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("url", e.url)
                    put("username", e.username)
                    put("password", e.password)
                    put("note", e.note)
                    put("timestamp", e.timestamp)
                }
            )
        }
        prefs.edit().putString(KEY_SAVED_PASSWORDS, arr.toString()).apply()
    } catch (_: Exception) {
    }
}

// ============================================================
// CSV 解析 / 生成（参考 VIA / Chrome 格式：name,url,username,password,note）
// ============================================================

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i += 2
                } else {
                    inQuotes = !inQuotes
                    i++
                }
            }
            c == ',' && !inQuotes -> {
                result.add(sb.toString())
                sb.clear()
                i++
            }
            else -> {
                sb.append(c)
                i++
            }
        }
    }
    result.add(sb.toString())
    return result
}

private fun escapeCsv(value: String): String {
    return if (value.contains(',') || value.contains('"') ||
        value.contains('\n') || value.contains('\r')
    ) {
        "\"${value.replace("\"", "\"\"")}\""
    } else value
}

internal fun parsePasswordCsv(context: Context, uri: Uri): List<PasswordEntry>? {
    return try {
        val text = context.contentResolver.openInputStream(uri)?.use {
            it.bufferedReader().readText()
        } ?: return null
        val lines = text.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val header = parseCsvLine(lines.first())
        val isHeader = header.any {
            val h = it.trim().lowercase()
            h == "url" || h == "username" || h == "password" || h == "name"
        }

        var urlIdx = -1
        var userIdx = -1
        var passIdx = -1
        var noteIdx = -1

        if (isHeader) {
            header.forEachIndexed { idx, h ->
                when (h.trim().lowercase()) {
                    "url" -> urlIdx = idx
                    "username" -> userIdx = idx
                    "password" -> passIdx = idx
                    "note" -> noteIdx = idx
                }
            }
        } else {
            // Chrome / VIA 默认顺序：name,url,username,password,note
            urlIdx = 1
            userIdx = 2
            passIdx = 3
            noteIdx = 4
        }

        if (urlIdx < 0 || passIdx < 0) return null

        val startIdx = if (isHeader) 1 else 0
        val entries = mutableListOf<PasswordEntry>()
        for (i in startIdx until lines.size) {
            val fields = parseCsvLine(lines[i])
            val url = fields.getOrNull(urlIdx)?.trim().orEmpty()
            val password = fields.getOrNull(passIdx).orEmpty()
            if (url.isBlank() || password.isBlank()) continue
            entries.add(
                PasswordEntry(
                    url = url,
                    username = fields.getOrNull(userIdx)?.trim().orEmpty(),
                    password = password,
                    note = if (noteIdx >= 0) fields.getOrNull(noteIdx)?.trim().orEmpty() else "",
                )
            )
        }
        entries
    } catch (_: Exception) {
        null
    }
}

internal fun exportPasswordCsv(context: Context, uri: Uri, entries: List<PasswordEntry>): Boolean {
    return try {
        val sb = StringBuilder()
        sb.append("name,url,username,password,note\n")
        entries.forEach { e ->
            val host = try {
                Uri.parse(e.url).host ?: e.url
            } catch (_: Exception) {
                e.url
            }
            sb.append(escapeCsv(host)).append(",")
            sb.append(escapeCsv(e.url)).append(",")
            sb.append(escapeCsv(e.username)).append(",")
            sb.append(escapeCsv(e.password)).append(",")
            sb.append(escapeCsv(e.note)).append("\n")
        }
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        } ?: return false
        true
    } catch (_: Exception) {
        false
    }
}

// ============================================================
// 个人信息页
// ============================================================

@Composable
fun SettingsPersonalInfoPage(
    onBack: () -> Unit,
    onOpenPasswordManager: () -> Unit,
    onOpenUxImprovement: () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsPersonalInfoMiuix(
            onBack = onBack,
            onOpenPasswordManager = onOpenPasswordManager,
            onOpenUxImprovement = onOpenUxImprovement,
        )
        UiMode.Material -> SettingsPersonalInfoMaterial(
            onBack = onBack,
            onOpenPasswordManager = onOpenPasswordManager,
            onOpenUxImprovement = onOpenUxImprovement,
        )
    }
}

@Composable
private fun SettingsPersonalInfoMiuix(
    onBack: () -> Unit,
    onOpenPasswordManager: () -> Unit,
    onOpenUxImprovement: () -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var uxImprovementEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_UX_IMPROVEMENT, true))
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
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur =
                    prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
                KEY_UX_IMPROVEMENT -> uxImprovementEnabled = prefs.getBoolean(key, true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

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
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

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
                            SmallTitle("功能")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "密码管理器",
                                    pageRoute = "settings_personal_info",
                                    pageTitle = "个人信息",
                                    itemKey = "settings_personal_info_password_manager",
                                ) {
                                    ArrowPreference(
                                        title = "密码管理器",
                                        onClick = onOpenPasswordManager,
                                    )
                                }
                            }

                            SmallTitle("其它")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "加入“用户体验改进计划”",
                                    pageRoute = "settings_personal_info",
                                    pageTitle = "个人信息",
                                    itemKey = "settings_personal_info_ux_improvement_toggle",
                                ) {
                                    SwitchPreference(
                                        title = "加入“用户体验改进计划”",
                                        summary = "允许 Mote 收集相关信息分析产品的使用情况，用以改善产品服务和提升用户体验。更多信息请查看用户体验改进计划",
                                        checked = uxImprovementEnabled,
                                        onCheckedChange = { checked ->
                                            uxImprovementEnabled = checked
                                            prefs.edit().putBoolean(KEY_UX_IMPROVEMENT, checked).apply()
                                            MoteApplication.setAnalyticsEnabled(checked)
                                        },
                                    )
                                }
                                SearchableItem(
                                    title = "查看用户体验改进计划",
                                    pageRoute = "settings_personal_info",
                                    pageTitle = "个人信息",
                                    itemKey = "settings_personal_info_ux_improvement_view",
                                ) {
                                    ArrowPreference(
                                        title = "查看用户体验改进计划",
                                        onClick = onOpenUxImprovement,
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
                    pageRoute = "settings_personal_info",
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
                                        startFraction = 0.25f,
                                        curve = 2.2f,
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
                    title = "个人信息",
                    largeTitle = "个人信息",
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
                    }
                )
            }
        }
    }
}

@OptIn(M3ExperimentalApi::class)
@Composable
private fun SettingsPersonalInfoMaterial(
    onBack: () -> Unit,
    onOpenPasswordManager: () -> Unit,
    onOpenUxImprovement: () -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var uxImprovementEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_UX_IMPROVEMENT, true))
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
                    title = { M3Text("个人信息") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
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
                item(key = "info_header") {
                    M3Text(
                        text = "功能",
                        color = M3MaterialTheme.colorScheme.primary,
                        style = M3MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    )
                }

                item(key = "info_items") {
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
                                        title = "密码管理器",
                                        onClick = onOpenPasswordManager,
                                    )
                                }
                            )
                        )
                    }
                }

                item(key = "other_header") {
                    M3Text(
                        text = "其它",
                        color = M3MaterialTheme.colorScheme.primary,
                        style = M3MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    )
                }

                item(key = "other_items") {
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
                                        title = "加入“用户体验改进计划”",
                                        summary = "允许 Mote 收集相关信息分析产品的使用情况，用以改善产品服务和提升用户体验。更多信息请查看用户体验改进计划",
                                        checked = uxImprovementEnabled,
                                        onCheckedChange = { checked ->
                                            uxImprovementEnabled = checked
                                            prefs.edit().putBoolean(KEY_UX_IMPROVEMENT, checked).apply()
                                            MoteApplication.setAnalyticsEnabled(checked)
                                        },
                                    )
                                },
                                {
                                    M3SegmentArrowItem(
                                        title = "查看用户体验改进计划",
                                        onClick = onOpenUxImprovement,
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
// 密码管理器页
// ============================================================

@Composable
fun PasswordManagerPage(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> PasswordManagerPageMiuix(onBack)
        UiMode.Material -> PasswordManagerPageMaterial(onBack)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordManagerPageMiuix(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur =
                    prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val savedPasswords = remember {
        mutableStateListOf<PasswordEntry>().apply {
            addAll(loadSavedPasswords(prefs))
        }
    }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<PasswordEntry?>(null) }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun persist() {
        saveSavedPasswords(prefs, savedPasswords.toList())
    }

    androidx.activity.compose.BackHandler(enabled = isMultiSelectMode) {
        exitMultiSelect()
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val entries = withContext(Dispatchers.IO) { parsePasswordCsv(context, uri) }
            if (entries == null) {
                Toast.makeText(context, "导入失败，请检查文件格式", Toast.LENGTH_LONG).show()
                return@launch
            }
            if (entries.isEmpty()) {
                Toast.makeText(context, "文件中没有找到密码", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val existing = loadSavedPasswords(prefs).toMutableList()
            var added = 0
            var skipped = 0
            entries.forEach { e ->
                val dup = existing.any {
                    it.url == e.url && it.username == e.username && it.password == e.password
                }
                if (dup) {
                    skipped++
                } else {
                    existing.add(e)
                    added++
                }
            }
            saveSavedPasswords(prefs, existing)
            savedPasswords.clear()
            savedPasswords.addAll(existing)
            val msg = buildString {
                append("已导入 $added 条")
                if (skipped > 0) append("，跳过 $skipped 条重复")
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (savedPasswords.isEmpty()) {
            Toast.makeText(context, "暂无密码可导出", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                exportPasswordCsv(context, uri, savedPasswords.toList())
            }
            if (ok) {
                Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "导出失败", Toast.LENGTH_LONG).show()
            }
        }
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
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

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
                    item(key = "functions") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("功能")
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "导入密码",
                                    pageRoute = "settings_password_manager",
                                    pageTitle = "密码管理器",
                                    itemKey = "settings_password_manager_import",
                                ) {
                                    ArrowPreference(
                                        title = "导入密码",
                                        summary = "从 CSV 文件中导入密码",
                                        onClick = {
                                            importLauncher.launch(
                                                arrayOf(
                                                    "text/csv",
                                                    "text/comma-separated-values",
                                                    "application/csv",
                                                    "application/vnd.ms-excel",
                                                    "text/plain",
                                                    "*/*",
                                                )
                                            )
                                        },
                                    )
                                }
                                SearchableItem(
                                    title = "导出密码",
                                    pageRoute = "settings_password_manager",
                                    pageTitle = "密码管理器",
                                    itemKey = "settings_password_manager_export",
                                ) {
                                    ArrowPreference(
                                        title = "导出密码",
                                        summary = "请在使用完成后及时删除，避免数据泄露",
                                        onClick = {
                                            val date = java.text.SimpleDateFormat(
                                                "yyyyMMdd_HHmmss", java.util.Locale.US
                                            ).format(java.util.Date())
                                            exportLauncher.launch("Mote_Passwords_$date.csv")
                                        },
                                    )
                                }
                            }
                        }
                    }

                    item(key = "saved_passwords") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("保存的密码")
                            if (savedPasswords.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
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
                                                imageVector = MiuixIcons.Lock,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = "暂无保存的密码",
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
                                        .padding(bottom = 12.dp),
                                ) {
                                    savedPasswords.forEachIndexed { idx, entry ->
                                        val isSelected = entry.id in selectedIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isMultiSelectMode) {
                                                            toggleSelect(entry.id)
                                                        } else {
                                                            editingEntry = entry
                                                            showEditDialog = true
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(entry.id)
                                                        }
                                                    }
                                                )
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (isMultiSelectMode) {
                                                Checkbox(
                                                    state = if (isSelected) ToggleableState.On
                                                    else ToggleableState.Off,
                                                    onClick = null,
                                                )
                                                Spacer(Modifier.width(16.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.url,
                                                    style = MiuixTheme.textStyles.body1,
                                                    color = MiuixTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = if (entry.username.isNotBlank())
                                                        entry.username
                                                    else "（无用户名）",
                                                    style = MiuixTheme.textStyles.body2,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                if (entry.note.isNotBlank()) {
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = entry.note,
                                                        style = MiuixTheme.textStyles.body2,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
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
                                        startFraction = 0.25f,
                                        curve = 2.2f,
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
                val barTitle = if (isMultiSelectMode) {
                    if (selectedIds.isEmpty()) "请选择项目"
                    else "已选择 ${selectedIds.size} 项"
                } else {
                    "密码管理器"
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
                            val allSelected = savedPasswords.isNotEmpty() &&
                                    selectedIds.size == savedPasswords.size
                            IconButton(onClick = {
                                if (allSelected) {
                                    selectedIds.clear()
                                } else {
                                    selectedIds.clear()
                                    selectedIds.addAll(savedPasswords.map { it.id })
                                }
                            }) {
                                Icon(
                                    imageVector = MiuixIcons.SelectAll,
                                    contentDescription = if (allSelected) "取消全选" else "全选",
                                    tint = if (allSelected)
                                        MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
                    .padding(bottom = navBarBottom + 24.dp)
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(28.dp)
                        shadowElevation = 24.dp.toPx()
                        ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                        spotShadowColor = Color.Transparent
                    }
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
                    IconButton(
                        onClick = {
                            if (selectedIds.size == 1) {
                                editingEntry = savedPasswords.find { it.id == selectedIds.first() }
                                if (editingEntry != null) showEditDialog = true
                            }
                        },
                        enabled = selectedIds.size == 1
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "编辑",
                            tint = if (selectedIds.size == 1)
                                MiuixTheme.colorScheme.onSurface
                            else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = {
                        if (selectedIds.isNotEmpty()) {
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
                    IconButton(onClick = {
                        editingEntry = null
                        showAddDialog = true
                    }) {
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

        PasswordEditDialogMiuix(
            show = showAddDialog,
            initial = null,
            onDismiss = {
                showAddDialog = false
                editingEntry = null
            },
            onSave = { url, username, password, note ->
                val newEntry = PasswordEntry(
                    url = url,
                    username = username,
                    password = password,
                    note = note,
                )
                savedPasswords.add(newEntry)
                persist()
                showAddDialog = false
                Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
            },
        )

        PasswordEditDialogMiuix(
            show = showEditDialog,
            initial = editingEntry,
            onDismiss = {
                showEditDialog = false
                editingEntry = null
            },
            onSave = { url, username, password, note ->
                val target = editingEntry
                if (target != null) {
                    val idx = savedPasswords.indexOfFirst { it.id == target.id }
                    if (idx >= 0) {
                        savedPasswords[idx] = target.copy(
                            url = url,
                            username = username,
                            password = password,
                            note = note,
                        )
                        persist()
                    }
                }
                showEditDialog = false
                editingEntry = null
                exitMultiSelect()
                Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
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
                    text = "确定要移除选中的 ${selectedIds.size} 条密码吗？",
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
                            val toRemove = selectedIds.toSet()
                            savedPasswords.removeAll { it.id in toRemove }
                            persist()
                            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
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

@Composable
private fun PasswordEditDialogMiuix(
    show: Boolean,
    initial: PasswordEntry?,
    onDismiss: () -> Unit,
    onSave: (url: String, username: String, password: String, note: String) -> Unit,
) {
    var urlInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    LaunchedEffect(show, initial?.id) {
        if (show) {
            urlInput = initial?.url ?: ""
            usernameInput = initial?.username ?: ""
            passwordInput = initial?.password ?: ""
            noteInput = initial?.note ?: ""
        }
    }

    WindowDialog(
        show = show,
        title = if (initial == null) "添加密码" else "编辑密码",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = "网页地址",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = "用户名",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = "密码",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = "备注",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

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
                        onSave(
                            urlInput.trim(),
                            usernameInput.trim(),
                            passwordInput,
                            noteInput.trim(),
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank() && passwordInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text(if (initial == null) "添加" else "保存") }
            }
        }
    }
}

// ============================================================
// 密码管理器页 —— Material 3 版
// ============================================================

@OptIn(M3ExperimentalApi::class, ExperimentalFoundationApi::class)
@Composable
private fun PasswordManagerPageMaterial(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val savedPasswords = remember {
        mutableStateListOf<PasswordEntry>().apply {
            addAll(loadSavedPasswords(prefs))
        }
    }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<PasswordEntry?>(null) }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun persist() {
        saveSavedPasswords(prefs, savedPasswords.toList())
    }

    androidx.activity.compose.BackHandler(enabled = isMultiSelectMode) {
        exitMultiSelect()
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val entries = withContext(Dispatchers.IO) { parsePasswordCsv(context, uri) }
            if (entries == null) {
                Toast.makeText(context, "导入失败，请检查文件格式", Toast.LENGTH_LONG).show()
                return@launch
            }
            if (entries.isEmpty()) {
                Toast.makeText(context, "文件中没有找到密码", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val existing = loadSavedPasswords(prefs).toMutableList()
            var added = 0
            var skipped = 0
            entries.forEach { e ->
                val dup = existing.any {
                    it.url == e.url && it.username == e.username && it.password == e.password
                }
                if (dup) {
                    skipped++
                } else {
                    existing.add(e)
                    added++
                }
            }
            saveSavedPasswords(prefs, existing)
            savedPasswords.clear()
            savedPasswords.addAll(existing)
            val msg = buildString {
                append("已导入 $added 条")
                if (skipped > 0) append("，跳过 $skipped 条重复")
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (savedPasswords.isEmpty()) {
            Toast.makeText(context, "暂无密码可导出", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                exportPasswordCsv(context, uri, savedPasswords.toList())
            }
            if (ok) {
                Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "导出失败", Toast.LENGTH_LONG).show()
            }
        }
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
                                    if (selectedIds.isEmpty()) "请选择项目"
                                    else "已选择 ${selectedIds.size} 项"
                                } else "密码管理器"
                            )
                        },
                        navigationIcon = {
                            M3IconButton(onClick = {
                                if (isMultiSelectMode) exitMultiSelect() else onBack()
                            }) {
                                M3Icon(
                                    imageVector = if (isMultiSelectMode) MiuixIcons.Close
                                    else MiuixIcons.Back,
                                    contentDescription = if (isMultiSelectMode) "退出多选" else "返回",
                                )
                            }
                        },
                        actions = {
                            if (isMultiSelectMode) {
                                val allSelected = savedPasswords.isNotEmpty() &&
                                        selectedIds.size == savedPasswords.size
                                M3IconButton(onClick = {
                                    if (allSelected) {
                                        selectedIds.clear()
                                    } else {
                                        selectedIds.clear()
                                        selectedIds.addAll(savedPasswords.map { it.id })
                                    }
                                }) {
                                    M3Icon(
                                        imageVector = MiuixIcons.SelectAll,
                                        contentDescription = if (allSelected) "取消全选" else "全选",
                                        tint = if (allSelected) M3MaterialTheme.colorScheme.primary
                                        else M3MaterialTheme.colorScheme.onSurface,
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
                    item(key = "functions_header") {
                        PasswordManagerSectionHeader("功能")
                    }

                    item(key = "functions") {
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
                                        M3SegmentItem(
                                            headline = { M3Text("导入密码") },
                                            supporting = {
                                                M3Text(
                                                    text = "从 CSV 文件中导入密码",
                                                    style = M3MaterialTheme.typography.bodyMedium,
                                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            },
                                            trailing = {
                                                M3Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            },
                                            onClick = {
                                                importLauncher.launch(
                                                    arrayOf(
                                                        "text/csv",
                                                        "text/comma-separated-values",
                                                        "application/csv",
                                                        "application/vnd.ms-excel",
                                                        "text/plain",
                                                        "*/*",
                                                    )
                                                )
                                            },
                                        )
                                    },
                                    {
                                        M3SegmentItem(
                                            headline = { M3Text("导出密码") },
                                            supporting = {
                                                M3Text(
                                                    text = "请在使用完成后及时删除，避免数据泄露",
                                                    style = M3MaterialTheme.typography.bodyMedium,
                                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            },
                                            trailing = {
                                                M3Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            },
                                            onClick = {
                                                val date = java.text.SimpleDateFormat(
                                                    "yyyyMMdd_HHmmss", java.util.Locale.US
                                                ).format(java.util.Date())
                                                exportLauncher.launch("Mote_Passwords_$date.csv")
                                            },
                                        )
                                    },
                                )
                            )
                        }
                    }

                    item(key = "saved_header") {
                        PasswordManagerSectionHeader("保存的密码")
                    }

                    item(key = "saved_items") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                .padding(bottom = M3_SEGMENT_GROUP_GAP),
                        ) {
                            if (savedPasswords.isEmpty()) {
                                androidx.compose.material3.Surface(
                                    shape = RoundedCornerShape(25.dp),
                                    color = androidx.compose.ui.graphics.lerp(
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
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            M3Icon(
                                                imageVector = MiuixIcons.Lock,
                                                contentDescription = null,
                                                tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            M3Text(
                                                text = "暂无保存的密码",
                                                style = M3MaterialTheme.typography.bodyMedium,
                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            } else {
                                val items: List<@Composable () -> Unit> =
                                    savedPasswords.map { entry ->
                                        {
                                            val isSelected = entry.id in selectedIds
                                            M3SegmentItem(
                                                headline = {
                                                    Column {
                                                        M3Text(
                                                            text = entry.url,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        M3Text(
                                                            text = if (entry.username.isNotBlank())
                                                                entry.username
                                                            else "（无用户名）",
                                                            style = M3MaterialTheme.typography.bodySmall,
                                                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        if (entry.note.isNotBlank()) {
                                                            Spacer(Modifier.height(2.dp))
                                                            M3Text(
                                                                text = entry.note,
                                                                style = M3MaterialTheme.typography.bodySmall,
                                                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        }
                                                    }
                                                },
                                                leading = if (isMultiSelectMode) {
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
                                                        if (isMultiSelectMode) {
                                                            toggleSelect(entry.id)
                                                        } else {
                                                            editingEntry = entry
                                                            showEditDialog = true
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(entry.id)
                                                        }
                                                    }
                                                )
                                            )
                                        }
                                    }
                                MoteSegmentedColumn(
                                    color = M3MaterialTheme.colorScheme.surface,content = items)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
                    .padding(bottom = navBarBottom + 24.dp)
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(28.dp)
                        shadowElevation = 8.dp.toPx()
                    }
                    .background(
                        color = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(28.dp),
                    )
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isMultiSelectMode) {
                    M3IconButton(
                        onClick = {
                            if (selectedIds.size == 1) {
                                editingEntry = savedPasswords.find { it.id == selectedIds.first() }
                                if (editingEntry != null) showEditDialog = true
                            }
                        },
                        enabled = selectedIds.size == 1
                    ) {
                        M3Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "编辑",
                            tint = if (selectedIds.size == 1)
                                M3MaterialTheme.colorScheme.onSurface
                            else M3MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    M3IconButton(onClick = {
                        if (selectedIds.isNotEmpty()) {
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
                    M3IconButton(onClick = {
                        editingEntry = null
                        showAddDialog = true
                    }) {
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
            PasswordEditDialogMaterial(
                initial = null,
                onDismiss = {
                    showAddDialog = false
                    editingEntry = null
                },
                onSave = { url, username, password, note ->
                    val newEntry = PasswordEntry(
                        url = url,
                        username = username,
                        password = password,
                        note = note,
                    )
                    savedPasswords.add(newEntry)
                    persist()
                    showAddDialog = false
                    Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showEditDialog) {
            PasswordEditDialogMaterial(
                initial = editingEntry,
                onDismiss = {
                    showEditDialog = false
                    editingEntry = null
                },
                onSave = { url, username, password, note ->
                    val target = editingEntry
                    if (target != null) {
                        val idx = savedPasswords.indexOfFirst { it.id == target.id }
                        if (idx >= 0) {
                            savedPasswords[idx] = target.copy(
                                url = url,
                                username = username,
                                password = password,
                                note = note,
                            )
                            persist()
                        }
                    }
                    showEditDialog = false
                    editingEntry = null
                    exitMultiSelect()
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showDeleteConfirmDialog) {
            M3AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { M3Text("删除") },
                text = { M3Text("确定要移除选中的 ${selectedIds.size} 条密码吗？") },
                confirmButton = {
                    M3TextButton(
                        onClick = {
                            val toRemove = selectedIds.toSet()
                            savedPasswords.removeAll { it.id in toRemove }
                            persist()
                            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                            exitMultiSelect()
                            showDeleteConfirmDialog = false
                        }
                    ) { M3Text("删除") }
                },
                dismissButton = {
                    M3TextButton(
                        onClick = { showDeleteConfirmDialog = false }
                    ) { M3Text("取消") }
                },
            )
        }
    }
}

@OptIn(M3ExperimentalApi::class)
@Composable
private fun PasswordEditDialogMaterial(
    initial: PasswordEntry?,
    onDismiss: () -> Unit,
    onSave: (url: String, username: String, password: String, note: String) -> Unit,
) {
    var urlInput by remember { mutableStateOf(initial?.url ?: "") }
    var usernameInput by remember { mutableStateOf(initial?.username ?: "") }
    var passwordInput by remember { mutableStateOf(initial?.password ?: "") }
    var noteInput by remember { mutableStateOf(initial?.note ?: "") }

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text(if (initial == null) "添加密码" else "编辑密码") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                M3OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { M3Text("网页地址") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                M3OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { M3Text("用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                M3OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { M3Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                M3OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { M3Text("备注") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            M3TextButton(
                onClick = {
                    onSave(
                        urlInput.trim(),
                        usernameInput.trim(),
                        passwordInput,
                        noteInput.trim(),
                    )
                },
                enabled = urlInput.isNotBlank() && passwordInput.isNotBlank(),
            ) { M3Text(if (initial == null) "添加" else "保存") }
        },
        dismissButton = {
            M3TextButton(onClick = onDismiss) { M3Text("取消") }
        },
    )
}

@Composable
private fun PasswordManagerSectionHeader(text: String) {
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