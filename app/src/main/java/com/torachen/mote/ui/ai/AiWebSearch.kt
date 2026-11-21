package com.torachen.mote.ui.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * 真实可用的联网能力：搜索 + 抓取网页正文。
 *
 * 只负责「查」和「读」，不负责决定查什么 —— 由 Agent 循环里的模型自主决定
 * 搜索几次、搜什么、打开哪个链接，因此支持在推理过程中分批、多轮搜索。
 */
internal object AiWebSearch {

    private const val MAX_RESULTS = 10
    private const val MAX_PAGE_CHARS = 9_000
    private const val SEARCH_PROVIDER_TIMEOUT_MS = 4_000L
    private const val SEARCH_TOTAL_TIMEOUT_MS = 6_500L
    private const val SEARCH_CACHE_TTL_MS = 3 * 60_000L
    private const val SEARCH_CACHE_MAX = 32
    private const val PAGE_CACHE_TTL_MS = 5 * 60_000L
    private const val PAGE_CACHE_MAX = 24

    private const val MOBILE_UA =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/130.0.0.0 Mobile Safari/537.36"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    private data class Timed<T>(val value: T, val createdAt: Long)

    private val searchCache = object : LinkedHashMap<String, Timed<List<Hit>>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Timed<List<Hit>>>?): Boolean =
            size > SEARCH_CACHE_MAX
    }

    private val pageCache = object : LinkedHashMap<String, Timed<Page>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Timed<Page>>?): Boolean =
            size > PAGE_CACHE_MAX
    }

    data class Hit(
        val title: String,
        val url: String,
        val snippet: String,
        val publishedAt: String? = null,
    )

    data class Page(
        val title: String,
        val url: String,
        val text: String,
    )

    suspend fun search(query: String, limit: Int = MAX_RESULTS): List<Hit> =
        withContext(Dispatchers.IO) {
            val clean = query.trim()
                .replace(Regex("\\s+"), " ")
                .trim('。', '，', '！', '？', '?', '!')
                .take(120)
            if (clean.isEmpty()) return@withContext emptyList()

            cachedSearch(clean)?.let { cached ->
                if (cached.isNotEmpty()) return@withContext cached.take(limit)
            }

            val hits = withTimeoutOrNull(SEARCH_TOTAL_TIMEOUT_MS) {
                searchFast(clean, limit)
            }.orEmpty()

            if (hits.isNotEmpty()) {
                putSearchCache(clean, hits)
                return@withContext hits
            }

            // 兜底只跑一次轻量 lite 结果，避免多个慢源把失败等待拖到十几秒。
            val fallback = withTimeoutOrNull(3_000L) {
                searchDuckDuckGoLite(clean)
            }.orEmpty()
            mergeHits(fallback, limit).also { result ->
                if (result.isNotEmpty()) putSearchCache(clean, result)
            }
        }

    /**
     * 同时发起多个搜索源，谁先返回非空结果就用谁。
     *
     * 旧实现会等两个源都完成后再合并；在某个源超时/被墙时，整体会被拖到 12 秒。
     * 这里用 Channel 竞争，其余请求在拿到首个结果后立即取消，常见情况 1 秒内返回。
     */
    private suspend fun searchFast(query: String, limit: Int): List<Hit> = supervisorScope {
        val providers: List<suspend (String) -> List<Hit>> = listOf(
            ::searchBingRss,
            ::searchBingHtml,
            ::searchDuckDuckGo,
        )
        val channel = Channel<List<Hit>>(Channel.UNLIMITED)
        val jobs = providers.map { provider ->
            launch {
                val result = withTimeoutOrNull(SEARCH_PROVIDER_TIMEOUT_MS) {
                    runCatching { provider(query) }.getOrElse {
                        if (it is CancellationException) throw it
                        emptyList()
                    }
                }.orEmpty()
                channel.send(result)
            }
        }
        var best = emptyList<Hit>()
        try {
            repeat(providers.size) {
                val batch = withTimeoutOrNull(SEARCH_PROVIDER_TIMEOUT_MS + 500L) {
                    channel.receive()
                } ?: return@supervisorScope best
                if (batch.isNotEmpty()) {
                    best = mergeHits(best + batch, limit)
                    if (best.size >= limit) return@supervisorScope best
                    // 已有一个可用结果时，只再给其它源一个很短的合并窗口，不阻塞首屏。
                    val extra = withTimeoutOrNull(180L) { channel.receive() }.orEmpty()
                    if (extra.isNotEmpty()) best = mergeHits(best + extra, limit)
                    return@supervisorScope best
                }
            }
        } finally {
            jobs.forEach { it.cancel() }
            channel.close()
        }
        best
    }

    private fun cachedSearch(query: String): List<Hit>? = synchronized(searchCache) {
        val entry = searchCache[query] ?: return null
        if (System.currentTimeMillis() - entry.createdAt > SEARCH_CACHE_TTL_MS) {
            searchCache.remove(query)
            null
        } else {
            entry.value
        }
    }

    private fun putSearchCache(query: String, hits: List<Hit>) = synchronized(searchCache) {
        searchCache[query] = Timed(hits, System.currentTimeMillis())
    }

    suspend fun fetchPage(url: String, maxChars: Int = MAX_PAGE_CHARS): Page? =
        withContext(Dispatchers.IO) {
            if (!url.startsWith("http", ignoreCase = true)) return@withContext null
            cachedPage(url)?.let { cached ->
                return@withContext cached.copy(text = cached.text.take(maxChars))
            }
            withTimeoutOrNull(10_000L) {
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
                    val text = htmlToText(html.take(1_500_000)).take(maxChars)
                    if (text.length < 40) return@use null
                    Page(title = title, url = res.request.url.toString(), text = text).also { page ->
                        putPageCache(url, page)
                    }
                }
            }
        }

    private fun cachedPage(url: String): Page? = synchronized(pageCache) {
        val entry = pageCache[url] ?: return null
        if (System.currentTimeMillis() - entry.createdAt > PAGE_CACHE_TTL_MS) {
            pageCache.remove(url)
            null
        } else {
            entry.value
        }
    }

    private fun putPageCache(url: String, page: Page) = synchronized(pageCache) {
        pageCache[url] = Timed(page, System.currentTimeMillis())
    }

    // ============================================================
    // 搜索引擎
    // ============================================================

    /** Bing 的 RSS 输出稳定、没有前端脚本，是最可靠的一路。 */
    private suspend fun searchBingRss(query: String): List<Hit> = runCatching {
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
                    publishedAt = tagValue(block, "pubDate").trim().takeIf { it.isNotBlank() },
                )
            }
            .take(MAX_RESULTS)
            .toList()
    }.getOrElse {
        if (it is CancellationException) throw it
        emptyList()
    }

    private suspend fun searchDuckDuckGo(query: String): List<Hit> = runCatching {
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
    }.getOrElse {
        if (it is CancellationException) throw it
        emptyList()
    }

    /** 部分网络环境下 html.duckduckgo.com 会被拦截，lite 版本作为兜底。 */
    private suspend fun searchDuckDuckGoLite(query: String): List<Hit> = runCatching {
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
    }.getOrElse {
        if (it is CancellationException) throw it
        emptyList()
    }

    private suspend fun searchBingHtml(query: String): List<Hit> = runCatching {
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
    }.getOrElse {
        if (it is CancellationException) throw it
        emptyList()
    }

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
                    publishedAt = hit.publishedAt,
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

    private suspend fun fetchText(url: String): String? =
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", MOBILE_UA)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7")
                .build()
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resume(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val text = runCatching {
                        response.use { res ->
                            if (!res.isSuccessful) return@use null
                            val body = res.body ?: return@use null
                            if (body.contentLength() > 5L * 1024L * 1024L) return@use null
                            val bytes = body.bytes()
                            if (bytes.isEmpty()) return@use null
                            decodeBytes(bytes, body.contentType())
                        }
                    }.getOrNull()
                    if (continuation.isActive) continuation.resume(text)
                }
            })
        }

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
