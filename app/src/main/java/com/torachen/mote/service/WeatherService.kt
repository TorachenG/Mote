@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.service

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================
// 天气数据模型
// ============================================================

internal data class WeatherDaily(
    val date: String,
    val dayLabel: String,
    val weatherCode: Int,
    val weatherText: String,
    val maxTemp: Double,
    val minTemp: Double,
    val windSpeedMax: Double,
    val windDirection: Int,
    val precipitation: Double,
)

internal data class WeatherInfo(
    val city: String,
    val currentTemp: Double,
    val currentWeatherCode: Int,
    val currentWeatherText: String,
    val currentWindSpeed: Double,
    val currentWindDirection: Int,
    val currentHumidity: Int,
    val source: String,
    val updatedAt: Long,
    val daily: List<WeatherDaily>,
)

// ============================================================
// 全局状态
// ============================================================

internal object WeatherStateHolder {
    var info by mutableStateOf<WeatherInfo?>(null)
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var lastFetchAt by mutableLongStateOf(0L)

    const val CACHE_TTL_MS = 10 * 60 * 1000L

    fun isFresh(): Boolean =
        info != null && (System.currentTimeMillis() - lastFetchAt) < CACHE_TTL_MS
}

// ============================================================
// 天气代码映射（WMO Weather interpretation codes）
// ============================================================

internal fun weatherCodeToText(code: Int): String = when (code) {
    0 -> "晴"
    1 -> "晴间多云"
    2 -> "局部多云"
    3 -> "阴"
    45, 48 -> "雾"
    51, 53, 55 -> "毛毛雨"
    56, 57 -> "冻毛毛雨"
    61 -> "小雨"
    63 -> "中雨"
    65 -> "大雨"
    66, 67 -> "冻雨"
    71 -> "小雪"
    73 -> "中雪"
    75 -> "大雪"
    77 -> "雪粒"
    80 -> "小阵雨"
    81 -> "中阵雨"
    82 -> "强阵雨"
    85 -> "小阵雪"
    86 -> "大阵雪"
    95 -> "雷阵雨"
    96, 99 -> "雷暴冰雹"
    else -> "未知"
}

internal fun windDirectionToText(deg: Int): String {
    val dirs = listOf("北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风")
    val normalized = ((deg % 360) + 360) % 360
    val idx = ((normalized + 22.5) / 45.0).toInt() % 8
    return dirs[idx]
}

// ============================================================
// 网络工具
// ============================================================

private fun httpGetText(urlStr: String, timeoutMs: Int = 8000): String {
    val conn = URL(urlStr).openConnection() as HttpURLConnection
    conn.connectTimeout = timeoutMs
    conn.readTimeout = timeoutMs
    conn.requestMethod = "GET"
    conn.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
    )
    conn.setRequestProperty("Accept", "application/json")
    return try {
        if (conn.responseCode !in 200..299) {
            throw RuntimeException("HTTP ${conn.responseCode}")
        }
        conn.inputStream.bufferedReader().use { it.readText() }
    } finally {
        conn.disconnect()
    }
}

// ============================================================
// 定位（IP 定位，零权限）
// ============================================================

private data class LocationInfo(
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

private fun locateByIp(): LocationInfo? {
    try {
        val body = httpGetText("https://ipapi.co/json/")
        val obj = JSONObject(body)
        val lat = obj.optDouble("latitude", Double.NaN)
        val lng = obj.optDouble("longitude", Double.NaN)
        val city = obj.optString("city", "").ifBlank {
            obj.optString("region", "")
        }
        if (!lat.isNaN() && !lng.isNaN()) {
            return LocationInfo(city.ifBlank { "当前位置" }, lat, lng)
        }
    } catch (_: Exception) {}

    try {
        val body = httpGetText(
            "http://ip-api.com/json/?lang=zh-CN&fields=status,country,regionName,city,lat,lon"
        )
        val obj = JSONObject(body)
        if (obj.optString("status", "") == "success") {
            val lat = obj.optDouble("lat", Double.NaN)
            val lng = obj.optDouble("lon", Double.NaN)
            val city = obj.optString("city", "").ifBlank {
                obj.optString("regionName", "")
            }
            if (!lat.isNaN() && !lng.isNaN()) {
                return LocationInfo(city.ifBlank { "当前位置" }, lat, lng)
            }
        }
    } catch (_: Exception) {}

    return null
}

// ============================================================
// 磁盘缓存（冷启动时直接读旧数据，避免闪"获取中"）
// ============================================================

private const val KEY_WEATHER_CACHE = "weather_cache_v1"

private fun weatherInfoToJson(info: WeatherInfo): JSONObject {
    return JSONObject().apply {
        put("city", info.city)
        put("currentTemp", info.currentTemp)
        put("currentWeatherCode", info.currentWeatherCode)
        put("currentWeatherText", info.currentWeatherText)
        put("currentWindSpeed", info.currentWindSpeed)
        put("currentWindDirection", info.currentWindDirection)
        put("currentHumidity", info.currentHumidity)
        put("source", info.source)
        put("updatedAt", info.updatedAt)
        val arr = JSONArray()
        info.daily.forEach { d ->
            arr.put(JSONObject().apply {
                put("date", d.date)
                put("dayLabel", d.dayLabel)
                put("weatherCode", d.weatherCode)
                put("weatherText", d.weatherText)
                put("maxTemp", d.maxTemp)
                put("minTemp", d.minTemp)
                put("windSpeedMax", d.windSpeedMax)
                put("windDirection", d.windDirection)
                put("precipitation", d.precipitation)
            })
        }
        put("daily", arr)
    }
}

private fun jsonToWeatherInfo(obj: JSONObject): WeatherInfo? {
    return try {
        val arr = obj.optJSONArray("daily")
        val daily = mutableListOf<WeatherDaily>()
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val d = arr.optJSONObject(i) ?: continue
                daily.add(WeatherDaily(
                    date = d.optString("date", ""),
                    dayLabel = d.optString("dayLabel", ""),
                    weatherCode = d.optInt("weatherCode", 0),
                    weatherText = d.optString("weatherText", ""),
                    maxTemp = d.optDouble("maxTemp", 0.0),
                    minTemp = d.optDouble("minTemp", 0.0),
                    windSpeedMax = d.optDouble("windSpeedMax", 0.0),
                    windDirection = d.optInt("windDirection", 0),
                    precipitation = d.optDouble("precipitation", 0.0),
                ))
            }
        }
        WeatherInfo(
            city = obj.optString("city", ""),
            currentTemp = obj.optDouble("currentTemp", 0.0),
            currentWeatherCode = obj.optInt("currentWeatherCode", 0),
            currentWeatherText = obj.optString("currentWeatherText", ""),
            currentWindSpeed = obj.optDouble("currentWindSpeed", 0.0),
            currentWindDirection = obj.optInt("currentWindDirection", 0),
            currentHumidity = obj.optInt("currentHumidity", 0),
            source = obj.optString("source", ""),
            updatedAt = obj.optLong("updatedAt", 0L),
            daily = daily,
        )
    } catch (_: Exception) { null }
}

private val cacheInitialized = java.util.concurrent.atomic.AtomicBoolean(false)

internal fun initWeatherCacheFromDisk(context: Context) {
    if (cacheInitialized.getAndSet(true)) return
    if (WeatherStateHolder.info != null) return
    try {
        val prefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WEATHER_CACHE, null) ?: return
        val root = JSONObject(raw)
        val infoObj = root.optJSONObject("info") ?: return
        val lastFetchAt = root.optLong("lastFetchAt", 0L)
        val info = jsonToWeatherInfo(infoObj) ?: return
        WeatherStateHolder.info = info
        WeatherStateHolder.lastFetchAt = lastFetchAt
    } catch (_: Exception) {}
}

private fun saveWeatherCache(context: Context, info: WeatherInfo, lastFetchAt: Long) {
    try {
        val root = JSONObject().apply {
            put("info", weatherInfoToJson(info))
            put("lastFetchAt", lastFetchAt)
        }
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WEATHER_CACHE, root.toString())
            .apply()
    } catch (_: Exception) {}
}

// ============================================================
// 天气获取
// ============================================================

internal suspend fun fetchWeather(context: Context, forceRefresh: Boolean = false) {
    initWeatherCacheFromDisk(context)

    if (!forceRefresh && WeatherStateHolder.isFresh()) return
    if (WeatherStateHolder.loading) return

    WeatherStateHolder.loading = true
    WeatherStateHolder.error = null

    try {
        val result = withContext(Dispatchers.IO) {
            val loc = locateByIp() ?: throw RuntimeException("无法获取位置")
            val url = buildString {
                append("https://api.open-meteo.com/v1/forecast")
                append("?latitude=").append(loc.latitude)
                append("&longitude=").append(loc.longitude)
                append("&current=temperature_2m,relative_humidity_2m,weather_code,")
                append("wind_speed_10m,wind_direction_10m")
                append("&daily=weather_code,temperature_2m_max,temperature_2m_min,")
                append("wind_speed_10m_max,wind_direction_10m_dominant,precipitation_sum")
                append("&timezone=auto")
                append("&past_days=1")
                append("&forecast_days=6")
            }
            val body = httpGetText(url, 10000)
            parseWeatherJson(body, loc.city)
        }
        WeatherStateHolder.info = result
        WeatherStateHolder.lastFetchAt = System.currentTimeMillis()
        WeatherStateHolder.error = null
        saveWeatherCache(context, result, WeatherStateHolder.lastFetchAt)
    } catch (e: Exception) {
        WeatherStateHolder.error = e.message ?: "获取天气失败"
        Log.w("WeatherService", "fetchWeather failed: ${e.message}")
    } finally {
        WeatherStateHolder.loading = false
    }
}

private fun parseWeatherJson(body: String, city: String): WeatherInfo {
    val root = JSONObject(body)

    val current = root.optJSONObject("current")
        ?: throw RuntimeException("缺少 current 字段")
    val currentTemp = current.optDouble("temperature_2m", 0.0)
    val currentHumidity = current.optInt("relative_humidity_2m", 0)
    val currentCode = current.optInt("weather_code", 0)
    val currentWindSpeed = current.optDouble("wind_speed_10m", 0.0)
    val currentWindDir = current.optInt("wind_direction_10m", 0)

    val daily = root.optJSONObject("daily")

    val timeArr = daily?.optJSONArray("time")
    val codeArr = daily?.optJSONArray("weather_code")
    val maxArr = daily?.optJSONArray("temperature_2m_max")
    val minArr = daily?.optJSONArray("temperature_2m_min")
    val windMaxArr = daily?.optJSONArray("wind_speed_10m_max")
    val windDirArr = daily?.optJSONArray("wind_direction_10m_dominant")
    val precipArr = daily?.optJSONArray("precipitation_sum")

    val dailyList = mutableListOf<WeatherDaily>()
    if (timeArr != null) {
        for (i in 0 until timeArr.length()) {
            val dateStr = timeArr.optString(i, "")
            val code = codeArr?.optInt(i, 0) ?: 0
            val maxT = maxArr?.optDouble(i, 0.0) ?: 0.0
            val minT = minArr?.optDouble(i, 0.0) ?: 0.0
            val windMax = windMaxArr?.optDouble(i, 0.0) ?: 0.0
            val windDir = windDirArr?.optInt(i, 0) ?: 0
            val precip = precipArr?.optDouble(i, 0.0) ?: 0.0

            dailyList.add(
                WeatherDaily(
                    date = dateStr,
                    dayLabel = dateToDayLabel(dateStr),
                    weatherCode = code,
                    weatherText = weatherCodeToText(code),
                    maxTemp = maxT,
                    minTemp = minT,
                    windSpeedMax = windMax,
                    windDirection = windDir,
                    precipitation = precip,
                )
            )
        }
    }

    return WeatherInfo(
        city = city,
        currentTemp = currentTemp,
        currentWeatherCode = currentCode,
        currentWeatherText = weatherCodeToText(currentCode),
        currentWindSpeed = currentWindSpeed,
        currentWindDirection = currentWindDir,
        currentHumidity = currentHumidity,
        source = "Open-Meteo · IP 定位",
        updatedAt = System.currentTimeMillis(),
        daily = dailyList,
    )
}

private fun dateToDayLabel(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date: Date = sdf.parse(dateStr) ?: return ""

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val target = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays =
            ((target.timeInMillis - today.timeInMillis) / (24L * 3600L * 1000L)).toInt()

        when (diffDays) {
            -1 -> "昨天"
            0 -> "今天"
            1 -> "明天"
            else -> {
                val cal = Calendar.getInstance().apply { time = date }
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "周一"
                    Calendar.TUESDAY -> "周二"
                    Calendar.WEDNESDAY -> "周三"
                    Calendar.THURSDAY -> "周四"
                    Calendar.FRIDAY -> "周五"
                    Calendar.SATURDAY -> "周六"
                    else -> "周日"
                }
            }
        }
    } catch (_: Exception) { "" }
}