package com.torachen.mote.ui.ai

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

/**
 * 每个对话独立的输入框草稿。
 *
 * 存放于 `cacheDir`：应用内「清空对话记录」会删除，系统清理缓存时也会被清理，
 * 符合“常驻缓存、只由软件或系统清理”的预期。
 */
internal object AiDraftStore {

    const val NEW_CONVERSATION_KEY = "__new__"
    private const val FILE_NAME = "ai_drafts.json"

    private var loaded = false
    private var appContext: Context? = null
    private val drafts = mutableMapOf<String, String>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var saveJob: Job? = null

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        appContext = context.applicationContext
        val raw = runCatching { file(context).readText() }.getOrNull().orEmpty()
        if (raw.isBlank()) return
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { key ->
                val text = json.optString(key)
                if (text.isNotBlank()) drafts[key] = text
            }
        }
    }

    fun get(context: Context, conversationId: String?): String {
        ensureLoaded(context)
        return drafts[conversationId ?: NEW_CONVERSATION_KEY].orEmpty()
    }

    fun set(context: Context, conversationId: String?, text: String) {
        ensureLoaded(context)
        val key = conversationId ?: NEW_CONVERSATION_KEY
        if (text.isBlank()) {
            if (drafts.remove(key) == null) return
        } else {
            if (drafts[key] == text) return
            drafts[key] = text
        }
        scheduleSave()
    }

    fun remove(context: Context, conversationId: String?) {
        ensureLoaded(context)
        val key = conversationId ?: NEW_CONVERSATION_KEY
        if (drafts.remove(key) == null) return
        scheduleSave()
    }

    fun clearAll(context: Context) {
        ensureLoaded(context)
        drafts.clear()
        saveJob?.cancel()
        saveJob = null
        runCatching { file(context).delete() }
    }

    private fun scheduleSave() {
        val snapshot = LinkedHashMap(drafts)
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(250L)
            val context = appContext ?: return@launch
            runCatching {
                val json = JSONObject()
                snapshot.forEach { (key, value) ->
                    if (value.isNotBlank()) json.put(key, value)
                }
                file(context).writeText(json.toString())
            }
        }
    }

    private fun file(context: Context): File = File(context.cacheDir, FILE_NAME)
}
