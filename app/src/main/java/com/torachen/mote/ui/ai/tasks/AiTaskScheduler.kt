package com.torachen.mote.ui.ai.tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Calendar

internal object AiTaskScheduler {

    private const val EXTRA_TASK_ID = "task_id"
    private const val EXTRA_RUN_ID = "run_id"

    fun schedule(context: Context, task: AiTask): Boolean {
        cancel(context, task.id)
        val trigger = task.trigger
        if (!task.enabled || trigger !is AiTaskTrigger.Schedule) {
            AiTaskStore.upsert(context, task.copy(nextRunAt = null))
            return false
        }
        val next = nextOccurrence(trigger.spec, System.currentTimeMillis())
        if (next == null) {
            AiTaskStore.upsert(context, task.copy(nextRunAt = null))
            return false
        }
        val pending = pendingIntent(context, task.id)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val exact = canScheduleExact(manager)
        runCatching {
            if (exact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
            }
        }.onFailure { return false }
        AiTaskStore.upsert(context, task.copy(nextRunAt = next))
        return true
    }

    fun cancel(context: Context, taskId: String) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent(context, taskId))
    }

    fun rescheduleAll(context: Context) {
        AiTaskStore.all(context).forEach { task ->
            if (task.enabled && task.trigger is AiTaskTrigger.Schedule) {
                schedule(context, task)
            }
        }
    }

    fun runNow(context: Context, taskId: String, runId: String? = null) {
        startTaskService(context, taskId, runId)
    }

    fun startTaskService(context: Context, taskId: String, runId: String? = null) {
        val intent = Intent(context, AiTaskService::class.java)
            .putExtra(EXTRA_TASK_ID, taskId)
            .putExtra(EXTRA_RUN_ID, runId)
        runCatching { ContextCompat.startForegroundService(context, intent) }
    }

    fun nextOccurrence(spec: AiScheduleSpec, after: Long): Long? {
        val calendar = Calendar.getInstance()
        return when (spec) {
            is AiScheduleSpec.Once -> spec.atMillis.takeIf { it > after }
            is AiScheduleSpec.Every -> after + spec.seconds.coerceAtLeast(60L) * 1_000L
            is AiScheduleSpec.Daily -> {
                calendar.timeInMillis = after
                calendar.set(Calendar.HOUR_OF_DAY, spec.hour)
                calendar.set(Calendar.MINUTE, spec.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                if (calendar.timeInMillis <= after) calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }

            is AiScheduleSpec.Weekly -> {
                if (spec.days.isEmpty()) return null
                calendar.timeInMillis = after
                for (offset in 0..7) {
                    val candidate = (calendar.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, offset)
                        set(Calendar.HOUR_OF_DAY, spec.hour)
                        set(Calendar.MINUTE, spec.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val isoDay = candidate.get(Calendar.DAY_OF_WEEK).let {
                        if (it == Calendar.SUNDAY) 7 else it - 1
                    }
                    if (isoDay in spec.days && candidate.timeInMillis > after) {
                        return candidate.timeInMillis
                    }
                }
                null
            }
        }
    }

    fun canScheduleExact(manager: AlarmManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { manager.canScheduleExactAlarms() }.getOrDefault(false)
        } else {
            true
        }

    private fun pendingIntent(context: Context, taskId: String): PendingIntent {
        val intent = Intent(context, AiTaskAlarmReceiver::class.java)
            .putExtra(EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode() and 0x7fffffff,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    val taskIdExtra: String get() = EXTRA_TASK_ID
}

/** 定时触发：进入前台服务执行任务。 */
class AiTaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(AiTaskScheduler.taskIdExtra) ?: return
        val task = AiTaskStore.byId(context, taskId) ?: return
        if (!task.enabled) return
        AiTaskScheduler.startTaskService(context, taskId)
        // 单次任务执行后不再重复；周期任务在服务完成后重新排程。
    }
}

/** 开机/应用更新后恢复闹钟。 */
class AiTaskBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            AiTaskScheduler.rescheduleAll(context)
        }
    }
}
