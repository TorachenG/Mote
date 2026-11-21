package com.torachen.mote.ui.ai.plugins

import android.content.Context
import android.net.Uri
import com.torachen.mote.ui.ai.security.AiAtomicJsonFile
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream

internal object AiPluginStore {

    private const val REGISTRY = "ai_plugins.json"
    private const val PLUGIN_DIR = "plugins"
    private const val MAX_PACKAGE_BYTES = 4 * 1024 * 1024L
    private val lock = Any()
    private var loaded = false
    private val plugins = mutableListOf<AiPlugin>()

    private fun registryFile(context: Context): File =
        File(context.applicationContext.filesDir, REGISTRY)

    fun pluginDir(context: Context, id: String): File =
        File(File(context.applicationContext.filesDir, PLUGIN_DIR), id)

    @Synchronized
    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        plugins.clear()
        plugins.addAll(AiPluginCodec.decodeList(AiAtomicJsonFile.read(registryFile(context))))
    }

    fun all(context: Context): List<AiPlugin> = synchronized(lock) {
        ensureLoaded(context)
        plugins.sortedByDescending { it.installedAt }
    }

    fun byId(context: Context, id: String): AiPlugin? = synchronized(lock) {
        ensureLoaded(context)
        plugins.firstOrNull { it.id == id }
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val index = plugins.indexOfFirst { it.id == id }
        if (index < 0) return false
        plugins[index] = plugins[index].copy(enabled = enabled, updatedAt = System.currentTimeMillis())
        persistLocked(context)
    }

    fun remove(context: Context, id: String): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val removed = plugins.removeAll { it.id == id }
        if (removed) {
            pluginDir(context, id).deleteRecursively()
            persistLocked(context)
        }
        removed
    }

    fun readme(context: Context, id: String): String =
        runCatching { File(pluginDir(context, id), "README.md").readText().take(20_000) }
            .getOrDefault("")

    fun install(context: Context, uri: Uri): Result<AiPlugin> {
        val temp = File(context.cacheDir, "plugin-install-${System.currentTimeMillis()}")
        return runCatching {
            temp.mkdirs()
            var total = 0L
            var manifest: AiPlugin? = null
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input.buffered()).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (entry.isDirectory) continue
                        val name = entry.name.replace('\\', '/')
                        if (name.startsWith("/") || name.split('/').any { it == ".." || it.isBlank() }) {
                            throw IllegalArgumentException("插件包包含非法路径")
                        }
                        val bytes = zip.readBytes()
                        total += bytes.size
                        if (total > MAX_PACKAGE_BYTES) {
                            throw IllegalArgumentException("插件包超过 4MB")
                        }
                        val file = File(temp, name)
                        if (!file.canonicalPath.startsWith(temp.canonicalPath + File.separator)) {
                            throw IllegalArgumentException("插件包路径越界")
                        }
                        file.parentFile?.mkdirs()
                        file.writeBytes(bytes)
                        if (name == "manifest.json") {
                            manifest = AiPluginCodec.decode(
                                runCatching { JSONObject(String(bytes, Charsets.UTF_8)) }
                                    .getOrElse { throw IllegalArgumentException("manifest.json 不是合法 JSON") },
                            ) ?: throw IllegalArgumentException("manifest.json 字段不合法")
                        }
                    }
                }
            } ?: throw IllegalArgumentException("无法读取插件包")
            val parsed = manifest ?: throw IllegalArgumentException("插件包缺少 manifest.json")
            if (parsed.tools.any { it.url.isBlank() }) throw IllegalArgumentException("插件工具有空 URL")
            val target = pluginDir(context, parsed.id)
            target.parentFile?.mkdirs()
            val previous = byId(context, parsed.id)
            val backup = File(target.parentFile, parsed.id + ".bak")
            if (target.exists()) {
                backup.deleteRecursively()
                if (!target.renameTo(backup)) throw IllegalStateException("无法替换旧插件")
            }
            if (!temp.renameTo(target)) {
                backup.renameTo(target)
                throw IllegalStateException("无法安装插件文件")
            }
            backup.deleteRecursively()
            temp.deleteRecursively()
            val now = System.currentTimeMillis()
            val installed = parsed.copy(
                // 新安装默认禁用，必须由用户在插件页显式启用。
                enabled = previous?.enabled ?: false,
                installedAt = previous?.installedAt ?: now,
                updatedAt = now,
            )
            synchronized(lock) {
                ensureLoaded(context)
                val index = plugins.indexOfFirst { it.id == installed.id }
                if (index >= 0) plugins[index] = installed else plugins.add(installed)
                persistLocked(context)
            }
            installed
        }.onFailure {
            temp.deleteRecursively()
        }
    }

    private fun persistLocked(context: Context): Boolean =
        AiAtomicJsonFile.write(
            registryFile(context),
            AiPluginCodec.encodeList(plugins),
        )
}
