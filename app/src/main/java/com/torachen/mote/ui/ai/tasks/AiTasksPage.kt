package com.torachen.mote.ui.ai.tasks

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.AiSettingsScaffold
import com.torachen.mote.ui.ai.preset.AiPresetStore
import com.torachen.mote.ui.ai.tasks.AiTaskRunStatus
import com.torachen.mote.ui.ai.tasks.AiTaskTrigger
import com.torachen.mote.ui.ai.tasks.AiScheduleSpec
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal object AiTaskNav {
    var editingId: String? = null
    var runTaskId: String? = null
    var runId: String? = null

    fun create() { editingId = null }
    fun edit(id: String) { editingId = id }
    fun runs(taskId: String) { runTaskId = taskId }
}

@Composable
fun AiTasksPage(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRuns: (String) -> Unit,
) {
    val context = LocalContext.current
    var reload by remember { mutableIntStateOf(0) }
    val tasks = remember(reload) { AiTaskStore.all(context) }
    val manager = context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
    val exactAllowed = AiTaskScheduler.canScheduleExact(manager)
    var notificationDenied by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notificationDenied = !granted }

    LaunchedEffect(Unit) {
        AiTaskScheduler.rescheduleAll(context)
        reload++
    }

    AiSettingsScaffold(
        title = "自动化任务",
        pageRoute = "ai_tasks",
        onBack = onBack,
        floatingBottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        AiTaskNav.create()
                        onEdit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("新建任务") }
            }
        },
    ) {
        if (!exactAllowed) {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                BasicComponent(
                    title = "精确闹钟未授权",
                    summary = "定时任务可能延迟。点此打开系统设置允许 Mote 使用精确闹钟。",
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:${context.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                )
            }
        }
        if (notificationDenied) {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                BasicComponent(
                    title = "通知权限未开启",
                    summary = "任务完成/失败的通知需要通知权限。点此请求。",
                    onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }
        }

        if (tasks.isEmpty()) {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "暂无自动化任务",
                        fontSize = 15.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            SmallTitle("任务列表")
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                tasks.forEach { task ->
                    SearchableItem(
                        title = task.name,
                        pageRoute = "ai_tasks",
                        pageTitle = "自动化任务",
                        itemKey = "ai_task_${task.id}",
                    ) {
                        BasicComponent(
                            title = task.name,
                            summary = buildString {
                                append(triggerLabel(task.trigger))
                                append(" · ")
                                append(task.lastStatus?.label ?: "未运行")
                                append(" · 下次 ")
                                append(AiTaskNotifications.formatTime(task.nextRunAt))
                            },
                            endActions = {
                                Switch(
                                    checked = task.enabled,
                                    onCheckedChange = { checked ->
                                        val updated = task.copy(enabled = checked)
                                        AiTaskStore.upsert(context, updated)
                                        if (checked) AiTaskScheduler.schedule(context, updated)
                                        else AiTaskScheduler.cancel(context, task.id)
                                        reload++
                                    },
                                )
                            },
                            onClick = {
                                AiTaskNav.edit(task.id)
                                onEdit()
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
fun AiTaskEditPage(onBack: () -> Unit, onRuns: (String) -> Unit) {
    val context = LocalContext.current
    val editingId = AiTaskNav.editingId
    val existing = remember(editingId) { editingId?.let { AiTaskStore.byId(context, it) } }
    val presets = remember { AiPresetStore.all(context) }

    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var instruction by remember { mutableStateOf(existing?.instruction.orEmpty()) }
    var presetId by remember { mutableStateOf(existing?.presetId ?: presets.firstOrNull()?.id.orEmpty()) }
    var triggerType by remember {
        mutableStateOf(
            when (existing?.trigger) {
                is AiTaskTrigger.Schedule -> when ((existing?.trigger as AiTaskTrigger.Schedule).spec) {
                    is AiScheduleSpec.Daily -> "每天"
                    is AiScheduleSpec.Every -> "间隔"
                    is AiScheduleSpec.Once -> "一次"
                    is AiScheduleSpec.Weekly -> "每周"
                }

                is AiTaskTrigger.AppLaunch -> "应用启动"
                is AiTaskTrigger.Notification -> "通知点击"
                else -> "手动"
            },
        )
    }
    var timeText by remember {
        mutableStateOf(
            ((existing?.trigger as? AiTaskTrigger.Schedule)?.spec as? AiScheduleSpec.Daily)
                ?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "09:00",
        )
    }
    var everyMinutes by remember {
        mutableStateOf(
            ((((existing?.trigger as? AiTaskTrigger.Schedule)?.spec as? AiScheduleSpec.Every)
                ?.seconds ?: 3600L) / 60L).toString(),
        )
    }
    var onceText by remember {
        mutableStateOf(
            ((existing?.trigger as? AiTaskTrigger.Schedule)?.spec as? AiScheduleSpec.Once)
                ?.atMillis
                ?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(it)) }
                .orEmpty(),
        )
    }
    var packageName by remember {
        mutableStateOf(
            when (val trigger = existing?.trigger) {
                is AiTaskTrigger.AppLaunch -> trigger.packageName.orEmpty()
                is AiTaskTrigger.Notification -> trigger.sourcePackage.orEmpty()
                else -> ""
            },
        )
    }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }
    var notifyOnFinish by remember { mutableStateOf(existing?.notifyOnFinish ?: true) }
    var retryMax by remember { mutableStateOf(existing?.retryMax ?: 0) }
    var showDelete by remember { mutableStateOf(false) }

    fun buildTrigger(): AiTaskTrigger? = when (triggerType) {
        "每天" -> {
            val parts = timeText.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull()
            val minute = parts.getOrNull(1)?.toIntOrNull()
            if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                null
            } else {
                AiTaskTrigger.Schedule(AiScheduleSpec.Daily(hour, minute))
            }
        }

        "间隔" -> {
            val minutes = everyMinutes.toLongOrNull()
            if (minutes == null || minutes < 1) null
            else AiTaskTrigger.Schedule(AiScheduleSpec.Every(minutes * 60L))
        }

        "一次" -> {
            val at = parseDateTime(onceText)
            if (at == null) null else AiTaskTrigger.Schedule(AiScheduleSpec.Once(at))
        }

        "应用启动" -> AiTaskTrigger.AppLaunch(packageName.trim().takeIf { it.isNotBlank() })
        "通知点击" -> AiTaskTrigger.Notification(packageName.trim().takeIf { it.isNotBlank() })
        else -> AiTaskTrigger.Manual
    }

    AiSettingsScaffold(
        title = if (existing == null) "新建自动化任务" else "编辑自动化任务",
        pageRoute = "ai_task_edit",
        onBack = onBack,
    ) {
        SmallTitle("任务")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TaskField("名称", name, { name = it }, "例如 每天早上总结新闻")
                TaskField(
                    "指令",
                    instruction,
                    { instruction = it },
                    "告诉 Agent 要做什么；它会使用预设里的工具和 MCP",
                    singleLine = false,
                    minHeight = 120.dp,
                )
                WindowDropdownPreference(
                    title = "Agent 预设",
                    summary = presets.firstOrNull { it.id == presetId }?.name ?: "选择预设",
                    items = presets.map { "${it.icon} ${it.name}" },
                    selectedIndex = presets.indexOfFirst { it.id == presetId }.coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        presets.getOrNull(index)?.let { presetId = it.id }
                    },
                )
            }
        }

        SmallTitle("触发方式")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WindowDropdownPreference(
                    title = "触发",
                    summary = triggerType,
                    items = listOf("手动", "每天", "间隔", "一次", "应用启动", "通知点击"),
                    selectedIndex = listOf("手动", "每天", "间隔", "一次", "应用启动", "通知点击")
                        .indexOf(triggerType).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        triggerType = listOf("手动", "每天", "间隔", "一次", "应用启动", "通知点击")
                            .getOrElse(index) { "手动" }
                    },
                )
                when (triggerType) {
                    "每天" -> TaskField("时间（HH:mm）", timeText, { timeText = it }, "09:00")
                    "间隔" -> TaskField("间隔（分钟）", everyMinutes, { everyMinutes = it }, "60")
                    "一次" -> TaskField("时间（yyyy-MM-dd HH:mm）", onceText, { onceText = it }, "2026-12-01 09:00")
                    "应用启动" -> TaskField(
                        "应用包名（留空 = Mote 自身启动）",
                        packageName,
                        { packageName = it },
                        "com.tencent.mm",
                    )

                    "通知点击" -> {
                        TaskField(
                            "来源应用包名",
                            packageName,
                            { packageName = it },
                            "com.tencent.mm",
                        )
                        Text(
                            "需要在系统“通知使用权”中允许 Mote；只有该应用的通知到达时触发。",
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                contentColor = MiuixTheme.colorScheme.onSurface,
                            ),
                        ) { Text("打开通知使用权设置") }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("启用", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("完成通知", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = notifyOnFinish, onCheckedChange = { notifyOnFinish = it })
                }
                TaskField("失败重试上限（0-5，保存后可在运行记录里手动重试）", "$retryMax", {
                    retryMax = it.toIntOrNull()?.coerceIn(0, 5) ?: 0
                }, "0")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = {
                    val trigger = buildTrigger()
                    if (trigger == null) {
                        Toast.makeText(context, "触发时间/间隔格式不正确", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (name.isBlank() || instruction.isBlank()) {
                        Toast.makeText(context, "请填写任务名称和指令", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val task = AiTask(
                        id = existing?.id ?: ("task_" + System.currentTimeMillis().toString(36)),
                        name = name.trim(),
                        instruction = instruction.trim(),
                        presetId = presetId,
                        trigger = trigger,
                        enabled = enabled,
                        notifyOnFinish = notifyOnFinish,
                        retryMax = retryMax,
                        createdAt = existing?.createdAt ?: 0L,
                    )
                    AiTaskStore.upsert(context, task)
                    AiTaskScheduler.cancel(context, task.id)
                    if (task.enabled) AiTaskScheduler.schedule(context, task)
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("保存") }
            if (existing != null) {
                Button(
                    onClick = { onRuns(existing.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("运行记录") }
            }
        }
        if (existing != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { AiTaskScheduler.runNow(context, existing.id) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("立即执行") }
            Button(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.error,
                ),
            ) { Text("删除任务") }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDelete && existing != null) {
        WindowDialog(
            show = true,
            title = "删除任务",
            summary = "删除后不再触发，运行记录也会一起删除。",
            onDismissRequest = { showDelete = false },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showDelete = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        AiTaskScheduler.cancel(context, existing.id)
                        AiTaskStore.remove(context, existing.id)
                        showDelete = false
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("删除") }
            }
        }
    }
}

@Composable
fun AiTaskRunsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val taskId = AiTaskNav.runTaskId ?: return
    var reload by remember { mutableIntStateOf(0) }
    val runs = remember(taskId, reload) { AiTaskStore.runs(context, taskId, 50) }
    var selected by remember { mutableStateOf<AiTaskRun?>(null) }

    AiSettingsScaffold(
        title = "运行记录",
        pageRoute = "ai_task_runs",
        onBack = onBack,
    ) {
        if (runs.isEmpty()) {
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    "还没有运行记录。",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                runs.forEach { run ->
                    BasicComponent(
                        title = run.status.label,
                        summary = buildString {
                            append(formatDateTime(run.startedAt))
                            append(" · ")
                            append(run.durationMs / 1000)
                            append("s")
                            if (run.inputTokens + run.outputTokens > 0) {
                                append(" · ")
                                append(run.inputTokens + run.outputTokens)
                                append(" token")
                            }
                            if (!run.error.isNullOrBlank()) {
                                append(" · ")
                                append(run.error.take(80))
                            }
                        },
                        onClick = { selected = run },
                    )
                }
            }
        }
        Button(
            onClick = { reload++ },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.secondaryContainer,
                contentColor = MiuixTheme.colorScheme.onSurface,
            ),
        ) { Text("刷新") }
        Spacer(Modifier.height(24.dp))
    }

    selected?.let { run ->
        WindowDialog(
            show = true,
            title = run.taskName,
            summary = run.status.label + " · " + formatDateTime(run.startedAt),
            onDismissRequest = { selected = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                run.error?.let {
                    Text("错误：$it", fontSize = 12.sp, color = MiuixTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                run.steps.forEach { step ->
                    Text(
                        "• ${step.title}" + if (step.detail.isNotBlank()) "：${step.detail.take(200)}" else "",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                if (run.output.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(run.output.take(4_000), fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        selected = null
                        AiTaskScheduler.runNow(context, taskId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("重试任务") }
            }
        }
    }
}

@Composable
private fun TaskField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .background(MiuixTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    placeholder,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface),
                singleLine = singleLine,
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            )
        }
    }
}

private fun parseDateTime(value: String): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(value)?.time
}.getOrNull()

private fun triggerLabel(trigger: AiTaskTrigger): String = when (trigger) {
    is AiTaskTrigger.Manual -> "手动"
    is AiTaskTrigger.Schedule -> when (val spec = trigger.spec) {
        is AiScheduleSpec.Once -> "一次"
        is AiScheduleSpec.Every -> "每 ${spec.seconds / 60} 分钟"
        is AiScheduleSpec.Daily -> "每天 %02d:%02d".format(spec.hour, spec.minute)
        is AiScheduleSpec.Weekly -> "每周"
    }

    is AiTaskTrigger.AppLaunch -> "应用启动"
    is AiTaskTrigger.Notification -> "通知点击"
}

private val AiTaskRunStatus.label: String
    get() = when (this) {
        AiTaskRunStatus.Queued -> "排队中"
        AiTaskRunStatus.Running -> "运行中"
        AiTaskRunStatus.WaitingConfirmation -> "等待确认"
        AiTaskRunStatus.Succeeded -> "成功"
        AiTaskRunStatus.Failed -> "失败"
        AiTaskRunStatus.Cancelled -> "已取消"
        AiTaskRunStatus.Skipped -> "已跳过"
    }

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
