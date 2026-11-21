package com.torachen.mote.ui.ai.plugins

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPluginCodecTest {

    @Test
    fun `round trips permissions and tools`() {
        val plugin = AiPlugin(
            id = "com.example.weather",
            name = "Weather",
            version = "1.0.0",
            description = "desc",
            icon = "🌤",
            author = "me",
            permissions = setOf(AiPluginPermission.Network, AiPluginPermission.Tools),
            prompt = "prompt",
            tools = listOf(
                AiPluginHttpTool(
                    name = "weather_now",
                    description = "now",
                    method = "GET",
                    url = "https://example.com/weather?city={{args.city}}",
                    inputSchema = JSONObject().put("type", "object"),
                ),
            ),
            enabled = true,
            installedAt = 1L,
            updatedAt = 2L,
        )
        val decoded = AiPluginCodec.decodeList(AiPluginCodec.encodeList(listOf(plugin))).single()
        assertEquals(plugin.id, decoded.id)
        assertEquals(plugin.name, decoded.name)
        assertEquals(plugin.version, decoded.version)
        assertEquals(plugin.permissions, decoded.permissions)
        assertEquals(plugin.prompt, decoded.prompt)
        assertEquals(plugin.tools.single().name, decoded.tools.single().name)
        assertEquals(plugin.tools.single().url, decoded.tools.single().url)
        assertEquals(plugin.enabled, decoded.enabled)
    }

    @Test
    fun `rejects invalid id and dangerous path stays out`() {
        assertNull(AiPluginCodec.decode(JSONObject().put("id", "../evil").put("name", "x")))
    }

    @Test
    fun `missing permissions defaults to none`() {
        val decoded = AiPluginCodec.decode(
            JSONObject().put("id", "com.example.x").put("name", "x"),
        )
        assertTrue(decoded!!.permissions.isEmpty())
    }
}
