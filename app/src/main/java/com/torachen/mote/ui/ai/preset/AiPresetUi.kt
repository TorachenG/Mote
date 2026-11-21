package com.torachen.mote.ui.ai.preset

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.ai.AiChatStore
import com.torachen.mote.ui.ai.AiConversationSettings
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
                    summary = item.description.ifBlank { "跟随全局 AI 设置" },
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

