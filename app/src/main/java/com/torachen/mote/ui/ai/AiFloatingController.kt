package com.torachen.mote.ui.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat

/** 悬浮助手开关与权限入口。 */
object AiFloatingController {

    private const val KEY_FLOATING_ENABLED = "ai_floating_enabled"
    internal const val EXTRA_TEMPORARY = "ai_floating_temporary"
    internal const val EXTRA_PROMPT = "ai_floating_prompt"
    internal const val EXTRA_NEW_CONVERSATION = "ai_floating_new_conversation"
    private var temporaryStopToken = 0

    fun openPanelWithPrompt(context: Context, prompt: String) {
        runCatching {
            context.startActivity(
                Intent(context, AiFloatingActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_PROMPT, prompt)
                    .putExtra(EXTRA_NEW_CONVERSATION, true),
            )
        }
    }

    fun isEnabled(context: Context): Boolean =
        aiPrefs(context).getBoolean(KEY_FLOATING_ENABLED, false)

    fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun start(context: Context): Boolean {
        if (!hasOverlayPermission(context)) return false
        val intent = Intent(context, AiFloatingBubbleService::class.java)
        runCatching { ContextCompat.startForegroundService(context, intent) }
            .onFailure { return false }
        aiPrefs(context).edit().putBoolean(KEY_FLOATING_ENABLED, true).apply()
        return true
    }

    /** Agent 控制外部应用时临时显示；不写入常驻开关。 */
    fun startTemporary(context: Context): Boolean {
        if (!hasOverlayPermission(context)) return false
        temporaryStopToken++
        val intent = Intent(context, AiFloatingBubbleService::class.java)
            .putExtra(EXTRA_TEMPORARY, true)
        return runCatching {
            ContextCompat.startForegroundService(context, intent)
            true
        }.getOrDefault(false)
    }

    fun stopTemporary(context: Context) {
        if (isEnabled(context)) return
        temporaryStopToken++
        AiFloatingBubbleService.setAgentBusy(false)
        runCatching { context.stopService(Intent(context, AiFloatingBubbleService::class.java)) }
    }

    /** 敏感应用提示后让悬浮球多停留一会儿，方便用户继续回到 Mote AI。 */
    fun stopTemporaryDelayed(context: Context, delayMs: Long = 7_000L) {
        if (isEnabled(context)) return
        val token = ++temporaryStopToken
        Handler(Looper.getMainLooper()).postDelayed({
            if (token == temporaryStopToken) {
                AiFloatingBubbleService.setAgentBusy(false)
                runCatching {
                    context.stopService(Intent(context, AiFloatingBubbleService::class.java))
                }
            }
        }, delayMs)
    }

    fun stop(context: Context) {
        runCatching { context.stopService(Intent(context, AiFloatingBubbleService::class.java)) }
        aiPrefs(context).edit().putBoolean(KEY_FLOATING_ENABLED, false).apply()
    }

    fun requestOverlayPermission(context: Context) {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
