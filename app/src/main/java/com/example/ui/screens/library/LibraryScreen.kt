package com.example.ui.screens.library

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.components.FastScroller
import com.example.ui.components.FastScrollerGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderItem
import com.example.data.model.SongItem
import com.example.data.model.SortOrder
import com.example.data.model.VideoItem
import com.example.ui.components.DisplaySettingsBottomSheet
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.responsiveGridCells
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.V2SFolderSymbol
import com.example.ui.components.V2SVideosSymbol
import com.example.ui.components.VideoOptionsBottomSheet
import com.example.ui.components.VideoThumbnail
import com.example.ui.theme.VlcOrange
import com.example.data.model.PlaylistData
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.QueueMusic

data class VideoTabPlaylistItem(
    val id: String,
    val title: String,
    val fileCount: Int,
    val previewVideo: VideoItem? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    videos: List<VideoItem>,
    folders: List<FolderItem> = emptyList(),
    allVideos: List<VideoItem> = videos,
    allSongs: List<SongItem> = emptyList(),
    favoriteVideos: List<VideoItem> = emptyList(),
    recentlyWatched: List<VideoItem> = emptyList(),
    playlists: List<PlaylistData> = emptyList(),
    onCreatePlaylist: (String, String) -> Unit = { _, _ -> },
    onDeletePlaylist: (String) -> Unit = {},
    onRenamePlaylist: (String, String) -> Unit = { _, _ -> },
    sortOrder: SortOrder = SortOrder.NEWEST,
    searchQuery: String = "",
    onSortChange: (SortOrder) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onPlayInShorts: (VideoItem) -> Unit = {},
    onPlayAsPip: (VideoItem) -> Unit = {},
    onOpenNormalPlayer: (VideoItem, List<VideoItem>) -> Unit = { _, _ -> },
    onPlayFromStart: (VideoItem) -> Unit = {},
    onPlayAsAudio: (VideoItem) -> Unit = {},
    onSongClick: (SongItem) -> Unit = {},
    onNavigateToShorts: () -> Unit = {},
    onNavigateToSongs: () -> Unit = {},
    onToggleFavorite: (VideoItem) -> Unit = {},
    onShareVideo: (VideoItem) -> Unit = {},
    onShowInfo: (VideoItem) -> Unit = {},
    onDeleteVideo: (VideoItem) -> Unit = {},
    onDeleteMultipleVideos: (List<VideoItem>) -> Unit = {},
    onShareMultipleVideos: (List<VideoItem>) -> Unit = {},
    onPlayMultipleVideos: (List<VideoItem>) -> Unit = {},
    onToggleMultipleFavorites: (List<VideoItem>, Boolean) -> Unit = { _, _ -> },
    onAddMultipleVideosToPlaylist: (String, List<VideoItem>) -> Unit = { _, _ -> },
    onRefreshScan: () -> Unit = {},
    onPlayLastPlayed: () -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTabIndex by remember { mutableIntStateOf(0) }
    val subTabs = listOf("VIDEOS", "FOLDERS", "PLAYLISTS")

    var isSearchVisible by remember { mutableStateOf(false) }
    var showDisplaySettingsSheet by remember { mutableStateOf(false) }

    // Multi-selection state
    var selectedVideoIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showSelectionOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteMultipleDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistMultipleDialog by remember { mutableStateOf(false) }
    var multiInfoVideos by remember { mutableStateOf<List<VideoItem>?>(null) }

    val context = LocalContext.current
    fun onOpenNormalPlayer(video: VideoItem) = onOpenNormalPlayer(video, videos)
    val displayPreferences = remember {
        context.getSharedPreferences("v2s_display_preferences", android.content.Context.MODE_PRIVATE)
    }

    var isListView by remember {
        mutableStateOf(displayPreferences.getBoolean("is_list_view", false))
    }
    var showOnlyFavorites by remember {
        mutableStateOf(displayPreferences.getBoolean("show_only_favorites", false))
    }
    var selectedOptionsVideo by remember { mutableStateOf<VideoItem?>(null) }

    var selectedFolder by remember { mutableStateOf<FolderItem?>(null) }
    var folderViewIsGrid by remember {
        mutableStateOf(displayPreferences.getBoolean("folder_view_grid", true))
    }

    val effectiveVideos = remember(videos, showOnlyFavorites) {
        if (showOnlyFavorites) {
            videos.filter { it.isFavorite }
        } else {
            videos
        }
    }

    val videoOnlyFolders = remember(folders) {
        folders
            .filter { it.videoCount > 0 }
            .map { it.copy(songCount = 0) }
    }

    val videoListState = rememberLazyListState()
    val videoGridState = rememberLazyGridState()
    val folderListState = rememberLazyListState()
    val folderGridState = rememberLazyGridState()

    var selectedPlaylistId by remember {
        mutableStateOf<String?>(null)
    }

    var showCreatePlaylistDialog by remember {
        mutableStateOf(false)
    }

    var newPlaylistName by remember {
        mutableStateOf("")
    }

    var playlistToRename by remember { mutableStateOf<PlaylistData?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistData?>(null) }
    var renamePlaylistTitle by remember { mutableStateOf("") }
    var videoForAddToPlaylist by remember { mutableStateOf<VideoItem?>(null) }

    val defaultPlaylists = remember(videos, favoriteVideos, recentlyWatched) {
        listOf(
            VideoTabPlaylistItem(
                id = "all",
                title = "All Videos",
                fileCount = videos.size,
                previewVideo = videos.firstOrNull()
            ),
            VideoTabPlaylistItem(
                id = "fav",
                title = "Favorites",
                fileCount = favoriteVideos.size,
                previewVideo = favoriteVideos.firstOrNull()
            ),
            VideoTabPlaylistItem(
                id = "recent",
                title = "History",
                fileCount = recentlyWatched.size,
                previewVideo = recentlyWatched.firstOrNull()
            )
        )
    }

    val appPlaylists = remember(playlists) {
        playlists.filter { pl ->
            !pl.isSystem && (
                pl.category.equals("VIDEOS", ignoreCase = true) ||
                pl.id.startsWith("pl_videos_")
            )
        }.map { pl ->
            val vidsInPl = pl.items.mapNotNull { it.videoItem }
            VideoTabPlaylistItem(
                id = pl.id,
                title = pl.title,
                fileCount = vidsInPl.size,
                previewVideo = vidsInPl.firstOrNull()
            )
        }
    }

    val allPlaylistItems = appPlaylists + defaultPlaylists

    fun resolveVideosForPlaylistId(plId: String): List<VideoItem> {
        return when (plId) {
            "all" -> videos
            "fav" -> favoriteVideos
            "recent" -> recentlyWatched
            else -> playlists.find { it.id == plId }?.items?.mapNotNull { it.videoItem } ?: emptyList()
        }
    }

    val activeVideoPlaylist: Pair<String, List<VideoItem>>? = remember(
        selectedPlaylistId,
        allPlaylistItems,
        videos,
        favoriteVideos,
        recentlyWatched,
        playlists
    ) {
        val id = selectedPlaylistId ?: return@remember null
        val item = allPlaylistItems.find { it.id == id } ?: return@remember null
        Pair(item.title, resolveVideosForPlaylistId(id))
    }

    BackHandler(
        enabled = selectedVideoIds.isNotEmpty() || selectedFolder != null || selectedPlaylistId != null
    ) {
        if (selectedVideoIds.isNotEmpty()) {
            selectedVideoIds = emptySet()
        } else if (selectedFolder != null) {
            selectedFolder = null
        } else if (selectedPlaylistId != null) {
            selectedPlaylistId = null
        }
    }

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

                // ---------------------------------------------------------
                // HEADER (Regular or Multi-Select)
                // ---------------------------------------------------------

                if (selectedVideoIds.isNotEmpty()) {
                    val selectedVideos = videos.filter { it.id in selectedVideoIds }
                    val totalDurationMs = selectedVideos.sumOf { it.durationMs }
                    val totalHours = totalDurationMs / 3600000
                    val totalMinutes = (totalDurationMs % 3600000) / 60000
                    val totalSeconds = (totalDurationMs % 60000) / 1000
                    val formattedDuration = if (totalHours > 0) {
                        String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", totalHours, totalMinutes, totalSeconds)
                    } else {
                        String.format(java.util.Locale.getDefault(), "%d:%02d", totalMinutes, totalSeconds)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { selectedVideoIds = emptySet() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Cancel Selection",
                                    tint = VlcOrange
                                )
                            }

                            Column {
                                Text(
                                    text = "${selectedVideoIds.size} selected",
                                    color = VlcOrange,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formattedDuration,
                                    color = VlcOrange.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // (i) Information icon
                            IconButton(
                                onClick = {
                                    if (selectedVideos.size == 1) {
                                        onShowInfo(selectedVideos.first())
                                    } else {
                                        multiInfoVideos = selectedVideos
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Video Information",
                                    tint = VlcOrange
                                )
                            }

                            // Play selected
                            IconButton(
                                onClick = {
                                    if (selectedVideos.isNotEmpty()) {
                                        onPlayMultipleVideos(selectedVideos)
                                        selectedVideoIds = emptySet()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play Selected",
                                    tint = VlcOrange
                                )
                            }

                            // Overflow menu (⋮)
                            Box {
                                IconButton(
                                    onClick = { showSelectionOverflowMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "More selection actions",
                                        tint = VlcOrange
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSelectionOverflowMenu,
                                    onDismissRequest = { showSelectionOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Share") },
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            onShareMultipleVideos(selectedVideos)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Play as audio") },
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            if (selectedVideos.isNotEmpty()) {
                                                onOpenNormalPlayer(selectedVideos.first())
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add to playlist") },
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            showAddToPlaylistMultipleDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            showDeleteMultipleDialog = true
                                        }
                                    )
                                    val allFavs = selectedVideos.all { it.isFavorite }
                                    DropdownMenuItem(
                                        text = { Text(if (allFavs) "Remove from favorites" else "Add to favorites") },
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            onToggleMultipleFavorites(selectedVideos, !allFavs)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
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
                                    text = "${videos.size}",
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {

                            IconButton(
                                onClick = {
                                    isSearchVisible = !isSearchVisible
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = if (
                                        isSearchVisible ||
                                        searchQuery.isNotEmpty()
                                    ) {
                                        VlcOrange
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onRefreshScan,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Rescan Library",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onAddLinkClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Link,
                                    contentDescription = "Add Video or Song Link",
                                    tint = VlcOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onPlayLastPlayed,
                                modifier = Modifier
                                    .testTag("play_last_video")
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = "Play last played video",
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
                }

                // ---------------------------------------------------------
                // SEARCH
                // ---------------------------------------------------------

                AnimatedVisibility(
                    visible = isSearchVisible || searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search videos, titles, folders...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = VlcOrange
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onSearchChange("")
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        isSearchVisible = false
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Close search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor =
                                MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor =
                                MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = VlcOrange,
                            unfocusedBorderColor =
                                MaterialTheme.colorScheme.outline,
                            focusedContainerColor =
                                MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor =
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp
                            )
                    )
                }

                // ---------------------------------------------------------
                // TABS
                // ---------------------------------------------------------

                TabRow(
                    selectedTabIndex = selectedSubTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = VlcOrange,
                    indicator = { tabPositions ->
                        if (selectedSubTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(
                                    tabPositions[selectedSubTabIndex]
                                ),
                                height = 2.5.dp,
                                color = VlcOrange
                            )
                        }
                    },
                    divider = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface
                                )
                        )
                    }
                ) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSubTabIndex == index,
                            onClick = {
                                selectedSubTabIndex = index
                                selectedFolder = null
                                selectedPlaylistId = null
                            },
                            modifier = Modifier.height(38.dp),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    when (index) {
                                        0 -> V2SVideosSymbol(size = 14.dp)
                                        1 -> V2SFolderSymbol(size = 14.dp)
                                        2 -> Icon(
                                            Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = if (selectedSubTabIndex == index) VlcOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedSubTabIndex == index) VlcOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }

                // ---------------------------------------------------------
                // TAB CONTENT
                // ---------------------------------------------------------

                when (selectedSubTabIndex) {

                    // =====================================================
                    // VIDEOS
                    // =====================================================

                    0 -> {

                        if (effectiveVideos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = VlcOrange.copy(alpha = 0.14f),
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.VideoLibrary,
                                                contentDescription = null,
                                                tint = VlcOrange,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (showOnlyFavorites) "No favorite videos yet" else "No videos or links yet",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )

                                    Text(
                                        text = "Add any YouTube, Shorts, or video link, or scan your phone memory to load device videos.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Video / Song Link", fontWeight = FontWeight.Bold)
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Scan Phone Memory")
                                    }
                                }
                            }
                        } else if (isListView) {

                            FastScroller(
                                listState = videoListState,
                                itemCount = effectiveVideos.size,
                                indexLabel = { idx ->
                                    effectiveVideos.getOrNull(idx)?.title?.take(1)?.uppercase() ?: ""
                                }
                            ) {
                                LazyColumn(
                                    state = videoListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("video_list_view"),
                                    contentPadding = PaddingValues(
                                        start = horizontalPadding,
                                        end = horizontalPadding,
                                        top = 12.dp,
                                        bottom = 88.dp
                                    ),
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                items(
                                    effectiveVideos,
                                    key = { it.id }
                                ) { video ->
                                    val isSelected = video.id in selectedVideoIds

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) VlcOrange else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    if (selectedVideoIds.isNotEmpty()) {
                                                        selectedVideoIds = if (isSelected) {
                                                            selectedVideoIds - video.id
                                                        } else {
                                                            selectedVideoIds + video.id
                                                        }
                                                    } else {
                                                        onOpenNormalPlayer(video)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedVideoIds = if (isSelected) {
                                                        selectedVideoIds - video.id
                                                    } else {
                                                        selectedVideoIds + video.id
                                                    }
                                                }
                                            )
                                    ) {

                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .size(
                                                        width = 90.dp,
                                                        height = 56.dp
                                                    )
                                                    .clip(
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .background(
                                                        MaterialTheme
                                                            .colorScheme
                                                            .surfaceVariant
                                                    )
                                            ) {
                                                VideoThumbnail(
                                                    video = video,
                                                    modifier =
                                                        Modifier.fillMaxSize()
                                                )

                                                if (isSelected) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = VlcOrange,
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Check,
                                                                contentDescription = "Selected",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 12.dp)
                                            ) {

                                                Text(
                                                    text = video.title,
                                                    color =
                                                        MaterialTheme
                                                            .colorScheme
                                                            .onSurface,
                                                    fontWeight =
                                                        FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    maxLines = 2,
                                                    overflow =
                                                        TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text =
                                                        video.formattedDuration,
                                                    color =
                                                        Color(0xFFC7C7CC),
                                                    fontSize = 13.sp
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    onToggleFavorite(video)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector =
                                                        if (video.isFavorite) {
                                                            Icons.Filled.Favorite
                                                        } else {
                                                            Icons.Filled.FavoriteBorder
                                                        },
                                                    contentDescription =
                                                        "Favorite",
                                                    tint =
                                                        if (video.isFavorite) {
                                                            VlcOrange
                                                        } else {
                                                            MaterialTheme
                                                                .colorScheme
                                                                .onSurfaceVariant
                                                        }
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    selectedOptionsVideo =
                                                        video
                                                }
                                            ) {
                                                Icon(
                                                    imageVector =
                                                        Icons.Filled.MoreVert,
                                                    contentDescription =
                                                        "Options",
                                                    tint =
                                                        MaterialTheme
                                                            .colorScheme
                                                            .onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        } else {

                            FastScrollerGrid(
                                gridState = videoGridState,
                                itemCount = effectiveVideos.size,
                                indexLabel = { idx ->
                                    effectiveVideos.getOrNull(idx)?.title?.take(1)?.uppercase() ?: ""
                                }
                            ) {
                                LazyVerticalGrid(
                                    columns = responsiveGridCells(
                                        windowSize = windowSize,
                                        compactColumns = 2,
                                        mediumColumns = 3,
                                        expandedColumns = 4
                                    ),
                                    state = videoGridState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("video_grid_view"),
                                    contentPadding = PaddingValues(
                                        start = horizontalPadding,
                                        end = horizontalPadding,
                                        top = 12.dp,
                                        bottom = 88.dp
                                    ),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(14.dp)
                                ) {

                                    items(
                                        effectiveVideos,
                                        key = { it.id }
                                    ) { video ->
                                        val isSelected = video.id in selectedVideoIds

                                        ExactVideoGridCard(
                                            video = video,
                                            isSelected = isSelected,
                                            isSelectionMode = selectedVideoIds.isNotEmpty(),
                                            onClick = {
                                                if (selectedVideoIds.isNotEmpty()) {
                                                    selectedVideoIds = if (isSelected) {
                                                        selectedVideoIds - video.id
                                                    } else {
                                                        selectedVideoIds + video.id
                                                    }
                                                } else {
                                                    onOpenNormalPlayer(video)
                                                }
                                            },
                                            onLongClick = {
                                                selectedVideoIds = if (isSelected) {
                                                    selectedVideoIds - video.id
                                                } else {
                                                    selectedVideoIds + video.id
                                                }
                                            },
                                            onOptionsClick = {
                                                selectedOptionsVideo = video
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================
                    // FOLDERS
                    // =====================================================

                    1 -> {

                        if (selectedFolder == null) {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                val totalVideoCount =
                                    videoOnlyFolders.sumOf {
                                        it.videoCount
                                    }

                                val totalBytes =
                                    videoOnlyFolders.sumOf {
                                        it.totalSizeBytes
                                    }

                                val totalMb =
                                    totalBytes / (1024 * 1024)

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color =
                                        MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 8.dp
                                        )
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 14.dp,
                                                vertical = 10.dp
                                            ),
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {

                                        Row(
                                            verticalAlignment =
                                                Alignment.CenterVertically,
                                            horizontalArrangement =
                                                Arrangement.spacedBy(10.dp)
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Filled.Storage,
                                                contentDescription = null,
                                                tint = VlcOrange,
                                                modifier =
                                                    Modifier.size(22.dp)
                                            )

                                            Column {

                                                Text(
                                                    text =
                                                        "Internal Storage Browser",
                                                    fontWeight =
                                                        FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color =
                                                        MaterialTheme
                                                            .colorScheme
                                                            .onSurface
                                                )

                                                Text(
                                                    text =
                                                        "$totalVideoCount videos • ${videoOnlyFolders.size} folders",
                                                    fontSize = 11.sp,
                                                    color =
                                                        MaterialTheme
                                                            .colorScheme
                                                            .onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape =
                                                RoundedCornerShape(20.dp),
                                            color =
                                                VlcOrange.copy(alpha = 0.18f)
                                        ) {

                                            Text(
                                                text =
                                                    if (totalMb > 1024) {
                                                        "${totalMb / 1024} GB"
                                                    } else {
                                                        "$totalMb MB"
                                                    },
                                                fontWeight =
                                                    FontWeight.Bold,
                                                color = VlcOrange,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 6.dp
                                        ),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text =
                                            "VIDEO FOLDERS (${videoOnlyFolders.size})",
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(4.dp)
                                    ) {

                                        IconButton(
                                            onClick = {
                                                folderViewIsGrid = !folderViewIsGrid
                                                displayPreferences.edit()
                                                    .putBoolean("folder_view_grid", folderViewIsGrid)
                                                    .apply()
                                            },
                                            modifier =
                                                Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector =
                                                    if (folderViewIsGrid) {
                                                        Icons.AutoMirrored.Filled.ViewList
                                                    } else {
                                                        Icons.Filled.GridView
                                                    },
                                                contentDescription =
                                                    "Toggle Grid/List View",
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurfaceVariant,
                                                modifier =
                                                    Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                isSearchVisible =
                                                    !isSearchVisible
                                            },
                                            modifier =
                                                Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector =
                                                    Icons.Filled.Search,
                                                contentDescription =
                                                    "Search Folders",
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurfaceVariant,
                                                modifier =
                                                    Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (videoOnlyFolders.isEmpty()) {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Column(
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Filled.Folder,
                                                contentDescription = null,
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .outline,
                                                modifier =
                                                    Modifier.size(64.dp)
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(16.dp)
                                            )

                                            Text(
                                                text =
                                                    "No Video Folders Found",
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurface,
                                                fontWeight =
                                                    FontWeight.Bold,
                                                fontSize = 16.sp
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(6.dp)
                                            )

                                            Text(
                                                text =
                                                    "Videos from storage will appear organized by directory",
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurfaceVariant,
                                                fontSize = 13.sp
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = onAddLinkClick,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = VlcOrange,
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Add Link", fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = onRefreshScan,
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Scan Memory")
                                                }
                                            }
                                        }
                                    }

                                } else if (folderViewIsGrid) {

                                    LazyVerticalGrid(
                                        columns = responsiveGridCells(
                                            windowSize = windowSize,
                                            compactColumns = 3,
                                            mediumColumns = 4,
                                            expandedColumns = 6
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag(
                                                "folder_grid_view"
                                            ),
                                        contentPadding =
                                            PaddingValues(
                                                start = horizontalPadding,
                                                end = horizontalPadding,
                                                top = 4.dp,
                                                bottom = 88.dp
                                            ),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(14.dp)
                                    ) {

                                        items(
                                            videoOnlyFolders,
                                            key = { it.path }
                                        ) { folder ->

                                            val folderVids =
                                                allVideos.filter {
                                                    it.parentFolderName
                                                        .equals(
                                                            folder.name,
                                                            ignoreCase = true
                                                        )
                                                }

                                            ExactFolderGridCard(
                                                folder = folder,
                                                previewVideos =
                                                    folderVids.take(4),
                                                onClick = {
                                                    selectedFolder =
                                                        folder
                                                }
                                            )
                                        }
                                    }

                                } else {

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag(
                                                "folder_list_view"
                                            ),
                                        contentPadding =
                                            PaddingValues(
                                                start = horizontalPadding,
                                                end = horizontalPadding,
                                                top = 4.dp,
                                                bottom = 88.dp
                                            ),
                                        verticalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {

                                        items(
                                            videoOnlyFolders,
                                            key = { it.path }
                                        ) { folder ->

                                            Surface(
                                                onClick = {
                                                    selectedFolder =
                                                        folder
                                                },
                                                shape =
                                                    RoundedCornerShape(12.dp),
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .surface,
                                                modifier =
                                                    Modifier.fillMaxWidth()
                                            ) {

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment =
                                                        Alignment.CenterVertically,
                                                    horizontalArrangement =
                                                        Arrangement.spacedBy(
                                                            14.dp
                                                        )
                                                ) {

                                                    Surface(
                                                        shape =
                                                            RoundedCornerShape(
                                                                10.dp
                                                            ),
                                                        color =
                                                            VlcOrange.copy(
                                                                alpha = 0.15f
                                                            ),
                                                        modifier =
                                                            Modifier.size(46.dp)
                                                    ) {

                                                        Box(
                                                            contentAlignment =
                                                                Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector =
                                                                    Icons.Filled.Folder,
                                                                contentDescription =
                                                                    null,
                                                                tint =
                                                                    VlcOrange,
                                                                modifier =
                                                                    Modifier.size(
                                                                        26.dp
                                                                    )
                                                            )
                                                        }
                                                    }

                                                    Column(
                                                        modifier =
                                                            Modifier.weight(
                                                                1f
                                                            )
                                                    ) {

                                                        Text(
                                                            text =
                                                                folder.name,
                                                            fontWeight =
                                                                FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color =
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .onSurface,
                                                            maxLines = 1,
                                                            overflow =
                                                                TextOverflow.Ellipsis
                                                        )

                                                        Text(
                                                            text =
                                                                "${folder.videoCount} videos • ${folder.formattedSize}",
                                                            fontSize = 11.sp,
                                                            color =
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .onSurfaceVariant
                                                        )
                                                    }

                                                    Icon(
                                                        imageVector =
                                                            Icons.Filled.ChevronRight,
                                                        contentDescription =
                                                            null,
                                                        tint =
                                                            MaterialTheme
                                                                .colorScheme
                                                                .outline
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            val activeFolder = selectedFolder!!

                            val folderVideos =
                                allVideos.filter {
                                    it.parentFolderName.equals(
                                        activeFolder.name,
                                        ignoreCase = true
                                    )
                                }

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 8.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    IconButton(
                                        onClick = {
                                            selectedFolder = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                    ) {

                                        Text(
                                            text =
                                                activeFolder.name.lowercase(),
                                            fontWeight =
                                                FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = VlcOrange,
                                            maxLines = 1,
                                            overflow =
                                                TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text =
                                                "${folderVideos.size} videos • ${activeFolder.formattedSize}",
                                            fontSize = 11.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                        )
                                    }

                                    if (folderVideos.isNotEmpty()) {

                                        Surface(
                                            shape =
                                                RoundedCornerShape(20.dp),
                                            color = VlcOrange,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .clickable {
                                                    onOpenNormalPlayer(
                                                        folderVideos.first()
                                                    )
                                                }
                                        ) {

                                            Row(
                                                modifier =
                                                    Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 6.dp
                                                    ),
                                                verticalAlignment =
                                                    Alignment.CenterVertically,
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(4.dp)
                                            ) {

                                                Icon(
                                                    imageVector =
                                                        Icons.Filled.PlayArrow,
                                                    contentDescription =
                                                        null,
                                                    tint = Color.Black,
                                                    modifier =
                                                        Modifier.size(16.dp)
                                                )

                                                Text(
                                                    text = "Play All",
                                                    color = Color.Black,
                                                    fontWeight =
                                                        FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            showDisplaySettingsSheet =
                                                true
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.Filled.MoreVert,
                                            contentDescription = "More",
                                            tint =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                        )
                                    }
                                }

                                if (folderVideos.isEmpty()) {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {
                                        Text(
                                            text =
                                                "No videos in this folder",
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }

                                } else {

                                    LazyVerticalGrid(
                                        columns = responsiveGridCells(
                                            windowSize = windowSize,
                                            compactColumns = 2,
                                            mediumColumns = 3,
                                            expandedColumns = 4
                                        ),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding =
                                            PaddingValues(
                                                start = horizontalPadding,
                                                end = horizontalPadding,
                                                top = 8.dp,
                                                bottom = 88.dp
                                            ),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(14.dp)
                                    ) {

                                        items(
                                            folderVideos,
                                            key = { it.id }
                                        ) { video ->
                                            val isSelected = video.id in selectedVideoIds

                                            ExactVideoGridCard(
                                                video = video,
                                                isSelected = isSelected,
                                                isSelectionMode = selectedVideoIds.isNotEmpty(),
                                                onClick = {
                                                    if (selectedVideoIds.isNotEmpty()) {
                                                        selectedVideoIds = if (isSelected) {
                                                            selectedVideoIds - video.id
                                                        } else {
                                                            selectedVideoIds + video.id
                                                        }
                                                    } else {
                                                        onOpenNormalPlayer(video)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedVideoIds = if (isSelected) {
                                                        selectedVideoIds - video.id
                                                    } else {
                                                        selectedVideoIds + video.id
                                                    }
                                                },
                                                onOptionsClick = {
                                                    selectedOptionsVideo =
                                                        video
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================
                    // PLAYLISTS
                    // =====================================================

                    2 -> {

                        if (activeVideoPlaylist == null) {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 12.dp
                                        ),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = "VIDEO PLAYLISTS",
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )

                                    Surface(
                                        shape =
                                            RoundedCornerShape(20.dp),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surfaceVariant,
                                        border = BorderStroke(
                                            1.dp,
                                            VlcOrange.copy(alpha = 0.25f)
                                        ),
                                        modifier =
                                            Modifier.clickable {
                                                showCreatePlaylistDialog =
                                                    true
                                            }
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                            verticalAlignment =
                                                Alignment.CenterVertically,
                                            horizontalArrangement =
                                                Arrangement.spacedBy(4.dp)
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Filled.Add,
                                                contentDescription = null,
                                                tint = VlcOrange,
                                                modifier =
                                                    Modifier.size(16.dp)
                                            )

                                            Text(
                                                text = "New Playlist",
                                                color = VlcOrange,
                                                fontWeight =
                                                    FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding =
                                        PaddingValues(
                                            start = horizontalPadding,
                                            end = horizontalPadding,
                                            top = 4.dp,
                                            bottom = 88.dp
                                        ),
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    items(
                                        allPlaylistItems,
                                        key = { it.id }
                                    ) { playlist ->

                                        val playlistVideos =
                                            remember(
                                                playlist.id,
                                                videos,
                                                favoriteVideos,
                                                recentlyWatched,
                                                playlists
                                            ) {
                                                resolveVideosForPlaylistId(playlist.id)
                                            }

                                        Surface(
                                            shape =
                                                RoundedCornerShape(16.dp),
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .surface,
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme
                                                    .colorScheme
                                                    .surfaceVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedPlaylistId = playlist.id
                                                }
                                        ) {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Box(
                                                    modifier = Modifier
                                                        .size(
                                                            width = 84.dp,
                                                            height = 56.dp
                                                        )
                                                        .clip(
                                                            RoundedCornerShape(
                                                                10.dp
                                                            )
                                                        )
                                                        .background(
                                                            MaterialTheme
                                                                .colorScheme
                                                                .surfaceVariant
                                                        )
                                                ) {

                                                    val preview =
                                                        playlist.previewVideo
                                                            ?: playlistVideos
                                                                .firstOrNull()

                                                    if (preview != null) {
                                                        VideoThumbnail(
                                                            video = preview,
                                                            modifier =
                                                                Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                                modifier = Modifier.size(26.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 14.dp)
                                                ) {

                                                    Text(
                                                        text =
                                                            playlist.title,
                                                        fontWeight =
                                                            FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color =
                                                            MaterialTheme
                                                                .colorScheme
                                                                .onSurface,
                                                        maxLines = 1,
                                                        overflow =
                                                            TextOverflow.Ellipsis
                                                    )

                                                    Text(
                                                        text =
                                                            "${playlistVideos.size} Files",
                                                        color =
                                                            MaterialTheme
                                                                .colorScheme
                                                                .onSurfaceVariant,
                                                        fontSize = 11.sp,
                                                        modifier =
                                                            Modifier.padding(
                                                                top = 3.dp
                                                            )
                                                    )
                                                }

                                                if (playlist.id !in listOf("all", "fav", "recent")) {
                                                    var showMenu by remember { mutableStateOf(false) }
                                                    Box {
                                                        IconButton(
                                                            onClick = { showMenu = true },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.MoreVert,
                                                                contentDescription = "Options",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        DropdownMenu(
                                                            expanded = showMenu,
                                                            onDismissRequest = { showMenu = false }
                                                        ) {
                                                            DropdownMenuItem(
                                                                text = { Text("Rename") },
                                                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                                                onClick = {
                                                                    showMenu = false
                                                                    val found = playlists.find { it.id == playlist.id }
                                                                    if (found != null) {
                                                                        playlistToRename = found
                                                                        renamePlaylistTitle = found.title
                                                                    }
                                                                }
                                                            )
                                                            DropdownMenuItem(
                                                                text = { Text("Delete Playlist", color = MaterialTheme.colorScheme.error) },
                                                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                                onClick = {
                                                                    showMenu = false
                                                                    val found = playlists.find { it.id == playlist.id }
                                                                    if (found != null) {
                                                                        playlistToDelete = found
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.ChevronRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            val (title, plVideos) =
                                activeVideoPlaylist

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 8.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    IconButton(
                                        onClick = {
                                            selectedPlaylistId = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                        )
                                    }

                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = title,
                                            fontWeight =
                                                FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface,
                                            maxLines = 1,
                                            overflow =
                                                TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text =
                                                "${plVideos.size} Files",
                                            fontSize = 11.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                        )
                                    }

                                    if (plVideos.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                onOpenNormalPlayer(
                                                    plVideos.first(),
                                                    plVideos
                                                )
                                            }
                                        ) {
                                            Text(
                                                "Play All",
                                                color = VlcOrange,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (plVideos.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = horizontalPadding, vertical = 48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Playlist is empty",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Add videos from video options (•••) menu",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = responsiveGridCells(
                                            windowSize = windowSize,
                                            compactColumns = 2,
                                            mediumColumns = 3,
                                            expandedColumns = 4
                                        ),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding =
                                            PaddingValues(
                                                start = horizontalPadding,
                                                end = horizontalPadding,
                                                top = 8.dp,
                                                bottom = 88.dp
                                            ),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(14.dp)
                                    ) {

                                        items(
                                            plVideos,
                                            key = { it.id }
                                        ) { video ->
                                            val isSelected = video.id in selectedVideoIds

                                            ExactVideoGridCard(
                                                video = video,
                                                isSelected = isSelected,
                                                isSelectionMode = selectedVideoIds.isNotEmpty(),
                                                onClick = {
                                                    if (selectedVideoIds.isNotEmpty()) {
                                                        selectedVideoIds = if (isSelected) {
                                                            selectedVideoIds - video.id
                                                        } else {
                                                            selectedVideoIds + video.id
                                                        }
                                                    } else {
                                                        onOpenNormalPlayer(video, plVideos)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedVideoIds = if (isSelected) {
                                                        selectedVideoIds - video.id
                                                    } else {
                                                        selectedVideoIds + video.id
                                                    }
                                                },
                                                onOptionsClick = {
                                                    selectedOptionsVideo =
                                                        video
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // FAB
            // -------------------------------------------------------------

            FloatingActionButton(
                onClick = {

                    val toPlay = when (selectedSubTabIndex) {

                        0 ->
                            videos.firstOrNull()

                        1 ->
                            (
                                selectedFolder?.let { folder ->
                                    allVideos.firstOrNull {
                                        it.parentFolderName.equals(
                                            folder.name,
                                            ignoreCase = true
                                        )
                                    }
                                }
                                    ?: videos.firstOrNull()
                                )

                        2 ->
                            activeVideoPlaylist?.second?.firstOrNull()
                                ?: favoriteVideos.firstOrNull()
                                ?: videos.firstOrNull()

                        else ->
                            videos.firstOrNull()
                    }

                    if (toPlay != null) {
                        onOpenNormalPlayer(toPlay)
                    }
                },
                containerColor = VlcOrange,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(56.dp)
                    .testTag("vlc_play_fab")
            ) {

                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // =====================================================================
    // NEW PLAYLIST DIALOG
    // =====================================================================

    if (showCreatePlaylistDialog) {

        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "New Playlist",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {

                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = {
                        newPlaylistName = it
                    },
                    placeholder = {
                        Text(
                            "Playlist name",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor =
                            MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor =
                            MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = VlcOrange,
                        unfocusedBorderColor =
                            MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {

                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim(), "VIDEOS")
                            showCreatePlaylistDialog = false
                            newPlaylistName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Create",
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
                    Text(
                        "Cancel",
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        )
    }

    // Rename Video Playlist Dialog
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

    // Delete Video Playlist Dialog
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
                    text = "Are you sure you want to delete \"${pl.title}\"? The video files themselves will not be deleted.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(pl.id)
                        playlistToDelete = null
                        if (selectedPlaylistId == pl.id) {
                            selectedPlaylistId = null
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

    // =====================================================================
    // DISPLAY SETTINGS
    // =====================================================================

    if (showDisplaySettingsSheet) {

        DisplaySettingsBottomSheet(
            isListView = isListView,
            onToggleListView = {
                isListView = it
                displayPreferences.edit()
                    .putBoolean("is_list_view", it)
                    .apply()
            },
            showOnlyFavorites = showOnlyFavorites,
            onToggleFavoritesOnly = {
                showOnlyFavorites = it
                displayPreferences.edit()
                    .putBoolean("show_only_favorites", it)
                    .apply()
            },
            sortOrder = sortOrder,
            onSortOrderSelected = { order ->
                onSortChange(order)
                showDisplaySettingsSheet = false
            },
            onDismiss = {
                showDisplaySettingsSheet = false
            }
        )
    }

    // =====================================================================
    // VIDEO OPTIONS
    // =====================================================================

    selectedOptionsVideo?.let { vid ->

        VideoOptionsBottomSheet(
            video = vid,
            onDismiss = {
                selectedOptionsVideo = null
            },
            onPlay = { target ->
                onOpenNormalPlayer(target)
            },
            onPlayInShorts = { target ->
                onPlayInShorts(target)
            },
            onPlayAsPip = { target ->
                onPlayAsPip(target)
            },
            onPlayFromStart = { target ->
                onPlayFromStart(target)
            },
            onPlayAll = {
                effectiveVideos.firstOrNull()?.let {
                    onOpenNormalPlayer(it)
                }
            },
            onPlayAsAudio = { target ->
                onPlayAsAudio(target)
            },
            onAddToQueue = { target ->
                Toast.makeText(
                    context,
                    "Added '${target.title}' to play queue",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onInsertNext = { target ->
                Toast.makeText(
                    context,
                    "Inserted '${target.title}' next in queue",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDownloadSubtitles = { target ->
                Toast.makeText(
                    context,
                    "Searching subtitles for '${target.title}'...",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onInformation = { target ->
                onShowInfo(target)
            },
            onAddToPlaylist = {
                videoForAddToPlaylist = vid
                showAddToPlaylistMultipleDialog = true
            },
            onSetAsRingtone = {
                Toast.makeText(
                    context,
                    "Audio track set as ringtone",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onToggleFavorite = { target ->
                onToggleFavorite(target)
            },
            onDelete = { target ->
                onDeleteVideo(target)
            },
            onShare = { target ->
                onShareVideo(target)
            },
            onCreateShortcut = { target ->
                Toast.makeText(
                    context,
                    "Launcher shortcut created for '${target.title}'",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // =====================================================================
    // MULTI-DELETE CONFIRMATION DIALOG
    // =====================================================================

    if (showDeleteMultipleDialog) {
        val selectedVideos = videos.filter { it.id in selectedVideoIds }

        AlertDialog(
            onDismissRequest = { showDeleteMultipleDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete ${selectedVideos.size} Videos Permanently?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete these ${selectedVideos.size} video file(s) permanently from your device storage memory? The files will be completely erased and this action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteMultipleDialog = false
                        onDeleteMultipleVideos(selectedVideos)
                        selectedVideoIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Permanently (${selectedVideos.size})", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMultipleDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // =====================================================================
    // MULTI-ADD-TO-PLAYLIST DIALOG
    // =====================================================================

    if (showAddToPlaylistMultipleDialog) {
        val targetVideos = if (videoForAddToPlaylist != null) {
            listOf(videoForAddToPlaylist!!)
        } else {
            allVideos.filter { it.id in selectedVideoIds }
        }

        AlertDialog(
            onDismissRequest = {
                showAddToPlaylistMultipleDialog = false
                videoForAddToPlaylist = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (targetVideos.size == 1) "Add \"${targetVideos.first().title}\" to Playlist" else "Add ${targetVideos.size} videos to Playlist",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (appPlaylists.isEmpty()) {
                        Text(
                            text = "No video playlists yet. Create one below.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(appPlaylists, key = { it.id }) { pl ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showAddToPlaylistMultipleDialog = false
                                            onAddMultipleVideosToPlaylist(pl.id, targetVideos)
                                            videoForAddToPlaylist = null
                                            selectedVideoIds = emptySet()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = VlcOrange,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = pl.title,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showAddToPlaylistMultipleDialog = false
                            showCreatePlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = VlcOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create New Playlist", color = VlcOrange)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddToPlaylistMultipleDialog = false
                    videoForAddToPlaylist = null
                }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // =====================================================================
    // MULTI-INFO SUMMARY DIALOG
    // =====================================================================

    multiInfoVideos?.let { infoList ->
        val totalSize = infoList.sumOf { it.sizeBytes }
        val totalDurationMs = infoList.sumOf { it.durationMs }
        val totalHours = totalDurationMs / 3600000
        val totalMinutes = (totalDurationMs % 3600000) / 60000
        val totalSeconds = (totalDurationMs % 60000) / 1000
        val formattedDuration = if (totalHours > 0) {
            String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", totalHours, totalMinutes, totalSeconds)
        } else {
            String.format(java.util.Locale.getDefault(), "%d:%02d", totalMinutes, totalSeconds)
        }

        AlertDialog(
            onDismissRequest = { multiInfoVideos = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Selection Details",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Total Selected: ${infoList.size} videos", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Combined Duration: $formattedDuration", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Combined Size: ${android.text.format.Formatter.formatFileSize(context, totalSize)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { multiInfoVideos = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================================================
// VIDEO GRID CARD
// ==========================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExactVideoGridCard(
    video: VideoItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onOptionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {

            VideoThumbnail(
                video = video,
                modifier = Modifier.fillMaxSize(),
                showBadge = false
            )

            // SD / HD / 4K Badge at top-left
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = when {
                        video.is4kUltraHd -> "4K"
                        video.resolutionLabel.contains("HD", ignoreCase = true) -> "HD"
                        else -> "SD"
                    },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = VlcOrange,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            } else if (isSelectionMode) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                ) {}
            } else {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                ) {

                    IconButton(
                        onClick = onOptionsClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = video.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp,
            modifier = Modifier.padding(
                top = 6.dp,
                start = 2.dp,
                end = 2.dp
            )
        )

        Text(
            text = if (video.formattedSize.isNotBlank()) "${video.formattedDuration} • ${video.formattedSize}" else video.formattedDuration,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = 2.dp,
                start = 2.dp,
                bottom = 4.dp
            )
        )
    }
}

// ==========================================================================
// FOLDER GRID CARD
// ==========================================================================

@Composable
private fun ExactFolderGridCard(
    folder: FolderItem,
    previewVideos: List<VideoItem>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Row(
                    modifier = Modifier.weight(1f)
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    ) {
                        if (previewVideos.isNotEmpty()) {
                            VideoThumbnail(
                                video = previewVideos[0],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    ) {
                        if (previewVideos.size > 1) {
                            VideoThumbnail(
                                video = previewVideos[1],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface
                        )
                )

                Row(
                    modifier = Modifier.weight(1f)
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    ) {
                        if (previewVideos.size > 2) {
                            VideoThumbnail(
                                video = previewVideos[2],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    ) {
                        if (previewVideos.size > 3) {
                            VideoThumbnail(
                                video = previewVideos[3],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = folder.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                top = 6.dp,
                start = 2.dp
            )
        )

        Text(
            text = "${folder.videoCount} videos • ${folder.formattedSize}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = 2.dp,
                start = 2.dp
            )
        )
    }
}
