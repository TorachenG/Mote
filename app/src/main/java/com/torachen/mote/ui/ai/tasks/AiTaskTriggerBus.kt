package com.torachen.mote.ui.ai.tasks

import android.content.Context

/**
 * 应用启动 / 通知点击触发。
 *
 * 同一来源 60 秒内只触发一次，避免切前后台或通知刷新造成连环执行。
 */
internal object AiTaskTriggerBus {

    private const val THROTTLE_MS = 60_000L
    private val lastTrigger = mutableMapOf<String, Long>()

    fun onAppForeground(context: Context, packageName: String, isMote: Boolean = false) {
        val now = System.currentTimeMillis()
        val key = "app:$packageName"
        if (now - (lastTrigger[key] ?: 0L) < THROTTLE_MS) return
        lastTrigger[key] = now
        val tasks = AiTaskStore.all(context).filter { task ->
            task.enabled && (task.trigger as? AiTaskTrigger.AppLaunch)?.let { trigger ->
                if (trigger.packageName.isNullOrBlank()) isMote else trigger.packageName == packageName
            } == true
        }
        tasks.forEach { AiTaskScheduler.startTaskService(context, it.id) }
    }

    fun onNotification(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        val now = System.currentTimeMillis()
        val key = "notification:$packageName"
        if (now - (lastTrigger[key] ?: 0L) < THROTTLE_MS) return
        lastTrigger[key] = now
        val tasks = AiTaskStore.all(context).filter { task ->
            task.enabled && (task.trigger as? AiTaskTrigger.Notification)
                ?.sourcePackage?.equals(packageName, ignoreCase = true) == true
        }
        tasks.forEach { AiTaskScheduler.startTaskService(context, it.id) }
    }
}
