package com.torachen.mote.ui.ai

/**
 * LaTeX 公式的轻量渲染。
 *
 * 不引入排版库，而是把常见 TeX 命令转换成 Unicode 字符：
 * 希腊字母、运算符、上下标、分数、根号、`\text{}` 等。
 * 复杂矩阵 / 多行对齐只做近似处理，但比直接显示 `$...$` 原始源码可读得多。
 */
internal object AiMath {

    /** 判断一段 `$...$` 里的内容像不像公式，避免把价格等文本误判。 */
    fun looksLikeMath(content: String): Boolean {
        if (content.isEmpty() || content.length > 160) return false
        if (content.first().isWhitespace() || content.last().isWhitespace()) return false
        if (content.contains('\n')) return false
        if (content.contains("://")) return false
        return content.any { it.isLetter() || it == '\\' || it in "^_=+*/<>" }
    }

    fun convert(latex: String): String {
        val out = StringBuilder()
        var index = 0
        while (index < latex.length) {
            val char = latex[index]
            when {
                char == '\\' -> {
                    val (end, text) = readCommand(latex, index)
                    out.append(text)
                    index = end
                }

                char == '{' -> {
                    val end = matchingBrace(latex, index)
                    if (end > index) {
                        out.append(convert(latex.substring(index + 1, end)))
                        index = end + 1
                    } else {
                        index++
                    }
                }

                char == '}' -> index++

                char == '^' || char == '_' -> {
                    val (end, atom) = readAtom(latex, index + 1)
                    out.append(script(convert(atom), superscript = char == '^'))
                    index = end
                }

                char == '~' -> {
                    out.append('\u00A0')
                    index++
                }

                char == '&' || char == '$' -> index++

                else -> {
                    out.append(char)
                    index++
                }
            }
        }
        return out.toString()
    }

    // ============================================================
    // 解析
    // ============================================================

    private fun readCommand(source: String, start: Int): Pair<Int, String> {
        var index = start + 1
        if (index >= source.length) return index to ""
        val char = source[index]
        if (!char.isLetter()) {
            index++
            val text = when (char) {
                ',', ':', ';' -> "\u2009"
                ' ' -> " "
                '!' -> ""
                '\\' -> "\n"
                '{', '}', '%', '$', '&', '#', '_' -> char.toString()
                '|' -> "‖"
                else -> char.toString()
            }
            return index to text
        }

        val nameStart = index
        while (index < source.length && source[index].isLetter()) index++
        val name = source.substring(nameStart, index)

        return when {
            name == "frac" || name == "dfrac" || name == "tfrac" -> {
                val (afterNumerator, numerator) = readGroup(source, index)
                val (afterDenominator, denominator) = readGroup(source, afterNumerator)
                afterDenominator to fraction(convert(numerator), convert(denominator))
            }

            name == "sqrt" -> {
                var cursor = index
                var root = ""
                if (cursor < source.length && source[cursor] == '[') {
                    val close = source.indexOf(']', cursor)
                    if (close > cursor) {
                        root = source.substring(cursor + 1, close)
                        cursor = close + 1
                    }
                }
                val (end, content) = readGroup(source, cursor)
                val converted = convert(content)
                val body = if (converted.length <= 1) converted else "($converted)"
                val prefix = if (root.isBlank()) "√" else script(convert(root), superscript = true) + "√"
                end to (prefix + body)
            }

            name in WRAPPER_COMMANDS || name in ACCENT_COMMANDS -> {
                val (end, content) = readGroup(source, index)
                val converted = convert(content)
                end to if (name == "mathbb") doubleStruck(converted) else converted
            }

            name == "begin" || name == "end" -> {
                val (end, _) = readGroup(source, index)
                end to ""
            }

            name == "quad" -> index to "\u2003"
            name == "qquad" -> index to "\u2003\u2003"
            name in IGNORED_COMMANDS -> index to ""
            GREEK.containsKey(name) -> index to GREEK.getValue(name)
            SYMBOLS.containsKey(name) -> index to SYMBOLS.getValue(name)
            name in FUNCTIONS -> index to name
            // 未知命令：去掉反斜杠保留名字，至少不显示原始 TeX。
            else -> index to name
        }
    }

    private fun readAtom(source: String, start: Int): Pair<Int, String> = readGroup(source, start)

    private fun readGroup(source: String, start: Int): Pair<Int, String> {
        var index = start
        while (index < source.length && source[index].isWhitespace()) index++
        if (index >= source.length) return index to ""
        if (source[index] == '{') {
            val end = matchingBrace(source, index)
            return if (end > index) {
                (end + 1) to source.substring(index + 1, end)
            } else {
                (index + 1) to ""
            }
        }
        if (source[index] == '\\') return readCommand(source, index)
        return (index + 1) to source[index].toString()
    }

    private fun matchingBrace(source: String, open: Int): Int {
        var depth = 0
        var index = open
        while (index < source.length) {
            when (source[index]) {
                '\\' -> index++
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
            index++
        }
        return -1
    }

    private fun fraction(numerator: String, denominator: String): String {
        val top = if (needsParens(numerator)) "($numerator)" else numerator
        val bottom = if (needsParens(denominator)) "($denominator)" else denominator
        return "$top/$bottom"
    }

    private fun needsParens(value: String): Boolean {
        if (value.length <= 1) return false
        if (value.startsWith("(") && value.endsWith(")")) return false
        return value.any { it in "+-=<>≤≥ " }
    }

    private fun script(text: String, superscript: Boolean): String {
        if (text.isEmpty()) return ""
        val map = if (superscript) SUPERSCRIPTS else SUBSCRIPTS
        val builder = StringBuilder(text.length)
        for (char in text) {
            val mapped = map[char] ?: return (if (superscript) "^" else "_") + text
            builder.append(mapped)
        }
        return builder.toString()
    }

    private fun doubleStruck(text: String): String {
        if (text.length != 1) return text
        return when (text[0]) {
            'R' -> "ℝ"
            'N' -> "ℕ"
            'Z' -> "ℤ"
            'Q' -> "ℚ"
            'C' -> "ℂ"
            'P' -> "ℙ"
            'H' -> "ℍ"
            else -> text
        }
    }

    // ============================================================
    // 词表
    // ============================================================

    private val WRAPPER_COMMANDS = setOf(
        "text", "mathrm", "mathbf", "mathit", "mathsf", "mathtt", "mathcal", "mathbb",
        "operatorname", "mbox", "textrm", "textbf",
    )

    private val ACCENT_COMMANDS = setOf(
        "overline", "underline", "vec", "bar", "hat", "dot", "ddot", "widetilde",
        "overrightarrow", "overleftarrow", "widehat",
    )

    private val IGNORED_COMMANDS = setOf(
        "left", "right", "big", "Big", "bigg", "Bigg", "bigl", "bigr", "Bigl", "Bigr",
        "displaystyle", "textstyle", "scriptstyle", "limits", "nolimits", "hline",
        "nonumber", "notag", "middle",
    )

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan", "cot", "sec", "csc", "arcsin", "arccos", "arctan",
        "sinh", "cosh", "tanh", "log", "ln", "lg", "exp", "lim", "max", "min",
        "sup", "inf", "det", "dim", "gcd", "lcm", "mod", "bmod", "arg", "deg", "Pr",
    )

    private val GREEK = mapOf(
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ",
        "epsilon" to "ε", "varepsilon" to "ε", "zeta" to "ζ", "eta" to "η",
        "theta" to "θ", "vartheta" to "ϑ", "iota" to "ι", "kappa" to "κ",
        "lambda" to "λ", "mu" to "μ", "nu" to "ν", "xi" to "ξ", "pi" to "π",
        "varpi" to "ϖ", "rho" to "ρ", "varrho" to "ϱ", "sigma" to "σ",
        "varsigma" to "ς", "tau" to "τ", "upsilon" to "υ", "phi" to "φ",
        "varphi" to "φ", "chi" to "χ", "psi" to "ψ", "omega" to "ω",
        "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ",
        "Xi" to "Ξ", "Pi" to "Π", "Sigma" to "Σ", "Upsilon" to "Υ",
        "Phi" to "Φ", "Psi" to "Ψ", "Omega" to "Ω",
    )

    private val SYMBOLS = mapOf(
        "times" to "×", "div" to "÷", "cdot" to "·", "pm" to "±", "mp" to "∓",
        "le" to "≤", "leq" to "≤", "ge" to "≥", "geq" to "≥", "ne" to "≠", "neq" to "≠",
        "approx" to "≈", "equiv" to "≡", "sim" to "∼", "propto" to "∝", "infty" to "∞",
        "partial" to "∂", "nabla" to "∇", "sum" to "∑", "prod" to "∏", "int" to "∫",
        "oint" to "∮", "forall" to "∀", "exists" to "∃", "nexists" to "∄",
        "emptyset" to "∅", "varnothing" to "∅", "in" to "∈", "notin" to "∉",
        "subset" to "⊂", "subseteq" to "⊆", "supset" to "⊃", "supseteq" to "⊇",
        "cup" to "∪", "cap" to "∩", "setminus" to "∖", "wedge" to "∧", "land" to "∧",
        "vee" to "∨", "lor" to "∨", "neg" to "¬", "lnot" to "¬", "to" to "→",
        "rightarrow" to "→", "leftarrow" to "←", "Rightarrow" to "⇒",
        "Leftarrow" to "⇐", "leftrightarrow" to "↔", "Leftrightarrow" to "⇔",
        "mapsto" to "↦", "uparrow" to "↑", "downarrow" to "↓", "degree" to "°",
        "circ" to "∘", "angle" to "∠", "perp" to "⊥", "parallel" to "∥",
        "cdots" to "⋯", "ldots" to "…", "dots" to "…", "therefore" to "∴",
        "because" to "∵", "star" to "⋆", "ast" to "∗", "oplus" to "⊕", "otimes" to "⊗",
        "prime" to "′", "ell" to "ℓ", "hbar" to "ℏ", "aleph" to "ℵ",
        "langle" to "⟨", "rangle" to "⟩", "lfloor" to "⌊", "rfloor" to "⌋",
        "lceil" to "⌈", "rceil" to "⌉",
    )

    private val SUPERSCRIPTS = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴', '5' to '⁵',
        '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹', '+' to '⁺', '-' to '⁻',
        '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ', 'f' to 'ᶠ',
        'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ', 'k' to 'ᵏ', 'l' to 'ˡ',
        'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ', 'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ',
        't' to 'ᵗ', 'u' to 'ᵘ', 'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ',
        'z' to 'ᶻ', 'α' to 'ᵅ', 'β' to 'ᵝ', 'γ' to 'ᵞ', 'δ' to 'ᵟ', 'θ' to 'ᶿ',
        'φ' to 'ᵠ', 'ω' to 'ᵡ',
    )

    private val SUBSCRIPTS = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄', '5' to '₅',
        '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉', '+' to '₊', '-' to '₋',
        '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ',
        'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ', 'p' to 'ₚ', 'r' to 'ᵣ',
        's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ', 'x' to 'ₓ',
    )
}
