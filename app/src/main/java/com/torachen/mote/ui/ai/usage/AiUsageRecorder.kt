package com.torachen.mote.ui.ai.usage

import android.content.Context
import com.torachen.mote.ui.ai.AiProviderConfig
import com.torachen.mote.ui.ai.AiTokenUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** 把 Agent 的每轮请求写入本地 SQLite；不阻塞 UI。 */
internal object AiUsageRecorder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun record(
        context: Context,
        request: AiRequestUsage,
        conversationId: String?,
        messageId: Long?,
        presetId: String?,
        provider: AiProviderConfig,
        modelId: String = provider.model,
    ) {
        val appContext = context.applicationContext
        val usage = request.usage
            ?.takeUnless { it.estimated }
            ?: AiTokenUsage(
                inputTokens = estimateChars(request.estimatedInputChars),
                outputTokens = estimateChars(request.estimatedOutputChars),
                totalTokens = estimateChars(request.estimatedInputChars) +
                        estimateChars(request.estimatedOutputChars),
                estimated = true,
            )
        val total = if (usage.totalTokens > 0) {
            usage.totalTokens
        } else {
            usage.inputTokens + usage.outputTokens + usage.cacheReadTokens + usage.cacheWriteTokens
        }
        val record = AiUsageRecord(
            requestId = request.requestId,
            conversationId = conversationId,
            messageId = messageId,
            presetId = presetId,
            providerId = provider.id,
            providerName = provider.name,
            protocol = provider.protocol.name,
            modelId = modelId,
            inputTokens = usage.inputTokens,
            outputTokens = usage.outputTokens,
            cacheReadTokens = usage.cacheReadTokens,
            cacheWriteTokens = usage.cacheWriteTokens,
            reasoningTokens = usage.reasoningTokens,
            totalTokens = total,
            estimated = usage.estimated,
            costUsd = AiPricing.cost(provider.id, modelId, usage),
            priceVersion = AiPricing.versionFor(provider.id, modelId),
            startedAt = request.startedAt,
            endedAt = request.endedAt,
            latencyMs = (request.endedAt - request.startedAt).coerceAtLeast(0L),
            success = request.success,
            errorCode = request.error,
            streamed = request.streamed,
        )
        scope.launch { AiUsageDb.insert(appContext, record) }
    }

    fun summarize(records: List<AiUsageRecord>): AiUsageSummary =
        records.fold(AiUsageSummary()) { summary, record -> summary.plus(record) }

    fun groupByModel(records: List<AiUsageRecord>): List<AiUsageGroup> =
        group(records) { "${it.providerId}|${it.modelId}" to it.modelId }

    fun groupByProvider(records: List<AiUsageRecord>): List<AiUsageGroup> =
        group(records) { it.providerId to it.providerName.ifBlank { it.providerId } }

    private fun group(
        records: List<AiUsageRecord>,
        keyOf: (AiUsageRecord) -> Pair<String, String>,
    ): List<AiUsageGroup> {
        val grouped = LinkedHashMap<String, Pair<String, MutableList<AiUsageRecord>>>()
        records.forEach { record ->
            val (key, label) = keyOf(record)
            grouped.getOrPut(key) { label to mutableListOf() }.second.add(record)
        }
        return grouped.map { (key, value) ->
            AiUsageGroup(key, value.first, summarize(value.second))
        }.sortedByDescending { it.summary.costUsd }
    }

    private fun estimateChars(chars: Int): Int =
        if (chars <= 0) 0 else ((chars + 3) / 4).coerceAtLeast(1)
}
