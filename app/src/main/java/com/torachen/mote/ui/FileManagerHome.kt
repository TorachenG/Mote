package com.torachen.mote.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

internal enum class FmTool { COMPRESS, EXTRACT, CONVERT, OCR, QQ, WECHAT, SCAN, TRASH }

internal data class FmToolEntry(
    val tool: FmTool,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val remoteIcon: String? = null,
    val usePrimary: Boolean = false,
    val whiteMask: Boolean = false,
)

internal val fmTools = listOf(
    FmToolEntry(FmTool.COMPRESS, "文件压缩", Icons.Rounded.FolderZip, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.EXTRACT, "文件解压", Icons.Rounded.FolderOpen, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.CONVERT, "格式转换", Icons.Rounded.SwapHoriz, Color(0xFF6B7A99)),
    FmToolEntry(FmTool.OCR, "提取文字", Icons.Rounded.TextFields, Color(0xFF7767F9)),
    FmToolEntry(
        FmTool.QQ, "QQ文件", Icons.Rounded.Chat, Color(0xFF12B7F5),
        remoteIcon = "https://img.icons8.com/color/96/qq.png",
        usePrimary = true,
        whiteMask = true,
    ),
    FmToolEntry(
        FmTool.WECHAT, "微信文件", Icons.Rounded.Forum, Color(0xFF1DCD3A),
        remoteIcon = "https://img.icons8.com/color/96/wechat.png",
    ),
    FmToolEntry(FmTool.SCAN, "拍照扫描", Icons.Rounded.QrCodeScanner, Color(0xFF16C060)),
    FmToolEntry(FmTool.TRASH, "回收站", Icons.Rounded.Delete, Color(0xFFE8452C)),
)

internal data class FmCategoryEntry(
    val category: FileCategory,
    val icon: ImageVector,
    val color: Color,
    val usePrimary: Boolean = false,
)

internal val fmCategories = listOf(
    FmCategoryEntry(FileCategory.DOCUMENT, Icons.Rounded.Description, Color(0xFFFFD966)),
    FmCategoryEntry(FileCategory.IMAGE, Icons.Rounded.Image, Color(0xFF7767F9)),
    FmCategoryEntry(FileCategory.VIDEO, Icons.Rounded.Movie, Color(0xFF7767F9)),
    FmCategoryEntry(FileCategory.AUDIO, Icons.Rounded.MusicNote, Color(0xFFE8452C)),
    FmCategoryEntry(FileCategory.ARCHIVE, Icons.Rounded.FolderZip, Color(0xFFFFFFFF), usePrimary = true),
    FmCategoryEntry(FileCategory.APK, Icons.Rounded.Android, Color(0xFF1DCD3A)),
)

internal fun fmStorageUsage(): Pair<Long, Long> {
    return try {
        val stat = android.os.StatFs(android.os.Environment.getExternalStorageDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        (total - free).coerceAtLeast(0L) to total
    } catch (_: Throwable) {
        0L to 0L
    }
}

internal fun fmFormatGb(bytes: Long): String {
    if (bytes <= 0L) return "0"
    val gb = bytes / 1024.0 / 1024.0 / 1024.0
    return if (gb >= 100) String.format("%.0f", gb) else String.format("%.1f", gb)
}

private val remoteIconCache = ConcurrentHashMap<String, ImageBitmap?>()

@Composable
private fun rememberRemoteIcon(url: String): ImageBitmap? {
    val state = produceState<ImageBitmap?>(initialValue = remoteIconCache[url], key1 = url) {
        if (remoteIconCache.containsKey(url)) return@produceState
        val bmp = withContext(Dispatchers.IO) {
            runCatching {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                }
                conn.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
        // ConcurrentHashMap 不允许 null value
        if (bmp != null) remoteIconCache[url] = bmp
        value = bmp
    }
    return state.value
}

/** 主页卡片头部，嵌在根目录列表顶部 */
@Composable
internal fun FileManagerHomeHeaderMiuix(
    controller: FileManagerController,
    onTool: (FmTool) -> Unit,
) {
    val onSurface = MiuixTheme.colorScheme.onSurface
    val storage = remember { fmStorageUsage() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(268.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.weight(2.1f).fillMaxHeight()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    Text(text = "实用功能", fontSize = 15.sp, color = onSurface)
                    Spacer(Modifier.height(12.dp))
                    fmTools.chunked(3).forEachIndexed { rowIndex, row ->
                        if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            row.forEach { tool ->
                                FmToolCellMiuix(
                                    tool = tool,
                                    modifier = Modifier.width(40.dp),
                                    onSurface = onSurface,
                                    onClick = { onTool(tool.tool) },
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.width(40.dp)) }
                        }
                    }
                }
            }
            FmStorageCardMiuix(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                used = storage.first,
                total = storage.second,
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                fmCategories.forEach { entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(40.dp)
                            .clickable { controller.openCategory(entry.category) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (entry.usePrimary) MiuixTheme.colorScheme.primary else entry.color,
                                    RoundedCornerShape(10.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = entry.category.label,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = entry.category.label,
                            fontSize = 9.sp,
                            color = onSurface,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FmToolCellMiuix(
    tool: FmToolEntry,
    modifier: Modifier,
    onSurface: Color,
    onClick: () -> Unit,
) {
    val remote = tool.remoteIcon?.let { rememberRemoteIcon(it) }
    val bg = if (tool.usePrimary) MiuixTheme.colorScheme.primary else tool.color
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (remote != null) {
                Image(
                    bitmap = remote,
                    contentDescription = tool.label,
                    contentScale = ContentScale.Fit,
                    colorFilter = if (tool.whiteMask) {
                        androidx.compose.ui.graphics.ColorFilter.tint(Color.White, androidx.compose.ui.graphics.BlendMode.SrcIn)
                    } else null,
                    modifier = Modifier.size(26.dp),
                )
            } else {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = tool.label,
            fontSize = 9.sp,
            color = onSurface,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun FmStorageCardMiuix(
    modifier: Modifier,
    used: Long,
    total: Long,
) {
    val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
    val primary = MiuixTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEBEBEB)),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((h * fraction).coerceAtLeast(8.dp))
                    .align(Alignment.BottomCenter)
                    .background(primary.copy(alpha = 0.25f)),
            )
        }
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Spacer(Modifier.weight(1f))
            Text(text = "已用", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = fmFormatGb(used),
                    fontSize = 24.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Column(modifier = Modifier.padding(start = 2.dp)) {
                    Text(
                        text = "GB",
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        text = "/${fmFormatGb(total)}GB",
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

/* ---------------- Material --------------- */

@Composable
internal fun FileManagerHomeHeaderMaterial(
    controller: FileManagerController,
    onTool: (FmTool) -> Unit,
) {
    val scheme = M3MaterialTheme.colorScheme
    val storage = remember { fmStorageUsage() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(268.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            M3Card(modifier = Modifier.weight(2.1f).fillMaxHeight()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    M3Text(text = "实用功能", fontSize = 15.sp, color = scheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    fmTools.chunked(3).forEachIndexed { rowIndex, row ->
                        if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            row.forEach { tool ->
                                FmToolCellMaterial(
                                    tool = tool,
                                    modifier = Modifier.width(40.dp),
                                    scheme = scheme,
                                    onClick = { onTool(tool.tool) },
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.width(40.dp)) }
                        }
                    }
                }
            }
            FmStorageCardMaterial(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                used = storage.first,
                total = storage.second,
            )
        }

        Spacer(Modifier.height(12.dp))

        M3Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                fmCategories.forEach { entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(40.dp)
                            .clickable { controller.openCategory(entry.category) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (entry.usePrimary) scheme.primary else entry.color,
                                    RoundedCornerShape(10.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            M3Icon(
                                imageVector = entry.icon,
                                contentDescription = entry.category.label,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        M3Text(
                            text = entry.category.label,
                            fontSize = 9.sp,
                            color = scheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FmToolCellMaterial(
    tool: FmToolEntry,
    modifier: Modifier,
    scheme: androidx.compose.material3.ColorScheme,
    onClick: () -> Unit,
) {
    val remote = tool.remoteIcon?.let { rememberRemoteIcon(it) }
    val bg = if (tool.usePrimary) scheme.primary else tool.color
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (remote != null) {
                Image(
                    bitmap = remote,
                    contentDescription = tool.label,
                    contentScale = ContentScale.Fit,
                    colorFilter = if (tool.whiteMask) {
                        androidx.compose.ui.graphics.ColorFilter.tint(Color.White, androidx.compose.ui.graphics.BlendMode.SrcIn)
                    } else null,
                    modifier = Modifier.size(26.dp),
                )
            } else {
                M3Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        M3Text(
            text = tool.label,
            fontSize = 11.sp,
            color = scheme.onSurface,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun FmStorageCardMaterial(
    modifier: Modifier,
    used: Long,
    total: Long,
) {
    val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
    val scheme = M3MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEBEBEB)),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((h * fraction).coerceAtLeast(8.dp))
                    .align(Alignment.BottomCenter)
                    .background(scheme.primary.copy(alpha = 0.25f)),
            )
        }
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Spacer(Modifier.weight(1f))
            M3Text(text = "已用", fontSize = 12.sp, color = scheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                M3Text(text = fmFormatGb(used), fontSize = 24.sp, color = scheme.onSurface)
                Column(modifier = Modifier.padding(start = 2.dp)) {
                    M3Text(text = "GB", fontSize = 9.sp, lineHeight = 11.sp, color = scheme.onSurfaceVariant)
                    M3Text(text = "/${fmFormatGb(total)}GB", fontSize = 9.sp, lineHeight = 11.sp, color = scheme.onSurfaceVariant)
                }
            }
        }
    }
}
