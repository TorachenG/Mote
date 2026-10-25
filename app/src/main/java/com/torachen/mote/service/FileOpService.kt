package com.torachen.mote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/**
 * 文件管理相关的后台任务：复制 / 移动 / 解压。
 * 以前台服务运行，通知栏分类「文件管理」展示进度。
 */
class FileOpService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra(EXTRA_ACTION) ?: return START_NOT_STICKY
        val sources = intent.getStringArrayExtra(EXTRA_SOURCES)?.toList().orEmpty()
        val destDir = intent.getStringExtra(EXTRA_DEST).orEmpty()
        val archive = intent.getStringExtra(EXTRA_ARCHIVE).orEmpty()

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("准备中…", 0, 0, true))

        FileOpState.running.value = true
        FileOpState.message.value = null

        scope.launch {
            val ok = try {
                when (action) {
                    ACTION_COPY -> copyAll(sources, destDir, move = false)
                    ACTION_MOVE -> copyAll(sources, destDir, move = true)
                    ACTION_EXTRACT -> extractArchive(File(archive), File(destDir))
                    ACTION_DELETE -> deleteAll(sources)
                    ACTION_COMPRESS -> compressTo(sources, destDir)
                    else -> false
                }
            } catch (e: Exception) {
                FileOpState.message.value = "失败：${e.message ?: "未知错误"}"
                false
            }

            val text = when (action) {
                ACTION_COPY -> "复制完成"
                ACTION_MOVE -> "移动完成"
                ACTION_DELETE -> "删除完成"
                ACTION_COMPRESS -> "压缩完成"
                else -> "解压完成"
            }
            if (ok) {
                FileOpState.message.value = text
            }
            FileOpState.running.value = false

            // 先移除前台通知，再发一条「完成」通知，短暂展示后自动消失
            stopForeground(STOP_FOREGROUND_REMOVE)
            notificationManager()?.notify(
                NOTIFICATION_ID,
                buildNotification(
                    if (ok) text else FileOpState.message.value ?: "操作失败",
                    100, 100, indeterminate = false, ongoing = false,
                )
            )
            delay(2_500)
            notificationManager()?.cancel(NOTIFICATION_ID)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /* ---------------- 复制 / 移动 ---------------- */

    private fun copyAll(sources: List<String>, destDir: String, move: Boolean): Boolean {
        val destRoot = File(destDir)
        if (!destRoot.exists()) destRoot.mkdirs()

        var total = 0L
        sources.forEach { total += sizeOf(File(it)) }
        var done = 0L
        var lastNotify = 0L

        fun progress(name: String, extra: Long = 0) {
            done += extra
            val now = System.currentTimeMillis()
            if (now - lastNotify > 250) {
                lastNotify = now
                val percent = if (total <= 0) 0 else ((done * 100) / total).toInt().coerceIn(0, 100)
                notificationManager()?.notify(
                    NOTIFICATION_ID,
                    buildNotification("正在${if (move) "移动" else "复制"}：$name", percent, 100, false)
                )
            }
        }

        fun copyFile(src: File, target: File) {
            target.parentFile?.mkdirs()
            BufferedInputStream(FileInputStream(src)).use { input ->
                BufferedOutputStream(FileOutputStream(target)).use { output ->
                    val buf = ByteArray(256 * 1024)
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        output.write(buf, 0, read)
                        progress(src.name, read.toLong())
                    }
                }
            }
            target.setLastModified(src.lastModified())
        }

        fun copyNode(src: File, target: File) {
            if (src.isDirectory) {
                target.mkdirs()
                src.listFiles()?.forEach { child ->
                    copyNode(child, File(target, child.name))
                }
                if (move) src.delete()
            } else if (src.isFile) {
                copyFile(src, target)
                if (move) src.delete()
            }
        }

        sources.forEach { path ->
            val src = File(path)
            if (!src.exists()) return@forEach
            copyNode(src, File(destRoot, src.name))
        }
        return true
    }

    private fun compressTo(paths: List<String>, zipPath: String): Boolean {
        val zipFile = File(zipPath)
        zipFile.parentFile?.mkdirs()
        java.util.zip.ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zip ->
            val buffer = ByteArray(256 * 1024)
            fun addNode(node: File, base: String) {
                if (node.isDirectory) {
                    node.listFiles()?.forEach { addNode(it, "$base${node.name}/") }
                } else if (node.isFile) {
                    zip.putNextEntry(java.util.zip.ZipEntry("$base${node.name}"))
                    FileInputStream(node).use { input ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            zip.write(buffer, 0, read)
                        }
                    }
                    zip.closeEntry()
                }
            }
            paths.forEach { path ->
                val file = File(path)
                if (file.exists()) addNode(file, "")
            }
        }
        return true
    }

    private fun deleteAll(paths: List<String>): Boolean {
        var lastNotify = 0L
        paths.forEach { path ->
            val target = File(path)
            if (target.exists()) target.deleteRecursively()
            val now = System.currentTimeMillis()
            if (now - lastNotify > 250) {
                lastNotify = now
                notificationManager()?.notify(
                    NOTIFICATION_ID,
                    buildNotification("正在删除：${target.name}", 0, 0, true)
                )
            }
        }
        return true
    }

    private fun sizeOf(file: File): Long {
        if (file.isFile) return file.length()
        var total = 0L
        file.listFiles()?.forEach { total += sizeOf(it) }
        return total
    }

    /* ---------------- 解压 ---------------- */

    private fun extractArchive(archive: File, destDir: File): Boolean {
        if (!archive.exists()) return false
        destDir.mkdirs()
        val name = archive.name.lowercase()
        return when {
            name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".apk") -> extractZip(archive, destDir)
            name.endsWith(".tar") -> extractTar(archive, destDir)
            name.endsWith(".tar.gz") || name.endsWith(".tgz") -> {
                val tmp = File(destDir, archive.nameWithoutExtension + ".tar")
                GZIPInputStream(BufferedInputStream(FileInputStream(archive))).use { input ->
                    FileOutputStream(tmp).use { output -> input.copyTo(output, 256 * 1024) }
                }
                val ok = extractTar(tmp, destDir)
                tmp.delete()
                ok
            }
            name.endsWith(".gz") -> {
                val out = File(destDir, archive.name.removeSuffix(".gz"))
                GZIPInputStream(BufferedInputStream(FileInputStream(archive))).use { input ->
                    BufferedOutputStream(FileOutputStream(out)).use { output ->
                        input.copyTo(output, 256 * 1024)
                    }
                }
                true
            }
            else -> {
                FileOpState.message.value = "暂不支持该压缩格式"
                false
            }
        }
    }

    private fun extractZip(archive: File, destDir: File): Boolean {
        val destPath = destDir.canonicalPath
        ZipInputStream(BufferedInputStream(FileInputStream(archive))).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val out = File(destDir, entry.name)
                if (!out.canonicalPath.startsWith(destPath)) continue
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(out)).use { output ->
                        zip.copyTo(output, 256 * 1024)
                    }
                }
                zip.closeEntry()
            }
        }
        return true
    }

    /** 极简 tar 解析，够用于常见的 .tar / .tar.gz */
    private fun extractTar(archive: File, destDir: File): Boolean {
        val destPath = destDir.canonicalPath
        BufferedInputStream(FileInputStream(archive)).use { input ->
            val header = ByteArray(512)
            while (true) {
                var read = 0
                while (read < 512) {
                    val n = input.read(header, read, 512 - read)
                    if (n <= 0) break
                    read += n
                }
                if (read < 512) break
                if (header.all { it.toInt() == 0 }) break

                val name = String(header, 0, 100).trimEnd('\u0000', ' ')
                if (name.isEmpty()) break
                val sizeField = String(header, 124, 12).trim('\u0000', ' ')
                val size = sizeField.toLongOrNull(8) ?: 0L
                val type = header[156].toInt().toChar()
                val isDir = type == '5' || name.endsWith("/")

                val out = File(destDir, name)
                if (!out.canonicalPath.startsWith(destPath)) {
                    skipFully(input, size)
                    skipFully(input, (512 - (size % 512)) % 512)
                    continue
                }

                if (isDir) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(out)).use { output ->
                        copyExactly(input, output, size)
                    }
                }
                skipFully(input, (512 - (size % 512)) % 512)
            }
        }
        return true
    }

    private fun copyExactly(input: BufferedInputStream, output: BufferedOutputStream, size: Long) {
        var remaining = size
        val buf = ByteArray(256 * 1024)
        while (remaining > 0) {
            val n = input.read(buf, 0, minOf(buf.size.toLong(), remaining).toInt())
            if (n <= 0) break
            output.write(buf, 0, n)
            remaining -= n
        }
    }

    private fun skipFully(input: BufferedInputStream, size: Long) {
        var remaining = size
        val buf = ByteArray(8 * 1024)
        while (remaining > 0) {
            val n = input.read(buf, 0, minOf(buf.size.toLong(), remaining).toInt())
            if (n <= 0) break
            remaining -= n
        }
    }

    /* ---------------- 通知 ---------------- */

    private fun notificationManager(): NotificationManager? =
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private fun createChannel() {
        val mgr = notificationManager() ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "文件管理", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "复制、移动、解压等文件操作的进度" }
            )
        }
    }

    private fun buildNotification(
        text: String,
        progress: Int,
        max: Int,
        indeterminate: Boolean,
        ongoing: Boolean = indeterminate,
    ): Notification {
        val intent = Intent(this, com.torachen.mote.ui.MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("文件管理")
            .setContentText(text)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setProgress(if (indeterminate) 0 else max, progress, indeterminate)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "file_operations"
        private const val NOTIFICATION_ID = 4301

        const val ACTION_COPY = "copy"
        const val ACTION_MOVE = "move"
        const val ACTION_EXTRACT = "extract"
        const val ACTION_DELETE = "delete"
        const val ACTION_COMPRESS = "compress"

        private const val EXTRA_ACTION = "action"
        private const val EXTRA_SOURCES = "sources"
        private const val EXTRA_DEST = "dest"
        private const val EXTRA_ARCHIVE = "archive"

        fun copy(context: Context, sources: List<String>, dest: String) =
            start(context, ACTION_COPY, sources, dest, null)

        fun move(context: Context, sources: List<String>, dest: String) =
            start(context, ACTION_MOVE, sources, dest, null)

        fun extract(context: Context, archive: String, dest: String) =
            start(context, ACTION_EXTRACT, emptyList(), dest, archive)

        fun delete(context: Context, paths: List<String>) =
            start(context, ACTION_DELETE, paths, "", null)

        fun compress(context: Context, paths: List<String>, zipPath: String) =
            start(context, ACTION_COMPRESS, paths, zipPath, null)

        private fun start(context: Context, action: String, sources: List<String>, dest: String, archive: String?) {
            val intent = Intent(context, FileOpService::class.java).apply {
                putExtra(EXTRA_ACTION, action)
                putExtra(EXTRA_SOURCES, sources.toTypedArray())
                putExtra(EXTRA_DEST, dest)
                if (archive != null) putExtra(EXTRA_ARCHIVE, archive)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

/** 供界面观察的任务状态 */
object FileOpState {
    val running = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
}
