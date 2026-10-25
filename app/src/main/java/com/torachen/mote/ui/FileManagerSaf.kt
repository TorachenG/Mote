package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

/**
 * SAF 数据源：用于 java.io 无法访问的目录（如其它应用的 Android/data）。
 * 通过 ExternalStorageProvider 的 DocumentsContract 列举与打开，等效 MT 管理器的做法。
 */
internal object FmSafStore {

    data class Entry(
        val name: String,
        val isDir: Boolean,
        val size: Long,
        val modified: Long,
        val docId: String,
    )

    private const val AUTHORITY = "com.android.externalstorage.documents"

    private fun parentDocId(relativePath: String): String {
        val clean = relativePath.trim('/')
        return if (clean.isEmpty()) "primary:" else "primary:$clean"
    }

    /** 列举目录内容；无权限或失败返回 null */
    fun list(context: Context, relativePath: String): List<Entry>? {
        return try {
            val childrenUri = DocumentsContract.buildChildDocumentsUri(
                AUTHORITY,
                parentDocId(relativePath),
            )
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            )
            val out = mutableListOf<Entry>()
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val docId = c.getString(0) ?: continue
                    val name = c.getString(1) ?: continue
                    if (name.startsWith(".")) {
                        // 与 java.io 版本一致，跳过隐藏项
                    }
                    val mime = c.getString(2).orEmpty()
                    val size = if (c.isNull(3)) 0L else c.getLong(3)
                    val modified = if (c.isNull(4)) 0L else c.getLong(4)
                    out.add(
                        Entry(
                            name = name,
                            isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR,
                            size = size,
                            modified = modified,
                            docId = docId,
                        )
                    )
                }
            }
            out.sortedWith(
                compareByDescending<Entry> { it.isDir }
                    .thenBy { entry ->
                        val n = entry.name
                        when {
                            n.startsWith(".") -> 0
                            n.firstOrNull()?.let { it.code in 0x4E00..0x9FFF } == true -> 1
                            else -> 2
                        }
                    }
                    .thenBy { it.name.lowercase() }
            )
        } catch (_: Throwable) {
            null
        }
    }

    /** 打开文件：交给系统解析（视频、图片、文档等） */
    fun open(context: Context, docId: String): Boolean {
        return try {
            val uri = DocumentsContract.buildDocumentUri(AUTHORITY, docId)
            val mime = context.contentResolver.getType(uri) ?: "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun docUri(docId: String): Uri = DocumentsContract.buildDocumentUri(AUTHORITY, docId)
}
