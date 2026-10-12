@file:Suppress("SpellCheckingInspection")
@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.torachen.mote.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import java.io.File
import java.util.Calendar
import java.util.Locale

// ★★★ MIUIX 工具（overscroll 回弹） ★★★
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

// ============================================================
// 常量
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private const val POLL_INTERVAL_MS = 800L
private val DELETE_RED = Color(0xFFFF3B30)
private const val TOOLBAR_BLUR_RADIUS = 30f

// ============================================================
// 全局状态：请求浏览器新建窗口打开某 URL
// ============================================================

internal object PendingOpenInNewWindowStore {
    var pending by mutableStateOf<String?>(null)
}

// ============================================================
// 数据模型
// ============================================================

private enum class DownloadGroup { RUNNING, COMPLETED, FAILED }

private data class DownloadItem(
    val id: Long,
    val fileName: String,
    val mimeType: String,
    val status: Int,
    val reason: Int,
    val bytesDownloaded: Long,
    val bytesTotal: Long,
    val localUri: String,
    val sourceUrl: String,
    val description: String,
    val isFileValid: Boolean = true,
    val date: Long = 0L,
) {
    val group: DownloadGroup
        get() = when (status) {
            DownloadManager.STATUS_RUNNING,
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_PAUSED -> DownloadGroup.RUNNING
            DownloadManager.STATUS_SUCCESSFUL -> DownloadGroup.COMPLETED
            DownloadManager.STATUS_FAILED -> DownloadGroup.FAILED
            else -> DownloadGroup.RUNNING
        }

    val hasKnownTotal: Boolean get() = bytesTotal > 0L
    val progress: Float
        get() = if (hasKnownTotal) (bytesDownloaded.toFloat() / bytesTotal).coerceIn(0f, 1f) else 0f

    val isInvalid: Boolean get() = group == DownloadGroup.COMPLETED && !isFileValid

    val statusText: String
        get() = when (status) {
            DownloadManager.STATUS_PENDING -> "等待中"
            DownloadManager.STATUS_RUNNING -> "下载中"
            DownloadManager.STATUS_PAUSED -> when (reason) {
                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "等待 WLAN"
                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "等待网络"
                DownloadManager.PAUSED_WAITING_TO_RETRY -> "等待重试"
                else -> "已暂停"
            }
            DownloadManager.STATUS_SUCCESSFUL -> if (isFileValid) "已完成" else "文件已失效"
            DownloadManager.STATUS_FAILED -> when (reason) {
                DownloadManager.ERROR_CANNOT_RESUME -> "无法恢复"
                DownloadManager.ERROR_DEVICE_NOT_FOUND -> "存储设备未找到"
                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "文件已存在"
                DownloadManager.ERROR_FILE_ERROR -> "文件错误"
                DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP 数据错误"
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足"
                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "重定向过多"
                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "未处理的 HTTP 状态码"
                else -> "下载失败"
            }
            else -> "未知状态"
        }
}

private data class SpeedSample(
    val bytes: Long,
    val timestamp: Long,
)

// ============================================================
// 下载管理页
// ============================================================

@Composable
fun DownloadManagerScreen(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner(parent = null)
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val smoothEase = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }

    val groupExpandSpec = remember { tween<IntSize>(durationMillis = 400, easing = smoothEase) }
    val groupShrinkSpec = remember { tween<IntSize>(durationMillis = 320, easing = smoothEase) }

    val groupFadeInSpec = remember { tween<Float>(durationMillis = 400, easing = smoothEase) }
    val groupFadeOutSpec = remember { tween<Float>(durationMillis = 320, easing = smoothEase) }

    val cardSizeSpec = remember { tween<IntSize>(durationMillis = 400, easing = smoothEase) }

    val progressSpec = remember { tween<Float>(durationMillis = 500, easing = LinearEasing) }

    val selectAllColorSpec = remember { tween<Color>(durationMillis = 220, easing = smoothEase) }

    val rowExitHeightSpec = remember { tween<IntSize>(durationMillis = 320, easing = smoothEase) }
    val rowEnterHeightSpec = remember { tween<IntSize>(durationMillis = 320, easing = smoothEase) }
    val rowExitFadeSpec = remember { tween<Float>(durationMillis = 320, easing = smoothEase) }
    val rowEnterFadeSpec = remember { tween<Float>(durationMillis = 280, easing = smoothEase) }

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val contentTopPadding = if (topBarBottomPx > 0) {
        with(density) { topBarBottomPx.toDp() }
    } else {
        statusBarHeight + TOP_BAR_FALLBACK_HEIGHT
    }

    // ---------------- 状态 ----------------
    val items = remember { mutableStateListOf<DownloadItem>() }
    var refreshTick by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    var pendingDeleteIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLocalFile by remember { mutableStateOf(false) }

    val deletingIds = remember { mutableStateListOf<Long>() }

    val speedMap = remember { mutableStateMapOf<Long, Long>() }
    val lastSampleMap = remember { mutableMapOf<Long, SpeedSample>() }
    val lastSpeedUpdateMap = remember { mutableMapOf<Long, Long>() }

    // ---------------- 轮询 ----------------
    LaunchedEffect(refreshTick) {
        while (true) {
            val list = queryDownloadItems(context)
            items.clear()
            items.addAll(list)

            val now = System.currentTimeMillis()
            val aliveIds = list.map { it.id }.toSet()
            val it2 = lastSampleMap.keys.iterator()
            while (it2.hasNext()) {
                if (it2.next() !in aliveIds) it2.remove()
            }
            val it3 = speedMap.keys.iterator()
            while (it3.hasNext()) {
                if (it3.next() !in aliveIds) it3.remove()
            }
            // ★ 修复：清理已消失任务的"最后速度更新时间"
            val it4 = lastSpeedUpdateMap.keys.iterator()
            while (it4.hasNext()) {
                if (it4.next() !in aliveIds) it4.remove()
            }

            list.forEach { item ->
                val last = lastSampleMap[item.id]
                if (last != null &&
                    item.status == DownloadManager.STATUS_RUNNING &&
                    item.bytesDownloaded >= last.bytes
                ) {
                    val dt = (now - last.timestamp) / 1000.0
                    if (dt >= 0.5) {
                        val db = item.bytesDownloaded - last.bytes
                        // ★ 修复：只有真正有下载量时才更新速度；
                        //   没增长时保持上次的值，不覆盖为 0
                        if (db > 0 && dt > 0) {
                            speedMap[item.id] = (db / dt).toLong()
                            lastSpeedUpdateMap[item.id] = now
                        }
                        lastSampleMap[item.id] = SpeedSample(item.bytesDownloaded, now)
                    }
                } else {
                    if (item.status != DownloadManager.STATUS_RUNNING) {
                        speedMap[item.id] = 0L
                        lastSpeedUpdateMap.remove(item.id)
                    }
                    lastSampleMap[item.id] = SpeedSample(item.bytesDownloaded, now)
                }
            }

            // ★ 修复：速度超时清理（5 秒没更新过速度就清 0）
            val SPEED_TIMEOUT_MS = 5000L
            speedMap.keys.toList().forEach { id ->
                val lastUpdate = lastSpeedUpdateMap[id] ?: 0L
                if (lastUpdate > 0L && now - lastUpdate > SPEED_TIMEOUT_MS) {
                    speedMap[id] = 0L
                    lastSpeedUpdateMap.remove(id)
                }
            }

            delay(POLL_INTERVAL_MS)
        }
    }

    // ---------------- 分组 ----------------
    val runningItems by remember {
        derivedStateOf { items.filter { it.group == DownloadGroup.RUNNING } }
    }
    val completedItems by remember {
        derivedStateOf { items.filter { it.group == DownloadGroup.COMPLETED } }
    }
    val failedItems by remember {
        derivedStateOf { items.filter { it.group == DownloadGroup.FAILED } }
    }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: Long) {
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun selectAll() {
        selectedIds.clear()
        selectedIds.addAll(items.map { it.id })
    }

    fun clearSelection() {
        selectedIds.clear()
    }

    fun removeWithAnimation(
        ids: List<Long>,
        deleteLocalFileToo: Boolean,
        snackbarMsg: String,
    ) {
        if (ids.isEmpty()) return

        deletingIds.addAll(ids)

        scope.launch {
            delay(400)

            withContext(Dispatchers.IO) {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                val snapshot = items.toList()
                ids.forEach { id ->
                    if (deleteLocalFileToo) {
                        try {
                            val item = snapshot.find { it.id == id }
                            if (item != null && item.isFileValid && item.localUri.isNotBlank()) {
                                val uri = Uri.parse(item.localUri)
                                if (uri.scheme == "file") {
                                    uri.path?.let { path -> File(path).delete() }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    try { dm?.remove(id) } catch (_: Exception) {}
                }
            }

            items.removeAll { it.id in ids }
            deletingIds.removeAll { it in ids }
            ids.forEach { id ->
                speedMap.remove(id)
                lastSampleMap.remove(id)
                lastSpeedUpdateMap.remove(id)
            }
            refreshTick++

            snackbarHostState.showSnackbar(
                message = snackbarMsg,
                duration = SnackbarDuration.Short,
            )
        }
    }

    fun removeOneHistory(id: Long) {
        removeWithAnimation(
            ids = listOf(id),
            deleteLocalFileToo = false,
            snackbarMsg = "已删除下载记录",
        )
    }

    fun handleDeleteSelected() {
        val idsToDelete = pendingDeleteIds.toList()
        val shouldDeleteLocal = deleteLocalFile

        showDeleteDialog = false
        deleteLocalFile = false
        pendingDeleteIds = emptyList()

        exitMultiSelect()

        removeWithAnimation(
            ids = idsToDelete,
            deleteLocalFileToo = shouldDeleteLocal,
            snackbarMsg = "已删除 ${idsToDelete.size} 项任务",
        )
    }

    fun handleCancelDownload(item: DownloadItem) {
        removeWithAnimation(
            ids = listOf(item.id),
            deleteLocalFileToo = true,
            snackbarMsg = "已取消下载",
        )
    }

    fun handleOpen(item: DownloadItem) {
        val fileName = item.fileName.lowercase()
        val mime = item.mimeType.lowercase()

        val isHlsOrDash =
            fileName.endsWith(".m3u8") ||
                    fileName.endsWith(".mpd") ||
                    mime.contains("mpegurl") ||
                    mime.contains("x-mpegurl") ||
                    mime.contains("dash+xml")

        if (isHlsOrDash) {
            val playUrl = item.sourceUrl.ifBlank { item.localUri }
            if (playUrl.isBlank()) {
                Toast.makeText(context, "无法获取播放地址", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                NativePlayerActivity.start(
                    context = context,
                    url = playUrl,
                    title = item.fileName,
                    fromWeb = false,
                )
            } catch (e: Exception) {
                Toast.makeText(context, "无法播放：${e.message}", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val isVideo = mime.startsWith("video/") || listOf(
            ".mp4", ".mkv", ".webm", ".mov", ".avi", ".flv",
            ".wmv", ".m4v", ".3gp", ".ts", ".rmvb"
        ).any { fileName.endsWith(it) }

        val isHtml = mime == "text/html" || mime == "application/xhtml+xml" || listOf(
            ".html", ".htm", ".xhtml"
        ).any { fileName.endsWith(it) }

        val uri: Uri? = try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.getUriForDownloadedFile(item.id)
                ?: if (item.localUri.isNotBlank()) Uri.parse(item.localUri) else null
        } catch (_: Exception) { null }

        if (uri == null) {
            Toast.makeText(context, "无法获取文件", Toast.LENGTH_SHORT).show()
            return
        }

        if (isVideo) {
            try {
                NativePlayerActivity.start(
                    context = context,
                    url = uri.toString(),
                    title = item.fileName,
                    fromWeb = false,
                )
            } catch (e: Exception) {
                Toast.makeText(context, "无法打开视频：${e.message}", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (isHtml) {
            PendingOpenInNewWindowStore.pending = uri.toString()
            onBack()
            return
        }

        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val openMime = try { dm.getMimeTypeForDownloadedFile(item.id) } catch (_: Exception) { null }
                ?: item.mimeType.ifBlank { "*/*" }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, openMime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开文件：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = isMultiSelectMode) { exitMultiSelect() }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            // ========== 内容层 ==========
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            if (items.isEmpty()) {
                                EmptyState()
                            } else {
                                // ============================================
                                // 下载中分组
                                // ============================================
                                AnimatedVisibility(
                                    visible = runningItems.isNotEmpty(),
                                    enter = expandVertically(
                                        animationSpec = groupExpandSpec,
                                        expandFrom = Alignment.Top,
                                    ) + fadeIn(animationSpec = groupFadeInSpec),
                                    exit = shrinkVertically(
                                        animationSpec = groupShrinkSpec,
                                        shrinkTowards = Alignment.Top,
                                    ) + fadeOut(animationSpec = groupFadeOutSpec),
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("下载中 (${runningItems.size}个文件)")
                                        Card(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .padding(bottom = 12.dp)
                                                .animateContentSize(cardSizeSpec),
                                        ) {
                                            runningItems.forEachIndexed { index, item ->
                                                AnimatedVisibility(
                                                    visible = item.id !in deletingIds,
                                                    enter = expandVertically(
                                                        animationSpec = rowEnterHeightSpec,
                                                        expandFrom = Alignment.Top,
                                                    ) + fadeIn(animationSpec = rowEnterFadeSpec),
                                                    exit = shrinkVertically(
                                                        animationSpec = rowExitHeightSpec,
                                                        shrinkTowards = Alignment.Top,
                                                    ) + fadeOut(animationSpec = rowExitFadeSpec),
                                                ) {
                                                    DownloadRunningItem(
                                                        item = item,
                                                        speed = speedMap[item.id] ?: 0L,
                                                        isFirst = index == 0,
                                                        isLast = index == runningItems.lastIndex,
                                                        isMultiSelectMode = isMultiSelectMode,
                                                        isSelected = item.id in selectedIds,
                                                        progressAnimationSpec = progressSpec,
                                                        onClick = {
                                                            if (isMultiSelectMode) toggleSelect(item.id)
                                                        },
                                                        onLongClick = {
                                                            if (!isMultiSelectMode) {
                                                                isMultiSelectMode = true
                                                                toggleSelect(item.id)
                                                            }
                                                        },
                                                        onCancelClick = {
                                                            if (isMultiSelectMode) {
                                                                toggleSelect(item.id)
                                                            } else {
                                                                handleCancelDownload(item)
                                                            }
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ============================================
                                // 已完成分组
                                // ============================================
                                AnimatedVisibility(
                                    visible = completedItems.isNotEmpty(),
                                    enter = expandVertically(
                                        animationSpec = groupExpandSpec,
                                        expandFrom = Alignment.Top,
                                    ) + fadeIn(animationSpec = groupFadeInSpec),
                                    exit = shrinkVertically(
                                        animationSpec = groupShrinkSpec,
                                        shrinkTowards = Alignment.Top,
                                    ) + fadeOut(animationSpec = groupFadeOutSpec),
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("已完成 (${completedItems.size}个文件)")
                                        Card(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .padding(bottom = 12.dp)
                                                .animateContentSize(cardSizeSpec),
                                        ) {
                                            completedItems.forEachIndexed { index, item ->
                                                AnimatedVisibility(
                                                    visible = item.id !in deletingIds,
                                                    enter = expandVertically(
                                                        animationSpec = rowEnterHeightSpec,
                                                        expandFrom = Alignment.Top,
                                                    ) + fadeIn(animationSpec = rowEnterFadeSpec),
                                                    exit = shrinkVertically(
                                                        animationSpec = rowExitHeightSpec,
                                                        shrinkTowards = Alignment.Top,
                                                    ) + fadeOut(animationSpec = rowExitFadeSpec),
                                                ) {
                                                    DownloadRowItem(
                                                        item = item,
                                                        isFirst = index == 0,
                                                        isLast = index == completedItems.lastIndex,
                                                        isMultiSelectMode = isMultiSelectMode,
                                                        isSelected = item.id in selectedIds,
                                                        progressAnimationSpec = progressSpec,
                                                        onClick = {
                                                            if (isMultiSelectMode) {
                                                                toggleSelect(item.id)
                                                            } else if (!item.isInvalid) {
                                                                handleOpen(item)
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (!isMultiSelectMode) {
                                                                isMultiSelectMode = true
                                                                toggleSelect(item.id)
                                                            }
                                                        },
                                                        onActionClick = {
                                                            if (isMultiSelectMode) {
                                                                toggleSelect(item.id)
                                                            } else if (item.isInvalid) {
                                                                removeOneHistory(item.id)
                                                            }
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ============================================
                                // 失败分组
                                // ============================================
                                AnimatedVisibility(
                                    visible = failedItems.isNotEmpty(),
                                    enter = expandVertically(
                                        animationSpec = groupExpandSpec,
                                        expandFrom = Alignment.Top,
                                    ) + fadeIn(animationSpec = groupFadeInSpec),
                                    exit = shrinkVertically(
                                        animationSpec = groupShrinkSpec,
                                        shrinkTowards = Alignment.Top,
                                    ) + fadeOut(animationSpec = groupFadeOutSpec),
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("失败 (${failedItems.size}个文件)")
                                        Card(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .padding(bottom = 12.dp)
                                                .animateContentSize(cardSizeSpec),
                                        ) {
                                            failedItems.forEachIndexed { index, item ->
                                                AnimatedVisibility(
                                                    visible = item.id !in deletingIds,
                                                    enter = expandVertically(
                                                        animationSpec = rowEnterHeightSpec,
                                                        expandFrom = Alignment.Top,
                                                    ) + fadeIn(animationSpec = rowEnterFadeSpec),
                                                    exit = shrinkVertically(
                                                        animationSpec = rowExitHeightSpec,
                                                        shrinkTowards = Alignment.Top,
                                                    ) + fadeOut(animationSpec = rowExitFadeSpec),
                                                ) {
                                                    DownloadRowItem(
                                                        item = item,
                                                        isFirst = index == 0,
                                                        isLast = index == failedItems.lastIndex,
                                                        isMultiSelectMode = isMultiSelectMode,
                                                        isSelected = item.id in selectedIds,
                                                        progressAnimationSpec = progressSpec,
                                                        onClick = {
                                                            if (isMultiSelectMode) toggleSelect(item.id)
                                                        },
                                                        onLongClick = {
                                                            if (!isMultiSelectMode) {
                                                                isMultiSelectMode = true
                                                                toggleSelect(item.id)
                                                            }
                                                        },
                                                        onActionClick = {
                                                            if (isMultiSelectMode) {
                                                                toggleSelect(item.id)
                                                            } else {
                                                                retryDownload(context, item)
                                                                scope.launch {
                                                                    delay(300)
                                                                    refreshTick++
                                                                }
                                                            }
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ★ 底部导航栏占位（参照 MIUIX 官方写法）
                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }

            // ========== 顶栏层 ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = TOP_BAR_BLUR_RADIUS,
                                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(
                                            color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA),
                                        ),
                                    ),
                                ),
                            )
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
                val barTitle = if (isMultiSelectMode) {
                    if (selectedIds.isEmpty()) "请选择项目"
                    else "已选择 ${selectedIds.size} 项"
                } else "下载管理"

                TopAppBar(
                    title = barTitle,
                    largeTitle = barTitle,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isMultiSelectMode) exitMultiSelect() else onBack()
                        }) {
                            Icon(
                                imageVector = if (isMultiSelectMode) MiuixIcons.Close else MiuixIcons.Back,
                                contentDescription = if (isMultiSelectMode) "退出多选" else "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        if (isMultiSelectMode) {
                            val allSelected = items.isNotEmpty() && selectedIds.size == items.size
                            val selectAllTint by animateColorAsState(
                                targetValue = if (allSelected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurface
                                },
                                animationSpec = selectAllColorSpec,
                                label = "selectAllTint",
                            )
                            IconButton(onClick = {
                                if (allSelected) clearSelection() else selectAll()
                            }) {
                                Icon(
                                    imageVector = MiuixIcons.SelectAll,
                                    contentDescription = if (allSelected) "取消全选" else "全选",
                                    tint = selectAllTint,
                                )
                            }
                        }
                    },
                )
            }

            // ========== 浮动工具栏 ==========
            val navBarBottom = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()

            AnimatedVisibility(
                visible = isMultiSelectMode && !showDeleteDialog,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 220),
                    initialOffsetY = { fullHeight -> fullHeight + 40 },
                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 200),
                    targetOffsetY = { fullHeight -> fullHeight + 40 },
                ) + fadeOut(animationSpec = tween(durationMillis = 150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarBottom + 24.dp)
                    .padding(horizontal = 24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(28.dp)
                            shadowElevation = 24.dp.toPx()
                            ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                            spotShadowColor = Color.Black.copy(alpha = 0.5f)
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .then(
                            if (blurSupported && backdrop != null) {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RoundedCornerShape(28.dp),
                                    blurRadius = TOOLBAR_BLUR_RADIUS,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = Color.White.copy(alpha = 0.9f),
                                            ),
                                        ),
                                    ),
                                )
                            } else {
                                Modifier.background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(28.dp),
                                )
                            }
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        val toShare = items.filter {
                            it.id in selectedIds && it.group == DownloadGroup.COMPLETED && !it.isInvalid
                        }
                        scope.launch {
                            if (toShare.isEmpty()) {
                                snackbarHostState.showSnackbar(
                                    message = "未选择可分享的文件",
                                    duration = SnackbarDuration.Short,
                                )
                            } else if (toShare.size == 1) {
                                shareDownloadedFile(context, toShare.first())
                            } else {
                                snackbarHostState.showSnackbar(
                                    message = "一次只能分享一个文件",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Forward,
                            contentDescription = "分享",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(onClick = {
                        if (selectedIds.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "请先选择要删除的项目",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        } else {
                            pendingDeleteIds = selectedIds.toList()
                            deleteLocalFile = false
                            showDeleteDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Delete,
                            contentDescription = "删除",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            // ========== Snackbar ==========
            SnackbarHost(
                state = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarBottom + if (isMultiSelectMode) 100.dp else 12.dp),
            )
        }
    }

    // ========== 删除确认弹窗 ==========
    val dialogNavOwner = rememberNavigationEventDispatcherOwner(parent = null)
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides dialogNavOwner
    ) {
        WindowDialog(
            show = showDeleteDialog,
            title = "删除",
            onDismissRequest = {
                showDeleteDialog = false
                deleteLocalFile = false
                pendingDeleteIds = emptyList()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "确定删除${pendingDeleteIds.size}条任务",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { deleteLocalFile = !deleteLocalFile }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        state = if (deleteLocalFile) ToggleableState.On
                        else ToggleableState.Off,
                        onClick = { deleteLocalFile = !deleteLocalFile },
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "同时删除本地文件",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            deleteLocalFile = false
                            pendingDeleteIds = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { handleDeleteSelected() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = DELETE_RED,
                        ),
                    ) {
                        Text("删除", color = DELETE_RED)
                    }
                }
            }
        }
    }
}

// ============================================================
// 下载中项目
// ============================================================

@Composable
private fun DownloadRunningItem(
    item: DownloadItem,
    speed: Long,
    isFirst: Boolean,
    isLast: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    progressAnimationSpec: FiniteAnimationSpec<Float>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val contentColor = MiuixTheme.colorScheme.onSurface
    val secondaryColor = MiuixTheme.colorScheme.onSurfaceVariantSummary

    val animatedProgress by animateFloatAsState(
        targetValue = if (item.hasKnownTotal) item.progress else 0f,
        animationSpec = progressAnimationSpec,
        label = "dl_progress_${item.id}",
    )

    val speedText: String? = if (speed > 0L) formatSpeed(speed) else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(
                start = 16.dp,
                end = 12.dp,
                top = if (isFirst) 14.dp else 12.dp,
                bottom = if (isLast) 14.dp else 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            InfiniteProgressIndicator(
                color = MiuixTheme.colorScheme.primary,
                size = 26.dp,
                strokeWidth = 2.5.dp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.fileName.ifBlank { "未命名文件" },
                fontSize = 15.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (item.hasKnownTotal) {
                        "${formatBytes(item.bytesDownloaded)} / ${formatBytes(item.bytesTotal)}"
                    } else {
                        formatBytes(item.bytesDownloaded)
                    },
                    fontSize = 11.sp,
                    color = secondaryColor,
                    maxLines = 1,
                )

                Spacer(Modifier.weight(1f))

                if (speedText != null) {
                    Text(
                        text = speedText,
                        fontSize = 11.sp,
                        color = secondaryColor,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )
        }

        Spacer(Modifier.width(12.dp))

        if (isMultiSelectMode) {
            Checkbox(
                state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                onClick = onCancelClick,
            )
        } else {
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .clickable(onClick = onCancelClick)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "取消",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ============================================================
// 单条下载项（已完成 / 失败 / 失效历史）
// ============================================================

@Composable
private fun DownloadRowItem(
    item: DownloadItem,
    isFirst: Boolean,
    isLast: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    progressAnimationSpec: FiniteAnimationSpec<Float>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val invalid = item.isInvalid
    val contentColor = if (invalid) Color(0xFF999999)
    else MiuixTheme.colorScheme.onSurface
    val secondaryColor = if (invalid) Color(0xFFBBBBBB)
    else MiuixTheme.colorScheme.onSurfaceVariantSummary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(
                start = 16.dp,
                end = 8.dp,
                top = if (isFirst) 14.dp else 10.dp,
                bottom = if (isLast) 14.dp else 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                invalid -> Icon(
                    imageVector = MiuixIcons.Close,
                    contentDescription = null,
                    tint = Color(0xFF999999),
                    modifier = Modifier.size(24.dp),
                )
                item.group == DownloadGroup.COMPLETED -> Icon(
                    imageVector = iconForMime(item.mimeType, item.fileName),
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                else -> Icon(
                    imageVector = MiuixIcons.Close,
                    contentDescription = null,
                    tint = DELETE_RED,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName.ifBlank { "未命名文件" },
                fontSize = 15.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))

            when {
                invalid -> {
                    Text(
                        text = "文件已被第三方应用删除",
                        fontSize = 12.sp,
                        color = secondaryColor,
                        maxLines = 1,
                    )
                }
                item.group == DownloadGroup.COMPLETED -> {
                    val timeStr = formatDownloadTime(item.date)
                    Text(
                        text = if (item.bytesTotal > 0) {
                            "${formatBytes(item.bytesTotal)} | $timeStr"
                        } else {
                            "已完成 | $timeStr"
                        },
                        fontSize = 12.sp,
                        color = secondaryColor,
                        maxLines = 1,
                    )
                }
                item.group == DownloadGroup.FAILED -> {
                    val timeStr = formatDownloadTime(item.date)
                    Text(
                        text = "${item.statusText} | $timeStr",
                        fontSize = 12.sp,
                        color = DELETE_RED,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        DownloadActionArea(
            item = item,
            isMultiSelectMode = isMultiSelectMode,
            isSelected = isSelected,
            secondaryColor = secondaryColor,
            onActionClick = onActionClick,
        )
    }
}

// ============================================================
// 右侧按钮区
// ============================================================

@Composable
private fun DownloadActionArea(
    item: DownloadItem,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    secondaryColor: Color,
    onActionClick: () -> Unit,
) {
    val localEase = remember { CubicBezierEasing(0.32f, 0.72f, 0f, 1f) }
    val checkboxEnterSpec = remember {
        tween<IntOffset>(durationMillis = 280, easing = localEase)
    }
    val checkboxExitSpec = remember {
        tween<IntOffset>(durationMillis = 240, easing = localEase)
    }
    val iconFadeInSpec = remember { tween<Float>(durationMillis = 220, easing = localEase) }
    val iconFadeOutSpec = remember { tween<Float>(durationMillis = 180, easing = localEase) }

    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isMultiSelectMode,
            enter = slideInHorizontally(
                animationSpec = checkboxEnterSpec,
                initialOffsetX = { fullWidth -> fullWidth + 20 },
            ) + fadeIn(animationSpec = iconFadeInSpec),
            exit = slideOutHorizontally(
                animationSpec = checkboxExitSpec,
                targetOffsetX = { fullWidth -> fullWidth + 20 },
            ) + fadeOut(animationSpec = iconFadeOutSpec),
        ) {
            Checkbox(
                state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                onClick = onActionClick,
            )
        }

        AnimatedVisibility(
            visible = !isMultiSelectMode,
            enter = fadeIn(animationSpec = iconFadeInSpec),
            exit = fadeOut(animationSpec = iconFadeOutSpec),
        ) {
            when {
                item.isInvalid -> IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "删除历史",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp),
                    )
                }
                item.group == DownloadGroup.FAILED -> IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = MiuixIcons.Refresh,
                        contentDescription = "重试",
                        tint = secondaryColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                else -> { /* 空 */ }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp)
            .padding(bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = MiuixIcons.Download,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "下载列表为空",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

// ============================================================
// 辅助函数
// ============================================================

private suspend fun queryDownloadItems(context: Context): List<DownloadItem> =
    withContext(Dispatchers.IO) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return@withContext emptyList()

        val cursor: Cursor = try {
            dm.query(DownloadManager.Query())
        } catch (_: Exception) {
            return@withContext emptyList()
        }

        val result = mutableListOf<DownloadItem>()
        try {
            if (!cursor.moveToFirst()) return@withContext emptyList()

            val idxId = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
            val idxTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val idxMediaType = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
            val idxStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val idxReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val idxBytes = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val idxTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val idxLocal = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val idxUri = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
            val idxDesc = cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)

            val idxDate = try {
                val i = cursor.getColumnIndex("date")
                if (i >= 0) i else {
                    try { cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP) }
                    catch (_: Exception) { -1 }
                }
            } catch (_: Exception) { -1 }

            do {
                val id = if (idxId >= 0) cursor.getLong(idxId) else continue
                val fileName = if (idxTitle >= 0) {
                    cursor.getStringOrNull(idxTitle) ?: "未命名文件"
                } else "未命名文件"
                val mime = if (idxMediaType >= 0) {
                    cursor.getStringOrNull(idxMediaType) ?: "*/*"
                } else "*/*"
                val status = if (idxStatus >= 0) cursor.getInt(idxStatus)
                else DownloadManager.STATUS_PENDING
                val reason = if (idxReason >= 0) cursor.getInt(idxReason) else 0
                val bytes = if (idxBytes >= 0) cursor.getLong(idxBytes) else 0L
                val total = if (idxTotal >= 0) cursor.getLong(idxTotal) else -1L
                val localUri = if (idxLocal >= 0) cursor.getStringOrNull(idxLocal) ?: "" else ""
                val srcUri = if (idxUri >= 0) cursor.getStringOrNull(idxUri) ?: "" else ""
                val desc = if (idxDesc >= 0) cursor.getStringOrNull(idxDesc) ?: "" else ""
                val date = try {
                    if (idxDate >= 0) cursor.getLong(idxDate) else 0L
                } catch (_: Exception) { 0L }

                var fileValid = true
                if (status == DownloadManager.STATUS_SUCCESSFUL && localUri.isNotBlank()) {
                    fileValid = checkFileExists(context, localUri)
                }

                result.add(
                    DownloadItem(
                        id = id,
                        fileName = fileName,
                        mimeType = mime,
                        status = status,
                        reason = reason,
                        bytesDownloaded = bytes,
                        bytesTotal = total,
                        localUri = localUri,
                        sourceUrl = srcUri,
                        description = desc,
                        isFileValid = fileValid,
                        date = date,
                    )
                )
            } while (cursor.moveToNext())
        } catch (_: Exception) {
        } finally {
            try { cursor.close() } catch (_: Exception) {}
        }

        result.sortWith(
            compareByDescending<DownloadItem> {
                when (it.group) {
                    DownloadGroup.RUNNING -> 3
                    DownloadGroup.FAILED -> 2
                    DownloadGroup.COMPLETED -> 1
                }
            }.thenByDescending { it.id }
        )

        result
    }

private fun checkFileExists(context: Context, uriStr: String): Boolean {
    return try {
        val uri = Uri.parse(uriStr)
        when (uri.scheme) {
            "file" -> {
                val path = uri.path ?: return false
                File(path).exists()
            }
            "content" -> {
                val projection = arrayOf(android.provider.OpenableColumns.SIZE)
                context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                    c.count > 0
                } ?: false
            }
            else -> true
        }
    } catch (_: Exception) {
        false
    }
}

private fun Cursor.getStringOrNull(index: Int): String? {
    if (index < 0 || isNull(index)) return null
    return try { getString(index) } catch (_: Exception) { null }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0L) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB/s", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.2f MB/s", mb)
}

private fun formatDownloadTime(ms: Long): String {
    if (ms <= 0L) return "未知时间"
    return try {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = ms }

        val nowDay = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetDay = Calendar.getInstance().apply {
            timeInMillis = target.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays =
            ((nowDay.timeInMillis - targetDay.timeInMillis) / (24L * 3600L * 1000L)).toInt()

        val h = target.get(Calendar.HOUR_OF_DAY)
        val m = target.get(Calendar.MINUTE)
        val timeStr = String.format(Locale.US, "%02d:%02d", h, m)

        when {
            diffDays == 0 -> "今天 $timeStr"
            diffDays == 1 -> "昨天 $timeStr"
            diffDays == 2 -> "前天 $timeStr"
            else -> {
                val y = target.get(Calendar.YEAR)
                val mo = target.get(Calendar.MONTH) + 1
                val d = target.get(Calendar.DAY_OF_MONTH)
                "${y}年${mo}月${d}日 $timeStr"
            }
        }
    } catch (_: Exception) {
        "未知时间"
    }
}

private fun iconForMime(mime: String, fileName: String): ImageVector {
    val lower = (mime + " " + fileName).lowercase()
    return when {
        lower.contains("video") || lower.contains(".mp4") ||
                lower.contains(".mkv") || lower.contains(".webm") -> MiuixIcons.Play
        lower.contains("audio") || lower.contains(".mp3") ||
                lower.contains(".m4a") || lower.contains(".flac") -> MiuixIcons.Music
        lower.contains("image") || lower.contains(".jpg") ||
                lower.contains(".jpeg") || lower.contains(".png") ||
                lower.contains(".gif") || lower.contains(".webp") -> MiuixIcons.Photos
        lower.contains("html") || lower.contains(".htm") -> MiuixIcons.File
        lower.contains("zip") || lower.contains("rar") ||
                lower.contains(".7z") || lower.contains("tar") -> MiuixIcons.Folder
        else -> MiuixIcons.File
    }
}

private fun shareDownloadedFile(context: Context, item: DownloadItem) {
    try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = dm.getUriForDownloadedFile(item.id)
            ?: run {
                if (item.localUri.isNotBlank()) Uri.parse(item.localUri) else null
            }
            ?: run {
                Toast.makeText(context, "无法获取文件", Toast.LENGTH_SHORT).show()
                return
            }

        val mime = try { dm.getMimeTypeForDownloadedFile(item.id) } catch (_: Exception) { null }
            ?: item.mimeType.ifBlank { "*/*" }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "分享文件").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun retryDownload(context: Context, item: DownloadItem) {
    if (item.sourceUrl.isBlank()) {
        Toast.makeText(context, "无法重试：源地址丢失", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager)?.remove(item.id)
    } catch (_: Exception) {}

    try {
        val request = DownloadManager.Request(Uri.parse(item.sourceUrl)).apply {
            setTitle(item.fileName.ifBlank { "下载文件" })
            setDescription("正在下载…")
            setMimeType(item.mimeType.ifBlank { null })
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        Toast.makeText(context, "已开始重新下载", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "重试失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}