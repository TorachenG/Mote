package com.torachen.mote.ui

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 文件管理主页上的工具入口 */
internal enum class FmTool {
    COMPRESS, EXTRACT, CONVERT, OCR, QQ, WECHAT, SCAN, TRASH
}

internal data class FmToolEntry(
    val tool: FmTool,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val enabled: Boolean,
)

internal val fmTools = listOf(
    FmToolEntry(FmTool.COMPRESS, "文件压缩", Icons.Rounded.FolderZip, Color(0xFF3B6EF6), true),
    FmToolEntry(FmTool.EXTRACT, "文件解压", Icons.Rounded.FolderOpen, Color(0xFF3B6EF6), true),
    FmToolEntry(FmTool.CONVERT, "格式转换", Icons.Rounded.SwapHoriz, Color(0xFF9AA0A6), false),
    FmToolEntry(FmTool.OCR, "提取文字", Icons.Rounded.TextFields, Color(0xFF9AA0A6), false),
    FmToolEntry(FmTool.QQ, "QQ文件", Icons.Rounded.Chat, Color(0xFF9AA0A6), false),
    FmToolEntry(FmTool.WECHAT, "微信文件", Icons.Rounded.Forum, Color(0xFF9AA0A6), false),
    FmToolEntry(FmTool.SCAN, "拍照扫描", Icons.Rounded.QrCodeScanner, Color(0xFF16C060), true),
    FmToolEntry(FmTool.TRASH, "回收站", Icons.Rounded.Delete, Color(0xFFE8452C), true),
)

internal data class FmCategoryEntry(
    val category: FileCategory,
    val icon: ImageVector,
    val color: Color,
)

internal val fmCategories = listOf(
    FmCategoryEntry(FileCategory.DOCUMENT, Icons.Rounded.Description, Color(0xFFF5C043)),
    FmCategoryEntry(FileCategory.IMAGE, Icons.Rounded.Image, Color(0xFF8B5CF6)),
    FmCategoryEntry(FileCategory.VIDEO, Icons.Rounded.Movie, Color(0xFF7C4DFF)),
    FmCategoryEntry(FileCategory.AUDIO, Icons.Rounded.MusicNote, Color(0xFFE8452C)),
    FmCategoryEntry(FileCategory.ARCHIVE, Icons.Rounded.FolderZip, Color(0xFF3B6EF6)),
    FmCategoryEntry(FileCategory.APK, Icons.Rounded.Android, Color(0xFF16C060)),
)

/** 已用 / 总量（字节） */
internal fun fmStorageUsage(): Pair<Long, Long> {
    return try {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
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

@Composable
internal fun FileManagerHomeMiuix(
    controller: FileManagerController,
    contentTopPadding: Dp,
    onOpenList: () -> Unit,
    onTool: (FmTool) -> Unit,
) {
    val surface = MiuixTheme.colorScheme.surface
    val cardColor = MiuixTheme.colorScheme.surfaceContainer
    val onSurface = MiuixTheme.colorScheme.onSurface
    val summary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val storage = remember { fmStorageUsage() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = contentTopPadding, bottom = 96.dp),
    ) {
        item(key = "home") {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 实用功能 + 存储空间
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(modifier = Modifier.weight(1.35f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(text = "实用功能", fontSize = 16.sp, color = onSurface)
                            Spacer(Modifier.height(12.dp))
                            fmTools.chunked(4).forEachIndexed { rowIndex, row ->
                                if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    row.forEach { tool ->
                                        FmToolCellMiuix(
                                            tool = tool,
                                            modifier = Modifier.weight(1f),
                                            cardColor = cardColor,
                                            onSurface = onSurface,
                                            summary = summary,
                                            onClick = { onTool(tool.tool) },
                                        )
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    Card(modifier = Modifier.weight(0.65f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "存储空间", fontSize = 16.sp, color = onSurface)
                            Spacer(Modifier.height(12.dp))
                            val used = storage.first
                            val total = storage.second
                            val fraction = if (total > 0) (used.toFloat() / total) else 0f
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(150.dp)
                                    .background(
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((150 * fraction).dp.coerceAtLeast(4.dp))
                                        .background(
                                            MiuixTheme.colorScheme.primary.copy(alpha = 0.38f),
                                            RoundedCornerShape(12.dp),
                                        )
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(text = "已用", fontSize = 12.sp, color = summary)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${fmFormatGb(used)} GB / ${fmFormatGb(total)} GB",
                                fontSize = 11.sp,
                                color = summary,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 分类入口
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        fmCategories.forEach { entry ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { controller.openCategory(entry.category) },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(entry.color, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = entry.icon,
                                        contentDescription = entry.category.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(text = entry.category.label, fontSize = 11.sp, color = onSurface)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FmToolCellMiuix(
    tool: FmToolEntry,
    modifier: Modifier,
    cardColor: Color,
    onSurface: Color,
    summary: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .then(if (tool.enabled) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = if (tool.enabled) tool.color else cardColor,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.label,
                tint = if (tool.enabled) Color.White else summary.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = tool.label,
            fontSize = 11.sp,
            color = if (tool.enabled) onSurface else summary,
        )
    }
}

@Composable
internal fun FileManagerHomeMaterial(
    controller: FileManagerController,
    contentTopPadding: Dp,
    onOpenList: () -> Unit,
    onTool: (FmTool) -> Unit,
) {
    val scheme = M3MaterialTheme.colorScheme
    val storage = remember { fmStorageUsage() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = contentTopPadding, bottom = 96.dp),
    ) {
        item(key = "home") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    M3Card(modifier = Modifier.weight(1.35f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            M3Text(text = "实用功能", fontSize = 16.sp, color = scheme.onSurface)
                            Spacer(Modifier.height(12.dp))
                            fmTools.chunked(4).forEachIndexed { rowIndex, row ->
                                if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    row.forEach { tool ->
                                        FmToolCellMaterial(
                                            tool = tool,
                                            modifier = Modifier.weight(1f),
                                            scheme = scheme,
                                            onClick = { onTool(tool.tool) },
                                        )
                                    }
                                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                    M3Card(modifier = Modifier.weight(0.65f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            M3Text(text = "存储空间", fontSize = 16.sp, color = scheme.onSurface)
                            Spacer(Modifier.height(12.dp))
                            val used = storage.first
                            val total = storage.second
                            val fraction = if (total > 0) used.toFloat() / total else 0f
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(150.dp)
                                    .background(scheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((150 * fraction).dp.coerceAtLeast(4.dp))
                                        .background(scheme.primary.copy(alpha = 0.38f), RoundedCornerShape(12.dp))
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            M3Text(text = "已用", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            M3Text(
                                text = "${fmFormatGb(used)} GB / ${fmFormatGb(total)} GB",
                                fontSize = 11.sp,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                M3Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        fmCategories.forEach { entry ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { controller.openCategory(entry.category) },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(entry.color, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    M3Icon(
                                        imageVector = entry.icon,
                                        contentDescription = entry.category.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                M3Text(text = entry.category.label, fontSize = 11.sp, color = scheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FmToolCellMaterial(
    tool: FmToolEntry,
    modifier: Modifier,
    scheme: androidx.compose.material3.ColorScheme,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.then(if (tool.enabled) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = if (tool.enabled) tool.color else scheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            M3Icon(
                imageVector = tool.icon,
                contentDescription = tool.label,
                tint = if (tool.enabled) Color.White else scheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        M3Text(
            text = tool.label,
            fontSize = 11.sp,
            color = if (tool.enabled) scheme.onSurface else scheme.onSurfaceVariant,
        )
    }
}
