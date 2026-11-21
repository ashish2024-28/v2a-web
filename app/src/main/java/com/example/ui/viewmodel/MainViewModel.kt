package com.example.ui.viewmodel

import com.example.ui.theme.AccentColor

import android.app.Application
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.VyloDatabase
import com.example.data.model.DurationFilter
import com.example.data.model.FolderItem
import com.example.data.model.PlaylistData
import com.example.data.model.PlaylistMediaItem
import com.example.data.model.PlaylistMediaType
import com.example.data.model.ShortsCompletionMode
import com.example.data.model.SongItem
import com.example.data.model.SortOrder
import com.example.data.model.VideoItem
import com.example.data.repository.MediaDeleteResult
import com.example.data.repository.VideoRepository
import com.example.domain.feed.SmartFeedEngine
import com.example.player.V2SMediaNotificationService
import com.example.player.VyloPlayerManager
import com.example.ui.components.V2SAlertPosition
import com.example.ui.components.WebLinkPlayback
import com.example.ui.components.checkHasMediaPermission
import com.example.util.MediaLinkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingDeleteRequest(
    val intentSenderRequest: IntentSenderRequest,
    val videoIds: List<Long> = emptyList(),
    val songIds: List<Long> = emptyList(),
    val onComplete: (Boolean) -> Unit = {}
)

enum class AppScreen(val title: String) {
    VIDEOS("Video"),
    SHORTS("Shorts"),
    SONGS("Audio"),
    LOCAL("Browse"),
    PLAYLISTS("Playlists"),
    SETTINGS("More"),
    NORMAL_PLAYER("Player");

    companion object {
        val HOME = VIDEOS
        val LIBRARY = VIDEOS
        val AUDIO = SONGS
        val BROWSE = LOCAL
        val MORE = SETTINGS
    }
}

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

enum class MediaMode {
    NORMAL,
    TESTING
}

data class VyloUiState(
    val currentScreen: AppScreen = AppScreen.VIDEOS,
    val mediaMode: MediaMode = MediaMode.NORMAL,
    val durationFilter: DurationFilter = DurationFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val searchQuery: String = "",
    val shortsFeed: List<VideoItem> = emptyList(),
    val activeShortsIndex: Int = 0,
    val normalPlayerVideo: VideoItem? = null,
    val normalPlayerQueue: List<VideoItem> = emptyList(),
    val songPlayerQueue: List<SongItem> = emptyList(),
    val activeFolder: FolderItem? = null,
    val isSongPlayerExpanded: Boolean = false,
    val isFloatingPipActive: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.YELLOW,

    // NEW: Global application font scale.
    // 1.0f = 100%, 0.8f = 80%, 1.5f = 150%.
    val fontScale: Float = 1.00f,
    val uiScale: Float = 1.00f,

    val autoAdvanceShorts: Boolean = false,
    val autoOpenWithShorts: Boolean = false,
    val shortsCompletionMode: ShortsCompletionMode = ShortsCompletionMode.CONTINUOUS,
    val showShortsProEndScreen: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val isScanning: Boolean = false,
    val hardwareAcceleration: Boolean = false,
    val multiplePlaybackEnabled: Boolean = false,
    val lyricsEnabled: Boolean = true,
    val autoFindLyrics: Boolean = true,
    val generateLyricsFromAudio: Boolean = true,
    val syncLyrics: Boolean = true,
    val saveGeneratedLyrics: Boolean = true,
    val alertPosition: V2SAlertPosition = V2SAlertPosition.TOP,
    val userNoticeMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appPreferences =
    application.getSharedPreferences(
        "v2s_preferences",
        android.content.Context.MODE_PRIVATE
    )

private val savedFontScale =
    appPreferences.getFloat("font_scale", 1.00f)
        .coerceIn(0.50f, 1.50f)

private val savedUiScale =
    appPreferences.getFloat("ui_scale", 1.00f)
        .let { stored ->
            // 1.11f was the old built-in default. Treat it as legacy default
            // so existing installs move to the new 100% / 140dp baseline.
            if (stored == 1.11f) 1.00f else stored
        }
        .coerceIn(0.80f, 1.40f)

    private val savedMediaMode = MediaMode.NORMAL

    private val savedThemeMode =
        runCatching {
            ThemeMode.valueOf(appPreferences.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        }.getOrDefault(ThemeMode.DARK)

    private val savedAccentColor =
        runCatching {
            AccentColor.valueOf(appPreferences.getString("accent_color", AccentColor.YELLOW.name) ?: AccentColor.YELLOW.name)
        }.getOrDefault(AccentColor.YELLOW)

    private val savedSortOrder =
        runCatching {
            SortOrder.valueOf(appPreferences.getString("sort_order", SortOrder.NEWEST.name) ?: SortOrder.NEWEST.name)
        }.getOrDefault(SortOrder.NEWEST)

    private val savedDurationFilter =
        runCatching {
            DurationFilter.valueOf(appPreferences.getString("duration_filter", DurationFilter.ALL.name) ?: DurationFilter.ALL.name)
        }.getOrDefault(DurationFilter.ALL)

    private val savedAlertPosition = V2SAlertPosition.TOP

    private val savedAutoOpenWithShorts =
        appPreferences.getBoolean("auto_open_with_shorts", false)
    private val savedAutoAdvanceShorts =
        appPreferences.getBoolean("auto_advance_shorts", false)
    private val savedHardwareAcceleration =
        appPreferences.getBoolean("hardware_acceleration", true)
    private val savedShortsCompletionMode =
        runCatching {
            ShortsCompletionMode.valueOf(
                appPreferences.getString("shorts_completion_mode", ShortsCompletionMode.CONTINUOUS.name)
                    ?: ShortsCompletionMode.CONTINUOUS.name
            )
        }.getOrDefault(ShortsCompletionMode.CONTINUOUS)

    // Lyrics are intentionally limited to predefined, app-provided lyrics.
    // No internet lookup and no audio-to-lyrics transcription are used.
    private val savedLyricsEnabled =
        appPreferences.getBoolean("lyrics_enabled", true)
    private val savedAutoFindLyrics = false
    private val savedGenerateLyrics = false
    private val savedSyncLyrics =
        appPreferences.getBoolean("sync_lyrics", true)
    private val savedSaveGeneratedLyrics = false

    private val playerPreferences =
        application.getSharedPreferences(
            "v2s_player_preferences",
            android.content.Context.MODE_PRIVATE
        )
    private val savedMultiplePlayback =
        playerPreferences.getBoolean("multiple_playback", false)
    private val database = VyloDatabase.getInstance(application)
    private val repository = VideoRepository(
        application,
        database.videoMetadataDao(),
        database.audioMetadataDao(),
        database.playlistDao(),
        database.userMediaLinkDao()
    )
    private val feedEngine = SmartFeedEngine()

    val playerManager = VyloPlayerManager.getOrCreate(
        context = application,
        onProgressUpdate = { videoId, positionMs ->
            viewModelScope.launch {
                repository.updatePlaybackPosition(videoId, positionMs)
            }
        },
        onPlaybackStarted = { videoId, positionMs ->
            viewModelScope.launch {
                repository.recordPlaybackStart(videoId, positionMs)
            }
        },
        onSongProgressUpdate = { songId, positionMs ->
            viewModelScope.launch {
                repository.updateSongPlaybackPosition(songId, positionMs)
            }
        },
        onSongPlaybackStarted = { songId, positionMs ->
            viewModelScope.launch {
                repository.recordSongPlaybackStart(songId, positionMs)
            }
        }
    )

    private val _uiState = MutableStateFlow(
        VyloUiState(
            currentScreen = if (savedAutoOpenWithShorts) AppScreen.SHORTS else AppScreen.VIDEOS,
            mediaMode = savedMediaMode,
            fontScale = savedFontScale,
            uiScale = savedUiScale,
            themeMode = savedThemeMode,
            accentColor = savedAccentColor,
            sortOrder = savedSortOrder,
            durationFilter = savedDurationFilter,
            autoOpenWithShorts = savedAutoOpenWithShorts,
            autoAdvanceShorts = savedAutoAdvanceShorts,
            shortsCompletionMode = savedShortsCompletionMode,
            hardwareAcceleration = savedHardwareAcceleration,
            multiplePlaybackEnabled = savedMultiplePlayback,
            lyricsEnabled = savedLyricsEnabled,
            autoFindLyrics = savedAutoFindLyrics,
            generateLyricsFromAudio = savedGenerateLyrics,
            syncLyrics = savedSyncLyrics,
            saveGeneratedLyrics = savedSaveGeneratedLyrics,
            alertPosition = savedAlertPosition
        )
    )
    val uiState: StateFlow<VyloUiState> = _uiState.asStateFlow()

    private val _pendingDeleteRequest = MutableStateFlow<PendingDeleteRequest?>(null)
    val pendingDeleteRequest = _pendingDeleteRequest.asStateFlow()

    private val _activeWebLink = MutableStateFlow<WebLinkPlayback?>(null)
    val activeWebLink = _activeWebLink.asStateFlow()

    fun closeWebLinkPlayer() {
        _activeWebLink.value = null
    }

    // Playback history stacks: tracks played items for smart back navigation
    private val songHistory = ArrayDeque<SongItem>()
    private val videoHistory = ArrayDeque<VideoItem>()
    private var isNavigatingHistory = false
    private var lastPreviousVideoClickTimeMs = 0L
    private var lastPreviousSongClickTimeMs = 0L

    // Reactive list of all videos
    val allVideos: StateFlow<List<VideoItem>> = repository.allVideosFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Reactive list of all songs
    val allSongs: StateFlow<List<SongItem>> = repository.allSongsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Reactive list of storage folders / locations
    val folders: StateFlow<List<FolderItem>> = repository.foldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive unified playlists list (Favorites, All Videos, All Songs, Recent + Custom User Playlists)
    val allPlaylists: StateFlow<List<PlaylistData>> = combine(
        allVideos,
        allSongs,
        repository.customPlaylistsFlow,
        repository.playlistItemsFlow
    ) { videos, songs, customPls, plItems ->
        val videoMap = videos.associateBy { it.id }
        val songMap = songs.associateBy { it.id }

        val videoMediaItems = videos.map { video ->
            PlaylistMediaItem(
                id = "v_${video.id}",
                mediaId = video.id,
                type = PlaylistMediaType.VIDEO,
                title = video.title,
                subtitle = "${video.resolutionLabel} • ${video.formattedDuration}",
                durationMs = video.durationMs,
                formattedDuration = video.formattedDuration,
                uri = video.uri,
                isFavorite = video.isFavorite,
                videoItem = video,
                resolutionLabel = video.resolutionLabel,
                formattedSize = video.formattedSize
            )
        }

        val songMediaItems = songs.map { song ->
            PlaylistMediaItem(
                id = "s_${song.id}",
                mediaId = song.id,
                type = PlaylistMediaType.AUDIO,
                title = song.title,
                subtitle = "${song.displaySubtitle} • ${song.formattedDuration}",
                durationMs = song.durationMs,
                formattedDuration = song.formattedDuration,
                uri = song.uri,
                albumArtUri = song.albumArtUri,
                isFavorite = song.isFavorite,
                songItem = song,
                formattedSize = ""
            )
        }

        // 1. Favorites playlist (both favorite videos and favorite songs)
        val favVideoItems = videoMediaItems.filter { it.isFavorite }
        val favSongItems = songMediaItems.filter { it.isFavorite }
        val allFavorites = favVideoItems + favSongItems

        val favoritesPlaylist = PlaylistData(
            id = "favorites",
            title = "Favorites",
            isSystem = true,
            isFavorites = true,
            category = "ALL",
            items = allFavorites
        )

        // 2. All Videos playlist
        val allVideosPlaylist = PlaylistData(
            id = "all_videos",
            title = "All Videos",
            isSystem = true,
            category = "VIDEOS",
            items = videoMediaItems
        )

        // 3. All Songs playlist
        val allSongsPlaylist = PlaylistData(
            id = "all_songs",
            title = "All Songs",
            isSystem = true,
            category = "SONGS",
            items = songMediaItems
        )

        // 4. History playlist (recently played videos & songs)
        val historyVideos = videoMediaItems.filter { (it.videoItem?.lastPlayedTimestamp ?: 0L) > 0L }
        val historySongs = songMediaItems.filter { (it.songItem?.lastPlayedTimestamp ?: 0L) > 0L }
        val allHistory = (historyVideos + historySongs).sortedByDescending {
            val vTime = it.videoItem?.lastPlayedTimestamp ?: 0L
            val sTime = it.songItem?.lastPlayedTimestamp ?: 0L
            maxOf(vTime, sTime)
        }

        val historyPlaylist = PlaylistData(
            id = "history",
            title = "History",
            isSystem = true,
            category = "ALL",
            items = allHistory
        )

        val defaultPlaylists = listOf(favoritesPlaylist, allVideosPlaylist, allSongsPlaylist, historyPlaylist)

        // 5. User Custom Playlists from Room DB
        val customPlaylists = customPls.map { plEntity ->
            val itemsForPl = plItems.filter { it.playlistId == plEntity.id }
            val resolvedItems = itemsForPl.mapNotNull { itemEntity ->
                if (itemEntity.mediaType == PlaylistMediaType.VIDEO.name) {
                    val vid = videoMap[itemEntity.mediaId]
                    vid?.let { video ->
                        PlaylistMediaItem(
                            id = "v_${video.id}",
                            mediaId = video.id,
                            type = PlaylistMediaType.VIDEO,
                            title = video.title,
                            subtitle = "${video.resolutionLabel} • ${video.formattedDuration}",
                            durationMs = video.durationMs,
                            formattedDuration = video.formattedDuration,
                            uri = video.uri,
                            isFavorite = video.isFavorite,
                            videoItem = video,
                            resolutionLabel = video.resolutionLabel,
                            formattedSize = video.formattedSize
                        )
                    }
                } else {
                    val sng = songMap[itemEntity.mediaId]
                    sng?.let { song ->
                        PlaylistMediaItem(
                            id = "s_${song.id}",
                            mediaId = song.id,
                            type = PlaylistMediaType.AUDIO,
                            title = song.title,
                            subtitle = "${song.displaySubtitle} • ${song.formattedDuration}",
                            durationMs = song.durationMs,
                            formattedDuration = song.formattedDuration,
                            uri = song.uri,
                            albumArtUri = song.albumArtUri,
                            isFavorite = song.isFavorite,
                            songItem = song,
                            formattedSize = ""
                        )
                    }
                }
            }

            PlaylistData(
                id = plEntity.id,
                title = plEntity.title,
                isSystem = false,
                category = plEntity.category,
                items = resolvedItems,
                createdAt = plEntity.createdAt
            )
        }

        // Custom user playlists on top, 4 default system playlists always at bottom
        customPlaylists + defaultPlaylists
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & sorted videos for Videos tab
    val libraryVideos: StateFlow<List<VideoItem>> = combine(
        allVideos,
        _uiState
    ) { videos, state ->
        var list = videos
        if (state.searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(state.searchQuery, ignoreCase = true) ||
                it.displayName.contains(state.searchQuery, ignoreCase = true)
            }
        }
        when (state.sortOrder) {
            SortOrder.NAME_ASC -> list.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOrder.FILENAME_ASC -> list.sortedBy { it.displayName.lowercase() }
            SortOrder.FILENAME_DESC -> list.sortedByDescending { it.displayName.lowercase() }
            SortOrder.DURATION_SHORT -> list.sortedBy { it.durationMs }
            SortOrder.DURATION_LONG -> list.sortedByDescending { it.durationMs }
            SortOrder.DATE_OLD -> list.sortedBy { it.dateAddedSec }
            SortOrder.DATE_NEW -> list.sortedByDescending { it.dateAddedSec }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Continue watching: videos with progress > 3s
    val continueWatchingVideos: StateFlow<List<VideoItem>> = allVideos.map { videos ->
        videos.filter { it.lastPositionMs > 3000L && (it.durationMs == 0L || it.lastPositionMs < it.durationMs - 4000L) }
            .sortedByDescending { it.lastPlayedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recently watched videos (full history sorted by latest timestamp)
    val recentlyWatchedVideos: StateFlow<List<VideoItem>> = allVideos.map { videos ->
        videos.filter { it.lastPlayedTimestamp > 0L }
            .sortedByDescending { it.lastPlayedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recently played songs, persisted through the audio metadata table.
    val recentlyWatchedSongs: StateFlow<List<SongItem>> = allSongs.map { songs ->
        songs.filter { it.lastPlayedTimestamp > 0L }
            .sortedByDescending { it.lastPlayedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Notification-safe next video action. Falls back to the library queue when no normal-player queue exists. */
    private fun playNextVideoFromNotification() {
        if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
            playNextInNormalPlayer()
            return
        }

        val current = playerManager.playerState.value.currentVideo ?: return
        val videos = allVideos.value
        val index = videos.indexOfFirst { it.id == current.id }
        if (index >= 0 && index < videos.lastIndex) {
            playerManager.playVideo(videos[index + 1], startPositionMs = videos[index + 1].lastPositionMs, autoPlay = true)
        }
    }

    /** Notification-safe previous video action. */
    private fun playPreviousVideoFromNotification() {
        playPreviousVideo()
    }

    // Recently added
    val recentlyAddedVideos: StateFlow<List<VideoItem>> = allVideos.map { videos ->
        videos.sortedByDescending { it.dateAddedSec }.take(15)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites
    val favoriteVideos: StateFlow<List<VideoItem>> = allVideos.map { videos ->
        videos.filter { it.isFavorite }.sortedByDescending { it.dateAddedSec }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
       V2SMediaNotificationService.actionListener =
    object :
        V2SMediaNotificationService.NotificationActionListener {

        override fun onPlay() {
            playerManager.resume()
        }

        override fun onPause() {
            playerManager.pause()
        }

        override fun onNext() {
            val state =
                playerManager.playerState.value

            if (state.isAudioOnly) {
                playNextSong()
            } else {
                playNextVideoFromNotification()
            }
        }

        override fun onSeekBackward() {
            playerManager.seekBackward(10_000L)
        }

        override fun onSeekForward() {
            playerManager.seekForward(10_000L)
        }

        override fun onPrevious() {
            val state =
                playerManager.playerState.value

            if (state.isAudioOnly || state.currentSong != null) {
                playPreviousSong()
            } else {
                playPreviousVideo()
            }
        }

        override fun onStop() {
            playerManager.stopMedia()
        }

        override fun onVolumeUp() {
            playerManager.increaseVolume()
        }

        override fun onVolumeDown() {
            playerManager.decreaseVolume()
        }
    }

        playerManager.onPlayNextMedia = {
            val state = playerManager.playerState.value
            if (_uiState.value.currentScreen == AppScreen.SHORTS) {
                onShortsPlaybackEnded()
            } else if (state.isAudioOnly || state.currentSong != null) {
                playNextSong()
            } else {
                playNextVideo()
            }
        }

        playerManager.onPlayPreviousMedia = {
            val state = playerManager.playerState.value
            if (_uiState.value.currentScreen == AppScreen.SHORTS) {
                val feed = _uiState.value.shortsFeed
                if (feed.isNotEmpty()) {
                    val prevIndex = (_uiState.value.activeShortsIndex - 1 + feed.size) % feed.size
                    onShortsPageChanged(prevIndex)
                }
            } else if (state.isAudioOnly || state.currentSong != null) {
                playPreviousSong()
            } else {
                playPreviousVideo()
            }
        }

        playerManager.onMediaPlaybackCompleted = {
            if (_uiState.value.currentScreen == AppScreen.SHORTS) {
                onShortsPlaybackEnded()
            } else {
                if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
                    _uiState.value = _uiState.value.copy(normalPlayerVideo = null)
                    navigateTo(AppScreen.VIDEOS)
                }
                if (_uiState.value.isSongPlayerExpanded) {
                    _uiState.value = _uiState.value.copy(isSongPlayerExpanded = false)
                }
                _uiState.value = _uiState.value.copy(isFloatingPipActive = false)
            }
        }

        val hasMediaAccess = checkHasMediaPermission(application)
        if (hasMediaAccess) {
            _uiState.value = _uiState.value.copy(hasStoragePermission = true)
        }
        refreshMediaScan()

        viewModelScope.launch {
            allVideos.collect { videos ->
                if (_uiState.value.normalPlayerQueue.isEmpty() && videos.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(normalPlayerQueue = videos)
                }
                regenerateShortsFeed(videos, _uiState.value.durationFilter, _uiState.value.sortOrder)
                if (videos.isNotEmpty() &&
                    _uiState.value.autoOpenWithShorts &&
                    _uiState.value.currentScreen == AppScreen.SHORTS &&
                    playerManager.playerState.value.currentVideo == null
                ) {
                    val first = _uiState.value.shortsFeed.firstOrNull() ?: videos.first()
                    if (!MediaLinkUtils.isWebEmbedLink(first.uri.toString())) {
                        playerManager.playVideo(first, autoPlay = true)
                    }
                }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasStoragePermission = granted)
        if (granted) {
            refreshMediaScan()
        }
    }

    fun setMediaMode(@Suppress("UNUSED_PARAMETER") mode: MediaMode) {
        appPreferences.edit().putString("media_mode", MediaMode.NORMAL.name).apply()
        _uiState.value = _uiState.value.copy(mediaMode = MediaMode.NORMAL)
        refreshMediaScan()
    }

    fun addUserMediaLink(
        url: String,
        title: String,
        mediaType: String,
        artistOrSource: String
    ) {
        viewModelScope.launch {
            val saved = repository.addUserMediaLink(url, title, mediaType, artistOrSource)
            _uiState.value = _uiState.value.copy(
                userNoticeMessage = "Saved \"${saved.title}\" locally"
            )
        }
    }

    fun updateUserMediaLink(
        mediaId: Long,
        url: String,
        title: String,
        mediaType: String,
        artistOrSource: String
    ) {
        viewModelScope.launch {
            val updated = repository.updateUserMediaLink(mediaId, url, title, mediaType, artistOrSource)
            if (updated != null) {
                val currentWeb = _activeWebLink.value
                if (currentWeb != null && currentWeb.mediaId == mediaId) {
                    _activeWebLink.value = currentWeb.copy(
                        title = updated.title,
                        url = updated.url,
                        sourceName = updated.sourceName,
                        isShort = updated.mediaType == "SHORT" || MediaLinkUtils.isYouTubeShortsUrl(updated.url)
                    )
                }
                _uiState.value = _uiState.value.copy(
                    userNoticeMessage = "Updated \"${updated.title}\""
                )
            }
        }
    }

    fun deleteUserLinkByMediaId(mediaId: Long) {
        viewModelScope.launch {
            repository.deleteUserMediaLinksByMediaIds(listOf(mediaId))
            repository.cleanupDeletedVideos(listOf(mediaId))
            repository.cleanupDeletedSongs(listOf(mediaId))
            if (_activeWebLink.value?.mediaId == mediaId) {
                _activeWebLink.value = null
            }
            _uiState.value = _uiState.value.copy(
                userNoticeMessage = "Removed link from library"
            )
        }
    }

    fun refreshMediaScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            repository.scanMedia(testingMode = false)
            _uiState.value = _uiState.value.copy(isScanning = false)
            regenerateShortsFeed(allVideos.value, _uiState.value.durationFilter, _uiState.value.sortOrder)
        }
    }

    private val screenBackStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        val current = _uiState.value.currentScreen

        // Shorts owns its video playback. When leaving Shorts for another
        // screen, stop that playback so the persistent bottom mini-player
        // is not created when Shorts is closed.
        if (current == AppScreen.SHORTS && screen != AppScreen.SHORTS) {
            playerManager.stopMedia()
        }

        if (addToBackStack && current != screen) {
            screenBackStack.add(current)
        }
        if (screen == AppScreen.SHORTS) {
            if (_uiState.value.shortsFeed.isEmpty() && allVideos.value.isNotEmpty()) {
                regenerateShortsFeed(allVideos.value, _uiState.value.durationFilter)
            }
            val feed = _uiState.value.shortsFeed.ifEmpty { allVideos.value }
            val index = _uiState.value.activeShortsIndex.coerceIn(0, (feed.size - 1).coerceAtLeast(0))
            if (feed.isNotEmpty()) {
                val targetVideo = feed[index]
                if (!MediaLinkUtils.isWebEmbedLink(targetVideo.uri.toString()) &&
                    playerManager.playerState.value.currentVideo?.id != targetVideo.id
                ) {
                    playerManager.playVideo(targetVideo, autoPlay = true)
                }
            }
        }
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun getRootScreenForActiveMedia(): AppScreen {
        val state = playerManager.playerState.value
        return if (state.currentSong != null || state.isAudioOnly) {
            AppScreen.SONGS
        } else {
            AppScreen.VIDEOS
        }
    }

    fun navigateBack(): Boolean {
        if (_uiState.value.currentScreen == AppScreen.SHORTS) {
            // Closing Shorts must not leave its current video in the
            // persistent mini-player at the bottom of the next screen.
            playerManager.stopMedia()
        }

        if (screenBackStack.isNotEmpty()) {
            val previousScreen = screenBackStack.removeAt(screenBackStack.size - 1)
            navigateTo(previousScreen, addToBackStack = false)
            return true
        }

        val rootScreen = getRootScreenForActiveMedia()
        if (_uiState.value.currentScreen != rootScreen) {
            navigateTo(rootScreen, addToBackStack = false)
            return true
        }
        return false
    }

    fun canNavigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) return true
        val rootScreen = getRootScreenForActiveMedia()
        return _uiState.value.currentScreen != rootScreen
    }

    fun restoreVideoPlayer() {
        val state = playerManager.playerState.value
        if (state.currentSong != null && state.currentVideo == null) {
            restoreSongPlayer()
            return
        }
        val video = state.currentVideo ?: _uiState.value.normalPlayerVideo
        if (video != null) {
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.NORMAL_PLAYER,
                normalPlayerVideo = video,
                isFloatingPipActive = false,
                isSongPlayerExpanded = false
            )
        }
    }

    fun restoreSongPlayer() {
        val state = playerManager.playerState.value
        if (state.currentVideo != null && state.currentSong == null) {
            restoreVideoPlayer()
            return
        }
        val song = state.currentSong
        if (song != null) {
            _uiState.value = _uiState.value.copy(
                isSongPlayerExpanded = true,
                normalPlayerVideo = null,
                isFloatingPipActive = false
            )
        } else if (allSongs.value.isNotEmpty()) {
            val first = allSongs.value.first()
            playSong(first)
            _uiState.value = _uiState.value.copy(
                isSongPlayerExpanded = true,
                normalPlayerVideo = null,
                isFloatingPipActive = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                userNoticeMessage = "No song is currently playing"
            )
        }
    }

    fun setFloatingPipActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isFloatingPipActive = active)
    }

    fun playVideoInPip(
        video: VideoItem,
        queue: List<VideoItem> = emptyList(),
        startFromBeginning: Boolean = false
    ) {
        val currentPlayingVideo = playerManager.playerState.value.currentVideo

        if (!isNavigatingHistory) {
            val prev = currentPlayingVideo ?: _uiState.value.normalPlayerVideo
            if (prev != null && prev.id != video.id) {
                videoHistory.addLast(prev)
                if (videoHistory.size > 50) videoHistory.removeFirst()
            }
        }

        val startPos = if (startFromBeginning) {
            0L
        } else when {
            currentPlayingVideo?.id == video.id ->
                playerManager.playerState.value.currentPositionMs
            video.lastPositionMs > 0 && video.durationMs > 0 && video.lastPositionMs < video.durationMs - 3000L ->
                video.lastPositionMs
            else ->
                0L
        }
        _uiState.value = _uiState.value.copy(
            normalPlayerVideo = video,
            normalPlayerQueue = if (queue.isNotEmpty()) queue else allVideos.value,
            isFloatingPipActive = true
        )
        playerManager.playVideo(video, startPositionMs = startPos, autoPlay = true)
    }

    fun closeMiniPlayer() {
        _uiState.value = _uiState.value.copy(isFloatingPipActive = false)
        playerManager.stopMedia()
    }

    fun setDurationFilter(filter: DurationFilter) {
        if (_uiState.value.durationFilter != filter) {
            appPreferences.edit().putString("duration_filter", filter.name).apply()
            _uiState.value = _uiState.value.copy(
                durationFilter = filter,
                activeShortsIndex = 0
            )
            regenerateShortsFeed(allVideos.value, filter, _uiState.value.sortOrder)
        }
    }

    fun setSortOrder(order: SortOrder) {
        if (_uiState.value.sortOrder != order) {
            appPreferences.edit().putString("sort_order", order.name).apply()
            _uiState.value = _uiState.value.copy(sortOrder = order)
            if (_uiState.value.currentScreen == AppScreen.SHORTS) {
                regenerateShortsFeed(allVideos.value, _uiState.value.durationFilter, order)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

/** Resume whatever media is currently active, otherwise resume the most recently played item. */
fun playLastPlayedMedia() {
    val current = playerManager.playerState.value
    if (current.currentVideo != null || current.currentSong != null) {
        playerManager.resume()
        return
    }

    val lastVideo = allVideos.value
        .filter { it.lastPlayedTimestamp > 0L }
        .maxByOrNull { it.lastPlayedTimestamp }
    val lastSong = allSongs.value
        .filter { it.lastPlayedTimestamp > 0L }
        .maxByOrNull { it.lastPlayedTimestamp }

    val videoTime = lastVideo?.lastPlayedTimestamp ?: 0L
    val songTime = lastSong?.lastPlayedTimestamp ?: 0L

    when {
        lastVideo != null && videoTime >= songTime ->
            playerManager.playVideo(lastVideo, startPositionMs = lastVideo.lastPositionMs, autoPlay = true)
        lastSong != null ->
            playerManager.playSong(lastSong, startPositionMs = lastSong.lastPositionMs, autoPlay = true)
        else ->
            _uiState.value = _uiState.value.copy(userNoticeMessage = "No previously played media yet")
    }
}

/** Resume only the most recently played video. Used by the Videos tab. */
fun playLastPlayedVideo() {
    val currentVideo = playerManager.playerState.value.currentVideo
    if (currentVideo != null) {
        playerManager.resume()
        return
    }

    val lastVideo = allVideos.value
        .filter { it.lastPlayedTimestamp > 0L }
        .maxByOrNull { it.lastPlayedTimestamp }

    if (lastVideo != null) {
        openNormalPlayer(lastVideo, allVideos.value)
    } else {
        _uiState.value = _uiState.value.copy(userNoticeMessage = "No previously played video yet")
    }
}

/** Resume only the most recently played song. Used by the Songs tab. */
fun playLastPlayedSong() {
    val currentSong = playerManager.playerState.value.currentSong
    if (currentSong != null) {
        playerManager.resume()
        return
    }

    val lastSong = allSongs.value
        .filter { it.lastPlayedTimestamp > 0L }
        .maxByOrNull { it.lastPlayedTimestamp }

    if (lastSong != null) {
        playerManager.playSong(lastSong, startPositionMs = lastSong.lastPositionMs, autoPlay = true)
    } else {
        _uiState.value = _uiState.value.copy(userNoticeMessage = "No previously played song yet")
    }
}

fun setFontScale(scale: Float) {
    val safeScale = scale
        .coerceIn(0.50f, 1.50f)
        .let { (it * 20f).toInt() / 20f.toFloat() }

    if (_uiState.value.fontScale == safeScale) {
        return
    }

    appPreferences.edit()
        .putFloat("font_scale", safeScale)
        .apply()

    _uiState.value = _uiState.value.copy(
        fontScale = safeScale
    )
}

fun resetFontScale() {
    setFontScale(1.00f)
}

fun setUiScale(scale: Float) {
    val safeScale = scale
        .coerceIn(0.80f, 1.40f)
        .let { (it * 100f).toInt() / 100f }

    if (_uiState.value.uiScale == safeScale) return

    appPreferences.edit()
        .putFloat("ui_scale", safeScale)
        .apply()

    _uiState.value = _uiState.value.copy(uiScale = safeScale)
}

fun resetUiScale() {
    setUiScale(1.00f)
}

    fun setAccentColor(color: AccentColor) {
        appPreferences.edit().putString("accent_color", color.name).apply()
        _uiState.value = _uiState.value.copy(accentColor = color)
    }

    fun setThemeMode(mode: ThemeMode) {
        appPreferences.edit().putString("theme_mode", mode.name).apply()
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun toggleAutoAdvanceShorts() {
        val enabled = !_uiState.value.autoAdvanceShorts
        appPreferences.edit().putBoolean("auto_advance_shorts", enabled).apply()
        _uiState.value = _uiState.value.copy(autoAdvanceShorts = enabled)
    }

    fun toggleAutoOpenWithShorts() {
        setAutoOpenWithShorts(!_uiState.value.autoOpenWithShorts)
    }

    fun setAutoOpenWithShorts(enabled: Boolean) {
        appPreferences.edit()
            .putBoolean("auto_open_with_shorts", enabled)
            .apply()

        _uiState.value = _uiState.value.copy(
            autoOpenWithShorts = enabled
        )
    }

    fun playVideoAuto(video: VideoItem, queue: List<VideoItem> = emptyList()) {
        if (_uiState.value.autoOpenWithShorts) {
            startShortsFromVideo(video)
        } else {
            openNormalPlayer(video, queue)
        }
    }

    fun toggleHardwareAcceleration() {
        val enabled = !_uiState.value.hardwareAcceleration
        appPreferences.edit().putBoolean("hardware_acceleration", enabled).apply()
        _uiState.value = _uiState.value.copy(hardwareAcceleration = enabled)
        playerManager.setHardwareAcceleration(enabled)
    }

    fun setMultiplePlaybackEnabled(enabled: Boolean) {
        playerManager.setMultiplePlaybackEnabled(enabled)
        _uiState.value = _uiState.value.copy(multiplePlaybackEnabled = enabled)
    }

    fun toggleMultiplePlayback() {
        setMultiplePlaybackEnabled(!_uiState.value.multiplePlaybackEnabled)
    }

    fun selectFolder(folder: FolderItem?) {
        _uiState.value = _uiState.value.copy(activeFolder = folder)
    }

    fun onShortsPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(activeShortsIndex = page)
        val feed = _uiState.value.shortsFeed
        if (page in feed.indices) {
            val video = feed[page]
            feedEngine.markVideoSeen(video.id)
            playerManager.playVideo(video, autoPlay = true)
        }
    }

    fun setShortsCompletionMode(mode: ShortsCompletionMode) {
        appPreferences.edit().putString("shorts_completion_mode", mode.name).apply()
        _uiState.value = _uiState.value.copy(shortsCompletionMode = mode)
    }

    fun dismissShortsProEndScreen() {
        _uiState.value = _uiState.value.copy(showShortsProEndScreen = false)
    }

    fun onShortsPlaybackEnded() {
        val feed = _uiState.value.shortsFeed
        if (feed.isEmpty()) return
        val currentIndex = _uiState.value.activeShortsIndex.coerceIn(0, feed.lastIndex)
        when (_uiState.value.shortsCompletionMode) {
            ShortsCompletionMode.REPEAT_ONE -> {
                playerManager.seekTo(0L)
                playerManager.play()
            }
            ShortsCompletionMode.CONTINUOUS -> {
                val nextIndex = (currentIndex + 1) % feed.size
                onShortsPageChanged(nextIndex)
            }
            ShortsCompletionMode.RANDOM_SHUFFLE -> {
                val candidates = feed.indices.filter { it != currentIndex }
                val nextIndex = if (candidates.isNotEmpty()) candidates.random() else 0
                onShortsPageChanged(nextIndex)
            }
            ShortsCompletionMode.PROFESSIONAL_END -> {
                _uiState.value = _uiState.value.copy(showShortsProEndScreen = true)
            }
        }
    }

    fun startShortsFromVideo(video: VideoItem) {
        val urlStr = video.uri.toString()
        if (MediaLinkUtils.isWebEmbedLink(urlStr)) {
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Extracting stream for \"${video.title}\"...")
            viewModelScope.launch {
                val extracted = MediaLinkUtils.extractPlayableStream(urlStr, preferAudioOnly = false)
                if (extracted != null) {
                    val resolvedVideo = video.copy(
                        uri = Uri.parse(extracted.streamUrl),
                        mimeType = extracted.mimeType ?: video.mimeType
                    )
                    launchShortsInExoPlayer(resolvedVideo)
                } else {
                    playerManager.pause()
                    _activeWebLink.value = WebLinkPlayback(
                        title = video.title,
                        url = urlStr,
                        sourceName = MediaLinkUtils.inferSourceName(urlStr),
                        isShort = true,
                        mediaId = video.id
                    )
                }
            }
            return
        }
        launchShortsInExoPlayer(video)
    }

    private fun launchShortsInExoPlayer(video: VideoItem) {
        val current = _uiState.value.currentScreen
        if (current != AppScreen.SHORTS) {
            screenBackStack.add(current)
        }
        val currentFeed = _uiState.value.shortsFeed.toMutableList()
        val index = currentFeed.indexOfFirst { it.id == video.id }
        if (index >= 0) {
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.SHORTS,
                activeShortsIndex = index
            )
        } else {
            val newFeed = listOf(video) + currentFeed.filter { it.id != video.id }
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.SHORTS,
                shortsFeed = newFeed,
                activeShortsIndex = 0
            )
        }
        playerManager.playVideo(video, autoPlay = true)
    }

    fun videoToSongItem(video: VideoItem): SongItem {
        val folder = if (video.parentFolderName.isNotBlank()) video.parentFolderName else "Video"
        return SongItem(
            id = video.id,
            uri = video.uri,
            title = video.title,
            artist = folder,
            album = "Video Audio",
            durationMs = video.durationMs,
            sizeBytes = video.sizeBytes,
            path = video.path,
            albumArtUri = video.uri,
            lastPositionMs = video.lastPositionMs,
            isFavorite = video.isFavorite
        )
    }

    fun playVideoAsAudio(video: VideoItem) {
        val urlStr = video.uri.toString()
        if (MediaLinkUtils.isWebEmbedLink(urlStr)) {
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Extracting audio from \"${video.title}\"...")
            viewModelScope.launch {
                val extracted = MediaLinkUtils.extractPlayableStream(urlStr, preferAudioOnly = true)
                val targetUri = extracted?.audioStreamUrl ?: extracted?.streamUrl
                if (targetUri != null) {
                    val songEquivalent = videoToSongItem(
                        video.copy(uri = Uri.parse(targetUri), mimeType = extracted.mimeType ?: video.mimeType)
                    )
                    launchVideoAsAudioPlayer(songEquivalent, video.lastPositionMs, video.uri)
                } else {
                    playerManager.pause()
                    _activeWebLink.value = WebLinkPlayback(
                        title = video.title,
                        url = urlStr,
                        sourceName = MediaLinkUtils.inferSourceName(urlStr),
                        isShort = false,
                        mediaId = video.id
                    )
                }
            }
            return
        }
        val songEquivalent = videoToSongItem(video)
        launchVideoAsAudioPlayer(songEquivalent, video.lastPositionMs, video.uri)
    }

    private fun launchVideoAsAudioPlayer(
        songEquivalent: SongItem,
        lastPosMs: Long,
        artworkUri: Uri
    ) {
        _uiState.value = _uiState.value.copy(
            normalPlayerVideo = null,
            isFloatingPipActive = false
        )
        if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
            navigateTo(AppScreen.VIDEOS)
        }
        playerManager.playSong(
            song = songEquivalent,
            startPositionMs = if (lastPosMs > 0) lastPosMs else 0L,
            autoPlay = true,
            asVideoAudio = true,
            notificationArtworkUri = artworkUri
        )
        _uiState.value = _uiState.value.copy(
            isSongPlayerExpanded = true,
            userNoticeMessage = "Playing \"${songEquivalent.title}\" as audio"
        )
    }

    fun playCurrentVideoAsAudio() {
        val currentVideo = uiState.value.normalPlayerVideo ?: playerManager.playerState.value.currentVideo
        if (currentVideo != null) {
            playVideoAsAudio(currentVideo)
        } else {
            _uiState.value = _uiState.value.copy(
                normalPlayerVideo = null,
                isFloatingPipActive = false
            )
            playerManager.playCurrentVideoAsAudio()
            if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
                navigateTo(AppScreen.VIDEOS)
            }
            _uiState.value = _uiState.value.copy(
                isSongPlayerExpanded = true
            )
        }
    }

    fun openNormalPlayer(
        video: VideoItem,
        queue: List<VideoItem> = emptyList(),
        startFromBeginning: Boolean = false
    ) {
        val urlStr = video.uri.toString()
        if (MediaLinkUtils.isWebEmbedLink(urlStr)) {
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Extracting stream for \"${video.title}\"...")
            viewModelScope.launch {
                val extracted = MediaLinkUtils.extractPlayableStream(urlStr, preferAudioOnly = false)
                if (extracted != null) {
                    val resolvedVideo = video.copy(
                        uri = Uri.parse(extracted.streamUrl),
                        mimeType = extracted.mimeType ?: video.mimeType
                    )
                    launchNormalPlayerInExoPlayer(resolvedVideo, queue, startFromBeginning)
                } else {
                    playerManager.pause()
                    _activeWebLink.value = WebLinkPlayback(
                        title = video.title,
                        url = urlStr,
                        sourceName = MediaLinkUtils.inferSourceName(urlStr),
                        isShort = video.path?.contains("/SHORT/") == true || MediaLinkUtils.isYouTubeShortsUrl(urlStr),
                        mediaId = video.id
                    )
                }
            }
            return
        }
        launchNormalPlayerInExoPlayer(video, queue, startFromBeginning)
    }

    private fun launchNormalPlayerInExoPlayer(
        video: VideoItem,
        queue: List<VideoItem>,
        startFromBeginning: Boolean
    ) {
        val current = _uiState.value.currentScreen
        if (current != AppScreen.NORMAL_PLAYER) {
            screenBackStack.add(current)
        }
        val currentPlayingVideo = playerManager.playerState.value.currentVideo

        if (!isNavigatingHistory) {
            val prev = currentPlayingVideo ?: _uiState.value.normalPlayerVideo
            if (prev != null && prev.id != video.id) {
                videoHistory.addLast(prev)
                if (videoHistory.size > 50) videoHistory.removeFirst()
            }
        }

        val startPos = if (startFromBeginning) {
            0L
        } else when {
            currentPlayingVideo?.id == video.id ->
                playerManager.playerState.value.currentPositionMs
            video.lastPositionMs > 0 ->
                video.lastPositionMs
            else ->
                0L
        }
        val effectiveQueue = when {
            queue.size > 1 -> queue
            allVideos.value.isNotEmpty() -> allVideos.value
            queue.isNotEmpty() -> queue
            else -> listOf(video)
        }
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.NORMAL_PLAYER,
            normalPlayerVideo = video,
            normalPlayerQueue = effectiveQueue,
            isSongPlayerExpanded = false,
            isFloatingPipActive = false
        )
        playerManager.playVideo(video, startPositionMs = startPos, autoPlay = true)
    }

    fun playNextVideo() {
        val currentVideo = playerManager.playerState.value.currentVideo ?: _uiState.value.normalPlayerVideo ?: return
        val queue = if (_uiState.value.normalPlayerQueue.isNotEmpty()) _uiState.value.normalPlayerQueue else allVideos.value
        if (queue.isEmpty()) return
        val state = playerManager.playerState.value

        if (state.isShuffle) {
            val candidates = queue.filter { it.id != currentVideo.id }
            if (candidates.isNotEmpty()) {
                val nextVideo = candidates.random()
                if (_uiState.value.isFloatingPipActive) {
                    playVideoInPip(nextVideo, queue, startFromBeginning = true)
                } else {
                    openNormalPlayer(nextVideo, queue, startFromBeginning = true)
                }
                return
            }
        }

        val currentIndex = queue.indexOfFirst { it.id == currentVideo.id }
        if (currentIndex in 0 until queue.size - 1) {
            val nextVideo = queue[currentIndex + 1]
            if (_uiState.value.isFloatingPipActive) {
                playVideoInPip(nextVideo, queue, startFromBeginning = true)
            } else {
                openNormalPlayer(nextVideo, queue, startFromBeginning = true)
            }
        } else if (state.repeatMode == androidx.media3.common.Player.REPEAT_MODE_ALL) {
            val nextVideo = queue.first()
            if (_uiState.value.isFloatingPipActive) {
                playVideoInPip(nextVideo, queue, startFromBeginning = true)
            } else {
                openNormalPlayer(nextVideo, queue, startFromBeginning = true)
            }
        } else {
            playerManager.stopMedia()
            if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
                _uiState.value = _uiState.value.copy(normalPlayerVideo = null)
                navigateTo(AppScreen.VIDEOS)
            }
        }
    }

    fun playPreviousVideo() {
        val currentVideo = playerManager.playerState.value.currentVideo ?: _uiState.value.normalPlayerVideo ?: return
        val currentPos = playerManager.playerState.value.currentPositionMs
        val now = System.currentTimeMillis()
        val isQuickConsecutiveClick = (now - lastPreviousVideoClickTimeMs) < 2000L
        lastPreviousVideoClickTimeMs = now

        // If playing video past 3 seconds and not a quick consecutive click, restart current video from beginning
        if (currentPos > 3000L && !isQuickConsecutiveClick) {
            playerManager.seekTo(0L)
            if (playerManager.playerState.value.isPlaying) {
                playerManager.play()
            }
            return
        }

        val queue = if (_uiState.value.normalPlayerQueue.isNotEmpty()) _uiState.value.normalPlayerQueue else allVideos.value
        if (queue.isEmpty()) return
        val state = playerManager.playerState.value

        // If user selected shuffle mode: go back according to shuffle mode
        if (state.isShuffle) {
            while (videoHistory.isNotEmpty()) {
                val lastVideo = videoHistory.removeLast()
                if (lastVideo.id != currentVideo.id) {
                    isNavigatingHistory = true
                    try {
                        if (_uiState.value.isFloatingPipActive) {
                            playVideoInPip(lastVideo, queue, startFromBeginning = true)
                        } else {
                            openNormalPlayer(lastVideo, queue, startFromBeginning = true)
                        }
                    } finally {
                        isNavigatingHistory = false
                    }
                    return
                }
            }

            // Fallback when no previous history: pick a random video from queue
            val candidates = queue.filter { it.id != currentVideo.id }
            val nextVideo = if (candidates.isNotEmpty()) candidates.random() else queue.first()
            if (_uiState.value.isFloatingPipActive) {
                playVideoInPip(nextVideo, queue, startFromBeginning = true)
            } else {
                openNormalPlayer(nextVideo, queue, startFromBeginning = true)
            }
            return
        }

        // Shuffle is OFF: navigate sequentially (checking history first)
        while (videoHistory.isNotEmpty()) {
            val lastVideo = videoHistory.removeLast()
            if (lastVideo.id != currentVideo.id) {
                isNavigatingHistory = true
                try {
                    if (_uiState.value.isFloatingPipActive) {
                        playVideoInPip(lastVideo, queue, startFromBeginning = true)
                    } else {
                        openNormalPlayer(lastVideo, queue, startFromBeginning = true)
                    }
                } finally {
                    isNavigatingHistory = false
                }
                return
            }
        }

        // Fallback when no previous history: play the sequentially previous video in the list
        val currentIndex = queue.indexOfFirst { it.id == currentVideo.id }
        val prevVideo = if (currentIndex > 0) {
            queue[currentIndex - 1]
        } else {
            queue.last()
        }
        if (_uiState.value.isFloatingPipActive) {
            playVideoInPip(prevVideo, queue, startFromBeginning = true)
        } else {
            openNormalPlayer(prevVideo, queue, startFromBeginning = true)
        }
    }

    fun seekRelativeVideo(offsetMs: Long) {
        val currentPos = playerManager.playerState.value.currentPositionMs
        val duration = playerManager.playerState.value.durationMs
        val targetPos = (currentPos + offsetMs).coerceIn(0L, if (duration > 0) duration else Long.MAX_VALUE)
        playerManager.seekTo(targetPos)
    }

    fun playNextInNormalPlayer() {
        playNextVideo()
    }

    fun playPreviousInNormalPlayer() {
        playPreviousVideo()
    }

    // Audio Playback
    fun playSong(
        song: SongItem,
        queue: List<SongItem> = emptyList(),
        startFromBeginning: Boolean = true,
        asVideoAudio: Boolean = false
    ) {
        val urlStr = song.uri.toString()
        if (MediaLinkUtils.isWebEmbedLink(urlStr)) {
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Extracting audio for \"${song.title}\"...")
            viewModelScope.launch {
                val extracted = MediaLinkUtils.extractPlayableStream(urlStr, preferAudioOnly = true)
                val targetUri = extracted?.audioStreamUrl ?: extracted?.streamUrl
                if (targetUri != null) {
                    val resolvedSong = song.copy(
                        uri = Uri.parse(targetUri),
                        mimeType = extracted.mimeType ?: song.mimeType,
                        albumArtUri = extracted.thumbnailUrl?.let(Uri::parse) ?: song.albumArtUri
                    )
                    launchSongInExoPlayer(resolvedSong, queue, startFromBeginning, asVideoAudio)
                } else {
                    playerManager.pause()
                    _activeWebLink.value = WebLinkPlayback(
                        title = song.title,
                        url = urlStr,
                        sourceName = song.artist.ifBlank { MediaLinkUtils.inferSourceName(urlStr) },
                        isShort = false,
                        mediaId = song.id
                    )
                }
            }
            return
        }
        launchSongInExoPlayer(song, queue, startFromBeginning, asVideoAudio)
    }

    private fun launchSongInExoPlayer(
        song: SongItem,
        queue: List<SongItem>,
        startFromBeginning: Boolean,
        asVideoAudio: Boolean
    ) {
        if (!isNavigatingHistory) {
            val prev = playerManager.playerState.value.currentSong
            if (prev != null && prev.id != song.id) {
                songHistory.addLast(prev)
                if (songHistory.size > 50) songHistory.removeFirst()
            }
        }
        _uiState.value = _uiState.value.copy(
            normalPlayerVideo = null,
            isFloatingPipActive = false,
            songPlayerQueue = if (queue.isNotEmpty()) queue else _uiState.value.songPlayerQueue
        )
        val startPos = if (startFromBeginning) 0L else song.lastPositionMs.coerceAtLeast(0L)
        playerManager.playSong(
            song = song,
            startPositionMs = startPos,
            autoPlay = true,
            asVideoAudio = asVideoAudio,
            notificationArtworkUri = song.albumArtUri ?: song.uri
        )
        setSongPlayerExpanded(true)
    }

    fun toggleSongFavorite(song: SongItem) {
        viewModelScope.launch {
            repository.toggleSongFavorite(song.id, song.isFavorite)
        }
    }

    fun playNextSong() {
        val state = playerManager.playerState.value
        val currentSong = state.currentSong ?: return
        val isVideoAudio = state.isVideoAudio

        val songs = if (isVideoAudio) {
            allVideos.value.map { videoToSongItem(it) }
        } else {
            if (_uiState.value.songPlayerQueue.isNotEmpty()) _uiState.value.songPlayerQueue else allSongs.value
        }

        if (songs.isEmpty()) return

        if (state.isShuffle) {
            val candidates = songs.filter { it.id != currentSong.id }
            if (candidates.isNotEmpty()) {
                val nextSong = candidates.random()
                playSong(nextSong, startFromBeginning = true, asVideoAudio = isVideoAudio)
                return
            }
        }

        val currentIndex = songs.indexOfFirst { it.id == currentSong.id }

        if (currentIndex < 0) {
            playSong(songs.first(), startFromBeginning = true, asVideoAudio = isVideoAudio)
            return
        }

        if (currentIndex < songs.lastIndex) {
            playSong(songs[currentIndex + 1], startFromBeginning = true, asVideoAudio = isVideoAudio)
        } else if (state.repeatMode == androidx.media3.common.Player.REPEAT_MODE_ALL) {
            playSong(songs.first(), startFromBeginning = true, asVideoAudio = isVideoAudio)
        } else {
            playerManager.stopMedia()
            setSongPlayerExpanded(false)
        }
    }

    fun playPreviousSong() {
        val state = playerManager.playerState.value
        val currentSong = state.currentSong ?: return
        val currentPos = state.currentPositionMs
        val now = System.currentTimeMillis()
        val isQuickConsecutiveClick = (now - lastPreviousSongClickTimeMs) < 2000L
        lastPreviousSongClickTimeMs = now

        // If playing song past 3 seconds and not a quick consecutive click, restart current song from beginning
        if (currentPos > 3000L && !isQuickConsecutiveClick) {
            playerManager.seekTo(0L)
            if (state.isPlaying) {
                playerManager.play()
            }
            return
        }

        val isVideoAudio = state.isVideoAudio
        val songs = if (isVideoAudio) {
            allVideos.value.map { videoToSongItem(it) }
        } else {
            if (_uiState.value.songPlayerQueue.isNotEmpty()) _uiState.value.songPlayerQueue else allSongs.value
        }

        if (songs.isEmpty()) return

        // If user selected shuffle mode: go back according to shuffle mode
        if (state.isShuffle) {
            while (songHistory.isNotEmpty()) {
                val lastSong = songHistory.removeLast()
                if (lastSong.id != currentSong.id) {
                    isNavigatingHistory = true
                    try {
                        playSong(lastSong, startFromBeginning = true, asVideoAudio = isVideoAudio)
                    } finally {
                        isNavigatingHistory = false
                    }
                    return
                }
            }

            // Fallback when no previous history: pick a random song according to shuffle
            val candidates = songs.filter { it.id != currentSong.id }
            val nextSong = if (candidates.isNotEmpty()) candidates.random() else songs.first()
            playSong(nextSong, startFromBeginning = true, asVideoAudio = isVideoAudio)
            return
        }

        // Shuffle is OFF: navigate sequentially (checking history first)
        while (songHistory.isNotEmpty()) {
            val lastSong = songHistory.removeLast()
            if (lastSong.id != currentSong.id) {
                isNavigatingHistory = true
                try {
                    playSong(lastSong, startFromBeginning = true, asVideoAudio = isVideoAudio)
                } finally {
                    isNavigatingHistory = false
                }
                return
            }
        }

        // Fallback when no previous history: play the sequentially previous song in the list
        val currentIndex = songs.indexOfFirst { it.id == currentSong.id }

        if (currentIndex < 0) {
            playSong(songs.first(), startFromBeginning = true, asVideoAudio = isVideoAudio)
            return
        }

        if (currentIndex > 0) {
            playSong(songs[currentIndex - 1], startFromBeginning = true, asVideoAudio = isVideoAudio)
        } else {
            playSong(songs.last(), startFromBeginning = true, asVideoAudio = isVideoAudio)
        }
    }

    fun setLyricsEnabled(enabled: Boolean) {
        appPreferences.edit().putBoolean("lyrics_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(lyricsEnabled = enabled)
    }

    // Kept for source compatibility with older settings code. These features
    // are intentionally disabled because V2S uses predefined lyrics only.
    fun setAutoFindLyrics(@Suppress("UNUSED_PARAMETER") enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoFindLyrics = false)
    }

    fun setGenerateLyricsFromAudio(@Suppress("UNUSED_PARAMETER") enabled: Boolean) {
        _uiState.value = _uiState.value.copy(generateLyricsFromAudio = false)
    }

    fun setSyncLyrics(enabled: Boolean) {
        appPreferences.edit().putBoolean("sync_lyrics", enabled).apply()
        _uiState.value = _uiState.value.copy(syncLyrics = enabled)
    }

    fun setSaveGeneratedLyrics(@Suppress("UNUSED_PARAMETER") enabled: Boolean) {
        _uiState.value = _uiState.value.copy(saveGeneratedLyrics = false)
    }

    fun setSongPlayerExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isSongPlayerExpanded = expanded)
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, video.isFavorite)
        }
    }

    fun removeSongFromHistory(song: SongItem) {
        viewModelScope.launch {
            repository.clearSongHistory(song.id)
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Removed \"${song.title}\" from song history")
        }
    }

    fun clearAllSongHistory() {
        viewModelScope.launch {
            repository.clearAllSongHistory()
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Song playback history cleared")
        }
    }

    fun removeVideoFromHistory(video: VideoItem) {
        viewModelScope.launch {
            repository.clearVideoHistory(video.id)
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Removed \"${video.title}\" from video history")
        }
    }

    fun clearAllVideoHistory() {
        viewModelScope.launch {
            repository.clearAllVideoHistory()
            _uiState.value = _uiState.value.copy(userNoticeMessage = "Video playback history cleared")
        }
    }

    fun clearAllPlaybackHistory() {
        viewModelScope.launch {
            repository.clearAllSongHistory()
            repository.clearAllVideoHistory()
            _uiState.value = _uiState.value.copy(userNoticeMessage = "All playback history cleared")
        }
    }

    private fun cleanMemoryAndPlayerForDeletedVideos(videoIds: List<Long>) {
        val currentNormalVideo = _uiState.value.normalPlayerVideo
        if (currentNormalVideo != null && videoIds.contains(currentNormalVideo.id)) {
            playerManager.pause()
            _uiState.value = _uiState.value.copy(
                normalPlayerVideo = null
            )
            if (_uiState.value.currentScreen == AppScreen.NORMAL_PLAYER) {
                navigateTo(AppScreen.VIDEOS)
            }
        }
        val currentVideo = playerManager.playerState.value.currentVideo
        if (currentVideo != null && videoIds.contains(currentVideo.id)) {
            playerManager.pause()
            _uiState.value = _uiState.value.copy(
                isFloatingPipActive = false
            )
        }
        val idSet = videoIds.toSet()
        regenerateShortsFeed(allVideos.value.filter { it.id !in idSet }, _uiState.value.durationFilter)
    }

    private fun cleanMemoryAndPlayerForDeletedSongs(songIds: List<Long>) {
        val currentSong = playerManager.playerState.value.currentSong
        if (currentSong != null && songIds.contains(currentSong.id)) {
            playerManager.pause()
            _uiState.value = _uiState.value.copy(isSongPlayerExpanded = false)
        }
    }

    fun deleteVideo(video: VideoItem, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repository.requestDeleteVideos(listOf(video))) {
                is MediaDeleteResult.Success -> {
                    cleanMemoryAndPlayerForDeletedVideos(listOf(video.id))
                    _uiState.value = _uiState.value.copy(userNoticeMessage = "Video deleted from device storage")
                    onComplete(true)
                }
                is MediaDeleteResult.RequiresPermission -> {
                    val request = IntentSenderRequest.Builder(result.intentSender).build()
                    _pendingDeleteRequest.value = PendingDeleteRequest(
                        intentSenderRequest = request,
                        videoIds = result.videoIds,
                        songIds = emptyList(),
                        onComplete = onComplete
                    )
                }
                is MediaDeleteResult.Failure -> {
                    _uiState.value = _uiState.value.copy(userNoticeMessage = result.message)
                    onComplete(false)
                }
            }
        }
    }

    fun deleteMultipleVideos(videos: List<VideoItem>, onComplete: (Int) -> Unit = {}) {
        if (videos.isEmpty()) {
            onComplete(0)
            return
        }
        viewModelScope.launch {
            when (val result = repository.requestDeleteVideos(videos)) {
                is MediaDeleteResult.Success -> {
                    val count = result.deletedCount
                    cleanMemoryAndPlayerForDeletedVideos(videos.map { it.id })
                    val message = if (count == 1) "1 video deleted from device storage" else "$count videos deleted from device storage"
                    _uiState.value = _uiState.value.copy(userNoticeMessage = message)
                    onComplete(count)
                }
                is MediaDeleteResult.RequiresPermission -> {
                    val request = IntentSenderRequest.Builder(result.intentSender).build()
                    _pendingDeleteRequest.value = PendingDeleteRequest(
                        intentSenderRequest = request,
                        videoIds = result.videoIds,
                        songIds = emptyList(),
                        onComplete = { granted ->
                            onComplete(if (granted) result.videoIds.size else 0)
                        }
                    )
                }
                is MediaDeleteResult.Failure -> {
                    _uiState.value = _uiState.value.copy(userNoticeMessage = result.message)
                    onComplete(0)
                }
            }
        }
    }

    fun deleteSong(song: SongItem, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repository.requestDeleteSongs(listOf(song))) {
                is MediaDeleteResult.Success -> {
                    cleanMemoryAndPlayerForDeletedSongs(listOf(song.id))
                    _uiState.value = _uiState.value.copy(userNoticeMessage = "Song deleted from device storage")
                    onComplete(true)
                }
                is MediaDeleteResult.RequiresPermission -> {
                    val request = IntentSenderRequest.Builder(result.intentSender).build()
                    _pendingDeleteRequest.value = PendingDeleteRequest(
                        intentSenderRequest = request,
                        videoIds = emptyList(),
                        songIds = result.songIds,
                        onComplete = onComplete
                    )
                }
                is MediaDeleteResult.Failure -> {
                    _uiState.value = _uiState.value.copy(userNoticeMessage = result.message)
                    onComplete(false)
                }
            }
        }
    }

    fun deleteMultipleSongs(songs: List<SongItem>, onComplete: (Int) -> Unit = {}) {
        if (songs.isEmpty()) {
            onComplete(0)
            return
        }
        viewModelScope.launch {
            when (val result = repository.requestDeleteSongs(songs)) {
                is MediaDeleteResult.Success -> {
                    val count = result.deletedCount
                    cleanMemoryAndPlayerForDeletedSongs(songs.map { it.id })
                    val msg = if (count == 1) "1 song deleted from device storage" else "$count songs deleted from device storage"
                    _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
                    onComplete(count)
                }
                is MediaDeleteResult.RequiresPermission -> {
                    val request = IntentSenderRequest.Builder(result.intentSender).build()
                    _pendingDeleteRequest.value = PendingDeleteRequest(
                        intentSenderRequest = request,
                        videoIds = emptyList(),
                        songIds = result.songIds,
                        onComplete = { granted ->
                            onComplete(if (granted) result.songIds.size else 0)
                        }
                    )
                }
                is MediaDeleteResult.Failure -> {
                    _uiState.value = _uiState.value.copy(userNoticeMessage = result.message)
                    onComplete(0)
                }
            }
        }
    }

    fun onMediaDeletePermissionResult(granted: Boolean) {
        val pending = _pendingDeleteRequest.value ?: return
        viewModelScope.launch {
            if (granted) {
                if (pending.videoIds.isNotEmpty()) {
                    repository.cleanupDeletedVideos(pending.videoIds)
                    cleanMemoryAndPlayerForDeletedVideos(pending.videoIds)
                    val msg = if (pending.videoIds.size == 1) "Video deleted from phone storage" else "${pending.videoIds.size} videos deleted from phone storage"
                    _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
                }
                if (pending.songIds.isNotEmpty()) {
                    repository.cleanupDeletedSongs(pending.songIds)
                    cleanMemoryAndPlayerForDeletedSongs(pending.songIds)
                    val msg = if (pending.songIds.size == 1) "Song deleted from phone storage" else "${pending.songIds.size} songs deleted from phone storage"
                    _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
                }
                pending.onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(userNoticeMessage = "Deletion cancelled")
                pending.onComplete(false)
            }
            _pendingDeleteRequest.value = null
        }
    }

    fun shareSong(song: SongItem) {
        repository.shareSong(song)
    }

    fun shareMultipleSongs(songs: List<SongItem>) {
        repository.shareMultipleSongs(songs)
    }

    fun addMultipleVideosToPlaylist(playlistId: String, videos: List<VideoItem>) {
        if (videos.isEmpty()) return
        viewModelScope.launch {
            val items = videos.map { Pair(PlaylistMediaType.VIDEO, it.id) }
            repository.addMultipleItemsToPlaylist(playlistId, items)
            val msg = if (videos.size == 1) "Added 1 video to playlist" else "Added ${videos.size} videos to playlist"
            _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
        }
    }

    fun addMultipleSongsToPlaylist(playlistId: String, songs: List<SongItem>) {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            val items = songs.map { Pair(PlaylistMediaType.AUDIO, it.id) }
            repository.addMultipleItemsToPlaylist(playlistId, items)
            val msg = if (songs.size == 1) "Added 1 song to playlist" else "Added ${songs.size} songs to playlist"
            _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
        }
    }

    fun addMultipleMediaItemsToPlaylist(playlistId: String, items: List<PlaylistMediaItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            val pairs = items.map { Pair(it.type, it.mediaId) }
            repository.addMultipleItemsToPlaylist(playlistId, pairs)
            val msg = if (items.size == 1) "Added 1 item to playlist" else "Added ${items.size} items to playlist"
            _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
        }
    }

    fun shareMultipleMediaItems(items: List<PlaylistMediaItem>) {
        if (items.isEmpty()) return
        val uris = ArrayList<Uri>()
        items.forEach { item ->
            val u = when (item.type) {
                PlaylistMediaType.VIDEO -> item.videoItem?.uri ?: item.uri
                PlaylistMediaType.AUDIO -> item.songItem?.uri ?: item.uri
            }
            if (u != Uri.EMPTY) {
                uris.add(u)
            }
        }
        if (uris.isEmpty()) return
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = android.content.Intent.createChooser(intent, "Share ${items.size} media files").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error sharing multiple items: ${e.message}")
        }
    }

    fun playMultiplePlaylistMediaItems(items: List<PlaylistMediaItem>) {
        if (items.isEmpty()) return
        val first = items.first()
        playPlaylistItem(first, items)
    }

    fun deleteMultiplePlaylistItems(playlistId: String, items: List<PlaylistMediaItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { item ->
                if (playlistId == "favorites") {
                    if (item.type == PlaylistMediaType.VIDEO && item.videoItem != null) {
                        repository.toggleFavorite(item.videoItem.id, false)
                    } else if (item.songItem != null) {
                        repository.toggleSongFavorite(item.songItem.id, false)
                    }
                } else {
                    repository.removeItemFromPlaylist(playlistId, item.type, item.mediaId)
                }
            }
            val msg = if (items.size == 1) "Removed 1 item from playlist" else "Removed ${items.size} items from playlist"
            _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
        }
    }

    fun shareVideo(video: VideoItem) {
        repository.shareVideo(video)
    }

    fun shareMultipleVideos(videos: List<VideoItem>) {
        repository.shareMultipleVideos(videos)
    }

    fun playMultipleVideos(videos: List<VideoItem>) {
        if (videos.isEmpty()) return
        val first = videos.first()
        val queue = videos.drop(1)
        openNormalPlayer(first, queue)
    }

    fun toggleMultipleFavorites(videos: List<VideoItem>, setFavorite: Boolean) {
        viewModelScope.launch {
            videos.forEach { video ->
                repository.toggleFavorite(video.id, !setFavorite)
            }
            val msg = if (setFavorite) {
                if (videos.size == 1) "Added to favorites" else "Added ${videos.size} videos to favorites"
            } else {
                if (videos.size == 1) "Removed from favorites" else "Removed ${videos.size} videos from favorites"
            }
            _uiState.value = _uiState.value.copy(userNoticeMessage = msg)
        }
    }

    fun setAlertPosition(position: V2SAlertPosition = V2SAlertPosition.TOP) {
        _uiState.value = _uiState.value.copy(
            alertPosition = V2SAlertPosition.TOP
        )
    }

    fun toggleAlertPosition() {
        _uiState.value = _uiState.value.copy(
            alertPosition = V2SAlertPosition.TOP
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAYLIST ACTIONS & PLAYBACK
    // ─────────────────────────────────────────────────────────────────────────

    fun createPlaylist(name: String, category: String = "ALL", onComplete: ((String) -> Unit)? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(name, category)
            onComplete?.invoke(id)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newTitle)
        }
    }

    fun addMediaToPlaylist(playlistId: String, item: PlaylistMediaItem) {
        viewModelScope.launch {
            repository.addItemToPlaylist(playlistId, item.type, item.mediaId)
        }
    }

    fun addMediaToPlaylist(playlistId: String, type: PlaylistMediaType, mediaId: Long) {
        viewModelScope.launch {
            repository.addItemToPlaylist(playlistId, type, mediaId)
        }
    }

    fun removeMediaFromPlaylist(playlistId: String, item: PlaylistMediaItem) {
        viewModelScope.launch {
            if (playlistId == "favorites") {
                if (item.type == PlaylistMediaType.VIDEO && item.videoItem != null) {
                    toggleFavorite(item.videoItem)
                }
            } else {
                repository.removeItemFromPlaylist(playlistId, item.type, item.mediaId)
            }
        }
    }

    fun playPlaylistItem(item: PlaylistMediaItem, playlistItems: List<PlaylistMediaItem> = emptyList()) {
        if (item.type == PlaylistMediaType.VIDEO && item.videoItem != null) {
            val videoQueue = playlistItems.mapNotNull { it.videoItem }
            val effectiveQueue = if (videoQueue.size > 1) videoQueue else allVideos.value
            openNormalPlayer(item.videoItem, effectiveQueue)
        } else if (item.songItem != null) {
            playSong(item.songItem)
        } else if (item.type == PlaylistMediaType.VIDEO) {
            // Video fallback
            val dummyVid = allVideos.value.find { it.id == item.mediaId }
            if (dummyVid != null) {
                openNormalPlayer(dummyVid, allVideos.value)
            }
        }
    }

    fun playItemAsAudio(item: PlaylistMediaItem) {
        if (item.songItem != null) {
            playSong(item.songItem)
        } else if (item.videoItem != null) {
            // Play video audio stream
            val songEquivalent = SongItem(
                id = item.videoItem.id,
                uri = item.videoItem.uri,
                title = item.videoItem.title,
                artist = "Video Audio",
                album = "V2S Playlists",
                durationMs = item.videoItem.durationMs,
                sizeBytes = item.videoItem.sizeBytes,
                path = item.videoItem.path
            )
            playSong(songEquivalent)
        }
    }

    fun playPlaylist(playlist: PlaylistData, shuffle: Boolean = false) {
        val items = if (shuffle) playlist.items.shuffled() else playlist.items
        if (items.isEmpty()) return
        val first = items.first()
        playPlaylistItem(first, items)
    }

    fun shareMediaItem(item: PlaylistMediaItem) {
        val mime = if (item.type == PlaylistMediaType.VIDEO) "video/*" else "audio/*"
        repository.shareMedia(item.uri, mime, item.title)
    }

    fun triggerTopAlert(message: String) {
        _uiState.value = _uiState.value.copy(userNoticeMessage = message)
    }

    fun clearNoticeMessage() {
        _uiState.value = _uiState.value.copy(userNoticeMessage = null)
    }

    private fun regenerateShortsFeed(
        videos: List<VideoItem>,
        filter: DurationFilter,
        sortOrder: SortOrder = _uiState.value.sortOrder
    ) {
        val eligible = videos.filter {
            MediaLinkUtils.isUserLinkId(it.id) || filter.matches(it.durationMs)
        }
        val sorted = when (sortOrder) {
            SortOrder.NAME_ASC -> eligible.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> eligible.sortedByDescending { it.title.lowercase() }
            SortOrder.FILENAME_ASC -> eligible.sortedBy { it.displayName.lowercase() }
            SortOrder.FILENAME_DESC -> eligible.sortedByDescending { it.displayName.lowercase() }
            SortOrder.DURATION_SHORT -> eligible.sortedBy { it.durationMs }
            SortOrder.DURATION_LONG -> eligible.sortedByDescending { it.durationMs }
            SortOrder.DATE_OLD -> eligible.sortedBy { it.dateAddedSec }
            SortOrder.DATE_NEW -> eligible.sortedByDescending { it.dateAddedSec }
        }
        val generated = if (sortOrder == SortOrder.NEWEST) {
            val linkedShorts = eligible.filter { it.path?.contains("/SHORT/") == true }
            val otherEligible = eligible.filter { it.path?.contains("/SHORT/") != true }
            val smart = feedEngine.generateFeed(otherEligible, filter)
            (linkedShorts + smart).distinctBy { it.id }
        } else {
            sorted
        }
        _uiState.value = _uiState.value.copy(
            shortsFeed = generated,
            activeShortsIndex = 0
        )
    }

    override fun onCleared() {
        super.onCleared()
        // If background play is enabled or media is currently playing, preserve playerManager
        if (!playerManager.playerState.value.isBackgroundPlayEnabled && !playerManager.playerState.value.isPlaying) {
            V2SMediaNotificationService.actionListener = null
            playerManager.release()
        }
    }
}
