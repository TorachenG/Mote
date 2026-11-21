package com.torachen.mote.ui.ai.mcp

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class McpToolAdapterTest {

    private val descriptor = McpToolDescriptor(
        serverId = "mcp_1",
        namespace = "demo",
        rawName = "create_issue",
        publicName = McpClient.publicToolName("demo", "create_issue"),
        title = "Create issue",
        description = "Create an issue",
        inputSchema = JSONObject().put("type", "object"),
        annotations = McpToolAnnotations(destructiveHint = true),
    )

    @Test
    fun `ask policy requires approval and keeps server identity`() {
        val spec = McpToolAdapter.toSpec(descriptor, McpToolPolicy.Ask, "Demo")
        assertTrue(spec.requiresApproval)
        assertTrue(spec.risky)
        assertEquals("Demo", spec.serverName)
        assertEquals("create_issue", spec.rawName)
        assertEquals(AiToolSource.Mcp, spec.source)
    }

    @Test
    fun `allow policy skips approval`() {
        val spec = McpToolAdapter.toSpec(descriptor, McpToolPolicy.Allow, "Demo")
        assertFalse(spec.requiresApproval)
    }

    @Test
    fun `result is wrapped as untrusted text`() {
        val result = McpCallResult(
            content = listOf(McpContentBlock.Text("hello"), McpContentBlock.ResourceLink("doc", "https://x")),
        )
        val outcome = McpToolAdapter.toOutcome(descriptor, result, supportsVision = false)
        assertFalse(outcome.failed)
        assertTrue(outcome.text.contains("<untrusted_mcp_result"))
        assertTrue(outcome.text.contains("Resource link: doc"))
        assertTrue(outcome.text.contains("</untrusted_mcp_result>"))
    }

    @Test
    fun `image is passed only when vision is supported`() {
        val result = McpCallResult(
            content = listOf(
                McpContentBlock.Image("image/png", "aGVsbG8="),
            ),
        )
        assertNotNull(McpToolAdapter.toOutcome(descriptor, result, supportsVision = true).image)
        assertEquals(null, McpToolAdapter.toOutcome(descriptor, result, supportsVision = false).image)
    }
}
