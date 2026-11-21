package com.torachen.mote.ui.ai.mcp

import org.json.JSONArray
import org.json.JSONObject

/** MCP 配置的 JSON 编解码；只做结构转换，不解析 secret 值。 */
internal object McpConfigCodec {

    fun encodeList(servers: List<McpServerConfig>): String {
        val array = JSONArray()
        servers.forEach { array.put(encode(it)) }
        return JSONObject()
            .put("version", 1)
            .put("servers", array)
            .toString()
    }

    fun decodeList(raw: String?): List<McpServerConfig> {
        if (raw.isNullOrBlank()) return emptyList()
        val root = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyList()
        val array = root.optJSONArray("servers") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                decode(obj)?.let(::add)
            }
        }
    }

    fun encode(config: McpServerConfig): JSONObject = JSONObject().apply {
        put("id", config.id)
        put("displayName", config.displayName)
        put("namespace", config.namespace)
        put("url", config.url)
        put("transport", config.transport.wireName)
        put("enabled", config.enabled)
        put("toolCallTimeoutMs", config.toolCallTimeoutMs)
        put("startupTimeoutMs", config.startupTimeoutMs)
        put("createdAt", config.createdAt)
        put("updatedAt", config.updatedAt)
        put(
            "reconnect",
            JSONObject().apply {
                put("enabled", config.reconnect.enabled)
                put("initialDelayMs", config.reconnect.initialDelayMs)
                put("maxDelayMs", config.reconnect.maxDelayMs)
                put("maxAttempts", config.reconnect.maxAttempts)
            },
        )
        when (val auth = config.auth) {
            is McpAuth.None -> put("auth", JSONObject().put("type", "none"))
            is McpAuth.Bearer -> put(
                "auth",
                JSONObject().put("type", "bearer").put("secretRef", auth.secretRef),
            )

            is McpAuth.Header -> put(
                "auth",
                JSONObject()
                    .put("type", "header")
                    .put("name", auth.name)
                    .put("secretRef", auth.secretRef),
            )
        }
        put(
            "headers",
            JSONArray().apply {
                config.headers.forEach { header ->
                    put(
                        JSONObject().apply {
                            put("name", header.name)
                            if (header.secretRef != null) {
                                put("secretRef", header.secretRef)
                            } else {
                                put("value", header.value)
                            }
                        },
                    )
                }
            },
        )
        config.stdio?.let { stdio ->
            put(
                "stdio",
                JSONObject().apply {
                    put("command", stdio.command)
                    put("args", JSONArray(stdio.args))
                    put("env", JSONObject(stdio.env))
                    put("envSecretRefs", JSONObject(stdio.envSecretRefs))
                    put("cwd", stdio.cwd)
                },
            )
        }
        put(
            "toolPolicies",
            JSONObject().apply {
                config.toolPolicies.forEach { (name, policy) -> put(name, policy.name) }
            },
        )
        put("disabledTools", JSONArray(config.disabledTools.toList()))
    }

    fun decode(obj: JSONObject): McpServerConfig? {
        val id = obj.optString("id").trim().ifBlank { return null }
        val namespace = obj.optString("namespace").trim().ifBlank { return null }
        val displayName = obj.optString("displayName").trim().ifBlank { namespace }
        val url = obj.optString("url").trim()
        val transport = McpTransport.fromWire(obj.optString("transport"))
            ?: McpTransport.StreamableHttp

        val auth = when (val authObj = obj.optJSONObject("auth")) {
            null -> McpAuth.None
            else -> when (authObj.optString("type")) {
                "bearer" -> authObj.optString("secretRef").takeIf { it.isNotBlank() }
                    ?.let { McpAuth.Bearer(it) } ?: McpAuth.None

                "header" -> {
                    val name = authObj.optString("name").trim()
                    val ref = authObj.optString("secretRef").trim()
                    if (name.isNotBlank() && ref.isNotBlank()) McpAuth.Header(name, ref) else McpAuth.None
                }

                else -> McpAuth.None
            }
        }

        val headers = buildList {
            val array = obj.optJSONArray("headers") ?: JSONArray()
            for (index in 0 until array.length()) {
                val header = array.optJSONObject(index) ?: continue
                val name = header.optString("name").trim()
                if (name.isBlank()) continue
                val ref = header.optString("secretRef").takeIf { it.isNotBlank() }
                add(McpHeader(name = name, value = header.optString("value"), secretRef = ref))
            }
        }

        val stdio = obj.optJSONObject("stdio")?.let { stdioObj ->
            val command = stdioObj.optString("command").trim()
            if (command.isBlank()) null else McpStdioConfig(
                command = command,
                args = stdioObj.optJSONArray("args")?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                        }
                    }
                }.orEmpty(),
                env = stdioObj.optJSONObject("env")?.let { envObj ->
                    buildMap {
                        envObj.keys().forEach { key ->
                            envObj.optString(key).takeIf { it.isNotBlank() }?.let { put(key, it) }
                        }
                    }
                }.orEmpty(),
                envSecretRefs = stdioObj.optJSONObject("envSecretRefs")?.let { envObj ->
                    buildMap {
                        envObj.keys().forEach { key ->
                            envObj.optString(key).takeIf { it.isNotBlank() }?.let { put(key, it) }
                        }
                    }
                }.orEmpty(),
                cwd = stdioObj.optString("cwd"),
            )
        }

        val reconnect = obj.optJSONObject("reconnect")?.let { reconnectObj ->
            McpReconnect(
                enabled = reconnectObj.optBoolean("enabled", true),
                initialDelayMs = reconnectObj.optLong("initialDelayMs", 500L).coerceAtLeast(1L),
                maxDelayMs = reconnectObj.optLong("maxDelayMs", 30_000L)
                    .coerceAtLeast(1L),
                maxAttempts = reconnectObj.optInt("maxAttempts", 10).coerceAtLeast(1),
            )
        } ?: McpReconnect()

        val policies = buildMap {
            obj.optJSONObject("toolPolicies")?.let { policyObj ->
                policyObj.keys().forEach { name ->
                    val policy = runCatching {
                        McpToolPolicy.valueOf(policyObj.optString(name))
                    }.getOrNull()
                    if (policy != null) put(name, policy)
                }
            }
        }

        val disabled = buildSet {
            obj.optJSONArray("disabledTools")?.let { array ->
                for (index in 0 until array.length()) {
                    array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }

        if (transport != McpTransport.Stdio && url.isBlank()) return null
        if (transport == McpTransport.Stdio && stdio == null) return null

        return McpServerConfig(
            id = id,
            displayName = displayName,
            namespace = namespace,
            url = url,
            transport = transport,
            auth = auth,
            headers = headers,
            stdio = stdio,
            enabled = obj.optBoolean("enabled", true),
            toolCallTimeoutMs = obj.optLong("toolCallTimeoutMs", 60_000L).coerceAtLeast(1_000L),
            startupTimeoutMs = obj.optLong("startupTimeoutMs", 30_000L).coerceAtLeast(1_000L),
            reconnect = reconnect,
            toolPolicies = policies,
            disabledTools = disabled,
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
        )
    }
}
