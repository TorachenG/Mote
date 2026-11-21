package com.torachen.mote.ui.ai.mcp

import android.content.Context
import com.torachen.mote.ui.ai.security.AiAtomicJsonFile
import java.io.File

/**
 * MCP server 配置的本地存储。
 *
 * 只保存非敏感结构；Token 通过 secretRef 指向 AndroidKeyStore。
 * 这里不主动连接，连接由后续 McpManager 负责。
 */
internal object McpStore {

    private const val FILE_NAME = "mcp_servers.json"
    private val lock = Any()
    private var loaded = false
    private val servers = mutableListOf<McpServerConfig>()

    private fun file(context: Context): File = File(context.applicationContext.filesDir, FILE_NAME)

    @Synchronized
    fun ensureLoaded(context: Context): List<McpServerConfig> {
        if (loaded) return servers.toList()
        loaded = true
        servers.clear()
        servers.addAll(McpConfigCodec.decodeList(AiAtomicJsonFile.read(file(context))))
        return servers.toList()
    }

    fun all(context: Context): List<McpServerConfig> = synchronized(lock) {
        ensureLoaded(context).toList()
    }

    fun byId(context: Context, id: String): McpServerConfig? = synchronized(lock) {
        ensureLoaded(context).firstOrNull { it.id == id }
    }

    fun upsert(context: Context, config: McpServerConfig): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val now = System.currentTimeMillis()
        val stored = config.copy(
            createdAt = if (config.createdAt > 0L) config.createdAt else now,
            updatedAt = now,
        )
        val index = servers.indexOfFirst { it.id == stored.id }
        if (index >= 0) servers[index] = stored else servers.add(stored)
        persistLocked(context)
    }

    fun remove(context: Context, id: String): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val removed = servers.removeAll { it.id == id }
        if (removed) persistLocked(context) else false
    }

    fun save(context: Context, list: List<McpServerConfig>): Boolean = synchronized(lock) {
        servers.clear()
        servers.addAll(list)
        loaded = true
        persistLocked(context)
    }

    private fun persistLocked(context: Context): Boolean {
        val text = McpConfigCodec.encodeList(servers)
        return AiAtomicJsonFile.write(file(context), text)
    }
}
