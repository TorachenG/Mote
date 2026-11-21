@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.widget.Toast
import android.provider.Settings as AndroidSettings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.torachen.mote.service.MoteAccessibilityService
import com.torachen.mote.MoteAutoSync
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.mcp.McpStore
import com.torachen.mote.ui.ai.preset.AiPresetStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

private val AI_CONTEXT_TURN_OPTIONS = listOf(
    0 to "不携带历史消息",
    6 to "最近 6 轮",
    12 to "最近 12 轮",
    20 to "最近 20 轮",
    40 to "最近 40 轮",
)

private val AI_FILE_LIMIT_OPTIONS = listOf(
    20_000 to "20,000 字符",
    60_000 to "60,000 字符",
    120_000 to "120,000 字符",
    200_000 to "200,000 字符",
    400_000 to "400,000 字符",
)

/**
 * AI 设置页。
 *
 * 结构与「法律信息」「搜索设置」等设置页一致：
 * 小节标题 + 白色圆角卡片 + Preference 组件。
 */
@Composable
fun AiSettingsPage(
    onBack: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenMcp: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenUsage: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenPlugins: () -> Unit,
) {
    val context = LocalContext.current
    AiProviderStore.ensureLoaded(context)
    AiSettings.ensureLoaded(context)
    AiChatStore.ensureLoaded(context)

    var showPromptDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = runCatching {
                AiChatStore.flush()
                withContext(Dispatchers.IO) { AiChatArchive.export(context, uri) }
            }
            Toast.makeText(
                context,
                result.fold(
                    { count -> if (count > 0) "已导出 $count 个对话" else "暂无对话可导出" },
                    { error -> "导出失败：${error.message ?: "未知错误"}" },
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    var permissionTick by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val accessibilityEnabled = remember(permissionTick) {
        MoteAccessibilityService.isEnabled(context)
    }
    val overlayGranted = remember(permissionTick) {
        AndroidSettings.canDrawOverlays(context)
    }

    val provider = AiProviderStore.active
    val conversationCount = AiChatStore.conversations.size
    val mcpCount = McpStore.all(context).size
    val presetCount = AiPresetStore.all(context).size
    val taskCount = com.torachen.mote.ui.ai.tasks.AiTaskStore.all(context).size
    val pluginCount = com.torachen.mote.ui.ai.plugins.AiPluginStore.all(context).size
    val promptSummary = if (AiSettings.systemPrompt == AiSettings.DEFAULT_SYSTEM_PROMPT) {
        "默认"
    } else {
        "已自定义"
    }

    AiSettingsScaffold(
        title = "AI 设置",
        pageRoute = "settings_ai",
        onBack = onBack,
    ) {
        SmallTitle("服务")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SearchableItem(
                title = "AI 服务",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_provider",
            ) {
                ArrowPreference(
                    title = "AI 服务",
                    summary = provider?.let {
                        "${it.name} · ${it.displayModelName(it.model)} (${it.model})"
                    } ?: "未配置",
                    onClick = onOpenProviders,
                )
            }
            SearchableItem(
                title = "系统提示词",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_system_prompt",
            ) {
                ArrowPreference(
                    title = "系统提示词",
                    summary = promptSummary,
                    onClick = { showPromptDialog = true },
                )
            }
        }

        SmallTitle("对话")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SearchableItem(
                title = "流式输出",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_stream",
            ) {
                SwitchPreference(
                    title = "流式输出",
                    summary = "逐字显示回复；关闭后等待完整回答再显示",
                    checked = AiSettings.streamEnabled,
                    onCheckedChange = { AiSettings.setStreamEnabled(context, it) },
                )
            }
            SearchableItem(
                title = "显示思考过程",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_thinking",
            ) {
                SwitchPreference(
                    title = "显示思考过程",
                    summary = "展示推理模型返回的思考内容，完成后自动收起",
                    checked = AiSettings.showThinking,
                    onCheckedChange = { AiSettings.setShowThinking(context, it) },
                )
            }
            SearchableItem(
                title = "上下文轮数",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_turns",
            ) {
                val options = AI_CONTEXT_TURN_OPTIONS
                WindowDropdownPreference(
                    title = "上下文轮数",
                    summary = "每次请求携带最近多少轮历史消息",
                    items = options.map { it.second },
                    selectedIndex = options
                        .indexOfFirst { it.first == AiSettings.contextTurns }
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        options.getOrNull(index)?.let {
                            AiSettings.setContextTurns(context, it.first)
                        }
                    },
                )
            }
            SearchableItem(
                title = "对话标题根据内容调整",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_title_from_content",
            ) {
                SwitchPreference(
                    title = "对话标题根据内容调整",
                    summary = "开启后使用首条提问作为标题；关闭后显示最新发送的内容",
                    checked = AiSettings.titleFromContent,
                    onCheckedChange = { AiSettings.setTitleFromContent(context, it) },
                )
            }
            SearchableItem(
                title = "进入 AI 对话页面时新建对话",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_new_on_open",
            ) {
                SwitchPreference(
                    title = "进入 AI 对话页面时新建对话",
                    summary = "每次进入对话页都从新对话开始，历史对话仍保留在侧边栏",
                    checked = AiSettings.newConversationOnOpen,
                    onCheckedChange = { AiSettings.setNewConversationOnOpen(context, it) },
                )
            }
            SearchableItem(
                title = "清空对话记录",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_clear",
            ) {
                ArrowPreference(
                    title = "清空对话记录",
                    summary = if (conversationCount == 0) {
                        "暂无记录"
                    } else {
                        "共 $conversationCount 个对话，将全部清空"
                    },
                    onClick = {
                        if (conversationCount == 0) {
                            Toast.makeText(context, "暂无对话记录", Toast.LENGTH_SHORT).show()
                        } else {
                            showClearDialog = true
                        }
                    },
                )
            }
            SearchableItem(
                title = "导出对话备份",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_export",
            ) {
                ArrowPreference(
                    title = "导出对话备份",
                    summary = "打包全部对话和分支，不含 API Key 与设置",
                    onClick = {
                        val name = "Mote_AI_对话_" +
                                SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                                    .format(Date()) + ".zip"
                        exportLauncher.launch(name)
                    },
                )
            }
            SearchableItem(
                title = "导入对话备份",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_import",
            ) {
                ArrowPreference(
                    title = "导入对话备份",
                    summary = "先校验备份，通过后替换当前全部对话",
                    onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                )
            }
        }

        SmallTitle("能力")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SwitchPreference(
                title = "联网搜索",
                summary = "允许 AI 自主搜索网页、读取网页正文；关闭后无法回答实时信息。对话页输入框上方也能快速开关",
                checked = AiSettings.webSearchEnabled,
                onCheckedChange = { AiSettings.setWebSearchEnabled(context, it) },
            )
            SwitchPreference(
                title = "自动操作",
                summary = "允许 AI 自己判断何时读屏、点击、输入、滑动；关闭后仍可以打开应用，但不能操作界面",
                checked = AiSettings.agentEnabled,
                onCheckedChange = { enabled ->
                    AiSettings.setAgentEnabled(context, enabled)
                    if (enabled && !accessibilityEnabled) {
                        Toast.makeText(
                            context,
                            "请在系统无障碍设置开启Mote AI 自动操作",
                            Toast.LENGTH_SHORT,
                        ).show()
                        MoteAccessibilityService.openAccessibilitySettings(context)
                    }
                },
            )
            ArrowPreference(
                title = "无障碍服务",
                summary = if (accessibilityEnabled) "已开启" else "未开启",
                onClick = { MoteAccessibilityService.openAccessibilitySettings(context) },
            )
            ArrowPreference(
                title = "悬浮窗权限",
                summary = if (overlayGranted) "已授权" else "未授权",
                onClick = { AiFloatingController.requestOverlayPermission(context) },
            )
            ArrowPreference(
                title = "打开悬浮面板",
                summary = "不启动悬浮球，直接以悬浮窗口打开对话",
                onClick = {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(context, AiFloatingActivity::class.java)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }

        SmallTitle("扩展")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SearchableItem(
                title = "MCP 服务",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_mcp",
            ) {
                ArrowPreference(
                    title = "MCP 服务",
                    summary = if (mcpCount == 0) "未配置" else "$mcpCount 个服务",
                    onClick = onOpenMcp,
                )
            }
            SearchableItem(
                title = "Agent 预设",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_presets",
            ) {
                ArrowPreference(
                    title = "Agent 预设",
                    summary = "$presetCount 个预设 · 新建对话时选择",
                    onClick = onOpenPresets,
                )
            }
            SearchableItem(
                title = "插件",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_plugins",
            ) {
                ArrowPreference(
                    title = "插件",
                    summary = if (pluginCount == 0) "未安装" else "$pluginCount 个插件",
                    onClick = onOpenPlugins,
                )
            }
            SearchableItem(
                title = "自动化任务",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_tasks",
            ) {
                ArrowPreference(
                    title = "自动化任务",
                    summary = if (taskCount == 0) "未创建" else "$taskCount 个任务",
                    onClick = onOpenTasks,
                )
            }
            SearchableItem(
                title = "用量统计",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_usage",
            ) {
                ArrowPreference(
                    title = "用量统计",
                    summary = "请求数、token、费用趋势；仅本机保存",
                    onClick = onOpenUsage,
                )
            }
        }

        SmallTitle("文件")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SearchableItem(
                title = "单文件字符上限",
                pageRoute = "settings_ai",
                pageTitle = "AI 设置",
                itemKey = "settings_ai_file_limit",
            ) {
                val options = AI_FILE_LIMIT_OPTIONS
                WindowDropdownPreference(
                    title = "单文件字符上限",
                    summary = "超出部分会被截断，避免请求体过大",
                    items = options.map { it.second },
                    selectedIndex = options
                        .indexOfFirst { it.first == AiSettings.maxFileChars }
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        options.getOrNull(index)?.let {
                            AiSettings.setMaxFileChars(context, it.first)
                        }
                    },
                )
            }
        }
    }

    AiSystemPromptDialog(
        show = showPromptDialog,
        onDismiss = { showPromptDialog = false },
        onSave = { value ->
            AiSettings.setSystemPrompt(context, value)
            showPromptDialog = false
        },
    )

    WindowDialog(
        show = showClearDialog,
        title = "清空所有对话",
        summary = "将删除全部对话、输入草稿、生成文件记录以及每个对话单独选择的模型和权限设置，且无法恢复。",
        onDismissRequest = { showClearDialog = false },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { showClearDialog = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("取消") }
            Button(
                onClick = {
                    AiChatStore.clearAllConversations()
                    showClearDialog = false
                    Toast.makeText(context, "已清空对话记录", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) { Text("清空") }
        }
    }

    WindowDialog(
        show = showImportConfirm,
        title = "导入对话备份",
        summary = "导入会替换当前全部对话、分支和草稿，无法撤销。API Key 和设置不会改变。",
        onDismissRequest = {
            showImportConfirm = false
            pendingImportUri = null
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("取消") }
            Button(
                onClick = {
                    val uri = pendingImportUri
                    showImportConfirm = false
                    pendingImportUri = null
                    if (uri == null) return@Button
                    AiChatStore.prepareForImport()
                    scope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.IO) { AiChatArchive.import(context, uri) }
                        }
                        result.onSuccess { count ->
                            AiChatStore.reloadFromDisk(context)
                            runCatching { MoteAutoSync.requestSync(context) }
                            Toast.makeText(
                                context,
                                "已导入 $count 个对话",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }.onFailure { error ->
                            Toast.makeText(
                                context,
                                "导入失败：${error.message ?: "未知错误"}",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) { Text("导入并替换") }
        }
    }
}

@Composable
private fun AiSystemPromptDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(show) {
        if (show) value = AiSettings.systemPrompt
    }

    WindowDialog(
        show = show,
        title = "系统提示词",
        summary = "用于设定 AI 的身份、语气和回答格式",
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TextField(
                value = value,
                onValueChange = { value = it },
                label = "系统提示词",
                useLabelAsPlaceholder = true,
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "留空则恢复默认提示词。",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        value = AiSettings.DEFAULT_SYSTEM_PROMPT
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("默认") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        onSave(value)
                        Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("保存") }
            }
        }
    }
}
