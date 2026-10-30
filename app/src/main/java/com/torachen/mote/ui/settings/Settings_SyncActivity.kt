@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import com.torachen.mote.MoteAutoSync
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
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
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

internal object CloudBaseConfig {
    const val ENV_ID = "mote-sync-d9geb238o1c1ea40e"
    // ★ 账号与同步数据都走这个云函数（公开路由），不再使用 CloudBase 身份认证
    const val FUNCTION_URL =
        "https://mote-sync-d9geb238o1c1ea40e-1515028654.ap-shanghai.app.tcloudbase.com/moteauth"
}

// ============================================================
// 云同步引擎与数据层（CloudBase HTTP API + 混合加密）
// ============================================================
internal object MoteCloudSyncManager {
    const val KEY_LAST_SYNC = "sync_last_modified"
    const val KEY_CLOUDBASE_ACCESS_TOKEN = "cloudbase_access_token"
    const val KEY_CLOUDBASE_REFRESH_TOKEN = "cloudbase_refresh_token"
    const val KEY_CLOUDBASE_USER_ID = "cloudbase_user_id"
    const val KEY_E2EE_ENABLED = "sync_e2ee_enabled"
    const val KEY_FALLBACK_PASS = "sync_fallback_pass"
    const val KEY_E2EE_DERIVED_KEY = "sync_e2ee_derived_key"

    const val KEY_WEBDAV_URL = "sync_webdav_url"
    const val KEY_WEBDAV_USER = "sync_webdav_user"
    const val KEY_WEBDAV_PASS = "sync_webdav_pass"

    const val CAT_BOOKMARKS = "bookmarks"
    const val CAT_BROWSE_HISTORY = "browse_history"
    const val CAT_SEARCH_HISTORY = "search_history"
    const val CAT_QUICK_URLS = "quick_urls"
    const val CAT_SETTINGS = "settings"
    const val CAT_PASSWORDS = "passwords"

    data class WebDavConfig(val url: String, val user: String, val pass: String)
    data class SyncData(val lastModified: Long, val payload: JSONObject)

    private val httpClient = OkHttpClient.Builder().build()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun getLocalPass(prefs: SharedPreferences): String {
        var pass = prefs.getString(KEY_FALLBACK_PASS, null)
        if (pass.isNullOrBlank()) {
            pass = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_FALLBACK_PASS, pass).apply()
        }
        return pass
    }

    fun getWebDavConfig(prefs: SharedPreferences): WebDavConfig? {
        val url = prefs.getString(KEY_WEBDAV_URL, "") ?: ""
        val user = prefs.getString(KEY_WEBDAV_USER, "") ?: ""
        val pass = prefs.getString(KEY_WEBDAV_PASS, "") ?: ""
        if (url.isBlank() || user.isBlank() || pass.isBlank()) return null
        return WebDavConfig(url, user, pass)
    }

    fun updateE2EEKey(username: String, password: String, prefs: SharedPreferences) {
        try {
            val salt = "MoteE2EESalt".toByteArray()
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec((username + password).toCharArray(), salt, 10000, 256)
            val secretKey = factory.generateSecret(spec)
            val keyBase64 = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
            prefs.edit().putString(KEY_E2EE_DERIVED_KEY, keyBase64).apply()
        } catch (_: Exception) {}
    }

    /** 端到端密钥：由「用户名 + 密码」派生，云端无法还原 */
    private fun e2eeKey(prefs: SharedPreferences): SecretKeySpec? {
        val keyBase64 = prefs.getString(KEY_E2EE_DERIVED_KEY, "") ?: ""
        if (keyBase64.isBlank()) return null
        return try {
            SecretKeySpec(Base64.decode(keyBase64, Base64.NO_WRAP), "AES")
        } catch (_: Exception) {
            null
        }
    }

    /** 普通加密密钥：由账号 uid 派生，换设备登录后即可恢复（云端理论上可解） */
    private fun normalKey(prefs: SharedPreferences): SecretKeySpec? {
        val uid = prefs.getString(KEY_CLOUDBASE_USER_ID, "") ?: ""
        if (uid.isBlank()) return null
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(uid.toCharArray(), "MoteNormalSalt".toByteArray(), 10000, 256)
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } catch (_: Exception) {
            null
        }
    }

    private fun encryptWith(key: SecretKeySpec, plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun tryDecrypt(key: SecretKeySpec, encryptedBase64: String): String? {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < 12) return null
            val iv = combined.copyOfRange(0, 12)
            val cipherText = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 加密方式由「高级数据保护」开关决定：
     * - 开启：端到端加密，密钥由账号密码派生，云端无法解密
     * - 关闭（默认）：普通加密，密钥由 uid 派生，云端可解，换设备同样能恢复
     */
    fun encrypt(plainText: String, prefs: SharedPreferences): String {
        val useE2EE = prefs.getBoolean(KEY_E2EE_ENABLED, false)
        val key = (if (useE2EE) e2eeKey(prefs) else normalKey(prefs))
            ?: throw Exception("缺少加密密钥，请重新登录后再同步")
        return encryptWith(key, plainText)
    }

    fun decrypt(encryptedBase64: String, prefs: SharedPreferences): String? {
        val useE2EE = prefs.getBoolean(KEY_E2EE_ENABLED, false)
        val primary = if (useE2EE) e2eeKey(prefs) else normalKey(prefs)
        val secondary = if (useE2EE) normalKey(prefs) else e2eeKey(prefs)

        if (primary != null) tryDecrypt(primary, encryptedBase64)?.let { return it }
        if (secondary != null) tryDecrypt(secondary, encryptedBase64)?.let { return it }

        // 兼容旧版本：本机随机口令 + 随机盐
        return try {
            val pass = getLocalPass(prefs)
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < 28) return null
            val salt = combined.copyOfRange(0, 16)
            val iv = combined.copyOfRange(16, 28)
            val cipherText = combined.copyOfRange(28, combined.size)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(pass.toCharArray(), salt, 10000, 256)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    /** 从已保存的 token 里解析用户名（老版本登录没存用户名时用） */
    fun usernameFromToken(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return ""
            val payload = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            ).toString(Charsets.UTF_8)
            JSONObject(payload).optString("username", "")
        } catch (_: Exception) {
            ""
        }
    }

    fun buildLocalData(prefs: SharedPreferences): JSONObject {
        val root = JSONObject()
        root.put("fallback_key", getLocalPass(prefs))

        if (prefs.getBoolean("sync_cat_$CAT_BOOKMARKS", true)) root.put("bookmarks_v2", prefs.getString("bookmarks_v2", "[]"))
        if (prefs.getBoolean("sync_cat_$CAT_BROWSE_HISTORY", false)) root.put("history", prefs.getString("history", "[]"))
        if (prefs.getBoolean("sync_cat_$CAT_SEARCH_HISTORY", false)) root.put("search_history", prefs.getString("search_history", ""))
        if (prefs.getBoolean("sync_cat_$CAT_QUICK_URLS", true)) root.put("home_quick_urls", prefs.getString("home_quick_urls", "[]"))
        if (prefs.getBoolean("sync_cat_$CAT_PASSWORDS", false)) root.put("saved_passwords_v1", prefs.getString("saved_passwords_v1", "[]"))

        if (prefs.getBoolean("sync_cat_$CAT_SETTINGS", true)) {
            val settings = JSONObject()
            val exclude = setOf(
                "privacy_agreed", "ux_improvement_enabled", "sync_webdav_url", "sync_webdav_user",
                "sync_webdav_pass", "sync_master_pass", "sync_last_modified", "session_snapshot",
                "bookmarks_v2", "history", "search_history", "home_quick_urls", "saved_passwords_v1",
                "app_icon_id", "about_last_check_time", "web_saved_zooms_v2", "mote_download_tasks_v1",
                "cloudbase_access_token", "cloudbase_refresh_token", "cloudbase_user_id",
                "sync_e2ee_enabled", "sync_fallback_pass", "sync_e2ee_derived_key",
                "sync_snapshot_v1"
            )
            val types = JSONObject()
            for ((k, v) in prefs.all) {
                if (k !in exclude && !k.startsWith("sync_cat_")) {
                    settings.put(k, v)
                    types.put(k, when (v) {
                        is Long -> "long"
                        is Int -> "int"
                        is Boolean -> "boolean"
                        is Float -> "float"
                        is String -> "string"
                        else -> "string"
                    })
                }
            }
            root.put("settings", settings)
            root.put("settings_types", types)
        }
        return root
    }

    fun applyRemoteData(
        prefs: SharedPreferences,
        data: JSONObject,
        markHistoryRemote: Boolean = true,
    ) {
        val editor = prefs.edit()
        if (prefs.getBoolean("sync_cat_$CAT_BOOKMARKS", true) && data.has("bookmarks_v2")) editor.putString("bookmarks_v2", data.getString("bookmarks_v2"))
        if (prefs.getBoolean("sync_cat_$CAT_BROWSE_HISTORY", false) && data.has("history")) {
            val rawHistory = data.optString("history", "[]")
            if (!markHistoryRemote) {
                editor.putString("history", rawHistory)
            } else {
                try {
                    val arr = org.json.JSONArray(rawHistory)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i)
                        obj?.put("isRemote", true)
                    }
                    editor.putString("history", arr.toString())
                } catch (_: Exception) {
                    editor.putString("history", rawHistory)
                }
            }
        }
        if (prefs.getBoolean("sync_cat_$CAT_SEARCH_HISTORY", false) && data.has("search_history")) editor.putString("search_history", data.getString("search_history"))
        if (prefs.getBoolean("sync_cat_$CAT_QUICK_URLS", true) && data.has("home_quick_urls")) editor.putString("home_quick_urls", data.getString("home_quick_urls"))
        if (prefs.getBoolean("sync_cat_$CAT_PASSWORDS", false) && data.has("saved_passwords_v1")) editor.putString("saved_passwords_v1", data.getString("saved_passwords_v1"))

        if (prefs.getBoolean("sync_cat_$CAT_SETTINGS", true) && data.has("settings")) {
            val settings = data.getJSONObject("settings")
            val types = data.optJSONObject("settings_types")
            settings.keys().forEach { k ->
                val v = settings.opt(k)
                val declared = types?.optString(k, "") ?: ""
                if (declared.isNotEmpty()) {
                    when (declared) {
                        "long" -> editor.putLong(k, (v as? Number)?.toLong() ?: v?.toString()?.toLongOrNull() ?: 0L)
                        "int" -> editor.putInt(k, (v as? Number)?.toInt() ?: v?.toString()?.toIntOrNull() ?: 0)
                        "boolean" -> editor.putBoolean(k, v as? Boolean ?: (v?.toString() == "true"))
                        "float" -> editor.putFloat(k, (v as? Number)?.toFloat() ?: v?.toString()?.toFloatOrNull() ?: 0f)
                        else -> editor.putString(k, v?.toString() ?: "")
                    }
                    return@forEach
                }
                // 按本地已有类型写回，避免 long 键被写成 int 导致读取时 ClassCastException
                when (val existing = prefs.all[k]) {
                    is Boolean -> editor.putBoolean(k, when (v) {
                        is Boolean -> v
                        is String -> v == "true"
                        else -> existing
                    })
                    is Long -> editor.putLong(k, when (v) {
                        is Number -> v.toLong()
                        is String -> v.toLongOrNull() ?: existing
                        else -> existing
                    })
                    is Int -> editor.putInt(k, when (v) {
                        is Number -> v.toInt()
                        is String -> v.toIntOrNull() ?: existing
                        else -> existing
                    })
                    is Float -> editor.putFloat(k, when (v) {
                        is Number -> v.toFloat()
                        is String -> v.toFloatOrNull() ?: existing
                        else -> existing
                    })
                    is String -> editor.putString(k, v?.toString() ?: existing)
                    null -> when (v) {
                        is Boolean -> editor.putBoolean(k, v)
                        is Int -> editor.putInt(k, v)
                        is Long -> editor.putLong(k, v)
                        is Double -> editor.putLong(k, v.toLong())
                        is Float -> editor.putFloat(k, v)
                        is String -> editor.putString(k, v)
                    }
                }
            }
        }
        editor.apply()
    }

    /** 统一调用云函数，返回 (data, error) */
    private fun callFunction(body: JSONObject): Pair<JSONObject?, String?> {
        val request = Request.Builder()
            .url(CloudBaseConfig.FUNCTION_URL)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()
        return try {
            httpClient.newCall(request).execute().use { resp ->
                val text = resp.body?.string() ?: ""
                if (!resp.isSuccessful) return null to "HTTP ${resp.code}"
                val obj = JSONObject(text)
                if (obj.optInt("code", -1) != 0) {
                    return null to obj.optString("message").ifBlank { "请求失败" }
                }
                (obj.optJSONObject("data") ?: JSONObject()) to null
            }
        } catch (e: Exception) {
            null to (e.message ?: "网络异常")
        }
    }

    /** 预热云函数，减少首次同步的冷启动等待（结果忽略） */
    fun warmUp() {
        try {
            callFunction(JSONObject().apply { put("action", "warmup") })
        } catch (_: Exception) {
        }
    }

    /** 注册账号（用户名+密码），成功后返回 true */
    fun registerAccount(username: String, password: String): Pair<Boolean, String?> {
        val (data, err) = callFunction(JSONObject().apply {
            put("action", "register")
            put("username", username)
            put("password", password)
        })
        return (data != null) to err
    }

    /** 登录，成功返回 { token, uid, username } */
    fun loginAccount(username: String, password: String): Pair<JSONObject?, String?> {
        return callFunction(JSONObject().apply {
            put("action", "login")
            put("username", username)
            put("password", password)
        })
    }

    /** 带自动刷新的调用：token 失效时用 refresh token 换新 token 再重试一次 */
    private fun callWithRefresh(body: JSONObject, prefs: SharedPreferences): Pair<JSONObject?, String?> {
        body.put("token", prefs.getString(KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: "")
        val first = callFunction(body)
        if (first.first != null) return first

        val refresh = prefs.getString(KEY_CLOUDBASE_REFRESH_TOKEN, "") ?: ""
        if (refresh.isBlank()) return first

        val (refreshed, _) = callFunction(JSONObject().apply {
            put("action", "refresh")
            put("refresh", refresh)
        })
        val newToken = refreshed?.optString("token").orEmpty()
        if (newToken.isBlank()) return first

        prefs.edit().putString(KEY_CLOUDBASE_ACCESS_TOKEN, newToken).apply()
        body.put("token", newToken)
        return callFunction(body)
    }

    data class PullResult(val content: String, val updatedAt: Long)

    /** 拉取云端密文与云端更新时间；token 过期会自动刷新 */
    fun pullRemote(prefs: SharedPreferences): Pair<PullResult?, String?> {
        val (data, err) = callWithRefresh(JSONObject().apply { put("action", "pull") }, prefs)
        if (data == null) return null to err
        return PullResult(
            content = data.optString("content", ""),
            updatedAt = data.optLong("updated_at", 0L),
        ) to null
    }

    /** 上传密文，返回云端写入时间（毫秒）；token 过期会自动刷新 */
    fun pushRemote(prefs: SharedPreferences, content: String): Pair<Long?, String?> {
        val (data, err) = callWithRefresh(JSONObject().apply {
            put("action", "push")
            put("content", content)
        }, prefs)
        if (data == null) return null to err
        return data.optLong("updated_at", 0L) to null
    }

    fun downloadFile(config: WebDavConfig): String? {
        return try {
            val conn = URL(config.url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            val auth = Base64.encodeToString("${config.user}:${config.pass}".toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", "Basic $auth")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            if (conn.responseCode == 200) {
                val out = ByteArrayOutputStream()
                conn.inputStream.copyTo(out)
                conn.disconnect()
                out.toString("UTF-8")
            } else { conn.disconnect(); null }
        } catch (e: Exception) { null }
    }

    fun uploadFile(config: WebDavConfig, content: String): Boolean {
        return try {
            val conn = URL(config.url).openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            val auth = Base64.encodeToString("${config.user}:${config.pass}".toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", "Basic $auth")
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
            val ok = conn.responseCode in 200..299
            conn.disconnect()
            ok
        } catch (e: Exception) { false }
    }
}

// ============================================================
// 入口
// ============================================================
@Composable
fun SettingsSyncPage(onBack: () -> Unit, onOpenBrowserSync: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsSyncMiuix(onBack, onOpenBrowserSync)
        UiMode.Material -> SettingsSyncMaterial(onBack, onOpenBrowserSync)
    }
}

// ============================================================
// MIUIX 版
// ============================================================
@Composable
private fun SettingsSyncMiuix(onBack: () -> Unit, onOpenBrowserSync: () -> Unit) {
    LaunchedEffect(Unit) {
        // 提前预热云函数，减少首次同步的冷启动等待
        withContext(Dispatchers.IO) { MoteCloudSyncManager.warmUp() }
    }
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) rememberLayerBackdrop { drawRect(surfaceColor); drawContent() } else null

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
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = SmoothEase),
        label = "settingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark) else Modifier
    val titleBarBlendColorEntries = remember(surfaceColor) { listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA))) }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    var useWebDav by remember { mutableStateOf(prefs.getString("sync_provider", "cloudbase") == "webdav") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    var catBookmarks by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_BOOKMARKS}", true)) }
    var catBrowseHistory by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_BROWSE_HISTORY}", false)) }
    var catSearchHistory by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_SEARCH_HISTORY}", false)) }
    var catQuickUrls by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_QUICK_URLS}", true)) }
    var catSettings by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_SETTINGS}", true)) }
    var catPasswords by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_PASSWORDS}", false)) }
    var e2eeEnabled by remember { mutableStateOf(prefs.getBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, false)) }
    var showE2eeDialog by remember { mutableStateOf(false) }
    var syncFrequencyIndex by remember {
        mutableIntStateOf(
            prefs.getInt(MoteAutoSync.KEY_FREQUENCY, MoteAutoSync.FREQ_REALTIME)
                .coerceIn(0, MoteAutoSync.FREQUENCY_OPTIONS.lastIndex)
        )
    }
    val syncFrequencyEntries = remember(syncFrequencyIndex) {
        listOf(
            DropdownEntry(
                items = MoteAutoSync.FREQUENCY_OPTIONS.mapIndexed { index, label ->
                    DropdownItem(
                        text = label,
                        selected = syncFrequencyIndex == index,
                        onClick = {
                            syncFrequencyIndex = index
                            prefs.edit().putInt(MoteAutoSync.KEY_FREQUENCY, index).apply()
                            MoteAutoSync.restart(context)
                        },
                    )
                },
            )
        )
    }
    var isSyncing by remember { mutableStateOf(false) }
    var conflictData by remember { mutableStateOf<Pair<MoteCloudSyncManager.SyncData, MoteCloudSyncManager.SyncData>?>(null) }

    var isLoggedIn by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "")?.isNotBlank() == true) }
    var currentUserEmail by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: "") }
    var currentUsername by remember {
        mutableStateOf(
            (prefs.getString("cloudbase_username", "") ?: "").ifBlank {
                MoteCloudSyncManager.usernameFromToken(
                    prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: ""
                )
            }
        )
    }
    var showAccountDialog by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var webDavUrl by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_URL, "") ?: "") }
    var webDavUser by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_USER, "") ?: "") }
    var webDavPass by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_PASS, "") ?: "") }

    fun saveConfig() {
        prefs.edit()
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_BOOKMARKS}", catBookmarks)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_BROWSE_HISTORY}", catBrowseHistory)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_SEARCH_HISTORY}", catSearchHistory)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_QUICK_URLS}", catQuickUrls)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_SETTINGS}", catSettings)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_PASSWORDS}", catPasswords)
            .putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, e2eeEnabled)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_URL, webDavUrl)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_USER, webDavUser)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_PASS, webDavPass)
            .apply()
    }

    fun applyLoginSuccess(data: JSONObject, accountName: String, accountPassword: String) {
        val token = data.optString("token").ifBlank { data.optString("access_token") }.ifBlank { data.optString("accessToken") }
        val refresh = data.optString("refresh")
            .ifBlank { data.optString("refresh_token") }
            .ifBlank { data.optString("refreshToken") }
        val uid = data.optString("uid")
            .ifBlank { data.optString("sub") }
            .ifBlank { data.optString("userId") }

        if (token.isBlank()) {
            errorDialogMessage = "登录失败: 未能获取到有效Token。\n\n完整响应:\n$data"
            return
        }

        prefs.edit()
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, token)
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN, refresh)
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, uid.ifBlank { accountName })
            .putString("cloudbase_username", accountName)
            .apply()
        isLoggedIn = true
        currentUserEmail = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: ""
        currentUsername = prefs.getString("cloudbase_username", "") ?: ""
        if (accountPassword.isNotBlank()) {
            MoteCloudSyncManager.updateE2EEKey(accountName, accountPassword, prefs)
        }
        showLoginDialog = false
        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
    }

    fun doLoginOrRegister() {
        if (isLoggingIn) return
        val account = usernameInput.trim().lowercase()
        val password = passwordInput
        if (account.isBlank() || password.isBlank()) {
            Toast.makeText(context, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
            return
        }
        isLoggingIn = true
        scope.launch {
            var login = withContext(Dispatchers.IO) {
                MoteCloudSyncManager.loginAccount(account, password)
            }
            var errorText = login.second
            if (login.first == null) {
                val (registered, registerError) = withContext(Dispatchers.IO) {
                    MoteCloudSyncManager.registerAccount(account, password)
                }
                if (registered) {
                    login = withContext(Dispatchers.IO) {
                        MoteCloudSyncManager.loginAccount(account, password)
                    }
                    errorText = login.second
                } else {
                    errorText = registerError
                }
            }
            withContext(Dispatchers.Main) {
                isLoggingIn = false
                val data = login.first
                if (data != null) {
                    applyLoginSuccess(data, account, password)
                } else {
                    errorDialogMessage = errorText ?: "登录失败"
                }
            }
        }
    }

    fun startSync() {
        saveConfig()
        isSyncing = true
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!useWebDav) {
                        val accessToken = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: ""
                        val userId = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: ""
                        if (accessToken.isBlank()) throw Exception("CloudBase 账号未登录")

                        val localObj = MoteCloudSyncManager.buildLocalData(prefs)
                        val localJson = localObj.toString()

                        val pulled = MoteCloudSyncManager.pullRemote(prefs)
                        val remote = pulled.first ?: throw Exception(pulled.second ?: "拉取云端数据失败")
                        val lastSyncAt = prefs.getLong(MoteCloudSyncManager.KEY_LAST_SYNC, 0L)

                        // 只有「云端在上次同步之后被改过」且「内容确实不同」才判定冲突，
                        // 避免本地易变字段导致每次同步都弹冲突
                        if (remote.content.isNotBlank() && remote.updatedAt > lastSyncAt) {
                            val remoteJson = MoteCloudSyncManager.decrypt(remote.content, prefs)
                            if (remoteJson != null) {
                                try {
                                    val obj = JSONObject(remoteJson)
                                    val remoteKey = obj.optString("fallback_key", "")
                                    if (remoteKey.isNotBlank()) {
                                        prefs.edit().putString(MoteCloudSyncManager.KEY_FALLBACK_PASS, remoteKey).apply()
                                    }
                                } catch (_: Exception) {}
                                if (remoteJson != localJson) {
                                    val remoteObjJson = JSONObject(remoteJson)
                                    withContext(Dispatchers.Main) {
                                        isSyncing = false
                                        conflictData = MoteCloudSyncManager.SyncData(0, localObj) to MoteCloudSyncManager.SyncData(0, remoteObjJson)
                                    }
                                    return@withContext
                                }
                            }
                        }

                        val encrypted = MoteCloudSyncManager.encrypt(localJson, prefs)
                        val pushed = MoteCloudSyncManager.pushRemote(prefs, encrypted)
                        val pushedAt = pushed.first
                        withContext(Dispatchers.Main) {
                            isSyncing = false
                            if (pushedAt != null) {
                                prefs.edit().putLong(MoteCloudSyncManager.KEY_LAST_SYNC, pushedAt).apply()
                                Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show()
                            } else {
                                errorDialogMessage = "同步失败：${pushed.second ?: "未知错误"}"
                            }
                        }
                    } else {
                        val config = MoteCloudSyncManager.getWebDavConfig(prefs) ?: throw Exception("WebDAV配置不完整")
                        val localObj = MoteCloudSyncManager.buildLocalData(prefs)
                        val localJson = localObj.toString()

                        val remoteCipher = MoteCloudSyncManager.downloadFile(config)
                        if (remoteCipher != null) {
                            val tempDecrypt = MoteCloudSyncManager.decrypt(remoteCipher, prefs)
                            if (tempDecrypt != null) {
                                try {
                                    val obj = JSONObject(tempDecrypt)
                                    val remoteKey = obj.optString("fallback_key", "")
                                    if (remoteKey.isNotBlank()) {
                                        prefs.edit().putString(MoteCloudSyncManager.KEY_FALLBACK_PASS, remoteKey).apply()
                                    }
                                } catch (_: Exception) {}
                            }
                            val remoteJson = MoteCloudSyncManager.decrypt(remoteCipher, prefs)
                            if (remoteJson != null && remoteJson != localJson) {
                                val remoteObj = JSONObject(remoteJson)
                                withContext(Dispatchers.Main) {
                                    isSyncing = false
                                    conflictData = MoteCloudSyncManager.SyncData(0, localObj) to MoteCloudSyncManager.SyncData(0, remoteObj)
                                }
                                return@withContext
                            }
                        }
                        val encrypted = MoteCloudSyncManager.encrypt(localJson, prefs)
                        val ok = MoteCloudSyncManager.uploadFile(config, encrypted)
                        withContext(Dispatchers.Main) {
                            isSyncing = false
                            if (ok) {
                                prefs.edit().putLong(MoteCloudSyncManager.KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                                Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show()
                            } else {
                                errorDialogMessage = "同步失败：WebDAV 上传失败，请检查地址和账号密码"
                            }
                        }
                    }
                } catch (e: Exception) {
                    val message = e.message ?: "未知错误"
                    withContext(Dispatchers.Main) {
                        isSyncing = false
                        if (message.contains("登录已过期") || message.contains("重新登录")) {
                            prefs.edit()
                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN)
                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN)
                                .apply()
                            isLoggedIn = false
                            usernameInput = ""
                            passwordInput = ""
                            showLoginDialog = true
                            Toast.makeText(context, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show()
                        } else {
                            errorDialogMessage = "同步异常：$message"
                        }
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(modifier = Modifier.fillMaxSize().then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).scrollEndHaptic().overScrollVertical(),
                    contentPadding = PaddingValues(
                        top = contentTopPadding + 4.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 88.dp
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (useWebDav) {
                                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 12.dp, bottom = 12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextField(webDavUrl, { webDavUrl = it }, label = "WebDAV 地址", modifier = Modifier.fillMaxWidth())
                                        TextField(webDavUser, { webDavUser = it }, label = "账号", modifier = Modifier.fillMaxWidth())
                                        TextField(webDavPass, { webDavPass = it }, label = "密码", modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }

                            SmallTitle("设置")
                            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                if (!useWebDav) {
                                    if (isLoggedIn) {
                                        BasicComponent(
                                            title = "账号",
                                            summary = currentUsername.ifBlank { currentUserEmail },
                                            onClick = { showAccountDialog = true },
                                            endActions = {
                                                Box(
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(MiuixTheme.colorScheme.secondaryContainer)
                                                        .clickable {
                                                            prefs.edit()
                                                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN)
                                                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN)
                                                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID)
                                                                .remove("cloudbase_username")
                                                                .apply()
                                                            isLoggedIn = false
                                                            usernameInput = ""
                                                            passwordInput = ""
                                                        }
                                                        .padding(horizontal = 16.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = "退出登录",
                                                        style = MiuixTheme.textStyles.body2,
                                                        color = MiuixTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                        )
                                    } else {
                                        BasicComponent(
                                            title = "登录/注册",
                                            onClick = {
                                                usernameInput = ""
                                                passwordInput = ""
                                                showLoginDialog = true
                                            }
                                        )
                                    }
                                }
                                WindowDropdownPreference(
                                    title = "同步频率",
                                    entries = syncFrequencyEntries,
                                    collapseOnSelection = true,
                                )
                                SwitchPreference(
                                    title = "高级数据保护",
                                    summary = "开启后将采用端到端加密技术，确保您的同步数据只能在可信设备上解密",
                                    checked = e2eeEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            showE2eeDialog = true
                                        } else {
                                            e2eeEnabled = false
                                            prefs.edit().putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, false).apply()
                                        }
                                    }
                                )
                            }

                            SmallTitle("同步项")
                            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    BasicComponent(
                                        title = "书签列表",
                                        onClick = { catBookmarks = !catBookmarks },
                                        endActions = {
                                            Checkbox(state = if (catBookmarks) ToggleableState.On else ToggleableState.Off, onClick = { catBookmarks = !catBookmarks })
                                        }
                                    )
                                    BasicComponent(
                                        title = "浏览历史",
                                        onClick = { catBrowseHistory = !catBrowseHistory },
                                        endActions = {
                                            Checkbox(state = if (catBrowseHistory) ToggleableState.On else ToggleableState.Off, onClick = { catBrowseHistory = !catBrowseHistory })
                                        }
                                    )
                                    BasicComponent(
                                        title = "搜索历史",
                                        onClick = { catSearchHistory = !catSearchHistory },
                                        endActions = {
                                            Checkbox(state = if (catSearchHistory) ToggleableState.On else ToggleableState.Off, onClick = { catSearchHistory = !catSearchHistory })
                                        }
                                    )
                                    BasicComponent(
                                        title = "快捷网址",
                                        onClick = { catQuickUrls = !catQuickUrls },
                                        endActions = {
                                            Checkbox(state = if (catQuickUrls) ToggleableState.On else ToggleableState.Off, onClick = { catQuickUrls = !catQuickUrls })
                                        }
                                    )
                                    BasicComponent(
                                        title = "设置选项",
                                        onClick = { catSettings = !catSettings },
                                        endActions = {
                                            Checkbox(state = if (catSettings) ToggleableState.On else ToggleableState.Off, onClick = { catSettings = !catSettings })
                                        }
                                    )
                                    BasicComponent(
                                        title = "保存的密码",
                                        onClick = { catPasswords = !catPasswords },
                                        endActions = {
                                            Checkbox(state = if (catPasswords) ToggleableState.On else ToggleableState.Off, onClick = { catPasswords = !catPasswords })
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()))
                        }
                    }
                }
                SettingsPageAutoScroll(lazyListState = listState, pageRoute = "settings_sync", contentTopPadding = contentTopPadding)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                Modifier.progressiveTextureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = 10f, gradient = ProgressiveBlur.Top.copy(
     startFraction = 0.75f,
     curve = 2.2f,
 ), colors = BlurDefaults.blurColors(
            blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
        )).background(
                                    Brush.verticalGradient(
                                        listOf(surfaceColor, Color.Transparent),
                                    ),
                                )
                            } else {
                                Modifier.textureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = TOP_BAR_BLUR_RADIUS, noiseCoefficient = BlurDefaults.NoiseCoefficient, colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries))
                            }
                        } else Modifier.background(surfaceColor)
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords -> val bottomPx = coords.boundsInWindow().bottom.toInt(); if (bottomPx != topBarBottomPx) topBarBottomPx = bottomPx }
            ) {
                TopAppBar(
                    title = "同步",
                    largeTitle = "同步",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(onBack = onBack, progressiveTitleBlur = progressiveTitleBlur, backCardAlpha = backCardAlpha, backCardMaterialModifier = backCardMaterialModifier, iconTint = MiuixTheme.colorScheme.onSurface)
                    },
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
                                entries = listOf(
                                    DropdownEntry(
                                        items = listOf(
                                            DropdownItem(
                                                text = "WebDAV",
                                                selected = useWebDav,
                                                onClick = {
                                                    showMoreMenu = false
                                                    useWebDav = !useWebDav
                                                    prefs.edit().putString("sync_provider", if (useWebDav) "webdav" else "cloudbase").apply()
                                                }
                                            ),
                                            DropdownItem(
                                                text = "与其它浏览器同步",
                                                onClick = {
                                                    showMoreMenu = false
                                                    onOpenBrowserSync()
                                                }
                                            )
                                        )
                                    )
                                ),
                                onDismissRequest = { showMoreMenu = false }
                            )
                        }
                    }
                )
            }

            // 悬浮固定底部按钮
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp).zIndex(10f)) {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(if (!isSyncing) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.primary.copy(alpha = 0.5f)).clickable(enabled = !isSyncing) { startSync() }, contentAlignment = Alignment.Center) {
                    Text(text = if (isSyncing) "同步中..." else "立即同步", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // 登录/注册 Dialog
    WindowDialog(
        show = showLoginDialog,
        title = "登录/注册",
        onDismissRequest = { if (!isLoggingIn) showLoginDialog = false }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = "用户名（账号不存在时自动注册）",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = "密码",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { if (!isLoggingIn) showLoginDialog = false },
                    enabled = !isLoggingIn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    ),
                ) { Text("取消") }
                Button(
                    onClick = { doLoginOrRegister() },
                    enabled = !isLoggingIn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                ) { Text(if (isLoggingIn) "处理中..." else "确定") }
            }
        }
    }

    WindowDialog(
        show = showE2eeDialog,
        title = "开启高级数据保护",
        onDismissRequest = { showE2eeDialog = false }
    ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "开启后，同步数据仅能由你的账号密码解密，服务端无法读取内容。\n\n需注意：\n1. 忘记密码将无法恢复数据，重置密码同样无效。\n2. 修改密码后，已同步的数据需重新同步一次。\n3. 建议使用足够复杂的密码，避免被暴力破解。", color = MiuixTheme.colorScheme.onSurface)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showE2eeDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface
                        ),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            e2eeEnabled = true
                            prefs.edit().putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, true).apply()
                            showE2eeDialog = false
                            Toast.makeText(context, "已开启高级数据保护", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                    ) { Text("开启") }
                }
            }
        }

    WindowDialog(
        show = showAccountDialog,
        title = "账号详情",
        onDismissRequest = { showAccountDialog = false }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "用户名：${currentUsername.ifBlank { "—" }}",
                color = MiuixTheme.colorScheme.onSurface,
                fontSize = 15.sp,
            )
            Text(
                text = "UID：${currentUserEmail.ifBlank { "—" }}",
                color = MiuixTheme.colorScheme.onSurface,
                fontSize = 15.sp,
            )
        }
    }

    // 错误详情弹窗
    WindowDialog(
        show = errorDialogMessage != null,
        title = "错误详情",
        onDismissRequest = { errorDialogMessage = null }
    ) {
        if (errorDialogMessage != null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = errorDialogMessage!!,
                            color = MiuixTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                        )
                    }
                }
                Button(
                    onClick = { errorDialogMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White)
                ) { Text("关闭") }
            }
        }
    }

    WindowDialog(show = conflictData != null, title = "同步冲突", onDismissRequest = { conflictData = null }) {
        if (conflictData != null) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("本地数据与云端数据不一致，请选择处理方式：", color = MiuixTheme.colorScheme.onSurface)
                Button(
                    onClick = {
                        val localObj = conflictData!!.first.payload
                        val remoteObj = conflictData!!.second.payload
                        scope.launch(Dispatchers.IO) {
                            val snapshotObj = try {
                                org.json.JSONObject(prefs.getString(MoteSyncMerge.SNAPSHOT_KEY, "") ?: "")
                            } catch (_: Exception) {
                                org.json.JSONObject()
                            }
                            val merged = MoteSyncMerge.merge(localObj, remoteObj, snapshotObj)
                            val mergedJson = merged.toString()
                            withContext(Dispatchers.Main) {
                                MoteCloudSyncManager.applyRemoteData(prefs, merged)
                                prefs.edit().putString(MoteSyncMerge.SNAPSHOT_KEY, mergedJson).apply()
                            }
                            val encrypted = MoteCloudSyncManager.encrypt(mergedJson, prefs)
                            if (!useWebDav) {
                                MoteCloudSyncManager.pushRemote(prefs, encrypted)
                            } else {
                                val config = MoteCloudSyncManager.getWebDavConfig(prefs)
                                if (config != null) MoteCloudSyncManager.uploadFile(config, encrypted)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "已合并本地与云端数据并同步", Toast.LENGTH_SHORT).show()
                                conflictData = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White)
                ) { Text("合并本地数据并同步") }
                Button(
                    onClick = {
                        val remoteObj = conflictData!!.second.payload
                        MoteCloudSyncManager.applyRemoteData(prefs, remoteObj)
                        prefs.edit()
                            .putString(MoteSyncMerge.SNAPSHOT_KEY, remoteObj.toString())
                            .apply()
                        Toast.makeText(context, "已用云端数据覆盖本地，部分设置需重启生效", Toast.LENGTH_LONG).show()
                        conflictData = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface)
                ) { Text("覆盖本地数据") }
            }
        }
    }
}

// ============================================================
// Material 3 版
// ============================================================
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSyncMaterial(onBack: () -> Unit, onOpenBrowserSync: () -> Unit) {
    LaunchedEffect(Unit) {
        // 提前预热云函数，减少首次同步的冷启动等待
        withContext(Dispatchers.IO) { MoteCloudSyncManager.warmUp() }
    }
    val navOwner = rememberNavigationEventDispatcherOwner()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val pageBg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    var useWebDav by remember { mutableStateOf(prefs.getString("sync_provider", "cloudbase") == "webdav") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    var catBookmarks by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_BOOKMARKS}", true)) }
    var catBrowseHistory by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_BROWSE_HISTORY}", false)) }
    var catSearchHistory by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_SEARCH_HISTORY}", false)) }
    var catQuickUrls by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_QUICK_URLS}", true)) }
    var catSettings by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_SETTINGS}", true)) }
    var catPasswords by remember { mutableStateOf(prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_PASSWORDS}", false)) }
    var e2eeEnabled by remember { mutableStateOf(prefs.getBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, false)) }
    var showE2eeDialog by remember { mutableStateOf(false) }
    var syncFrequencyIndex by remember {
        mutableIntStateOf(
            prefs.getInt(MoteAutoSync.KEY_FREQUENCY, MoteAutoSync.FREQ_REALTIME)
                .coerceIn(0, MoteAutoSync.FREQUENCY_OPTIONS.lastIndex)
        )
    }
    var isSyncing by remember { mutableStateOf(false) }
    var conflictData by remember { mutableStateOf<Pair<MoteCloudSyncManager.SyncData, MoteCloudSyncManager.SyncData>?>(null) }

    var isLoggedIn by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "")?.isNotBlank() == true) }
    var currentUserEmail by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: "") }
    var currentUsername by remember {
        mutableStateOf(
            (prefs.getString("cloudbase_username", "") ?: "").ifBlank {
                MoteCloudSyncManager.usernameFromToken(
                    prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: ""
                )
            }
        )
    }
    var showAccountDialog by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var webDavUrl by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_URL, "") ?: "") }
    var webDavUser by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_USER, "") ?: "") }
    var webDavPass by remember { mutableStateOf(prefs.getString(MoteCloudSyncManager.KEY_WEBDAV_PASS, "") ?: "") }

    fun saveConfig() {
        prefs.edit()
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_BOOKMARKS}", catBookmarks)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_BROWSE_HISTORY}", catBrowseHistory)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_SEARCH_HISTORY}", catSearchHistory)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_QUICK_URLS}", catQuickUrls)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_SETTINGS}", catSettings)
            .putBoolean("sync_cat_${MoteCloudSyncManager.CAT_PASSWORDS}", catPasswords)
            .putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, e2eeEnabled)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_URL, webDavUrl)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_USER, webDavUser)
            .putString(MoteCloudSyncManager.KEY_WEBDAV_PASS, webDavPass)
            .apply()
    }

    fun applyLoginSuccess(data: JSONObject, accountName: String, accountPassword: String) {
        val token = data.optString("token").ifBlank { data.optString("access_token") }.ifBlank { data.optString("accessToken") }
        val refresh = data.optString("refresh_token").ifBlank { data.optString("refreshToken") }
        val uid = data.optString("uid")
            .ifBlank { data.optString("sub") }
            .ifBlank { data.optString("userId") }

        if (token.isBlank()) {
            errorDialogMessage = "登录失败: 未能获取到有效Token。\n\n完整响应:\n$data"
            return
        }

        prefs.edit()
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, token)
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN, refresh)
            .putString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, uid.ifBlank { accountName })
            .putString("cloudbase_username", accountName)
            .apply()
        isLoggedIn = true
        currentUserEmail = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: ""
        currentUsername = prefs.getString("cloudbase_username", "") ?: ""
        if (accountPassword.isNotBlank()) {
            MoteCloudSyncManager.updateE2EEKey(accountName, accountPassword, prefs)
        }
        showLoginDialog = false
        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
    }

    fun doLoginOrRegister() {
        if (isLoggingIn) return
        val account = usernameInput.trim().lowercase()
        val password = passwordInput
        if (account.isBlank() || password.isBlank()) {
            Toast.makeText(context, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
            return
        }
        isLoggingIn = true
        scope.launch {
            var login = withContext(Dispatchers.IO) {
                MoteCloudSyncManager.loginAccount(account, password)
            }
            var errorText = login.second
            if (login.first == null) {
                val (registered, registerError) = withContext(Dispatchers.IO) {
                    MoteCloudSyncManager.registerAccount(account, password)
                }
                if (registered) {
                    login = withContext(Dispatchers.IO) {
                        MoteCloudSyncManager.loginAccount(account, password)
                    }
                    errorText = login.second
                } else {
                    errorText = registerError
                }
            }
            withContext(Dispatchers.Main) {
                isLoggingIn = false
                val data = login.first
                if (data != null) {
                    applyLoginSuccess(data, account, password)
                } else {
                    errorDialogMessage = errorText ?: "登录失败"
                }
            }
        }
    }

    fun startSync() {
        saveConfig()
        isSyncing = true
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!useWebDav) {
                        val accessToken = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: ""
                        val userId = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: ""
                        if (accessToken.isBlank()) throw Exception("CloudBase 账号未登录")

                        val localObj = MoteCloudSyncManager.buildLocalData(prefs)
                        val localJson = localObj.toString()

                        val pulled = MoteCloudSyncManager.pullRemote(prefs)
                        val remote = pulled.first ?: throw Exception(pulled.second ?: "拉取云端数据失败")
                        val lastSyncAt = prefs.getLong(MoteCloudSyncManager.KEY_LAST_SYNC, 0L)

                        // 只有「云端在上次同步之后被改过」且「内容确实不同」才判定冲突，
                        // 避免本地易变字段导致每次同步都弹冲突
                        if (remote.content.isNotBlank() && remote.updatedAt > lastSyncAt) {
                            val remoteJson = MoteCloudSyncManager.decrypt(remote.content, prefs)
                            if (remoteJson != null) {
                                try {
                                    val obj = JSONObject(remoteJson)
                                    val remoteKey = obj.optString("fallback_key", "")
                                    if (remoteKey.isNotBlank()) {
                                        prefs.edit().putString(MoteCloudSyncManager.KEY_FALLBACK_PASS, remoteKey).apply()
                                    }
                                } catch (_: Exception) {}
                                if (remoteJson != localJson) {
                                    val remoteObjJson = JSONObject(remoteJson)
                                    withContext(Dispatchers.Main) {
                                        isSyncing = false
                                        conflictData = MoteCloudSyncManager.SyncData(0, localObj) to MoteCloudSyncManager.SyncData(0, remoteObjJson)
                                    }
                                    return@withContext
                                }
                            }
                        }

                        val encrypted = MoteCloudSyncManager.encrypt(localJson, prefs)
                        val pushed = MoteCloudSyncManager.pushRemote(prefs, encrypted)
                        val pushedAt = pushed.first
                        withContext(Dispatchers.Main) {
                            isSyncing = false
                            if (pushedAt != null) {
                                prefs.edit().putLong(MoteCloudSyncManager.KEY_LAST_SYNC, pushedAt).apply()
                                Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show()
                            } else {
                                errorDialogMessage = "同步失败：${pushed.second ?: "未知错误"}"
                            }
                        }
                    } else {
                        val config = MoteCloudSyncManager.getWebDavConfig(prefs) ?: throw Exception("WebDAV配置不完整")
                        val localObj = MoteCloudSyncManager.buildLocalData(prefs)
                        val localJson = localObj.toString()

                        val remoteCipher = MoteCloudSyncManager.downloadFile(config)
                        if (remoteCipher != null) {
                            val tempDecrypt = MoteCloudSyncManager.decrypt(remoteCipher, prefs)
                            if (tempDecrypt != null) {
                                try {
                                    val obj = JSONObject(tempDecrypt)
                                    val remoteKey = obj.optString("fallback_key", "")
                                    if (remoteKey.isNotBlank()) {
                                        prefs.edit().putString(MoteCloudSyncManager.KEY_FALLBACK_PASS, remoteKey).apply()
                                    }
                                } catch (_: Exception) {}
                            }
                            val remoteJson = MoteCloudSyncManager.decrypt(remoteCipher, prefs)
                            if (remoteJson != null && remoteJson != localJson) {
                                val remoteObj = JSONObject(remoteJson)
                                withContext(Dispatchers.Main) {
                                    isSyncing = false
                                    conflictData = MoteCloudSyncManager.SyncData(0, localObj) to MoteCloudSyncManager.SyncData(0, remoteObj)
                                }
                                return@withContext
                            }
                        }
                        val encrypted = MoteCloudSyncManager.encrypt(localJson, prefs)
                        val ok = MoteCloudSyncManager.uploadFile(config, encrypted)
                        withContext(Dispatchers.Main) {
                            isSyncing = false
                            if (ok) {
                                prefs.edit().putLong(MoteCloudSyncManager.KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                                Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show()
                            } else {
                                errorDialogMessage = "同步失败：WebDAV 上传失败，请检查地址和账号密码"
                            }
                        }
                    }
                } catch (e: Exception) {
                    val message = e.message ?: "未知错误"
                    withContext(Dispatchers.Main) {
                        isSyncing = false
                        if (message.contains("登录已过期") || message.contains("重新登录")) {
                            prefs.edit()
                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN)
                                .remove(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN)
                                .apply()
                            isLoggedIn = false
                            usernameInput = ""
                            passwordInput = ""
                            showLoginDialog = true
                            Toast.makeText(context, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show()
                        } else {
                            errorDialogMessage = "同步异常：$message"
                        }
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("同步") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) { M3Icon(imageVector = MiuixIcons.Back, contentDescription = "返回") }
                    },
                    actions = {
                        Box {
                            M3IconButton(onClick = { showMoreMenu = true }) {
                                M3Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "更多")
                            }
                            androidx.compose.material3.DropdownMenu(
                                shape = RoundedCornerShape(16.dp),
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { M3Text("WebDAV") },
                                    leadingIcon = { if (useWebDav) M3Icon(imageVector = Icons.Filled.Check, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        useWebDav = !useWebDav
                                        prefs.edit().putString("sync_provider", if (useWebDav) "webdav" else "cloudbase").apply()
                                    }
                                )
                            }
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 88.dp
                    )
                ) {
                    if (useWebDav) {
                        item(key = "webdav") {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING).padding(bottom = M3_SEGMENT_GROUP_GAP)) {
                                    M3Surface(shape = RoundedCornerShape(16.dp), color = M3MaterialTheme.colorScheme.surfaceContainer) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            M3OutlinedTextField(webDavUrl, { webDavUrl = it }, label = { M3Text("WebDAV 地址") }, modifier = Modifier.fillMaxWidth())
                                            M3OutlinedTextField(webDavUser, { webDavUser = it }, label = { M3Text("账号") }, modifier = Modifier.fillMaxWidth())
                                            M3OutlinedTextField(webDavPass, { webDavPass = it }, label = { M3Text("密码") }, modifier = Modifier.fillMaxWidth())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "sync_settings") {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            M3Text("设置", color = M3MaterialTheme.colorScheme.primary, style = M3MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING).padding(bottom = M3_SEGMENT_GROUP_GAP)) {
                                MoteSegmentedColumn(
                                    color = M3MaterialTheme.colorScheme.surface,
                                    content = buildList {
                                        if (!useWebDav) {
                                            add {
                                                if (isLoggedIn) {
                                                    M3SegmentItem(
                                                        headline = { M3Text("账号") },
                                                        supporting = { M3Text(currentUsername.ifBlank { currentUserEmail }, color = M3MaterialTheme.colorScheme.onSurfaceVariant) },
                                                        trailing = {
                                                            M3Surface(
                                                                onClick = {
                                                                    prefs.edit().remove(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN).remove(MoteCloudSyncManager.KEY_CLOUDBASE_REFRESH_TOKEN).remove(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID).remove("cloudbase_username").apply()
                                                                    isLoggedIn = false; usernameInput = ""; passwordInput = ""
                                                                },
                                                                shape = RoundedCornerShape(16.dp),
                                                                color = M3MaterialTheme.colorScheme.secondaryContainer,
                                                                contentColor = M3MaterialTheme.colorScheme.onSecondaryContainer,
                                                            ) {
                                                                Box(modifier = Modifier.height(32.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                                                    M3Text("退出登录", style = M3MaterialTheme.typography.bodyMedium)
                                                                }
                                                            }
                                                        },
                                                        onClick = { showAccountDialog = true }
                                                    )
                                                } else {
                                                    M3SegmentArrowItem(
                                                        title = "登录/注册",
                                                        onClick = {
                                                            usernameInput = ""
                                                            passwordInput = ""
                                                            showLoginDialog = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        add {
                                            M3SegmentDropdownItem(
                                                title = "同步频率",
                                                summary = null,
                                                options = MoteAutoSync.FREQUENCY_OPTIONS,
                                                selectedIndex = syncFrequencyIndex,
                                                onSelected = { index ->
                                                    syncFrequencyIndex = index
                                                    prefs.edit().putInt(MoteAutoSync.KEY_FREQUENCY, index).apply()
                                                    MoteAutoSync.restart(context)
                                                },
                                            )
                                        }
                                        add {
                                            M3SegmentSwitchItem(
                                                title = "高级数据保护",
                                                summary = "开启后将采用端到端加密技术，确保您的同步数据只能在可信设备上解密",
                                                checked = e2eeEnabled,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        showE2eeDialog = true
                                                    } else {
                                                        e2eeEnabled = false
                                                        prefs.edit().putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, false).apply()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item(key = "sync_content") {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            M3Text("同步项", color = M3MaterialTheme.colorScheme.primary, style = M3MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING).padding(bottom = M3_SEGMENT_GROUP_GAP)) {
                                MoteSegmentedColumn(
                                    color = M3MaterialTheme.colorScheme.surface,
                                    content = listOf(
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("书签列表", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catBookmarks, onCheckedChange = { catBookmarks = it }) },
                                                onClick = { catBookmarks = !catBookmarks }
                                            )
                                        },
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("浏览历史", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catBrowseHistory, onCheckedChange = { catBrowseHistory = it }) },
                                                onClick = { catBrowseHistory = !catBrowseHistory }
                                            )
                                        },
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("搜索历史", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catSearchHistory, onCheckedChange = { catSearchHistory = it }) },
                                                onClick = { catSearchHistory = !catSearchHistory }
                                            )
                                        },
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("快捷网址", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catQuickUrls, onCheckedChange = { catQuickUrls = it }) },
                                                onClick = { catQuickUrls = !catQuickUrls }
                                            )
                                        },
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("设置选项", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catSettings, onCheckedChange = { catSettings = it }) },
                                                onClick = { catSettings = !catSettings }
                                            )
                                        },
                                        {
                                            M3SegmentItem(
                                                headline = { M3Text("保存的密码", style = M3MaterialTheme.typography.bodyLarge) },
                                                trailing = { androidx.compose.material3.Checkbox(checked = catPasswords, onCheckedChange = { catPasswords = it }) },
                                                onClick = { catPasswords = !catPasswords }
                                            )
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                // 悬浮固定底部按钮
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp).zIndex(10f)) {
                    M3Surface(
                        onClick = { if (!isSyncing) startSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = M3MaterialTheme.colorScheme.primary,
                        contentColor = M3MaterialTheme.colorScheme.onPrimary
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            M3Text(text = if (isSyncing) "同步中..." else "立即同步", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isLoggingIn) showLoginDialog = false },
            title = { M3Text("登录/注册") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    M3OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { M3Text("用户名（账号不存在时自动注册）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    M3OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { M3Text("密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                M3TextButton(
                    onClick = { doLoginOrRegister() },
                    enabled = !isLoggingIn
                ) { M3Text(if (isLoggingIn) "处理中..." else "确定") }
            },
            dismissButton = {
                M3TextButton(
                    onClick = { if (!isLoggingIn) showLoginDialog = false },
                    enabled = !isLoggingIn
                ) { M3Text("取消") }
            }
        )
    }

    if (showE2eeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showE2eeDialog = false },
            title = { M3Text("开启高级数据保护") },
            text = {
                Box(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    M3Text("开启后，同步数据仅能由你的账号密码解密，服务端无法读取内容。\n\n需注意：\n1. 忘记密码将无法恢复数据，重置密码同样无效。\n2. 修改密码后，已同步的数据需重新同步一次。\n3. 建议使用足够复杂的密码，避免被暴力破解。", style = M3MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                M3TextButton(onClick = {
                    e2eeEnabled = true
                    prefs.edit().putBoolean(MoteCloudSyncManager.KEY_E2EE_ENABLED, true).apply()
                    showE2eeDialog = false
                    Toast.makeText(context, "已开启高级数据保护", Toast.LENGTH_SHORT).show()
                }) { M3Text("开启") }
            },
            dismissButton = {
                M3TextButton(onClick = { showE2eeDialog = false }) { M3Text("取消") }
            }
        )
    }

    if (showAccountDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { M3Text("账号详情") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    M3Text("用户名：${currentUsername.ifBlank { "—" }}")
                    M3Text("UID：${currentUserEmail.ifBlank { "—" }}")
                }
            },
            confirmButton = {
                M3TextButton(onClick = { showAccountDialog = false }) { M3Text("关闭") }
            }
        )
    }

    // 错误详情弹窗
    if (errorDialogMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { M3Text("错误详情") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        M3Text(
                            text = errorDialogMessage!!,
                            style = M3MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
            confirmButton = {
                M3TextButton(onClick = { errorDialogMessage = null }) {
                    M3Text("关闭")
                }
            }
        )
    }

    if (conflictData != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { conflictData = null },
            title = { M3Text("同步冲突") },
            text = { M3Text("本地数据与云端数据不一致，请选择处理方式：") },
            confirmButton = {
                M3TextButton(
                    onClick = {
                        val localObj = conflictData!!.first.payload
                        val remoteObj = conflictData!!.second.payload
                        scope.launch(Dispatchers.IO) {
                            val snapshotObj = try {
                                org.json.JSONObject(prefs.getString(MoteSyncMerge.SNAPSHOT_KEY, "") ?: "")
                            } catch (_: Exception) {
                                org.json.JSONObject()
                            }
                            val merged = MoteSyncMerge.merge(localObj, remoteObj, snapshotObj)
                            val mergedJson = merged.toString()
                            withContext(Dispatchers.Main) {
                                MoteCloudSyncManager.applyRemoteData(prefs, merged)
                                prefs.edit().putString(MoteSyncMerge.SNAPSHOT_KEY, mergedJson).apply()
                            }
                            val encrypted = MoteCloudSyncManager.encrypt(mergedJson, prefs)
                            if (!useWebDav) {
                                MoteCloudSyncManager.pushRemote(prefs, encrypted)
                            } else {
                                val config = MoteCloudSyncManager.getWebDavConfig(prefs)
                                if (config != null) MoteCloudSyncManager.uploadFile(config, encrypted)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "已合并本地与云端数据并同步", Toast.LENGTH_SHORT).show()
                                conflictData = null
                            }
                        }
                    }
                ) { M3Text("合并本地数据并同步") }
            },
            dismissButton = {
                M3TextButton(onClick = {
                        val remoteObj = conflictData!!.second.payload
                        MoteCloudSyncManager.applyRemoteData(prefs, remoteObj)
                        prefs.edit()
                            .putString(MoteSyncMerge.SNAPSHOT_KEY, remoteObj.toString())
                            .apply()
                        Toast.makeText(context, "已用云端数据覆盖本地，部分设置需重启生效", Toast.LENGTH_LONG).show()
                        conflictData = null
                }) { M3Text("覆盖本地数据") }
            }
        )
    }
}

/**
 * 与其它浏览器同步：图文教程页，完全沿用设置页骨架。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsSyncBrowsersPage(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsSyncBrowsersMiuix(onBack, onOpenUrl)
        UiMode.Material -> SettingsSyncBrowsersMaterial(onBack, onOpenUrl)
    }
}

private const val BROWSER_SYNC_STORE_URL =
    "https://microsoftedge.microsoft.com/addons/detail/gknjcogfnimdkgnefmiajfcfopgbfpcg"

@Composable
private fun SettingsSyncBrowsersMiuix(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
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
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 12.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        "在电脑浏览器里同步 Mote 的书签",
                                        style = MiuixTheme.textStyles.title4,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "安装 Mote 书签同步扩展，用与手机相同的账号登录，即可双向同步书签。",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "1. 打开扩展商店，点击「获取」安装「Mote 书签同步」。",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "2. 安装完成后，在浏览器工具栏点击 Mote 图标，选择「打开设置」。",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "3. 用与 Mote 相同的账号和密码登录，等待首次同步完成。",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "4. 之后在浏览器新增、修改或删除书签，都会与 Mote 双向同步。",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "前往扩展商店",
                                    summary = "在 Edge 加载项商店安装 Mote 书签同步",
                                    onClick = { onOpenUrl(BROWSER_SYNC_STORE_URL) },
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                                )
                            )
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
                                        startFraction = 0.75f,
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
                    title = "与其它浏览器同步",
                    largeTitle = "与其它浏览器同步",
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSyncBrowsersMaterial(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
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
                    title = { M3Text("与其它浏览器同步") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
                            M3Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                            )
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "content") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        M3Surface(
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            color = M3MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                M3Text(
                                    "在电脑浏览器里同步 Mote 的书签",
                                    style = M3MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(8.dp))
                                M3Text(
                                    "安装 Mote 书签同步扩展，用与手机相同的账号登录，即可双向同步书签。",
                                    style = M3MaterialTheme.typography.bodyMedium,
                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(16.dp))
                                Spacer(Modifier.height(16.dp))
                                M3Text(
                                    "1. 打开扩展商店，点击「获取」安装「Mote 书签同步」。\n" +
                                            "2. 安装完成后，在浏览器工具栏点击 Mote 图标，选择「打开设置」。\n" +
                                            "3. 用与 Mote 相同的账号和密码登录，等待首次同步完成。\n" +
                                            "4. 之后在浏览器新增、修改或删除书签，都会与 Mote 双向同步。",
                                    style = M3MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        Spacer(Modifier.height(M3_SEGMENT_GROUP_GAP))

                        MoteSegmentedColumn(
                            color = M3MaterialTheme.colorScheme.surface,
                            content = listOf(
                                {
                                    M3SegmentArrowItem(
                                        title = "前往扩展商店",
                                        onClick = { onOpenUrl(BROWSER_SYNC_STORE_URL) },
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
