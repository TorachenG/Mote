package com.torachen.mote.ui

import com.torachen.mote.ui.filemanager.FileManagerHomePage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import android.webkit.MimeTypeMap
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import com.torachen.mote.service.FileOpService
import com.torachen.mote.service.FileOpState
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File as FileIcon
import top.yukonga.miuix.kmp.icon.extended.FolderFill
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

internal data class FsEntry(
    val file: java.io.File,
    val isDir: Boolean,
    val size: Long,
    val modified: Long,
)

internal enum class FileCategory(val label: String, val extensions: Set<String>) {
    DOCUMENT("文档", setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "epub", "csv")),
    IMAGE("图片", setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg")),
    VIDEO("视频", setOf("mp4", "mkv", "avi", "mov", "webm", "flv", "m4v", "3gp")),
    AUDIO("音乐", setOf("mp3", "flac", "wav", "aac", "ogg", "m4a", "ape")),
    ARCHIVE("压缩包", setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")),
    APK("安装包", setOf("apk", "apks", "xapk")),
}

internal enum class PendingOp { COPY, MOVE }

private const val FM_BLUR_RADIUS = 25f
private const val FM_BLEND_ALPHA = 0.8f
private val FM_FALLBACK_HEIGHT = 128.dp

internal class FileManagerController(
    val context: Context,
    val onOpenInBrowser: (String) -> Unit,
    val onBack: () -> Unit,
    val onOpenDir: (path: String?, category: String?, title: String?, fromShortcut: Boolean) -> Unit = { _, _, _, _ -> },
) {
    val root: java.io.File = Environment.getExternalStorageDirectory()

    var currentDir by mutableStateOf(root)
    var dirEntries by mutableStateOf<Map<String, List<FsEntry>>>(emptyMap())
    var selected by mutableStateOf<List<FsEntry>>(emptyList())
    /** 多选模式独立状态：清空选择后仍保持多选 */
    var selectionMode by mutableStateOf(false)
    var pendingOp by mutableStateOf<PendingOp?>(null)
    var pickDir by mutableStateOf<java.io.File?>(null)
    var needsAllFiles by mutableStateOf(false)
    /** 当前目录存在但无法读取（典型：其它应用的 Android/data） */
    var dirUnreadable by mutableStateOf(false)
    /** SAF 浏览模式：java.io 不可读时改用 DocumentsContract 列举 */
    var safMode by mutableStateOf(false)
    var safPath by mutableStateOf("")
    var safEntries by mutableStateOf<List<FmSafStore.Entry>>(emptyList())
    var safFailed by mutableStateOf(false)
    /** 由页面注入：请求 SAF 目录授权 */
    var onRequestSafGrant: (() -> Unit)? = null

    /** 是否显示主页（与目录列表互斥） */
    var showHome by mutableStateOf(true)

    /** 独立路由页模式：返回根目录的上一级应退出页面，而不是切回主页 */
    var routeMode by mutableStateOf(false)

    /** 当前是否在回收站目录 */
    val isInRecycle: Boolean
        get() = currentDir.absolutePath == recycleRoot.absolutePath ||
            currentDir.absolutePath.startsWith(recycleRoot.absolutePath + "/")

    /** 从快捷方式直接进入时：未点过目录轴前，返回直接退出页面 */
    var upNavEnabled by mutableStateOf(false)

    /** 标题覆盖（点过目录轴后改为「手机存储目录」） */
    var titleOverride by mutableStateOf<String?>(null)

    fun enterList(dir: java.io.File) {
        showHome = false
        category = null
        selected = emptyList()
        currentDir = dir
    }

    fun goHome() {
        showHome = true
        category = null
        selected = emptyList()
    }

    // 分类浏览
    var category by mutableStateOf<FileCategory?>(null)
    var categoryFiles by mutableStateOf<List<FsEntry>>(emptyList())
    var categoryLoading by mutableStateOf(false)
    /** 目录切换/重新扫描期间为 true，界面先不渲染旧列表，避免闪回顶部 */
    var reloading by mutableStateOf(false)
    /** 每个目录的滚动位置记忆（firstVisibleIndex to offset） */
    val scrollMemory = mutableMapOf<String, Pair<Int, Int>>()
    /** 每个目录独立的 LazyListState，切回时天然保留滚动位置，无需异步恢复 */
    private val listStateMap = mutableMapOf<String, androidx.compose.foundation.lazy.LazyListState>()

    fun listStateFor(path: String): androidx.compose.foundation.lazy.LazyListState =
        listStateMap.getOrPut(path) { androidx.compose.foundation.lazy.LazyListState() }
    private val scanScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )

    val selecting: Boolean get() = selectionMode

    fun enterSelection(entry: FsEntry) {
        selectionMode = true
        if (!isSelected(entry)) selected = selected + entry
    }

    var paintStartIndex = -1
    private var paintTarget = false
    private var paintRowHeightPx = 0f

    fun beginPaint(index: Int, rowHeightPx: Float) {
        val list = entriesOf(currentDir)
        val entry = list.getOrNull(index) ?: return
        paintStartIndex = index
        paintRowHeightPx = rowHeightPx.coerceAtLeast(1f)
        // 起始行已在 enterSelection 中选中，后续按“选中”状态涂抹，不要把起始行再反选
        paintTarget = isSelected(entry)
    }

    fun paintBy(offsetPx: Float) {
        if (paintStartIndex < 0) return
        val list = entriesOf(currentDir)
        val index = (paintStartIndex + (offsetPx / paintRowHeightPx).roundToInt())
            .coerceIn(0, (list.size - 1).coerceAtLeast(0))
        applyPaint(index)
    }

    fun endPaint() {
        paintStartIndex = -1
    }

    private fun applyPaint(index: Int) {
        val entry = entriesOf(currentDir).getOrNull(index) ?: return
        if (isSelected(entry) != paintTarget) toggle(entry)
    }

    fun exitSelection() {
        selectionMode = false
        selected = emptyList()
    }

    fun openCategory(target: FileCategory) {
        category = target
        categoryLoading = true
        categoryFiles = emptyList()
        scanScope.launch {
            scanCategory(target) { partial ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main.immediate) {
                    categoryFiles = partial
                }
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                categoryLoading = false
            }
        }
    }

    fun closeCategory() {
        category = null
    }

    /** 返回键/返回按钮统一处理：返回 true 表示已消费 */
    fun enterSaf(absolutePath: String) {
        safMode = true
        safPath = absolutePath
            .removePrefix(root.absolutePath)
            .trim('/')
        loadSaf()
    }

    fun loadSaf() {
        scanScope.launch {
            val list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FmSafStore.list(context, safPath)
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                safEntries = list.orEmpty()
                safFailed = list == null
            }
        }
    }

    fun safOpen(entry: FmSafStore.Entry) {
        if (entry.isDir) {
            safPath = entry.docId.removePrefix("primary:").trim('/')
            loadSaf()
        } else {
            if (!FmSafStore.open(context, entry.docId)) {
                android.widget.Toast.makeText(context, "无法打开该文件", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun safUp(): Boolean {
        if (safPath.isEmpty()) return false
        safPath = safPath.substringBeforeLast('/', "")
        loadSaf()
        return true
    }

    fun onBackPressed(): Boolean {
        if (safMode) {
            if (safUp()) return true
            safMode = false
            return true
        }
        if (selectionMode) {
            exitSelection()
            return true
        }
        if (category != null) {
            if (routeMode) onBack() else closeCategory()
            return true
        }
        if (isInRecycle && routeMode) {
            onBack()
            return true
        }
        if (currentDir != root) {
            up()
            return true
        }
        if (!showHome) {
            if (routeMode) {
                onBack()
            } else {
                goHome()
            }
            return true
        }
        return false
    }

    private suspend fun scanCategory(
        target: FileCategory,
        onPartial: suspend (List<FsEntry>) -> Unit,
    ): List<FsEntry> {
        val roots = listOf(
            root,
            java.io.File(root, "Download"),
            java.io.File(root, "Documents"),
            java.io.File(root, "DCIM"),
            java.io.File(root, "Pictures"),
            java.io.File(root, "Movies"),
            java.io.File(root, "Music"),
        )
        val out = mutableListOf<FsEntry>()
        val seen = mutableSetOf<String>()
        fun walk(dir: java.io.File, depth: Int) {
            if (depth > 6 || out.size >= 600) return
            val children = dir.listFiles() ?: return
            for (child in children) {
                if (out.size >= 600) return
                if (child.name.startsWith(".")) continue
                if (child.isDirectory) {
                    if (child.name != "Android") walk(child, depth + 1)
                } else if (child.extension.lowercase() in target.extensions) {
                    if (seen.add(child.absolutePath)) {
                        out.add(FsEntry(child, false, child.length(), child.lastModified()))
                    }
                }
            }
        }
        roots.filter { it.isDirectory }.forEach { rootDir ->
            walk(rootDir, 0)
            if (out.isNotEmpty()) onPartial(out.sortedByDescending { it.modified })
        }
        return out.sortedByDescending { it.modified }
    }

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
        // 已有缓存时不再遮盖，避免切回目录瞬间出现“闪白/闪内容”
        reloading = entriesOf(currentDir).isEmpty()
        val dir = currentDir
        val list = withContext(Dispatchers.IO) {
            dir.listFiles()?.map { file ->
                FsEntry(file, file.isDirectory, if (file.isFile) file.length() else 0L, file.lastModified())
            }?.sortedWith(
                compareByDescending<FsEntry> { it.isDir }
                    .thenBy { entry ->
                        val name = entry.file.name
                        when {
                            name.startsWith(".") -> 0
                            name.firstOrNull()?.let { it.code in 0x4E00..0x9FFF } == true -> 1
                            else -> 2
                        }
                    }
                    .thenBy { it.file.name.lowercase() }
            )
                ?: emptyList()
        }
        dirUnreadable = dir.exists() && dir.listFiles() == null
        if (dirUnreadable) {
            enterSaf(dir.absolutePath)
        } else {
            safMode = false
        }
        dirEntries = dirEntries + (dir.absolutePath to list)
        val path = dir.absolutePath
        needsAllFiles = !hasAllFiles(context)
        reloading = false
    }

    fun enter(dir: java.io.File) {
        if (!dir.isDirectory) return
        currentDir = dir
        selected = emptyList()
        pickDir = dir
    }

    fun up(): Boolean {
        val parent = currentDir.parentFile ?: return false
        if (parent.absolutePath.length < root.absolutePath.length) return false
        currentDir = parent
        selected = emptyList()
        pickDir = parent
        return true
    }

    fun allEntries(): List<FsEntry> = entriesOf(currentDir)

    fun allSelected(): Boolean {
        val all = allEntries()
        return all.isNotEmpty() && selected.size >= all.size
    }

    fun toggleSelectAll() {
        val all = allEntries()
        selected = if (allSelected()) emptyList() else all
    }

    fun toggle(entry: FsEntry) {
        val existing = selected.firstOrNull { it.file.absolutePath == entry.file.absolutePath }
        selected = if (existing != null) selected - existing else selected + entry
    }

    fun isSelected(entry: FsEntry): Boolean =
        selected.any { it.file.absolutePath == entry.file.absolutePath }

    fun beginMove(op: PendingOp) {
        if (selected.isEmpty()) return
        pendingOp = op
        pickDir = currentDir
    }

    fun cancelPick() {
        pendingOp = null
        pickDir = null
        selected = emptyList()
    }

    fun confirmPick() {
        val op = pendingOp ?: return
        val items = selected
        val dest = pickDir ?: return
        if (items.isEmpty()) return
        if (items.any { it.isDir && dest.absolutePath.startsWith(it.file.absolutePath) }) return
        val paths = items.map { it.file.absolutePath }
        when (op) {
            PendingOp.COPY -> FileOpService.copy(context, paths, dest.absolutePath)
            PendingOp.MOVE -> FileOpService.move(context, paths, dest.absolutePath)
        }
        pendingOp = null
        pickDir = null
        selected = emptyList()
    }

    fun compressSelected(): java.io.File? {
        val items = selected
        if (items.isEmpty()) return null
        val baseDir = items.first().file.parentFile ?: currentDir
        val name = if (items.size == 1) items.first().file.name + ".zip" else "压缩包.zip"
        val zip = java.io.File(baseDir, name)
        FileOpService.compress(context, items.map { it.file.absolutePath }, zip.absolutePath)
        selected = emptyList()
        return zip
    }

    val recycleRoot: java.io.File get() = java.io.File(context.filesDir, "recycle")

    fun deleteSelected() {
        val items = selected
        if (items.isEmpty()) return
        val bucket = java.io.File(recycleRoot, System.currentTimeMillis().toString())
        bucket.mkdirs()
        val moved = items.count { item ->
            runCatching { item.file.renameTo(java.io.File(bucket, item.file.name)) }.getOrDefault(false)
        }
        selected = emptyList()
        FileOpState.message.value = "已移入回收站（保留 7 天）：$moved 项"
    }

    fun purgeRecycleBin() {
        scanScope.launch(Dispatchers.IO) {
            val expire = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            recycleRoot.listFiles()?.forEach { bucket ->
                if (!bucket.isDirectory) return@forEach
                // 每个文件单独按修改时间计算 30 天
                bucket.walkTopDown().filter { it.isFile }.forEach { file ->
                    if (file.lastModified() < expire) {
                        runCatching { file.delete() }
                    }
                }
                // 清理空目录
                bucket.walkBottomUp().filter { it.isDirectory && it != bucket }.forEach { dir ->
                    if (dir.listFiles()?.isEmpty() != false) runCatching { dir.delete() }
                }
                if (bucket.listFiles()?.isEmpty() != false) runCatching { bucket.delete() }
            }
        }
    }

    fun openRecycleBin() {
        recycleRoot.mkdirs()
        currentDir = recycleRoot
        selected = emptyList()
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

internal fun requestAllFiles(context: Context) {
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
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "无法读取文件：${e.message ?: "路径不受支持"}", android.widget.Toast.LENGTH_SHORT).show()
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
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "没有可以打开该文件的应用", android.widget.Toast.LENGTH_SHORT).show()
    }
}

internal fun fmFormatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.0f KB", kb)
    val mb = kb / 1024
    if (mb < 1024) return String.format("%.1f MB", mb)
    return String.format("%.2f GB", mb / 1024)
}

internal fun fmFormatTime(ms: Long): String {
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

/** 主页 → 目录页的跳转参数（路由用） */
internal object FileManagerDirRequest {
    var path: String? = null
    var category: String? = null
    var title: String? = null
    /** 每次进入递增，确保路由页面重新创建状态 */
    var token: Long = 0L
    /** 来自主页快捷方式（应用卡 / 分类 / 手机存储目录）：返回直接退出页面 */
    var fromShortcut: Boolean = false
}

@Composable
internal fun FileManagerPage(
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onSelectionModeChange: (Boolean) -> Unit = {},
    controller: FileManagerController? = null,
    onOpenScanner: () -> Unit = {},
    onSubPageChange: (Boolean) -> Unit = {},
    pageTitle: String = "文件管理",
) {
    val context = LocalContext.current
    val realController = controller ?: remember { FileManagerController(context, onOpenInBrowser, onBack) }
    val controller = realController
    val safLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            FmSafStore.treeUri = uri
            controller.loadSaf()
        }
    }
    androidx.compose.runtime.SideEffect {
        controller.onRequestSafGrant = {
            val docId = "primary:" + controller.safPath
            runCatching {
                safLauncher.launch(
                    android.provider.DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        docId,
                    )
                )
            }
        }
    }

    LaunchedEffect(controller.currentDir) { controller.reload() }
    LaunchedEffect(Unit) { controller.purgeRecycleBin() }
    LaunchedEffect(controller.currentDir, controller.category) {
        onSubPageChange(controller.currentDir != controller.root || controller.category != null)
    }
    LaunchedEffect(controller.selecting) { onSelectionModeChange(controller.selecting) }

    // 从系统「所有文件访问权限」页返回时，应用不会重建，需要在 ON_RESUME 重新检查并刷新
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = hasAllFiles(context)
                if (granted == controller.needsAllFiles) {
                    controller.needsAllFiles = !granted
                }
                if (granted) {
                    permissionScope.launch { controller.reload() }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val message by FileOpState.message.collectAsState()
    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        FileOpState.message.value = null
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        controller.reload()
    }

    // 快捷方式进入且未点过目录轴：不拦截返回，直接由路由出栈（含预见式返回）
    val shortcutNoUp = controller.routeMode && !controller.upNavEnabled
    BackHandler(
        enabled = controller.selecting ||
            (controller.category == null &&
                !controller.isInRecycle &&
                controller.currentDir != controller.root &&
                !shortcutNoUp)
    ) {
        controller.onBackPressed()
    }

    var infoItems by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shareItems by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var showZipShareDialog by remember { mutableStateOf(false) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmText by remember { mutableStateOf("") }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val uiScope = rememberCoroutineScope()

    val requestCompress: () -> Unit = {
        confirmTitle = "压缩"
        confirmText = "确定将选中的 ${controller.selected.size} 项压缩为 zip？"
        confirmAction = { controller.compressSelected() }
    }
    val requestExtract: (FsEntry) -> Unit = { one ->
        confirmTitle = "解压"
        confirmText = "确定解压「${one.file.name}」到当前目录？"
        confirmAction = { controller.extract(one); controller.exitSelection() }
    }
    val requestDelete: () -> Unit = {
        if (controller.selected.isNotEmpty()) showDeleteDialog = true
    }

    fun doShare(items: List<FsEntry>) {
        if (items.isEmpty()) return
        uiScope.launch {
            Toast.makeText(context, "正在压缩…", Toast.LENGTH_SHORT).show()
            val zip = withContext(Dispatchers.IO) { zipForShare(context, items) }
            if (zip == null) {
                Toast.makeText(context, "压缩失败", Toast.LENGTH_SHORT).show()
                return@launch
            }
            shareFile(context, zip)
            controller.exitSelection()
            delay(60000)
            withContext(Dispatchers.IO) { zip.delete() }
        }
    }

    val onTool: (FmTool) -> Unit = { tool ->
        // 其余功能先不下发任何行为，仅保留回收站
        if (tool == FmTool.TRASH) controller.openRecycleBin()
    }

    AnimatedContent(
        targetState = controller.showHome,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(300, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))) { -it / 3 } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(260)) { it } + fadeOut(tween(160)))
            } else {
                (slideInHorizontally(tween(300, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))) { it } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(260)) { -it / 3 } + fadeOut(tween(160)))
            }
        },
        label = "home",
    ) { home ->
        if (home) {
            FileManagerHomePage(controller, onOpenScanner)
        } else {
    when (LocalUiMode.current) {
        UiMode.Miuix -> FileManagerMiuix(
            controller = controller,
            onAbout = { items -> infoItems = items; showInfoDialog = true },
            onShare = { items -> shareItems = items; if (items.any { it.isDir }) showZipShareDialog = true else doShare(items) },
            onDelete = requestDelete,
            onCompress = requestCompress,
            onExtract = requestExtract,
            onTool = onTool,
            pageTitle = pageTitle,
        )
        UiMode.Material -> FileManagerMaterial(
            controller = controller,
            onAbout = { items -> infoItems = items; showInfoDialog = true },
            onShare = { items -> shareItems = items; if (items.any { it.isDir }) showZipShareDialog = true else doShare(items) },
            onDelete = requestDelete,
            onCompress = requestCompress,
            onExtract = requestExtract,
            onTool = onTool,
            pageTitle = pageTitle,
        )
    }
        }
    }

    WindowDialog(show = showDeleteDialog, title = "删除", onDismissRequest = { showDeleteDialog = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "确定删除 ${controller.selected.size} 个文件？删除后无法找回。",
                    style = MiuixTheme.textStyles.body1,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = MiuixButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) { Text("取消") }
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            controller.deleteSelected()
                        },
                        modifier = Modifier.weight(1f),
                        colors = MiuixButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = Color(0xFFE5484D),
                        ),
                    ) { Text("删除", color = Color(0xFFE5484D)) }
                }
            }
        }



    WindowDialog(show = showZipShareDialog, title = "分享", onDismissRequest = { showZipShareDialog = false }) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "选中的内容包含文件夹，需要先压缩成 zip 才能分享。",
                style = MiuixTheme.textStyles.body1,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showZipShareDialog = false },
                    modifier = Modifier.weight(1f),
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        showZipShareDialog = false
                        doShare(shareItems)
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("压缩并分享") }
            }
        }
    }

    if (infoItems.isNotEmpty()) {
        FileInfoDialog(
            items = infoItems,
            show = showInfoDialog,
            onJump = { target ->
                controller.enter(target)
                controller.exitSelection()
                showInfoDialog = false
            },
            onDismiss = { showInfoDialog = false },
        )
    }
}

/* ---------------- MIUIX ---------------- */

@Composable
private fun FileManagerMiuix(
    controller: FileManagerController,
    onAbout: (List<FsEntry>) -> Unit,
    onShare: (List<FsEntry>) -> Unit,
    onDelete: () -> Unit,
    onCompress: () -> Unit,
    onExtract: (FsEntry) -> Unit,
    onTool: (FmTool) -> Unit,
    pageTitle: String,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val density = LocalDensity.current
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = controller.listStateFor(controller.currentDir.absolutePath)
    var headerHeightDp by remember { mutableStateOf(0.dp) }
    val pullUi = remember { PullRefreshUiState() }
    var pendingScroll by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { controller.scrollMemory[controller.currentDir.absolutePath] = it }
    }
    LaunchedEffect(controller.currentDir) {
        pendingScroll = controller.scrollMemory[controller.currentDir.absolutePath]
    }
    LaunchedEffect(controller.dirEntries, pendingScroll) {
        val target = pendingScroll ?: return@LaunchedEffect
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) {
            listState.scrollToItem(target.first.coerceIn(0, count - 1), target.second)
            pendingScroll = null
        }
    }
    val surface = MiuixTheme.colorScheme.surface

    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surface)
            drawContent()
        }
    } else null

    val prefs = remember {
        controller.context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true))
    }
    var materialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0 &&
                !listState.isScrollInProgress
        }
    }
    val themeIsDark = surface.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "fmBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        // 文件管理页返回按钮材质更透明：直接调材质混合透明度
        buildBackCardMaterialModifier(
            materialStyle, blurSupported, backdrop, themeIsDark, blendAlpha = 0.55f,
        )
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surface) {
        listOf(BlendColorEntry(color = surface.copy(alpha = FM_BLEND_ALPHA)))
    }

    var barBottomPx by remember { mutableIntStateOf(0) }
    val topPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp
    val hasBreadcrumbRow = !controller.safMode && controller.category == null && !controller.isInRecycle

    // 退出多选时用上一次的选择渲染，保证胶囊栏有完整的退出动画
    var barItems by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var barPending by remember { mutableStateOf<PendingOp?>(null) }
    LaunchedEffect(controller.selected) {
        if (controller.selected.isNotEmpty()) barItems = controller.selected
    }
    LaunchedEffect(controller.pendingOp) {
        if (controller.pendingOp != null) barPending = controller.pendingOp
        else if (!controller.selecting) barPending = null
    }

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            if (controller.category != null) {
                CategoryListMiuix(controller, topPadding)
            } else {
            AnimatedContent(
                targetState = controller.currentDir,
                modifier = Modifier.fillMaxSize(),
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
                PullRefreshContainer(
                    canPull = {
                        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    },
                    onRefresh = { controller.reload() },
                    indicatorTopPadding = 52.dp,
                    uiState = pullUi,
                ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .graphicsLayer { alpha = 1f }
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = if (hasBreadcrumbRow) topPadding + 44.dp else topPadding, bottom = navBottom + 96.dp),
                    overscrollEffect = null,
                ) {
                    if (controller.safMode) {
                        item(key = "saf_header") { MiuixBreadcrumbSaf(controller) }
                        if (controller.safFailed) {
                            item(key = "saf_failed") {
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                        Text("无法读取该目录", fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "系统未授予该目录的访问权限。可先授予「所有文件访问权限」，或点击下方按钮选择该目录授权。",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        top.yukonga.miuix.kmp.basic.Button(
                                            onClick = { controller.onRequestSafGrant?.invoke() },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("选择目录授权")
                                        }
                                    }
                                }
                            }
                        }
                        items(controller.safEntries) { entry ->
                            SafEntryRow(entry) { controller.safOpen(entry) }
                        }
                    }
                    if (controller.dirUnreadable && !controller.safMode) {
                        item(key = "unreadable_hint") {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(
                                        "受系统限制，Mote 无法读取该目录",
                                        fontSize = 15.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Android 11 起，普通应用不能访问其它应用的 Android/data 目录，可在系统「文件管理」中查看。",
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                    }
                    if (controller.needsAllFiles) {
                        item {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "受系统限制，Mote 需要「所有文件访问权限」才能显示全部文件",
                                    onClick = { requestAllFiles(controller.context) },
                                )
                            }
                        }
                    }
                    if (!controller.safMode) {
                        items(controller.entriesOf(dir)) { entry ->
                            MiuixEntryRow(controller, entry)
                        }
                    }
                }
                }
            }
            }
        }


        if (controller.currentDir == controller.root && (pullUi.offset > 0f || pullUi.refreshing)) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val triggerPx = with(density) { 74.dp.toPx() }
            val progress = if (pullUi.refreshing) 1f else (pullUi.offset / triggerPx).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topPadding + 2.dp)
                    .size(22.dp)
                    .graphicsLayer {
                        val sc = progress * 1.6f
                        scaleX = sc
                        scaleY = sc
                        translationY = pullUi.offset * 0.35f
                    },
            ) {
                top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                    color = if (progress >= 1f) MiuixTheme.colorScheme.primary else Color(0xFF666666),
                    size = 22.dp,
                    strokeWidth = 2.5.dp,
                )
            }
        }

        // 模糊层比标题栏更高，覆盖到目录轴下方，避免内容从目录轴处直接透出
        // 整段标题栏满强度模糊，渐变带收在标题栏底部之内，避免糊到第一条选项
        val fadeBand = 24.dp
        val blurTotalHeight = if (hasBreadcrumbRow) topPadding + 44.dp else topPadding
        val headerFraction = ((blurTotalHeight - fadeBand) / blurTotalHeight).coerceIn(0f, 0.95f)
        if (blurSupported && backdrop != null && materialStyle != 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(blurTotalHeight)
                    .align(Alignment.TopCenter)
                    .then(
                        if (progressiveTitleBlur) {
                            Modifier.progressiveTextureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = FM_BLUR_RADIUS,
                                gradient = ProgressiveBlur(
                                    angle = 90f,
                                    startFraction = headerFraction,
                                    endFraction = 1f,
                                ),
                                colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                            )
                        } else {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = FM_BLUR_RADIUS,
                                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                            )
                        }
                    ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .then(if (blurSupported && backdrop != null && materialStyle != 0) Modifier else Modifier.background(surface))
                .statusBarsPadding()
                .onGloballyPositioned { coords ->
                    val bottom = coords.boundsInWindow().bottom.toInt()
                    if (bottom != barBottomPx) barBottomPx = bottom
                },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = if (controller.pendingOp != null) "选择目标目录" else (controller.titleOverride ?: pageTitle),
                largeTitle = if (controller.pendingOp != null) "选择目标目录" else (controller.titleOverride ?: pageTitle),
                color = Color.Transparent,
                defaultWindowInsetsPadding = false,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    when {
                        controller.pendingOp != null -> IconButton(onClick = { controller.cancelPick() }) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        controller.selecting -> SettingsTopBarAction(
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            onClick = { controller.exitSelection() },
                            contentDescription = "退出多选",
                            icon = MiuixIcons.ChevronBackward,
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                        else -> SettingsBackButton(
                            onBack = {
                                if (!controller.onBackPressed()) controller.onBack()
                            },
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    if (controller.selecting) {
                        val allSelected = controller.allSelected()
                        SettingsTopBarAction(
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            onClick = { controller.toggleSelectAll() },
                            contentDescription = "全选",
                            icon = MiuixIcons.SelectAll,
                            tint = if (allSelected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
            }
        }

        // 目录轴固定在标题栏下方（不参与标题栏高度测量）
        if (hasBreadcrumbRow) Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = topPadding)
                .height(44.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            MiuixBreadcrumb(controller, controller.currentDir, 26.dp, controller.category)
        }

        val liveBarItems = if (controller.selected.isNotEmpty()) controller.selected else barItems
        AnimatedVisibility(
            visible = controller.selecting || controller.pendingOp != null || controller.selected.isNotEmpty(),
            enter = slideInVertically(
                animationSpec = tween(280, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
                initialOffsetY = { it },
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(200, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)),
                targetOffsetY = { it },
            ) + fadeOut(tween(140)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp),
        ) {
            if (barPending != null) {
                MiuixPickBar(controller)
            } else if (liveBarItems.isNotEmpty()) {
                MiuixActionBar(
                    items = liveBarItems,
                    onAbout = { onAbout(liveBarItems) },
                    onShare = { onShare(liveBarItems) },
                    onOpen = { one -> controller.open(one); controller.exitSelection() },
                    onCopy = { controller.beginMove(PendingOp.COPY) },
                    onMove = { controller.beginMove(PendingOp.MOVE) },
                    onExtract = onExtract,
                    onCompress = onCompress,
                    onDelete = onDelete,
                )
            }
        }

    }
}

@Composable
private fun MiuixBreadcrumb(
    controller: FileManagerController,
    dir: java.io.File,
    startPadding: androidx.compose.ui.unit.Dp,
    category: FileCategory? = null,
    onNavigateUp: () -> Unit = {},
) {
    val inRecycle = dir.absolutePath.startsWith(controller.recycleRoot.absolutePath)
    val segments = remember(dir) {
        if (inRecycle) listOf(controller.recycleRoot) else controller.segmentsOf(dir)
    }
    val items = if (category != null) {
        listOf(
            BreadcrumbItem(path = controller.root.absolutePath, text = "内部存储"),
            BreadcrumbItem(path = "category:${category.name}", text = category.label),
        )
    } else {
        segments.map {
            BreadcrumbItem(
                path = it.absolutePath,
                text = if (it == controller.root) "内部存储" else it.name,
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        BreadcrumbBar(
            items = items,
            highlightIndex = items.lastIndex,
            onItemClick = { index ->
                if (index < items.lastIndex) onNavigateUp()
                if (category != null) {
                    controller.closeCategory()
                } else {
                    segments.getOrNull(index)?.let {
                        if (controller.pendingOp != null) controller.pickDir = it else controller.enter(it)
                    }
                }
            },
            insideMargin = PaddingValues(start = startPadding, end = 16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PullIndicatorItem(state: PullRefreshUiState) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val dp = with(density) { state.offset.toDp() }
    val triggerPx = with(density) { 74.dp.toPx() }
    val progress = if (state.refreshing) 1f else (state.offset / triggerPx).coerceIn(0f, 1f)
    if (dp > 0.dp || state.refreshing) {
        Box(
            modifier = Modifier.fillMaxWidth().height(dp.coerceAtLeast(if (state.refreshing) 36.dp else 0.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        val s = progress * 1.6f
                        scaleX = s
                        scaleY = s
                    },
            ) {
                top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                    color = if (progress >= 1f) MiuixTheme.colorScheme.primary else Color(0xFF666666),
                    size = 22.dp,
                    strokeWidth = 2.5.dp,
                )
            }
        }
    } else {
        Spacer(Modifier.height(0.dp))
    }
}

/** 按扩展名返回与主页一致的图标与配色；null 表示使用通用文件图标 */
internal fun fmTypeIcon(file: java.io.File): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color>? {
    val ext = file.extension.lowercase()
    return when {
        ext in FileCategory.ARCHIVE.extensions ->
            androidx.compose.material.icons.Icons.Rounded.FolderZip to Color(0xFF3B6EF6)
        ext in FileCategory.DOCUMENT.extensions ->
            androidx.compose.material.icons.Icons.Rounded.Description to Color(0xFFFFD966)
        ext in FileCategory.IMAGE.extensions ->
            androidx.compose.material.icons.Icons.Rounded.Image to Color(0xFF7767F9)
        ext in FileCategory.VIDEO.extensions ->
            androidx.compose.material.icons.Icons.Rounded.Movie to Color(0xFF7767F9)
        ext in FileCategory.AUDIO.extensions ->
            androidx.compose.material.icons.Icons.Rounded.MusicNote to Color(0xFFE8452C)
        ext in FileCategory.APK.extensions ->
            androidx.compose.material.icons.Icons.Rounded.Android to Color(0xFF1DCD3A)
        else -> null
    }
}

/** 多选态下从右侧 checkbox 区域拖动：沿路切换选中；其它区域手势不拦截，列表可正常滑动 */
private fun Modifier.dragSelectModifier(
    controller: FileManagerController,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
): Modifier = this.pointerInput(controller.selecting) {
    if (!controller.selecting) return@pointerInput
    val slop = 10.dp.toPx()
    val checkboxZone = 72.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val startY = down.position.y
        val fromCheckbox = down.position.x > size.width - checkboxZone

        fun rowAt(y: Float): FsEntry? {
            val info = listState.layoutInfo
            val entryCount = controller.entriesOf(controller.currentDir).size
            if (entryCount <= 0) return null
            val startIndex = info.totalItemsCount - entryCount
            val item = info.visibleItemsInfo.firstOrNull { y >= it.offset && y <= it.offset + it.size } ?: return null
            return controller.entriesOf(controller.currentDir).getOrNull(item.index - startIndex)
        }

        fun applyAt(y: Float, target: Boolean) {
            val entry = rowAt(y) ?: return
            if (controller.isSelected(entry) != target) controller.toggle(entry)
        }

        var painting = false
        var target = false
        var lastY = startY
        var autoScroll = 0f
        var autoJob: kotlinx.coroutines.Job? = null

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            lastY = change.position.y

            if (!painting && fromCheckbox && kotlin.math.abs(lastY - startY) > slop) {
                painting = true
                target = rowAt(startY)?.let { !controller.isSelected(it) } ?: true
                applyAt(startY, target)
            }

            if (painting) {
                change.consume()
                applyAt(lastY, target)
                val info = listState.layoutInfo
                val top = info.viewportStartOffset + 140f
                val bottom = info.viewportEndOffset - 160f
                autoScroll = when {
                    lastY < top -> -16f
                    lastY > bottom -> 16f
                    else -> 0f
                }
                if (autoScroll != 0f && autoJob == null) {
                    autoJob = scope.launch {
                        while (autoScroll != 0f) {
                            listState.scrollBy(autoScroll)
                            applyAt(lastY, target)
                            kotlinx.coroutines.delay(16)
                        }
                    }
                }
            }
        }
        autoScroll = 0f
        autoJob?.cancel()
    }
}

/** 文件管理目录页（作为应用内路由，带进入/退出动画与预测性返回） */
@Composable
internal fun FileManagerDirPage(
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    initialPath: String? = null,
    initialCategory: String? = null,
    pageTitle: String = "文件管理",
    requestToken: Long = 0L,
    fromShortcut: Boolean = false,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // 首帧前就落到目标目录，避免先渲染主页再动画切换（那会产生额外的淡入淡出）
    val controller = androidx.compose.runtime.remember(initialPath, initialCategory, requestToken) {
        FileManagerController(context, onOpenInBrowser, onBack).apply {
            routeMode = true
            upNavEnabled = !fromShortcut
            showHome = false
            initialPath?.let { currentDir = java.io.File(it) }
            initialCategory?.let { name ->
                runCatching { FileCategory.valueOf(name) }.getOrNull()?.let { openCategory(it) }
            }
        }
    }
    FileManagerPage(
        onBack = onBack,
        onOpenInBrowser = onOpenInBrowser,
        controller = controller,
        pageTitle = pageTitle,
    )
}


@Composable
private fun MiuixBreadcrumbSaf(controller: FileManagerController) {
    val segments = if (controller.safPath.isEmpty()) emptyList()
    else controller.safPath.split("/")
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        BreadcrumbBar(
            items = (listOf("内部存储") + segments).mapIndexed { index, text ->
                BreadcrumbItem(path = index.toString(), text = text)
            },
            highlightIndex = segments.size,
            onItemClick = { index ->
                controller.safPath = segments.take(index).joinToString("/")
                controller.loadSaf()
            },
            insideMargin = PaddingValues(start = 26.dp, end = 16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SafEntryRow(entry: FmSafStore.Entry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (entry.isDir) MiuixIcons.FolderFill else MiuixIcons.FileIcon,
            contentDescription = null,
            tint = if (entry.isDir) Color(0xFFFFD966) else MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.size(if (entry.isDir) 30.dp else 26.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, fontSize = 16.sp, color = MiuixTheme.colorScheme.onSurface, maxLines = 1)
            val summary = if (entry.isDir) {
                fmFormatTime(entry.modified)
            } else {
                listOf(fmFormatSize(entry.size), fmFormatTime(entry.modified))
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            }
            if (summary.isNotBlank()) {
                Text(summary, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
            }
        }
        if (entry.isDir) {
            Icon(
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategoryListMiuix(
    controller: FileManagerController,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    val listState = rememberLazyListState()
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    if (controller.categoryLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    size = 18.dp,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "正在加载...",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        return
    }
    if (controller.categoryLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    size = 18.dp,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "正在加载...",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        return
    }
    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(200)) + slideInHorizontally(tween(280)) { it / 8 },
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding, bottom = 96.dp),
    ) {
        if (false) {
        } else if (controller.categoryFiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Text("没有找到相关文件", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
        } else {
            items(controller.categoryFiles) { entry -> MiuixEntryRow(controller, entry) }
        }
    }
    }

}

@Composable
private fun CategoryListMaterial(
    controller: FileManagerController,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    val listState = rememberLazyListState()
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(200)) + slideInHorizontally(tween(280)) { it / 8 },
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding, bottom = 96.dp),
    ) {
        if (false) {
        } else if (controller.categoryFiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    M3Text("没有找到相关文件", fontSize = 13.sp, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(controller.categoryFiles) { entry -> MaterialEntryRow(controller, entry) }
        }
    }
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

    val selecting = controller.selecting
    val selected = controller.isSelected(entry)

    val onClick: () -> Unit = {
        when {
            controller.pendingOp != null -> controller.pickDir = entry.file
            selecting -> controller.toggle(entry)
            entry.isDir -> controller.enter(entry.file)
            else -> controller.open(entry)
        }
    }
    val onLongClick: () -> Unit = {
        if (controller.pendingOp == null) controller.toggle(entry)
    }

    val rowIndex = remember(entry.file.absolutePath, controller.currentDir) {
        controller.entriesOf(controller.currentDir).indexOfFirst {
            it.file.absolutePath == entry.file.absolutePath
        }
    }
    var rowHeightPx by remember { mutableFloatStateOf(1f) }
    var paintDy by remember { mutableFloatStateOf(0f) }
    var lastLongPressAt by remember { mutableStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowHeightPx = it.size.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(entry.file.absolutePath) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        lastLongPressAt = System.currentTimeMillis()
                        controller.enterSelection(entry)
                        controller.beginPaint(rowIndex, rowHeightPx)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        paintDy += amount.y
                        controller.paintBy(paintDy)
                    },
                    onDragEnd = {
                        paintDy = 0f
                        controller.endPaint()
                        lastLongPressAt = System.currentTimeMillis()
                    },
                    onDragCancel = { paintDy = 0f; controller.endPaint() },
                )
            }
            .clickable(onClick = onClick)
            .padding(start = 26.dp, end = 26.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EntryThumb(entry)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.file.name,
                style = MiuixTheme.textStyles.body1,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            if (summary.isNotBlank()) {
                Text(
                    summary,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
        if (selecting) {
            Spacer(Modifier.width(8.dp))
            androidx.compose.animation.AnimatedVisibility(
                visible = selecting,
                enter = fadeIn(tween(180)) + expandHorizontally(tween(220)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(200)),
            ) {
                Checkbox(
                    state = if (selected) androidx.compose.ui.state.ToggleableState.On
                    else androidx.compose.ui.state.ToggleableState.Off,
                    onClick = null,
                )
            }
        } else if (entry.isDir) {
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** 读取 APK 自身的应用图标 */
private fun loadApkIcon(context: android.content.Context, file: java.io.File): ImageBitmap? {
    return try {
        val pm = context.packageManager
        val info = pm.getPackageArchiveInfo(file.absolutePath, 0) ?: return null
        val app = info.applicationInfo ?: return null
        app.sourceDir = file.absolutePath
        app.publicSourceDir = file.absolutePath
        val drawable = app.loadIcon(pm) as? android.graphics.drawable.BitmapDrawable ?: return null
        drawable.bitmap?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

@Composable
private fun EntryThumb(entry: FsEntry, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, entry.file.absolutePath) {
        value = when {
            entry.isDir -> null
            isImage(entry.file.name) -> withContext(Dispatchers.IO) { loadThumbnail(entry.file) }
            entry.file.name.endsWith(".apk", ignoreCase = true) ->
                withContext(Dispatchers.IO) { loadApkIcon(context, entry.file) }
            else -> null
        }
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val typed = if (entry.isDir) null else fmTypeIcon(entry.file)
            Icon(
                imageVector = when {
                    entry.isDir -> MiuixIcons.FolderFill
                    typed != null -> typed.first
                    else -> androidx.compose.material.icons.Icons.Rounded.Description
                },
                contentDescription = null,
                tint = when {
                    entry.isDir -> Color(0xFFFFD966)
                    typed != null -> typed.second
                    else -> MiuixTheme.colorScheme.primary
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MiuixActionBar(
    items: List<FsEntry>,
    onOpen: (FsEntry) -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onExtract: (FsEntry) -> Unit,
    onCompress: () -> Unit,
    onDelete: () -> Unit,
    onAbout: () -> Unit,
    onShare: () -> Unit,
) {
    val single = items.size == 1
    val one = items.firstOrNull()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (single && one != null && !one.isDir && isArchive(one.file.name)) {
            CapsuleAction(androidx.compose.material.icons.Icons.Rounded.Unarchive, "解压", enabled = true) { onExtract(one) }
        } else {
            CapsuleAction(androidx.compose.material.icons.Icons.Rounded.Archive, "压缩", enabled = true) { onCompress() }
        }
        CapsuleAction(MiuixIcons.Copy, "复制", enabled = true) { onCopy() }
        CapsuleAction(MiuixIcons.MoveFile, "移动", enabled = true) { onMove() }
        CapsuleAction(MiuixIcons.Delete, "删除", enabled = true) { onDelete() }
        var showMore by remember { mutableStateOf(false) }
        Box {
            CapsuleAction(MiuixIcons.More, "更多", enabled = true) { showMore = true }
            WindowCascadingListPopup(
                show = showMore,
                entries = listOf(
                    DropdownEntry(
                        items = listOf(
                            DropdownItem(text = "关于", onClick = { showMore = false; onAbout() }),
                            DropdownItem(text = "分享", onClick = { showMore = false; onShare() }),
                        )
                    )
                ),
                onDismissRequest = { showMore = false },
            )
        }
    }
}

@Composable
private fun CapsuleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = if (enabled) MiuixTheme.colorScheme.onSurface
            else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = if (enabled) MiuixTheme.colorScheme.onSurface
            else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
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
private fun FileManagerMaterial(
    controller: FileManagerController,
    onAbout: (List<FsEntry>) -> Unit,
    onShare: (List<FsEntry>) -> Unit,
    onDelete: () -> Unit,
    onCompress: () -> Unit,
    onExtract: (FsEntry) -> Unit,
    onTool: (FmTool) -> Unit,
    pageTitle: String,
) {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    val topState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topState)
    val surface = M3MaterialTheme.colorScheme.surface
    var barBottomPx by remember { mutableIntStateOf(0) }
    val topPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp
    var headerHeightDp by remember { mutableStateOf(0.dp) }

    var barItems by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var barPending by remember { mutableStateOf<PendingOp?>(null) }
    LaunchedEffect(controller.selected) {
        if (controller.selected.isNotEmpty()) barItems = controller.selected
    }
    LaunchedEffect(controller.pendingOp) {
        if (controller.pendingOp != null) barPending = controller.pendingOp
        else if (!controller.selecting) barPending = null
    }

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(top = topPadding)) {
            val subPageM = controller.currentDir != controller.root && !controller.safMode
            val breadcrumbShiftM by animateDpAsState(
                targetValue = if (subPageM) 0.dp else headerHeightDp,
                animationSpec = tween(320, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
                label = "breadcrumbShiftM",
            )
            if (subPageM) {
                Box(modifier = Modifier.graphicsLayer { translationY = breadcrumbShiftM.toPx() }) {
                    MiuixBreadcrumb(controller, controller.currentDir, 16.dp, controller.category)
                }
            }

            if (controller.category != null) {
                CategoryListMaterial(controller, topPadding)
            } else {
            AnimatedContent(
                targetState = controller.currentDir,
                modifier = Modifier.weight(1f),
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
                val materialFileListState = controller.listStateFor(controller.currentDir.absolutePath)
    val materialPullUi = remember { PullRefreshUiState() }
    var pendingScrollM by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    LaunchedEffect(materialFileListState) {
        snapshotFlow {
            materialFileListState.firstVisibleItemIndex to materialFileListState.firstVisibleItemScrollOffset
        }.collect {
            controller.scrollMemory[controller.currentDir.absolutePath] = it
        }
    }
    LaunchedEffect(controller.currentDir) {
        pendingScrollM = controller.scrollMemory[controller.currentDir.absolutePath]
    }
    LaunchedEffect(controller.dirEntries, pendingScrollM) {
        val target = pendingScrollM ?: return@LaunchedEffect
        val count = materialFileListState.layoutInfo.totalItemsCount
        if (count > 0) {
            materialFileListState.scrollToItem(target.first.coerceIn(0, count - 1), target.second)
            pendingScrollM = null
        }
    }
                PullRefreshContainer(
                    canPull = {
                        materialFileListState.firstVisibleItemIndex == 0 &&
                            materialFileListState.firstVisibleItemScrollOffset == 0
                    },
                    onRefresh = { controller.reload() },
                    indicatorTopPadding = 52.dp,
                    uiState = materialPullUi,
                ) {
                LazyColumn(
                    state = materialFileListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .graphicsLayer { alpha = 1f },
                    contentPadding = PaddingValues(bottom = navBottom + 96.dp),
                ) {
                    if (controller.safMode) {
                        item(key = "saf_header") { MiuixBreadcrumbSaf(controller) }
                        if (controller.safFailed) {
                            item(key = "saf_failed") {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    M3Text("无法读取该目录", fontSize = 15.sp, color = M3MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    M3Text(
                                        "系统未授予该目录的访问权限，可点击下方按钮选择该目录授权。",
                                        fontSize = 12.sp,
                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    androidx.compose.material3.Button(
                                        onClick = { controller.onRequestSafGrant?.invoke() },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        M3Text("选择目录授权")
                                    }
                                }
                            }
                        }
                        items(controller.safEntries) { entry ->
                            SafEntryRow(entry) { controller.safOpen(entry) }
                        }
                    }
                    if (controller.needsAllFiles) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                    .padding(bottom = M3_SEGMENT_GROUP_GAP),
                            ) {
                                MoteSegmentedColumn(
                                    content = listOf(
                                        {
                                            M3SegmentArrowItem(
                                                title = "受系统限制，Mote 需要「所有文件访问权限」才能显示全部文件",
                                                onClick = { requestAllFiles(controller.context) },
                                            )
                                        }
                                    )
                                )
                            }
                        }
                    }
                    if (!controller.safMode) {
                        items(controller.entriesOf(dir)) { entry ->
                            MaterialEntryRow(controller, entry)
                        }
                    }
                }
                }
            }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(surface)
                .statusBarsPadding()
                .onGloballyPositioned { coords ->
                    val bottom = coords.boundsInWindow().bottom.toInt()
                    if (bottom != barBottomPx) barBottomPx = bottom
                },
        ) {
            LargeTopAppBar(
                title = { M3Text(if (controller.pendingOp != null) "选择目标目录" else "文件管理") },
                navigationIcon = {
                    M3IconButton(onClick = {
                        when {
                            controller.pendingOp != null -> controller.cancelPick()
                            controller.selecting -> controller.exitSelection()
                            !controller.up() -> controller.onBack()
                        }
                    }) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                actions = {
                    if (controller.selecting) {
                        M3IconButton(onClick = { controller.toggleSelectAll() }) {
                            M3Icon(Icons.Filled.SelectAll, contentDescription = "全选")
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                scrollBehavior = scrollBehavior,
            )
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = controller.selected.isNotEmpty() || controller.pendingOp != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                M3Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                ) {
                    if (barPending != null) {
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
                    } else {
                        val items = barItems
                        val single = items.size == 1
                        val one = items.firstOrNull()
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (single && one != null && !one.isDir && isArchive(one.file.name)) {
                                M3CapsuleAction(Icons.Rounded.Unarchive, "解压", true) {
                                    controller.extract(one)
                                    controller.exitSelection()
                                }
                            } else {
                                M3CapsuleAction(Icons.Rounded.Archive, "压缩", true) { controller.compressSelected() }
                            }
                            M3CapsuleAction(Icons.Filled.Folder, "复制", true) { controller.beginMove(PendingOp.COPY) }
                            M3CapsuleAction(Icons.Filled.Folder, "移动", true) { controller.beginMove(PendingOp.MOVE) }
                            M3CapsuleAction(Icons.Filled.Delete, "删除", true) { onDelete() }
                            if (single && one != null && !one.isDir && isArchive(one.file.name)) {
                                M3CapsuleAction(Icons.Filled.Folder, "解压", true) {
                                    controller.extract(one)
                                    controller.exitSelection()
                                }
                            }
                            M3CapsuleAction(Icons.Rounded.MoreVert, "更多", true) {
                                onAbout(controller.selected.ifEmpty { barItems })
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialEntryRow(controller: FileManagerController, entry: FsEntry) {
    val summary = if (entry.isDir) fmFormatTime(entry.modified)
    else listOfNotNull(
        fmFormatSize(entry.size).takeIf { it.isNotBlank() },
        fmFormatTime(entry.modified),
    ).joinToString(" · ")

    val selected = controller.isSelected(entry)

    Row(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = {
                    when {
                        controller.pendingOp != null -> controller.pickDir = entry.file
                        controller.selecting -> controller.toggle(entry)
                        entry.isDir -> controller.enter(entry.file)
                        else -> controller.open(entry)
                    }
                },
                onLongClick = {
                    if (controller.pendingOp == null) controller.toggle(entry)
                },
            )
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
        androidx.compose.animation.AnimatedVisibility(
            visible = controller.selecting,
            enter = fadeIn(tween(180)) + expandHorizontally(tween(220)),
            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(200)),
        ) {
            androidx.compose.material3.Checkbox(checked = selected, onCheckedChange = null)
        }
        if (!controller.selecting && entry.isDir) {
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
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = if (enabled) M3MaterialTheme.colorScheme.onSurface
    else M3MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Column(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        M3Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp), tint = color)
        Spacer(Modifier.height(2.dp))
        M3Text(label, fontSize = 11.sp, color = color)
    }
}

private fun dirSize(file: java.io.File): Long {
    if (file.isFile) return file.length()
    var total = 0L
    file.listFiles()?.forEach { total += dirSize(it) }
    return total
}

private fun typeLabel(file: java.io.File, isDir: Boolean): String {
    if (isDir) return "文件夹"
    val ext = file.name.substringAfterLast('.', "").uppercase()
    return if (ext.isBlank()) "文件" else "$ext 文件"
}

@Composable
private fun FileInfoDialog(
    items: List<FsEntry>,
    show: Boolean,
    onJump: (java.io.File) -> Unit,
    onDismiss: () -> Unit,
) {
    val single = items.size == 1
    val one = items.firstOrNull()
    val title = if (single && one != null) one.file.name else {
        val first = one?.file?.name ?: ""
        "$first 等 ${items.size} 个文件"
    }

    val totalSize by produceState(initialValue = -1L, items) {
        value = withContext(Dispatchers.IO) {
            var sum = 0L
            items.forEach { entry ->
                sum += if (entry.isDir) dirSize(entry.file) else entry.size
            }
            sum
        }
    }

    WindowDialog(show = show, title = title, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (single && one != null) {
                InfoRow("位置") {
                    Text(
                        one.file.absolutePath,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onJump(one.file.parentFile ?: one.file) },
                    )
                }
                InfoRow("类型") { Text(typeLabel(one.file, one.isDir), style = MiuixTheme.textStyles.body1) }
                InfoRow("大小") {
                    val size = if (!one.isDir) one.size else totalSize
                    Text(
                        if (size < 0) "计算中…" else fmFormatSize(size).ifBlank { "0 B" },
                        style = MiuixTheme.textStyles.body1,
                    )
                }
                InfoRow("时间") { Text(fmFormatTime(one.modified), style = MiuixTheme.textStyles.body1) }
                InfoRow("可读") { Text(if (one.file.canRead()) "是" else "否", style = MiuixTheme.textStyles.body1) }
                InfoRow("可写") { Text(if (one.file.canWrite()) "是" else "否", style = MiuixTheme.textStyles.body1) }
                InfoRow("隐藏") { Text(if (one.file.isHidden) "是" else "否", style = MiuixTheme.textStyles.body1) }
            } else {
                InfoRow("总计大小") {
                    Text(
                        if (totalSize < 0) "计算中…" else fmFormatSize(totalSize).ifBlank { "0 B" },
                        style = MiuixTheme.textStyles.body1,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            "$label：",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Box(modifier = Modifier.weight(1f)) { value() }
    }
}

private fun zipForShare(context: android.content.Context, items: List<FsEntry>): java.io.File? {
    return try {
        val dir = java.io.File(context.cacheDir, "share").apply { mkdirs() }
        val zip = java.io.File(dir, "Mote分享_${System.currentTimeMillis()}.zip")
        java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(zip))).use { out ->
            val buffer = ByteArray(256 * 1024)
            fun add(node: java.io.File, base: String) {
                if (node.isDirectory) {
                    node.listFiles()?.forEach { add(it, "$base${node.name}/") }
                } else if (node.isFile) {
                    out.putNextEntry(java.util.zip.ZipEntry("$base${node.name}"))
                    java.io.FileInputStream(node).use { input ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            out.write(buffer, 0, read)
                        }
                    }
                    out.closeEntry()
                }
            }
            items.forEach { add(it.file, "") }
        }
        zip
    } catch (_: Throwable) {
        null
    }
}

private fun shareFile(context: android.content.Context, file: java.io.File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "分享压缩包").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        Toast.makeText(context, "没有可以分享的应用", Toast.LENGTH_SHORT).show()
    }
}
