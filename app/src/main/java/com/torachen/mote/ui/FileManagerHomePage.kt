package com.torachen.mote.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 文件管理主页：与目录页完全分离 */
@Composable
internal fun FileManagerHomePage(
    controller: FileManagerController,
    onOpenScanner: () -> Unit,
) {
    val context = controller.context
    val surface = MiuixTheme.colorScheme.surface
    val onSurface = MiuixTheme.colorScheme.onSurface
    val summary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val storage = remember { fmStorageUsage() }
    val apps = remember { fmDetectedApps(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(surface),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item(key = "header") {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "返回",
                        tint = onSurface,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { controller.onBack() }
                            .padding(9.dp),
                    )
                }
                Text(
                    text = "文件管理",
                    fontSize = 34.sp,
                    color = onSurface,
                    modifier = Modifier.padding(start = 26.dp, bottom = 10.dp),
                )
            }
        }

        // 功能区：5 列 × 2 行
        item(key = "tools") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                insideMargin = PaddingValues(0.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)) {
                    fmHomeTools.chunked(5).forEachIndexed { rowIndex, row ->
                        if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            row.forEach { tool ->
                                HomeToolCell(tool, onSurface) {
                                    when (tool.tool) {
                                        FmTool.SCAN -> onOpenScanner()
                                        FmTool.TRASH -> controller.openRecycleBin()
                                        FmTool.COMPRESS, FmTool.EXTRACT ->
                                            controller.enterList(controller.root)
                                        else -> controller.enterList(controller.root)
                                    }
                                }
                            }
                            repeat(5 - row.size) { Spacer(Modifier.width(44.dp)) }
                        }
                    }
                }
            }
        }

        // 存储环
        item(key = "storage") {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                insideMargin = PaddingValues(0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val used = storage.first
                    val total = storage.second
                    val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = fraction,
                            size = 72.dp,
                            strokeWidth = 6.dp,
                        )
                        Text(
                            text = "${(fraction * 100).toInt()}%",
                            fontSize = 16.sp,
                            color = onSurface,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = fmStorageHint(fraction),
                            fontSize = 16.sp,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "已占用 ${fmFormatGb(used)} GB / ${fmFormatGb(total)} GB" +
                                "（可用 ${fmFormatGb((total - used).coerceAtLeast(0))} GB）",
                            fontSize = 12.sp,
                            color = summary,
                        )
                    }
                }
            }
        }

        // 手机存储目录
        item(key = "root") {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                insideMargin = PaddingValues(0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { controller.enterList(controller.root) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(text = "手机存储目录", fontSize = 16.sp, color = onSurface, modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = summary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // 分类：2 列
        item(key = "categories") {
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                fmCategories.chunked(2).forEachIndexed { index, pair ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { entry ->
                            Card(modifier = Modifier.weight(1f), insideMargin = PaddingValues(0.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { controller.openCategory(entry.category) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (entry.usePrimary) MiuixTheme.colorScheme.primary else entry.color,
                                                RoundedCornerShape(9.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = entry.icon,
                                            contentDescription = entry.category.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(text = entry.category.label, fontSize = 15.sp, color = onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 已安装应用下载目录
        if (apps.isNotEmpty()) {
            item(key = "apps") {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    insideMargin = PaddingValues(0.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        apps.forEachIndexed { index, app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val dir = java.io.File(app.dirPath)
                                        if (dir.exists()) controller.enterList(dir) else controller.enterList(controller.root)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Android,
                                    contentDescription = null,
                                    tint = app.color,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(text = app.name, fontSize = 16.sp, color = onSurface, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = summary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeToolCell(tool: FmToolEntry, onSurface: Color, onClick: () -> Unit) {
    val remote = tool.remoteIcon?.let { rememberRemoteIcon(it) }
    Column(
        modifier = Modifier.width(44.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (tool.usePrimary) MiuixTheme.colorScheme.primary else tool.color,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (remote != null) {
                androidx.compose.foundation.Image(
                    bitmap = remote,
                    contentDescription = tool.label,
                    colorFilter = if (tool.whiteMask) {
                        androidx.compose.ui.graphics.ColorFilter.tint(
                            Color.White,
                            androidx.compose.ui.graphics.BlendMode.SrcIn,
                        )
                    } else null,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(text = tool.label, fontSize = 9.sp, color = onSurface, maxLines = 1, softWrap = false)
    }
}

internal val fmHomeTools: List<FmToolEntry> = listOf(
    FmToolEntry(FmTool.COMPRESS, "文件压缩", Icons.Rounded.FolderZip, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.EXTRACT, "文件解压", Icons.Rounded.FolderOpen, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.CONVERT, "格式转换", Icons.Rounded.SwapHoriz, Color(0xFFE5E5E5)),
    FmToolEntry(FmTool.QQ, "文件互传", Icons.Rounded.QrCodeScanner, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.WECHAT, "微信文件", Icons.Rounded.Forum, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.OCR, "文字提取", Icons.Rounded.TextFields, Color(0xFF7767F9)),
    FmToolEntry(FmTool.SCAN, "扫描文档", Icons.Rounded.QrCodeScanner, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.TRASH, "回收站", Icons.Rounded.Delete, Color(0xFFE8452C)),
    FmToolEntry(FmTool.CONVERT, "拍照扫描", Icons.Rounded.QrCodeScanner, Color(0xFF16C060)),
    FmToolEntry(FmTool.OCR, "更多功能", Icons.Rounded.SwapHoriz, Color(0xFF9AA0A6)),
)

internal fun fmStorageHint(fraction: Float): String = when {
    fraction >= 0.95f -> "存储空间已接近上限"
    fraction >= 0.8f -> "存储空间比较紧张"
    fraction >= 0.5f -> "存储空间还算宽裕"
    else -> "当前存储空间十分充裕"
}

internal data class FmDetectedApp(val name: String, val dirPath: String, val color: Color)

internal fun fmDetectedApps(context: android.content.Context): List<FmDetectedApp> {
    val pm = context.packageManager
    val root = android.os.Environment.getExternalStorageDirectory()
    val candidates = listOf(
        Triple("QQ", "com.tencent.mobileqq", "Android/data/com.tencent.mobileqq/tencent/QQfile_recv"),
        Triple("微信", "com.tencent.mm", "Android/data/com.tencent.mm/MicroMsg/Download"),
        Triple("X", "com.twitter.android", "Android/data/com.twitter.android/files"),
        Triple("哔哩哔哩", "tv.danmaku.bili", "Android/data/tv.danmaku.bili/download"),
    )
    val colors = listOf(Color(0xFF12B7F5), Color(0xFF07C160), Color(0xFF000000), Color(0xFFFB7299))
    val out = mutableListOf<FmDetectedApp>()
    candidates.forEachIndexed { index, (name, pkg, path) ->
        val installed = runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        if (installed) {
            out.add(FmDetectedApp(name, java.io.File(root, path).absolutePath, colors[index]))
        }
    }
    return out
}
