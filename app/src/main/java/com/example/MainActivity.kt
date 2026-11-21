
package com.example

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.player.EarphonePreferences
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

import com.example.data.model.VideoItem

import com.example.ui.components.AddMediaLinkDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.FloatingPipWindow
import com.example.ui.components.PermissionGate
import com.example.ui.components.PersistentMiniPlayer
import com.example.ui.components.UserLinkEditData
import com.example.ui.components.V2SAlertHost
import com.example.ui.components.V2SSplashOpenAnimation
import com.example.ui.components.V2SWelcomeGreetingDialog
import com.example.ui.components.WebLinkPlayerDialog
import com.example.ui.components.rememberV2SDeviceInfo
import com.example.ui.components.VideoInfoBottomSheet
import com.example.ui.components.VyloBottomBar

import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.local.LocalScreen
import com.example.ui.screens.player.NormalPlayerScreen
import com.example.ui.screens.playlists.PlaylistsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shorts.ShortsScreen
import com.example.ui.screens.songs.PlayitAudioPlayerView
import com.example.ui.screens.songs.SongsScreen

import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val isInPipModeState = mutableStateOf(false)

    // Becoming Noisy receiver: auto pause when headphones/earbuds unplugged or Bluetooth disconnected
    private var isNoisyReceiverRegistered = false
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                val playerManager = viewModel.playerManager
                if (EarphonePreferences.load(this@MainActivity).pauseOnUnplugEnabled) {
                    playerManager.pause()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isNoisyReceiverRegistered) {
            try {
                registerReceiver(
                    becomingNoisyReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                )
                isNoisyReceiverRegistered = true
            } catch (_: Exception) {}
        }
    }

    override fun onStop() {
        super.onStop()
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(becomingNoisyReceiver)
                isNoisyReceiverRegistered = false
            } catch (_: Exception) {}
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val playerState = viewModel.playerManager.playerState.value
        val uiState = viewModel.uiState.value
        val hasActiveVideo = playerState.currentVideo != null
        if (hasActiveVideo && (playerState.isPlaying || uiState.isFloatingPipActive || uiState.currentScreen == AppScreen.NORMAL_PLAYER)) {
            enterPictureInPicture()
        }
    }

    fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val isInPip by isInPipModeState

            MyApplicationTheme(
                themeMode = uiState.themeMode,
                fontScale = uiState.fontScale,
                uiScale = uiState.uiScale,
                accentColor = uiState.accentColor,
            ) {
                if (isInPip) {
                    SystemPipPlayerSurface(
                        playerManager = viewModel.playerManager
                    )
                } else {
                    VyloApp(
                        viewModel = viewModel,
                        isInPip = isInPip
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
    }

    companion object {
        var instance: MainActivity? = null

        fun closeApp() {
            try {
                instance?.let { act ->
                    act.runOnUiThread {
                        act.finishAffinity()
                    }
                }
            } catch (_: Exception) {}
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SystemPipPlayerSurface(
    playerManager: com.example.player.VyloPlayerManager,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getOrCreatePlayer()
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = playerManager.getOrCreatePlayer()
            },
            onReset = { view ->
                view.player = null
            },
            onRelease = { view ->
                view.player = null
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun VyloApp(
    viewModel: MainViewModel,
    isInPip: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerManager.playerState.collectAsState()

    val allVideos by viewModel.allVideos.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val libraryVideos by viewModel.libraryVideos.collectAsState()
    val recentlyWatched by viewModel.recentlyWatchedVideos.collectAsState()
    val recentlyWatchedSongs by viewModel.recentlyWatchedSongs.collectAsState()
    val favoriteVideos by viewModel.favoriteVideos.collectAsState()
    val activeWebLink by viewModel.activeWebLink.collectAsState()

    var addLinkDialogInitialType by remember {
        mutableStateOf<String?>(null)
    }

    var editingLinkData by remember {
        mutableStateOf<UserLinkEditData?>(null)
    }

    /*
     * Fast startup - no blocking splash delay.
     */
    var showSplashAnimation by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var videoToDelete by remember {
        mutableStateOf<VideoItem?>(null)
    }

    var videoForInfo by remember {
        mutableStateOf<VideoItem?>(null)
    }

    val deleteMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onMediaDeletePermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    val pendingDelete by viewModel.pendingDeleteRequest.collectAsState()

    LaunchedEffect(pendingDelete) {
        pendingDelete?.let { pending ->
            try {
                deleteMediaLauncher.launch(pending.intentSenderRequest)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to launch media delete permission request", e)
                viewModel.onMediaDeletePermissionResult(false)
            }
        }
    }

    val context = LocalContext.current
    val welcomePrefs = remember {
        context.getSharedPreferences("v2s_welcome_prefs", android.content.Context.MODE_PRIVATE)
    }
    val deviceInfo = rememberV2SDeviceInfo()
    var showWelcomeGreetingDialog by remember {
        mutableStateOf(!welcomePrefs.getBoolean("dont_show_welcome_greeting", false))
    }
    var dontShowWelcomeAgain by remember {
        mutableStateOf(welcomePrefs.getBoolean("dont_show_welcome_greeting", false))
    }

    /*
     * Playback errors are reported to the user via top alert banner.
     */
    LaunchedEffect(playerState.error) {
        playerState.error?.let { error ->
            viewModel.triggerTopAlert("Playback error: $error")
            viewModel.playerManager.clearPlaybackError()
        }
    }

    /*
     * Background playback policy:
     * When the app moves to background / stopped / closed:
     * Keep playing ANY video, Short, or song if Background Play is enabled,
     * or pause when Background Play is turned off.
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isInPip, uiState.currentScreen) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || (event == Lifecycle.Event.ON_PAUSE && !isInPip)) {
                val state = viewModel.playerManager.playerState.value
                val hasActiveMedia = state.currentVideo != null ||
                    state.currentSong != null ||
                    state.isAudioOnly ||
                    state.isVideoAudio
                val canContinueInBg = isInPip || (state.isBackgroundPlayEnabled && hasActiveMedia)
                if (!canContinueInBg) {
                    viewModel.playerManager.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    /*
     * Universal Back button: returns to previous screen / page.
     */
    BackHandler(
        enabled = viewModel.canNavigateBack() || uiState.currentScreen == AppScreen.NORMAL_PLAYER || uiState.isSongPlayerExpanded
    ) {
        if (uiState.isSongPlayerExpanded) {
            viewModel.setSongPlayerExpanded(false)
        } else if (uiState.currentScreen == AppScreen.NORMAL_PLAYER) {
            if (!viewModel.navigateBack()) {
                viewModel.navigateTo(AppScreen.VIDEOS, addToBackStack = false)
            }
        } else if (!viewModel.navigateBack()) {
            val isPlayingSong = playerState.currentSong != null || playerState.isAudioOnly
            val targetScreen = if (isPlayingSong) AppScreen.SONGS else AppScreen.VIDEOS
            if (uiState.currentScreen != targetScreen) {
                viewModel.navigateTo(targetScreen, addToBackStack = false)
            }
        }
    }


    PermissionGate(
        hasPermission = uiState.hasStoragePermission,

        onPermissionGranted = { granted ->

            viewModel.onPermissionResult(
                granted
            )
        }
    ) {

        Scaffold(

            modifier = Modifier.fillMaxSize(),

            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                )
            },

            bottomBar = {
                if (
                    uiState.currentScreen != AppScreen.NORMAL_PLAYER &&
                    !uiState.isSongPlayerExpanded
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        /*
                         * Persistent mini player:
                         * Shown across sections (Videos, Songs, Local, Playlists, Settings)
                         * whenever a song or video is active, EXCEPT on Shorts.
                         */
                        val hasActiveMedia = (playerState.currentVideo != null || playerState.currentSong != null) &&
                            uiState.currentScreen != AppScreen.SHORTS

                        if (hasActiveMedia && !uiState.isFloatingPipActive) {
                            PersistentMiniPlayer(
                                playerManager = viewModel.playerManager,
                                isFloatingPip = false,
                                onFloatingPipChange = { active ->
                                    viewModel.setFloatingPipActive(active)
                                },
                                onExpand = {
                                    if (playerState.currentSong != null) {
                                        viewModel.restoreSongPlayer()
                                    } else {
                                        viewModel.restoreVideoPlayer()
                                    }
                                },
                                onPrevious = {
                                    if (playerState.currentSong != null) {
                                        viewModel.playPreviousSong()
                                    } else {
                                        viewModel.playPreviousVideo()
                                    }
                                },
                                onNext = {
                                    if (playerState.currentSong != null) {
                                        viewModel.playNextSong()
                                    } else {
                                        viewModel.playNextVideo()
                                    }
                                },
                                onClose = {
                                    viewModel.closeMiniPlayer()
                                }
                            )
                        }

                        /*
                         * Main bottom navigation.
                         */
                        VyloBottomBar(
                            currentScreen = uiState.currentScreen,
                            onNavigate = { screen ->
                                viewModel.navigateTo(screen)
                            }
                        )
                    }
                }
            }

        ) { innerPadding ->

            /*
             * Full-screen background.
             *
             * The inner content is limited to 1200dp on
             * large displays and centered horizontally.
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background
                    )
            ) {

                /*
                 * Responsive application content container.
                 *
                 * Phones:
                 * - Uses the available width.
                 *
                 * Tablets / large screens:
                 * - Stops growing beyond 1200dp.
                 * - Remains centered.
                 */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1200.dp)
                        .align(Alignment.Center)
                ) {

                    when (uiState.currentScreen) {

                        /*
                         * VIDEOS / LIBRARY
                         */
                        AppScreen.VIDEOS -> {

                            LibraryScreen(

                                videos = libraryVideos,

                                folders = folders,

                                allVideos = allVideos,

                                allSongs = allSongs,

                                favoriteVideos = favoriteVideos,

                                recentlyWatched = recentlyWatched,

                                sortOrder = uiState.sortOrder,

                                searchQuery = uiState.searchQuery,

                                onSortChange = {

                                    viewModel.setSortOrder(
                                        it
                                    )
                                },

                                onSearchChange = {

                                    viewModel.setSearchQuery(
                                        it
                                    )
                                },

                                onPlayInShorts = {

                                    viewModel.startShortsFromVideo(
                                        it
                                    )
                                },

                                onPlayAsPip = {
                                    viewModel.playVideoInPip(
                                        it,
                                        libraryVideos
                                    )
                                },

                                onOpenNormalPlayer = { video, queue ->
                                    viewModel.openNormalPlayer(
                                        video,
                                        if (queue.isNotEmpty()) queue else libraryVideos
                                    )
                                },

                                onPlayFromStart = {
                                    viewModel.openNormalPlayer(
                                        it,
                                        libraryVideos,
                                        startFromBeginning = true
                                    )
                                },

                                onPlayAsAudio = {
                                    viewModel.playVideoAsAudio(it)
                                },

                                onAddMultipleVideosToPlaylist = { playlistId, list ->
                                    viewModel.addMultipleVideosToPlaylist(playlistId, list)
                                },

                                onSongClick = {

                                    viewModel.playSong(
                                        it
                                    )
                                },

                                onNavigateToShorts = {

                                    viewModel.navigateTo(
                                        AppScreen.SHORTS
                                    )
                                },

                                onNavigateToSongs = {

                                    viewModel.navigateTo(
                                        AppScreen.SONGS
                                    )
                                },

                                onToggleFavorite = {

                                    viewModel.toggleFavorite(
                                        it
                                    )
                                },

                                onShareVideo = {

                                    viewModel.shareVideo(
                                        it
                                    )
                                },

                                onShowInfo = {

                                    videoForInfo = it
                                },

                                onDeleteVideo = {

                                    videoToDelete = it
                                },

                                onDeleteMultipleVideos = { list ->
                                    viewModel.deleteMultipleVideos(list)
                                },

                                onShareMultipleVideos = { list ->
                                    viewModel.shareMultipleVideos(list)
                                },

                                onPlayMultipleVideos = { list ->
                                    viewModel.playMultipleVideos(list)
                                },

                                onToggleMultipleFavorites = { list, makeFav ->
                                    viewModel.toggleMultipleFavorites(list, makeFav)
                                },

                                onRefreshScan = {
                                    viewModel.refreshMediaScan()
                                },

                                onPlayLastPlayed = {
                                    viewModel.playLastPlayedVideo()
                                },

                                playlists = allPlaylists,

                                onCreatePlaylist = { name, cat ->
                                    viewModel.createPlaylist(name, cat)
                                },

                                onDeletePlaylist = {
                                    viewModel.deletePlaylist(it)
                                },

                                onRenamePlaylist = { id, name ->
                                    viewModel.renamePlaylist(id, name)
                                },

                                onAddLinkClick = {
                                    addLinkDialogInitialType = "VIDEO"
                                },

                                modifier = Modifier.padding(
                                    bottom =
                                        innerPadding
                                            .calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * SHORTS
                         */
                        AppScreen.SHORTS -> {

                            ShortsScreen(

                                feed = uiState.shortsFeed,

                                activeFilter =
                                    uiState.durationFilter,

                                playerManager =
                                    viewModel.playerManager,

                                onFilterSelected = {

                                    viewModel.setDurationFilter(
                                        it
                                    )
                                },

                                onVideoChanged = {

                                    viewModel.onShortsPageChanged(
                                        it
                                    )
                                },

                                onToggleFavorite = {

                                    viewModel.toggleFavorite(
                                        it
                                    )
                                },

                                onShareVideo = {

                                    viewModel.shareVideo(
                                        it
                                    )
                                },

                                onOpenInNormalPlayer = {

                                    viewModel.openNormalPlayer(
                                        it
                                    )
                                },

                                onShowInfo = {

                                    videoForInfo = it
                                },

                                onDeleteVideo = {

                                    videoToDelete = it
                                },

                                currentSortOrder =
                                    uiState.sortOrder,

                                onSortSelected = {

                                    viewModel.setSortOrder(
                                        it
                                    )
                                },

                                onAddLinkClick = {
                                    addLinkDialogInitialType = "SHORT"
                                },

                                onPlayWebLink = {
                                    viewModel.openNormalPlayer(it)
                                },

                                modifier = Modifier.padding(
                                    bottom =
                                        innerPadding
                                            .calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * SONGS
                         */
                        AppScreen.SONGS -> {
                            SongsScreen(
                                songs = allSongs,
                                allVideos = allVideos,
                                recentlyWatchedSongs = recentlyWatchedSongs,
                                playerManager = viewModel.playerManager,
                                onPlaySong = { song ->
                                    viewModel.playSong(song)
                                },
                                onPlayPrevious = {
                                    viewModel.playPreviousSong()
                                },
                                onPlayNext = {
                                    viewModel.playNextSong()
                                },
                                onWatchAsVideo = { video ->
                                    viewModel.openNormalPlayer(video)
                                },
                                onOpenPlayer = {
                                    viewModel.restoreSongPlayer()
                                },
                                lyricsEnabled = uiState.lyricsEnabled,
                                onRefreshScan = {
                                    viewModel.refreshMediaScan()
                                },
                                onPlayLastPlayed = {
                                    viewModel.playLastPlayedSong()
                                },
                                playlists = allPlaylists,
                                onDeleteSong = { song ->
                                    viewModel.deleteSong(song)
                                },
                                onDeleteMultipleSongs = { songs ->
                                    viewModel.deleteMultipleSongs(songs)
                                },
                                onShareSong = { song ->
                                    viewModel.shareSong(song)
                                },
                                onShareMultipleSongs = { songs ->
                                    viewModel.shareMultipleSongs(songs)
                                },
                                onAddMultipleSongsToPlaylist = { playlistId, songs ->
                                    viewModel.addMultipleSongsToPlaylist(playlistId, songs)
                                },
                                onPlayMultipleSongs = { songs ->
                                    if (songs.isNotEmpty()) {
                                        viewModel.playSong(songs.first())
                                    }
                                },
                                onClearSongHistory = {
                                    viewModel.clearAllSongHistory()
                                },
                                onRemoveSongFromHistory = { song ->
                                    viewModel.removeSongFromHistory(song)
                                },
                                onCreatePlaylist = { name, cat ->
                                    viewModel.createPlaylist(name, cat)
                                },
                                onDeletePlaylist = {
                                    viewModel.deletePlaylist(it)
                                },
                                onRenamePlaylist = { id, name ->
                                    viewModel.renamePlaylist(id, name)
                                },
                                onAddLinkClick = {
                                    addLinkDialogInitialType = "SONG"
                                },
                                modifier = Modifier.padding(
                                    bottom = innerPadding.calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * LOCAL / FOLDERS
                         */
                        AppScreen.LOCAL -> {

                            LocalScreen(

                                folders = folders,

                                allVideos = allVideos,

                                allSongs = allSongs,

                                onVideoClick = {

                                    viewModel.openNormalPlayer(
                                        it,
                                        allVideos
                                    )
                                },

                                onSongClick = { song ->

                                    viewModel.playSong(
                                        song
                                    )

                                    viewModel.navigateTo(
                                        AppScreen.SONGS
                                    )
                                },

                                onRefreshScan = {

                                    viewModel.refreshMediaScan()
                                },

                                onAddLinkClick = {
                                    addLinkDialogInitialType = "VIDEO"
                                },

                                modifier = Modifier.padding(
                                    bottom =
                                        innerPadding
                                            .calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * PLAYLISTS
                         */
                        AppScreen.PLAYLISTS -> {

                            PlaylistsScreen(

                                playlists = allPlaylists,

                                allVideos = allVideos,

                                allSongs = allSongs,

                                onPlayMedia = { item, queue ->

                                    viewModel.playPlaylistItem(
                                        item,
                                        queue
                                    )
                                },

                                onPlayAsAudio = { item ->

                                    viewModel.playItemAsAudio(
                                        item
                                    )
                                },

                                onPlayPlaylist = {
                                        playlist,
                                        shuffle ->

                                    viewModel.playPlaylist(
                                        playlist,
                                        shuffle
                                    )
                                },

                                onCreatePlaylist = { name, cat ->
                                    viewModel.createPlaylist(
                                        name,
                                        cat
                                    )
                                },

                                onRenamePlaylist = {
                                        id,
                                        name ->

                                    viewModel.renamePlaylist(
                                        id,
                                        name
                                    )
                                },

                                onDeletePlaylist = {

                                    viewModel.deletePlaylist(
                                        it
                                    )
                                },

                                onAddMediaToPlaylist = {
                                        playlistId,
                                        type,
                                        mediaId ->

                                    viewModel.addMediaToPlaylist(
                                        playlistId,
                                        type,
                                        mediaId
                                    )
                                },

                                onRemoveMediaFromPlaylist = {
                                        playlistId,
                                        item ->

                                    viewModel.removeMediaFromPlaylist(
                                        playlistId,
                                        item
                                    )
                                },

                                onShareMedia = {
                                    viewModel.shareMediaItem(
                                        it
                                    )
                                },

                                onDeleteMultiplePlaylistItems = { playlistId, items ->
                                    viewModel.deleteMultiplePlaylistItems(playlistId, items)
                                },

                                onAddMultipleMediaItemsToPlaylist = { playlistId, items ->
                                    viewModel.addMultipleMediaItemsToPlaylist(playlistId, items)
                                },

                                onShareMultipleMedia = { items ->
                                    viewModel.shareMultipleMediaItems(items)
                                },

                                onPlayMultipleMedia = { items ->
                                    viewModel.playMultiplePlaylistMediaItems(items)
                                },

                                onDeleteMultiplePlaylists = { ids ->
                                    ids.forEach { viewModel.deletePlaylist(it) }
                                },

                                modifier = Modifier.padding(
                                    bottom =
                                        innerPadding
                                            .calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * SETTINGS
                         */
                        AppScreen.SETTINGS -> {

                            SettingsScreen(

                                viewModel = viewModel,

                                modifier = Modifier.padding(
                                    bottom =
                                        innerPadding
                                            .calculateBottomPadding()
                                )
                            )
                        }


                        /*
                         * NORMAL VIDEO PLAYER
                         */
                        AppScreen.NORMAL_PLAYER -> {
                            val playlistVideos = if (uiState.normalPlayerQueue.isNotEmpty()) {
                                uiState.normalPlayerQueue
                            } else {
                                allVideos
                            }

                            NormalPlayerScreen(
                                video = uiState.normalPlayerVideo,
                                playlist = playlistVideos,
                                allVideos = allVideos,
                                onSelectVideo = { selectedVideo ->
                                    viewModel.openNormalPlayer(selectedVideo, allVideos)
                                },
                                playerManager = viewModel.playerManager,

                                onBack = {
                                    if (!viewModel.navigateBack()) {
                                        viewModel.navigateTo(
                                            AppScreen.VIDEOS,
                                            addToBackStack = false
                                        )
                                    }
                                },

                                onMinimizeToPip = {
                                    viewModel.setFloatingPipActive(true)
                                    if (!viewModel.navigateBack()) {
                                        viewModel.navigateTo(
                                            AppScreen.VIDEOS,
                                            addToBackStack = false
                                        )
                                    }
                                },

                                /*
                                 * PLAY VIDEO AS AUDIO
                                 */
                                onPlayAsAudio = {

                                    viewModel.playCurrentVideoAsAudio()
                                },

                                /*
                                 * PLAY NEXT VIDEO
                                 */
                                onPlayNext = {

                                    viewModel.playNextInNormalPlayer()
                                },

                                /*
                                 * PLAY PREVIOUS VIDEO
                                 */
                                onPlayPrevious = {

                                    viewModel.playPreviousInNormalPlayer()
                                },

                                onToggleFavorite = {

                                    viewModel.toggleFavorite(
                                        it
                                    )
                                },

                                onShare = {

                                    viewModel.shareVideo(
                                        it
                                    )
                                },

                                onShowInfo = {

                                    videoForInfo = it
                                }
                            )
                        }
                    }
                }


                /*
                 * Delete confirmation dialog.
                 */
                if (videoToDelete != null) {
                    DeleteConfirmationDialog(
                        video = videoToDelete,
                        onConfirm = {
                            val target = videoToDelete
                            videoToDelete = null
                            target?.let { video ->
                                viewModel.deleteVideo(video)
                            }
                        },
                        onDismiss = {
                            videoToDelete = null
                        }
                    )
                }


                /*
                 * Video information sheet.
                 */
                if (videoForInfo != null) {

                    VideoInfoBottomSheet(

                        video = videoForInfo,

                        onDismiss = {

                            videoForInfo = null
                        }
                    )
                }


                /*
                 * Fullscreen PlayIt-styled Audio Player View:
                 * Displayed across all screens whenever expanded.
                 */
                AnimatedVisibility(
                    visible = uiState.isSongPlayerExpanded && (playerState.currentSong != null || allSongs.isNotEmpty()),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    PlayitAudioPlayerView(
                        playerManager = viewModel.playerManager,
                        allSongs = allSongs,
                        allVideos = if (playerState.isVideoAudio) allVideos else emptyList(),
                        onClose = {
                            viewModel.setSongPlayerExpanded(false)
                        },
                        onPlaySong = { songItem ->
                            viewModel.playSong(songItem)
                        },
                        onPlayPrevious = {
                            viewModel.playPreviousSong()
                        },
                        onPlayNext = {
                            viewModel.playNextSong()
                        },
                        onWatchAsVideo = { videoItem ->
                            viewModel.setSongPlayerExpanded(false)
                            viewModel.openNormalPlayer(videoItem)
                        },
                        lyricsEnabled = uiState.lyricsEnabled,
                        modifier = Modifier.fillMaxSize()
                    )
                }


                /*
                 * Responsive V2S Alert Banner - Fixed strictly at the top.
                 */
                V2SAlertHost(
                    message = uiState.userNoticeMessage,
                    onDismiss = { viewModel.clearNoticeMessage() }
                )


                /*
                 * In-app Floating Picture-in-Picture window overlay
                 * Floats over the current screen and allows playback while browsing.
                 */
                val pipVideo = playerState.currentVideo
                if (uiState.isFloatingPipActive && pipVideo != null &&
                    uiState.currentScreen != AppScreen.NORMAL_PLAYER &&
                    uiState.currentScreen != AppScreen.SHORTS
                ) {
                    var pipOffsetX by remember { mutableFloatStateOf(0f) }
                    var pipOffsetY by remember { mutableFloatStateOf(0f) }

                    FloatingPipWindow(
                        videoTitle = pipVideo.title,
                        playerManager = viewModel.playerManager,
                        isPlaying = playerState.isPlaying,
                        progress = if (playerState.durationMs > 0) {
                            (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f,
                        currentPositionMs = playerState.currentPositionMs,
                        durationMs = playerState.durationMs,
                        offsetX = pipOffsetX,
                        offsetY = pipOffsetY,
                        onDrag = { dx, dy ->
                            pipOffsetX += dx
                            pipOffsetY += dy
                        },
                        onTogglePlay = { viewModel.playerManager.togglePlayPause() },
                        onPrevious = { viewModel.playPreviousVideo() },
                        onNext = { viewModel.playNextVideo() },
                        onSeekRelative = { offsetMs -> viewModel.seekRelativeVideo(offsetMs) },
                        onExpand = {
                            viewModel.restoreVideoPlayer()
                        },
                        onDock = {
                            viewModel.setFloatingPipActive(false)
                        },
                        onClose = {
                            viewModel.closeMiniPlayer()
                        }
                    )
                }


                /*
                 * Responsive Adaptive Welcome Greeting Dialog with Device Model Identification.
                 */
                V2SWelcomeGreetingDialog(
                    isOpen = showWelcomeGreetingDialog,
                    onDismiss = { showWelcomeGreetingDialog = false },
                    deviceInfo = deviceInfo,
                    currentThemeMode = uiState.themeMode,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    dontShowAgain = dontShowWelcomeAgain,
                    onToggleDontShowAgain = { checked ->
                        dontShowWelcomeAgain = checked
                        welcomePrefs.edit()
                            .putBoolean("dont_show_welcome_greeting", checked)
                            .apply()
                    }
                )


                /*
                 * Startup animation.
                 *
                 * Disabled for faster startup.
                 */
                if (showSplashAnimation) {

                    V2SSplashOpenAnimation(

                        onAnimationFinished = {

                            showSplashAnimation = false
                        }
                    )
                }

                if (addLinkDialogInitialType != null || editingLinkData != null) {
                    AddMediaLinkDialog(
                        initialMediaType = editingLinkData?.mediaType ?: addLinkDialogInitialType ?: "VIDEO",
                        editingLink = editingLinkData,
                        onDismiss = {
                            addLinkDialogInitialType = null
                            editingLinkData = null
                        },
                        onSaveLink = { url, title, mediaType, artistOrSource ->
                            val editTarget = editingLinkData
                            if (editTarget != null) {
                                viewModel.updateUserMediaLink(editTarget.mediaId, url, title, mediaType, artistOrSource)
                            } else {
                                viewModel.addUserMediaLink(url, title, mediaType, artistOrSource)
                            }
                            addLinkDialogInitialType = null
                            editingLinkData = null
                        }
                    )
                }

                activeWebLink?.let { webPlayback ->
                    WebLinkPlayerDialog(
                        playback = webPlayback,
                        onClose = { viewModel.closeWebLinkPlayer() },
                        onEditLink = webPlayback.mediaId?.let { id ->
                            {
                                editingLinkData = UserLinkEditData(
                                    mediaId = id,
                                    url = webPlayback.url,
                                    title = webPlayback.title,
                                    mediaType = if (webPlayback.isShort) "SHORT" else com.example.util.MediaLinkUtils.inferMediaType(webPlayback.url),
                                    artistOrSource = webPlayback.sourceName
                                )
                            }
                        },
                        onDeleteLink = webPlayback.mediaId?.let { id ->
                            {
                                viewModel.deleteUserLinkByMediaId(id)
                                viewModel.closeWebLinkPlayer()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
