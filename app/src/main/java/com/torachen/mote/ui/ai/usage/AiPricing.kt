package com.torachen.mote.ui.ai.usage

import com.torachen.mote.ui.ai.AiTokenUsage

/**
 * 本地估算价格表（美元 / 百万 token）。
 *
 * provider 的账单才是权威；这里只用于趋势和“大概花了多少”，没有价格的模型显示 —。
 */
internal object AiPricing {

    const val VERSION = "2026-11-estimate"

    data class Price(
        val inputPerM: Double,
        val outputPerM: Double,
        val cacheReadPerM: Double = inputPerM,
        val cacheWritePerM: Double = inputPerM,
    )

    private val prices = listOf(
        // DeepSeek
        "deepseek-chat" to Price(0.27, 1.10, 0.07, 0.27),
        "deepseek-reasoner" to Price(0.55, 2.19, 0.14, 0.55),
        // OpenAI
        "gpt-4o" to Price(2.50, 10.00, 1.25, 2.50),
        "gpt-4o-mini" to Price(0.15, 0.60, 0.075, 0.15),
        "gpt-4.1" to Price(2.00, 8.00, 0.50, 2.00),
        "o3" to Price(2.00, 8.00, 0.50, 2.00),
        // Anthropic
        "claude-3-5-sonnet" to Price(3.00, 15.00, 0.30, 3.75),
        "claude-3-7-sonnet" to Price(3.00, 15.00, 0.30, 3.75),
        "claude-sonnet-4" to Price(3.00, 15.00, 0.30, 3.75),
        "claude-opus-4" to Price(15.00, 75.00, 1.50, 18.75),
        // Gemini
        "gemini-2.0-flash" to Price(0.10, 0.40, 0.025, 0.10),
        "gemini-2.5-pro" to Price(1.25, 10.00, 0.31, 1.25),
        "gemini-2.5-flash" to Price(0.30, 2.50, 0.075, 0.30),
        // Qwen / 常见兼容模型
        "qwen-max" to Price(1.60, 6.40),
        "qwen-plus" to Price(0.40, 1.20),
        "glm-4" to Price(0.60, 2.40),
        "kimi-k2" to Price(0.60, 2.50),
    )

    fun cost(providerId: String?, modelId: String, usage: AiTokenUsage): Double? {
        val price = find(providerId, modelId) ?: return null
        val input = usage.inputTokens / 1_000_000.0 * price.inputPerM
        val output = usage.outputTokens / 1_000_000.0 * price.outputPerM
        val cacheRead = usage.cacheReadTokens / 1_000_000.0 * price.cacheReadPerM
        val cacheWrite = usage.cacheWriteTokens / 1_000_000.0 * price.cacheWritePerM
        return input + output + cacheRead + cacheWrite
    }

    fun versionFor(providerId: String?, modelId: String): String? =
        if (find(providerId, modelId) != null) VERSION else null

    private fun find(providerId: String?, modelId: String): Price? {
        val id = modelId.lowercase()
        prices.firstOrNull { (model, _) -> id == model }?.let { return it.second }
        prices.firstOrNull { (model, _) -> id.startsWith(model) }?.let { return it.second }
        // 只按模型名兜底，不按 provider 猜价格，避免把自定义服务算错。
        return null
    }
}
