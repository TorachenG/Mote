package com.torachen.mote.ui.ai.usage

import com.torachen.mote.ui.ai.AiTokenUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AiPricingTest {

    @Test
    fun `calculates cost from disjoint buckets`() {
        val usage = AiTokenUsage(
            inputTokens = 1_000_000,
            outputTokens = 1_000_000,
            cacheReadTokens = 1_000_000,
            totalTokens = 2_000_000,
        )
        val cost = AiPricing.cost("deepseek", "deepseek-chat", usage)
        assertNotNull(cost)
        assertEquals(0.27 + 1.10 + 0.07, cost!!, 0.000001)
    }

    @Test
    fun `returns null for unknown model`() {
        assertNull(AiPricing.cost("custom", "totally-unknown-model", AiTokenUsage()))
    }

    @Test
    fun `summary keeps estimated and failed counts`() {
        val record = AiUsageRecord(
            requestId = "r1",
            conversationId = null,
            messageId = null,
            presetId = null,
            providerId = "p",
            providerName = "P",
            protocol = "OpenAi",
            modelId = "m",
            inputTokens = 10,
            outputTokens = 20,
            cacheReadTokens = 0,
            cacheWriteTokens = 0,
            reasoningTokens = 0,
            totalTokens = 30,
            estimated = true,
            costUsd = 0.01,
            priceVersion = null,
            startedAt = 1L,
            endedAt = 2L,
            latencyMs = 1L,
            success = false,
            errorCode = "x",
            streamed = true,
        )
        val summary = AiUsageRecorder.summarize(listOf(record))
        assertEquals(1, summary.requests)
        assertEquals(30, summary.totalTokens)
        assertEquals(1, summary.estimatedRequests)
        assertEquals(1, summary.failedRequests)
    }
}
