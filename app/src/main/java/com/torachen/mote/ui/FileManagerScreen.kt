package com.torachen.mote.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.torachen.mote.service.FileOpService
import com.torachen.mote.service.FileOpState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.File as FileIcon
import top.yukonga.miuix.kmp.icon.extended.Folder as FolderIcon
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.preference.ArrowPreference
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
    val root: java.io.File = Environment.getExternalStorageDirectory()

    var currentDir by mutableStateOf(root)
    var dirEntries by mutableStateOf<Map<String, List<FsEntry>>>(emptyMap())
    var selected by mutableStateOf<FsEntry?>(null)
    var pendingOp by mutableStateOf<PendingOp?>(null)
    var pickDir by mutableStateOf<java.io.File?>(null)
    var needsAllFiles by mutableStateOf(false)

    fun segmentsOf(dir: java.io.File): List<java.io.File> {
        val list = mutableListOf<java.io.File>()
        var f: java.io.File? = dir
        while (f != null && f.absolutePath.length >= root.absolutePath.length) {
            list.add(0, f)
            f = f.parentFile
        }
        return list
    }

    fun entriesOf(dir: java.io.File): List<FsEntry> = dirEntries[dir.absolutePath].orEmpty()

    suspend fun reload() {
        val dir = currentDir
        val list = withContext(Dispatchers.IO) {
            dir.listFiles()?.map { file ->
                FsEntry(file, file.isDirectory, if (file.isFile) file.length() else 0L, file.lastModified())
            }?.sortedWith(compareByDescending<FsEntry> { it.isDir }.thenBy { it.file.name.lowercase() })
                ?: emptyList()
        }
        dirEntries = dirEntries + (dir.absolutePath to list)
        val path = dir.absolutePath
        needsAllFiles = !hasAllFiles(context) &&
            (path.contains("/Android/data") || path.contains("/Android/obb"))
    }

    fun enter(dir: java.io.File) {
        if (!dir.isDirectory) return
        currentDir = dir
        selected = null
        pickDir = dir
    }

    fun up(): Boolean {
        val parent = currentDir.parentFile ?: return false
        if (parent.absolutePath.length < root.absolutePath.length) return false
        currentDir = parent
        selected = null
        pickDir = parent
        return true
    }

    fun beginMove(op: PendingOp) {
        if (selected == null) return
        pendingOp = op
        pickDir = currentDir
    }

    fun cancelPick() {
        pendingOp = null
        pickDir = null
        selected = null
    }

    fun confirmPick() {
        val op = pendingOp ?: return
        val item = selected ?: return
        val dest = pickDir ?: return
        if (item.isDir && dest.absolutePath.startsWith(item.file.absolutePath)) return
        when (op) {
            PendingOp.COPY -> FileOpService.copy(context, listOf(item.file.absolutePath), dest.absolutePath)
            PendingOp.MOVE -> FileOpService.move(context, listOf(item.file.absolutePath), dest.absolutePath)
        }
        pendingOp = null
        pickDir = null
        selected = null
    }

    fun extract(item: FsEntry) {
        val parent = item.file.parentFile ?: return
        val base = item.file.name.substringBeforeLast('.', item.file.name)
        FileOpService.extract(context, item.file.absolutePath, java.io.File(parent, base).absolutePath)
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true

private fun requestAllFiles(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    try {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
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

    if (mime == "text/html" || name.endsWith(".html") || name.endsWith(".htm")) {
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

private fun fmFormatTime(ms: Long): String {
    if (ms <= 0) return ""
    return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(ms))
}

private fun isArchive(name: String): Boolean {
    val n = name.lowercase()
    return n.endsWith(".zip") || n.endsWith(".tar") || n.endsWith(".tar.gz") ||
        n.endsWith(".tgz") || n.endsWith(".gz") || n.endsWith(".jar") || n.endsWith(".apk")
}

private fun isImage(name: String): Boolean {
    val n = name.lowercase()
    return listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".heic", ".heif", ".avif")
        .any { n.endsWith(it) }
}

private fun loadThumbnail(file: java.io.File): ImageBitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 160 || bounds.outHeight / sample > 160) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        BitmapFactory.decodeFile(file.absolutePath, opts)?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun FileManagerPage(
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
) {
    val context = LocalContext.current
    val controller = remember { FileManagerController(context, onOpenInBrowser, onBack) }

    LaunchedEffect(controller.currentDir) { controller.reload() }

    val running by FileOpState.running.collectAsState()
    val message by FileOpState.message.collectAsState()
    var toast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        toast = msg
        controller.reload()
    }

    BackHandler(enabled = controller.currentDir != controller.root) {
        controller.up()
    }

    val toastText = toast ?: if (running) "正在处理…" else null

    when (LocalUiMode.current) {
        UiMode.Miuix -> FileManagerMiuix(controller, toastText)
        UiMode.Material -> FileManagerMaterial(controller, toastText)
    }
}

/* ---------------- MIUIX ---------------- */

@Composable
private fun FileManagerMiuix(controller: FileManagerController, toast: String?) {
    val scrollBehavior = MiuixScrollBehavior()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (controller.pendingOp != null) "选择目标目录" else "文件管理",
                largeTitle = if (controller.pendingOp != null) "选择目标目录" else "文件管理",
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            controller.pendingOp != null -> controller.cancelPick()
                            !controller.up() -> controller.onBack()
                        }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = controller.currentDir,
                transitionSpec = {
                    val forward = targetState.absolutePath.length > initialState.absolutePath.length
                    val slide = tween<androidx.compose.ui.unit.IntOffset>(
                        durationMillis = 320,
                        easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                    )
                    val fade = tween<Float>(durationMillis = 200)
                    if (forward) {
                        (slideInHorizontally(slide) { it } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { -it / 3 } + fadeOut(fade))
                    } else {
                        (slideInHorizontally(slide) { -it / 3 } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { it } + fadeOut(fade))
                    }
                },
                label = "dir",
            ) { dir ->
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    item {
                        MiuixBreadcrumb(controller, dir)
                        if (controller.needsAllFiles) {
                            BasicComponent(
                                title = "需要「所有文件访问权限」才能浏览 Android/data",
                                summary = "点击去授权",
                                onClick = { requestAllFiles(controller.context) },
                            )
                        }
                    }
                    items(controller.entriesOf(dir)) { entry ->
                        MiuixEntryRow(controller, entry)
                    }
                    item { Spacer(Modifier.height(90.dp)) }
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
                    MiuixPickBar(controller)
                } else if (item != null) {
                    MiuixActionBar(controller, item)
                }
            }

            if (toast != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 96.dp)) {
                    ToastText(toast)
                }
            }
        }
    }
}

@Composable
private fun MiuixBreadcrumb(controller: FileManagerController, dir: java.io.File) {
    val segments = remember(dir) { controller.segmentsOf(dir) }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        BreadcrumbBar(
            items = segments.map {
                BreadcrumbItem(
                    path = it.absolutePath,
                    text = if (it == controller.root) "内部存储" else it.name,
                )
            },
            highlightIndex = segments.lastIndex,
            onItemClick = { index ->
                segments.getOrNull(index)?.let {
                    if (controller.pendingOp != null) controller.pickDir = it else controller.enter(it)
                }
            },
            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiuixEntryRow(controller: FileManagerController, entry: FsEntry) {
    val summary = if (entry.isDir) fmFormatTime(entry.modified)
    else listOfNotNull(
        fmFormatSize(entry.size).takeIf { it.isNotBlank() },
        fmFormatTime(entry.modified),
    ).joinToString(" · ")

    val onClick = {
        when {
            controller.pendingOp != null -> controller.pickDir = entry.file
            entry.isDir -> controller.enter(entry.file)
            else -> controller.open(entry)
        }
    }
    val onLongClick = {
        if (controller.pendingOp == null) {
            controller.selected = if (controller.selected?.file == entry.file) null else entry
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        if (entry.isDir) {
            ArrowPreference(
                title = entry.file.name,
                summary = summary,
                startAction = { EntryThumb(entry) },
                modifier = Modifier.fillMaxWidth(),
                onClick = null,
            )
        } else {
            BasicComponent(
                title = entry.file.name,
                summary = summary,
                startAction = { EntryThumb(entry) },
                modifier = Modifier.fillMaxWidth(),
                onClick = null,
            )
        }
    }
}

@Composable
private fun EntryThumb(entry: FsEntry) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, entry.file.absolutePath) {
        value = if (!entry.isDir && isImage(entry.file.name)) {
            withContext(Dispatchers.IO) { loadThumbnail(entry.file) }
        } else null
    }
    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = if (entry.isDir || isArchive(entry.file.name)) MiuixIcons.FolderIcon
                else MiuixIcons.FileIcon,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(26.dp),
            )
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
        CapsuleAction(MiuixIcons.FileIcon, "打开") { controller.open(item); controller.selected = null }
        CapsuleAction(MiuixIcons.Copy, "复制") { controller.beginMove(PendingOp.COPY) }
        CapsuleAction(MiuixIcons.MoveFile, "移动") { controller.beginMove(PendingOp.MOVE) }
        if (!item.isDir && isArchive(item.file.name)) {
            CapsuleAction(MiuixIcons.FolderIcon, "解压") { controller.extract(item); controller.selected = null }
        }
    }
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
        Text(
            "取消", fontSize = 14.sp, color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.clickable { controller.cancelPick() },
        )
        Text(
            "确定", fontSize = 14.sp, color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.clickable { controller.confirmPick() },
        )
    }
}

/* ---------------- Material 3 ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileManagerMaterial(controller: FileManagerController, toast: String?) {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val topState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topState)

    Column(modifier = Modifier.fillMaxSize().background(M3MaterialTheme.colorScheme.surface)) {
        LargeTopAppBar(
            title = {
                M3Text(
                    if (controller.pendingOp != null) "选择目标目录" else "文件管理",
                    fontWeight = FontWeight.Medium,
                )
            },
            navigationIcon = {
                M3IconButton(onClick = {
                    when {
                        controller.pendingOp != null -> controller.cancelPick()
                        !controller.up() -> controller.onBack()
                    }
                }) {
                    M3Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "返回",
                    )
                }
            },
            scrollBehavior = scrollBehavior,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = controller.currentDir,
                transitionSpec = {
                    val forward = targetState.absolutePath.length > initialState.absolutePath.length
                    val slide = tween<androidx.compose.ui.unit.IntOffset>(
                        durationMillis = 320,
                        easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                    )
                    val fade = tween<Float>(durationMillis = 200)
                    if (forward) {
                        (slideInHorizontally(slide) { it } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { -it / 3 } + fadeOut(fade))
                    } else {
                        (slideInHorizontally(slide) { -it / 3 } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { it } + fadeOut(fade))
                    }
                },
                label = "m3dir",
            ) { dir ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {
                    item {
                        MiuixBreadcrumb(controller, dir)
                        if (controller.needsAllFiles) {
                            M3Surface(
                                color = M3MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                                    .clickable { requestAllFiles(controller.context) },
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    M3Text("需要「所有文件访问权限」才能浏览 Android/data", fontSize = 13.sp)
                                    M3Text(
                                        "点击去授权",
                                        fontSize = 13.sp,
                                        color = M3MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    items(controller.entriesOf(dir)) { entry ->
                        MaterialEntryRow(controller, entry)
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
            }

            val item = controller.selected
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = item != null || controller.pendingOp != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                M3Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                ) {
                    if (controller.pendingOp != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            M3Text(
                                "取消", color = M3MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { controller.cancelPick() },
                            )
                            M3Text(
                                "确定", color = M3MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { controller.confirmPick() },
                            )
                        }
                    } else if (item != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            M3CapsuleAction(Icons.Filled.InsertDriveFile, "打开") {
                                controller.open(item); controller.selected = null
                            }
                            M3CapsuleAction(Icons.Filled.Folder, "复制") { controller.beginMove(PendingOp.COPY) }
                            M3CapsuleAction(Icons.Filled.Folder, "移动") { controller.beginMove(PendingOp.MOVE) }
                            if (!item.isDir && isArchive(item.file.name)) {
                                M3CapsuleAction(Icons.Filled.Folder, "解压") {
                                    controller.extract(item); controller.selected = null
                                }
                            }
                        }
                    }
                }
            }
            }

            if (toast != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 96.dp)) {
                    ToastText(toast)
                }
            }
        }
    }
}

@Composable
private fun MaterialEntryRow(controller: FileManagerController, entry: FsEntry) {
    val summary = if (entry.isDir) fmFormatTime(entry.modified)
    else listOfNotNull(
        fmFormatSize(entry.size).takeIf { it.isNotBlank() },
        fmFormatTime(entry.modified),
    ).joinToString(" · ")

    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable {
                if (controller.pendingOp != null) controller.pickDir = entry.file
                else if (entry.isDir) controller.enter(entry.file)
                else controller.open(entry)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EntryThumb(entry)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            M3Text(entry.file.name, fontSize = 15.sp, maxLines = 1)
            if (summary.isNotBlank()) {
                M3Text(
                    summary,
                    fontSize = 12.sp,
                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        if (entry.isDir) {
            M3Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
