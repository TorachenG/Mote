package com.torachen.mote.ui.filemanager

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import rikka.shizuku.Shizuku
import java.io.File

/**
 * Shizuku 通道：以 ADB 级 shell 权限访问 Android/data 等受限目录。
 * 与 MT 管理器在无 root 时使用的方式一致。
 */
internal object ShizukuBridge {

    const val PERMISSION_REQUEST_CODE = 4401

    fun binderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasPermission(): Boolean = runCatching {
        binderAlive() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun requestPermission(): Boolean = runCatching {
        if (!binderAlive()) return false
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        true
    }.getOrDefault(false)

    /** Shizuku 13 隐藏了 newProcess，这里用反射调用（MT 同类方案也用此通道） */
    private fun run(cmd: String): Process? = runCatching {
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
        )
        m.isAccessible = true
        m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as? Process
    }.getOrNull()

    /** 返回目录项；失败返回 null */
    data class Entry(
        val name: String,
        val isDir: Boolean,
        val size: Long,
        val modified: Long,
    )

    fun list(path: String): List<Entry>? {
        if (!hasPermission()) return null
        return runCatching {
            val process = run("ls -lA '" + path.replace("'", "'\\''") + "'") ?: return null
            val out = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            val result = mutableListOf<Entry>()
            // toybox ls 默认输出 `yyyy-MM-dd HH:mm`
            val timeFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            out.forEach { line ->
                if (line.isBlank() || line.startsWith("total ")) return@forEach
                val parts = line.split(Regex("\\s+"), 8)
                if (parts.size < 8) return@forEach
                val isDir = parts[0].startsWith("d")
                val name = parts[7]
                if (name != "." && name != "..") {
                    result.add(
                        Entry(
                            name = name,
                            isDir = isDir,
                            size = parts[4].toLongOrNull() ?: 0L,
                            modified = runCatching {
                                timeFormat.parse("${parts[5]} ${parts[6]}")?.time ?: 0L
                            }.getOrDefault(0L),
                        )
                    )
                }
            }
            result.sortedWith(
                compareByDescending<Entry> { it.isDir }.thenBy { it.name.lowercase() }
            )
        }.getOrNull()
    }

    fun delete(path: String): Boolean = runCatching {
        if (!hasPermission()) return false
        run("rm -rf '" + path.replace("'", "'\\''") + "'")?.waitFor() == 0
    }.getOrDefault(false)

    fun rename(from: String, to: String): Boolean = runCatching {
        if (!hasPermission()) return false
        run("mv -f '" + from.replace("'", "'\\''") + "' '" + to.replace("'", "'\\''") + "'")?.waitFor() == 0
    }.getOrDefault(false)

    /** 复制 / 移动（回退通道通过 shell 访问受限目录） */
    fun copyOrMove(sources: List<String>, destDir: String, move: Boolean): Boolean = runCatching {
        if (!hasPermission()) return false
        val dest = "'" + destDir.trimEnd('/').replace("'", "'\\''") + "/'"
        sources.forEach { source ->
            val src = "'" + source.replace("'", "'\\''") + "'"
            val cmd = (if (move) "mv -f " else "cp -rf ") + src + " " + dest
            if (run(cmd)?.waitFor() != 0) return false
        }
        true
    }.getOrDefault(false)

    /** 复制到应用缓存后交给系统打开（可编辑的由外部应用写回缓存，再用 copyBack 写回） */
    fun copyToCache(context: Context, path: String): File? = runCatching {
        if (!hasPermission()) return null
        val name = File(path).name.ifBlank { "file" }
        val dir = File(context.cacheDir, "shizuku").apply { mkdirs() }
        val target = File(dir, name)
        val ok = run("cp -f '" + path.replace("'", "'\\''") + "' '" + target.absolutePath + "'")?.waitFor() == 0
        if (ok && target.exists()) target else null
    }.getOrNull()

    fun copyBack(local: File, destPath: String): Boolean = runCatching {
        if (!hasPermission()) return false
        run("cp -f '" + local.absolutePath + "' '" + destPath.replace("'", "'\\''") + "'")?.waitFor() == 0
    }.getOrDefault(false)

    fun openLocal(context: Context, file: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = context.contentResolver.getType(uri) ?: "*/*"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        true
    }.getOrDefault(false)
}
