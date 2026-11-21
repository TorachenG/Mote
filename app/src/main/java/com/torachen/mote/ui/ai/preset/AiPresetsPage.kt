package com.torachen.mote.ui.ai.preset

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.AiFilePermission
import com.torachen.mote.ui.ai.AiProviderStore
import com.torachen.mote.ui.ai.AiReasoningLevel
import com.torachen.mote.ui.ai.AiSettingsScaffold
import com.torachen.mote.ui.ai.aiBuiltInToolOptions
import com.torachen.mote.ui.ai.mcp.McpStore
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

internal object AiPresetNav {
    var editingId: String? = null
    fun open(id: String) { editingId = id }
    fun create() { editingId = null }
}

@Composable
fun AiPresetsPage(
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    val presets = AiPresetStore.all(context)
    val defaultId = AiPresetStore.defaultId()

    AiSettingsScaffold(
        title = "Agent 预设",
        pageRoute = "ai_presets",
        onBack = onBack,
        floatingBottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        AiPresetNav.create()
                        onEdit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("新建预设") }
            }
        },
    ) {
        SmallTitle("Agent 预设")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "预设决定一次对话的模型、提示词、可用工具、MCP、文件权限和自动操作。",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            presets.forEach { preset ->
                SearchableItem(
                    title = preset.name,
                    pageRoute = "ai_presets",
                    pageTitle = "Agent 预设",
                    itemKey = "ai_preset_${preset.id}",
                ) {
                    BasicComponent(
                        title = "${preset.icon}  ${preset.name}" +
                                if (preset.id == defaultId) "   · 默认" else "",
                        summary = preset.description.ifBlank {
                            "${preset.allowedTools?.size ?: "全部"} 个工具 · " +
                                    "${preset.mcpServerIds?.size ?: "全部"} 个 MCP"
                        },
                        onClick = {
                            AiPresetNav.open(preset.id)
                            onEdit()
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
fun AiPresetEditPage(onBack: () -> Unit) {
    val context = LocalContext.current
    AiProviderStore.ensureLoaded(context)
    AiPresetStore.ensureLoaded(context)
    val editingId = AiPresetNav.editingId
    val existing = remember(editingId) { editingId?.let { AiPresetStore.byId(it) } }
    val providers = AiProviderStore.providers
    val mcpServers = remember { McpStore.all(context) }
    val tools = remember { aiBuiltInToolOptions() }

    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var icon by remember { mutableStateOf(existing?.icon ?: "🤖") }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var useGlobalPrompt by remember { mutableStateOf(existing?.systemPrompt == null) }
    var prompt by remember { mutableStateOf(existing?.systemPrompt.orEmpty()) }
    var providerId by remember { mutableStateOf(existing?.providerId) }
    var modelId by remember { mutableStateOf(existing?.modelId) }
    var reasoning by remember { mutableStateOf(existing?.reasoning) }
    var filePermission by remember { mutableStateOf(existing?.filePermission) }
    var deviceControl by remember { mutableStateOf(existing?.deviceControl) }
    var contextTurns by remember { mutableStateOf(existing?.contextTurns) }
    var useAllTools by remember { mutableStateOf(existing?.allowedTools == null) }
    val selectedTools = remember {
        mutableStateListOf<String>().apply { addAll(existing?.allowedTools.orEmpty()) }
    }
    var useAllMcp by remember { mutableStateOf(existing?.mcpServerIds == null) }
    val selectedMcp = remember {
        mutableStateListOf<String>().apply { addAll(existing?.mcpServerIds.orEmpty()) }
    }
    var backgroundAllowed by remember { mutableStateOf(existing?.backgroundAllowed ?: false) }
    var showDelete by remember { mutableStateOf(false) }

    val provider = providerId?.let { AiProviderStore.byId(it) }
    val modelOptions = provider?.models.orEmpty()

    fun buildPreset(): AiAgentPreset {
        val id = existing?.id ?: ("preset_" + System.currentTimeMillis().toString(36))
        return AiAgentPreset(
            id = id,
            name = name.trim().ifBlank { "未命名预设" },
            icon = icon.trim().ifBlank { "🤖" }.take(8),
            description = description.trim().take(500),
            systemPrompt = if (useGlobalPrompt) null else prompt.trim().takeIf { it.isNotBlank() },
            providerId = providerId,
            modelId = modelId,
            reasoning = reasoning,
            allowedTools = if (useAllTools) null else selectedTools.toSet(),
            mcpServerIds = if (useAllMcp) null else selectedMcp.toSet(),
            filePermission = filePermission,
            deviceControl = deviceControl,
            contextTurns = contextTurns,
            backgroundAllowed = backgroundAllowed,
            builtIn = existing?.builtIn ?: false,
            createdAt = existing?.createdAt ?: 0L,
        )
    }

    AiSettingsScaffold(
        title = if (existing == null) "新建 Agent 预设" else "编辑 Agent 预设",
        pageRoute = "ai_preset_edit",
        onBack = onBack,
    ) {
        SmallTitle("基本信息")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PresetField("名称", name, { name = it }, "例如 研究助手")
                PresetField("图标（emoji）", icon, { icon = it }, "🤖")
                PresetField("描述", description, { description = it }, "说明这个预设适合什么任务")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("使用全局系统提示词", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = useGlobalPrompt, onCheckedChange = { useGlobalPrompt = it })
                }
                if (!useGlobalPrompt) {
                    PresetField(
                        "系统提示词",
                        prompt,
                        { prompt = it },
                        "定义身份、边界和回答格式",
                        singleLine = false,
                        minHeight = 140.dp,
                    )
                }
            }
        }

        SmallTitle("模型与推理")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WindowDropdownPreference(
                    title = "默认服务",
                    summary = provider?.name ?: "跟随当前服务",
                    items = listOf("跟随当前服务") + providers.map { it.name },
                    selectedIndex = providerId?.let { id ->
                        providers.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.plus(1)
                    } ?: 0,
                    onSelectedIndexChange = { index ->
                        providerId = providers.getOrNull(index - 1)?.id
                        modelId = null
                    },
                )
                if (provider != null && modelOptions.isNotEmpty()) {
                    WindowDropdownPreference(
                        title = "默认模型",
                        summary = modelId ?: "跟随服务默认（${provider.model}）",
                        items = listOf("跟随服务默认") + modelOptions,
                        selectedIndex = modelId?.let { id ->
                            modelOptions.indexOfFirst { it == id }.takeIf { it >= 0 }?.plus(1)
                        } ?: 0,
                        onSelectedIndexChange = { index ->
                            modelId = modelOptions.getOrNull(index - 1)
                        },
                    )
                }
                WindowDropdownPreference(
                    title = "推理等级",
                    summary = reasoning?.label ?: "跟随全局",
                    items = listOf("跟随全局") + AiReasoningLevel.entries.map { it.label },
                    selectedIndex = reasoning?.let { level ->
                        AiReasoningLevel.entries.indexOf(level).takeIf { it >= 0 }?.plus(1)
                    } ?: 0,
                    onSelectedIndexChange = { index ->
                        reasoning = AiReasoningLevel.entries.getOrNull(index - 1)
                    },
                )
            }
        }

        SmallTitle("能力与权限")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WindowDropdownPreference(
                    title = "文件权限",
                    summary = filePermission?.label ?: "跟随全局",
                    items = listOf("跟随全局") + AiFilePermission.entries.map { it.label },
                    selectedIndex = filePermission?.let { permission ->
                        AiFilePermission.entries.indexOf(permission).takeIf { it >= 0 }?.plus(1)
                    } ?: 0,
                    onSelectedIndexChange = { index ->
                        filePermission = AiFilePermission.entries.getOrNull(index - 1)
                    },
                )
                WindowDropdownPreference(
                    title = "自动操作",
                    summary = when (deviceControl) {
                        true -> "开启"
                        false -> "关闭"
                        null -> "跟随全局"
                    },
                    items = listOf("跟随全局", "开启", "关闭"),
                    selectedIndex = when (deviceControl) {
                        null -> 0
                        true -> 1
                        false -> 2
                    },
                    onSelectedIndexChange = { index ->
                        deviceControl = when (index) {
                            1 -> true
                            2 -> false
                            else -> null
                        }
                    },
                )
                WindowDropdownPreference(
                    title = "上下文轮数",
                    summary = contextTurns?.let { if (it == 0) "不携带历史" else "最近 $it 轮" } ?: "跟随全局",
                    items = listOf("跟随全局", "不携带历史", "6 轮", "12 轮", "20 轮", "40 轮"),
                    selectedIndex = when (contextTurns) {
                        null -> 0
                        0 -> 1
                        6 -> 2
                        12 -> 3
                        20 -> 4
                        40 -> 5
                        else -> 0
                    },
                    onSelectedIndexChange = { index ->
                        contextTurns = when (index) {
                            1 -> 0
                            2 -> 6
                            3 -> 12
                            4 -> 20
                            5 -> 40
                            else -> null
                        }
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("允许后台任务使用", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = backgroundAllowed, onCheckedChange = { backgroundAllowed = it })
                }
            }
        }

        SmallTitle("内置工具")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("使用全部内置工具", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = useAllTools, onCheckedChange = { useAllTools = it })
                }
                if (!useAllTools) {
                    tools.forEach { (toolName, title) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("$title（$toolName）", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                            Checkbox(
                                state = if (toolName in selectedTools) ToggleableState.On else ToggleableState.Off,
                                onClick = {
                                    if (toolName in selectedTools) selectedTools.remove(toolName)
                                    else selectedTools.add(toolName)
                                },
                            )
                        }
                    }
                }
            }
        }

        SmallTitle("MCP 服务")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("使用全部已启用 MCP", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
                    Switch(checked = useAllMcp, onCheckedChange = { useAllMcp = it })
                }
                if (!useAllMcp) {
                    if (mcpServers.isEmpty()) {
                        Text(
                            "还没有 MCP 服务；先去 AI 设置 → MCP 服务添加。",
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    } else {
                        mcpServers.forEach { server ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    server.displayName,
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                )
                                Checkbox(
                                    state = if (server.id in selectedMcp) ToggleableState.On else ToggleableState.Off,
                                    onClick = {
                                        if (server.id in selectedMcp) selectedMcp.remove(server.id)
                                        else selectedMcp.add(server.id)
                                    },
                                )
                            }
                        }
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
                    AiPresetStore.upsert(context, buildPreset())
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("保存") }
            if (existing != null) {
                Button(
                    onClick = {
                        AiPresetStore.setDefault(context, existing.id)
                        Toast.makeText(context, "已设为默认预设", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("设为默认") }
            }
        }

        if (existing != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (AiPresetStore.duplicate(context, existing.id) != null) {
                        Toast.makeText(context, "已复制预设，可在列表中继续编辑", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("复制这个预设") }
            if (!existing.builtIn) {
                Button(
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.error,
                    ),
                ) { Text("删除这个预设") }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDelete && existing != null) {
        WindowDialog(
            show = true,
            title = "删除预设",
            summary = "删除后，使用这个预设的对话会回退到默认预设；已生成的对话内容不受影响。",
            onDismissRequest = { showDelete = false },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showDelete = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        AiPresetStore.remove(context, existing.id)
                        showDelete = false
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("删除") }
            }
        }
    }
}

@Composable
private fun PresetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 0.dp,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label.ifBlank { placeholder },
        useLabelAsPlaceholder = label.isBlank() && placeholder.isNotBlank(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
    )
}
