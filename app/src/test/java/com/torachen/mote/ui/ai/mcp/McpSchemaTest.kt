package com.torachen.mote.ui.ai.mcp

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class McpSchemaTest {

    private val schema = JSONObject()
        .put("type", "object")
        .put(
            "properties",
            JSONObject()
                .put("query", JSONObject().put("type", "string").put("minLength", 1))
                .put("limit", JSONObject().put("type", "integer").put("minimum", 1).put("maximum", 20))
                .put(
                    "tags",
                    JSONObject()
                        .put("type", "array")
                        .put("items", JSONObject().put("type", "string")),
                ),
        )
        .put("required", JSONArray().put("query"))
        .put("additionalProperties", false)

    @Test
    fun `accepts valid arguments`() {
        val args = JSONObject()
            .put("query", "hello")
            .put("limit", 5)
            .put("tags", JSONArray().put("a"))
        assertNull(McpSchema.validate(schema, args))
    }

    @Test
    fun `rejects missing required and unknown fields`() {
        assertNotNull(McpSchema.validate(schema, JSONObject()))
        assertNotNull(McpSchema.validate(schema, JSONObject().put("query", "x").put("other", 1)))
    }

    @Test
    fun `rejects wrong types and bounds`() {
        assertNotNull(McpSchema.validate(schema, JSONObject().put("query", 1)))
        assertNotNull(McpSchema.validate(schema, JSONObject().put("query", "x").put("limit", 0)))
        assertNotNull(McpSchema.validate(schema, JSONObject().put("query", "x").put("limit", 1.5)))
    }

    @Test
    fun `summary caps oversized schema`() {
        val summary = McpSchema.summary(schema, 80)
        assertTrue(summary.length <= 80)
        assertTrue(summary.contains("query"))
    }
}
