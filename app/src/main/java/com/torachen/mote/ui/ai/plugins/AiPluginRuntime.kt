package com.torachen.mote.ui.ai.plugins

import android.content.Context
import com.torachen.mote.ui.ai.AiToolCall
import com.torachen.mote.ui.ai.AiToolOutcome
import com.torachen.mote.ui.ai.mcp.AiToolGroup
import com.torachen.mote.ui.ai.mcp.AiToolSource
import com.torachen.mote.ui.ai.mcp.AiToolSpec
import com.torachen.mote.ui.ai.mcp.McpSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** 声明式插件运行时：只执行 HTTP 工具 + 提示词注入，不执行第三方代码。 */
internal object AiPluginRuntime {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun tools(context: Context): List<AiToolSpec> =
        AiPluginStore.all(context)
            .filter { it.enabled && AiPluginPermission.Tools in it.permissions }
            .flatMap { plugin ->
                plugin.tools.map { tool ->
                    AiToolSpec(
                        name = publicName(plugin.id, tool.name),
                        title = tool.name,
                        summary = tool.description.ifBlank { "插件 ${plugin.name} 的工具" },
                        params = McpSchema.summary(tool.inputSchema, 1_500),
                        group = AiToolGroup.OTHER,
                        inputSchema = tool.inputSchema,
                        source = AiToolSource.Plugin,
                        requiresApproval = true,
                        risky = tool.method != "GET",
                        untrustedOutput = true,
                        serverId = plugin.id,
                        serverName = plugin.name,
                        rawName = tool.name,
                    )
                }
            }

    fun promptSections(context: Context): List<Pair<String, String>> =
        AiPluginStore.all(context)
            .filter { it.enabled && AiPluginPermission.Prompt in it.permissions }
            .mapNotNull { plugin ->
                plugin.prompt.trim().takeIf { it.isNotBlank() }?.let { plugin.name to it }
            }

    suspend fun executeTool(context: Context, call: AiToolCall): AiToolOutcome {
        val found = findTool(context, call.name)
            ?: return AiToolOutcome(
                text = "插件工具「${call.name}」不在已启用插件清单里。",
                title = "插件工具不可用",
                failed = true,
            )
        val (plugin, tool) = found
        if (AiPluginPermission.Network !in plugin.permissions) {
            return AiToolOutcome(
                text = "插件「${plugin.name}」没有声明 network 权限，不能发网络请求。",
                title = "插件无网络权限",
                failed = true,
            )
        }
        McpSchema.validate(tool.inputSchema, call.arguments)?.let { error ->
            return AiToolOutcome(
                text = "插件工具参数不正确：$error",
                title = "插件参数错误",
                failed = true,
            )
        }
        return try {
            val result = withContext(Dispatchers.IO) { executeHttp(plugin, tool, call.arguments) }
            AiToolOutcome(
                text = "<untrusted_plugin_result plugin=\"${plugin.name.take(64)}\" " +
                        "tool=\"${tool.name}\">\n${result.take(8_000)}\n</untrusted_plugin_result>",
                title = "插件：${tool.name}",
                failed = false,
            )
        } catch (cancel: kotlinx.coroutines.CancellationException) {
            throw cancel
        } catch (throwable: Throwable) {
            AiToolOutcome(
                text = "插件工具执行失败：${throwable.message?.take(500) ?: "未知错误"}",
                title = "插件执行失败",
                failed = true,
            )
        }
    }

    private fun executeHttp(
        plugin: AiPlugin,
        tool: AiPluginHttpTool,
        args: JSONObject,
    ): String {
        val url = render(tool.url, args)
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            throw IllegalArgumentException("插件 URL 必须是 http/https")
        }
        val requestBody = if (tool.method == "GET") {
            null
        } else {
            val body = tool.bodyTemplate.ifBlank { args.toString() }
            body.toRequestBody("application/json; charset=utf-8".toMediaType())
        }
        val builder = Request.Builder().url(url).method(tool.method, requestBody)
        tool.headers.forEach { (key, value) ->
            builder.header(key, render(value, args))
        }
        client.newCall(builder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "HTTP ${response.code}：${body.take(300)}",
                )
            }
            return body
        }
    }

    private fun findTool(context: Context, name: String): Pair<AiPlugin, AiPluginHttpTool>? {
        AiPluginStore.all(context)
            .filter { it.enabled && AiPluginPermission.Tools in it.permissions }
            .forEach { plugin ->
                plugin.tools.forEach { tool ->
                    if (publicName(plugin.id, tool.name) == name) return plugin to tool
                }
            }
        return null
    }

    fun publicName(pluginId: String, toolName: String): String {
        val raw = "plugin__${pluginId}__${toolName}"
        return raw.replace(Regex("[^A-Za-z0-9_-]"), "_").take(64)
    }

    private fun render(template: String, args: JSONObject): String {
        val regex = Regex("\\{\\{args\\.([A-Za-z0-9_.-]+)}}")
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            args.optString(key)
        }
    }
}
