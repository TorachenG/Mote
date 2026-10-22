@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.DrawableRes
import com.torachen.mote.R

/**
 * 桌面图标选项。
 *
 * 每个选项对应 AndroidManifest 里的一个 [android.content.pm.ActivityInfo]（activity-alias），
 * 切换时只启用选中的那个，其余禁用；别名都指向 [com.torachen.mote.ui.MainActivity]。
 */
internal data class AppIconOption(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int,
    /** 别名后缀，实际组件名为 `context.packageName + aliasSuffix`。 */
    val aliasSuffix: String,
    val category: String,
)

internal val APP_ICON_OPTIONS: List<AppIconOption> = listOf(
    AppIconOption("default", "默认", R.mipmap.ic_logo, ".IconDefault", "默认"),
    AppIconOption("sparkle", "星芒", R.mipmap.ic_logo_sparkle, ".IconSparkle", "默认"),
    AppIconOption("tri_blue", "蓝色", R.mipmap.ic_logo_tri_blue, ".IconNetwork", "三角"),
    AppIconOption("tri_green", "绿色", R.mipmap.ic_logo_tri_green, ".IconWave", "三角"),
    AppIconOption("tri_island", "岛屿", R.mipmap.ic_logo_tri_island, ".IconOrbit", "三角"),
)

internal object AppIconManager {

    private const val KEY_APP_ICON = "app_icon_id"

    fun currentId(context: Context): String {
        val prefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_APP_ICON, null)
        return if (APP_ICON_OPTIONS.any { it.id == saved }) saved!! else APP_ICON_OPTIONS.first().id
    }

    /**
     * 解析真实存在的别名组件名。
     *
     * 关键点：清单里 `android:name=".IconWave"` 是按 **manifest 的 package（命名空间）** 展开的，
     * 也就是 `com.torachen.mote.IconWave`；而 debug 包的 applicationId 是
     * `com.torachen.mote.debug`。直接用 packageName 拼会得到一个不存在的组件，
     * setComponentEnabledSetting 会抛异常 —— 表现就是"点了没反应"。
     *
     * 命名空间可以从 R 类所在的包里拿到（AGP 把 R 生成在 namespace 下）。
     */
    private fun aliasClassName(context: Context, option: AppIconOption): String {
        val namespace = R::class.java.`package`?.name.orEmpty()
        return if (namespace.isNotEmpty() && !namespace.endsWith(".R")) {
            namespace + option.aliasSuffix
        } else {
            context.packageName + option.aliasSuffix
        }
    }

    /**
     * 切换桌面图标：只启用选中的别名，其余全部禁用。
     * 使用 DONT_KILL_APP，避免把正在运行的进程杀掉。
     */
    fun apply(context: Context, id: String): Boolean {
        val target = APP_ICON_OPTIONS.firstOrNull { it.id == id } ?: return false
        val pm = context.packageManager
        val pkg = context.packageName

        // ★ 先启用目标别名，成功后再禁用其它别名。
        //   反过来的话，一旦启用失败就会出现"所有别名都被禁用、桌面图标消失"的情况。
        val targetClass = aliasClassName(context, target)
        try {
            pm.setComponentEnabledSetting(
                ComponentName(pkg, targetClass),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        } catch (e: Exception) {
            Log.w("AppIconManager", "enable failed: $targetClass / ${e.message}")
            return false
        }

        APP_ICON_OPTIONS.forEach { option ->
            if (option.id == target.id) return@forEach
            val className = aliasClassName(context, option)
            try {
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, className),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            } catch (e: Exception) {
                Log.w("AppIconManager", "disable failed: $className / ${e.message}")
            }
        }

        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_ICON, target.id)
            .apply()
        return true
    }
}
