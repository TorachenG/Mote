package com.torachen.mote.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * 轻量代码语法高亮（无第三方依赖）。
 *
 * 只做常见语言的词法着色：注释、字符串、数字、关键字、函数名、注解、JSON 键、XML 属性。
 * 不是完整的语法解析器，但足够让对话里的代码块更好读。
 */

internal enum class AiCodeLanguage {
    C_LIKE,
    PYTHON,
    SHELL,
    JSON,
    XML,
    SQL,
    PLAIN,
}

internal enum class AiTokenKind {
    Plain,
    Keyword,
    String,
    Number,
    Comment,
    Function,
    Property,
    Annotation,
}

internal data class AiCodeToken(
    val text: String,
    val kind: AiTokenKind,
)

internal data class AiCodeTheme(
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val property: Color,
    val annotation: Color,
)

internal object AiCodeHighlighter {

    fun languageOf(info: String): AiCodeLanguage {
        val name = info.trim().lowercase().substringBefore(' ').substringBefore(',').substringBefore('{')
        return when (name) {
            "kt", "kotlin", "kts",
            "java", "js", "javascript", "jsx", "mjs", "cjs",
            "ts", "typescript", "tsx",
            "c", "h", "cpp", "c++", "cc", "hpp", "cs", "csharp",
            "go", "golang", "rs", "rust", "swift", "dart", "php", "scala", "groovy",
            -> AiCodeLanguage.C_LIKE

            "py", "python", "python3", "py3" -> AiCodeLanguage.PYTHON
            "sh", "bash", "shell", "zsh", "console", "shell-session", "powershell", "ps1" ->
                AiCodeLanguage.SHELL

            "json", "json5", "jsonc" -> AiCodeLanguage.JSON
            "html", "xml", "svg", "xhtml", "vue", "xaml" -> AiCodeLanguage.XML
            "sql", "mysql", "postgresql", "postgres", "sqlite" -> AiCodeLanguage.SQL
            else -> AiCodeLanguage.PLAIN
        }
    }

    fun highlight(code: String, info: String, theme: AiCodeTheme): AnnotatedString {
        val tokens = tokenize(code, languageOf(info))
        if (tokens.all { it.kind == AiTokenKind.Plain }) return AnnotatedString(code)
        return buildAnnotatedString {
            tokens.forEach { token ->
                val color = token.kind.color(theme)
                if (color == null) {
                    append(token.text)
                } else {
                    withStyle(SpanStyle(color = color)) { append(token.text) }
                }
            }
        }
    }

    fun tokenize(code: String, language: AiCodeLanguage): List<AiCodeToken> = when (language) {
        AiCodeLanguage.C_LIKE -> scanCLike(code)
        AiCodeLanguage.PYTHON -> scanPython(code)
        AiCodeLanguage.SHELL -> scanShell(code)
        AiCodeLanguage.JSON -> scanJson(code)
        AiCodeLanguage.XML -> scanXml(code)
        AiCodeLanguage.SQL -> scanSql(code)
        AiCodeLanguage.PLAIN -> listOf(AiCodeToken(code, AiTokenKind.Plain))
    }

    private fun AiTokenKind.color(theme: AiCodeTheme): Color? = when (this) {
        AiTokenKind.Plain -> null
        AiTokenKind.Keyword -> theme.keyword
        AiTokenKind.String -> theme.string
        AiTokenKind.Number -> theme.number
        AiTokenKind.Comment -> theme.comment
        AiTokenKind.Function -> theme.function
        AiTokenKind.Property -> theme.property
        AiTokenKind.Annotation -> theme.annotation
    }
}

// ============================================================
// 词法扫描
// ============================================================

private val C_LIKE_KEYWORDS = setOf(
    // Kotlin
    "abstract", "actual", "annotation", "as", "break", "by", "catch", "class", "companion",
    "const", "constructor", "continue", "crossinline", "data", "delegate", "do", "dynamic",
    "else", "enum", "expect", "external", "false", "field", "file", "final", "finally", "for",
    "fun", "get", "if", "import", "in", "infix", "init", "inline", "inner", "interface",
    "internal", "is", "lateinit", "noinline", "null", "object", "open", "operator", "out",
    "override", "package", "param", "private", "protected", "public", "reified", "return",
    "sealed", "set", "super", "suspend", "tailrec", "this", "throw", "true", "try", "typealias",
    "typeof", "val", "var", "vararg", "when", "where", "while",
    // Java / C / C++
    "boolean", "byte", "char", "double", "float", "int", "long", "short", "void", "new",
    "static", "native", "synchronized", "transient", "volatile", "instanceof", "extends",
    "implements", "throws", "throw", "assert", "strictfp", "struct", "typedef", "union",
    "unsigned", "signed", "sizeof", "goto", "extern", "register", "auto", "switch", "case",
    "default", "break", "continue",
    // JavaScript / TypeScript
    "async", "await", "function", "let", "export", "yield", "delete", "typeof", "undefined",
    "declare", "namespace", "readonly", "type", "keyof", "infer", "satisfies", "of",
    // Go / Rust / Swift / Dart
    "fn", "impl", "trait", "pub", "mut", "use", "mod", "match", "loop", "unsafe", "dyn",
    "crate", "self", "Self", "defer", "chan", "select", "go", "map", "func", "nil", "range",
    "package", "guard", "let", "protocol", "extension", "deinit", "throws", "rethrows",
    "var", "final", "lazy", "weak", "unowned", "some", "any", "where",
)

private val PYTHON_KEYWORDS = setOf(
    "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class",
    "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global",
    "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return",
    "try", "while", "with", "yield", "match", "case", "self", "cls",
)

private val SHELL_KEYWORDS = setOf(
    "if", "then", "else", "elif", "fi", "for", "while", "until", "do", "done", "case", "esac",
    "function", "in", "select", "time", "export", "local", "readonly", "declare", "typeset",
    "unset", "set", "source", "alias", "echo", "printf", "cd", "pushd", "popd", "read",
    "return", "exit", "break", "continue", "eval", "exec", "trap", "shift", "test", "sudo",
    "true", "false", "null", "elif",
)

private val SQL_KEYWORDS = setOf(
    "select", "from", "where", "insert", "into", "values", "update", "set", "delete", "create",
    "table", "alter", "drop", "index", "view", "join", "inner", "left", "right", "full", "outer",
    "on", "as", "and", "or", "not", "null", "is", "in", "like", "between", "group", "by",
    "order", "having", "limit", "offset", "union", "all", "distinct", "case", "when", "then",
    "else", "end", "primary", "key", "foreign", "references", "default", "unique", "check",
    "constraint", "if", "exists", "int", "integer", "varchar", "text", "date", "timestamp",
    "boolean", "count", "sum", "avg", "min", "max", "asc", "desc",
)

private val JSON_KEYWORDS = setOf("true", "false", "null")

private class TokenSink(private val code: String) {
    val tokens = mutableListOf<AiCodeToken>()

    fun add(kind: AiTokenKind, start: Int, end: Int) {
        if (end <= start) return
        val text = code.substring(start, end)
        val last = tokens.lastOrNull()
        if (last != null && last.kind == kind) {
            tokens[tokens.lastIndex] = AiCodeToken(last.text + text, kind)
        } else {
            tokens.add(AiCodeToken(text, kind))
        }
    }
}

private fun Char.isIdentStart(): Boolean = isLetter() || this == '_' || this == '$'

private fun Char.isIdentPart(): Boolean = isLetterOrDigit() || this == '_' || this == '$'

private fun lineEnd(code: String, start: Int): Int {
    var index = start
    while (index < code.length && code[index] != '\n') index++
    return index
}

private fun blockEnd(code: String, start: Int, close: String): Int {
    val end = code.indexOf(close, start)
    return if (end < 0) code.length else end + close.length
}

private fun stringEnd(code: String, quoteIndex: Int): Int {
    val quote = code[quoteIndex]
    var index = quoteIndex + 1
    while (index < code.length) {
        when {
            code[index] == '\\' -> index += 2
            code[index] == quote -> return index + 1
            code[index] == '\n' -> return index
            else -> index++
        }
    }
    return code.length
}

private fun tripleStringEnd(code: String, start: Int, quote: String): Int {
    val end = code.indexOf(quote, start + 3)
    return if (end < 0) code.length else end + 3
}

private fun numberEnd(code: String, start: Int): Int {
    var index = start
    if (index < code.length && (code[index] == '-' || code[index] == '+')) index++
    if (index + 1 < code.length && code[index] == '0' && (code[index + 1] == 'x' || code[index + 1] == 'X')) {
        index += 2
        while (index < code.length && (code[index].isLetterOrDigit() || code[index] == '_')) index++
        return index
    }
    while (index < code.length && (code[index].isDigit() || code[index] == '_')) index++
    if (index < code.length && code[index] == '.') {
        index++
        while (index < code.length && (code[index].isDigit() || code[index] == '_')) index++
    }
    if (index < code.length && (code[index] == 'e' || code[index] == 'E')) {
        var probe = index + 1
        if (probe < code.length && (code[probe] == '+' || code[probe] == '-')) probe++
        if (probe < code.length && code[probe].isDigit()) {
            index = probe
            while (index < code.length && code[index].isDigit()) index++
        }
    }
    while (index < code.length && code[index] in "fFlLdDuU") index++
    return index
}

private fun identEnd(code: String, start: Int): Int {
    var index = start
    while (index < code.length && code[index].isIdentPart()) index++
    return index
}

/** XML 标签/属性名允许 `-`、`:`、`.`（如 `svg:path`、`my-tag`）。 */
private fun xmlNameEnd(code: String, start: Int): Int {
    var index = start
    while (index < code.length) {
        val char = code[index]
        if (!(char.isLetterOrDigit() || char == '_' || char == '-' || char == ':' || char == '.')) break
        index++
    }
    return index
}

private fun nextNonSpace(code: String, start: Int): Int {
    var index = start
    while (index < code.length && code[index].isWhitespace()) index++
    return index
}

private fun isFunctionCall(code: String, afterIdentifier: Int): Boolean {
    val next = nextNonSpace(code, afterIdentifier)
    return next < code.length && code[next] == '('
}

private fun scanCLike(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        val char = code[index]
        when {
            char.isWhitespace() -> {
                val start = index
                while (index < code.length && code[index].isWhitespace()) index++
                sink.add(AiTokenKind.Plain, start, index)
            }

            code.startsWith("//", index) -> {
                val start = index
                index = lineEnd(code, index)
                sink.add(AiTokenKind.Comment, start, index)
            }

            code.startsWith("/*", index) -> {
                val start = index
                index = blockEnd(code, index + 2, "*/")
                sink.add(AiTokenKind.Comment, start, index)
            }

            code.startsWith("\"\"\"", index) -> {
                val start = index
                index = tripleStringEnd(code, index, "\"\"\"")
                sink.add(AiTokenKind.String, start, index)
            }

            char == '"' || char == '\'' || char == '`' -> {
                val start = index
                index = stringEnd(code, index)
                sink.add(AiTokenKind.String, start, index)
            }

            char == '@' && index + 1 < code.length && code[index + 1].isIdentStart() -> {
                val start = index
                index = identEnd(code, index + 1)
                sink.add(AiTokenKind.Annotation, start, index)
            }

            char.isDigit() || (char == '.' && index + 1 < code.length && code[index + 1].isDigit()) -> {
                val start = index
                index = numberEnd(code, index)
                sink.add(AiTokenKind.Number, start, index)
            }

            char.isIdentStart() -> {
                val start = index
                index = identEnd(code, index)
                val word = code.substring(start, index)
                val kind = when {
                    word in C_LIKE_KEYWORDS -> AiTokenKind.Keyword
                    isFunctionCall(code, index) -> AiTokenKind.Function
                    else -> AiTokenKind.Plain
                }
                sink.add(kind, start, index)
            }

            else -> {
                sink.add(AiTokenKind.Plain, index, index + 1)
                index++
            }
        }
    }
    return sink.tokens
}

private fun scanPython(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        val char = code[index]
        when {
            char.isWhitespace() -> {
                val start = index
                while (index < code.length && code[index].isWhitespace()) index++
                sink.add(AiTokenKind.Plain, start, index)
            }

            char == '#' -> {
                val start = index
                index = lineEnd(code, index)
                sink.add(AiTokenKind.Comment, start, index)
            }

            code.startsWith("\"\"\"", index) || code.startsWith("'''", index) -> {
                val start = index
                val quote = code.substring(index, index + 3)
                index = tripleStringEnd(code, index, quote)
                sink.add(AiTokenKind.String, start, index)
            }

            char == '"' || char == '\'' -> {
                val start = index
                index = stringEnd(code, index)
                sink.add(AiTokenKind.String, start, index)
            }

            char == '@' && index + 1 < code.length && code[index + 1].isIdentStart() -> {
                val start = index
                index = identEnd(code, index + 1)
                sink.add(AiTokenKind.Annotation, start, index)
            }

            char.isDigit() -> {
                val start = index
                index = numberEnd(code, index)
                sink.add(AiTokenKind.Number, start, index)
            }

            char.isIdentStart() -> {
                val start = index
                index = identEnd(code, index)
                val word = code.substring(start, index)
                val kind = when {
                    word in PYTHON_KEYWORDS -> AiTokenKind.Keyword
                    isFunctionCall(code, index) -> AiTokenKind.Function
                    else -> AiTokenKind.Plain
                }
                sink.add(kind, start, index)
            }

            else -> {
                sink.add(AiTokenKind.Plain, index, index + 1)
                index++
            }
        }
    }
    return sink.tokens
}

private fun scanShell(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        val char = code[index]
        when {
            char.isWhitespace() -> {
                val start = index
                while (index < code.length && code[index].isWhitespace()) index++
                sink.add(AiTokenKind.Plain, start, index)
            }

            char == '#' -> {
                val start = index
                index = lineEnd(code, index)
                sink.add(AiTokenKind.Comment, start, index)
            }

            char == '"' || char == '\'' || char == '`' -> {
                val start = index
                index = stringEnd(code, index)
                sink.add(AiTokenKind.String, start, index)
            }

            char == '$' -> {
                val start = index
                if (index + 1 < code.length && code[index + 1] == '{') {
                    index = blockEnd(code, index + 2, "}")
                } else {
                    index++
                    while (index < code.length && code[index].isIdentPart()) index++
                }
                sink.add(AiTokenKind.Property, start, index)
            }

            char.isDigit() -> {
                val start = index
                index = numberEnd(code, index)
                sink.add(AiTokenKind.Number, start, index)
            }

            char.isIdentStart() -> {
                val start = index
                index = identEnd(code, index)
                val word = code.substring(start, index)
                val kind = if (word in SHELL_KEYWORDS) AiTokenKind.Keyword else AiTokenKind.Plain
                sink.add(kind, start, index)
            }

            else -> {
                sink.add(AiTokenKind.Plain, index, index + 1)
                index++
            }
        }
    }
    return sink.tokens
}

private fun scanJson(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        val char = code[index]
        when {
            char.isWhitespace() -> {
                val start = index
                while (index < code.length && code[index].isWhitespace()) index++
                sink.add(AiTokenKind.Plain, start, index)
            }

            char == '"' -> {
                val start = index
                index = stringEnd(code, index)
                val next = nextNonSpace(code, index)
                val isKey = next < code.length && code[next] == ':'
                sink.add(if (isKey) AiTokenKind.Property else AiTokenKind.String, start, index)
            }

            char.isDigit() || char == '-' -> {
                val start = index
                index = numberEnd(code, index)
                sink.add(AiTokenKind.Number, start, index)
            }

            char.isLetter() -> {
                val start = index
                index = identEnd(code, index)
                val word = code.substring(start, index)
                val kind = if (word in JSON_KEYWORDS) {
                    AiTokenKind.Keyword
                } else {
                    AiTokenKind.Plain
                }
                sink.add(kind, start, index)
            }

            else -> {
                sink.add(AiTokenKind.Plain, index, index + 1)
                index++
            }
        }
    }
    return sink.tokens
}

private fun scanXml(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        when {
            code.startsWith("<!--", index) -> {
                val start = index
                index = blockEnd(code, index + 4, "-->")
                sink.add(AiTokenKind.Comment, start, index)
            }

            code.startsWith("<?", index) -> {
                val start = index
                index = blockEnd(code, index + 2, "?>")
                sink.add(AiTokenKind.Comment, start, index)
            }

            code[index] == '<' -> {
                val start = index
                index++
                if (index < code.length && code[index] == '/') index++
                sink.add(AiTokenKind.Plain, start, index)
                val nameStart = index
                index = xmlNameEnd(code, index)
                if (index > nameStart) sink.add(AiTokenKind.Keyword, nameStart, index)
                while (index < code.length && code[index] != '>') {
                    val char = code[index]
                    when {
                        char.isWhitespace() -> {
                            val spaceStart = index
                            while (index < code.length && code[index].isWhitespace()) index++
                            sink.add(AiTokenKind.Plain, spaceStart, index)
                        }

                        char == '/' -> {
                            sink.add(AiTokenKind.Plain, index, index + 1)
                            index++
                        }

                        char == '"' || char == '\'' -> {
                            val quoteStart = index
                            index = stringEnd(code, index)
                            sink.add(AiTokenKind.String, quoteStart, index)
                        }

                        char.isIdentStart() -> {
                            val attrStart = index
                            index = xmlNameEnd(code, index)
                            val next = nextNonSpace(code, index)
                            val kind = if (next < code.length && code[next] == '=') {
                                AiTokenKind.Property
                            } else {
                                AiTokenKind.Plain
                            }
                            sink.add(kind, attrStart, index)
                        }

                        else -> {
                            sink.add(AiTokenKind.Plain, index, index + 1)
                            index++
                        }
                    }
                }
                if (index < code.length) {
                    sink.add(AiTokenKind.Plain, index, index + 1)
                    index++
                }
            }

            else -> {
                val start = index
                while (index < code.length && code[index] != '<') index++
                sink.add(AiTokenKind.Plain, start, index)
            }
        }
    }
    return sink.tokens
}

private fun scanSql(code: String): List<AiCodeToken> {
    val sink = TokenSink(code)
    var index = 0
    while (index < code.length) {
        val char = code[index]
        when {
            char.isWhitespace() -> {
                val start = index
                while (index < code.length && code[index].isWhitespace()) index++
                sink.add(AiTokenKind.Plain, start, index)
            }

            code.startsWith("--", index) -> {
                val start = index
                index = lineEnd(code, index)
                sink.add(AiTokenKind.Comment, start, index)
            }

            code.startsWith("/*", index) -> {
                val start = index
                index = blockEnd(code, index + 2, "*/")
                sink.add(AiTokenKind.Comment, start, index)
            }

            char == '\'' || char == '"' -> {
                val start = index
                index = stringEnd(code, index)
                sink.add(AiTokenKind.String, start, index)
            }

            char.isDigit() -> {
                val start = index
                index = numberEnd(code, index)
                sink.add(AiTokenKind.Number, start, index)
            }

            char.isIdentStart() -> {
                val start = index
                index = identEnd(code, index)
                val word = code.substring(start, index).lowercase()
                val kind = if (word in SQL_KEYWORDS) AiTokenKind.Keyword else AiTokenKind.Plain
                sink.add(kind, start, index)
            }

            else -> {
                sink.add(AiTokenKind.Plain, index, index + 1)
                index++
            }
        }
    }
    return sink.tokens
}
