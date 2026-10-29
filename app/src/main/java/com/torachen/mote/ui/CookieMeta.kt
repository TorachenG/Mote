package com.torachen.mote.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 自行维护的 Cookie 元数据（WebView 不暴露 Cookie 属性） */
internal data class CookieMeta(
    val origin: String,
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val scriptAccessible: Boolean,
    val created: Long,
    val expires: Long,
)

internal object CookieMetaStore {

    private const val PREFS = "mote_cookie_meta"
    private const val KEY = "entries"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun load(context: Context): MutableList<CookieMeta> {
        val raw = prefs(context).getString(KEY, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            val out = mutableListOf<CookieMeta>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                out.add(
                    CookieMeta(
                        origin = o.optString("origin"),
                        name = o.optString("name"),
                        value = o.optString("value"),
                        domain = o.optString("domain"),
                        path = o.optString("path", "/"),
                        secure = o.optBoolean("secure", false),
                        scriptAccessible = o.optBoolean("script", true),
                        created = o.optLong("created", 0L),
                        expires = o.optLong("expires", 0L),
                    )
                )
            }
            out
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun save(context: Context, list: List<CookieMeta>) {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("origin", m.origin)
                    put("name", m.name)
                    put("value", m.value)
                    put("domain", m.domain)
                    put("path", m.path)
                    put("secure", m.secure)
                    put("script", m.scriptAccessible)
                    put("created", m.created)
                    put("expires", m.expires)
                }
            )
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    /** 解析并记录 document.cookie 写入 */
    fun recordSetCookie(context: Context, raw: String, host: String) {
        if (raw.isBlank()) return
        val parts = raw.split(';').map { it.trim() }.filter { it.isNotBlank() }
        val first = parts.firstOrNull() ?: return
        val name = first.substringBefore('=').trim()
        val value = first.substringAfter('=', "")
        if (name.isBlank()) return

        var domain = host
        var path = "/"
        var secure = false
        var httpOnly = false
        var expires = 0L
        parts.drop(1).forEach { attr ->
            val k = attr.substringBefore('=').trim().lowercase()
            val v = attr.substringAfter('=', "").trim()
            when (k) {
                "domain" -> if (v.isNotBlank()) domain = v.removePrefix(".")
                "path" -> if (v.isNotBlank()) path = v
                "secure" -> secure = true
                "httponly" -> httpOnly = true
                "max-age" -> v.toLongOrNull()?.let { maxAge ->
                    expires = System.currentTimeMillis() + maxAge * 1000
                }
                "expires" -> parseDate(v)?.let { expires = it }
            }
        }
        upsert(context, CookieMeta(
            origin = "https://$host",
            name = name,
            value = value,
            domain = domain,
            path = path,
            secure = secure,
            scriptAccessible = !httpOnly,
            created = System.currentTimeMillis(),
            expires = expires,
        ))
    }

    fun upsert(context: Context, meta: CookieMeta) {
        val list = load(context)
        val idx = list.indexOfFirst { it.origin == meta.origin && it.name == meta.name }
        if (idx >= 0) {
            val old = list[idx]
            list[idx] = meta.copy(created = if (old.created > 0) old.created else meta.created)
        } else {
            list.add(meta)
        }
        save(context, list)
    }

    /** 为已存在但未记录属性的 Cookie 补一条基础记录 */
    fun ensure(context: Context, origin: String, name: String, value: String, host: String) {
        val list = load(context)
        if (list.any { it.origin == origin && it.name == name }) return
        list.add(
            CookieMeta(
                origin = origin,
                name = name,
                value = value,
                domain = host,
                path = "/",
                secure = origin.startsWith("https"),
                scriptAccessible = true,
                created = System.currentTimeMillis(),
                expires = 0L,
            )
        )
        save(context, list)
    }

    fun remove(context: Context, origin: String, name: String) {
        val list = load(context).filterNot { it.origin == origin && it.name == name }
        save(context, list)
    }

    fun removeOrigin(context: Context, origin: String) {
        val list = load(context).filterNot { it.origin == origin }
        save(context, list)
    }

    fun find(context: Context, origin: String, name: String): CookieMeta? =
        load(context).firstOrNull { it.origin == origin && it.name == name }

    private fun parseDate(value: String): Long? = try {
        val fmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        fmt.parse(value)?.time
    } catch (_: Exception) {
        null
    }
}

internal fun formatCookieTime(ms: Long): String {
    if (ms <= 0L) return "未知"
    return try {
        SimpleDateFormat("yyyy年M月d日 HH:mm:ss", Locale.CHINA).format(Date(ms))
    } catch (_: Exception) {
        "未知"
    }
}
