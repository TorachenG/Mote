package com.torachen.mote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.torachen.mote.ui.MoteCloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 按「同步频率」设置驱动自动同步。
 *
 * - 实时同步（FREQ_REALTIME）：监听本地数据变化，防抖后立刻同步
 * - 每隔 1 分钟（FREQ_INTERVAL）：启动即同步一次，之后每分钟一次
 * - 启动时同步（FREQ_STARTUP）：只由 [MoteApplication] 在冷启动时执行一次
 */
object MoteAutoSync {

    const val KEY_FREQUENCY = "sync_frequency"
    const val FREQ_REALTIME = 0
    const val FREQ_INTERVAL = 1
    const val FREQ_STARTUP = 2

    val FREQUENCY_OPTIONS = listOf("实时同步", "每隔 1 分钟同步", "启动时同步")

    private const val TAG = "MoteAutoSync"
    private const val PREFS = "mote_prefs"
    private const val KEY_LAST_SYNC = "sync_last_modified"
    private const val DEBOUNCE_MS = 1200L
    private const val INTERVAL_MS = 60_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loopJob: Job? = null
    private var debounceJob: Job? = null
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    @Volatile private var syncing = false
    @Volatile private var applyingRemote = false

    /** 同步项对应的偏好键 */
    private val DATA_KEYS = setOf(
        "bookmarks_v2", "history", "search_history", "home_quick_urls", "saved_passwords_v1"
    )

    /** 与同步无关、不应触发同步的键（与 buildLocalData 的排除表保持一致） */
    private val IGNORED_KEYS = setOf(
        "privacy_agreed", "ux_improvement_enabled", "sync_webdav_url", "sync_webdav_user",
        "sync_webdav_pass", "sync_master_pass", "sync_last_modified", "session_snapshot",
        "app_icon_id", "about_last_check_time", "web_saved_zooms_v2", "mote_download_tasks_v1",
        "cloudbase_access_token", "cloudbase_refresh_token", "cloudbase_user_id",
        "cloudbase_username", "sync_e2ee_enabled", "sync_fallback_pass", "sync_e2ee_derived_key",
        "sync_frequency", "sync_provider", "sync_auto_on_start", "sync_conflict_pending"
    )

    fun start(context: Context) {
        val app = context.applicationContext
        stop(app)
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        when (prefs.getInt(KEY_FREQUENCY, FREQ_REALTIME)) {
            FREQ_REALTIME -> {
                val l = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == null || applyingRemote || !isSyncableKey(p, key)) return@OnSharedPreferenceChangeListener
                    scheduleDebouncedSync(app)
                }
                prefs.registerOnSharedPreferenceChangeListener(l)
                listener = l
                syncNow(app)
            }

            FREQ_INTERVAL -> {
                loopJob = scope.launch {
                    while (isActive) {
                        syncNow(app)
                        delay(INTERVAL_MS)
                    }
                }
            }

            else -> Unit // 启动时同步：由 MoteApplication 负责
        }
    }

    fun stop(context: Context) {
        val app = context.applicationContext
        listener?.let {
            try {
                app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(it)
            } catch (_: Exception) {
            }
        }
        listener = null
        loopJob?.cancel()
        loopJob = null
        debounceJob?.cancel()
        debounceJob = null
    }

    fun restart(context: Context) = start(context)

    fun syncNow(context: Context) {
        if (syncing) return
        syncing = true
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!prefs.getBoolean("sync_auto_on_start", true)) return
            val provider = prefs.getString("sync_provider", "cloudbase") ?: "cloudbase"
            if (provider == "cloudbase") syncCloudBase(prefs) else syncWebDav(prefs)
        } catch (e: Exception) {
            Log.w(TAG, "auto sync failed: ${e.message}")
        } finally {
            syncing = false
            applyingRemote = false
        }
    }

    private fun scheduleDebouncedSync(app: Context) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(DEBOUNCE_MS)
            syncNow(app)
        }
    }

    private fun isSyncableKey(prefs: SharedPreferences, key: String): Boolean {
        if (key in IGNORED_KEYS) return false
        if (key.startsWith("sync_cat_")) return false
        if (key in DATA_KEYS) {
            val cat = when (key) {
                "bookmarks_v2" -> MoteCloudSyncManager.CAT_BOOKMARKS
                "history" -> MoteCloudSyncManager.CAT_BROWSE_HISTORY
                "search_history" -> MoteCloudSyncManager.CAT_SEARCH_HISTORY
                "home_quick_urls" -> MoteCloudSyncManager.CAT_QUICK_URLS
                "saved_passwords_v1" -> MoteCloudSyncManager.CAT_PASSWORDS
                else -> null
            }
            return cat != null && prefs.getBoolean("sync_cat_$cat", true)
        }
        return prefs.getBoolean("sync_cat_${MoteCloudSyncManager.CAT_SETTINGS}", true)
    }

    private fun markSynced(prefs: SharedPreferences) {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    private fun syncCloudBase(prefs: SharedPreferences) {
        val accessToken = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_ACCESS_TOKEN, "") ?: ""
        val userId = prefs.getString(MoteCloudSyncManager.KEY_CLOUDBASE_USER_ID, "") ?: ""
        if (accessToken.isBlank() || userId.isBlank()) return

        val remote = MoteCloudSyncManager.pullRemote(prefs).first ?: return
        val lastSyncAt = prefs.getLong(KEY_LAST_SYNC, 0L)
        val localJson = MoteCloudSyncManager.buildLocalData(prefs).toString()

        if (remote.content.isBlank()) {
            pushLocal(prefs, localJson)
            return
        }

        val remoteJson = MoteCloudSyncManager.decrypt(remote.content, prefs)
        if (remoteJson == localJson) {
            markSynced(prefs)
            return
        }

        if (remoteJson != null && remote.updatedAt > lastSyncAt) {
            applyingRemote = true
            try {
                MoteCloudSyncManager.applyRemoteData(prefs, JSONObject(remoteJson))
            } finally {
                applyingRemote = false
            }
            markSynced(prefs)
            return
        }

        pushLocal(prefs, localJson)
    }

    private fun pushLocal(prefs: SharedPreferences, localJson: String) {
        val cipher = MoteCloudSyncManager.encrypt(localJson, prefs)
        val pushed = MoteCloudSyncManager.pushRemote(prefs, cipher).first
        if (pushed != null) markSynced(prefs)
    }

    private fun syncWebDav(prefs: SharedPreferences) {
        val config = MoteCloudSyncManager.getWebDavConfig(prefs) ?: return
        val localJson = MoteCloudSyncManager.buildLocalData(prefs).toString()
        val remoteCipher = MoteCloudSyncManager.downloadFile(config)
        val remoteJson = remoteCipher?.let { MoteCloudSyncManager.decrypt(it, prefs) }
        if (remoteJson == localJson) {
            markSynced(prefs)
            return
        }
        val cipher = MoteCloudSyncManager.encrypt(localJson, prefs)
        if (MoteCloudSyncManager.uploadFile(config, cipher)) markSynced(prefs)
    }
}
