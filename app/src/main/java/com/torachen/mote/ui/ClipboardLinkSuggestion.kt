package com.torachen.mote.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences

/** 读取剪贴板最新项，若为网址则返回可跳转的 URL */
internal fun readClipboardUrl(context: Context, prefs: SharedPreferences): String? {
    if (!prefs.getBoolean("clipboard_read_enabled", true)) return null
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return null
    val clip = manager.primaryClip ?: return null
    if (clip.itemCount <= 0) return null
    val text = runCatching { clip.getItemAt(0).coerceToText(context)?.toString() }
        .getOrNull()?.trim().orEmpty()
    if (text.isEmpty() || text.length > 2048) return null
    return when {
        text.startsWith("http://", ignoreCase = true) ||
                text.startsWith("https://", ignoreCase = true) -> text
        Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(/[^\\s]*)?$").matches(text) -> "https://$text"
        else -> null
    }
}
