package com.torachen.mote.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCodeHighlighterTest {

    private fun tokenize(code: String, language: AiCodeLanguage) =
        AiCodeHighlighter.tokenize(code, language)

    @Test
    fun `maps common language names`() {
        assertEquals(AiCodeLanguage.C_LIKE, AiCodeHighlighter.languageOf("kotlin"))
        assertEquals(AiCodeLanguage.C_LIKE, AiCodeHighlighter.languageOf("kt"))
        assertEquals(AiCodeLanguage.C_LIKE, AiCodeHighlighter.languageOf("typescript"))
        assertEquals(AiCodeLanguage.PYTHON, AiCodeHighlighter.languageOf("python"))
        assertEquals(AiCodeLanguage.SHELL, AiCodeHighlighter.languageOf("bash"))
        assertEquals(AiCodeLanguage.JSON, AiCodeHighlighter.languageOf("json"))
        assertEquals(AiCodeLanguage.XML, AiCodeHighlighter.languageOf("html"))
        assertEquals(AiCodeLanguage.SQL, AiCodeHighlighter.languageOf("sql"))
        assertEquals(AiCodeLanguage.PLAIN, AiCodeHighlighter.languageOf("mermaid"))
        assertEquals(AiCodeLanguage.PLAIN, AiCodeHighlighter.languageOf(""))
    }

    @Test
    fun `kotlin tokens`() {
        val tokens = tokenize("fun main() {\n    val x = \"hi\" // note\n}", AiCodeLanguage.C_LIKE)
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "fun" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "val" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Function && it.text == "main" })
        assertTrue(tokens.any { it.kind == AiTokenKind.String && it.text == "\"hi\"" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Comment && it.text == "// note" })
    }

    @Test
    fun `python tokens`() {
        val tokens = tokenize("def foo(a, b):\n    return a + b  # sum", AiCodeLanguage.PYTHON)
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "def" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Function && it.text == "foo" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Comment && it.text.startsWith("#") })
    }

    @Test
    fun `json keys and values`() {
        val tokens = tokenize("{\"a\": -1, \"b\": true}", AiCodeLanguage.JSON)
        assertTrue(tokens.any { it.kind == AiTokenKind.Property && it.text == "\"a\"" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Number && it.text == "-1" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "true" })
    }

    @Test
    fun `xml tags attributes and values`() {
        val tokens = tokenize("<a href=\"x\">text</a>", AiCodeLanguage.XML)
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "a" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Property && it.text == "href" })
        assertTrue(tokens.any { it.kind == AiTokenKind.String && it.text == "\"x\"" })
    }

    @Test
    fun `shell keywords and variables`() {
        val tokens = tokenize("echo \$HOME # hi", AiCodeLanguage.SHELL)
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "echo" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Property && it.text == "\$HOME" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Comment })
    }

    @Test
    fun `sql keywords are case insensitive`() {
        val tokens = tokenize("select id from users", AiCodeLanguage.SQL)
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "select" })
        assertTrue(tokens.any { it.kind == AiTokenKind.Keyword && it.text == "from" })
        assertTrue(tokens.none { it.kind == AiTokenKind.Keyword && it.text == "users" })
    }

    @Test
    fun `token text always reconstructs the source`() {
        val samples = mapOf(
            AiCodeLanguage.C_LIKE to "fun main() {\n  val x = \"hi\" // note\n  println(x)\n}",
            AiCodeLanguage.PYTHON to "def foo(a, b):\n    # sum\n    return a + b\n",
            AiCodeLanguage.SHELL to "echo \"\$HOME\" # hi\n",
            AiCodeLanguage.JSON to "{\"a\": -1, \"b\": true}",
            AiCodeLanguage.XML to "<a href=\"x\">t</a>",
            AiCodeLanguage.SQL to "SELECT * FROM users WHERE id = 1;",
            AiCodeLanguage.PLAIN to "任意文字 **不解析**",
        )
        samples.forEach { (language, code) ->
            val joined = tokenize(code, language).joinToString("") { it.text }
            assertEquals(language.name, code, joined)
        }
    }
}
