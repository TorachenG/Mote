package com.torachen.mote.ui

import android.content.Context
import org.json.JSONArray

/** 浏览器标识（UA）预设 */
internal data class UaPreset(val name: String, val ua: String?)

internal object UaStore {
    private const val PREFS = "mote_prefs"
    private const val KEY_PRESET = "ua_preset_index"
    private const val KEY_CUSTOM_LIST = "ua_custom_list"
    private const val KEY_CUSTOM_SELECTED = "ua_custom_selected"
    private const val KEY_DESKTOP = "ua_desktop_index"

    private val CHROME_ANDROID =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Mobile Safari/537.36"
    private val CHROME_ANDROID_TABLET =
        "Mozilla/5.0 (Linux; Android 14; SM-X710) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36"
    private val CHROME_WINDOWS =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36"
    private val IE11 =
        "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko"
    private val MAC_SAFARI =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
            "Version/17.4 Safari/605.1.15"
    private val IPHONE =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
            "Version/17.4 Mobile/15E148 Safari/604.1"
    private val IPAD =
        "Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
            "Version/17.4 Mobile/15E148 Safari/604.1"
    private val SYMBIAN =
        "Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaN8-00/012.002; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) " +
            "AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.3.0 Mobile Safari/533.4 3gpp-gba"

    val presets = listOf(
        UaPreset("默认", null),
        UaPreset("Android（手机）", CHROME_ANDROID),
        UaPreset("Android（平板）", CHROME_ANDROID_TABLET),
        UaPreset("Windows（Chrome）", CHROME_WINDOWS),
        UaPreset("Windows（IE 11）", IE11),
        UaPreset("MacOS", MAC_SAFARI),
        UaPreset("iPhone", IPHONE),
        UaPreset("iPad", IPAD),
        UaPreset("Symbian（塞班）", SYMBIAN),
    )

    /** 电脑模式可选 UA（0=默认，1=Win Chrome，2=IE11，3=MacOS） */
    val desktopOptions = listOf(
        "默认",
        "Windows（Chrome）",
        "Windows（IE 11）",
        "MacOS",
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun presetIndex(context: Context): Int =
        prefs(context).getInt(KEY_PRESET, 0).coerceIn(0, presets.lastIndex)

    fun setPresetIndex(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_PRESET, index).apply()
    }

    fun customList(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_CUSTOM_LIST, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    fun setCustomList(context: Context, list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_CUSTOM_LIST, arr.toString()).apply()
    }

    fun customSelected(context: Context): String? =
        prefs(context).getString(KEY_CUSTOM_SELECTED, null)

    fun setCustomSelected(context: Context, ua: String?) {
        prefs(context).edit().putString(KEY_CUSTOM_SELECTED, ua).apply()
    }

    fun desktopIndex(context: Context): Int =
        prefs(context).getInt(KEY_DESKTOP, 0).coerceIn(0, desktopOptions.lastIndex)

    fun setDesktopIndex(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_DESKTOP, index).apply()
    }

    fun desktopUa(context: Context): String? = when (desktopIndex(context)) {
        1 -> CHROME_WINDOWS
        2 -> IE11
        3 -> MAC_SAFARI
        else -> null
    }

    /** 当前生效的 UA；null 表示使用系统默认 */
    fun effectiveUa(context: Context, desktopMode: Boolean): String? {
        if (desktopMode) {
            return desktopUa(context) ?: presets.getOrNull(presetIndex(context))?.ua
        }
        customSelected(context)?.takeIf { it.isNotBlank() }?.let { return it }
        return presets.getOrNull(presetIndex(context))?.ua
    }

    fun previewName(context: Context): String {
        customSelected(context)?.takeIf { it.isNotBlank() }?.let { return "自定义" }
        return presets.getOrNull(presetIndex(context))?.name ?: "默认"
    }
}
