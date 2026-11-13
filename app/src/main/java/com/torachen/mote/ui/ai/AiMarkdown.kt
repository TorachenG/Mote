@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 对话 Markdown 渲染器（无第三方依赖）。
 *
 * 支持：
 * - 块级：标题、段落、无序 / 有序 / 任务列表（可嵌套、可续行）、引用（可嵌套）、
 *   提示框 `> [!NOTE]`、分隔线、围栏代码块（``` / ~~~，带复制、横向滚动和语法高亮）、
 *   **真表格**（表头、对齐、边框、自适应列宽、过宽横向滚动）、独立图片（http 或 data:image）、
 *   公式块 `$$...$$` / `\[...\]`、Mermaid 图表（```mermaid，内置离线渲染）；
 * - 行内：粗体、斜体、粗斜体、删除线、高亮 `==text==`、行内代码、可点击链接、
 *   裸链接 / `<https://…>` 自动链接、行内图片占位、`<br>` 换行、行内公式 `$...$`。
 *
 * 输出的是「块」，代码块、表格、列表、图片各自拿到合适的排版，
 * 而不是全部塞进一个 Text。
 */
internal data class AiMarkdownPalette(
    val text: Color,
    val secondary: Color,
    val accent: Color,
    val codeBackground: Color,
    val codeText: Color,
    val divider: Color,
    val warning: Color = Color(0xFFE6A23C),
)

internal sealed interface AiMdBlock {
    data class Paragraph(val text: AnnotatedString) : AiMdBlock
    data class Heading(val level: Int, val text: AnnotatedString) : AiMdBlock
    data class Code(val code: String, val language: String) : AiMdBlock
    data class Quote(val level: Int, val text: AnnotatedString) : AiMdBlock
    data class Callout(val kind: String, val text: AnnotatedString) : AiMdBlock
    data class ListBlock(val items: List<AiMdListItem>) : AiMdBlock
    data class Table(
        val header: List<AnnotatedString>,
        val alignments: List<AiTableAlign>,
        val rows: List<List<AnnotatedString>>,
    ) : AiMdBlock {
        val columnCount: Int
            get() = maxOf(
                header.size,
                alignments.size,
                rows.maxOfOrNull { it.size } ?: 0,
            )
    }

    data class Image(val alt: String, val url: String) : AiMdBlock
    data class Math(val latex: String) : AiMdBlock
    data object Divider : AiMdBlock
}

internal data class AiMdListItem(
    val marker: String,
    val level: Int,
    val text: AnnotatedString,
)

private const val URL_TAG = "MOTE_MD_URL"

private val headingRegex = Regex("^(#{1,6})\\s+(.*)$")
private val unorderedListRegex = Regex("^(\\s*)([-*+•])\\s+(.*)$")
private val orderedListRegex = Regex("^(\\s*)(\\d{1,3})[.)]\\s+(.*)$")
private val taskListRegex = Regex("^(\\s*)[-*+]\\s+\\[([ xX])]\\s+(.*)$")
private val quoteRegex = Regex("^\\s*>.*$")
private val dividerRegex = Regex("^\\s*([-*_])\\s*(\\1\\s*){2,}$")
private val blockImageRegex = Regex("""^!\[([^\]]*)]\(\s*(\S+?)(?:\s+"[^"]*")?\s*\)$""")
private val calloutRegex = Regex("^\\[!(\\w+)]$")

// ============================================================
// 解析
// ============================================================

internal fun parseAiMarkdown(
    source: String,
    palette: AiMarkdownPalette,
    baseFontSize: TextUnit = 16.sp,
): List<AiMdBlock> {
    val blocks = mutableListOf<AiMdBlock>()
    val paragraph = StringBuilder()

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        paragraph.setLength(0)
        if (text.isNotEmpty()) {
            blocks.add(AiMdBlock.Paragraph(inlineMarkdown(text, palette, baseFontSize)))
        }
    }

    val lines = source
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    var index = 0
    while (index < lines.size) {
        val line = lines[index].trimEnd()
        val trimmed = line.trimStart()

        val fence = when {
            trimmed.startsWith("```") -> "```"
            trimmed.startsWith("~~~") -> "~~~"
            else -> null
        }
        if (fence != null) {
            flushParagraph()
            val language = trimmed.removePrefix(fence).trim()
            val code = StringBuilder()
            index++
            while (index < lines.size && !lines[index].trimStart().startsWith(fence)) {
                code.append(lines[index]).append('\n')
                index++
            }
            if (index < lines.size) index++
            blocks.add(AiMdBlock.Code(code.toString().trimEnd('\n'), language))
            continue
        }

        val mathBlock = matchMathBlock(lines, index)
        if (mathBlock != null) {
            flushParagraph()
            blocks.add(AiMdBlock.Math(mathBlock.latex))
            index = mathBlock.nextIndex
            continue
        }

        when {
            line.isBlank() -> {
                flushParagraph()
                index++
            }

            headingRegex.matches(trimmed) -> {
                flushParagraph()
                val match = headingRegex.matchEntire(trimmed)!!
                val level = match.groupValues[1].length.coerceIn(1, 6)
                val title = match.groupValues[2].trimEnd('#', ' ')
                blocks.add(
                    AiMdBlock.Heading(
                        level = level,
                        text = inlineMarkdown(title, palette, baseFontSize),
                    ),
                )
                index++
            }

            dividerRegex.matches(trimmed) -> {
                flushParagraph()
                blocks.add(AiMdBlock.Divider)
                index++
            }

            isTableStart(lines, index) -> {
                flushParagraph()
                val headerCells = AiMarkdownTables.splitRow(lines[index])
                index++
                val alignments = AiMarkdownTables.alignments(lines[index], headerCells.size)
                index++
                val rawRows = mutableListOf<List<String>>()
                while (index < lines.size) {
                    val next = lines[index]
                    if (next.isBlank() || !next.contains('|')) break
                    if (startsNewBlock(next.trimStart())) break
                    rawRows.add(AiMarkdownTables.splitRow(next))
                    index++
                }
                val columns = maxOf(
                    headerCells.size,
                    alignments.size,
                    rawRows.maxOfOrNull { it.size } ?: 0,
                ).coerceAtLeast(1)
                val header = List(columns) { column ->
                    inlineMarkdown(headerCells.getOrElse(column) { "" }, palette, baseFontSize)
                }
                val aligns = List(columns) { column ->
                    alignments.getOrElse(column) { AiTableAlign.Start }
                }
                val rows = rawRows.map { row ->
                    List(columns) { column ->
                        inlineMarkdown(row.getOrElse(column) { "" }, palette, baseFontSize)
                    }
                }
                blocks.add(AiMdBlock.Table(header = header, alignments = aligns, rows = rows))
            }

            blockImageRegex.matches(trimmed) -> {
                flushParagraph()
                val match = blockImageRegex.matchEntire(trimmed)!!
                val url = sanitizeImageUrl(match.groupValues[2])
                if (url != null) {
                    blocks.add(AiMdBlock.Image(alt = match.groupValues[1], url = url))
                } else {
                    blocks.add(AiMdBlock.Paragraph(inlineMarkdown(trimmed, palette, baseFontSize)))
                }
                index++
            }

            quoteRegex.matches(line) -> {
                flushParagraph()
                val quoteLines = mutableListOf<String>()
                var level = 1
                while (index < lines.size) {
                    val quoteLine = lines[index].trim()
                    if (!quoteRegex.matches(quoteLine)) break
                    var rest = quoteLine
                    var depth = 0
                    while (rest.startsWith(">")) {
                        depth++
                        rest = rest.removePrefix(">").trimStart()
                    }
                    if (depth > 0) level = maxOf(level, depth)
                    quoteLines.add(rest)
                    index++
                }
                val text = inlineMarkdown(quoteLines.joinToString("\n"), palette, baseFontSize)
                val callout = calloutRegex.matchEntire(quoteLines.firstOrNull()?.trim().orEmpty())
                    ?.groupValues?.get(1)?.lowercase()
                if (callout != null) {
                    quoteLines.removeAt(0)
                    blocks.add(
                        AiMdBlock.Callout(
                            kind = callout,
                            text = inlineMarkdown(quoteLines.joinToString("\n"), palette, baseFontSize),
                        ),
                    )
                } else {
                    blocks.add(AiMdBlock.Quote(level = level, text = text))
                }
            }

            parseListLine(line) != null -> {
                flushParagraph()
                val items = mutableListOf<AiMdListItem>()
                var current = parseListLine(line)!!
                val itemText = StringBuilder(current.text)
                index++
                while (index < lines.size) {
                    val nextRaw = lines[index]
                    if (nextRaw.isBlank()) break
                    val nextLine = nextRaw.trimEnd()
                    val nextItem = parseListLine(nextLine)
                    if (nextItem != null) {
                        items.add(
                            AiMdListItem(
                                marker = current.marker,
                                level = current.level,
                                text = inlineMarkdown(itemText.toString().trim(), palette, baseFontSize),
                            ),
                        )
                        current = nextItem
                        itemText.setLength(0)
                        itemText.append(nextItem.text)
                        index++
                        continue
                    }
                    val nextTrimmed = nextLine.trimStart()
                    if (startsNewBlock(nextTrimmed) && !nextLine.startsWith("  ")) break
                    itemText.append('\n').append(nextTrimmed)
                    index++
                }
                items.add(
                    AiMdListItem(
                        marker = current.marker,
                        level = current.level,
                        text = inlineMarkdown(itemText.toString().trim(), palette, baseFontSize),
                    ),
                )
                blocks.add(AiMdBlock.ListBlock(items))
            }

            else -> {
                if (paragraph.isNotEmpty()) paragraph.append('\n')
                paragraph.append(trimmed)
                index++
            }
        }
    }
    flushParagraph()
    return blocks
}

private data class ParsedListLine(
    val level: Int,
    val marker: String,
    val text: String,
)

private fun parseListLine(line: String): ParsedListLine? {
    taskListRegex.matchEntire(line)?.let { match ->
        val level = (match.groupValues[1].length / 2).coerceIn(0, 4)
        val checked = match.groupValues[2].equals("x", ignoreCase = true)
        return ParsedListLine(level, if (checked) "☑" else "☐", match.groupValues[3])
    }
    unorderedListRegex.matchEntire(line)?.let { match ->
        val level = (match.groupValues[1].length / 2).coerceIn(0, 4)
        return ParsedListLine(level, "•", match.groupValues[3])
    }
    orderedListRegex.matchEntire(line)?.let { match ->
        val level = (match.groupValues[1].length / 2).coerceIn(0, 4)
        return ParsedListLine(level, match.groupValues[2] + ".", match.groupValues[3])
    }
    return null
}

private fun startsNewBlock(trimmed: String): Boolean =
    trimmed.startsWith("```") ||
            trimmed.startsWith("~~~") ||
            headingRegex.matches(trimmed) ||
            dividerRegex.matches(trimmed) ||
            quoteRegex.matches(trimmed) ||
            parseListLine(trimmed) != null ||
            trimmed.startsWith("![")

private data class AiMathBlockMatch(
    val latex: String,
    val nextIndex: Int,
)

/** 识别独立的 `$$...$$` / `\[...\]` 公式块。 */
private fun matchMathBlock(lines: List<String>, index: Int): AiMathBlockMatch? {
    val line = lines[index].trim()
    if (line == "$$" || line == "\\[") {
        val close = if (line == "$$") "$$" else "\\]"
        val body = StringBuilder()
        var cursor = index + 1
        while (cursor < lines.size && lines[cursor].trim() != close) {
            if (body.isNotEmpty()) body.append('\n')
            body.append(lines[cursor].trim())
            cursor++
        }
        if (cursor >= lines.size) return null
        return AiMathBlockMatch(body.toString().trim(), cursor + 1)
    }
    if (line.length > 4 && line.startsWith("$$") && line.endsWith("$$")) {
        return AiMathBlockMatch(line.substring(2, line.length - 2).trim(), index + 1)
    }
    if (line.length > 4 && line.startsWith("\\[") && line.endsWith("\\]")) {
        return AiMathBlockMatch(line.substring(2, line.length - 2).trim(), index + 1)
    }
    return null
}

// ---------- 表格 ----------

private fun isTableStart(lines: List<String>, index: Int): Boolean {
    val header = lines.getOrNull(index) ?: return false
    if (!header.contains('|')) return false
    val separator = lines.getOrNull(index + 1) ?: return false
    return AiMarkdownTables.isSeparator(separator)
}

// ============================================================
// 行内语法
// ============================================================

private fun inlineMarkdown(
    text: String,
    palette: AiMarkdownPalette,
    baseFontSize: TextUnit,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    appendInline(builder, text, palette, baseFontSize)
    return builder.toAnnotatedString()
}

private fun appendInline(
    builder: AnnotatedString.Builder,
    text: String,
    palette: AiMarkdownPalette,
    baseFontSize: TextUnit,
) {
    var index = 0
    while (index < text.length) {
        val char = text[index]
        val lineBreakEnd = if (char == '<') lineBreakTagEnd(text, index) else -1
        when {
            text.startsWith("\\(", index) || text.startsWith("\\[", index) -> {
                val closing = if (text[index + 1] == '(') "\\)" else "\\]"
                val end = text.indexOf(closing, index + 2)
                if (end > index + 2 && AiMath.looksLikeMath(text.substring(index + 2, end))) {
                    appendMath(builder, text.substring(index + 2, end))
                    index = end + closing.length
                } else {
                    builder.append(text.substring(index, index + 2))
                    index += 2
                }
            }

            char == '\\' && index + 1 < text.length -> {
                builder.append(text[index + 1])
                index += 2
            }

            lineBreakEnd > index -> {
                builder.append('\n')
                index = lineBreakEnd
            }

            text.startsWith("***", index) -> {
                val end = text.indexOf("***", index + 3)
                if (end > index + 3) {
                    builder.pushStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                    )
                    appendInline(builder, text.substring(index + 3, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 3
                } else {
                    builder.append("*")
                    index++
                }
            }

            text.startsWith("**", index) -> {
                val end = text.indexOf("**", index + 2)
                if (end > index + 2) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    appendInline(builder, text.substring(index + 2, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 2
                } else {
                    builder.append('*')
                    index++
                }
            }

            text.startsWith("__", index) -> {
                val end = text.indexOf("__", index + 2)
                if (end > index + 2) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    appendInline(builder, text.substring(index + 2, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 2
                } else {
                    builder.append('_')
                    index++
                }
            }

            text.startsWith("~~", index) -> {
                val end = text.indexOf("~~", index + 2)
                if (end > index + 2) {
                    builder.pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    appendInline(builder, text.substring(index + 2, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 2
                } else {
                    builder.append('~')
                    index++
                }
            }

            text.startsWith("==", index) -> {
                val end = text.indexOf("==", index + 2)
                if (end > index + 2) {
                    builder.pushStyle(
                        SpanStyle(background = palette.accent.copy(alpha = 0.16f)),
                    )
                    appendInline(builder, text.substring(index + 2, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 2
                } else {
                    builder.append('=')
                    index++
                }
            }

            char == '`' -> {
                val end = text.indexOf('`', index + 1)
                if (end > index + 1) {
                    builder.pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = palette.codeBackground,
                            color = palette.codeText,
                        ),
                    )
                    builder.append(text.substring(index + 1, end))
                    builder.pop()
                    index = end + 1
                } else {
                    builder.append('`')
                    index++
                }
            }

            char == '$' -> {
                val display = text.startsWith("$$", index)
                if (display) {
                    val end = text.indexOf("$$", index + 2)
                    if (end > index + 2 && AiMath.looksLikeMath(text.substring(index + 2, end))) {
                        appendMath(builder, text.substring(index + 2, end))
                        index = end + 2
                    } else {
                        builder.append("$$")
                        index += 2
                    }
                } else {
                    val end = text.indexOf('$', index + 1)
                    if (end > index + 1 && AiMath.looksLikeMath(text.substring(index + 1, end))) {
                        appendMath(builder, text.substring(index + 1, end))
                        index = end + 1
                    } else {
                        builder.append('$')
                        index++
                    }
                }
            }

            char == '!' && text.getOrNull(index + 1) == '[' -> {
                val link = parseInlineLink(text, index + 1)
                if (link != null) {
                    appendLink(
                        builder = builder,
                        label = link.label.ifBlank { "图片" },
                        url = link.url,
                        palette = palette,
                        baseFontSize = baseFontSize,
                    )
                    index = link.end + 1
                } else {
                    builder.append('!')
                    index++
                }
            }

            char == '[' -> {
                val link = parseInlineLink(text, index)
                if (link != null) {
                    appendLink(
                        builder = builder,
                        label = link.label.ifBlank { link.url },
                        url = link.url,
                        palette = palette,
                        baseFontSize = baseFontSize,
                    )
                    index = link.end + 1
                } else {
                    builder.append('[')
                    index++
                }
            }

            char == '<' -> {
                val end = text.indexOf('>', index + 1)
                val inner = if (end > index + 1) text.substring(index + 1, end) else ""
                if (inner.startsWith("http://", ignoreCase = true) ||
                    inner.startsWith("https://", ignoreCase = true)
                ) {
                    appendLink(
                        builder = builder,
                        label = inner,
                        url = inner,
                        palette = palette,
                        baseFontSize = baseFontSize,
                    )
                    index = end + 1
                } else {
                    builder.append(char)
                    index++
                }
            }

            text.startsWith("http://", index) || text.startsWith("https://", index) -> {
                var end = index
                while (end < text.length &&
                    !text[end].isWhitespace() &&
                    text[end] !in "）)]}>,，。；！？"
                ) {
                    end++
                }
                val url = text.substring(index, end).trimEnd('.', ',', ';')
                val finalEnd = index + url.length
                if (url.length > 8) {
                    appendLink(
                        builder = builder,
                        label = url,
                        url = url,
                        palette = palette,
                        baseFontSize = baseFontSize,
                    )
                } else {
                    builder.append(url)
                }
                index = finalEnd
            }

            (char == '*' || char == '_') && isItalicStart(text, index) -> {
                val end = findItalicEnd(text, index + 1, char)
                if (end > index + 1) {
                    builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    appendInline(builder, text.substring(index + 1, end), palette, baseFontSize)
                    builder.pop()
                    index = end + 1
                } else {
                    builder.append(char)
                    index++
                }
            }

            else -> {
                builder.append(char)
                index++
            }
        }
    }
}

private fun appendMath(
    builder: AnnotatedString.Builder,
    latex: String,
) {
    builder.pushStyle(
        SpanStyle(fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic),
    )
    builder.append(AiMath.convert(latex))
    builder.pop()
}

private fun appendLink(
    builder: AnnotatedString.Builder,
    label: String,
    url: String,
    palette: AiMarkdownPalette,
    baseFontSize: TextUnit,
) {
    val start = builder.length
    builder.pushStyle(
        SpanStyle(
            color = palette.accent,
            textDecoration = TextDecoration.Underline,
        ),
    )
    appendInline(builder, label, palette, baseFontSize)
    builder.pop()
    builder.addStringAnnotation(URL_TAG, url, start, builder.length)
}

private fun isItalicStart(text: String, index: Int): Boolean {
    if (index + 1 >= text.length) return false
    val next = text[index + 1]
    if (next.isWhitespace()) return false
    val previous = text.getOrNull(index - 1)
    return previous == null || previous.isWhitespace() || previous in "（([{，。！？；：、"
}

/** 找斜体结束符，跳过 `**` / `__` 这类粗体标记，避免把粗体的一部分当成斜体结尾。 */
private fun findItalicEnd(text: String, start: Int, marker: Char): Int {
    var index = start
    while (index < text.length) {
        val char = text[index]
        if (char == marker) {
            val doubled = index + 1 < text.length && text[index + 1] == marker
            if (doubled) {
                index += 2
                continue
            }
            return index
        }
        index++
    }
    return -1
}

/** 识别 `<br>` / `<br/>` / `<br />`，返回标签结束后的下标；不是换行标签时返回 -1。 */
private fun lineBreakTagEnd(text: String, index: Int): Int {
    val end = text.indexOf('>', index + 1)
    if (end !in (index + 3)..(index + 5)) return -1
    val tag = text.substring(index + 1, end).trim().lowercase()
    return if (tag == "br" || tag == "br/" || tag == "br /") end + 1 else -1
}

// ============================================================
// Compose 渲染
// ============================================================

@Composable
internal fun AiMarkdown(
    source: String,
    palette: AiMarkdownPalette,
    baseStyle: TextStyle,
    baseFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(source, palette, baseFontSize) {
        parseAiMarkdown(source, palette, baseFontSize)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is AiMdBlock.Paragraph -> {
                    AiInlineText(
                        text = block.text,
                        style = baseStyle,
                        color = palette.text,
                        lineHeight = baseFontSize * 1.65f,
                    )
                }

                is AiMdBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> baseFontSize * 1.35f
                        2 -> baseFontSize * 1.22f
                        3 -> baseFontSize * 1.12f
                        else -> baseFontSize
                    }
                    AiInlineText(
                        text = block.text,
                        style = baseStyle.copy(
                            fontSize = size,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = palette.text,
                        lineHeight = size * 1.35f,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                is AiMdBlock.Code -> {
                    if (isMermaidLanguage(block.language) &&
                        AiMermaidSupport.isSupported(LocalContext.current)
                    ) {
                        AiMermaidDiagram(
                            code = block.code,
                            dark = palette.text.luminance() > 0.5f,
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        AiCodeBlock(block = block, palette = palette)
                    }
                }

                is AiMdBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = ((block.level - 1).coerceIn(0, 3) * 10).dp)
                            .height(IntrinsicSize.Min),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(palette.accent.copy(alpha = 0.6f)),
                        )
                        AiInlineText(
                            text = block.text,
                            style = baseStyle.copy(fontStyle = FontStyle.Italic),
                            color = palette.secondary,
                            lineHeight = baseFontSize * 1.65f,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                is AiMdBlock.Callout -> {
                    val warning = block.kind == "warning" || block.kind == "caution"
                    val color = if (warning) palette.warning else palette.accent
                    val title = when (block.kind) {
                        "note" -> "提示"
                        "tip" -> "技巧"
                        "important" -> "重要"
                        "warning" -> "警告"
                        "caution" -> "注意"
                        else -> "提示"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.10f))
                            .height(IntrinsicSize.Min),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(color),
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = color,
                            )
                            if (block.text.text.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                AiInlineText(
                                    text = block.text,
                                    style = baseStyle,
                                    color = palette.text,
                                    lineHeight = baseFontSize * 1.6f,
                                )
                            }
                        }
                    }
                }

                is AiMdBlock.ListBlock -> {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = (item.level * 16).dp),
                            ) {
                                Text(
                                    text = item.marker,
                                    style = baseStyle,
                                    color = palette.accent,
                                    modifier = Modifier.width(26.dp),
                                )
                                AiInlineText(
                                    text = item.text,
                                    style = baseStyle,
                                    color = palette.text,
                                    lineHeight = baseFontSize * 1.65f,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                is AiMdBlock.Table -> {
                    AiTableBlock(
                        table = block,
                        palette = palette,
                        baseStyle = baseStyle,
                        baseFontSize = baseFontSize,
                    )
                }

                is AiMdBlock.Image -> {
                    AiImageBlock(block = block, palette = palette)
                }

                is AiMdBlock.Math -> {
                    val rendered = remember(block.latex) { AiMath.convert(block.latex) }
                    BasicText(
                        text = rendered,
                        style = baseStyle.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = baseFontSize * 1.08f,
                            color = palette.text,
                            textAlign = TextAlign.Center,
                            lineHeight = baseFontSize * 1.75f,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                }

                AiMdBlock.Divider -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(palette.divider),
                    )
                }
            }
        }
    }
}

@Composable
private fun AiCodeBlock(block: AiMdBlock.Code, palette: AiMarkdownPalette) {
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val codeTheme = remember(palette) { codeThemeFor(palette) }
    val highlighted = remember(block.code, block.language, codeTheme) {
        AiCodeHighlighter.highlight(block.code, block.language, codeTheme)
    }
    val codeStyle = remember(palette) {
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = palette.codeText,
            lineHeight = 18.sp,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.codeBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = block.language.ifBlank { "代码" },
                fontSize = 10.sp,
                color = palette.secondary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        clipboard.setText(AnnotatedString(block.code))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = "复制代码",
                    tint = palette.secondary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
        ) {
            BasicText(
                text = highlighted,
                style = codeStyle,
            )
        }
    }
}

@Composable
private fun AiInlineText(
    text: AnnotatedString,
    style: TextStyle,
    color: Color,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val hasLink = remember(text) {
        text.getStringAnnotations(URL_TAG, 0, text.length).isNotEmpty()
    }
    val resolvedStyle = style.copy(color = color, lineHeight = lineHeight, textAlign = textAlign)
    if (!hasLink) {
        BasicText(
            text = text,
            style = resolvedStyle,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicText(
        text = text,
        style = resolvedStyle,
        modifier = modifier.pointerInput(text) {
            detectTapGestures { position ->
                val layout = layoutResult ?: return@detectTapGestures
                val offset = layout.getOffsetForPosition(position)
                    .coerceIn(0, (text.length - 1).coerceAtLeast(0))
                val url = text.getStringAnnotations(URL_TAG, offset, offset)
                    .firstOrNull()
                    ?.item
                if (!url.isNullOrBlank()) {
                    if (!AiAppLauncher.openUrl(context, url)) {
                        runCatching { uriHandler.openUri(url) }
                    }
                }
            }
        },
        onTextLayout = { layoutResult = it },
    )
}

// ---------- 表格渲染 ----------

private data class AiTableMetrics(
    val widths: List<Dp>,
    val tableWidth: Dp,
    val scrollable: Boolean,
)

@Composable
private fun AiTableBlock(
    table: AiMdBlock.Table,
    palette: AiMarkdownPalette,
    baseStyle: TextStyle,
    baseFontSize: TextUnit,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val headerStyle = baseStyle.copy(
        fontSize = baseFontSize * 0.92f,
        fontWeight = FontWeight.SemiBold,
        lineHeight = baseFontSize * 1.4f,
    )
    val cellStyle = baseStyle.copy(
        fontSize = baseFontSize * 0.92f,
        lineHeight = baseFontSize * 1.4f,
    )
    val shape = RoundedCornerShape(8.dp)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val availablePx = with(density) { availableWidth.toPx() }
        val metrics = remember(table, availablePx, headerStyle, cellStyle, density) {
            measureTable(table, measurer, availablePx, density, headerStyle, cellStyle)
        }
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (metrics.scrollable) Modifier.horizontalScroll(scrollState) else Modifier,
                ),
        ) {
            Column(
                modifier = Modifier
                    .width(if (metrics.scrollable) metrics.tableWidth else availableWidth)
                    .clip(shape)
                    .border(1.dp, palette.divider, shape),
            ) {
                AiTableRow(
                    cells = table.header,
                    widths = metrics.widths,
                    alignments = table.alignments,
                    header = true,
                    palette = palette,
                    headerStyle = headerStyle,
                    cellStyle = cellStyle,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.divider),
                )
                table.rows.forEachIndexed { rowIndex, row ->
                    AiTableRow(
                        cells = row,
                        widths = metrics.widths,
                        alignments = table.alignments,
                        header = false,
                        palette = palette,
                        headerStyle = headerStyle,
                        cellStyle = cellStyle,
                    )
                    if (rowIndex != table.rows.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(palette.divider.copy(alpha = 0.6f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiTableRow(
    cells: List<AnnotatedString>,
    widths: List<Dp>,
    alignments: List<AiTableAlign>,
    header: Boolean,
    palette: AiMarkdownPalette,
    headerStyle: TextStyle,
    cellStyle: TextStyle,
) {
    val dividerColor = palette.divider
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (header) palette.codeBackground else Color.Transparent)
            .drawBehind {
                val stroke = 1.dp.toPx()
                var x = 0f
                widths.forEachIndexed { index, width ->
                    x += width.toPx()
                    if (index != widths.lastIndex) {
                        drawLine(
                            color = dividerColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = stroke,
                        )
                    }
                }
            },
    ) {
        cells.forEachIndexed { column, cell ->
            AiInlineText(
                text = cell,
                style = if (header) headerStyle else cellStyle,
                color = palette.text,
                lineHeight = (if (header) headerStyle else cellStyle).lineHeight,
                modifier = Modifier
                    .width(widths.getOrElse(column) { 0.dp })
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                textAlign = when (alignments.getOrElse(column) { AiTableAlign.Start }) {
                    AiTableAlign.Start -> TextAlign.Start
                    AiTableAlign.Center -> TextAlign.Center
                    AiTableAlign.End -> TextAlign.End
                },
            )
        }
    }
}

private fun measureTable(
    table: AiMdBlock.Table,
    measurer: TextMeasurer,
    availablePx: Float,
    density: Density,
    headerStyle: TextStyle,
    cellStyle: TextStyle,
): AiTableMetrics {
    val columns = table.columnCount.coerceAtLeast(1)
    val paddingPx = with(density) { 9.dp.toPx() } * 2
    val borderPx = with(density) { 1.dp.toPx() }
    val chrome = borderPx * (columns + 1)
    val densityScale = density.density
    val contentAvailable = (availablePx - chrome).coerceAtLeast(columns * 36f * densityScale)
    val maxCellPx = (contentAvailable * 0.72f).coerceAtLeast(56f * densityScale)
    val minCellPx = 40f * densityScale

    val widths = FloatArray(columns)
    val allRows = ArrayList<List<AnnotatedString>>(table.rows.size + 1)
    allRows.add(table.header)
    allRows.addAll(table.rows)
    allRows.forEachIndexed { rowIndex, cells ->
        val style = if (rowIndex == 0) headerStyle else cellStyle
        for (column in 0 until columns) {
            val cell = cells.getOrElse(column) { AnnotatedString("") }
            val measured = runCatching {
                measurer.measure(
                    text = cell,
                    style = style,
                    constraints = Constraints(maxWidth = maxCellPx.toInt().coerceAtLeast(1)),
                ).size.width
            }.getOrDefault(0)
            widths[column] = maxOf(widths[column], measured.toFloat() + paddingPx)
        }
    }

    var total = 0f
    for (column in 0 until columns) {
        widths[column] = widths[column].coerceIn(minCellPx + paddingPx, maxCellPx + paddingPx)
        total += widths[column]
    }
    total += chrome
    if (total < availablePx) {
        val extra = (availablePx - total) / columns
        for (column in 0 until columns) widths[column] += extra
        total = availablePx
    }

    return AiTableMetrics(
        widths = widths.map { with(density) { it.toDp() } },
        tableWidth = with(density) { total.toDp() },
        scrollable = total > availablePx + 1f,
    )
}

// ---------- 图片 ----------

@Composable
private fun AiImageBlock(block: AiMdBlock.Image, palette: AiMarkdownPalette) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val bitmap by produceState<Bitmap?>(initialValue = null, block.url) {
        value = AiMarkdownImageLoader.load(block.url)
    }
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.codeBackground)
            .clickable {
                if (!AiAppLauncher.openUrl(context, block.url)) {
                    runCatching { uriHandler.openUri(block.url) }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = block.alt.ifBlank { "图片" },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = block.alt.ifBlank { "图片加载中…" },
                fontSize = 12.sp,
                color = palette.secondary,
                maxLines = 2,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

/** 根据代码文字是深色还是浅色，选择两套对比度合适的高亮配色。 */
private fun codeThemeFor(palette: AiMarkdownPalette): AiCodeTheme {
    val lightText = palette.codeText.luminance() > 0.5f
    return if (lightText) {
        AiCodeTheme(
            keyword = Color(0xFF569CD6),
            string = Color(0xFFCE9178),
            number = Color(0xFFB5CEA8),
            comment = Color(0xFF6A9955),
            function = Color(0xFFDCDCAA),
            property = Color(0xFF9CDCFE),
            annotation = Color(0xFFDCDCAA),
        )
    } else {
        AiCodeTheme(
            keyword = Color(0xFF7C3AED),
            string = Color(0xFF0A7D3B),
            number = Color(0xFFB45309),
            comment = Color(0xFF6B7280),
            function = Color(0xFF1D4ED8),
            property = Color(0xFF0F766E),
            annotation = Color(0xFFB45309),
        )
    }
}

/** 对话图片的轻量加载器：降采样后最多缓存 6 张，避免整页图片吃光内存。 */
private object AiMarkdownImageLoader {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/130.0.0.0 Mobile Safari/537.36"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val cache = object : LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean =
            size > 6
    }

    private val failed: MutableSet<String> = Collections.newSetFromMap(
        ConcurrentHashMap<String, Boolean>(),
    )

    suspend fun load(url: String): Bitmap? = withContext(Dispatchers.IO) {
        if (url.startsWith("data:image", ignoreCase = true)) {
            return@withContext runCatching { decodeDataUri(url) }.getOrNull()
        }
        synchronized(cache) { cache[url] }?.let { return@withContext it }
        if (url in failed) return@withContext null
        val bitmap = runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "image/*,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body ?: return@use null
                if (body.contentLength() > 8L * 1024L * 1024L) return@use null
                val bytes = body.bytes()
                if (bytes.isEmpty()) return@use null
                decodeSampled(bytes)
            }
        }.getOrNull()
        if (bitmap == null) {
            failed.add(url)
        } else {
            synchronized(cache) { cache[url] = bitmap }
        }
        bitmap
    }

    private fun decodeDataUri(uri: String): Bitmap? {
        val comma = uri.indexOf(',')
        if (comma < 0) return null
        if (!uri.substring(0, comma).contains(";base64", ignoreCase = true)) return null
        val bytes = runCatching {
            android.util.Base64.decode(
                uri.substring(comma + 1),
                android.util.Base64.DEFAULT,
            )
        }.getOrNull() ?: return null
        return decodeSampled(bytes)
    }

    private fun decodeSampled(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
        var sampleSize = 1
        while (maxDimension / sampleSize > 1400) {
            sampleSize *= 2
        }
        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        )
    }
}
