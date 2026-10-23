package com.torachen.mote

import android.app.Application
import android.content.Context
import android.util.Log
import com.aptabase.Aptabase
import com.torachen.mote.ui.MoteAppContextHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Locale

class MoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MoteAppContextHolder.appContext = applicationContext
        installCrashHandler()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_PRIVACY_AGREED, false)) {
            initAptabase(DEFAULT_APP_KEY)
            tryAutoSync()
        }
    }

    fun initAptabase(appKey: String) {
        if (aptabaseInitialized) return
        try {
            Aptabase.instance.initialize(this, appKey)
            aptabaseInitialized = true
        } catch (e: Exception) {
            Log.w(TAG, "Aptabase init failed: ${e.message}")
            return
        }
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        analyticsEnabled = prefs.getBoolean(KEY_UX_IMPROVEMENT, true)
        if (analyticsEnabled) {
            trackEventSafe("app_launched", emptyMap())
        }
        flushPendingCrashReports()
        registerSettingChangeTracker()
        registerAnalyticsToggleWatcher()
        reportBlockerStats()
    }

    private fun tryAutoSync() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sync_auto_on_start", true)) return

        val provider = prefs.getString("sync_provider", "cloudbase") ?: "cloudbase"

        val lastSync = prefs.getLong("sync_last_modified", 0L)
        if (System.currentTimeMillis() - lastSync < 10 * 60 * 1000L) return

        CoroutineScope(Dispatchers.IO).launch {
            if (provider == "cloudbase") {
                val accessToken = prefs.getString("cloudbase_access_token", "") ?: return@launch
                val userId = prefs.getString("cloudbase_user_id", "") ?: return@launch
                if (accessToken.isBlank() || userId.isBlank()) return@launch
                try {
                    val encryptedData = com.torachen.mote.ui.MoteCloudSyncManager
                        .pullRemote(accessToken).first ?: return@launch
                    if (encryptedData.isNotBlank()) {
                        val remoteJson = com.torachen.mote.ui.MoteCloudSyncManager.decrypt(encryptedData, prefs)
                        val localObj = com.torachen.mote.ui.MoteCloudSyncManager.buildLocalData(prefs)
                        val localJson = localObj.toString()
                        if (remoteJson != null && remoteJson != localJson) {
                            // 有冲突，静默跳过，等用户手动处理
                        } else if (remoteJson != null) {
                            prefs.edit().putLong("sync_last_modified", System.currentTimeMillis()).apply()
                        }
                    }
                } catch (_: Exception) {}
            } else {
                val config = com.torachen.mote.ui.MoteCloudSyncManager.getWebDavConfig(prefs) ?: return@launch
                try {
                    val localObj = com.torachen.mote.ui.MoteCloudSyncManager.buildLocalData(prefs)
                    val localJson = localObj.toString()

                    val remoteCipher = com.torachen.mote.ui.MoteCloudSyncManager.downloadFile(config)
                    if (remoteCipher != null) {
                        val remoteJson = com.torachen.mote.ui.MoteCloudSyncManager.decrypt(remoteCipher, prefs)
                        if (remoteJson != null && remoteJson != localJson) return@launch
                    }

                    val encrypted = com.torachen.mote.ui.MoteCloudSyncManager.encrypt(localJson, prefs)
                    val ok = com.torachen.mote.ui.MoteCloudSyncManager.uploadFile(config, encrypted)
                    if (ok) prefs.edit().putLong("sync_last_modified", System.currentTimeMillis()).apply()
                } catch (_: Exception) {}
            }
        }
    }

    companion object {
        private const val TAG = "MoteAnalytics"
        private const val PREFS_NAME = "mote_prefs"
        private const val KEY_PRIVACY_AGREED = "privacy_agreed"
        private const val KEY_UX_IMPROVEMENT = "ux_improvement_enabled"
        private const val CRASH_FILE = "pending_crash.json"
        private const val DEFAULT_APP_KEY = "A-US-1587001410"

        @Volatile private var aptabaseInitialized = false
        @Volatile private var analyticsEnabled = false
        @Volatile private var settingTrackerRegistered = false

        fun trackEventSafe(name: String, properties: Map<String, Any>) {
            try {
                if (!aptabaseInitialized || !analyticsEnabled) return
                val sanitized = properties.mapValues { (_, v) -> if (v is String) v.take(256) else v }
                Aptabase.instance.trackEvent(name, sanitized)
            } catch (e: Exception) { Log.w(TAG, "trackEventSafe failed: ${e.message}") }
        }

        private fun installCrashHandler() {
            val ctx = MoteAppContextHolder.appContext ?: return
            val default = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    val record = JSONObject().apply {
                        put("type", throwable.javaClass.name)
                        put("message", (throwable.message ?: "").take(512))
                        put("thread", thread.name)
                        put("time", System.currentTimeMillis())
                    }
                    File(ctx.filesDir, CRASH_FILE).writeText(record.toString())
                } catch (_: Exception) {}
                default?.uncaughtException(thread, throwable)
            }
        }

        private fun flushPendingCrashReports() {
            val ctx = MoteAppContextHolder.appContext ?: return
            try {
                val f = File(ctx.filesDir, CRASH_FILE)
                if (!f.exists()) return
                val raw = f.readText()
                f.delete()
                if (raw.isBlank()) return
                val obj = JSONObject(raw)
                trackEventSafe("crash", mapOf(
                    "exception_type" to obj.optString("type", ""),
                    "message" to obj.optString("message", ""),
                    "thread" to obj.optString("thread", "")
                ))
            } catch (_: Exception) {}
        }

        private fun registerSettingChangeTracker() {
            if (settingTrackerRegistered) return
            val ctx = MoteAppContextHolder.appContext ?: return
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ignoredKeys = setOf(
                "saved_passwords_v1", "bookmarks_v2", "history", "search_history", "session_snapshot",
                "about_last_check_time", "web_saved_zooms_v2", "home_quick_urls", "web_site_protection",
                "custom_search_engines", "active_custom_engine_id", "mote_download_tasks_v1",
                "privacy_agreed", "ux_improvement_enabled", "app_icon_id", "control_color_suggest_dismissed",
                "last_session_url", "sync_webdav_url", "sync_webdav_user", "sync_webdav_pass",
                "sync_master_pass", "sync_last_modified", "sync_provider",
                "cloudbase_access_token", "cloudbase_refresh_token", "cloudbase_user_id",
                "sync_e2ee_enabled", "sync_fallback_pass"
            )
            prefs.registerOnSharedPreferenceChangeListener { _, key ->
                if (key.isNullOrBlank() || key in ignoredKeys || key.startsWith("blocker_daily_") || key.startsWith("sync_cat_")) return@registerOnSharedPreferenceChangeListener
                val value = prefs.all[key] ?: return@registerOnSharedPreferenceChangeListener
                trackEventSafe("setting_changed", mapOf("key" to key, "value" to value.toString().take(256)))
            }
            settingTrackerRegistered = true
        }

        fun setAnalyticsEnabled(enabled: Boolean) { analyticsEnabled = enabled }

        private fun registerAnalyticsToggleWatcher() {
            val ctx = MoteAppContextHolder.appContext ?: return
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_UX_IMPROVEMENT) analyticsEnabled = prefs.getBoolean(KEY_UX_IMPROVEMENT, true)
            }
        }

        private fun reportBlockerStats() {
            try {
                val ctx = MoteAppContextHolder.appContext ?: return
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val yesterday = java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(java.util.Date(System.currentTimeMillis() - 86400000L))
                val adsKey = "blocker_daily_${yesterday}_ads"
                val trackersKey = "blocker_daily_${yesterday}_trackers"
                val ads = prefs.getInt(adsKey, 0)
                val trackers = prefs.getInt(trackersKey, 0)
                if (ads > 0 || trackers > 0) {
                    trackEventSafe("blocker_stats_daily", mapOf("ads_blocked" to ads, "trackers_blocked" to trackers))
                    prefs.edit().remove(adsKey).remove(trackersKey).apply()
                }
                val cutoff = System.currentTimeMillis() - 7L * 86400000L
                prefs.all.keys.filter { it.startsWith("blocker_daily_") }.forEach { k ->
                    val dateStr = k.removePrefix("blocker_daily_").substringBefore("_")
                    try {
                        val d = java.text.SimpleDateFormat("yyyyMMdd", Locale.US).parse(dateStr)
                        if (d != null && d.time < cutoff) prefs.edit().remove(k).apply()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }
}