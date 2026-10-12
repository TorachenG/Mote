package com.torachen.mote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import android.widget.Toast
import com.torachen.mote.R
import com.torachen.mote.ui.NativePlayerActivity
import com.torachen.mote.ui.PlaybackSessionHolder
import com.torachen.mote.ui.addBookmarkToRoot
import com.torachen.mote.ui.isVideoBookmarked
import com.torachen.mote.ui.removeVideoBookmark

internal object PlaybackCoverHolder {
    @Volatile var coverBytes: ByteArray? = null
    @Volatile var coverVersion: Int = 0
}

class PlaybackService : Service() {

    companion object {
        const val ACTION_START = "com.torachen.mote.player.START"
        const val ACTION_PLAY = "com.torachen.mote.player.PLAY"
        const val ACTION_PAUSE = "com.torachen.mote.player.PAUSE"
        const val ACTION_FORWARD = "com.torachen.mote.player.FORWARD"
        const val ACTION_BACKWARD = "com.torachen.mote.player.BACKWARD"
        const val ACTION_STOP = "com.torachen.mote.player.STOP"
        const val ACTION_BOOKMARK = "com.torachen.mote.player.BOOKMARK"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_POSITION = "extra_position"
        const val EXTRA_DURATION = "extra_duration"

        private const val CHANNEL_ID = "mote_playback"
        private const val NOTIFICATION_ID = 9001
        private const val SEEK_STEP_MS = 15_000L

        private const val CUSTOM_ACTION_BOOKMARK = "CUSTOM_ACTION_BOOKMARK"
        private const val CUSTOM_ACTION_BACKWARD = "CUSTOM_ACTION_BACKWARD"
        private const val CUSTOM_ACTION_FORWARD = "CUSTOM_ACTION_FORWARD"
        private const val CUSTOM_ACTION_STOP = "CUSTOM_ACTION_STOP"
    }

    private var playerListener: Player.Listener? = null
    private var mediaSession: MediaSessionCompat? = null
    private var currentTitle: String = "视频播放"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        setupMediaSession()
    }

    private fun setupMediaSession() {
        val session = MediaSessionCompat(this, "MotePlayback").apply {
            setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    PlaybackSessionHolder.player?.play()
                    refreshNotification()
                }

                override fun onPause() {
                    PlaybackSessionHolder.player?.pause()
                    refreshNotification()
                }

                override fun onStop() {
                    PlaybackSessionHolder.player?.pause()
                    stopForegroundCompat()
                    stopSelf()
                }

                override fun onSeekTo(pos: Long) {
                    PlaybackSessionHolder.player?.seekTo(pos)
                    refreshNotification()
                }

                override fun onFastForward() {
                    val p = PlaybackSessionHolder.player ?: return
                    val target = (p.currentPosition + SEEK_STEP_MS)
                        .coerceAtMost(p.duration.coerceAtLeast(0L))
                    p.seekTo(target)
                    refreshNotification()
                }

                override fun onRewind() {
                    val p = PlaybackSessionHolder.player ?: return
                    val target = (p.currentPosition - SEEK_STEP_MS).coerceAtLeast(0L)
                    p.seekTo(target)
                    refreshNotification()
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    super.onCustomAction(action, extras)
                    when (action) {
                        CUSTOM_ACTION_BOOKMARK -> {
                            val url = PlaybackSessionHolder.currentUrl
                            val title = PlaybackSessionHolder.currentTitle
                            if (url.isNotBlank()) {
                                try {
                                    val isBookmarked = isVideoBookmarked(
                                        this@PlaybackService, url
                                    )
                                    if (isBookmarked) {
                                        removeVideoBookmark(this@PlaybackService, url)
                                        Toast.makeText(
                                            this@PlaybackService, "已取消书签",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        addBookmarkToRoot(
                                            this@PlaybackService,
                                            title.ifBlank { url },
                                            url
                                        )
                                        Toast.makeText(
                                            this@PlaybackService, "已添加到书签",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    refreshNotification()
                                } catch (_: Exception) {}
                            }
                        }

                        CUSTOM_ACTION_BACKWARD -> {
                            val p = PlaybackSessionHolder.player ?: return
                            val target = (p.currentPosition - SEEK_STEP_MS)
                                .coerceAtLeast(0L)
                            p.seekTo(target)
                            refreshNotification()
                        }

                        CUSTOM_ACTION_FORWARD -> {
                            val p = PlaybackSessionHolder.player ?: return
                            val target = (p.currentPosition + SEEK_STEP_MS)
                                .coerceAtMost(p.duration.coerceAtLeast(0L))
                            p.seekTo(target)
                            refreshNotification()
                        }

                        CUSTOM_ACTION_STOP -> {
                            PlaybackSessionHolder.player?.pause()
                            stopForegroundCompat()
                            stopSelf()
                        }
                    }
                }
            })
            isActive = true
        }
        mediaSession = session
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val player = PlaybackSessionHolder.player

        when (action) {
            ACTION_PLAY -> {
                player?.play()
                refreshNotification()
            }
            ACTION_PAUSE -> {
                player?.pause()
                refreshNotification()
            }
            ACTION_FORWARD -> {
                val p = player ?: return START_STICKY
                val target = (p.currentPosition + SEEK_STEP_MS)
                    .coerceAtMost(p.duration.coerceAtLeast(0L))
                p.seekTo(target)
                refreshNotification()
            }
            ACTION_BACKWARD -> {
                val p = player ?: return START_STICKY
                val target = (p.currentPosition - SEEK_STEP_MS).coerceAtLeast(0L)
                p.seekTo(target)
                refreshNotification()
            }
            ACTION_BOOKMARK -> {
                val url = PlaybackSessionHolder.currentUrl
                val title = PlaybackSessionHolder.currentTitle
                if (url.isNotBlank()) {
                    try {
                        val isBookmarked = isVideoBookmarked(this, url)
                        if (isBookmarked) {
                            removeVideoBookmark(this, url)
                            Toast.makeText(this, "已取消书签", Toast.LENGTH_SHORT).show()
                        } else {
                            addBookmarkToRoot(this, title.ifBlank { url }, url)
                            Toast.makeText(this, "已添加到书签", Toast.LENGTH_SHORT).show()
                        }
                    } catch (_: Exception) {}
                }
                return START_STICKY
            }
            ACTION_STOP -> {
                player?.pause()
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val title = intent?.getStringExtra(EXTRA_TITLE) ?: currentTitle
        currentTitle = title
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true)
            ?: (player?.isPlaying == true)
        val position = intent?.getLongExtra(EXTRA_POSITION, -1L)?.takeIf { it >= 0 }
            ?: (player?.currentPosition ?: 0L)
        val duration = intent?.getLongExtra(EXTRA_DURATION, -1L)?.takeIf { it >= 0 }
            ?: (player?.duration ?: 0L)

        showNotification(title, isPlaying, position, duration)

        if (player != null && playerListener == null) {
            val l = object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    refreshNotification()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    refreshNotification()
                }
            }
            playerListener = l
            player.addListener(l)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        playerListener?.let { PlaybackSessionHolder.player?.removeListener(it) }
        playerListener = null
        try { mediaSession?.release() } catch (_: Exception) {}
        mediaSession = null
        super.onDestroy()
    }

    private fun refreshNotification() {
        val player = PlaybackSessionHolder.player ?: return
        showNotification(
            title = currentTitle,
            isPlaying = player.isPlaying,
            position = player.currentPosition,
            duration = player.duration.coerceAtLeast(0L),
        )
    }

    private fun showNotification(
        title: String?,
        isPlaying: Boolean,
        position: Long,
        duration: Long,
    ) {
        val session = mediaSession

        // ---- 内容点击：打开视频播放器 ----
        val currentUrl = PlaybackSessionHolder.currentUrl
        val currentTitle = PlaybackSessionHolder.currentTitle
        val contentPi: PendingIntent? = if (currentUrl.isNotBlank()) {
            val playerIntent = Intent(this, NativePlayerActivity::class.java).apply {
                putExtra(NativePlayerActivity.EXTRA_VIDEO_URL, currentUrl)
                putExtra(NativePlayerActivity.EXTRA_VIDEO_TITLE, currentTitle)
                putExtra(NativePlayerActivity.EXTRA_START_POSITION, position)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            PendingIntent.getActivity(this, 0, playerIntent, pendingFlags())
        } else {
            val openIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            openIntent?.let { PendingIntent.getActivity(this, 0, it, pendingFlags()) }
        }

        // ---- 更新 MediaSession 元数据 + 播放状态 ----
        if (session != null) {
            val metadataBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title ?: "视频播放")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mote")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

            // ★ 恢复最初工作的封面写法
            val coverBytes = PlaybackCoverHolder.coverBytes
            if (coverBytes != null) {
                try {
                    val bmp = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
                    if (bmp != null) {
                        metadataBuilder.putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmp,
                        )
                        metadataBuilder.putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ART, bmp,
                        )
                    }
                } catch (_: Exception) {}
            }
            session.setMetadata(metadataBuilder.build())

            // 书签图标动态切换
            val isBookmarked = currentUrl.isNotBlank() &&
                    try { isVideoBookmarked(this, currentUrl) } catch (_: Exception) { false }
            val bookmarkIcon = if (isBookmarked) {
                R.drawable.ic_media_favorite_cancel
            } else {
                R.drawable.ic_media_favorite_add
            }
            val bookmarkTitle = if (isBookmarked) "取消书签" else "添加书签"

            // ★ 关键：用 FAST_FORWARD/REWIND 声明媒体会话能力（不加 SKIP）
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_FAST_FORWARD or
                            PlaybackStateCompat.ACTION_REWIND
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    position,
                    1.0f,
                )

            // CustomAction 顺序：Backward(0), Forward(1), Bookmark(2), Stop(3)
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    CUSTOM_ACTION_BACKWARD, "后退 15 秒", R.drawable.ic_media_replay_15s
                ).build()
            )
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    CUSTOM_ACTION_FORWARD, "前进 15 秒", R.drawable.ic_media_forward_15s
                ).build()
            )
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    CUSTOM_ACTION_BOOKMARK, bookmarkTitle, bookmarkIcon
                ).build()
            )
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    CUSTOM_ACTION_STOP, "关闭",
                    android.R.drawable.ic_menu_close_clear_cancel
                ).build()
            )

            session.setPlaybackState(stateBuilder.build())
        }

        // ---- 构建通知 ----
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle(title ?: "视频播放")
            .setContentText(
                if (duration > 0) "${fmtTimeLong(position)} / ${fmtTimeLong(duration)}"
                else if (isPlaying) "正在播放" else "已暂停"
            )
            .setContentIntent(contentPi)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // ★ 恢复最初工作的封面写法：setLargeIcon
        val coverBytes2 = PlaybackCoverHolder.coverBytes
        if (coverBytes2 != null) {
            try {
                val bmp = BitmapFactory.decodeByteArray(
                    coverBytes2, 0, coverBytes2.size
                )
                if (bmp != null) builder.setLargeIcon(bmp)
            } catch (_: Exception) {}
        }

        if (session != null) {
            builder.setStyle(
                MediaStyle().setMediaSession(session.sessionToken)
            )
        }

        val notification: Notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildActionPi(action: String, requestCode: Int): PendingIntent {
        val i = Intent(this, PlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, requestCode, i, pendingFlags())
    }

    private fun pendingFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "后台播放",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply { setShowBadge(false) }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    private fun fmtTimeLong(ms: Long): String {
        if (ms <= 0) return "00:00"
        val t = ms / 1000
        val h = t / 3600
        val m = (t % 3600) / 60
        val s = t % 60
        return if (h > 0)
            String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s)
        else
            String.format(java.util.Locale.US, "%02d:%02d", m, s)
    }
}