package com.torachen.mote.ui

import android.content.Context

/**
 * 网页安全中心的累计防护统计。
 * 三个分类分别对应：智能拦截 / 隐私保护 / 权限管控。
 */
object WebSecurityStats {

    const val CATEGORY_SMART = "smart"
    const val CATEGORY_PRIVACY = "privacy"
    const val CATEGORY_PERMISSION = "permission"

    private const val PREFS = "mote_security_stats"
    private fun key(category: String) = "count_$category"

    fun bump(context: Context, category: String, delta: Int = 1) {
        if (delta <= 0) return
        runCatching {
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val next = prefs.getInt(key(category), 0) + delta
            prefs.edit().putInt(key(category), next).apply()
        }
    }

    fun count(context: Context, category: String): Int =
        runCatching {
            context.applicationContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(key(category), 0)
        }.getOrDefault(0)

    fun total(context: Context): Int =
        count(context, CATEGORY_SMART) + count(context, CATEGORY_PRIVACY) + count(context, CATEGORY_PERMISSION)
}
