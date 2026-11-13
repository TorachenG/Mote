package com.torachen.mote.ui.ai

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * 按名称打开应用 / 在 Mote 浏览器打开网址。
 *
 * 只提供能力，不做任何「关键词命中就直接跳转」的判断：
 * 是否打开应用、打开哪个应用由 Agent 里的模型决定，再由工具执行器调用这里。
 */
internal object AiAppLauncher {

    data class OpenResult(
        val success: Boolean,
        val message: String,
        val packageName: String = "",
        val label: String = "",
    )

    private data class Target(
        val packageName: String,
        val label: String,
        val score: Int,
    )

    private val aliases = mapOf(
        "qq" to "com.tencent.mobileqq",
        "微信" to "com.tencent.mm",
        "wechat" to "com.tencent.mm",
        "支付宝" to "com.eg.android.AlipayGphone",
        "alipay" to "com.eg.android.AlipayGphone",
        "哔哩哔哩" to "tv.danmaku.bili",
        "b站" to "tv.danmaku.bili",
        "bilibili" to "tv.danmaku.bili",
        "抖音" to "com.ss.android.ugc.aweme",
        "小红书" to "com.xingin.xhs",
        "淘宝" to "com.taobao.taobao",
        "京东" to "com.jingdong.app.mall",
        "拼多多" to "com.xunmeng.pinduoduo",
        "设置" to "com.android.settings",
        "相机" to "com.android.camera",
        "计算器" to "com.android.calculator2",
        "高德地图" to "com.autonavi.minimap",
        "地图" to "com.autonavi.minimap",
        "qq音乐" to "com.tencent.qqmusic",
        "网易云音乐" to "com.netease.cloudmusic",
        "文件管理" to "com.android.documentsui",
        "浏览器" to "com.android.chrome",
        "chrome" to "com.android.chrome",
    )

    fun open(context: Context, rawName: String): OpenResult {
        val name = normalizeName(rawName)
        if (name.isBlank()) {
            return OpenResult(false, "应用名称为空。")
        }
        val pm = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = runCatching {
            pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
        }.getOrDefault(emptyList())

        val aliasPackage = aliases.entries.firstOrNull { (alias, _) ->
            val normalizedAlias = normalizeName(alias)
            normalizedAlias == name ||
                    (normalizedAlias.length >= 2 && name.contains(normalizedAlias))
        }?.value

        val candidates = activities.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            val pkg = activity.packageName
            val label = runCatching { info.loadLabel(pm).toString() }.getOrDefault("")
            val normalizedLabel = normalizeName(label)
            val normalizedPackage = pkg.lowercase()
            val score = when {
                aliasPackage != null && pkg.equals(aliasPackage, ignoreCase = true) -> 120
                normalizedPackage == name -> 110
                normalizedLabel == name -> 105
                normalizedPackage.endsWith(".$name") -> 95
                normalizedLabel.startsWith(name) -> 88
                normalizedLabel.contains(name) -> 78
                normalizedPackage.contains(name) -> 70
                name.length >= 2 && normalizedLabel.length >= 2 &&
                        name.contains(normalizedLabel) -> 60
                else -> 0
            }
            if (score <= 0) null else Target(pkg, label.ifBlank { pkg }, score)
        }.sortedByDescending { it.score }

        val target = candidates.firstOrNull()
            ?: return OpenResult(
                success = false,
                message = "没有在已安装的应用里找到「${rawName.take(24)}」。" +
                        "请用户确认应用名称，或者告诉我它属于哪一类应用。",
            )

        val intent = pm.getLaunchIntentForPackage(target.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return OpenResult(
            success = false,
            message = "找到了「${target.label}」，但它没有可用的启动入口。",
            packageName = target.packageName,
            label = target.label,
        )

        val opened = runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
        return if (opened) {
            OpenResult(
                success = true,
                message = "已打开「${target.label}」（${target.packageName}）。",
                packageName = target.packageName,
                label = target.label,
            )
        } else {
            OpenResult(
                success = false,
                message = "启动「${target.label}」失败，可能是系统限制了后台启动，请稍后重试。",
                packageName = target.packageName,
                label = target.label,
            )
        }
    }

    /** 用 Mote 自己的浏览器打开网址（新标签页）。 */
    fun openUrl(context: Context, url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        if (uri.scheme != "http" && uri.scheme != "https") return false
        val intent = Intent(context, com.torachen.mote.ui.MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun normalizeName(raw: String): String = raw
        .trim()
        .trim('「', '」', '"', '\'', '。', '，', ' ')
        .removeSuffix("App")
        .removeSuffix("app")
        .removeSuffix("应用")
        .replace(" ", "")
        .replace("\u3000", "")
        .lowercase()
}
