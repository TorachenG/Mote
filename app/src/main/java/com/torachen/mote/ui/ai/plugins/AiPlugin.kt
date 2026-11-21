package com.torachen.mote.ui.ai.plugins

import org.json.JSONArray
import org.json.JSONObject

internal enum class AiPluginPermission(val wire: String, val label: String) {
    Network("network", "网络访问"),
    Prompt("prompt", "注入提示词"),
    Tools("tools", "注册工具");

    companion object {
        fun fromWire(value: String): AiPluginPermission? =
            entries.firstOrNull { it.wire == value }
    }
}

internal data class AiPluginHttpTool(
    val name: String,
    val description: String,
    val method: String = "GET",
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val bodyTemplate: String = "",
    val inputSchema: JSONObject = JSONObject().put("type", "object"),
)

internal data class AiPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String = "",
    val author: String = "",
    val permissions: Set<AiPluginPermission> = emptySet(),
    val prompt: String = "",
    val tools: List<AiPluginHttpTool> = emptyList(),
    val enabled: Boolean = false,
    val installedAt: Long = 0L,
    val updatedAt: Long = 0L,
)

internal object AiPluginCodec {

    fun encodeList(plugins: List<AiPlugin>): String {
        val array = JSONArray()
        plugins.forEach { array.put(encode(it)) }
        return JSONObject().put("version", 1).put("plugins", array).toString()
    }

    fun decodeList(raw: String?): List<AiPlugin> {
        if (raw.isNullOrBlank()) return emptyList()
        val root = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyList()
        val array = root.optJSONArray("plugins") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                decode(obj)?.let(::add)
            }
        }
    }

    fun encode(plugin: AiPlugin): JSONObject = JSONObject().apply {
        put("id", plugin.id)
        put("name", plugin.name)
        put("version", plugin.version)
        put("description", plugin.description)
        put("author", plugin.author)
        put("permissions", JSONArray(plugin.permissions.map { it.wire }))
        put("prompt", plugin.prompt)
        put("enabled", plugin.enabled)
        put("installedAt", plugin.installedAt)
        put("updatedAt", plugin.updatedAt)
        put(
            "tools",
            JSONArray().apply {
                plugin.tools.forEach { tool ->
                    put(
                        JSONObject().apply {
                            put("name", tool.name)
                            put("description", tool.description)
                            put("method", tool.method)
                            put("url", tool.url)
                            put("headers", JSONObject(tool.headers))
                            put("body", tool.bodyTemplate)
                            put("inputSchema", tool.inputSchema)
                        },
                    )
                }
            },
        )
    }

    /** 解析插件包里的 manifest.json；与安装后的注册表使用同一结构。 */
    fun decode(obj: JSONObject): AiPlugin? {
        val id = obj.optString("id").trim()
        if (!id.matches(Regex("[A-Za-z0-9_.-]{2,64}"))) return null
        val name = obj.optString("name").trim().ifBlank { id }
        val version = obj.optString("version").trim().ifBlank { "1.0.0" }
        val permissions = buildSet {
            obj.optJSONArray("permissions")?.let { array ->
                for (index in 0 until array.length()) {
                    AiPluginPermission.fromWire(array.optString(index))?.let(::add)
                }
            }
        }
        val tools = buildList {
            obj.optJSONArray("tools")?.let { array ->
                for (index in 0 until array.length()) {
                    val tool = array.optJSONObject(index) ?: continue
                    val toolName = tool.optString("name").trim()
                    val url = tool.optString("url").trim()
                    if (!toolName.matches(Regex("[A-Za-z0-9_-]{1,64}"))) continue
                    if (!url.startsWith("https://") && !url.startsWith("http://")) continue
                    add(
                        AiPluginHttpTool(
                            name = toolName,
                            description = tool.optString("description").take(1_000),
                            method = tool.optString("method", "GET").uppercase()
                                .takeIf { it in setOf("GET", "POST", "PUT", "PATCH", "DELETE") }
                                ?: "GET",
                            url = url,
                            headers = tool.optJSONObject("headers")?.let { headers ->
                                buildMap {
                                    headers.keys().forEach { key ->
                                        headers.optString(key).takeIf { it.isNotBlank() }
                                            ?.let { put(key, it) }
                                    }
                                }
                            }.orEmpty(),
                            bodyTemplate = tool.optString("body").take(20_000),
                            inputSchema = tool.optJSONObject("inputSchema")
                                ?: JSONObject().put("type", "object"),
                        ),
                    )
                }
            }
        }.take(40)
        return AiPlugin(
            id = id,
            name = name.take(80),
            version = version.take(32),
            description = obj.optString("description").take(1_000),
            author = obj.optString("author").take(80),
            permissions = permissions,
            prompt = obj.optString("prompt").take(8_000),
            tools = tools,
            enabled = obj.optBoolean("enabled", false),
            installedAt = obj.optLong("installedAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
        )
    }
}
