package com.torachen.mote.ui.ai.usage

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

internal object AiUsageExport {

    fun csv(records: List<AiUsageRecord>): String = buildString {
        append(
            "time,provider,model,protocol,input_tokens,output_tokens," +
                    "cache_read_tokens,cache_write_tokens,reasoning_tokens,total_tokens," +
                    "estimated,cost_usd,latency_ms,success,error,conversation_id,request_id\n",
        )
        records.forEach { record ->
            append(cell(isoTime(record.startedAt))).append(',')
            append(cell(record.providerName)).append(',')
            append(cell(record.modelId)).append(',')
            append(cell(record.protocol)).append(',')
            append(record.inputTokens).append(',')
            append(record.outputTokens).append(',')
            append(record.cacheReadTokens).append(',')
            append(record.cacheWriteTokens).append(',')
            append(record.reasoningTokens).append(',')
            append(record.totalTokens).append(',')
            append(record.estimated).append(',')
            append(record.costUsd?.let { "%.8f".format(it) } ?: "").append(',')
            append(record.latencyMs ?: "").append(',')
            append(record.success).append(',')
            append(cell(record.errorCode.orEmpty())).append(',')
            append(cell(record.conversationId.orEmpty())).append(',')
            append(cell(record.requestId)).append('\n')
        }
    }

    fun json(records: List<AiUsageRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject().apply {
                    put("request_id", record.requestId)
                    put("conversation_id", record.conversationId)
                    put("message_id", record.messageId)
                    put("preset_id", record.presetId)
                    put("provider", record.providerName)
                    put("provider_id", record.providerId)
                    put("model", record.modelId)
                    put("protocol", record.protocol)
                    put("input_tokens", record.inputTokens)
                    put("output_tokens", record.outputTokens)
                    put("cache_read_tokens", record.cacheReadTokens)
                    put("cache_write_tokens", record.cacheWriteTokens)
                    put("reasoning_tokens", record.reasoningTokens)
                    put("total_tokens", record.totalTokens)
                    put("estimated", record.estimated)
                    put("cost_usd", record.costUsd)
                    put("price_version", record.priceVersion)
                    put("started_at", record.startedAt)
                    put("ended_at", record.endedAt)
                    put("latency_ms", record.latencyMs)
                    put("success", record.success)
                    put("error", record.errorCode)
                    put("streamed", record.streamed)
                },
            )
        }
        return JSONObject().put("exported_at", System.currentTimeMillis())
            .put("records", array)
            .toString(2)
    }

    fun write(context: Context, uri: Uri, text: String): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) }
        true
    }.getOrDefault(false)

    private fun cell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains('\n') || escaped.contains('"')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun isoTime(timestamp: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date(timestamp))
}
