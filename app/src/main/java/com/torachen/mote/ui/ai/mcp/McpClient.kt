package com.torachen.mote.ui.ai.mcp

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * 单个 MCP server 的协议客户端：initialize / tools/list / tools/call。
 *
 * 连接管理、重连、工具注册不在这里，由后续 McpManager 负责；
 * 这里只保证一次连接生命周期内的协议正确性。
 */
internal class McpClient(
    private val config: McpServerConfig,
    private val secretResolver: (String) -> String?,
    private val clientVersion: String = "0.3.5",
) {

    private val secrets = mutableListOf<String>()

    private fun resolveSecret(ref: String?): String? {
        if (ref.isNullOrBlank()) return null
        val value = secretResolver(ref)
        if (!value.isNullOrBlank()) secrets.add(value)
        return value
    }

    private val transport: McpChannel = when (config.transport) {
        McpTransport.Stdio -> {
            val stdio = config.stdio ?: throw McpException(
                McpFailureReason.Config,
                "stdio MCP 缺少 command",
                retryable = false,
            )
            val env = LinkedHashMap(stdio.env)
            stdio.envSecretRefs.forEach { (key, ref) ->
                resolveSecret(ref)?.let { env[key] = it }
            }
            McpStdioTransport(stdio, env)
        }

        McpTransport.StreamableHttp, McpTransport.LegacySse -> {
            val authValue = when (val auth = config.auth) {
                is McpAuth.None -> null
                is McpAuth.Bearer -> resolveSecret(auth.secretRef)
                is McpAuth.Header -> resolveSecret(auth.secretRef)
            }
            val headerValues = buildMap {
                config.headers.forEach { header ->
                    header.secretRef?.let { ref ->
                        resolveSecret(ref)?.let { put(header.name, it) }
                    }
                }
            }
            McpHttpTransport(config, authValue, headerValues, secrets.toList())
        }
    }

    var protocolVersion: String = ""
        private set
    var serverInfo: McpServerInfo = McpServerInfo()
        private set
    var instructions: String = ""
        private set
    var capabilities: JSONObject = JSONObject()
        private set

    suspend fun connect(startupTimeoutMs: Long = config.startupTimeoutMs): McpClient {
        try {
            transport.start()
            initialize(startupTimeoutMs)
            return this
        } catch (throwable: Throwable) {
            close()
            throw McpErrors.wrap(throwable)
        }
    }

    private suspend fun initialize(timeoutMs: Long) {
        val id = McpJsonRpc.nextId()
        val params = JSONObject().apply {
            put("protocolVersion", "2025-06-18")
            put("capabilities", JSONObject())
            put(
                "clientInfo",
                JSONObject()
                    .put("name", "Mote")
                    .put("version", clientVersion),
            )
        }
        val response = transport.request(
            McpJsonRpc.request(id, "initialize", params),
            timeoutMs.coerceAtLeast(1_000L),
        ) ?: throw McpException(McpFailureReason.Protocol, "MCP initialize 未返回响应")

        McpJsonRpc.errorOf(response)?.let { throw McpErrors.rpc(it) }
        val result = McpJsonRpc.resultOf(response)
            ?: throw McpException(McpFailureReason.Protocol, "MCP initialize 缺少 result")

        protocolVersion = result.optString("protocolVersion").ifBlank { "unknown" }
        instructions = result.optString("instructions").take(8_192)
        capabilities = result.optJSONObject("capabilities") ?: JSONObject()
        serverInfo = result.optJSONObject("serverInfo")?.let { info ->
            McpServerInfo(
                name = info.optString("name"),
                version = info.optString("version"),
                title = info.optString("title"),
            )
        } ?: McpServerInfo()
        transport.protocolVersion = protocolVersion
        transport.notify(McpJsonRpc.notification("notifications/initialized"))
    }

    suspend fun listTools(timeoutMs: Long = config.toolCallTimeoutMs): List<McpToolDescriptor> {
        // 没有 tools capability 的 server 不发送 tools/list。
        if (capabilities.length() > 0 && !capabilities.has("tools")) return emptyList()

        val tools = mutableListOf<McpToolDescriptor>()
        val seen = mutableSetOf<String>()
        var cursor: String? = null
        var pages = 0
        do {
            if (pages++ >= MAX_LIST_PAGES) {
                throw McpException(
                    McpFailureReason.Protocol,
                    "MCP tools/list 分页超过 $MAX_LIST_PAGES 页，server 的 cursor 可能未终止",
                    retryable = false,
                )
            }
            val params = JSONObject()
            cursor?.takeIf { it.isNotBlank() }?.let { params.put("cursor", it) }
            val id = McpJsonRpc.nextId()
            val response = transport.request(
                McpJsonRpc.request(id, "tools/list", params),
                timeoutMs.coerceAtLeast(1_000L),
            ) ?: throw McpException(McpFailureReason.Protocol, "MCP tools/list 未返回响应")
            McpJsonRpc.errorOf(response)?.let { throw McpErrors.rpc(it) }
            val result = McpJsonRpc.resultOf(response)
                ?: throw McpException(McpFailureReason.Protocol, "MCP tools/list 缺少 result")
            val array = result.optJSONArray("tools") ?: JSONArray()
            for (index in 0 until array.length()) {
                val tool = array.optJSONObject(index) ?: continue
                val rawName = tool.optString("name").trim()
                if (rawName.isBlank()) continue
                if (tools.size >= MAX_TOOLS) {
                    throw McpException(
                        McpFailureReason.Protocol,
                        "MCP 工具数量超过 $MAX_TOOLS",
                        retryable = false,
                    )
                }
                val publicName = publicToolName(config.namespace, rawName)
                if (!seen.add(publicName)) {
                    throw McpException(
                        McpFailureReason.Protocol,
                        "MCP server 返回了重复工具名：$rawName",
                        retryable = false,
                    )
                }
                val annotations = tool.optJSONObject("annotations")
                tools.add(
                    McpToolDescriptor(
                        serverId = config.id,
                        namespace = config.namespace,
                        rawName = rawName,
                        publicName = publicName,
                        title = tool.optString("title").ifBlank { rawName },
                        description = tool.optString("description").take(2_000),
                        inputSchema = tool.optJSONObject("inputSchema") ?: JSONObject().put("type", "object"),
                        outputSchema = tool.optJSONObject("outputSchema"),
                        annotations = McpToolAnnotations(
                            readOnlyHint = annotations?.optBooleanOrNull("readOnlyHint"),
                            destructiveHint = annotations?.optBooleanOrNull("destructiveHint"),
                            idempotentHint = annotations?.optBooleanOrNull("idempotentHint"),
                            openWorldHint = annotations?.optBooleanOrNull("openWorldHint"),
                        ),
                    ),
                )
            }
            cursor = result.optString("nextCursor").takeIf { it.isNotBlank() }
        } while (cursor != null)
        return tools
    }

    suspend fun callTool(
        rawName: String,
        arguments: JSONObject,
        timeoutMs: Long = config.toolCallTimeoutMs,
    ): McpCallResult {
        val id = McpJsonRpc.nextId()
        val params = JSONObject()
            .put("name", rawName)
            .put("arguments", arguments)
        val response = transport.request(
            McpJsonRpc.request(id, "tools/call", params),
            timeoutMs.coerceAtLeast(1_000L),
        ) ?: throw McpException(McpFailureReason.Protocol, "MCP tools/call 未返回响应")
        McpJsonRpc.errorOf(response)?.let { throw McpErrors.rpc(it) }
        val result = McpJsonRpc.resultOf(response)
            ?: throw McpException(McpFailureReason.Protocol, "MCP tools/call 缺少 result")
        return McpCallResult(
            content = parseContent(result.optJSONArray("content")),
            structuredContent = result.optJSONObject("structuredContent"),
            isError = result.optBoolean("isError", false),
            raw = result,
        )
    }

    suspend fun listResources(
        timeoutMs: Long = config.toolCallTimeoutMs,
    ): List<McpResourceDescriptor> {
        if (capabilities.length() > 0 && !capabilities.has("resources")) return emptyList()
        val resources = mutableListOf<McpResourceDescriptor>()
        var cursor: String? = null
        var pages = 0
        do {
            if (pages++ >= MAX_LIST_PAGES) break
            val params = JSONObject()
            cursor?.takeIf { it.isNotBlank() }?.let { params.put("cursor", it) }
            val id = McpJsonRpc.nextId()
            val response = transport.request(
                McpJsonRpc.request(id, "resources/list", params),
                timeoutMs.coerceAtLeast(1_000L),
            ) ?: break
            McpJsonRpc.errorOf(response)?.let { throw McpErrors.rpc(it) }
            val result = McpJsonRpc.resultOf(response) ?: break
            val array = result.optJSONArray("resources") ?: JSONArray()
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val uri = item.optString("uri").trim()
                if (uri.isBlank()) continue
                resources.add(
                    McpResourceDescriptor(
                        uri = uri,
                        name = item.optString("name").ifBlank { uri },
                        mimeType = item.optString("mimeType"),
                        description = item.optString("description").take(300),
                    ),
                )
            }
            cursor = result.optString("nextCursor").takeIf { it.isNotBlank() }
        } while (cursor != null)
        return resources
    }

    suspend fun readResource(
        uri: String,
        timeoutMs: Long = config.toolCallTimeoutMs,
    ): String {
        val id = McpJsonRpc.nextId()
        val params = JSONObject().put("uri", uri)
        val response = transport.request(
            McpJsonRpc.request(id, "resources/read", params),
            timeoutMs.coerceAtLeast(1_000L),
        ) ?: throw McpException(McpFailureReason.Protocol, "MCP resources/read 未返回响应")
        McpJsonRpc.errorOf(response)?.let { throw McpErrors.rpc(it) }
        val result = McpJsonRpc.resultOf(response)
            ?: throw McpException(McpFailureReason.Protocol, "MCP resources/read 缺少 result")
        val contents = result.optJSONArray("contents") ?: JSONArray()
        return buildString {
            for (index in 0 until contents.length()) {
                val item = contents.optJSONObject(index) ?: continue
                if (index > 0) append("\n\n")
                append("资源：").append(item.optString("uri").ifBlank { uri }).append('\n')
                val mime = item.optString("mimeType")
                if (mime.isNotBlank()) append("类型：").append(mime).append('\n')
                val text = item.optString("text")
                if (text.isNotBlank()) {
                    append(text.take(8_000))
                } else if (item.has("blob")) {
                    append("[binary resource: ").append(item.optString("blob").length).append(" base64 chars]")
                }
            }
        }.take(8_000)
    }

    fun notifications(): Flow<JSONObject> = transport.notifications()

    fun diagnostics(): String = transport.diagnostics()

    fun close() {
        transport.close()
    }

    private fun parseContent(array: JSONArray?): List<McpContentBlock> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val block = array.optJSONObject(index) ?: continue
                when (block.optString("type")) {
                    "text" -> add(McpContentBlock.Text(block.optString("text")))
                    "image" -> add(
                        McpContentBlock.Image(
                            mimeType = block.optString("mimeType"),
                            data = block.optString("data"),
                        ),
                    )

                    "resource_link" -> add(
                        McpContentBlock.ResourceLink(
                            name = block.optString("name"),
                            uri = block.optString("uri"),
                        ),
                    )

                    "audio" -> add(McpContentBlock.Audio(block.optString("mimeType")))
                    "resource" -> add(
                        McpContentBlock.EmbeddedResource(
                            block.optJSONObject("resource")?.optString("uri"),
                        ),
                    )

                    else -> add(McpContentBlock.Unknown(block.optString("type")))
                }
            }
        }
    }

    private fun JSONObject.optBooleanOrNull(key: String): Boolean? =
        if (has(key) && !isNull(key)) optBoolean(key) else null

    companion object {
        private const val MAX_LIST_PAGES = 64
        private const val MAX_TOOLS = 200
        private const val MAX_PUBLIC_NAME_LENGTH = 64
        private val INVALID_NAME_CHARS = Regex("[^A-Za-z0-9_-]")

        /** 与 DSH 相同的稳定命名：mcp__<namespace>__<rawName>。 */
        fun publicToolName(namespace: String, rawName: String): String {
            val safeNamespace = namespace.trim().ifBlank { "mcp" }
            val joined = "mcp__${safeNamespace}__${rawName.trim()}"
            val normalized = joined.replace(INVALID_NAME_CHARS, "_")
            if (normalized == joined && normalized.length <= MAX_PUBLIC_NAME_LENGTH) {
                return normalized
            }
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("$safeNamespace\u0000$rawName".toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 0xFF) }
                .take(12)
            val prefix = normalized.take(MAX_PUBLIC_NAME_LENGTH - digest.length - 1)
            return "${prefix}_$digest"
        }

        suspend fun probe(
            config: McpServerConfig,
            secretResolver: (String) -> String?,
        ): McpProbeResult {
            val startedAt = System.currentTimeMillis()
            val client = McpClient(config, secretResolver)
            return try {
                client.connect(config.startupTimeoutMs)
                val tools = client.listTools()
                McpProbeResult(
                    ok = true,
                    serverInfo = client.serverInfo,
                    protocolVersion = client.protocolVersion,
                    toolNames = tools.map { it.rawName },
                    instructions = client.instructions,
                    durationMs = System.currentTimeMillis() - startedAt,
                )
            } catch (throwable: Throwable) {
                val error = McpErrors.wrap(throwable)
                McpProbeResult(
                    ok = false,
                    durationMs = System.currentTimeMillis() - startedAt,
                    error = McpErrors.redact(error.message.orEmpty(), client.secretsSnapshot()),
                    failureReason = error.kind,
                )
            } finally {
                client.close()
            }
        }
    }

    private fun secretsSnapshot(): List<String> = secrets.toList()
}
