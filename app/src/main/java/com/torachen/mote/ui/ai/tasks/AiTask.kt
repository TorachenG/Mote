package com.torachen.mote.ui.ai.tasks

import com.torachen.mote.ui.ai.AiSource

internal data class AiTask(
    val id: String,
    val name: String,
    val instruction: String,
    val presetId: String,
    val trigger: AiTaskTrigger,
    val enabled: Boolean = true,
    val notifyOnFinish: Boolean = true,
    val retryMax: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastRunAt: Long? = null,
    val nextRunAt: Long? = null,
    val lastStatus: AiTaskRunStatus? = null,
)

internal sealed interface AiTaskTrigger {
    data object Manual : AiTaskTrigger
    data class Schedule(val spec: AiScheduleSpec) : AiTaskTrigger
    data class AppLaunch(val packageName: String? = null) : AiTaskTrigger
    data class Notification(val sourcePackage: String? = null) : AiTaskTrigger
}

internal sealed interface AiScheduleSpec {
    data class Once(val atMillis: Long) : AiScheduleSpec
    data class Every(val seconds: Long) : AiScheduleSpec
    data class Daily(val hour: Int, val minute: Int) : AiScheduleSpec
    data class Weekly(val days: Set<Int>, val hour: Int, val minute: Int) : AiScheduleSpec
}

internal enum class AiTaskRunStatus {
    Queued,
    Running,
    WaitingConfirmation,
    Succeeded,
    Failed,
    Cancelled,
    Skipped,
}

internal data class AiTaskRunStep(
    val title: String,
    val detail: String = "",
    val state: String,
    val startedAt: Long,
    val endedAt: Long? = null,
)

internal data class AiTaskRun(
    val id: String,
    val taskId: String,
    val taskName: String,
    val trigger: String,
    val status: AiTaskRunStatus,
    val scheduledAt: Long? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val providerName: String = "",
    val modelId: String = "",
    val steps: List<AiTaskRunStep> = emptyList(),
    val output: String = "",
    val error: String? = null,
    val sources: List<AiSource> = emptyList(),
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val estimated: Boolean = false,
    val retryOf: String? = null,
) {
    val durationMs: Long get() = (endedAt ?: System.currentTimeMillis()) - startedAt
}
