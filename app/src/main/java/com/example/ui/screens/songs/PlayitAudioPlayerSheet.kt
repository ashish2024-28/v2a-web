package com.example.ui.screens.songs

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import android.media.MediaMetadataRetriever
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMicros
import coil.request.videoFrameOption
import com.example.data.lyrics.LyricsRepository
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import com.example.player.EqualizerPreset
import com.example.player.VyloPlayerManager
import com.example.ui.components.rememberV2SWindowSize
import com.example.ui.components.responsiveGridCells
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcOrange
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fullscreen Playit-styled Audio Player Screen / Sheet
 * Features 2-cycle views:
 * 1) Album / Video Poster Artwork View with Marquee title, seek buttons, track ends-at pill, orange slider, and 5 controls.
 * 2) Queue / All Videos Grid & List Option with animated equalizer playing badge, real-time search, and instant play on tap.
 * 3) Top right 3-dots popup menu with 12 items matching PLAYit exactly (Play as video, Sleep timer, Speed, Equalizer, Jump to time, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayitAudioPlayerView(
    playerManager: VyloPlayerManager,
    allSongs: List<SongItem>,
    allVideos: List<VideoItem> = emptyList(),
    onClose: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onPlayPrevious: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onWatchAsVideo: ((VideoItem) -> Unit)? = null,
    lyricsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val playerState by playerManager.playerState.collectAsState()
    val song = playerState.currentSong ?: allSongs.firstOrNull()

    var isQueueOpen by remember { mutableStateOf(false) }
    var isGridMode by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMoreMenuDropdown by remember { mutableStateOf(false) }

    var availableLyrics by remember(song?.id) { mutableStateOf<List<LyricLine>>(emptyList()) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSongOptionsSheet by remember { mutableStateOf(false) }
    var showEditTagsDialog by remember { mutableStateOf(false) }

    if (song != null) {
        LaunchedEffect(song.id) {
            val loaded = loadLyrics(context, song)
            if (loaded.isNotEmpty()) {
                availableLyrics = loaded
            } else {
                val repoDoc = LyricsRepository.load(context, song)
                if (repoDoc.lines.isNotEmpty()) {
                    availableLyrics = repoDoc.lines.map { LyricLine(it.timeMs ?: 0L, it.text) }
                } else {
                    availableLyrics = emptyList()
                }
            }
        }
    }

    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showJumpToTimeDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showAbRepeatDialog by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf<String?>(null) }
    var showTopVolumeSlider by remember { mutableStateOf(false) }
    val windowSize = rememberV2SWindowSize()

    BackHandler(enabled = true) {
        onClose()
    }

    if (song == null) {
        onClose()
        return
    }

    val currentMs = playerState.currentPositionMs
    val durMs = if (playerState.durationMs > 0) playerState.durationMs else song.durationMs
    val progress = if (durMs > 0) (currentMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f

    val isVideoAudio = playerState.isVideoAudio && playerState.currentVideo == null
    val currentVideo = if (isVideoAudio) allVideos.firstOrNull { it.id == song.id } else null

    // Format current and remaining time
    val curSec = currentMs / 1000
    val remMs = (durMs - currentMs).coerceAtLeast(0L)
    val remSec = remMs / 1000
    val curFormatted = String.format(Locale.getDefault(), "%d:%02d", (curSec % 3600) / 60, curSec % 60)
    val remFormatted = String.format(Locale.getDefault(), "-%d:%02d", (remSec % 3600) / 60, remSec % 60)

    // Calculate Track index and Ends At time (e.g. Track: 2 / 54 • Ends at: 8:48:39 PM)
    val currentQueueIndex = if (isVideoAudio) {
        val idx = allVideos.indexOfFirst { it.id == song.id }
        if (idx >= 0) idx else 0
    } else {
        val idx = allSongs.indexOfFirst { it.id == song.id }
        if (idx >= 0) idx else 0
    }
    val totalQueueCount = if (isVideoAudio && allVideos.isNotEmpty()) allVideos.size else allSongs.size.coerceAtLeast(1)
    val endsAtTime = remember(currentMs, durMs) {
        val endEpoch = System.currentTimeMillis() + remMs
        SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(endEpoch))
    }

    // Filter videos / songs when in search mode
    val displayVideos = remember(allVideos, searchQuery) {
        if (searchQuery.isBlank()) allVideos else allVideos.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.parentFolderName.contains(searchQuery, ignoreCase = true)
        }
    }

    val displaySongs = remember(allSongs, searchQuery) {
        if (searchQuery.isBlank()) allSongs else allSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val queueListState = rememberLazyListState()
    val queueGridState = rememberLazyGridState()

    val currentItemIndex = remember(song.id, isVideoAudio, displaySongs, displayVideos) {
        if (isVideoAudio) {
            displayVideos.indexOfFirst { it.id == song.id }
        } else {
            displaySongs.indexOfFirst { it.id == song.id }
        }
    }

    LaunchedEffect(song.id, isQueueOpen) {
        if (isQueueOpen && currentItemIndex >= 0) {
            queueListState.animateScrollToItem((currentItemIndex - 1).coerceAtLeast(0))
            queueGridState.animateScrollToItem((currentItemIndex - 1).coerceAtLeast(0))
        }
    }

    // Navigation lambdas with full wrap-around support
    val playNextTrack: () -> Unit = {
        if (onPlayNext != null) {
            onPlayNext()
        } else if (isVideoAudio && allVideos.isNotEmpty()) {
            val idx = allVideos.indexOfFirst { it.id == song.id }
            val target = if (idx >= 0 && idx < allVideos.size - 1) allVideos[idx + 1] else allVideos.first()
            val songEquiv = SongItem(
                id = target.id,
                uri = target.uri,
                title = target.title,
                artist = target.parentFolderName,
                album = "Video Audio",
                durationMs = target.durationMs,
                sizeBytes = target.sizeBytes,
                path = target.path,
                albumArtUri = target.uri,
                lastPositionMs = 0L,
                isFavorite = target.isFavorite
            )
            onPlaySong(songEquiv)
        } else if (allSongs.isNotEmpty()) {
            val idx = allSongs.indexOfFirst { it.id == song.id }
            val target = if (idx >= 0 && idx < allSongs.size - 1) allSongs[idx + 1] else allSongs.first()
            onPlaySong(target)
        }
    }

    val playPreviousTrack: () -> Unit = {
        if (onPlayPrevious != null) {
            onPlayPrevious()
        } else if (playerManager.playerState.value.currentPositionMs > 3000L) {
            playerManager.seekTo(0L)
            if (playerManager.playerState.value.isPlaying) {
                playerManager.play()
            }
        } else if (isVideoAudio && allVideos.isNotEmpty()) {
            val idx = allVideos.indexOfFirst { it.id == song.id }
            val target = if (idx > 0) allVideos[idx - 1] else allVideos.last()
            val songEquiv = SongItem(
                id = target.id,
                uri = target.uri,
                title = target.title,
                artist = target.parentFolderName,
                album = "Video Audio",
                durationMs = target.durationMs,
                sizeBytes = target.sizeBytes,
                path = target.path,
                albumArtUri = target.uri,
                lastPositionMs = 0L,
                isFavorite = target.isFavorite
            )
            onPlaySong(songEquiv)
        } else if (allSongs.isNotEmpty()) {
            val idx = allSongs.indexOfFirst { it.id == song.id }
            val target = if (idx > 0) allSongs[idx - 1] else allSongs.last()
            onPlaySong(target)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1015))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { /* consume taps to prevent falling through to background screens */ })
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP HEADER (Screenshot 1, 2, 3) ───────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Small Video/Album Artwork Thumbnail + Back action + Title
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Audio Player",
                                tint = Color.White
                            )
                        }

                        // Mini Thumbnail
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onClose() },
                            color = Color(0xFF20222B)
                        ) {
                            if (isVideoAudio && currentVideo != null) {
                                val vData: Any? = currentVideo.let {
                                    if (it.isDemoSample) it.uri else if (!it.path.isNullOrBlank() && File(it.path).exists()) it.path else it.uri
                                }
                                if (vData != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(vData)
                                            .decoderFactory { result, options, _ ->
                                                VideoFrameDecoder(result.source, options)
                                            }
                                            .videoFrameMicros(1_000_000L)
                                            .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = song.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = PlayitCyan,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            } else if (song.albumArtUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(song.albumArtUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = PlayitCyan,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        }

                        // Title & Subtitle with marquee
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = if (song.artist.isNotBlank()) song.artist else (if (isVideoAudio) currentVideo?.parentFolderName ?: "Video Audio" else "Unknown Artist"),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Lyrics, Volume, Search Icon, Playlist/Queue Icon, and 3-dots Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Top Lyrics Button (Always accessible on top)
                        Surface(
                            onClick = { showLyricsSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (availableLyrics.isNotEmpty()) Color(0x3300D2FF) else Color(0x22FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (availableLyrics.isNotEmpty()) PlayitCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = "Show Lyrics",
                                    tint = if (availableLyrics.isNotEmpty()) PlayitCyan else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Lyrics",
                                    color = if (availableLyrics.isNotEmpty()) PlayitCyan else Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // 3-Dots Menu Dropdown Button
                        Box {
                            IconButton(
                                onClick = { showMoreMenuDropdown = true },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White
                                )
                            }

                            // 3-Dots Dropdown Menu
                            DropdownMenu(
                                expanded = showMoreMenuDropdown,
                                onDismissRequest = { showMoreMenuDropdown = false },
                                modifier = Modifier
                                    .background(Color(0xFF1E202B))
                                    .padding(vertical = 4.dp)
                            ) {
                                // 0. Song details & options (opens full options sheet matching Image 1)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Song details & options",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Tune, null, tint = VlcOrange, modifier = Modifier.size(20.dp))
                                    },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showSongOptionsSheet = true
                                    }
                                )

                                HorizontalDivider(color = Color(0xFF2C2F3D), modifier = Modifier.padding(vertical = 4.dp))

                                // 1. Search track
                                DropdownMenuItem(
                                    text = { Text("Search track", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        isSearchOpen = !isSearchOpen
                                        if (!isSearchOpen) searchQuery = ""
                                    }
                                )
                                // 2. Queue View
                                DropdownMenuItem(
                                    text = { Text(if (isQueueOpen) "Hide queue" else "Show queue", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        isQueueOpen = !isQueueOpen
                                    }
                                )
                                // 3. Grid/List View
                                if (isQueueOpen) {
                                    DropdownMenuItem(
                                        text = { Text(if (isGridMode) "List View" else "Grid View", color = Color.White, fontSize = 14.sp) },
                                        leadingIcon = { Icon(if (isGridMode) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                        onClick = {
                                            showMoreMenuDropdown = false
                                            isGridMode = !isGridMode
                                        }
                                    )
                                }

                                HorizontalDivider(color = Color(0xFF2C2F3D), modifier = Modifier.padding(vertical = 4.dp))

                                // 4. Sleep Timer
                                DropdownMenuItem(
                                    text = { Text("Sleep timer", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Bedtime, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showSleepTimerDialog = true
                                    }
                                )

                                // 5. Playback Speed
                                DropdownMenuItem(
                                    text = { Text("Playback speed", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Speed, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showSpeedDialog = true
                                    }
                                )

                                // 6. Jump to Time
                                DropdownMenuItem(
                                    text = { Text("Jump to Time", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Timelapse, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showJumpToTimeDialog = true
                                    }
                                )

                                // 7. Equalizer
                                DropdownMenuItem(
                                    text = { Text("Equalizer", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.GraphicEq, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showEqualizerSheet = true
                                    }
                                )

                                // 8. Play as video
                                DropdownMenuItem(
                                    text = { Text("Play as video", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.OndemandVideo, null, tint = PlayitCyan, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        val targetVideo = currentVideo ?: allVideos.firstOrNull { it.id == song.id }
                                        if (targetVideo != null) {
                                            onClose()
                                            onWatchAsVideo?.invoke(targetVideo)
                                        } else {
                                            Toast.makeText(context, "Cannot play as video: audio-only file", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                // 9. Bookmarks
                                DropdownMenuItem(
                                    text = { Text("Bookmarks", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.BookmarkBorder, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showBookmarksDialog = true
                                    }
                                )

                                // 10. A-B repeat
                                DropdownMenuItem(
                                    text = { Text("A-B repeat", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Repeat, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showAbRepeatDialog = true
                                    }
                                )

                                HorizontalDivider(color = Color(0xFF2C2F3D), modifier = Modifier.padding(vertical = 4.dp))

                                // 11. Add to Playlist
                                DropdownMenuItem(
                                    text = { Text("Add to Playlist", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        Toast.makeText(context, "Added '${song.title}' to playlist", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                // 12. Set as Ringtone
                                DropdownMenuItem(
                                    text = { Text("Set as Ringtone", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.RingVolume, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        Toast.makeText(context, "Set as Ringtone: ${song.title}", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                // 13. Edit Tags
                                DropdownMenuItem(
                                    text = { Text("Edit Tags", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Description, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showEditTagsDialog = true
                                    }
                                )

                                // 14. Copy Song Name
                                DropdownMenuItem(
                                    text = { Text("Copy Song Name", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Song Name", song.title)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Song name copied", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                // 15. Share the track info
                                DropdownMenuItem(
                                    text = { Text("Share the track info", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, "Listening to ${song.title} on V2S Player")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Track"))
                                    }
                                )

                                // 16. Song Info
                                DropdownMenuItem(
                                    text = { Text("Song Info", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Info, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showSongInfoDialog = true
                                    }
                                )

                                HorizontalDivider(color = Color(0xFF2C2F3D), modifier = Modifier.padding(vertical = 4.dp))

                                // 17. Audio player tips
                                DropdownMenuItem(
                                    text = { Text("Audio player tips", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Lightbulb, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showTipsDialog = "Audio Player Tips: Tap the top playlist icon to view all videos in List or Grid mode, or toggle sleep timer and equalizer from this menu."
                                    }
                                )

                                // 18. Playlist tips
                                DropdownMenuItem(
                                    text = { Text("Playlist tips", color = Color.White, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showMoreMenuDropdown = false
                                        showTipsDialog = "Playlist Tips: You can switch tracks instantly from the queue or grid, and change between Shuffle and Repeat modes from the bottom bar."
                                    }
                                )
                            }
                        }
                    }
                }

                // Search Bar Input
                AnimatedVisibility(visible = isSearchOpen) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search videos / audio...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = VlcOrange) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, "Clear", tint = Color.White)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E202B),
                            unfocusedContainerColor = Color(0xFF1E202B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = VlcOrange,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                // Top Volume Control Slider
                AnimatedVisibility(
                    visible = showTopVolumeSlider,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = Color(0xFF1E202B),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2D3C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { playerManager.decreaseVolume() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                    contentDescription = "Volume Down",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Slider(
                                value = playerState.volumePercent.toFloat(),
                                onValueChange = { vol ->
                                    playerManager.setSystemVolumePercent(vol.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = VlcOrange,
                                    activeTrackColor = VlcOrange,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )

                            IconButton(
                                onClick = { playerManager.increaseVolume() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume Up",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── MAIN BODY: (1: Poster View OR 2: All Videos Queue List / Grid) ─────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isQueueOpen) {
                    // ── QUEUE / ALL VIDEOS VIEW (Screenshot 1) ──────────────────────
                    if (isGridMode) {
                        // ── GRID VIEW (All videos in 2-columns grid) ───────────────
                        LazyVerticalGrid(
                            state = queueGridState,
                            columns = responsiveGridCells(
                                windowSize = windowSize,
                                compactColumns = 2,
                                mediumColumns = 3,
                                expandedColumns = 4
                            ),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isVideoAudio && allVideos.isNotEmpty()) {
                                itemsIndexed(displayVideos, key = { _, v -> "grid_video_${v.id}" }) { index, videoItem ->
                                    val isPlayingThis = videoItem.id == song.id
                                    Surface(
                                        onClick = {
                                            val songEquiv = SongItem(
                                                id = videoItem.id,
                                                uri = videoItem.uri,
                                                title = videoItem.title,
                                                artist = if (videoItem.parentFolderName.isNotBlank()) videoItem.parentFolderName else "Video",
                                                album = "Video Audio",
                                                durationMs = videoItem.durationMs,
                                                sizeBytes = videoItem.sizeBytes,
                                                path = videoItem.path,
                                                albumArtUri = videoItem.uri,
                                                lastPositionMs = videoItem.lastPositionMs,
                                                isFavorite = videoItem.isFavorite
                                            )
                                            onPlaySong(songEquiv)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPlayingThis) Color(0xFF26293A) else Color(0xFF191B24),
                                        border = if (isPlayingThis) androidx.compose.foundation.BorderStroke(1.5.dp, VlcOrange) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .background(Color.Black)
                                            ) {
                                                val vData: Any = if (videoItem.isDemoSample) videoItem.uri else if (!videoItem.path.isNullOrBlank() && File(videoItem.path).exists()) videoItem.path else videoItem.uri
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(vData)
                                                        .decoderFactory { result, options, _ ->
                                                            VideoFrameDecoder(result.source, options)
                                                        }
                                                        .videoFrameMicros(if (videoItem.durationMs > 2000L) 1_000_000L else 0L)
                                                        .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = videoItem.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )

                                                // Duration pill
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.75f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = videoItem.formattedDuration,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }

                                                if (isPlayingThis) {
                                                    Surface(
                                                        color = VlcOrange,
                                                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                                                        modifier = Modifier.align(Alignment.TopStart)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.GraphicEq,
                                                            contentDescription = "Playing",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .padding(2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = videoItem.title,
                                                color = if (isPlayingThis) VlcOrange else Color.White,
                                                fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(displaySongs, key = { _, s -> "grid_song_${s.id}" }) { index, songItem ->
                                    val isPlayingThis = songItem.id == song.id
                                    Surface(
                                        onClick = { onPlaySong(songItem) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPlayingThis) Color(0xFF26293A) else Color(0xFF191B24),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingThis) Icons.Filled.GraphicEq else Icons.Filled.MusicNote,
                                                contentDescription = null,
                                                tint = if (isPlayingThis) VlcOrange else PlayitCyan,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = songItem.title,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                maxLines = 2,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ── LIST VIEW (All videos list with animated equalizer, Screenshot 1) ──
                        LazyColumn(
                            state = queueListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isVideoAudio && allVideos.isNotEmpty()) {
                                itemsIndexed(displayVideos, key = { _, v -> "list_video_${v.id}" }) { index, videoItem ->
                                    val isPlayingThis = videoItem.id == song.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isPlayingThis) Color(0x33FF7A00) else Color.Transparent)
                                            .clickable {
                                                val songEquiv = SongItem(
                                                    id = videoItem.id,
                                                    uri = videoItem.uri,
                                                    title = videoItem.title,
                                                    artist = if (videoItem.parentFolderName.isNotBlank()) videoItem.parentFolderName else "Video",
                                                    album = "Video Audio",
                                                    durationMs = videoItem.durationMs,
                                                    sizeBytes = videoItem.sizeBytes,
                                                    path = videoItem.path,
                                                    albumArtUri = videoItem.uri,
                                                    lastPositionMs = videoItem.lastPositionMs,
                                                    isFavorite = videoItem.isFavorite
                                                )
                                                onPlaySong(songEquiv)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Playing Graphic Indicator on left (Screenshot 1)
                                        if (isPlayingThis) {
                                            PlayitEqualizerBarsIndicator(
                                                modifier = Modifier.size(width = 24.dp, height = 24.dp)
                                            )
                                        }

                                        // Video Thumbnail
                                        Surface(
                                            modifier = Modifier
                                                .width(72.dp)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            color = Color.Black
                                        ) {
                                            val vData: Any = if (videoItem.isDemoSample) videoItem.uri else if (!videoItem.path.isNullOrBlank() && File(videoItem.path).exists()) videoItem.path else videoItem.uri
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(vData)
                                                    .decoderFactory { result, options, _ ->
                                                        VideoFrameDecoder(result.source, options)
                                                    }
                                                    .videoFrameMicros(if (videoItem.durationMs > 2000L) 1_000_000L else 0L)
                                                    .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = videoItem.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // Title and Duration • SD/HD badge (Screenshot 1)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = videoItem.title,
                                                color = if (isPlayingThis) Color.White else Color.White.copy(alpha = 0.95f),
                                                fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${videoItem.formattedDuration} • ${if (videoItem.height >= 720) "HD" else "SD"}",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
                                        }

                                        // 3-dots Menu for individual video
                                        IconButton(
                                            onClick = {
                                                onWatchAsVideo?.invoke(videoItem)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "Options",
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(displaySongs, key = { _, s -> "list_song_${s.id}" }) { index, songItem ->
                                    val isPlayingThis = songItem.id == song.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isPlayingThis) Color(0x33FF7A00) else Color.Transparent)
                                            .border(
                                                width = if (isPlayingThis) 1.dp else 0.dp,
                                                color = if (isPlayingThis) VlcOrange else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onPlaySong(songItem) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (isPlayingThis) {
                                            PlayitEqualizerBarsIndicator(
                                                modifier = Modifier.size(width = 24.dp, height = 24.dp)
                                            )
                                        } else {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%02d", index + 1),
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.width(24.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            color = Color(0xFF222432)
                                        ) {
                                            if (songItem.albumArtUri != null) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(songItem.albumArtUri)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = songItem.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.MusicNote,
                                                    contentDescription = null,
                                                    tint = if (isPlayingThis) VlcOrange else PlayitCyan,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(10.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = songItem.title,
                                                color = if (isPlayingThis) VlcOrange else Color.White,
                                                fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${songItem.artist} • ${songItem.formattedDuration}",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ── POSTER & ALBUM ARTWORK VIEW (Screenshot 2) ──────────────────
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Large Square Poster Card (No accidental drag/swipe/scroll track changes)
                        Surface(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shadow(16.dp, RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
                            color = Color(0xFF14151C)
                        ) {
                            val isVid = isVideoAudio && currentVideo != null
                            val videoData = if (isVid) {
                                currentVideo?.let { if (it.isDemoSample) it.uri else if (!it.path.isNullOrBlank() && File(it.path).exists()) it.path else it.uri }
                            } else null
                            val albumArt = song.albumArtUri ?: playerState.notificationArtworkUri

                            if (isVid && videoData != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(videoData)
                                        .decoderFactory { result, options, _ ->
                                            VideoFrameDecoder(result.source, options)
                                        }
                                        .videoFrameMicros(1_000_000L)
                                        .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (albumArt != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(albumArt)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Default iconic clean PlayIt music note placeholder (Screenshot 1)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF14151C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = "Audio track",
                                        tint = Color.White.copy(alpha = 0.92f),
                                        modifier = Modifier.size(110.dp)
                                    )
                                }
                            }
                        }

                        // Large Title & Subtitle (Screenshot 2)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (song.artist.isNotBlank()) song.artist else (if (isVideoAudio) currentVideo?.parentFolderName ?: "Video Audio" else "Unknown Artist"),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Quick Seek Buttons: -10s and +10s (Screenshot 2)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { playerManager.seekBackward(10_000) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Replay10,
                                    contentDescription = "Replay 10s",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = { playerManager.seekForward(10_000) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Dedicated Volume Control Slider (Left decreases, Right increases, no percentage text)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(top = 4.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    playerManager.decreaseVolume()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (playerState.volumePercent == 0) {
                                        Icons.AutoMirrored.Filled.VolumeMute
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeDown
                                    },
                                    contentDescription = "Volume Down",
                                    tint = if (playerState.volumePercent == 0) VlcOrange else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Slider(
                                value = playerState.volumePercent.toFloat(),
                                onValueChange = { vol ->
                                    playerManager.setSystemVolumePercent(vol.toInt())
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = VlcOrange,
                                    activeTrackColor = VlcOrange,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                            )

                            IconButton(
                                onClick = { playerManager.increaseVolume() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume Up",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── PERSISTENT TRANSPORT SECTION (Screenshots 1 & 2) ──────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Track Indicator Capsule (Track: 2 / 54 • Ends at: 8:48:39 PM)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Track: ${currentQueueIndex + 1} / $totalQueueCount  •  Ends at: $endsAtTime",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                // 2. Progress Slider & Current/Remaining Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                ) {
                    Slider(
                        value = progress,
                        onValueChange = { frac ->
                            val targetMs = (frac * durMs).toLong()
                            playerManager.seekTo(targetMs)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = VlcOrange,
                            activeTrackColor = VlcOrange,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("audio_player_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = curFormatted,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = remFormatted,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Bottom 5 Control Buttons (Shuffle, Prev, Play/Pause, Next, Repeat)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Button 1: Shuffle
                    IconButton(
                        onClick = { playerManager.toggleShuffle() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.isShuffle) VlcOrange else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Button 2: Previous Track
                    IconButton(
                        onClick = { playPreviousTrack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Button 3: Play / Pause Large Circular Button
                    Surface(
                        onClick = { playerManager.togglePlayPause() },
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(12.dp, CircleShape)
                            .testTag("audio_play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color(0xFF0F1015),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Button 4: Next Track
                    IconButton(
                        onClick = { playNextTrack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Button 5: Repeat Mode
                    IconButton(
                        onClick = {
                            val newMode = when (playerState.repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                            playerManager.setRepeatMode(newMode)
                            val label = when (newMode) {
                                Player.REPEAT_MODE_ALL -> "Repeat All"
                                Player.REPEAT_MODE_ONE -> "Repeat One"
                                else -> "Repeat Off"
                            }
                            Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (playerState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) VlcOrange else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // ── Equalizer Modal Bottom Sheet ──────────────────────────────────────
        if (showEqualizerSheet) {
            PlayitEqualizerBottomSheet(
                playerManager = playerManager,
                onDismiss = { showEqualizerSheet = false }
            )
        }

        // ── Sleep Timer Dialog ────────────────────────────────────────────────
        if (showSleepTimerDialog) {
            SleepTimerSelectorDialog(
                currentMinutes = playerState.sleepTimerMinutesRemaining,
                onSelectMinutes = { min ->
                    playerManager.setSleepTimer(min)
                    showSleepTimerDialog = false
                    Toast.makeText(context, if (min != null) "Sleep timer: $min mins" else "Sleep timer cancelled", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showSleepTimerDialog = false }
            )
        }

        // ── Playback Speed Dialog ─────────────────────────────────────────────
        if (showSpeedDialog) {
            PlaybackSpeedSelectorDialog(
                currentSpeed = playerState.playbackSpeed,
                onSelectSpeed = { speed ->
                    playerManager.setPlaybackSpeed(speed)
                    showSpeedDialog = false
                    Toast.makeText(context, "Speed: ${speed}x", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showSpeedDialog = false }
            )
        }

        // ── Jump to Time Dialog ───────────────────────────────────────────────
        if (showJumpToTimeDialog) {
            JumpToTimeDialog(
                durationMs = durMs,
                onSeek = { ms ->
                    playerManager.seekTo(ms)
                    showJumpToTimeDialog = false
                },
                onDismiss = { showJumpToTimeDialog = false }
            )
        }

        // ── File Info Modal Dialog ────────────────────────────────────────────
        if (showSongInfoDialog) {
            SongInfoModalDialog(
                song = song,
                onDismiss = { showSongInfoDialog = false }
            )
        }

        // ── Bookmarks Dialog ──────────────────────────────────────────────────
        if (showBookmarksDialog) {
            AlertDialog(
                onDismissRequest = { showBookmarksDialog = false },
                containerColor = Color(0xFF1E202B),
                title = { Text("Bookmarks", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Bookmark saved at: $curFormatted", color = Color.White.copy(alpha = 0.8f))
                        Text("You can return to this exact timestamp anytime.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        Toast.makeText(context, "Bookmark saved at $curFormatted", Toast.LENGTH_SHORT).show()
                        showBookmarksDialog = false
                    }) {
                        Text("Save Bookmark", color = VlcOrange)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookmarksDialog = false }) {
                        Text("Close", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // ── A-B Repeat Dialog ─────────────────────────────────────────────────
        if (showAbRepeatDialog) {
            AlertDialog(
                onDismissRequest = { showAbRepeatDialog = false },
                containerColor = Color(0xFF1E202B),
                title = { Text("A-B Repeat", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text("Set Point A and Point B to loop a specific section of this track.", color = Color.White.copy(alpha = 0.8f))
                },
                confirmButton = {
                    TextButton(onClick = {
                        Toast.makeText(context, "Point A set at $curFormatted", Toast.LENGTH_SHORT).show()
                        showAbRepeatDialog = false
                    }) {
                        Text("Set Point A", color = VlcOrange)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAbRepeatDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // ── Lyrics Bottom Sheet ───────────────────────────────────────────────
        if (showLyricsSheet) {
            LyricsBottomSheet(
                song = song,
                currentPositionMs = playerState.currentPositionMs,
                onDismiss = { showLyricsSheet = false }
            )
        }

        // ── Tips Dialog ───────────────────────────────────────────────────────
        if (showTipsDialog != null) {
            AlertDialog(
                onDismissRequest = { showTipsDialog = null },
                containerColor = Color(0xFF1E202B),
                title = { Text("Help & Tips", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text(showTipsDialog ?: "", color = Color.White.copy(alpha = 0.85f)) },
                confirmButton = {
                    TextButton(onClick = { showTipsDialog = null }) {
                        Text("Got it", color = VlcOrange)
                    }
                }
            )
        }

        // ── Song Details & Options Bottom Sheet (Image 1) ──────────────────────
        if (showSongOptionsSheet) {
            PlayitSongOptionsBottomSheet(
                song = song,
                isPlaying = playerState.isPlaying,
                onPlayPause = { playerManager.togglePlayPause() },
                onPlayNext = { playNextTrack() },
                onDismiss = { showSongOptionsSheet = false },
                onOpenSongInfo = {
                    showSongOptionsSheet = false
                    showSongInfoDialog = true
                },
                onEditTags = {
                    showSongOptionsSheet = false
                    showEditTagsDialog = true
                }
            )
        }

        // ── Edit Tags Dialog ──────────────────────────────────────────────────
        if (showEditTagsDialog) {
            PlayitEditTagsDialog(
                song = song,
                onDismiss = { showEditTagsDialog = false }
            )
        }
    }
}

/**
 * Animated 3 Equalizer Bars for Currently Playing Item (Screenshot 1)
 */
@Composable
fun PlayitEqualizerBarsIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(h1)
                .background(VlcOrange, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(h2)
                .background(VlcOrange, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(h3)
                .background(VlcOrange, RoundedCornerShape(1.dp))
        )
    }
}

/**
 * Jump to Specific Time Dialog
 */
@Composable
fun JumpToTimeDialog(
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var minutesText by remember { mutableStateOf("") }
    var secondsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E202B),
        title = { Text("Jump to Time", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter timestamp to jump:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) minutesText = it },
                        label = { Text("Min", color = Color.White.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = VlcOrange
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) secondsText = it },
                        label = { Text("Sec", color = Color.White.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = VlcOrange
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = minutesText.toLongOrNull() ?: 0L
                val s = secondsText.toLongOrNull() ?: 0L
                val targetMs = ((m * 60) + s) * 1000L
                onSeek(targetMs.coerceIn(0L, durationMs))
            }) {
                Text("Jump", color = VlcOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

// ── Glowing Turntable Composable ──────────────────────────────────────────────

@Composable
fun PlayitGlowingTurntable(
    isPlaying: Boolean,
    artworkUri: android.net.Uri?,
    artworkPath: String?,
    isVideoAudio: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glowing Aura Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.minDimension / 2f) * if (isPlaying) auraPulse else 0.95f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VlcOrange.copy(alpha = if (isPlaying) 0.35f else 0.1f),
                        PlayitCyan.copy(alpha = if (isPlaying) 0.15f else 0.04f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Outer Vinyl Disc
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .rotate(if (isPlaying) rotation else 0f)
                .clip(CircleShape)
                .background(Color(0xFF14151B))
                .border(2.dp, Color(0xFF2D303E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Vinyl grooves rings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2, size.height / 2)
                for (r in listOf(0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.85f)) {
                    drawCircle(
                        color = Color(0xFF222430),
                        radius = (size.minDimension / 2) * r,
                        center = centerOffset,
                        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Center Artwork Circle
            Surface(
                modifier = Modifier
                    .fillMaxSize(0.48f)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF383A4A), CircleShape),
                color = Color(0xFF1A1C25)
            ) {
                if (artworkUri != null || artworkPath != null) {
                    if (isVideoAudio) {
                        val vData: Any? = if (!artworkPath.isNullOrBlank() && File(artworkPath).exists()) artworkPath else artworkUri
                        if (vData != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(vData)
                                    .decoderFactory { result, options, _ ->
                                        VideoFrameDecoder(result.source, options)
                                    }
                                    .videoFrameMicros(1_000_000L)
                                    .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Video Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        val imgData = artworkUri ?: if (!artworkPath.isNullOrBlank() && File(artworkPath).exists()) artworkPath else null
                        if (imgData != null) {
                            AsyncImage(
                                model = imgData,
                                contentDescription = "Album Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = PlayitCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Center spindle hole
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1015))
                    .border(1.5.dp, Color(0xFF4B4F63), CircleShape)
            )
        }
    }
}

// ── 5-Band Equalizer Modal Bottom Sheet ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayitEqualizerBottomSheet(
    playerManager: VyloPlayerManager,
    onDismiss: () -> Unit
) {
    val playerState by playerManager.playerState.collectAsState()
    var selectedPreset by remember { mutableStateOf(playerState.equalizerPreset) }
    var bassBoostEnabled by remember { mutableStateOf(false) }
    var virtualizerEnabled by remember { mutableStateOf(false) }
    val bands = remember { listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz") }
    var bandLevels by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161822),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFF383A48)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equalizer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                TextButton(onClick = {
                    selectedPreset = EqualizerPreset.FLAT
                    bandLevels = listOf(0f, 0f, 0f, 0f, 0f)
                    playerManager.setEqualizerPreset(EqualizerPreset.FLAT)
                }) {
                    Text("Reset", color = VlcOrange)
                }
            }

            // Presets Horizontal Scroll
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EqualizerPreset.entries.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        onClick = {
                            selectedPreset = preset
                            playerManager.setEqualizerPreset(preset)
                            bandLevels = when (preset) {
                                EqualizerPreset.BASS_BOOST -> listOf(6f, 4f, 1f, 0f, -1f)
                                EqualizerPreset.TREBLE_BOOST -> listOf(-1f, 0f, 1f, 4f, 6f)
                                EqualizerPreset.VOCAL -> listOf(-2f, 2f, 5f, 3f, 0f)
                                EqualizerPreset.ROCK -> listOf(4f, 2f, -1f, 2f, 5f)
                                EqualizerPreset.POP -> listOf(-1f, 2f, 4f, 2f, -1f)
                                EqualizerPreset.JAZZ -> listOf(3f, 1f, 0f, 2f, 4f)
                                EqualizerPreset.FLAT -> listOf(0f, 0f, 0f, 0f, 0f)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) VlcOrange else Color(0xFF222432)
                    ) {
                        Text(
                            text = preset.label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5-Band Vertical Slider Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF1E202C), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bands.forEachIndexed { index, freq ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+${bandLevels[index].toInt()}dB", color = VlcOrange, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(6.dp)
                                .background(Color(0xFF2E3142), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(((bandLevels[index] + 10f) / 20f).coerceIn(0.1f, 1f))
                                    .background(VlcOrange, RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(freq, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }

            // Bass Boost & 3D Audio Switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E202C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bass Boost", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Enhance low-end audio thump", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
                Switch(
                    checked = bassBoostEnabled,
                    onCheckedChange = {
                        bassBoostEnabled = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VlcOrange)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E202C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("3D Surround Sound", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Virtualize spatial soundstage", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
                Switch(
                    checked = virtualizerEnabled,
                    onCheckedChange = {
                        virtualizerEnabled = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VlcOrange)
                )
            }
        }
    }
}

// ── Sleep Timer Selector Dialog ───────────────────────────────────────────────

@Composable
fun SleepTimerSelectorDialog(
    currentMinutes: Int?,
    onSelectMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = { Text("Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("Turn Off", null),
                    Pair("15 Minutes", 15),
                    Pair("30 Minutes", 30),
                    Pair("45 Minutes", 45),
                    Pair("60 Minutes", 60),
                    Pair("90 Minutes", 90)
                ).forEach { (label, min) ->
                    val isSelected = currentMinutes == min
                    Surface(
                        onClick = { onSelectMinutes(min) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) VlcOrange else Color(0xFF272938),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VlcOrange)
            }
        }
    )
}

// ── Playback Speed Selector Dialog ────────────────────────────────────────────

@Composable
fun PlaybackSpeedSelectorDialog(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = { Text("Speed Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Surface(
                        onClick = { onSelectSpeed(speed) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) VlcOrange else Color(0xFF272938),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VlcOrange)
            }
        }
    )
}

// ── Song Info Modal Dialog ────────────────────────────────────────────────────

@Composable
fun SongInfoModalDialog(
    song: SongItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = { Text("File Info", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoItemRow(label = "Title", value = song.title)
                InfoItemRow(label = "Artist", value = if (song.artist.isNotBlank()) song.artist else "Unknown")
                InfoItemRow(label = "Album", value = if (song.album.isNotBlank()) song.album else "Unknown")
                InfoItemRow(label = "Duration", value = song.formattedDuration)
                InfoItemRow(label = "Path", value = song.path ?: song.uri.toString())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = VlcOrange, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun InfoItemRow(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ── Song Details & Options Bottom Sheet (Matches Image 1) ─────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayitSongOptionsBottomSheet(
    song: SongItem,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onDismiss: () -> Unit,
    onOpenSongInfo: () -> Unit,
    onEditTags: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isFavorite by remember(song.id) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E202B),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF545458))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // ── Song Header (Art + Title + Artist • Album + Duration) ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color(0xFF2C2410)
                ) {
                    if (song.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(song.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = VlcOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (song.artist.isNotBlank()) song.artist else "Unknown Artist"} • ${if (song.album.isNotBlank()) song.album else "Music"}",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.formattedDuration,
                        color = Color(0xFF636366),
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFF2C2F3D),
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )

            // ── Quick Actions Row (Play/Pause, Next, Like, Share) ─────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayitQuickActionButton(
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    label = if (isPlaying) "Pause" else "Play",
                    tint = VlcOrange,
                    onClick = {
                        onPlayPause()
                    }
                )

                PlayitQuickActionButton(
                    icon = Icons.Filled.SkipNext,
                    label = "Next",
                    tint = Color.White,
                    onClick = {
                        onPlayNext()
                        onDismiss()
                    }
                )

                PlayitQuickActionButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = if (isFavorite) "Liked" else "Like",
                    tint = if (isFavorite) VlcOrange else Color.White,
                    onClick = {
                        isFavorite = !isFavorite
                        Toast.makeText(
                            context,
                            if (isFavorite) "Added to favorites" else "Removed from favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                PlayitQuickActionButton(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    tint = Color.White,
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "Listening to ${song.title} - ${song.artist}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Track"))
                        onDismiss()
                    }
                )
            }

            HorizontalDivider(
                color = Color(0xFF2C2F3D),
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )

            // ── Scrollable Menu Actions List ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PlayitMenuRowItem(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    label = "Add to Play Queue",
                    onClick = {
                        Toast.makeText(context, "Added to Play Queue", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                PlayitMenuRowItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = "Add to Playlist",
                    onClick = {
                        Toast.makeText(context, "Added '${song.title}' to playlist", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                HorizontalDivider(color = Color(0xFF252734), modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))

                PlayitMenuRowItem(
                    icon = Icons.Filled.RingVolume,
                    label = "Set as Ringtone",
                    onClick = {
                        Toast.makeText(context, "Set as Ringtone: ${song.title}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                PlayitMenuRowItem(
                    icon = Icons.Filled.Description,
                    label = "Edit Tags",
                    onClick = {
                        onEditTags()
                    }
                )

                PlayitMenuRowItem(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy Song Name",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Song Name", song.title)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Song name copied", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                PlayitMenuRowItem(
                    icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
                    label = "Create Shortcut",
                    onClick = {
                        Toast.makeText(context, "Shortcut created for ${song.title}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                HorizontalDivider(color = Color(0xFF252734), modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))

                PlayitMenuRowItem(
                    icon = Icons.Filled.Info,
                    label = "Song Info",
                    trailingText = song.formattedDuration,
                    onClick = {
                        onOpenSongInfo()
                    }
                )

                PlayitMenuRowItem(
                    icon = Icons.Filled.Folder,
                    label = "Show in Folder",
                    onClick = {
                        Toast.makeText(context, "Location: ${song.path ?: "Internal Storage"}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                HorizontalDivider(color = Color(0xFF252734), modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))

                PlayitMenuRowItem(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    tint = Color(0xFFFF453A),
                    onClick = {
                        Toast.makeText(context, "Track removed: ${song.title}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayitQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2D3C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = Color(0xFFAEAEB2),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlayitMenuRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = label,
            color = tint,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (trailingText != null) {
            Text(
                text = trailingText,
                color = Color(0xFF636366),
                fontSize = 12.sp
            )
        }
    }
}

// ── Edit Tags Dialog ──────────────────────────────────────────────────────────

@Composable
fun PlayitEditTagsDialog(
    song: SongItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E202B),
        title = {
            Text("Edit Tags", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF14151C),
                        unfocusedContainerColor = Color(0xFF14151C),
                        focusedIndicatorColor = VlcOrange,
                        unfocusedIndicatorColor = Color(0xFF2A2D3C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF14151C),
                        unfocusedContainerColor = Color(0xFF14151C),
                        focusedIndicatorColor = VlcOrange,
                        unfocusedIndicatorColor = Color(0xFF2A2D3C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF14151C),
                        unfocusedContainerColor = Color(0xFF14151C),
                        focusedIndicatorColor = VlcOrange,
                        unfocusedIndicatorColor = Color(0xFF2A2D3C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Toast.makeText(context, "Tags updated: $title", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Save", color = VlcOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
