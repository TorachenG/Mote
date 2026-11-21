package com.torachen.mote.ui.ai.mcp

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.lang.Process

/**
 * 通过 Shizuku 的 shell 通道启动 stdio MCP server。
 *
 * 这是高权限能力：命令以 ADB shell 身份运行，能访问普通应用访问不到的数据。
 * 因此 UI 必须对每个 stdio server 做明确的风险确认，不能默认开启。
 */
internal object McpStdioShizuku {

    const val PERMISSION_REQUEST_CODE = 4402

    fun binderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasPermission(): Boolean = runCatching {
        binderAlive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun requestPermission(): Boolean = runCatching {
        if (!binderAlive()) return false
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        true
    }.getOrDefault(false)

    /**
     * 启动一个长生命周期进程。
     * @param resolvedEnv 已经解析好的明文 env；只存在于内存，不落盘。
     */
    fun start(config: McpStdioConfig, resolvedEnv: Map<String, String>): Process {
        if (!hasPermission()) {
            throw McpException(
                McpFailureReason.ShizukuUnavailable,
                "stdio MCP 需要 Shizuku 权限，请先启动 Shizuku 并授权 Mote",
                retryable = false,
            )
        }
        val script = buildScript(config)
        val env = buildEnv(resolvedEnv)
        val process = runCatching {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java,
            )
            method.isAccessible = true
            method.invoke(
                null,
                arrayOf("/system/bin/sh", "-c", script),
                env,
                config.cwd.trim().ifBlank { "/data/local/tmp" },
            ) as? Process
        }.getOrNull()
        return process ?: throw McpException(
            McpFailureReason.ShizukuUnavailable,
            "Shizuku 无法启动 stdio 进程：请确认 Shizuku 已运行且权限未被撤销",
            retryable = false,
        )
    }

    private fun buildScript(config: McpStdioConfig): String {
        val command = buildString {
            append("exec ").append(quote(config.command))
            config.args.forEach { arg ->
                append(' ').append(quote(arg))
            }
        }
        val cwd = config.cwd.trim()
        return if (cwd.isBlank()) command else "cd ${quote(cwd)} && $command"
    }

    private fun buildEnv(resolved: Map<String, String>): Array<String> {
        val values = linkedMapOf(
            "PATH" to "/sbin:/system/sbin:/system/bin:/system/xbin",
            "HOME" to "/data/local/tmp",
        )
        resolved.forEach { (key, value) ->
            if (key.isNotBlank() && value.isNotEmpty()) values[key] = value
        }
        return values.map { (key, value) -> "$key=$value" }.toTypedArray()
    }

    private fun quote(value: String): String =
        "'" + value.replace("'", "'\\''") + "'"
}
