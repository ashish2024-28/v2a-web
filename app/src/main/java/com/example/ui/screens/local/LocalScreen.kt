package com.example.ui.screens.local

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderItem
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.responsiveGridCells
import com.example.ui.components.VideoThumbnail
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcOrange
import androidx.compose.material.icons.automirrored.filled.ViewList

enum class FolderFileFilter(
    val label: String
) {
    ALL("All Files"),
    VIDEOS("Videos"),
    SONGS("Audio")
}

@Composable
fun LocalScreen(
    folders: List<FolderItem>,
    allVideos: List<VideoItem>,
    allSongs: List<SongItem>,
    onVideoClick: (VideoItem) -> Unit,
    onSongClick: (SongItem) -> Unit,
    onRefreshScan: () -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var selectedFolder by remember {
        mutableStateOf<FolderItem?>(null)
    }

    var isGridView by remember {
        mutableStateOf(true)
    }

    var fileFilter by remember {
        mutableStateOf(FolderFileFilter.ALL)
    }

    ResponsiveScreen(
        modifier = modifier
    ) { windowSize, horizontalPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {

            // =================================================================
            // FOLDER LIST SCREEN
            // =================================================================

            if (selectedFolder == null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {

                    // ---------------------------------------------------------
                    // HEADER
                    // ---------------------------------------------------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 12.dp
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

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(9.dp)
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Filled.Folder,
                                    contentDescription =
                                        "Folders",
                                    tint = VlcOrange,
                                    modifier =
                                        Modifier.size(25.dp)
                                )

                                Text(
                                    text = "Browse",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleLarge,
                                    fontWeight =
                                        FontWeight.Black,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onBackground
                                )
                            }
                        }

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {

                            IconButton(
                                onClick = onAddLinkClick
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Filled.Link,
                                    contentDescription =
                                        "Add Video or Song Link",
                                    tint = VlcOrange
                                )
                            }

                            IconButton(
                                onClick = onRefreshScan
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Filled.Refresh,
                                    contentDescription =
                                        "Scan Storage",
                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    isGridView = !isGridView
                                },
                                modifier =
                                    Modifier.testTag(
                                        "toggle_view_mode"
                                    )
                            ) {

                                Icon(
                                    imageVector =
                                        if (isGridView) {
                                            Icons.AutoMirrored.Filled.ViewList
                                        } else {
                                            Icons.Filled.GridView
                                        },
                                    contentDescription =
                                        "Toggle Grid/List View",
                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // STORAGE SUMMARY
                    // ---------------------------------------------------------

                    val totalVideos =
                        folders.sumOf {
                            it.videoCount
                        }

                    val totalSongs =
                        folders.sumOf {
                            it.songCount
                        }

                    val totalBytes =
                        folders.sumOf {
                            it.totalSizeBytes
                        }

                    val totalMb =
                        totalBytes / (1024 * 1024)

                    Surface(
                        shape =
                            RoundedCornerShape(14.dp),
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 4.dp
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
                                            "Internal Storage",
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
                                            "$totalVideos videos • $totalSongs songs",
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
                                    VlcOrange.copy(
                                        alpha = 0.15f
                                    )
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
                                    fontSize = 12.sp,
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                )
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // FILE FILTERS
                    // ---------------------------------------------------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        FolderFileFilter.values()
                            .forEach { filter ->

                                FilterChip(
                                    selected =
                                        fileFilter == filter,
                                    onClick = {
                                        fileFilter = filter
                                    },
                                    label = {
                                        Text(
                                            filter.label
                                        )
                                    },
                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
                                                selectedContainerColor =
                                                    VlcOrange.copy(
                                                        alpha = 0.15f
                                                    ),
                                                selectedLabelColor =
                                                    VlcOrange
                                            )
                                )
                            }
                    }

                    // ---------------------------------------------------------
                    // EMPTY STATE
                    // ---------------------------------------------------------

                    if (folders.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp),
                                modifier =
                                    Modifier.padding(24.dp)
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Filled.FolderOpen,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                            .copy(alpha = 0.4f),
                                    modifier =
                                        Modifier.size(56.dp)
                                )

                                Text(
                                    text =
                                        "No phone media folders found yet",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurface
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onRefreshScan,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VlcOrange,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Scan Phone Memory", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onAddLinkClick,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Add Link")
                                    }
                                }
                            }
                        }

                    } else if (isGridView) {

                        // -----------------------------------------------------
                        // GRID
                        // -----------------------------------------------------

                        LazyVerticalGrid(
                            columns = responsiveGridCells(
                                windowSize = windowSize,
                                compactColumns = 2,
                                mediumColumns = 3,
                                expandedColumns = 4
                            ),
                            contentPadding =
                                PaddingValues(
                                    start = horizontalPadding,
                                    end = horizontalPadding,
                                    top = 16.dp,
                                    bottom = 16.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            items(
                                folders,
                                key = { it.path }
                            ) { folder ->

                                val folderVids =
                                    allVideos.filter {
                                        it.parentFolderName.equals(
                                            folder.name,
                                            ignoreCase = true
                                        )
                                    }

                                VlcFolderGridCard(
                                    folder = folder,
                                    previewVideos =
                                        folderVids.take(4),
                                    onClick = {
                                        selectedFolder = folder
                                    }
                                )
                            }

                            item {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 12.dp
                                        ),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text =
                                            "Folder not found?",
                                        fontSize = 12.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )

                                    Text(
                                        text =
                                            "Manage your scan list",
                                        color =
                                            PlayitCyan,
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier =
                                            Modifier.clickable {
                                                onRefreshScan()
                                            }
                                    )
                                }
                            }
                        }

                    } else {

                        // -----------------------------------------------------
                        // LIST
                        // -----------------------------------------------------

                        LazyColumn(
                            contentPadding =
                                PaddingValues(
                                    start = horizontalPadding,
                                    end = horizontalPadding,
                                    top = 16.dp,
                                    bottom = 16.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            items(
                                folders,
                                key = { it.path }
                            ) { folder ->

                                FolderListRow(
                                    folder = folder,
                                    onClick = {
                                        selectedFolder = folder
                                    }
                                )
                            }
                        }
                    }
                }

            } else {

                // =================================================================
                // FOLDER DETAIL
                // =================================================================

                val active =
                    selectedFolder!!

                val folderVideos =
                    remember(
                        allVideos,
                        active
                    ) {
                        allVideos.filter {
                            it.parentFolderName.equals(
                                active.name,
                                ignoreCase = true
                            )
                        }
                    }

                val folderSongs =
                    remember(
                        allSongs,
                        active
                    ) {
                        allSongs.filter {

                            it.path?.contains(
                                active.name,
                                ignoreCase = true
                            ) == true ||

                                (
                                    active.name.equals(
                                        "Music",
                                        ignoreCase = true
                                    ) &&
                                        it.path?.contains(
                                            "Music"
                                        ) == true
                                    )
                        }
                    }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {

                    // ---------------------------------------------------------
                    // FOLDER HEADER
                    // ---------------------------------------------------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                selectedFolder = null
                            },
                            modifier =
                                Modifier.testTag(
                                    "folder_back_button"
                                )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.AutoMirrored
                                        .Filled
                                        .ArrowBack,
                                contentDescription =
                                    "Back",
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
                                text =
                                    active.name,
                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge,
                                fontWeight =
                                    FontWeight.Bold,
                                color = VlcOrange,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Text(
                                text =
                                    "${folderVideos.size} videos • ${folderSongs.size} songs",
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
                    }

                    // ---------------------------------------------------------
                    // FILTER TABS
                    // ---------------------------------------------------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        FolderFileFilter.values()
                            .forEach { filter ->

                                val selected =
                                    fileFilter == filter

                                Surface(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            fileFilter =
                                                filter
                                        },
                                    color =
                                        if (selected) {
                                            VlcOrange.copy(
                                                alpha = 0.12f
                                            )
                                        } else {
                                            MaterialTheme
                                                .colorScheme
                                                .surface
                                        },
                                    border =
                                        if (selected) {
                                            BorderStroke(
                                                1.dp,
                                                VlcOrange.copy(
                                                    alpha = 0.55f
                                                )
                                            )
                                        } else {
                                            null
                                        }
                                ) {

                                    Text(
                                        text =
                                            filter.label,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 7.dp
                                            ),
                                        color =
                                            if (selected) {
                                                VlcOrange
                                            } else {
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                            },
                                        fontSize = 12.sp,
                                        fontWeight =
                                            if (selected) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            }
                                    )
                                }
                            }
                    }

                    // ---------------------------------------------------------
                    // VIDEO GRID
                    // ---------------------------------------------------------

                    if (
                        fileFilter ==
                            FolderFileFilter.VIDEOS ||

                        (
                            fileFilter ==
                                FolderFileFilter.ALL &&
                                folderVideos.isNotEmpty() &&
                                folderSongs.isEmpty()
                            )
                    ) {

                        LazyVerticalGrid(
                            columns = responsiveGridCells(
                                windowSize = windowSize,
                                compactColumns = 2,
                                mediumColumns = 3,
                                expandedColumns = 4
                            ),
                            contentPadding =
                                PaddingValues(
                                    start = horizontalPadding,
                                    end = horizontalPadding,
                                    top = 12.dp,
                                    bottom = 12.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp),
                            modifier =
                                Modifier.fillMaxSize()
                        ) {

                            items(
                                folderVideos,
                                key = {
                                    "v_${it.id}"
                                }
                            ) { video ->

                                VlcFolderVideoCard(
                                    video = video,
                                    onClick = {
                                        onVideoClick(video)
                                    }
                                )
                            }
                        }

                    } else {

                        // -----------------------------------------------------
                        // MIXED LIST
                        // -----------------------------------------------------

                        LazyColumn(
                            contentPadding =
                                PaddingValues(
                                    start = horizontalPadding,
                                    end = horizontalPadding,
                                    top = 16.dp,
                                    bottom = 16.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier.fillMaxSize()
                        ) {

                            if (
                                fileFilter ==
                                    FolderFileFilter.ALL ||
                                fileFilter ==
                                    FolderFileFilter.VIDEOS
                            ) {

                                items(
                                    folderVideos,
                                    key = {
                                        "v_${it.id}"
                                    }
                                ) { video ->

                                    Surface(
                                        onClick = {
                                            onVideoClick(video)
                                        },
                                        shape =
                                            RoundedCornerShape(12.dp),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surface,
                                        tonalElevation = 2.dp,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .testTag(
                                                    "folder_video_${video.id}"
                                                )
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                            verticalAlignment =
                                                Alignment.CenterVertically,
                                            horizontalArrangement =
                                                Arrangement.spacedBy(
                                                    12.dp
                                                )
                                        ) {

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(
                                                            width = 80.dp,
                                                            height = 50.dp
                                                        )
                                                        .clip(
                                                            RoundedCornerShape(
                                                                8.dp
                                                            )
                                                        )
                                            ) {

                                                VideoThumbnail(
                                                    video = video,
                                                    modifier =
                                                        Modifier.fillMaxSize()
                                                )
                                            }

                                            Column(
                                                modifier =
                                                    Modifier.weight(1f)
                                            ) {

                                                Text(
                                                    text =
                                                        video.title,
                                                    fontWeight =
                                                        FontWeight.SemiBold,
                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .bodyMedium,
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
                                                        video.formattedDuration,
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

                                            Icon(
                                                imageVector =
                                                    Icons.Filled.PlayArrow,
                                                contentDescription =
                                                    "Play",
                                                tint =
                                                    VlcOrange
                                            )
                                        }
                                    }
                                }
                            }

                            if (
                                fileFilter ==
                                    FolderFileFilter.ALL ||
                                fileFilter ==
                                    FolderFileFilter.SONGS
                            ) {

                                items(
                                    folderSongs,
                                    key = {
                                        "s_${it.id}"
                                    }
                                ) { song ->

                                    Surface(
                                        onClick = {
                                            onSongClick(song)
                                        },
                                        shape =
                                            RoundedCornerShape(12.dp),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surface,
                                        tonalElevation = 2.dp,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .testTag(
                                                    "folder_song_${song.id}"
                                                )
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                            verticalAlignment =
                                                Alignment.CenterVertically,
                                            horizontalArrangement =
                                                Arrangement.spacedBy(
                                                    12.dp
                                                )
                                        ) {

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(40.dp)
                                                        .clip(
                                                            RoundedCornerShape(
                                                                8.dp
                                                            )
                                                        )
                                                        .background(
                                                            PlayitCyan.copy(
                                                                alpha = 0.15f
                                                            )
                                                        ),
                                                contentAlignment =
                                                    Alignment.Center
                                            ) {

                                                Icon(
                                                    imageVector =
                                                        Icons.Filled.MusicNote,
                                                    contentDescription =
                                                        null,
                                                    tint =
                                                        PlayitCyan
                                                )
                                            }

                                            Column(
                                                modifier =
                                                    Modifier.weight(1f)
                                            ) {

                                                Text(
                                                    text =
                                                        song.title,
                                                    fontWeight =
                                                        FontWeight.SemiBold,
                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .bodyMedium,
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
                                                        "${song.artist} • ${song.formattedDuration}",
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

                                            Icon(
                                                imageVector =
                                                    Icons.Filled.PlayArrow,
                                                contentDescription =
                                                    "Play",
                                                tint =
                                                    PlayitCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // FOLDER FAB
                // -------------------------------------------------------------

                FloatingActionButton(
                    onClick = {

                        if (folderVideos.isNotEmpty()) {
                            onVideoClick(
                                folderVideos.first()
                            )
                        } else if (
                            folderSongs.isNotEmpty()
                        ) {
                            onSongClick(
                                folderSongs.first()
                            )
                        }
                    },
                    containerColor = VlcOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.PlayArrow,
                        contentDescription =
                            "Play folder",
                        modifier =
                            Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// ==========================================================================
// FOLDER GRID CARD
// ==========================================================================

@Composable
private fun VlcFolderGridCard(
    folder: FolderItem,
    previewVideos: List<VideoItem>,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(14.dp)
            )
            .clickable(
                onClick = onClick
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                )
        ) {

            if (previewVideos.isNotEmpty()) {

                if (previewVideos.size >= 4) {

                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                    ) {

                        Row(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Box(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                VideoThumbnail(
                                    video =
                                        previewVideos[0],
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(1.dp)
                            )

                            Box(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                VideoThumbnail(
                                    video =
                                        previewVideos[1],
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(1.dp)
                        )

                        Row(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Box(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                VideoThumbnail(
                                    video =
                                        previewVideos[2],
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(1.dp)
                            )

                            Box(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                VideoThumbnail(
                                    video =
                                        previewVideos[3],
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                } else {

                    VideoThumbnail(
                        video =
                            previewVideos.first(),
                        modifier =
                            Modifier.fillMaxSize()
                    )
                }

            } else {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
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
                            Modifier.size(48.dp)
                    )
                }
            }
        }

        Text(
            text = folder.name,
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    top = 6.dp,
                    start = 2.dp
                )
        )

        Text(
            text =
                "${folder.totalItemsCount} videos",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            modifier =
                Modifier.padding(
                    start = 2.dp
                )
        )
    }
}

// ==========================================================================
// FOLDER VIDEO CARD
// ==========================================================================

@Composable
private fun VlcFolderVideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onClick
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(
                    RoundedCornerShape(10.dp)
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

            Surface(
                shape =
                    RoundedCornerShape(4.dp),
                color =
                    Color.Black.copy(
                        alpha = 0.75f
                    ),
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(6.dp)
            ) {

                Text(
                    text =
                        video.formattedDuration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.padding(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Text(
            text = video.title,
            fontWeight =
                FontWeight.SemiBold,
            fontSize = 13.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    top = 4.dp,
                    start = 2.dp
                )
        )
    }
}

// ==========================================================================
// FOLDER LIST ROW
// ==========================================================================

@Composable
fun FolderListRow(
    folder: FolderItem,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        shape =
            RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface,
        tonalElevation = 2.dp,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(
                    "folder_row_${folder.name}"
                )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            VlcOrange.copy(
                                alpha = 0.15f
                            )
                        ),
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
                        Modifier.size(28.dp)
                )
            }

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        folder.name,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                Text(
                    text =
                        "${folder.formattedTotalCount} • ${folder.formattedTotalSize}",
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

            Icon(
                imageVector =
                    Icons.Filled.ChevronRight,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                        .copy(alpha = 0.6f)
            )
        }
    }
}
