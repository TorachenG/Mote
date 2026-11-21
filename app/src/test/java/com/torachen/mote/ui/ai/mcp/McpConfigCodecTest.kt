package com.torachen.mote.ui.ai.mcp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class McpConfigCodecTest {

    @Test
    fun `round trips http config with auth and policies`() {
        val config = McpServerConfig(
            id = "mcp_1",
            displayName = "GitHub",
            namespace = "github",
            url = "https://mcp.example.com/mcp",
            transport = McpTransport.StreamableHttp,
            auth = McpAuth.Bearer("provider:github:token"),
            headers = listOf(McpHeader("X-Trace", "1")),
            toolPolicies = mapOf("create_issue" to McpToolPolicy.Ask),
            disabledTools = setOf("delete_repo"),
            createdAt = 1L,
            updatedAt = 2L,
        )
        val decoded = McpConfigCodec.decodeList(McpConfigCodec.encodeList(listOf(config))).single()
        assertEquals(config.id, decoded.id)
        assertEquals(config.namespace, decoded.namespace)
        assertEquals(config.url, decoded.url)
        assertEquals(McpTransport.StreamableHttp, decoded.transport)
        assertTrue(decoded.auth is McpAuth.Bearer)
        assertEquals("provider:github:token", (decoded.auth as McpAuth.Bearer).secretRef)
        assertEquals(McpToolPolicy.Ask, decoded.toolPolicies["create_issue"])
        assertEquals(setOf("delete_repo"), decoded.disabledTools)
    }

    @Test
    fun `round trips stdio config`() {
        val config = McpServerConfig(
            id = "mcp_stdio",
            displayName = "Local",
            namespace = "local",
            url = "",
            transport = McpTransport.Stdio,
            stdio = McpStdioConfig(
                command = "/data/local/tmp/node",
                args = listOf("server.js"),
                env = mapOf("NODE_ENV" to "production"),
                envSecretRefs = mapOf("API_KEY" to "mcp:local:key"),
                cwd = "/data/local/tmp",
            ),
        )
        val decoded = McpConfigCodec.decodeList(McpConfigCodec.encodeList(listOf(config))).single()
        assertEquals(McpTransport.Stdio, decoded.transport)
        assertEquals("/data/local/tmp/node", decoded.stdio?.command)
        assertEquals(listOf("server.js"), decoded.stdio?.args)
        assertEquals("mcp:local:key", decoded.stdio?.envSecretRefs?.get("API_KEY"))
    }

    @Test
    fun `rejects invalid http config without url`() {
        val raw = """{"version":1,"servers":[{"id":"x","namespace":"x","transport":"streamable-http"}]}"""
        assertTrue(McpConfigCodec.decodeList(raw).isEmpty())
    }
}
