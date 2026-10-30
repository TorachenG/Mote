@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.UserHandle
import android.os.storage.StorageManager
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
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
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import java.io.File

import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api as M3ExperimentalApi
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ============================================================
// 数据模型
// ============================================================

internal data class SpaceCategoryItem(
    val key: String,
    val title: String,
    val summary: String? = null,
    val isDanger: Boolean = false,
    val sizeBytes: Long = 0L,
)

// ============================================================
// 存储统计工具
// ============================================================

internal fun dirSize(file: File?): Long {
    if (file == null || !file.exists()) return 0L
    if (file.isFile) return file.length()
    var total = 0L
    val children = file.listFiles() ?: return 0L
    for (child in children) total += dirSize(child)
    return total
}

internal fun webViewCacheSize(context: Context): Long {
    val candidates = listOf(
        File(context.filesDir.parentFile, "app_webview/Default/Cache"),
        File(context.filesDir.parentFile, "app_webview/Default/Code Cache"),
        File(context.cacheDir, "WebView"),
        File(context.cacheDir, "org.chromium.android_webview"),
    )
    return candidates.sumOf { dirSize(it) }
}

internal fun imageCacheSize(context: Context): Long {
    return dirSize(File(context.cacheDir, "images"))
}

internal fun videoCacheSize(context: Context): Long {
    return dirSize(File(context.cacheDir, "mote_media_cache"))
}

internal fun otherCacheSize(context: Context): Long {
    val known = setOf(
        "images",
        "mote_media_cache",
        "WebView",
        "org.chromium.android_webview",
    )
    val children = context.cacheDir.listFiles() ?: return 0L
    var total = 0L
    for (child in children) {
        if (child.name !in known) total += dirSize(child)
    }
    return total
}

private fun searchHistorySize(prefs: SharedPreferences): Long {
    val raw = prefs.getString("search_history", "") ?: ""
    return raw.toByteArray(Charsets.UTF_8).size.toLong()
}

private fun browsingHistorySize(prefs: SharedPreferences): Long {
    val raw = prefs.getString("history", "") ?: ""
    return raw.toByteArray(Charsets.UTF_8).size.toLong()
}

private fun cookieSize(context: Context): Long {
    return try {
        val cookie = CookieManager.getInstance().getCookie("https://mote.local") ?: ""
        cookie.toByteArray(Charsets.UTF_8).size.toLong()
    } catch (_: Exception) {
        0L
    }
}

private fun passwordSize(prefs: SharedPreferences): Long {
    val raw = prefs.getString(KEY_SAVED_PASSWORDS, "") ?: ""
    return raw.toByteArray(Charsets.UTF_8).size.toLong()
}

private fun downloadedImagesSize(): Long {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Mote",
    )
    val children = dir.listFiles() ?: return 0L
    val imageExts = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif")
    var total = 0L
    for (f in children) {
        if (f.isFile && f.extension.lowercase() in imageExts) total += f.length()
    }
    return total
}

private fun downloadedVideosSize(): Long {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Mote",
    )
    val children = dir.listFiles() ?: return 0L
    val videoExts = setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v")
    var total = 0L
    for (f in children) {
        if (f.isFile && f.extension.lowercase() in videoExts) total += f.length()
    }
    return total
}

private fun downloadedOtherSize(): Long {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Mote",
    )
    val children = dir.listFiles() ?: return 0L
    val imageExts = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif")
    val videoExts = setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v")
    var total = 0L
    for (f in children) {
        if (!f.isFile) continue
        val ext = f.extension.lowercase()
        if (ext !in imageExts && ext !in videoExts) total += f.length()
    }
    return total
}

internal fun appUsedBytes(context: Context): Long {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return try {
            val manager = context.getSystemService(Context.STORAGE_STATS_SERVICE)
                    as? StorageStatsManager ?: return dirSize(context.filesDir) + dirSize(context.cacheDir)
            val stats = manager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                context.packageName,
                UserHandle.getUserHandleForUid(android.os.Process.myUid()),
            )
            stats.appBytes + stats.dataBytes + stats.cacheBytes
        } catch (_: Exception) {
            dirSize(context.filesDir) + dirSize(context.cacheDir)
        }
    }
    return dirSize(context.filesDir) + dirSize(context.cacheDir)
}

internal fun totalStorageBytes(): Long {
    return try {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        stat.totalBytes
    } catch (_: Exception) {
        0L
    }
}

internal fun totalUsedBytes(): Long {
    return try {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        stat.totalBytes - stat.availableBytes
    } catch (_: Exception) {
        0L
    }
}

internal fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(java.util.Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
        else -> String.format(java.util.Locale.US, "%.1f KB", kb)
    }
}

internal fun formatSizeMb(bytes: Long): String {
    val mb = bytes / 1024.0 / 1024.0
    return if (mb < 1.0) {
        val kb = bytes / 1024.0
        String.format(java.util.Locale.US, "%.1f KB", kb)
    } else {
        String.format(java.util.Locale.US, "%.1f MB", mb)
    }
}

// ============================================================
// 自动清理与清理执行
// ============================================================

internal object AutoCacheCleaner {
    const val KEY_AUTO_CLEAR_CACHE = "auto_clear_cache_limit"
    const val DEFAULT_LIMIT_INDEX = 2 // 320MB

    suspend fun checkAndClean(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_AUTO_CLEAR_CACHE, DEFAULT_LIMIT_INDEX)
        if (index == 0) return@withContext // 关闭状态

        val limitMb = when (index) {
            1 -> 160L
            2 -> 320L
            3 -> 640L
            4 -> 1280L
            else -> 320L
        }
        val limitBytes = limitMb * 1024 * 1024

        val totalCache = webViewCacheSize(context) +
                imageCacheSize(context) +
                videoCacheSize(context) +
                otherCacheSize(context)

        // 只有在总缓存大于设定的阈值时，才执行清理
        if (totalCache > limitBytes) {
            // 计算出需要释放多少空间才降回阈值以下
            val diffBytes = totalCache - limitBytes
            var deletedBytes = 0L

            // 1. 收集所有可精确控制的自建缓存文件（不包括 WebView）
            val files = mutableListOf<File>()

            fun collectFiles(dir: File?) {
                if (dir == null || !dir.exists()) return
                val children = dir.listFiles() ?: return
                for (child in children) {
                    if (child.isFile) {
                        files.add(child)
                    } else if (child.isDirectory) {
                        collectFiles(child)
                    }
                }
            }

            // 收集视频缓存
            collectFiles(File(context.cacheDir, "mote_media_cache"))
            // 收集图片缓存
            collectFiles(File(context.cacheDir, "images"))

            // 收集其它缓存（排除已知的和 WebView 相关的）
            val known = setOf("images", "mote_media_cache", "WebView", "org.chromium.android_webview")
            context.cacheDir.listFiles()?.forEach {
                if (it.name !in known) {
                    if (it.isFile) files.add(it) else collectFiles(it)
                }
            }

            // 2. 业界常识：按最后修改时间升序排序（最旧的文件排在最前面）
            files.sortBy { it.lastModified() }

            // 3. 从最旧的文件开始删，直到腾出足够的空间
            for (f in files) {
                if (deletedBytes >= diffBytes) break
                val length = f.length()
                if (f.delete()) {
                    deletedBytes += length
                }
            }

            // 4. 如果清理完旧文件，空间依然没降到阈值以下（说明 WebView 缓存占大头）
            // 则清空 WebView 缓存来兜底。
            if (deletedBytes < diffBytes) {
                withContext(Dispatchers.Main) {
                    clearWebViewCache(context)
                }
            }
        }
    }

    internal fun clearWebViewCache(context: Context) {
        try {
            val wv = WebView(context)
            wv.clearCache(true)
            wv.destroy()
        } catch (_: Exception) {}
        try {
            WebStorage.getInstance().deleteAllData()
        } catch (_: Exception) {}
    }

    internal fun clearImageCache(context: Context) {
        File(context.cacheDir, "images").deleteRecursively()
    }

    internal fun clearVideoCache(context: Context) {
        File(context.cacheDir, "mote_media_cache").deleteRecursively()
    }

    internal fun clearOtherCache(context: Context) {
        val known = setOf(
            "images",
            "mote_media_cache",
            "WebView",
            "org.chromium.android_webview",
        )
        val children = context.cacheDir.listFiles() ?: return
        for (child in children) {
            if (child.name !in known) child.deleteRecursively()
        }
    }
}

private fun clearSearchHistory(prefs: SharedPreferences) {
    prefs.edit().putString("search_history", "").apply()
}

private fun clearBrowsingHistory(prefs: SharedPreferences) {
    prefs.edit().putString("history", "").apply()
}

private fun clearCookies() {
    try {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    } catch (_: Exception) {}
}

private fun clearPasswords(prefs: SharedPreferences) {
    prefs.edit().remove(KEY_SAVED_PASSWORDS).apply()
}

private fun deleteDownloadedFiles(predicate: (File) -> Boolean) {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Mote",
    )
    val children = dir.listFiles() ?: return
    for (f in children) {
        if (f.isFile && predicate(f)) f.delete()
    }
}

// ============================================================
// 页面入口
// ============================================================

@Composable
fun SettingsSpaceManagementPage(
    onBack: () -> Unit,
    onOpenCacheSettings: () -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsSpaceManagementMiuix(onBack, onOpenCacheSettings)
        UiMode.Material -> SettingsSpaceManagementMaterial(onBack, onOpenCacheSettings)
    }
}

@Composable
fun CacheSettingsPage(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> CacheSettingsMiuix(onBack)
        UiMode.Material -> CacheSettingsMaterial(onBack)
    }
}

// ============================================================
// 空间管理页 - MIUIX
// ============================================================

@Composable
private fun SettingsSpaceManagementMiuix(
    onBack: () -> Unit,
    onOpenCacheSettings: () -> Unit
) {
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

    var showMoreMenu by remember { mutableStateOf(false) }

    // ---------------- 实时数据 ----------------
    var cacheSizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var privacySizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var localSizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var usedStorage by remember { mutableStateOf(0L) }
    var totalStorage by remember { mutableStateOf(0L) }

    val cacheChecked = remember { mutableStateOf(setOf("web", "image", "video", "other")) }
    val privacyChecked = remember { mutableStateOf(emptySet<String>()) }
    val localChecked = remember { mutableStateOf(emptySet<String>()) }

    var cacheExpanded by remember { mutableStateOf(true) }
    var privacyExpanded by remember { mutableStateOf(false) }
    var localExpanded by remember { mutableStateOf(false) }

    var isClearing by remember { mutableStateOf(false) }
    var displayTotalBytes by remember { mutableStateOf(0L) }

    suspend fun refreshSizes() {
        val result = withContext(Dispatchers.IO) {
            val cache = mapOf(
                "web" to webViewCacheSize(context),
                "image" to imageCacheSize(context),
                "video" to videoCacheSize(context),
                "other" to otherCacheSize(context),
            )
            val privacy = mapOf(
                "search" to searchHistorySize(prefs),
                "browse" to browsingHistorySize(prefs),
                "cookie" to cookieSize(context),
                "password" to passwordSize(prefs),
            )
            val local = mapOf(
                "image" to downloadedImagesSize(),
                "video" to downloadedVideosSize(),
                "other" to downloadedOtherSize(),
            )
            Triple(cache, privacy, local) to (totalUsedBytes() to totalStorageBytes())
        }
        cacheSizes = result.first.first
        privacySizes = result.first.second
        localSizes = result.first.third
        usedStorage = result.second.first
        totalStorage = result.second.second
        displayTotalBytes = cacheSizes.values.sum() + privacySizes.values.sum() + localSizes.values.sum()
    }

    LaunchedEffect(Unit) { refreshSizes() }

    fun selectedTotalBytes(): Long {
        var total = 0L
        cacheSizes.forEach { (k, v) -> if (k in cacheChecked.value) total += v }
        privacySizes.forEach { (k, v) -> if (k in privacyChecked.value) total += v }
        localSizes.forEach { (k, v) -> if (k in localChecked.value) total += v }
        return total
    }

    fun groupState(keys: Set<String>, checked: Set<String>): ToggleableState {
        if (keys.isEmpty()) return ToggleableState.Off
        val count = keys.count { it in checked }
        return when {
            count == 0 -> ToggleableState.Off
            count == keys.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }

    fun toggleGroup(keys: Set<String>, checked: MutableState<Set<String>>) {
        checked.value = if (checked.value.containsAll(keys)) checked.value - keys else checked.value + keys
    }

    fun toggleItem(key: String, checked: MutableState<Set<String>>) {
        checked.value = if (key in checked.value) checked.value - key else checked.value + key
    }

    fun performClear() {
        if (isClearing) return
        isClearing = true
        scope.launch {
            withContext(Dispatchers.IO) {
                if ("web" in cacheChecked.value) withContext(Dispatchers.Main) { AutoCacheCleaner.clearWebViewCache(context) }
                if ("image" in cacheChecked.value) AutoCacheCleaner.clearImageCache(context)
                if ("video" in cacheChecked.value) AutoCacheCleaner.clearVideoCache(context)
                if ("other" in cacheChecked.value) AutoCacheCleaner.clearOtherCache(context)
                if ("search" in privacyChecked.value) clearSearchHistory(prefs)
                if ("browse" in privacyChecked.value) clearBrowsingHistory(prefs)
                if ("cookie" in privacyChecked.value) clearCookies()
                if ("password" in privacyChecked.value) clearPasswords(prefs)
                if ("image" in localChecked.value) deleteDownloadedFiles { f -> f.extension.lowercase() in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif") }
                if ("video" in localChecked.value) deleteDownloadedFiles { f -> f.extension.lowercase() in setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v") }
                if ("other" in localChecked.value) deleteDownloadedFiles { f -> val ext = f.extension.lowercase(); val img = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif"); val vid = setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v"); ext !in img && ext !in vid }
            }
            isClearing = false
            cacheChecked.value = emptySet()
            privacyChecked.value = emptySet()
            localChecked.value = emptySet()
            refreshSizes()
        }
    }

    val isBtnEnabled = selectedTotalBytes() > 0 && !isClearing
    val btnBgColor by animateColorAsState(
        targetValue = if (isBtnEnabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainer,
        animationSpec = tween(300, easing = SmoothEase), label = "clearBtnBg"
    )
    val btnTextColor by animateColorAsState(
        targetValue = if (isBtnEnabled) Color.White else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        animationSpec = tween(300, easing = SmoothEase), label = "clearBtnText"
    )

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(modifier = Modifier.fillMaxSize().then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).scrollEndHaptic().overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 88.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "header") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 顶部横幅
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 16.dp)) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    AnimatedContent(
                                        targetState = formatSizeMb(displayTotalBytes),
                                        transitionSpec = {
                                            ((fadeIn(tween(180, easing = SmoothEase)) + scaleIn(animationSpec = tween(200, easing = SmoothEase), initialScale = 0.92f)).togetherWith(fadeOut(tween(120, easing = SmoothEase)) + scaleOut(animationSpec = tween(140, easing = SmoothEase), targetScale = 1.04f))) using
                                                androidx.compose.animation.SizeTransform(clip = false)
                                        },
                                        label = "spaceTotalAnim",
                                    ) { text ->
                                        Text(text = text, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "存储情况", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                    Spacer(Modifier.weight(1f))
                                    Text(text = if (totalStorage > 0) "${formatSize(usedStorage)} / ${formatSize(totalStorage)}" else formatSize(usedStorage), fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(progress = if (totalStorage > 0) (usedStorage.toFloat() / totalStorage).coerceIn(0f, 1f) else 0f, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                            }

                            Spacer(Modifier.height(8.dp))

                            SpaceCategoryCardMiuix("缓存数据", cacheExpanded, { cacheExpanded = !cacheExpanded }, groupState(setOf("web", "image", "video", "other"), cacheChecked.value), { toggleGroup(setOf("web", "image", "video", "other"), cacheChecked) }, listOf(
                                SpaceCategoryItem("web", "网页缓存", sizeBytes = cacheSizes["web"] ?: 0L),
                                SpaceCategoryItem("image", "图片缓存", sizeBytes = cacheSizes["image"] ?: 0L),
                                SpaceCategoryItem("video", "视频缓存", sizeBytes = cacheSizes["video"] ?: 0L),
                                SpaceCategoryItem("other", "其它缓存", sizeBytes = cacheSizes["other"] ?: 0L)
                            ), cacheChecked.value, { toggleItem(it, cacheChecked) })

                            SpaceCategoryCardMiuix("隐私数据", privacyExpanded, { privacyExpanded = !privacyExpanded }, groupState(setOf("search", "browse", "cookie", "password"), privacyChecked.value), { toggleGroup(setOf("search", "browse", "cookie", "password"), privacyChecked) }, listOf(
                                SpaceCategoryItem("search", "搜索历史", sizeBytes = privacySizes["search"] ?: 0L),
                                SpaceCategoryItem("browse", "浏览历史", sizeBytes = privacySizes["browse"] ?: 0L),
                                SpaceCategoryItem("cookie", "Cookies", sizeBytes = privacySizes["cookie"] ?: 0L),
                                SpaceCategoryItem("password", "用户名和密码", summary = "谨慎清理", isDanger = true, sizeBytes = privacySizes["password"] ?: 0L)
                            ), privacyChecked.value, { toggleItem(it, privacyChecked) })

                            SpaceCategoryCardMiuix("本地文件", localExpanded, { localExpanded = !localExpanded }, groupState(setOf("image", "video", "other"), localChecked.value), { toggleGroup(setOf("image", "video", "other"), localChecked) }, listOf(
                                SpaceCategoryItem("image", "下载的图片", sizeBytes = localSizes["image"] ?: 0L),
                                SpaceCategoryItem("video", "下载的视频", sizeBytes = localSizes["video"] ?: 0L),
                                SpaceCategoryItem("other", "下载的其它文件", sizeBytes = localSizes["other"] ?: 0L)
                            ), localChecked.value, { toggleItem(it, localChecked) })
                        }
                    }
                }
                SettingsPageAutoScroll(lazyListState = listState, pageRoute = "settings_space_management", contentTopPadding = contentTopPadding)
            }

            // 顶部栏与菜单
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).then(if (blurSupported && backdrop != null && materialStyle != 0) { if (progressiveTitleBlur) Modifier.progressiveTextureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = 10f, gradient = ProgressiveBlur.Top.copy(
     startFraction = 0.75f,
     curve = 1f,
 ), colors = BlurDefaults.blurColors(
            blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
        )).background(
                                                                                                                                                                                Brush.verticalGradient(
                                                                                                                                                                                    listOf(surfaceColor, Color.Transparent),
                                                                                                                                                                                ),
                                                                                                                                                                            ) else Modifier.textureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = TOP_BAR_BLUR_RADIUS, noiseCoefficient = BlurDefaults.NoiseCoefficient, colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries)) } else Modifier.background(surfaceColor)).statusBarsPadding().onGloballyPositioned { coords -> val bottomPx = coords.boundsInWindow().bottom.toInt(); if (bottomPx != topBarBottomPx) { topBarBottomPx = bottomPx } }) {
                TopAppBar(
                    title = "空间管理", largeTitle = "空间管理", scrollBehavior = scrollBehavior, color = Color.Transparent, defaultWindowInsetsPadding = false,
                    navigationIcon = { SettingsBackButton(onBack = onBack, progressiveTitleBlur = progressiveTitleBlur, backCardAlpha = backCardAlpha, backCardMaterialModifier = backCardMaterialModifier, iconTint = MiuixTheme.colorScheme.onSurface) },
                    actions = {
                        Box {
                            SettingsTopBarAction(
                                backCardAlpha = backCardAlpha,
                                backCardMaterialModifier = backCardMaterialModifier,
                                onClick = { showMoreMenu = true },
                                contentDescription = "更多",
                                icon = MiuixIcons.More,
                            )
                            WindowCascadingListPopup(
                                show = showMoreMenu,
                                entries = listOf(DropdownEntry(items = listOf(DropdownItem(text = "清理设置", onClick = { showMoreMenu = false; onOpenCacheSettings() })))),
                                onDismissRequest = { showMoreMenu = false }
                            )
                        }
                    }
                )
            }

            // 底部清理按钮
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp).zIndex(10f)) {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(btnBgColor).clickable(enabled = isBtnEnabled) { performClear() }, contentAlignment = Alignment.Center) {
                    Text(text = "清理选中项目", color = btnTextColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SpaceCategoryCardMiuix(
    title: String, expanded: Boolean, onToggleExpand: () -> Unit, groupState: ToggleableState, onToggleGroup: () -> Unit,
    items: List<SpaceCategoryItem>, checkedKeys: Set<String>, onToggleItem: (String) -> Unit,
) {
    val totalBytes = items.sumOf { it.sizeBytes }
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
        BasicComponent(
            title = title, onClick = onToggleExpand,
            endActions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "共${formatSize(totalBytes)}",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.width(8.dp))
                    Checkbox(
                        state = groupState,
                        onClick = onToggleGroup,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        )
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onToggleItem(item.key) }).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
                            if (item.summary != null) {
                                Spacer(Modifier.height(2.dp))
                                Text(text = item.summary, fontSize = 12.sp, color = if (item.isDanger) Color(0xFFFF3B30) else MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                        }
                        Text(text = formatSize(item.sizeBytes), fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.width(12.dp))
                        Checkbox(state = if (item.key in checkedKeys) ToggleableState.On else ToggleableState.Off, onClick = { onToggleItem(item.key) })
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ============================================================
// 空间管理页 - Material 3
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun SettingsSpaceManagementMaterial(
    onBack: () -> Unit,
    onOpenCacheSettings: () -> Unit
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val pageBg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    var cacheSizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var privacySizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var localSizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var usedStorage by remember { mutableStateOf(0L) }
    var totalStorage by remember { mutableStateOf(0L) }

    val cacheChecked = remember { mutableStateOf(setOf("web", "image", "video", "other")) }
    val privacyChecked = remember { mutableStateOf(emptySet<String>()) }
    val localChecked = remember { mutableStateOf(emptySet<String>()) }

    var cacheExpanded by remember { mutableStateOf(true) }
    var privacyExpanded by remember { mutableStateOf(false) }
    var localExpanded by remember { mutableStateOf(false) }

    var isClearing by remember { mutableStateOf(false) }
    var displayTotalBytes by remember { mutableStateOf(0L) }
    var showMoreMenu by remember { mutableStateOf(false) }

    suspend fun refreshSizes() {
        val result = withContext(Dispatchers.IO) {
            val cache = mapOf("web" to webViewCacheSize(context), "image" to imageCacheSize(context), "video" to videoCacheSize(context), "other" to otherCacheSize(context))
            val privacy = mapOf("search" to searchHistorySize(prefs), "browse" to browsingHistorySize(prefs), "cookie" to cookieSize(context), "password" to passwordSize(prefs))
            val local = mapOf("image" to downloadedImagesSize(), "video" to downloadedVideosSize(), "other" to downloadedOtherSize())
            Triple(cache, privacy, local) to (totalUsedBytes() to totalStorageBytes())
        }
        cacheSizes = result.first.first; privacySizes = result.first.second; localSizes = result.first.third
        usedStorage = result.second.first; totalStorage = result.second.second
        displayTotalBytes = cacheSizes.values.sum() + privacySizes.values.sum() + localSizes.values.sum()
    }

    LaunchedEffect(Unit) { refreshSizes() }

    fun selectedTotalBytes(): Long {
        var total = 0L
        cacheSizes.forEach { (k, v) -> if (k in cacheChecked.value) total += v }
        privacySizes.forEach { (k, v) -> if (k in privacyChecked.value) total += v }
        localSizes.forEach { (k, v) -> if (k in localChecked.value) total += v }
        return total
    }

    fun groupState(keys: Set<String>, checked: Set<String>): ToggleableState {
        if (keys.isEmpty()) return ToggleableState.Off
        val count = keys.count { it in checked }
        return when { count == 0 -> ToggleableState.Off; count == keys.size -> ToggleableState.On; else -> ToggleableState.Indeterminate }
    }

    fun toggleGroup(keys: Set<String>, checked: MutableState<Set<String>>) { checked.value = if (checked.value.containsAll(keys)) checked.value - keys else checked.value + keys }
    fun toggleItem(key: String, checked: MutableState<Set<String>>) { checked.value = if (key in checked.value) checked.value - key else checked.value + key }

    fun performClear() {
        if (isClearing) return
        isClearing = true
        scope.launch {
            withContext(Dispatchers.IO) {
                if ("web" in cacheChecked.value) withContext(Dispatchers.Main) { AutoCacheCleaner.clearWebViewCache(context) }
                if ("image" in cacheChecked.value) AutoCacheCleaner.clearImageCache(context)
                if ("video" in cacheChecked.value) AutoCacheCleaner.clearVideoCache(context)
                if ("other" in cacheChecked.value) AutoCacheCleaner.clearOtherCache(context)
                if ("search" in privacyChecked.value) clearSearchHistory(prefs)
                if ("browse" in privacyChecked.value) clearBrowsingHistory(prefs)
                if ("cookie" in privacyChecked.value) clearCookies()
                if ("password" in privacyChecked.value) clearPasswords(prefs)
                if ("image" in localChecked.value) deleteDownloadedFiles { f -> f.extension.lowercase() in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif") }
                if ("video" in localChecked.value) deleteDownloadedFiles { f -> f.extension.lowercase() in setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v") }
                if ("other" in localChecked.value) deleteDownloadedFiles { f -> val ext = f.extension.lowercase(); val img = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif"); val vid = setOf("mp4", "mkv", "webm", "mov", "avi", "flv", "wmv", "3gp", "ts", "m4v"); ext !in img && ext !in vid }
            }
            isClearing = false
            cacheChecked.value = emptySet(); privacyChecked.value = emptySet(); localChecked.value = emptySet()
            refreshSizes()
        }
    }

    val isBtnEnabled = selectedTotalBytes() > 0 && !isClearing
    val btnBgColor by animateColorAsState(if (isBtnEnabled) M3MaterialTheme.colorScheme.primary else M3MaterialTheme.colorScheme.surfaceContainerHighest, tween(300, easing = SmoothEase), label = "clearBtnBg")
    val btnTextColor by animateColorAsState(if (isBtnEnabled) M3MaterialTheme.colorScheme.onPrimary else M3MaterialTheme.colorScheme.onSurfaceVariant, tween(300, easing = SmoothEase), label = "clearBtnText")

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("空间管理") },
                    navigationIcon = { M3IconButton(onClick = onBack) { M3Icon(imageVector = MiuixIcons.Back, contentDescription = "返回") } },
                    actions = {
                        Box {
                            M3IconButton(onClick = { showMoreMenu = true }) { M3Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "更多") }
                            M3DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = M3MaterialTheme.colorScheme.surfaceContainerHigh) {
                                M3DropdownMenuItem(text = { M3Text("清理设置") }, onClick = { showMoreMenu = false; onOpenCacheSettings() })
                            }
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 88.dp)) {
                    item(key = "header") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 16.dp)) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    AnimatedContent(
                                        targetState = formatSizeMb(displayTotalBytes),
                                        transitionSpec = {
                                            ((fadeIn(tween(180, easing = SmoothEase)) + scaleIn(animationSpec = tween(200, easing = SmoothEase), initialScale = 0.92f)).togetherWith(fadeOut(tween(120, easing = SmoothEase)) + scaleOut(animationSpec = tween(140, easing = SmoothEase), targetScale = 1.04f))) using
                                                androidx.compose.animation.SizeTransform(clip = false)
                                        },
                                        label = "spaceTotalAnim",
                                    ) { text -> M3Text(text = text, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = M3MaterialTheme.colorScheme.onSurface) }
                                }
                                Spacer(Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    M3Text(text = "存储情况", fontSize = 13.sp, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.weight(1f))
                                    M3Text(text = if (totalStorage > 0) "${formatSize(usedStorage)} / ${formatSize(totalStorage)}" else formatSize(usedStorage), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(8.dp))
                                androidx.compose.material3.LinearProgressIndicator(progress = { if (totalStorage > 0) (usedStorage.toFloat() / totalStorage).coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                            }

                            SpaceCategoryCardMaterial("缓存数据", cacheExpanded, { cacheExpanded = !cacheExpanded }, groupState(setOf("web", "image", "video", "other"), cacheChecked.value), { toggleGroup(setOf("web", "image", "video", "other"), cacheChecked) }, listOf(
                                SpaceCategoryItem("web", "网页缓存", sizeBytes = cacheSizes["web"] ?: 0L),
                                SpaceCategoryItem("image", "图片缓存", sizeBytes = cacheSizes["image"] ?: 0L),
                                SpaceCategoryItem("video", "视频缓存", sizeBytes = cacheSizes["video"] ?: 0L),
                                SpaceCategoryItem("other", "其它缓存", sizeBytes = cacheSizes["other"] ?: 0L)
                            ), cacheChecked.value, { toggleItem(it, cacheChecked) })

                            SpaceCategoryCardMaterial("隐私数据", privacyExpanded, { privacyExpanded = !privacyExpanded }, groupState(setOf("search", "browse", "cookie", "password"), privacyChecked.value), { toggleGroup(setOf("search", "browse", "cookie", "password"), privacyChecked) }, listOf(
                                SpaceCategoryItem("search", "搜索历史", sizeBytes = privacySizes["search"] ?: 0L),
                                SpaceCategoryItem("browse", "浏览历史", sizeBytes = privacySizes["browse"] ?: 0L),
                                SpaceCategoryItem("cookie", "Cookies", sizeBytes = privacySizes["cookie"] ?: 0L),
                                SpaceCategoryItem("password", "用户名和密码", summary = "谨慎清理", isDanger = true, sizeBytes = privacySizes["password"] ?: 0L)
                            ), privacyChecked.value, { toggleItem(it, privacyChecked) })

                            SpaceCategoryCardMaterial("本地文件", localExpanded, { localExpanded = !localExpanded }, groupState(setOf("image", "video", "other"), localChecked.value), { toggleGroup(setOf("image", "video", "other"), localChecked) }, listOf(
                                SpaceCategoryItem("image", "下载的图片", sizeBytes = localSizes["image"] ?: 0L),
                                SpaceCategoryItem("video", "下载的视频", sizeBytes = localSizes["video"] ?: 0L),
                                SpaceCategoryItem("other", "下载的其它文件", sizeBytes = localSizes["other"] ?: 0L)
                            ), localChecked.value, { toggleItem(it, localChecked) })
                        }
                    }
                }

                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp).zIndex(10f)) {
                    M3Surface(
                        onClick = { if (isBtnEnabled) performClear() },
                        enabled = isBtnEnabled,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = btnBgColor,
                        contentColor = btnTextColor
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            M3Text(text = "清理选中项目", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpaceCategoryCardMaterial(
    title: String, expanded: Boolean, onToggleExpand: () -> Unit, groupState: ToggleableState, onToggleGroup: () -> Unit,
    items: List<SpaceCategoryItem>, checkedKeys: Set<String>, onToggleItem: (String) -> Unit,
) {
    val totalBytes = items.sumOf { it.sizeBytes }
    val color = lerp(M3MaterialTheme.colorScheme.surfaceContainerLow, M3MaterialTheme.colorScheme.secondaryContainer, 0.10f)

    M3Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
        shape = RoundedCornerShape(25.dp),
        color = color,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3Text(title, style = M3MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(Modifier.weight(1f))
                M3Text(
                    "共${formatSize(totalBytes)}",
                    style = M3MaterialTheme.typography.bodyMedium,
                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.TriStateCheckbox(
                    state = groupState,
                    onClick = onToggleGroup,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            AnimatedVisibility(
                visible = expanded
            ) {
                Column {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = { onToggleItem(item.key) }).padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                M3Text(item.title, style = M3MaterialTheme.typography.bodyMedium)
                                if (item.summary != null) {
                                    Spacer(Modifier.height(2.dp))
                                    M3Text(item.summary, style = M3MaterialTheme.typography.bodySmall, color = if (item.isDanger) Color(0xFFFF3B30) else M3MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            M3Text(formatSize(item.sizeBytes), style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            androidx.compose.material3.Checkbox(checked = item.key in checkedKeys, onCheckedChange = { onToggleItem(item.key) })
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

// ============================================================
// 清理设置页 - MIUIX
// ============================================================

@Composable
private fun CacheSettingsMiuix(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }

    var autoClearIndex by remember { mutableIntStateOf(prefs.getInt(AutoCacheCleaner.KEY_AUTO_CLEAR_CACHE, AutoCacheCleaner.DEFAULT_LIMIT_INDEX)) }

    val options = listOf("关闭", "160MB", "320MB（默认）", "640MB", "1280MB")
    val entries = remember(autoClearIndex) {
        listOf(DropdownEntry(items = options.mapIndexed { index, s ->
            DropdownItem(text = s, selected = autoClearIndex == index, onClick = {
                autoClearIndex = index
                prefs.edit().putInt(AutoCacheCleaner.KEY_AUTO_CLEAR_CACHE, index).apply()
            })
        }))
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) { rememberLayerBackdrop { drawRect(surfaceColor); drawContent() } } else null

    var progressiveTitleBlur by remember { mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true)) }
    var materialStyle by remember { mutableIntStateOf(prefs.getInt("ui_material_style", 2)) }
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
    val isAtTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 && !listState.isScrollInProgress } }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f, animationSpec = tween(400, easing = SmoothEase), label = "settingsBackCardAlpha")
    val backCardMaterialModifier = if (progressiveTitleBlur) buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark) else Modifier
    val titleBarBlendColorEntries = remember(surfaceColor) { listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA))) }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(modifier = Modifier.fillMaxSize().then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).scrollEndHaptic().overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SmallTitle("功能")
                            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                WindowDropdownPreference(
                                    title = "自动清理缓存",
                                    summary = "缓存到达上限时清理旧缓存",
                                    entries = entries,
                                    collapseOnSelection = true
                                )
                            }
                            Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()))
                        }
                    }
                }
                SettingsPageAutoScroll(lazyListState = listState, pageRoute = "settings_cache_settings", contentTopPadding = contentTopPadding)
            }

            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).then(if (blurSupported && backdrop != null && materialStyle != 0) { if (progressiveTitleBlur) Modifier.progressiveTextureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = 10f, gradient = ProgressiveBlur.Top.copy(
     startFraction = 0.75f,
     curve = 1f,
 ), colors = BlurDefaults.blurColors(
            blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
        )).background(
                                                                                                                                                                                Brush.verticalGradient(
                                                                                                                                                                                    listOf(surfaceColor, Color.Transparent),
                                                                                                                                                                                ),
                                                                                                                                                                            ) else Modifier.textureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = TOP_BAR_BLUR_RADIUS, noiseCoefficient = BlurDefaults.NoiseCoefficient, colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries)) } else Modifier.background(surfaceColor)).statusBarsPadding().onGloballyPositioned { coords -> val bottomPx = coords.boundsInWindow().bottom.toInt(); if (bottomPx != topBarBottomPx) { topBarBottomPx = bottomPx } }) {
                TopAppBar(
                    title = "清理设置", largeTitle = "清理设置", scrollBehavior = scrollBehavior, color = Color.Transparent, defaultWindowInsetsPadding = false,
                    navigationIcon = { SettingsBackButton(onBack = onBack, progressiveTitleBlur = progressiveTitleBlur, backCardAlpha = backCardAlpha, backCardMaterialModifier = backCardMaterialModifier, iconTint = MiuixTheme.colorScheme.onSurface) }
                )
            }
        }
    }
}

// ============================================================
// 清理设置页 - Material 3
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun CacheSettingsMaterial(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val pageBg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    var autoClearIndex by remember { mutableIntStateOf(prefs.getInt(AutoCacheCleaner.KEY_AUTO_CLEAR_CACHE, AutoCacheCleaner.DEFAULT_LIMIT_INDEX)) }
    val options = listOf("关闭", "160MB", "320MB（默认）", "640MB", "1280MB")

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("清理设置") },
                    navigationIcon = { M3IconButton(onClick = onBack) { M3Icon(imageVector = MiuixIcons.Back, contentDescription = "返回") } },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(bottom = 24.dp)) {
                item(key = "header") {
                    M3Text("功能", color = M3MaterialTheme.colorScheme.primary, style = M3MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))
                }
                item(key = "content") {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING).padding(bottom = M3_SEGMENT_GROUP_GAP)) {
                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentDropdownItem(
                                        title = "自动清理缓存",
                                        summary = "缓存到达上限时清理旧缓存",
                                        options = options,
                                        selectedIndex = autoClearIndex,
                                        onSelected = {
                                            autoClearIndex = it
                                            prefs.edit().putInt(AutoCacheCleaner.KEY_AUTO_CLEAR_CACHE, it).apply()
                                        }
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