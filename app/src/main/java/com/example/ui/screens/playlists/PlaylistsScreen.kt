package com.example.ui.screens.playlists

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameOption
import android.media.MediaMetadataRetriever
import com.example.data.model.PlaylistData
import com.example.data.model.PlaylistMediaItem
import com.example.data.model.PlaylistMediaType
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import com.example.ui.components.DeleteGenericConfirmationDialog
import com.example.ui.components.DeleteMultipleConfirmationDialog
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.responsiveGridCells
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.V2SPlaylistSymbol
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcOrange
import com.example.ui.theme.VlcOrangeDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistData>,
    allVideos: List<VideoItem>,
    allSongs: List<SongItem>,
    onPlayMedia: (PlaylistMediaItem, List<PlaylistMediaItem>) -> Unit,
    onPlayAsAudio: (PlaylistMediaItem) -> Unit,
    onPlayPlaylist: (PlaylistData, Boolean) -> Unit,
    onCreatePlaylist: (String, String) -> Unit = { _, _ -> },
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddMediaToPlaylist: (String, PlaylistMediaType, Long) -> Unit,
    onRemoveMediaFromPlaylist: (String, PlaylistMediaItem) -> Unit,
    onShareMedia: (PlaylistMediaItem) -> Unit,
    onDeleteMultiplePlaylistItems: (String, List<PlaylistMediaItem>) -> Unit = { _, _ -> },
    onAddMultipleMediaItemsToPlaylist: (String, List<PlaylistMediaItem>) -> Unit = { _, _ -> },
    onShareMultipleMedia: (List<PlaylistMediaItem>) -> Unit = {},
    onPlayMultipleMedia: (List<PlaylistMediaItem>) -> Unit = {},
    onDeleteMultiplePlaylists: (List<String>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Videos, 2: Songs
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Multi-select for overview
    var isOverviewSelectionMode by remember { mutableStateOf(false) }
    var selectedPlaylistIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteMultiplePlaylistsConfirmation by remember { mutableStateOf(false) }

    // Dialog & Sheet states
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistData?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistData?>(null) }
    var playlistForActions by remember { mutableStateOf<PlaylistData?>(null) }
    var itemForActions by remember {
        mutableStateOf<Pair<PlaylistMediaItem, PlaylistData>?>(null)
    }
    var showAddMediaSheetForPlaylist by remember {
        mutableStateOf<PlaylistData?>(null)
    }
    var itemForInfo by remember { mutableStateOf<PlaylistMediaItem?>(null) }

    val activePlaylist = playlists.find { it.id == selectedPlaylistId }

    if (activePlaylist != null) {
        BackHandler {
            selectedPlaylistId = null
        }

        PlaylistDetailView(
            playlist = activePlaylist,
            playlists = playlists,
            onBack = { selectedPlaylistId = null },
            onPlayMedia = { item ->
                onPlayMedia(item, activePlaylist.items)
            },
            onPlayAsAudio = onPlayAsAudio,
            onPlayAll = { shuffle ->
                onPlayPlaylist(activePlaylist, shuffle)
            },
            onOpenAddMedia = {
                showAddMediaSheetForPlaylist = activePlaylist
            },
            onOpenItemActions = { item ->
                itemForActions = Pair(item, activePlaylist)
            },
            onOpenPlaylistActions = {
                playlistForActions = activePlaylist
            },
            onDeleteMultiplePlaylistItems = onDeleteMultiplePlaylistItems,
            onAddMultipleMediaItemsToPlaylist = onAddMultipleMediaItemsToPlaylist,
            onShareMultipleMedia = onShareMultipleMedia,
            onPlayMultipleMedia = onPlayMultipleMedia,
            onCreatePlaylist = { name -> onCreatePlaylist(name, activePlaylist.category) },
            modifier = modifier
        )
    } else {
        BackHandler(enabled = isOverviewSelectionMode) {
            isOverviewSelectionMode = false
            selectedPlaylistIds = emptySet()
        }

        val categoryFilteredPlaylists = when (selectedCategoryFilter) {
            1 -> playlists.filter { it.isVideoPlaylist }
            2 -> playlists.filter { it.isSongPlaylist }
            else -> playlists
        }

        val filteredPlaylists = if (searchQuery.isBlank()) {
            categoryFilteredPlaylists
        } else {
            categoryFilteredPlaylists.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }

        val userPlaylists = filteredPlaylists.filter { !it.isSystem }
        val defaultPlaylists = filteredPlaylists.filter { it.isSystem }

        val sectionedUserPlaylists = userPlaylists
            .groupBy { it.sectionHeader }
            .toSortedMap()

        ResponsiveScreen(
            modifier = modifier
        ) { windowSize, horizontalPadding ->

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    // Top App Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                        ) {
                            if (isOverviewSelectionMode) {
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                isOverviewSelectionMode = false
                                                selectedPlaylistIds = emptySet()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Cancel Selection",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Text(
                                            text = "${selectedPlaylistIds.size} Selected",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val allUserIds = userPlaylists.map { it.id }.toSet()
                                        val isAllSelected = selectedPlaylistIds.isNotEmpty() && selectedPlaylistIds.containsAll(allUserIds)

                                        TextButton(
                                            onClick = {
                                                selectedPlaylistIds = if (isAllSelected) {
                                                    emptySet()
                                                } else {
                                                    allUserIds
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isAllSelected) Icons.Filled.Deselect else Icons.Filled.SelectAll,
                                                contentDescription = null,
                                                tint = VlcOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isAllSelected) "Deselect All" else "Select All",
                                                color = VlcOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
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
                                        modifier = Modifier
                                            .weight(1f, fill = false),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        V2SBrandLogo(
                                            showTagline = horizontalPadding > 10.dp
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                        ) {
                                            Text(
                                                text = "${playlists.size}",
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
                                        if (userPlaylists.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    isOverviewSelectionMode = true
                                                },
                                                modifier = Modifier
                                                    .testTag("playlist_select_btn")
                                                    .size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Checklist,
                                                    contentDescription = "Select",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                isSearchActive = !isSearchActive
                                                if (!isSearchActive) {
                                                    searchQuery = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .testTag("playlist_search_btn")
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSearchActive) {
                                                    Icons.Filled.Clear
                                                } else {
                                                    Icons.Filled.Search
                                                },
                                                contentDescription = "Search Playlists",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                showCreateDialog = true
                                            },
                                            modifier = Modifier
                                                .testTag("playlist_add_btn")
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "New Playlist",
                                                tint = VlcOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Box {
                                            IconButton(
                                                onClick = {
                                                    showOverflowMenu = true
                                                },
                                                modifier = Modifier
                                                    .testTag("playlist_overflow_btn")
                                                    .size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "More Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showOverflowMenu,
                                                onDismissRequest = {
                                                    showOverflowMenu = false
                                                }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("New Playlist")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Filled.LibraryAdd,
                                                            contentDescription = null,
                                                            tint = VlcOrange
                                                        )
                                                    },
                                                    onClick = {
                                                        showOverflowMenu = false
                                                        showCreateDialog = true
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = {
                                                        Text("Search Playlists")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Filled.Search,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        showOverflowMenu = false
                                                        isSearchActive = true
                                                    }
                                                )

                                                if (userPlaylists.isNotEmpty()) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text("Select Playlists")
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Checklist,
                                                                contentDescription = null,
                                                                tint = VlcOrange
                                                            )
                                                        },
                                                        onClick = {
                                                            showOverflowMenu = false
                                                            isOverviewSelectionMode = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = isSearchActive,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                    },
                                    placeholder = {
                                        Text(
                                            "Search playlists...",
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
                                                    searchQuery = ""
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Clear,
                                                    contentDescription = "Clear"
                                                )
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VlcOrange,
                                        unfocusedBorderColor =
                                            MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 6.dp
                                        )
                                )
                            }
                        }
                    }

                    // Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == 0,
                            onClick = { selectedCategoryFilter = 0 },
                            label = { Text("All (${playlists.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VlcOrange.copy(alpha = 0.2f),
                                selectedLabelColor = VlcOrange
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategoryFilter == 0,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = VlcOrange
                            )
                        )

                        val videoCount = remember(playlists) { playlists.count { it.isVideoPlaylist } }
                        FilterChip(
                            selected = selectedCategoryFilter == 1,
                            onClick = { selectedCategoryFilter = 1 },
                            label = { Text("Videos ($videoCount)") },
                            leadingIcon = {
                                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VlcOrange.copy(alpha = 0.2f),
                                selectedLabelColor = VlcOrange
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategoryFilter == 1,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = VlcOrange
                            )
                        )

                        val songCount = remember(playlists) { playlists.count { it.isSongPlaylist } }
                        FilterChip(
                            selected = selectedCategoryFilter == 2,
                            onClick = { selectedCategoryFilter = 2 },
                            label = { Text("Songs ($songCount)") },
                            leadingIcon = {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VlcOrange.copy(alpha = 0.2f),
                                selectedLabelColor = VlcOrange
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategoryFilter == 2,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = VlcOrange
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Empty state
                    if (filteredPlaylists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalPadding)
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                V2SPlaylistSymbol(size = 64.dp)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "No matching playlists"
                                    } else {
                                        "No Playlists Found"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Tap '+' to create your first custom playlist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        showCreateDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VlcOrange
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null
                                    )

                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )

                                    Text("New Playlist")
                                }
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
                            contentPadding = PaddingValues(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = 8.dp,
                                bottom = if (isOverviewSelectionMode && selectedPlaylistIds.isNotEmpty()) 120.dp else 96.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {

                            if (userPlaylists.isNotEmpty()) {
                                sectionedUserPlaylists.forEach { (sectionHeader, playlistsInSection) ->
                                    item(
                                        span = {
                                            GridItemSpan(maxLineSpan)
                                        }
                                    ) {
                                        Text(
                                            text = sectionHeader,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = VlcOrange,
                                            modifier = Modifier.padding(
                                                top = 8.dp,
                                                bottom = 4.dp
                                            )
                                        )
                                    }

                                    items(
                                        playlistsInSection,
                                        key = { it.id }
                                    ) { playlist ->
                                        val isSelected = playlist.id in selectedPlaylistIds
                                        PlaylistCard(
                                            playlist = playlist,
                                            isSelectionMode = isOverviewSelectionMode,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isOverviewSelectionMode) {
                                                    selectedPlaylistIds = if (isSelected) {
                                                        selectedPlaylistIds - playlist.id
                                                    } else {
                                                        selectedPlaylistIds + playlist.id
                                                    }
                                                } else {
                                                    selectedPlaylistId = playlist.id
                                                }
                                            },
                                            onLongClick = {
                                                if (!isOverviewSelectionMode && !playlist.isSystem) {
                                                    isOverviewSelectionMode = true
                                                    selectedPlaylistIds = setOf(playlist.id)
                                                }
                                            },
                                            onToggleSelect = {
                                                selectedPlaylistIds = if (isSelected) {
                                                    selectedPlaylistIds - playlist.id
                                                } else {
                                                    selectedPlaylistIds + playlist.id
                                                }
                                            },
                                            onPlay = {
                                                onPlayPlaylist(
                                                    playlist,
                                                    false
                                                )
                                            },
                                            onMore = {
                                                playlistForActions = playlist
                                            }
                                        )
                                    }
                                }
                            }

                            if (defaultPlaylists.isNotEmpty()) {
                                item(
                                    span = {
                                        GridItemSpan(maxLineSpan)
                                    }
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VlcOrange,
                                        modifier = Modifier.padding(
                                            top = if (userPlaylists.isNotEmpty()) 16.dp else 8.dp,
                                            bottom = 4.dp
                                        )
                                    )
                                }

                                items(
                                    defaultPlaylists,
                                    key = { it.id }
                                ) { playlist ->
                                    PlaylistCard(
                                        playlist = playlist,
                                        isSelectionMode = false,
                                        isSelected = false,
                                        onClick = {
                                            selectedPlaylistId = playlist.id
                                        },
                                        onPlay = {
                                            onPlayPlaylist(
                                                playlist,
                                                false
                                            )
                                        },
                                        onMore = {
                                            playlistForActions = playlist
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Overview Selection Floating Action Bar
                if (isOverviewSelectionMode && selectedPlaylistIds.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = horizontalPadding, end = horizontalPadding)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF242730),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${selectedPlaylistIds.size} selected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Button(
                                onClick = {
                                    showDeleteMultiplePlaylistsConfirmation = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delete (${selectedPlaylistIds.size})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteMultiplePlaylistsConfirmation) {
        DeleteMultipleConfirmationDialog(
            count = selectedPlaylistIds.size,
            mediaType = "playlists",
            onConfirm = {
                onDeleteMultiplePlaylists(selectedPlaylistIds.toList())
                selectedPlaylistIds = emptySet()
                isOverviewSelectionMode = false
                showDeleteMultiplePlaylistsConfirmation = false
                Toast.makeText(context, "Playlists deleted", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showDeleteMultiplePlaylistsConfirmation = false
            }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DIALOGS & BOTTOM SHEETS
    // ─────────────────────────────────────────────────────────────────────────

    if (showCreateDialog) {
        var playlistNameInput by remember {
            mutableStateOf("")
        }
        var selectedCreateCategory by remember(selectedCategoryFilter) {
            mutableStateOf(
                when (selectedCategoryFilter) {
                    1 -> "VIDEOS"
                    2 -> "SONGS"
                    else -> "ALL"
                }
            )
        }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
            },
            title = {
                Text(
                    "New Playlist",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = playlistNameInput,
                        onValueChange = {
                            playlistNameInput = it
                        },
                        label = {
                            Text("Playlist name")
                        },
                        placeholder = {
                            Text("e.g. My Favorites")
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VlcOrange,
                            focusedLabelColor = VlcOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Playlist Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCreateCategory == "ALL",
                            onClick = { selectedCreateCategory = "ALL" },
                            label = { Text("All Media", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedCreateCategory == "VIDEOS",
                            onClick = { selectedCreateCategory = "VIDEOS" },
                            label = { Text("Videos", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(13.dp))
                            }
                        )
                        FilterChip(
                            selected = selectedCreateCategory == "SONGS",
                            onClick = { selectedCreateCategory = "SONGS" },
                            label = { Text("Songs", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(13.dp))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            onCreatePlaylist(playlistNameInput.trim(), selectedCreateCategory)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange
                    ),
                    enabled = playlistNameInput.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    playlistToRename?.let { pl ->

        var renameInput by remember {
            mutableStateOf(pl.title)
        }

        AlertDialog(
            onDismissRequest = {
                playlistToRename = null
            },
            title = {
                Text(
                    "Rename Playlist",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = {
                        renameInput = it
                    },
                    label = {
                        Text("Playlist title")
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
                        if (renameInput.isNotBlank()) {
                            onRenamePlaylist(
                                pl.id,
                                renameInput
                            )
                            playlistToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VlcOrange
                    ),
                    enabled = renameInput.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        playlistToRename = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    playlistToDelete?.let { pl ->

        AlertDialog(
            onDismissRequest = {
                playlistToDelete = null
            },
            title = {
                Text(
                    "Delete Playlist",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${pl.title}\"? " +
                        "Media files will not be deleted from your device."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(pl.id)

                        if (selectedPlaylistId == pl.id) {
                            selectedPlaylistId = null
                        }

                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        playlistToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    playlistForActions?.let { pl ->

        PlaylistActionsBottomSheet(
            playlist = pl,
            onDismiss = {
                playlistForActions = null
            },
            onPlay = {
                onPlayPlaylist(pl, false)
                playlistForActions = null
            },
            onShuffle = {
                onPlayPlaylist(pl, true)
                playlistForActions = null
            },
            onAddMedia = {
                showAddMediaSheetForPlaylist = pl
                playlistForActions = null
            },
            onRename = {
                playlistToRename = pl
                playlistForActions = null
            },
            onDelete = {
                playlistToDelete = pl
                playlistForActions = null
            }
        )
    }

    itemForActions?.let { (item, pl) ->

        ItemActionsBottomSheet(
            item = item,
            playlist = pl,
            onDismiss = {
                itemForActions = null
            },
            onPlay = {
                onPlayMedia(item, pl.items)
                itemForActions = null
            },
            onPlayAsAudio = {
                onPlayAsAudio(item)
                itemForActions = null
            },
            onShowInfo = {
                itemForInfo = item
                itemForActions = null
            },
            onRemoveFromPlaylist = {
                onRemoveMediaFromPlaylist(pl.id, item)
                itemForActions = null
            },
            onShare = {
                onShareMedia(item)
                itemForActions = null
            }
        )
    }

    itemForInfo?.let { item ->

        AlertDialog(
            onDismissRequest = {
                itemForInfo = null
            },
            title = {
                Text(
                    "Track Information",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Title: ${item.title}",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Type: ${
                            if (item.type == PlaylistMediaType.VIDEO) {
                                "Video"
                            } else {
                                "Audio"
                            }
                        }"
                    )

                    Text(
                        "Duration: ${item.formattedDuration}"
                    )

                    if (item.resolutionLabel.isNotBlank()) {
                        Text(
                            "Resolution: ${item.resolutionLabel}"
                        )
                    }

                    if (item.formattedSize.isNotBlank()) {
                        Text(
                            "Size: ${item.formattedSize}"
                        )
                    }

                    Text(
                        "URI: ${item.uri}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemForInfo = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    showAddMediaSheetForPlaylist?.let { pl ->

        AddMediaToPlaylistSheet(
            playlist = pl,
            allVideos = allVideos,
            allSongs = allSongs,
            onDismiss = {
                showAddMediaSheetForPlaylist = null
            },
            onAddMedia = { type, id ->
                onAddMediaToPlaylist(
                    pl.id,
                    type,
                    id
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST CARD
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistCard(
    playlist: PlaylistData,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
    onPlay: () -> Unit,
    onMore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, VlcOrange) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("playlist_card_${playlist.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp
                        )
                    )
                    .background(Color(0xFF1B1E26))
            ) {
                PlaylistCollageThumbnail(
                    playlist = playlist,
                    modifier = Modifier.fillMaxSize()
                )

                val systemBadge = when {
                    playlist.isFavorites || playlist.id == "favorites" -> Icons.Filled.Favorite to Color(0xFFFF4081)
                    playlist.id == "all_videos" -> Icons.Filled.Videocam to VlcOrange
                    playlist.id == "all_songs" -> Icons.Filled.MusicNote to Color(0xFF00E5FF)
                    playlist.id == "history" -> Icons.Filled.History to Color(0xFFFFB703)
                    else -> null
                }

                if (systemBadge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Black.copy(alpha = 0.65f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = systemBadge.first,
                            contentDescription = playlist.title,
                            tint = systemBadge.second,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    val badgeLabel = when {
                        playlist.category.equals("VIDEOS", ignoreCase = true) || playlist.id.startsWith("pl_videos_") -> "VIDEO"
                        playlist.category.equals("SONGS", ignoreCase = true) || playlist.id.startsWith("pl_songs_") -> "SONG"
                        else -> null
                    }
                    if (badgeLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                color = if (badgeLabel == "VIDEO") VlcOrange else Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isSelectionMode && !playlist.isSystem) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelect() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = VlcOrange,
                                uncheckedColor = Color.White
                            )
                        )
                    }
                } else if (!isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onPlay),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Playlist",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "${playlist.totalTracks} tracks • ${playlist.formattedShortDuration}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isSelectionMode) {
                    IconButton(
                        onClick = onMore,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST COLLAGE THUMBNAIL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistCollageThumbnail(
    playlist: PlaylistData,
    modifier: Modifier = Modifier
) {
    val items = playlist.items

    if (items.isEmpty()) {
        Box(
            modifier = modifier.background(
                Color(0xFF20242E)
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    playlist.isFavorites || playlist.id == "favorites" -> Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFFF4081).copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    playlist.id == "all_videos" -> Icon(Icons.Filled.Videocam, contentDescription = null, tint = VlcOrange.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    playlist.id == "all_songs" -> Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF).copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    playlist.id == "history" -> Icon(Icons.Filled.History, contentDescription = null, tint = Color(0xFFFFB703).copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    else -> V2SPlaylistSymbol(size = 44.dp)
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Empty",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        val firstItem = items.first()
        MediaThumbnailImage(
            item = firstItem,
            modifier = modifier
        )
    }
}

@Composable
private fun MediaThumbnailImage(
    item: PlaylistMediaItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (item.type == PlaylistMediaType.VIDEO) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.uri)
                .decoderFactory(
                    VideoFrameDecoder.Factory()
                )
                .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = modifier.background(
                Color(0xFF1E222A)
            )
        )
    } else {
        if (item.albumArtUri != null) {
            AsyncImage(
                model = item.albumArtUri,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = modifier.background(
                    Color(0xFF1E222A)
                )
            )
        } else {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = VlcOrange.copy(alpha = 0.75f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST DETAIL VIEW
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PlaylistDetailView(
    playlist: PlaylistData,
    playlists: List<PlaylistData> = emptyList(),
    onBack: () -> Unit,
    onPlayMedia: (PlaylistMediaItem) -> Unit,
    onPlayAsAudio: (PlaylistMediaItem) -> Unit,
    onPlayAll: (Boolean) -> Unit,
    onOpenAddMedia: () -> Unit,
    onOpenItemActions: (PlaylistMediaItem) -> Unit,
    onOpenPlaylistActions: () -> Unit,
    onDeleteMultiplePlaylistItems: (String, List<PlaylistMediaItem>) -> Unit = { _, _ -> },
    onAddMultipleMediaItemsToPlaylist: (String, List<PlaylistMediaItem>) -> Unit = { _, _ -> },
    onShareMultipleMedia: (List<PlaylistMediaItem>) -> Unit = {},
    onPlayMultipleMedia: (List<PlaylistMediaItem>) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Multi-selection states
    var isDetailSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteMultipleConfirmation by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistForAddDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isDetailSelectionMode) {
        isDetailSelectionMode = false
        selectedItemIds = emptySet()
    }

    val filteredItems = if (searchQuery.isBlank()) {
        playlist.items
    } else {
        playlist.items.filter {
            it.title.contains(
                searchQuery,
                ignoreCase = true
            ) ||
                it.subtitle.contains(
                    searchQuery,
                    ignoreCase = true
                )
        }
    }

    val selectedItems = remember(selectedItemIds, playlist.items) {
        playlist.items.filter { it.mediaId in selectedItemIds }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDetailSelectionMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        isDetailSelectionMode = false
                                        selectedItemIds = emptySet()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Cancel Selection",
                                        tint = Color.White
                                    )
                                }

                                Text(
                                    text = "${selectedItemIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val allFilteredIds = filteredItems.map { it.mediaId }.toSet()
                                val isAllSelected = selectedItemIds.isNotEmpty() && selectedItemIds.containsAll(allFilteredIds)

                                TextButton(
                                    onClick = {
                                        selectedItemIds = if (isAllSelected) {
                                            emptySet()
                                        } else {
                                            allFilteredIds
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isAllSelected) Icons.Filled.Deselect else Icons.Filled.SelectAll,
                                        contentDescription = null,
                                        tint = VlcOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAllSelected) "Deselect All" else "Select All",
                                        color = VlcOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 6.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )

                            if (filteredItems.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        isDetailSelectionMode = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Checklist,
                                        contentDescription = "Select Items",
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    isSearchActive =
                                        !isSearchActive
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSearchActive) {
                                        Icons.Filled.Clear
                                    } else {
                                        Icons.Filled.Search
                                    },
                                    contentDescription =
                                        "Search in Playlist",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = onOpenPlaylistActions
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription =
                                        "Playlist Options",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isSearchActive
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            placeholder = {
                                Text(
                                    "Filter tracks in playlist...",
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VlcOrange,
                                unfocusedBorderColor =
                                    Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 6.dp
                                )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 12.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color(0xFF1B1E26)
                                )
                        ) {
                            PlaylistCollageThumbnail(
                                playlist = playlist,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = playlist.title,
                                style =
                                    MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "${playlist.formattedDuration} • ${playlist.totalTracks} tracks",
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onPlayAll(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VlcOrange
                            ),
                            shape = RoundedCornerShape(24.dp),
                            enabled = playlist.items.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag(
                                    "playlist_play_all_btn"
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "PLAY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                onPlayAll(true)
                            },
                            shape = RoundedCornerShape(24.dp),
                            enabled = playlist.items.isNotEmpty(),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        FilledTonalButton(
                            onClick = onOpenAddMedia,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Media",
                                tint = VlcOrange,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text = "Add",
                                color = VlcOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No matching tracks"
                            } else {
                                "No Tracks in this Playlist"
                            },
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Tap '+ Add' above to add any videos or audio tracks",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = if (isDetailSelectionMode && selectedItemIds.isNotEmpty()) 120.dp else 96.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        filteredItems,
                        key = { it.id }
                    ) { item ->
                        val isSelected = item.mediaId in selectedItemIds
                        TrackListItem(
                            item = item,
                            isSelectionMode = isDetailSelectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isDetailSelectionMode) {
                                    selectedItemIds = if (isSelected) {
                                        selectedItemIds - item.mediaId
                                    } else {
                                        selectedItemIds + item.mediaId
                                    }
                                } else {
                                    onPlayMedia(item)
                                }
                            },
                            onLongClick = {
                                if (!isDetailSelectionMode) {
                                    isDetailSelectionMode = true
                                    selectedItemIds = setOf(item.mediaId)
                                }
                            },
                            onToggleSelect = {
                                selectedItemIds = if (isSelected) {
                                    selectedItemIds - item.mediaId
                                } else {
                                    selectedItemIds + item.mediaId
                                }
                            },
                            onMore = {
                                onOpenItemActions(item)
                            }
                        )
                    }
                }
            }
        }

        // Multi-Selection Floating Action Bar for Playlist detail
        if (isDetailSelectionMode && selectedItemIds.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF242730),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedItemIds.size} selected",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onPlayMultipleMedia(selectedItems)
                                isDetailSelectionMode = false
                                selectedItemIds = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Selected",
                                tint = VlcOrange
                            )
                        }

                        IconButton(
                            onClick = {
                                showAddToPlaylistDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Add Selected to Playlist",
                                tint = PlayitCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                onShareMultipleMedia(selectedItems)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Selected",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                showDeleteMultipleConfirmation = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove Selected from Playlist",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog for selected playlist items
    if (showDeleteMultipleConfirmation) {
        DeleteMultipleConfirmationDialog(
            count = selectedItems.size,
            mediaType = "playlist item(s)",
            onConfirm = {
                onDeleteMultiplePlaylistItems(playlist.id, selectedItems)
                isDetailSelectionMode = false
                selectedItemIds = emptySet()
                showDeleteMultipleConfirmation = false
            },
            onDismiss = {
                showDeleteMultipleConfirmation = false
            }
        )
    }

    // Add to Playlist Dialog for selected items
    if (showAddToPlaylistDialog) {
        val userPlaylists = playlists.filter { !it.isSystem && it.id != playlist.id }
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = {
                Text("Add ${selectedItems.size} items to Playlist")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            showAddToPlaylistDialog = false
                            showCreatePlaylistForAddDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = VlcOrange)
                            Text("+ Create New Playlist", color = VlcOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (userPlaylists.isEmpty()) {
                        Text(
                            "No other custom playlists found. Tap above to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            items(userPlaylists, key = { it.id }) { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddMultipleMediaItemsToPlaylist(pl.id, selectedItems)
                                            showAddToPlaylistDialog = false
                                            isDetailSelectionMode = false
                                            selectedItemIds = emptySet()
                                            Toast.makeText(context, "Added to ${pl.title}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = VlcOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = pl.title,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${pl.totalTracks} tracks",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create New Playlist dialog while adding
    if (showCreatePlaylistForAddDialog) {
        var newPlaylistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistForAddDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Enter playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newPlaylistName.trim()
                        if (name.isNotEmpty()) {
                            onCreatePlaylist(name)
                            showCreatePlaylistForAddDialog = false
                            Toast.makeText(context, "Playlist created. Select it from the list.", Toast.LENGTH_SHORT).show()
                            showAddToPlaylistDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VlcOrange)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistForAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TRACK LIST ITEM
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackListItem(
    item: PlaylistMediaItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = VlcOrange,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )
        }

        if (item.type == PlaylistMediaType.VIDEO) {
            Box(
                modifier = Modifier
                    .size(
                        width = 64.dp,
                        height = 40.dp
                    )
                    .clip(
                        RoundedCornerShape(6.dp)
                    )
                    .background(
                        Color(0xFF1E222A)
                    )
            ) {
                MediaThumbnailImage(
                    item = item,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(2.dp)
                        )
                        .padding(
                            horizontal = 3.dp,
                            vertical = 1.dp
                        )
                ) {
                    Text(
                        text = item.formattedDuration,
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF232832)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.albumArtUri != null) {
                    MediaThumbnailImage(
                        item = item,
                        modifier = Modifier.fillMaxSize()
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

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (
                    item.type == PlaylistMediaType.AUDIO
                ) {
                    "♪♪ ${item.title}"
                } else {
                    item.title
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isSelectionMode) {
            IconButton(
                onClick = onMore,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST ACTIONS BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistActionsBottomSheet(
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onAddMedia: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor =
            MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    bottom = 12.dp
                )
            )

            ActionRow(
                icon = Icons.Filled.PlayArrow,
                label = "Play",
                onClick = onPlay
            )

            ActionRow(
                icon = Icons.Filled.Shuffle,
                label = "Shuffle",
                onClick = onShuffle
            )

            ActionRow(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = "Add Media to Playlist",
                onClick = onAddMedia
            )

            if (!playlist.isSystem) {
                ActionRow(
                    icon = Icons.Filled.Edit,
                    label = "Rename",
                    onClick = onRename
                )

                ActionRow(
                    icon = Icons.Filled.DeleteOutline,
                    label = "Delete Playlist",
                    iconColor =
                        MaterialTheme.colorScheme.error,
                    onClick = onDelete
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ITEM ACTIONS BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemActionsBottomSheet(
    item: PlaylistMediaItem,
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayAsAudio: () -> Unit,
    onShowInfo: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onShare: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor =
            MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    bottom = 12.dp
                )
            )

            ActionRow(
                icon = Icons.Filled.PlayArrow,
                label = "Play",
                onClick = onPlay
            )

            if (item.type == PlaylistMediaType.VIDEO) {
                ActionRow(
                    icon = Icons.Filled.Headphones,
                    label = "Play as audio",
                    onClick = onPlayAsAudio
                )
            }

            ActionRow(
                icon = Icons.Filled.Info,
                label = "Information",
                onClick = onShowInfo
            )

            ActionRow(
                icon = Icons.Filled.DeleteOutline,
                label = if (playlist.isFavorites) {
                    "Remove from favorites"
                } else {
                    "Remove from playlist"
                },
                iconColor =
                    MaterialTheme.colorScheme.error,
                onClick = onRemoveFromPlaylist
            )

            ActionRow(
                icon = Icons.Filled.Share,
                label = "Share",
                onClick = onShare
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    iconColor: Color = VlcOrange,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADD MEDIA TO PLAYLIST SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMediaToPlaylistSheet(
    playlist: PlaylistData,
    allVideos: List<VideoItem>,
    allSongs: List<SongItem>,
    onDismiss: () -> Unit,
    onAddMedia: (PlaylistMediaType, Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    val isVideosOnly = !playlist.isSystem && playlist.isVideoPlaylist
    val isSongsOnly = !playlist.isSystem && playlist.isSongPlaylist

    var selectedTab by remember(playlist.id, isVideosOnly, isSongsOnly) {
        mutableIntStateOf(
            when {
                isVideosOnly -> 1
                isSongsOnly -> 2
                else -> 0
            }
        )
    }

    var query by remember {
        mutableStateOf("")
    }

    var addedIds by remember {
        mutableStateOf(
            playlist.items
                .map {
                    "${it.type}_${it.mediaId}"
                }
                .toSet()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor =
            MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = when {
                    isVideosOnly -> "Add Videos to \"${playlist.title}\""
                    isSongsOnly -> "Add Songs to \"${playlist.title}\""
                    else -> "Add Media to \"${playlist.title}\""
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                },
                placeholder = {
                    Text(
                        when {
                            isVideosOnly -> "Search videos to add..."
                            isSongsOnly -> "Search songs to add..."
                            else -> "Search media to add..."
                        }
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VlcOrange
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (!isVideosOnly && !isSongsOnly) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                        },
                        text = {
                            Text("All")
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                        },
                        text = {
                            Text(
                                "Videos (${allVideos.size})"
                            )
                        }
                    )

                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                        },
                        text = {
                            Text(
                                "Audio (${allSongs.size})"
                            )
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            val videoItems = allVideos
                .filter {
                    query.isBlank() ||
                        it.title.contains(
                            query,
                            ignoreCase = true
                        )
                }
                .map {
                    Pair(
                        PlaylistMediaType.VIDEO,
                        it.id to it.title
                    )
                }

            val songItems = allSongs
                .filter {
                    query.isBlank() ||
                        it.title.contains(
                            query,
                            ignoreCase = true
                        )
                }
                .map {
                    Pair(
                        PlaylistMediaType.AUDIO,
                        it.id to it.title
                    )
                }

            val displayList = when {
                isVideosOnly || selectedTab == 1 -> videoItems
                isSongsOnly || selectedTab == 2 -> songItems
                else -> videoItems + songItems
            }

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No media found",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 24.dp
                    )
                ) {
                    items(
                        displayList,
                        key = {
                            "${it.first}_${it.second.first}"
                        }
                    ) { (type, pair) ->

                        val (id, title) = pair
                        val key = "${type}_$id"
                        val isAdded =
                            addedIds.contains(key)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isAdded) {
                                        onAddMedia(
                                            type,
                                            id
                                        )

                                        addedIds =
                                            addedIds + key
                                    }
                                }
                                .padding(
                                    vertical = 10.dp,
                                    horizontal = 4.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    if (
                                        type ==
                                        PlaylistMediaType.VIDEO
                                    ) {
                                        Icons.Filled.Videocam
                                    } else {
                                        Icons.Filled.MusicNote
                                    },
                                contentDescription = null,
                                tint =
                                    if (
                                        type ==
                                        PlaylistMediaType.VIDEO
                                    ) {
                                        Color(0xFF64B5F6)
                                    } else {
                                        VlcOrange
                                    },
                                modifier =
                                    Modifier.size(24.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(12.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {
                                Text(
                                    text = title,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow =
                                        TextOverflow.Ellipsis
                                )

                                Text(
                                    text =
                                        if (
                                            type ==
                                            PlaylistMediaType.VIDEO
                                        ) {
                                            "Video"
                                        } else {
                                            "Audio track"
                                        },
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }

                            if (isAdded) {
                                Text(
                                    text = "Added",
                                    color = VlcOrange,
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        onAddMedia(
                                            type,
                                            id
                                        )

                                        addedIds =
                                            addedIds + key
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Filled.Add,
                                        contentDescription =
                                            "Add",
                                        tint = VlcOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
