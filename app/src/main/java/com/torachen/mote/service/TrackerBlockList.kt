package com.torachen.mote.service

/**
 * 跟踪器 / 广告域名黑名单。
 * 基于 EasyPrivacy 与 Disconnect 列表精简的高频域名。
 */
internal object TrackerBlockList {

    private val EXACT_BLOCKLIST: Set<String> = setOf(
        // ---- 广告 ----
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adnxs.com", "adsrvr.org", "advertising.com", "adzerk.net",
        "amazon-adsystem.com", "criteo.com", "criteo.net", "casalemedia.com",
        "contextweb.com", "openx.net", "pubmatic.com", "rubiconproject.com",
        "taboola.com", "outbrain.com", "smartadserver.com", "teads.tv",
        "sharethrough.com", "indexexchange.com", "sovrn.com", "yieldmo.com",
        "bidswitch.net", "turn.com", "mathtag.com", "media.net",

        // ---- 跟踪 / 分析 ----
        "google-analytics.com", "googletagmanager.com", "googletagservices.com",
        "scorecardresearch.com", "quantserve.com", "comscore.com",
        "mixpanel.com", "segment.com", "segment.io", "amplitude.com",
        "heap.io", "fullstory.com", "hotjar.com", "mouseflow.com",
        "crazyegg.com", "clicktale.net", "luckyorange.com",
        "bugsnag.com", "sentry.io", "newrelic.com", "datadoghq.com",
        "branch.io", "adjust.com", "appsflyer.com", "kochava.com",
        "chartbeat.com", "parsely.com", "mgid.com", "revcontent.com",

        // ---- 社交追踪 ----
        "connect.facebook.net", "platform.twitter.com", "syndication.twitter.com",
        "platform.instagram.com", "platform.linkedin.com",
        "addthis.com", "sharethis.com", "addtoany.com",

        // ---- 指纹识别 ----
        "fingerprintjs.com", "fpjs.io", "maxmind.com",
    )

    // 严格模式额外拦截：营销自动化 / 推送 / 企业分析 / DMP
    private val STRICT_EXTRA: Set<String> = setOf(
        "adroll.com",
        "qualtrics.com", "alchemer.com", "clarity.ms",
        "lr-ingest.io", "lr-in.com",
        "visualwebsiteoptimizer.com", "vwo.com", "optimizely.com",
        "abtasty.com", "dynamicyield.com", "monetate.net",
        "evergage.com", "bluecore.com", "exponea.com",
        "tealiumiq.com", "tealium.com", "ensighten.com",
        "tagcommander.com", "commandersact.com",
        "pushcrew.com", "pushengage.com", "izooto.com",
        "cleverpush.com", "onesignal.com",
        "sailthru.com", "bluekai.com", "exelator.com",
        "demdex.net", "omtrdc.net", "everesttech.net", "adobedtm.com",
    )

    /**
     * 检查 URL 是否属于跟踪器 / 广告域名。
     * @return 命中的分类（"ad" / "tracker" / "social"），null 表示不拦截
     */
    fun classify(url: String): String? {
        val lower = url.lowercase()
        if (!lower.startsWith("http")) return null

        val host = try {
            lower.substringAfter("://").substringBefore('/').substringBefore(':')
        } catch (_: Exception) { return null }
        if (host.isBlank()) return null

        for (blocked in EXACT_BLOCKLIST) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return when {
                    blocked.contains("analytics") || blocked.contains("scorecard")
                            || blocked.contains("comscore") || blocked.contains("mixpanel")
                            || blocked.contains("segment") || blocked.contains("amplitude")
                            || blocked.contains("hotjar") || blocked.contains("sentry")
                            || blocked.contains("branch") || blocked.contains("adjust")
                            || blocked.contains("appsflyer") -> "tracker"

                    blocked.contains("facebook") || blocked.contains("twitter")
                            || blocked.contains("instagram") || blocked.contains("linkedin")
                            || blocked.contains("addthis") || blocked.contains("sharethis") -> "social"

                    else -> "ad"
                }
            }
        }
        return null
    }

    /**
     * 按防护级别检查。
     * @param level 0=基本，1=均衡（推荐），2=严格
     */
    fun classifyWithLevel(url: String, level: Int): String? {
        val base = classify(url)
        if (base != null) {
            if (level == 0 && base == "social") return null
            return base
        }
        if (level >= 2) {
            return classifyStrict(url)
        }
        return null
    }

    private fun classifyStrict(url: String): String? {
        val host = try {
            val lower = url.lowercase()
            if (!lower.startsWith("http")) return null
            lower.substringAfter("://").substringBefore('/').substringBefore(':')
        } catch (_: Exception) {
            return null
        }
        if (host.isBlank()) return null
        for (blocked in STRICT_EXTRA) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return "tracker"
            }
        }
        return null
    }
}