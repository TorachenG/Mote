package com.torachen.mote.ui.ai.tasks

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.torachen.mote.ui.ai.AiFileEditRequest
import com.torachen.mote.ui.ai.AiSource
import com.torachen.mote.ui.ai.AiStepState
import com.torachen.mote.ui.ai.AiAgentRunner
import com.torachen.mote.ui.ai.mcp.AiToolApprovalDecision
import com.torachen.mote.ui.ai.preset.AiPresetResolver
import com.torachen.mote.ui.ai.usage.AiRequestUsage
import com.torachen.mote.ui.ai.usage.AiUsageRecorder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 任务执行前台服务。
 *
 * 一次只执行一个任务，避免两个 Agent 同时占用无障碍/悬浮窗。
 * 所有工具审批 fail-closed；敏感操作不会在后台静默执行。
 */
class AiTaskService : Service() {

    companion object {
        const val ACTION_STOP = "com.torachen.mote.ai.task.STOP"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_RUN_ID = "run_id"
        const val EXTRA_RETRY_OF = "retry_of"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queue = Channel<String>(Channel.UNLIMITED)
    private var runnerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        AiTaskNotifications.ensureChannel(this)
        scope.launch {
            for (taskId in queue) {
                executeTask(taskId)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            runnerJob?.cancel()
            stopSelf()
            return START_NOT_STICKY
        }
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID)
        if (taskId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }
        val task = AiTaskStore.byId(this, taskId)
        if (task == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(
            this,
            AiTaskNotifications.runningId(taskId),
            AiTaskNotifications.running(this, task),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
        queue.trySend(taskId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun executeTask(taskId: String) {
        runnerJob = kotlin.coroutines.coroutineContext[Job]
        val task = AiTaskStore.byId(this, taskId) ?: return
        if (!task.enabled) return
        val agent = AiPresetResolver.resolvePreset(this, task.presetId)
        val startedAt = System.currentTimeMillis()
        val runId = UUID.randomUUID().toString().replace("-", "").take(12)
        val steps = mutableListOf<AiTaskRunStep>()
        val sources = mutableListOf<AiSource>()
        var inputTokens = 0
        var outputTokens = 0
        var estimated = false
        var output = ""
        var error: String? = null
        var status = AiTaskRunStatus.Failed

        val run = AiTaskRun(
            id = runId,
            taskId = task.id,
            taskName = task.name,
            trigger = triggerName(task.trigger),
            status = AiTaskRunStatus.Running,
            scheduledAt = task.nextRunAt,
            startedAt = startedAt,
            providerName = agent?.provider?.name.orEmpty(),
            modelId = agent?.provider?.model.orEmpty(),
        )
        AiTaskStore.appendRun(this, run)

        try {
            if (agent == null) {
                throw IllegalStateException("没有可用的 AI 服务，请先在设置中配置")
            }
            if (!agent.backgroundAllowed) {
                throw IllegalStateException("当前预设不允许后台任务，请在 Agent 预设中开启“允许后台任务使用”")
            }
            if (!agent.provider.isConfigured) {
                throw IllegalStateException("当前预设的 AI 服务未配置 API Key 或模型")
            }
            val result = AiAgentRunner.run(
                context = this,
                provider = agent.provider,
                systemPrompt = agent.systemPrompt + "\n\n" + agent.reasoning.instruction,
                history = emptyList(),
                userRequest = task.instruction,
                userImages = emptyList(),
                files = emptyList(),
                permission = agent.filePermission,
                reasoning = agent.reasoning,
                onThought = {},
                onStep = { step ->
                    steps.add(
                        AiTaskRunStep(
                            title = step.title,
                            detail = step.detail,
                            state = step.state.name,
                            startedAt = System.currentTimeMillis(),
                            endedAt = if (step.state == AiStepState.Running) null
                            else System.currentTimeMillis(),
                        ),
                    )
                },
                onContent = {},
                onCall = {},
                onConfirmEdit = { _: AiFileEditRequest -> false },
                onNotice = {},
                onGeneratedFile = { file ->
                    steps.add(
                        AiTaskRunStep(
                            title = "已生成文件：${file.name}",
                            state = AiStepState.Done.name,
                            startedAt = System.currentTimeMillis(),
                            endedAt = System.currentTimeMillis(),
                        ),
                    )
                },
                onSources = { list -> sources.addAll(list) },
                deviceControlEnabled = agent.deviceControl,
                allowedTools = agent.allowedTools,
                mcpServerIds = agent.mcpServerIds,
                onApproveTool = { AiToolApprovalDecision.Deny },
                onRequestUsage = { request ->
                    val usage = request.usage
                    if (usage == null) {
                        estimated = true
                        inputTokens += (request.estimatedInputChars + 3) / 4
                        outputTokens += (request.estimatedOutputChars + 3) / 4
                    } else {
                        inputTokens += usage.inputTokens
                        outputTokens += usage.outputTokens
                        if (usage.estimated) estimated = true
                    }
                    AiUsageRecorder.record(
                        context = this,
                        request = request,
                        conversationId = null,
                        messageId = null,
                        presetId = agent.presetId,
                        provider = agent.provider,
                    )
                },
            )
            output = result.text
            status = AiTaskRunStatus.Succeeded
        } catch (cancel: CancellationException) {
            status = AiTaskRunStatus.Cancelled
            error = "已取消"
        } catch (throwable: Throwable) {
            status = AiTaskRunStatus.Failed
            error = throwable.message?.take(500) ?: throwable.javaClass.simpleName
        } finally {
            val endedAt = System.currentTimeMillis()
            val finished = run.copy(
                status = status,
                endedAt = endedAt,
                steps = steps.toList(),
                output = output,
                error = error,
                sources = sources.toList(),
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                estimated = estimated,
            )
            AiTaskStore.appendRun(this, finished)
            val updated = task.copy(
                lastRunAt = endedAt,
                lastStatus = status,
                nextRunAt = null,
            )
            AiTaskStore.upsert(this, updated)
            if (task.enabled && task.trigger is AiTaskTrigger.Schedule) {
                val schedule = (task.trigger as AiTaskTrigger.Schedule).spec
                if (schedule is AiScheduleSpec.Once) {
                    AiTaskStore.upsert(this, updated.copy(enabled = false))
                } else {
                    AiTaskScheduler.schedule(this, updated)
                }
            }
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            if (task.notifyOnFinish) {
                AiTaskNotifications.notifyFinished(this, task, finished)
            }
            runnerJob = null
            stopSelf()
        }
    }

    private fun triggerName(trigger: AiTaskTrigger): String = when (trigger) {
        is AiTaskTrigger.Manual -> "手动"
        is AiTaskTrigger.Schedule -> "定时"
        is AiTaskTrigger.AppLaunch -> "应用启动"
        is AiTaskTrigger.Notification -> "通知点击"
    }
}
