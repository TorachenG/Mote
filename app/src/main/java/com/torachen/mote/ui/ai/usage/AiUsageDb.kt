package com.torachen.mote.ui.ai.usage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** 本地用量数据库；不上传、不参与云同步。 */
internal object AiUsageDb {

    private const val DB_NAME = "ai_usage.db"
    private const val DB_VERSION = 1

    @Volatile
    private var helper: Helper? = null

    private fun helper(context: Context): Helper =
        helper ?: synchronized(this) {
            helper ?: Helper(context.applicationContext).also { helper = it }
        }

    fun insert(context: Context, record: AiUsageRecord): Boolean = runCatching {
        val values = ContentValues().apply {
            put("request_id", record.requestId)
            put("conversation_id", record.conversationId)
            put("message_id", record.messageId)
            put("preset_id", record.presetId)
            put("provider_id", record.providerId)
            put("provider_name", record.providerName)
            put("protocol", record.protocol)
            put("model_id", record.modelId)
            put("input_tokens", record.inputTokens)
            put("output_tokens", record.outputTokens)
            put("cache_read_tokens", record.cacheReadTokens)
            put("cache_write_tokens", record.cacheWriteTokens)
            put("reasoning_tokens", record.reasoningTokens)
            put("total_tokens", record.totalTokens)
            put("estimated", if (record.estimated) 1 else 0)
            record.costUsd?.let { put("cost_usd", it) } ?: putNull("cost_usd")
            put("price_version", record.priceVersion)
            put("started_at", record.startedAt)
            put("ended_at", record.endedAt)
            put("latency_ms", record.latencyMs)
            put("success", if (record.success) 1 else 0)
            put("error_code", record.errorCode)
            put("streamed", if (record.streamed) 1 else 0)
        }
        helper(context).writableDatabase.insertWithOnConflict(
            "ai_usage",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        ) != -1L
    }.getOrDefault(false)

    fun querySince(context: Context, since: Long, limit: Int = 20_000): List<AiUsageRecord> =
        runCatching {
            helper(context).readableDatabase.query(
                "ai_usage",
                null,
                "started_at >= ?",
                arrayOf(since.toString()),
                null,
                null,
                "started_at DESC",
                limit.toString(),
            ).use { cursor -> cursor.toRecords() }
        }.getOrDefault(emptyList())

    fun queryAll(context: Context, limit: Int = 20_000): List<AiUsageRecord> =
        runCatching {
            helper(context).readableDatabase.query(
                "ai_usage",
                null,
                null,
                null,
                null,
                null,
                "started_at DESC",
                limit.toString(),
            ).use { cursor -> cursor.toRecords() }
        }.getOrDefault(emptyList())

    fun clear(context: Context): Boolean = runCatching {
        helper(context).writableDatabase.delete("ai_usage", null, null)
        true
    }.getOrDefault(false)

    private fun Cursor.toRecords(): List<AiUsageRecord> {
        val result = ArrayList<AiUsageRecord>(count)
        while (moveToNext()) {
            result.add(
                AiUsageRecord(
                    id = getLong(getColumnIndexOrThrow("id")),
                    requestId = getString(getColumnIndexOrThrow("request_id")),
                    conversationId = getStringOrNull("conversation_id"),
                    messageId = getLongOrNull("message_id"),
                    presetId = getStringOrNull("preset_id"),
                    providerId = getString(getColumnIndexOrThrow("provider_id")),
                    providerName = getStringOrNull("provider_name").orEmpty(),
                    protocol = getStringOrNull("protocol").orEmpty(),
                    modelId = getString(getColumnIndexOrThrow("model_id")),
                    inputTokens = getInt(getColumnIndexOrThrow("input_tokens")),
                    outputTokens = getInt(getColumnIndexOrThrow("output_tokens")),
                    cacheReadTokens = getInt(getColumnIndexOrThrow("cache_read_tokens")),
                    cacheWriteTokens = getInt(getColumnIndexOrThrow("cache_write_tokens")),
                    reasoningTokens = getInt(getColumnIndexOrThrow("reasoning_tokens")),
                    totalTokens = getInt(getColumnIndexOrThrow("total_tokens")),
                    estimated = getInt(getColumnIndexOrThrow("estimated")) == 1,
                    costUsd = getDoubleOrNull("cost_usd"),
                    priceVersion = getStringOrNull("price_version"),
                    startedAt = getLong(getColumnIndexOrThrow("started_at")),
                    endedAt = getLongOrNull("ended_at"),
                    latencyMs = getLongOrNull("latency_ms"),
                    success = getInt(getColumnIndexOrThrow("success")) == 1,
                    errorCode = getStringOrNull("error_code"),
                    streamed = getInt(getColumnIndexOrThrow("streamed")) == 1,
                ),
            )
        }
        return result
    }

    private fun Cursor.getStringOrNull(name: String): String? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getString(index)
    }

    private fun Cursor.getLongOrNull(name: String): Long? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getLong(index)
    }

    private fun Cursor.getDoubleOrNull(name: String): Double? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getDouble(index)
    }

    private class Helper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE ai_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    request_id TEXT NOT NULL UNIQUE,
                    conversation_id TEXT,
                    message_id INTEGER,
                    preset_id TEXT,
                    provider_id TEXT NOT NULL,
                    provider_name TEXT,
                    protocol TEXT,
                    model_id TEXT NOT NULL,
                    input_tokens INTEGER NOT NULL DEFAULT 0,
                    output_tokens INTEGER NOT NULL DEFAULT 0,
                    cache_read_tokens INTEGER NOT NULL DEFAULT 0,
                    cache_write_tokens INTEGER NOT NULL DEFAULT 0,
                    reasoning_tokens INTEGER NOT NULL DEFAULT 0,
                    total_tokens INTEGER NOT NULL DEFAULT 0,
                    estimated INTEGER NOT NULL DEFAULT 0,
                    cost_usd REAL,
                    price_version TEXT,
                    started_at INTEGER NOT NULL,
                    ended_at INTEGER,
                    latency_ms INTEGER,
                    success INTEGER NOT NULL DEFAULT 1,
                    error_code TEXT,
                    streamed INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX idx_ai_usage_started ON ai_usage(started_at)")
            db.execSQL("CREATE INDEX idx_ai_usage_model ON ai_usage(provider_id, model_id)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // 用量是本地统计，升级直接重建；不触碰对话数据。
            db.execSQL("DROP TABLE IF EXISTS ai_usage")
            onCreate(db)
        }
    }
}
