package com.torachen.mote.ui.ai.security

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * 把旧版 `ai_providers` 里的明文 apiKey 迁到 AndroidKeyStore。
 *
 * 两阶段提交：
 * 1. 先把所有 Key 写入 Keystore，并逐个解密校验；
 * 2. 全部成功后才用只含 apiKeyRef 的 JSON 覆盖旧 JSON。
 *
 * 任何一步失败都保留旧 JSON，避免用户丢 Key；下次启动会重试。
 */
internal object AiProviderSecretMigrator {

    private const val TAG = "AiProviderSecretMigrator"
    private const val PREFS_NAME = "mote_prefs"
    private const val KEY_PROVIDERS = "ai_providers"

    data class Result(
        val migrated: Int,
        val failed: Boolean,
        val message: String? = null,
    )

    fun migrate(context: Context): Result {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_PROVIDERS, null)
        if (raw.isNullOrBlank()) return Result(0, false)

        val array = runCatching { JSONArray(raw) }.getOrNull()
            ?: return Result(0, true, "ai_providers 不是合法 JSON")

        var migrated = 0
        val pending = mutableListOf<Pair<String, String>>() // ref to plaintext
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val id = obj.optString("id").trim()
            if (id.isBlank()) continue
            val key = obj.optString("apiKey")
            if (key.isBlank()) continue
            val ref = obj.optString("apiKeyRef").trim().ifBlank { "provider:$id:apiKey" }
            if (!AiSecretStore.put(context, ref, key)) {
                return Result(
                    migrated = 0,
                    failed = true,
                    message = "Keystore 写入失败，保留旧配置",
                )
            }
            pending.add(ref to key)
            obj.remove("apiKey")
            obj.put("apiKeyRef", ref)
            migrated++
        }

        if (migrated == 0) return Result(0, false)

        // 回读校验，确保密钥真的可用，再把明文从 prefs 移除。
        for ((ref, expected) in pending) {
            if (AiSecretStore.get(context, ref) != expected) {
                return Result(0, true, "密钥回读校验失败，保留旧配置")
            }
        }

        val committed = prefs.edit()
            .putString(KEY_PROVIDERS, array.toString())
            .commit()
        if (!committed) {
            return Result(0, true, "配置写入失败，保留旧配置")
        }
        Log.i(TAG, "migrated $migrated provider key(s) to AndroidKeyStore")
        return Result(migrated, false)
    }
}
