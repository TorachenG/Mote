package com.torachen.mote.ui.ai.mcp

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class McpClientTest {

    private lateinit var server: HttpServer
    private val requests = mutableListOf<JSONObject>()
    private val sessionHeaders = mutableListOf<String?>()

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.executor = Executors.newCachedThreadPool()
        server.createContext("/") { exchange -> handle(exchange) }
        server.start()
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `connects lists paginated tools and calls a tool`() = runBlocking {
        val client = McpClient(config("/mcp"), { null })
        try {
            client.connect()
            assertEquals("2025-06-18", client.protocolVersion)
            assertEquals("fake", client.serverInfo.name)

            val tools = client.listTools()
            assertEquals(3, tools.size)
            assertTrue(tools.any { it.publicName == "mcp__fake__echo" })
            assertTrue(tools.any { it.publicName.startsWith("mcp__fake__admin_reset") })

            val result = client.callTool("echo", JSONObject().put("text", "hi"))
            assertTrue(result.text().contains("echo:hi"))
            assertTrue(sessionHeaders.filterNotNull().contains("session-1"))
        } finally {
            client.close()
        }
    }

    @Test
    fun `probe reports server info and tool names`() = runBlocking {
        val result = McpClient.probe(config("/mcp"), { null })
        assertTrue(result.ok)
        assertEquals("fake", result.serverInfo?.name)
        assertEquals(3, result.toolNames.size)
        assertEquals("2025-06-18", result.protocolVersion)
    }

    @Test
    fun `auth failure is classified`() = runBlocking {
        val client = McpClient(config("/auth"), { null })
        try {
            client.connect()
            fail("expected auth failure")
        } catch (error: McpException) {
            assertEquals(McpFailureReason.Auth, error.kind)
        } finally {
            client.close()
        }
    }

    @Test
    fun `isError result is preserved`() = runBlocking {
        val client = McpClient(config("/mcp"), { null })
        try {
            client.connect()
            val result = client.callTool("fail", JSONObject())
            assertTrue(result.isError)
            assertTrue(result.text().contains("boom"))
        } finally {
            client.close()
        }
    }

    @Test
    fun `lists and reads resources`() = runBlocking {
        val client = McpClient(config("/mcp"), { null })
        try {
            client.connect()
            val resources = client.listResources()
            assertEquals(1, resources.size)
            assertEquals("memo://one", resources.single().uri)
            assertTrue(client.readResource("memo://one").contains("hello resource"))
        } finally {
            client.close()
        }
    }

    @Test
    fun `parses streamable http sse response`() = runBlocking {
        val client = McpClient(config("/sse"), { null })
        try {
            client.connect()
            val result = client.callTool("sse_echo", JSONObject().put("x", 1))
            assertEquals("sse-ok", result.text())
        } finally {
            client.close()
        }
    }

    private fun config(path: String): McpServerConfig = McpServerConfig(
        id = "mcp_test",
        displayName = "Fake",
        namespace = "fake",
        url = "http://127.0.0.1:${server.address.port}$path",
        transport = McpTransport.StreamableHttp,
        toolCallTimeoutMs = 3_000L,
        startupTimeoutMs = 3_000L,
    )

    private fun handle(exchange: HttpExchange) {
        val path = exchange.requestURI.path
        if (path == "/auth") {
            respond(exchange, 401, """{"error":"unauthorized"}""")
            return
        }
        val body = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
        if (body.isBlank()) {
            respond(exchange, 202, "")
            return
        }
        val request = JSONObject(body)
        synchronized(requests) { requests.add(request) }
        sessionHeaders.add(exchange.requestHeaders.getFirst("Mcp-Session-Id"))

        val method = request.optString("method")
        val id = request.optLong("id", -1L)
        when {
            method == "initialize" -> {
                val result = JSONObject()
                    .put("protocolVersion", "2025-06-18")
                    .put(
                        "capabilities",
                        JSONObject().put("tools", JSONObject()).put("resources", JSONObject()),
                    )
                    .put(
                        "serverInfo",
                        JSONObject().put("name", "fake").put("version", "1.0"),
                    )
                    .put("instructions", "fake server")
                respondJson(exchange, 200, McpJsonRpc.result(id, result), "session-1")
            }

            method == "notifications/initialized" -> respond(exchange, 202, "")

            method == "tools/list" -> {
                val cursor = request.optJSONObject("params")?.optString("cursor").orEmpty()
                val tools = JSONArray()
                if (cursor == "page-2") {
                    tools.put(tool("admin.reset", "admin action"))
                } else {
                    tools.put(tool("echo", "echo text"))
                    tools.put(tool("fail", "always fails"))
                }
                val result = JSONObject().put("tools", tools)
                if (cursor != "page-2") result.put("nextCursor", "page-2")
                respondJson(exchange, 200, McpJsonRpc.result(id, result), null)
            }

            method == "resources/list" -> {
                val resources = JSONArray().put(
                    JSONObject()
                        .put("uri", "memo://one")
                        .put("name", "One")
                        .put("mimeType", "text/plain"),
                )
                respondJson(
                    exchange,
                    200,
                    McpJsonRpc.result(id, JSONObject().put("resources", resources)),
                    null,
                )
            }

            method == "resources/read" -> {
                val contents = JSONArray().put(
                    JSONObject()
                        .put("uri", "memo://one")
                        .put("mimeType", "text/plain")
                        .put("text", "hello resource"),
                )
                respondJson(
                    exchange,
                    200,
                    McpJsonRpc.result(id, JSONObject().put("contents", contents)),
                    null,
                )
            }

            method == "tools/call" -> {
                val params = request.optJSONObject("params") ?: JSONObject()
                val name = params.optString("name")
                if (path == "/sse" && name == "sse_echo") {
                    val frame = "event: message\n" +
                        "data: " + McpJsonRpc.result(
                            id,
                            JSONObject().put(
                                "content",
                                JSONArray().put(
                                    JSONObject().put("type", "text").put("text", "sse-ok"),
                                ),
                            ),
                        ).toString() + "\n\n"
                    exchange.responseHeaders.add("Content-Type", "text/event-stream")
                    exchange.sendResponseHeaders(200, 0)
                    exchange.responseBody.use { it.write(frame.toByteArray(Charsets.UTF_8)) }
                    return
                }
                val content = JSONArray()
                if (name == "fail") {
                    content.put(JSONObject().put("type", "text").put("text", "boom"))
                    respondJson(
                        exchange,
                        200,
                        McpJsonRpc.result(
                            id,
                            JSONObject().put("content", content).put("isError", true),
                        ),
                        null,
                    )
                } else {
                    val text = params.optJSONObject("arguments")?.optString("text").orEmpty()
                    content.put(JSONObject().put("type", "text").put("text", "echo:$text"))
                    respondJson(
                        exchange,
                        200,
                        McpJsonRpc.result(id, JSONObject().put("content", content)),
                        null,
                    )
                }
            }

            else -> respondJson(
                exchange,
                200,
                McpJsonRpc.error(id, -32601, "Method not found"),
                null,
            )
        }
    }

    private fun tool(name: String, description: String): JSONObject = JSONObject()
        .put("name", name)
        .put("description", description)
        .put(
            "inputSchema",
            JSONObject()
                .put("type", "object")
                .put(
                    "properties",
                    JSONObject().put("text", JSONObject().put("type", "string")),
                ),
        )

    private fun respondJson(
        exchange: HttpExchange,
        code: Int,
        obj: JSONObject,
        sessionId: String?,
    ) {
        val bytes = obj.toString().toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        sessionId?.let { exchange.responseHeaders.add("Mcp-Session-Id", it) }
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun respond(exchange: HttpExchange, code: Int, text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        if (bytes.isEmpty()) {
            exchange.sendResponseHeaders(code, -1)
        } else {
            exchange.sendResponseHeaders(code, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }
}
