package com.torachen.mote.ui.ai.mcp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

/** MCP 工具调用的确认弹窗；由 AiChatPage / AiFloatingActivity 各挂一次。 */
@Composable
internal fun AiToolApprovalDialog() {
    val request = AiToolApproval.request ?: return
    WindowDialog(
        show = true,
        title = "允许调用 MCP 工具？",
        summary = buildString {
            append(request.serverName.ifBlank { "MCP 服务" })
            append(" · ")
            append(request.toolName)
            append('\n')
            append(request.description.take(400))
        },
        onDismissRequest = { AiToolApproval.resolve(AiToolApprovalDecision.Deny) },
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (request.risky) {
                Text(
                    text = "该工具被标记为可能产生副作用，请确认参数后再允许。",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = "参数：${request.argumentsPreview}",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { AiToolApproval.resolve(AiToolApprovalDecision.Deny) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("拒绝") }
                Button(
                    onClick = { AiToolApproval.resolve(AiToolApprovalDecision.AllowOnce) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("仅本次") }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { AiToolApproval.resolve(AiToolApprovalDecision.DenyAlways) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("始终拒绝") }
                Button(
                    onClick = { AiToolApproval.resolve(AiToolApprovalDecision.AllowAlways) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("始终允许") }
            }
        }
    }
}
