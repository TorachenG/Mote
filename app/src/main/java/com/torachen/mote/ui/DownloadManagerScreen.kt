@file:Suppress("SpellCheckingInspection")
@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.torachen.mote.ui

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import com.torachen.mote.MoteApplication
import com.torachen.mote.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// Material 3 Imports
import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.Checkbox as M3Checkbox
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.LinearProgressIndicator as M3LinearProgressIndicator
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.SnackbarDuration as M3SnackbarDuration
import androidx.compose.material3.SnackbarHost as M3SnackbarHost
import androidx.compose.material3.SnackbarHostState as M3SnackbarHostState
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote

// ============================================================
// 常量与全局状态
// ============================================================

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val DELETE_RED = Color(0xFFFF3B30)
private const val TOOLBAR_BLUR_RADIUS = 30f

private val DOWNLOAD_SEG_BIG_CORNER = 25.dp
private val DOWNLOAD_SEG_SMALL_CORNER = 5.dp
private val DOWNLOAD_SEG_GAP = 4.dp
private val DOWNLOAD_LIST_HORIZONTAL_PADDING = 16.dp
private val DOWNLOAD_GROUP_GAP = 12.dp

internal val DATA_LIMIT_OPTIONS = listOf("无限制", "500MB", "400MB", "300MB", "200MB", "100MB", "50MB", "20MB", "10MB", "禁止下载")
internal const val DATA_LIMIT_DEFAULT = 0
internal const val KEY_DATA_LIMIT = "download_data_limit"

internal val SPEED_LIMIT_OPTIONS = listOf("无限制", "20MB/S", "10MB/S", "5MB/S", "1MB/S")
internal const val SPEED_LIMIT_DEFAULT = 0
internal const val KEY_SPEED_LIMIT = "download_speed_limit"

internal object PendingOpenInNewWindowStore {
    var pending by mutableStateOf<String?>(null)
}

internal object PendingOpenDownloadsStore {
    var pending by mutableStateOf(false)
}

internal object PendingTaskDetailStore {
    var pending by mutableStateOf<MoteDownloadTask?>(null)
}

enum class MoteDownloadStatus {
    PENDING, RUNNING, PAUSED, COMPLETED, FAILED
}

data class MoteDownloadTask(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val mimeType: String,
    val filePath: String,
    val uri: String? = null,
    var status: MoteDownloadStatus = MoteDownloadStatus.PENDING,
    var downloadedBytes: Long = 0L,
    var totalBytes: Long = -1L,
    var downloadSpeed: Long = 0L,
    var errorMsg: String? = null,
    val createTime: Long = System.currentTimeMillis(),
    var isMissing: Boolean = false,
    var isRetryFromMissing: Boolean = false,
    var failedAfterRetry: Boolean = false
) {
    val progress: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
}

// ============================================================
// 内部下载器（协程 + 通知 + 广播控制 + 持久化）
// ============================================================

internal object MoteDownloader {
    val tasks = mutableStateListOf<MoteDownloadTask>()
    private val jobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private const val CHANNEL_ID = "mote_download_channel"
    private const val NOTIFICATION_ID = 9002
    private var receiverRegistered = false

    private const val PREFS_NAME = "mote_prefs"
    private const val KEY_DOWNLOAD_TASKS = "mote_download_tasks_v1"
    private var isLoaded = false

    var toastEvent by mutableStateOf<String?>(null)

    fun isWifiNetwork(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (_: Exception) { false }
    }

    fun ensureLoaded() {
        if (isLoaded) return
        isLoaded = true
        val context = MoteAppContextHolder.appContext ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_DOWNLOAD_TASKS, null) ?: return
        try {
            val arr = JSONArray(raw)
            tasks.clear()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                if (id.isBlank()) continue

                val statusStr = obj.optString("status", "PAUSED")
                var status = try { MoteDownloadStatus.valueOf(statusStr) } catch (_: Exception) { MoteDownloadStatus.PAUSED }
                if (status == MoteDownloadStatus.RUNNING || status == MoteDownloadStatus.PENDING) {
                    status = MoteDownloadStatus.PAUSED
                }

                val uri = obj.optString("uri", "").ifBlank { null }
                val filePath = obj.optString("filePath", "")

                var actualDownloadedBytes = obj.optLong("downloadedBytes", 0L)
                var isMissing = false

                if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")?.use { pfd ->
                            actualDownloadedBytes = pfd.statSize
                        }
                    } catch (_: Exception) {
                        if (status == MoteDownloadStatus.COMPLETED) isMissing = true
                    }
                } else {
                    val f = File(filePath)
                    if (f.exists()) {
                        actualDownloadedBytes = f.length()
                    } else {
                        if (status == MoteDownloadStatus.COMPLETED) isMissing = true
                    }
                }

                tasks.add(MoteDownloadTask(
                    id = id,
                    url = obj.optString("url", ""),
                    fileName = obj.optString("fileName", ""),
                    mimeType = obj.optString("mimeType", ""),
                    filePath = filePath,
                    uri = uri,
                    status = status,
                    downloadedBytes = actualDownloadedBytes,
                    totalBytes = obj.optLong("totalBytes", -1L),
                    createTime = obj.optLong("createTime", System.currentTimeMillis()),
                    isMissing = isMissing
                ))
            }
            tasks.sortByDescending { it.createTime }
        } catch (_: Exception) {}
    }

    private fun saveTasks() {
        val context = MoteAppContextHolder.appContext ?: return
        try {
            val arr = JSONArray()
            tasks.forEach { task ->
                arr.put(JSONObject().apply {
                    put("id", task.id)
                    put("url", task.url)
                    put("fileName", task.fileName)
                    put("mimeType", task.mimeType)
                    put("filePath", task.filePath)
                    put("uri", task.uri ?: "")
                    put("status", task.status.name)
                    put("downloadedBytes", task.downloadedBytes)
                    put("totalBytes", task.totalBytes)
                    put("createTime", task.createTime)
                })
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_DOWNLOAD_TASKS, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun start(context: Context, task: MoteDownloadTask) {
        val existingIndex = tasks.indexOfFirst { it.id == task.id }
        if (existingIndex >= 0) {
            tasks[existingIndex] = task
        } else {
            tasks.add(0, task)
        }
        saveTasks()
        start(context, task.id)
    }

    fun start(context: Context, taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return
        val task = tasks[index]

        if (task.status == MoteDownloadStatus.RUNNING) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isWifi = isWifiNetwork(context)
        val dataLimitIndex = prefs.getInt(KEY_DATA_LIMIT, DATA_LIMIT_DEFAULT)
        val dataLimitMb = when (dataLimitIndex) {
            1 -> 500; 2 -> 400; 3 -> 300; 4 -> 200; 5 -> 100; 6 -> 50; 7 -> 20; 8 -> 10; 9 -> -1; else -> 0
        }

        if (!isWifi && dataLimitMb == -1) {
            updateTask(taskId) { it.copy(status = MoteDownloadStatus.PAUSED) }
            toastEvent = "当前处于数据网络且已开启禁止下载，任务已暂停"
            return
        }

        updateTask(taskId) { it.copy(status = MoteDownloadStatus.RUNNING, errorMsg = null) }
        updateNotification(context)

        jobs[task.id]?.cancel()
        jobs[task.id] = scope.launch {
            try {
                val url = URL(task.url)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 15000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")

                val mediaUriStr = task.uri
                val mediaUri: Uri? = mediaUriStr?.let { Uri.parse(it) }
                val useMediaStore = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaUri != null

                var startPos = 0L
                if (useMediaStore) {
                    try {
                        context.contentResolver.openFileDescriptor(mediaUri!!, "r")?.use { pfd ->
                            startPos = pfd.statSize.coerceAtLeast(0L)
                        }
                    } catch (_: Exception) {
                        startPos = 0L
                    }
                } else {
                    val file = File(task.filePath)
                    if (file.exists()) {
                        startPos = file.length()
                    }
                }

                if (startPos > 0) {
                    conn.setRequestProperty("Range", "bytes=$startPos-")
                }

                conn.connect()
                val code = conn.responseCode
                if (code !in 200..299 && code != 206) {
                    throw RuntimeException("HTTP $code")
                }

                if (code == 200) {
                    startPos = 0L
                }

                val total = if (code == 200) {
                    conn.contentLength.toLong()
                } else {
                    startPos + conn.contentLength
                }

                updateTask(task.id) { it.copy(totalBytes = total, downloadedBytes = startPos) }

                if (useMediaStore) {
                    val pfd = context.contentResolver.openFileDescriptor(mediaUri!!, "w")
                        ?: throw RuntimeException("无法打开 MediaStore 文件")
                    try {
                        val fos = FileOutputStream(pfd.fileDescriptor)
                        try {
                            val channel = fos.channel
                            channel.position(startPos)

                            val input = conn.inputStream
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var currentPos = startPos
                            var lastTime = System.currentTimeMillis()
                            var lastBytes = currentPos

                            var lastSpeedCheckTime = System.currentTimeMillis()
                            var bytesSinceLastCheck = 0L
                            val speedLimitIndex = prefs.getInt(KEY_SPEED_LIMIT, SPEED_LIMIT_DEFAULT)
                            val speedLimitBytesPerSec = when(speedLimitIndex) {
                                1 -> 20 * 1024 * 1024L
                                2 -> 10 * 1024 * 1024L
                                3 -> 5 * 1024 * 1024L
                                4 -> 1 * 1024 * 1024L
                                else -> 0L
                            }

                            while (isActive && tasks.find { it.id == task.id }?.status == MoteDownloadStatus.RUNNING) {
                                bytesRead = input.read(buffer)
                                if (bytesRead == -1) break

                                if (speedLimitBytesPerSec > 0) {
                                    bytesSinceLastCheck += bytesRead
                                    val now = System.currentTimeMillis()
                                    val elapsed = now - lastSpeedCheckTime
                                    if (elapsed > 0) {
                                        val expectedTime = (bytesSinceLastCheck * 1000L) / speedLimitBytesPerSec
                                        if (expectedTime > elapsed) {
                                            delay(expectedTime - elapsed)
                                        }
                                        if (elapsed > 1000) {
                                            lastSpeedCheckTime = System.currentTimeMillis()
                                            bytesSinceLastCheck = 0
                                        }
                                    }
                                }

                                if (!isWifi && dataLimitMb > 0) {
                                    val limitBytes = dataLimitMb * 1024L * 1024L
                                    val sessionBytes = currentPos - startPos
                                    if (sessionBytes > limitBytes) {
                                        pause(task.id)
                                        toastEvent = "已达数据网络限额，已暂停下载"
                                        break
                                    }
                                }

                                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead))
                                currentPos += bytesRead

                                val now = System.currentTimeMillis()
                                var speed = tasks.find { it.id == task.id }?.downloadSpeed ?: 0L
                                if (now - lastTime >= 500) {
                                    val timeDiff = (now - lastTime) / 1000.0
                                    val byteDiff = currentPos - lastBytes
                                    speed = if (timeDiff > 0) (byteDiff / timeDiff).toLong() else 0L
                                    lastTime = now
                                    lastBytes = currentPos
                                }

                                updateTask(task.id) { it.copy(downloadedBytes = currentPos, downloadSpeed = speed) }
                            }

                            try { input.close() } catch (_: Exception) {}
                        } finally {
                            try { fos.close() } catch (_: Exception) {}
                        }
                    } finally {
                        try { pfd.close() } catch (_: Exception) {}
                    }

                    if (tasks.find { it.id == task.id }?.status == MoteDownloadStatus.RUNNING) {
                        try {
                            context.contentResolver.update(
                                mediaUri,
                                ContentValues().apply {
                                    put(MediaStore.Downloads.IS_PENDING, 0)
                                },
                                null, null,
                            )
                        } catch (_: Exception) {}
                        updateTask(task.id) { it.copy(status = MoteDownloadStatus.COMPLETED, isMissing = false) }
                    }
                } else {
                    val file = File(task.filePath)
                    file.parentFile?.mkdirs()

                    val input = conn.inputStream
                    val raf = RandomAccessFile(file, "rw")
                    raf.seek(startPos)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var currentPos = startPos

                    var lastTime = System.currentTimeMillis()
                    var lastBytes = currentPos

                    var lastSpeedCheckTime = System.currentTimeMillis()
                    var bytesSinceLastCheck = 0L
                    val speedLimitIndex = prefs.getInt(KEY_SPEED_LIMIT, SPEED_LIMIT_DEFAULT)
                    val speedLimitBytesPerSec = when(speedLimitIndex) {
                        1 -> 20 * 1024 * 1024L
                        2 -> 10 * 1024 * 1024L
                        3 -> 5 * 1024 * 1024L
                        4 -> 1 * 1024 * 1024L
                        else -> 0L
                    }

                    while (isActive && tasks.find { it.id == task.id }?.status == MoteDownloadStatus.RUNNING) {
                        bytesRead = input.read(buffer)
                        if (bytesRead == -1) break

                        if (speedLimitBytesPerSec > 0) {
                            bytesSinceLastCheck += bytesRead
                            val now = System.currentTimeMillis()
                            val elapsed = now - lastSpeedCheckTime
                            if (elapsed > 0) {
                                val expectedTime = (bytesSinceLastCheck * 1000L) / speedLimitBytesPerSec
                                if (expectedTime > elapsed) {
                                    delay(expectedTime - elapsed)
                                }
                                if (elapsed > 1000) {
                                    lastSpeedCheckTime = System.currentTimeMillis()
                                    bytesSinceLastCheck = 0
                                }
                            }
                        }

                        if (!isWifi && dataLimitMb > 0) {
                            val limitBytes = dataLimitMb * 1024L * 1024L
                            val sessionBytes = currentPos - startPos
                            if (sessionBytes > limitBytes) {
                                pause(task.id)
                                toastEvent = "已达数据网络限额，已暂停下载"
                                break
                            }
                        }

                        raf.write(buffer, 0, bytesRead)
                        currentPos += bytesRead

                        val now = System.currentTimeMillis()
                        var speed = tasks.find { it.id == task.id }?.downloadSpeed ?: 0L
                        if (now - lastTime >= 500) {
                            val timeDiff = (now - lastTime) / 1000.0
                            val byteDiff = currentPos - lastBytes
                            speed = if (timeDiff > 0) (byteDiff / timeDiff).toLong() else 0L
                            lastTime = now
                            lastBytes = currentPos
                        }

                        updateTask(task.id) { it.copy(downloadedBytes = currentPos, downloadSpeed = speed) }
                    }

                    raf.close()
                    input.close()

                    if (tasks.find { it.id == task.id }?.status == MoteDownloadStatus.RUNNING) {
                        updateTask(task.id) { it.copy(status = MoteDownloadStatus.COMPLETED, isMissing = false) }
                    }
                }
                updateNotification(context)
            } catch (e: Exception) {
                if (tasks.find { it.id == task.id }?.status == MoteDownloadStatus.RUNNING) {
                    val taskNow = tasks.find { it.id == task.id }
                    val isRetry = taskNow?.isRetryFromMissing == true
                    if (isRetry) {
                        toastEvent = "重新下载失败：${e.message ?: "网络错误"}"
                    }
                    updateTask(task.id) {
                        it.copy(
                            status = MoteDownloadStatus.FAILED,
                            errorMsg = e.message,
                            failedAfterRetry = isRetry,
                            isRetryFromMissing = false
                        )
                    }
                }
                updateNotification(context)
            }
        }
    }

    fun pause(taskId: String) {
        updateTask(taskId) {
            if (it.status == MoteDownloadStatus.RUNNING) {
                jobs[taskId]?.cancel()
                it.copy(status = MoteDownloadStatus.PAUSED, downloadSpeed = 0L)
            } else it
        }
        MoteAppContextHolder.appContext?.let { updateNotification(it) }
    }

    fun remove(taskId: String, deleteFile: Boolean) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            val task = tasks[index]
            jobs[taskId]?.cancel()
            jobs.remove(taskId)
            if (deleteFile) {
                val uriStr = task.uri
                if (uriStr != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        MoteAppContextHolder.appContext?.contentResolver
                            ?.delete(Uri.parse(uriStr), null, null)
                    } catch (_: Exception) {}
                } else {
                    File(task.filePath).delete()
                }
            }
            tasks.removeAt(index)
            saveTasks()
        }
        MoteAppContextHolder.appContext?.let { updateNotification(it) }
    }

    fun retry(context: Context, taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            updateTask(taskId) { it.copy(status = MoteDownloadStatus.PENDING, downloadedBytes = 0L, downloadSpeed = 0L) }
            start(context, taskId)
        }
    }

    fun redownloadMissing(context: Context, taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            updateTask(taskId) {
                it.copy(
                    isRetryFromMissing = true,
                    failedAfterRetry = false,
                    isMissing = false,
                    status = MoteDownloadStatus.PENDING,
                    downloadedBytes = 0L,
                    downloadSpeed = 0L,
                )
            }
            start(context, taskId)
        }
    }

    fun updateMissingStatus(taskId: String, isMissing: Boolean) {
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx >= 0) {
            tasks[idx] = tasks[idx].copy(isMissing = isMissing)
            saveTasks()
        }
    }

    private fun updateTask(taskId: String, block: (MoteDownloadTask) -> MoteDownloadTask) {
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx >= 0) {
            val oldTask = tasks[idx]
            val newTask = block(oldTask)
            tasks[idx] = newTask
            if (oldTask.status != newTask.status || oldTask.isMissing != newTask.isMissing) {
                saveTasks()
            }
            if (oldTask.status != MoteDownloadStatus.COMPLETED &&
                newTask.status == MoteDownloadStatus.COMPLETED
            ) {
                MoteApplication.trackEventSafe(
                    "download_completed",
                    mapOf(
                        "mime_type" to newTask.mimeType.take(64),
                        "size_bytes" to newTask.downloadedBytes,
                    )
                )
            }
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "下载管理",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply { setShowBadge(false) }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    private fun registerReceiver(context: Context) {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction("com.torachen.mote.PAUSE_DOWNLOAD")
            addAction("com.torachen.mote.RESUME_DOWNLOAD")
            addAction("com.torachen.mote.CANCEL_DOWNLOAD")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                val appCtx = ctx?.applicationContext ?: return
                when (action) {
                    "com.torachen.mote.PAUSE_DOWNLOAD" -> {
                        tasks.forEach { if (it.status == MoteDownloadStatus.RUNNING) pause(it.id) }
                    }
                    "com.torachen.mote.RESUME_DOWNLOAD" -> {
                        tasks.forEach { if (it.status == MoteDownloadStatus.PAUSED) start(appCtx, it.id) }
                    }
                    "com.torachen.mote.CANCEL_DOWNLOAD" -> {
                        tasks.toList().forEach { remove(it.id, true) }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        receiverRegistered = true
    }

    private fun updateNotification(context: Context) {
        ensureChannel(context)
        registerReceiver(context)

        val prefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notification_bar_enabled", true)) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        }

        val activeTasks = tasks.filter { it.status == MoteDownloadStatus.RUNNING || it.status == MoteDownloadStatus.PAUSED }
        if (activeTasks.isEmpty()) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle("Mote 下载")
            .setContentText("正在处理 ${activeTasks.size} 个文件")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_downloads", true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pi)

        val isAnyRunning = activeTasks.any { it.status == MoteDownloadStatus.RUNNING }
        if (isAnyRunning) {
            val pauseIntent = Intent("com.torachen.mote.PAUSE_DOWNLOAD")
            val pausePi = PendingIntent.getBroadcast(context, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_pause, "暂停", pausePi)
        } else {
            val resumeIntent = Intent("com.torachen.mote.RESUME_DOWNLOAD")
            val resumePi = PendingIntent.getBroadcast(context, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "继续", resumePi)
        }

        val cancelIntent = Intent("com.torachen.mote.CANCEL_DOWNLOAD")
        val cancelPi = PendingIntent.getBroadcast(context, 3, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "取消", cancelPi)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) { }
    }
}

@Composable
private fun MonitorFileStatus(context: Context) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            val tasksToUpdate = mutableListOf<Pair<String, Boolean>>()
            withContext(Dispatchers.IO) {
                MoteDownloader.tasks.forEach { task ->
                    if (task.status == MoteDownloadStatus.COMPLETED) {
                        var exists = false
                        if (task.uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                val uri = Uri.parse(task.uri)
                                val cursor = context.contentResolver.query(
                                    uri, arrayOf(MediaStore.Downloads._ID), null, null, null
                                )
                                val recordExists = cursor?.use { it.moveToFirst() } ?: false

                                if (recordExists) {
                                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                                        val actualSize = pfd.statSize
                                        exists = actualSize > 0 && (task.downloadedBytes <= 0 || actualSize == task.downloadedBytes)
                                    }
                                }
                            } catch (_: Exception) {
                                exists = false
                            }
                        } else {
                            val f = File(task.filePath)
                            exists = f.exists() && f.length() > 0 && (task.downloadedBytes <= 0 || f.length() == task.downloadedBytes)
                        }
                        if (!exists && !task.isMissing) {
                            tasksToUpdate.add(task.id to true)
                        } else if (exists && task.isMissing) {
                            tasksToUpdate.add(task.id to false)
                        }
                    }
                }
            }
            tasksToUpdate.forEach { (id, isMissing) ->
                MoteDownloader.updateMissingStatus(id, isMissing)
            }
        }
    }
}

@Composable
fun DownloadManagerScreen(
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit = {},
    onOpenTaskDetail: () -> Unit = {},
    onOpenFileManager: () -> Unit = {},
) {
    val context = LocalContext.current
    LaunchedEffect(MoteDownloader.toastEvent) {
        val msg = MoteDownloader.toastEvent
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            MoteDownloader.toastEvent = null
        }
    }

    MonitorFileStatus(context)

    when (LocalUiMode.current) {
        UiMode.Miuix -> DownloadManagerScreenMiuix(onBack, onOpenDownloadSettings, onOpenTaskDetail, onOpenFileManager)
        UiMode.Material -> DownloadManagerScreenMaterial(onBack, onOpenDownloadSettings, onOpenTaskDetail, onOpenFileManager)
    }
}

// ============================================================
// MIUIX 版下载管理页
// ============================================================

@Composable
private fun DownloadManagerScreenMiuix(
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    onOpenFileManager: () -> Unit,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 忽略结果 */ }
    LaunchedEffect(Unit) {
        MoteDownloader.ensureLoaded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
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

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val contentTopPadding = if (topBarBottomPx > 0) {
        with(density) { topBarBottomPx.toDp() }
    } else {
        statusBarHeight + TOP_BAR_FALLBACK_HEIGHT
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "downloadBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var pendingDeleteIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLocalFile by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDataLimitDialog by remember { mutableStateOf(false) }

    val tasks = MoteDownloader.tasks

    val runningItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.RUNNING || it.status == MoteDownloadStatus.PENDING || it.status == MoteDownloadStatus.PAUSED } } }
    val completedItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.COMPLETED } } }
    val failedItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.FAILED } } }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun handleDeleteSelected() {
        val ids = pendingDeleteIds.toList()
        val shouldDeleteLocal = deleteLocalFile
        showDeleteDialog = false
        deleteLocalFile = false
        pendingDeleteIds = emptyList()
        exitMultiSelect()
        ids.forEach { MoteDownloader.remove(it, shouldDeleteLocal) }
        scope.launch {
            snackbarHostState.showSnackbar("已删除 ${ids.size} 项任务", duration = SnackbarDuration.Short)
        }
    }

    BackHandler(enabled = isMultiSelectMode) { exitMultiSelect() }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(modifier = Modifier.fillMaxSize().then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (tasks.isEmpty()) {
                                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 12.dp, bottom = 12.dp)) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = MiuixIcons.Download, contentDescription = null, tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(48.dp))
                                            Spacer(Modifier.height(12.dp))
                                            Text(text = "下载列表为空", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                        }
                                    }
                                }
                            } else {
                                if (runningItems.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("下载中 (${runningItems.size}个文件)")
                                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                            runningItems.forEachIndexed { index, item ->
                                                DownloadRunningItemMiuix(
                                                    item = item,
                                                    isFirst = index == 0,
                                                    isLast = index == runningItems.lastIndex,
                                                    isMultiSelectMode = isMultiSelectMode,
                                                    isSelected = item.id in selectedIds,
                                                    onClick = {
                                                        if (isMultiSelectMode) toggleSelect(item.id)
                                                        else {
                                                            PendingTaskDetailStore.pending = item
                                                            onOpenTaskDetail()
                                                        }
                                                    },
                                                    onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                                    onActionClick = {
                                                        if (isMultiSelectMode) toggleSelect(item.id)
                                                        else if (item.status == MoteDownloadStatus.RUNNING) MoteDownloader.pause(item.id)
                                                        else {
                                                            val isWifi = MoteDownloader.isWifiNetwork(context)
                                                            val limitIdx = prefs.getInt(KEY_DATA_LIMIT, DATA_LIMIT_DEFAULT)
                                                            if (!isWifi && limitIdx == 9) {
                                                                showDataLimitDialog = true
                                                            } else {
                                                                MoteDownloader.start(context, item.id)
                                                            }
                                                        }
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }

                                if (completedItems.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("已完成 (${completedItems.size}个文件)")
                                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                            completedItems.forEachIndexed { index, item ->
                                                DownloadRowItemMiuix(
                                                    item = item,
                                                    isFirst = index == 0,
                                                    isLast = index == completedItems.lastIndex,
                                                    isMultiSelectMode = isMultiSelectMode,
                                                    isSelected = item.id in selectedIds,
                                                    onClick = {
                                                        if (isMultiSelectMode) toggleSelect(item.id)
                                                        else {
                                                            PendingTaskDetailStore.pending = item
                                                            onOpenTaskDetail()
                                                        }
                                                    },
                                                    onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                                    onActionClick = { if (isMultiSelectMode) toggleSelect(item.id) },
                                                    onRetry = { MoteDownloader.redownloadMissing(context, item.id) },
                                                    onDelete = { MoteDownloader.remove(item.id, false) }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (failedItems.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SmallTitle("失败 (${failedItems.size}个文件)")
                                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                            failedItems.forEachIndexed { index, item ->
                                                DownloadRowItemMiuix(
                                                    item = item,
                                                    isFirst = index == 0,
                                                    isLast = index == failedItems.lastIndex,
                                                    isMultiSelectMode = isMultiSelectMode,
                                                    isSelected = item.id in selectedIds,
                                                    onClick = {
                                                        if (isMultiSelectMode) toggleSelect(item.id)
                                                        else {
                                                            PendingTaskDetailStore.pending = item
                                                            onOpenTaskDetail()
                                                        }
                                                    },
                                                    onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                                    onActionClick = { if (isMultiSelectMode) toggleSelect(item.id) else MoteDownloader.retry(context, item.id) },
                                                    onRetry = { MoteDownloader.retry(context, item.id) },
                                                    onDelete = { MoteDownloader.remove(item.id, false) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()))
                        }
                    }
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
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
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
                val barTitle = if (isMultiSelectMode) { if (selectedIds.isEmpty()) "请选择项目" else "已选择 ${selectedIds.size} 项" } else "下载管理"
                TopAppBar(
                    title = barTitle, largeTitle = barTitle, scrollBehavior = scrollBehavior, color = Color.Transparent, defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        if (isMultiSelectMode) {
                            IconButton(onClick = { exitMultiSelect() }) {
                                Icon(imageVector = MiuixIcons.Close, contentDescription = "退出多选", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        } else {
                            SettingsBackButton(
                                onBack = onBack,
                                progressiveTitleBlur = progressiveTitleBlur,
                                backCardAlpha = backCardAlpha,
                                backCardMaterialModifier = backCardMaterialModifier,
                                iconTint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        if (isMultiSelectMode) {
                            val allSelected = tasks.isNotEmpty() && selectedIds.size == tasks.size
                            SettingsTopBarAction(
                                backCardAlpha = backCardAlpha,
                                backCardMaterialModifier = backCardMaterialModifier,
                                onClick = {
                                    if (allSelected) {
                                        selectedIds.clear()
                                    } else {
                                        selectedIds.clear()
                                        selectedIds.addAll(tasks.map { it.id })
                                    }
                                },
                                contentDescription = "全选",
                                icon = MiuixIcons.SelectAll,
                                tint = if (allSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                            )
                        } else {
                            Box {
                                SettingsTopBarAction(
                                    backCardAlpha = backCardAlpha,
                                    backCardMaterialModifier = backCardMaterialModifier,
                                    onClick = { showMoreMenu = true },
                                    contentDescription = "更多",
                                    icon = MiuixIcons.More,
                                )
                                WindowCascadingListPopup(
                                    show = showMoreMenu,
                                    entries = listOf(
                                        DropdownEntry(
                                            items = listOf(
                                                DropdownItem(
                                                    text = "下载设置",
                                                    onClick = {
                                                        showMoreMenu = false
                                                        onOpenDownloadSettings()
                                                    }
                                                ),
                                                DropdownItem(
                                                    text = "打开下载目录",
                                                    onClick = {
                                                        showMoreMenu = false
                                                        openDownloadFolder(context)
                                                    }
                                                )
                                            )
                                        )
                                    ),
                                    onDismissRequest = { showMoreMenu = false }
                                )
                            }
                        }
                    }
                )
            }

            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            AnimatedVisibility(
                visible = !showDeleteDialog,
                enter = slideInVertically(tween(220), initialOffsetY = { it + 40 }) + fadeIn(tween(180)),
                exit = slideOutVertically(tween(200), targetOffsetY = { it + 40 }) + fadeOut(tween(150)),
                modifier = Modifier.align(Alignment.BottomEnd).zIndex(10f).padding(end = 24.dp, bottom = navBarBottom + 24.dp),
            ) {
                Row(
                    modifier = Modifier.height(56.dp).graphicsLayer {
                        shape = RoundedCornerShape(28.dp);
                        shadowElevation = 24.dp.toPx()
                        ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                        spotShadowColor = Color.Transparent
                    }.clip(RoundedCornerShape(28.dp)).then(
                        buildBackCardMaterialModifier(
                            materialStyle = materialStyle,
                            blurSupported = blurSupported,
                            backdrop = backdrop,
                            themeIsDark = themeIsDark,
                            shape = RoundedCornerShape(28.dp)
                        )
                    ).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isMultiSelectMode) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onOpenFileManager() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Folder,
                                contentDescription = "文件管理",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text("文件管理", fontSize = 11.sp, color = MiuixTheme.colorScheme.onSurface)
                        }
                    }
                    if (isMultiSelectMode) {
                    IconButton(onClick = {
                        val toShare = tasks.filter { it.id in selectedIds && it.status == MoteDownloadStatus.COMPLETED && !it.isMissing }
                        if (toShare.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("未选择可分享的文件", duration = SnackbarDuration.Short)
                            }
                        } else {
                            shareSelectedFiles(context, toShare)
                        }
                    }) {
                        Icon(imageVector = MiuixIcons.Share, contentDescription = "分享", tint = MiuixTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }

                    IconButton(onClick = { if (selectedIds.isNotEmpty()) { pendingDeleteIds = selectedIds.toList(); deleteLocalFile = false; showDeleteDialog = true } }) {
                        Icon(imageVector = MiuixIcons.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                    }
                }
            }

            SnackbarHost(state = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBarBottom + if (isMultiSelectMode) 100.dp else 12.dp))
        }
    }

    val dialogNavOwner = rememberNavigationEventDispatcherOwner()
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides dialogNavOwner) {
        WindowDialog(show = showDeleteDialog, title = "删除", onDismissRequest = { showDeleteDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "确定删除${pendingDeleteIds.size}条任务", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MiuixTheme.textStyles.body1, color = DialogDefaults.summaryColor())
                Row(modifier = Modifier.fillMaxWidth().clickable { deleteLocalFile = !deleteLocalFile }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(state = if (deleteLocalFile) ToggleableState.On else ToggleableState.Off, onClick = { deleteLocalFile = !deleteLocalFile })
                    Spacer(Modifier.width(10.dp))
                    Text(text = "同时删除本地文件", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { showDeleteDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface)) { Text("取消") }
                    Button(onClick = { handleDeleteSelected() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = DELETE_RED)) { Text("删除", color = DELETE_RED) }
                }
            }
        }

        WindowDialog(
            show = showDataLimitDialog,
            title = "下载限制",
            onDismissRequest = { showDataLimitDialog = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "当前处于数据网络，您开启了数据网络禁止下载的设置，请关闭后再继续下载。",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body1,
                    color = DialogDefaults.summaryColor(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            showDataLimitDialog = false
                            onOpenDownloadSettings()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("关闭") }
                }
            }
        }
    }
}

@Composable
fun DownloadTaskDetailScreen(
    onBack: () -> Unit
) {
    val task = PendingTaskDetailStore.pending
    if (task == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val context = LocalContext.current
    when (LocalUiMode.current) {
        UiMode.Miuix -> TaskDetailScreenMiuix(
            task = task,
            onBack = onBack,
            onOpen = { openDownloadedFile(context, task, onBack) }
        )
        UiMode.Material -> TaskDetailScreenMaterial(
            task = task,
            onBack = onBack,
            onOpen = { openDownloadedFile(context, task, onBack) }
        )
    }
}

@Composable
private fun TaskDetailScreenMiuix(
    task: MoteDownloadTask,
    onBack: () -> Unit,
    onOpen: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
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

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val contentTopPadding = if (topBarBottomPx > 0) {
        with(density) { topBarBottomPx.toDp() }
    } else {
        statusBarHeight + TOP_BAR_FALLBACK_HEIGHT
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "detailBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner()) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(modifier = Modifier.fillMaxSize().then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding, bottom = 100.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 12.dp),
                                colors = CardDefaults.defaultColors(
                                    color = MiuixTheme.colorScheme.surfaceContainer,
                                    contentColor = MiuixTheme.colorScheme.onSurface
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    // 1. 横栏
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = iconForMime(task.mimeType, task.fileName),
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.primary,
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = task.fileName.ifBlank { "未命名文件" },
                                            style = MiuixTheme.textStyles.body1,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    // 2. 信息项
                                    val labelStyle = MiuixTheme.textStyles.body1
                                    val valueStyle = MiuixTheme.textStyles.body2.copy(color = MiuixTheme.colorScheme.onSurfaceVariantSummary)

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("下载状态", style = labelStyle, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val statusText = when (task.status) {
                                            MoteDownloadStatus.PENDING -> "等待中"
                                            MoteDownloadStatus.RUNNING -> "下载中"
                                            MoteDownloadStatus.PAUSED -> "已暂停"
                                            MoteDownloadStatus.COMPLETED -> "已完成"
                                            MoteDownloadStatus.FAILED -> "下载失败"
                                        }
                                        Text(statusText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("文件大小", style = labelStyle, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val sizeText = if (task.totalBytes > 0) formatBytes(task.totalBytes) else formatBytes(task.downloadedBytes)
                                        Text(sizeText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("完成时间", style = labelStyle, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val timeText = if (task.status == MoteDownloadStatus.COMPLETED) formatDownloadTime(task.createTime) else "--"
                                        Text(timeText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("下载来源", style = labelStyle, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        Row(
                                            modifier = Modifier.clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("url", task.url))
                                                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Copy,
                                                contentDescription = "复制",
                                                tint = MiuixTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("复制", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = task.url,
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
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
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                Modifier.progressiveTextureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
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
                    title = "任务详情",
                    largeTitle = "任务详情",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable { onOpen() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "打开",
                    color = Color.White,
                    fontSize = MiuixTheme.textStyles.body1.fontSize,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DownloadRunningItemMiuix(
    item: MoteDownloadTask,
    isFirst: Boolean,
    isLast: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val contentColor = MiuixTheme.colorScheme.onSurface
    val secondaryColor = MiuixTheme.colorScheme.onSurfaceVariantSummary

    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 16.dp, end = 12.dp, top = if (isFirst) 14.dp else 12.dp, bottom = if (isLast) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconForMime(item.mimeType, item.fileName),
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName.ifBlank { "未命名文件" },
                style = MiuixTheme.textStyles.body1,
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
                    text = "${formatBytes(item.downloadedBytes)} / ${if (item.totalBytes > 0) formatBytes(item.totalBytes) else "未知大小"}",
                    style = MiuixTheme.textStyles.body2,
                    color = secondaryColor,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                if (item.downloadSpeed > 0) {
                    Text(
                        text = formatSpeed(item.downloadSpeed),
                        style = MiuixTheme.textStyles.body2,
                        color = secondaryColor,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = item.progress,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            )
        }

        Spacer(Modifier.width(12.dp))

        if (isMultiSelectMode) {
            Checkbox(
                state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                onClick = onActionClick
            )
        } else {
            Box(
                modifier = Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(MiuixTheme.colorScheme.secondaryContainer).clickable(onClick = onActionClick).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (item.status == MoteDownloadStatus.RUNNING) "暂停" else "继续",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DownloadRowItemMiuix(
    item: MoteDownloadTask,
    isFirst: Boolean,
    isLast: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
) {
    val isInvalid = item.isMissing
    val contentColor = if (isInvalid) Color.Gray else if (item.status == MoteDownloadStatus.FAILED && item.failedAfterRetry) Color.Gray else MiuixTheme.colorScheme.onSurface
    val secondaryColor = if (isInvalid) Color.Gray.copy(alpha = 0.7f) else MiuixTheme.colorScheme.onSurfaceVariantSummary
    val iconTint = if (isInvalid) Color.Gray else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 16.dp, end = 8.dp, top = if (isFirst) 14.dp else 10.dp, bottom = if (isLast) 14.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isInvalid) MiuixIcons.Close else iconForMime(item.mimeType, item.fileName),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName.ifBlank { "未命名文件" },
                style = MiuixTheme.textStyles.body1,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            if (isInvalid) {
                Text(
                    text = "文件已被第三方应用删除",
                    style = MiuixTheme.textStyles.body2,
                    color = secondaryColor,
                    maxLines = 1,
                )
            } else if (item.status == MoteDownloadStatus.FAILED && item.failedAfterRetry) {
                Text(
                    text = "重新下载失败 | ${formatDownloadTime(item.createTime)}",
                    style = MiuixTheme.textStyles.body2,
                    color = secondaryColor,
                    maxLines = 1,
                )
            } else if (item.status == MoteDownloadStatus.FAILED) {
                Text(
                    text = "下载失败 | ${formatDownloadTime(item.createTime)}",
                    style = MiuixTheme.textStyles.body2,
                    color = secondaryColor,
                    maxLines = 1,
                )
            } else {
                Text(
                    text = if (item.totalBytes > 0) "${formatBytes(item.totalBytes)} | ${formatDownloadTime(item.createTime)}" else "已完成 | ${formatDownloadTime(item.createTime)}",
                    style = MiuixTheme.textStyles.body2,
                    color = secondaryColor,
                    maxLines = 1,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        if (isMultiSelectMode) {
            Checkbox(
                state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                onClick = onActionClick
            )
        } else if (isInvalid) {
            Box(
                modifier = Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(MiuixTheme.colorScheme.primary).clickable(onClick = onRetry).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "重新下载",
                    style = MiuixTheme.textStyles.body2,
                    color = Color.White,
                )
            }
        } else if (item.status == MoteDownloadStatus.FAILED && item.failedAfterRetry) {
            IconButton(onClick = onDelete) {
                Icon(imageVector = MiuixIcons.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(20.dp))
            }
        } else if (item.status == MoteDownloadStatus.FAILED) {
            IconButton(onClick = onActionClick) {
                Icon(imageVector = MiuixIcons.Refresh, contentDescription = "重试", tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ============================================================
// Material 3 版下载管理页
// ============================================================

private fun downloadSegmentShapeFor(index: Int, count: Int): Shape {
    val big = DOWNLOAD_SEG_BIG_CORNER
    val small = DOWNLOAD_SEG_SMALL_CORNER
    return when {
        count <= 1 -> RoundedCornerShape(big)
        index == 0 -> RoundedCornerShape(topStart = big, topEnd = big, bottomStart = small, bottomEnd = small)
        index == count - 1 -> RoundedCornerShape(topStart = small, topEnd = small, bottomStart = big, bottomEnd = big)
        else -> RoundedCornerShape(small)
    }
}

@Composable
private fun downloadItemSurfaceColor(): Color = lerp(
    M3MaterialTheme.colorScheme.surfaceContainerLow,
    M3MaterialTheme.colorScheme.secondaryContainer,
    0.10f,
)

@Composable
private fun M3DownloadSectionHeader(text: String) {
    M3Text(
        text = text,
        color = M3MaterialTheme.colorScheme.primary,
        style = M3MaterialTheme.typography.titleSmall,
        modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DownloadManagerScreenMaterial(
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    onOpenFileManager: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pageBg = M3MaterialTheme.colorScheme.surface
    val navOwner = rememberNavigationEventDispatcherOwner()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 忽略结果 */ }
    LaunchedEffect(Unit) {
        MoteDownloader.ensureLoaded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var pendingDeleteIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLocalFile by remember { mutableStateOf(false) }
    val snackbarHostState = remember { M3SnackbarHostState() }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDataLimitDialog by remember { mutableStateOf(false) }

    val tasks = MoteDownloader.tasks

    val runningItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.RUNNING || it.status == MoteDownloadStatus.PENDING || it.status == MoteDownloadStatus.PAUSED } } }
    val completedItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.COMPLETED } } }
    val failedItems by remember { derivedStateOf { tasks.filter { it.status == MoteDownloadStatus.FAILED } } }

    fun exitMultiSelect() {
        isMultiSelectMode = false
        selectedIds.clear()
    }

    fun toggleSelect(id: String) {
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun handleDeleteSelected() {
        val ids = pendingDeleteIds.toList()
        val shouldDeleteLocal = deleteLocalFile
        showDeleteDialog = false
        deleteLocalFile = false
        pendingDeleteIds = emptyList()
        exitMultiSelect()
        ids.forEach { MoteDownloader.remove(it, shouldDeleteLocal) }
        scope.launch { snackbarHostState.showSnackbar("已删除 ${ids.size} 项任务", duration = M3SnackbarDuration.Short) }
    }

    BackHandler(enabled = isMultiSelectMode) { exitMultiSelect() }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        M3Scaffold(
            containerColor = pageBg,
            snackbarHost = { M3SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = {
                        M3Text(
                            if (isMultiSelectMode) {
                                if (selectedIds.isEmpty()) "请选择项目" else "已选择 ${selectedIds.size} 项"
                            } else "下载管理"
                        )
                    },
                    navigationIcon = {
                        M3IconButton(onClick = { if (isMultiSelectMode) exitMultiSelect() else onBack() }) {
                            M3Icon(imageVector = if (isMultiSelectMode) Icons.Filled.Close else MiuixIcons.Back, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (isMultiSelectMode) {
                            val allSelected = tasks.isNotEmpty() && selectedIds.size == tasks.size
                            M3IconButton(onClick = {
                                if (allSelected) {
                                    selectedIds.clear()
                                } else {
                                    selectedIds.clear()
                                    selectedIds.addAll(tasks.map { it.id })
                                }
                            }) {
                                M3Icon(imageVector = MiuixIcons.SelectAll, contentDescription = "全选")
                            }
                        } else {
                            Box {
                                M3IconButton(onClick = { showMoreMenu = true }) {
                                    M3Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "更多")
                                }
                                M3DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                    M3DropdownMenuItem(
                                        text = { M3Text("下载设置") },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenDownloadSettings()
                                        }
                                    )
                                    M3DropdownMenuItem(
                                        text = { M3Text("打开下载目录") },
                                        onClick = {
                                            showMoreMenu = false
                                            openDownloadFolder(context)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg),
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (tasks.isEmpty()) {
                        item(key = "empty") { M3DownloadEmptyState() }
                    } else {
                        if (runningItems.isNotEmpty()) {
                            item(key = "running_header") { M3DownloadSectionHeader("下载中 (${runningItems.size}个文件)") }
                            item(key = "running_list") {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING).padding(bottom = DOWNLOAD_GROUP_GAP), verticalArrangement = Arrangement.spacedBy(DOWNLOAD_SEG_GAP)) {
                                    val list = runningItems
                                    list.forEachIndexed { index, item ->
                                        M3DownloadRunningItem(
                                            item = item,
                                            shape = downloadSegmentShapeFor(index, list.size),
                                            isMultiSelectMode = isMultiSelectMode,
                                            isSelected = item.id in selectedIds,
                                            onClick = {
                                                if (isMultiSelectMode) toggleSelect(item.id)
                                                else {
                                                    PendingTaskDetailStore.pending = item
                                                    onOpenTaskDetail()
                                                }
                                            },
                                            onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                            onActionClick = {
                                                if (isMultiSelectMode) toggleSelect(item.id)
                                                else if (item.status == MoteDownloadStatus.RUNNING) MoteDownloader.pause(item.id)
                                                else {
                                                    val prefs = context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
                                                    val isWifi = MoteDownloader.isWifiNetwork(context)
                                                    val limitIdx = prefs.getInt(KEY_DATA_LIMIT, DATA_LIMIT_DEFAULT)
                                                    if (!isWifi && limitIdx == 9) {
                                                        showDataLimitDialog = true
                                                    } else {
                                                        MoteDownloader.start(context, item.id)
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        if (completedItems.isNotEmpty()) {
                            item(key = "completed_header") { M3DownloadSectionHeader("已完成 (${completedItems.size}个文件)") }
                            item(key = "completed_list") {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING).padding(bottom = DOWNLOAD_GROUP_GAP), verticalArrangement = Arrangement.spacedBy(DOWNLOAD_SEG_GAP)) {
                                    val list = completedItems
                                    list.forEachIndexed { index, item ->
                                        M3DownloadFinishedItem(
                                            item = item,
                                            shape = downloadSegmentShapeFor(index, list.size),
                                            isMultiSelectMode = isMultiSelectMode,
                                            isSelected = item.id in selectedIds,
                                            onClick = {
                                                if (isMultiSelectMode) toggleSelect(item.id)
                                                else {
                                                    PendingTaskDetailStore.pending = item
                                                    onOpenTaskDetail()
                                                }
                                            },
                                            onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                            onActionClick = { if (isMultiSelectMode) toggleSelect(item.id) },
                                            onRetry = { MoteDownloader.redownloadMissing(context, item.id) }
                                        )
                                    }
                                }
                            }
                        }

                        if (failedItems.isNotEmpty()) {
                            item(key = "failed_header") { M3DownloadSectionHeader("失败 (${failedItems.size}个文件)") }
                            item(key = "failed_list") {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING).padding(bottom = DOWNLOAD_GROUP_GAP), verticalArrangement = Arrangement.spacedBy(DOWNLOAD_SEG_GAP)) {
                                    val list = failedItems
                                    list.forEachIndexed { index, item ->
                                        M3DownloadFailedItem(
                                            item = item,
                                            shape = downloadSegmentShapeFor(index, list.size),
                                            isMultiSelectMode = isMultiSelectMode,
                                            isSelected = item.id in selectedIds,
                                            onClick = {
                                                if (isMultiSelectMode) toggleSelect(item.id)
                                                else {
                                                    PendingTaskDetailStore.pending = item
                                                    onOpenTaskDetail()
                                                }
                                            },
                                            onLongClick = { if (!isMultiSelectMode) { isMultiSelectMode = true; toggleSelect(item.id) } },
                                            onActionClick = { if (isMultiSelectMode) toggleSelect(item.id) else MoteDownloader.retry(context, item.id) },
                                            onDelete = { MoteDownloader.remove(item.id, false) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                AnimatedVisibility(
                    visible = !showDeleteDialog,
                    enter = slideInVertically(tween(220), initialOffsetY = { it + 40 }) + fadeIn(tween(180)),
                    exit = slideOutVertically(tween(200), targetOffsetY = { it + 40 }) + fadeOut(tween(150)),
                    modifier = Modifier.align(Alignment.BottomCenter).zIndex(10f).padding(bottom = navBarBottom + 24.dp).padding(horizontal = 24.dp),
                ) {
                    M3Surface(shape = RoundedCornerShape(28.dp), color = M3MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                        Row(modifier = Modifier.height(56.dp).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!isMultiSelectMode) {
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onOpenFileManager() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    M3Icon(Icons.Filled.Folder, contentDescription = "文件管理", modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.height(2.dp))
                                    M3Text("文件管理", fontSize = 11.sp)
                                }
                            }
                            if (isMultiSelectMode) {
                            M3IconButton(onClick = {
                                val toShare = tasks.filter { it.id in selectedIds && it.status == MoteDownloadStatus.COMPLETED && !it.isMissing }
                                if (toShare.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("未选择可分享的文件", duration = M3SnackbarDuration.Short) }
                                } else {
                                    shareSelectedFiles(context, toShare)
                                }
                            }) {
                                M3Icon(imageVector = MiuixIcons.Share, contentDescription = "分享", tint = M3MaterialTheme.colorScheme.onSurface)
                            }

                            M3IconButton(onClick = { if (selectedIds.isNotEmpty()) { pendingDeleteIds = selectedIds.toList(); deleteLocalFile = false; showDeleteDialog = true } }) {
                                M3Icon(imageVector = MiuixIcons.Delete, contentDescription = "删除", tint = M3MaterialTheme.colorScheme.onSurface)
                            }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        M3AlertDialog(
            onDismissRequest = { showDeleteDialog = false; deleteLocalFile = false; pendingDeleteIds = emptyList() },
            title = { M3Text("删除") },
            text = {
                Column {
                    M3Text("确定删除 ${pendingDeleteIds.size} 条任务")
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { deleteLocalFile = !deleteLocalFile }, verticalAlignment = Alignment.CenterVertically) {
                        M3Checkbox(checked = deleteLocalFile, onCheckedChange = { deleteLocalFile = it })
                        Spacer(Modifier.width(8.dp))
                        M3Text("同时删除本地文件")
                    }
                }
            },
            confirmButton = { M3TextButton(onClick = { handleDeleteSelected() }) { M3Text(text = "删除", color = M3MaterialTheme.colorScheme.error) } },
            dismissButton = { M3TextButton(onClick = { showDeleteDialog = false; deleteLocalFile = false; pendingDeleteIds = emptyList() }) { M3Text("取消") } },
        )
    }

    if (showDataLimitDialog) {
        M3AlertDialog(
            onDismissRequest = { showDataLimitDialog = false },
            title = { M3Text("下载限制") },
            text = { M3Text("当前处于数据网络，您开启了数据网络禁止下载的设置，请关闭后再继续下载。") },
            confirmButton = {
                M3TextButton(onClick = {
                    showDataLimitDialog = false
                    onOpenDownloadSettings()
                }) { M3Text("关闭") }
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailScreenMaterial(
    task: MoteDownloadTask,
    onBack: () -> Unit,
    onOpen: () -> Unit
) {
    val context = LocalContext.current
    val pageBg = M3MaterialTheme.colorScheme.surface

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner()) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("任务详情") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
                            M3Icon(imageVector = MiuixIcons.Back, contentDescription = "返回")
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            M3Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING)
                                    .padding(top = 12.dp, bottom = 12.dp),
                                shape = RoundedCornerShape(DOWNLOAD_SEG_BIG_CORNER),
                                color = downloadItemSurfaceColor(),
                                contentColor = M3MaterialTheme.colorScheme.onSurface
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                            M3Icon(
                                                imageVector = m3IconForMime(task.mimeType, task.fileName),
                                                contentDescription = null,
                                                tint = M3MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        M3Text(
                                            text = task.fileName.ifBlank { "未命名文件" },
                                            style = M3MaterialTheme.typography.bodyLarge,
                                            color = M3MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    val labelStyle = M3MaterialTheme.typography.bodyLarge
                                    val valueStyle = M3MaterialTheme.typography.bodyMedium.copy(color = M3MaterialTheme.colorScheme.onSurfaceVariant)

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        M3Text("下载状态", style = labelStyle, color = M3MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val statusText = when (task.status) {
                                            MoteDownloadStatus.PENDING -> "等待中"
                                            MoteDownloadStatus.RUNNING -> "下载中"
                                            MoteDownloadStatus.PAUSED -> "已暂停"
                                            MoteDownloadStatus.COMPLETED -> "已完成"
                                            MoteDownloadStatus.FAILED -> "下载失败"
                                        }
                                        M3Text(statusText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        M3Text("文件大小", style = labelStyle, color = M3MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val sizeText = if (task.totalBytes > 0) formatBytes(task.totalBytes) else formatBytes(task.downloadedBytes)
                                        M3Text(sizeText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        M3Text("完成时间", style = labelStyle, color = M3MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        val timeText = if (task.status == MoteDownloadStatus.COMPLETED) formatDownloadTime(task.createTime) else "--"
                                        M3Text(timeText, style = valueStyle)
                                    }
                                    Spacer(Modifier.height(24.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        M3Text("下载来源", style = labelStyle, color = M3MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.weight(1f))
                                        Row(
                                            modifier = Modifier.clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("url", task.url))
                                                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            M3Icon(
                                                imageVector = MiuixIcons.Copy,
                                                contentDescription = "复制",
                                                tint = M3MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            M3Text("复制", style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    M3Text(
                                        text = task.url,
                                        style = M3MaterialTheme.typography.bodyMedium,
                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.primary)
                        .clickable { onOpen() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "打开",
                        color = Color.White,
                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun M3DownloadRunningItem(
    item: MoteDownloadTask,
    shape: Shape,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    M3Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = shape,
        color = downloadItemSurfaceColor(),
        contentColor = M3MaterialTheme.colorScheme.onSurface,
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                WavyCircularProgress(
                    progress = if (item.totalBytes > 0) item.progress else -1f,
                    modifier = Modifier.fillMaxSize(),
                    color = M3MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp,
                    waveCount = 7,
                    waveAmplitudeFactor = 0.6f,
                )
                M3Icon(
                    imageVector = m3IconForMime(item.mimeType, item.fileName),
                    contentDescription = null,
                    tint = M3MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                M3Text(text = item.fileName.ifBlank { "未命名文件" }, style = M3MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    M3Text(
                        text = "${formatBytes(item.downloadedBytes)} / ${if (item.totalBytes > 0) formatBytes(item.totalBytes) else "未知"}",
                        style = M3MaterialTheme.typography.bodySmall,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Spacer(Modifier.weight(1f))
                    if (item.downloadSpeed > 0) {
                        M3Text(
                            text = formatSpeed(item.downloadSpeed),
                            style = M3MaterialTheme.typography.bodySmall,
                            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                M3LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                )
            }

            Spacer(Modifier.width(12.dp))

            if (isMultiSelectMode) {
                M3Checkbox(checked = isSelected, onCheckedChange = { onActionClick() })
            } else {
                M3Surface(onClick = onActionClick, shape = RoundedCornerShape(16.dp), color = M3MaterialTheme.colorScheme.secondaryContainer, contentColor = M3MaterialTheme.colorScheme.onSecondaryContainer) {
                    Box(modifier = Modifier.height(32.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        M3Text(
                            text = if (item.status == MoteDownloadStatus.RUNNING) "暂停" else "继续",
                            style = M3MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun M3DownloadFinishedItem(
    item: MoteDownloadTask,
    shape: Shape,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
    onRetry: () -> Unit,
) {
    val isInvalid = item.isMissing
    val contentColor = if (isInvalid) M3MaterialTheme.colorScheme.onSurfaceVariant else M3MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isInvalid) M3MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else M3MaterialTheme.colorScheme.onSurfaceVariant

    M3Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = shape,
        color = downloadItemSurfaceColor(),
        contentColor = contentColor,
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (isInvalid) {
                    M3Icon(imageVector = Icons.Rounded.LinkOff, contentDescription = null, tint = M3MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
                } else {
                    M3Icon(imageVector = m3IconForMime(item.mimeType, item.fileName), contentDescription = null, tint = M3MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                M3Text(text = item.fileName.ifBlank { "未命名文件" }, style = M3MaterialTheme.typography.bodyLarge, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                if (isInvalid) {
                    M3Text(text = "文件已被第三方应用删除", style = M3MaterialTheme.typography.bodySmall, color = secondaryColor, maxLines = 1)
                } else {
                    M3Text(text = if (item.totalBytes > 0) "${formatBytes(item.totalBytes)} | ${formatDownloadTime(item.createTime)}" else "已完成 | ${formatDownloadTime(item.createTime)}", style = M3MaterialTheme.typography.bodySmall, color = secondaryColor, maxLines = 1)
                }
            }
            Spacer(Modifier.width(8.dp))

            if (isMultiSelectMode) {
                M3Checkbox(checked = isSelected, onCheckedChange = { onActionClick() })
            } else if (isInvalid) {
                M3Surface(onClick = onRetry, shape = RoundedCornerShape(16.dp), color = M3MaterialTheme.colorScheme.primary, contentColor = M3MaterialTheme.colorScheme.onPrimary) {
                    Box(modifier = Modifier.height(32.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        M3Text("重新下载", style = M3MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun M3DownloadFailedItem(
    item: MoteDownloadTask,
    shape: Shape,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val isRetryFailed = item.failedAfterRetry
    val contentColor = if (isRetryFailed) M3MaterialTheme.colorScheme.onSurfaceVariant else M3MaterialTheme.colorScheme.onSurface

    M3Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = shape,
        color = downloadItemSurfaceColor(),
        contentColor = contentColor,
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                M3Icon(imageVector = Icons.Rounded.ErrorOutline, contentDescription = null, tint = M3MaterialTheme.colorScheme.error, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                M3Text(text = item.fileName.ifBlank { "未命名文件" }, style = M3MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                M3Text(
                    text = if (isRetryFailed) "重新下载失败 | ${formatDownloadTime(item.createTime)}" else "下载失败 | ${formatDownloadTime(item.createTime)}",
                    style = M3MaterialTheme.typography.bodySmall,
                    color = if (isRetryFailed) M3MaterialTheme.colorScheme.onSurfaceVariant else M3MaterialTheme.colorScheme.error,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            if (isMultiSelectMode) {
                M3Checkbox(checked = isSelected, onCheckedChange = { onActionClick() })
            } else if (isRetryFailed) {
                M3IconButton(onClick = onDelete) {
                    M3Icon(imageVector = Icons.Filled.Delete, contentDescription = "删除", tint = M3MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            } else {
                M3IconButton(onClick = onActionClick) {
                    M3Icon(imageVector = Icons.Filled.Refresh, contentDescription = "重试", tint = M3MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun M3DownloadEmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING).padding(top = 24.dp, bottom = 24.dp)) {
        M3Surface(shape = RoundedCornerShape(DOWNLOAD_SEG_BIG_CORNER), color = downloadItemSurfaceColor(), contentColor = M3MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    M3Icon(imageVector = MiuixIcons.Download, contentDescription = null, tint = M3MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    M3Text(text = "下载列表为空", style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ============================================================
// 辅助工具与函数
// ============================================================

private fun openDownloadFolder(context: Context) {
    val docUri = DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        "primary:Download/Mote"
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(docUri, DocumentsContract.Document.MIME_TYPE_DIR)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    val pm = context.packageManager
    val activities = pm.queryIntentActivities(intent, 0)

    if (activities.isNotEmpty()) {
        val systemFileManagers = listOf(
            "com.android.documentsui",
            "com.google.android.documentsui",
            "com.android.fileexplorer",
            "com.miui.fileexplorer",
            "com.huawei.filemanager",
            "com.oppo.filemanager",
            "com.vivo.filemanager",
            "com.coloros.filemanager"
        )

        val targetInfo = activities.firstOrNull {
            it.activityInfo.packageName in systemFileManagers
        } ?: activities.firstOrNull {
            val pkg = it.activityInfo.packageName
            !pkg.contains("quark") && !pkg.contains("baidu") &&
                    !pkg.contains("tencent") && !pkg.contains("alipan") &&
                    !pkg.contains("115") && !pkg.contains("uc") &&
                    pkg != "com.android.providers.downloads.ui"
        }

        if (targetInfo != null) {
            try {
                intent.setClassName(targetInfo.activityInfo.packageName, targetInfo.activityInfo.name)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
            }
        }
    }

    Toast.makeText(context, "无法打开文件管理器\n路径已复制到剪贴板，请手动前往", Toast.LENGTH_LONG).show()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("path", "Download/Mote"))
}

private fun shareSelectedFiles(context: Context, tasks: List<MoteDownloadTask>) {
    val validTasks = tasks.filter { !it.isMissing }
    if (validTasks.isEmpty()) return

    val mimeTypes = validTasks.map { it.mimeType }.distinct()
    if (mimeTypes.size > 1) {
        Toast.makeText(context, "只能分享同类型文件", Toast.LENGTH_SHORT).show()
        return
    }

    val mimeType = mimeTypes.first().ifBlank { "*/*" }

    try {
        val uris = ArrayList<Uri>()
        val authority = "${context.packageName}.fileprovider"
        validTasks.forEach { task ->
            val uriStr = task.uri
            val uri = if (uriStr != null) {
                Uri.parse(uriStr)
            } else {
                FileProvider.getUriForFile(context, authority, File(task.filePath))
            }
            uris.add(uri)
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "分享文件").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openDownloadedFile(context: Context, item: MoteDownloadTask, onBack: () -> Unit) {
    val fileName = item.fileName.lowercase()
    val mime = item.mimeType.lowercase()

    val localFile: File? = if (item.uri == null) File(item.filePath) else null
    val contentUri: Uri = if (item.uri != null) {
        Uri.parse(item.uri)
    } else {
        val f = localFile
        if (f == null || !f.exists()) {
            Toast.makeText(context, "文件已不存在", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, f)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开文件：${e.message}", Toast.LENGTH_SHORT).show()
            return
        }
    }

    val isHlsOrDash = fileName.endsWith(".m3u8") || fileName.endsWith(".mpd") ||
            mime.contains("mpegurl") || mime.contains("dash+xml")

    if (isHlsOrDash) {
        try {
            NativePlayerActivity.start(context, url = item.url, title = item.fileName, fromWeb = false)
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

    if (isVideo) {
        try {
            NativePlayerActivity.start(context, url = contentUri.toString(), title = item.fileName, fromWeb = false)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开视频：${e.message}", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (isHtml) {
        PendingOpenInNewWindowStore.pending = contentUri.toString()
        onBack()
        return
    }

    try {
        var openMime = mime
        if (openMime.isBlank() || openMime == "application/octet-stream" || openMime == "*/*") {
            val ext = fileName.substringAfterLast('.', "")
            if (ext.isNotBlank()) {
                openMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
            }
        }

        val isApk = fileName.endsWith(".apk") || openMime == "application/vnd.android.package-archive"
        if (isApk) {
            openMime = "application/vnd.android.package-archive"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "请先允许安装未知来源应用", Toast.LENGTH_LONG).show()
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, openMime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

    } catch (e: Exception) {
        Toast.makeText(context, "无法打开文件：${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun iconForMime(mime: String, fileName: String): ImageVector {
    val lower = (mime + " " + fileName).lowercase()
    return when {
        lower.contains("video") || lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".webm") -> MiuixIcons.Play
        lower.contains("audio") || lower.contains(".mp3") || lower.contains(".m4a") || lower.contains(".flac") -> MiuixIcons.Music
        lower.contains("image") || lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".gif") || lower.contains(".webp") -> MiuixIcons.Photos
        lower.contains("html") || lower.contains(".htm") -> MiuixIcons.File
        lower.contains("zip") || lower.contains("rar") || lower.contains(".7z") || lower.contains("tar") -> MiuixIcons.Folder
        else -> MiuixIcons.File
    }
}

private fun m3IconForMime(mime: String, fileName: String): ImageVector {
    val lower = (mime + " " + fileName).lowercase()
    return when {
        lower.contains("video") || lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".webm") || lower.contains(".mov") || lower.contains(".avi") || lower.contains(".flv") || lower.contains(".m4v") || lower.contains(".wmv") || lower.contains(".3gp") -> Icons.Rounded.Movie
        lower.contains("audio") || lower.contains(".mp3") || lower.contains(".m4a") || lower.contains(".flac") || lower.contains(".wav") || lower.contains(".ogg") || lower.contains(".aac") || lower.contains(".opus") -> Icons.Rounded.MusicNote
        lower.contains("image") || lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".gif") || lower.contains(".webp") || lower.contains(".bmp") || lower.contains(".svg") -> Icons.Rounded.Image
        lower.contains("html") || lower.contains(".htm") || lower.contains(".xhtml") -> Icons.Rounded.Description
        lower.contains("zip") || lower.contains("rar") || lower.contains(".7z") || lower.contains("tar") || lower.contains(".gz") || lower.contains(".bz2") -> Icons.Rounded.FolderZip
        else -> Icons.Rounded.InsertDriveFile
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0L) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB/s", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.2f MB/s", mb)
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

private fun formatDownloadTime(ms: Long): String {
    if (ms <= 0L) return "未知时间"
    return try {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = ms }
        val nowDay = Calendar.getInstance().apply { timeInMillis = now.timeInMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val targetDay = Calendar.getInstance().apply { timeInMillis = target.timeInMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val diffDays = ((nowDay.timeInMillis - targetDay.timeInMillis) / (24L * 3600L * 1000L)).toInt()
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
    } catch (_: Exception) { "未知时间" }
}

@Composable
private fun WavyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 2.5.dp,
    waveCount: Int = 7,
    waveAmplitudeFactor: Float = 0.6f,
    animationDurationMs: Int = 800,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = animationDurationMs, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "wavyPhase",
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(x = w / 2f, y = h / 2f)
        val strokePx = strokeWidth.toPx()
        val totalMargin = strokePx * 0.5f + strokePx * waveAmplitudeFactor
        val baseRadius = (minOf(w, h) / 2f - totalMargin).coerceAtLeast(1f)
        val amplitude = strokePx * waveAmplitudeFactor

        val effectiveProgress = if (progress < 0f) 0.75f else progress.coerceIn(0f, 1f)
        if (effectiveProgress > 0.005f) {
            val sweepDeg = 360f * effectiveProgress
            val progressPath = buildWavyArcPath(
                center = center, baseRadius = baseRadius, amplitude = amplitude,
                waveCount = waveCount, startAngleDeg = -90f, sweepAngleDeg = sweepDeg, phase = phase,
            )
            drawPath(path = progressPath, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

private fun buildWavyArcPath(
    center: Offset, baseRadius: Float, amplitude: Float, waveCount: Int,
    startAngleDeg: Float, sweepAngleDeg: Float, phase: Float,
): Path {
    val path = Path()
    val absSweep = kotlin.math.abs(sweepAngleDeg)
    val steps = maxOf(2, (absSweep / 2f).toInt())
    val direction = if (sweepAngleDeg >= 0f) 1f else -1f
    val waveCountF = waveCount.toFloat()

    for (i in 0..steps) {
        val t = i.toFloat() / steps
        val deg = startAngleDeg + direction * absSweep * t
        val rad = (deg * Math.PI / 180.0).toFloat()
        val theta = ((deg + 90f) * Math.PI / 180.0).toFloat()
        val sinValue = kotlin.math.sin((waveCountF * theta + phase).toDouble()).toFloat()
        val r = baseRadius + amplitude * sinValue
        val x = center.x + r * kotlin.math.cos(rad.toDouble()).toFloat()
        val y = center.y + r * kotlin.math.sin(rad.toDouble()).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

// ============================================================
// 下载设置页面
// ============================================================

@Composable
fun DownloadSettingsScreen(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> DownloadSettingsMiuix(onBack)
        UiMode.Material -> DownloadSettingsMaterial(onBack)
    }
}

@Composable
private fun DownloadSettingsMiuix(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
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
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val contentTopPadding = if (topBarBottomPx > 0) {
        with(density) { topBarBottomPx.toDp() }
    } else {
        statusBarHeight + TOP_BAR_FALLBACK_HEIGHT
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "settingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    var dataLimitIndex by remember { mutableIntStateOf(prefs.getInt(KEY_DATA_LIMIT, DATA_LIMIT_DEFAULT)) }
    var speedLimitIndex by remember { mutableIntStateOf(prefs.getInt(KEY_SPEED_LIMIT, SPEED_LIMIT_DEFAULT)) }

    val dataLimitEntries = remember(dataLimitIndex) {
        listOf(DropdownEntry(items = DATA_LIMIT_OPTIONS.mapIndexed { index, s ->
            DropdownItem(text = s, selected = dataLimitIndex == index, onClick = {
                dataLimitIndex = index
                prefs.edit().putInt(KEY_DATA_LIMIT, index).apply()
            })
        }))
    }

    val speedLimitEntries = remember(speedLimitIndex) {
        listOf(DropdownEntry(items = SPEED_LIMIT_OPTIONS.mapIndexed { index, s ->
            DropdownItem(text = s, selected = speedLimitIndex == index, onClick = {
                speedLimitIndex = index
                prefs.edit().putInt(KEY_SPEED_LIMIT, index).apply()
            })
        }))
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 12.dp),
                            ) {
                                WindowDropdownPreference(
                                    title = "数据网络限制",
                                    entries = dataLimitEntries,
                                    collapseOnSelection = true,
                                )
                                WindowDropdownPreference(
                                    title = "下载速度限制",
                                    entries = speedLimitEntries,
                                    collapseOnSelection = true,
                                )
                            }

                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }

                SettingsPageAutoScroll(
                    lazyListState = listState,
                    pageRoute = "download_settings",
                    contentTopPadding = contentTopPadding,
                )
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
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
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
                    title = "下载设置",
                    largeTitle = "下载设置",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DownloadSettingsMaterial(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val pageBg = M3MaterialTheme.colorScheme.surface
    val navOwner = rememberNavigationEventDispatcherOwner()

    var dataLimitIndex by remember { mutableIntStateOf(prefs.getInt(KEY_DATA_LIMIT, DATA_LIMIT_DEFAULT)) }
    var speedLimitIndex by remember { mutableIntStateOf(prefs.getInt(KEY_SPEED_LIMIT, SPEED_LIMIT_DEFAULT)) }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("下载设置") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
                            M3Icon(imageVector = MiuixIcons.Back, contentDescription = "返回")
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "content") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DOWNLOAD_LIST_HORIZONTAL_PADDING)
                                .padding(top = 12.dp, bottom = DOWNLOAD_GROUP_GAP)
                        ) {
                            MoteSegmentedColumn(
                                content = listOf(
                                    {
                                        M3SegmentDropdownItem(
                                            title = "数据网络限制",
                                            summary = null,
                                            options = DATA_LIMIT_OPTIONS,
                                            selectedIndex = dataLimitIndex,
                                            onSelected = {
                                                dataLimitIndex = it
                                                prefs.edit().putInt(KEY_DATA_LIMIT, it).apply()
                                            }
                                        )
                                    },
                                    {
                                        M3SegmentDropdownItem(
                                            title = "下载速度限制",
                                            summary = null,
                                            options = SPEED_LIMIT_OPTIONS,
                                            selectedIndex = speedLimitIndex,
                                            onSelected = {
                                                speedLimitIndex = it
                                                prefs.edit().putInt(KEY_SPEED_LIMIT, it).apply()
                                            }
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
