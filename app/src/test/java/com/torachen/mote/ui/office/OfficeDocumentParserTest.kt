package com.torachen.mote.ui.office

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class OfficeDocumentParserTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun zip(file: File, entries: Map<String, String>) {
        ZipOutputStream(file.outputStream()).use { output ->
            entries.forEach { (name, content) ->
                output.putNextEntry(ZipEntry(name))
                output.write(content.toByteArray(Charsets.UTF_8))
                output.closeEntry()
            }
        }
    }

    @Test
    fun `recognizes office extensions`() {
        assertEquals(OfficeKind.WORD, OfficeDocumentParser.kindOf("a.docx"))
        assertEquals(OfficeKind.EXCEL, OfficeDocumentParser.kindOf("b.XLSX"))
        assertEquals(OfficeKind.PPT, OfficeDocumentParser.kindOf("c.pptx"))
        assertNull(OfficeDocumentParser.kindOf("d.doc"))
        assertNull(OfficeDocumentParser.kindOf("e.pdf"))
    }

    @Test
    fun `parses docx headings paragraphs and tables`() {
        val file = temp.newFile("sample.docx")
        zip(
            file,
            mapOf(
                "word/document.xml" to """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                      <w:body>
                        <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>项目总结</w:t></w:r></w:p>
                        <w:p><w:r><w:t>第一段</w:t></w:r><w:r><w:t>继续</w:t></w:r></w:p>
                        <w:tbl>
                          <w:tr>
                            <w:tc><w:p><w:r><w:t>名称</w:t></w:r></w:p></w:tc>
                            <w:tc><w:p><w:r><w:t>数量</w:t></w:r></w:p></w:tc>
                          </w:tr>
                        </w:tbl>
                        <w:p><w:r><w:t>A &amp; B</w:t></w:r></w:p>
                      </w:body>
                    </w:document>
                """.trimIndent(),
            ),
        )
        val content = OfficeDocumentParser.parse(file, OfficeKind.WORD) as OfficePreviewContent.Word
        assertEquals(4, content.paragraphs.size)
        assertEquals("项目总结", content.paragraphs[0].text)
        assertEquals(1, content.paragraphs[0].heading)
        assertEquals("第一段继续", content.paragraphs[1].text)
        assertEquals("名称    数量", content.paragraphs[2].text)
        assertEquals("A & B", content.paragraphs[3].text)
    }

    @Test
    fun `parses xlsx shared strings and numbers`() {
        val file = temp.newFile("sample.xlsx")
        zip(
            file,
            mapOf(
                "xl/sharedStrings.xml" to """
                    <sst><si><t>名称</t></si><si><t>数量</t></si><si><t>苹果</t></si></sst>
                """.trimIndent(),
                "xl/workbook.xml" to """
                    <workbook><sheets><sheet name="库存" sheetId="1" r:id="rId1"/></sheets></workbook>
                """.trimIndent(),
                "xl/_rels/workbook.xml.rels" to """
                    <Relationships>
                      <Relationship Id="rId1" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                """.trimIndent(),
                "xl/worksheets/sheet1.xml" to """
                    <worksheet><sheetData>
                      <row r="1">
                        <c r="A1" t="s"><v>0</v></c>
                        <c r="B1" t="s"><v>1</v></c>
                      </row>
                      <row r="2">
                        <c r="A2" t="s"><v>2</v></c>
                        <c r="B2"><v>3</v></c>
                      </row>
                    </sheetData></worksheet>
                """.trimIndent(),
            ),
        )
        val content = OfficeDocumentParser.parse(file, OfficeKind.EXCEL) as OfficePreviewContent.Excel
        assertEquals(1, content.sheets.size)
        val sheet = content.sheets.first()
        assertEquals("库存", sheet.name)
        assertEquals(listOf("名称", "数量"), sheet.rows[0])
        assertEquals(listOf("苹果", "3"), sheet.rows[1])
    }

    @Test
    fun `parses pptx slides in order`() {
        val file = temp.newFile("sample.pptx")
        fun slide(title: String, body: String) = """
            <p:sld xmlns:a="a" xmlns:p="p"><p:cSld><p:spTree>
              <p:sp>
                <p:nvSpPr><p:nvPr><p:ph type="title"/></p:nvPr></p:nvSpPr>
                <p:txBody><a:p><a:r><a:t>$title</a:t></a:r></a:p></p:txBody>
              </p:sp>
              <p:sp><p:txBody><a:p><a:r><a:t>$body</a:t></a:r></a:p></p:txBody></p:sp>
            </p:spTree></p:cSld></p:sld>
        """.trimIndent()

        zip(
            file,
            mapOf(
                "ppt/slides/slide2.xml" to slide("第二页", "要点 B"),
                "ppt/slides/slide1.xml" to slide("第一页", "要点 A"),
            ),
        )
        val content = OfficeDocumentParser.parse(file, OfficeKind.PPT) as OfficePreviewContent.Ppt
        assertEquals(2, content.slides.size)
        assertEquals(1, content.slides[0].index)
        assertEquals("第一页", content.slides[0].title)
        assertEquals(listOf("要点 A"), content.slides[0].lines)
        assertEquals("第二页", content.slides[1].title)
    }

    @Test
    fun `missing document xml returns empty content`() {
        val file = temp.newFile("broken.docx")
        zip(file, mapOf("readme.txt" to "nothing"))
        val content = OfficeDocumentParser.parse(file, OfficeKind.WORD) as OfficePreviewContent.Word
        assertTrue(content.paragraphs.isEmpty())
    }
}
