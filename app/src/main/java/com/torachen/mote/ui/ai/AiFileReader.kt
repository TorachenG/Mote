@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

/**
 * 把用户选择的文件转换成可以放进模型上下文的文本（以及可选的图片 base64）。
 *
 * 支持：
 * - 纯文本 / Markdown / JSON / XML / HTML / CSV / 代码等；
 * - PDF（逐页渲染 + 本地 OCR，不依赖任何 PDF 文本库）；
 * - PNG / JPG / WEBP / BMP / GIF（本地 OCR；开启视觉输入时同时附带原图）；
 * - DOCX / PPTX / XLSX（直接解析 OOXML，无第三方依赖）。
 */
object AiFileReader {

    private const val MAX_TEXT_BYTES = 8L * 1024L * 1024L
    private const val MAX_BINARY_BYTES = 40L * 1024L * 1024L
    private const val MAX_VISION_BYTES = 6L * 1024L * 1024L
    private const val MAX_PDF_PAGES = 30
    private const val MAX_IMAGE_SIDE = 2200

    private val textExtensions = setOf(
        "txt", "md", "markdown", "mdx", "json", "json5", "xml", "html", "htm", "xhtml",
        "css", "scss", "less", "js", "mjs", "cjs", "ts", "tsx", "jsx", "vue", "svelte",
        "kt", "kts", "java", "py", "rb", "php", "go", "rs", "c", "h", "cpp", "hpp", "cc",
        "cs", "swift", "dart", "sh", "bash", "zsh", "bat", "cmd", "ps1", "sql", "yaml",
        "yml", "toml", "ini", "cfg", "conf", "properties", "gradle", "log", "csv", "tsv",
        "rst", "tex", "srt", "vtt", "ass", "lrc", "m3u", "m3u8", "gitignore", "env",
        "jsonl", "ndjson", "eml", "jsonc", "org", "adoc", "asciidoc", "diff", "patch",
        "http", "proto", "graphql", "gql", "groovy", "pl", "pm", "r", "lua", "vim",
        "makefile", "cmake", "dockerfile", "nginx", "desktop",
        "mhtml", "mht", "fb2", "opml", "rss", "atom", "ics", "vcf", "gpx", "kml",
        "svg", "webmanifest", "plist", "strings", "po", "pot", "reg", "url", "webloc",
        "lock", "diff", "patch", "srt", "vtt", "ass", "ssa", "lrc",
    )

    private val imageExtensions = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif")
    private val audioExtensions = setOf(
        "mp3", "wav", "m4a", "aac", "ogg", "flac", "opus", "amr", "3gp", "wma",
    )

    suspend fun read(context: Context, uri: Uri): AiAttachment = withContext(Dispatchers.IO) {
        val name = queryDisplayName(context, uri)
        val mime = context.contentResolver.getType(uri)
            ?: guessMimeFromName(name)
        val size = querySize(context, uri)
        val extension = name.substringAfterLast('.', "").lowercase()

        val maxBytes = if (extension in textExtensions) MAX_TEXT_BYTES else MAX_BINARY_BYTES
        if (size > maxBytes) {
            throw IOException("文件过大（${formatAiFileSize(size)}），请选择 40 MB 以内的文件")
        }

        val bytes = readAllBytes(context, uri, maxBytes)
        if (bytes.isEmpty()) throw IOException("文件内容为空")

        val imagePath = if (extension in imageExtensions || mime.startsWith("image/")) {
            File(context.cacheDir, "ai_image_${System.nanoTime()}.img").apply {
                writeBytes(bytes)
            }.absolutePath
        } else {
            null
        }

        val maxChars = AiSettings.maxFileChars

        when {
            extension in audioExtensions || mime.startsWith("audio/") -> {
                readAudio(context, uri, bytes, name, mime, maxChars)
            }

            extension == "pdf" || mime == "application/pdf" -> {
                readPdf(context, bytes, name, maxChars)
            }

            extension in imageExtensions || mime.startsWith("image/") -> {
                readImage(bytes, name, mime, maxChars)
            }

            extension in setOf("docx", "docm", "dotx", "dotm") ||
                    mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                val xml = readZipEntry(bytes, "word/document.xml")
                    ?: throw IOException("无法解析 DOCX：缺少 word/document.xml")
                buildTextAttachment(name, mime, bytes.size.toLong(), docxXmlToText(xml), maxChars, null)
            }

            extension in setOf("pptx", "pptm", "potx", "potm") ||
                    mime == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                buildTextAttachment(name, mime, bytes.size.toLong(), pptxToText(bytes), maxChars, null)
            }

            extension in setOf("xlsx", "xlsm", "xltx", "xltm") ||
                    mime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                buildTextAttachment(name, mime, bytes.size.toLong(), xlsxToText(bytes), maxChars, null)
            }

            extension == "odt" || extension == "ods" || extension == "odp" -> {
                buildTextAttachment(name, mime, bytes.size.toLong(), odfToText(bytes), maxChars, null)
            }

            extension == "epub" -> {
                buildTextAttachment(name, mime, bytes.size.toLong(), epubToText(bytes), maxChars, null)
            }

            extension == "rtf" -> {
                buildTextAttachment(
                    name,
                    mime,
                    bytes.size.toLong(),
                    rtfToText(bytes.toString(Charsets.UTF_8)),
                    maxChars,
                    null,
                )
            }

            extension in setOf("zip", "apk", "jar", "war", "aab") -> {
                buildTextAttachment(
                    name,
                    mime,
                    bytes.size.toLong(),
                    archiveToText(bytes),
                    maxChars,
                    null,
                )
            }

            extension in textExtensions || mime.startsWith("text/") ||
                    mime == "application/json" || mime == "application/xml" -> {
                val decoded = decodeText(bytes)
                val text = if (extension == "html" || extension == "htm" || extension == "xhtml") {
                    htmlToText(decoded)
                } else {
                    decoded
                }
                buildTextAttachment(name, mime, bytes.size.toLong(), text, maxChars, null)
            }

            extension == "doc" || extension == "ppt" || extension == "xls" -> {
                buildTextAttachment(
                    name,
                    mime,
                    bytes.size.toLong(),
                    legacyOfficeToText(bytes),
                    maxChars,
                    null,
                )
            }

            else -> throw IOException("暂不支持该文件类型（$name）")
        }.copy(uriString = uri.toString(), imagePath = imagePath)
    }

    // ============================================================
    // 音频
    // ============================================================

    private suspend fun readAudio(
        context: Context,
        uri: Uri,
        bytes: ByteArray,
        name: String,
        mime: String,
        maxChars: Int,
    ): AiAttachment {
        val metadata = runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION,
                )?.toLongOrNull() ?: 0L
                val title = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_TITLE,
                ).orEmpty()
                title to duration
            } finally {
                retriever.release()
            }
        }.getOrDefault("" to 0L)

        val transcript = transcribeAudio(context, uri)
        val text = buildString {
            append("音频文件：").append(name).append("\n")
            if (metadata.second > 0L) {
                append("时长：").append(metadata.second / 1000).append(" 秒\n")
            }
            if (metadata.first.isNotBlank()) {
                append("标题：").append(metadata.first).append("\n")
            }
            if (transcript.isNotBlank()) {
                append("\n语音转写：\n").append(transcript)
            } else {
                append("\n当前设备未能直接转写该音频，请结合文件信息回答；")
                append("如需逐字总结，请用户补充文字稿。")
            }
        }
        return buildTextAttachment(name, mime, bytes.size.toLong(), text, maxChars, null)
    }

    /** Android 13+ 支持直接把音频文件交给系统语音识别；失败时仅保留元数据。 */
    private suspend fun transcribeAudio(context: Context, uri: Uri): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return ""
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return ""
        return withContext(Dispatchers.Main) {
            withTimeoutOrNull(25_000L) {
                suspendCancellableCoroutine { continuation ->
                    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                    fun finish(result: String?) {
                        runCatching { recognizer.destroy() }
                        if (continuation.isActive) continuation.resume(result)
                    }
                    recognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) = Unit
                        override fun onBeginningOfSpeech() = Unit
                        override fun onRmsChanged(rmsdB: Float) = Unit
                        override fun onBufferReceived(buffer: ByteArray?) = Unit
                        override fun onEndOfSpeech() = Unit
                        override fun onError(error: Int) = finish("")
                        override fun onResults(results: Bundle?) {
                            finish(
                                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                    ?.joinToString(" ")
                                    .orEmpty(),
                            )
                        }

                        override fun onPartialResults(partialResults: Bundle?) = Unit
                        override fun onEvent(eventType: Int, params: Bundle?) = Unit
                    })
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, uri)
                    }
                    runCatching { recognizer.startListening(intent) }
                        .onFailure { finish("") }
                    continuation.invokeOnCancellation {
                        runCatching { recognizer.destroy() }
                    }
                }
            }.orEmpty()
        }
    }

    // ============================================================
    // 文本 / HTML
    // ============================================================

    private fun buildTextAttachment(
        name: String,
        mime: String,
        size: Long,
        rawText: String,
        maxChars: Int,
        image: Pair<String, String>?,
    ): AiAttachment {
        val normalized = rawText
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
        if (normalized.isBlank()) {
            throw IOException("没有从文件中读取到可用文字")
        }
        val (text, note) = truncate(normalized, maxChars)
        return AiAttachment(
            id = System.nanoTime(),
            name = name,
            mime = mime,
            sizeBytes = size,
            text = text,
            imageMime = image?.first,
            imageBase64 = image?.second,
            note = note,
        )
    }

    private fun truncate(text: String, maxChars: Int): Pair<String, String?> {
        if (text.length <= maxChars) return text to null
        return text.take(maxChars) to "内容较长，已截取前 $maxChars 个字符"
    }

    private fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 4 &&
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()
        ) {
            return String(bytes, Charsets.UTF_16LE)
        }
        if (bytes.size >= 4 &&
            bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()
        ) {
            return String(bytes, Charsets.UTF_16BE)
        }
        val utf8 = String(bytes, Charsets.UTF_8)
        if (utf8.indexOf('\uFFFD') < 0) return utf8
        return runCatching { String(bytes, Charset.forName("GB18030")) }.getOrDefault(utf8)
    }

    private fun htmlToText(html: String): String {
        var text = html.replace(
            Regex("(?is)<(script|style|noscript|svg)\\b[^>]*>.*?</\\1>"),
            " ",
        )
        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")
        text = text.replace(Regex("(?i)</(p|div|section|article|li|tr|h[1-6]|blockquote|pre)>"), "\n")
        text = text.replace(Regex("(?i)<li\\b[^>]*>"), "• ")
        text = text.replace(Regex("(?i)<t[dh]\\b[^>]*>"), "\t")
        text = text.replace(Regex("<[^>]+>"), "")
        return unescapeXml(text)
    }

    // ============================================================
    // PDF
    // ============================================================

    private suspend fun readPdf(
        context: Context,
        bytes: ByteArray,
        name: String,
        maxChars: Int,
    ): AiAttachment {
        val cacheDir = File(context.cacheDir, "ai_files").apply { mkdirs() }
        val temp = File(cacheDir, "ai_${System.nanoTime()}.pdf")
        return try {
            temp.writeBytes(bytes)
            val builder = StringBuilder()
            var processed = 0
            var total = 0

            ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    total = renderer.pageCount
                    val pages = minOf(total, MAX_PDF_PAGES)
                    val recognizer = TextRecognition.getClient(
                        ChineseTextRecognizerOptions.Builder().build(),
                    )
                    try {
                        for (index in 0 until pages) {
                            renderer.openPage(index).use { page ->
                                val targetWidth = 1600
                                val scale = targetWidth.toFloat() / page.width.coerceAtLeast(1)
                                val height = (page.height * scale).toInt().coerceAtLeast(1)
                                val bitmap = Bitmap.createBitmap(
                                    targetWidth,
                                    height,
                                    Bitmap.Config.ARGB_8888,
                                )
                                Canvas(bitmap).drawColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                val pageText = runCatching {
                                    recognize(recognizer, bitmap)
                                }.getOrDefault("")
                                bitmap.recycle()
                                if (pageText.isNotBlank()) {
                                    builder.append("--- 第 ").append(index + 1).append(" 页 ---\n")
                                    builder.append(pageText.trim()).append("\n\n")
                                }
                                processed = index + 1
                            }
                        }
                    } finally {
                        runCatching { recognizer.close() }
                    }
                }
            }

            val note = buildString {
                if (total > MAX_PDF_PAGES) {
                    append("PDF 共 ").append(total).append(" 页，仅解析了前 ")
                        .append(MAX_PDF_PAGES).append(" 页")
                }
                if (processed == 0) {
                    if (isNotEmpty()) append("；")
                    append("未能渲染 PDF 页面")
                }
            }.ifBlank { null }

            if (builder.isBlank()) {
                throw IOException("未能从 PDF 中识别出文字（可能是空白页或扫描质量过低）")
            }
            buildTextAttachment(name, "application/pdf", bytes.size.toLong(), builder.toString(), maxChars, null)
                .copy(note = note)
        } finally {
            runCatching { temp.delete() }
        }
    }

    // ============================================================
    // 图片
    // ============================================================

    private suspend fun readImage(
        bytes: ByteArray,
        name: String,
        mime: String,
        maxChars: Int,
    ): AiAttachment {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("无法读取图片，请确认文件未损坏")
        }
        var sample = 1
        var longest = max(bounds.outWidth, bounds.outHeight)
        while (longest / sample > MAX_IMAGE_SIDE) sample *= 2

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IOException("无法解码图片")

        val recognized = runCatching {
            val recognizer = TextRecognition.getClient(
                ChineseTextRecognizerOptions.Builder().build(),
            )
            try {
                recognize(recognizer, bitmap)
            } finally {
                runCatching { recognizer.close() }
            }
        }.getOrDefault("")

        if (!bitmap.isRecycled) bitmap.recycle()

        val imageMime = when (mime.lowercase()) {
            "image/jpg" -> "image/jpeg"
            else -> mime.ifBlank { guessMimeFromName(name) }
        }
        val vision = if (bytes.size <= MAX_VISION_BYTES) {
            imageMime to Base64.encodeToString(bytes, Base64.NO_WRAP)
        } else {
            null
        }

        val text = recognized.trim()
        val note = buildString {
            if (text.isBlank()) {
                append("图片中未识别到文字")
                if (vision == null) append("，且图片超过 6 MB 无法作为视觉输入")
            }
            if (vision == null) {
                if (isNotEmpty()) append("；")
                append("图片超过 6 MB，未附带原图")
            }
        }.ifBlank { null }

        val finalText = if (text.isBlank()) {
            "[图片：$name，未识别到文字]"
        } else {
            "【图片文字识别结果：$name】\n$text"
        }
        return buildTextAttachment(name, imageMime, bytes.size.toLong(), finalText, maxChars, vision)
            .copy(note = note)
    }

    private suspend fun recognize(recognizer: TextRecognizer, bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result.text)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
        }

    // ============================================================
    // OOXML
    // ============================================================

    private fun readCurrentZipEntry(zip: ZipInputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = zip.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun readZipEntry(bytes: ByteArray, entryName: String): String? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == entryName) {
                    return zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
            }
        }
        return null
    }

    private fun docxXmlToText(xml: String): String {
        var text = xml
        text = text.replace(Regex("(?s)<w:instrText[^>]*>.*?</w:instrText>"), "")
        text = text.replace(Regex("(?s)<w:delText[^>]*>.*?</w:delText>"), "")
        text = text.replace(Regex("<w:tab\\b[^>]*/?>"), "\t")
        text = text.replace(Regex("<w:br\\b[^>]*/?>"), "\n")
        text = text.replace(Regex("<w:cr\\b[^>]*/?>"), "\n")
        text = text.replace(Regex("</w:p>"), "\n")
        text = text.replace(Regex("</w:tr>"), "\n")
        text = text.replace(Regex("</w:tc>"), "\t")
        text = text.replace(Regex("<[^>]+>"), "")
        return unescapeXml(text)
    }

    /** ODT / ODS / ODP 共用 content.xml。 */
    private fun odfToText(bytes: ByteArray): String {
        val xml = readZipEntry(bytes, "content.xml")
            ?: throw IOException("无法解析 OpenDocument：缺少 content.xml")
        var text = xml
        text = text.replace(Regex("<text:tab\\b[^>]*/?>"), "\t")
        text = text.replace(Regex("<text:line-break\\b[^>]*/?>"), "\n")
        text = text.replace(Regex("</text:p>"), "\n")
        text = text.replace(Regex("</text:h>"), "\n")
        text = text.replace(Regex("</table:table-cell>"), "\t")
        text = text.replace(Regex("</table:table-row>"), "\n")
        text = text.replace(Regex("<[^>]+>"), "")
        return unescapeXml(text).trim().ifBlank {
            throw IOException("OpenDocument 中没有可读取的文字")
        }
    }

    /** EPUB：按 spine 顺序读取 xhtml/html 正文。 */
    private fun epubToText(bytes: ByteArray): String {
        val chapters = readZipEntries(bytes) { name ->
            val lower = name.lowercase()
            !lower.contains("nav") && (lower.endsWith(".xhtml") ||
                    lower.endsWith(".html") || lower.endsWith(".htm"))
        }.sortedBy { it.first }
        if (chapters.isEmpty()) throw IOException("无法解析 EPUB：没有找到正文内容")
        val builder = StringBuilder()
        chapters.forEach { (_, content) ->
            var text = content
            text = text.replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), "")
            text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")
            text = text.replace(Regex("(?i)</(p|div|h[1-6]|li|tr|section|article)>"), "\n")
            text = text.replace(Regex("<[^>]+>"), "")
            text = unescapeXml(text).trim()
            if (text.isNotBlank()) builder.append(text).append("\n\n")
        }
        return builder.toString().trim().ifBlank {
            throw IOException("EPUB 中没有可读取的文字")
        }
    }

    /** 旧版 Office 二进制：尽力提取可读字符串。 */
    private fun legacyOfficeToText(bytes: ByteArray): String {
        val utf16 = String(bytes, Charsets.UTF_16LE)
        val utf8 = String(bytes, Charsets.UTF_8)
        val candidate = listOf(utf16, utf8).maxByOrNull { text ->
            text.count { it.code in 32..126 || it.code > 0x2E80 }
        }.orEmpty()
        val cleaned = candidate
            .filter { it == '\n' || it == '\t' || it.code >= 32 }
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
        if (cleaned.count { it.code in 32..126 || it.code > 0x2E80 } < 20) {
            throw IOException("旧版 Office 文件中没有提取到可读文字，请另存为 DOCX/PPTX/XLSX")
        }
        return cleaned
    }

    /** 压缩包/安装包：列出目录，并读取其中的文本类文件。 */
    private fun archiveToText(bytes: ByteArray): String {
        val builder = StringBuilder()
        var count = 0
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (count < 80) {
                val entry = zip.nextEntry ?: break
                count++
                builder.append("• ").append(entry.name).append("\n")
                val entryName = entry.name.lowercase()
                val readable = entryName.substringAfterLast('.', "") in textExtensions
                if (readable && !entry.isDirectory) {
                    val item = runCatching { readCurrentZipEntry(zip) }.getOrNull()
                    if (item != null && item.size <= 300_000) {
                        val text = decodeText(item).trim()
                        if (text.isNotBlank()) {
                            builder.append("--- ").append(entry.name).append(" ---\n")
                            builder.append(text.take(3_000)).append("\n\n")
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        if (builder.isBlank()) throw IOException("压缩包中没有可读取的内容")
        return builder.toString().trim()
    }


    private fun rtfToText(raw: String): String {
        var text = raw
        text = text.replace(Regex("\\\\'([0-9a-fA-F]{2})")) { match ->
            match.groupValues[1].toInt(16).toChar().toString()
        }
        text = text.replace(Regex("\\\\u(-?\\d+)\\??"), "")
        text = text.replace(Regex("\\\\par\\b"), "\n")
        text = text.replace(Regex("\\\\line\\b"), "\n")
        text = text.replace(Regex("\\\\tab\\b"), "\t")
        text = text.replace(Regex("\\\\[a-zA-Z]+-?\\d* ?"), "")
        text = text.replace(Regex("[{}]"), "")
        return text.replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun pptxToText(bytes: ByteArray): String {
        val slides = readZipEntries(bytes) { name ->
            name.matches(Regex("ppt/slides/slide\\d+\\.xml"))
        }.sortedBy { it.first }
        if (slides.isEmpty()) throw IOException("无法解析 PPTX：没有找到幻灯片内容")
        val builder = StringBuilder()
        slides.forEach { (entryName, content) ->
            val pageNumber = Regex("slide(\\d+)").find(entryName)?.groupValues?.get(1) ?: "?"
            var text = content
            text = text.replace(Regex("<a:br\\b[^>]*/?>"), "\n")
            text = text.replace(Regex("</a:p>"), "\n")
            text = text.replace(Regex("</a:tr>"), "\n")
            text = text.replace(Regex("</a:tc>"), "\t")
            text = text.replace(Regex("<[^>]+>"), "")
            text = unescapeXml(text).trim()
            if (text.isNotBlank()) {
                builder.append("--- 幻灯片 ").append(pageNumber).append(" ---\n")
                builder.append(text).append("\n\n")
            }
        }
        if (builder.isBlank()) throw IOException("PPTX 中没有可读取的文字")
        return builder.toString()
    }

    private fun xlsxToText(bytes: ByteArray): String {
        val entries = readZipEntries(bytes) { name ->
            name == "xl/sharedStrings.xml" || name.matches(Regex("xl/worksheets/sheet\\d+\\.xml"))
        }
        val sharedXml = entries.firstOrNull { it.first == "xl/sharedStrings.xml" }?.second
        val shared = if (sharedXml == null) {
            emptyList()
        } else {
            Regex("(?s)<si>(.*?)</si>").findAll(sharedXml).map { match ->
                val inner = match.groupValues[1]
                unescapeXml(inner.replace(Regex("<[^>]+>"), ""))
            }.toList()
        }

        val sheets = entries
            .filter { it.first.startsWith("xl/worksheets/") }
            .sortedBy { it.first }
        if (sheets.isEmpty()) throw IOException("无法解析 XLSX：没有找到工作表")

        val builder = StringBuilder()
        sheets.forEachIndexed { sheetIndex, (_, content) ->
            builder.append("--- 工作表 ").append(sheetIndex + 1).append(" ---\n")
            val rows = Regex("(?s)<row\\b[^>]*>(.*?)</row>").findAll(content)
            rows.forEach { rowMatch ->
                val cells = Regex("(?s)<c\\b([^>]*)>(.*?)</c>").findAll(rowMatch.groupValues[1])
                val values = cells.mapNotNull { cell ->
                    val attrs = cell.groupValues[1]
                    val inner = cell.groupValues[2]
                    val type = Regex("t=\"([^\"]+)\"").find(attrs)?.groupValues?.get(1)
                    val value = Regex("(?s)<v>(.*?)</v>").find(inner)?.groupValues?.get(1)
                    val inline = Regex("(?s)<is>.*?<t[^>]*>(.*?)</t>.*?</is>")
                        .find(inner)?.groupValues?.get(1)
                    when {
                        type == "s" && value != null ->
                            shared.getOrNull(value.trim().toIntOrNull() ?: -1).orEmpty()

                        type == "inlineStr" && inline != null -> unescapeXml(inline)
                        value != null -> unescapeXml(value)
                        else -> ""
                    }
                }
                if (values.any { it.isNotBlank() }) {
                    builder.append(values.joinToString("\t")).append("\n")
                }
            }
            builder.append("\n")
        }
        if (builder.isBlank()) throw IOException("XLSX 中没有可读取的内容")
        return builder.toString()
    }

    private fun readZipEntries(
        bytes: ByteArray,
        filter: (String) -> Boolean,
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (filter(entry.name)) {
                    result.add(entry.name to readCurrentZipEntry(zip).toString(Charsets.UTF_8))
                }
                zip.closeEntry()
            }
        }
        return result
    }

    // ============================================================
    // 通用工具
    // ============================================================

    private fun queryDisplayName(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val value = cursor.getString(0)
                    if (!value.isNullOrBlank()) return value
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "文件"
    }

    private fun querySize(context: Context, uri: Uri): Long {
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) {
                    return cursor.getLong(0)
                }
            }
        }
        return 0L
    }

    private fun readAllBytes(context: Context, uri: Uri, maxBytes: Long): ByteArray {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("无法打开文件")
        input.use { stream ->
            val buffer = ByteArray(64 * 1024)
            val output = java.io.ByteArrayOutputStream()
            var total = 0L
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                total += read
                if (total > maxBytes) {
                    throw IOException("文件过大，请选择更小的文件")
                }
                output.write(buffer, 0, read)
            }
            return output.toByteArray()
        }
    }

    private fun guessMimeFromName(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "gif" -> "image/gif"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "json" -> "application/json"
            "html", "htm" -> "text/html"
            "csv" -> "text/csv"
            else -> "text/plain"
        }

    private fun unescapeXml(text: String): String {
        if ('&' !in text) return text
        return text
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
                val code = match.groupValues[1].toIntOrNull(16)
                if (code != null) String(Character.toChars(code)) else match.value
            }
            .replace(Regex("&#(\\d+);")) { match ->
                val code = match.groupValues[1].toIntOrNull()
                if (code != null) String(Character.toChars(code)) else match.value
            }
    }
}
