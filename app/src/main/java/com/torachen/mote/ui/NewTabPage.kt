@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui
import com.torachen.mote.service.weatherCodeToText

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.torachen.mote.R
import com.torachen.mote.service.WeatherDaily
import com.torachen.mote.service.WeatherStateHolder
import com.torachen.mote.service.fetchWeather
import com.torachen.mote.service.hasFineLocationPermission
import com.torachen.mote.service.setWeatherPrecise
import com.torachen.mote.service.initWeatherCacheFromDisk
import com.torachen.mote.service.initWeatherPreciseFromPrefs
import com.torachen.mote.service.loadBitmapForCrop
import com.torachen.mote.service.windDirectionToText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import androidx.compose.foundation.border

// ============================================================
// Material 3（新标签页 Material 版用）
// ============================================================
import androidx.compose.material.icons.Icons as M3Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api as M3ExperimentalApi
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider
import androidx.compose.ui.graphics.lerp

// ============================================================
// 常量
// ============================================================

private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private const val KEY_HOME_BANNER_CONTENT = "home_banner_content"
private const val KEY_HOME_BACKGROUND_CONTENT = "home_background_content"
private const val KEY_HOME_SHOW_QUICK_URLS = "home_show_quick_urls"
private const val KEY_HOME_QUICK_URLS = "home_quick_urls"
private const val KEY_SCAN_MODULE_ENABLED = "scan_module_enabled"
private const val KEY_QUICK_INPUT_BAR = "quick_input_bar_enabled"

private const val KEY_HOME_BANNER_FONT_AUTO = "home_banner_font_auto"
private const val KEY_HOME_BANNER_FONT_WEIGHT = "home_banner_font_weight"

internal const val KEY_HOME_CUSTOM_WALLPAPER_PATH = "home_custom_wallpaper_path"
private const val BACKGROUND_CONTENT_BING_INDEX = 1
internal const val KEY_HOME_CUSTOM_WALLPAPER_COLOR = "home_custom_wallpaper_color"
internal const val CUSTOM_WALLPAPER_FILE_NAME = "home_wallpaper.jpg"

// ★ 自定义横幅（图片横幅）：文件与 prefs key
internal const val KEY_HOME_CUSTOM_BANNER_PATH = "home_custom_banner_path"
internal const val CUSTOM_BANNER_FILE_NAME = "home_banner_image.img"
private const val BANNER_IMAGE_INDEX = 1

// ★ 必应壁纸「前一天/后一天」所需的两个 prefs key
private const val KEY_BING_DAY_OFFSET = "home_bing_day_offset"
private const val KEY_BING_OFFSET_DATE = "home_bing_offset_date"

private val SEARCH_FULL_HEIGHT = 52.dp
private val SEARCH_FULL_WIDTH_PADDING = 24.dp
private val SEARCH_COLLAPSED_HEIGHT = SEARCH_FULL_HEIGHT * 5f / 6f
private const val SEARCH_COLLAPSED_WIDTH_FRACTION = 2f / 3f
private val SEARCH_ICON_SIZE = 32.dp

private val SEARCH_FOCUS_TOP_Y = 8.dp

private val SEARCH_MORPH_TOP_Y = 6.dp
private val SEARCH_MORPH_HEIGHT = 44.dp
private val SEARCH_MORPH_HPADDING = 12.dp

private const val SEARCH_MORPH_MS = 300
private const val HISTORY_DELAY_MS = 100L
private val HISTORY_GAP = 10.dp

private const val HOME_BLUR_RADIUS = 25f

private val SWIPE_WINDOW_TRIGGER = 72.dp

private val QUICK_URL_CELL_HEIGHT = 92.dp
private const val QUICK_URL_COLUMNS = 4
private const val QUICK_URL_MAX_ROWS = 2

private const val MAX_QUICK_URLS = 24
private const val QUICK_URL_FULL_HINT = "快捷网址位置已满，请删除其它网址再进行添加"

private val QUICK_URL_MORE_BUTTON_SIZE = 48.dp

private const val FOLDER_OPEN_MS = 380
private const val FOLDER_CLOSE_MS = 300

private val FOLDER_TARGET_WIDTH = 300.dp
private val FOLDER_FINAL_CORNER = 24.dp
private val FOLDER_ITEM_HEIGHT = 78.dp
private val FOLDER_INNER_PADDING_H = 16.dp
private val FOLDER_INNER_PADDING_V = 18.dp

private val FOLDER_BOTTOM_PADDING = 16.dp

private val FOLDER_EDIT_BAR_GAP = 0.dp

private val FOLDER_EDIT_BAR_BUTTON_HEIGHT = 48.dp

private const val QUICK_URL_ENTER_MS = 320
private const val QUICK_URL_EXIT_MS = 260
private const val QUICK_URL_REMOVE_DELAY_MS = 280L

private val FOLDER_OUTSIDE_BLUR_DP = 2.dp

private const val REORDER_MS = 200

private const val WALLPAPER_ZOOM_MAX = 1.2f

private val WALLPAPER_SEARCH_BLUR_DP = 5.dp
private const val SEARCH_FOCUS_BG_MS = 340

private val BANNER_CONTENT_OPTIONS = listOf("关闭", "图片", "时间", "天气", "问候语")

private val BACKGROUND_CONTENT_OPTIONS = listOf(
    "默认", "必应壁纸", "自定义图片", "极简白", "素雅灰", "极夜黑",
    "浅灰蓝", "极客紫", "嫩芽绿", "咸蛋黄", "蜜桃粉", "元气红",
)

private const val BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX = 2
private const val BANNER_TIME_INDEX = 2
private const val BANNER_WEATHER_INDEX = 3
private const val BANNER_GREETING_INDEX = 4

// ============================================================
// 数据模型
// ============================================================

internal data class QuickUrl(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val faviconKey: String? = null,
)

internal object NewTabControlColorStore {
    var color by mutableStateOf<Color?>(null)
}

private fun defaultQuickUrls(): List<QuickUrl> = emptyList()

// ============================================================
// 持久化
// ============================================================

internal fun loadQuickUrlsFromPrefs(prefs: SharedPreferences): List<QuickUrl> {
    val raw = prefs.getString(KEY_HOME_QUICK_URLS, null) ?: return defaultQuickUrls()
    return try {
        val arr = JSONArray(raw)
        val list = mutableListOf<QuickUrl>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val url = obj.optString("url", "")
            if (url.isBlank()) continue
            list.add(QuickUrl(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", url),
                url = url,
                faviconKey = obj.optString("favicon", "").ifBlank { null },
            ))
        }
        list
    } catch (_: Exception) { defaultQuickUrls() }
}

internal fun saveQuickUrlsToPrefs(prefs: SharedPreferences, urls: List<QuickUrl>) {
    try {
        val arr = JSONArray()
        urls.forEach { u ->
            arr.put(JSONObject().apply {
                put("id", u.id); put("title", u.title); put("url", u.url)
                put("favicon", u.faviconKey ?: "")
            })
        }
        prefs.edit().putString(KEY_HOME_QUICK_URLS, arr.toString()).apply()
    } catch (_: Exception) {}
}

private data class FlatBookmark(
    val title: String,
    val url: String,
    val faviconKey: String?,
)

private fun loadFlatBookmarks(prefs: SharedPreferences): List<FlatBookmark> {
    val raw = prefs.getString("bookmarks_v2", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        val result = mutableListOf<FlatBookmark>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            collectBookmarks(obj, result)
        }
        result
    } catch (_: Exception) { emptyList() }
}

private fun collectBookmarks(obj: JSONObject, out: MutableList<FlatBookmark>) {
    val isFolder = obj.optBoolean("isFolder", false)
    if (isFolder) {
        val children = obj.optJSONArray("children") ?: return
        for (i in 0 until children.length()) {
            val child = children.optJSONObject(i) ?: continue
            collectBookmarks(child, out)
        }
    } else {
        val url = obj.optString("url", "")
        if (url.isBlank()) return
        out.add(FlatBookmark(
            title = obj.optString("title", url),
            url = url,
            faviconKey = obj.optString("favicon", "").ifBlank { null },
        ))
    }
}

// ============================================================
// 自定义壁纸
// ============================================================

internal fun customWallpaperFile(context: Context): File =
    File(context.filesDir, CUSTOM_WALLPAPER_FILE_NAME)

internal fun hasCustomWallpaper(context: Context): Boolean {
    val f = customWallpaperFile(context)
    return f.exists() && f.length() > 0L
}

internal fun saveCustomWallpaper(context: Context, uri: Uri): Boolean {
    return try {
        val destFile = customWallpaperFile(context)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output, 8192) }
        } ?: return false
        val bmp = BitmapFactory.decodeFile(destFile.absolutePath)
        if (bmp != null) {
            val avgColor = computeAverageColor(bmp)
            try { bmp.recycle() } catch (_: Exception) {}
            context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_HOME_CUSTOM_WALLPAPER_COLOR, avgColor.toArgb().toLong())
                .putString(KEY_HOME_CUSTOM_WALLPAPER_PATH, destFile.absolutePath)
                .apply()
        }
        true
    } catch (_: Exception) { false }
}

internal fun loadCustomWallpaper(context: Context): Bitmap? {
    return try {
        val file = customWallpaperFile(context)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val display = context.resources.displayMetrics
        val maxTarget = maxOf(display.widthPixels, display.heightPixels) * 2
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxTarget ||
            bounds.outHeight / (sample * 2) >= maxTarget) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeFile(file.absolutePath, opts)
    } catch (_: Exception) { null }
}

// ============================================================
// 自定义横幅（图片横幅）
// ============================================================

internal fun customBannerFile(context: Context): File =
    File(context.filesDir, CUSTOM_BANNER_FILE_NAME)

internal fun hasCustomBannerImage(context: Context): Boolean {
    val file = customBannerFile(context)
    return file.exists() && file.length() > 0L
}

internal fun saveCustomBanner(context: Context, uri: Uri): Boolean {
    return try {
        val destFile = customBannerFile(context)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output, 8192) }
        } ?: return false
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOME_CUSTOM_BANNER_PATH, destFile.absolutePath)
            .apply()
        true
    } catch (_: Exception) {
        false
    }
}

internal fun clearCustomBanner(context: Context): Boolean {
    return try {
        customBannerFile(context).delete()
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HOME_CUSTOM_BANNER_PATH)
            .apply()
        true
    } catch (_: Exception) {
        false
    }
}

internal fun loadCustomBanner(context: Context): Bitmap? {
    return try {
        val file = customBannerFile(context)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= 1600) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeFile(file.absolutePath, opts)
    } catch (_: Exception) {
        null
    }
}

internal fun computeAverageColor(bmp: Bitmap): Color {
    return try {
        val small = Bitmap.createScaledBitmap(bmp, 1, 1, true)
        val pixel = small.getPixel(0, 0)
        if (small !== bmp) { try { small.recycle() } catch (_: Exception) {} }
        Color(pixel)
    } catch (_: Exception) { Color(0xFF888888) }
}
internal fun evaluateBackgroundIsLight(bmp: Bitmap): Boolean {
    return try {
        val small = Bitmap.createScaledBitmap(bmp, 3, 3, true)
        var topLum = 0f
        var midLum = 0f
        var bottomLum = 0f

        for (x in 0..2) {
            topLum += Color(small.getPixel(x, 0)).luminance()
            midLum += Color(small.getPixel(x, 1)).luminance()
            bottomLum += Color(small.getPixel(x, 2)).luminance()
        }
        if (small !== bmp) { try { small.recycle() } catch (_: Exception) {} }

        topLum /= 3f
        midLum /= 3f
        bottomLum /= 3f

        val avgLum = (topLum * 0.3f + midLum * 0.4f + bottomLum * 0.3f)

        if (topLum < 0.3f || bottomLum < 0.3f) {
            return false
        }

        if (midLum > 0.7f) {
            return true
        }

        avgLum > 0.5f
    } catch (_: Exception) {
        val small = Bitmap.createScaledBitmap(bmp, 1, 1, true)
        val lum = Color(small.getPixel(0, 0)).luminance()
        if (small !== bmp) { try { small.recycle() } catch (_: Exception) {} }
        lum > 0.5f
    }
}
// ============================================================
// 壁纸
// ============================================================

internal data class WallpaperSpec(
    val brush: Brush?,
    val bitmap: Bitmap? = null,
    val isLightBg: Boolean,
    val toolbarColor: Color,
)

internal fun wallpaperFor(
    index: Int,
    isDarkTheme: Boolean,
    customBitmap: Bitmap?,
    bingBitmap: Bitmap? = null,
): WallpaperSpec {
    return when (index) {
        0 -> if (isDarkTheme) WallpaperSpec(
            brush = Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0E0E0E))),
            isLightBg = false,
            toolbarColor = Color(0xFF0E0E0E),
        ) else WallpaperSpec(
            brush = Brush.verticalGradient(listOf(Color(0xFFFAFAFA), Color(0xFFF2F2F2))),
            isLightBg = true,
            toolbarColor = Color(0xFFF2F2F2),
        )
        1 -> if (bingBitmap != null) {
            val avg = computeAverageColor(bingBitmap)
            val isLight = evaluateBackgroundIsLight(bingBitmap)
            WallpaperSpec(null, bingBitmap, isLight, avg)
        } else {
            WallpaperSpec(SolidColor(Color(0xFF222222)), isLightBg = false, toolbarColor = Color(0xFF222222))
        }
        2 -> {
            if (customBitmap != null) {
                val avg = computeAverageColor(customBitmap)
                val isLight = evaluateBackgroundIsLight(customBitmap)
                WallpaperSpec(null, customBitmap, isLight, avg)
            } else wallpaperFor(0, isDarkTheme, null)
        }
        3 -> WallpaperSpec(SolidColor(Color(0xFFFFFFFF)), isLightBg = true, toolbarColor = Color(0xFFFFFFFF))
        4 -> WallpaperSpec(SolidColor(Color(0xFFF0F0F0)), isLightBg = true, toolbarColor = Color(0xFFF0F0F0))
        5 -> WallpaperSpec(SolidColor(Color(0xFF0A0A0A)), isLightBg = false, toolbarColor = Color(0xFF0A0A0A))
        6 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFBCCCDB), Color(0xFFE2EBF2))), isLightBg = true, toolbarColor = Color(0xFFE2EBF2))
        7 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFD2B4F0), Color(0xFFE6D6F5))), isLightBg = true, toolbarColor = Color(0xFFE6D6F5))
        8 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFB3E6B8), Color(0xFFD6F5D9))), isLightBg = true, toolbarColor = Color(0xFFD6F5D9))
        9 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFFFE07B), Color(0xFFFFF0B3))), isLightBg = true, toolbarColor = Color(0xFFFFF0B3))
        10 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFFFB3CC), Color(0xFFFFD9E6))), isLightBg = true, toolbarColor = Color(0xFFFFD9E6))
        11 -> WallpaperSpec(Brush.verticalGradient(listOf(Color(0xFFFF9E99), Color(0xFFFFC7C2))), isLightBg = true, toolbarColor = Color(0xFFFFC7C2))
        else -> WallpaperSpec(SolidColor(Color(0xFFFAFAFA)), isLightBg = true, toolbarColor = Color(0xFFFAFAFA))
    }
}

private fun decodeBingBitmapSampled(file: File): Bitmap? {
    return try {
        // 先只读图片头，拿到原始尺寸，不分配像素内存
        val bounds = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // 目标最大边 2560px：能覆盖绝大多数手机屏幕的宽高需求，
        // 同时把 4K(UHD) 图采样到 1920x1080，1920x1080 原图不采样（保持无损）。
        val targetMaxEdge = 2560
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetMaxEdge ||
            bounds.outHeight / (sample * 2) >= targetMaxEdge
        ) {
            sample *= 2
        }

        val opts = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        android.graphics.BitmapFactory.decodeFile(file.absolutePath, opts)
    } catch (_: Exception) {
        null
    }
}

private fun loadBingWallpaperFromCache(context: Context, dayOffset: Int = 0): Bitmap? {
    return try {
        val targetDate = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
        }
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(targetDate.time)
        val cacheDir = File(context.cacheDir, "bing_wallpapers")
        if (!cacheDir.exists()) return null

        val hdFile = File(cacheDir, "bing_${dateStr}_UHD.jpg")
        if (hdFile.exists()) {
            return decodeBingBitmapSampled(hdFile)
        }

        val stdFile = File(cacheDir, "bing_${dateStr}_1920x1080.jpg")
        if (stdFile.exists()) {
            return decodeBingBitmapSampled(stdFile)
        }

        val legacyFile = File(cacheDir, "bing_$dateStr.jpg")
        if (legacyFile.exists()) {
            return decodeBingBitmapSampled(legacyFile)
        }

        // 目标日期无缓存：返回 null，由 LaunchedEffect 去下载
        null
    } catch (_: Exception) {
        null
    }
}

private suspend fun downloadBingWallpaper(
    context: Context,
    highRes: Boolean = false,
    dayOffset: Int = 0,
): Bitmap? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val targetDate = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
    }
    val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(targetDate.time)
    val cacheDir = File(context.cacheDir, "bing_wallpapers").apply { if (!exists()) mkdirs() }
    val suffix = if (highRes) "_UHD" else "_1920x1080"
    val cacheFile = File(cacheDir, "bing_${dateStr}$suffix.jpg")

    try {
        val url = java.net.URL(
            "https://www.bing.com/HPImageArchive.aspx?format=js&idx=${dayOffset}&n=1&mkt=zh-CN"
        )
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        val jsonStr = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = org.json.JSONObject(jsonStr)
        val images = json.getJSONArray("images")
        if (images.length() > 0) {
            val imgObj = images.getJSONObject(0)
            val imgUrl = "https://www.bing.com" + imgObj.getString("urlbase") + "$suffix.jpg"

            val imgConn = java.net.URL(imgUrl).openConnection() as java.net.HttpURLConnection
            imgConn.connectTimeout = 10000
            imgConn.readTimeout = 10000
            val bmp = android.graphics.BitmapFactory.decodeStream(imgConn.inputStream)
            imgConn.disconnect()

            if (bmp != null) {
                try {
                    java.io.FileOutputStream(cacheFile).use { out ->
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                } catch (_: Exception) {}
                return@withContext bmp
            }
        }
    } catch (_: Exception) {}
    return@withContext null
}

/**
 * 读取必应壁纸的日期偏移（0=今天，1=昨天，最多 7）。
 * 若「记录偏移的日期」不是今天，说明已经跨天，把偏移重置为 0 并写回，
 * 实现「切换后当天保持，第二天恢复当天壁纸」。
 */
private fun loadBingDayOffset(prefs: SharedPreferences): Int {
    val savedDate = prefs.getString(KEY_BING_OFFSET_DATE, "") ?: ""
    val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        .format(java.util.Date())
    if (savedDate != todayStr) {
        prefs.edit()
            .putInt(KEY_BING_DAY_OFFSET, 0)
            .putString(KEY_BING_OFFSET_DATE, todayStr)
            .apply()
        return 0
    }
    return prefs.getInt(KEY_BING_DAY_OFFSET, 0).coerceIn(0, 7)
}

/**
 * 保存必应壁纸的日期偏移，并记录「设置日期 = 今天」，
 * 以便第二天自动重置为 0。
 */
private fun saveBingDayOffset(prefs: SharedPreferences, offset: Int) {
    val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        .format(java.util.Date())
    prefs.edit()
        .putInt(KEY_BING_DAY_OFFSET, offset.coerceIn(0, 7))
        .putString(KEY_BING_OFFSET_DATE, todayStr)
        .apply()
}

/**
 * 只请求必应 API 获取今日高清原图的完整 URL，不下载图片本身。
 * 供「下载原图」走浏览器统一的下载器（enqueueDownload）使用。
 */
internal fun fetchBingWallpaperUrl(
    highRes: Boolean = true,
    dayOffset: Int = 0,
): String? {
    return try {
        val suffix = if (highRes) "_UHD" else "_1920x1080"
        // ★ 与 downloadBingWallpaper 保持一致的偏移语义：
        //   idx=0 是今天，idx=1 是昨天……最多 7 天前（必应接口上限）。
        val offset = dayOffset.coerceIn(0, 7)
        val conn = URL("https://www.bing.com/HPImageArchive.aspx?format=js&idx=$offset&n=1&mkt=zh-CN")
            .openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        try {
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            val images = json.optJSONArray("images") ?: return null
            if (images.length() == 0) return null
            val imgObj = images.getJSONObject(0)
            val urlbase = imgObj.optString("urlbase", "")
            if (urlbase.isBlank()) return null
            "https://www.bing.com$urlbase$suffix.jpg"
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}

internal fun getToolbarColorForNewTab(
    prefs: SharedPreferences,
    isDarkTheme: Boolean,
): Color {
    val index = prefs.getInt(KEY_HOME_BACKGROUND_CONTENT, 0)
    if (index == 1) {
        val colorLong = prefs.getLong(KEY_HOME_CUSTOM_WALLPAPER_COLOR, 0L)
        if (colorLong != 0L) return Color(colorLong.toInt())
        return if (isDarkTheme) Color(0xFF0E0E0E) else Color(0xFFF2F2F2)
    }
    return wallpaperFor(index, isDarkTheme, null).toolbarColor
}

// ============================================================
// FaviconStore
// ============================================================

internal object FaviconStore {

    private const val DIR_NAME = "favicons"
    private const val MAX_DIM = 192
    private const val MIN_ACCEPTABLE_DIM = 32
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

    private val memoryCache = ConcurrentHashMap<String, Bitmap>()
    private val pendingFetches = ConcurrentHashMap<String, Boolean>()

    private fun dir(context: Context): File {
        val d = File(context.filesDir, DIR_NAME)
        if (!d.exists()) d.mkdirs()
        return d
    }

    private fun keyToFileName(key: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(key.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) sb.append("%02x".format(b))
        return "$sb.png"
    }

    fun get(context: Context, key: String): Bitmap? {
        if (key.isBlank()) return null
        memoryCache[key]?.let { return it }
        return try {
            val f = File(dir(context), keyToFileName(key))
            if (!f.exists() || f.length() == 0L) return null
            val bmp = BitmapFactory.decodeFile(f.absolutePath) ?: return null
            memoryCache[key] = bmp
            bmp
        } catch (_: Exception) {
            null
        }
    }

    fun has(context: Context, key: String): Boolean {
        if (key.isBlank()) return false
        if (memoryCache.containsKey(key)) return true
        return try {
            val f = File(dir(context), keyToFileName(key))
            f.exists() && f.length() > 0L
        } catch (_: Exception) {
            false
        }
    }

    fun save(context: Context, key: String, bitmap: Bitmap): Boolean {
        if (key.isBlank() || bitmap.width <= 0 || bitmap.height <= 0) return false
        return try {
            val scaled = if (bitmap.width > MAX_DIM || bitmap.height > MAX_DIM) {
                val ratio = MAX_DIM.toFloat() / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt().coerceAtLeast(1),
                    (bitmap.height * ratio).toInt().coerceAtLeast(1),
                    true,
                )
            } else bitmap

            val f = File(dir(context), keyToFileName(key))
            FileOutputStream(f).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            memoryCache[key] = scaled
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchFromHost(context: Context, host: String): Bitmap? =
        withContext(Dispatchers.IO) {
            if (host.isBlank()) return@withContext null
            memoryCache[host]?.let { return@withContext it }
            if (has(context, host)) return@withContext get(context, host)

            if (pendingFetches.putIfAbsent(host, true) != null) return@withContext null
            try {
                val candidates = listOf(
                    "https://$host/apple-touch-icon.png",
                    "https://$host/apple-touch-icon-precomposed.png",
                    "https://$host/favicon.ico",
                )
                var best: Bitmap? = null
                for (url in candidates) {
                    val bmp = try {
                        val conn = URL(url).openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        conn.instanceFollowRedirects = true
                        conn.setRequestProperty("User-Agent", USER_AGENT)
                        conn.connect()
                        if (conn.responseCode !in 200..299) {
                            conn.disconnect()
                            null
                        } else {
                            val b = BitmapFactory.decodeStream(conn.inputStream)
                            conn.disconnect()
                            b
                        }
                    } catch (_: Exception) {
                        null
                    }
                    if (bmp != null &&
                        bmp.width >= MIN_ACCEPTABLE_DIM &&
                        bmp.height >= MIN_ACCEPTABLE_DIM
                    ) {
                        best = bmp
                        break
                    }
                }
                if (best != null) save(context, host, best)
                best
            } catch (e: Exception) {
                Log.w("FaviconStore", "fetchFromHost [$host] fail: ${e.message}")
                null
            } finally {
                pendingFetches.remove(host)
            }
        }

    fun clear(context: Context) {
        memoryCache.clear()
        try {
            dir(context).listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {}
    }
}

// ============================================================
// 材质构建器
// ============================================================
@Composable
@Suppress("UNUSED_PARAMETER")
private fun getSurfaceOverlayColor(
    materialStyle: Int,
    isDarkTheme: Boolean,
    onWallpaper: Color,
    blurTintColor: Color,
): Color {
    return if (materialStyle == 0) {
        // ★ 普通材质：直接使用 blurTintColor（它已经根据是否图片背景计算好了透明或实心色）
        blurTintColor
    } else {
        // 模糊材质：背景色会被模糊覆盖，这里给个基础色即可
        onWallpaper.copy(alpha = 0.08f)
    }
}

@Composable
private fun buildMaterialBlurModifier(
    materialStyle: Int,
    shape: Shape,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    backgroundColor: Color,
    blurTintColor: Color,
): Modifier {
    if (!useBlur || backdrop == null) {
        return Modifier.background(backgroundColor, shape)
    }
    return when (materialStyle) {
        1 -> { // 高级材质
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = shape,
                blurRadius = HOME_BLUR_RADIUS,
                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(BlendColorEntry(color = blurTintColor)),
                ),
            )
        }
        2 -> { // 轻透磨砂
            // ★ 通过传入的 blurTintColor 亮度判断用户设置的材质深浅
            val isDarkTint = blurTintColor.luminance() < 0.5f

            // 根据深浅色意图切换高光和混合模式
            val highlight = if (isDarkTint) Highlight.GlassStrokeSmallDark else Highlight.GlassStrokeSmallLight
            val blendMode = BlurBlendMode.SrcOver

            // ★ 修复：baseSurface 不再用 MiuixTheme.colorScheme.surfaceContainer
            //   （它会跟随软件深色模式，把"材质颜色"设置覆盖掉），
            //   改为由 isDarkTint 决定的固定基准色，
            //   让轻透磨砂的颜色完全由"材质颜色"下拉驱动。
            val baseSurface = if (isDarkTint) Color(0xFF121212) else Color(0xFFF2F2F2)
            val blendColor = if (isDarkTint) {
                lerp(baseSurface, Color.Black, 0.25f).copy(alpha = 0.25f)
            } else {
                // ★ 解决浅色太亮的问题：提亮比例从 0.7 降到 0.4，透明度降到 0.4
                lerp(baseSurface, Color.White, 0.25f).copy(alpha = 0.25f)
            }

            val blurColors = BlurDefaults.blurColors(
                blendColors = listOf(
                    BlendColorEntry(
                        color = blendColor,
                        mode = blendMode,
                    )
                ),
                contrast = 1.25f,
                saturation = 1.25f,
            )
            Modifier
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                    ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                    spotShadowColor = Color.Transparent
                }
                .textureBlur(
                    backdrop = backdrop,
                    shape = shape,
                    blurRadius = 25f,
                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                    colors = blurColors,
                    highlight = highlight,
                )
        }
        else -> { // 普通材质
            Modifier.background(backgroundColor, shape)
        }
    }
}

// ============================================================
// 主入口
// ============================================================

@Composable
private fun NewTabPageMiuix(
    prefs: SharedPreferences,
    searchEngines: List<Pair<String, String>>,
    searchHistory: List<String>,
    isTop: Boolean,
    historyModuleEnabled: Boolean = true,
    bottomInset: Dp = 50.dp,
    onClearSearchHistory: () -> Unit,
    onDeleteSearchHistoryItem: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenScanner: () -> Unit,
    windowCount: Int = 1,
    onSwipeWindow: (Int) -> Unit = {},
    onSwipeNav: (Int) -> Unit = {},
    onSwipeVisual: (direction: Int, offsetPx: Float, isDragging: Boolean) -> Unit = { _, _, _ -> },
    canSwipeBack: Boolean = false,
    canSwipeForward: Boolean = false,
    onOpenNewTabCustomization: () -> Unit = {},
    onRequestDownload: (PendingDownload) -> Unit = {},
    onOpenCrop: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    remember(context) {
        initWeatherCacheFromDisk(context)
        true
    }

    val focusManager = LocalFocusManager.current

    var bannerContent by remember { mutableIntStateOf(prefs.getInt(KEY_HOME_BANNER_CONTENT, 1)) }
    val showBanner = bannerContent != 0
    var backgroundContent by remember { mutableIntStateOf(prefs.getInt(KEY_HOME_BACKGROUND_CONTENT, BACKGROUND_CONTENT_BING_INDEX)) }
    var showQuickUrls by remember { mutableStateOf(prefs.getBoolean(KEY_HOME_SHOW_QUICK_URLS, true)) }
    var scanModuleEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_SCAN_MODULE_ENABLED, true)) }

    var quickInputBarEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_QUICK_INPUT_BAR, true))
    }

    var bannerFontAuto by remember { mutableStateOf(prefs.getBoolean(KEY_HOME_BANNER_FONT_AUTO, true)) }
    var bannerFontWeight by remember { mutableIntStateOf(prefs.getInt(KEY_HOME_BANNER_FONT_WEIGHT, 400)) }

    var controlColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_control_color_mode", 1).coerceIn(0, 3))
    }
    var materialColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_material_color_mode", 0).coerceIn(0, 3))
    }
    var materialStyle by remember { mutableIntStateOf(prefs.getInt("ui_material_style", 2)) }
    var backgroundVignetteEnabled by remember {
        mutableStateOf(prefs.getBoolean("home_background_vignette", true))
    }

    var currentEngineIndex by remember { mutableIntStateOf(prefs.getInt("default_search_engine", 0)) }
    LaunchedEffect(searchEngines) {
        val maxIdx = searchEngines.lastIndex.coerceAtLeast(0)
        if (currentEngineIndex > maxIdx) currentEngineIndex = maxIdx
    }

    val wallpaperVersion = remember { mutableIntStateOf(0) }
    val customWallpaperBitmap = remember(backgroundContent, wallpaperVersion.intValue) {
        if (backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX) loadCustomWallpaper(context) else null
    }

    // ★ 自定义横幅位图：只在「图片」横幅下加载，切换横幅内容/更换图片时刷新
    val bannerImageVersion = remember { mutableIntStateOf(0) }
    val customBannerBitmap = remember(bannerContent, bannerImageVersion.intValue) {
        if (bannerContent == BANNER_IMAGE_INDEX) loadCustomBanner(context) else null
    }

    // ★ 必应壁纸展示的日期偏移（0=今天，1=昨天，最多 7）。
    //   在 remember 时读取并检查：若「记录偏移的日期」不是今天，重置为 0，
    //   实现「切换后当天保持，第二天恢复当天壁纸」。
    var bingDayOffset by remember { mutableIntStateOf(loadBingDayOffset(prefs)) }

    // ★ 修复：加入 backgroundContent + bingDayOffset 作为 key，
    //   切到必应壁纸或切换日期时立即从缓存加载；
    //   否则 remember 不重算、LaunchedEffect 又因「当日缓存已存在」跳过下载，
    //   导致 bingWallpaperBitmap 一直是 null，背景不显示。
    var bingWallpaperBitmap by remember(backgroundContent, bingDayOffset) {
        mutableStateOf(
            if (backgroundContent == BACKGROUND_CONTENT_BING_INDEX)
                loadBingWallpaperFromCache(context, bingDayOffset)
            else null
        )
    }

    // ★ 必应壁纸：优先加载低清，随后拉取高清并渐变覆盖（dayOffset 对应必应 API 的 idx）
    LaunchedEffect(backgroundContent, bingDayOffset) {
        if (backgroundContent == BACKGROUND_CONTENT_BING_INDEX) {
            val targetDate = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -bingDayOffset)
            }
            val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(targetDate.time)
            val cacheDir = File(context.cacheDir, "bing_wallpapers")
            val targetHdFile = File(cacheDir, "bing_${dateStr}_UHD.jpg")

            if (!targetHdFile.exists()) {
                val targetStdFile = File(cacheDir, "bing_${dateStr}_1920x1080.jpg")
                val targetLegacyFile = File(cacheDir, "bing_$dateStr.jpg")
                if (!targetStdFile.exists() && !targetLegacyFile.exists()) {
                    val lowRes = downloadBingWallpaper(context, highRes = false, dayOffset = bingDayOffset)
                    if (lowRes != null) {
                        bingWallpaperBitmap = lowRes
                    }
                }

                val hdRes = downloadBingWallpaper(context, highRes = true, dayOffset = bingDayOffset)
                if (hdRes != null) {
                    bingWallpaperBitmap = hdRes
                }
            }
        }
    }

    val quickUrls = remember {
        mutableStateListOf<QuickUrl>().apply { addAll(loadQuickUrlsFromPrefs(prefs)) }
    }

    val alreadyAnimatedIds = remember {
        mutableStateListOf<String>().apply {
            addAll(quickUrls.map { it.id })
        }
    }

    val removingIds = remember { mutableStateListOf<String>() }

    var isEditMode by remember { mutableStateOf(false) }
    var editSnapshot by remember { mutableStateOf<List<QuickUrl>?>(null) }
    var isFolderEditMode by remember { mutableStateOf(false) }
    var folderEditSnapshot by remember { mutableStateOf<List<QuickUrl>?>(null) }

    var draggingItemId by remember { mutableStateOf<String?>(null) }
    var draggingFromFolder by remember { mutableStateOf(false) }
    var dragAbsPos by remember { mutableStateOf(Offset.Zero) }
    var dragOriginalIndex by remember { mutableIntStateOf(-1) }
    var dragTargetIndex by remember { mutableIntStateOf(-1) }
    var dragReleaseAnimating by remember { mutableStateOf(false) }
    var dragReleaseTargetIndex by remember { mutableIntStateOf(-1) }
    var releaseOverridePx by remember { mutableStateOf<Offset?>(null) }

    var moreButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var gridBounds by remember { mutableStateOf<Rect?>(null) }
    var folderBounds by remember { mutableStateOf<Rect?>(null) }

    var isFolderOpen by remember { mutableStateOf(false) }
    var folderSourceBounds by remember { mutableStateOf<Rect?>(null) }
    val folderAnimProgress = remember { Animatable(0f) }

    var currentMaxRows by remember { mutableIntStateOf(QUICK_URL_MAX_ROWS) }

    val visibleCount by remember {
        derivedStateOf {
            val maxCells = (currentMaxRows * QUICK_URL_COLUMNS).coerceAtLeast(QUICK_URL_COLUMNS)
            val needsMore = (quickUrls.size + 1) > maxCells
            if (needsMore) (maxCells - 1).coerceAtLeast(0) else quickUrls.size
        }
    }

    val outerDisplayOrder: List<String> = remember(
        quickUrls.toList(), draggingItemId, dragTargetIndex, dragOriginalIndex, draggingFromFolder
    ) {
        val ids = quickUrls.map { it.id }.toMutableList()
        val dragId = draggingItemId
        if (dragId == null || draggingFromFolder) return@remember ids
        val from = ids.indexOf(dragId)
        if (from < 0 || dragTargetIndex < 0) return@remember ids
        ids.removeAt(from)
        val adjusted = dragTargetIndex.coerceIn(0, ids.size)
        ids.add(adjusted, dragId)
        ids
    }

    val folderDisplayOrder: List<String> = remember(
        quickUrls.toList(), draggingItemId, dragTargetIndex, dragOriginalIndex,
        draggingFromFolder, isFolderEditMode, visibleCount
    ) {
        val ids = quickUrls.map { it.id }.toMutableList()
        val dragId = draggingItemId
        if (dragId == null || !draggingFromFolder) return@remember ids
        val from = ids.indexOf(dragId)
        if (from < 0 || dragTargetIndex < 0) return@remember ids
        ids.removeAt(from)
        val adjusted = dragTargetIndex.coerceIn(0, ids.size)
        ids.add(adjusted, dragId)
        ids
    }

    val folderList: List<QuickUrl> = remember(quickUrls.toList(), isFolderEditMode, visibleCount) {
        if (isFolderEditMode) quickUrls.toList() else quickUrls.drop(visibleCount)
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingUrl by remember { mutableStateOf<QuickUrl?>(null) }

    var showBannerFontSheet by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showWeatherDetailSheet by remember { mutableStateOf(false) }

    var searchFocused by remember { mutableStateOf(false) }
    var searchingOut by remember { mutableStateOf(false) }

    var longPressPos by remember { mutableStateOf(Offset.Zero) }
    var showBgMenu by remember { mutableStateOf(false) }

    val searchFocusAnim = remember { Animatable(0f) }
    LaunchedEffect(searchFocused) {
        searchFocusAnim.animateTo(
            targetValue = if (searchFocused) 1f else 0f,
            animationSpec = tween(
                durationMillis = SEARCH_FOCUS_BG_MS,
                easing = SmoothEase,
            ),
        )
    }

    val searchHistorySnapshot by remember {
        derivedStateOf { searchHistory.toList() }
    }

    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) { now = Calendar.getInstance(); delay(1000L) }
    }

    var query by remember { mutableStateOf("") }
    var searchSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean("search_suggest_enabled", true))
    }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(query, currentEngineIndex, searchSuggestEnabled) {
        if (searchSuggestEnabled && query.isNotBlank()) {
            kotlinx.coroutines.delay(150)
            try {
                suggestions = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchSearchSuggestions(query, currentEngineIndex)
                }
            } catch (_: Exception) {
                suggestions = emptyList()
            }
        } else {
            suggestions = emptyList()
        }
    }

    LaunchedEffect(bannerContent) {
        if (bannerContent == BANNER_WEATHER_INDEX) {
            fetchWeather(context)
            while (true) {
                delay(WeatherStateHolder.CACHE_TTL_MS)
                fetchWeather(context, forceRefresh = true)
            }
        }
    }

    val pickWallpaperLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val ok = saveCustomWallpaper(context, uri)
            if (ok) {
                backgroundContent = BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX
                prefs.edit().putInt(KEY_HOME_BACKGROUND_CONTENT, BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX).apply()
            }
        }
    }

    // ★ 与「主题与界面 → 更换背景图片」完全一致的路径：
    //   选图 → loadBitmapForCrop → PendingCropBitmapStore → onOpenCrop()（pushPage("crop")）
    val pickWallpaperForCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    loadBitmapForCrop(context, uri)
                }
                if (bmp != null) {
                    PendingCropBitmapStore.pending = bmp
                    onOpenCrop()
                } else {
                    Toast.makeText(context, "无法读取图片", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val themeSurface = MiuixTheme.colorScheme.surface
    val themeIsDark = remember(themeSurface) { themeSurface.luminance() < 0.5f }
    val wallpaper = remember(
        backgroundContent,
        themeIsDark,
        customWallpaperBitmap,
        themeSurface,
        bingWallpaperBitmap,
    ) {
        if (backgroundContent == 0) {
            WallpaperSpec(
                brush = SolidColor(themeSurface),
                isLightBg = themeSurface.luminance() > 0.5f,
                toolbarColor = themeSurface,
            )
        } else {
            wallpaperFor(backgroundContent, themeIsDark, customWallpaperBitmap, bingWallpaperBitmap)
        }
    }

    val onWallpaper = when (controlColorMode) {
        0 -> MiuixTheme.colorScheme.onSurface
        1 -> if (wallpaper.isLightBg) Color(0xFF000000) else Color(0xFFFFFFFF)
        2 -> Color(0xFF000000)
        3 -> Color(0xFFFFFFFF)
        else -> if (wallpaper.isLightBg) Color(0xFF000000) else Color(0xFFFFFFFF)
    }
    val onWallpaperSecondary = onWallpaper.copy(alpha = 0.6f)

    LaunchedEffect(onWallpaper) {
        NewTabControlColorStore.color = onWallpaper
    }
    DisposableEffect(Unit) {
        onDispose {
            NewTabControlColorStore.color = null
        }
    }

    val isCustomImageBg = backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX
    val isBingBg = backgroundContent == BACKGROUND_CONTENT_BING_INDEX
    // ★ 需求 2a：材质风格/颜色仅在自定义背景为必应壁纸或自定义图片时生效，其余时候用 MIUIX 普通灰色材质
    val effectiveMaterialStyle = if (isCustomImageBg || isBingBg) materialStyle else 0
    val blurSupported = isRuntimeShaderSupported()

    val backdrop: LayerBackdrop? = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null
    val useBlur = (isCustomImageBg || isBingBg) && blurSupported && backdrop != null

    val folderBlurBackdrop: LayerBackdrop? = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null

    val blurTintColor: Color = when (materialColorMode) {
        1 -> Color.White.copy(alpha = 0.2f)
        2 -> Color.Black.copy(alpha = 0.2f)
        else -> if (themeIsDark) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
    }

    val searchBg: Color = blurTintColor

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_HOME_BANNER_CONTENT -> bannerContent = prefs.getInt(key, 1)
                KEY_HOME_BACKGROUND_CONTENT -> backgroundContent = prefs.getInt(key, 0)
                KEY_HOME_SHOW_QUICK_URLS -> showQuickUrls = prefs.getBoolean(key, true)
                KEY_SCAN_MODULE_ENABLED -> scanModuleEnabled = prefs.getBoolean(key, true)
                KEY_QUICK_INPUT_BAR -> quickInputBarEnabled = prefs.getBoolean(key, true)
                KEY_HOME_BANNER_FONT_AUTO -> bannerFontAuto = prefs.getBoolean(key, true)
                KEY_HOME_BANNER_FONT_WEIGHT -> bannerFontWeight = prefs.getInt(key, 400)
                "search_suggest_enabled" -> searchSuggestEnabled = prefs.getBoolean(key, true)
                "ui_control_color_mode" -> {
                    controlColorMode = prefs.getInt(key, 0).coerceIn(0, 3)
                }
                "ui_material_color_mode" -> {
                    materialColorMode = prefs.getInt(key, 0).coerceIn(0, 3)
                }
                "ui_material_style" -> {
                    materialStyle = prefs.getInt(key, 1)
                }
                "home_background_vignette" -> {
                    backgroundVignetteEnabled = prefs.getBoolean(key, true)
                }
                KEY_HOME_QUICK_URLS -> {
                    val latest = loadQuickUrlsFromPrefs(prefs)
                    val same = latest.size == quickUrls.size &&
                            latest.indices.all { latest[it].id == quickUrls[it].id }
                    if (!same) { quickUrls.clear(); quickUrls.addAll(latest) }
                }
                KEY_HOME_CUSTOM_WALLPAPER_PATH,
                KEY_HOME_CUSTOM_WALLPAPER_COLOR -> wallpaperVersion.intValue++
                KEY_HOME_CUSTOM_BANNER_PATH -> bannerImageVersion.intValue++
                "default_search_engine" -> {
                    currentEngineIndex = prefs.getInt(key, 0)
                        .coerceIn(0, searchEngines.lastIndex.coerceAtLeast(0))
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val isLightSystemBars = remember(onWallpaper) { onWallpaper.luminance() < 0.5f }

    LaunchedEffect(isTop, isLightSystemBars, themeIsDark) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (isTop) {
            controller.isAppearanceLightStatusBars = isLightSystemBars
            controller.isAppearanceLightNavigationBars = isLightSystemBars
        } else {
            controller.isAppearanceLightStatusBars = !themeIsDark
            controller.isAppearanceLightNavigationBars = !themeIsDark
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val activity = context as? Activity
                val window = activity?.window ?: return@onDispose
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !themeIsDark
                controller.isAppearanceLightNavigationBars = !themeIsDark
            } catch (_: Exception) {}
        }
    }

    fun enterOuterEditMode() {
        if (!isEditMode) {
            editSnapshot = quickUrls.toList()
            isEditMode = true
        }
    }

    fun commitOuterEdit() {
        editSnapshot = null
        isEditMode = false
    }

    fun cancelOuterEdit() {
        editSnapshot?.let { snap ->
            quickUrls.clear()
            quickUrls.addAll(snap)
            saveQuickUrlsToPrefs(prefs, quickUrls)
        }
        editSnapshot = null
        isEditMode = false
    }

    fun enterFolderEditMode() {
        if (!isFolderEditMode) {
            folderEditSnapshot = quickUrls.toList()
            isFolderEditMode = true
        }
    }

    fun commitFolderEdit() {
        folderEditSnapshot = null
        isFolderEditMode = false
    }

    fun cancelFolderEdit() {
        folderEditSnapshot?.let { snap ->
            quickUrls.clear()
            quickUrls.addAll(snap)
            saveQuickUrlsToPrefs(prefs, quickUrls)
        }
        folderEditSnapshot = null
        isFolderEditMode = false
    }

    fun removeQuickUrlWithAnimation(url: QuickUrl) {
        if (url.id in removingIds) return
        removingIds.add(url.id)
        scope.launch {
            delay(QUICK_URL_REMOVE_DELAY_MS)
            quickUrls.removeAll { it.id == url.id }
            saveQuickUrlsToPrefs(prefs, quickUrls)
            removingIds.remove(url.id)
        }
    }

    fun tryOpenAddDialog() {
        if (quickUrls.size >= MAX_QUICK_URLS) {
            Toast.makeText(context, QUICK_URL_FULL_HINT, Toast.LENGTH_LONG).show()
            return
        }
        editingUrl = null
        showEditDialog = true
    }

    fun computeInsertIndexFromPos(centerX: Float, centerY: Float): Int {
        val center = Offset(centerX, centerY)

        val folderRect = folderBounds
        if (draggingFromFolder && folderRect != null && folderRect.contains(center)) {
            val innerPadH = with(density) { FOLDER_INNER_PADDING_H.toPx() }
            val innerPadV = with(density) { FOLDER_INNER_PADDING_V.toPx() }
            val itemHeightPx = with(density) { FOLDER_ITEM_HEIGHT.toPx() }
            val innerWidth = folderRect.width - innerPadH * 2
            if (innerWidth > 0f) {
                val localX = centerX - folderRect.left - innerPadH
                val localY = centerY - folderRect.top - innerPadV
                if (localX >= 0f && localY >= 0f) {
                    val cellW = innerWidth / QUICK_URL_COLUMNS
                    val col = (localX / cellW).toInt().coerceIn(0, QUICK_URL_COLUMNS - 1)
                    val row = (localY / itemHeightPx).toInt().coerceAtLeast(0)
                    val innerIdx = row * QUICK_URL_COLUMNS + col
                    val baseOffset = if (isFolderEditMode) 0 else visibleCount
                    return (baseOffset + innerIdx).coerceIn(0, quickUrls.size - 1)
                }
            }
        }

        if (!draggingFromFolder) {
            val gridRect = gridBounds
            val gridCellW = (gridRect?.width ?: 0f) / QUICK_URL_COLUMNS
            val gridCellH = with(density) { QUICK_URL_CELL_HEIGHT.toPx() }
            if (gridRect != null && gridCellW > 0f && gridCellH > 0f) {
                val clampedX = centerX.coerceIn(gridRect.left, gridRect.right - 1f)
                val clampedY = centerY.coerceIn(gridRect.top, gridRect.bottom - 1f)
                val localX = clampedX - gridRect.left
                val localY = clampedY - gridRect.top
                val col = (localX / gridCellW).toInt().coerceIn(0, QUICK_URL_COLUMNS - 1)
                val row = (localY / gridCellH).toInt().coerceAtLeast(0)
                val rawIdx = row * QUICK_URL_COLUMNS + col
                val upperBound = (visibleCount - 1).coerceAtLeast(0)
                return rawIdx.coerceIn(0, upperBound)
            }
        }

        return dragTargetIndex
    }

    fun handleDragStart(url: QuickUrl, itemWindowPos: Offset, fromFolder: Boolean) {
        if (fromFolder) {
            if (!isFolderEditMode) enterFolderEditMode()
        } else {
            if (!isEditMode) enterOuterEditMode()
        }
        val idx = quickUrls.indexOfFirst { it.id == url.id }
        if (idx < 0) return
        draggingItemId = url.id
        draggingFromFolder = fromFolder
        dragAbsPos = itemWindowPos
        dragOriginalIndex = idx
        dragTargetIndex = idx
        dragReleaseAnimating = false
        dragReleaseTargetIndex = -1
        releaseOverridePx = null
    }

    fun handleDragMove(delta: Offset) {
        if (draggingItemId == null) return
        dragAbsPos += delta
        val gridRect = gridBounds
        val cellWpx = (gridRect?.width ?: 0f) / QUICK_URL_COLUMNS
        val cellHpx = with(density) { QUICK_URL_CELL_HEIGHT.toPx() }
        if (cellWpx <= 0f || cellHpx <= 0f) return
        val centerX = dragAbsPos.x + cellWpx / 2f
        val centerY = dragAbsPos.y + cellHpx / 2f
        val newTarget = computeInsertIndexFromPos(centerX, centerY)
        if (newTarget != dragTargetIndex) dragTargetIndex = newTarget
    }

    fun handleDragEnd() {
        val id = draggingItemId
        if (id == null) {
            dragReleaseAnimating = false
            dragReleaseTargetIndex = -1
            releaseOverridePx = null
            return
        }
        val curIdx = quickUrls.indexOfFirst { it.id == id }
        val target = dragTargetIndex

        if (curIdx >= 0 && target >= 0 && target != curIdx) {
            val item = quickUrls.removeAt(curIdx)
            val insertAt = target.coerceIn(0, quickUrls.size)
            quickUrls.add(insertAt, item)
            saveQuickUrlsToPrefs(prefs, quickUrls)
            dragReleaseAnimating = false
            dragReleaseTargetIndex = insertAt
            releaseOverridePx = null
            draggingItemId = null
            draggingFromFolder = false
            dragOriginalIndex = -1
            dragTargetIndex = -1
        } else {
            dragReleaseTargetIndex = curIdx
            releaseOverridePx = dragAbsPos
            draggingItemId = null
            dragReleaseAnimating = true
            draggingFromFolder = false
            dragOriginalIndex = -1
            dragTargetIndex = -1
        }
    }

    LaunchedEffect(dragReleaseAnimating) {
        if (dragReleaseAnimating) {
            delay(QUICK_URL_EXIT_MS.toLong() + 50L)
            dragReleaseAnimating = false
            dragReleaseTargetIndex = -1
            releaseOverridePx = null
        }
    }

    BackHandler(enabled = searchFocused || (isFolderOpen && draggingItemId == null) || isEditMode) {
        if (searchFocused) {
            focusManager.clearFocus()
            searchFocused = false
        } else if (isFolderOpen) {
            if (isFolderEditMode) {
                cancelFolderEdit()
            } else {
                isFolderOpen = false
            }
        } else if (isEditMode) {
            cancelOuterEdit()
        }
    }

    fun performSearch(input: String) {
        val t = input.trim()
        if (t.isBlank()) return
        searchingOut = true
        focusManager.clearFocus()
        searchFocused = false
        scope.launch {
            delay(SEARCH_MORPH_MS.toLong())
            onNavigate(t)
            query = ""
            delay(400)
            searchingOut = false
        }
    }

    val bannerClickable = (bannerContent == BANNER_TIME_INDEX ||
            bannerContent == BANNER_GREETING_INDEX ||
            bannerContent == BANNER_WEATHER_INDEX)

    val hasHistoryPopup = historyModuleEnabled && searchHistorySnapshot.isNotEmpty()

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val quickInputBarBottomInset = maxOf(
        imeBottomPadding,
        navBarBottomPadding + bottomInset,
    )
    val isImeVisible = imeBottomPadding > 0.dp

    val isEditModeState = rememberUpdatedState(isEditMode || isFolderEditMode)
    val windowCountState = rememberUpdatedState(windowCount)
    val onSwipeWindowState = rememberUpdatedState(onSwipeWindow)
    val onSwipeNavState = rememberUpdatedState(onSwipeNav)
    val onSwipeVisualState = rememberUpdatedState(onSwipeVisual)
    val draggingItemIdState = rememberUpdatedState(draggingItemId)
    val canSwipeBackState = rememberUpdatedState(canSwipeBack)
    val canSwipeForwardState = rememberUpdatedState(canSwipeForward)

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (folderBlurBackdrop != null) {
                        Modifier.layerBackdrop(folderBlurBackdrop)
                    } else Modifier
                )
                .pointerInput(Unit) {
                    val triggerPx = SWIPE_WINDOW_TRIGGER.toPx()
                    val slopPx = 20f
                    awaitPointerEventScope {
                        while (true) {
                            val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val down = downEvent.changes.firstOrNull { it.changedToDown() }
                                ?: continue

                            val editModeNow = isEditModeState.value
                            val winCountNow = windowCountState.value

                            if (editModeNow || draggingItemIdState.value != null) {
                                while (true) {
                                    val ev = awaitPointerEvent(PointerEventPass.Initial)
                                    if (ev.changes.none { it.pressed }) break
                                }
                                continue
                            }

                            val downId = down.id
                            val startX = down.position.x
                            val startY = down.position.y

                            var isHorizontal = false
                            var cancelled = false
                            var accumulated = 0f

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == downId }
                                    ?: break
                                if (!change.pressed) break

                                if (draggingItemIdState.value != null) {
                                    cancelled = true
                                    isHorizontal = false
                                    break
                                }

                                val dx = change.position.x - startX
                                val dy = change.position.y - startY

                                if (!isHorizontal && !cancelled) {
                                    if (kotlin.math.abs(dx) > slopPx ||
                                        kotlin.math.abs(dy) > slopPx
                                    ) {
                                        if (kotlin.math.abs(dx) >=
                                            kotlin.math.abs(dy) * 1.5f
                                        ) {
                                            isHorizontal = true
                                        } else {
                                            cancelled = true
                                            break
                                        }
                                    }
                                }

                                if (isHorizontal) {
                                    accumulated = dx
                                    val direction = when {
                                        dx > 0 -> -1
                                        dx < 0 -> 1
                                        else -> 0
                                    }
                                    val visualAllowed = when {
                                        direction == 0 -> false
                                        winCountNow > 1 -> true
                                        direction == -1 -> canSwipeBackState.value
                                        direction == 1 -> canSwipeForwardState.value
                                        else -> false
                                    }
                                    if (visualAllowed) {
                                        onSwipeVisualState.value(direction, kotlin.math.abs(dx), true)
                                    }
                                    change.consume()
                                }
                            }

                            onSwipeVisualState.value(0, 0f, false)

                            if (isHorizontal && !cancelled) {
                                val dir = when {
                                    accumulated > triggerPx -> -1
                                    accumulated < -triggerPx -> 1
                                    else -> 0
                                }
                                if (dir != 0) {
                                    if (winCountNow > 1) {
                                        onSwipeWindowState.value(dir)
                                    } else {
                                        val allowed = if (dir == -1) {
                                            canSwipeBackState.value
                                        } else {
                                            canSwipeForwardState.value
                                        }
                                        if (allowed) {
                                            onSwipeNavState.value(dir)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (useBlur && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                focusManager.clearFocus()
                                searchFocused = false
                            },
                            onLongPress = { offset ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                longPressPos = offset
                                showBgMenu = true
                            }
                        )
                    }
            ) {
                val bgModifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val zoomProgress = maxOf(
                            folderAnimProgress.value,
                            searchFocusAnim.value,
                        )
                        val s = 1f + (WALLPAPER_ZOOM_MAX - 1f) * zoomProgress
                        scaleX = s
                        scaleY = s

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (blurSupported && searchFocusAnim.value > 0.01f) {
                                val blurPx = WALLPAPER_SEARCH_BLUR_DP.toPx() * searchFocusAnim.value
                                renderEffect = RenderEffect
                                    .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            } else {
                                renderEffect = null
                            }
                        } else {
                            renderEffect = null
                        }
                    }

                Crossfade(
                    targetState = wallpaper.bitmap,
                    animationSpec = tween(600, easing = SmoothEase),
                    label = "wallpaperCrossfade"
                ) { bmp ->
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = bgModifier,
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(modifier = bgModifier.background(brush = wallpaper.brush ?: SolidColor(Color(0xFFFAFAFA))))
                    }
                }

                val isImageBg = backgroundContent == BACKGROUND_CONTENT_BING_INDEX || backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX
                val vignetteAlpha by animateFloatAsState(
                    targetValue = if (isImageBg && backgroundVignetteEnabled) 1f else 0f,
                    animationSpec = tween(300, easing = SmoothEase),
                    label = "vignetteAlpha",
                )
                if (vignetteAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                        center = center,
                                        radius = size.maxDimension * 0.75f
                                    )
                                )
                            }
                            .graphicsLayer { alpha = vignetteAlpha }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val screenH = maxHeight

                    val searchTargetY = when {
                        searchingOut -> SEARCH_MORPH_TOP_Y
                        searchFocused && hasHistoryPopup -> SEARCH_FOCUS_TOP_Y
                        else -> screenH * 0.45f
                    }
                    val searchTopY by animateDpAsState(
                        targetValue = searchTargetY,
                        animationSpec = tween(
                            durationMillis = if (searchingOut) SEARCH_MORPH_MS else 340,
                            easing = SmoothEase,
                        ),
                        label = "searchTopY",
                    )

                    val bannerTopY = screenH * 0.22f
                    val quickUrlsTopY = screenH * 0.45f - 26.dp + 52.dp + 44.dp

                    val quickUrlsAvailableH = (screenH - quickUrlsTopY - bottomInset).coerceAtLeast(0.dp)
                    val maxRows = (quickUrlsAvailableH / QUICK_URL_CELL_HEIGHT)
                        .toInt()
                        .coerceIn(1, QUICK_URL_MAX_ROWS)

                    LaunchedEffect(maxRows) {
                        if (currentMaxRows != maxRows) currentMaxRows = maxRows
                    }

                    AnimatedVisibility(
                        visible = showBanner && !searchingOut && !(searchFocused && hasHistoryPopup),
                        enter = fadeIn(tween(220)) + scaleIn(
                            animationSpec = tween(240, easing = SmoothEase),
                            initialScale = 0.92f,
                            transformOrigin = TransformOrigin(0.5f, 1f),
                        ),
                        exit = fadeOut(tween(180)) + scaleOut(
                            animationSpec = tween(200, easing = SmoothEase),
                            targetScale = 0.92f,
                            transformOrigin = TransformOrigin(0.5f, 1f),
                        ),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = bannerTopY),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable(
                                    enabled = bannerClickable,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    focusManager.clearFocus()
                                    when (bannerContent) {
                                        BANNER_WEATHER_INDEX -> showWeatherDetailSheet = true
                                        else -> showBannerFontSheet = true
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BannerContent(
                                contentIndex = bannerContent,
                                customBanner = customBannerBitmap,
                                now = now,
                                onWallpaper = onWallpaper,
                                onWallpaperSecondary = onWallpaperSecondary,
                                fontAuto = bannerFontAuto,
                                fontWeightManual = bannerFontWeight,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .offset(y = searchTopY),
                    ) {
                        SearchField(
                            query = query,
                            onQueryChange = { query = it },
                            onSubmit = { performSearch(query) },
                            onFocusChange = { searchFocused = it },
                            searchingOut = searchingOut,
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            backgroundColor = searchBg,
                            showScanner = scanModuleEnabled,
                            onScannerClick = {
                                focusManager.clearFocus()
                                searchFocused = false
                                onOpenScanner()
                            },
                            searchEngines = searchEngines,
                            currentEngineIndex = currentEngineIndex,
                            onEngineSelected = { index ->
                                val clamped = index.coerceIn(0, searchEngines.lastIndex.coerceAtLeast(0))
                                prefs.edit()
                                    .putInt("default_search_engine", clamped)
                                    .putInt("default_search_engine_display", clamped)
                                    .apply()
                            },
                            horizontalPadding = SEARCH_FULL_WIDTH_PADDING,
                            backdrop = backdrop,
                            useBlur = useBlur,
                            blurTintColor = blurTintColor,
                            materialStyle = effectiveMaterialStyle,
                            onWallpaperForHistory = onWallpaper,
                            onWallpaperSecondaryForHistory = onWallpaperSecondary,
                            history = if (searchFocused) searchHistorySnapshot else emptyList(),
                            historyModuleEnabled = historyModuleEnabled,
                            onHistoryItemClick = { item ->
                                query = item
                                performSearch(item)
                            },
                            onDeleteHistoryItem = { item -> onDeleteSearchHistoryItem(item) },
                            onClearHistoryClick = { showClearHistoryConfirm = true },
                            suggestions = if (searchFocused) suggestions else emptyList(),
                            onSuggestionClick = { item ->
                                query = item
                                performSearch(item)
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = showQuickUrls && !searchFocused && !searchingOut,
                        enter = fadeIn(tween(200)) + scaleIn(tween(220, easing = SmoothEase), initialScale = 0.95f),
                        exit = fadeOut(tween(160)) + scaleOut(tween(180, easing = SmoothEase), targetScale = 0.95f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = quickUrlsTopY),
                    ) {
                        QuickUrlGrid(
                            allUrls = quickUrls,
                            displayOrder = outerDisplayOrder,
                            isEditMode = isEditMode,
                            isFolderOpen = isFolderOpen,
                            folderAnimProgress = folderAnimProgress.value,
                            visibleCount = visibleCount,
                            draggingItemId = draggingItemId,
                            draggingFromFolder = draggingFromFolder,
                            dragAbsPos = dragAbsPos,
                            dragReleaseAnimating = dragReleaseAnimating,
                            dragReleaseTargetIndex = dragReleaseTargetIndex,
                            releaseOverridePx = releaseOverridePx,
                            alreadyAnimatedIds = alreadyAnimatedIds,
                            removingIds = removingIds,
                            onGridBoundsChanged = { rect -> gridBounds = rect },
                            onMoreButtonBoundsChanged = { rect -> moreButtonBounds = rect },
                            onItemDragStart = { url, itemWindowPos ->
                                handleDragStart(url, itemWindowPos, fromFolder = false)
                            },
                            onItemDragMove = { delta -> handleDragMove(delta) },
                            onItemDragEnd = { handleDragEnd() },
                            onOpen = { url -> onNavigate(url.url) },
                            onDelete = { url -> removeQuickUrlWithAnimation(url) },
                            onEdit = { url -> editingUrl = url; showEditDialog = true; commitOuterEdit() },
                            onAdd = { tryOpenAddDialog() },
                            onMoreClick = {
                                if (isEditMode) {
                                    commitOuterEdit()
                                    enterFolderEditMode()
                                }
                                folderSourceBounds = moreButtonBounds
                                isFolderOpen = true
                            },
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            onDone = { commitOuterEdit() },
                            onCancel = { cancelOuterEdit() },
                            backdrop = backdrop,
                            useBlur = useBlur,
                            blurTintColor = blurTintColor,
                            materialStyle = effectiveMaterialStyle,
                            themeIsDark = themeIsDark,
                        )
                    }
                }
            }

            val quickInputBarBg = MiuixTheme.colorScheme.surface
            val quickInputButtonBg =
                if (!themeIsDark) Color.White
                else MiuixTheme.colorScheme.secondaryContainer
            val quickInputTextColor = MiuixTheme.colorScheme.onSurface

            val animatedQuickInputBarInset by animateDpAsState(
                targetValue = quickInputBarBottomInset,
                animationSpec = tween(
                    durationMillis = 160,
                    easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                ),
                label = "quickInputBarInset",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = animatedQuickInputBarInset),
            ) {
                AnimatedVisibility(
                    visible = quickInputBarEnabled && searchFocused && isImeVisible,
                    enter = slideInVertically(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = LinearEasing,
                        ),
                        initialOffsetY = { fullHeight -> fullHeight + 300 },
                    ) + fadeIn(tween(160, easing = LinearEasing)),
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = 240,
                            easing = LinearEasing,
                        ),
                        targetOffsetY = { fullHeight -> fullHeight + 400 },
                    ) + fadeOut(tween(220, easing = LinearEasing)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(quickInputBarBg)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val quickTexts = listOf("https://", "www.", "/", ".com", ".cn", ".net", ".org")
                        quickTexts.forEach { text ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(quickInputButtonBg)
                                    .clickable { query = query + text }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(text, color = quickInputTextColor)
                            }
                        }
                    }
                }
            }
        }

        QuickUrlFolderOverlay(
            isOpen = isFolderOpen,
            sourceBounds = folderSourceBounds,
            folderList = folderList,
            folderDisplayOrder = folderDisplayOrder,
            isFolderEditMode = isFolderEditMode,
            visibleCount = visibleCount,
            draggingItemId = draggingItemId,
            draggingFromFolder = draggingFromFolder,
            dragAbsPos = dragAbsPos,
            dragReleaseAnimating = dragReleaseAnimating,
            dragReleaseTargetIndex = dragReleaseTargetIndex,
            releaseOverridePx = releaseOverridePx,
            removingIds = removingIds,
            animProgress = folderAnimProgress,
            onFolderBoundsChanged = { rect -> folderBounds = rect },
            onItemDragStart = { url, itemWindowPos ->
                handleDragStart(url, itemWindowPos, fromFolder = true)
            },
            onItemDragMove = { delta -> handleDragMove(delta) },
            onItemDragEnd = { handleDragEnd() },
            onOpen = { url ->
                isFolderOpen = false
                onNavigate(url.url)
            },
            onAdd = {
                tryOpenAddDialog()
            },
            onDeleteUrl = { url -> removeQuickUrlWithAnimation(url) },
            onDismiss = {
                if (isFolderEditMode) {
                    cancelFolderEdit()
                } else {
                    isFolderOpen = false
                }
            },
            onDone = { commitFolderEdit() },
            onCancel = { cancelFolderEdit() },
            onWallpaper = onWallpaper,
            blurBackdrop = folderBlurBackdrop,
            useBlur = useBlur,
            outsideBlurRadius = with(density) { FOLDER_OUTSIDE_BLUR_DP.toPx() },
            materialStyle = effectiveMaterialStyle,
        )

        val lpX = with(density) { longPressPos.x.toDp() }
        val lpY = with(density) { longPressPos.y.toDp() }

        Box(
            modifier = Modifier
                .offset(x = lpX, y = lpY)
                .size(1.dp)
        ) {
            val menuEntries = remember(backgroundContent, bingDayOffset) {
                buildList {
                    // ★ 必应壁纸时新增「下载原图」「前一天」「后一天」，
                    //   与「定制新标签页」之间由 DropdownEntry 分割线分隔。
                    //   「前一天」「后一天」常驻显示，到达边界时 disabled 变灰。
                    if (backgroundContent == BACKGROUND_CONTENT_BING_INDEX) {
                        val canPrev = bingDayOffset < 7
                        val canNext = bingDayOffset > 0
                        val bingItems = buildList {
                            add(
                                DropdownItem(
                                    text = "下载原图",
                                    icon = { modifier ->
                                        Icon(
                                            imageVector = MiuixIcons.Download,
                                            contentDescription = null,
                                            modifier = modifier
                                        )
                                    },
                                    onClick = {
                                        showBgMenu = false
                                        val offsetForDownload = bingDayOffset
                                        scope.launch {
                                            val url = withContext(Dispatchers.IO) {
                                                fetchBingWallpaperUrl(
                                                    highRes = true,
                                                    dayOffset = offsetForDownload,
                                                )
                                            }
                                            if (url.isNullOrBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "获取原图链接失败",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            } else {
                                                val targetDate = java.util.Calendar.getInstance().apply {
                                                    add(
                                                        java.util.Calendar.DAY_OF_YEAR,
                                                        -offsetForDownload,
                                                    )
                                                }
                                                val dateStr = java.text.SimpleDateFormat(
                                                    "yyyyMMdd", java.util.Locale.US,
                                                ).format(targetDate.time)
                                                onRequestDownload(
                                                    PendingDownload(
                                                        url = url,
                                                        userAgent = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
                                                        contentDisposition = null,
                                                        mimetype = "image/jpeg",
                                                        fileName = "Mote_Bing_${dateStr}.jpg",
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                            )
                            add(
                                DropdownItem(
                                    text = "前一天",
                                    icon = { modifier ->
                                        Icon(
                                            imageVector = MiuixIcons.ChevronBackward,
                                            contentDescription = null,
                                            modifier = modifier
                                        )
                                    },
                                    enabled = canPrev,
                                    onClick = {
                                        if (!canPrev) return@DropdownItem
                                        showBgMenu = false
                                        val next = (bingDayOffset + 1).coerceAtMost(7)
                                        saveBingDayOffset(prefs, next)
                                        bingDayOffset = next
                                    }
                                )
                            )
                            add(
                                DropdownItem(
                                    text = "后一天",
                                    icon = { modifier ->
                                        Icon(
                                            imageVector = MiuixIcons.ChevronForward,
                                            contentDescription = null,
                                            modifier = modifier
                                        )
                                    },
                                    enabled = canNext,
                                    onClick = {
                                        if (!canNext) return@DropdownItem
                                        showBgMenu = false
                                        val next = (bingDayOffset - 1).coerceAtLeast(0)
                                        saveBingDayOffset(prefs, next)
                                        bingDayOffset = next
                                    }
                                )
                            )
                        }
                        add(DropdownEntry(items = bingItems))
                    } else if (backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX) {
                        // ★ 自定义图片背景：组内只有「更换背景图片」，与「定制新标签页」之间由分割线分隔
                        add(
                            DropdownEntry(
                                items = listOf(
                                    DropdownItem(
                                        text = "更换背景图片",
                                        icon = { modifier ->
                                            Icon(
                                                imageVector = MiuixIcons.Photos,
                                                contentDescription = null,
                                                modifier = modifier
                                            )
                                        },
                                        onClick = {
                                            showBgMenu = false
                                            pickWallpaperForCropLauncher.launch("image/*")
                                        }
                                    )
                                )
                            )
                        )
                    }
                    add(
                        DropdownEntry(
                            items = listOf(
                                DropdownItem(
                                    text = "定制新标签页",
                                    icon = { modifier ->
                                        Icon(
                                            imageVector = MiuixIcons.Album,
                                            contentDescription = null,
                                            modifier = modifier
                                        )
                                    },
                                    onClick = {
                                        showBgMenu = false
                                        onOpenNewTabCustomization()
                                    }
                                )
                            )
                        )
                    )
                }
            }
            WindowCascadingListPopup(
                show = showBgMenu,
                entries = menuEntries,
                onDismissRequest = { showBgMenu = false },
                minWidth = 180.dp,
                enableWindowDim = false,
                dropdownColors = DropdownDefaults.dropdownColors(),
            )
        }
    }

    AddEditQuickUrlDialog(
        show = showEditDialog,
        initial = editingUrl,
        prefs = prefs,
        onDismiss = { showEditDialog = false; editingUrl = null },
        onSave = { title, url, faviconKey ->
            val existing = editingUrl
            if (existing == null) {
                if (quickUrls.size >= MAX_QUICK_URLS) {
                    Toast.makeText(context, QUICK_URL_FULL_HINT, Toast.LENGTH_LONG).show()
                } else {
                    val newUrl = QuickUrl(
                        title = title.ifBlank { url },
                        url = url,
                        faviconKey = faviconKey,
                    )
                    quickUrls.add(newUrl)
                    saveQuickUrlsToPrefs(prefs, quickUrls)
                    alreadyAnimatedIds.remove(newUrl.id)
                    if (faviconKey == null) {
                        scope.launch {
                            val host = try { Uri.parse(url).host } catch (_: Exception) { null }
                            if (!host.isNullOrBlank()) {
                                val bmp = FaviconStore.fetchFromHost(context, host)
                                if (bmp != null) {
                                    val idx = quickUrls.indexOfFirst { it.id == newUrl.id }
                                    if (idx >= 0) {
                                        quickUrls[idx] = quickUrls[idx].copy(faviconKey = host)
                                        saveQuickUrlsToPrefs(prefs, quickUrls)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val idx = quickUrls.indexOfFirst { it.id == existing.id }
                if (idx >= 0) {
                    val finalFaviconKey = faviconKey ?: existing.faviconKey
                    val urlChanged = url != existing.url
                    quickUrls[idx] = existing.copy(
                        title = title.ifBlank { url },
                        url = url,
                        faviconKey = if (urlChanged) faviconKey else finalFaviconKey,
                    )
                    saveQuickUrlsToPrefs(prefs, quickUrls)
                    if (urlChanged || finalFaviconKey == null) {
                        scope.launch {
                            val host = try { Uri.parse(url).host } catch (_: Exception) { null }
                            if (!host.isNullOrBlank()) {
                                val bmp = FaviconStore.fetchFromHost(context, host)
                                if (bmp != null) {
                                    val i2 = quickUrls.indexOfFirst { it.id == existing.id }
                                    if (i2 >= 0) {
                                        quickUrls[i2] = quickUrls[i2].copy(faviconKey = host)
                                        saveQuickUrlsToPrefs(prefs, quickUrls)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            showEditDialog = false
            editingUrl = null
        },
    )

    BannerFontSheet(
        show = showBannerFontSheet,
        isGreeting = bannerContent == BANNER_GREETING_INDEX,
        fontAuto = bannerFontAuto,
        fontWeight = bannerFontWeight,
        onFontAutoChange = { auto ->
            bannerFontAuto = auto
            prefs.edit().putBoolean(KEY_HOME_BANNER_FONT_AUTO, auto).apply()
        },
        onFontWeightChange = { w ->
            val clamped = w.coerceIn(100, 900)
            bannerFontWeight = clamped
            prefs.edit().putInt(KEY_HOME_BANNER_FONT_WEIGHT, clamped).apply()
        },
        onDismiss = { showBannerFontSheet = false },
    )

    WeatherDetailSheet(
        show = showWeatherDetailSheet,
        onRefresh = {
            scope.launch {
                fetchWeather(context, forceRefresh = false)
            }
        },
        onDismiss = { showWeatherDetailSheet = false },
    )

    WindowDialog(
        show = showClearHistoryConfirm,
        title = "清空搜索历史",
        onDismissRequest = { showClearHistoryConfirm = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "确定要清空全部搜索历史吗？此操作无法撤销。",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showClearHistoryConfirm = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        onClearSearchHistory()
                        showClearHistoryConfirm = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("清空") }
            }
        }
    }
}

// ============================================================
// 快捷网址网格（外部）
// ============================================================

@Composable
private fun QuickUrlGrid(
    allUrls: List<QuickUrl>,
    displayOrder: List<String>,
    isEditMode: Boolean,
    isFolderOpen: Boolean,
    folderAnimProgress: Float,
    visibleCount: Int,
    draggingItemId: String?,
    draggingFromFolder: Boolean,
    dragAbsPos: Offset,
    dragReleaseAnimating: Boolean,
    dragReleaseTargetIndex: Int,
    releaseOverridePx: Offset?,
    alreadyAnimatedIds: MutableList<String>,
    removingIds: List<String>,
    onGridBoundsChanged: (Rect) -> Unit,
    onMoreButtonBoundsChanged: (Rect) -> Unit,
    onItemDragStart: (QuickUrl, Offset) -> Unit,
    onItemDragMove: (Offset) -> Unit,
    onItemDragEnd: () -> Unit,
    onOpen: (QuickUrl) -> Unit,
    onDelete: (QuickUrl) -> Unit,
    onEdit: (QuickUrl) -> Unit,
    onAdd: () -> Unit,
    onMoreClick: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    blurTintColor: Color,
    materialStyle: Int,
    themeIsDark: Boolean, // ★ 新增
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = QUICK_URL_COLUMNS
        val cellWidth = maxWidth / columns
        val cellHeight = QUICK_URL_CELL_HEIGHT
        val cellWpx = with(density) { cellWidth.toPx() }
        val cellHpx = with(density) { cellHeight.toPx() }

        val maxCells = (QUICK_URL_COLUMNS * QUICK_URL_MAX_ROWS).coerceAtLeast(columns)
        val totalCells = allUrls.size + 1
        val needsMore = totalCells > maxCells

        val hiddenList = if (needsMore) allUrls.drop(visibleCount) else emptyList()
        val totalVisibleCells = visibleCount + 1
        val displayRows = (totalVisibleCells + columns - 1) / columns
        val gridHeight = cellHeight * displayRows

        val visibleItems: List<QuickUrl> = remember(displayOrder, visibleCount) {
            displayOrder
                .take(visibleCount)
                .mapNotNull { id -> allUrls.find { it.id == id } }
        }

        var gridWindowPos by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
                .onGloballyPositioned { coords ->
                    try {
                        val bounds = coords.boundsInWindow()
                        gridWindowPos = Offset(bounds.left, bounds.top)
                        onGridBoundsChanged(bounds)
                    } catch (_: Exception) {}
                },
        ) {
            visibleItems.forEachIndexed { index, url ->
                key(url.id) {
                    val isDragging = url.id == draggingItemId && !draggingFromFolder
                    val isReleaseTarget = dragReleaseAnimating &&
                            url.id != draggingItemId &&
                            index == dragReleaseTargetIndex
                    val isRemoving = url.id in removingIds

                    val targetGridX = cellWpx * (index % columns)
                    val targetGridY = cellHpx * (index / columns)

                    val dragGridX = dragAbsPos.x - gridWindowPos.x
                    val dragGridY = dragAbsPos.y - gridWindowPos.y

                    val animX by animateFloatAsState(
                        targetValue = if (isDragging) dragGridX else targetGridX,
                        animationSpec = if (isDragging) snap() else tween(REORDER_MS, easing = SmoothEase),
                        label = "x_${url.id}",
                    )
                    val animY by animateFloatAsState(
                        targetValue = if (isDragging) dragGridY else targetGridY,
                        animationSpec = if (isDragging) snap() else tween(REORDER_MS, easing = SmoothEase),
                        label = "y_${url.id}",
                    )

                    var itemWindowPos by remember(url.id) { mutableStateOf(Offset.Zero) }

                    val appearProgress = remember(url.id) { Animatable(1f) }
                    LaunchedEffect(url.id) {
                        if (url.id !in alreadyAnimatedIds) {
                            appearProgress.snapTo(0f)
                            appearProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = QUICK_URL_ENTER_MS, easing = SmoothEase),
                            )
                            alreadyAnimatedIds.add(url.id)
                        } else {
                            appearProgress.snapTo(1f)
                        }
                    }

                    val deleteProgress by animateFloatAsState(
                        targetValue = if (isRemoving) 0f else 1f,
                        animationSpec = tween(durationMillis = QUICK_URL_EXIT_MS, easing = SmoothEase),
                        label = "delete_${url.id}",
                    )

                    val zIdx = when {
                        isDragging -> 100f
                        isReleaseTarget -> 90f
                        else -> 0f
                    }
                    val scale = if (isDragging) 1.05f else 1f

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(animX.roundToInt(), animY.roundToInt())
                            }
                            .size(cellWidth, cellHeight)
                            .zIndex(zIdx)
                            .graphicsLayer {
                                val enterScale = 0.6f + appearProgress.value * 0.4f
                                scaleX = scale * enterScale * deleteProgress
                                scaleY = scale * enterScale * deleteProgress
                                alpha = appearProgress.value * deleteProgress
                            }
                            .onGloballyPositioned { coords ->
                                try { itemWindowPos = coords.positionInWindow() } catch (_: Exception) {}
                            }
                            .pointerInput(url.id) {
                                detectTapGestures(
                                    onTap = { if (!isEditMode) onOpen(url) else onEdit(url) },
                                )
                            }
                            .pointerInput(url.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        onItemDragStart(url, itemWindowPos)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onItemDragMove(dragAmount)
                                    },
                                    onDragEnd = { onItemDragEnd() },
                                    onDragCancel = { onItemDragEnd() },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        QuickUrlItem(
                            url = url,
                            isEditMode = isEditMode,
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            onDelete = { onDelete(url) },
                            backdrop = backdrop,
                            useBlur = useBlur,
                            blurTintColor = blurTintColor,
                            materialStyle = materialStyle,
                            themeIsDark = themeIsDark,
                        )
                    }
                }
            }

            val lastIndex = visibleCount
            val lastTargetX = cellWpx * (lastIndex % columns)
            val lastTargetY = cellHpx * (lastIndex / columns)
            val lastAnimX by animateFloatAsState(
                targetValue = lastTargetX,
                animationSpec = tween(REORDER_MS, easing = SmoothEase),
                label = "lastCellX",
            )
            val lastAnimY by animateFloatAsState(
                targetValue = lastTargetY,
                animationSpec = tween(REORDER_MS, easing = SmoothEase),
                label = "lastCellY",
            )
            val moreAlpha = if (needsMore) (1f - folderAnimProgress).coerceIn(0f, 1f) else 0f

            Box(
                modifier = Modifier
                    .offset { IntOffset(lastAnimX.roundToInt(), lastAnimY.roundToInt()) }
                    .size(cellWidth, cellHeight)
                    .graphicsLayer { alpha = if (needsMore) moreAlpha else 1f },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = needsMore,
                    transitionSpec = {
                        fadeIn(tween(200, easing = SmoothEase)) togetherWith
                                fadeOut(tween(180, easing = SmoothEase))
                    },
                    label = "lastCellContent",
                ) { hasMore ->
                    if (hasMore) {
                        QuickUrlMoreButton(
                            hiddenUrls = hiddenList,
                            onClick = onMoreClick,
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            backdrop = backdrop,
                            useBlur = useBlur,
                            blurTintColor = blurTintColor,
                            onButtonBoundsChanged = onMoreButtonBoundsChanged,
                            materialStyle = materialStyle,
                            themeIsDark = themeIsDark,
                        )
                    } else {
                        AddQuickUrlButton(
                            onClick = onAdd,
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            backdrop = backdrop,
                            useBlur = useBlur,
                            blurTintColor = blurTintColor,
                            materialStyle = materialStyle,
                            themeIsDark = themeIsDark,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(tween(200)) + scaleIn(
                    tween(220, easing = SmoothEase), initialScale = 0.8f,
                ),
                exit = fadeOut(tween(160)) + scaleOut(
                    tween(180, easing = SmoothEase), targetScale = 0.8f,
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = 48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onCancel() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("取消", color = MiuixTheme.colorScheme.onSurface, fontSize = 13.sp)
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(tween(200)) + scaleIn(
                    tween(220, easing = SmoothEase), initialScale = 0.8f,
                ),
                exit = fadeOut(tween(160)) + scaleOut(
                    tween(180, easing = SmoothEase), targetScale = 0.8f,
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MiuixTheme.colorScheme.primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onDone() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("确定", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

// ============================================================
// 文件夹形变弹窗
// ============================================================

@Composable
private fun QuickUrlFolderOverlay(
    isOpen: Boolean,
    sourceBounds: Rect?,
    folderList: List<QuickUrl>,
    folderDisplayOrder: List<String>,
    isFolderEditMode: Boolean,
    visibleCount: Int,
    draggingItemId: String?,
    draggingFromFolder: Boolean,
    dragAbsPos: Offset,
    dragReleaseAnimating: Boolean,
    dragReleaseTargetIndex: Int,
    releaseOverridePx: Offset?,
    removingIds: List<String>,
    animProgress: Animatable<Float, *>,
    onFolderBoundsChanged: (Rect) -> Unit,
    onItemDragStart: (QuickUrl, Offset) -> Unit,
    onItemDragMove: (Offset) -> Unit,
    onItemDragEnd: () -> Unit,
    onOpen: (QuickUrl) -> Unit,
    onAdd: () -> Unit,
    onDeleteUrl: (QuickUrl) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onWallpaper: Color,
    blurBackdrop: LayerBackdrop?,
    useBlur: Boolean,
    outsideBlurRadius: Float,
    materialStyle: Int,
) {
    if (sourceBounds == null) return

    val density = LocalDensity.current

    LaunchedEffect(isOpen) {
        if (isOpen) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(FOLDER_OPEN_MS, easing = SmoothEase),
            )
        } else {
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(FOLDER_CLOSE_MS, easing = SmoothEase),
            )
        }
    }

    val shouldDraw = isOpen || animProgress.value > 0.005f
    if (!shouldDraw) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        val p = animProgress.value

        val moreBtnSizePx = with(density) { QUICK_URL_MORE_BUTTON_SIZE.toPx() }
        val targetWidthPx = with(density) { FOLDER_TARGET_WIDTH.toPx() }
        val itemHeightPx = with(density) { FOLDER_ITEM_HEIGHT.toPx() }

        val totalItems = folderList.size + 1
        val rows = ((totalItems + QUICK_URL_COLUMNS - 1) / QUICK_URL_COLUMNS).coerceAtLeast(1)
        val contentHeightPx = rows * itemHeightPx

        val topPadPx = with(density) { FOLDER_INNER_PADDING_V.toPx() }
        val gapPx = with(density) { FOLDER_EDIT_BAR_GAP.toPx() }
        val buttonRowHeightPx = with(density) { FOLDER_EDIT_BAR_BUTTON_HEIGHT.toPx() }
        val bottomPadEditPx = with(density) { FOLDER_BOTTOM_PADDING.toPx() }
        val bottomPadViewPx = with(density) { FOLDER_INNER_PADDING_V.toPx() }

        val targetHeightPxRaw = if (isFolderEditMode) {
            topPadPx + contentHeightPx + gapPx + buttonRowHeightPx + bottomPadEditPx
        } else {
            topPadPx + contentHeightPx + bottomPadViewPx
        }
        val maxTargetHeightPx = screenHeightPx * 0.75f

        val animatedTargetHeightPx by animateFloatAsState(
            targetValue = targetHeightPxRaw.coerceAtMost(maxTargetHeightPx),
            animationSpec = tween(220, easing = SmoothEase),
            label = "folderTargetHeight",
        )
        val targetHeightPx = animatedTargetHeightPx

        val targetLeftPx = (screenWidthPx - targetWidthPx) / 2f
        val targetTopPx = (screenHeightPx - targetHeightPx) / 2f

        val srcCenterX = sourceBounds.left + sourceBounds.width / 2f
        val srcCenterY = sourceBounds.top + sourceBounds.height / 2f
        val targetCenterX = targetLeftPx + targetWidthPx / 2f
        val targetCenterY = targetTopPx + targetHeightPx / 2f

        val curWidthPx = moreBtnSizePx + (targetWidthPx - moreBtnSizePx) * p
        val curHeightPx = moreBtnSizePx + (targetHeightPx - moreBtnSizePx) * p
        val curCenterX = srcCenterX + (targetCenterX - srcCenterX) * p
        val curCenterY = srcCenterY + (targetCenterY - srcCenterY) * p
        val curLeftPx = curCenterX - curWidthPx / 2f
        val curTopPx = curCenterY - curHeightPx / 2f

        val curWidthDp = with(density) { curWidthPx.toDp() }
        val curHeightDp = with(density) { curHeightPx.toDp() }
        val curLeftDp = with(density) { curLeftPx.toDp() }
        val curTopDp = with(density) { curTopPx.toDp() }

        val targetCornerPx = with(density) { FOLDER_FINAL_CORNER.toPx() }
        val curCornerPx = minOf(targetCornerPx, curWidthPx / 2f, curHeightPx / 2f)
        val curCornerDp = with(density) { curCornerPx.toDp() }

        val targetHeightDp = with(density) { targetHeightPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = p.coerceIn(0f, 1f) }
                .then(
                    if (useBlur && blurBackdrop != null) {
                        Modifier.textureBlur(
                            backdrop = blurBackdrop,
                            shape = RoundedCornerShape(0.dp),
                            blurRadius = outsideBlurRadius,
                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                            colors = BlurDefaults.blurColors(
                                blendColors = listOf(
                                    BlendColorEntry(color = Color.Black.copy(alpha = 0.25f)),
                                ),
                            ),
                        )
                    } else {
                        Modifier.background(Color.Black.copy(alpha = 0.25f))
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { if (p >= 0.99f) onDismiss() }
        )

        Box(
            modifier = Modifier
                .offset(x = curLeftDp, y = curTopDp)
                .size(curWidthDp, curHeightDp)
                .onGloballyPositioned { coords ->
                    if (isOpen) {
                        try { onFolderBoundsChanged(coords.boundsInWindow()) } catch (_: Exception) {}
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(curCornerDp))
                    .then(
                        buildMaterialBlurModifier(
                            materialStyle = materialStyle,
                            shape = RoundedCornerShape(curCornerDp),
                            backdrop = blurBackdrop,
                            useBlur = useBlur,
                            backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
                            blurTintColor = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {}
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(QUICK_URL_MORE_BUTTON_SIZE)
                        .graphicsLayer {
                            alpha = (1f - p * 2f).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    FolderMiniGridContent(hiddenUrls = folderList.take(4))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.TopStart,
                ) {
                    Box(
                        modifier = Modifier
                            .size(FOLDER_TARGET_WIDTH, targetHeightDp)
                            .graphicsLayer {
                                val sx = if (targetWidthPx > 0f) curWidthPx / targetWidthPx else 1f
                                val sy = if (targetHeightPx > 0f) curHeightPx / targetHeightPx else 1f
                                scaleX = sx
                                scaleY = sy
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                    ) {
                        FolderExpandedContent(
                            folderList = folderList,
                            folderDisplayOrder = folderDisplayOrder,
                            isFolderEditMode = isFolderEditMode,
                            visibleCount = visibleCount,
                            draggingItemId = draggingItemId,
                            draggingFromFolder = draggingFromFolder,
                            dragAbsPos = dragAbsPos,
                            dragReleaseAnimating = dragReleaseAnimating,
                            dragReleaseTargetIndex = dragReleaseTargetIndex,
                            removingIds = removingIds,
                            appearProgress = p,
                            onItemDragStart = onItemDragStart,
                            onItemDragMove = onItemDragMove,
                            onItemDragEnd = onItemDragEnd,
                            onOpen = onOpen,
                            onAdd = onAdd,
                            onDeleteUrl = onDeleteUrl,
                            onDone = onDone,
                            onCancel = onCancel,
                            onWallpaper = onWallpaper,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 文件夹弹窗内容
// ============================================================

@Composable
private fun FolderExpandedContent(
    folderList: List<QuickUrl>,
    folderDisplayOrder: List<String>,
    isFolderEditMode: Boolean,
    visibleCount: Int,
    draggingItemId: String?,
    draggingFromFolder: Boolean,
    dragAbsPos: Offset,
    dragReleaseAnimating: Boolean,
    dragReleaseTargetIndex: Int,
    removingIds: List<String>,
    appearProgress: Float,
    onItemDragStart: (QuickUrl, Offset) -> Unit,
    onItemDragMove: (Offset) -> Unit,
    onItemDragEnd: () -> Unit,
    onOpen: (QuickUrl) -> Unit,
    onAdd: () -> Unit,
    onDeleteUrl: (QuickUrl) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onWallpaper: Color,
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FOLDER_INNER_PADDING_H),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FOLDER_INNER_PADDING_V),
        ) {
            val cols = QUICK_URL_COLUMNS
            val totalItems = folderList.size + 1
            val rows = (totalItems + cols - 1) / cols
            val cellWidth = maxWidth / cols
            val cellHeight = FOLDER_ITEM_HEIGHT
            val cellWpx = with(density) { cellWidth.toPx() }
            val cellHpx = with(density) { cellHeight.toPx() }
            val gridHeight = cellHeight * rows

            val displayItems: List<QuickUrl> = remember(folderDisplayOrder) {
                folderDisplayOrder
                    .mapNotNull { id -> folderList.find { it.id == id } }
            }

            var gridWindowPos by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .onGloballyPositioned { coords ->
                        try {
                            gridWindowPos = coords.positionInWindow()
                        } catch (_: Exception) {}
                    },
            ) {
                displayItems.forEachIndexed { index, url ->
                    key(url.id) {
                        val isDragging = url.id == draggingItemId && draggingFromFolder
                        val isReleaseTarget = dragReleaseAnimating &&
                                draggingFromFolder &&
                                url.id != draggingItemId &&
                                index == dragReleaseTargetIndex
                        val isRemoving = url.id in removingIds

                        val targetX = cellWpx * (index % cols)
                        val targetY = cellHpx * (index / cols)

                        val dragGridX = dragAbsPos.x - gridWindowPos.x
                        val dragGridY = dragAbsPos.y - gridWindowPos.y

                        val animX by animateFloatAsState(
                            targetValue = if (isDragging) dragGridX else targetX,
                            animationSpec = if (isDragging) snap() else tween(REORDER_MS, easing = SmoothEase),
                            label = "fx_${url.id}",
                        )
                        val animY by animateFloatAsState(
                            targetValue = if (isDragging) dragGridY else targetY,
                            animationSpec = if (isDragging) snap() else tween(REORDER_MS, easing = SmoothEase),
                            label = "fy_${url.id}",
                        )

                        var itemWindowPos by remember(url.id) { mutableStateOf(Offset.Zero) }

                        val deleteProgress by animateFloatAsState(
                            targetValue = if (isRemoving) 0f else 1f,
                            animationSpec = tween(durationMillis = QUICK_URL_EXIT_MS, easing = SmoothEase),
                            label = "folderDelete_${url.id}",
                        )

                        val zIdx = when {
                            isDragging -> 100f
                            isReleaseTarget -> 90f
                            else -> 0f
                        }
                        val scale = if (isDragging) 1.05f else 1f

                        val itemCol = index % cols
                        val itemRow = index / cols
                        val originX = 0.5f - itemCol
                        val originY = 0.5f - itemRow

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(animX.roundToInt(), animY.roundToInt()) }
                                .size(cellWidth, cellHeight)
                                .zIndex(zIdx)
                                .graphicsLayer {
                                    val pp = appearProgress.coerceIn(0f, 1f)
                                    scaleX = scale * pp * deleteProgress
                                    scaleY = scale * pp * deleteProgress
                                    alpha = pp * deleteProgress
                                    transformOrigin = TransformOrigin(originX, originY)
                                }
                                .onGloballyPositioned { coords ->
                                    try { itemWindowPos = coords.positionInWindow() } catch (_: Exception) {}
                                }
                                .pointerInput(url.id) {
                                    detectTapGestures(
                                        onTap = { if (!isFolderEditMode) onOpen(url) },
                                    )
                                }
                                .pointerInput(url.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { _ ->
                                            onItemDragStart(url, itemWindowPos)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onItemDragMove(dragAmount)
                                        },
                                        onDragEnd = { onItemDragEnd() },
                                        onDragCancel = { onItemDragEnd() },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            FolderGridItem(
                                url = url,
                                isFolderEditMode = isFolderEditMode,
                                onWallpaper = onWallpaper,
                                onDelete = { onDeleteUrl(url) },
                            )
                        }
                    }
                }

                if (!isFolderEditMode) {
                    val addIndex = displayItems.size
                    val addCol = addIndex % cols
                    val addRow = addIndex / cols
                    val addOriginX = 0.5f - addCol
                    val addOriginY = 0.5f - addRow
                    val addTargetX = cellWpx * addCol
                    val addTargetY = cellHpx * addRow

                    val addAnimX by animateFloatAsState(
                        targetValue = addTargetX,
                        animationSpec = tween(REORDER_MS, easing = SmoothEase),
                        label = "folderAddX",
                    )
                    val addAnimY by animateFloatAsState(
                        targetValue = addTargetY,
                        animationSpec = tween(REORDER_MS, easing = SmoothEase),
                        label = "folderAddY",
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(addAnimX.roundToInt(), addAnimY.roundToInt()) }
                            .size(cellWidth, cellHeight)
                            .graphicsLayer {
                                val pp = appearProgress.coerceIn(0f, 1f)
                                scaleX = pp
                                scaleY = pp
                                alpha = pp
                                transformOrigin = TransformOrigin(addOriginX, addOriginY)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        FolderGridAddButton(
                            onClick = onAdd,
                            color = onWallpaper,
                        )
                    }
                }
            }
        }

        if (isFolderEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = FOLDER_BOTTOM_PADDING),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(FOLDER_EDIT_BAR_BUTTON_HEIGHT),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .weight(1f)
                        .height(FOLDER_EDIT_BAR_BUTTON_HEIGHT),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun FolderGridItem(
    url: QuickUrl,
    isFolderEditMode: Boolean,
    onWallpaper: Color,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val host = remember(url.url) {
        try { url.url.substringAfter("://").substringBefore('/') } catch (_: Exception) { "" }
    }
    val initial = remember(url.title, host) {
        url.title.firstOrNull()?.toString() ?: host.firstOrNull()?.toString() ?: "?"
    }

    val favicon by produceState<Bitmap?>(
        initialValue = url.faviconKey?.let { FaviconStore.get(context, it) },
        url.faviconKey,
    ) {
        value = url.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (favicon != null) {
                    Image(
                        bitmap = favicon!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = initial,
                        color = onWallpaper,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = url.title.ifBlank { host },
                fontSize = 10.sp,
                color = onWallpaper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedVisibility(
            visible = isFolderEditMode,
            enter = fadeIn(tween(180)) + scaleIn(
                tween(200, easing = SmoothEase), initialScale = 0.5f,
            ),
            exit = fadeOut(tween(140)) + scaleOut(
                tween(160, easing = SmoothEase), targetScale = 0.5f,
            ),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(MiuixIcons.Close, "删除", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun FolderGridAddButton(
    onClick: () -> Unit,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                MiuixIcons.Add,
                "添加",
                tint = color,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("添加", fontSize = 10.sp, color = color)
    }
}

@Composable
private fun FolderMiniGridContent(hiddenUrls: List<QuickUrl>) {
    val preview = hiddenUrls.take(4)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FolderMiniIcon(preview.getOrNull(0))
            FolderMiniIcon(preview.getOrNull(1))
        }
        Spacer(Modifier.height(3.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FolderMiniIcon(preview.getOrNull(2))
            FolderMiniIcon(preview.getOrNull(3))
        }
    }
}

@Composable
private fun FolderMiniIcon(url: QuickUrl?) {
    val context = LocalContext.current

    val favicon by produceState<Bitmap?>(
        initialValue = url?.faviconKey?.let { FaviconStore.get(context, it) },
        url?.faviconKey,
    ) {
        value = url?.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Box(
        modifier = Modifier.size(14.dp).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (favicon != null) {
            Image(
                bitmap = favicon!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

// ============================================================
// 无极字重
// ============================================================

private fun continuousFontWeight(cal: Calendar): Int {
    val totalMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val hours = totalMinutes / 60.0
    val radians = (hours - 6.0) * Math.PI / 12.0
    val weight = 400.0 - 250.0 * kotlin.math.sin(radians)
    return weight.roundToInt().coerceIn(100, 900)
}

private fun effectiveFontWeight(
    cal: Calendar,
    fontAuto: Boolean,
    manualWeight: Int,
): Int {
    return if (fontAuto) continuousFontWeight(cal) else manualWeight.coerceIn(100, 900)
}

// ============================================================
// 横幅
// ============================================================

@Composable
private fun BannerContent(
    contentIndex: Int,
    customBanner: Bitmap?,
    now: Calendar,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    fontAuto: Boolean,
    fontWeightManual: Int,
) {
    when (contentIndex) {
        1 -> {
            if (customBanner != null) {
                Image(
                    bitmap = customBanner.asImageBitmap(),
                    contentDescription = "图片",
                    modifier = Modifier
                        .size(width = 232.dp, height = 130.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.size(128.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.mote_logo),
                        contentDescription = "图片",
                        modifier = Modifier.size(112.dp),
                    )
                }
            }
        }
        2 -> {
            val h = now.get(Calendar.HOUR_OF_DAY)
            val m = now.get(Calendar.MINUTE)
            val timeText = String.format(Locale.US, "%02d:%02d", h, m)
            val weight = effectiveFontWeight(now, fontAuto, fontWeightManual)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeText, fontSize = 72.sp, fontWeight = FontWeight(weight), color = onWallpaper)
                Spacer(Modifier.height(4.dp))
                Text(formatDateText(now), fontSize = 14.sp, color = onWallpaperSecondary)
            }
        }
        3 -> {
            val info = WeatherStateHolder.info
            val loading = WeatherStateHolder.loading
            val error = WeatherStateHolder.error
            when {
                info != null -> {
                    val bannerKey = Triple(
                        info.currentTemp.roundToInt(),
                        info.currentWeatherText,
                        info.city,
                    )
                    AnimatedContent(
                        targetState = bannerKey,
                        transitionSpec = {
                            (fadeIn(tween(420, easing = SmoothEase)) +
                                    scaleIn(
                                        animationSpec = tween(420, easing = SmoothEase),
                                        initialScale = 0.88f,
                                    ))
                                .togetherWith(
                                    fadeOut(tween(220, easing = SmoothEase)) +
                                            scaleOut(
                                                animationSpec = tween(220, easing = SmoothEase),
                                                targetScale = 1.08f,
                                            )
                                )
                        },
                        label = "weatherBanner",
                    ) { key ->
                        val (temp, text, city) = key
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$temp°",
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Light,
                                color = onWallpaper,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = text,
                                    fontSize = 15.sp,
                                    color = onWallpaper,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = city,
                                    fontSize = 13.sp,
                                    color = onWallpaperSecondary,
                                )
                            }
                        }
                    }
                }
                loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "—°",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light,
                            color = onWallpaper.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "获取天气中…",
                            fontSize = 13.sp,
                            color = onWallpaperSecondary,
                        )
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "天气",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = onWallpaper,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = error ?: "点击重试",
                            fontSize = 13.sp,
                            color = onWallpaperSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        4 -> {
            val h = now.get(Calendar.HOUR_OF_DAY)
            val greeting = when (h) {
                in 5 until 11 -> "早上好"; in 11 until 14 -> "中午好"
                in 14 until 18 -> "下午好"; in 18 until 23 -> "晚上好"
                else -> "夜深了"
            }
            val weight = effectiveFontWeight(now, fontAuto, fontWeightManual)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(greeting, fontSize = 32.sp, fontWeight = FontWeight(weight), color = onWallpaper)
                Spacer(Modifier.height(8.dp))
                Text(formatDateText(now), fontSize = 14.sp, color = onWallpaperSecondary)
            }
        }
        else -> Unit
    }
}

private fun formatDateText(cal: Calendar): String {
    val y = cal.get(Calendar.YEAR); val m = cal.get(Calendar.MONTH) + 1; val d = cal.get(Calendar.DAY_OF_MONTH)
    val week = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "周一"; Calendar.TUESDAY -> "周二"; Calendar.WEDNESDAY -> "周三"
        Calendar.THURSDAY -> "周四"; Calendar.FRIDAY -> "周五"; Calendar.SATURDAY -> "周六"
        else -> "周日"
    }
    return "${y}年${m}月${d}日 $week"
}

// ============================================================
// M3 横幅内容（大 Material 时钟 / 天气图标 / 问候语）
// 与 Miuix 版的 BannerContent 完全分开，互不影响
// ============================================================

@Composable
private fun M3BannerContent(
    contentIndex: Int,
    customBanner: Bitmap?,
    now: Calendar,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    fontAuto: Boolean,
    fontWeightManual: Int,
) {
    when (contentIndex) {
        1 -> {
            if (customBanner != null) {
                Image(
                    bitmap = customBanner.asImageBitmap(),
                    contentDescription = "图片",
                    modifier = Modifier
                        .size(width = 232.dp, height = 130.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.size(128.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.mote_logo),
                        contentDescription = "图片",
                        modifier = Modifier.size(112.dp),
                    )
                }
            }
        }
        2 -> {
            val h = now.get(Calendar.HOUR_OF_DAY)
            val m = now.get(Calendar.MINUTE)
            val timeText = String.format(Locale.US, "%02d:%02d", h, m)
            val weight = effectiveFontWeight(now, fontAuto, fontWeightManual)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                M3WavyClock(
                    hour = h,
                    minute = m,
                    fillColor = M3MaterialTheme.colorScheme.primary,
                    hourHandColor = M3MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f),
                    minuteHandColor = M3MaterialTheme.colorScheme.onPrimary,
                    dotColor = M3MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                    modifier = Modifier.size(170.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = timeText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight(weight),
                    color = onWallpaper,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatDateText(now),
                    fontSize = 14.sp,
                    color = onWallpaperSecondary,
                )
            }
        }
        3 -> {
            val info = WeatherStateHolder.info
            val loading = WeatherStateHolder.loading
            val error = WeatherStateHolder.error
            when {
                info != null -> {
                    val bannerKey = Triple(
                        info.currentTemp.roundToInt(),
                        info.currentWeatherCode,
                        info.city,
                    )
                    AnimatedContent(
                        targetState = bannerKey,
                        transitionSpec = {
                            (fadeIn(tween(420, easing = SmoothEase)) +
                                    scaleIn(
                                        animationSpec = tween(420, easing = SmoothEase),
                                        initialScale = 0.88f,
                                    ))
                                .togetherWith(
                                    fadeOut(tween(220, easing = SmoothEase)) +
                                            scaleOut(
                                                animationSpec = tween(220, easing = SmoothEase),
                                                targetScale = 1.08f,
                                            )
                                )
                        },
                        label = "m3WeatherBanner",
                    ) { key ->
                        val (temp, code, city) = key
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "$temp°",
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onWallpaper,
                                )
                                Spacer(Modifier.width(6.dp))
                                M3WeatherIcon(
                                    code = code,
                                    tint = onWallpaper,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .size(30.dp),
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weatherCodeToText(code),
                                    fontSize = 15.sp,
                                    color = onWallpaper,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = city,
                                    fontSize = 13.sp,
                                    color = onWallpaperSecondary,
                                )
                            }
                        }
                    }
                }
                loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "—°",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = onWallpaper.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "获取天气中…",
                            fontSize = 13.sp,
                            color = onWallpaperSecondary,
                        )
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "天气",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = onWallpaper,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = error ?: "点击重试",
                            fontSize = 13.sp,
                            color = onWallpaperSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        4 -> {
            val h = now.get(Calendar.HOUR_OF_DAY)
            val greeting = when (h) {
                in 5 until 11 -> "早上好"; in 11 until 14 -> "中午好"
                in 14 until 18 -> "下午好"; in 18 until 23 -> "晚上好"
                else -> "夜深了"
            }
            val weight = effectiveFontWeight(now, fontAuto, fontWeightManual)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(greeting, fontSize = 32.sp, fontWeight = FontWeight(weight), color = onWallpaper)
                Spacer(Modifier.height(8.dp))
                Text(formatDateText(now), fontSize = 14.sp, color = onWallpaperSecondary)
            }
        }
        else -> Unit
    }
}

// ============================================================
// M3 时钟（波浪边缘 + 实心表盘 + 时分针，无刻度）
// ============================================================

@Composable
private fun M3WavyClock(
    hour: Int,
    minute: Int,
    fillColor: Color,
    hourHandColor: Color,
    minuteHandColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        val cx = w / 2f
        val cy = h / 2f

        val diskBaseR = minDim * 0.42f
        val waveAmp = diskBaseR * 0.07f
        val waves = 12
        val steps = 480
        val path = Path()
        for (i in 0..steps) {
            val t = i.toDouble() / steps
            val angle = t * 2.0 * Math.PI
            val r = diskBaseR + waveAmp * kotlin.math.sin(angle * waves).toFloat()
            val x = cx + r * kotlin.math.cos(angle).toFloat()
            val y = cy + r * kotlin.math.sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, fillColor)

        val dotAngleRad = Math.toRadians(120.0)
        val dotDist = diskBaseR * 0.62f
        val dotX = cx + kotlin.math.cos(dotAngleRad).toFloat() * dotDist
        val dotY = cy + kotlin.math.sin(dotAngleRad).toFloat() * dotDist
        drawCircle(
            color = dotColor,
            radius = diskBaseR * 0.09f,
            center = Offset(dotX, dotY),
        )

        val hourAngle = Math.toRadians(
            ((hour % 12) * 30 + minute * 0.5 - 90).toDouble()
        )
        val hourLen = diskBaseR * 0.42f
        drawLine(
            color = hourHandColor,
            start = Offset(cx, cy),
            end = Offset(
                cx + kotlin.math.cos(hourAngle).toFloat() * hourLen,
                cy + kotlin.math.sin(hourAngle).toFloat() * hourLen,
            ),
            strokeWidth = diskBaseR * 0.16f,
            cap = StrokeCap.Round,
        )

        val minuteAngle = Math.toRadians((minute * 6 - 90).toDouble())
        val minuteLen = diskBaseR * 0.60f
        drawLine(
            color = minuteHandColor,
            start = Offset(cx, cy),
            end = Offset(
                cx + kotlin.math.cos(minuteAngle).toFloat() * minuteLen,
                cy + kotlin.math.sin(minuteAngle).toFloat() * minuteLen,
            ),
            strokeWidth = diskBaseR * 0.14f,
            cap = StrokeCap.Round,
        )
    }
}

// ============================================================
// M3 天气图标（Canvas 自绘，根据 WMO weather code 分支）
// ============================================================

@Composable
private fun M3WeatherIcon(
    code: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = size.minDimension * 0.24f

        when (code) {
            0 -> {
                drawSun(cx, cy, r, tint)
            }
            1, 2 -> {
                drawSun(cx - w * 0.12f, cy - h * 0.14f, r * 0.75f, tint)
                drawCloud(cx + w * 0.06f, cy + h * 0.08f, Size(w * 0.9f, h * 0.9f), tint)
            }
            3 -> {
                drawCloud(cx, cy, Size(w * 0.9f, h * 0.9f), tint)
            }
            45, 48 -> {
                drawCloud(cx, cy - h * 0.08f, Size(w * 0.9f, h * 0.7f), tint)
                for (i in 0 until 3) {
                    val y = cy + h * 0.20f + i * h * 0.10f
                    drawLine(
                        color = tint.copy(alpha = 0.7f),
                        start = Offset(w * 0.12f, y),
                        end = Offset(w * 0.88f, y),
                        strokeWidth = h * 0.05f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> {
                drawCloud(cx, cy - h * 0.08f, Size(w * 0.9f, h * 0.7f), tint)
                for (i in 0 until 3) {
                    val x = cx + (i - 1) * w * 0.18f
                    drawLine(
                        color = tint,
                        start = Offset(x, cy + h * 0.16f),
                        end = Offset(x - w * 0.02f, cy + h * 0.32f),
                        strokeWidth = h * 0.045f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            71, 73, 75, 77, 85, 86 -> {
                drawCloud(cx, cy - h * 0.08f, Size(w * 0.9f, h * 0.7f), tint)
                for (i in 0 until 3) {
                    val x = cx + (i - 1) * w * 0.18f
                    drawCircle(
                        color = tint,
                        radius = h * 0.04f,
                        center = Offset(x, cy + h * 0.24f),
                    )
                }
            }
            95, 96, 99 -> {
                drawCloud(cx, cy - h * 0.08f, Size(w * 0.9f, h * 0.7f), tint)
                val path = Path().apply {
                    moveTo(cx + w * 0.02f, cy + h * 0.14f)
                    lineTo(cx - w * 0.06f, cy + h * 0.30f)
                    lineTo(cx + w * 0.02f, cy + h * 0.30f)
                    lineTo(cx - w * 0.04f, cy + h * 0.44f)
                    lineTo(cx + w * 0.09f, cy + h * 0.26f)
                    lineTo(cx + w * 0.02f, cy + h * 0.26f)
                    close()
                }
                drawPath(path, tint)
            }
            else -> {
                drawCloud(cx, cy, Size(w * 0.9f, h * 0.9f), tint)
            }
        }
    }
}

private fun DrawScope.drawSun(cx: Float, cy: Float, r: Float, color: Color) {
    drawCircle(color = color, radius = r, center = Offset(cx, cy))
    for (i in 0 until 8) {
        val angle = Math.toRadians((i * 45).toDouble())
        val cos = kotlin.math.cos(angle).toFloat()
        val sin = kotlin.math.sin(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(cx + cos * r * 1.35f, cy + sin * r * 1.35f),
            end = Offset(cx + cos * r * 1.75f, cy + sin * r * 1.75f),
            strokeWidth = r * 0.22f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawCloud(
    cx: Float,
    cy: Float,
    size: Size,
    color: Color,
) {
    val w = size.width
    val h = size.height
    val cloudR = minOf(w, h) * 0.22f

    drawCircle(color, cloudR * 0.75f, Offset(cx - cloudR * 0.85f, cy + cloudR * 0.15f))
    drawCircle(color, cloudR, Offset(cx, cy - cloudR * 0.22f))
    drawCircle(color, cloudR * 0.85f, Offset(cx + cloudR * 0.9f, cy + cloudR * 0.10f))
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - cloudR * 1.55f, cy + cloudR * 0.08f),
        size = Size(cloudR * 3.1f, cloudR * 0.9f),
        cornerRadius = CornerRadius(cloudR * 0.45f),
    )
}

// ============================================================
// 天气详情弹窗
// ============================================================

@Composable
private fun WeatherDetailSheet(
    show: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetNavOwner = rememberNavigationEventDispatcherOwner()
    val info = WeatherStateHolder.info
    val loading = WeatherStateHolder.loading
    val error = WeatherStateHolder.error

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var precise by remember {
        initWeatherPreciseFromPrefs(context)
        mutableStateOf(WeatherStateHolder.precise)
    }
    var showMoreMenu by remember { mutableStateOf(false) }

    fun applyPrecise(enabled: Boolean) {
        setWeatherPrecise(context, enabled)
        precise = enabled
        scope.launch { fetchWeather(context, forceRefresh = true) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            applyPrecise(true)
        } else {
            Toast.makeText(context, "需要精确定位权限才能精确到县、市", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(show) {
        if (show) {
            onRefresh()
            while (true) {
                delay(5 * 60 * 1000L)
                onRefresh()
            }
        }
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides sheetNavOwner
    ) {
        WindowBottomSheet(
            show = show,
            onDismissRequest = onDismiss,
            title = "天气",
            backgroundColor = MiuixTheme.colorScheme.surface,
            allowDismiss = true,
            endAction = {
                Box {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showMoreMenu = true },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.More,
                            contentDescription = "更多",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    if (showMoreMenu) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, with(density) { 44.dp.roundToPx() }),
                            onDismissRequest = { showMoreMenu = false },
                            properties = PopupProperties(focusable = true),
                        ) {
                            Card(modifier = Modifier.width(196.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showMoreMenu = false
                                            when {
                                                precise -> applyPrecise(false)
                                                hasFineLocationPermission(context) -> applyPrecise(true)
                                                else -> locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    )
                                                )
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "精确到县、市",
                                        fontSize = 15.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (precise) {
                                        Text(
                                            text = "已开启",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                when {
                    info != null -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .padding(bottom = 12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = info.city,
                                    fontSize = 15.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${info.currentTemp.roundToInt()}",
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Light,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "°C",
                                        fontSize = 20.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 12.dp),
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = info.currentWeatherText,
                                    fontSize = 16.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    WeatherMetricItem(
                                        label = "风向",
                                        value = windDirectionToText(info.currentWindDirection),
                                    )
                                    WeatherMetricItem(
                                        label = "风速",
                                        value = "${info.currentWindSpeed.roundToInt()} km/h",
                                    )
                                    WeatherMetricItem(
                                        label = "湿度",
                                        value = "${info.currentHumidity}%",
                                    )
                                }
                            }
                        }

                        if (info.daily.isNotEmpty()) {
                            Box(modifier = Modifier.offset(x = (-8).dp)) {
                                SmallTitle("近 7 日天气")
                            }
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                ) {
                                    info.daily.forEach { day ->
                                        WeatherDailyRow(day)
                                    }
                                }
                            }
                        }

                        Text(
                            text = "数据源：${info.source}  ·  ${formatUpdateTime(info.updatedAt)}",
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "加载中…",
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = error ?: "暂无天气数据",
                                    fontSize = 14.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                    ),
                                ) { Text("重试") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun WeatherDailyRow(day: WeatherDaily) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = day.dayLabel,
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.width(52.dp),
        )
        Text(
            text = day.weatherText,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${day.minTemp.roundToInt()}° / ${day.maxTemp.roundToInt()}°",
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurface,
        )
    }
}

private fun formatUpdateTime(ts: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return String.format(Locale.US, "%02d:%02d 更新", h, m)
}

// ============================================================
// 横幅字重 BottomSheet
// ============================================================

@Composable
private fun BannerFontSheet(
    show: Boolean,
    isGreeting: Boolean,
    fontAuto: Boolean,
    fontWeight: Int,
    onFontAutoChange: (Boolean) -> Unit,
    onFontWeightChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetNavOwner = rememberNavigationEventDispatcherOwner()

    val now = remember { Calendar.getInstance() }
    val displayedWeight = if (fontAuto) {
        continuousFontWeight(now)
    } else {
        fontWeight.coerceIn(100, 900)
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides sheetNavOwner
    ) {
        WindowBottomSheet(
            show = show,
            onDismissRequest = onDismiss,
            title = if (isGreeting) "问候语字重" else "时间字重",
            backgroundColor = MiuixTheme.colorScheme.surface,
            allowDismiss = true,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "自动跟随时间",
                        summary = "正午最细、午夜最粗",
                        checked = fontAuto,
                        onCheckedChange = onFontAutoChange,
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "字重",
                                fontSize = 15.sp,
                                color = MiuixTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = displayedWeight.toString(),
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Slider(
                            value = displayedWeight.toFloat(),
                            onValueChange = { v ->
                                if (fontAuto) onFontAutoChange(false)
                                onFontWeightChange(v.roundToInt())
                            },
                            valueRange = 100f..900f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "细",
                                fontSize = 11.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "粗",
                                fontSize = 11.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 搜索框
// ============================================================

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    searchingOut: Boolean,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    backgroundColor: Color,
    showScanner: Boolean,
    onScannerClick: () -> Unit,
    searchEngines: List<Pair<String, String>>,
    currentEngineIndex: Int,
    onEngineSelected: (Int) -> Unit,
    horizontalPadding: Dp,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    blurTintColor: Color,
    materialStyle: Int,
    onWallpaperForHistory: Color,
    onWallpaperSecondaryForHistory: Color,
    history: List<String>,
    historyModuleEnabled: Boolean,
    onHistoryItemClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val screenWidth = maxWidth

        val fullWidthFocus = screenWidth - horizontalPadding * 2
        val fullWidthMorph = screenWidth - SEARCH_MORPH_HPADDING * 2
        val collapsedWidth = fullWidthFocus * SEARCH_COLLAPSED_WIDTH_FRACTION

        val animatedWidth by animateDpAsState(
            targetValue = when {
                searchingOut -> fullWidthMorph
                isFocused -> fullWidthFocus
                else -> collapsedWidth
            },
            animationSpec = tween(
                durationMillis = if (searchingOut) SEARCH_MORPH_MS else 340,
                easing = SmoothEase,
            ),
            label = "searchFieldWidth",
        )

        val animatedHeight by animateDpAsState(
            targetValue = when {
                searchingOut -> SEARCH_MORPH_HEIGHT
                isFocused -> SEARCH_FULL_HEIGHT
                else -> SEARCH_COLLAPSED_HEIGHT
            },
            animationSpec = tween(
                durationMillis = if (searchingOut) SEARCH_MORPH_MS else 340,
                easing = SmoothEase,
            ),
            label = "searchFieldHeight",
        )

        val contentAlpha by animateFloatAsState(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = tween(durationMillis = 200, easing = SmoothEase),
            label = "searchFieldContentAlpha",
        )

        val activeContentAlpha by animateFloatAsState(
            targetValue = if (searchingOut) 0f else 1f,
            animationSpec = tween(
                durationMillis = if (searchingOut) SEARCH_MORPH_MS else 200,
                easing = SmoothEase,
            ),
            label = "searchFieldActiveContentAlpha",
        )

        val iconSidePadding = ((animatedHeight - SEARCH_ICON_SIZE) / 2).coerceAtLeast(0.dp)

        val engineEntries = remember(searchEngines, currentEngineIndex) {
            val presets = searchEngines.take(6)
            val customs = searchEngines.drop(6)
            buildList {
                add(
                    DropdownEntry(
                        items = presets.mapIndexed { index, (name, _) ->
                            DropdownItem(
                                text = name,
                                selected = currentEngineIndex == index,
                                onClick = { onEngineSelected(index) },
                            )
                        }
                    )
                )
                if (customs.isNotEmpty()) {
                    add(
                        DropdownEntry(
                            items = customs.mapIndexed { i, (name, _) ->
                                val actualIndex = i + 6
                                DropdownItem(
                                    text = name,
                                    selected = currentEngineIndex == actualIndex,
                                    onClick = { onEngineSelected(actualIndex) },
                                )
                            }
                        )
                    )
                }
            }
        }

        val searchBarShape = RoundedCornerShape(animatedHeight / 2)
        val searchBarBgModifier = buildMaterialBlurModifier(
            materialStyle = materialStyle,
            shape = searchBarShape,
            backdrop = backdrop,
            useBlur = useBlur,
            backgroundColor = backgroundColor,
            blurTintColor = blurTintColor,
        )

        var historyVisible by remember { mutableStateOf(false) }
        LaunchedEffect(isFocused, history.isNotEmpty(), searchingOut, historyModuleEnabled) {
            val shouldShow = historyModuleEnabled && isFocused && history.isNotEmpty() && !searchingOut
            if (shouldShow) {
                delay(HISTORY_DELAY_MS)
                val shouldShowNow = historyModuleEnabled && isFocused && history.isNotEmpty() && !searchingOut
                historyVisible = shouldShowNow
            } else {
                historyVisible = false
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = onWallpaper),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .clip(searchBarShape)
                    .then(searchBarBgModifier)
                    .onFocusChanged { focusState ->
                        val wasFocused = isFocused
                        isFocused = focusState.isFocused
                        onFocusChange(focusState.isFocused)
                        if (wasFocused && !focusState.isFocused) {
                            onQueryChange("")
                        }
                    },
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = activeContentAlpha }
                            .padding(horizontal = iconSidePadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.requiredSize(SEARCH_ICON_SIZE),
                            contentAlignment = Alignment.Center,
                        ) {
                            WindowIconCascadingDropdownMenu(entries = engineEntries) {
                                Icon(
                                    imageVector = MiuixIcons.Search,
                                    contentDescription = "切换搜索引擎",
                                    tint = onWallpaper,
                                    modifier = Modifier.size(25.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .graphicsLayer { alpha = contentAlpha },
                            ) {
                                if (query.isEmpty()) {
                                    Text(
                                        "请输入关键词或网址",
                                        fontSize = 15.sp,
                                        color = onWallpaperSecondary
                                    )
                                }
                                inner()
                            }
                            Text(
                                text = "搜索",
                                fontSize = 15.sp,
                                color = onWallpaperSecondary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .graphicsLayer { alpha = 1f - contentAlpha },
                            )
                        }
                        if (query.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { alpha = contentAlpha }
                                    .size(SEARCH_ICON_SIZE)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { onQueryChange("") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    MiuixIcons.Close,
                                    "清空",
                                    tint = onWallpaperSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = showScanner,
                            enter = fadeIn(tween(160)) + scaleIn(
                                tween(200, easing = SmoothEase),
                                initialScale = 0.7f
                            ),
                            exit = fadeOut(tween(120)) + scaleOut(
                                tween(160, easing = SmoothEase),
                                targetScale = 0.7f
                            ),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                MiuixCircleIconButton(
                                    onClick = onScannerClick,
                                    size = SEARCH_ICON_SIZE
                                ) {
                                    Icon(
                                        MiuixIcons.Scan,
                                        "扫一扫",
                                        tint = onWallpaper,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                },
            )

            val showSuggestionPanel = isFocused && query.isNotBlank() && suggestions.isNotEmpty()
            AnimatedVisibility(
                visible = showSuggestionPanel,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                ) + expandVertically(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    expandFrom = Alignment.Top,
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 140),
                ) + shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    shrinkTowards = Alignment.Top,
                ) + scaleOut(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    targetScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(HISTORY_GAP))

                    val suggestionShape = RoundedCornerShape(28.dp)
                    val suggestionBgModifier = buildMaterialBlurModifier(
                        materialStyle = materialStyle,
                        shape = suggestionShape,
                        backdrop = backdrop,
                        useBlur = useBlur,
                        backgroundColor = backgroundColor,
                        blurTintColor = blurTintColor,
                    )

                    Box(
                        modifier = Modifier
                            .width(animatedWidth)
                            .clip(suggestionShape)
                            .then(suggestionBgModifier)
                            .animateContentSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp),
                        ) {
                            items(
                                count = suggestions.size,
                                key = { idx -> "sug_${suggestions[idx]}" },
                            ) { idx ->
                                val item = suggestions[idx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { onSuggestionClick(item) }
                                        .padding(
                                            start = 18.dp,
                                            end = 20.dp,
                                            top = 12.dp,
                                            bottom = 12.dp,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Search,
                                        contentDescription = null,
                                        tint = onWallpaperSecondaryForHistory,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = item,
                                        fontSize = 15.sp,
                                        color = onWallpaperForHistory,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = historyVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                ) + expandVertically(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    expandFrom = Alignment.Top,
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 140),
                ) + shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    shrinkTowards = Alignment.Top,
                ) + scaleOut(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    targetScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(HISTORY_GAP))

                    val historyShape = RoundedCornerShape(28.dp)
                    val historyBgModifier = buildMaterialBlurModifier(
                        materialStyle = materialStyle,
                        shape = historyShape,
                        backdrop = backdrop,
                        useBlur = useBlur,
                        backgroundColor = backgroundColor,
                        blurTintColor = blurTintColor,
                    )

                    Box(
                        modifier = Modifier
                            .width(animatedWidth)
                            .clip(historyShape)
                            .then(historyBgModifier)
                            .heightIn(max = 400.dp)
                            .animateContentSize(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 18.dp,
                                        end = 12.dp,
                                        top = 12.dp,
                                        bottom = 6.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "搜索历史",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = onWallpaperSecondaryForHistory,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "清空",
                                    fontSize = 13.sp,
                                    color = onWallpaperForHistory,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { onClearHistoryClick() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 6.dp),
                            ) {
                                items(
                                    count = history.size,
                                    key = { idx -> "hist_${history[idx]}" },
                                ) { idx ->
                                    val item = history[idx]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                            ) { onHistoryItemClick(item) }
                                            .padding(
                                                start = 18.dp,
                                                end = 20.dp,
                                                top = 12.dp,
                                                bottom = 12.dp,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Recent,
                                            contentDescription = null,
                                            tint = onWallpaperSecondaryForHistory,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = item,
                                            fontSize = 15.sp,
                                            color = onWallpaperForHistory,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                ) { onDeleteHistoryItem(item) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = "删除",
                                                tint = onWallpaperSecondaryForHistory,
                                                modifier = Modifier.size(14.dp),
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
}

// ============================================================
// 单个快捷网址
// ============================================================

@Composable
private fun QuickUrlItem(
    url: QuickUrl,
    isEditMode: Boolean,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    onDelete: () -> Unit,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    blurTintColor: Color,
    materialStyle: Int,
    themeIsDark: Boolean, // ★ 新增
) {
    val context = LocalContext.current
    val host = remember(url.url) {
        try { url.url.substringAfter("://").substringBefore('/') } catch (_: Exception) { "" }
    }
    val initial = remember(url.title, host) {
        url.title.firstOrNull()?.toString() ?: host.firstOrNull()?.toString() ?: "?"
    }

    val favicon by produceState<Bitmap?>(
        initialValue = url.faviconKey?.let { FaviconStore.get(context, it) },
        url.faviconKey,
    ) {
        value = url.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val iconBgModifier = when {
                favicon != null -> Modifier
                else -> buildMaterialBlurModifier(
                    materialStyle = materialStyle,
                    shape = CircleShape,
                    backdrop = backdrop,
                    useBlur = useBlur,
                    backgroundColor = getSurfaceOverlayColor(materialStyle, themeIsDark, onWallpaper, blurTintColor),
                    blurTintColor = blurTintColor,
                )
            }

            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).then(iconBgModifier),
                contentAlignment = Alignment.Center,
            ) {
                if (favicon != null) {
                    Image(
                        bitmap = favicon!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = initial,
                        color = onWallpaper,
                        fontSize = 20.sp, fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = url.title.ifBlank { host },
                fontSize = 11.sp, color = onWallpaper,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            )
        }

        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn(tween(180)) + scaleIn(tween(200, easing = SmoothEase), initialScale = 0.5f),
            exit = fadeOut(tween(140)) + scaleOut(tween(160, easing = SmoothEase), targetScale = 0.5f),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp).clip(CircleShape).background(Color(0xFFFF3B30))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(MiuixIcons.Close, "删除", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ============================================================
// 加号按钮
// ============================================================

@Composable
private fun AddQuickUrlButton(
    onClick: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    blurTintColor: Color,
    materialStyle: Int,
    themeIsDark: Boolean, // ★ 新增
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val bgModifier = buildMaterialBlurModifier(
            materialStyle = materialStyle,
            shape = CircleShape,
            backdrop = backdrop,
            useBlur = useBlur,
            backgroundColor = getSurfaceOverlayColor(materialStyle, themeIsDark, onWallpaper, blurTintColor),
            blurTintColor = blurTintColor,
        )

        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).then(bgModifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(MiuixIcons.Add, "添加", tint = onWallpaperSecondary, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text("添加", fontSize = 11.sp, color = onWallpaperSecondary)
    }
}

// ============================================================
// "更多"按钮
// ============================================================

@Composable
private fun QuickUrlMoreButton(
    hiddenUrls: List<QuickUrl>,
    onClick: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    backdrop: LayerBackdrop?,
    useBlur: Boolean,
    blurTintColor: Color,
    onButtonBoundsChanged: (Rect) -> Unit,
    materialStyle: Int,
    themeIsDark: Boolean, // ★ 新增
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val bgModifier = buildMaterialBlurModifier(
            materialStyle = materialStyle,
            shape = CircleShape,
            backdrop = backdrop,
            useBlur = useBlur,
            backgroundColor = getSurfaceOverlayColor(materialStyle, themeIsDark, onWallpaper, blurTintColor),
            blurTintColor = blurTintColor,
        )

        Box(
            modifier = Modifier
                .size(QUICK_URL_MORE_BUTTON_SIZE)
                .clip(CircleShape)
                .then(bgModifier)
                .onGloballyPositioned { coords ->
                    try {
                        onButtonBoundsChanged(coords.boundsInWindow())
                    } catch (_: Exception) {}
                },
            contentAlignment = Alignment.Center,
        ) {
            FolderMiniGridContent(hiddenUrls = hiddenUrls)
        }

        Spacer(Modifier.height(6.dp))
        Text("更多", fontSize = 11.sp, color = onWallpaperSecondary)
    }
}

// ============================================================
// 添加/编辑对话框
// ============================================================

@Composable
private fun AddEditQuickUrlDialog(
    show: Boolean,
    initial: QuickUrl?,
    prefs: SharedPreferences,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, faviconKey: String?) -> Unit,
) {
    var titleInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var faviconKeyInput by remember { mutableStateOf<String?>(null) }
    var showBookmarkPicker by remember { mutableStateOf(false) }

    LaunchedEffect(show, initial?.id) {
        if (show) {
            titleInput = initial?.title ?: ""
            urlInput = initial?.url ?: ""
            faviconKeyInput = initial?.faviconKey
            showBookmarkPicker = false
        }
    }

    WindowDialog(
        show = show && !showBookmarkPicker,
        title = if (initial == null) "添加快捷网址" else "编辑快捷网址",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(titleInput, { titleInput = it }, label = "名称", singleLine = true, modifier = Modifier.fillMaxWidth())
            TextField(urlInput, { urlInput = it }, label = "网址", singleLine = true, modifier = Modifier.fillMaxWidth())

            Box(
                modifier = Modifier
                    .fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showBookmarkPicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("从书签添加", fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
                        var url = urlInput.trim()
                        if (url.isNotBlank()) {
                            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
                                url = "https://$url"
                            }
                            onSave(titleInput.trim(), url, faviconKeyInput)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text(if (initial == null) "添加" else "保存") }
            }
        }
    }

    BookmarkPickerDialog(
        show = showBookmarkPicker,
        prefs = prefs,
        onSelect = { title, url, faviconKey ->
            titleInput = title
            urlInput = url
            faviconKeyInput = faviconKey
            showBookmarkPicker = false
        },
        onDismiss = { showBookmarkPicker = false },
    )
}

@Composable
private fun BookmarkPickerDialog(
    show: Boolean,
    prefs: SharedPreferences,
    onSelect: (title: String, url: String, faviconKey: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val bookmarks = remember(show) { if (show) loadFlatBookmarks(prefs) else emptyList() }
    val context = LocalContext.current

    WindowDialog(show = show, title = "选择书签", onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("暂无书签", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                    items(count = bookmarks.size, key = { idx -> "bm_${bookmarks[idx].url}" }) { idx ->
                        val bm = bookmarks[idx]

                        val bmFavicon by produceState<Bitmap?>(
                            initialValue = bm.faviconKey?.let { FaviconStore.get(context, it) },
                            bm.faviconKey,
                        ) {
                            value = bm.faviconKey?.let {
                                withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onSelect(bm.title, bm.url, bm.faviconKey) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                if (bmFavicon != null) {
                                    Image(
                                        bitmap = bmFavicon!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(5.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Icon(
                                        MiuixIcons.Favorites, null,
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    bm.title.ifBlank { bm.url },
                                    fontSize = 15.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    bm.url,
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
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("取消") }
        }
    }
}

// ============================================================
// 新标签页 —— 入口分发
// ============================================================

@Composable
internal fun NewTabPage(
    prefs: SharedPreferences,
    searchEngines: List<Pair<String, String>>,
    searchHistory: List<String>,
    isTop: Boolean,
    historyModuleEnabled: Boolean = true,
    bottomInset: Dp = 50.dp,
    onClearSearchHistory: () -> Unit,
    onDeleteSearchHistoryItem: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenScanner: () -> Unit,
    windowCount: Int = 1,
    onSwipeWindow: (Int) -> Unit = {},
    onSwipeNav: (Int) -> Unit = {},
    onSwipeVisual: (direction: Int, offsetPx: Float, isDragging: Boolean) -> Unit = { _, _, _ -> },
    canSwipeBack: Boolean = false,
    canSwipeForward: Boolean = false,
    onOpenNewTabCustomization: () -> Unit = {},
    onRequestDownload: (PendingDownload) -> Unit = {},
    onOpenCrop: () -> Unit = {},
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> NewTabPageMiuix(
            prefs = prefs,
            searchEngines = searchEngines,
            searchHistory = searchHistory,
            isTop = isTop,
            historyModuleEnabled = historyModuleEnabled,
            bottomInset = bottomInset,
            onClearSearchHistory = onClearSearchHistory,
            onDeleteSearchHistoryItem = onDeleteSearchHistoryItem,
            onNavigate = onNavigate,
            onOpenScanner = onOpenScanner,
            windowCount = windowCount,
            onSwipeWindow = onSwipeWindow,
            onSwipeNav = onSwipeNav,
            onSwipeVisual = onSwipeVisual,
            canSwipeBack = canSwipeBack,
            canSwipeForward = canSwipeForward,
            onOpenNewTabCustomization = onOpenNewTabCustomization,
            onRequestDownload = onRequestDownload,
            onOpenCrop = onOpenCrop,
        )
        UiMode.Material -> NewTabPageMaterial(
            prefs = prefs,
            searchEngines = searchEngines,
            searchHistory = searchHistory,
            isTop = isTop,
            historyModuleEnabled = historyModuleEnabled,
            bottomInset = bottomInset,
            onClearSearchHistory = onClearSearchHistory,
            onDeleteSearchHistoryItem = onDeleteSearchHistoryItem,
            onNavigate = onNavigate,
            onOpenScanner = onOpenScanner,
            onOpenNewTabCustomization = onOpenNewTabCustomization,
        )
    }
}

// ============================================================
// 新标签页 —— Material 3 版
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun NewTabPageMaterial(
    prefs: SharedPreferences,
    searchEngines: List<Pair<String, String>>,
    searchHistory: List<String>,
    isTop: Boolean,
    historyModuleEnabled: Boolean = true,
    bottomInset: Dp = 50.dp,
    onClearSearchHistory: () -> Unit,
    onDeleteSearchHistoryItem: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenNewTabCustomization: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    remember(context) {
        initWeatherCacheFromDisk(context)
        true
    }

    var bannerContent by remember {
        mutableIntStateOf(prefs.getInt(KEY_HOME_BANNER_CONTENT, 1))
    }
    var backgroundContent by remember {
        mutableIntStateOf(prefs.getInt(KEY_HOME_BACKGROUND_CONTENT, BACKGROUND_CONTENT_BING_INDEX))
    }
    var showQuickUrls by remember {
        mutableStateOf(prefs.getBoolean(KEY_HOME_SHOW_QUICK_URLS, true))
    }
    var scanModuleEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_SCAN_MODULE_ENABLED, true))
    }

    var bannerFontAuto by remember {
        mutableStateOf(prefs.getBoolean(KEY_HOME_BANNER_FONT_AUTO, true))
    }
    var bannerFontWeight by remember {
        mutableIntStateOf(prefs.getInt(KEY_HOME_BANNER_FONT_WEIGHT, 400))
    }

    var controlColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_control_color_mode", 1).coerceIn(0, 3))
    }
    var materialColorMode by remember {
        mutableIntStateOf(prefs.getInt("ui_material_color_mode", 0).coerceIn(0, 3))
    }
    var backgroundVignetteEnabled by remember {
        mutableStateOf(prefs.getBoolean("home_background_vignette", true))
    }

    var currentEngineIndex by remember {
        mutableIntStateOf(prefs.getInt("default_search_engine", 0))
    }

    var query by remember { mutableStateOf("") }

    var searchSuggestEnabled by remember {
        mutableStateOf(prefs.getBoolean("search_suggest_enabled", true))
    }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(query, currentEngineIndex, searchSuggestEnabled) {
        if (searchSuggestEnabled && query.isNotBlank()) {
            kotlinx.coroutines.delay(150)
            try {
                suggestions = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchSearchSuggestions(query, currentEngineIndex)
                }
            } catch (_: Exception) {
                suggestions = emptyList()
            }
        } else {
            suggestions = emptyList()
        }
    }

    val wallpaperVersion = remember { mutableIntStateOf(0) }
    val bannerImageVersion = remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "ui_control_color_mode" ->
                    controlColorMode = prefs.getInt(key, 0).coerceIn(0, 3)
                "ui_material_color_mode" ->
                    materialColorMode = prefs.getInt(key, 0).coerceIn(0, 3)
                "home_background_vignette" ->
                    backgroundVignetteEnabled = prefs.getBoolean(key, true)
                KEY_HOME_BANNER_CONTENT ->
                    bannerContent = prefs.getInt(key, 1)
                KEY_HOME_BACKGROUND_CONTENT ->
                    backgroundContent = prefs.getInt(key, 0)
                KEY_HOME_SHOW_QUICK_URLS ->
                    showQuickUrls = prefs.getBoolean(key, true)
                KEY_SCAN_MODULE_ENABLED ->
                    scanModuleEnabled = prefs.getBoolean(key, true)
                KEY_HOME_BANNER_FONT_AUTO ->
                    bannerFontAuto = prefs.getBoolean(key, true)
                KEY_HOME_BANNER_FONT_WEIGHT ->
                    bannerFontWeight = prefs.getInt(key, 400)
                KEY_HOME_CUSTOM_WALLPAPER_PATH,
                KEY_HOME_CUSTOM_WALLPAPER_COLOR ->
                    wallpaperVersion.intValue++
                KEY_HOME_CUSTOM_BANNER_PATH ->
                    bannerImageVersion.intValue++
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val customWallpaperBitmap = remember(backgroundContent, wallpaperVersion.intValue) {
        if (backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX) loadCustomWallpaper(context) else null
    }

    // ★ 自定义横幅位图（Material 版，与 Miuix 版各自独立）
    val customBannerBitmap = remember(bannerContent, bannerImageVersion.intValue) {
        if (bannerContent == BANNER_IMAGE_INDEX) loadCustomBanner(context) else null
    }

    var bingWallpaperBitmap by remember(backgroundContent) {
        mutableStateOf(if (backgroundContent == BACKGROUND_CONTENT_BING_INDEX) loadBingWallpaperFromCache(context) else null)
    }

    // ★ 必应壁纸：优先加载低清，随后拉取高清并渐变覆盖
    LaunchedEffect(backgroundContent) {
        if (backgroundContent == BACKGROUND_CONTENT_BING_INDEX) {
            val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
            val cacheDir = File(context.cacheDir, "bing_wallpapers")
            val todayHdFile = File(cacheDir, "bing_${dateStr}_UHD.jpg")

            if (!todayHdFile.exists()) {
                val todayStdFile = File(cacheDir, "bing_${dateStr}_1920x1080.jpg")
                val todayLegacyFile = File(cacheDir, "bing_$dateStr.jpg")
                if (!todayStdFile.exists() && !todayLegacyFile.exists()) {
                    val lowRes = downloadBingWallpaper(context, highRes = false)
                    if (lowRes != null) {
                        bingWallpaperBitmap = lowRes
                    }
                }

                val hdRes = downloadBingWallpaper(context, highRes = true)
                if (hdRes != null) {
                    bingWallpaperBitmap = hdRes
                }
            }
        }
    }

    val quickUrls = remember {
        mutableStateListOf<QuickUrl>().apply {
            addAll(loadQuickUrlsFromPrefs(prefs))
        }
    }

    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Calendar.getInstance()
            delay(1000L)
        }
    }

    var draggingItemId by remember { mutableStateOf<String?>(null) }
    var dragAbsPos by remember { mutableStateOf(Offset.Zero) }
    var dragOriginalIndex by remember { mutableIntStateOf(-1) }
    var dragTargetIndex by remember { mutableIntStateOf(-1) }
    var gridWindowPos by remember { mutableStateOf(Offset.Zero) }
    var gridWindowWidthPx by remember { mutableFloatStateOf(0f) }
    var gridWindowHeightPx by remember { mutableFloatStateOf(0f) }

    var longPressPos by remember { mutableStateOf(Offset.Zero) }
    var showBgMenu by remember { mutableStateOf(false) }

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val themeSurface = M3MaterialTheme.colorScheme.surface

    // ★ 契合主题 = 直接用 Material 主题的 surface（跟设置页一致）
    val pageBg = themeSurface

    val wallpaper = remember(
        backgroundContent, systemDark, customWallpaperBitmap, themeSurface, bingWallpaperBitmap
    ) {
        if (backgroundContent == 0) {
            WallpaperSpec(
                brush = SolidColor(themeSurface),
                isLightBg = themeSurface.luminance() > 0.5f,
                toolbarColor = themeSurface,
            )
        } else {
            wallpaperFor(backgroundContent, systemDark, customWallpaperBitmap, bingWallpaperBitmap)
        }
    }

    // ★ 需求 2a：材质颜色仅在自定义背景为必应壁纸或自定义图片时生效，其余时候回落 Material 默认灰色
    val effectiveMaterialColorMode = if (
        backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX ||
        backgroundContent == BACKGROUND_CONTENT_BING_INDEX
    ) materialColorMode else 0

    val searchBarColor: Color = when (effectiveMaterialColorMode) {
        1 -> Color.White.copy(alpha = 0.85f) // 浅色
        2 -> Color.Black.copy(alpha = 0.65f) // 深色
        else -> when { // 跟随软件
            backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX && customWallpaperBitmap != null ->
                if (wallpaper.isLightBg) Color.White.copy(alpha = 0.85f)
                else Color.Black.copy(alpha = 0.65f)

            backgroundContent == BACKGROUND_CONTENT_BING_INDEX && bingWallpaperBitmap != null ->
                if (wallpaper.isLightBg) Color.White.copy(alpha = 0.85f)
                else Color.Black.copy(alpha = 0.65f)

            else -> M3MaterialTheme.colorScheme.surfaceContainerHigh
        }
    }

    // ★ 搜索栏前景（图标/文字）色：跟搜索栏背景对比
    val searchBarContentColor: Color =
        if (searchBarColor.luminance() > 0.5f) Color(0xFF000000)
        else Color(0xFFFFFFFF)
    val searchBarHintColor: Color = searchBarContentColor.copy(alpha = 0.5f)

    // ★ Banner 颜色跟随「控件颜色」设置
    val onWallpaper = when (controlColorMode) {
        0 -> M3MaterialTheme.colorScheme.onSurface
        1 -> if (wallpaper.isLightBg) Color(0xFF000000) else Color(0xFFFFFFFF)
        2 -> Color(0xFF000000)
        3 -> Color(0xFFFFFFFF)
        else -> if (wallpaper.isLightBg) Color(0xFF000000) else Color(0xFFFFFFFF)
    }
    val onWallpaperSecondary = onWallpaper.copy(alpha = 0.6f)

    LaunchedEffect(onWallpaper) {
        NewTabControlColorStore.color = onWallpaper
    }
    DisposableEffect(Unit) {
        onDispose {
            NewTabControlColorStore.color = null
        }
    }

    var isEditMode by remember { mutableStateOf(false) }
    var editSnapshot by remember { mutableStateOf<List<QuickUrl>?>(null) }

    fun enterQuickUrlEditMode() {
        if (!isEditMode) {
            editSnapshot = quickUrls.toList()
            isEditMode = true
        }
    }

    fun commitQuickUrlEdit() {
        editSnapshot = null
        isEditMode = false
    }

    fun cancelQuickUrlEdit() {
        editSnapshot?.let { snap ->
            quickUrls.clear()
            quickUrls.addAll(snap)
            saveQuickUrlsToPrefs(prefs, quickUrls)
        }
        editSnapshot = null
        isEditMode = false
    }

    fun removeQuickUrl(url: QuickUrl) {
        quickUrls.removeAll { it.id == url.id }
        saveQuickUrlsToPrefs(prefs, quickUrls)
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingUrl by remember { mutableStateOf<QuickUrl?>(null) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showBannerFontSheet by remember { mutableStateOf(false) }
    var showWeatherDetailSheet by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    var searchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = searchFocused || isEditMode) {
        if (searchFocused) {
            focusManager.clearFocus()
            searchFocused = false
        } else if (isEditMode) {
            cancelQuickUrlEdit()
        }
    }

    LaunchedEffect(bannerContent) {
        if (bannerContent == BANNER_WEATHER_INDEX) {
            fetchWeather(context)
            while (true) {
                delay(WeatherStateHolder.CACHE_TTL_MS)
                fetchWeather(context, forceRefresh = true)
            }
        }
    }

    fun performSearch(input: String) {
        val t = input.trim()
        if (t.isBlank()) return
        onNavigate(t)
        query = ""
    }

    fun computeInsertIndexFromPos(
        centerX: Float,
        centerY: Float,
        visibleCount: Int,
    ): Int {
        if (gridWindowWidthPx <= 0f || gridWindowHeightPx <= 0f) return dragTargetIndex

        val cellHpx = with(density) { QUICK_URL_CELL_HEIGHT.toPx() }
        val cellW = gridWindowWidthPx / QUICK_URL_COLUMNS
        val cellH = cellHpx

        val localX = (centerX - gridWindowPos.x).coerceIn(0f, gridWindowWidthPx - 1f)
        val localY = (centerY - gridWindowPos.y).coerceAtLeast(0f)

        val col = (localX / cellW).toInt().coerceIn(0, QUICK_URL_COLUMNS - 1)
        val row = (localY / cellH).toInt().coerceAtLeast(0)
        val rawIdx = row * QUICK_URL_COLUMNS + col
        return rawIdx.coerceIn(0, (visibleCount - 1).coerceAtLeast(0))
    }

    fun handleDragEnd() {
        val id = draggingItemId
        if (id == null) return
        val curIdx = quickUrls.indexOfFirst { it.id == id }
        val target = dragTargetIndex

        if (curIdx >= 0 && target >= 0 && target != curIdx) {
            val item = quickUrls.removeAt(curIdx)
            val insertAt = target.coerceIn(0, quickUrls.size)
            quickUrls.add(insertAt, item)
            saveQuickUrlsToPrefs(prefs, quickUrls)
        }

        draggingItemId = null
        dragOriginalIndex = -1
        dragTargetIndex = -1
    }

    BackHandler(enabled = searchFocused) {
        focusManager.clearFocus()
        searchFocused = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        searchFocused = false
                    },
                    onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        longPressPos = offset
                        showBgMenu = true
                    }
                )
            },
    ) {
        Crossfade(
            targetState = wallpaper.bitmap,
            animationSpec = tween(600, easing = SmoothEase),
            label = "wallpaperCrossfadeMaterial"
        ) { bmp ->
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else if (wallpaper.brush != null && backgroundContent != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = wallpaper.brush),
                )
            }
        }

        // ★ 新增：背景暗角滤镜
        val isImageBg = backgroundContent == BACKGROUND_CONTENT_BING_INDEX || backgroundContent == BACKGROUND_CONTENT_CUSTOM_IMAGE_INDEX
        val vignetteAlpha by animateFloatAsState(
            targetValue = if (isImageBg && backgroundVignetteEnabled) 1f else 0f,
            animationSpec = tween(300, easing = SmoothEase),
            label = "vignetteAlpha",
        )
        if (vignetteAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                center = center,
                                radius = size.maxDimension * 0.75f
                            )
                        )
                    }
                    .graphicsLayer { alpha = vignetteAlpha }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            val screenH = maxHeight

            val hasHistoryPopup = historyModuleEnabled && searchHistory.isNotEmpty()

            val bannerTopY = screenH * 0.08f
            val quickUrlsTopY = screenH * 0.45f - 26.dp + 52.dp + 44.dp
            val searchTargetY = when {
                searchFocused && hasHistoryPopup -> SEARCH_FOCUS_TOP_Y
                else -> screenH * 0.45f
            }
            val searchTopY by animateDpAsState(
                targetValue = searchTargetY,
                animationSpec = tween(340, easing = SmoothEase),
                label = "m3SearchTopY",
            )

            AnimatedVisibility(
                visible = bannerContent != 0 && !(searchFocused && hasHistoryPopup),
                enter = fadeIn(tween(220)) + scaleIn(
                    tween(240, easing = SmoothEase),
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
                exit = fadeOut(tween(180)) + scaleOut(
                    tween(200, easing = SmoothEase),
                    targetScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = bannerTopY),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable(
                            enabled = bannerContent == BANNER_WEATHER_INDEX ||
                                    bannerContent == BANNER_GREETING_INDEX ||
                                    bannerContent == BANNER_TIME_INDEX,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            when (bannerContent) {
                                BANNER_WEATHER_INDEX -> showWeatherDetailSheet = true
                                else -> showBannerFontSheet = true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    M3BannerContent(
                        contentIndex = bannerContent,
                        customBanner = customBannerBitmap,
                        now = now,
                        onWallpaper = onWallpaper,
                        onWallpaperSecondary = onWallpaperSecondary,
                        fontAuto = bannerFontAuto,
                        fontWeightManual = bannerFontWeight,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .offset(y = searchTopY),
            ) {
                M3NewTabSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSubmit = { performSearch(query) },
                    onFocusChange = { searchFocused = it },
                    searchBarColor = searchBarColor,
                    searchBarContentColor = searchBarContentColor,
                    searchBarHintColor = searchBarHintColor,
                    searchEngines = searchEngines,
                    currentEngineIndex = currentEngineIndex,
                    onEngineSelected = { index ->
                        val clamped = index.coerceIn(
                            0, searchEngines.lastIndex.coerceAtLeast(0)
                        )
                        currentEngineIndex = clamped
                        prefs.edit()
                            .putInt("default_search_engine", clamped)
                            .putInt("default_search_engine_display", clamped)
                            .apply()
                    },
                    showScanner = scanModuleEnabled,
                    onScannerClick = { onOpenScanner() },
                    horizontalPadding = 24.dp,
                    history = searchHistory,
                    historyModuleEnabled = historyModuleEnabled,
                    onHistoryItemClick = { item ->
                        query = item
                        performSearch(item)
                    },
                    onDeleteHistoryItem = { item ->
                        onDeleteSearchHistoryItem(item)
                    },
                    onClearHistoryClick = {
                        showClearHistoryConfirm = true
                    },
                    suggestions = if (searchFocused) suggestions else emptyList(),
                    onSuggestionClick = { item ->
                        query = item
                        performSearch(item)
                    }
                )
            }

            AnimatedVisibility(
                visible = showQuickUrls && !searchFocused,
                enter = fadeIn(tween(200)) + scaleIn(
                    tween(220, easing = SmoothEase),
                    initialScale = 0.95f,
                ),
                exit = fadeOut(tween(160)) + scaleOut(
                    tween(180, easing = SmoothEase),
                    targetScale = 0.95f,
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = quickUrlsTopY),
            ) {
                M3QuickUrlGrid(
                    allUrls = quickUrls,
                    isEditMode = isEditMode,
                    draggingItemId = draggingItemId,
                    dragAbsPos = dragAbsPos,
                    dragTargetIndex = dragTargetIndex,
                    onGridBoundsChanged = { pos, w, h ->
                        gridWindowPos = pos
                        gridWindowWidthPx = w
                        gridWindowHeightPx = h
                    },
                    onItemDragStart = { url, itemWindowPos ->
                        enterQuickUrlEditMode()
                        draggingItemId = url.id
                        dragAbsPos = itemWindowPos
                        dragOriginalIndex = quickUrls.indexOfFirst { it.id == url.id }
                        dragTargetIndex = dragOriginalIndex
                    },
                    onItemDragMove = { delta ->
                        if (draggingItemId == null) return@M3QuickUrlGrid
                        dragAbsPos += delta

                        val cellWpx = gridWindowWidthPx / QUICK_URL_COLUMNS
                        val cellHpx = with(density) { QUICK_URL_CELL_HEIGHT.toPx() }
                        if (cellWpx <= 0f || cellHpx <= 0f) return@M3QuickUrlGrid

                        val centerX = dragAbsPos.x + cellWpx / 2f
                        val centerY = dragAbsPos.y + cellHpx / 2f
                        val newTarget = computeInsertIndexFromPos(
                            centerX = centerX,
                            centerY = centerY,
                            visibleCount = quickUrls.size,
                        )
                        if (newTarget != dragTargetIndex) dragTargetIndex = newTarget
                    },
                    onItemDragEnd = { handleDragEnd() },
                    onOpen = { url -> onNavigate(url.url) },
                    onAdd = {
                        editingUrl = null
                        showAddEditDialog = true
                    },
                    onEdit = { url ->
                        editingUrl = url
                        showAddEditDialog = true
                    },
                    onMoreClick = { showFolderDialog = true },
                    onDelete = { url -> removeQuickUrl(url) },
                    onCancelEdit = { cancelQuickUrlEdit() },
                    onCommitEdit = { commitQuickUrlEdit() },
                    onWallpaper = onWallpaper,
                    onWallpaperSecondary = onWallpaperSecondary,
                )
            }
        }

        if (showAddEditDialog) {
            M3AddEditQuickUrlDialog(
                initial = editingUrl,
                onDismiss = {
                    showAddEditDialog = false
                    editingUrl = null
                },
                onSave = { title, url, faviconKey ->
                    val existing = editingUrl
                    if (existing == null) {
                        if (quickUrls.size >= MAX_QUICK_URLS) {
                            Toast.makeText(
                                context,
                                QUICK_URL_FULL_HINT,
                                Toast.LENGTH_LONG,
                            ).show()
                        } else {
                            val newUrl = QuickUrl(
                                title = title.ifBlank { url },
                                url = url,
                                faviconKey = faviconKey,
                            )
                            quickUrls.add(newUrl)
                            saveQuickUrlsToPrefs(prefs, quickUrls)
                        }
                    } else {
                        val idx = quickUrls.indexOfFirst { it.id == existing.id }
                        if (idx >= 0) {
                            quickUrls[idx] = existing.copy(
                                title = title.ifBlank { url },
                                url = url,
                                faviconKey = faviconKey ?: existing.faviconKey,
                            )
                            saveQuickUrlsToPrefs(prefs, quickUrls)
                        }
                    }
                    showAddEditDialog = false
                    editingUrl = null
                },
            )
        }

        if (showFolderDialog) {
            M3QuickUrlFolderDialog(
                show = true,
                urls = quickUrls,
                onOpen = { url ->
                    showFolderDialog = false
                    onNavigate(url.url)
                },
                onAdd = {
                    showFolderDialog = false
                    editingUrl = null
                    showAddEditDialog = true
                },
                onEdit = { url ->
                    showFolderDialog = false
                    editingUrl = url
                    showAddEditDialog = true
                },
                onDelete = { url ->
                    quickUrls.removeAll { it.id == url.id }
                    saveQuickUrlsToPrefs(prefs, quickUrls)
                },
                onDismiss = { showFolderDialog = false },
                onWallpaper = onWallpaper,
                onWallpaperSecondary = onWallpaperSecondary,
            )
        }

        if (showBannerFontSheet) {
            M3BannerFontDialog(
                show = true,
                isGreeting = bannerContent == BANNER_GREETING_INDEX,
                fontAuto = bannerFontAuto,
                fontWeight = bannerFontWeight,
                onFontAutoChange = { auto ->
                    bannerFontAuto = auto
                    prefs.edit().putBoolean(KEY_HOME_BANNER_FONT_AUTO, auto).apply()
                },
                onFontWeightChange = { w ->
                    val clamped = w.coerceIn(100, 900)
                    bannerFontWeight = clamped
                    prefs.edit().putInt(KEY_HOME_BANNER_FONT_WEIGHT, clamped).apply()
                },
                onDismiss = { showBannerFontSheet = false },
            )
        }

        if (showWeatherDetailSheet) {
            M3WeatherDetailDialog(
                show = true,
                onRefresh = {
                    scope.launch { fetchWeather(context, forceRefresh = false) }
                },
                onDismiss = { showWeatherDetailSheet = false },
            )
        }

        if (showClearHistoryConfirm) {
            M3AlertDialog(
                onDismissRequest = { showClearHistoryConfirm = false },
                title = { M3Text("清空搜索历史") },
                text = { M3Text("确定要清空全部搜索历史吗？此操作无法撤销。") },
                confirmButton = {
                    M3TextButton(onClick = {
                        onClearSearchHistory()
                        showClearHistoryConfirm = false
                    }) { M3Text("清空") }
                },
                dismissButton = {
                    M3TextButton(onClick = { showClearHistoryConfirm = false }) {
                        M3Text("取消")
                    }
                },
            )
        }

        val lpX = with(density) { longPressPos.x.toDp() }
        val lpY = with(density) { longPressPos.y.toDp() }

        Box(
            modifier = Modifier
                .offset(x = lpX, y = lpY)
                .size(1.dp)
        ) {
            M3DropdownMenu(
                expanded = showBgMenu,
                onDismissRequest = { showBgMenu = false }
            ) {
                M3DropdownMenuItem(
                    text = { M3Text("定制新标签页") },
                    onClick = {
                        showBgMenu = false
                        onOpenNewTabCustomization()
                    }
                )
            }
        }
    }
}

// ============================================================
// Material 3 新标签页 —— 搜索栏
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3NewTabSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    searchBarColor: Color,
    searchBarContentColor: Color,
    searchBarHintColor: Color,
    searchEngines: List<Pair<String, String>>,
    currentEngineIndex: Int,
    onEngineSelected: (Int) -> Unit,
    showScanner: Boolean,
    onScannerClick: () -> Unit,
    horizontalPadding: Dp,
    history: List<String>,
    historyModuleEnabled: Boolean,
    onHistoryItemClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
) {
    var engineMenuExpanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val screenWidth = maxWidth

        val fullWidthFocus = screenWidth - horizontalPadding * 2
        val collapsedWidth = fullWidthFocus * SEARCH_COLLAPSED_WIDTH_FRACTION

        val animatedWidth by animateDpAsState(
            targetValue = if (isFocused) fullWidthFocus else collapsedWidth,
            animationSpec = tween(durationMillis = 340, easing = SmoothEase),
            label = "m3SearchWidth",
        )

        val animatedHeight by animateDpAsState(
            targetValue = if (isFocused) SEARCH_FULL_HEIGHT else SEARCH_COLLAPSED_HEIGHT,
            animationSpec = tween(durationMillis = 340, easing = SmoothEase),
            label = "m3SearchHeight",
        )

        val contentAlpha by animateFloatAsState(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = tween(durationMillis = 200, easing = SmoothEase),
            label = "m3SearchContentAlpha",
        )

        val iconSidePadding = ((animatedHeight - SEARCH_ICON_SIZE) / 2).coerceAtLeast(0.dp)
        val searchShape = RoundedCornerShape(animatedHeight / 2)

        var historyVisible by remember { mutableStateOf(false) }
        LaunchedEffect(isFocused, history.isNotEmpty(), query, historyModuleEnabled) {
            val shouldShow = historyModuleEnabled &&
                    isFocused &&
                    history.isNotEmpty() &&
                    query.isBlank()
            if (shouldShow) {
                delay(150L)
                val stillShould = historyModuleEnabled &&
                        isFocused &&
                        history.isNotEmpty() &&
                        query.isBlank()
                historyVisible = stillShould
            } else {
                historyVisible = false
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = searchBarContentColor),
                cursorBrush = SolidColor(searchBarContentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .clip(searchShape)
                    .background(searchBarColor)
                    .onFocusChanged { focusState ->
                        val wasFocused = isFocused
                        isFocused = focusState.isFocused
                        onFocusChange(focusState.isFocused)
                        if (wasFocused && !focusState.isFocused) {
                            onQueryChange("")
                        }
                    },
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = iconSidePadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(SEARCH_ICON_SIZE)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { engineMenuExpanded = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                M3Icon(
                                    imageVector = M3Icons.Filled.Search,
                                    contentDescription = "切换搜索引擎",
                                    tint = searchBarContentColor,
                                    modifier = Modifier.size(25.dp),
                                )
                            }
                            M3DropdownMenu(
                                expanded = engineMenuExpanded,
                                onDismissRequest = { engineMenuExpanded = false },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(M3MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                val presets = searchEngines.take(6)
                                val customs = searchEngines.drop(6)

                                presets.forEachIndexed { index, (name, _) ->
                                    M3DropdownMenuItem(
                                        text = {
                                            M3Text(
                                                text = name,
                                                color = if (index == currentEngineIndex) {
                                                    M3MaterialTheme.colorScheme.primary
                                                } else {
                                                    M3MaterialTheme.colorScheme.onSurface
                                                },
                                            )
                                        },
                                        onClick = {
                                            onEngineSelected(index)
                                            engineMenuExpanded = false
                                        },
                                    )
                                }
                                if (customs.isNotEmpty()) {
                                    androidx.compose.material3.HorizontalDivider()
                                    customs.forEachIndexed { i, (name, _) ->
                                        val actualIndex = i + 6
                                        M3DropdownMenuItem(
                                            text = {
                                                M3Text(
                                                    text = name,
                                                    color = if (actualIndex == currentEngineIndex) {
                                                        M3MaterialTheme.colorScheme.primary
                                                    } else {
                                                        M3MaterialTheme.colorScheme.onSurface
                                                    },
                                                )
                                            },
                                            onClick = {
                                                onEngineSelected(actualIndex)
                                                engineMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .graphicsLayer { alpha = contentAlpha },
                            ) {
                                if (query.isEmpty()) {
                                    Text(
                                        "搜索或输入网址",
                                        fontSize = 15.sp,
                                        color = searchBarHintColor,
                                    )
                                }
                                inner()
                            }
                            Text(
                                text = "搜索",
                                fontSize = 15.sp,
                                color = searchBarHintColor,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .graphicsLayer { alpha = 1f - contentAlpha },
                            )
                        }

                        if (isFocused && query.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { alpha = contentAlpha }
                                    .size(SEARCH_ICON_SIZE)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { onQueryChange("") },
                                contentAlignment = Alignment.Center,
                            ) {
                                M3Icon(
                                    imageVector = M3Icons.Filled.Close,
                                    contentDescription = "清空",
                                    tint = searchBarContentColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showScanner,
                            enter = fadeIn(tween(160)) + scaleIn(
                                tween(200, easing = SmoothEase),
                                initialScale = 0.7f,
                            ),
                            exit = fadeOut(tween(120)) + scaleOut(
                                tween(160, easing = SmoothEase),
                                targetScale = 0.7f,
                            ),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(SEARCH_ICON_SIZE)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { onScannerClick() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    M3Icon(
                                        imageVector = MiuixIcons.Scan,
                                        contentDescription = "扫一扫",
                                        tint = searchBarContentColor,
                                        modifier = Modifier.size(25.dp),
                                    )
                                }
                            }
                        }
                    }
                },
            )

            val showSuggestionPanel = query.isNotBlank() && isFocused && suggestions.isNotEmpty()
            AnimatedVisibility(
                visible = showSuggestionPanel,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 220, easing = SmoothEase),
                ) + expandVertically(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    expandFrom = Alignment.Top,
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 280, easing = SmoothEase),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 140),
                ) + shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    shrinkTowards = Alignment.Top,
                ) + scaleOut(
                    animationSpec = tween(durationMillis = 200, easing = SmoothEase),
                    targetScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f),
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(10.dp))
                    M3Surface(
                        modifier = Modifier.width(animatedWidth),
                        shape = RoundedCornerShape(20.dp),
                        color = searchBarColor,
                        contentColor = searchBarContentColor,
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            contentPadding = PaddingValues(vertical = 6.dp),
                        ) {
                            items(
                                count = suggestions.size,
                                key = { idx -> "m3_sug_${suggestions[idx]}" },
                            ) { idx ->
                                val item = suggestions[idx]
                                M3HistoryRow(
                                    text = item,
                                    textColor = searchBarContentColor,
                                    iconTint = searchBarHintColor,
                                    icon = MiuixIcons.Search,
                                    onClick = { onSuggestionClick(item) },
                                    onDelete = null,
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = historyVisible && query.isBlank(),
                enter = fadeIn(tween(180)) + expandVertically(
                    animationSpec = tween(220, easing = SmoothEase),
                    expandFrom = Alignment.Top,
                ),
                exit = fadeOut(tween(140)) + shrinkVertically(
                    animationSpec = tween(180, easing = SmoothEase),
                    shrinkTowards = Alignment.Top,
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(10.dp))
                    M3Surface(
                        modifier = Modifier.width(animatedWidth),
                        shape = RoundedCornerShape(20.dp),
                        color = searchBarColor,
                        contentColor = searchBarContentColor,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 4.dp,
                                        top = 2.dp,
                                        bottom = 0.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                M3Text(
                                    text = "搜索历史",
                                    style = M3MaterialTheme.typography.labelMedium,
                                    color = searchBarHintColor,
                                    modifier = Modifier.weight(1f),
                                )
                                M3TextButton(
                                    onClick = onClearHistoryClick,
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 0.dp,
                                    ),
                                    modifier = Modifier.height(32.dp),
                                ) {
                                    M3Text(
                                        text = "清空",
                                        style = M3MaterialTheme.typography.labelMedium,
                                        color = M3MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp),
                            ) {
                                items(
                                    count = history.size,
                                    key = { idx -> "m3_hist_${history[idx]}" },
                                ) { idx ->
                                    val item = history[idx]
                                    M3HistoryRow(
                                        text = item,
                                        textColor = searchBarContentColor,
                                        iconTint = searchBarHintColor,
                                        icon = MiuixIcons.Recent,
                                        onClick = { onHistoryItemClick(item) },
                                        onDelete = { onDeleteHistoryItem(item) },
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

@Composable
private fun M3HistoryRow(
    text: String,
    textColor: Color,
    iconTint: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        M3Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        M3Text(
            text = text,
            style = M3MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(4.dp))
        if (onDelete != null) {
            M3IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                M3Icon(
                    imageVector = M3Icons.Filled.Close,
                    contentDescription = "删除",
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ============================================================
// Material 3 新标签页 —— 快捷网址网格
// ============================================================

@Composable
private fun M3QuickUrlGrid(
    allUrls: List<QuickUrl>,
    isEditMode: Boolean,
    draggingItemId: String?,
    dragAbsPos: Offset,
    dragTargetIndex: Int,
    onGridBoundsChanged: (windowPos: Offset, wPx: Float, hPx: Float) -> Unit,
    onItemDragStart: (QuickUrl, Offset) -> Unit,
    onItemDragMove: (Offset) -> Unit,
    onItemDragEnd: () -> Unit,
    onOpen: (QuickUrl) -> Unit,
    onAdd: () -> Unit,
    onEdit: (QuickUrl) -> Unit,
    onMoreClick: () -> Unit,
    onDelete: (QuickUrl) -> Unit,
    onCancelEdit: () -> Unit,
    onCommitEdit: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val columns = QUICK_URL_COLUMNS
        val cellWidth = maxWidth / columns
        val cellHeight = QUICK_URL_CELL_HEIGHT
        val cellWpx = with(density) { cellWidth.toPx() }
        val cellHpx = with(density) { cellHeight.toPx() }

        val maxCells = (QUICK_URL_COLUMNS * QUICK_URL_MAX_ROWS).coerceAtLeast(columns)
        val totalCells = allUrls.size + 1
        val needsMore = totalCells > maxCells

        val visibleCount = if (needsMore) (maxCells - 1).coerceAtLeast(0)
        else allUrls.size

        val totalVisibleCells = visibleCount + 1
        val displayRows = (totalVisibleCells + columns - 1) / columns
        val gridHeight = cellHeight * displayRows

        val displayOrder: List<String> = remember(
            allUrls.toList(), draggingItemId, dragTargetIndex
        ) {
            val ids = allUrls.map { it.id }.toMutableList()
            val dragId = draggingItemId
            if (dragId == null || dragTargetIndex < 0) return@remember ids
            val from = ids.indexOf(dragId)
            if (from < 0) return@remember ids
            ids.removeAt(from)
            val adjusted = dragTargetIndex.coerceIn(0, ids.size)
            ids.add(adjusted, dragId)
            ids
        }

        val visibleItems: List<QuickUrl> = remember(displayOrder, visibleCount) {
            displayOrder
                .take(visibleCount)
                .mapNotNull { id -> allUrls.find { it.id == id } }
        }

        var gridWindowPosLocal by remember {
            mutableStateOf(Offset.Zero)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
                .onGloballyPositioned { coords ->
                    val bounds = coords.boundsInWindow()
                    gridWindowPosLocal = Offset(
                        bounds.left, bounds.top
                    )
                    onGridBoundsChanged(
                        gridWindowPosLocal,
                        bounds.width,
                        bounds.height,
                    )
                },
        ) {
            visibleItems.forEachIndexed { index, url ->
                key(url.id) {
                    val isDragging = url.id == draggingItemId

                    val targetGridX = cellWpx * (index % columns)
                    val targetGridY = cellHpx * (index / columns)

                    val dragGridX = dragAbsPos.x - gridWindowPosLocal.x
                    val dragGridY = dragAbsPos.y - gridWindowPosLocal.y

                    val animX by animateFloatAsState(
                        targetValue = if (isDragging) dragGridX else targetGridX,
                        animationSpec = if (isDragging) snap()
                        else tween(200, easing = SmoothEase),
                        label = "m3_quick_x_${url.id}",
                    )
                    val animY by animateFloatAsState(
                        targetValue = if (isDragging) dragGridY else targetGridY,
                        animationSpec = if (isDragging) snap()
                        else tween(200, easing = SmoothEase),
                        label = "m3_quick_y_${url.id}",
                    )

                    var itemWindowPos by remember(url.id) {
                        mutableStateOf(Offset.Zero)
                    }

                    val scale = if (isDragging) 1.05f else 1f

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(animX.roundToInt(), animY.roundToInt())
                            }
                            .size(cellWidth, cellHeight)
                            .zIndex(if (isDragging) 10f else 0f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .onGloballyPositioned { coords ->
                                try {
                                    itemWindowPos = coords.positionInWindow()
                                } catch (_: Exception) {}
                            }
                            .pointerInput(url.id, isEditMode) {
                                detectTapGestures(
                                    onTap = {
                                        if (isEditMode) onEdit(url) else onOpen(url)
                                    },
                                )
                            }
                            .pointerInput(url.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        onItemDragStart(url, itemWindowPos)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onItemDragMove(dragAmount)
                                    },
                                    onDragEnd = { onItemDragEnd() },
                                    onDragCancel = { onItemDragEnd() },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        M3QuickUrlItem(
                            url = url,
                            isEditMode = isEditMode,
                            onWallpaper = onWallpaper,
                            onWallpaperSecondary = onWallpaperSecondary,
                            onClick = {},
                            onLongClick = {},
                            onDelete = { onDelete(url) },
                        )
                    }
                }
            }

            val lastIndex = visibleCount
            val lastTargetX = cellWpx * (lastIndex % columns)
            val lastTargetY = cellHpx * (lastIndex / columns)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(lastTargetX.roundToInt(), lastTargetY.roundToInt())
                    }
                    .size(cellWidth, cellHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (needsMore) {
                    M3QuickUrlMoreButton(
                        hiddenUrls = allUrls.drop(visibleCount),
                        onClick = onMoreClick,
                        onWallpaper = onWallpaper,
                        onWallpaperSecondary = onWallpaperSecondary,
                    )
                } else {
                    M3AddQuickUrlButton(
                        onClick = onAdd,
                        onWallpaper = onWallpaper,
                        onWallpaperSecondary = onWallpaperSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(tween(200)) + androidx.compose.animation.scaleIn(
                    tween(220, easing = SmoothEase), initialScale = 0.8f,
                ),
                exit = fadeOut(tween(160)) + androidx.compose.animation.scaleOut(
                    tween(180, easing = SmoothEase), targetScale = 0.8f,
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = 48.dp),
            ) {
                M3Surface(
                    onClick = onCancelEdit,
                    shape = RoundedCornerShape(20.dp),
                    color = M3MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = M3MaterialTheme.colorScheme.onSurface,
                ) {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        M3Text(
                            text = "取消",
                            style = M3MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(tween(200)) + androidx.compose.animation.scaleIn(
                    tween(220, easing = SmoothEase), initialScale = 0.8f,
                ),
                exit = fadeOut(tween(160)) + androidx.compose.animation.scaleOut(
                    tween(180, easing = SmoothEase), targetScale = 0.8f,
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 48.dp),
            ) {
                M3Surface(
                    onClick = onCommitEdit,
                    shape = RoundedCornerShape(20.dp),
                    color = M3MaterialTheme.colorScheme.primary,
                    contentColor = M3MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        M3Text(
                            text = "确定",
                            style = M3MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun M3QuickUrlItem(
    url: QuickUrl,
    isEditMode: Boolean,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val host = remember(url.url) {
        try { url.url.substringAfter("://").substringBefore('/') } catch (_: Exception) { "" }
    }
    val initial = remember(url.title, host) {
        url.title.firstOrNull()?.toString() ?: host.firstOrNull()?.toString() ?: "?"
    }

    val favicon by produceState<Bitmap?>(
        initialValue = url.faviconKey?.let { FaviconStore.get(context, it) },
        url.faviconKey,
    ) {
        value = url.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (favicon != null) {
                    Image(
                        bitmap = favicon!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    M3Text(
                        text = initial,
                        color = onWallpaper,
                        style = M3MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            M3Text(
                text = url.title.ifBlank { host },
                style = M3MaterialTheme.typography.bodySmall,
                color = onWallpaper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn(tween(180)) + androidx.compose.animation.scaleIn(
                tween(200, easing = SmoothEase), initialScale = 0.5f,
            ),
            exit = fadeOut(tween(140)) + androidx.compose.animation.scaleOut(
                tween(160, easing = SmoothEase), targetScale = 0.5f,
            ),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                M3Icon(
                    imageVector = M3Icons.Filled.Close,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun M3AddQuickUrlButton(
    onClick: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(onWallpaper.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            M3Icon(
                imageVector = M3Icons.Filled.Add,
                contentDescription = "添加",
                tint = onWallpaperSecondary,
            )
        }
        Spacer(Modifier.height(6.dp))
        M3Text(
            text = "添加",
            style = M3MaterialTheme.typography.bodySmall,
            color = onWallpaperSecondary,
        )
    }
}

// ============================================================
// Material 3 新标签页 —— 添加/编辑快捷网址对话框
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3AddEditQuickUrlDialog(
    initial: QuickUrl?,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, faviconKey: String?) -> Unit,
) {
    var titleInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var faviconKeyInput by remember { mutableStateOf<String?>(null) }
    var showBookmarkPicker by remember { mutableStateOf(false) }

    LaunchedEffect(initial?.id) {
        titleInput = initial?.title ?: ""
        urlInput = initial?.url ?: ""
        faviconKeyInput = initial?.faviconKey
    }

    if (!showBookmarkPicker) {
        M3AlertDialog(
            onDismissRequest = onDismiss,
            title = { M3Text(if (initial == null) "添加快捷网址" else "编辑快捷网址") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    M3OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { M3Text("名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    M3OutlinedTextField(
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
                M3TextButton(
                    onClick = {
                        var url = urlInput.trim()
                        if (url.isNotBlank()) {
                            if (!url.startsWith("http://") &&
                                !url.startsWith("https://") &&
                                !url.startsWith("ftp://")
                            ) {
                                url = "https://$url"
                            }
                            onSave(titleInput.trim(), url, faviconKeyInput)
                        }
                    },
                    enabled = urlInput.isNotBlank(),
                ) { M3Text(if (initial == null) "添加" else "保存") }
            },
            dismissButton = {
                M3TextButton(onClick = onDismiss) { M3Text("取消") }
            },
        )
    } else {
        M3BookmarkPickerDialog(
            show = true,
            onSelect = { title, url, faviconKey ->
                titleInput = title
                urlInput = url
                faviconKeyInput = faviconKey
                showBookmarkPicker = false
            },
            onDismiss = { showBookmarkPicker = false },
        )
    }
}

// ============================================================
// Material 3 新标签页 —— 字重弹窗
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3BannerFontDialog(
    show: Boolean,
    isGreeting: Boolean,
    fontAuto: Boolean,
    fontWeight: Int,
    onFontAutoChange: (Boolean) -> Unit,
    onFontWeightChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return

    val now = remember { Calendar.getInstance() }
    val displayedWeight = if (fontAuto) continuousFontWeight(now)
    else fontWeight.coerceIn(100, 900)

    val animatedWeight by animateFloatAsState(
        targetValue = displayedWeight.toFloat(),
        animationSpec = tween(
            durationMillis = 200,
            easing = SmoothEase,
        ),
        label = "m3FontWeightSlider",
    )

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text(if (isGreeting) "问候语字重" else "时间字重") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFontAutoChange(!fontAuto) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        M3Text("自动跟随时间")
                        Spacer(Modifier.height(2.dp))
                        M3Text(
                            text = "正午最细、午夜最粗",
                            style = M3MaterialTheme.typography.bodySmall,
                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    M3Switch(
                        checked = fontAuto,
                        onCheckedChange = null,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    M3Text("字重")
                    Spacer(Modifier.weight(1f))
                    M3Text(
                        text = animatedWeight.roundToInt().toString(),
                        style = M3MaterialTheme.typography.bodyMedium,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.material3.Slider(
                    value = animatedWeight,
                    onValueChange = { v ->
                        if (fontAuto) onFontAutoChange(false)
                        onFontWeightChange(v.roundToInt())
                    },
                    valueRange = 100f..900f,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    M3Text(
                        text = "细",
                        style = M3MaterialTheme.typography.labelSmall,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    M3Text(
                        text = "粗",
                        style = M3MaterialTheme.typography.labelSmall,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            M3TextButton(onClick = onDismiss) { M3Text("关闭") }
        },
    )
}

// ============================================================
// Material 3 新标签页 —— 天气详情弹窗
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3WeatherDetailDialog(
    show: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return

    val info = WeatherStateHolder.info
    val loading = WeatherStateHolder.loading
    val error = WeatherStateHolder.error

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var precise by remember {
        initWeatherPreciseFromPrefs(context)
        mutableStateOf(WeatherStateHolder.precise)
    }
    var showMoreMenu by remember { mutableStateOf(false) }

    fun applyPrecise(enabled: Boolean) {
        setWeatherPrecise(context, enabled)
        precise = enabled
        scope.launch { fetchWeather(context, forceRefresh = true) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            applyPrecise(true)
        } else {
            Toast.makeText(context, "需要精确定位权限才能精确到县、市", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(show) {
        if (show) {
            onRefresh()
            while (true) {
                delay(60 * 1000L)
                onRefresh()
            }
        }
    }

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                M3Text(
                    text = "天气",
                    modifier = Modifier.weight(1f),
                )
                Box {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showMoreMenu = true },
                    ) {
                        M3Icon(
                            imageVector = MiuixIcons.More,
                            contentDescription = "更多",
                            tint = M3MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    if (showMoreMenu) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, with(density) { 38.dp.roundToPx() }),
                            onDismissRequest = { showMoreMenu = false },
                            properties = PopupProperties(focusable = true),
                        ) {
                            M3Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                                shadowElevation = 8.dp,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .width(196.dp)
                                        .clickable {
                                            showMoreMenu = false
                                            when {
                                                precise -> applyPrecise(false)
                                                hasFineLocationPermission(context) -> applyPrecise(true)
                                                else -> locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    )
                                                )
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    M3Text(
                                        text = "精确到县、市",
                                        fontSize = 15.sp,
                                        color = M3MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (precise) {
                                        M3Text(
                                            text = "已开启",
                                            fontSize = 12.sp,
                                            color = M3MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
            ) {
                when {
                    info != null -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            item(key = "current") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    M3Text(
                                        text = info.city,
                                        style = M3MaterialTheme.typography.bodyMedium,
                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        M3Text(
                                            text = "${info.currentTemp.roundToInt()}",
                                            style = M3MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        M3Text(
                                            text = "°C",
                                            style = M3MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        M3WeatherIcon(
                                            code = info.currentWeatherCode,
                                            tint = M3MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(top = 12.dp)
                                                .size(36.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    M3Text(info.currentWeatherText)
                                    Spacer(Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                    ) {
                                        M3WeatherMetric("风向", windDirectionToText(info.currentWindDirection))
                                        M3WeatherMetric("风速", "${info.currentWindSpeed.roundToInt()} km/h")
                                        M3WeatherMetric("湿度", "${info.currentHumidity}%")
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    M3HorizontalDivider()
                                }
                            }

                            items(
                                count = info.daily.size,
                                key = { idx -> "m3_wd_${info.daily[idx].date}" },
                            ) { idx ->
                                M3WeatherDailyRow(info.daily[idx])
                            }

                            item(key = "source") {
                                Spacer(Modifier.height(8.dp))
                                M3Text(
                                    text = "数据源：${info.source}",
                                    style = M3MaterialTheme.typography.labelSmall,
                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            M3Text(
                                text = "加载中…",
                                style = M3MaterialTheme.typography.bodyMedium,
                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            M3Text(
                                text = error ?: "暂无天气数据",
                                style = M3MaterialTheme.typography.bodyMedium,
                                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            M3TextButton(onClick = onDismiss) { M3Text("关闭") }
        },
    )
}

@Composable
private fun M3WeatherMetric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        M3Text(
            text = label,
            style = M3MaterialTheme.typography.labelSmall,
            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        M3Text(
            text = value,
            style = M3MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun M3WeatherDailyRow(day: WeatherDaily) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        M3Text(
            text = day.dayLabel,
            style = M3MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(52.dp),
        )
        M3Text(
            text = day.weatherText,
            style = M3MaterialTheme.typography.bodySmall,
            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        M3Text(
            text = "${day.minTemp.roundToInt()}° / ${day.maxTemp.roundToInt()}°",
            style = M3MaterialTheme.typography.bodyMedium,
        )
    }
}

// ============================================================
// Material 3 新标签页 —— "更多"按钮（4 个小图标预览）
// ============================================================

@Composable
private fun M3QuickUrlMoreButton(
    hiddenUrls: List<QuickUrl>,
    onClick: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(onWallpaper.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            M3QuickUrlMoreMiniGrid(hiddenUrls = hiddenUrls.take(4))
        }
        Spacer(Modifier.height(6.dp))
        M3Text(
            text = "更多",
            style = M3MaterialTheme.typography.bodySmall,
            color = onWallpaperSecondary,
        )
    }
}

@Composable
private fun M3QuickUrlMoreMiniGrid(hiddenUrls: List<QuickUrl>) {
    val preview = hiddenUrls.take(4)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            M3QuickUrlMiniIcon(preview.getOrNull(0))
            M3QuickUrlMiniIcon(preview.getOrNull(1))
        }
        Spacer(Modifier.height(3.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            M3QuickUrlMiniIcon(preview.getOrNull(2))
            M3QuickUrlMiniIcon(preview.getOrNull(3))
        }
    }
}

@Composable
private fun M3QuickUrlMiniIcon(url: QuickUrl?) {
    val context = LocalContext.current
    val favicon by produceState<Bitmap?>(
        initialValue = url?.faviconKey?.let { FaviconStore.get(context, it) },
        url?.faviconKey,
    ) {
        value = url?.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Box(
        modifier = Modifier.size(14.dp).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (favicon != null) {
            Image(
                bitmap = favicon!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

// ============================================================
// Material 3 新标签页 —— "更多"文件夹弹窗
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3QuickUrlFolderDialog(
    show: Boolean,
    urls: List<QuickUrl>,
    onOpen: (QuickUrl) -> Unit,
    onAdd: () -> Unit,
    onEdit: (QuickUrl) -> Unit,
    onDelete: (QuickUrl) -> Unit,
    onDismiss: () -> Unit,
    onWallpaper: Color,
    onWallpaperSecondary: Color,
) {
    if (!show) return

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("全部快捷网址（${urls.size}）") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        count = urls.size,
                        key = { idx -> urls[idx].id },
                    ) { idx ->
                        val url = urls[idx]
                        M3QuickUrlFolderRow(
                            url = url,
                            onOpen = { onOpen(url) },
                            onEdit = { onEdit(url) },
                            onDelete = { onDelete(url) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            M3TextButton(onClick = onAdd) { M3Text("添加") }
        },
        dismissButton = {
            M3TextButton(onClick = onDismiss) { M3Text("关闭") }
        },
    )
}

@Composable
private fun M3QuickUrlFolderRow(
    url: QuickUrl,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val host = remember(url.url) {
        try { url.url.substringAfter("://").substringBefore('/') } catch (_: Exception) { "" }
    }
    val initial = remember(url.title, host) {
        url.title.firstOrNull()?.toString() ?: host.firstOrNull()?.toString() ?: "?"
    }

    val favicon by produceState<Bitmap?>(
        initialValue = url.faviconKey?.let { FaviconStore.get(context, it) },
        url.faviconKey,
    ) {
        value = url.faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (favicon != null) {
                Image(
                    bitmap = favicon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                M3Text(
                    text = initial,
                    style = M3MaterialTheme.typography.titleMedium,
                    color = M3MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            M3Text(
                text = url.title.ifBlank { host },
                style = M3MaterialTheme.typography.bodyLarge,
                color = M3MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            M3Text(
                text = url.url,
                style = M3MaterialTheme.typography.bodySmall,
                color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        M3IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp),
        ) {
            M3Icon(
                imageVector = MiuixIcons.Rename,
                contentDescription = "编辑",
                tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        M3IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp),
        ) {
            M3Icon(
                imageVector = M3Icons.Filled.Close,
                contentDescription = "删除",
                tint = M3MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ============================================================
// Material 3 新标签页 —— 从书签选择对话框
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3BookmarkPickerDialog(
    show: Boolean,
    onSelect: (title: String, url: String, faviconKey: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    val bookmarks = remember(show) {
        if (show) loadFlatBookmarks(prefs) else emptyList()
    }

    M3AlertDialog(
        onDismissRequest = onDismiss,
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
                            key = { idx -> "m3_bm_${bookmarks[idx].url}" },
                        ) { idx ->
                            val bm = bookmarks[idx]
                            M3BookmarkPickerRow(
                                title = bm.title,
                                url = bm.url,
                                faviconKey = bm.faviconKey,
                                onClick = {
                                    onSelect(bm.title, bm.url, bm.faviconKey)
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            M3TextButton(onClick = onDismiss) { M3Text("取消") }
        },
    )
}

@Composable
private fun M3BookmarkPickerRow(
    title: String,
    url: String,
    faviconKey: String?,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val bmFavicon by produceState<Bitmap?>(
        initialValue = faviconKey?.let { FaviconStore.get(context, it) },
        faviconKey,
    ) {
        value = faviconKey?.let {
            withContext(Dispatchers.IO) { FaviconStore.get(context, it) }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (bmFavicon != null) {
                Image(
                    bitmap = bmFavicon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                M3Icon(
                    imageVector = MiuixIcons.Favorites,
                    contentDescription = null,
                    tint = M3MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
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