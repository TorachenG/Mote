package com.torachen.mote.ui.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * 真实可用的联网能力：搜索 + 抓取网页正文。
 *
 * 只负责「查」和「读」，不负责决定查什么 —— 由 Agent 循环里的模型自主决定
 * 搜索几次、搜什么、打开哪个链接，因此支持在推理过程中分批、多轮搜索。
 */
internal object AiWebSearch {

    private const val MAX_RESULTS = 10
    private const val MAX_PAGE_CHARS = 9_000

    private const val MOBILE_UA =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/130.0.0.0 Mobile Safari/537.36"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    data class Hit(
        val title: String,
        val url: String,
        val snippet: String,
    )

    data class Page(
        val title: String,
        val url: String,
        val text: String,
    )

    suspend fun search(query: String, limit: Int = MAX_RESULTS): List<Hit> =
        withContext(Dispatchers.IO) {
            val clean = query.trim().trim('。', '，', '！', '？', '?', '!')
            if (clean.isEmpty()) return@withContext emptyList()
            withTimeoutOrNull(12_000L) {
                val batches = coroutineScope {
                    listOf(
                        async { withTimeoutOrNull(7_000L) { searchBingRss(clean) } ?: emptyList() },
                        async { withTimeoutOrNull(7_000L) { searchDuckDuckGo(clean) } ?: emptyList() },
                    ).awaitAll()
                }
                var hits = mergeHits(batches.flatten(), limit)
                if (hits.isEmpty()) {
                    hits = mergeHits(
                        listOf(searchBingHtml(clean), searchDuckDuckGoLite(clean)).flatten(),
                        limit,
                    )
                }
                hits
            }.orEmpty()
        }

    suspend fun fetchPage(url: String, maxChars: Int = MAX_PAGE_CHARS): Page? =
        withContext(Dispatchers.IO) {
            if (!url.startsWith("http", ignoreCase = true)) return@withContext null
            withTimeoutOrNull(14_000L) {
                val response = runCatching {
                    client.newCall(
                        Request.Builder()
                            .url(url)
                            .header("User-Agent", MOBILE_UA)
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7")
                            .build(),
                    ).execute()
                }.getOrNull() ?: return@withTimeoutOrNull null
                response.use { res ->
                    if (!res.isSuccessful) return@use null
                    val body = res.body ?: return@use null
                    val length = body.contentLength()
                    if (length > 6L * 1024L * 1024L) return@use null
                    val bytes = runCatching { body.bytes() }.getOrNull() ?: return@use null
                    if (bytes.isEmpty()) return@use null
                    val html = decodeBytes(bytes, body.contentType())
                    val title = Regex(
                        "(?is)<title[^>]*>(.*?)</title>",
                    ).find(html)?.groupValues?.get(1)?.let(::unescapeEntities)?.trim().orEmpty()
                    val text = htmlToText(html).take(maxChars)
                    if (text.length < 40) return@use null
                    Page(title = title, url = res.request.url.toString(), text = text)
                }
            }
        }

    // ============================================================
    // 搜索引擎
    // ============================================================

    /** Bing 的 RSS 输出稳定、没有前端脚本，是最可靠的一路。 */
    private fun searchBingRss(query: String): List<Hit> = runCatching {
        val url = "https://www.bing.com/search?q=${encode(query)}&format=rss&setlang=zh-CN"
        val xml = fetchText(url) ?: return emptyList()
        Regex("<item\\b[^>]*>(.*?)</item>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(xml)
            .mapNotNull { match ->
                val block = match.groupValues[1]
                val link = tagValue(block, "link").ifBlank { tagValue(block, "guid") }
                if (!link.startsWith("http", ignoreCase = true)) return@mapNotNull null
                Hit(
                    title = stripTags(unescapeEntities(tagValue(block, "title"))),
                    url = link,
                    snippet = stripTags(unescapeEntities(tagValue(block, "description"))),
                )
            }
            .take(MAX_RESULTS)
            .toList()
    }.getOrDefault(emptyList())

    private fun searchDuckDuckGo(query: String): List<Hit> = runCatching {
        val html = fetchText("https://html.duckduckgo.com/html/?q=${encode(query)}")
            ?: return emptyList()
        Regex(
            """<a[^>]*class="result__a"[^>]*href="([^"]+)"[^>]*>(.*?)</a>.*?""" +
                    """<a[^>]*class="result__snippet"[^>]*>(.*?)</a>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        ).findAll(html).mapNotNull { match ->
            val link = decodeDuckUrl(match.groupValues[1])
            if (!link.startsWith("http", ignoreCase = true)) return@mapNotNull null
            Hit(
                title = unescapeEntities(stripTags(match.groupValues[2])),
                url = link,
                snippet = unescapeEntities(stripTags(match.groupValues[3])),
            )
        }.take(MAX_RESULTS).toList()
    }.getOrDefault(emptyList())

    /** 部分网络环境下 html.duckduckgo.com 会被拦截，lite 版本作为兜底。 */
    private fun searchDuckDuckGoLite(query: String): List<Hit> = runCatching {
        val html = fetchText("https://lite.duckduckgo.com/lite/?q=${encode(query)}")
            ?: return emptyList()
        Regex(
            """<a[^>]*class="result-link"[^>]*href="([^"]+)"[^>]*>(.*?)</a>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        ).findAll(html).mapNotNull { match ->
            val link = decodeDuckUrl(match.groupValues[1])
            if (!link.startsWith("http", ignoreCase = true)) return@mapNotNull null
            Hit(
                title = unescapeEntities(stripTags(match.groupValues[2])),
                url = link,
                snippet = "",
            )
        }.take(MAX_RESULTS).toList()
    }.getOrDefault(emptyList())

    private fun searchBingHtml(query: String): List<Hit> = runCatching {
        val html = fetchText("https://cn.bing.com/search?q=${encode(query)}&setlang=zh-CN")
            ?: return emptyList()
        Regex(
            """<li class="b_algo".*?<h2><a[^>]*href="([^"]+)"[^>]*>(.*?)</a>.*?(?:<p[^>]*>(.*?)</p>)?""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        ).findAll(html).mapNotNull { match ->
            val link = match.groupValues[1]
            if (!link.startsWith("http", ignoreCase = true)) return@mapNotNull null
            Hit(
                title = unescapeEntities(stripTags(match.groupValues[2])),
                url = link,
                snippet = unescapeEntities(stripTags(match.groupValues[3])),
            )
        }.take(MAX_RESULTS).toList()
    }.getOrDefault(emptyList())

    private fun mergeHits(hits: List<Hit>, limit: Int): List<Hit> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Hit>()
        hits.forEach { hit ->
            if (hit.url.isBlank() || !hit.url.startsWith("http", ignoreCase = true)) return@forEach
            val key = canonicalKey(hit.url)
            if (key.isEmpty() || !seen.add(key)) return@forEach
            if (isJunkUrl(hit.url)) return@forEach
            result.add(
                Hit(
                    title = hit.title.take(200),
                    url = hit.url.take(2048),
                    snippet = hit.snippet.replace(Regex("\\s+"), " ").trim().take(400),
                ),
            )
            if (result.size >= limit) return result
        }
        return result
    }

    private fun canonicalKey(url: String): String = runCatching {
        val noFragment = url.substringBefore('#')
        val host = noFragment.substringAfter("://").substringBefore('/').lowercase()
            .removePrefix("www.")
        val path = noFragment.substringAfter("://").substringAfter('/', "")
            .substringBefore('?')
            .trimEnd('/')
        "$host/$path"
    }.getOrDefault(url)

    private fun isJunkUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("/search?") ||
                lower.contains("bing.com/search") ||
                lower.contains("duckduckgo.com/y.js") ||
                lower.endsWith(".pdf")
    }

    private fun decodeDuckUrl(raw: String): String {
        val normalized = if (raw.startsWith("//")) "https:$raw" else raw
        val encoded = Regex("""[?&]uddg=([^&]+)""").find(normalized)?.groupValues?.get(1)
            ?: return unescapeEntities(normalized)
        return runCatching { URLDecoder.decode(encoded, "UTF-8") }
            .getOrDefault(normalized)
    }

    // ============================================================
    // 网络与文本处理
    // ============================================================

    private fun fetchText(url: String): String? = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", MOBILE_UA)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body ?: return null
            if (body.contentLength() > 5L * 1024L * 1024L) return null
            val bytes = body.bytes()
            if (bytes.isEmpty()) return null
            decodeBytes(bytes, body.contentType())
        }
    }.getOrNull()

    private fun decodeBytes(bytes: ByteArray, contentType: MediaType?): String {
        var charset: Charset? = contentType?.charset()
        if (charset == null) {
            val head = String(bytes, 0, minOf(bytes.size, 4096), Charsets.ISO_8859_1)
            val declared = Regex("(?i)charset=[\"']?([a-zA-Z0-9_\\-]+)")
                .find(head)?.groupValues?.get(1)
            charset = declared?.let {
                runCatching { Charset.forName(it) }.getOrNull()
            }
        }
        val primary = runCatching { String(bytes, charset ?: Charsets.UTF_8) }
            .getOrElse { String(bytes, Charsets.UTF_8) }
        val bad = primary.count { it == '\uFFFD' }
        if (bad > 0 && bad * 160 > primary.length) {
            runCatching { String(bytes, Charset.forName("GB18030")) }
                .getOrNull()?.let { return it }
        }
        return primary
    }

    private fun htmlToText(html: String): String {
        var text = html
        text = text.replace(
            Regex("(?is)<(script|style|noscript|svg|iframe|head|nav|footer)[^>]*>.*?</\\1>"),
            " ",
        )
        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")
        text = text.replace(
            Regex("(?i)</(p|div|h[1-6]|li|tr|section|article|blockquote|pre|dd|dt)>"),
            "\n",
        )
        text = text.replace(Regex("(?s)<[^>]+>"), " ")
        text = unescapeEntities(text)
        return text
            .replace(Regex("[ \\t\\u00A0\\u3000]+"), " ")
            .replace(Regex("\\n[ \\t]*"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun stripTags(html: String): String =
        unescapeEntities(html.replace(Regex("(?s)<[^>]+>"), " ")).trim()

    private fun tagValue(block: String, tag: String): String = Regex(
        "<$tag\\b[^>]*>(.*?)</$tag>",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
    ).find(block)?.groupValues?.get(1)
        ?.removePrefix("<![CDATA[")
        ?.removeSuffix("]]>")
        ?.trim()
        .orEmpty()

    private fun unescapeEntities(raw: String): String {
        var text = raw
            .replace("&nbsp;", " ")
            .replace("&ensp;", " ")
            .replace("&emsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&ldquo;", "“")
            .replace("&rdquo;", "”")
            .replace("&hellip;", "…")
            .replace("&mdash;", "—")
        text = Regex("&#x([0-9a-fA-F]{1,6});").replace(text) { match ->
            codePointToString(match.groupValues[1].toIntOrNull(16))
        }
        text = Regex("&#(\\d{1,7});").replace(text) { match ->
            codePointToString(match.groupValues[1].toIntOrNull())
        }
        return text
    }

    private fun codePointToString(code: Int?): String {
        if (code == null || code <= 0 || code > 0x10FFFF) return ""
        return runCatching { String(Character.toChars(code)) }.getOrDefault("")
    }

    private fun encode(text: String): String =
        runCatching { URLEncoder.encode(text, "UTF-8") }.getOrDefault(text)
}
