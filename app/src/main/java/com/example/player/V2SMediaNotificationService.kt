package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class V2SMediaNotificationService : Service() {

    interface NotificationActionListener {
        fun onPlay()
        fun onPause()
        fun onNext()
        fun onPrevious()
        fun onSeekBackward()
        fun onSeekForward()
        fun onStop()
        fun onVolumeUp()
        fun onVolumeDown()
    }

    inner class LocalBinder : Binder() {
        fun getService(): V2SMediaNotificationService = this@V2SMediaNotificationService
    }

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private var foregroundStarted = false
    private var isNotificationExpanded = true
    private var usageStartTimeElapsed: Long = 0L

    private data class CachedNotificationData(
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val isAudio: Boolean,
        val artworkUri: Uri?,
        val isVideoAudio: Boolean,
        val positionMs: Long,
        val durationMs: Long
    )
    private var lastCachedData: CachedNotificationData? = null

    override fun onCreate() {
        super.onCreate()

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (usageStartTimeElapsed == 0L) {
            usageStartTimeElapsed = android.os.SystemClock.elapsedRealtime()
        }

        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_PLAY -> {
                actionListener?.onPlay() ?: run {
                    VyloPlayerManager.activeManager?.play()
                }
            }

            ACTION_PAUSE -> {
                actionListener?.onPause() ?: run {
                    VyloPlayerManager.activeManager?.pause()
                }
            }

            ACTION_PREV -> {
                actionListener?.onPrevious() ?: run {
                    VyloPlayerManager.activeManager?.onPlayPreviousMedia?.invoke()
                        ?: VyloPlayerManager.activeManager?.previousTrack()
                }
            }

            ACTION_NEXT -> {
                actionListener?.onNext() ?: run {
                    VyloPlayerManager.activeManager?.onPlayNextMedia?.invoke()
                        ?: VyloPlayerManager.activeManager?.nextTrack()
                }
            }

            ACTION_SEEK_BACKWARD -> {
                actionListener?.onSeekBackward() ?: run {
                    VyloPlayerManager.activeManager?.seekRelative(-10_000L)
                }
            }

            ACTION_SEEK_FORWARD -> {
                actionListener?.onSeekForward() ?: run {
                    VyloPlayerManager.activeManager?.seekRelative(10_000L)
                }
            }

            ACTION_VOLUME_UP -> {
                actionListener?.onVolumeUp() ?: run {
                    VyloPlayerManager.activeManager?.increaseVolume()
                }
            }

            ACTION_VOLUME_DOWN -> {
                actionListener?.onVolumeDown() ?: run {
                    VyloPlayerManager.activeManager?.decreaseVolume()
                }
            }

            ACTION_TOGGLE_EXPAND -> {
                isNotificationExpanded = !isNotificationExpanded
                lastCachedData?.let { data ->
                    showNotification(
                        title = data.title,
                        artist = data.artist,
                        isPlaying = data.isPlaying,
                        isAudio = data.isAudio,
                        artworkUri = data.artworkUri,
                        isVideoAudio = data.isVideoAudio,
                        positionMs = data.positionMs,
                        durationMs = data.durationMs
                    )
                }
            }

            ACTION_CLOSE_ALL,
            ACTION_STOP,
            ACTION_DISMISS -> {
                actionListener?.onStop() ?: run {
                    VyloPlayerManager.activeManager?.stopMedia()
                }

                stopForeground(
                    STOP_FOREGROUND_REMOVE
                )
                foregroundStarted = false
                notificationManager.cancel(NOTIFICATION_ID)

                if (intent.action == ACTION_CLOSE_ALL) {
                    MainActivity.closeApp()
                }

                stopSelf()
            }

            ACTION_SHOW -> {

                val title =
                    intent.getStringExtra(EXTRA_TITLE) ?: "V2S"

                val artist =
                    intent.getStringExtra(EXTRA_ARTIST) ?: ""

                val isPlaying =
                    intent.getBooleanExtra(EXTRA_IS_PLAYING, false)

                val isAudio =
                    intent.getBooleanExtra(EXTRA_IS_AUDIO, false)

                val artworkUri =
                    intent.getStringExtra(EXTRA_ARTWORK_URI)?.let(Uri::parse)

                val isVideoAudio =
                    intent.getBooleanExtra(EXTRA_IS_VIDEO_AUDIO, false)

                val positionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)

                if (usageStartTimeElapsed == 0L) {
                    usageStartTimeElapsed = android.os.SystemClock.elapsedRealtime()
                }
                showNotification(
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    isAudio = isAudio,
                    artworkUri = artworkUri,
                    isVideoAudio = isVideoAudio,
                    positionMs = positionMs,
                    durationMs = durationMs
                )
            }

            ACTION_HIDE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                foregroundStarted = false
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val manager = VyloPlayerManager.activeManager
        val state = manager?.playerState?.value
        if (state != null && state.isBackgroundPlayEnabled && state.isPlaying) {
            lastCachedData?.let { data ->
                showNotification(
                    title = data.title,
                    artist = data.artist,
                    isPlaying = true,
                    isAudio = data.isAudio,
                    artworkUri = data.artworkUri,
                    isVideoAudio = data.isVideoAudio,
                    positionMs = state.currentPositionMs,
                    durationMs = state.durationMs
                )
            }
            super.onTaskRemoved(rootIntent)
            return
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        foregroundStarted = false
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "V2S Media Player",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    "V2S audio and video background playback controls"

                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                enableLights(false)

                lockscreenVisibility =
                    Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        artist: String,
        isPlaying: Boolean,
        isAudio: Boolean,
        artworkUri: Uri?,
        isVideoAudio: Boolean,
        positionMs: Long,
        durationMs: Long
    ) {
        lastCachedData = CachedNotificationData(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            isAudio = isAudio,
            artworkUri = artworkUri,
            isVideoAudio = isVideoAudio,
            positionMs = positionMs,
            durationMs = durationMs
        )

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cleanArtist = artist.replace("Video Audio", "", ignoreCase = true).trim()
        val subtitle = when {
            cleanArtist.isNotBlank() -> cleanArtist
            isAudio -> "Audio"
            else -> "Background Video"
        }
        val playbackTime = "${formatPlaybackTime(positionMs)} / ${formatPlaybackTime(durationMs)}"

        // Top header displays user active duration instead of current track second
        val elapsedUsageMs = if (usageStartTimeElapsed > 0L) {
            (android.os.SystemClock.elapsedRealtime() - usageStartTimeElapsed).coerceAtLeast(0L)
        } else 0L
        val usageDurationText = formatUsageDuration(elapsedUsageMs)

        val progress = if (durationMs > 0L) {
            ((positionMs.coerceIn(0L, durationMs).toDouble() / durationMs) * 1000.0).toInt()
        } else 0

        val compact = RemoteViews(packageName, R.layout.notification_v2s_compact).apply {
            setTextViewText(R.id.notification_header, "V2S  •  $usageDurationText")
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_subtitle, subtitle)
            setImageViewResource(
                R.id.notification_play_pause,
                if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
            )
            setOnClickPendingIntent(R.id.notification_prev, buildActionIntent(ACTION_PREV, 1))
            setOnClickPendingIntent(R.id.notification_seek_back, buildActionIntent(ACTION_SEEK_BACKWARD, 2))
            setOnClickPendingIntent(R.id.notification_play_pause, buildActionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY, 3))
            setOnClickPendingIntent(R.id.notification_seek_forward, buildActionIntent(ACTION_SEEK_FORWARD, 4))
            setOnClickPendingIntent(R.id.notification_next, buildActionIntent(ACTION_NEXT, 5))
            // Down arrow to toggle/expand full controls
            setOnClickPendingIntent(R.id.notification_expand, buildActionIntent(ACTION_TOGGLE_EXPAND, 6))
            // Cross button to close notification AND application
            setOnClickPendingIntent(R.id.notification_close, buildActionIntent(ACTION_CLOSE_ALL, 7))
        }

        val expanded = RemoteViews(packageName, R.layout.notification_v2s_expanded).apply {
            setTextViewText(R.id.notification_header, "V2S  •  $usageDurationText")
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_subtitle, subtitle)
            setTextViewText(R.id.notification_time, playbackTime)
            setProgressBar(R.id.notification_seek, 1000, progress, false)
            setImageViewResource(
                R.id.notification_play_pause,
                if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
            )
            setOnClickPendingIntent(R.id.notification_prev, buildActionIntent(ACTION_PREV, 9))
            setOnClickPendingIntent(R.id.notification_seek_back, buildActionIntent(ACTION_SEEK_BACKWARD, 10))
            setOnClickPendingIntent(R.id.notification_play_pause, buildActionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY, 12))
            setOnClickPendingIntent(R.id.notification_seek_forward, buildActionIntent(ACTION_SEEK_FORWARD, 14))
            setOnClickPendingIntent(R.id.notification_next, buildActionIntent(ACTION_NEXT, 15))
            setOnClickPendingIntent(R.id.notification_volume_down, buildActionIntent(ACTION_VOLUME_DOWN, 17))
            setOnClickPendingIntent(R.id.notification_volume_up, buildActionIntent(ACTION_VOLUME_UP, 18))
            // Up arrow to collapse/hide extra controls
            setOnClickPendingIntent(R.id.notification_expand, buildActionIntent(ACTION_TOGGLE_EXPAND, 19))
            // Cross button to close notification AND application
            setOnClickPendingIntent(R.id.notification_close, buildActionIntent(ACTION_CLOSE_ALL, 20))
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_media)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(openAppIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(isPlaying)
            .setAutoCancel(false)
            .setCustomContentView(compact)
            .setCustomBigContentView(if (isNotificationExpanded) expanded else compact)
            .setCustomHeadsUpContentView(compact)

        val notification = builder.build()
        if (!foregroundStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            foregroundStarted = true
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun formatPlaybackTime(milliseconds: Long): String {
        if (milliseconds <= 0L) return "00:00"
        val totalSeconds = milliseconds / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    private fun formatUsageDuration(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return when {
            hours > 0L -> "Used: ${hours}h ${minutes}m"
            minutes > 0L -> "Used: ${minutes}m"
            else -> "Used: ${seconds}s"
        }
    }

    private fun buildActionIntent(
        action: String,
        requestCode: Int
    ): PendingIntent {

        return PendingIntent.getService(
            this,
            requestCode,
            Intent(
                this,
                V2SMediaNotificationService::class.java
            ).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {

        const val CHANNEL_ID =
            "v2s_media_player_top"

        const val NOTIFICATION_ID =
            1001

        const val ACTION_SHOW =
            "com.example.v2s.ACTION_SHOW"

        const val ACTION_HIDE =
            "com.example.v2s.ACTION_HIDE"

        const val ACTION_PLAY =
            "com.example.v2s.ACTION_PLAY"

        const val ACTION_PAUSE =
            "com.example.v2s.ACTION_PAUSE"

        const val ACTION_PREV =
            "com.example.v2s.ACTION_PREV"

        const val ACTION_NEXT =
            "com.example.v2s.ACTION_NEXT"

        const val ACTION_SEEK_BACKWARD =
            "com.example.v2s.ACTION_SEEK_BACKWARD"

        const val ACTION_SEEK_FORWARD =
            "com.example.v2s.ACTION_SEEK_FORWARD"

        const val ACTION_STOP =
            "com.example.v2s.ACTION_STOP"

        const val ACTION_DISMISS =
            "com.example.v2s.ACTION_DISMISS"

        const val ACTION_CLOSE_ALL =
            "com.example.v2s.ACTION_CLOSE_ALL"

        const val ACTION_TOGGLE_EXPAND =
            "com.example.v2s.ACTION_TOGGLE_EXPAND"

        const val ACTION_VOLUME_UP =
            "com.example.v2s.ACTION_VOLUME_UP"

        const val ACTION_VOLUME_DOWN =
            "com.example.v2s.ACTION_VOLUME_DOWN"

        const val EXTRA_TITLE =
            "title"

        const val EXTRA_ARTIST =
            "artist"

        const val EXTRA_IS_PLAYING =
            "isPlaying"

        const val EXTRA_IS_AUDIO =
            "isAudio"

        const val EXTRA_ARTWORK_URI =
            "artworkUri"

        const val EXTRA_IS_VIDEO_AUDIO =
            "isVideoAudio"

        const val EXTRA_POSITION_MS = "positionMs"
        const val EXTRA_DURATION_MS = "durationMs"
        var actionListener:
                NotificationActionListener? = null

        fun updateNotification(
            context: Context,
            title: String,
            artist: String = "",
            isPlaying: Boolean,
            isAudio: Boolean,
            artworkUri: Uri? = null,
            isVideoAudio: Boolean = false,
            positionMs: Long = 0L,
            durationMs: Long = 0L
        ) {

            val intent =
                Intent(
                    context,
                    V2SMediaNotificationService::class.java
                ).apply {

                    action = ACTION_SHOW

                    putExtra(
                        EXTRA_TITLE,
                        title
                    )

                    putExtra(
                        EXTRA_ARTIST,
                        artist
                    )

                    putExtra(
                        EXTRA_IS_PLAYING,
                        isPlaying
                    )

                    putExtra(
                        EXTRA_IS_AUDIO,
                        isAudio
                    )

                    putExtra(
                        EXTRA_ARTWORK_URI,
                        artworkUri?.toString()
                    )

                    putExtra(
                        EXTRA_IS_VIDEO_AUDIO,
                        isVideoAudio
                    )

                    putExtra(EXTRA_POSITION_MS, positionMs)
                    putExtra(EXTRA_DURATION_MS, durationMs)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(intent)

            } else {

                context.startService(intent)
            }
        }

        fun hideNotification(context: Context) {
            // Do not call startService() here: on Android O+ that can throw when
            // the app is in the background. Stopping the service is sufficient
            // to remove its foreground notification.
            context.stopService(
                Intent(context, V2SMediaNotificationService::class.java)
            )
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }
    }
}