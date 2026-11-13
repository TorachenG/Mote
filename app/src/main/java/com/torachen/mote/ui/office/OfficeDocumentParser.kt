package com.torachen.mote.ui.office

import java.io.File
import java.util.zip.ZipFile

/** 支持预览的 Office 文档类型。 */
internal enum class OfficeKind {
    WORD,
    EXCEL,
    PPT,
}

internal data class WordParagraph(
    val text: String,
    /** 0 = 正文，1~6 = 标题级别 */
    val heading: Int = 0,
)

internal data class SheetData(
    val name: String,
    val rows: List<List<String>>,
)

internal data class SlideData(
    val index: Int,
    val title: String,
    val lines: List<String>,
)

internal sealed interface OfficePreviewContent {
    data class Word(val paragraphs: List<WordParagraph>) : OfficePreviewContent
    data class Excel(val sheets: List<SheetData>) : OfficePreviewContent
    data class Ppt(val slides: List<SlideData>) : OfficePreviewContent
}

/**
 * 解析 OOXML（docx / xlsx / pptx）成可渲染的简单结构。
 *
 * 不依赖任何 Office 库：直接读取 zip 里的 XML。
 * 目标是“能看懂内容”的预览，而不是像素级还原排版。
 */
internal object OfficeDocumentParser {

    private const val MAX_PARAGRAPHS = 3_000
    private const val MAX_SHEETS = 8
    private const val MAX_ROWS = 200
    private const val MAX_COLUMNS = 32
    private const val MAX_SLIDES = 80
    private const val MAX_XML_CHARS = 12_000_000

    private val dotAll = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)

    fun kindOf(fileName: String): OfficeKind? = when (fileName.substringAfterLast('.', "").lowercase()) {
        "docx" -> OfficeKind.WORD
        "xlsx" -> OfficeKind.EXCEL
        "pptx" -> OfficeKind.PPT
        else -> null
    }

    fun titleOf(kind: OfficeKind): String = when (kind) {
        OfficeKind.WORD -> "Word 文档"
        OfficeKind.EXCEL -> "Excel 表格"
        OfficeKind.PPT -> "PPT 演示文稿"
    }

    fun parse(file: File, kind: OfficeKind): OfficePreviewContent {
        ZipFile(file).use { zip ->
            return when (kind) {
                OfficeKind.WORD -> OfficePreviewContent.Word(parseDocx(zip))
                OfficeKind.EXCEL -> OfficePreviewContent.Excel(parseXlsx(zip))
                OfficeKind.PPT -> OfficePreviewContent.Ppt(parsePptx(zip))
            }
        }
    }

    // ============================================================
    // Word
    // ============================================================

    private fun parseDocx(zip: ZipFile): List<WordParagraph> {
        val xml = zip.readEntry("word/document.xml") ?: return emptyList()
        val paragraphs = mutableListOf<WordParagraph>()
        val blockRegex = Regex("<w:(p|tbl)(?:\\s[^>]*)?>(.*?)</w:\\1>", dotAll)
        blockRegex.findAll(xml).forEach { match ->
            if (paragraphs.size >= MAX_PARAGRAPHS) return@forEach
            when (match.groupValues[1].lowercase()) {
                "tbl" -> paragraphs.addAll(extractTableRows(match.groupValues[2]))
                else -> {
                    val inner = match.groupValues[2]
                    val text = extractWordText(inner)
                    if (text.isNotBlank()) {
                        val style = Regex("<w:pStyle[^>]*w:val=\"([^\"]+)\"", dotAll)
                            .find(inner)?.groupValues?.get(1)
                        paragraphs.add(WordParagraph(text.trim(), headingLevel(style)))
                    }
                }
            }
        }
        return paragraphs
    }

    private fun extractWordText(inner: String): String {
        val builder = StringBuilder()
        val tokenRegex = Regex(
            "<w:(t|tab|br)(?:\\s[^>]*)?(?:/>|>(.*?)</w:\\1>)",
            dotAll,
        )
        tokenRegex.findAll(inner).forEach { token ->
            when (token.groupValues[1].lowercase()) {
                "tab" -> builder.append('\t')
                "br" -> builder.append('\n')
                else -> builder.append(unescapeXml(token.groupValues[2]))
            }
        }
        return builder.toString()
    }

    private fun extractTableRows(tableXml: String): List<WordParagraph> {
        val rows = mutableListOf<WordParagraph>()
        Regex("<w:tr(?:\\s[^>]*)?>(.*?)</w:tr>", dotAll).findAll(tableXml).forEach { row ->
            val cells = Regex("<w:tc(?:\\s[^>]*)?>(.*?)</w:tc>", dotAll)
                .findAll(row.groupValues[1])
                .map { extractWordText(it.groupValues[1]).trim() }
                .toList()
            if (cells.isNotEmpty()) {
                rows.add(WordParagraph(cells.joinToString("    "), heading = 0))
            }
        }
        return rows
    }

    private fun headingLevel(style: String?): Int {
        if (style.isNullOrBlank()) return 0
        val lower = style.lowercase()
        if (!lower.contains("heading") && !lower.contains("标题") && !lower.startsWith("h")) {
            return 0
        }
        return Regex("([1-6])").find(lower)?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(1, 6) ?: 1
    }

    // ============================================================
    // Excel
    // ============================================================

    private fun parseXlsx(zip: ZipFile): List<SheetData> {
        val sharedStrings = zip.readEntry("xl/sharedStrings.xml")
            ?.let(::parseSharedStrings)
            .orEmpty()

        val workbook = zip.readEntry("xl/workbook.xml")
        val rels = zip.readEntry("xl/_rels/workbook.xml.rels")
        val targets = parseRelationships(rels)
        val sheets = parseSheetRefs(workbook, targets)
        val entries = sheets.ifEmpty {
            zip.entries().asSequence()
                .filter { Regex("xl/worksheets/sheet\\d+\\.xml", RegexOption.IGNORE_CASE).matches(it.name) }
                .sortedBy { entryNumber(it.name, "sheet") }
                .map { it.name to ("Sheet" + entryNumber(it.name, "sheet")) }
                .toList()
        }

        val result = mutableListOf<SheetData>()
        for ((path, name) in entries.take(MAX_SHEETS)) {
            val xml = zip.readEntry(path) ?: continue
            val rows = parseSheetRows(xml, sharedStrings)
            result.add(SheetData(name = name.ifBlank { path.substringAfterLast('/') }, rows = rows))
        }
        return result
    }

    private fun parseSharedStrings(xml: String): List<String> =
        Regex("<si(?:\\s[^>]*)?>(.*?)</si>", dotAll).findAll(xml).map { item ->
            Regex("<t(?:\\s[^>]*)?>(.*?)</t>", dotAll)
                .findAll(item.groupValues[1])
                .joinToString("") { unescapeXml(it.groupValues[1]) }
        }.toList()

    private fun parseRelationships(xml: String?): Map<String, String> {
        if (xml.isNullOrBlank()) return emptyMap()
        return Regex("<Relationship\\b([^>]*)/?>", dotAll).findAll(xml).mapNotNull { match ->
            val attrs = match.groupValues[1]
            val id = xmlAttr(attrs, "Id") ?: return@mapNotNull null
            val target = xmlAttr(attrs, "Target") ?: return@mapNotNull null
            id to normalizePartPath(target)
        }.toMap()
    }

    private fun parseSheetRefs(workbook: String?, targets: Map<String, String>): List<Pair<String, String>> {
        if (workbook.isNullOrBlank()) return emptyList()
        return Regex("<sheet\\b([^>]*)/?>", dotAll).findAll(workbook).mapNotNull { match ->
            val attrs = match.groupValues[1]
            val name = xmlAttr(attrs, "name").orEmpty()
            val relId = xmlAttr(attrs, "r:id")
            val target = relId?.let { targets[it] } ?: return@mapNotNull null
            target to name
        }.toList()
    }

    private fun parseSheetRows(xml: String, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        Regex("<row\\b[^>]*>(.*?)</row>", dotAll).findAll(xml).forEach { rowMatch ->
            if (rows.size >= MAX_ROWS) return@forEach
            val cells = sortedMapOf<Int, String>()
            var nextColumn = 0
            Regex("<c\\b([^>]*)>(.*?)</c>", dotAll).findAll(rowMatch.groupValues[1]).forEach { cellMatch ->
                val attrs = cellMatch.groupValues[1]
                val inner = cellMatch.groupValues[2]
                val reference = xmlAttr(attrs, "r")
                val type = xmlAttr(attrs, "t")
                val column = reference?.let(::columnIndex)
                    ?.takeIf { it in 0 until MAX_COLUMNS }
                    ?: nextColumn.takeIf { it < MAX_COLUMNS }
                    ?: return@forEach
                nextColumn = column + 1
                if (cells.size >= MAX_COLUMNS) return@forEach
                val raw = Regex("<v(?:\\s[^>]*)?>(.*?)</v>", dotAll)
                    .find(inner)?.groupValues?.get(1)
                val inline = Regex("<is(?:\\s[^>]*)?>(.*?)</is>", dotAll)
                    .find(inner)?.groupValues?.get(1)
                    ?.let { isXml ->
                        Regex("<t(?:\\s[^>]*)?>(.*?)</t>", dotAll)
                            .findAll(isXml)
                            .joinToString("") { unescapeXml(it.groupValues[1]) }
                    }
                val value = when (type?.lowercase()) {
                    "s" -> raw?.toIntOrNull()?.let { sharedStrings.getOrNull(it) }.orEmpty()
                    "inlinestr" -> inline.orEmpty()
                    "str" -> unescapeXml(raw.orEmpty())
                    "b" -> if (raw == "1") "TRUE" else "FALSE"
                    else -> unescapeXml(raw.orEmpty())
                }
                cells[column] = value
            }
            if (cells.isNotEmpty()) {
                val columns = cells.lastKey() + 1
                rows.add(List(columns) { cells[it] ?: "" })
            }
        }
        return rows
    }

    private fun columnIndex(reference: String): Int {
        var value = 0
        var letters = 0
        for (char in reference) {
            if (!char.isLetter()) break
            value = value * 26 + (char.uppercaseChar() - 'A' + 1)
            letters++
        }
        if (letters == 0) return -1
        return value - 1
    }

    // ============================================================
    // PPT
    // ============================================================

    private fun parsePptx(zip: ZipFile): List<SlideData> {
        val entries = zip.entries().asSequence()
            .filter { Regex("ppt/slides/slide\\d+\\.xml", RegexOption.IGNORE_CASE).matches(it.name) }
            .sortedBy { entryNumber(it.name, "slide") }
            .take(MAX_SLIDES)
            .toList()

        val slides = mutableListOf<SlideData>()
        entries.forEachIndexed { index, entry ->
            val xml = zip.readEntry(entry.name) ?: return@forEachIndexed
            val texts = Regex("<a:t(?:\\s[^>]*)?>(.*?)</a:t>", dotAll)
                .findAll(xml)
                .map { unescapeXml(it.groupValues[1]).trim() }
                .filter { it.isNotEmpty() }
                .toList()
            val title = findSlideTitle(xml) ?: texts.firstOrNull().orEmpty()
            val body = if (title.isNotEmpty()) {
                val mutable = texts.toMutableList()
                val titleIndex = mutable.indexOfFirst { it == title }
                if (titleIndex >= 0) mutable.removeAt(titleIndex)
                mutable
            } else {
                texts
            }
            slides.add(SlideData(index = index + 1, title = title, lines = body))
        }
        return slides
    }

    private fun findSlideTitle(slideXml: String): String? {
        Regex("<p:sp\\b.*?</p:sp>", dotAll).findAll(slideXml).forEach { shape ->
            val value = shape.value
            if (value.contains("<p:ph") &&
                (value.contains("type=\"title\"") || value.contains("type=\"ctrTitle\""))
            ) {
                val text = Regex("<a:t(?:\\s[^>]*)?>(.*?)</a:t>", dotAll)
                    .find(value)?.groupValues?.get(1)
                    ?.let(::unescapeXml)?.trim()
                if (!text.isNullOrEmpty()) return text
            }
        }
        return null
    }

    // ============================================================
    // 工具
    // ============================================================

    private fun ZipFile.readEntry(name: String): String? {
        val entry = getEntry(name) ?: return null
        if (entry.size > MAX_XML_CHARS) return null
        return runCatching {
            getInputStream(entry).use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
        }.getOrNull()
    }

    private fun normalizePartPath(target: String): String {
        val cleaned = target.trim().replace('\\', '/').removePrefix("/")
        return if (cleaned.startsWith("xl/")) cleaned else "xl/$cleaned"
    }

    private fun xmlAttr(attrs: String, name: String): String? =
        Regex("(?:^|\\s)${Regex.escape(name)}=\"([^\"]*)\"").find(attrs)?.groupValues?.get(1)

    private fun entryNumber(name: String, prefix: String): Int =
        Regex("$prefix(\\d+)\\.xml", RegexOption.IGNORE_CASE)
            .find(name)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    private fun unescapeXml(raw: String): String {
        if (raw.isEmpty()) return raw
        var text = raw
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
        text = Regex("&#x([0-9a-fA-F]{1,6});").replace(text) { match ->
            codePointToString(match.groupValues[1].toIntOrNull(16))
        }
        text = Regex("&#(\\d{1,7});").replace(text) { match ->
            codePointToString(match.groupValues[1].toIntOrNull())
        }
        return text.replace("&amp;", "&")
    }

    private fun codePointToString(code: Int?): String {
        if (code == null || code <= 0 || code > 0x10FFFF) return ""
        return runCatching { String(Character.toChars(code)) }.getOrDefault("")
    }
}
