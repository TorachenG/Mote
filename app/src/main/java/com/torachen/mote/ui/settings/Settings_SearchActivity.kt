@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
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
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Send
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
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import kotlinx.coroutines.delay
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.ui.zIndex
// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)


private const val PRESS_FADE_IN_MS = 220
private const val PRESS_HOLD_MS = 120
private const val PRESS_FADE_OUT_MS = 320
private const val PRESS_GAP_MS = 140

internal val BUILT_IN_SEARCH_ENGINES = listOf(
    "必应",
    "抖音",
    "百度",
    "神马",
    "搜狗",
    "谷歌",
)

internal const val CUSTOM_ENGINE_LABEL = "自定义引擎"
internal const val CUSTOM_ENGINE_INDEX = 6

private const val KEY_DEFAULT_ENGINE = "default_search_engine"
private const val KEY_DEFAULT_ENGINE_DISPLAY = "default_search_engine_display"

private const val KEY_ENGINE_SWITCHER_BAR = "engine_switcher_bar_enabled"
private const val KEY_QUICK_INPUT_BAR = "quick_input_bar_enabled"
private const val KEY_SCAN_MODULE = "scan_module_enabled"
private const val KEY_HISTORY_MODULE = "history_module_enabled"
private const val KEY_SEARCH_SUGGEST = "search_suggest_enabled"

internal suspend fun fetchSearchSuggestions(query: String, engineIndex: Int): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
    val urlStr = when (engineIndex) {
        0 -> "https://api.bing.com/qsonhs.aspx?type=cb&q=$encodedQuery"
        2 -> "https://suggestion.baidu.com/su?wd=$encodedQuery&action=opensearch&ie=utf-8"
        5 -> "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"
        4 -> "https://w.sugg.sogou.com/sugg/ajaj_json.jsp?key=$encodedQuery&type=web&ori=yes"
        else -> "https://api.bing.com/qsonhs.aspx?type=cb&q=$encodedQuery"
    }
    try {
        val conn = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
        if (conn.responseCode == 200) {
            val body = conn.inputStream.bufferedReader().readText()
            return@withContext when (engineIndex) {
                2, 4, 5 -> {
                    val arr = org.json.JSONArray(body)
                    val list = mutableListOf<String>()
                    val suggestArr = arr.optJSONArray(1)
                    if (suggestArr != null) {
                        for (i in 0 until suggestArr.length()) {
                            list.add(suggestArr.optString(i))
                        }
                    }
                    list
                }
                else -> {
                    val json = org.json.JSONObject(body)
                    val results = json.optJSONObject("AS")?.optJSONArray("Results")
                    val list = mutableListOf<String>()
                    if (results != null && results.length() > 0) {
                        val suggests = results.getJSONObject(0).optJSONArray("Suggests")
                        if (suggests != null) {
                            for (i in 0 until suggests.length()) {
                                suggests.optJSONObject(i)?.optString("Txt")?.let { if (it.isNotBlank()) list.add(it) }
                            }
                        }
                    }
                    list
                }
            }
        }
    } catch (_: Exception) {}
    emptyList()
}

// ============================================================
// 自定义搜索引擎
// ============================================================

internal const val KEY_ACTIVE_CUSTOM_ENGINE_ID = "active_custom_engine_id"

internal data class CustomSearchEngine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
)

private const val KEY_CUSTOM_SEARCH_ENGINES = "custom_search_engines"

internal fun loadCustomSearchEngines(prefs: SharedPreferences): List<CustomSearchEngine> {
    val raw = prefs.getString(KEY_CUSTOM_SEARCH_ENGINES, null) ?: return emptyList()
    return try {
        val arr = org.json.JSONArray(raw)
        val list = mutableListOf<CustomSearchEngine>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val name = obj.optString("name", "")
            val url = obj.optString("url", "")
            if (url.isBlank()) continue
            list.add(
                CustomSearchEngine(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    name = name.ifBlank { url },
                    url = url,
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun saveCustomSearchEngines(prefs: SharedPreferences, engines: List<CustomSearchEngine>) {
    try {
        val arr = org.json.JSONArray()
        engines.forEach { e ->
            arr.put(org.json.JSONObject().apply {
                put("id", e.id)
                put("name", e.name)
                put("url", e.url)
            })
        }
        prefs.edit().putString(KEY_CUSTOM_SEARCH_ENGINES, arr.toString()).apply()
    } catch (_: Exception) {
    }
}

internal fun buildSearchUrlTemplate(url: String, keyword: String): String? {
    if (url.isBlank() || keyword.isBlank()) return null

    if (url.contains(keyword, ignoreCase = true)) {
        return url.replace(keyword, "%s", ignoreCase = true)
    }

    try {
        val encodedKw = java.net.URLEncoder.encode(keyword, "UTF-8")
        if (url.contains(encodedKw, ignoreCase = true)) {
            return url.replace(encodedKw, "%s", ignoreCase = true)
        }
        val encodedKwSpace = encodedKw.replace("+", "%20")
        if (url.contains(encodedKwSpace, ignoreCase = true)) {
            return url.replace(encodedKwSpace, "%s", ignoreCase = true)
        }
        val uriEncodedKw = android.net.Uri.encode(keyword)
        if (url.contains(uriEncodedKw, ignoreCase = true)) {
            return url.replace(uriEncodedKw, "%s", ignoreCase = true)
        }
    } catch (_: Exception) {}

    try {
        val uri = android.net.Uri.parse(url)
        val query = uri.query
        if (query != null) {
            val params = query.split("&")
            for (param in params) {
                val kv = param.split("=")
                if (kv.size == 2) {
                    val key = kv[0]
                    val value = kv[1]
                    try {
                        val decodedValue = java.net.URLDecoder.decode(value, "UTF-8")
                        if (decodedValue.equals(keyword, ignoreCase = true)) {
                            val newParam = "$key=%s"
                            val newQuery = query.replace(param, newParam)
                            return url.replace(query, newQuery)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    } catch (_: Exception) {}

    return null
}

// ============================================================
// 状态
// ============================================================

@Immutable
private data class SearchPageState(
    val engineSwitcherBarEnabled: Boolean,
    val quickInputBarEnabled: Boolean,
    val scanModuleEnabled: Boolean,
    val historyModuleEnabled: Boolean,
    val searchSuggestEnabled: Boolean,
    val activeEngineName: String,
)

@Immutable
private data class SearchPageActions(
    val onBack: () -> Unit,
    val onEngineSwitcherBarChange: (Boolean) -> Unit,
    val onQuickInputBarChange: (Boolean) -> Unit,
    val onScanModuleChange: (Boolean) -> Unit,
    val onHistoryModuleChange: (Boolean) -> Unit,
    val onSearchSuggestChange: (Boolean) -> Unit,
    val onOpenCustomEngineManager: () -> Unit,
)

// ============================================================
// 入口
// ============================================================

@Composable
fun SettingsSearchPage(
    onBack: () -> Unit,
    onOpenCustomEngineManager: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    var engineSwitcherBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_ENGINE_SWITCHER_BAR, true))
    }
    var quickInputBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_QUICK_INPUT_BAR, true))
    }
    var scanModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_SCAN_MODULE, true))
    }
    var historyModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_HISTORY_MODULE, true))
    }
    var searchSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_SEARCH_SUGGEST, true))
    }

    fun getActiveEngineName(): String {
        val activeId = prefs.getString(KEY_ACTIVE_CUSTOM_ENGINE_ID, null)
        val engines = loadCustomSearchEngines(prefs)
        return if (!activeId.isNullOrBlank()) {
            engines.find { it.id == activeId }?.name ?: "必应"
        } else {
            val index = prefs.getInt(KEY_DEFAULT_ENGINE, 0)
            BUILT_IN_SEARCH_ENGINES.getOrNull(index) ?: "必应"
        }
    }

    var activeEngineName by remember { mutableStateOf(getActiveEngineName()) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CUSTOM_SEARCH_ENGINES || key == KEY_ACTIVE_CUSTOM_ENGINE_ID || key == KEY_DEFAULT_ENGINE) {
                activeEngineName = getActiveEngineName()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val state = SearchPageState(
        engineSwitcherBarEnabled = engineSwitcherBarEnabled,
        quickInputBarEnabled = quickInputBarEnabled,
        scanModuleEnabled = scanModuleEnabled,
        historyModuleEnabled = historyModuleEnabled,
        searchSuggestEnabled = searchSuggestEnabled,
        activeEngineName = activeEngineName,
    )

    val actions = SearchPageActions(
        onBack = onBack,
        onEngineSwitcherBarChange = {
            engineSwitcherBarEnabled = it
            prefs.edit().putBoolean(KEY_ENGINE_SWITCHER_BAR, it).apply()
        },
        onQuickInputBarChange = {
            quickInputBarEnabled = it
            prefs.edit().putBoolean(KEY_QUICK_INPUT_BAR, it).apply()
        },
        onScanModuleChange = {
            scanModuleEnabled = it
            prefs.edit().putBoolean(KEY_SCAN_MODULE, it).apply()
        },
        onHistoryModuleChange = {
            historyModuleEnabled = it
            prefs.edit().putBoolean(KEY_HISTORY_MODULE, it).apply()
        },
        onSearchSuggestChange = {
            searchSuggestEnabled = it
            prefs.edit().putBoolean(KEY_SEARCH_SUGGEST, it).apply()
        },
        onOpenCustomEngineManager = onOpenCustomEngineManager,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsSearchMiuix(state, actions)
        UiMode.Material -> SettingsSearchMaterial(state, actions)
    }
}

// ============================================================
// MIUIX 版
// ============================================================

@Composable
private fun SettingsSearchMiuix(
    state: SearchPageState,
    actions: SearchPageActions,
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

    val flashProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (SettingsLayoutNavArgs.flashSearchHistoryOnEnter) {
            SettingsLayoutNavArgs.flashSearchHistoryOnEnter = false
            delay(320)

            repeat(2) { idx ->
                if (idx > 0) delay(PRESS_GAP_MS.toLong())

                flashProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(PRESS_FADE_IN_MS, easing = SmoothEase),
                )
                delay(PRESS_HOLD_MS.toLong())
                flashProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(PRESS_FADE_OUT_MS, easing = SmoothEase),
                )
            }
        }
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

            val fillerHeight = (
                    viewportHeight - contentTopPadding - contentHeight - 12.dp + 1.dp
                    ).coerceAtLeast(0.dp)

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
                        bottom = 12.dp,
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { contentHeightPx = it.size.height }
                        ) {
                            SmallTitle("搜索引擎")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "默认搜索引擎",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_default_engine",
                                ) {
                                    ArrowPreference(
                                        title = "默认搜索引擎",
                                        endActions = {
                                            AnimatedContent(
                                                targetState = state.activeEngineName,
                                                transitionSpec = {
                                                    (fadeIn(tween(200, easing = SmoothEase)) +
                                                            scaleIn(initialScale = 0.8f, animationSpec = tween(200, easing = SmoothEase)))
                                                        .togetherWith(
                                                            fadeOut(tween(150, easing = SmoothEase)) +
                                                                    scaleOut(targetScale = 0.8f, animationSpec = tween(150, easing = SmoothEase))
                                                        )
                                                },
                                                label = "customEngineNameAnim"
                                            ) { name ->
                                                Text(
                                                    text = name,
                                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                                )
                                            }
                                        },
                                        onClick = actions.onOpenCustomEngineManager,
                                    )
                                }
                                SearchableItem(
                                    title = "搜索引擎切换栏",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_engine_switcher_bar",
                                ) {
                                    SwitchPreference(
                                        title = "搜索引擎切换栏",
                                        checked = state.engineSwitcherBarEnabled,
                                        onCheckedChange = actions.onEngineSwitcherBarChange,
                                    )
                                }
                            }

                            SmallTitle("辅助功能")

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SearchableItem(
                                    title = "搜索建议",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_suggest",
                                ) {
                                    SwitchPreference(
                                        title = "搜索建议",
                                        summary = "在搜索栏输入内容时显示搜索建议",
                                        checked = state.searchSuggestEnabled,
                                        onCheckedChange = actions.onSearchSuggestChange,
                                    )
                                }
                                SearchableItem(
                                    title = "快捷输入栏",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_quick_input_bar",
                                ) {
                                    SwitchPreference(
                                        title = "快捷输入栏",
                                        summary = "在主页搜索栏输入时，键盘上方显示常用字符",
                                        checked = state.quickInputBarEnabled,
                                        onCheckedChange = actions.onQuickInputBarChange,
                                    )
                                }
                                SearchableItem(
                                    title = "扫一扫模块",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_scan_module",
                                ) {
                                    SwitchPreference(
                                        title = "扫一扫模块",
                                        checked = state.scanModuleEnabled,
                                        onCheckedChange = actions.onScanModuleChange,
                                    )
                                }
                                SearchableItem(
                                    title = "搜索历史模块",
                                    pageRoute = "settings_search",
                                    pageTitle = "搜索设置",
                                    itemKey = "settings_search_history_module",
                                ) {
                                    Box {
                                        SwitchPreference(
                                            title = "搜索历史模块",
                                            checked = state.historyModuleEnabled,
                                            onCheckedChange = actions.onHistoryModuleChange,
                                        )
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    color = MiuixTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.08f * flashProgress.value
                                                    )
                                                )
                                        )
                                    }
                                }
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

                SettingsPageAutoScroll(
                    lazyListState = listState,
                    pageRoute = "settings_search",
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
                                        endFraction = 1.35f,
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
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
                    title = "搜索设置",
                    largeTitle = "搜索设置",
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
private fun SettingsSearchMaterial(
    state: SearchPageState,
    actions: SearchPageActions,
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
                    title = { M3Text("搜索设置") },
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
                item(key = "engine_header") {
                    SearchSectionHeader("搜索引擎")
                }

                item(key = "engine_items") {
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
                                        headline = { M3Text("默认搜索引擎") },
                                        trailing = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AnimatedContent(
                                                    targetState = state.activeEngineName,
                                                    transitionSpec = {
                                                        (fadeIn(tween(200, easing = SmoothEase)) +
                                                                scaleIn(initialScale = 0.8f, animationSpec = tween(200, easing = SmoothEase)))
                                                            .togetherWith(
                                                                fadeOut(tween(150, easing = SmoothEase)) +
                                                                        scaleOut(targetScale = 0.8f, animationSpec = tween(150, easing = SmoothEase))
                                                            )
                                                    },
                                                    label = "m3CustomEngineNameAnim"
                                                ) { name ->
                                                    M3Text(
                                                        text = name,
                                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                        style = M3MaterialTheme.typography.bodyMedium,
                                                    )
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                M3Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        },
                                        onClick = actions.onOpenCustomEngineManager
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "搜索引擎切换栏",
                                        summary = null,
                                        checked = state.engineSwitcherBarEnabled,
                                        onCheckedChange = actions.onEngineSwitcherBarChange,
                                    )
                                },
                            )
                        )
                    }
                }

                item(key = "aux_header") {
                    SearchSectionHeader("辅助功能")
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
                                        title = "搜索建议",
                                        summary = "在搜索栏输入内容时显示搜索建议",
                                        checked = state.searchSuggestEnabled,
                                        onCheckedChange = actions.onSearchSuggestChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "快捷输入栏",
                                        summary = "在主页搜索栏输入时，键盘上方显示常用字符",
                                        checked = state.quickInputBarEnabled,
                                        onCheckedChange = actions.onQuickInputBarChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "扫一扫模块",
                                        summary = null,
                                        checked = state.scanModuleEnabled,
                                        onCheckedChange = actions.onScanModuleChange,
                                    )
                                },
                                {
                                    M3SegmentSwitchItem(
                                        title = "搜索历史模块",
                                        summary = null,
                                        checked = state.historyModuleEnabled,
                                        onCheckedChange = actions.onHistoryModuleChange,
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
private fun SearchSectionHeader(text: String) {
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
// 自定义搜索引擎管理页
// ============================================================

@Composable
fun CustomSearchEnginesPage(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> CustomSearchEnginesPageMiuix(onBack)
        UiMode.Material -> CustomSearchEnginesPageMaterial(onBack)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomSearchEnginesPageMiuix(onBack: () -> Unit) {
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

    val engines = remember {
        mutableStateListOf<CustomSearchEngine>().apply {
            addAll(loadCustomSearchEngines(prefs))
        }
    }

    var activeEngineId by remember {
        mutableStateOf(prefs.getString(KEY_ACTIVE_CUSTOM_ENGINE_ID, null))
    }
    var activePresetIndex by remember {
        mutableIntStateOf(prefs.getInt(KEY_DEFAULT_ENGINE, 0))
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingEngine by remember { mutableStateOf<CustomSearchEngine?>(null) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id)
        else selectedIds.add(id)
    }

    fun persist() {
        saveCustomSearchEngines(prefs, engines.toList())
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
                    item(key = "presets") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("预设")
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                BUILT_IN_SEARCH_ENGINES.forEachIndexed { index, name ->
                                    RadioButtonPreference(
                                        title = name,
                                        selected = activeEngineId == null && activePresetIndex == index,
                                        onClick = {
                                            activeEngineId = null
                                            activePresetIndex = index
                                            prefs.edit()
                                                .putInt(KEY_DEFAULT_ENGINE, index)
                                                .putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, "")
                                                .apply()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item(key = "customs") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("自定义")
                            if (engines.isEmpty()) {
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
                                                imageVector = MiuixIcons.Send,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = "暂无自定义引擎",
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
                                    engines.forEachIndexed { idx, engine ->
                                        val isSelected = engine.id in selectedIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isMultiSelectMode) {
                                                            toggleSelect(engine.id)
                                                        } else {
                                                            activeEngineId = engine.id
                                                            prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, engine.id).apply()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(engine.id)
                                                        }
                                                    }
                                                )
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (!isMultiSelectMode) {
                                                RadioButton(
                                                    selected = activeEngineId == engine.id,
                                                    onClick = null
                                                )
                                            } else {
                                                Checkbox(
                                                    state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                                                    onClick = null
                                                )
                                            }
                                            Spacer(Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = engine.name.ifBlank { engine.url },
                                                    style = MiuixTheme.textStyles.body1,
                                                    color = MiuixTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                if (engine.url.isNotBlank()) {
                                                    Text(
                                                        text = engine.url,
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
                                        endFraction = 1.35f,
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
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
                    if (selectedIds.isEmpty()) "请选择项目" else "已选择 ${selectedIds.size} 项"
                } else {
                    "默认搜索引擎"
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
                            val allSelected = engines.isNotEmpty() && selectedIds.size == engines.size
                            IconButton(onClick = {
                                if (allSelected) selectedIds.clear() else {
                                    selectedIds.clear()
                                    selectedIds.addAll(engines.map { it.id })
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
                                editingEngine = engines.find { it.id == selectedIds.first() }
                                if (editingEngine != null) showEditDialog = true
                            }
                        },
                        enabled = selectedIds.size == 1
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "编辑",
                            tint = if (selectedIds.size == 1) MiuixTheme.colorScheme.onSurface
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
                    IconButton(onClick = { showTestDialog = true }) {
                        Icon(
                            imageVector = MiuixIcons.Search,
                            contentDescription = "测试",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
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

        val activeEngineName = if (activeEngineId != null) engines.find { it.id == activeEngineId }?.name else BUILT_IN_SEARCH_ENGINES.getOrNull(activePresetIndex)
        val activeEngineUrl = if (activeEngineId != null) engines.find { it.id == activeEngineId }?.url else null

        CustomSearchEngineTestDialogMiuix(
            show = showTestDialog,
            engineName = activeEngineName ?: "测试",
            engineUrl = activeEngineUrl,
            presetIndex = activePresetIndex,
            onDismiss = { showTestDialog = false },
        )

        CustomSearchEngineAddDialogMiuix(
            show = showAddDialog,
            onDismiss = { showAddDialog = false },
            onAdd = { name, url ->
                val newEngine = CustomSearchEngine(
                    name = name.ifBlank { url },
                    url = url,
                )
                engines.add(newEngine)
                if (activeEngineId == null && engines.size == 1) {
                    activeEngineId = newEngine.id
                    prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, newEngine.id).apply()
                }
                persist()
                showAddDialog = false
                Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
            },
        )

        CustomSearchEngineEditDialogMiuix(
            show = showEditDialog,
            engine = editingEngine,
            onDismiss = { showEditDialog = false },
            onSave = { name, url ->
                val index = engines.indexOfFirst { it.id == editingEngine?.id }
                if (index >= 0) {
                    engines[index] = engines[index].copy(name = name, url = url)
                    persist()
                }
                showEditDialog = false
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
                    text = "确定要移除选中的 ${selectedIds.size} 条自定义引擎吗？",
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
                            val remaining = engines.filter { it.id !in toRemove }
                            engines.clear()
                            engines.addAll(remaining)
                            if (activeEngineId in toRemove) {
                                activeEngineId = engines.firstOrNull()?.id
                                prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, activeEngineId ?: "").apply()
                            }
                            persist()
                            Toast.makeText(context, "已移除选中的自定义引擎", Toast.LENGTH_SHORT).show()
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
private fun CustomSearchEnginesPageMaterial(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val engines = remember {
        mutableStateListOf<CustomSearchEngine>().apply {
            addAll(loadCustomSearchEngines(prefs))
        }
    }

    var activeEngineId by remember {
        mutableStateOf(prefs.getString(KEY_ACTIVE_CUSTOM_ENGINE_ID, null))
    }
    var activePresetIndex by remember {
        mutableIntStateOf(prefs.getInt(KEY_DEFAULT_ENGINE, 0))
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingEngine by remember { mutableStateOf<CustomSearchEngine?>(null) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id)
        else selectedIds.add(id)
    }

    fun persist() {
        saveCustomSearchEngines(prefs, engines.toList())
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
                            M3Text(if (isMultiSelectMode) "已选择 ${selectedIds.size} 项" else "默认搜索引擎")
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
                                val allSelected = engines.isNotEmpty() && selectedIds.size == engines.size
                                M3IconButton(onClick = {
                                    if (allSelected) selectedIds.clear() else {
                                        selectedIds.clear()
                                        selectedIds.addAll(engines.map { it.id })
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
                    item(key = "presets") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            M3Text("预设", modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp), color = M3MaterialTheme.colorScheme.primary, style = M3MaterialTheme.typography.titleSmall)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                    .padding(bottom = M3_SEGMENT_GROUP_GAP),
                            ) {
                                val presetItems: List<@Composable () -> Unit> = BUILT_IN_SEARCH_ENGINES.mapIndexed { index, name ->
                                    {
                                        M3SegmentItem(
                                            headline = { M3Text(name) },
                                            leading = {
                                                androidx.compose.material3.RadioButton(
                                                    selected = activeEngineId == null && activePresetIndex == index,
                                                    onClick = null,
                                                )
                                            },
                                            onClick = {
                                                activeEngineId = null
                                                activePresetIndex = index
                                                prefs.edit().putInt(KEY_DEFAULT_ENGINE, index).putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, "").apply()
                                            }
                                        )
                                    }
                                }
                                MoteSegmentedColumn(
                                    color = M3MaterialTheme.colorScheme.surface,content = presetItems)
                            }
                        }
                    }

                    item(key = "customs") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            M3Text("自定义", modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp), color = M3MaterialTheme.colorScheme.primary, style = M3MaterialTheme.typography.titleSmall)
                            if (engines.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                        .padding(bottom = M3_SEGMENT_GROUP_GAP)
                                ) {
                                    M3Surface(
                                        shape = RoundedCornerShape(25.dp),
                                        color = lerp(M3MaterialTheme.colorScheme.surfaceContainerLow, M3MaterialTheme.colorScheme.secondaryContainer, 0.10f),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                M3Icon(imageVector = MiuixIcons.Send, contentDescription = null, tint = M3MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                                                Spacer(Modifier.height(12.dp))
                                                M3Text("暂无自定义引擎", style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    val items: List<@Composable () -> Unit> = engines.map { engine ->
                                        {
                                            val isSelected = engine.id in selectedIds
                                            M3SegmentItem(
                                                headline = {
                                                    Column {
                                                        M3Text(engine.name.ifBlank { engine.url })
                                                        Spacer(Modifier.height(2.dp))
                                                        M3Text(engine.url, style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                },
                                                leading = {
                                                    if (!isMultiSelectMode) {
                                                        androidx.compose.material3.RadioButton(selected = activeEngineId == engine.id, onClick = null)
                                                    } else {
                                                        androidx.compose.material3.Checkbox(checked = isSelected, onCheckedChange = null)
                                                    }
                                                },
                                                onClick = {
                                                    if (isMultiSelectMode) {
                                                        toggleSelect(engine.id)
                                                    } else {
                                                        activeEngineId = engine.id
                                                        prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, engine.id).apply()
                                                    }
                                                },
                                                modifier = Modifier.combinedClickable(
                                                    onClick = {
                                                        if (isMultiSelectMode) {
                                                            toggleSelect(engine.id)
                                                        } else {
                                                            activeEngineId = engine.id
                                                            prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, engine.id).apply()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isMultiSelectMode) {
                                                            isMultiSelectMode = true
                                                            toggleSelect(engine.id)
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
                                editingEngine = engines.find { it.id == selectedIds.first() }
                                if (editingEngine != null) showEditDialog = true
                            }
                        },
                        enabled = selectedIds.size == 1
                    ) {
                        M3Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "编辑",
                            tint = if (selectedIds.size == 1) M3MaterialTheme.colorScheme.onSurface else M3MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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
                    M3IconButton(onClick = { showTestDialog = true }) {
                        M3Icon(
                            imageVector = MiuixIcons.Search,
                            contentDescription = "测试",
                            tint = M3MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
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

        val activeEngineName = if (activeEngineId != null) engines.find { it.id == activeEngineId }?.name else BUILT_IN_SEARCH_ENGINES.getOrNull(activePresetIndex)
        val activeEngineUrl = if (activeEngineId != null) engines.find { it.id == activeEngineId }?.url else null

        CustomSearchEngineTestDialogMaterial(
            show = showTestDialog,
            engineName = activeEngineName ?: "测试",
            engineUrl = activeEngineUrl,
            presetIndex = activePresetIndex,
            onDismiss = { showTestDialog = false },
        )

        if (showAddDialog) {
            CustomSearchEngineAddDialogMaterial(
                onDismiss = { showAddDialog = false },
                onAdd = { name, url ->
                    val newEngine = CustomSearchEngine(name = name.ifBlank { url }, url = url)
                    engines.add(newEngine)
                    if (activeEngineId == null && engines.size == 1) {
                        activeEngineId = newEngine.id
                        prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, newEngine.id).apply()
                    }
                    persist()
                    showAddDialog = false
                    Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showEditDialog) {
            CustomSearchEngineEditDialogMaterial(
                engine = editingEngine,
                onDismiss = { showEditDialog = false },
                onSave = { name, url ->
                    val index = engines.indexOfFirst { it.id == editingEngine?.id }
                    if (index >= 0) {
                        engines[index] = engines[index].copy(name = name, url = url)
                        persist()
                    }
                    showEditDialog = false
                    exitMultiSelect()
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showDeleteConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { M3Text("删除") },
                text = { M3Text("确定要移除选中的 ${selectedIds.size} 条自定义引擎吗？") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val toRemove = selectedIds.toSet()
                            val remaining = engines.filter { it.id !in toRemove }
                            engines.clear()
                            engines.addAll(remaining)
                            if (activeEngineId in toRemove) {
                                activeEngineId = engines.firstOrNull()?.id
                                prefs.edit().putString(KEY_ACTIVE_CUSTOM_ENGINE_ID, activeEngineId ?: "").apply()
                            }
                            persist()
                            Toast.makeText(context, "已移除选中的自定义引擎", Toast.LENGTH_SHORT).show()
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
// 添加自定义引擎对话框
// ============================================================

@Composable
private fun CustomSearchEngineAddDialogMiuix(
    show: Boolean,
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var keywordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(show) {
        if (show) {
            nameInput = ""
            urlInput = ""
            keywordInput = ""
        }
    }

    WindowDialog(
        show = show,
        title = "添加自定义引擎",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(value = nameInput, onValueChange = { nameInput = it }, label = "引擎名称", singleLine = true, modifier = Modifier.fillMaxWidth())
            TextField(value = urlInput, onValueChange = { urlInput = it }, label = "搜索网址", singleLine = true, modifier = Modifier.fillMaxWidth())
            TextField(value = keywordInput, onValueChange = { keywordInput = it }, label = "搜索内容", singleLine = true, modifier = Modifier.fillMaxWidth())

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("使用教程", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text("1.先在准备添加的搜索引擎中输入任意关键词进行搜索（建议使用例如“test”这种简单的英文）；", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    Text("2.分别复制跳转的链接与搜索关键词，粘贴到【搜索网址】与【搜索内容】中；", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    Text("添加完成后，可以选中并点击底部搜索按钮测试搜索引擎是否可以正常使用。", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val url = urlInput.trim()
                        val kw = keywordInput.trim()
                        if (url.isNotBlank() && kw.isNotBlank()) {
                            val finalUrl = buildSearchUrlTemplate(url, kw)
                            if (finalUrl != null) onAdd(nameInput.trim(), finalUrl)
                            else Toast.makeText(context, "未能识别搜索词，请检查输入是否正确", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank() && keywordInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White),
                ) { Text("添加") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CustomSearchEngineAddDialogMaterial(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var keywordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("添加自定义引擎") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { M3Text("引擎名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { M3Text("搜索网址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = keywordInput, onValueChange = { keywordInput = it }, label = { M3Text("搜索内容") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                M3Surface(shape = RoundedCornerShape(12.dp), color = M3MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        M3Text("使用教程", style = M3MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        M3Text("1.先在准备添加的搜索引擎中输入任意关键词进行搜索（建议使用例如“test”这种简单的英文）；", style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                        M3Text("2.分别复制跳转的链接与搜索关键词，粘贴到【搜索网址】与【搜索内容】中；", style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                        M3Text("添加完成后，可以选中并点击底部搜索按钮测试搜索引擎是否可以正常使用。", style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val url = urlInput.trim()
                    val kw = keywordInput.trim()
                    if (url.isNotBlank() && kw.isNotBlank()) {
                        val finalUrl = buildSearchUrlTemplate(url, kw)
                        if (finalUrl != null) onAdd(nameInput.trim(), finalUrl)
                        else Toast.makeText(context, "未能识别搜索词，请检查输入是否正确", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = urlInput.isNotBlank() && keywordInput.isNotBlank(),
            ) { M3Text("添加") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { M3Text("取消") }
        },
    )
}

// ============================================================
// 编辑自定义引擎对话框
// ============================================================

@Composable
private fun CustomSearchEngineEditDialogMiuix(
    show: Boolean,
    engine: CustomSearchEngine?,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var keywordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(show, engine) {
        if (show) {
            nameInput = engine?.name ?: ""
            urlInput = engine?.url ?: ""
            keywordInput = ""
        }
    }

    WindowDialog(
        show = show,
        title = "编辑自定义引擎",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(value = nameInput, onValueChange = { nameInput = it }, label = "引擎名称", singleLine = true, modifier = Modifier.fillMaxWidth())
            TextField(value = urlInput, onValueChange = { urlInput = it }, label = "搜索网址", singleLine = true, modifier = Modifier.fillMaxWidth())
            TextField(value = keywordInput, onValueChange = { keywordInput = it }, label = "搜索内容", singleLine = true, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val url = urlInput.trim()
                        val kw = keywordInput.trim()
                        if (url.isNotBlank()) {
                            var finalUrl = url
                            if (kw.isNotBlank()) {
                                val built = buildSearchUrlTemplate(url, kw)
                                if (built != null) finalUrl = built
                            }
                            onSave(nameInput.trim(), finalUrl)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White),
                ) { Text("保存") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CustomSearchEngineEditDialogMaterial(
    engine: CustomSearchEngine?,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf(engine?.name ?: "") }
    var urlInput by remember { mutableStateOf(engine?.url ?: "") }
    var keywordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("编辑自定义引擎") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { M3Text("引擎名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { M3Text("搜索网址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = keywordInput, onValueChange = { keywordInput = it }, label = { M3Text("搜索内容") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val url = urlInput.trim()
                    val kw = keywordInput.trim()
                    if (url.isNotBlank()) {
                        var finalUrl = url
                        if (kw.isNotBlank()) {
                            val built = buildSearchUrlTemplate(url, kw)
                            if (built != null) finalUrl = built
                        }
                        onSave(nameInput.trim(), finalUrl)
                    }
                },
                enabled = urlInput.isNotBlank(),
            ) { M3Text("保存") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { M3Text("取消") }
        },
    )
}

// ============================================================
// 测试自定义引擎对话框
// ============================================================

@Composable
private fun CustomSearchEngineTestDialogMiuix(
    show: Boolean,
    engineName: String,
    engineUrl: String?,
    presetIndex: Int,
    onDismiss: () -> Unit,
) {
    var keywordInput by remember { mutableStateOf("") }
    var testUrl by remember { mutableStateOf("") }

    LaunchedEffect(show) {
        if (show) {
            keywordInput = ""
            testUrl = ""
        }
    }

    WindowDialog(
        show = show,
        title = "搜索测试",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = keywordInput,
                onValueChange = { keywordInput = it },
                label = "输入关键词",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (testUrl.isNotBlank()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            isClickable = false
                            isFocusable = false
                            setOnTouchListener { _, _ -> true }
                        }
                    },
                    update = { wv ->
                        if (wv.url != testUrl) wv.loadUrl(testUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        if (keywordInput.isNotBlank()) {
                            val baseUrl = engineUrl ?: when (presetIndex) {
                                0 -> "https://cn.bing.com/search?q=%s"
                                1 -> "https://www.douyin.com/search/%s"
                                2 -> "https://m.baidu.com/s?word=%s"
                                3 -> "https://m.sm.cn/s?q=%s"
                                4 -> "https://m.sogou.com/web/searchList.jsp?keyword=%s"
                                5 -> "https://www.google.com/search?q=%s"
                                else -> "https://cn.bing.com/search?q=%s"
                            }
                            testUrl = baseUrl.replace("%s", java.net.URLEncoder.encode(keywordInput.trim(), "UTF-8"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = keywordInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("搜索") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CustomSearchEngineTestDialogMaterial(
    show: Boolean,
    engineName: String,
    engineUrl: String?,
    presetIndex: Int,
    onDismiss: () -> Unit,
) {
    if (!show) return
    var keywordInput by remember { mutableStateOf("") }
    var testUrl by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("搜索测试") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { M3Text("输入关键词") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (testUrl.isNotBlank()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                isClickable = false
                                isFocusable = false
                                setOnTouchListener { _, _ -> true }
                            }
                        },
                        update = { wv ->
                            if (wv.url != testUrl) wv.loadUrl(testUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (keywordInput.isNotBlank()) {
                        val baseUrl = engineUrl ?: when (presetIndex) {
                            0 -> "https://cn.bing.com/search?q=%s"
                            1 -> "https://www.douyin.com/search/%s"
                            2 -> "https://m.baidu.com/s?word=%s"
                            3 -> "https://m.sm.cn/s?q=%s"
                            4 -> "https://m.sogou.com/web/searchList.jsp?keyword=%s"
                            5 -> "https://www.google.com/search?q=%s"
                            else -> "https://cn.bing.com/search?q=%s"
                        }
                        testUrl = baseUrl.replace("%s", java.net.URLEncoder.encode(keywordInput.trim(), "UTF-8"))
                    }
                },
                enabled = keywordInput.isNotBlank(),
            ) { M3Text("搜索") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                M3Text("取消")
            }
        },
    )
}
