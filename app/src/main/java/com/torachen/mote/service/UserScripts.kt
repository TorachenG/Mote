@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.service

import android.content.Context
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * 用户脚本（对标 VIA 的脚本管理）。
 *
 * 存储：filesDir/userscripts/scripts.json（单个 JSON 数组，简单可靠）
 * 总开关：mote_prefs / userscript_master_enabled
 */
internal data class UserScript(
    val id: String,
    val name: String,
    val version: String,
    val code: String,
    val enabled: Boolean = true,
    /** 通过「下载脚本」添加时记录来源地址，用于「更新脚本」。 */
    val sourceUrl: String? = null,
)

internal object UserScriptStore {

    private const val PREFS = "mote_prefs"
    private const val KEY_MASTER = "userscript_master_enabled"
    private const val DIR_NAME = "userscripts"
    private const val FILE_NAME = "scripts.json"

    const val DEFAULT_CODE = """// ==UserScript==
// @name         新建脚本
// @version      1.0.0
// @description  在这里写你的脚本
// @match        *://*/*
// ==/UserScript==

(function () {
    'use strict';

    console.log('[Mote] script loaded');

    // 例如：
    // document.body.style.background = '#111';
})();"""

    fun masterEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MASTER, true)

    fun setMasterEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MASTER, enabled)
            .apply()
    }

    private fun storeFile(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, FILE_NAME)
    }

    fun list(context: Context): List<UserScript> {
        return try {
            val file = storeFile(context)
            if (!file.exists()) return emptyList()
            val arr = JSONArray(file.readText())
            (0 until arr.length()).mapNotNull { index ->
                val obj = arr.optJSONObject(index) ?: return@mapNotNull null
                UserScript(
                    id = obj.optString("id").ifBlank { newId() },
                    name = obj.optString("name", "未命名脚本"),
                    version = obj.optString("version", "1.0.0"),
                    code = obj.optString("code", ""),
                    enabled = obj.optBoolean("enabled", true),
                    sourceUrl = obj.optString("sourceUrl").takeIf { it.isNotBlank() },
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, scripts: List<UserScript>) {
        try {
            val arr = JSONArray()
            scripts.forEach { script ->
                arr.put(
                    JSONObject().apply {
                        put("id", script.id)
                        put("name", script.name)
                        put("version", script.version)
                        put("code", script.code)
                        put("enabled", script.enabled)
                        put("sourceUrl", script.sourceUrl ?: "")
                    }
                )
            }
            storeFile(context).writeText(arr.toString())
        } catch (_: Exception) {
        }
    }

    fun upsert(context: Context, script: UserScript) {
        val current = list(context).toMutableList()
        val index = current.indexOfFirst { it.id == script.id }
        if (index >= 0) current[index] = script else current.add(script)
        save(context, current)
    }

    fun delete(context: Context, id: String) {
        save(context, list(context).filterNot { it.id == id })
    }

    fun rename(context: Context, id: String, name: String) {
        save(context, list(context).map { if (it.id == id) it.copy(name = name) else it })
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean) {
        save(context, list(context).map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    fun newId(): String = UUID.randomUUID().toString()

    /** 从脚本代码里读 @name / @version。 */
    fun readMeta(code: String): Pair<String?, String?> {
        val name = Regex("""//\s*@name\s+(.+)""").find(code)?.groupValues?.getOrNull(1)?.trim()
        val version = Regex("""//\s*@version\s+(.+)""").find(code)?.groupValues?.getOrNull(1)?.trim()
        return name to version
    }

    private fun matchesUrl(script: UserScript, url: String): Boolean {
        val patterns = Regex("""//\s*@(match|include)\s+(.+)""")
            .findAll(script.code)
            .map { it.groupValues[2].trim() }
            .filter { it.isNotBlank() }
            .toList()
        if (patterns.isEmpty()) return true
        return patterns.any { pattern ->
            try {
                globToRegex(pattern).matches(url)
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun globToRegex(glob: String): Regex {
        val sb = StringBuilder("^")
        for (ch in glob) {
            when (ch) {
                '*' -> sb.append(".*")
                '?' -> sb.append('.')
                '.', '\\', '+', '(', ')', '[', ']', '{', '}', '|', '^', '$' -> sb.append('\\').append(ch)
                else -> sb.append(ch)
            }
        }
        sb.append('$')
        return Regex(sb.toString())
    }

    /** 页面加载完成后注入已启用且匹配当前网址的脚本。 */
    fun injectInto(webView: WebView) {
        try {
            val context = webView.context
            if (!masterEnabled(context)) return
            val url = webView.url ?: return
            if (!url.startsWith("http://") && !url.startsWith("https://")) return
            list(context).forEach { script ->
                if (!script.enabled || script.code.isBlank()) return@forEach
                if (!matchesUrl(script, url)) return@forEach
                val js = buildString {
                    append("(function(){try{\n")
                    append(script.code)
                    append("\n}catch(e){console.error('[Mote script] ' + e);}})();")
                }
                webView.evaluateJavascript(js, null)
            }
        } catch (_: Exception) {
        }
    }
}
