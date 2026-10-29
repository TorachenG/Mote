@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.scripts

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api as M3ExperimentalApi
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

import com.torachen.mote.service.UserScript
import com.torachen.mote.service.UserScriptStore
import com.torachen.mote.ui.KEY_PROGRESSIVE_TITLE_BLUR
import com.torachen.mote.ui.LegalDocumentScaffold
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.M3SegmentItem
import com.torachen.mote.ui.M3SegmentSwitchItem
import com.torachen.mote.ui.M3_SEGMENT_GROUP_GAP
import com.torachen.mote.ui.M3_SEGMENT_LIST_HORIZONTAL_PADDING
import com.torachen.mote.ui.MoteSegmentedColumn
import com.torachen.mote.ui.UiMode
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

    var showUrlDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<UserScript?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<UserScript?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun reload() {
        scripts = UserScriptStore.list(context)
    }

    fun openEditor(script: UserScript?) {
        ScriptsNavArgs.editingId = script?.id
        onOpenEditor(script?.id)
    }

    fun requestRename(script: UserScript) {
        renameTarget = script
        renameText = script.name
    }

    fun requestDelete(script: UserScript) {
        deleteTarget = script
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

    fun updateScripts() {
        if (busy) return
        val downloadable = scripts.filter { !it.sourceUrl.isNullOrBlank() }
        if (downloadable.isEmpty()) {
            Toast.makeText(context, "没有可更新的脚本", Toast.LENGTH_SHORT).show()
            return
        }
        busy = true
        scope.launch {
            var updated = 0
            withContext(Dispatchers.IO) {
                downloadable.forEach { script ->
                    val body = runCatching {
                        httpGetText(script.sourceUrl!!)
                    }.getOrNull()
                    if (!body.isNullOrBlank()) {
                        val (name, version) = UserScriptStore.readMeta(body)
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

    fun downloadScript(url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "请填写脚本地址", Toast.LENGTH_SHORT).show()
            return
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
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> ScriptsPageMiuix(
            onBack = onBack,
            scripts = scripts,
            masterEnabled = masterEnabled,
            onMasterEnabledChange = { checked ->
                masterEnabled = checked
                UserScriptStore.setMasterEnabled(context, checked)
            },
            onAddScript = { openEditor(null) },
            onDownloadScript = { showUrlDialog = true },
            onImportScript = { importLauncher.launch(arrayOf("*/*")) },
            onUpdateScripts = { updateScripts() },
            onEditScript = { script -> openEditor(script) },
            onRenameScript = { script -> requestRename(script) },
            onDeleteScript = { script -> requestDelete(script) },
            onToggleScript = { script, checked ->
                UserScriptStore.setEnabled(context, script.id, checked)
                reload()
            },
        )
        UiMode.Material -> ScriptsPageMaterial(
            onBack = onBack,
            scripts = scripts,
            masterEnabled = masterEnabled,
            onMasterEnabledChange = { checked ->
                masterEnabled = checked
                UserScriptStore.setMasterEnabled(context, checked)
            },
            onAddScript = { openEditor(null) },
            onDownloadScript = { showUrlDialog = true },
            onImportScript = { importLauncher.launch(arrayOf("*/*")) },
            onUpdateScripts = { updateScripts() },
            onEditScript = { script -> openEditor(script) },
            onRenameScript = { script -> requestRename(script) },
            onDeleteScript = { script -> requestDelete(script) },
            onToggleScript = { script, checked ->
                UserScriptStore.setEnabled(context, script.id, checked)
                reload()
            },
        )
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            // 下载脚本：填写地址
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

            // 重命名
            val renaming = renameTarget
            var lastRenaming by remember { mutableStateOf(renaming) }
            if (renaming != null) lastRenaming = renaming
            WindowDialog(
                show = renaming != null,
                title = "重命名",
                onDismissRequest = { renameTarget = null },
            ) {
                val script = lastRenaming
                if (script != null) {
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
                                        UserScriptStore.rename(context, script.id, newName)
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
            var lastDeleting by remember { mutableStateOf(deleting) }
            if (deleting != null) lastDeleting = deleting
            WindowDialog(
                show = deleting != null,
                title = "删除脚本",
                onDismissRequest = { deleteTarget = null },
            ) {
                val script = lastDeleting
                if (script != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "确定删除「${script.name}」吗？",
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
                                    UserScriptStore.delete(context, script.id)
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

        UiMode.Material -> {
            if (showUrlDialog) {
                M3DownloadScriptDialog(
                    onDismiss = { showUrlDialog = false },
                    onDownload = { url -> downloadScript(url) },
                )
            }

            val renaming = renameTarget
            if (renaming != null) {
                M3RenameScriptDialog(
                    initialName = renameText,
                    onDismiss = { renameTarget = null },
                    onConfirm = { newName ->
                        if (newName.isNotBlank()) {
                            UserScriptStore.rename(context, renaming.id, newName)
                            reload()
                        }
                        renameTarget = null
                    },
                )
            }

            val deleting = deleteTarget
            if (deleting != null) {
                M3DeleteScriptDialog(
                    scriptName = deleting.name,
                    onDismiss = { deleteTarget = null },
                    onConfirm = {
                        UserScriptStore.delete(context, deleting.id)
                        deleteTarget = null
                        reload()
                    },
                )
            }
        }
    }
}

// ============================================================
// 脚本列表页 - MIUIX 版
// ============================================================

@Composable
private fun ScriptsPageMiuix(
    onBack: () -> Unit,
    scripts: List<UserScript>,
    masterEnabled: Boolean,
    onMasterEnabledChange: (Boolean) -> Unit,
    onAddScript: () -> Unit,
    onDownloadScript: () -> Unit,
    onImportScript: () -> Unit,
    onUpdateScripts: () -> Unit,
    onEditScript: (UserScript) -> Unit,
    onRenameScript: (UserScript) -> Unit,
    onDeleteScript: (UserScript) -> Unit,
    onToggleScript: (UserScript, Boolean) -> Unit,
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var menuTarget by remember { mutableStateOf<UserScript?>(null) }

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
                                        onAddScript()
                                    },
                                ),
                                DropdownItem(
                                    text = "下载脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        onDownloadScript()
                                    },
                                ),
                                DropdownItem(
                                    text = "导入脚本",
                                    onClick = {
                                        showMoreMenu = false
                                        onImportScript()
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
                                        onUpdateScripts()
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
                    onCheckedChange = onMasterEnabledChange,
                )
            }
        }

        // ★ 没有脚本时不显示「脚本」标题与卡片
        if (scripts.isNotEmpty()) {
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
                            run {
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
                                        onToggle = { checked -> onToggleScript(script, checked) },
                                        onClick = { onEditScript(script) },
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
                                                onEditScript(target)
                                            }
                                        },
                                    ),
                                    DropdownItem(
                                        text = "重命名",
                                        onClick = {
                                            val target = menuTarget
                                            menuTarget = null
                                            if (target != null) {
                                                onRenameScript(target)
                                            }
                                        },
                                    ),
                                    DropdownItem(
                                        text = "删除",
                                        onClick = {
                                            val target = menuTarget
                                            menuTarget = null
                                            if (target != null) {
                                                onDeleteScript(target)
                                            }
                                        },
                                    ),
                                ),
                            ),
                        ),
                        onDismissRequest = { menuTarget = null },
                    )
                }
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
// 脚本列表页 - Material 3 版
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun ScriptsPageMaterial(
    onBack: () -> Unit,
    scripts: List<UserScript>,
    masterEnabled: Boolean,
    onMasterEnabledChange: (Boolean) -> Unit,
    onAddScript: () -> Unit,
    onDownloadScript: () -> Unit,
    onImportScript: () -> Unit,
    onUpdateScripts: () -> Unit,
    onEditScript: (UserScript) -> Unit,
    onRenameScript: (UserScript) -> Unit,
    onDeleteScript: (UserScript) -> Unit,
    onToggleScript: (UserScript, Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val pageBg = M3MaterialTheme.colorScheme.surface

    M3Scaffold(
        containerColor = pageBg,
        contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
        topBar = {
            M3TopAppBar(
                title = { M3Text("脚本") },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    Box {
                        M3IconButton(onClick = { menuExpanded = true }) {
                            M3Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "更多",
                            )
                        }
                        M3DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            M3DropdownMenuItem(
                                text = { M3Text("添加脚本") },
                                onClick = {
                                    menuExpanded = false
                                    onAddScript()
                                },
                            )
                            M3DropdownMenuItem(
                                text = { M3Text("下载脚本") },
                                onClick = {
                                    menuExpanded = false
                                    onDownloadScript()
                                },
                            )
                            M3DropdownMenuItem(
                                text = { M3Text("导入脚本") },
                                onClick = {
                                    menuExpanded = false
                                    onImportScript()
                                },
                            )
                            M3HorizontalDivider()
                            M3DropdownMenuItem(
                                text = { M3Text("更新脚本") },
                                onClick = {
                                    menuExpanded = false
                                    onUpdateScripts()
                                },
                            )
                        }
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "feature_header") {
                ScriptSectionHeader("功能")
            }
            item(key = "feature_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                ) {
                    MoteSegmentedColumn(
                        content = listOf(
                            {
                                M3SegmentSwitchItem(
                                    title = "启用脚本",
                                    checked = masterEnabled,
                                    onCheckedChange = onMasterEnabledChange,
                                )
                            }
                        )
                    )
                }
            }

            if (scripts.isNotEmpty()) {
                item(key = "script_header") {
                    ScriptSectionHeader("脚本")
                }
                item(key = "script_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                            .padding(bottom = M3_SEGMENT_GROUP_GAP),
                    ) {
                        val items: List<@Composable () -> Unit> = scripts.map { script ->
                            {
                                M3ScriptRow(
                                    script = script,
                                    onToggle = { checked -> onToggleScript(script, checked) },
                                    onClick = { onEditScript(script) },
                                    onEdit = { onEditScript(script) },
                                    onRename = { onRenameScript(script) },
                                    onDelete = { onDeleteScript(script) },
                                )
                            }
                        }
                        MoteSegmentedColumn(content = items)
                    }
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
    }
}

@Composable
private fun M3ScriptRow(
    script: UserScript,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        M3SegmentItem(
            headline = { M3Text(script.name) },
            supporting = {
                M3Text(
                    text = "版本 ${script.version}",
                    style = M3MaterialTheme.typography.bodyMedium,
                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailing = {
                M3Switch(
                    checked = script.enabled,
                    onCheckedChange = onToggle,
                )
            },
            onClick = null,
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true },
            ),
        )
        M3DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = M3MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            M3DropdownMenuItem(
                text = { M3Text("编辑") },
                onClick = {
                    menuExpanded = false
                    onEdit()
                },
            )
            M3DropdownMenuItem(
                text = { M3Text("重命名") },
                onClick = {
                    menuExpanded = false
                    onRename()
                },
            )
            M3DropdownMenuItem(
                text = { M3Text("删除") },
                onClick = {
                    menuExpanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun ScriptSectionHeader(text: String) {
    M3Text(
        text = text,
        color = M3MaterialTheme.colorScheme.primary,
        style = M3MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 32.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
    )
}

// ============================================================
// Material 3 弹窗
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3DownloadScriptDialog(
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit,
) {
    var urlText by remember { mutableStateOf("") }

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("下载脚本") },
        text = {
            M3OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { M3Text("脚本地址") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            M3TextButton(
                onClick = { onDownload(urlText.trim()) },
                enabled = urlText.isNotBlank(),
            ) {
                M3Text("下载")
            }
        },
        dismissButton = {
            M3TextButton(onClick = onDismiss) {
                M3Text("取消")
            }
        },
    )
}

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3RenameScriptDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialName) }

    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("重命名") },
        text = {
            M3OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { M3Text("脚本名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            M3TextButton(onClick = { onConfirm(text.trim()) }) {
                M3Text("确定")
            }
        },
        dismissButton = {
            M3TextButton(onClick = onDismiss) {
                M3Text("取消")
            }
        },
    )
}

@OptIn(M3ExperimentalApi::class)
@Composable
private fun M3DeleteScriptDialog(
    scriptName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    M3AlertDialog(
        onDismissRequest = onDismiss,
        title = { M3Text("删除脚本") },
        text = { M3Text("确定删除「$scriptName」吗？") },
        confirmButton = {
            M3TextButton(onClick = onConfirm) {
                M3Text(
                    text = "删除",
                    color = M3MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            M3TextButton(onClick = onDismiss) {
                M3Text("取消")
            }
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
    when (LocalUiMode.current) {
        UiMode.Miuix -> ScriptEditorPageMiuix(onBack, onSaved)
        UiMode.Material -> ScriptEditorPageMaterial(onBack, onSaved)
    }
}

@Composable
private fun ScriptEditorPageMiuix(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }

    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true))
    }
    var materialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur =
                    prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + 128.dp

    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.8f)))
    }

    val editingId = ScriptsNavArgs.editingId
    val existing = remember(editingId) {
        editingId?.let { id -> UserScriptStore.list(context).firstOrNull { it.id == id } }
    }
    var code by remember {
        mutableStateOf(existing?.code ?: UserScriptStore.DEFAULT_CODE)
    }
    val titleText = if (editingId == null) "添加脚本" else (existing?.name ?: "添加脚本")

    fun save() {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            Toast.makeText(context, "脚本内容不能为空", Toast.LENGTH_SHORT).show()
            return
        }
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

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentTopPadding + 4.dp),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .padding(top = 12.dp, bottom = 12.dp),
                    ) {
                        BasicTextField(
                            value = code,
                            onValueChange = { code = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
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
                    Spacer(
                        Modifier.height(
                            WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding()
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                Modifier.progressiveTextureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = 25f,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = 25f,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            }
                        } else {
                            Modifier.background(surfaceColor)
                        }
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    }
            ) {
                TopAppBar(
                    title = titleText,
                    largeTitle = titleText,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { save() }) {
                            Icon(
                                imageVector = MiuixIcons.Ok,
                                contentDescription = "确定",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(M3ExperimentalApi::class)
@Composable
private fun ScriptEditorPageMaterial(
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
    var name by remember { mutableStateOf(existing?.name ?: "添加脚本") }

    val pageBg = M3MaterialTheme.colorScheme.surface

    M3Scaffold(
        containerColor = pageBg,
        contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
        topBar = {
            M3TopAppBar(
                title = {
                    M3Text(if (editingId == null) "添加脚本" else name)
                },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    M3IconButton(onClick = {
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
                        M3Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = "确定",
                            tint = M3MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(M3MaterialTheme.colorScheme.surfaceContainerLow)
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
                    color = M3MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(M3MaterialTheme.colorScheme.primary),
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