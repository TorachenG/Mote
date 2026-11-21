package com.torachen.mote.ui.ai.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AI 密钥的本地加密存储。
 *
 * 设计目标：
 * - API Key、MCP Token、插件密钥永远不以明文写入 SharedPreferences / JSON；
 * - 使用 AndroidKeyStore 生成的 AES-256-GCM 密钥，密文与 IV 存独立 prefs；
 * - 用 secretRef 作为 AAD，防止把一条密文挪到另一个引用上；
 * - Keystore 密钥不可用时（例如备份恢复到新设备）返回 null，让上层提示重新输入，
 *   而不是崩溃或静默使用空 Key。
 *
 * 注意：`mote_secrets` 必须从系统备份/云同步中排除，因为 Keystore 密钥不会随备份恢复。
 */
internal object AiSecretStore {

    private const val PREFS_NAME = "mote_secrets"
    private const val KEY_ALIAS = "mote_ai_secret_key_v1"
    private const val VALUE_PREFIX = "v1:"
    private const val GCM_TAG_BITS = 128

    @Volatile
    private var cachedKey: SecretKey? = null

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    @Synchronized
    private fun secretKey(): SecretKey? {
        cachedKey?.let { return it }
        return runCatching {
            val store = keyStore()
            val existing = store.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (existing != null) {
                cachedKey = existing.secretKey
                return existing.secretKey
            }
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore",
            )
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generator.generateKey().also { cachedKey = it }
        }.getOrNull()
    }

    /** 新建一个稳定、可读的 secretRef。 */
    fun newRef(prefix: String): String {
        val safe = prefix.trim().ifBlank { "secret" }
        return "$safe:${UUID.randomUUID()}"
    }

    /**
     * 写入或覆盖一个密钥。空值等价于删除。
     * @return true 表示写入成功；false 表示 Keystore 不可用或写入失败。
     */
    fun put(context: Context, ref: String, value: String): Boolean {
        if (ref.isBlank()) return false
        if (value.isEmpty()) {
            delete(context, ref)
            return true
        }
        val key = secretKey() ?: return false
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher.updateAAD(ref.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            val cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            val encoded = VALUE_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
            prefs(context).edit().putString(ref, encoded).commit()
        }.getOrDefault(false)
    }

    /** 读取密钥；Keystore 密钥失效、密文损坏或不存在时返回 null。 */
    fun get(context: Context, ref: String): String? {
        if (ref.isBlank()) return null
        val raw = prefs(context).getString(ref, null) ?: return null
        if (!raw.startsWith(VALUE_PREFIX)) return null
        val key = secretKey() ?: return null
        return runCatching {
            val combined = Base64.decode(raw.removePrefix(VALUE_PREFIX), Base64.NO_WRAP)
            if (combined.size <= 12) return null
            val iv = combined.copyOfRange(0, 12)
            val cipherText = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.updateAAD(ref.toByteArray(Charsets.UTF_8))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        }.getOrNull()
    }

    fun has(context: Context, ref: String): Boolean = get(context, ref) != null

    fun delete(context: Context, ref: String) {
        if (ref.isBlank()) return
        prefs(context).edit().remove(ref).apply()
    }

    /** 只允许测试/迁移失败时使用：清空所有密文，不删除 Keystore 密钥。 */
    fun clearAll(context: Context) {
        prefs(context).edit().clear().commit()
    }
}
