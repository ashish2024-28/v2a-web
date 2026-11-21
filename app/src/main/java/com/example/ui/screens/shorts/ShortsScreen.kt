@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens.shorts

import android.app.Activity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.DurationFilter
import com.example.data.model.SortOrder
import com.example.data.model.VideoItem
import com.example.player.VyloPlayerManager
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.V2SShortsSymbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.MediaLinkUtils

private val YouTubeRed = Color(0xFFFF0000)

enum class ShortsPlayMode(val label: String, val description: String) {
    REPEAT_ONE("Repeat Current", "Loop current short continuously"),
    CONTINUOUS("Auto Next", "Play continuously to next short"),
    SHUFFLE("Random Shuffle", "Pick and play a random short next"),
    PLAY_ONCE("Play Once", "Stop when finished and show replay button")
}

private enum class ShortsVideoMode {
    FIT,
    FILL,
    ZOOM,
    ORIGINAL
}

@Composable
fun ShortsScreen(
    feed: List<VideoItem>,
    activeFilter: DurationFilter,
    playerManager: VyloPlayerManager,
    onFilterSelected: (DurationFilter) -> Unit,
    onVideoChanged: (Int) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    onOpenInNormalPlayer: (VideoItem) -> Unit,
    onShowInfo: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit,
    currentSortOrder: SortOrder = SortOrder.NEWEST,
    onSortSelected: (SortOrder) -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    onPlayWebLink: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember {
        context.getSharedPreferences("v2s_shorts_prefs", android.content.Context.MODE_PRIVATE)
    }

    var shortsPlayMode by remember {
        val saved = preferences.getString("shorts_play_mode", ShortsPlayMode.CONTINUOUS.name) ?: ShortsPlayMode.CONTINUOUS.name
        mutableStateOf(runCatching { ShortsPlayMode.valueOf(saved) }.getOrDefault(ShortsPlayMode.CONTINUOUS))
    }

    // In-memory UI states (transient like/unlike, not persisted)
    val likedMap = remember { mutableStateMapOf<Long, Boolean>() }
    val unlikedMap = remember { mutableStateMapOf<Long, Boolean>() }
    val expandedCaptions = remember { mutableStateMapOf<Long, Boolean>() }

    var selectedShareVideo by remember { mutableStateOf<VideoItem?>(null) }
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var shortsVideoMode by remember { mutableStateOf(ShortsVideoMode.FIT) }

    // Keep screen awake while shorts video is playing (prevents screen lock/sleep)
    val activity = context as? Activity
    val playerState by playerManager.playerState.collectAsState()
    val isShortsPlaying = playerState.isPlaying
    androidx.compose.runtime.DisposableEffect(isShortsPlaying) {
        if (isShortsPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (feed.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.VideocamOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "No Shorts available",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Try selecting 'All Shorts' to play all videos in Shorts feed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = onAddLinkClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Shorts / Video Link",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { onFilterSelected(DurationFilter.ALL) },
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Show All Shorts",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { feed.size }
    )

    var hasCompletedCurrent by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            hasCompletedCurrent = false
            onVideoChanged(page)
        }
    }

    // Handle end-of-video logic for Shorts according to user's chosen playback mode
    LaunchedEffect(playerState.currentPositionMs, playerState.durationMs, playerState.isPlaying, shortsPlayMode) {
        val duration = playerState.durationMs
        val position = playerState.currentPositionMs
        if (duration > 1500L && position >= duration - 400L) {
            when (shortsPlayMode) {
                ShortsPlayMode.REPEAT_ONE -> {
                    // Loop current short continuously
                    playerManager.seekTo(0L)
                    playerManager.play()
                }
                ShortsPlayMode.CONTINUOUS -> {
                    // Auto play next short continuously
                    if (!pagerState.isScrollInProgress) {
                        val nextPage = if (pagerState.currentPage < feed.size - 1) {
                            pagerState.currentPage + 1
                        } else {
                            0
                        }
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
                ShortsPlayMode.SHUFFLE -> {
                    // Random shuffle to another short
                    if (!pagerState.isScrollInProgress) {
                        val candidates = (0 until feed.size).filter { it != pagerState.currentPage }
                        val randomPage = if (candidates.isNotEmpty()) candidates.random() else 0
                        pagerState.animateScrollToPage(randomPage)
                    }
                }
                ShortsPlayMode.PLAY_ONCE -> {
                    // Play once: pause at end and display professional replay overlay
                    hasCompletedCurrent = true
                    playerManager.pause()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!playerManager.playerState.value.isBackgroundPlayEnabled) {
                playerManager.pause()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val isNarrow = screenWidth < 360.dp
        val topBarHorizontalPad = if (isNarrow) 10.dp else 16.dp
        val headerLogoSize = if (isNarrow) 38.dp else 44.dp
        val headerActionSize = if (isNarrow) 38.dp else 42.dp
        // Vertical Pager (Smooth YouTube Shorts full-screen snapping)
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { index -> feed.getOrNull(index)?.id ?: index.toLong() }
        ) { page ->
            val video = feed[page]
            val isCurrentPage = pagerState.currentPage == page

            // In-memory dummy like / unlike state (not stored)
            val isLiked = likedMap[video.id] ?: false
            val isUnliked = unlikedMap[video.id] ?: false
            val isCaptionExpanded = expandedCaptions[video.id] ?: false

            YouTubeReelsItemView(
                video = video,
                isActive = isCurrentPage,
                isCompleted = isCurrentPage && hasCompletedCurrent,
                shortsPlayMode = shortsPlayMode,
                videoMode = shortsVideoMode,
                isLiked = isLiked,
                isUnliked = isUnliked,
                isCaptionExpanded = isCaptionExpanded,
                playerManager = playerManager,
                onReplay = {
                    hasCompletedCurrent = false
                    playerManager.seekTo(0L)
                    playerManager.play()
                },
                onPlayNext = {
                    hasCompletedCurrent = false
                    coroutineScope.launch {
                        val nextPage = if (pagerState.currentPage < feed.size - 1) {
                            pagerState.currentPage + 1
                        } else 0
                        pagerState.animateScrollToPage(nextPage)
                    }
                },
                onToggleLike = {
                    val nextLiked = !isLiked
                    likedMap[video.id] = nextLiked
                    if (nextLiked) {
                        unlikedMap[video.id] = false
                    }
                },
                onToggleUnlike = {
                    val nextUnliked = !isUnliked
                    unlikedMap[video.id] = nextUnliked
                    if (nextUnliked) {
                        likedMap[video.id] = false
                    }
                },
                onDoubleTapLike = {
                    likedMap[video.id] = true
                    unlikedMap[video.id] = false
                },
                onToggleCaption = {
                    expandedCaptions[video.id] = !isCaptionExpanded
                },
                onShareClicked = {
                    // Directly share this exact same video
                    onShareVideo(video)
                },
                onOpenNormal = { onOpenInNormalPlayer(video) },
                onPlayWebLink = { onPlayWebLink(video) },
                onShowInfo = { onShowInfo(video) },
                onDelete = { videoToDelete = video }
            )
        }

        // Top Navigation Bar (Clean YouTube Shorts Overlay with 3-dot filter menu)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(
                    start = topBarHorizontalPad,
                    end = topBarHorizontalPad,
                    top = 6.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Shorts Logo & Total Count Badge
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                V2SBrandLogo(
                    showTagline = !isNarrow
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9E00).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${feed.size}",
                        color = Color(0xFFFFD600),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 1.5.dp
                        )
                    )
                }
            }

            // Top-Right Controls: Add Link + Mute + 3-Dot Filter / Sort Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                IconButton(
                    onClick = onAddLinkClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = "Add Short Link",
                        tint = YouTubeRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        if (isMuted) playerManager.mute() else playerManager.unmute()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3-Dot Menu at top-right end for All Sort By & Multiple Filters
                Box {
                    IconButton(
                        onClick = { showTopMenu = true },
                        modifier = Modifier
                            .testTag("shorts_top_menu_button")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Sort and Filter",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        containerColor = Color(0xFF1C1C1E)
                    ) {
                        // Section Header: SHORTS PLAYBACK MODE
                        Text(
                            text = "PLAYBACK ON COMPLETION",
                            color = YouTubeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        ShortsPlayMode.values().forEach { mode ->
                            val isSelected = mode == shortsPlayMode
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                ShortsPlayMode.REPEAT_ONE -> Icons.Filled.RepeatOne
                                                ShortsPlayMode.CONTINUOUS -> Icons.Filled.Repeat
                                                ShortsPlayMode.SHUFFLE -> Icons.Filled.Shuffle
                                                ShortsPlayMode.PLAY_ONCE -> Icons.Filled.Replay
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) YouTubeRed else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = mode.label,
                                                color = if (isSelected) YouTubeRed else Color.White,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = mode.description,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = YouTubeRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    shortsPlayMode = mode
                                    preferences.edit().putString("shorts_play_mode", mode.name).apply()
                                    showTopMenu = false
                                }
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Section Header: DURATION FILTERS
                        Text(
                            text = "FILTER BY DURATION",
                            color = YouTubeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        DurationFilter.values().forEach { filter ->
                            val isSelected = filter == activeFilter
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Schedule,
                                            contentDescription = null,
                                            tint = if (isSelected) YouTubeRed else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = filter.label,
                                            color = if (isSelected) YouTubeRed else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = YouTubeRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    showTopMenu = false
                                    onFilterSelected(filter)
                                }
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Section Header: SORT BY
                        Text(
                            text = "SORT BY",
                            color = YouTubeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        val sortOptions = listOf(
                            SortOrder.DATE_NEW to "Date Added (Newest)",
                            SortOrder.DATE_OLD to "Date Added (Oldest)",
                            SortOrder.DURATION_SHORT to "Duration (Shortest first)",
                            SortOrder.DURATION_LONG to "Duration (Longest first)",
                            SortOrder.NAME_ASC to "Title (A to Z)",
                            SortOrder.NAME_DESC to "Title (Z to A)"
                        )

                        sortOptions.forEach { (sortOrder, label) ->
                            val isSelected = sortOrder == currentSortOrder
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (sortOrder) {
                                                SortOrder.DATE_NEW, SortOrder.DATE_OLD -> Icons.Filled.AccessTime
                                                SortOrder.DURATION_SHORT -> Icons.Filled.ArrowUpward
                                                SortOrder.DURATION_LONG -> Icons.Filled.ArrowDownward
                                                SortOrder.NAME_ASC, SortOrder.NAME_DESC -> Icons.AutoMirrored.Filled.Sort
                                                else -> Icons.AutoMirrored.Filled.Sort
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) YouTubeRed else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = label,
                                            color = if (isSelected) YouTubeRed else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = YouTubeRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    showTopMenu = false
                                    onSortSelected(sortOrder)
                                }
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "VIDEO SIZE",
                            color = YouTubeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        ShortsVideoMode.values().forEach { mode ->
                            val selected = shortsVideoMode == mode
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (mode) {
                                            ShortsVideoMode.FIT -> "Fit • Show Full Video"
                                            ShortsVideoMode.FILL -> "Fill • No Black Bars"
                                            ShortsVideoMode.ZOOM -> "Zoom • Crop to Screen"
                                            ShortsVideoMode.ORIGINAL -> "Original Ratio"
                                        },
                                        color = if (selected) YouTubeRed else Color.White,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                trailingIcon = {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = YouTubeRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    shortsVideoMode = mode
                                    showTopMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Share Bottom Sheet if invoked
    selectedShareVideo?.let { vid ->
        ShortsShareBottomSheet(
            video = vid,
            onDismiss = { selectedShareVideo = null },
            onNativeShare = { targetVid ->
                onShareVideo(targetVid)
            }
        )
    }

    // Permanent Delete Confirmation Alert
    videoToDelete?.let { vid ->
        DeleteConfirmationDialog(
            video = vid,
            onConfirm = {
                val target = vid
                videoToDelete = null
                onDeleteVideo(target)
            },
            onDismiss = {
                videoToDelete = null
            }
        )
    }
}

@Composable
private fun YouTubeReelsItemView(
    video: VideoItem,
    isActive: Boolean,
    isCompleted: Boolean = false,
    shortsPlayMode: ShortsPlayMode = ShortsPlayMode.CONTINUOUS,
    videoMode: ShortsVideoMode,
    isLiked: Boolean,
    isUnliked: Boolean,
    isCaptionExpanded: Boolean,
    playerManager: VyloPlayerManager,
    onReplay: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onToggleLike: () -> Unit,
    onToggleUnlike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onToggleCaption: () -> Unit,
    onShareClicked: () -> Unit,
    onOpenNormal: () -> Unit,
    onPlayWebLink: () -> Unit = onOpenNormal,
    onShowInfo: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val playerState by playerManager.playerState.collectAsState()
    val isWebLink = remember(video.uri) { MediaLinkUtils.isWebEmbedLink(video.uri.toString()) }
    val remoteThumbUrl = remember(video.uri) { MediaLinkUtils.getYouTubeThumbnailUrl(video.uri.toString()) }
    val webSourceName = remember(video.uri) { MediaLinkUtils.inferSourceName(video.uri.toString()) }

    var showPlayPauseAnim by remember { mutableStateOf(false) }
    var isPausedAnimState by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    var isHoldingSpeed by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Heart bounce animation spec
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale"
    )

    val unlikeScale by animateFloatAsState(
        targetValue = if (isUnliked) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "unlike_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id, isWebLink) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTapLike()
                        showHeartBurst = true
                        coroutineScope.launch {
                            delay(850)
                            showHeartBurst = false
                        }
                    },
                    onTap = {
                        if (isWebLink) {
                            onPlayWebLink()
                        } else if (isActive) {
                            val nowPlaying = playerState.isPlaying
                            playerManager.togglePlayPause()
                            isPausedAnimState = nowPlaying
                            showPlayPauseAnim = true
                            coroutineScope.launch {
                                delay(650)
                                showPlayPauseAnim = false
                            }
                        }
                    },
                    onLongPress = {
                        if (isActive && !isWebLink) {
                            // 2x speed preview during press & hold
                            isHoldingSpeed = true
                            playerManager.setTemporaryPlaybackSpeed(2.0f)
                        }
                    }
                )
            }
    ) {
        // Player Surface (Full screen Edge-to-Edge zoom)
        if (isWebLink) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1026), Color.Black, Color(0xFF1A0D10))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (remoteThumbUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(remoteThumbUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayWebLink,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YouTubeRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play $webSourceName Short",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch $webSourceName Short",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (isActive) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = if (isActive) playerManager.getOrCreatePlayer() else null
                        useController = false
                        keepScreenOn = isActive && playerState.isPlaying
                        resizeMode = when (videoMode) {
                            ShortsVideoMode.FIT, ShortsVideoMode.ORIGINAL ->
                                AspectRatioFrameLayout.RESIZE_MODE_FIT
                            ShortsVideoMode.FILL ->
                                AspectRatioFrameLayout.RESIZE_MODE_FILL
                            ShortsVideoMode.ZOOM ->
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = if (isActive) playerManager.getOrCreatePlayer() else null
                    view.keepScreenOn = isActive && playerState.isPlaying
                    view.resizeMode = when (videoMode) {
                        ShortsVideoMode.FIT, ShortsVideoMode.ORIGINAL ->
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        ShortsVideoMode.FILL ->
                            AspectRatioFrameLayout.RESIZE_MODE_FILL
                        ShortsVideoMode.ZOOM ->
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                onReset = { view ->
                    view.player = null
                },
                onRelease = { view ->
                    view.player = null
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp, bottom = 2.dp)
            )
        }

        // Holding 2X Speed Indicator (Top Center)
        AnimatedVisibility(
            visible = isHoldingSpeed,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    isHoldingSpeed = false
                    playerManager.restorePersistentPlaybackSpeed()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "2X Speed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Big Double-Tap Floating Heart Burst Animation (Center)
        AnimatedVisibility(
            visible = showHeartBurst,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        // Single-Tap Play/Pause Indicator
        AnimatedVisibility(
            visible = showPlayPauseAnim,
            enter = scaleIn(tween(120, easing = FastOutSlowInEasing)) + fadeIn(tween(80)),
            exit = scaleOut(tween(220)) + fadeOut(tween(220)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPausedAnimState) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        // Buffering Indicator
        if (isActive && !isWebLink && playerState.isBuffering && playerState.error == null) {
            CircularProgressIndicator(
                color = YouTubeRed,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center)
            )
        }

        // Error Overlay
        if (isActive && !isWebLink && playerState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = YouTubeRed,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Unable to load short",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Button(
                        onClick = { playerManager.retryCurrent() },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Gradient overlay for bottom controls & captions
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Right Action Rail (Shorts Style - Like, Unlike, Share, More)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIKE BUTTON (Write "Like" below, toggle like / unlike)
            ShortsActionButton(
                icon = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                label = "Like",
                tint = if (isLiked) YouTubeRed else Color.White,
                scale = likeScale,
                testTag = "shorts_like_button",
                onClick = onToggleLike
            )

            // 2. UNLIKE BUTTON (Write "Unlike" below, toggle unlike)
            ShortsActionButton(
                icon = if (isUnliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                label = "Unlike",
                tint = if (isUnliked) YouTubeRed else Color.White,
                scale = unlikeScale,
                testTag = "shorts_unlike_button",
                onClick = onToggleUnlike
            )

            // 3. SHARE BUTTON (Write "Share" below, shares same video)
            ShortsActionButton(
                icon = Icons.Filled.Share,
                label = "Share",
                tint = Color.White,
                testTag = "shorts_share_button",
                onClick = onShareClicked
            )

            // 4. MORE (3 dots dropdown: Details, Open in Player, Delete)
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.size(32.dp).testTag("shorts_more_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    containerColor = Color(0xFF1E1E20)
                ) {
                    DropdownMenuItem(
                        text = { Text("Video Details", color = Color.White) },
                        onClick = {
                            showMoreMenu = false
                            onShowInfo()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open in Player", color = Color.White) },
                        onClick = {
                            showMoreMenu = false
                            onOpenNormal()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Video", color = YouTubeRed) },
                        onClick = {
                            showMoreMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        // Bottom-Left Channel & Video Metadata Info (Clean YouTube layout)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Channel Avatar & Handle Row: "@Shorts in V2C"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF0000), Color(0xFFFF5252))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "@Shorts in V2C",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Video Title directly below channel name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleCaption)
            ) {
                Text(
                    text = if (isCaptionExpanded) {
                        "${video.title}\n\n#Shorts #V2S #Trending\nQuality: ${video.resolutionLabel} • Duration: ${video.formattedDuration}"
                    } else {
                        video.title
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    maxLines = if (isCaptionExpanded) 5 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (video.title.length > 35) {
                    Text(
                        text = if (isCaptionExpanded) "Show less" else "...more",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Professional Completion Overlay (When video completes in Play Once mode)
        if (isActive && isCompleted && shortsPlayMode == ShortsPlayMode.PLAY_ONCE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Short Completed",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = video.title,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Replay Button
                        Button(
                            onClick = onReplay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YouTubeRed,
                                contentColor = Color.White
                            ),
                            shape = CircleShape,
                            modifier = Modifier.testTag("shorts_replay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Replay", fontWeight = FontWeight.SemiBold)
                        }

                        // Next Short Button
                        OutlinedButton(
                            onClick = onPlayNext,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            shape = CircleShape,
                            modifier = Modifier.testTag("shorts_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Short",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Next", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Live Red Playback Progress Bar (Scrubber Line at very bottom edge)
        if (isActive && playerState.durationMs > 0) {
            val progress = (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(YouTubeRed)
                )
            }
        }
    }
}

@Composable
private fun ShortsActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    scale: Float = 1.0f,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
fun DurationFilterRow(
    activeFilter: DurationFilter,
    onFilterSelected: (DurationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationFilter.values().forEach { filter ->
                val selected = filter == activeFilter
                val chipColor = if (selected) Color.White.copy(alpha = 0.25f) else Color.Transparent
                val textColor = if (selected) Color.White else Color.White.copy(alpha = 0.65f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipColor)
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("filter_chip_${filter.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
