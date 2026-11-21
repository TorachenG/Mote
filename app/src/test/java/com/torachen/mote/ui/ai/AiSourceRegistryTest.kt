package com.torachen.mote.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AiSourceRegistryTest {

    @Test
    fun `same url keeps one stable index`() {
        val registry = AiSourceRegistry()
        val first = registry.add("https://example.com/a", "A", "摘要")
        val second = registry.add("https://example.com/a", "A2", "新摘要")
        val third = registry.add("https://example.com/b", "B")
        assertEquals(1, first?.index)
        assertSame(first, second)
        assertEquals("A", second?.title)
        assertEquals(2, third?.index)
        assertEquals(2, registry.snapshot().size)
    }

    @Test
    fun `non http urls are ignored`() {
        val registry = AiSourceRegistry()
        assertEquals(null, registry.add("javascript:alert(1)", "bad"))
        assertEquals(0, registry.snapshot().size)
    }
}
