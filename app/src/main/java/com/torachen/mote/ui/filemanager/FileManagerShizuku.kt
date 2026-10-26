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

    /** 返回 (名称, 是否目录)；失败返回 null */
    fun list(path: String): List<Pair<String, Boolean>>? {
        if (!hasPermission()) return null
        return runCatching {
            val process = run("ls -lA '" + path.replace("'", "'\\''") + "'") ?: return null
            val out = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            val result = mutableListOf<Pair<String, Boolean>>()
            out.forEach { line ->
                if (line.isBlank() || line.startsWith("total ")) return@forEach
                val parts = line.split(Regex("\\s+"), 8)
                if (parts.size < 8) return@forEach
                val isDir = parts[0].startsWith("d")
                val name = parts[7]
                if (name != "." && name != "..") result.add(name to isDir)
            }
            result.sortedWith(compareByDescending<Pair<String, Boolean>> { it.second }.thenBy { it.first.lowercase() })
        }.getOrNull()
    }

    fun delete(path: String): Boolean = runCatching {
        if (!hasPermission()) return false
        run("rm -rf '" + path.replace("'", "'\\''") + "'")?.waitFor() == 0
    }.getOrDefault(false)

    fun rename(from: String, to: String): Boolean = runCatching {
        if (!hasPermission()) return false
        run("mv '" + from.replace("'", "'\\''") + "' '" + to.replace("'", "'\\''") + "'")?.waitFor() == 0
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
