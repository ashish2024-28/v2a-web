@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.AudioDeviceInfo
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.avi.AviExtractor
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.extractor.amr.AmrExtractor
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp3.Mp3Extractor
import androidx.media3.extractor.ts.AdtsExtractor
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.session.MediaSession
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

enum class DecoderMode(val label: String, val description: String) {
    HW("HW", "Hardware Decoder (Fast GPU)"),
    SW("SW", "Software Decoder (Universal Formats)")
}

enum class VideoScaleMode(val label: String) {
    FIT("Fit"),
    ORIGINAL("100%"),
    CROP("16:9 Crop"),
    FILL("Stretch"),
    ULTRAWIDE_18_9("18:9"),
    CINEMATIC_21_9("21:9")
}

enum class EqualizerPreset(val label: String) {
    FLAT("Flat / Normal"),
    BASS_BOOST("Bass Boost"),
    TREBLE_BOOST("Treble Boost"),
    ROCK("Rock"),
    POP("Pop"),
    VOCAL("Vocal Boost"),
    JAZZ("Jazz")
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val currentVideo: VideoItem? = null,
    val currentSong: SongItem? = null,
    val isAudioOnly: Boolean = false,
    val isVideoAudio: Boolean = false,
    val notificationArtworkUri: android.net.Uri? = null,
    val volumePercent: Int = 100,
    val brightnessPercent: Int = 80,
    val scaleMode: VideoScaleMode = VideoScaleMode.FIT,
    val equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isShuffle: Boolean = false,
    val isSubtitlesEnabled: Boolean = false,
    val isBackgroundPlayEnabled: Boolean = true,
    val isMultiplePlaybackEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val sleepTimerMinutesRemaining: Int? = null,
    val error: String? = null,
    val decoderMode: DecoderMode = DecoderMode.HW,
    val availableVideoTracks: List<String> = emptyList(),
    val currentVideoTrackIndex: Int = 0,
    val isSmartMuxerDetected: Boolean = false,
    val availableAudioTracks: List<String> = emptyList(),
    val currentAudioTrackIndex: Int = 0,
    val availableSubtitleTracks: List<String> = emptyList(),
    val currentSubtitleTrackIndex: Int = -1,
    val subtitleDelayMs: Long = 0L,
    val subtitleFontSizeSp: Int = 18,
    val repeatPointA: Long? = null,
    val repeatPointB: Long? = null,
    val isAbRepeatActive: Boolean = false
)

class VyloPlayerManager(
    private val context: Context,
    private var onProgressUpdate: (videoId: Long, positionMs: Long) -> Unit,
    private var onPlaybackStarted: (videoId: Long, positionMs: Long) -> Unit = { _, _ -> },
    private var onSongProgressUpdate: (songId: Long, positionMs: Long) -> Unit = { _, _ -> },
    private var onSongPlaybackStarted: (songId: Long, positionMs: Long) -> Unit = { _, _ -> }
) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("v2s_player_preferences", Context.MODE_PRIVATE)

    private val savedPlaybackSpeed = preferences.getFloat("playback_speed", 1.0f).coerceIn(0.25f, 4.0f)
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val savedVolumePercent = readSystemVolumePercent()
    private val savedBrightnessPercent = preferences.getInt("brightness_percent", 80).coerceIn(0, 100)
    private val savedScaleMode = runCatching {
        VideoScaleMode.valueOf(preferences.getString("scale_mode", VideoScaleMode.FIT.name) ?: VideoScaleMode.FIT.name)
    }.getOrDefault(VideoScaleMode.FIT)
    private val savedEqualizerPreset = runCatching {
        EqualizerPreset.valueOf(preferences.getString("equalizer_preset", EqualizerPreset.FLAT.name) ?: EqualizerPreset.FLAT.name)
    }.getOrDefault(EqualizerPreset.FLAT)
    private val savedRepeatMode = preferences.getInt("repeat_mode", Player.REPEAT_MODE_OFF)
    private val savedShuffle = preferences.getBoolean("shuffle", true)
    private val savedBackgroundPlay = preferences.getBoolean("background_play", true)
    private val savedMultiplePlayback = preferences.getBoolean("multiple_playback", false)
    private val savedDecoderMode = runCatching {
        DecoderMode.valueOf(preferences.getString("decoder_mode", DecoderMode.HW.name) ?: DecoderMode.HW.name)
    }.getOrDefault(DecoderMode.HW)
    private val savedSubtitleFontSize = preferences.getInt("subtitle_font_size", 18).coerceIn(12, 32)

    private var exoPlayer: ExoPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var mediaSession: MediaSession? = null
    var onPlayNextMedia: (() -> Unit)? = null
    var onPlayPreviousMedia: (() -> Unit)? = null
    var onMediaPlaybackCompleted: (() -> Unit)? = null
    private val secondaryPlayers = mutableMapOf<String, ExoPlayer>()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var lastPersistedPositionMs: Long = 0L
    private var playbackStartRecorded = false
    private var lastNotificationUpdateAtMs = 0L
    private var lastNonZeroVolumePercent = savedVolumePercent.coerceAtLeast(1)
    private var codecRecoveryAttempts = 0

    // Global V2S earbud controller. It lives with the MediaSession rather than
    // MainActivity, so Bluetooth/media-button controls continue to work when the
    // player screen is not visible. It only consumes custom gestures while a
    // headset/earbud output is actually connected.
    private var earbudClickCount = 0
    private var lastEarbudClickAtMs = 0L
    private val earbudHandler = Handler(Looper.getMainLooper())
    private val earbudClickRunnable = Runnable { dispatchCustomEarbudClicks() }


    private val _playerState = MutableStateFlow(
        PlayerUiState(
            playbackSpeed = savedPlaybackSpeed,
            volumePercent = savedVolumePercent,
            brightnessPercent = savedBrightnessPercent,
            scaleMode = savedScaleMode,
            equalizerPreset = savedEqualizerPreset,
            repeatMode = savedRepeatMode,
            isShuffle = savedShuffle,
            isBackgroundPlayEnabled = savedBackgroundPlay,
            isMultiplePlaybackEnabled = savedMultiplePlayback,
            decoderMode = savedDecoderMode,
            subtitleFontSizeSp = savedSubtitleFontSize
        )
    )
    val playerState = _playerState.asStateFlow()

    init {
        activeManager = this
    }

    fun attachCallbacks(
        onProgressUpdate: (videoId: Long, positionMs: Long) -> Unit,
        onPlaybackStarted: (videoId: Long, positionMs: Long) -> Unit,
        onSongProgressUpdate: (songId: Long, positionMs: Long) -> Unit,
        onSongPlaybackStarted: (songId: Long, positionMs: Long) -> Unit
    ) {
        this.onProgressUpdate = onProgressUpdate
        this.onPlaybackStarted = onPlaybackStarted
        this.onSongProgressUpdate = onSongProgressUpdate
        this.onSongPlaybackStarted = onSongPlaybackStarted
    }

    fun clearPlaybackError() {
        if (_playerState.value.error != null) {
            _playerState.value = _playerState.value.copy(error = null)
        }
    }

    // ── Notification helper ────────────────────────────────────────────────────

    private fun postNotification(
        isPlaying: Boolean,
        force: Boolean = false
    ) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (!force && now - lastNotificationUpdateAtMs < 1_000L) return
        lastNotificationUpdateAtMs = now
        val state = _playerState.value
        if (!state.isBackgroundPlayEnabled) {
            V2SMediaNotificationService.hideNotification(context)
            return
        }

        val song = state.currentSong
        val video = state.currentVideo
        if (song == null && video == null) return

        // Background media playback policy:
        // Allow songs, video-as-audio, and regular videos to hold a foreground
        // media notification when Background Play is enabled.
        val isAudioPlay = song != null || state.isAudioOnly || state.isVideoAudio

        val title = song?.title ?: video?.title ?: "V2S"
        val artist = when {
            state.isVideoAudio -> "Video Audio"
            song != null -> song.artist
            else -> video?.parentFolderName?.takeIf { it.isNotBlank() } ?: "Background Video"
        }
        val artworkUri = when {
            state.isVideoAudio -> state.notificationArtworkUri
            song != null -> song.albumArtUri
            video != null -> video.uri
            else -> null
        }

        V2SMediaNotificationService.updateNotification(
            context = context,
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            isAudio = isAudioPlay,
            artworkUri = artworkUri,
            isVideoAudio = state.isVideoAudio,
            positionMs = state.currentPositionMs,
            durationMs = state.durationMs
        )
    }

    private fun hideNotification() {
        V2SMediaNotificationService.hideNotification(context)
    }

    // ── ExoPlayer ─────────────────────────────────────────────────────────────

    @OptIn(UnstableApi::class)
    /**
     * Handles a Bluetooth/wired headset media-button Intent globally through
     * MediaSession. Standard media commands are left alone unless custom V2S
     * controls are enabled and an audio output device is connected.
     */
    private fun handleGlobalEarbudMediaButton(intent: Intent): Boolean {
        val settings = EarphonePreferences.load(context)
        if (!settings.isCustomControlsEnabled || !isHeadsetOrEarbudConnected()) return false

        val event = intent.getParcelableExtra<android.view.KeyEvent>(Intent.EXTRA_KEY_EVENT)
            ?: return false
        if (event.action != android.view.KeyEvent.ACTION_DOWN) return true

        when (event.keyCode) {
            android.view.KeyEvent.KEYCODE_HEADSETHOOK,
            android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // Repeated key-down events from a long press can be used for a
                // configured seek action instead of being counted as many clicks.
                if (event.repeatCount > 0) {
                    if (settings.singleClickAction == EarphoneAction.SEEK_FORWARD_10S) {
                        seekForward(10_000L)
                    }
                    return true
                }
                val now = android.os.SystemClock.uptimeMillis()
                if (now - lastEarbudClickAtMs > settings.clickIntervalMs + 50L) {
                    earbudClickCount = 0
                }
                lastEarbudClickAtMs = now
                earbudClickCount++
                earbudHandler.removeCallbacks(earbudClickRunnable)
                earbudHandler.postDelayed(earbudClickRunnable, settings.clickIntervalMs)
                return true
            }

            // Physical left/right information is not standardized by Bluetooth.
            // When the headset sends explicit previous/next media commands, those
            // are the closest reliable representation of left/right gestures.
            android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                executeGlobalEarbudAction(settings.doubleClickLeftAction)
                return true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_NEXT -> {
                executeGlobalEarbudAction(settings.doubleClickRightAction)
                return true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                executeGlobalEarbudAction(EarphoneAction.SEEK_FORWARD_10S)
                return true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_REWIND -> {
                executeGlobalEarbudAction(EarphoneAction.SEEK_BACKWARD_10S)
                return true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> {
                play()
                return true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                pause()
                return true
            }
        }
        return false
    }

    private fun dispatchCustomEarbudClicks() {
        val settings = EarphonePreferences.load(context)
        if (!settings.isCustomControlsEnabled || !isHeadsetOrEarbudConnected()) {
            earbudClickCount = 0
            return
        }
        val action = when (earbudClickCount) {
            1 -> settings.singleClickAction
            2 -> settings.doubleClickAction
            3 -> settings.tripleClickAction
            else -> EarphoneAction.NONE
        }
        earbudClickCount = 0
        executeGlobalEarbudAction(action)
    }

    private fun executeGlobalEarbudAction(action: EarphoneAction) {
        when (action) {
            EarphoneAction.PLAY_PAUSE -> togglePlayPause()
            EarphoneAction.NEXT_TRACK -> onPlayNextMedia?.invoke() ?: nextTrack()
            EarphoneAction.PREVIOUS_TRACK -> onPlayPreviousMedia?.invoke() ?: previousTrack()
            EarphoneAction.SEEK_FORWARD_10S -> seekForward(10_000L)
            EarphoneAction.SEEK_BACKWARD_10S -> seekBackward(10_000L)
            EarphoneAction.INCREASE_VOLUME -> increaseVolume()
            EarphoneAction.DECREASE_VOLUME -> decreaseVolume()
            EarphoneAction.MUTE_UNMUTE -> toggleMute()
            EarphoneAction.TOGGLE_SPEED -> {
                val speeds = listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                val current = _playerState.value.playbackSpeed
                val index = speeds.indexOfFirst { kotlin.math.abs(it - current) < 0.05f }
                setPlaybackSpeed(speeds[(if (index < 0) 0 else index + 1) % speeds.size])
            }
            EarphoneAction.NONE -> Unit
        }
    }

    private fun toggleMute() {
        val current = _playerState.value.volumePercent
        if (current > 0) {
            preferences.edit().putInt("earphone_previous_volume", current).apply()
            setSystemVolumePercent(0)
        } else {
            val previous = preferences.getInt("earphone_previous_volume", 100).coerceAtLeast(1)
            setSystemVolumePercent(previous)
        }
    }

    private fun isHeadsetOrEarbudConnected(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn || audioManager.isWiredHeadsetOn
            } else {
                audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                    when (device.type) {
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_BLE_HEADSET,
                        AudioDeviceInfo.TYPE_HEARING_AID,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_USB_HEADSET -> true
                        else -> false
                    }
                }
            }
        } catch (_: SecurityException) {
            // If Bluetooth device information is unavailable, fail closed so
            // custom controls cannot hijack ordinary phone media controls.
            false
        } catch (_: Exception) {
            false
        }
    }

    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer != null) return exoPlayer!!

        val userAgent = androidx.media3.common.util.Util.getUserAgent(context, "V2S_Media_Player")
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(20000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        // Media3's default extractor set covers MP4/M4V, Matroska/WebM, MP3/AAC,
        // MPEG-TS/PS, FLV, WAV, AMR and several other containers. AVI is added
        // explicitly because it is not guaranteed to be part of every default
        // extractor configuration. This keeps container detection reliable for
        // MediaStore content:// URIs as well as normal file paths.
        val baseExtractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setConstantBitrateSeekingAlwaysEnabled(true)
            .setMatroskaExtractorFlags(MatroskaExtractor.FLAG_EMIT_RAW_SUBTITLE_DATA)
            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
            .setAdtsExtractorFlags(AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
            .setAmrExtractorFlags(AmrExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS)

        val extractorsFactory = androidx.media3.extractor.ExtractorsFactory {
            val defaults = baseExtractorsFactory.createExtractors().toMutableList()
            if (defaults.none { it is AviExtractor }) {
                defaults.add(
                    AviExtractor(
                        AviExtractor.FLAG_EMIT_RAW_SUBTITLE_DATA,
                        DefaultSubtitleParserFactory()
                    )
                )
            }
            defaults.toTypedArray()
        }
        val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)
            .setDataSourceFactory(dataSourceFactory)

        // Hardware-accelerated playback configuration for smooth 4K/UHD video rendering
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            .setEnableDecoderFallback(true)
            .forceEnableMediaCodecAsynchronousQueueing()
            .setMediaCodecSelector { mimeType, requiresSecure, requiresTunneling ->
                try {
                    val defaultDecoders = androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecure, requiresTunneling)
                    if (_playerState.value.decoderMode == DecoderMode.SW) {
                        val swDecoders = defaultDecoders.filter {
                            !it.hardwareAccelerated ||
                                    it.name.startsWith("c2.android", ignoreCase = true) ||
                                    it.name.startsWith("OMX.google", ignoreCase = true)
                        }
                        if (swDecoders.isNotEmpty()) swDecoders else defaultDecoders
                    } else {
                        // Place hardware-accelerated decoders first (GPU/VPU decoders e.g., Qualcomm, Samsung Exynos, MediaTek, Nvidia),
                        // and append software decoders as seamless fallback for exotic/unsupported codecs.
                        val hwDecoders = defaultDecoders.filter { it.hardwareAccelerated }
                        val swDecoders = defaultDecoders.filter { !it.hardwareAccelerated }
                        val combined = hwDecoders + swDecoders
                        if (combined.isNotEmpty()) combined else defaultDecoders
                    }
                } catch (e: Exception) {
                    androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecure, requiresTunneling)
                }
            }

        // Optimized load control specifically tuned for 4K/UHD bitrates:
        // Generous buffer allocations prevent frame dropping or micro-stutters during high-bitrate 4K playback
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 25_000,
                /* maxBufferMs = */ 60_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // Track selector configured to automatically support full UHD/4K resolutions
        val trackSelector = DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setMaxVideoSize(3840, 2160)
                .setForceHighestSupportedBitrate(false)
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowVideoNonSeamlessAdaptiveness(true)
                .build()
        }

        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            // Media3 owns audio-focus lifecycle for the single player/session.
            // Do not also request focus manually: two independent focus requests
            // can deliver AUDIOFOCUS_LOSS to this app and immediately pause playback.
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                repeatMode = if (_playerState.value.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                shuffleModeEnabled = _playerState.value.isShuffle
                volume = 1f
                playbackParameters = PlaybackParameters(_playerState.value.playbackSpeed)
                addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        updateTracksInfo(tracks)
                    }

                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                        if (!hasUserSelectedVideoTrack && _playerState.value.availableVideoTracks.size > 1) {
                            exoPlayer?.let { updateTracksInfo(it.currentTracks) }
                        }
                    }

                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        setupAudioEffects(audioSessionId)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(
                            "V2SPlayer",
                            "isPlaying=$isPlaying state=$playbackState media=${currentMediaItem?.mediaId}"
                        )
                        _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                        if (isPlaying && !playbackStartRecorded) {
                            _playerState.value.currentVideo?.let { video ->
                                onPlaybackStarted(video.id, currentPosition)
                                playbackStartRecorded = true
                            }
                            _playerState.value.currentSong?.let { song ->
                                onSongPlaybackStarted(song.id, currentPosition)
                                playbackStartRecorded = true
                            }
                        }
                        // ▼ V2S: update persistent notification like VLC / PlayIt
                        postNotification(isPlaying, force = true)
                        if (isPlaying) {
                            startProgressTracking()
                        } else {
                            stopProgressTracking()
                            _playerState.value.currentVideo?.let { video ->
                                onProgressUpdate(video.id, currentPosition)
                            }
                            _playerState.value.currentSong?.let { song ->
                                onSongProgressUpdate(song.id, currentPosition)
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val isBuffering = playbackState == Player.STATE_BUFFERING
                        _playerState.value = _playerState.value.copy(
                            isBuffering = isBuffering,
                            durationMs = if (duration > 0) duration else _playerState.value.durationMs
                        )

                        if (playbackState == Player.STATE_READY) {
                            codecRecoveryAttempts = 0
                            _playerState.value = _playerState.value.copy(
                                durationMs = duration,
                                bufferedPositionMs = bufferedPosition,
                                error = null
                            )
                        }

                        if (playbackState == Player.STATE_ENDED) {
                            scope.launch(Dispatchers.Main) {
                                handleMediaEnded()
                            }
                        }
                    }

                    private fun handlePlaybackError(error: androidx.media3.common.PlaybackException) {
                        val message = error.localizedMessage?.takeIf { it.isNotBlank() }
                            ?: "Cannot load this media"
                        Log.e("V2SPlayer", "Playback failed: $message", error)

                        val isDecoderOrResourceError = error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ||
                                error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                                message.contains("MediaCodec", ignoreCase = true) ||
                                message.contains("resource", ignoreCase = true) ||
                                message.contains("decoder", ignoreCase = true) ||
                                message.contains("Released", ignoreCase = true) ||
                                message.contains("system resources", ignoreCase = true) ||
                                message.contains("interface", ignoreCase = true) ||
                                message.contains("component", ignoreCase = true) ||
                                message.contains("format", ignoreCase = true) ||
                                message.contains("codec", ignoreCase = true)

                        if (isDecoderOrResourceError && codecRecoveryAttempts < 3) {
                            codecRecoveryAttempts++
                            Log.w("V2SPlayer", "Decoder or resource reclamation issue ($message). Auto-recovering player (attempt $codecRecoveryAttempts/3)...")
                            scope.launch(Dispatchers.Main) {
                                delay(200)
                                if (_playerState.value.decoderMode == DecoderMode.HW) {
                                    setDecoderMode(DecoderMode.SW)
                                } else {
                                    val currentMedia = exoPlayer?.currentMediaItem
                                    val pos = exoPlayer?.currentPosition ?: _playerState.value.currentPositionMs
                                    val wasPlaying = exoPlayer?.playWhenReady ?: true
                                    try {
                                        exoPlayer?.stop()
                                        exoPlayer?.release()
                                    } catch (_: Exception) {}
                                    exoPlayer = null
                                    if (currentMedia != null) {
                                        val newPlayer = getOrCreatePlayer()
                                        newPlayer.setMediaItem(currentMedia)
                                        newPlayer.seekTo(pos)
                                        newPlayer.prepare()
                                        if (wasPlaying) newPlayer.play()
                                    }
                                }
                            }
                            return
                        }

                        // Audio track fallback: If audio decoding failed, try alternate audio track or continue video
                        val player = exoPlayer
                        if (player != null && (message.contains("audio", ignoreCase = true) || message.contains("ac3", ignoreCase = true) || message.contains("dts", ignoreCase = true))) {
                            val availableAudios = _playerState.value.availableAudioTracks
                            val currentAudioIdx = _playerState.value.currentAudioTrackIndex
                            if (availableAudios.size > 1 && currentAudioIdx == 0) {
                                Log.w("V2SPlayer", "Audio track error, switching to alternate audio track...")
                                selectAudioTrack(1)
                                player.prepare()
                                player.play()
                                return
                            }
                        }

                        _playerState.value = _playerState.value.copy(
                            error = message,
                            isBuffering = false,
                            isPlaying = false
                        )
                        stopProgressTracking()
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        handlePlaybackError(error)
                    }

                    override fun onPlayerErrorChanged(error: androidx.media3.common.PlaybackException?) {
                        if (error != null) {
                            handlePlaybackError(error)
                        }
                    }
                })
            }
        exoPlayer = player
        if (mediaSession == null) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?: Intent(Intent.ACTION_MAIN).apply {
                        setPackage(context.packageName)
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                launchIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                val sessionActivityPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                mediaSession = MediaSession.Builder(context, player)
                    .setSessionActivity(sessionActivityPendingIntent)
                    .setCallback(object : MediaSession.Callback {
                        override fun onMediaButtonEvent(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                            intent: Intent
                        ): Boolean {
                            return if (handleGlobalEarbudMediaButton(intent)) {
                                true
                            } else {
                                super.onMediaButtonEvent(session, controller, intent)
                            }
                        }

                        override fun onPlayerCommandRequest(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                            playerCommand: Int
                        ): Int {
                            if (playerCommand == Player.COMMAND_SEEK_TO_NEXT || playerCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM) {
                                onPlayNextMedia?.invoke()
                            } else if (playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS || playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM) {
                                onPlayPreviousMedia?.invoke()
                            }
                            return super.onPlayerCommandRequest(session, controller, playerCommand)
                        }
                    })
                    .build()
            } catch (e: Exception) {
                Log.e("V2SPlayer", "Error initializing MediaSession: ${e.message}", e)
            }
        }
        return player
    }

    /**
     * Persist the exact position of the media that is currently loaded before
     * another media item replaces it. This is especially important for Shorts,
     * where a vertical swipe can happen before the normal 5-second persistence
     * interval fires.
     */
    private fun persistCurrentPosition() {
        val state = _playerState.value
        val position = (exoPlayer?.currentPosition ?: state.currentPositionMs)
            .coerceAtLeast(0L)

        state.currentVideo?.let { video ->
            onProgressUpdate(video.id, position)
        }

        state.currentSong?.let { song ->
            onSongProgressUpdate(song.id, position)
        }

        lastPersistedPositionMs = position
        _playerState.value = state.copy(currentPositionMs = position)
    }

    private fun readSystemVolumePercent(): Int {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((current.toFloat() / max.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    }

    private fun mediaKey(state: PlayerUiState): String? = when {
        state.currentVideo != null -> "video:${state.currentVideo.id}"
        state.currentSong != null -> "song:${state.currentSong.id}"
        else -> null
    }

    private fun prepareForNewMedia(stateBeforeSwitch: PlayerUiState) {
        val currentPlayer = exoPlayer ?: return
        val key = mediaKey(stateBeforeSwitch)
        if (_playerState.value.isMultiplePlaybackEnabled && key != null && currentPlayer.isPlaying) {
            if (secondaryPlayers.size >= 1) {
                releaseSecondaryPlayers()
            }
            secondaryPlayers[key]?.release()
            secondaryPlayers[key] = currentPlayer
            currentPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        secondaryPlayers.remove(key)?.release()
                    }
                }
            })
            exoPlayer = null
            return
        }
        releaseSecondaryPlayers()
    }

    private fun releaseSecondaryPlayers() {
        secondaryPlayers.values.forEach { it.release() }
        secondaryPlayers.clear()
    }

    private fun handleMediaEnded() {
        val state = _playerState.value
        when (state.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                exoPlayer?.seekTo(0L)
                startCurrentMedia()
                _playerState.value = _playerState.value.copy(
                    currentPositionMs = 0L,
                    isPlaying = true
                )
            }
            Player.REPEAT_MODE_ALL -> {
                onPlayNextMedia?.invoke()
            }
            else -> {
                // Repeat Mode OFF: still attempt to play next item in the queue;
                // onPlayNextMedia/playNextVideo/playNextSong handles stopping if at the end of the queue.
                onPlayNextMedia?.invoke()
            }
        }
    }

    private fun buildMediaItem(
        uri: Uri,
        path: String?,
        title: String?,
        isAudio: Boolean,
        declaredMimeType: String? = null
    ): MediaItem {
        val contentResolverMime = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        val mimeType = MediaPlaybackUtils.resolveMimeType(
            declaredMimeType = declaredMimeType ?: contentResolverMime,
            path = path,
            uriString = uri.toString(),
            isAudio = isAudio
        )
        val builder = MediaItem.Builder().setUri(uri)
        // A MIME hint is especially important for MediaStore content:// URIs because
        // their last path segment is usually only a numeric database ID. Progressive
        // media can still be sniffed by Media3 when no hint is available.
        if (!mimeType.isNullOrBlank()) {
            builder.setMimeType(mimeType)
        }

        // Subtitle support: scan for sidecar subtitle files (.srt, .ass, .ssa, .sub, .idx, .vtt, .txt)
        if (!isAudio && path != null) {
            val subtitleConfigs = findSubtitlesForFile(path)
            if (subtitleConfigs.isNotEmpty()) {
                builder.setSubtitleConfigurations(subtitleConfigs)
            }
        }

        if (!title.isNullOrBlank()) {
            builder.setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )
        }

        return builder.build()
    }

    private fun findSubtitlesForFile(filePath: String): List<MediaItem.SubtitleConfiguration> {
        return runCatching {
            val videoFile = File(filePath)
            val parentDir = videoFile.parentFile ?: return emptyList()
            val baseName = videoFile.nameWithoutExtension
            val subExtensions = listOf("srt", "ass", "ssa", "vtt", "sub", "idx", "txt")
            val result = mutableListOf<MediaItem.SubtitleConfiguration>()

            val files = parentDir.listFiles() ?: return emptyList()
            for (f in files) {
                if (!f.isFile) continue
                val ext = f.extension.lowercase()
                if (ext in subExtensions && f.nameWithoutExtension.startsWith(baseName, ignoreCase = true)) {
                    val mime = when (ext) {
                        "srt", "sub", "txt", "idx" -> MimeTypes.APPLICATION_SUBRIP
                        "ass", "ssa" -> MimeTypes.TEXT_SSA
                        "vtt" -> MimeTypes.TEXT_VTT
                        else -> MimeTypes.APPLICATION_SUBRIP
                    }
                    val label = if (f.nameWithoutExtension.length > baseName.length) {
                        f.nameWithoutExtension.substring(baseName.length).trim('.', '_', '-', ' ')
                    } else {
                        f.extension.uppercase()
                    }
                    val config = MediaItem.SubtitleConfiguration.Builder(Uri.fromFile(f))
                        .setMimeType(mime)
                        .setLanguage("en")
                        .setLabel(if (label.isNotBlank()) label else "Subtitle ${result.size + 1}")
                        .setSelectionFlags(if (result.isEmpty()) C.SELECTION_FLAG_DEFAULT else 0)
                        .build()
                    result.add(config)
                }
            }
            result
        }.getOrDefault(emptyList())
    }

    fun playVideo(
        video: VideoItem,
        startPositionMs: Long = 0L,
        autoPlay: Boolean = true
    ) {
        val stateBeforeSwitch = _playerState.value
        if (stateBeforeSwitch.currentVideo?.id == video.id && stateBeforeSwitch.currentSong == null && exoPlayer != null) {
            if (startPositionMs > 0L) exoPlayer?.seekTo(startPositionMs) else exoPlayer?.seekTo(0L)
            if (autoPlay) {
                startCurrentMedia()
            } else {
                exoPlayer?.pause()
            }
            return
        }

        // Save the outgoing item before a multiple-playback handoff can detach it.
        if (stateBeforeSwitch.currentVideo?.id != video.id || stateBeforeSwitch.currentSong != null) {
            persistCurrentPosition()
        }

        prepareForNewMedia(stateBeforeSwitch)
        val player = getOrCreatePlayer()

        val resolvedPosition = when {
            startPositionMs > 0L -> startPositionMs
            stateBeforeSwitch.currentVideo?.id == video.id ->
                (exoPlayer?.currentPosition ?: stateBeforeSwitch.currentPositionMs).coerceAtLeast(0L)
            video.lastPositionMs > 0L &&
                (video.durationMs <= 0L || video.lastPositionMs < video.durationMs - 3000L) ->
                video.lastPositionMs
            else -> 0L
        }

        _playerState.value =
            _playerState.value.copy(
                currentVideo = video,
                currentSong = null,
                isAudioOnly = false,
                isVideoAudio = false,
                notificationArtworkUri = null,
                currentPositionMs = resolvedPosition,
                durationMs = if (video.durationMs > 0) video.durationMs else 0L,
                error = null
            )

        lastPersistedPositionMs = resolvedPosition
        playbackStartRecorded = false
        lastNotificationUpdateAtMs = 0L
        hasUserSelectedVideoTrack = false

        val mediaItem = buildMediaItem(
            uri = video.uri,
            path = video.path,
            title = video.title,
            isAudio = false,
            declaredMimeType = video.mimeType
        )
        player.setMediaItem(mediaItem, resolvedPosition.coerceAtLeast(0L))

        if (autoPlay) {
        }
        player.prepare()
        player.playWhenReady = autoPlay
        if (autoPlay) player.play()

        postNotification(autoPlay, force = true)
    }

    fun playSong(
        song: SongItem,
        startPositionMs: Long = 0L,
        autoPlay: Boolean = true,
        asVideoAudio: Boolean = false,
        notificationArtworkUri: android.net.Uri? = song.albumArtUri
    ) {
        val stateBeforeSwitch = _playerState.value
        if (stateBeforeSwitch.currentSong?.id == song.id && stateBeforeSwitch.currentVideo == null && exoPlayer != null) {
            if (startPositionMs > 0L) exoPlayer?.seekTo(startPositionMs) else exoPlayer?.seekTo(0L)
            if (autoPlay) {
                startCurrentMedia()
            } else {
                exoPlayer?.pause()
            }
            return
        }

        // Save the outgoing item before a multiple-playback handoff can detach it.
        if (stateBeforeSwitch.currentSong?.id != song.id || stateBeforeSwitch.currentVideo != null) {
            persistCurrentPosition()
        }

        prepareForNewMedia(stateBeforeSwitch)
        val player = getOrCreatePlayer()

        val resolvedPosition = if (startPositionMs > 0L) startPositionMs else 0L

        _playerState.value =
            _playerState.value.copy(
                currentSong = song,
                currentVideo = null,
                isAudioOnly = true,
                isVideoAudio = asVideoAudio,
                notificationArtworkUri = notificationArtworkUri,
                currentPositionMs = resolvedPosition,
                durationMs = if (song.durationMs > 0) song.durationMs else 0L,
                error = null
            )

        lastPersistedPositionMs = resolvedPosition
        playbackStartRecorded = false
        lastNotificationUpdateAtMs = 0L

        val mediaItem = buildMediaItem(
            uri = song.uri,
            path = song.path,
            title = song.title,
            isAudio = true,
            declaredMimeType = song.mimeType
        )
        player.setMediaItem(mediaItem, resolvedPosition.coerceAtLeast(0L))

        if (autoPlay) {
        }
        player.prepare()
        player.playWhenReady = autoPlay
        if (autoPlay) player.play()

        postNotification(autoPlay, force = true)
    }

    /**
     * Starts the currently loaded item even when the player is idle/ended or
     * has just recovered from a decoder/load error. UI buttons, notifications
     * and Bluetooth controls all go through this path so they behave the same.
     */
    private fun startCurrentMedia() {
        var player = exoPlayer

        // The UI must still be able to start playback if the player was released
        // during decoder recovery. Re-create it from the media stored in UI state.
        if (player == null || player.currentMediaItem == null) {
            val state = _playerState.value
            val position = state.currentPositionMs.coerceAtLeast(0L)
            when {
                state.currentVideo != null -> {
                    playVideo(state.currentVideo, startPositionMs = position, autoPlay = true)
                    return
                }
                state.currentSong != null -> {
                    playSong(
                        state.currentSong,
                        startPositionMs = position,
                        autoPlay = true,
                        asVideoAudio = state.isVideoAudio,
                        notificationArtworkUri = state.notificationArtworkUri
                    )
                    return
                }
                else -> return
            }
        }

        player = exoPlayer ?: return
        val currentMedia = player.currentMediaItem ?: return

        val position = player.currentPosition.coerceAtLeast(0L)
        val hasError = player.playerError != null
        val needsPrepare = player.playbackState == Player.STATE_IDLE ||
            player.playbackState == Player.STATE_ENDED ||
            hasError

        if (needsPrepare) {
            // Clear a failed/ended playback session before starting it again.
            // This is important because Bluetooth MEDIA_PLAY calls resume(), while
            // the on-screen button calls togglePlayPause(). Both must recover the
            // exact same way after a decoder or loading error.
            val restartFromBeginning = player.playbackState == Player.STATE_ENDED
            val restorePosition = if (restartFromBeginning) 0L else position
            Log.d(
                "V2SPlayer",
                "UI/Bluetooth start recovery: state=${player.playbackState}, error=${player.playerError?.errorCode}, position=$restorePosition"
            )
            player.playWhenReady = false
            // Rebuild the timeline after an ERROR/ENDED state instead of
            // preparing the failed timeline in place.
            if (hasError || player.playbackState == Player.STATE_ENDED) {
                player.setMediaItem(currentMedia, restorePosition)
                player.prepare()
            } else {
                player.prepare()
                if (restorePosition > 0L) {
                    player.seekTo(restorePosition)
                } else if (restartFromBeginning) {
                    player.seekTo(0L)
                }
            }
        }

        player.playWhenReady = true
        player.play()
    }

    fun togglePlayPause() {
        val player = exoPlayer

        // Do NOT use playWhenReady as the pause condition. After a playback/decoder
        // error Media3 can leave playWhenReady=true while isPlaying=false. In that
        // state the old code treated the Play button as Pause, so tapping the UI
        // could never recover.
        if (player?.isPlaying == true) {
            pause()
        } else {
            startCurrentMedia()
        }
    }

    fun play() {
        startCurrentMedia()
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
        exoPlayer?.pause()
    }

    fun resume() {
        startCurrentMedia()
    }

    fun stop() { stopMedia() }

    fun previousTrack() {
        onPlayPreviousMedia?.invoke() ?: run {
            exoPlayer?.seekTo(0L)
        }
    }

    fun nextTrack() {
        onPlayNextMedia?.invoke() ?: run {
            seekForward(10_000L)
        }
    }

    fun seekRelative(offsetMs: Long) {
        if (offsetMs >= 0) {
            seekForward(offsetMs)
        } else {
            seekBackward(-offsetMs)
        }
    }

    fun stopMedia() {
        releaseSecondaryPlayers()
        stopProgressTracking()
        _playerState.value.currentVideo?.let { video ->
            exoPlayer?.let { player -> onProgressUpdate(video.id, player.currentPosition) }
        }
        _playerState.value.currentSong?.let { song ->
            exoPlayer?.let { player -> onSongProgressUpdate(song.id, player.currentPosition) }
        }
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        lastNotificationUpdateAtMs = 0L
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            currentVideo = null,
            currentSong = null,
            isAudioOnly = false,
            isVideoAudio = false,
            notificationArtworkUri = null,
            currentPositionMs = 0L,
            durationMs = 0L,
            bufferedPositionMs = 0L,
            error = null
        )
        // ▼ V2S: hide notification when media stops (like VLC dismiss)
        hideNotification()
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _playerState.value.durationMs.coerceAtLeast(0L))
        exoPlayer?.seekTo(bounded)
        _playerState.value = _playerState.value.copy(currentPositionMs = bounded)
    }

    fun seekForward(deltaMs: Long = 10_000L) {
        exoPlayer?.let {
            val target = (it.currentPosition + deltaMs).coerceAtMost(it.duration.coerceAtLeast(0L))
            it.seekTo(target)
        }
    }

    fun seekBackward(deltaMs: Long = 10_000L) {
        exoPlayer?.let {
            val target = (it.currentPosition - deltaMs).coerceAtLeast(0L)
            it.seekTo(target)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val safe = speed.coerceIn(0.25f, 4.0f)
        preferences.edit().putFloat("playback_speed", safe).apply()
        exoPlayer?.playbackParameters = PlaybackParameters(safe)
        _playerState.value = _playerState.value.copy(playbackSpeed = safe)
    }

    /**
     * Changes speed only for the current player state. This is intentionally
     * not persisted, so temporary gestures (for example Shorts long-press)
     * cannot overwrite the user's global playback-speed setting.
     */
    fun setTemporaryPlaybackSpeed(speed: Float) {
        val safe = speed.coerceIn(0.25f, 4.0f)
        exoPlayer?.playbackParameters = PlaybackParameters(safe)
        _playerState.value = _playerState.value.copy(playbackSpeed = safe)
    }

    /** Restores the globally persisted playback speed after a temporary gesture. */
    fun restorePersistentPlaybackSpeed() {
        val safe = preferences.getFloat("playback_speed", 1.0f).coerceIn(0.25f, 4.0f)
        exoPlayer?.playbackParameters = PlaybackParameters(safe)
        _playerState.value = _playerState.value.copy(playbackSpeed = safe)
    }

    fun setMultiplePlaybackEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("multiple_playback", enabled).apply()
        if (!enabled) releaseSecondaryPlayers()
        _playerState.value = _playerState.value.copy(isMultiplePlaybackEnabled = enabled)
    }

    private fun syncSystemVolume() {
        _playerState.value = _playerState.value.copy(volumePercent = readSystemVolumePercent())
    }

    fun setSystemVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 200)
        if (clamped in 1..100) lastNonZeroVolumePercent = clamped

        if (clamped <= 100) {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val target = ((clamped / 100f) * max).roundToInt().coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            try {
                loudnessEnhancer?.enabled = false
            } catch (_: Exception) {}
        } else {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
            try {
                val boostGainMb = (((clamped - 100) / 100f) * 1500).toInt()
                loudnessEnhancer?.apply {
                    setTargetGain(boostGainMb)
                    enabled = true
                }
            } catch (e: Exception) {
                Log.e("V2SPlayer", "Audio boost error: ${e.message}")
            }
        }
        _playerState.value = _playerState.value.copy(volumePercent = clamped)
    }

    private fun setupAudioEffects(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                val boost = (_playerState.value.volumePercent - 100).coerceAtLeast(0)
                if (boost > 0) {
                    setTargetGain((boost / 100f * 1500).toInt())
                    enabled = true
                } else {
                    enabled = false
                }
            }
        } catch (e: Exception) {
            Log.e("V2SPlayer", "LoudnessEnhancer setup error: ${e.message}")
        }
    }

    fun mute() {
        lastNonZeroVolumePercent = readSystemVolumePercent().coerceAtLeast(1)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
        try {
            loudnessEnhancer?.enabled = false
        } catch (_: Exception) {}
        _playerState.value = _playerState.value.copy(volumePercent = 0)
    }

    fun unmute() {
        setSystemVolumePercent(lastNonZeroVolumePercent.coerceIn(1, 100))
    }

    fun increaseVolume() {
        setSystemVolumePercent((_playerState.value.volumePercent + 5).coerceAtMost(200))
    }

    fun decreaseVolume() {
        setSystemVolumePercent((_playerState.value.volumePercent - 5).coerceAtLeast(0))
    }

    fun setBrightnessPercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        preferences.edit().putInt("brightness_percent", clamped).apply()
        _playerState.value = _playerState.value.copy(brightnessPercent = clamped)
    }

    fun cycleScaleMode(): VideoScaleMode {
        val modes = VideoScaleMode.values()
        val nextIndex = (_playerState.value.scaleMode.ordinal + 1) % modes.size
        val nextMode = modes[nextIndex]
        preferences.edit().putString("scale_mode", nextMode.name).apply()
        _playerState.value = _playerState.value.copy(scaleMode = nextMode)
        return nextMode
    }

    fun setScaleMode(mode: VideoScaleMode) {
        preferences.edit().putString("scale_mode", mode.name).apply()
        _playerState.value = _playerState.value.copy(scaleMode = mode)
    }

    fun toggleLock() {
        _playerState.value = _playerState.value.copy(isLocked = !_playerState.value.isLocked)
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        preferences.edit().putString("equalizer_preset", preset.name).apply()
        _playerState.value = _playerState.value.copy(equalizerPreset = preset)
    }

    fun setRepeatMode(mode: Int) {
        preferences.edit().putInt("repeat_mode", mode).apply()
        exoPlayer?.repeatMode = if (mode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _playerState.value = _playerState.value.copy(repeatMode = mode)
    }

    fun setShuffle(enabled: Boolean) {
        preferences.edit().putBoolean("shuffle", enabled).apply()
        exoPlayer?.shuffleModeEnabled = enabled
        _playerState.value = _playerState.value.copy(isShuffle = enabled)
    }

    fun toggleShuffle() {
        setShuffle(!_playerState.value.isShuffle)
    }

    fun setSubtitlesEnabled(enabled: Boolean) {
        val player = exoPlayer ?: return
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled)
            .build()
        _playerState.value = _playerState.value.copy(isSubtitlesEnabled = enabled)
    }

    private var hasUserSelectedVideoTrack: Boolean = false

    private fun updateTracksInfo(tracks: Tracks) {
        val videoTracks = mutableListOf<String>()
        val videoFormats = mutableListOf<androidx.media3.common.Format>()
        var selectedVideo = 0
        var videoIdx = 0

        val audioTracks = mutableListOf<String>()
        var selectedAudio = 0
        var audioIdx = 0

        val subtitleTracks = mutableListOf<String>()
        var selectedSub = -1
        var subIdx = 0

        for (group in tracks.groups) {
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        videoFormats.add(format)
                        val res = if (format.width > 0 && format.height > 0) "${format.width}x${format.height}" else ""
                        val fps = if (format.frameRate > 0) "${format.frameRate.roundToInt()}fps" else ""
                        val bitrate = if (format.bitrate > 0) "${format.bitrate / 1000}kbps" else ""
                        val label = listOfNotNull(format.label, res, fps, bitrate).filter { it.isNotBlank() }.joinToString(" • ")
                        val streamLabel = if (label.isNotBlank()) "Stream ${videoIdx + 1} ($label)" else "Stream ${videoIdx + 1}"
                        videoTracks.add(streamLabel)
                        if (group.isTrackSelected(i)) {
                            selectedVideo = videoIdx
                        }
                        videoIdx++
                    }
                }
                C.TRACK_TYPE_AUDIO -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language?.uppercase() ?: "Track ${audioIdx + 1}"
                        val channels = if (format.channelCount > 0) "${format.channelCount}ch" else ""
                        val label = listOfNotNull(format.label, lang, channels).filter { it.isNotBlank() }.joinToString(" • ")
                        audioTracks.add(label.ifEmpty { "Audio Track ${audioIdx + 1}" })
                        if (group.isTrackSelected(i)) {
                            selectedAudio = audioIdx
                        }
                        audioIdx++
                    }
                }
                C.TRACK_TYPE_TEXT -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language?.uppercase() ?: "Sub ${subIdx + 1}"
                        val label = format.label ?: lang
                        subtitleTracks.add(label.ifEmpty { "Subtitle ${subIdx + 1}" })
                        if (group.isTrackSelected(i)) {
                            selectedSub = subIdx
                        }
                        subIdx++
                    }
                }
            }
        }

        // Automatic Multi-Stream / PLAYit smart selection:
        // VidMate, SnapTube, and PLAYit Smart Muxer embed a promotional warning video as Stream 1 (index 0)
        // asking users to install PLAYit. The actual full video is ALWAYS Stream 2 (index 1).
        // Automatically bypass the promotional stream and play the real video stream!
        if (videoTracks.size > 1 && !hasUserSelectedVideoTrack) {
            val bestIdx = if (videoFormats.size == videoTracks.size) {
                videoFormats.indices.maxWithOrNull(
                    compareBy<Int> { idx ->
                        val f = videoFormats[idx]
                        val area = if (f.width > 0 && f.height > 0) f.width.toLong() * f.height.toLong() else 0L
                        val br = if (f.bitrate > 0) f.bitrate.toLong() else 0L
                        area * 10_000L + br
                    }.thenBy { idx ->
                        // In case of tie (e.g. equal resolution or unmeasured 0), Stream 2 (index 1) ALWAYS beats Stream 1 (index 0)!
                        idx
                    }
                ) ?: 1
            } else {
                // If format sizes aren't populated yet, default to Stream 2 (index 1) to bypass promo stream
                1
            }

            val targetIdx = if (bestIdx in videoTracks.indices) bestIdx else 1
            if (targetIdx != selectedVideo && targetIdx in videoTracks.indices) {
                Log.i("V2SPlayer", "Auto-switching to real video stream index $targetIdx (PLAYit / multi-stream auto-bypass)")
                selectVideoTrack(targetIdx, isUserAction = false)
                selectedVideo = targetIdx
            }
        }

        _playerState.value = _playerState.value.copy(
            availableVideoTracks = videoTracks,
            currentVideoTrackIndex = selectedVideo,
            availableAudioTracks = audioTracks,
            currentAudioTrackIndex = selectedAudio,
            availableSubtitleTracks = subtitleTracks,
            currentSubtitleTrackIndex = selectedSub,
            isSubtitlesEnabled = selectedSub >= 0,
            isSmartMuxerDetected = videoTracks.size > 1
        )
    }

    fun getAvailableVideoTracks(): List<String> = _playerState.value.availableVideoTracks

    fun toggleVideoStream() {
        val count = _playerState.value.availableVideoTracks.size
        if (count <= 1) return
        val next = (_playerState.value.currentVideoTrackIndex + 1) % count
        selectVideoTrack(next, isUserAction = true)
    }

    fun selectVideoTrack(trackIndex: Int, isUserAction: Boolean = true) {
        if (isUserAction) {
            hasUserSelectedVideoTrack = true
        }
        val player = exoPlayer ?: return
        var currentIndex = 0
        val tracks = player.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_VIDEO) {
                for (i in 0 until group.length) {
                    if (currentIndex == trackIndex) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, i)
                            )
                            .build()
                        _playerState.value = _playerState.value.copy(currentVideoTrackIndex = trackIndex)
                        return
                    }
                    currentIndex++
                }
            }
        }
    }

    fun getAvailableAudioTracks(): List<String> {
        return _playerState.value.availableAudioTracks.ifEmpty {
            val player = exoPlayer ?: return emptyList()
            val list = mutableListOf<String>()
            val tracks = player.currentTracks
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_AUDIO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language?.uppercase() ?: "Audio Track ${list.size + 1}"
                        val channels = if (format.channelCount > 0) "${format.channelCount}ch" else ""
                        val label = listOfNotNull(format.label, lang, channels).filter { it.isNotBlank() }.joinToString(" • ")
                        list.add(label.ifEmpty { "Audio Track ${list.size + 1}" })
                    }
                }
            }
            list
        }
    }

    fun selectAudioTrack(trackIndex: Int) {
        val player = exoPlayer ?: return
        var currentIndex = 0
        val tracks = player.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    if (currentIndex == trackIndex) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, i)
                            )
                            .build()
                        _playerState.value = _playerState.value.copy(currentAudioTrackIndex = trackIndex)
                        return
                    }
                    currentIndex++
                }
            }
        }
    }

    fun getAvailableSubtitleTracks(): List<String> {
        return _playerState.value.availableSubtitleTracks.ifEmpty {
            val player = exoPlayer ?: return emptyList()
            val list = mutableListOf<String>()
            val tracks = player.currentTracks
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language?.uppercase() ?: "Sub ${list.size + 1}"
                        val label = format.label ?: lang
                        list.add(label.ifEmpty { "Subtitle Track ${list.size + 1}" })
                    }
                }
            }
            list
        }
    }

    fun selectSubtitleTrack(trackIndex: Int) {
        val player = exoPlayer ?: return
        if (trackIndex < 0) {
            setSubtitlesEnabled(false)
            _playerState.value = _playerState.value.copy(currentSubtitleTrackIndex = -1, isSubtitlesEnabled = false)
            return
        }
        setSubtitlesEnabled(true)
        var currentIndex = 0
        val tracks = player.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    if (currentIndex == trackIndex) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, i)
                            )
                            .build()
                        _playerState.value = _playerState.value.copy(currentSubtitleTrackIndex = trackIndex, isSubtitlesEnabled = true)
                        return
                    }
                    currentIndex++
                }
            }
        }
    }

    fun addExternalSubtitle(uri: Uri, label: String = "External Subtitle") {
        val player = exoPlayer ?: return
        val currentMediaItem = player.currentMediaItem ?: return
        val ext = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase() ?: ""
        val mimeType = when (ext) {
            "srt", "sub", "txt", "idx" -> MimeTypes.APPLICATION_SUBRIP
            "vtt" -> MimeTypes.TEXT_VTT
            "ssa", "ass" -> MimeTypes.TEXT_SSA
            "ttml", "xml", "dfxp" -> MimeTypes.APPLICATION_TTML
            else -> MimeTypes.APPLICATION_SUBRIP
        }
        val subConfig = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mimeType)
            .setLanguage("und")
            .setLabel(label)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()

        val existingConfigs = currentMediaItem.localConfiguration?.subtitleConfigurations ?: emptyList()
        val newConfigs = existingConfigs + subConfig
        val newMediaItem = currentMediaItem.buildUpon()
            .setSubtitleConfigurations(newConfigs)
            .build()

        val pos = player.currentPosition
        val isPlaying = player.isPlaying
        player.setMediaItem(newMediaItem, pos)
        player.prepare()
        if (isPlaying) player.play()
        setSubtitlesEnabled(true)
    }

    fun setSubtitleDelayMs(delayMs: Long) {
        _playerState.value = _playerState.value.copy(subtitleDelayMs = delayMs)
    }

    fun setSubtitleFontSizeSp(sizeSp: Int) {
        val safe = sizeSp.coerceIn(12, 36)
        preferences.edit().putInt("subtitle_font_size", safe).apply()
        _playerState.value = _playerState.value.copy(subtitleFontSizeSp = safe)
    }

    fun toggleDecoderMode(): DecoderMode {
        val nextMode = if (_playerState.value.decoderMode == DecoderMode.HW) DecoderMode.SW else DecoderMode.HW
        setDecoderMode(nextMode)
        return nextMode
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        val targetMode = if (enabled) DecoderMode.HW else DecoderMode.SW
        setDecoderMode(targetMode)
    }

    fun setDecoderMode(mode: DecoderMode) {
        if (_playerState.value.decoderMode == mode && exoPlayer != null) return
        preferences.edit().putString("decoder_mode", mode.name).apply()
        _playerState.value = _playerState.value.copy(decoderMode = mode)

        val player = exoPlayer ?: return
        val currentMediaItem = player.currentMediaItem ?: return
        val currentPos = player.currentPosition
        val wasPlaying = player.playWhenReady

        stopProgressTracking()
        // MediaSession is bound to the old ExoPlayer instance. Recreate it
        // whenever the player is recreated (for example after decoder switch).
        try {
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
        player.stop()
        player.release()
        exoPlayer = null

        val newPlayer = getOrCreatePlayer()
        newPlayer.setMediaItem(currentMediaItem)
        newPlayer.seekTo(currentPos)
        newPlayer.prepare()
        if (wasPlaying) {
            newPlayer.play()
        }
    }

    fun setRepeatPointA() {
        val pos = exoPlayer?.currentPosition ?: _playerState.value.currentPositionMs
        val pointB = _playerState.value.repeatPointB
        _playerState.value = _playerState.value.copy(
            repeatPointA = pos,
            isAbRepeatActive = pointB != null && pos < pointB
        )
    }

    fun setRepeatPointB() {
        val pos = exoPlayer?.currentPosition ?: _playerState.value.currentPositionMs
        val pointA = _playerState.value.repeatPointA
        if (pointA != null && pos > pointA) {
            _playerState.value = _playerState.value.copy(
                repeatPointB = pos,
                isAbRepeatActive = true
            )
        } else {
            _playerState.value = _playerState.value.copy(repeatPointB = pos)
        }
    }

    fun clearAbRepeat() {
        _playerState.value = _playerState.value.copy(
            repeatPointA = null,
            repeatPointB = null,
            isAbRepeatActive = false
        )
    }

    fun toggleBackgroundPlay() {
        val enabled = !_playerState.value.isBackgroundPlayEnabled
        preferences.edit().putBoolean("background_play", enabled).apply()
        _playerState.value = _playerState.value.copy(
            isBackgroundPlayEnabled = enabled
        )
        if (enabled && (_playerState.value.currentVideo != null || _playerState.value.currentSong != null)) {
            postNotification(_playerState.value.isPlaying, force = true)
        } else if (!enabled) {
            hideNotification()
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = null)
            return
        }

        _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000L)
                remaining--
                _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = remaining)
            }
            pause()
            _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = null)
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = if (player.duration > 0) player.duration else _playerState.value.durationMs

                    val sysVol = readSystemVolumePercent()
                    val curVol = _playerState.value.volumePercent
                    val finalVol = if (curVol > 100) curVol else sysVol

                    // Check A-B Repeat loop boundary
                    val st = _playerState.value
                    if (st.isAbRepeatActive && st.repeatPointA != null && st.repeatPointB != null) {
                        if (pos >= st.repeatPointB) {
                            player.seekTo(st.repeatPointA)
                        }
                    }

                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = player.bufferedPosition,
                        volumePercent = finalVol
                    )
                    if (_playerState.value.isBackgroundPlayEnabled) {
                        postNotification(isPlaying = true)
                    }
                    if (kotlin.math.abs(pos - lastPersistedPositionMs) >= 5_000L) {
                        _playerState.value.currentVideo?.let { v ->
                            onProgressUpdate(v.id, pos)
                        }
                        _playerState.value.currentSong?.let { song ->
                            onSongProgressUpdate(song.id, pos)
                        }
                        lastPersistedPositionMs = pos
                    }
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun retryCurrent() {
        val currentVideo = _playerState.value.currentVideo
        val currentSong  = _playerState.value.currentSong
        val currentPos   = _playerState.value.currentPositionMs
        if (currentVideo != null) playVideo(currentVideo, currentPos, true)
        else if (currentSong != null) playSong(currentSong, currentPos, true)
    }

fun playCurrentVideoAsAudio() {

    val video =
        _playerState.value.currentVideo
            ?: return

    val position =
        exoPlayer?.currentPosition
            ?: _playerState.value.currentPositionMs

    val songEquivalent =
        SongItem(
            id = video.id,
            uri = video.uri,
            title = video.title,
            artist = "Video Audio",
            album = "V2S",
            durationMs = video.durationMs,
            sizeBytes = video.sizeBytes,
            path = video.path,
            mimeType = video.mimeType
        )

    playSong(
        song = songEquivalent,
        startPositionMs = position,
        autoPlay = true,
        asVideoAudio = true,
        notificationArtworkUri = video.uri
    )
}

    fun release() {
        earbudHandler.removeCallbacks(earbudClickRunnable)
        earbudClickCount = 0
        releaseSecondaryPlayers()
        stopProgressTracking()
        sleepTimerJob?.cancel()
        _playerState.value.currentVideo?.let { video ->
            exoPlayer?.let { player -> onProgressUpdate(video.id, player.currentPosition) }
        }
        _playerState.value.currentSong?.let { song ->
            exoPlayer?.let { player -> onSongProgressUpdate(song.id, player.currentPosition) }
        }
        try {
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
        exoPlayer?.release()
        exoPlayer = null
        if (activeManager == this) {
            activeManager = null
        }
        hideNotification()
    }

    companion object {
        @Volatile
        var activeManager: VyloPlayerManager? = null

        fun getOrCreate(
            context: Context,
            onProgressUpdate: (videoId: Long, positionMs: Long) -> Unit,
            onPlaybackStarted: (videoId: Long, positionMs: Long) -> Unit,
            onSongProgressUpdate: (songId: Long, positionMs: Long) -> Unit,
            onSongPlaybackStarted: (songId: Long, positionMs: Long) -> Unit
        ): VyloPlayerManager {
            val existing = activeManager
            if (existing != null) {
                existing.attachCallbacks(
                    onProgressUpdate = onProgressUpdate,
                    onPlaybackStarted = onPlaybackStarted,
                    onSongProgressUpdate = onSongProgressUpdate,
                    onSongPlaybackStarted = onSongPlaybackStarted
                )
                return existing
            }
            return synchronized(this) {
                val again = activeManager
                if (again != null) {
                    again.attachCallbacks(
                        onProgressUpdate = onProgressUpdate,
                        onPlaybackStarted = onPlaybackStarted,
                        onSongProgressUpdate = onSongProgressUpdate,
                        onSongPlaybackStarted = onSongPlaybackStarted
                    )
                    again
                } else {
                    VyloPlayerManager(
                        context = context.applicationContext,
                        onProgressUpdate = onProgressUpdate,
                        onPlaybackStarted = onPlaybackStarted,
                        onSongProgressUpdate = onSongProgressUpdate,
                        onSongPlaybackStarted = onSongPlaybackStarted
                    ).also { activeManager = it }
                }
            }
        }
    }
}
