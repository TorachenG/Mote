package com.torachen.mote.ui.ai.mcp

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class McpJsonRpcTest {

    @Test
    fun `builds request with id and params`() {
        val request = McpJsonRpc.request(7L, "tools/list", JSONObject().put("cursor", "x"))
        assertEquals("2.0", request.getString("jsonrpc"))
        assertEquals(7L, request.getLong("id"))
        assertEquals("tools/list", request.getString("method"))
        assertEquals("x", request.getJSONObject("params").getString("cursor"))
    }

    @Test
    fun `notification has no id`() {
        val notification = McpJsonRpc.notification("notifications/initialized")
        assertTrue(McpJsonRpc.isNotification(notification))
        assertFalse(McpJsonRpc.isResponse(notification))
    }

    @Test
    fun `recognizes numeric and string response ids`() {
        assertEquals(3L, McpJsonRpc.responseId(McpJsonRpc.result(3L, JSONObject())))
        assertEquals(4L, McpJsonRpc.responseId(JSONObject().put("id", "4").put("result", JSONObject())))
        assertNull(McpJsonRpc.responseId(JSONObject().put("id", "nope")))
    }

    @Test
    fun `parses rpc error`() {
        val error = McpJsonRpc.errorOf(McpJsonRpc.error(1L, -32601, "Method not found"))
        assertNotNull(error)
        assertEquals(-32601, error?.code)
        assertEquals("Method not found", error?.message)
    }
}
