package com.torachen.mote.ui.ai

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 生成 Word / Excel / PPT。
 *
 * 默认写入内部共享存储的 Download/Mote；PPT 使用内置 10 页模板，
 * Word/Excel 直接生成最小 OOXML 包。
 */
internal object AiDocumentWriter {

    private const val FENCE_OPEN = "```mote-file"
    private const val FENCE_CLOSE = "```"

    /** 供 AI 工具直接调用：生成文件并返回可展示的文件信息。 */
    fun create(context: Context, type: String, title: String, content: String): AiGeneratedFile? {
        val normalized = normalizeType(type) ?: return null
        val safeTitle = title.ifBlank { "Mote 生成文件" }
        return generate(context, normalized, safeTitle, content)
    }

    fun materialize(
        context: Context,
        assistant: AiUiMessage,
        directRequest: String = "",
    ): AiGeneratedFile? {
        val source = assistant.content
        val start = source.indexOf(FENCE_OPEN)
        if (start < 0) {
            return materializeDirect(context, assistant, directRequest)
        }
        val bodyStart = start + FENCE_OPEN.length
        val end = source.indexOf(FENCE_CLOSE, bodyStart)
        if (end < 0) {
            return materializeDirect(context, assistant, directRequest)
        }
        val json = runCatching {
            JSONObject(source.substring(bodyStart, end).trim())
        }.getOrNull()
        if (json == null) {
            return materializeDirect(context, assistant, directRequest)
        }
        val type = normalizeType(json.optString("type"))
        val title = json.optString("title").ifBlank { "Mote 生成文件" }
        val content = json.optString("content")
        if (type == null || content.isBlank()) {
            return materializeDirect(context, assistant, directRequest)
        }
        val result = runCatching { generate(context, type, title, content) }
        val file = result.getOrNull()
        val replacement = if (file != null) {
            "已生成文件：Download/Mote/${file.name}"
        } else {
            "> **文件生成失败**：" +
                    (result.exceptionOrNull()?.message ?: "未知错误")
        }
        assistant.content = buildString {
            append(source, 0, start)
            append(replacement)
            append(source, end + FENCE_CLOSE.length, source.length)
        }
        return file
    }

    /** 模型没有按约定输出代码块时，按用户意图直接把回答整理成文件。 */
    private fun materializeDirect(
        context: Context,
        assistant: AiUiMessage,
        request: String,
    ): AiGeneratedFile? {
        if (request.isBlank()) return null
        val wantsGenerate = request.contains("生成") ||
                request.contains("做一份") ||
                request.contains("写一份") ||
                request.contains("导出")
        if (!wantsGenerate) return null
        val type = when {
            request.contains("ppt", ignoreCase = true) ||
                    request.contains("幻灯片") -> "pptx"
            request.contains("表格") ||
                    request.contains("excel", ignoreCase = true) ||
                    request.contains("xlsx", ignoreCase = true) -> "xlsx"
            request.contains("word", ignoreCase = true) ||
                    request.contains("文档") -> "docx"
            else -> return null
        }
        val content = assistant.content.trim()
        if (content.length < 80) return null
        val title = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trimStart('#', ' ')
            ?.take(50)
            ?.ifBlank { "Mote 生成文件" }
            ?: "Mote 生成文件"
        val result = runCatching { generate(context, type, title, content) }
        val file = result.getOrNull()
        assistant.content = content + if (file != null) {
            "\n\n已生成文件：Download/Mote/${file.name}"
        } else {
            "\n\n> **文件生成失败**：" +
                    (result.exceptionOrNull()?.message ?: "未知错误")
        }
        return file
    }

    private fun normalizeType(raw: String): String? = when (raw.trim().lowercase()) {
        "pptx", "ppt", "powerpoint", "幻灯片" -> "pptx"
        "docx", "doc", "word", "文档" -> "docx"
        "xlsx", "xls", "excel", "表格" -> "xlsx"
        else -> null
    }

    private fun generate(
        context: Context,
        type: String,
        title: String,
        content: String,
    ): AiGeneratedFile? {
        val safeTitle = title.map { ch ->
            if (ch == '\\' || ch == '/' || ch == ':' || ch == '*' || ch == '?' ||
                ch == '"' || ch == '<' || ch == '>' || ch == '|'
            ) {
                '_'
            } else {
                ch
            }
        }.joinToString("").take(80)
        val fileName = when (type) {
            "docx" -> "$safeTitle.docx"
            "xlsx" -> "$safeTitle.xlsx"
            else -> "$safeTitle.pptx"
        }
        val bytes = when (type) {
            "docx" -> wordBytes(title, content)
            "xlsx" -> tableBytes(title, content)
            else -> pptBytes(context, title, content)
        }
        val saved = saveToDownloads(context, fileName, bytes) ?: return null
        val size = saved.length().takeIf { it > 0L } ?: bytes.size.toLong()
        return AiGeneratedFile(
            path = saved.absolutePath,
            name = fileName,
            sizeBytes = size,
            kind = aiGeneratedFileKindOf(fileName),
        )
    }

    private fun saveToDownloads(context: Context, fileName: String, bytes: ByteArray): File? {
        val direct = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Mote/$fileName",
        )
        val directWritten = runCatching {
            direct.parentFile?.mkdirs()
            direct.writeBytes(bytes)
            true
        }.getOrDefault(false)
        if (directWritten) {
            runCatching {
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(direct.absolutePath),
                    arrayOf(mimeFor(fileName)),
                    null,
                )
            }
            return direct
        }

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeFor(fileName))
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/Mote",
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values,
                ) ?: error("无法创建下载文件")
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Mote",
                ).apply { mkdirs() }
                File(dir, fileName).writeBytes(bytes)
            }
            direct
        }.getOrNull()
    }

    private fun mimeFor(name: String): String = when {
        name.endsWith(".docx") ->
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        name.endsWith(".xlsx") ->
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else ->
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    }

    private fun wordBytes(title: String, content: String): ByteArray {
        val paragraphs = buildList {
            add(title)
            addAll(content.lines())
        }.filter { it.isNotBlank() }
        val body = paragraphs.joinToString("") { line ->
            val headingLevel = line.takeWhile { it == '#' }.length.coerceIn(0, 6)
            val text = escape(line.trimStart('#', ' '))
            val style = if (headingLevel > 0) {
                "<w:pPr><w:pStyle w:val=\"Heading$headingLevel\"/></w:pPr>"
            } else {
                ""
            }
            "<w:p>$style<w:r><w:t xml:space=\"preserve\">$text</w:t></w:r></w:p>"
        }
        val document = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:body>$body<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/></w:sectPr></w:body>
</w:document>"""
        return zip(
            mapOf(
                "[Content_Types].xml" to contentTypes(
                    """<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>""",
                ),
                "_rels/.rels" to rootRels("word/document.xml"),
                "word/document.xml" to document,
            ),
        )
    }

    private fun tableBytes(title: String, content: String): ByteArray {
        val rows = content.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                line.split('\t', ',', '|')
                    .map { it.trim().trim('|').trim() }
                    .filter { it.isNotEmpty() }
            }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(listOf(title)) }
        val sheetRows = buildString {
            append("<row r=\"1\">")
            rows.first().forEachIndexed { index, value ->
                append("<c r=\"${column(index)}1\" t=\"inlineStr\"><is><t>${escape(value)}</t></is></c>")
            }
            append("</row>")
            rows.drop(1).forEachIndexed { rowIndex, cells ->
                val r = rowIndex + 2
                append("<row r=\"$r\">")
                cells.forEachIndexed { index, value ->
                    append("<c r=\"${column(index)}$r\" t=\"inlineStr\"><is><t>${escape(value)}</t></is></c>")
                }
                append("</row>")
            }
        }
        val sheet = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$sheetRows</sheetData></worksheet>"""
        val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets></workbook>"""
        return zip(
            mapOf(
                "[Content_Types].xml" to contentTypes(
                    """<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""",
                ),
                "_rels/.rels" to rootRels("xl/workbook.xml"),
                "xl/workbook.xml" to workbook,
                "xl/_rels/workbook.xml.rels" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""",
                "xl/worksheets/sheet1.xml" to sheet,
            ),
        )
    }

    private fun pptBytes(context: Context, title: String, content: String): ByteArray {
        val template = context.assets.open("ai_pptx_template.pptx").use { it.readBytes() }
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(template)).use { zip ->
            val buffer = ByteArray(8192)
            while (true) {
                val entry = zip.nextEntry ?: break
                val output = ByteArrayOutputStream()
                while (true) {
                    val count = zip.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                }
                entries[entry.name] = output.toByteArray()
                zip.closeEntry()
            }
        }
        val slides = splitSlides(title, content)
        for (index in 1..10) {
            val slide = slides.getOrNull(index - 1)
            val slideTitle = slide?.first.orEmpty()
            val slideBody = slide?.second.orEmpty()
            val key = "ppt/slides/slide$index.xml"
            val xml = entries[key]?.toString(Charsets.UTF_8) ?: continue
            entries[key] = xml
                .replace("{{TITLE_$index}}", escape(slideTitle))
                .replace("{{BODY_$index}}", escape(slideBody))
                .toByteArray(Charsets.UTF_8)
        }
        return zipBytes(entries)
    }

    private fun splitSlides(title: String, content: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var currentTitle = title
        val body = StringBuilder()
        content.lines().forEach { line ->
            if (line.startsWith("#") || line.startsWith("## ")) {
                if (body.isNotBlank()) {
                    result.add(currentTitle to body.toString().trim())
                    body.clear()
                }
                currentTitle = line.trimStart('#', ' ').ifBlank { title }
            } else {
                body.append(line).append('\n')
            }
        }
        if (body.isNotBlank() || result.isEmpty()) {
            result.add(currentTitle to body.toString().trim())
        }
        return result.take(10).map { (t, b) ->
            (t.ifBlank { title }) to b.take(2_500)
        }
    }

    private fun zip(files: Map<String, String>): ByteArray =
        zipBytes(files.mapValues { it.value.toByteArray(Charsets.UTF_8) })

    private fun zipBytes(files: Map<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            files.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun contentTypes(overrides: String): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>$overrides</Types>"""

    private fun rootRels(target: String): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="$target"/>
</Relationships>"""

    private fun column(index: Int): String {
        var value = index + 1
        val builder = StringBuilder()
        while (value > 0) {
            val rem = (value - 1) % 26
            builder.append(('A'.code + rem).toChar())
            value = (value - 1) / 26
        }
        return builder.reverse().toString()
    }

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
