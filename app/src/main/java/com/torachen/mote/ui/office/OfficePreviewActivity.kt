@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.office

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import java.io.File
import java.util.Locale

/**
 * Word / Excel / PPT 预览页。
 *
 * 直接解析 docx / xlsx / pptx 的 OOXML 内容并用原生 Compose 渲染，
 * 不依赖系统里是否安装了 Office 应用。
 */
class OfficePreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val path = intent.getStringExtra(EXTRA_PATH).orEmpty()
        if (path.isBlank()) {
            finish()
            return
        }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            .ifBlank { File(path).name }
        setContent {
            PreviewTheme {
                OfficePreviewScreen(
                    path = path,
                    title = title,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        private const val EXTRA_PATH = "extra_path"
        private const val EXTRA_TITLE = "extra_title"

        fun start(context: Context, path: String, title: String = File(path).name) {
            val intent = Intent(context, OfficePreviewActivity::class.java).apply {
                putExtra(EXTRA_PATH, path)
                putExtra(EXTRA_TITLE, title)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            runCatching { context.startActivity(intent) }
        }
    }
}

@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    val mode = remember { prefs.getInt("ui_color_mode", 0) }
    val dark = when (mode) {
        1 -> false
        2, 3 -> true
        else -> isSystemInDarkTheme()
    }
    val colors = remember(dark) {
        if (dark) darkColorScheme() else lightColorScheme()
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    MiuixTheme(colors = colors) {
        content()
    }
}

private sealed interface PreviewState {
    data object Loading : PreviewState
    data class Ready(val content: OfficePreviewContent, val sizeBytes: Long) : PreviewState
    data class Error(val message: String) : PreviewState
}

@Composable
private fun OfficePreviewScreen(
    path: String,
    title: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val state by produceState<PreviewState>(PreviewState.Loading, path) {
        value = withContext(Dispatchers.IO) { loadPreview(path) }
    }
    val kind = remember(path) { OfficeDocumentParser.kindOf(File(path).name) }
    val sizeBytes = (state as? PreviewState.Ready)?.sizeBytes ?: 0L
    val subtitle = buildString {
        append(kind?.let(OfficeDocumentParser::titleOf) ?: "文档")
        if (sizeBytes > 0L) {
            append(" · ")
            append(formatSize(sizeBytes))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MiuixIcons.ChevronBackward,
                    contentDescription = "返回",
                    tint = MiuixTheme.colorScheme.onSurface,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.title4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MiuixTheme.colorScheme.dividerLine),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            when (val current = state) {
                PreviewState.Loading -> LoadingState()
                is PreviewState.Error -> ErrorState(
                    message = current.message,
                    onOpenExternally = { openExternally(context, path) },
                )

                is PreviewState.Ready -> when (val content = current.content) {
                    is OfficePreviewContent.Word -> WordContent(content.paragraphs)
                    is OfficePreviewContent.Excel -> ExcelContent(content.sheets)
                    is OfficePreviewContent.Ppt -> PptContent(content.slides)
                }
            }
        }
    }
}

private fun loadPreview(path: String): PreviewState {
    val file = File(path)
    if (!file.isFile) {
        return PreviewState.Error("文件不存在或已被删除")
    }
    val kind = OfficeDocumentParser.kindOf(file.name)
        ?: return PreviewState.Error("暂不支持预览该格式，可以用其它应用打开")
    return runCatching {
        PreviewState.Ready(OfficeDocumentParser.parse(file, kind), file.length())
    }.getOrElse {
        PreviewState.Error("解析失败：${it.message ?: "文档可能已损坏"}")
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(size = 30.dp)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "正在解析文档…",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun ErrorState(message: String, onOpenExternally: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onOpenExternally,
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.primary,
                contentColor = MiuixTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("用其它应用打开")
        }
    }
}

// ============================================================
// Word
// ============================================================

@Composable
private fun WordContent(paragraphs: List<WordParagraph>) {
    if (paragraphs.isEmpty()) {
        EmptyState("文档里没有可显示的文本")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(paragraphs) { paragraph ->
            val heading = paragraph.heading
            Text(
                text = paragraph.text,
                fontSize = when (heading) {
                    1 -> 23.sp
                    2 -> 20.sp
                    3 -> 18.sp
                    else -> 16.sp
                },
                lineHeight = when (heading) {
                    1 -> 32.sp
                    2 -> 29.sp
                    3 -> 27.sp
                    else -> 27.sp
                },
                fontWeight = if (heading > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = if (heading > 0) Modifier.padding(top = 4.dp) else Modifier,
            )
        }
    }
}

// ============================================================
// Excel
// ============================================================

@Composable
private fun ExcelContent(sheets: List<SheetData>) {
    if (sheets.isEmpty() || sheets.all { it.rows.isEmpty() }) {
        EmptyState("表格里没有可显示的数据")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(sheets) { sheet ->
            SheetBlock(sheet)
        }
    }
}

 @Composable
private fun SheetBlock(sheet: SheetData) {
    val rows = sheet.rows.take(80).map { it.take(20) }
    Column {
        Text(
            text = sheet.name,
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.SemiBold,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        if (rows.isEmpty()) {
            Text(
                text = "空表格",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            return@Column
        }
        val scrollState = rememberScrollState()
        val columnCount = rows.maxOfOrNull { it.size } ?: 0
        val tableWidth = (columnCount * 118).dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MiuixTheme.colorScheme.surfaceContainer)
                .horizontalScroll(scrollState),
        ) {
            Column(modifier = Modifier.width(tableWidth)) {
                rows.forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .width(tableWidth)
                            .height(40.dp),
                    ) {
                        row.forEachIndexed { columnIndex, cell ->
                            Box(
                                modifier = Modifier
                                    .width(118.dp)
                                    .fillMaxHeight()
                                    .background(
                                        if (rowIndex == 0) {
                                            MiuixTheme.colorScheme.secondaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = cell,
                                    fontSize = 13.sp,
                                    fontWeight = if (rowIndex == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (columnIndex != row.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .background(MiuixTheme.colorScheme.dividerLine),
                                )
                            }
                        }
                    }
                    if (rowIndex != rows.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(tableWidth)
                                .height(1.dp)
                                .background(MiuixTheme.colorScheme.dividerLine),
                        )
                    }
                }
            }
        }
        if (sheet.rows.size > rows.size || sheet.rows.any { it.size > 20 }) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "仅显示前 80 行、20 列",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ============================================================
// PPT
// ============================================================

@Composable
private fun PptContent(slides: List<SlideData>) {
    if (slides.isEmpty()) {
        EmptyState("演示文稿里没有可显示的页面")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(slides) { slide ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
            ) {
                Text(
                    text = String.format(Locale.CHINA, "第 %d 页", slide.index),
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (slide.title.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = slide.title,
                        fontSize = 18.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
                slide.lines.forEach { line ->
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(
                            text = line,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

// ============================================================
// 工具
// ============================================================

private fun openExternally(context: Context, path: String) {
    val file = File(path)
    if (!file.isFile) return
    val mime = when (file.extension.lowercase()) {
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        else -> "*/*"
    }
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "用其它应用打开"))
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return ""
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format(Locale.CHINA, "%.0f KB", bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L -> String.format(Locale.CHINA, "%.1f MB", bytes / 1024.0 / 1024.0)
        else -> String.format(Locale.CHINA, "%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
    }
}
