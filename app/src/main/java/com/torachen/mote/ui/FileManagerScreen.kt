package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.torachen.mote.service.FileOpService
import com.torachen.mote.service.FileOpState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File as FileIcon
import top.yukonga.miuix.kmp.icon.extended.Folder as FolderIcon
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class FsEntry(
    val file: java.io.File,
    val isDir: Boolean,
    val size: Long,
    val modified: Long,
)

private enum class PendingOp { COPY, MOVE }

private class FileManagerController(
    val context: Context,
    val onOpenInBrowser: (String) -> Unit,
    val onBack: () -> Unit,
) {
    var currentDir by mutableStateOf(rootDir())
    var entries by mutableStateOf<List<FsEntry>>(emptyList())
    var selected by mutableStateOf<FsEntry?>(null)
    var pendingOp by mutableStateOf<PendingOp?>(null)
    var pickDir by mutableStateOf<java.io.File?>(null)
    var needsAllFiles by mutableStateOf(false)

    fun rootDir(): java.io.File = Environment.getExternalStorageDirectory()

    suspend fun reload() {
        val dir = currentDir
        val list = withContext(Dispatchers.IO) {
            dir.listFiles()?.map { file ->
                FsEntry(file, file.isDirectory, if (file.isFile) file.length() else 0L, file.lastModified())
            }?.sortedWith(compareByDescending<FsEntry> { it.isDir }.thenBy { it.file.name.lowercase() })
                ?: emptyList()
        }
        entries = list
        val path = dir.absolutePath
        needsAllFiles = !hasAllFiles(context) &&
            (path.contains("/Android/data") || path.contains("/Android/obb"))
    }

    fun enter(dir: java.io.File) {
        currentDir = dir
        selected = null
    }

    fun up(): Boolean {
        val parent = currentDir.parentFile ?: return false
        if (!parent.canRead()) return false
        currentDir = parent
        selected = null
        return true
    }

    fun beginMove(op: PendingOp) {
        val item = selected ?: return
        pendingOp = op
        pickDir = currentDir
        selected = item
    }

    fun cancelPick() {
        pendingOp = null
        pickDir = null
    }

    fun confirmPick() {
        val op = pendingOp ?: return
        val item = selected ?: return
        val dest = pickDir ?: return
        val destPath = dest.absolutePath
        if (item.isDir && destPath.startsWith(item.file.absolutePath)) return
        when (op) {
            PendingOp.COPY -> FileOpService.copy(context, listOf(item.file.absolutePath), destPath)
            PendingOp.MOVE -> FileOpService.move(context, listOf(item.file.absolutePath), destPath)
        }
        pendingOp = null
        pickDir = null
        selected = null
    }

    fun extract(item: FsEntry) {
        val parent = item.file.parentFile ?: return
        val name = item.file.name
        val base = name.substringBeforeLast('.', name)
        val dest = java.io.File(parent, base)
        FileOpService.extract(context, item.file.absolutePath, dest.absolutePath)
    }

    fun open(item: FsEntry) {
        if (item.isDir) {
            enter(item.file)
            return
        }
        openFile(context, item.file, onOpenInBrowser)
    }
}

private fun hasAllFiles(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else true

private fun requestAllFiles(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private fun mimeOf(file: java.io.File): String {
    val ext = file.name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
}

private fun openFile(context: Context, file: java.io.File, onOpenInBrowser: (String) -> Unit) {
    val mime = mimeOf(file)
    val name = file.name.lowercase()
    val uri = try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        return
    }

    val isVideo = mime.startsWith("video/") || listOf(
        ".mp4", ".mkv", ".webm", ".mov", ".avi", ".flv", ".m4v", ".3gp", ".ts"
    ).any { name.endsWith(it) }
    if (isVideo) {
        try {
            NativePlayerActivity.start(context, url = uri.toString(), title = file.name, fromWeb = false)
        } catch (_: Exception) {
        }
        return
    }

    val isHtml = mime == "text/html" || name.endsWith(".html") || name.endsWith(".htm")
    if (isHtml) {
        onOpenInBrowser(uri.toString())
        return
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "打开文件").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
    }
}

private fun fmFormatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.0f KB", kb)
    val mb = kb / 1024
    if (mb < 1024) return String.format("%.1f MB", mb)
    return String.format("%.2f GB", mb / 1024)
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return ""
    val d = java.util.Date(ms)
    return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(d)
}

@Composable
fun FileManagerPage(
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
) {
    val context = LocalContext.current
    val controller = remember { FileManagerController(context, onOpenInBrowser, onBack) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller.currentDir) { controller.reload() }

    val running by FileOpState.running.collectAsState()
    val message by FileOpState.message.collectAsState()
    val snackbar = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        snackbar.value = msg
        controller.reload()
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> FileManagerMiuix(controller, scope, snackbar.value, running)
        UiMode.Material -> FileManagerMaterial(controller, scope, snackbar.value, running)
    }
}

/* ---------------- Miuix ---------------- */

@Composable
private fun FileManagerMiuix(
    controller: FileManagerController,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarText: String?,
    running: Boolean,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = if (controller.pendingOp != null) "选择目标目录" else "文件管理",
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(it).padding(top = 4.dp)) {
                BreadcrumbBarMiuix(controller)
                if (controller.needsAllFiles) {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { requestAllFiles(controller.context) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("需要「所有文件访问权限」才能浏览 Android/data", fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("去授权", fontSize = 13.sp, color = MiuixTheme.colorScheme.primary)
                        }
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(controller.entries) { entry ->
                        MiuixFileRow(
                            entry = entry,
                            selected = controller.selected?.file == entry.file ||
                                (controller.pendingOp != null && false),
                            onClick = {
                                if (controller.pendingOp != null) return@MiuixFileRow
                                val ops = controller.selected
                                if (ops != null) {
                                    controller.selected = null
                                } else {
                                    controller.open(entry)
                                }
                            },
                            onLongClick = {
                                if (controller.pendingOp == null) {
                                    controller.selected = if (controller.selected?.file == entry.file) null else entry
                                }
                            },
                        )
                    }
                }
            }
        }

        val item = controller.selected
        val showBar = item != null || controller.pendingOp != null
        AnimatedVisibility(
            visible = showBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp),
        ) {
            if (controller.pendingOp != null) {
                MiuixPickBar(controller)
            } else if (item != null) {
                MiuixActionBar(controller, item)
            }
        }

        if (snackbarText != null || running) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 90.dp),
            ) {
                ToastText(snackbarText ?: "正在处理…")
            }
        }
    }
}

@Composable
private fun BreadcrumbBarMiuix(controller: FileManagerController) {
    val segments = remember(controller.currentDir) {
        val list = mutableListOf<java.io.File>()
        var f: java.io.File? = controller.currentDir
        while (f != null && f.absolutePath.length >= controller.rootDir().absolutePath.length) {
            list.add(0, f)
            f = f.parentFile
        }
        list
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        segments.forEach { dir ->
            val label = if (dir == controller.rootDir()) "内部存储" else dir.name
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .clickable {
                        if (controller.pendingOp != null) controller.pickDir = dir
                        else controller.enter(dir)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(label, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiuixFileRow(
    entry: FsEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) MiuixTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                .background(MiuixTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (entry.isDir) MiuixIcons.FolderIcon else MiuixIcons.FileIcon,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.file.name, fontSize = 15.sp, maxLines = 1)
            val sub = if (entry.isDir) formatTime(entry.modified)
            else listOfNotNull(fmFormatSize(entry.size).takeIf { it.isNotBlank() }, formatTime(entry.modified)).joinToString(" · ")
            if (sub.isNotBlank()) {
                Text(sub, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun MiuixActionBar(controller: FileManagerController, item: FsEntry) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CapsuleAction(MiuixIcons.FolderIcon, "打开") { controller.open(item); controller.selected = null }
        CapsuleAction(MiuixIcons.Copy, "复制") { controller.beginMove(PendingOp.COPY) }
        CapsuleAction(MiuixIcons.MoveFile, "移动") { controller.beginMove(PendingOp.MOVE) }
        if (!item.isDir && isArchive(item.file.name)) {
            CapsuleAction(MiuixIcons.FolderIcon, "解压") { controller.extract(item); controller.selected = null }
        }
    }
}

private fun isArchive(name: String): Boolean {
    val n = name.lowercase()
    return n.endsWith(".zip") || n.endsWith(".tar") || n.endsWith(".tar.gz") ||
        n.endsWith(".tgz") || n.endsWith(".gz") || n.endsWith(".jar") || n.endsWith(".apk")
}

@Composable
private fun CapsuleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun MiuixPickBar(controller: FileManagerController) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("取消", fontSize = 14.sp, color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.clickable { controller.cancelPick() })
        Text("确定", fontSize = 14.sp, color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.clickable { controller.confirmPick() })
    }
}

/* ---------------- Material 3 ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileManagerMaterial(
    controller: FileManagerController,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarText: String?,
    running: Boolean,
) {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(modifier = Modifier.fillMaxSize().background(M3MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            M3TopAppBar(
                title = { M3Text(if (controller.pendingOp != null) "选择目标目录" else "文件管理") },
                navigationIcon = {
                    M3IconButton(onClick = { controller.onBack() }) {
                        M3Icon(MiuixIcons.ChevronBackward, contentDescription = "返回")
                    }
                },
            )
            BreadcrumbBarMaterial(controller)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(controller.entries) { entry ->
                    MaterialFileRow(
                        entry = entry,
                        selected = controller.selected?.file == entry.file,
                        onClick = {
                            if (controller.pendingOp != null) return@MaterialFileRow
                            if (controller.selected != null) controller.selected = null
                            else controller.open(entry)
                        },
                        onLongClick = {
                            if (controller.pendingOp == null) {
                                controller.selected = if (controller.selected?.file == entry.file) null else entry
                            }
                        },
                    )
                }
            }
        }

        val item = controller.selected
        AnimatedVisibility(
            visible = item != null || controller.pendingOp != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp),
        ) {
            if (controller.pendingOp != null) {
                M3Surface(shape = RoundedCornerShape(28.dp), color = M3MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        M3Text("取消", color = M3MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { controller.cancelPick() })
                        M3Text("确定", color = M3MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { controller.confirmPick() })
                    }
                }
            } else if (item != null) {
                M3Surface(shape = RoundedCornerShape(28.dp), color = M3MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        M3CapsuleAction(Icons.Filled.Folder, "打开") { controller.open(item); controller.selected = null }
                        M3CapsuleAction(Icons.Filled.InsertDriveFile, "复制") { controller.beginMove(PendingOp.COPY) }
                        M3CapsuleAction(Icons.Filled.InsertDriveFile, "移动") { controller.beginMove(PendingOp.MOVE) }
                        if (!item.isDir && isArchive(item.file.name)) {
                            M3CapsuleAction(Icons.Filled.InsertDriveFile, "解压") {
                                controller.extract(item); controller.selected = null
                            }
                        }
                    }
                }
            }
        }
        if (snackbarText != null) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 90.dp)) {
                ToastText(snackbarText)
            }
        }
    }
}

@Composable
private fun BreadcrumbBarMaterial(controller: FileManagerController) {
    val segments = remember(controller.currentDir) {
        val list = mutableListOf<java.io.File>()
        var f: java.io.File? = controller.currentDir
        while (f != null && f.absolutePath.length >= controller.rootDir().absolutePath.length) {
            list.add(0, f)
            f = f.parentFile
        }
        list
    }
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        segments.forEach { dir ->
            val label = if (dir == controller.rootDir()) "内部存储" else dir.name
            M3Surface(
                shape = RoundedCornerShape(50),
                color = M3MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.clickable {
                    if (controller.pendingOp != null) controller.pickDir = dir else controller.enter(dir)
                },
            ) { M3Text(label, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialFileRow(
    entry: FsEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) M3MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        M3Icon(
            imageVector = if (entry.isDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            M3Text(entry.file.name, fontSize = 15.sp, maxLines = 1)
            val sub = if (entry.isDir) formatTime(entry.modified)
            else listOfNotNull(fmFormatSize(entry.size).takeIf { it.isNotBlank() }, formatTime(entry.modified)).joinToString(" · ")
            if (sub.isNotBlank()) {
                M3Text(sub, fontSize = 12.sp, color = M3MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun M3CapsuleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        M3Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        M3Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun ToastText(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC2B2B2B))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text, fontSize = 13.sp, color = Color.White)
    }
}
