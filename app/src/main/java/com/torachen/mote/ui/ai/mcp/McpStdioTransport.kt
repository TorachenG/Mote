package com.torachen.mote.ui.ai.mcp

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.lang.Process
import java.util.concurrent.ConcurrentHashMap

/**
 * stdio MCP transport：一行一个 JSON-RPC 消息。
 *
 * 通过 Shizuku 启动子进程后，stdin/stdout 就是普通的 Process 流；stderr 单独读取，
 * 仅用于诊断，不进入模型上下文。
 */
internal class McpStdioTransport(
    private val config: McpStdioConfig,
    private val resolvedEnv: Map<String, String>,
) : McpChannel {

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private val pending = ConcurrentHashMap<Long, CompletableDeferred<JSONObject>>()
    private val notificationFlow = MutableSharedFlow<JSONObject>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST,
    )
    private var scope: CoroutineScope? = null
    private var readerJob: Job? = null
    private var stderrJob: Job? = null
    private val stderr = StringBuilder()
    private val writeLock = Any()

    @Volatile
    private var closed = false

    override var sessionId: String? = null
    override var protocolVersion: String? = null

    override suspend fun start() {
        if (process != null) return
        val launched = McpStdioShizuku.start(config, resolvedEnv)
        process = launched
        writer = BufferedWriter(OutputStreamWriter(launched.outputStream, Charsets.UTF_8))
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = newScope
        readerJob = newScope.launch {
            readLoop(launched.inputStream.bufferedReader(Charsets.UTF_8))
        }
        stderrJob = newScope.launch {
            readStderr(launched.errorStream.bufferedReader(Charsets.UTF_8))
        }
    }

    override suspend fun request(payload: JSONObject, timeoutMs: Long): JSONObject? {
        if (closed) throw McpException(McpFailureReason.Cancelled, "stdio MCP 已关闭", retryable = false)
        val id = McpJsonRpc.responseId(payload)
            ?: throw McpException(McpFailureReason.Protocol, "请求缺少 JSON-RPC id", retryable = false)
        val deferred = CompletableDeferred<JSONObject>()
        pending[id] = deferred
        try {
            send(payload)
            return withTimeout(timeoutMs.coerceAtLeast(1_000L)) { deferred.await() }
        } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
            throw McpException(
                McpFailureReason.Timeout,
                "stdio MCP 请求超时（${timeoutMs}ms）",
                cause = timeout,
            )
        } finally {
            pending.remove(id)
        }
    }

    override suspend fun notify(payload: JSONObject) {
        if (closed) return
        send(payload)
    }

    override fun notifications(): Flow<JSONObject> = notificationFlow.asSharedFlow()

    override fun diagnostics(): String = synchronized(stderr) { stderr.toString() }

    private fun send(payload: JSONObject) {
        synchronized(writeLock) {
            val out = writer ?: throw McpException(
                McpFailureReason.Network,
                "stdio MCP 进程未启动",
            )
            runCatching {
                out.write(payload.toString())
                out.newLine()
                out.flush()
            }.getOrElse { error ->
                throw McpException(
                    McpFailureReason.Network,
                    "写入 stdio MCP 失败：${error.message ?: "进程已退出"}",
                    cause = error,
                )
            }
        }
    }

    private fun readLoop(reader: BufferedReader) {
        try {
            while (!closed) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                val obj = runCatching { JSONObject(line) }.getOrNull() ?: continue
                if (McpJsonRpc.isResponse(obj)) {
                    val id = McpJsonRpc.responseId(obj) ?: continue
                    pending.remove(id)?.complete(obj)
                } else if (McpJsonRpc.isNotification(obj)) {
                    notificationFlow.tryEmit(obj)
                }
            }
        } catch (_: Throwable) {
            // 进程退出或流关闭；下面统一结束 pending。
        } finally {
            failPending("stdio MCP 进程已退出")
        }
    }

    private fun readStderr(reader: BufferedReader) {
        try {
            while (!closed) {
                val line = reader.readLine() ?: break
                synchronized(stderr) {
                    if (stderr.length > 8_000) stderr.setLength(8_000)
                    stderr.append(line).append('\n')
                }
            }
        } catch (_: Throwable) {
            // stderr 只是诊断，不影响主流程。
        }
    }

    private fun failPending(message: String) {
        val error = McpException(McpFailureReason.Network, message)
        pending.values.forEach { it.completeExceptionally(error) }
        pending.clear()
    }

    override fun close() {
        if (closed) return
        closed = true
        failPending("stdio MCP 已关闭")
        runCatching { writer?.close() }
        writer = null
        scope?.cancel()
        scope = null
        readerJob = null
        stderrJob = null
        runCatching { process?.destroy() }
        process = null
    }
}
