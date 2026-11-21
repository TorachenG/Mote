package com.torachen.mote.ui.ai.security

import java.io.File
import java.io.FileOutputStream

/**
 * 小型配置文件的原子写入。
 *
 * Android 上直接 writeText 在进程被杀时可能留下半截 JSON；这里先写同目录临时文件，
 * fsync 后再 rename，保证读到的要么是旧版本，要么是新版本。
 */
internal object AiAtomicJsonFile {

    fun read(file: File): String? = runCatching {
        if (!file.isFile) null else file.readText()
    }.getOrNull()

    fun write(file: File, text: String): Boolean {
        if (file.parentFile?.exists() == false) {
            file.parentFile?.mkdirs()
        }
        val temp = File(file.parentFile, file.name + ".tmp")
        val backup = File(file.parentFile, file.name + ".bak")
        return runCatching {
            FileOutputStream(temp).use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))
                output.flush()
                output.fd.sync()
            }
            if (file.exists()) {
                if (backup.exists()) backup.delete()
                if (!file.renameTo(backup)) {
                    return@runCatching false
                }
            }
            if (temp.renameTo(file)) {
                backup.delete()
                true
            } else {
                // 恢复旧文件，避免 rename 失败导致配置丢失。
                if (backup.exists()) backup.renameTo(file)
                false
            }
        }.getOrDefault(false).also { ok ->
            if (!ok) runCatching { temp.delete() }
        }
    }
}
