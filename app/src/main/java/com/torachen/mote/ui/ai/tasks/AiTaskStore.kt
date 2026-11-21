package com.torachen.mote.ui.ai.tasks

import android.content.Context
import com.torachen.mote.ui.ai.AiSource
import com.torachen.mote.ui.ai.security.AiAtomicJsonFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal object AiTaskStore {

    private const val FILE_NAME = "ai_tasks.json"
    private const val RUN_DIR = "ai_task_runs"
    private const val MAX_RUNS_PER_TASK = 50
    private val lock = Any()
    private var loaded = false
    private val tasks = mutableListOf<AiTask>()

    private fun file(context: Context): File = File(context.applicationContext.filesDir, FILE_NAME)
    private fun runDir(context: Context): File = File(context.applicationContext.filesDir, RUN_DIR)

    @Synchronized
    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        tasks.clear()
        val raw = AiAtomicJsonFile.read(file(context)) ?: return
        val root = runCatching { JSONObject(raw) }.getOrNull() ?: return
        val array = root.optJSONArray("tasks") ?: return
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            decodeTask(obj)?.let(tasks::add)
        }
    }

    fun all(context: Context): List<AiTask> = synchronized(lock) {
        ensureLoaded(context)
        tasks.sortedByDescending { it.updatedAt }
    }

    fun byId(context: Context, id: String): AiTask? = synchronized(lock) {
        ensureLoaded(context)
        tasks.firstOrNull { it.id == id }
    }

    fun upsert(context: Context, task: AiTask): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val now = System.currentTimeMillis()
        val stored = task.copy(
            createdAt = if (task.createdAt > 0) task.createdAt else now,
            updatedAt = now,
        )
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) tasks[index] = stored else tasks.add(stored)
        persistLocked(context)
    }

    fun remove(context: Context, id: String): Boolean = synchronized(lock) {
        ensureLoaded(context)
        val removed = tasks.removeAll { it.id == id }
        if (removed) {
            File(runDir(context), id).deleteRecursively()
            persistLocked(context)
        }
        removed
    }

    fun appendRun(context: Context, run: AiTaskRun): Boolean {
        val dir = File(runDir(context), run.taskId).apply { mkdirs() }
        val written = AiAtomicJsonFile.write(
            File(dir, run.id + ".json"),
            encodeRun(run).toString(),
        )
        if (written) {
            dir.listFiles()?.sortedByDescending { it.lastModified() }
                ?.drop(MAX_RUNS_PER_TASK)
                ?.forEach { it.delete() }
        }
        return written
    }

    fun runs(context: Context, taskId: String, limit: Int = 20): List<AiTaskRun> {
        val dir = File(runDir(context), taskId)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?.take(limit)
            ?.mapNotNull { runCatching { decodeRun(JSONObject(it.readText())) }.getOrNull() }
            .orEmpty()
    }

    fun run(context: Context, taskId: String, runId: String): AiTaskRun? {
        val file = File(File(runDir(context), taskId), "$runId.json")
        return runCatching { decodeRun(JSONObject(file.readText())) }.getOrNull()
    }

    fun clearRuns(context: Context, taskId: String) {
        File(runDir(context), taskId).deleteRecursively()
    }

    private fun persistLocked(context: Context): Boolean {
        val array = JSONArray()
        tasks.forEach { array.put(encodeTask(it)) }
        return AiAtomicJsonFile.write(
            file(context),
            JSONObject().put("version", 1).put("tasks", array).toString(),
        )
    }

    private fun encodeTask(task: AiTask): JSONObject = JSONObject().apply {
        put("id", task.id)
        put("name", task.name)
        put("instruction", task.instruction)
        put("presetId", task.presetId)
        put("enabled", task.enabled)
        put("notifyOnFinish", task.notifyOnFinish)
        put("retryMax", task.retryMax)
        put("createdAt", task.createdAt)
        put("updatedAt", task.updatedAt)
        task.lastRunAt?.let { put("lastRunAt", it) }
        task.nextRunAt?.let { put("nextRunAt", it) }
        task.lastStatus?.let { put("lastStatus", it.name) }
        put("trigger", encodeTrigger(task.trigger))
    }

    private fun decodeTask(obj: JSONObject): AiTask? {
        val id = obj.optString("id").trim().ifBlank { return null }
        val trigger = decodeTrigger(obj.optJSONObject("trigger")) ?: return null
        return AiTask(
            id = id,
            name = obj.optString("name").ifBlank { "未命名任务" },
            instruction = obj.optString("instruction"),
            presetId = obj.optString("presetId").ifBlank { "builtin_default" },
            trigger = trigger,
            enabled = obj.optBoolean("enabled", true),
            notifyOnFinish = obj.optBoolean("notifyOnFinish", true),
            retryMax = obj.optInt("retryMax", 0).coerceIn(0, 5),
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
            lastRunAt = obj.optLong("lastRunAt", 0L).takeIf { it > 0L },
            nextRunAt = obj.optLong("nextRunAt", 0L).takeIf { it > 0L },
            lastStatus = runCatching {
                AiTaskRunStatus.valueOf(obj.optString("lastStatus"))
            }.getOrNull(),
        )
    }

    private fun encodeTrigger(trigger: AiTaskTrigger): JSONObject = JSONObject().apply {
        when (trigger) {
            is AiTaskTrigger.Manual -> put("type", "manual")
            is AiTaskTrigger.Schedule -> {
                put("type", "schedule")
                put("schedule", encodeSchedule(trigger.spec))
            }

            is AiTaskTrigger.AppLaunch -> {
                put("type", "app_launch")
                trigger.packageName?.let { put("package", it) }
            }

            is AiTaskTrigger.Notification -> {
                put("type", "notification")
                trigger.sourcePackage?.let { put("package", it) }
            }
        }
    }

    private fun decodeTrigger(obj: JSONObject?): AiTaskTrigger? {
        if (obj == null) return AiTaskTrigger.Manual
        return when (obj.optString("type")) {
            "schedule" -> decodeSchedule(obj.optJSONObject("schedule"))
                ?.let { AiTaskTrigger.Schedule(it) }

            "app_launch" -> AiTaskTrigger.AppLaunch(
                obj.optString("package").takeIf { it.isNotBlank() },
            )

            "notification" -> AiTaskTrigger.Notification(
                obj.optString("package").takeIf { it.isNotBlank() },
            )

            else -> AiTaskTrigger.Manual
        }
    }

    private fun encodeSchedule(spec: AiScheduleSpec): JSONObject = JSONObject().apply {
        when (spec) {
            is AiScheduleSpec.Once -> {
                put("kind", "once")
                put("at", spec.atMillis)
            }

            is AiScheduleSpec.Every -> {
                put("kind", "every")
                put("seconds", spec.seconds)
            }

            is AiScheduleSpec.Daily -> {
                put("kind", "daily")
                put("hour", spec.hour)
                put("minute", spec.minute)
            }

            is AiScheduleSpec.Weekly -> {
                put("kind", "weekly")
                put("days", JSONArray(spec.days.toList()))
                put("hour", spec.hour)
                put("minute", spec.minute)
            }
        }
    }

    private fun decodeSchedule(obj: JSONObject?): AiScheduleSpec? {
        if (obj == null) return null
        return when (obj.optString("kind")) {
            "once" -> AiScheduleSpec.Once(obj.optLong("at"))
            "every" -> AiScheduleSpec.Every(obj.optLong("seconds", 3600L).coerceAtLeast(60L))
            "daily" -> AiScheduleSpec.Daily(
                obj.optInt("hour", 9).coerceIn(0, 23),
                obj.optInt("minute", 0).coerceIn(0, 59),
            )

            "weekly" -> {
                val days = buildSet {
                    obj.optJSONArray("days")?.let { array ->
                        for (index in 0 until array.length()) {
                            array.optInt(index, 0).takeIf { it in 1..7 }?.let(::add)
                        }
                    }
                }
                AiScheduleSpec.Weekly(
                    days.ifEmpty { setOf(1) },
                    obj.optInt("hour", 9).coerceIn(0, 23),
                    obj.optInt("minute", 0).coerceIn(0, 59),
                )
            }

            else -> null
        }
    }

    private fun encodeRun(run: AiTaskRun): JSONObject = JSONObject().apply {
        put("id", run.id)
        put("taskId", run.taskId)
        put("taskName", run.taskName)
        put("trigger", run.trigger)
        put("status", run.status.name)
        run.scheduledAt?.let { put("scheduledAt", it) }
        put("startedAt", run.startedAt)
        run.endedAt?.let { put("endedAt", it) }
        put("providerName", run.providerName)
        put("modelId", run.modelId)
        put("output", run.output)
        run.error?.let { put("error", it) }
        put("inputTokens", run.inputTokens)
        put("outputTokens", run.outputTokens)
        put("estimated", run.estimated)
        run.retryOf?.let { put("retryOf", it) }
        put(
            "steps",
            JSONArray().apply {
                run.steps.forEach { step ->
                    put(
                        JSONObject().apply {
                            put("title", step.title)
                            put("detail", step.detail)
                            put("state", step.state)
                            put("startedAt", step.startedAt)
                            step.endedAt?.let { put("endedAt", it) }
                        },
                    )
                }
            },
        )
        put(
            "sources",
            JSONArray().apply {
                run.sources.forEach { source ->
                    put(
                        JSONObject().apply {
                            put("index", source.index)
                            put("url", source.url)
                            put("title", source.title)
                            put("siteName", source.siteName)
                            put("snippet", source.snippet)
                        },
                    )
                }
            },
        )
    }

    private fun decodeRun(obj: JSONObject): AiTaskRun {
        val steps = buildList {
            obj.optJSONArray("steps")?.let { array ->
                for (index in 0 until array.length()) {
                    val step = array.optJSONObject(index) ?: continue
                    add(
                        AiTaskRunStep(
                            title = step.optString("title"),
                            detail = step.optString("detail"),
                            state = step.optString("state"),
                            startedAt = step.optLong("startedAt"),
                            endedAt = step.optLong("endedAt", 0L).takeIf { it > 0L },
                        ),
                    )
                }
            }
        }
        val sources = buildList {
            obj.optJSONArray("sources")?.let { array ->
                for (index in 0 until array.length()) {
                    val source = array.optJSONObject(index) ?: continue
                    add(
                        AiSource(
                            index = source.optInt("index"),
                            url = source.optString("url"),
                            title = source.optString("title"),
                            siteName = source.optString("siteName"),
                            snippet = source.optString("snippet"),
                        ),
                    )
                }
            }
        }
        return AiTaskRun(
            id = obj.optString("id"),
            taskId = obj.optString("taskId"),
            taskName = obj.optString("taskName"),
            trigger = obj.optString("trigger"),
            status = runCatching {
                AiTaskRunStatus.valueOf(obj.optString("status"))
            }.getOrDefault(AiTaskRunStatus.Failed),
            scheduledAt = obj.optLong("scheduledAt", 0L).takeIf { it > 0L },
            startedAt = obj.optLong("startedAt"),
            endedAt = obj.optLong("endedAt", 0L).takeIf { it > 0L },
            providerName = obj.optString("providerName"),
            modelId = obj.optString("modelId"),
            steps = steps,
            output = obj.optString("output"),
            error = obj.optString("error").takeIf { it.isNotBlank() },
            sources = sources,
            inputTokens = obj.optInt("inputTokens"),
            outputTokens = obj.optInt("outputTokens"),
            estimated = obj.optBoolean("estimated"),
            retryOf = obj.optString("retryOf").takeIf { it.isNotBlank() },
        )
    }
}
