package com.torachen.mote.ui.ai

/** 来源登记：同一 URL 只编号一次，编号按首次出现的顺序，和 Demo 的 SourceRegistry 一致。 */
internal class AiSourceRegistry {
    private val list = mutableListOf<AiSource>()
    private val byUrl = mutableMapOf<String, AiSource>()

    fun add(
        url: String,
        title: String,
        snippet: String = "",
        publishedAt: String? = null,
    ): AiSource? {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://", ignoreCase = true) &&
            !cleanUrl.startsWith("https://", ignoreCase = true)
        ) {
            return null
        }
        byUrl[cleanUrl]?.let { existing ->
            if (existing.title == cleanUrl && title.isNotBlank()) {
                existing.title = title
            }
            if (existing.snippet.isBlank() && snippet.isNotBlank()) {
                existing.snippet = snippet
            }
            existing.publishedAt = existing.publishedAt ?: publishedAt
            return existing
        }
        val source = AiSource(
            index = list.size + 1,
            url = cleanUrl,
            title = title.trim().ifBlank { cleanUrl },
            siteName = siteNameOf(cleanUrl),
            snippet = snippet.trim(),
            publishedAt = publishedAt,
        )
        list.add(source)
        byUrl[cleanUrl] = source
        return source
    }

    fun snapshot(): List<AiSource> = list.toList()

    private fun siteNameOf(url: String): String = runCatching {
        java.net.URI(url).host.orEmpty().removePrefix("www.")
    }.getOrDefault("")
}
