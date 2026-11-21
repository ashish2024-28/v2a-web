
package com.example.ui.screens.songs

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.FastScroller
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import com.example.data.model.PlaylistData
import com.example.ui.components.DeleteGenericConfirmationDialog
import com.example.ui.components.DeleteMultipleConfirmationDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import com.example.player.VyloPlayerManager
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.V2SFolderSymbol
import com.example.ui.components.V2SSongsSymbol
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcAmber
import com.example.ui.theme.VlcOrange
import com.example.ui.theme.VlcOrangeDark
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd

// ── Sort order ────────────────────────────────────────────────────────────────

enum class SongSortOrder(val label: String) {
    TRACK_ASC("Track • Ascending"),
    TRACK_DESC("Track • Descending"),
    NAME_ASC("File name • A → Z"),
    NAME_DESC("File name • Z → A"),
    LENGTH_SHORT("Length • Shortest first"),
    LENGTH_LONG("Length • Longest first"),
    DATE_OLD("Release date • Oldest first"),
    DATE_NEW("Release date • Newest first"),
    ADDED_OLD("Recently added • Oldest first"),
    ADDED_NEW("Recently added • Newest first")
}

// ── Data classes ──────────────────────────────────────────────────────────────

data class AudioFolder(
    val name: String,
    val path: String,
    val songCount: Int,
    val totalDurationMs: Long
)

data class AudioPlaylist(
    val id: String,
    val name: String,
    val songCount: Int,
    val songs: List<SongItem> = emptyList()
)

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    songs: List<SongItem>,
    allVideos: List<VideoItem> = emptyList(),
    recentlyWatchedSongs: List<SongItem> = emptyList(),
    playerManager: VyloPlayerManager,
    onPlaySong: (SongItem) -> Unit,
    onPlayPrevious: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onWatchAsVideo: ((VideoItem) -> Unit)? = null,
    onOpenPlayer: () -> Unit = {},
    onRefreshScan: () -> Unit = {},
    onPlayLastPlayed: () -> Unit = {},
    lyricsEnabled: Boolean = true,
    playlists: List<PlaylistData> = emptyList(),
    onCreatePlaylist: (String, String) -> Unit = { _, _ -> },
    onDeletePlaylist: (String) -> Unit = {},
    onRenamePlaylist: (String, String) -> Unit = { _, _ -> },
    onDeleteSong: (SongItem) -> Unit = {},
    onDeleteMultipleSongs: (List<SongItem>) -> Unit = {},
    onShareSong: (SongItem) -> Unit = {},
    onShareMultipleSongs: (List<SongItem>) -> Unit = {},
    onAddMultipleSongsToPlaylist: (String, List<SongItem>) -> Unit = { _, _ -> },
    onPlayMultipleSongs: (List<SongItem>) -> Unit = {},
    onClearSongHistory: () -> Unit = {},
    onRemoveSongFromHistory: (SongItem) -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerState by playerManager.playerState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isFavoriteAlbum by remember { mutableStateOf(false) }
    var showDisplaySettingsSheet by remember { mutableStateOf(false) }
    var showTrackNumbers by remember { mutableStateOf(true) }
    var sortOrder by remember { mutableStateOf(SongSortOrder.TRACK_ASC) }

    // Multi-select & Batch operations state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongIds by remember { mutableStateOf(setOf<Long>()) }
    var songToDelete by remember { mutableStateOf<SongItem?>(null) }
    var showDeleteMultipleConfirmation by remember { mutableStateOf(false) }
    var showAddToPlaylistMultipleDialog by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<SongItem?>(null) }

    // Tab state: 0=Songs, 1=Folders, 2=Playlists
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Folders", "Playlists")

    val filteredSongs = remember(songs, searchQuery, sortOrder) {
        val base = if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
            }
        }

        when (sortOrder) {
            SongSortOrder.TRACK_ASC -> base
            SongSortOrder.TRACK_DESC -> base.reversed()
            SongSortOrder.NAME_ASC -> base.sortedBy { it.title.lowercase() }
            SongSortOrder.NAME_DESC -> base.sortedByDescending { it.title.lowercase() }
            SongSortOrder.LENGTH_SHORT -> base.sortedBy { it.durationMs }
            SongSortOrder.LENGTH_LONG -> base.sortedByDescending { it.durationMs }
            SongSortOrder.DATE_OLD -> base.sortedBy { it.dateAddedSec }
            SongSortOrder.DATE_NEW -> base.sortedByDescending { it.dateAddedSec }
            SongSortOrder.ADDED_OLD -> base.sortedBy { it.dateAddedSec }
            SongSortOrder.ADDED_NEW -> base.sortedByDescending { it.dateAddedSec }
        }
    }

    val audioFolders = remember(songs) {
        songs.groupBy { song ->
            val path = song.uri.toString()
            val idx = path.lastIndexOf('/')
            if (idx > 0) {
                path.substring(0, idx).substringAfterLast('/')
            } else {
                "Music"
            }
        }.map { (folderName, folderSongs) ->
            AudioFolder(
                name = folderName,
                path = folderName,
                songCount = folderSongs.size,
                totalDurationMs = folderSongs.sumOf { it.durationMs }
            )
        }.sortedBy { it.name }
    }

    var selectedAudioFolder by remember { mutableStateOf<AudioFolder?>(null) }
    var selectedAudioPlaylist by remember { mutableStateOf<AudioPlaylist?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var playlistToRename by remember { mutableStateOf<PlaylistData?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistData?>(null) }
    var renamePlaylistTitle by remember { mutableStateOf("") }

    val songSectionPlaylists = remember(playlists) {
        playlists.filter { pl ->
            !pl.isSystem && (
                pl.category.equals("SONGS", ignoreCase = true) ||
                pl.id.startsWith("pl_songs_")
            )
        }.map { pl ->
            val plSongs = pl.items.mapNotNull { it.songItem }
            AudioPlaylist(
                id = pl.id,
                name = pl.title,
                songCount = plSongs.size,
                songs = plSongs
            )
        }
    }

    val defaultAudioPlaylists = remember(songs, playlists) {
        val favPlaylist = playlists.firstOrNull { it.isFavorites || it.id == "favorites" }
        val favSongs = favPlaylist?.items?.mapNotNull { it.songItem } ?: songs.filter { it.isFavorite }
        listOf(
            AudioPlaylist(
                "favourites",
                "Favorites",
                favSongs.size,
                favSongs
            )
        )
    }

    val audioPlaylists = songSectionPlaylists + defaultAudioPlaylists

    val activeAudioPlaylist = remember(selectedAudioPlaylist, audioPlaylists) {
        if (selectedAudioPlaylist != null) {
            audioPlaylists.firstOrNull { it.id == selectedAudioPlaylist?.id } ?: selectedAudioPlaylist
        } else null
    }

    val thisYearSongs = remember(filteredSongs) {
        filteredSongs.filter { it.dateAddedSec >= 1704067200L }
    }

    val lastYearSongs = remember(filteredSongs) {
        filteredSongs.filter {
            it.dateAddedSec in 1672531200L..1704067199L
        }
    }

    val olderSongs = remember(filteredSongs) {
        filteredSongs.filter { it.dateAddedSec < 1672531200L }
    }

    val totalDurationMs = remember(songs) {
        songs.sumOf { it.durationMs }
    }

    val totalHours = totalDurationMs / (1000 * 60 * 60)
    val totalMins = (totalDurationMs / (1000 * 60)) % 60
    val totalSecs = (totalDurationMs / 1000) % 60

    val formattedTotalTime =
        if (totalHours > 0) {
            "${totalHours}h ${totalMins}min ${totalSecs}s"
        } else {
            "${totalMins}min ${totalSecs}s"
        }

    BackHandler(
        enabled = selectedAudioFolder != null || selectedAudioPlaylist != null
    ) {
        if (selectedAudioFolder != null) {
            selectedAudioFolder = null
        } else if (selectedAudioPlaylist != null) {
            selectedAudioPlaylist = null
        }
    }

    // ── Responsive screen wrapper ─────────────────────────────────────────────
    ResponsiveScreen(
        modifier = modifier
    ) { windowSize, horizontalPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {

                // ── Header with V2S Brand Logo ────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = horizontalPadding,
                            vertical = 5.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        V2SBrandLogo(
                            showTagline = true
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "${songs.size}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 1.5.dp
                                )
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isSearchVisible = !isSearchVisible
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search songs",
                                tint = if (
                                    isSearchVisible || searchQuery.isNotEmpty()
                                ) {
                                    VlcOrange
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onAddLinkClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = "Add Song Link",
                                tint = VlcOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenPlayer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Equalizer,
                                contentDescription = "Equalizer / Audio Player",
                                tint = VlcOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                showDisplaySettingsSheet = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search songs or artists…",
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                null,
                                tint = VlcOrange
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        null
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VlcOrange
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp
                            )
                    )
                }

                // ── Tabs: Songs / Folders / Playlists ─────────────────────────
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = VlcOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                tabPositions[selectedTab]
                            ),
                            color = VlcOrange
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    when (index) {
                                        0 -> {
                                            V2SSongsSymbol(size = 18.dp)
                                        }

                                        1 -> {
                                            V2SFolderSymbol(size = 18.dp)
                                        }

                                        2 -> {
                                            Icon(
                                                Icons.AutoMirrored.Filled.QueueMusic,
                                                contentDescription = null,
                                                tint = if (
                                                    selectedTab == index
                                                ) {
                                                    VlcOrange
                                                } else {
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = title,
                                        fontWeight = if (
                                            selectedTab == index
                                        ) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.SemiBold
                                        },
                                        fontSize = 15.sp,
                                        color = if (
                                            selectedTab == index
                                        ) {
                                            VlcOrange
                                        } else {
                                            MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        )
                    }
                }

                // ── Tab Content ──────────────────────────────────────────────
                when (selectedTab) {
                    0 -> SongsTabContent(
                        filteredSongs = filteredSongs,
                        thisYearSongs = thisYearSongs,
                        lastYearSongs = lastYearSongs,
                        olderSongs = olderSongs,
                        playerState = playerState,
                        showTrackNumbers = showTrackNumbers,
                        isFavoriteAlbum = isFavoriteAlbum,
                        formattedTotalTime = formattedTotalTime,
                        onPlaySong = onPlaySong,
                        onToggleFavorite = {
                            isFavoriteAlbum = !isFavoriteAlbum
                        },
                        playerManager = playerManager,
                        context = context,
                        isSelectionMode = isSelectionMode,
                        selectedSongIds = selectedSongIds,
                        onToggleSelectSong = { id ->
                            selectedSongIds = if (selectedSongIds.contains(id)) {
                                selectedSongIds - id
                            } else {
                                selectedSongIds + id
                            }
                            if (selectedSongIds.isEmpty() && isSelectionMode) {
                                // keep selection mode or let user exit
                            }
                        },
                        onStartSelectionWithSong = { id ->
                            isSelectionMode = true
                            selectedSongIds = setOf(id)
                        },
                        onDeleteSongRequest = { song ->
                            songToDelete = song
                        },
                        onShareSongRequest = { song ->
                            onShareSong(song)
                        },
                        onAddToPlaylistRequest = { song ->
                            songForAddToPlaylist = song
                        },
                        onAddLinkClick = onAddLinkClick,
                        onRefreshScan = onRefreshScan
                    )

                    1 -> FoldersTabContent(
                        folders = audioFolders,
                        songs = songs,
                        selectedFolder = selectedAudioFolder,
                        onFolderClick = {
                            selectedAudioFolder = it
                        },
                        onBack = {
                            selectedAudioFolder = null
                        },
                        onPlaySong = onPlaySong,
                        playerState = playerState
                    )

                    2 -> PlaylistsTabContent(
                        playlists = audioPlaylists,
                        selectedPlaylist = activeAudioPlaylist,
                        onPlaylistClick = {
                            selectedAudioPlaylist = it
                        },
                        onBack = {
                            selectedAudioPlaylist = null
                        },
                        onNewPlaylist = {
                            showCreatePlaylistDialog = true
                        },
                        onRenameClick = { pl ->
                            val found = playlists.find { it.id == pl.id }
                            if (found != null) {
                                playlistToRename = found
                                renamePlaylistTitle = found.title
                            }
                        },
                        onDeleteClick = { pl ->
                            val found = playlists.find { it.id == pl.id }
                            if (found != null) {
                                playlistToDelete = found
                            }
                        },
                        onPlaySong = onPlaySong,
                        playerState = playerState,
                        context = context
                    )
                }
            }

            // ── Floating Multi-Select Action Bar ──────────────────────────────
            AnimatedVisibility(
                visible = isSelectionMode && selectedSongIds.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E1E24),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333340))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VlcOrange
                        ) {
                            Text(
                                text = "${selectedSongIds.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedSongIds = if (selectedSongIds.size == songs.size) {
                                    emptySet()
                                } else {
                                    songs.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedSongIds.size == songs.size) Icons.Filled.Clear else Icons.Filled.SelectAll,
                                contentDescription = "Select all",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val selectedSongs = songs.filter { it.id in selectedSongIds }
                                if (selectedSongs.isNotEmpty()) {
                                    onPlayMultipleSongs(selectedSongs)
                                    isSelectionMode = false
                                    selectedSongIds = emptySet()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Selected",
                                tint = PlayitCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                showAddToPlaylistMultipleDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Add to playlist",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val selectedSongs = songs.filter { it.id in selectedSongIds }
                                onShareMultipleSongs(selectedSongs)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                showDeleteMultipleConfirmation = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Deletion Confirmation Dialogs ─────────────────────────────────────────
    songToDelete?.let { song ->
        DeleteGenericConfirmationDialog(
            title = "Delete Song Permanently?",
            message = "Are you sure you want to permanently delete \"${song.title}\" from your device storage? This cannot be undone.",
            onConfirm = {
                onDeleteSong(song)
                songToDelete = null
            },
            onDismiss = {
                songToDelete = null
            }
        )
    }

    if (showDeleteMultipleConfirmation) {
        DeleteMultipleConfirmationDialog(
            count = selectedSongIds.size,
            mediaType = "songs",
            onConfirm = {
                val toDelete = songs.filter { it.id in selectedSongIds }
                onDeleteMultipleSongs(toDelete)
                selectedSongIds = emptySet()
                isSelectionMode = false
                showDeleteMultipleConfirmation = false
            },
            onDismiss = {
                showDeleteMultipleConfirmation = false
            }
        )
    }

    if (showAddToPlaylistMultipleDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistMultipleDialog = false },
            title = { Text("Add ${selectedSongIds.size} Songs to Playlist", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (songSectionPlaylists.isEmpty()) {
                        Text("No songs playlists found. Create one first.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            items(songSectionPlaylists, key = { it.id }) { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val selectedSongs = songs.filter { it.id in selectedSongIds }
                                            onAddMultipleSongsToPlaylist(pl.id, selectedSongs)
                                            showAddToPlaylistMultipleDialog = false
                                            isSelectionMode = false
                                            selectedSongIds = emptySet()
                                            Toast.makeText(context, "Added to ${pl.name}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = VlcOrange)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(pl.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddToPlaylistMultipleDialog = false
                    showCreatePlaylistDialog = true
                }) {
                    Text("New Playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistMultipleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    songForAddToPlaylist?.let { song ->
        AlertDialog(
            onDismissRequest = { songForAddToPlaylist = null },
            title = { Text("Add \"${song.title}\" to Playlist", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (songSectionPlaylists.isEmpty()) {
                        Text("No songs playlists found. Create one first.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            items(songSectionPlaylists, key = { it.id }) { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddMultipleSongsToPlaylist(pl.id, listOf(song))
                                            songForAddToPlaylist = null
                                            Toast.makeText(context, "Added to ${pl.name}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = VlcOrange)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(pl.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    songForAddToPlaylist = null
                    showCreatePlaylistDialog = true
                }) {
                    Text("New Playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { songForAddToPlaylist = null }) {
                    Text("Cancel")
                }
            }
        )
    }



    // ── Display & Sort Settings Sheet ─────────────────────────────────────────
    if (showDisplaySettingsSheet) {
        SongsDisplaySettingsSheet(
            showTrackNumbers = showTrackNumbers,
            onToggleShowTrackNumbers = {
                showTrackNumbers = it
            },
            sortOrder = sortOrder,
            onSortOrderSelected = {
                sortOrder = it
            },
            onDismiss = {
                showDisplaySettingsSheet = false
            }
        )
    }

    // ── Create Playlist Dialog ────────────────────────────────────────────────
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = {
                Text(
                    text = "New Playlist",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = {
                        newPlaylistName = it
                    },
                    label = {
                        Text("Playlist Name")
                    },
                    placeholder = {
                        Text("e.g. Workout Mix, Road Trip")
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim(), "SONGS")
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange
                    )
                ) {
                    Text(
                        "Create",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreatePlaylistDialog = false
                        newPlaylistName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Songs Playlist Dialog
    playlistToRename?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToRename = null },
            title = {
                Text(
                    text = "Rename Playlist",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = renamePlaylistTitle,
                    onValueChange = { renamePlaylistTitle = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renamePlaylistTitle.isNotBlank()) {
                            onRenamePlaylist(pl.id, renamePlaylistTitle.trim())
                            playlistToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VlcOrange)
                ) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Songs Playlist Dialog
    playlistToDelete?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = {
                Text(
                    text = "Delete Playlist?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${pl.title}\"? The songs will remain in your audio library.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(pl.id)
                        playlistToDelete = null
                        if (selectedAudioPlaylist?.id == pl.id) {
                            selectedAudioPlaylist = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Songs Tab Content ─────────────────────────────────────────────────────────

@Composable
private fun SongsTabContent(
    filteredSongs: List<SongItem>,
    thisYearSongs: List<SongItem>,
    lastYearSongs: List<SongItem>,
    olderSongs: List<SongItem>,
    playerState: com.example.player.PlayerUiState,
    showTrackNumbers: Boolean,
    isFavoriteAlbum: Boolean,
    formattedTotalTime: String,
    onPlaySong: (SongItem) -> Unit,
    onToggleFavorite: () -> Unit,
    playerManager: VyloPlayerManager,
    context: android.content.Context,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<Long> = emptySet(),
    onToggleSelectSong: (Long) -> Unit = {},
    onStartSelectionWithSong: (Long) -> Unit = {},
    onDeleteSongRequest: (SongItem) -> Unit = {},
    onShareSongRequest: (SongItem) -> Unit = {},
    onAddToPlaylistRequest: (SongItem) -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    onRefreshScan: () -> Unit = {}
) {
    if (filteredSongs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VlcOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        null,
                        tint = VlcOrange.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "No songs or links yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    "Add any YouTube Music, SoundCloud, or MP3 link, or scan your phone memory for audio files.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = onAddLinkClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Add Song / Music Link", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRefreshScan,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Phone Memory")
                }
            }
        }

        return
    }

    val songsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val playingSongId = if (!playerState.isVideoAudio) playerState.currentSong?.id else null

    LaunchedEffect(playingSongId) {
        if (playingSongId != null) {
            var itemIdx = 0
            var found = false
            if (thisYearSongs.isNotEmpty()) {
                val idxInThisYear = thisYearSongs.indexOfFirst { it.id == playingSongId }
                if (idxInThisYear >= 0) {
                    itemIdx = 1 + idxInThisYear
                    found = true
                }
            }
            if (!found && lastYearSongs.isNotEmpty()) {
                val idxInLastYear = lastYearSongs.indexOfFirst { it.id == playingSongId }
                if (idxInLastYear >= 0) {
                    val prevOffset = if (thisYearSongs.isNotEmpty()) 1 + thisYearSongs.size else 0
                    itemIdx = prevOffset + 1 + idxInLastYear
                    found = true
                }
            }
            if (!found && olderSongs.isNotEmpty()) {
                val idxInOlder = olderSongs.indexOfFirst { it.id == playingSongId }
                if (idxInOlder >= 0) {
                    val prevOffset = (if (thisYearSongs.isNotEmpty()) 1 + thisYearSongs.size else 0) +
                        (if (lastYearSongs.isNotEmpty()) 1 + lastYearSongs.size else 0)
                    itemIdx = prevOffset + 1 + idxInOlder
                    found = true
                }
            }
            if (found) {
                // item 0 is the top vinyl disc header
                songsListState.animateScrollToItem((itemIdx + 1).coerceAtLeast(0))
            }
        }
    }

    FastScroller(
        listState = songsListState,
        itemCount = filteredSongs.size,
        indexLabel = { idx ->
            filteredSongs.getOrNull(idx)?.title?.take(1)?.uppercase() ?: ""
        }
    ) {
        LazyColumn(
            state = songsListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (playerState.currentSong != null) {
                    110.dp
                } else {
                    24.dp
                }
            )
        ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    VinylRecordDisc(
                        modifier = Modifier.size(100.dp),
                        isPlaying = playerState.isPlaying &&
                            playerState.currentSong != null && !playerState.isVideoAudio
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (!playerState.isVideoAudio && playerState.currentSong != null) {
                                playerState.currentSong?.album ?: "All Songs"
                            } else {
                                "All Songs"
                            },
                            style = MaterialTheme.typography.titleLarge, fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = if (!playerState.isVideoAudio && playerState.currentSong != null) {
                                playerState.currentSong?.artist ?: "Various Artists"
                            } else {
                                "Various Artists"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "${filteredSongs.size} songs • $formattedTotalTime",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (filteredSongs.isNotEmpty()) {
                                onPlaySong(filteredSongs.first())
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VlcOrange
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 28.dp,
                            vertical = 10.dp
                        ),
                        modifier = Modifier.testTag("play_album_button")
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            "PLAY ALL",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            if (filteredSongs.isNotEmpty()) {
                                onPlaySong(filteredSongs.shuffled().first())
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Shuffle,
                            "Shuffle",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Add to Playlist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            "Add to playlist",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite
                    ) {
                        Icon(
                            imageVector = if (isFavoriteAlbum) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = "Favorite",
                            tint = if (isFavoriteAlbum) {
                                VlcOrange
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        if (thisYearSongs.isNotEmpty()) {
            item {
                TimelineSectionHeader(title = "This year")
            }

            items(
                thisYearSongs,
                key = { it.id }
            ) { song ->
                V2SSongRowItem(
                    song = song,
                    isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id,
                    trackNumber = if (showTrackNumbers) {
                        filteredSongs.indexOf(song) + 1
                    } else {
                        null
                    },
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelectSong(song.id)
                        } else {
                            onPlaySong(song)
                        }
                    },
                    playerManager = playerManager,
                    context = context,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedSongIds.contains(song.id),
                    onToggleSelect = { onToggleSelectSong(song.id) },
                    onLongClick = { onStartSelectionWithSong(song.id) },
                    onDeleteRequest = { onDeleteSongRequest(song) },
                    onShareRequest = { onShareSongRequest(song) },
                    onAddToPlaylistRequest = { onAddToPlaylistRequest(song) }
                )
            }
        }

        if (lastYearSongs.isNotEmpty()) {
            item {
                TimelineSectionHeader(title = "Last year")
            }

            items(
                lastYearSongs,
                key = { it.id }
            ) { song ->
                V2SSongRowItem(
                    song = song,
                    isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id,
                    trackNumber = if (showTrackNumbers) {
                        filteredSongs.indexOf(song) + 1
                    } else {
                        null
                    },
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelectSong(song.id)
                        } else {
                            onPlaySong(song)
                        }
                    },
                    playerManager = playerManager,
                    context = context,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedSongIds.contains(song.id),
                    onToggleSelect = { onToggleSelectSong(song.id) },
                    onLongClick = { onStartSelectionWithSong(song.id) },
                    onDeleteRequest = { onDeleteSongRequest(song) },
                    onShareRequest = { onShareSongRequest(song) },
                    onAddToPlaylistRequest = { onAddToPlaylistRequest(song) }
                )
            }
        }

        if (olderSongs.isNotEmpty()) {
            item {
                TimelineSectionHeader(title = "Older media")
            }

            items(
                olderSongs,
                key = { it.id }
            ) { song ->
                V2SSongRowItem(
                    song = song,
                    isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id,
                    trackNumber = if (showTrackNumbers) {
                        filteredSongs.indexOf(song) + 1
                    } else {
                        null
                    },
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelectSong(song.id)
                        } else {
                            onPlaySong(song)
                        }
                    },
                    playerManager = playerManager,
                    context = context,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedSongIds.contains(song.id),
                    onToggleSelect = { onToggleSelectSong(song.id) },
                    onLongClick = { onStartSelectionWithSong(song.id) },
                    onDeleteRequest = { onDeleteSongRequest(song) },
                    onShareRequest = { onShareSongRequest(song) },
                    onAddToPlaylistRequest = { onAddToPlaylistRequest(song) }
                )
            }
        }
    }
    }
}

// ── Folders Tab ───────────────────────────────────────────────────────────────

@Composable
private fun FoldersTabContent(
    folders: List<AudioFolder>,
    songs: List<SongItem>,
    selectedFolder: AudioFolder?,
    onFolderClick: (AudioFolder) -> Unit,
    onBack: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    playerState: com.example.player.PlayerUiState
) {
    if (selectedFolder != null) {
        val folderSongs = remember(selectedFolder, songs) {
            songs.filter { song ->
                val path = song.uri.toString()
                val idx = path.lastIndexOf('/')

                val name = if (idx > 0) {
                    path.substring(0, idx).substringAfterLast('/')
                } else {
                    "Music"
                }

                name == selectedFolder.name
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = selectedFolder.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = VlcOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${folderSongs.size} song${
                            if (folderSongs.size != 1) "s" else ""
                        }",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (folderSongs.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VlcOrange,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                onPlaySong(folderSongs.first())
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )

                            Text(
                                "Play All",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (folderSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs in this folder",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        folderSongs,
                        key = { it.id }
                    ) { song ->
                        V2SPlainSongRow(
                            song = song,
                            isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id,
                            onClick = {
                                onPlaySong(song)
                            }
                        )
                    }
                }
            }
        }

        return
    }

    if (folders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VlcOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Folder,
                        null,
                        tint = VlcOrange.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "No audio folders",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Folders with audio files will appear here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = "${folders.size} folder${
                    if (folders.size != 1) "s" else ""
                }",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
            )
        }

        items(
            folders,
            key = { it.path }
        ) { folder ->
            AudioFolderRow(
                folder = folder,
                onClick = {
                    onFolderClick(folder)
                }
            )
        }
    }
}

@Composable
private fun V2SPlainSongRow(
    song: SongItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isPlaying) {
                        VlcOrange.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Filled.GraphicEq
                } else {
                    Icons.Filled.MusicNote
                },
                contentDescription = null,
                tint = if (isPlaying) {
                    VlcOrange
                } else {
                    PlayitCyan
                },
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                fontWeight = if (isPlaying) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                fontSize = 15.sp,
                color = if (isPlaying) {
                    VlcOrange
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    if (song.artist.isNotBlank()) {
                        append(song.artist)
                    }

                    if (
                        song.artist.isNotBlank() &&
                        song.formattedDuration.isNotBlank()
                    ) {
                        append(" • ")
                    }

                    append(song.formattedDuration)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isPlaying) {
            Icon(
                Icons.Filled.GraphicEq,
                null,
                tint = VlcOrange,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                Icons.Filled.PlayArrow,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun AudioFolderRow(
    folder: AudioFolder,
    onClick: () -> Unit
) {
    val mins = folder.totalDurationMs / (1000 * 60)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VlcOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Folder,
                null,
                tint = VlcOrange,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = folder.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${folder.songCount} song${
                    if (folder.songCount != 1) "s" else ""
                } • ${mins}min",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = VlcOrange.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    "Play folder",
                    tint = VlcOrange,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ── Playlists Tab ─────────────────────────────────────────────────────────────

@Composable
private fun PlaylistsTabContent(
    playlists: List<AudioPlaylist>,
    selectedPlaylist: AudioPlaylist?,
    onPlaylistClick: (AudioPlaylist) -> Unit,
    onBack: () -> Unit,
    onNewPlaylist: () -> Unit,
    onRenameClick: (AudioPlaylist) -> Unit = {},
    onDeleteClick: (AudioPlaylist) -> Unit = {},
    onPlaySong: (SongItem) -> Unit,
    playerState: com.example.player.PlayerUiState,
    context: android.content.Context
) {
    if (selectedPlaylist != null) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = selectedPlaylist.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = VlcOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${selectedPlaylist.songs.size} song${
                            if (selectedPlaylist.songs.size != 1) "s" else ""
                        }",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedPlaylist.songs.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VlcOrange,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                onPlaySong(selectedPlaylist.songs.first())
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )

                            Text(
                                "Play All",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (selectedPlaylist.songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Playlist is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Add songs from a song's ••• menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        selectedPlaylist.songs,
                        key = { it.id }
                    ) { song ->
                        V2SPlainSongRow(
                            song = song,
                            isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id,
                            onClick = {
                                onPlaySong(song)
                            }
                        )
                    }
                }
            }
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNewPlaylist)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VlcOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.PlaylistAdd,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "New Playlist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = VlcOrange
                    )

                    Text(
                        "Create a new audio playlist",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = 0.25f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VlcOrange.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                null,
                                tint = VlcOrange.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "No playlists yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Tap New Playlist to get started",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        } else {
            items(
                playlists,
                key = { it.id }
            ) { playlist ->
                AudioPlaylistRow(
                    playlist = playlist,
                    onClick = {
                        onPlaylistClick(playlist)
                    },
                    onRenameClick = { onRenameClick(playlist) },
                    onDeleteClick = { onDeleteClick(playlist) }
                )
            }
        }
    }
}

@Composable
private fun AudioPlaylistRow(
    playlist: AudioPlaylist,
    onClick: () -> Unit,
    onRenameClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.QueueMusic,
                null,
                tint = VlcOrange,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (playlist.songCount > 0) {
                    "${playlist.songCount} songs"
                } else {
                    "Empty playlist"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        var showMenu by remember { mutableStateOf(false) }

        Box {
            IconButton(
                onClick = {
                    showMenu = true
                }
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Play",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.PlayArrow,
                            null,
                            tint = VlcOrange
                        )
                    },
                    onClick = {
                        onClick()
                        showMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Play Next",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.SkipNext,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        showMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Add to Queue",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        showMenu = false
                    }
                )

                if (playlist.id != "favourites") {
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Rename",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRenameClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete Playlist",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant
            .copy(alpha = 0.25f),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ── Song History Tab ──────────────────────────────────────────────────────────

@Composable
private fun SongHistoryTabContent(
    historySongs: List<SongItem>,
    onPlaySong: (SongItem) -> Unit,
    onPlayMultipleSongs: (List<SongItem>) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onRemoveFromHistory: (SongItem) -> Unit = {},
    onShareSong: (SongItem) -> Unit = {},
    onDeleteSong: (SongItem) -> Unit = {},
    onAddToPlaylist: (SongItem) -> Unit = {},
    playerState: com.example.player.PlayerUiState,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<Long> = emptySet(),
    onToggleSelectSong: (Long) -> Unit = {},
    onStartSelectionWithSong: (Long) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredHistory = remember(historySongs, searchQuery) {
        if (searchQuery.isBlank()) {
            historySongs
        } else {
            val q = searchQuery.trim().lowercase()
            historySongs.filter {
                it.title.lowercase().contains(q) ||
                    it.artist.lowercase().contains(q) ||
                    it.album.lowercase().contains(q)
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    "Clear Song History?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "This will remove all songs from your playback history. Your actual audio files will not be deleted.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search in History & Clear Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search in song history...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = VlcOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VlcOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (historySongs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredHistory.size} song${if (filteredHistory.size != 1) "s" else ""} in history",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (filteredHistory.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onPlayMultipleSongs(filteredHistory)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VlcOrange.copy(alpha = 0.15f),
                                    contentColor = VlcOrange
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Play All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        TextButton(
                            onClick = { showClearConfirmDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Clear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VlcOrange.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = VlcOrange,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = if (searchQuery.isNotBlank()) "No songs match \"$searchQuery\"" else "No Song History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (searchQuery.isNotBlank()) "Try a different search term" else "Songs you listen to will automatically appear here with their play time and quick actions.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = filteredHistory,
                    key = { it.id }
                ) { song ->
                    val isPlaying = !playerState.isVideoAudio && playerState.currentSong?.id == song.id && playerState.isPlaying
                    val isSelected = selectedSongIds.contains(song.id)

                    SongHistoryItemRow(
                        song = song,
                        isPlaying = isPlaying,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onPlaySong = { onPlaySong(song) },
                        onToggleSelect = { onToggleSelectSong(song.id) },
                        onLongClick = { onStartSelectionWithSong(song.id) },
                        onRemoveFromHistory = { onRemoveFromHistory(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onShareSong = { onShareSong(song) },
                        onDeleteSong = { onDeleteSong(song) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SongHistoryItemRow(
    song: SongItem,
    isPlaying: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onPlaySong: () -> Unit,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveFromHistory: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShareSong: () -> Unit,
    onDeleteSong: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val relativeTime = remember(song.lastPlayedTimestamp) {
        if (song.lastPlayedTimestamp <= 0L) {
            "Recently played"
        } else {
            val now = System.currentTimeMillis()
            val diff = (now - song.lastPlayedTimestamp).coerceAtLeast(0L)
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days == 1L -> "Yesterday"
                days < 7 -> "${days}d ago"
                else -> {
                    val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(song.lastPlayedTimestamp))
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onPlaySong()
                    }
                },
                onLongClick = {
                    onLongClick()
                }
            )
            .background(
                if (isSelected) {
                    VlcOrange.copy(alpha = 0.15f)
                } else if (isPlaying) {
                    VlcOrange.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = VlcOrange,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.size(24.dp)
            )
        }

        // Song Artwork / Icon with play indicator
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isPlaying) VlcOrange else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = VlcOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isPlaying) VlcOrange else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (song.artist.isNotBlank()) song.artist else "Unknown Artist",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = "•",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = song.formattedDuration,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = relativeTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // 3-dot Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                DropdownMenuItem(
                    text = { Text("Play Now") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = VlcOrange
                        )
                    },
                    onClick = {
                        showMenu = false
                        onPlaySong()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Remove from History") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = VlcOrange
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRemoveFromHistory()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Share") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        onShareSong()
                    }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFE53935)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color(0xFFE53935)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteSong()
                    }
                )
            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ── Song Row Item with Full 3-dot Options Sheet ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun V2SSongRowItem(
    song: SongItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    playerManager: VyloPlayerManager,
    context: android.content.Context,
    trackNumber: Int? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDeleteRequest: () -> Unit = {},
    onShareRequest: () -> Unit = {},
    onAddToPlaylistRequest: () -> Unit = {}
) {
    var showOptionsSheet by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) VlcOrange.copy(alpha = 0.12f) else Color.Transparent
            )
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = VlcOrange,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else if (trackNumber != null) {
            Text(
                text = trackNumber.toString().padStart(2),
                color = if (isPlaying) {
                    VlcOrange
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isPlaying) {
                        VlcOrange.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Filled.GraphicEq
                } else {
                    Icons.Filled.MusicNote
                },
                contentDescription = null,
                tint = if (isPlaying) {
                    VlcOrange
                } else {
                    PlayitCyan
                },
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = song.title,
                fontWeight = if (isPlaying) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                fontSize = 14.sp,
                color = if (isPlaying) {
                    VlcOrange
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    if (song.artist.isNotBlank()) {
                        append(song.artist)
                    }

                    if (
                        song.artist.isNotBlank() &&
                        song.formattedDuration.isNotBlank()
                    ) {
                        append(" • ")
                    }

                    append(song.formattedDuration)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = {
                showOptionsSheet = true
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showOptionsSheet = false
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            containerColor = Color(0xFF1A1A1C),
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(
                            width = 36.dp,
                            height = 4.dp
                        ),
                    shape = RoundedCornerShape(2.dp),
                    color = Color(0xFF3A3A3C)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            tint = VlcOrange,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = buildString {
                                if (song.artist.isNotBlank()) {
                                    append(song.artist)
                                }

                                if (song.album.isNotBlank()) {
                                    if (song.artist.isNotBlank()) {
                                        append(" • ")
                                    }
                                    append(song.album)
                                }
                            },
                            color = Color(0xFF8E8E93),
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = song.formattedDuration,
                            color = Color(0xFF636366),
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFF3A3A3C),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        icon = Icons.Filled.PlayArrow,
                        label = "Play",
                        tint = VlcOrange
                    ) {
                        onClick()
                        showOptionsSheet = false
                    }

                    QuickActionButton(
                        icon = Icons.Filled.SkipNext,
                        label = "Next",
                        tint = Color.White
                    ) {
                        Toast.makeText(
                            context,
                            "Will play next",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }

                    QuickActionButton(
                        icon = if (isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        label = if (isFavorite) "Liked" else "Like",
                        tint = if (isFavorite) {
                            VlcOrange
                        } else {
                            Color.White
                        }
                    ) {
                        isFavorite = !isFavorite

                        Toast.makeText(
                            context,
                            if (isFavorite) {
                                "Added to Favourites"
                            } else {
                                "Removed from Favourites"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    QuickActionButton(
                        icon = Icons.Filled.Share,
                        label = "Share",
                        tint = Color.White
                    ) {
                        onShareRequest()
                        showOptionsSheet = false
                    }
                }

                HorizontalDivider(
                    color = Color(0xFF545458)
                )

                SongOptionItem(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    label = "Add to Play Queue",
                    tint = Color.White,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Added to queue",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                SongOptionItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = "Add to Playlist",
                    tint = Color.White,
                    onClick = {
                        onAddToPlaylistRequest()
                        showOptionsSheet = false
                    }
                )

                HorizontalDivider(
                    color = Color(0xFF545458),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                SongOptionItem(
                    icon = Icons.Filled.RingVolume,
                    label = "Set as Ringtone",
                    tint = Color.White,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Set as Ringtone",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                SongOptionItem(
                    icon = Icons.Filled.Description,
                    label = "Edit Tags",
                    tint = Color.White,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Edit Tags: ${song.title}",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                SongOptionItem(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy Song Name",
                    tint = Color.White,
                    onClick = {
                        val clipboard =
                            context.getSystemService(
                                android.content.Context.CLIPBOARD_SERVICE
                            ) as android.content.ClipboardManager

                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "song",
                                song.title
                            )
                        )

                        Toast.makeText(
                            context,
                            "Copied!",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                SongOptionItem(
                    icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
                    label = "Create Shortcut",
                    tint = Color.White,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Create Shortcut",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                HorizontalDivider(
                    color = Color(0xFF545458),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                SongOptionItem(
                    icon = Icons.Filled.Info,
                    label = "Song Info",
                    tint = Color.White,
                    trailingContent = {
                        Text(
                            song.formattedDuration,
                            color = Color(0xFF636366),
                            fontSize = 12.sp
                        )
                    },
                    onClick = {
                        Toast.makeText(
                            context,
                            "Song: ${song.title}\nArtist: ${song.artist}",
                            Toast.LENGTH_LONG
                        ).show()

                        showOptionsSheet = false
                    }
                )

                SongOptionItem(
                    icon = Icons.Filled.Folder,
                    label = "Show in Folder",
                    tint = Color.White,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Show in Folder",
                            Toast.LENGTH_SHORT
                        ).show()

                        showOptionsSheet = false
                    }
                )

                HorizontalDivider(
                    color = Color(0xFF545458),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                SongOptionItem(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    tint = Color(0xFFFF453A),
                    onClick = {
                        showOptionsSheet = false
                        onDeleteRequest()
                    }
                )
            }
        }
    }
}

// ── Quick Action Button ───────────────────────────────────────────────────────

@Composable
private fun QuickActionButton(
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
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp
            )
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF545458)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            label,
            color = Color(0xFFAEAEB2),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Song Option List Item ─────────────────────────────────────────────────────

@Composable
private fun SongOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = label,
            color = if (label == "Delete") {
                Color(0xFFFF453A)
            } else {
                Color.White
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        trailingContent?.invoke()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun TimelineSectionHeader(
    title: String
) {
    Text(
        text = title,
        color = VlcOrange,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(
            horizontal = 20.dp,
            vertical = 10.dp
        )
    )
}

@Composable
fun VinylRecordDisc(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(
            w / 2f,
            h / 2f
        )

        drawCircle(
            color = Color(0xFF1E2124),
            radius = w * 0.48f,
            center = center
        )

        for (i in 1..4) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = w * (0.24f + (i * 0.05f)),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5f
                )
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    VlcAmber,
                    VlcOrange,
                    VlcOrangeDark
                ),
                center = center,
                radius = w * 0.2f
            ),
            radius = w * 0.2f,
            center = center
        )

        drawCircle(
            color = Color(0xFF0D1117),
            radius = w * 0.06f,
            center = center
        )
    }
}

// ── Display & Sort Settings Sheet ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongsDisplaySettingsSheet(
    showTrackNumbers: Boolean,
    onToggleShowTrackNumbers: (Boolean) -> Unit,
    sortOrder: SongSortOrder,
    onSortOrderSelected: (SongSortOrder) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Display Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onToggleShowTrackNumbers(!showTrackNumbers)
                    }
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Show track numbers",
                    fontSize = 15.sp
                )

                Checkbox(
                    checked = showTrackNumbers,
                    onCheckedChange = onToggleShowTrackNumbers,
                    colors = CheckboxDefaults.colors(
                        checkedColor = VlcOrange
                    )
                )
            }

            HorizontalDivider()

            Text(
                text = "Sort by",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                )
            )

            SongSortOrder.values().forEach { order ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSortOrderSelected(order)
                        }
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        order.label,
                        fontSize = 14.sp
                    )

                    if (sortOrder == order) {
                        Icon(
                            Icons.Filled.Check,
                            "Selected",
                            tint = VlcOrange
                        )
                    }
                }
            }
        }
    }
}
