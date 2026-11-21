package com.torachen.mote.ui.ai.mcp

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * HTTP 系 MCP 传输。
 *
 * - StreamableHttp：POST JSON-RPC；响应可能是 application/json，也可能是 text/event-stream。
 * - LegacySse：GET 建立 SSE 流，先等 endpoint 事件，再向该 endpoint POST 消息。
 *
 * 只依赖 OkHttp，不引入额外 SSE/MCP SDK。
 */
internal class McpHttpTransport(
    private val config: McpServerConfig,
    authValue: String?,
    secretHeaderValues: Map<String, String>,
    private val secretValues: List<String>,
) : McpChannel {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.SECONDS)
        .build()

    private val extraHeaders: Map<String, String> = buildMap {
        config.headers.forEach { header ->
            if (header.name.isBlank()) return@forEach
            val value = if (header.secretRef != null) {
                secretHeaderValues[header.name].orEmpty()
            } else {
                header.value
            }
            if (value.isNotBlank()) put(header.name, value)
        }
        when (val auth = config.auth) {
            is McpAuth.None -> Unit
            is McpAuth.Bearer -> {
                if (!authValue.isNullOrBlank()) put("Authorization", "Bearer $authValue")
            }

            is McpAuth.Header -> {
                if (!authValue.isNullOrBlank()) put(auth.name, authValue)
            }
        }
    }

    private val notificationFlow = MutableSharedFlow<JSONObject>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST,
    )
    private val pending = ConcurrentHashMap<Long, CompletableDeferred<JSONObject>>()
    private var scope: CoroutineScope? = null
    private var legacyCall: Call? = null
    private var legacyResponse: Response? = null
    private var messageEndpoint: String? = null

    @Volatile
    private var closed = false

    override var sessionId: String? = null
    override var protocolVersion: String? = null

    override suspend fun start() {
        if (config.transport != McpTransport.LegacySse) return
        if (messageEndpoint != null) return
        val request = Request.Builder()
            .url(config.url)
            .get()
            .header("Accept", "text/event-stream")
            .apply { extraHeaders.forEach { (k, v) -> header(k, v) } }
            .build()
        val call = client.newCall(request)
        legacyCall = call
        val response = execute(call)
        if (!response.isSuccessful) {
            val body = runCatching { response.body?.string().orEmpty() }.getOrDefault("")
            response.close()
            throw McpErrors.http(response.code, McpErrors.redact(body, secretValues))
        }
        legacyResponse = response
        val source = response.body?.source()
            ?: throw McpException(McpFailureReason.Protocol, "MCP SSE 响应为空", retryable = false)
        val endpoint = CompletableDeferred<String>()
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = newScope
        newScope.launch { readLegacyStream(source, endpoint) }
        messageEndpoint = withTimeout(config.startupTimeoutMs.coerceAtLeast(1_000L)) {
            endpoint.await()
        }
    }

    override suspend fun request(payload: JSONObject, timeoutMs: Long): JSONObject? {
        if (closed) throw McpException(McpFailureReason.Cancelled, "MCP 已关闭", retryable = false)
        val id = McpJsonRpc.responseId(payload)
        if (config.transport == McpTransport.LegacySse) {
            if (id == null) {
                postLegacy(payload)
                return null
            }
            val deferred = CompletableDeferred<JSONObject>()
            pending[id] = deferred
            try {
                postLegacy(payload)
                return withTimeout(timeoutMs.coerceAtLeast(1_000L)) { deferred.await() }
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                throw McpException(
                    McpFailureReason.Timeout,
                    "MCP 请求超时（${timeoutMs}ms）",
                    cause = timeout,
                )
            } finally {
                pending.remove(id)
            }
        }

        val response = execute(buildPostRequest(config.url, payload), timeoutMs)
        response.use { res ->
            updateSession(res)
            if (!res.isSuccessful) {
                val body = runCatching { res.body?.string().orEmpty() }.getOrDefault("")
                throw McpErrors.http(res.code, McpErrors.redact(body, secretValues))
            }
            if (id == null || res.code == 202 || res.code == 204) return null
            val mediaType = res.body?.contentType()?.toString().orEmpty()
            if (mediaType.contains("text/event-stream", ignoreCase = true)) {
                return readStreamableSseResponse(res, id, timeoutMs)
            }
            val body = res.body?.string().orEmpty()
            return runCatching { JSONObject(body) }.getOrElse {
                throw McpException(
                    McpFailureReason.Protocol,
                    "MCP 返回了非 JSON 响应：${body.take(200)}",
                    retryable = false,
                )
            }
        }
    }

    override suspend fun notify(payload: JSONObject) {
        if (closed) return
        if (config.transport == McpTransport.LegacySse) {
            postLegacy(payload)
            return
        }
        val response = execute(buildPostRequest(config.url, payload))
        response.use { res ->
            updateSession(res)
            if (!res.isSuccessful) {
                val body = runCatching { res.body?.string().orEmpty() }.getOrDefault("")
                throw McpErrors.http(res.code, McpErrors.redact(body, secretValues))
            }
        }
    }

    override fun notifications(): Flow<JSONObject> = notificationFlow.asSharedFlow()

    private suspend fun readStreamableSseResponse(
        response: Response,
        expectedId: Long,
        timeoutMs: Long,
    ): JSONObject = withTimeout(timeoutMs.coerceAtLeast(1_000L)) {
        withContext(Dispatchers.IO) {
            val source = response.body?.source()
                ?: throw McpException(McpFailureReason.Protocol, "MCP SSE 响应为空")
            val parser = McpSseParser()
            while (true) {
                val line = source.readUtf8Line() ?: break
                val event = parser.feed(line) ?: continue
                val obj = runCatching { JSONObject(event.data) }.getOrNull() ?: continue
                if (McpJsonRpc.isResponse(obj) && McpJsonRpc.responseId(obj) == expectedId) {
                    return@withContext obj
                }
                if (McpJsonRpc.isNotification(obj)) {
                    notificationFlow.tryEmit(obj)
                }
            }
            throw McpException(
                McpFailureReason.Protocol,
                "MCP SSE 流在返回响应前结束",
                retryable = false,
            )
        }
    }

    private suspend fun readLegacyStream(
        source: okio.BufferedSource,
        endpoint: CompletableDeferred<String>,
    ) {
        val parser = McpSseParser()
        try {
            while (!closed) {
                val line = source.readUtf8Line() ?: break
                val event = parser.feed(line) ?: continue
                when (event.event) {
                    "endpoint" -> {
                        val resolved = resolveEndpoint(event.data)
                        messageEndpoint = resolved
                        if (!endpoint.isCompleted) endpoint.complete(resolved)
                    }

                    else -> {
                        val obj = runCatching { JSONObject(event.data) }.getOrNull() ?: continue
                        if (McpJsonRpc.isResponse(obj)) {
                            val id = McpJsonRpc.responseId(obj) ?: continue
                            pending.remove(id)?.complete(obj)
                        } else if (McpJsonRpc.isNotification(obj)) {
                            notificationFlow.tryEmit(obj)
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // 连接关闭；下面统一结束 pending。
        } finally {
            if (!endpoint.isCompleted) {
                endpoint.completeExceptionally(
                    McpException(McpFailureReason.Network, "MCP SSE 连接在 endpoint 之前结束"),
                )
            }
            val error = McpException(McpFailureReason.Network, "MCP SSE 连接已断开")
            pending.values.forEach { it.completeExceptionally(error) }
            pending.clear()
        }
    }

    private fun resolveEndpoint(raw: String): String {
        val value = raw.trim()
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return runCatching { URI(config.url).resolve(value).toString() }
            .getOrElse { throw McpException(McpFailureReason.Protocol, "MCP SSE endpoint 无效：$value") }
    }

    private suspend fun postLegacy(payload: JSONObject) {
        val endpoint = messageEndpoint ?: throw McpException(
            McpFailureReason.Network,
            "MCP SSE 尚未就绪",
            retryable = true,
        )
        val response = execute(buildPostRequest(endpoint, payload), config.toolCallTimeoutMs)
        response.use { res ->
            updateSession(res)
            if (!res.isSuccessful) {
                val body = runCatching { res.body?.string().orEmpty() }.getOrDefault("")
                throw McpErrors.http(res.code, McpErrors.redact(body, secretValues))
            }
        }
    }

    private fun buildPostRequest(url: String, payload: JSONObject): Request =
        Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Accept", "application/json, text/event-stream")
            .apply {
                sessionId?.takeIf { it.isNotBlank() }?.let { header("Mcp-Session-Id", it) }
                protocolVersion?.takeIf { it.isNotBlank() }?.let { header("MCP-Protocol-Version", it) }
                extraHeaders.forEach { (k, v) -> header(k, v) }
            }
            .build()

    private fun updateSession(response: Response) {
        response.header("Mcp-Session-Id")?.takeIf { it.isNotBlank() }?.let { sessionId = it }
    }

    private suspend fun execute(request: Request, timeoutMs: Long? = null): Response =
        suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        timeoutMs?.let { call.timeout().timeout(it.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS) }
        if (config.transport == McpTransport.LegacySse) legacyCall = call
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(McpErrors.wrap(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        continuation.resume(response) { _, _, _ -> response.close() }
                    } else {
                        response.close()
                    }
                }
            },
        )
    }

    private suspend fun execute(call: Call): Response = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(McpErrors.wrap(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        continuation.resume(response) { _, _, _ -> response.close() }
                    } else {
                        response.close()
                    }
                }
            },
        )
    }

    override fun close() {
        if (closed) return
        closed = true
        pending.values.forEach {
            it.completeExceptionally(McpException(McpFailureReason.Cancelled, "MCP 已关闭"))
        }
        pending.clear()
        scope?.cancel()
        scope = null
        runCatching { legacyCall?.cancel() }
        runCatching { legacyResponse?.close() }
        legacyCall = null
        legacyResponse = null
        messageEndpoint = null
    }
}
