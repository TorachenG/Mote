package com.torachen.mote.ui.ai.preset

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.ai.AiChatStore
import com.torachen.mote.ui.ai.AiConversationSettings
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

/** 输入栏上方的预设 chip；点击直接弹下拉菜单切换当前会话预设。 */
@Composable
internal fun AiPresetChip() {
    val context = LocalContext.current
    val conversationId = AiChatStore.activeConversationId
    val preset = AiConversationSettings.presetFor(conversationId) ?: return
    val presets = AiPresetStore.all(context)
    val entries = listOf(
        DropdownEntry(
            items = presets.map { item ->
                DropdownItem(
                    text = item.name,
                    selected = item.id == preset.id,
                    onClick = {
                        if (item.id != preset.id) {
                            AiConversationSettings.setPreset(context, conversationId, item.id)
                        }
                    },
                )
            },
        ),
    )

    WindowIconCascadingDropdownMenu(
        entries = entries,
        backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
        cornerRadius = 16.dp,
        minHeight = 36.dp,
        minWidth = 0.dp,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(36.dp).padding(horizontal = 12.dp),
            ) {
                MiuixText(
                    text = preset.name,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        },
    )
}

/** 新建对话时选择预设。 */
@Composable
internal fun AiPresetPickerDialog(
    show: Boolean,
    title: String = "选择 Agent 预设",
    subtitle: String = "预设决定模型、系统提示词、可用工具与权限。",
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    if (!show) return
    val context = LocalContext.current
    val presets = remember(show) { AiPresetStore.all(context) }
    val defaultId = AiPresetStore.defaultId()

    WindowDialog(
        show = true,
        title = title,
        summary = subtitle,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { preset ->
                BasicComponent(
                    title = "${preset.icon}  ${preset.name}" +
                            if (preset.id == defaultId) "   · 默认" else "",
                    summary = preset.description.ifBlank {
                        buildString {
                            append(if (preset.allowedTools == null) "全部内置工具" else "${preset.allowedTools.size} 个工具")
                            append(" · ")
                            append(if (preset.mcpServerIds == null) "全部 MCP" else "${preset.mcpServerIds.size} 个 MCP")
                            append(" · ")
                            append(preset.deviceControl?.let { if (it) "可自动操作" else "不自动操作" } ?: "自动操作跟随全局")
                        }
                    },
                    onClick = { onSelect(preset.id) },
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
