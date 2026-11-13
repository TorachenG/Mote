package com.torachen.mote.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 表格 / 链接解析的纯 JVM 单测，不依赖 Android 运行时。 */
class AiMarkdownSyntaxTest {

    @Test
    fun `splits table row with outer pipes`() {
        assertEquals(
            listOf("名称", "数量", "备注"),
            AiMarkdownTables.splitRow("| 名称 | 数量 | 备注 |"),
        )
    }

    @Test
    fun `splits table row without outer pipes`() {
        assertEquals(listOf("a", "b"), AiMarkdownTables.splitRow("a | b"))
    }

    @Test
    fun `keeps escaped pipe inside a cell`() {
        assertEquals(listOf("a | b", "c"), AiMarkdownTables.splitRow("| a \\| b | c |"))
    }

    @Test
    fun `keeps pipe inside inline code`() {
        assertEquals(listOf("`x|y`", "z"), AiMarkdownTables.splitRow("| `x|y` | z |"))
    }

    @Test
    fun `detects separator row`() {
        assertTrue(AiMarkdownTables.isSeparator("| --- | :---: | ---: |"))
        assertTrue(AiMarkdownTables.isSeparator(":--|--:"))
        assertFalse(AiMarkdownTables.isSeparator("| a | b |"))
        assertFalse(AiMarkdownTables.isSeparator("---"))
    }

    @Test
    fun `parses alignments`() {
        assertEquals(
            listOf(AiTableAlign.Start, AiTableAlign.Center, AiTableAlign.End),
            AiMarkdownTables.alignments("| :-- | :---: | --: |", 3),
        )
    }

    @Test
    fun `pads alignments for missing cells`() {
        assertEquals(
            listOf(AiTableAlign.Start, AiTableAlign.Start),
            AiMarkdownTables.alignments("| --- |", 2),
        )
    }

    @Test
    fun `parses inline link with nested parentheses`() {
        val text = "[文档](https://example.com/a_(b))"
        val link = parseInlineLink(text, 0)
        assertEquals("文档", link?.label)
        assertEquals("https://example.com/a_(b)", link?.url)
        assertEquals(text.length - 1, link?.end)
    }

    @Test
    fun `drops link title`() {
        val link = parseInlineLink("[标题](https://example.com/a \"标题\")", 0)
        assertEquals("https://example.com/a", link?.url)
    }

    @Test
    fun `rejects unsafe schemes`() {
        assertNull(parseInlineLink("[x](javascript:alert(1))", 0))
        assertNull(parseInlineLink("[x](file:///data/local/tmp/a)", 0))
    }

    @Test
    fun `accepts friendly url forms`() {
        assertEquals("https://www.example.com", sanitizeUrl("www.example.com"))
        assertEquals("https://example.com/a", sanitizeUrl("//example.com/a"))
        assertEquals("mailto:a@b.com", sanitizeUrl("mailto:a@b.com"))
    }

    @Test
    fun `image sanitizer allows inline image data uris`() {
        assertEquals("data:image/png;base64,AAAA", sanitizeImageUrl("data:image/png;base64,AAAA"))
        assertEquals("https://a.com/x.png", sanitizeImageUrl("https://a.com/x.png"))
        assertNull(sanitizeImageUrl("data:text/html;base64,AAAA"))
    }

    @Test
    fun `unclosed link target is ignored`() {
        assertNull(parseInlineLink("[x](https://example.com", 0))
    }
}
