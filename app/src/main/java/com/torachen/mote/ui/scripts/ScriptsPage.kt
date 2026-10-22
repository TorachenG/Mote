@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.scripts

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog

import com.torachen.mote.service.UserScript
import com.torachen.mote.service.UserScriptStore
import com.torachen.mote.ui.LegalDocumentScaffold
import java.net.HttpURLConnection
import java.net.URL

/** 页面间传递正在编辑的脚本 id（null 表示新建）。 */
object ScriptsNavArgs {
    var editingId: String? = null

    /** 脚本数据版本号：保存/更新后自增，列表页据此刷新。 */
    var storeVersion by mutableIntStateOf(0)
}

// ============================================================
// 脚本列表页
// ============================================================

@Composable
fun ScriptsPage(
    onBack: () -> Unit,
    onOpenEditor: (String?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var scripts by remember { mutableStateOf(UserScriptStore.list(context)) }
    val storeVersion = ScriptsNavArgs.storeVersion
    LaunchedEffect(storeVersion) {
        scripts = UserScriptStore.list(context)
    }
    var masterEnabled by remember { mutableStateOf(UserScriptStore.masterEnabled(context)) }

    var showMoreMenu by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var menuTarget by remember { mutableStateOf<UserScript?>(null) }
    var renameTarget by remember { mutableStateOf<UserScript?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<UserScript?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun reload() {
        scripts = UserScriptStore.list(context)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.getOrNull()
                }
                if (text.isNullOrBlank()) {
                    Toast.makeText(context, "读取脚本失败", Toast.LENGTH_SHORT).show()
                } else {
                    addScriptFromText(context, text, sourceUrl = null)
                    reload()
                    Toast.makeText(context, "脚本已导入", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LegalDocumentScaffold(
        title = "脚本",
        onBack = onBack,
        actions = {
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = MiuixIcons.More,
                        contentDescription = "更多",
                        tint = MiuixTheme.colorScheme.onSurface,
                    )
                }
                WindowCascadingListPopup(
                    show = showMoreMenu,
                    entries = listOf(
                        DropdownEntry(
                            items = listOf(
                                DropdownItem(
                                    text = "添加脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenEditor(null)
                                    },
                                ),
                                DropdownItem(
                                    text = "下载脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        showUrlDialog = true
                                    },
                                ),
                                DropdownItem(
                                    text = "导入脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        importLauncher.launch(arrayOf("*/*"))
                                    },
                                ),
                            ),
                        ),
                        DropdownEntry(
                            items = listOf(
                                DropdownItem(
                                    text = "更新脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        if (busy) return@DropdownItem
                                        val downloadable = scripts.filter { !it.sourceUrl.isNullOrBlank() }
                                        if (downloadable.isEmpty()) {
                                            Toast.makeText(context, "没有可更新的脚本", Toast.LENGTH_SHORT).show()
                                        } else {
                                            busy = true
                                            scope.launch {
                                                var updated = 0
                                                withContext(Dispatchers.IO) {
                                                    downloadable.forEach { script ->
                                                        val body = runCatching {
                                                            httpGetText(script.sourceUrl!!)
                                                        }.getOrNull()
                                                        if (!body.isNullOrBlank()) {
                                                            val (name, version) =
                                                                UserScriptStore.readMeta(body)
                                                            UserScriptStore.upsert(
                                                                context,
                                                                script.copy(
                                                                    name = name ?: script.name,
                                                                    version = version ?: script.version,
                                                                    code = body,
                                                                ),
                                                            )
                                                            updated++
                                                        }
                                                    }
                                                }
                                                busy = false
                                                reload()
                                                Toast.makeText(
                                                    context,
                                                    if (updated > 0) "已更新 $updated 个脚本" else "更新失败，请检查网络",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    },
                                ),
                            ),
                        ),
                    ),
                    onDismissRequest = { showMoreMenu = false },
                )
            }
        },
    ) {
        item(key = "feature_title") {
            SmallTitle("功能")
        }
        item(key = "feature_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                SwitchPreference(
                    title = "启用脚本",
                    checked = masterEnabled,
                    onCheckedChange = { checked ->
                        masterEnabled = checked
                        UserScriptStore.setMasterEnabled(context, checked)
                    },
                )
            }
        }

        item(key = "script_title") {
            SmallTitle("脚本")
        }
        item(key = "script_card") {
            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        if (scripts.isEmpty()) {
                            Text(
                                text = "还没有脚本，点右上角「更多」添加",
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                            )
                        } else {
                            scripts.forEachIndexed { index, script ->
                                if (index > 0) {
                                    Spacer(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp)
                                            .height(0.5.dp)
                                            .background(
                                                MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.15f)
                                            ),
                                    )
                                }
                                ScriptRow(
                                    script = script,
                                    onToggle = { checked ->
                                        UserScriptStore.setEnabled(context, script.id, checked)
                                        reload()
                                    },
                                    onClick = {
                                        ScriptsNavArgs.editingId = script.id
                                        onOpenEditor(script.id)
                                    },
                                    onLongClick = { menuTarget = script },
                                )
                            }
                        }
                    }
                }

                WindowCascadingListPopup(
                    show = menuTarget != null,
                    entries = listOf(
                        DropdownEntry(
                            items = listOf(
                                DropdownItem(
                                    text = "编辑",
                                    onClick = {
                                        val target = menuTarget
                                        menuTarget = null
                                        if (target != null) {
                                            ScriptsNavArgs.editingId = target.id
                                            onOpenEditor(target.id)
                                        }
                                    },
                                ),
                                DropdownItem(
                                    text = "重命名",
                                    onClick = {
                                        val target = menuTarget
                                        menuTarget = null
                                        if (target != null) {
                                            renameTarget = target
                                            renameText = target.name
                                        }
                                    },
                                ),
                                DropdownItem(
                                    text = "删除",
                                    onClick = {
                                        val target = menuTarget
                                        menuTarget = null
                                        deleteTarget = target
                                    },
                                ),
                            ),
                        ),
                    ),
                    onDismissRequest = { menuTarget = null },
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(
                Modifier
                    .height(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                    )
            )
        }
    }

    // 下载脚本：填写地址
    if (showUrlDialog) {
        var urlText by remember { mutableStateOf("") }
        WindowDialog(
            show = showUrlDialog,
            title = "下载脚本",
            onDismissRequest = { showUrlDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = "脚本地址",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { showUrlDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            val url = urlText.trim()
                            if (url.isBlank()) {
                                Toast.makeText(context, "请填写脚本地址", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            showUrlDialog = false
                            busy = true
                            scope.launch {
                                val body = withContext(Dispatchers.IO) {
                                    runCatching { httpGetText(url) }.getOrNull()
                                }
                                busy = false
                                if (body.isNullOrBlank()) {
                                    Toast.makeText(context, "下载失败，请检查地址", Toast.LENGTH_SHORT).show()
                                } else {
                                    addScriptFromText(context, body, sourceUrl = url)
                                    reload()
                                    Toast.makeText(context, "脚本已添加", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("下载") }
                }
            }
        }
    }

    // 重命名
    val renaming = renameTarget
    if (renaming != null) {
        WindowDialog(
            show = true,
            title = "重命名",
            onDismissRequest = { renameTarget = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = "脚本名称",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { renameTarget = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            val newName = renameText.trim()
                            if (newName.isNotBlank()) {
                                UserScriptStore.rename(context, renaming.id, newName)
                                reload()
                            }
                            renameTarget = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("确定") }
                }
            }
        }
    }

    // 删除确认
    val deleting = deleteTarget
    if (deleting != null) {
        WindowDialog(
            show = true,
            title = "删除脚本",
            onDismissRequest = { deleteTarget = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "确定删除「${deleting.name}」吗？",
                    fontSize = 15.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { deleteTarget = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            UserScriptStore.delete(context, deleting.id)
                            deleteTarget = null
                            reload()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("删除") }
                }
            }
        }
    }
}

@Composable
private fun ScriptRow(
    script: UserScript,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    // 用 Miuix 的 BasicComponent，间距/左右内边距与设置页的 SwitchPreference 完全一致
    BasicComponent(
        modifier = Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        title = script.name,
        summary = "版本 ${script.version}",
        endActions = {
            Switch(
                checked = script.enabled,
                onCheckedChange = onToggle,
            )
        },
    )
}

// ============================================================
// 脚本编辑页
// ============================================================

@Composable
fun ScriptEditorPage(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val editingId = ScriptsNavArgs.editingId

    val existing = remember(editingId) {
        editingId?.let { id -> UserScriptStore.list(context).firstOrNull { it.id == id } }
    }

    var code by remember {
        mutableStateOf(existing?.code ?: UserScriptStore.DEFAULT_CODE)
    }
    var name by remember { mutableStateOf(existing?.name ?: "新建脚本") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MiuixIcons.ChevronBackward,
                    contentDescription = "返回",
                    tint = MiuixTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = if (editingId == null) "新建脚本" else name,
                fontSize = 18.sp,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            IconButton(onClick = {
                val trimmed = code.trim()
                if (trimmed.isEmpty()) {
                    Toast.makeText(context, "脚本内容不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    val (metaName, metaVersion) = UserScriptStore.readMeta(trimmed)
                    val script = UserScript(
                        id = existing?.id ?: UserScriptStore.newId(),
                        name = metaName ?: existing?.name ?: "未命名脚本",
                        version = metaVersion ?: existing?.version ?: "1.0.0",
                        code = trimmed,
                        enabled = existing?.enabled ?: true,
                        sourceUrl = existing?.sourceUrl,
                    )
                    UserScriptStore.upsert(context, script)
                    ScriptsNavArgs.storeVersion++
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
            }) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "确定",
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer)
                .padding(12.dp),
        ) {
            BasicTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            )
        }
    }
}

// ============================================================
// 工具
// ============================================================

private fun addScriptFromText(context: Context, text: String, sourceUrl: String?) {
    val (name, version) = UserScriptStore.readMeta(text)
    val script = UserScript(
        id = UserScriptStore.newId(),
        name = name ?: "导入脚本",
        version = version ?: "1.0.0",
        code = text,
        enabled = true,
        sourceUrl = sourceUrl,
    )
    UserScriptStore.upsert(context, script)
}

private fun httpGetText(urlStr: String): String? {
    return try {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.requestMethod = "GET"
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
        )
        if (conn.responseCode !in 200..299) return null
        conn.inputStream.bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        null
    }
}
