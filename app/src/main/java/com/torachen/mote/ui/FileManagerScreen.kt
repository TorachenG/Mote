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
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.File as FileIcon
import top.yukonga.miuix.kmp.icon.extended.Folder as FolderIcon
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private data class FsEntry(
    val file: java.io.File,
    val isDir: Boolean,
    val size: Long,
    val modified: Long,
)

private enum class PendingOp { COPY, MOVE }

private const val FM_BLUR_RADIUS = 25f
private const val FM_BLEND_ALPHA = 0.8f
private val FM_FALLBACK_HEIGHT = 128.dp

private class FileManagerController(
    val context: Context,
    val onOpenInBrowser: (String) -> Unit,
    val onBack: () -> Unit,
) {
    val root: java.io.File = Environment.getExternalStorageDirectory()

    var currentDir by mutableStateOf(root)
    var dirEntries by mutableStateOf<Map<String, List<FsEntry>>>(emptyMap())
    var selected by mutableStateOf<List<FsEntry>>(emptyList())
    var pendingOp by mutableStateOf<PendingOp?>(null)
    var pickDir by mutableStateOf<java.io.File?>(null)
    var needsAllFiles by mutableStateOf(false)

    val selecting: Boolean get() = selected.isNotEmpty()

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
        needsAllFiles = !hasAllFiles(context)
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

    BackHandler(enabled = controller.selecting || controller.currentDir != controller.root) {
        when {
            controller.selecting -> controller.selected = emptyList()
            else -> controller.up()
        }
    }

    var infoItems by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    val toastText = toast ?: if (running) "正在处理…" else null

    when (LocalUiMode.current) {
        UiMode.Miuix -> FileManagerMiuix(controller, toastText, onInfo = { infoItems = it })
        UiMode.Material -> FileManagerMaterial(controller, toastText, onInfo = { infoItems = it })
    }

    if (infoItems.isNotEmpty()) {
        FileInfoDialog(
            items = infoItems,
            onJump = { target ->
                controller.enter(target)
                controller.selected = emptyList()
                infoItems = emptyList()
            },
            onDismiss = { infoItems = emptyList() },
        )
    }
}

/* ---------------- MIUIX ---------------- */

@Composable
private fun FileManagerMiuix(controller: FileManagerController, toast: String?, onInfo: (List<FsEntry>) -> Unit) {
    val scrollBehavior = MiuixScrollBehavior()
    val density = LocalDensity.current
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()
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
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surface) {
        listOf(BlendColorEntry(color = surface.copy(alpha = FM_BLEND_ALPHA)))
    }

    var barBottomPx by remember { mutableIntStateOf(0) }
    val topPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp

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
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = topPadding, bottom = 96.dp),
                    overscrollEffect = null,
                ) {
                    if (controller.needsAllFiles) {
                        item {
                            BasicComponent(
                                title = "需要「所有文件访问权限」才能显示全部文件（含未知格式）",
                                summary = "点击去授权",
                                onClick = { requestAllFiles(controller.context) },
                            )
                        }
                    }
                    items(controller.entriesOf(dir)) { entry ->
                        MiuixEntryRow(controller, entry)
                    }
                }
            }
        }


        // 模糊层比标题栏更高，覆盖到目录轴下方，避免内容从目录轴处直接透出
        // 整段标题栏满强度模糊，渐变带收在标题栏底部之内，避免糊到第一条选项
        val fadeBand = 24.dp
        val blurTotalHeight = topPadding
        val headerFraction = ((topPadding - fadeBand) / topPadding).coerceIn(0f, 0.95f)
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
                title = if (controller.pendingOp != null) "选择目标目录" else "文件管理",
                largeTitle = if (controller.pendingOp != null) "选择目标目录" else "文件管理",
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
                        controller.selecting -> IconButton(onClick = { controller.selected = emptyList() }) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "退出多选",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        else -> SettingsBackButton(
                            onBack = {
                                if (!controller.up()) controller.onBack()
                            },
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
            MiuixBreadcrumb(controller, controller.currentDir, 26.dp)
            }
        }

        AnimatedVisibility(
            visible = controller.selecting || controller.pendingOp != null || controller.selected.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 20.dp),
        ) {
            if (barPending != null) {
                MiuixPickBar(controller)
            } else if (barItems.isNotEmpty()) {
                MiuixActionBar(
                    items = barItems,
                    onOpen = { one -> controller.open(one); controller.selected = emptyList() },
                    onCopy = { controller.beginMove(PendingOp.COPY) },
                    onMove = { controller.beginMove(PendingOp.MOVE) },
                    onExtract = { one -> controller.extract(one); controller.selected = emptyList() },
                    onInfo = { onInfo(controller.selected.ifEmpty { barItems }) },
                )
            }
        }

        if (toast != null) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 96.dp)) {
                ToastText(toast)
            }
        }
    }
}

@Composable
private fun MiuixBreadcrumb(
    controller: FileManagerController,
    dir: java.io.File,
    startPadding: androidx.compose.ui.unit.Dp,
) {
    val segments = remember(dir) { controller.segmentsOf(dir) }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
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
            insideMargin = PaddingValues(start = startPadding, end = 16.dp),
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        if (entry.isDir) {
            ArrowPreference(
                title = entry.file.name,
                summary = summary,
                startAction = { EntryThumb(entry) },
                endActions = {
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
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = null,
            )
        } else {
            BasicComponent(
                title = entry.file.name,
                summary = summary,
                startAction = { EntryThumb(entry) },
                endActions = {
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
                },
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
private fun MiuixActionBar(
    items: List<FsEntry>,
    onOpen: (FsEntry) -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onExtract: (FsEntry) -> Unit,
    onInfo: () -> Unit,
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
        CapsuleAction(MiuixIcons.FileIcon, "打开", enabled = single) { one?.let(onOpen) }
        CapsuleAction(MiuixIcons.Copy, "复制", enabled = true) { onCopy() }
        CapsuleAction(MiuixIcons.MoveFile, "移动", enabled = true) { onMove() }
        if (single && one != null && !one.isDir && isArchive(one.file.name)) {
            CapsuleAction(MiuixIcons.FolderIcon, "解压", enabled = true) { onExtract(one) }
        }
        CapsuleAction(MiuixIcons.Info, "关于", enabled = true) { onInfo() }
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
private fun FileManagerMaterial(controller: FileManagerController, toast: String?, onInfo: (List<FsEntry>) -> Unit) {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    val topState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topState)
    val surface = M3MaterialTheme.colorScheme.surface
    var barBottomPx by remember { mutableIntStateOf(0) }
    val topPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp

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
            MiuixBreadcrumb(controller, controller.currentDir, 16.dp)

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    if (controller.needsAllFiles) {
                        item {
                            M3Surface(
                                color = M3MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
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
                            controller.selecting -> controller.selected = emptyList()
                            !controller.up() -> controller.onBack()
                        }
                    }) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
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
                            M3CapsuleAction(Icons.Filled.InsertDriveFile, "打开", single) {
                                one?.let { controller.open(it); controller.selected = emptyList() }
                            }
                            M3CapsuleAction(Icons.Filled.Folder, "复制", true) { controller.beginMove(PendingOp.COPY) }
                            M3CapsuleAction(Icons.Filled.Folder, "移动", true) { controller.beginMove(PendingOp.MOVE) }
                            if (single && one != null && !one.isDir && isArchive(one.file.name)) {
                                M3CapsuleAction(Icons.Filled.Folder, "解压", true) {
                                    controller.extract(one)
                                    controller.selected = emptyList()
                                }
                            }
                            M3CapsuleAction(Icons.Filled.Folder, "关于", true) {
                                onInfo(controller.selected.ifEmpty { barItems })
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

@Composable
private fun FileInfoDialog(
    items: List<FsEntry>,
    onJump: (java.io.File) -> Unit,
    onDismiss: () -> Unit,
) {
    val single = items.size == 1
    val one = items.firstOrNull()
    val title = if (single && one != null) one.file.name else {
        val first = one?.file?.name ?: ""
        "$first 等 ${items.size} 个文件"
    }
    val totalSize = items.sumOf { if (it.isDir) 0L else it.size }

    WindowDialog(show = true, title = title, onDismissRequest = onDismiss) {
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
                InfoRow("大小") {
                    Text(
                        if (one.isDir) "文件夹" else fmFormatSize(one.size),
                        style = MiuixTheme.textStyles.body1,
                    )
                }
                InfoRow("时间") { Text(fmFormatTime(one.modified), style = MiuixTheme.textStyles.body1) }
                InfoRow("可读") { Text(if (one.file.canRead()) "是" else "否", style = MiuixTheme.textStyles.body1) }
                InfoRow("可写") { Text(if (one.file.canWrite()) "是" else "否", style = MiuixTheme.textStyles.body1) }
                InfoRow("隐藏") { Text(if (one.file.isHidden) "是" else "否", style = MiuixTheme.textStyles.body1) }
            } else {
                InfoRow("总计大小") { Text(fmFormatSize(totalSize), style = MiuixTheme.textStyles.body1) }
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
