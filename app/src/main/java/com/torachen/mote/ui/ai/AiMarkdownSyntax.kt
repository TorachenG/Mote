package com.torachen.mote.ui.ai

/**
 * Markdown 里可以脱离 Compose 单测的纯文本解析部分：
 * 表格行拆分、分隔行识别、对齐方式，以及行内链接解析。
 */

internal enum class AiTableAlign {
    Start,
    Center,
    End,
}

internal data class AiInlineLink(
    val label: String,
    val url: String,
    val end: Int,
)

internal object AiMarkdownTables {

    private val separatorCellRegex = Regex("^:?-{1,}:?$")

    fun isSeparator(line: String): Boolean {
        if (!line.contains('-') || !line.contains('|')) return false
        val cells = splitRow(line)
        if (cells.isEmpty()) return false
        return cells.all { it.trim().matches(separatorCellRegex) }
    }

    fun alignments(line: String, columns: Int): List<AiTableAlign> {
        val cells = splitRow(line)
        return List(columns) { index ->
            val cell = cells.getOrElse(index) { "" }.trim()
            when {
                cell.startsWith(":") && cell.endsWith(":") -> AiTableAlign.Center
                cell.endsWith(":") -> AiTableAlign.End
                else -> AiTableAlign.Start
            }
        }
    }

    /** 拆分表格行，忽略 `\|` 与行内代码里的竖线。 */
    fun splitRow(line: String): List<String> {
        var text = line.trim()
        if (text.startsWith("|")) text = text.substring(1)
        if (text.endsWith("|") && !text.endsWith("\\|")) {
            text = text.substring(0, text.length - 1)
        }
        val cells = mutableListOf<String>()
        val cell = StringBuilder()
        var index = 0
        var inCode = false
        while (index < text.length) {
            val char = text[index]
            when {
                char == '\\' && index + 1 < text.length && text[index + 1] == '|' -> {
                    cell.append('|')
                    index += 2
                }

                char == '`' -> {
                    inCode = !inCode
                    cell.append(char)
                    index++
                }

                char == '|' && !inCode -> {
                    cells.add(cell.toString().trim())
                    cell.setLength(0)
                    index++
                }

                else -> {
                    cell.append(char)
                    index++
                }
            }
        }
        cells.add(cell.toString().trim())
        return cells
    }
}

/** 解析 `[label](target)`，返回标签、可点击 URL 与右括号下标。 */
internal fun parseInlineLink(text: String, bracketIndex: Int): AiInlineLink? {
    if (text.getOrNull(bracketIndex) != '[') return null
    val close = text.indexOf(']', bracketIndex + 1)
    if (close <= bracketIndex + 1) return null
    if (text.getOrNull(close + 1) != '(') return null
    val end = findClosingParen(text, close + 2)
    if (end < 0) return null
    val label = text.substring(bracketIndex + 1, close)
    val target = text.substring(close + 2, end).trim()
    if (target.isEmpty()) return null
    val rawUrl = target.substringBefore(' ').substringBefore('\t')
    val url = sanitizeUrl(rawUrl) ?: return null
    return AiInlineLink(label, url, end)
}

/** 找到与 `(` 配对的 `)`，支持成对括号与转义。 */
internal fun findClosingParen(text: String, start: Int): Int {
    var depth = 1
    var index = start
    while (index < text.length) {
        when (text[index]) {
            '\\' -> index++
            '(' -> depth++
            ')' -> {
                depth--
                if (depth == 0) return index
            }
        }
        index++
    }
    return -1
}

/** 只把安全协议变成可点击链接，避免 `javascript:` 之类的注入。 */
internal fun sanitizeUrl(raw: String): String? {
    val url = raw.trim().trim('<', '>', '"', '\'')
    if (url.isEmpty()) return null
    return when {
        url.startsWith("http://", ignoreCase = true) -> url
        url.startsWith("https://", ignoreCase = true) -> url
        url.startsWith("mailto:", ignoreCase = true) -> url
        url.startsWith("tel:", ignoreCase = true) -> url
        url.startsWith("mote://", ignoreCase = true) -> url
        url.startsWith("//") -> "https:$url"
        url.startsWith("www.") -> "https://$url"
        else -> null
    }
}

/** 图片地址额外允许内联的 data:image（模型直接返回的 base64 图片）。 */
internal fun sanitizeImageUrl(raw: String): String? {
    val url = raw.trim().trim('<', '>', '"', '\'')
    if (url.startsWith("data:image/", ignoreCase = true)) return url
    return sanitizeUrl(url)
}
