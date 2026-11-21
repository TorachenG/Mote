package com.torachen.mote.ui.ai.mcp

import android.content.Context
import com.torachen.mote.ui.ai.AiToolCall
import com.torachen.mote.ui.ai.AiToolOutcome
import com.torachen.mote.ui.ai.security.AiSecretStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * MCP server 连接池。
 *
 * 每个 server 一个协程；工具清单按“整代替换”更新，断线按指数退避重连。
 * 连接是懒加载的：真正要用某组 server 时才 ensureConnected，避免启动时连接所有服务。
 */
internal object McpManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Job>()
    private val clients = ConcurrentHashMap<String, McpClient>()
    private val runningConfigs = ConcurrentHashMap<String, McpServerConfig>()

    private val statusFlow = MutableStateFlow<Map<String, McpServerStatus>>(emptyMap())
    private val toolsFlow = MutableStateFlow<Map<String, List<McpToolDescriptor>>>(emptyMap())

    val statuses: StateFlow<Map<String, McpServerStatus>> = statusFlow.asStateFlow()
    val tools: StateFlow<Map<String, List<McpToolDescriptor>>> = toolsFlow.asStateFlow()

    @Volatile
    private var appContext: Context? = null

    fun start(context: Context) {
        appContext = context.applicationContext
        McpStore.ensureLoaded(context)
        // 不在这里连接全部 server：启动时无网络/服务不可用会拖慢冷启动。
        // 对话前由 awaitReady/ensureConnected 懒加载；设置页打开时显式 refresh。
    }

    /** 配置变更后调用：停掉删除/禁用/改配置的 server，启动新增的。 */
    fun refresh(context: Context) {
        val configs = McpStore.all(context)
        val ids = configs.map { it.id }.toSet()
        jobs.keys.filter { it !in ids }.forEach { id ->
            stopServer(id)
            statusFlow.update { it - id }
        }
        configs.forEach { config ->
            val previous = runningConfigs[config.id]
            if (!config.enabled) {
                stopServer(config.id)
                updateStatus(config.id, McpServerStatus.Disabled)
                return@forEach
            }
            if (previous != null && previous != config) {
                stopServer(config.id)
            }
            if (jobs[config.id] == null) startServer(context, config)
        }
    }

    fun ensureConnected(context: Context, serverIds: Set<String>? = null) {
        McpStore.all(context)
            .filter { it.enabled && (serverIds == null || it.id in serverIds) }
            .forEach { startServer(context, it) }
    }

    /**
     * 首次对话前短暂等待 MCP 连接/工具发现，避免第一轮看不到工具。
     * 超时后照常继续，模型只看到已就绪的工具。
     */
    suspend fun awaitReady(
        context: Context,
        serverIds: Set<String>? = null,
        timeoutMs: Long = 4_000L,
    ) {
        val configs = McpStore.all(context)
            .filter { it.enabled && (serverIds == null || it.id in serverIds) }
        if (configs.isEmpty()) return
        ensureConnected(context, serverIds)
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val statuses = statusFlow.value
            val settled = configs.all { config ->
                when (val status = statuses[config.id]) {
                    is McpServerStatus.Ready, is McpServerStatus.Failed -> true
                    else -> false
                }
            }
            if (settled) return
            delay(100L)
        }
    }

    fun enabledConfigs(context: Context): List<McpServerConfig> =
        McpStore.all(context).filter { it.enabled }

    fun specsFor(
        context: Context,
        serverIds: Set<String>? = null,
    ): List<AiToolSpec> {
        val configs = McpStore.all(context).associateBy { it.id }
        return toolsFlow.value.entries
            .filter { (id, _) -> configs[id]?.enabled == true && (serverIds == null || id in serverIds) }
            .flatMap { (serverId, descriptors) ->
                val config = configs[serverId] ?: return@flatMap emptyList()
                descriptors
                    .filter { it.rawName !in config.disabledTools }
                    .map { descriptor ->
                        McpToolAdapter.toSpec(
                            descriptor = descriptor,
                            policy = config.toolPolicies[descriptor.rawName] ?: McpToolPolicy.Ask,
                            serverName = config.displayName,
                        )
                    }
            }
    }

    /** MCP 资源读取工具（跨 server）；没有启用的 server 时不出现在工具清单。 */
    fun resourceSpecs(context: Context, serverIds: Set<String>? = null): List<AiToolSpec> {
        if (McpStore.all(context).none {
                it.enabled && (serverIds == null || it.id in serverIds)
            }
        ) {
            return emptyList()
        }
        val listSchema = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject().put("server", JSONObject().put("type", "string")),
            )
            .put("additionalProperties", false)
        val readSchema = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject()
                    .put("server", JSONObject().put("type", "string"))
                    .put("uri", JSONObject().put("type", "string")),
            )
            .put("required", org.json.JSONArray().put("server").put("uri"))
            .put("additionalProperties", false)
        return listOf(
            AiToolSpec(
                name = "mcp_list_resources",
                title = "列出 MCP 资源",
                summary = "列出已连接 MCP server 暴露的资源（可选指定 server）。",
                params = "server（可选，server id 或 namespace）",
                group = AiToolGroup.MCP,
                inputSchema = listSchema,
                source = AiToolSource.Mcp,
                untrustedOutput = true,
            ),
            AiToolSpec(
                name = "mcp_read_resource",
                title = "读取 MCP 资源",
                summary = "按 URI 读取 MCP server 的资源正文。",
                params = "server（必填）、uri（必填）",
                group = AiToolGroup.MCP,
                inputSchema = readSchema,
                source = AiToolSource.Mcp,
                untrustedOutput = true,
            ),
        )
    }

    suspend fun executeResourceTool(
        context: Context,
        call: AiToolCall,
        serverIds: Set<String>? = null,
    ): AiToolOutcome {
        val requested = call.string("server")
        val configs = McpStore.all(context).filter {
            it.enabled && (serverIds == null || it.id in serverIds) &&
                    (requested.isBlank() || it.id == requested ||
                            it.namespace.equals(requested, ignoreCase = true) ||
                            it.displayName.equals(requested, ignoreCase = true))
        }
        if (configs.isEmpty()) {
            return AiToolOutcome(
                text = "没有匹配的 MCP server；请先在 AI 设置中启用并连接 MCP 服务。",
                title = "MCP 资源不可用",
                failed = true,
            )
        }
        return try {
            when (call.name) {
                "mcp_list_resources" -> {
                    val body = buildString {
                        configs.forEach { config ->
                            val client = clients[config.id]
                            if (client == null) {
                                append("【").append(config.displayName).append("】未连接\n")
                                return@forEach
                            }
                            val resources = client.listResources()
                            append("【").append(config.displayName)
                                .append(" / ").append(config.namespace).append("】")
                                .append(resources.size).append(" 个资源\n")
                            resources.forEach { resource ->
                                append("- ").append(resource.uri)
                                append("  ").append(resource.name)
                                if (resource.mimeType.isNotBlank()) append("  [").append(resource.mimeType).append(']')
                                if (resource.description.isNotBlank()) {
                                    append("\n  ").append(resource.description.take(200))
                                }
                                append('\n')
                            }
                        }
                    }.ifBlank { "没有可读取的 MCP 资源。" }
                    AiToolOutcome(
                        text = "<untrusted_mcp_resources>\n" + body.take(8_000) +
                                "\n</untrusted_mcp_resources>",
                        title = "已列出 MCP 资源",
                    )
                }

                "mcp_read_resource" -> {
                    val uri = call.string("uri")
                    if (uri.isBlank()) throw McpException(
                        McpFailureReason.Protocol,
                        "缺少资源 URI",
                        retryable = false,
                    )
                    val config = configs.first()
                    val client = clients[config.id] ?: throw McpException(
                        McpFailureReason.Network,
                        "MCP server「${config.displayName}」未连接",
                    )
                    val text = client.readResource(uri)
                    AiToolOutcome(
                        text = "<untrusted_mcp_resource server=\"${config.namespace}\" uri=\"$uri\">\n" +
                                text + "\n</untrusted_mcp_resource>",
                        title = "已读取 MCP 资源",
                    )
                }

                else -> AiToolOutcome("未知 MCP 资源操作。", "未知 MCP 资源操作", failed = true)
            }
        } catch (cancel: kotlinx.coroutines.CancellationException) {
            throw cancel
        } catch (throwable: Throwable) {
            val error = McpErrors.wrap(throwable)
            AiToolOutcome(
                text = "MCP 资源读取失败：${error.message.orEmpty().take(500)}",
                title = "MCP 资源读取失败",
                failed = true,
            )
        }
    }

    /** 已连接 server 的 instructions；模型提示中作为不可信外部说明展示。 */
    fun instructionsFor(context: Context, serverIds: Set<String>? = null): List<Pair<String, String>> =
        McpStore.all(context)
            .filter { it.enabled && (serverIds == null || it.id in serverIds) }
            .mapNotNull { config ->
                val text = clients[config.id]?.instructions?.trim().orEmpty()
                if (text.isBlank()) null else config.displayName to text
            }

    suspend fun executeTool(
        context: Context,
        call: AiToolCall,
        supportsVision: Boolean,
    ): AiToolOutcome {
        val descriptor = toolsFlow.value.values
            .flatten()
            .firstOrNull { it.publicName == call.name }
            ?: return AiToolOutcome(
                text = "MCP 工具「${call.name}」不在当前已连接的工具清单里，请重新连接服务。",
                title = "MCP 工具不可用",
                failed = true,
            )
        val client = clients[descriptor.serverId]
            ?: return AiToolOutcome(
                text = "MCP 服务「${descriptor.namespace}」未连接，请先在 AI 设置中测试连接。",
                title = "MCP 未连接",
                failed = true,
            )
        return try {
            val result = client.callTool(descriptor.rawName, call.arguments)
            McpToolAdapter.toOutcome(descriptor, result, supportsVision)
        } catch (cancel: kotlinx.coroutines.CancellationException) {
            throw cancel
        } catch (throwable: Throwable) {
            val error = McpErrors.wrap(throwable)
            if (error.retryable) scheduleReconnect(context, descriptor.serverId)
            AiToolOutcome(
                text = "MCP 工具「${descriptor.title}」调用失败：${error.message.orEmpty().take(500)}",
                title = "MCP 调用失败",
                failed = true,
            )
        }
    }

    suspend fun probe(config: McpServerConfig): McpProbeResult {
        val context = appContext ?: return McpProbeResult(
            ok = false,
            error = "应用上下文未初始化",
            failureReason = McpFailureReason.Config,
        )
        return McpClient.probe(config) { ref -> AiSecretStore.get(context, ref) }
    }

    fun setToolPolicy(context: Context, serverId: String, rawName: String, policy: McpToolPolicy) {
        val config = McpStore.byId(context, serverId) ?: return
        val updated = config.copy(
            toolPolicies = config.toolPolicies + (rawName to policy),
            updatedAt = System.currentTimeMillis(),
        )
        McpStore.upsert(context, updated)
        // 工具描述本身不变，但下次构建 AiToolSpec 时会读到新 policy。
    }

    fun restart(context: Context, serverId: String) {
        val config = McpStore.byId(context, serverId) ?: return
        stopServer(serverId)
        if (config.enabled) startServer(context, config)
    }

    fun stopAll() {
        jobs.keys.toList().forEach { stopServer(it) }
    }

    fun hasShizuku(): Boolean = McpStdioShizuku.hasPermission()

    fun shizukuAvailable(): Boolean = McpStdioShizuku.binderAlive()

    fun requestShizukuPermission(): Boolean = McpStdioShizuku.requestPermission()

    private fun startServer(context: Context, config: McpServerConfig) {
        if (jobs[config.id] != null) return
        runningConfigs[config.id] = config
        jobs[config.id] = scope.launch {
            var attempt = 0
            while (isActive) {
                updateStatus(
                    config.id,
                    if (attempt == 0) McpServerStatus.Connecting(0)
                    else McpServerStatus.Connecting(attempt),
                )
                var client: McpClient? = null
                try {
                    client = McpClient(
                        config = config,
                        secretResolver = { ref -> AiSecretStore.get(context, ref) },
                    )
                    val connected = client.connect()
                    clients[config.id]?.close()
                    clients[config.id] = connected
                    var tools = connected.listTools()
                    toolsFlow.update { it + (config.id to tools) }
                    updateStatus(
                        config.id,
                        McpServerStatus.Ready(
                            toolCount = tools.size,
                            connectedAt = System.currentTimeMillis(),
                            serverInfo = connected.serverInfo,
                            protocolVersion = connected.protocolVersion,
                        ),
                    )
                    attempt = 0

                    if (config.transport == McpTransport.StreamableHttp) {
                        // 无长连接通知通道；连接由每次 request 维持，失败时由 executeTool 触发重连。
                        awaitCancellation()
                    }
                    connected.notifications().collect { notification ->
                        if (notification.optString("method") == "notifications/tools/list_changed") {
                            tools = connected.listTools()
                            toolsFlow.update { it + (config.id to tools) }
                            updateStatus(
                                config.id,
                                McpServerStatus.Ready(
                                    toolCount = tools.size,
                                    connectedAt = System.currentTimeMillis(),
                                    serverInfo = connected.serverInfo,
                                    protocolVersion = connected.protocolVersion,
                                ),
                            )
                        }
                    }
                    throw McpException(McpFailureReason.Network, "MCP 连接已断开")
                } catch (cancel: kotlinx.coroutines.CancellationException) {
                    throw cancel
                } catch (throwable: Throwable) {
                    val error = McpErrors.wrap(throwable)
                    clients.remove(config.id)?.close()
                    toolsFlow.update { it - config.id }
                    attempt++
                    val reconnect = config.reconnect
                    val nonRetryable = !error.retryable || error.kind in setOf(
                        McpFailureReason.Config,
                        McpFailureReason.Auth,
                        McpFailureReason.ShizukuUnavailable,
                        McpFailureReason.Unsupported,
                    )
                    if (nonRetryable || !reconnect.enabled || attempt > reconnect.maxAttempts) {
                        updateStatus(
                            config.id,
                            McpServerStatus.Failed(
                                reason = if (reconnect.enabled) McpFailureReason.ReconnectExhausted else error.kind,
                                message = error.message.orEmpty().take(500),
                                retryable = reconnect.enabled,
                            ),
                        )
                        break
                    }
                    val delayMs = (reconnect.initialDelayMs * (1L shl (attempt - 1).coerceAtMost(10)))
                        .coerceAtMost(reconnect.maxDelayMs)
                    updateStatus(
                        config.id,
                        McpServerStatus.Reconnecting(
                            attempt = attempt,
                            maxAttempts = reconnect.maxAttempts,
                            nextRetryAt = System.currentTimeMillis() + delayMs,
                            lastError = error.message.orEmpty().take(500),
                        ),
                    )
                    delay(delayMs)
                }
            }
            // 失败耗尽/主动 break 后允许下一次 ensureConnected 重新启动。
            jobs.remove(config.id)
        }
    }

    private fun scheduleReconnect(context: Context, serverId: String) {
        val config = McpStore.byId(context, serverId) ?: return
        if (!config.enabled) return
        stopServer(serverId)
        if (config.enabled) startServer(context, config)
    }

    private fun stopServer(id: String) {
        jobs.remove(id)?.cancel()
        runningConfigs.remove(id)
        clients.remove(id)?.close()
        toolsFlow.update { it - id }
    }

    private fun updateStatus(id: String, status: McpServerStatus) {
        statusFlow.update { it + (id to status) }
    }
}
