package com.torachen.mote.ui.ai.mcp

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.AiAddCapsule
import com.torachen.mote.ui.ai.AiSettingsScaffold
import com.torachen.mote.ui.ai.security.AiSecretStore
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

internal object McpServerNav {
    var editingId: String? = null
    fun open(id: String) { editingId = id }
    fun new() { editingId = null }
}

@Composable
fun McpServersPage(
    onBack: () -> Unit,
    onEdit: (String?) -> Unit,
) {
    val context = LocalContext.current
    var reload by remember { mutableIntStateOf(0) }
    val statuses by McpManager.statuses.collectAsState()
    val servers = remember(reload) { McpStore.all(context) }
    var showShizukuDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        McpManager.start(context)
        McpManager.refresh(context)
    }

    DisposableEffect(Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { code, grant ->
            if (code == McpStdioShizuku.PERMISSION_REQUEST_CODE &&
                grant == PackageManager.PERMISSION_GRANTED
            ) {
                McpManager.refresh(context)
                reload++
            }
        }
        runCatching { Shizuku.addRequestPermissionResultListener(listener) }
        onDispose {
            runCatching { Shizuku.removeRequestPermissionResultListener(listener) }
        }
    }

    AiSettingsScaffold(
        title = "MCP 服务",
        pageRoute = "ai_mcp_servers",
        onBack = onBack,
        floatingBottomBar = {
            AiAddCapsule(
                contentDescription = "添加 MCP 服务",
                onClick = {
                    McpServerNav.new()
                    onEdit(null)
                },
            )
        },
    ) {
        Spacer(Modifier.height(12.dp))
        if (servers.none { it.transport == McpTransport.Stdio } || McpManager.hasShizuku()) {
            // 没有 stdio server 或已授权时不打扰。
        } else {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                BasicComponent(
                    title = "stdio MCP 需要 Shizuku",
                    summary = if (McpManager.shizukuAvailable()) {
                        "Shizuku 已运行，但 Mote 还没有获得权限"
                    } else {
                        "请先安装并启动 Shizuku，Mote 才能以 shell 身份启动本地 MCP server"
                    },
                    onClick = {
                        if (McpManager.shizukuAvailable()) {
                            McpManager.requestShizukuPermission()
                        } else {
                            showShizukuDialog = true
                        }
                    },
                )
            }
        }

        if (servers.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无 MCP 服务",
                            fontSize = 15.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "支持 Streamable HTTP、旧版 HTTP+SSE，以及通过 Shizuku 启动的 stdio",
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                servers.forEach { server ->
                    SearchableItem(
                        title = server.displayName,
                        pageRoute = "ai_mcp_servers",
                        pageTitle = "MCP 服务",
                        itemKey = "ai_mcp_${server.id}",
                    ) {
                        val status = statuses[server.id] ?: if (server.enabled) {
                            McpServerStatus.Connecting(0)
                        } else {
                            McpServerStatus.Disabled
                        }
                        BasicComponent(
                            title = server.displayName,
                            summary = buildString {
                                append(statusLabel(status))
                                append(" · ")
                                append(transportLabel(server.transport))
                                if (status is McpServerStatus.Ready) {
                                    append(" · ${status.toolCount} 个工具")
                                }
                            },
                            startAction = {
                                Badge(
                                    containerColor = statusColor(status),
                                    modifier = Modifier.padding(end = 10.dp),
                                )
                            },
                            endActions = {
                                Switch(
                                    checked = server.enabled,
                                    onCheckedChange = { checked ->
                                        McpStore.upsert(
                                            context,
                                            server.copy(enabled = checked),
                                        )
                                        McpManager.refresh(context)
                                        reload++
                                    },
                                )
                            },
                            onClick = {
                                McpServerNav.open(server.id)
                                onEdit(server.id)
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }

    if (showShizukuDialog) {
        WindowDialog(
            show = true,
            title = "需要 Shizuku",
            summary = "stdio MCP 会以 ADB shell 身份执行命令，权限很高。请先安装并启动 Shizuku，" +
                    "再回到这里授权。只在你信任要运行的命令时使用。",
            onDismissRequest = { showShizukuDialog = false },
        ) {
            Button(
                onClick = { showShizukuDialog = false },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("知道了") }
        }
    }
}

@Composable
fun McpServerEditPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editingId = McpServerNav.editingId
    val existing = remember(editingId) { editingId?.let { McpStore.byId(context, it) } }

    var displayName by remember { mutableStateOf(existing?.displayName.orEmpty()) }
    var namespace by remember { mutableStateOf(existing?.namespace.orEmpty()) }
    var url by remember { mutableStateOf(existing?.url.orEmpty()) }
    var transport by remember { mutableStateOf(existing?.transport ?: McpTransport.StreamableHttp) }
    var authType by remember {
        mutableStateOf(
            when (existing?.auth) {
                is McpAuth.Bearer -> "Bearer"
                is McpAuth.Header -> "Header"
                else -> "无"
            },
        )
    }
    var token by remember { mutableStateOf("") }
    var headerName by remember {
        mutableStateOf((existing?.auth as? McpAuth.Header)?.name.orEmpty())
    }
    var command by remember { mutableStateOf(existing?.stdio?.command.orEmpty()) }
    var argsText by remember { mutableStateOf(existing?.stdio?.args?.joinToString(" ").orEmpty()) }
    var envText by remember {
        mutableStateOf(
            existing?.stdio?.env?.entries?.joinToString("\n") { "${it.key}=${it.value}" }.orEmpty(),
        )
    }
    var cwd by remember { mutableStateOf(existing?.stdio?.cwd.orEmpty()) }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }
    var stdioConfirmed by remember { mutableStateOf(existing?.stdio != null) }
    var probeResult by remember { mutableStateOf<McpProbeResult?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    val allTools by McpManager.tools.collectAsState()

    fun buildConfig(): McpServerConfig {
        val id = existing?.id ?: ("mcp_" + System.currentTimeMillis().toString(36))
        val ref = existing?.let { refFor(it) } ?: "mcp:$id:token"
        val oldAuth = existing?.auth
        val auth = when (authType) {
            "Bearer" -> {
                val effective = token.ifBlank {
                    (oldAuth as? McpAuth.Bearer)?.secretRef?.let { AiSecretStore.get(context, it) }.orEmpty()
                }
                if (effective.isNotBlank()) McpAuth.Bearer(ref) else McpAuth.None
            }

            "Header" -> {
                val effective = token.ifBlank {
                    (oldAuth as? McpAuth.Header)?.secretRef?.let { AiSecretStore.get(context, it) }.orEmpty()
                }
                if (headerName.isNotBlank() && effective.isNotBlank()) {
                    McpAuth.Header(headerName.trim(), ref)
                } else {
                    McpAuth.None
                }
            }

            else -> McpAuth.None
        }
        return McpServerConfig(
            id = id,
            displayName = displayName.trim().ifBlank { namespace.trim().ifBlank { "MCP" } },
            namespace = namespace.trim().ifBlank { "mcp" },
            url = url.trim(),
            transport = transport,
            auth = auth,
            headers = existing?.headers.orEmpty(),
            stdio = if (transport == McpTransport.Stdio) {
                McpStdioConfig(
                    command = command.trim(),
                    args = argsText.split(Regex("\\s+")).filter { it.isNotBlank() },
                    env = parseEnv(envText),
                    envSecretRefs = existing?.stdio?.envSecretRefs.orEmpty(),
                    cwd = cwd.trim(),
                )
            } else {
                null
            },
            enabled = enabled,
            toolCallTimeoutMs = existing?.toolCallTimeoutMs ?: 60_000L,
            startupTimeoutMs = existing?.startupTimeoutMs ?: 30_000L,
            reconnect = existing?.reconnect ?: McpReconnect(),
            toolPolicies = existing?.toolPolicies.orEmpty(),
            disabledTools = existing?.disabledTools.orEmpty(),
            createdAt = existing?.createdAt ?: 0L,
        )
    }

    fun persistToken(config: McpServerConfig) {
        if (token.isNotBlank()) {
            val ref = when (val auth = config.auth) {
                is McpAuth.Bearer -> auth.secretRef
                is McpAuth.Header -> auth.secretRef
                else -> null
            }
            ref?.let { AiSecretStore.put(context, it, token) }
        }
    }

    AiSettingsScaffold(
        title = if (existing == null) "添加 MCP 服务" else "编辑 MCP 服务",
        pageRoute = "ai_mcp_edit",
        onBack = onBack,
    ) {
        SmallTitle("基本信息")
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                McpField("名称", displayName, { displayName = it }, "例如 GitHub")
                McpField("命名空间", namespace, { namespace = it }, "只允许字母/数字/_/-，用于 mcp__<namespace>__<tool>")
                McpField("URL", url, { url = it }, "https://example.com/mcp", enabled = transport != McpTransport.Stdio)
                WindowDropdownPreference(
                    title = "传输方式",
                    summary = transportLabel(transport),
                    items = listOf("Streamable HTTP", "HTTP + SSE（旧版）", "stdio（Shizuku）"),
                    selectedIndex = when (transport) {
                        McpTransport.StreamableHttp -> 0
                        McpTransport.LegacySse -> 1
                        McpTransport.Stdio -> 2
                    },
                    onSelectedIndexChange = { index ->
                        transport = when (index) {
                            1 -> McpTransport.LegacySse
                            2 -> McpTransport.Stdio
                            else -> McpTransport.StreamableHttp
                        }
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("启用", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        }

        if (transport == McpTransport.Stdio) {
            SmallTitle("stdio 命令")
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "通过 Shizuku 以 shell 身份启动。命令和参数由你负责，Mote 只负责转发 stdio。",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.error,
                    )
                    McpField("命令", command, { command = it }, "/data/local/tmp/node")
                    McpField("参数（空格分隔）", argsText, { argsText = it }, "server.js --port 0")
                    McpField("工作目录", cwd, { cwd = it }, "/data/local/tmp")
                    McpField(
                        "环境变量（每行 KEY=VALUE）",
                        envText,
                        { envText = it },
                        "NODE_ENV=production",
                        singleLine = false,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "我确认这条命令会以 shell 身份运行",
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Checkbox(
                            state = if (stdioConfirmed) ToggleableState.On else ToggleableState.Off,
                            onClick = { stdioConfirmed = !stdioConfirmed },
                        )
                    }
                    if (!McpManager.hasShizuku()) {
                        Button(
                            onClick = {
                                if (McpManager.shizukuAvailable()) {
                                    McpManager.requestShizukuPermission()
                                } else {
                                    Toast.makeText(context, "请先安装并启动 Shizuku", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                contentColor = MiuixTheme.colorScheme.onSurface,
                            ),
                        ) { Text(if (McpManager.shizukuAvailable()) "授权 Shizuku" else "Shizuku 未运行") }
                    }
                }
            }
        } else {
            SmallTitle("认证")
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindowDropdownPreference(
                        title = "认证方式",
                        summary = authType,
                        items = listOf("无", "Bearer Token", "自定义 Header"),
                        selectedIndex = when (authType) {
                            "Bearer" -> 1
                            "Header" -> 2
                            else -> 0
                        },
                        onSelectedIndexChange = { index ->
                            authType = when (index) {
                                1 -> "Bearer"
                                2 -> "Header"
                                else -> "无"
                            }
                        },
                    )
                    if (authType == "Header") {
                        McpField("Header 名称", headerName, { headerName = it }, "X-API-Key")
                    }
                    if (authType != "无") {
                        McpField(
                            "Token / Header 值",
                            token,
                            { token = it },
                            if (existing?.auth is McpAuth.None || existing == null) "输入后将加密保存" else "留空表示不修改",
                            secret = true,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = {
                    val config = buildConfig()
                    persistToken(config)
                    scope.launch {
                        val result = McpManager.probe(config)
                        probeResult = result
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("测试连接") }
            Button(
                onClick = {
                    val config = buildConfig()
                    if (config.namespace.isBlank() ||
                        (config.transport != McpTransport.Stdio && config.url.isBlank()) ||
                        (config.transport == McpTransport.Stdio && config.stdio?.command.isNullOrBlank())
                    ) {
                        Toast.makeText(context, "请填写名称、命名空间、URL 或 stdio 命令", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (config.transport == McpTransport.Stdio && !stdioConfirmed) {
                        Toast.makeText(context, "请先确认 stdio 命令以 shell 身份运行", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    persistToken(config)
                    McpStore.upsert(context, config)
                    McpManager.refresh(context)
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("保存") }
        }

        if (existing != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    McpStore.remove(context, existing.id)
                    McpManager.refresh(context)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.error,
                ),
            ) { Text("删除这个 MCP 服务") }

            val tools = allTools[existing.id].orEmpty()
            val policyVersion = reload
            if (tools.isNotEmpty()) {
                SmallTitle("工具清单")
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    tools.forEach { tool ->
                        val policy = remember(existing.id, tool.rawName, policyVersion) {
                            McpStore.byId(context, existing.id)
                                ?.toolPolicies?.get(tool.rawName) ?: McpToolPolicy.Ask
                        }
                        WindowDropdownPreference(
                            title = tool.title.ifBlank { tool.rawName },
                            summary = buildString {
                                append(tool.rawName)
                                append(" · ")
                                append(policyLabel(policy))
                                if (tool.description.isNotBlank()) {
                                    append(" · ")
                                    append(tool.description.take(80))
                                }
                            },
                            items = listOf("每次询问", "始终允许", "始终拒绝"),
                            selectedIndex = when (policy) {
                                McpToolPolicy.Ask -> 0
                                McpToolPolicy.Allow -> 1
                                McpToolPolicy.Deny -> 2
                            },
                            onSelectedIndexChange = { index ->
                                McpManager.setToolPolicy(
                                    context,
                                    existing.id,
                                    tool.rawName,
                                    when (index) {
                                        1 -> McpToolPolicy.Allow
                                        2 -> McpToolPolicy.Deny
                                        else -> McpToolPolicy.Ask
                                    },
                                )
                                reload++
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    probeResult?.let { result ->
        WindowDialog(
            show = true,
            title = if (result.ok) "连接成功" else "连接失败",
            summary = buildString {
                if (result.ok) {
                    append("协议：").append(result.protocolVersion).append('\n')
                    append("服务：").append(result.serverInfo?.name.orEmpty()).append('\n')
                    append("工具：").append(result.toolNames.size).append(" 个\n")
                    if (result.toolNames.isNotEmpty()) {
                        append(result.toolNames.take(30).joinToString("\n") { "· $it" })
                        if (result.toolNames.size > 30) append("\n…")
                        append('\n')
                    }
                    append("耗时：").append(result.durationMs).append("ms")
                } else {
                    append(result.error.orEmpty())
                }
            },
            onDismissRequest = { probeResult = null },
        ) {
            Button(
                onClick = { probeResult = null },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("关闭") }
        }
    }
}

@Composable
private fun McpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    secret: Boolean = false,
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label.ifBlank { placeholder },
        useLabelAsPlaceholder = true,
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        visualTransformation = if (secret) PasswordVisualTransformation()
        else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun parseEnv(text: String): Map<String, String> = buildMap {
    text.lineSequence().forEach { line ->
        val index = line.indexOf('=')
        if (index > 0) {
            val key = line.substring(0, index).trim()
            val value = line.substring(index + 1).trim()
            if (key.isNotBlank() && value.isNotBlank()) put(key, value)
        }
    }
}

private fun refFor(config: McpServerConfig): String = when (val auth = config.auth) {
    is McpAuth.Bearer -> auth.secretRef
    is McpAuth.Header -> auth.secretRef
    else -> "mcp:${config.id}:token"
}

private fun transportLabel(transport: McpTransport): String = when (transport) {
    McpTransport.StreamableHttp -> "Streamable HTTP"
    McpTransport.LegacySse -> "HTTP + SSE（旧版）"
    McpTransport.Stdio -> "stdio（Shizuku）"
}

private fun policyLabel(policy: McpToolPolicy): String = when (policy) {
    McpToolPolicy.Ask -> "每次询问"
    McpToolPolicy.Allow -> "始终允许"
    McpToolPolicy.Deny -> "始终拒绝"
}

private fun statusLabel(status: McpServerStatus): String = when (status) {
    is McpServerStatus.Disabled -> "已停用"
    is McpServerStatus.Connecting -> "连接中"
    is McpServerStatus.Ready -> "已连接"
    is McpServerStatus.Reconnecting -> "重连中（${status.attempt}/${status.maxAttempts}）"
    is McpServerStatus.Failed -> "失败：${status.reason.name}"
}

private fun statusColor(status: McpServerStatus): Color = when (status) {
    is McpServerStatus.Ready -> Color(0xFF34C759)
    is McpServerStatus.Connecting, is McpServerStatus.Reconnecting -> Color(0xFFFF9F0A)
    is McpServerStatus.Failed -> Color(0xFFFF3B30)
    is McpServerStatus.Disabled -> Color(0xFF8E8E93)
}
