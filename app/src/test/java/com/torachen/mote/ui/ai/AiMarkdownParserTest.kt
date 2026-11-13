package com.torachen.mote.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Markdown 解析（块级 + 行内）的 JVM 单测。 */
class AiMarkdownParserTest {

    private val palette = AiMarkdownPalette(
        text = Color.Black,
        secondary = Color.Gray,
        accent = Color.Blue,
        codeBackground = Color.LightGray,
        codeText = Color.Black,
        divider = Color.Gray,
    )

    @Test
    fun `parses gfm table with alignments`() {
        val blocks = parseAiMarkdown(
            """
            | 名称 | 数量 |
            | :--- | ---: |
            | 苹果 | 3 |
            | 香蕉 | 5 |
            """.trimIndent(),
            palette,
            16.sp,
        )
        val table = blocks.single() as AiMdBlock.Table
        assertEquals(2, table.columnCount)
        assertEquals("名称", table.header[0].text)
        assertEquals("数量", table.header[1].text)
        assertEquals(2, table.rows.size)
        assertEquals("苹果", table.rows[0][0].text)
        assertEquals("香蕉", table.rows[1][0].text)
        assertEquals(AiTableAlign.Start, table.alignments[0])
        assertEquals(AiTableAlign.End, table.alignments[1])
    }

    @Test
    fun `table does not swallow following paragraph`() {
        val blocks = parseAiMarkdown(
            "| a | b |\n| --- | --- |\n| 1 | 2 |\n\n后面的话",
            palette,
            16.sp,
        )
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is AiMdBlock.Table)
        val paragraph = blocks[1] as AiMdBlock.Paragraph
        assertEquals("后面的话", paragraph.text.text)
    }

    @Test
    fun `plain dashed line stays a divider`() {
        val blocks = parseAiMarkdown("上一段\n\n---\n\n下一段", palette, 16.sp)
        assertTrue(blocks.any { it is AiMdBlock.Divider })
    }

    @Test
    fun `nested list keeps levels and continuation lines`() {
        val blocks = parseAiMarkdown(
            "- 第一项\n  - 子项\n    续行\n- 第二项",
            palette,
            16.sp,
        )
        val list = blocks.single() as AiMdBlock.ListBlock
        assertEquals(3, list.items.size)
        assertEquals(0, list.items[0].level)
        assertEquals(1, list.items[1].level)
        assertTrue(list.items[1].text.text.contains("续行"))
        assertEquals("•", list.items[0].marker)
    }

    @Test
    fun `quote lines are grouped into one block`() {
        val blocks = parseAiMarkdown("> 第一行\n> 第二行", palette, 16.sp)
        val quote = blocks.single() as AiMdBlock.Quote
        assertEquals("第一行\n第二行", quote.text.text)
    }

    @Test
    fun `github style callout`() {
        val blocks = parseAiMarkdown("> [!WARNING]\n> 注意备份", palette, 16.sp)
        val callout = blocks.single() as AiMdBlock.Callout
        assertEquals("warning", callout.kind)
        assertEquals("注意备份", callout.text.text)
    }

    @Test
    fun `parses image and fenced code`() {
        val blocks = parseAiMarkdown(
            "![图](https://a.com/x.png)\n\n```kotlin\nval a = 1\n```",
            palette,
            16.sp,
        )
        assertTrue(blocks[0] is AiMdBlock.Image)
        assertEquals("https://a.com/x.png", (blocks[0] as AiMdBlock.Image).url)
        val code = blocks[1] as AiMdBlock.Code
        assertEquals("kotlin", code.language)
        assertEquals("val a = 1", code.code)
    }

    @Test
    fun `inline bold and clickable link annotations`() {
        val blocks = parseAiMarkdown(
            "**粗体** 和 [链接](https://a.com) 与 `code`",
            palette,
            16.sp,
        )
        val text = (blocks.single() as AiMdBlock.Paragraph).text
        assertTrue(text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        val annotations = text.getStringAnnotations(0, text.length)
        assertTrue(annotations.any { it.item == "https://a.com" })
    }

    @Test
    fun `br tag becomes newline`() {
        val blocks = parseAiMarkdown("第一行<br>第二行", palette, 16.sp)
        val text = (blocks.single() as AiMdBlock.Paragraph).text.text
        assertEquals("第一行\n第二行", text)
    }

    @Test
    fun `data uri image becomes image block`() {
        val blocks = parseAiMarkdown(
            "![示意图](data:image/png;base64,AAAA)",
            palette,
            16.sp,
        )
        val image = blocks.single() as AiMdBlock.Image
        assertEquals("示意图", image.alt)
        assertTrue(image.url.startsWith("data:image/png;base64,"))
    }

    @Test
    fun `italic can wrap bold without breaking`() {
        val blocks = parseAiMarkdown("*a **b** c*", palette, 16.sp)
        val text = (blocks.single() as AiMdBlock.Paragraph).text
        assertTrue(text.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
        assertTrue(text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun `display math block`() {
        val blocks = parseAiMarkdown("$$\nE = mc^2\n$$", palette, 16.sp)
        val math = blocks.single() as AiMdBlock.Math
        assertEquals("E = mc^2", math.latex)
    }

    @Test
    fun `inline math is converted and loses dollars`() {
        val blocks = parseAiMarkdown("质能方程 \$E=mc^2\$ 很重要", palette, 16.sp)
        val text = (blocks.single() as AiMdBlock.Paragraph).text.text
        assertTrue(text.contains("E=mc²"))
        assertFalse(text.contains("\$"))
    }

    @Test
    fun `currency is not treated as math`() {
        val blocks = parseAiMarkdown("价格 \$5，优惠 \$3", palette, 16.sp)
        val text = (blocks.single() as AiMdBlock.Paragraph).text.text
        assertTrue(text.contains("\$5"))
    }
}
