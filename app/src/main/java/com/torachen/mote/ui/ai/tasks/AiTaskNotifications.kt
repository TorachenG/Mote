package com.torachen.mote.ui.ai.tasks

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.torachen.mote.R
import com.torachen.mote.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object AiTaskNotifications {

    const val CHANNEL_ID = "ai_tasks"
    private const val RUNNING_ID_BASE = 7_100

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "AI 自动化任务",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "定时任务、后台运行与执行结果"
                },
            )
        }
    }

    fun running(context: Context, task: AiTask): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle("正在执行：${task.name}")
            .setContentText("Mote AI 正在后台完成任务，可在通知栏停止")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openApp(context))
            .addAction(0, "停止", stopIntent(context, task.id))
            .build()
    }

    fun notifyFinished(context: Context, task: AiTask, run: AiTaskRun) {
        ensureChannel(context)
        val success = run.status == AiTaskRunStatus.Succeeded
        val title = if (success) "任务完成：${task.name}" else "任务失败：${task.name}"
        val text = run.error?.take(180) ?: run.output.take(180).ifBlank { "没有输出" }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp(context))
            .build()
        runCatching {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(notificationId(task.id), notification)
        }
    }

    fun notifyWaiting(context: Context, task: AiTask) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle("任务需要确认：${task.name}")
            .setContentText("后台任务遇到需要用户确认的操作，已停止等待处理")
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        runCatching {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(notificationId(task.id), notification)
        }
    }

    fun runningId(taskId: String): Int = RUNNING_ID_BASE + (taskId.hashCode() and 0xff)

    fun notificationId(taskId: String): Int = 8_100 + (taskId.hashCode() and 0xff)

    fun stopIntent(context: Context, taskId: String): PendingIntent =
        PendingIntent.getService(
            context,
            taskId.hashCode() and 0xffff,
            Intent(context, AiTaskService::class.java)
                .setAction(AiTaskService.ACTION_STOP)
                .putExtra(AiTaskService.EXTRA_TASK_ID, taskId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun formatTime(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "未排程"
        return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
