package com.torachen.mote.ui.ai.preset

import com.torachen.mote.ui.ai.AiFilePermission
import com.torachen.mote.ui.ai.AiReasoningLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPresetCodecTest {

    @Test
    fun `round trips explicit preset`() {
        val preset = AiAgentPreset(
            id = "preset_1",
            name = "Research",
            description = "desc",
            systemPrompt = "prompt",
            providerId = "p1",
            modelId = "m1",
            reasoning = AiReasoningLevel.High,
            allowedTools = setOf("web_search", "fetch_page"),
            mcpServerIds = setOf("mcp_1"),
            filePermission = AiFilePermission.EditWithConfirm,
            deviceControl = false,
            contextTurns = 20,
            backgroundAllowed = true,
            builtIn = false,
            createdAt = 1L,
            updatedAt = 2L,
        )
        val (defaultId, decoded) = AiPresetCodec.decodeList(
            AiPresetCodec.encodeList(listOf(preset), "preset_1"),
        )
        assertEquals("preset_1", defaultId)
        val copy = decoded.single()
        assertEquals(preset, copy)
    }

    @Test
    fun `keeps null as inherit and empty as none`() {
        val inherit = AiAgentPreset(id = "a", name = "Inherit")
        val none = AiAgentPreset(
            id = "b",
            name = "None",
            allowedTools = emptySet(),
            mcpServerIds = emptySet(),
        )
        val decoded = AiPresetCodec.decodeList(
            AiPresetCodec.encodeList(listOf(inherit, none), "a"),
        ).second
        assertNull(decoded[0].allowedTools)
        assertNull(decoded[0].mcpServerIds)
        assertTrue(decoded[1].allowedTools!!.isEmpty())
        assertTrue(decoded[1].mcpServerIds!!.isEmpty())
    }

    @Test
    fun `rejects rows without id`() {
        val raw = """{"version":1,"defaultId":"x","presets":[{"name":"bad"}]}"""
        assertTrue(AiPresetCodec.decodeList(raw).second.isEmpty())
    }
}
