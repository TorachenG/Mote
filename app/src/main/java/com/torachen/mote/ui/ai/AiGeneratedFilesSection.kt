@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.torachen.mote.ui.filemanager.LocalFileViewerRequest
import com.torachen.mote.ui.filemanager.LocalFileViewerState
import com.torachen.mote.ui.fmFormatSize
import com.torachen.mote.ui.fmTypeIcon
import com.torachen.mote.ui.office.OfficePreviewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

/** 汇总当前对话里所有回答生成的文件（按路径去重，保持生成顺序）。 */
internal fun collectAiGeneratedFiles(messages: List<AiUiMessage>): List<AiGeneratedFile> {
    val seen = LinkedHashSet<String>()
    val result = mutableListOf<AiGeneratedFile>()
    messages.forEach { message ->
        message.generatedFiles.forEach { file ->
            if (file.path.isNotBlank() && seen.add(file.path)) {
                result.add(file)
            }
        }
    }
    return result
}

/**
 * 对话底部「本次对话生成的文件」区域。
 *
 * 列数随屏幕宽度变化：手机 2 列，大屏最多 4 列；
 * 图片显示缩略图，其它文件显示「图标 + 文件名 + 大小」的长方卡片。
 */
@Composable
internal fun AiGeneratedFilesSection(
    files: List<AiGeneratedFile>,
    modifier: Modifier = Modifier,
) {
    if (files.isEmpty()) return
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp, bottom = 4.dp),
    ) {
        val columns = (maxWidth / 152.dp).toInt().coerceIn(1, 4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "本次对话生成的文件",
                    style = MiuixTheme.textStyles.subtitle,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${files.size}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            files.chunked(columns).forEach { rowFiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowFiles.forEach { file ->
                        Box(modifier = Modifier.weight(1f)) {
                            AiGeneratedFileCard(file)
                        }
                    }
                    repeat(columns - rowFiles.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AiGeneratedFileCard(file: AiGeneratedFile) {
    val context = LocalContext.current
    if (file.kind == AiGeneratedFileKind.Image) {
        AiGeneratedImageCard(file = file, onClick = { openAiGeneratedFile(context, file) })
        return
    }

    val typed = fmTypeIcon(File(file.path))
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(shape)
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .clickable { openAiGeneratedFile(context, file) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = typed?.first ?: Icons.Rounded.Description,
                contentDescription = null,
                tint = typed?.second ?: MiuixTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = fmFormatSize(file.sizeBytes).ifBlank { "文件" },
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AiGeneratedImageCard(
    file: AiGeneratedFile,
    onClick: () -> Unit,
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, file.path) {
        value = withContext(Dispatchers.IO) { loadThumbnail(file.path, 480) }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            ComposeImage(
                bitmap = image,
                contentDescription = file.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = file.name,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** 点击生成的文件：Office 走内置预览，图片走内置图片查看，其它交给系统。 */
internal fun openAiGeneratedFile(context: Context, file: AiGeneratedFile) {
    val local = File(file.path)
    if (!local.isFile) {
        Toast.makeText(context, "文件不存在或已被删除", Toast.LENGTH_SHORT).show()
        return
    }
    when (file.kind) {
        AiGeneratedFileKind.Word,
        AiGeneratedFileKind.Excel,
        AiGeneratedFileKind.Ppt,
        -> OfficePreviewActivity.start(context, local.absolutePath, file.name)

        AiGeneratedFileKind.Image -> openImageInViewer(context, local, file.name)
        else -> openWithSystemApp(context, local)
    }
}

private fun openImageInViewer(context: Context, file: File, title: String) {
    val uri = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()
    if (uri == null) {
        openWithSystemApp(context, file)
        return
    }
    LocalFileViewerState.current = LocalFileViewerRequest.Web(
        path = file.absolutePath,
        title = title,
        browserUrl = uri.toString(),
        isImage = true,
    )
}

private fun openWithSystemApp(context: Context, file: File) {
    val extension = file.extension.lowercase()
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "打开文件")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }.onFailure {
        Toast.makeText(context, "没有可以打开该文件的应用", Toast.LENGTH_SHORT).show()
    }
}

private fun loadThumbnail(path: String, targetPx: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
    while (maxDimension / sampleSize > targetPx) {
        sampleSize *= 2
    }
    val bitmap = runCatching {
        BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        )
    }.getOrNull() ?: return null
    return bitmap.asImageBitmap()
}
