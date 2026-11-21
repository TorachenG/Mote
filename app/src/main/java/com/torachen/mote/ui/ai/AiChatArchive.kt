package com.torachen.mote.ui.ai

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * AI 对话历史 ZIP 备份。
 *
 * 参考官方 chat-demo 的 history 设计做了 Android 侧的精简实现：
 * manifest + 会话索引 + 每个会话原始 JSON。导入前先完整校验，全部通过后才替换本地记录。
 */
internal object AiChatArchive {

    private const val FORMAT = "mote-ai-history"
    private const val VERSION = 1
    private const val MAX_ENTRY_BYTES = 24 * 1024 * 1024
    private const val MAX_ENTRIES = 2_000

    fun export(context: Context, uri: Uri): Int {
        val index = AiConversationIndex.load(context) ?: return 0
        val output = context.contentResolver.openOutputStream(uri)?.buffered()
            ?: error("无法写入备份文件")
        val conversations = JSONArray()
        val payloads = mutableListOf<Pair<String, String>>()
        index.conversations.forEach { meta ->
            val raw = AiConversationStore.raw(context, meta.id) ?: return@forEach
            val safe = meta.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
            payloads.add("chats/$safe.json" to raw)
            conversations.put(
                JSONObject().apply {
                    put("id", meta.id)
                    put("title", meta.title.take(200))
                    put("updatedAt", meta.updatedAt)
                    put("entry", "chats/$safe.json")
                },
            )
        }
        if (payloads.isEmpty()) {
            output.close()
            return 0
        }
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(
                JSONObject().apply {
                    put("format", FORMAT)
                    put("version", VERSION)
                    put("exportedAt", System.currentTimeMillis())
                }.toString().toByteArray(),
            )
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("conversations.json"))
            zip.write(
                JSONObject().apply {
                    put("active", index.activeId.orEmpty())
                    put("conversations", conversations)
                }.toString().toByteArray(),
            )
            zip.closeEntry()

            payloads.forEach { (path, raw) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(raw.toByteArray())
                zip.closeEntry()
            }
        }
        return payloads.size
    }

    fun import(context: Context, uri: Uri): Int {
        val entries = readEntries(context, uri)
        val manifest = JSONObject(entries["manifest.json"] ?: error("备份缺少 manifest"))
        if (manifest.optString("format") != FORMAT) error("不是 Mote AI 对话备份")
        if (manifest.optInt("version", 0) != VERSION) error("备份版本不受支持")

        val index = JSONObject(entries["conversations.json"] ?: error("备份缺少会话索引"))
        val array = index.optJSONArray("conversations") ?: JSONArray()
        val validated = mutableListOf<Triple<String, String, String>>()
        val seen = HashSet<String>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val id = item.optString("id").trim()
            if (id.isBlank() || !seen.add(id)) continue
            val entry = item.optString("entry")
            val raw = entries[entry] ?: continue
            val obj = runCatching { JSONObject(raw) }.getOrNull() ?: continue
            if (!obj.has("messages")) continue
            validated.add(Triple(id, item.optString("title").ifBlank { "新对话" }, raw))
        }
        if (validated.isEmpty()) error("备份里没有可导入的对话")

        // 校验全部通过后才替换，避免导入中途失败把原记录删掉。
        val old = AiConversationIndex.load(context)?.conversations.orEmpty()
        old.forEach { AiConversationStore.delete(context, it.id) }
        validated.forEach { (id, _, raw) ->
            AiConversationStore.writeRaw(context, id, raw)
        }
        val metas = validated.map { (id, title, _) ->
            val obj = runCatching { JSONObject(entries["conversations.json"].orEmpty()) }.getOrNull()
            val updatedAt = obj
                ?.optJSONArray("conversations")
                ?.let { arr ->
                    (0 until arr.length())
                        .mapNotNull { arr.optJSONObject(it) }
                        .firstOrNull { it.optString("id") == id }
                        ?.optLong("updatedAt")
                }
                ?: System.currentTimeMillis()
            AiConversationMeta(id = id, title = title, updatedAt = updatedAt)
        }
        val active = index.optString("active").takeIf { id -> metas.any { it.id == id } }
            ?: metas.firstOrNull()?.id
        AiConversationIndex.save(context, active, metas)
        return metas.size
    }

    private fun readEntries(context: Context, uri: Uri): Map<String, String> {
        val input = context.contentResolver.openInputStream(uri)?.buffered()
            ?: error("无法读取备份文件")
        val map = mutableMapOf<String, String>()
        ZipInputStream(input).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            var count = 0
            while (entry != null) {
                if (++count > MAX_ENTRIES) error("备份条目过多")
                if (!entry.isDirectory) {
                    val name = entry.name.replace('\\', '/')
                    if (name.startsWith("/") || name.contains("..")) error("备份路径不合法")
                    val bytes = zip.readBytesLimited(MAX_ENTRY_BYTES)
                    map[name] = bytes.toString(Charsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return map
    }

    private fun ZipInputStream.readBytesLimited(limit: Int): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
            val read = read(chunk)
            if (read <= 0) break
            buffer.write(chunk, 0, read)
            if (buffer.size() > limit) error("备份条目过大")
        }
        return buffer.toByteArray()
    }
}
