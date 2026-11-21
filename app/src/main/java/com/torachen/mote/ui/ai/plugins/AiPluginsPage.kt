package com.torachen.mote.ui.ai.plugins

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.AiSettingsScaffold
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun AiPluginsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    var reload by remember { mutableIntStateOf(0) }
    val plugins = remember(reload) { AiPluginStore.all(context) }
    var selected by remember { mutableStateOf<AiPlugin?>(null) }
    var installed by remember { mutableStateOf<AiPlugin?>(null) }

    val installLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val result = AiPluginStore.install(context, uri)
            result.onSuccess { plugin ->
                installed = plugin
                reload++
                Toast.makeText(context, "已安装 ${plugin.name}，请确认权限后启用", Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                Toast.makeText(context, "安装失败：${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AiSettingsScaffold(
        title = "插件",
        pageRoute = "ai_plugins",
        onBack = onBack,
        floatingBottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = { installLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("安装插件（ZIP）") }
            }
        },
    ) {
        SmallTitle("插件")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Text(
                "插件只支持声明式能力：HTTP 工具、提示词、MCP 配置；不执行第三方 Node/JS 代码。",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(14.dp),
            )
        }
        if (plugins.isEmpty()) {
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("暂无插件", fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
                }
            }
        } else {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                plugins.forEach { plugin ->
                    SearchableItem(
                        title = plugin.name,
                        pageRoute = "ai_plugins",
                        pageTitle = "插件",
                        itemKey = "ai_plugin_${plugin.id}",
                    ) {
                        BasicComponent(
                            title = "${plugin.icon}  ${plugin.name}",
                            summary = "v${plugin.version} · ${plugin.tools.size} 个工具 · " +
                                    plugin.permissions.joinToString("、") { it.label },
                            endActions = {
                                Switch(
                                    checked = plugin.enabled,
                                    onCheckedChange = { enabled ->
                                        AiPluginStore.setEnabled(context, plugin.id, enabled)
                                        reload++
                                    },
                                )
                            },
                            onClick = { selected = plugin },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }

    installed?.let { plugin ->
        WindowDialog(
            show = true,
            title = "已安装：${plugin.name}",
            summary = "插件默认禁用。启用前请确认它申请的权限和工具。",
            onDismissRequest = { installed = null },
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                plugin.permissions.forEach { permission ->
                    Text("· ${permission.label}", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                }
                plugin.tools.forEach { tool ->
                    Text(
                        "· ${tool.name}（${tool.method}）",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { installed = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) { Text("稍后") }
                    Button(
                        onClick = {
                            AiPluginStore.setEnabled(context, plugin.id, true)
                            installed = null
                            reload++
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = MiuixTheme.colorScheme.onPrimary,
                        ),
                    ) { Text("启用") }
                }
            }
        }
    }

    selected?.let { plugin ->
        val readme = remember(plugin.id) { AiPluginStore.readme(context, plugin.id) }
        WindowDialog(
            show = true,
            title = "${plugin.icon} ${plugin.name}",
            summary = "v${plugin.version} · ${plugin.author.ifBlank { "未知作者" }}",
            onDismissRequest = { selected = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(plugin.description, fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text("权限", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                plugin.permissions.forEach {
                    Text("· ${it.label}（${it.wire}）", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                Spacer(Modifier.height(8.dp))
                Text("工具", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface)
                plugin.tools.forEach {
                    Text(
                        "· ${it.name}：${it.description.take(120)}",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                if (readme.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(readme.take(4_000), fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            AiPluginStore.setEnabled(context, plugin.id, !plugin.enabled)
                            selected = null
                            reload++
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = if (plugin.enabled) MiuixTheme.colorScheme.secondaryContainer
                            else MiuixTheme.colorScheme.primary,
                            contentColor = if (plugin.enabled) MiuixTheme.colorScheme.onSurface
                            else MiuixTheme.colorScheme.onPrimary,
                        ),
                    ) { Text(if (plugin.enabled) "禁用" else "启用") }
                    Button(
                        onClick = {
                            AiPluginStore.remove(context, plugin.id)
                            selected = null
                            reload++
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.error,
                        ),
                    ) { Text("卸载") }
                }
            }
        }
    }
}
