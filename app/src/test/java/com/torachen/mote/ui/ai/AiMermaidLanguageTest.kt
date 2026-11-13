package com.torachen.mote.ui.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiMermaidLanguageTest {

    @Test
    fun `detects mermaid fences`() {
        assertTrue(isMermaidLanguage("mermaid"))
        assertTrue(isMermaidLanguage(" Mermaid "))
        assertTrue(isMermaidLanguage("mermaid title=\"图\""))
    }

    @Test
    fun `ignores other languages`() {
        assertFalse(isMermaidLanguage(""))
        assertFalse(isMermaidLanguage("kotlin"))
        assertFalse(isMermaidLanguage("mermaidx"))
        assertFalse(isMermaidLanguage("json"))
    }
}
