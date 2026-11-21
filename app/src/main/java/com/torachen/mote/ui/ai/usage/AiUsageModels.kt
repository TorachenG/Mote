package com.torachen.mote.ui.ai.usage

internal data class AiUsageRecord(
    val id: Long = 0L,
    val requestId: String,
    val conversationId: String?,
    val messageId: Long?,
    val presetId: String?,
    val providerId: String,
    val providerName: String,
    val protocol: String,
    val modelId: String,
    val inputTokens: Int,
    val outputTokens: Int,
    val cacheReadTokens: Int,
    val cacheWriteTokens: Int,
    val reasoningTokens: Int,
    val totalTokens: Int,
    val estimated: Boolean,
    val costUsd: Double?,
    val priceVersion: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val latencyMs: Long?,
    val success: Boolean,
    val errorCode: String?,
    val streamed: Boolean,
)

internal data class AiUsageSummary(
    val requests: Int = 0,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val cacheReadTokens: Int = 0,
    val cacheWriteTokens: Int = 0,
    val reasoningTokens: Int = 0,
    val totalTokens: Int = 0,
    val costUsd: Double = 0.0,
    val estimatedRequests: Int = 0,
    val failedRequests: Int = 0,
) {
    fun plus(record: AiUsageRecord): AiUsageSummary = AiUsageSummary(
        requests = requests + 1,
        inputTokens = inputTokens + record.inputTokens,
        outputTokens = outputTokens + record.outputTokens,
        cacheReadTokens = cacheReadTokens + record.cacheReadTokens,
        cacheWriteTokens = cacheWriteTokens + record.cacheWriteTokens,
        reasoningTokens = reasoningTokens + record.reasoningTokens,
        totalTokens = totalTokens + record.totalTokens,
        costUsd = costUsd + (record.costUsd ?: 0.0),
        estimatedRequests = estimatedRequests + if (record.estimated) 1 else 0,
        failedRequests = failedRequests + if (!record.success) 1 else 0,
    )
}

internal data class AiUsageGroup(
    val key: String,
    val label: String,
    val summary: AiUsageSummary,
)
