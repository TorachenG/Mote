package com.torachen.mote.ui.ai

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

/**
 * 每个对话独立的输入框草稿，参考 DeepSeek Demo 的 drafts Map：
 * key 是 conversationId，value 是该对话自己的文本和附件。
 *
 * 文本持久化到 filesDir；附件只保存在本次进程内，避免把 Base64 文件内容写进草稿文件。
 * 即使对话还没有消息，也会用自己的 conversationId 独立保存。
 */
internal object AiDraftStore {

    const val NEW_CONVERSATION_KEY = "__new__"
    private const val FILE_NAME = "ai_drafts.json"

    data class Draft(
        val text: String = "",
        val attachments: List<AiAttachment> = emptyList(),
    ) {
        val isEmpty: Boolean get() = text.isBlank() && attachments.isEmpty()
    }

    private var loaded = false
    private var appContext: Context? = null
    private val drafts = mutableStateMapOf<String, Draft>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var saveJob: Job? = null

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        appContext = context.applicationContext
        val target = file(context)
        val legacy = legacyFile(context)
        val raw = runCatching { target.readText() }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: runCatching { legacy.readText() }.getOrNull().orEmpty()
        if (raw.isBlank()) return
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { key ->
                val text = when (val value = json.opt(key)) {
                    is String -> value
                    is JSONObject -> value.optString("text")
                    else -> ""
                }
                if (text.isNotBlank()) drafts[key] = Draft(text = text)
            }
        }
        if (!target.exists()) {
            runCatching { legacy.copyTo(target, overwrite = true) }
        }
    }

    fun draft(context: Context, conversationId: String?): Draft {
        ensureLoaded(context)
        return drafts[conversationId ?: NEW_CONVERSATION_KEY] ?: Draft()
    }

    fun get(context: Context, conversationId: String?): String =
        draft(context, conversationId).text

    fun attachments(context: Context, conversationId: String?): List<AiAttachment> =
        draft(context, conversationId).attachments

    fun setText(context: Context, conversationId: String?, text: String) {
        ensureLoaded(context)
        val key = conversationId ?: NEW_CONVERSATION_KEY
        val current = drafts[key] ?: Draft()
        val next = current.copy(text = text)
        if (next.isEmpty) {
            if (drafts.remove(key) == null) return
        } else {
            if (current == next) return
            drafts[key] = next
        }
        scheduleSave()
    }

    fun setAttachments(
        context: Context,
        conversationId: String?,
        attachments: List<AiAttachment>,
    ) {
        ensureLoaded(context)
        val key = conversationId ?: NEW_CONVERSATION_KEY
        val current = drafts[key] ?: Draft()
        val next = current.copy(attachments = attachments.toList())
        if (next.isEmpty) {
            if (drafts.remove(key) == null) return
        } else {
            if (current == next) return
            drafts[key] = next
        }
        scheduleSave()
    }

    /** 保留与旧调用兼容：清空某个对话的整份草稿。 */
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
        runCatching { legacyFile(context).delete() }
    }

    private fun scheduleSave() {
        val snapshot = drafts
            .filterValues { it.text.isNotBlank() }
            .mapValues { it.value.text }
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(250L)
            val context = appContext ?: return@launch
            runCatching {
                val json = JSONObject()
                snapshot.forEach { (key, value) -> json.put(key, value) }
                file(context).writeText(json.toString())
            }
        }
    }

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)
    private fun legacyFile(context: Context): File = File(context.cacheDir, FILE_NAME)
}
