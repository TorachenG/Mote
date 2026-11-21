package com.torachen.mote.ui.ai.preset

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.service.MoteAccessibilityService
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
                            if (item.deviceControl == true) {
                                val enabled = MoteAccessibilityService.isEnabled(context)
                                val connected = MoteAccessibilityService.instance != null
                                val message = when {
                                    !enabled ->
                                        "已切换为手机模式，但系统无障碍未开启，暂时不能操作手机。请到设置 → 无障碍开启 Mote AI 自动操作。"

                                    !connected ->
                                        "已切换为手机模式，正在等待无障碍服务连接。"

                                    else ->
                                        "已切换为手机模式，自动操作可用。"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
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

