package com.torachen.mote.ui.ai.mcp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class McpSseParserTest {

    @Test
    fun `parses multiline data and event name`() {
        val parser = McpSseParser()
        assertNull(parser.feed("event: message"))
        assertNull(parser.feed("data: {\"a\":1,"))
        assertNull(parser.feed("data: \"b\":2}"))
        val event = parser.feed("")
        assertEquals("message", event?.event)
        assertEquals("{\"a\":1,\n\"b\":2}", event?.data)
    }

    @Test
    fun `ignores comments and dispatches only on blank line`() {
        val parser = McpSseParser()
        assertNull(parser.feed(": keep-alive"))
        assertNull(parser.feed("data: hello"))
        assertEquals("hello", parser.feed("")?.data)
        assertNull(parser.feed(""))
    }

    @Test
    fun `handles CRLF and retry id`() {
        val parser = McpSseParser()
        parser.feed("id: 42\r")
        parser.feed("retry: 1500\r")
        parser.feed("data: hi\r")
        val event = parser.feed("\r")
        assertEquals("42", event?.id)
        assertEquals(1500L, event?.retryMs)
        assertEquals("hi", event?.data)
    }

    @Test
    fun `flush emits unterminated final event`() {
        val parser = McpSseParser()
        parser.feed("data: tail")
        assertEquals("tail", parser.flush()?.data)
    }
}
