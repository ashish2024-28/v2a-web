package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import android.media.MediaMetadataRetriever
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMicros
import coil.request.videoFrameOption
import com.example.player.VyloPlayerManager
import java.io.File
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcOrange
import com.example.ui.theme.VlcOrangeLight
import com.example.ui.theme.VlcOrangeDark
import kotlin.math.roundToInt

enum class MiniPlayerMode {
    DOCKED,
    FLOATING_PIP
}

@OptIn(UnstableApi::class)
@Composable
fun PersistentMiniPlayer(
    playerManager: VyloPlayerManager,
    isFloatingPip: Boolean = false,
    onFloatingPipChange: ((Boolean) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by playerManager.playerState.collectAsState()
    val video = playerState.currentVideo
    val song = playerState.currentSong
    val isVideoAudio = playerState.isVideoAudio

    // Only visible if there is active media
    val hasActiveMedia = video != null || song != null

    var internalMode by remember { mutableStateOf(MiniPlayerMode.DOCKED) }
    val effectiveMode = if (onFloatingPipChange != null) {
        if (isFloatingPip) MiniPlayerMode.FLOATING_PIP else MiniPlayerMode.DOCKED
    } else {
        internalMode
    }

    // Position for draggable Floating PiP mode
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = hasActiveMedia,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (effectiveMode == MiniPlayerMode.FLOATING_PIP && video != null) {
            // Floating Picture-in-Picture window overlay
            FloatingPipWindow(
                videoTitle = video.title,
                playerManager = playerManager,
                isPlaying = playerState.isPlaying,
                progress = if (playerState.durationMs > 0) {
                    (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f,
                currentPositionMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                offsetX = offsetX,
                offsetY = offsetY,
                onDrag = { dx, dy ->
                    offsetX += dx
                    offsetY += dy
                },
                onTogglePlay = { playerManager.togglePlayPause() },
                onSeekRelative = { offsetMs ->
                    val cur = playerManager.playerState.value.currentPositionMs
                    val dur = playerManager.playerState.value.durationMs
                    playerManager.seekTo((cur + offsetMs).coerceIn(0L, if (dur > 0) dur else Long.MAX_VALUE))
                },
                onExpand = onExpand,
                onDock = {
                    if (onFloatingPipChange != null) {
                        onFloatingPipChange(false)
                    } else {
                        internalMode = MiniPlayerMode.DOCKED
                    }
                },
                onClose = onClose
            )
        } else {
            // Docked Bottom Mini-Player Bar
            DockedMiniPlayerBar(
                title = video?.title ?: song?.title ?: "Media Playing",
                subtitle = if (video != null) {
                    "${video.formattedDuration} • ${video.parentFolderName}"
                } else if (isVideoAudio) {
                    "Video Audio • ${song?.artist ?: "Videos"}"
                } else {
                    "${song?.artist ?: "Unknown Artist"} • ${song?.formattedDuration ?: ""}"
                },
                isVideo = video != null,
                isVideoAudio = isVideoAudio,
                artworkUri = video?.uri ?: song?.albumArtUri ?: playerState.notificationArtworkUri,
                artworkPath = if (video != null && !video.path.isNullOrBlank() && File(video.path).exists()) video.path else null,
                playerManager = playerManager,
                isPlaying = playerState.isPlaying,
                progress = if (playerState.durationMs > 0) {
                    (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f,
                onPrevious = onPrevious,
                onTogglePlay = { playerManager.togglePlayPause() },
                onNext = onNext,
                onTogglePip = {
                    if (video != null) {
                        if (onFloatingPipChange != null) {
                            onFloatingPipChange(true)
                        } else {
                            internalMode = MiniPlayerMode.FLOATING_PIP
                        }
                    }
                },
                onExpand = onExpand,
                onClose = onClose
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun DockedMiniPlayerBar(
    title: String,
    subtitle: String,
    isVideo: Boolean,
    isVideoAudio: Boolean = false,
    artworkUri: android.net.Uri? = null,
    artworkPath: String? = null,
    playerManager: VyloPlayerManager,
    isPlaying: Boolean,
    progress: Float,
    onPrevious: (() -> Unit)? = null,
    onTogglePlay: () -> Unit,
    onNext: (() -> Unit)? = null,
    onTogglePip: () -> Unit,
    onExpand: () -> Unit,
    onClose: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onExpand,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .height(82.dp)
            .testTag("persistent_mini_player")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Media Preview Container
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black,
                    modifier = Modifier
                        .width(68.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    if (isVideo || isVideoAudio) {
                        val videoTarget: Any? = artworkPath ?: artworkUri
                        if (videoTarget != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(videoTarget)
                                    .decoderFactory { result, options, _ ->
                                        VideoFrameDecoder(result.source, options)
                                    }
                                    .videoFrameMicros(1_000_000L)
                                    .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                    .memoryCacheKey("mini_thumb_${artworkPath ?: artworkUri}")
                                    .diskCacheKey("mini_thumb_${artworkPath ?: artworkUri}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1E222A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else if (artworkUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artworkUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Middle: Title & Subtitle Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isVideoAudio) PlayitCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right: Action Buttons (Previous, Play/Pause, Next, Close)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-2).dp)
                ) {
                    if (isVideo) {
                        // Floating PiP switch button
                        IconButton(
                            onClick = onTogglePip,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("pip_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureInPictureAlt,
                                contentDescription = "Floating Picture-in-Picture",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Previous Button
                    if (onPrevious != null) {
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_previous")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = VlcOrange,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Button
                    if (onNext != null) {
                        IconButton(
                            onClick = onNext,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_next")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Three-dot menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mini_player_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open player") },
                                onClick = {
                                    showMenu = false
                                    onExpand()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.OpenInFull, contentDescription = null)
                                }
                            )
                            if (onPrevious != null) {
                                DropdownMenuItem(
                                    text = { Text("Previous") },
                                    onClick = {
                                        showMenu = false
                                        onPrevious()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.SkipPrevious, contentDescription = null)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(if (isPlaying) "Pause" else "Play") },
                                onClick = {
                                    showMenu = false
                                    onTogglePlay()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            )
                            if (onNext != null) {
                                DropdownMenuItem(
                                    text = { Text("Next") },
                                    onClick = {
                                        showMenu = false
                                        onNext()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.SkipNext, contentDescription = null)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Close player") },
                                onClick = {
                                    showMenu = false
                                    onClose()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Close, contentDescription = null)
                                }
                            )
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("mini_player_close")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Player",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Progress Bar Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .background(VlcOrange)
                )
            }
        }
    }
}

private fun formatPipTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun FloatingPipWindow(
    videoTitle: String,
    playerManager: VyloPlayerManager,
    isPlaying: Boolean,
    progress: Float,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    offsetX: Float,
    offsetY: Float,
    onDrag: (Float, Float) -> Unit,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onSeekRelative: (Long) -> Unit = {},
    onExpand: () -> Unit,
    onDock: () -> Unit,
    onClose: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    
    // Dynamic Size Levels: 
    // 0 = XS (180dp), 1 = S (230dp), 2 = M (290dp), 3 = L (350dp), 4 = FULL (Full Screen)
    var sizeLevel by remember { mutableStateOf(1) }
    val isFullScreen = sizeLevel == 4

    val sizeLabel = when (sizeLevel) {
        0 -> "XS"
        1 -> "S"
        2 -> "M"
        3 -> "L"
        else -> "FULL"
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val targetWidth = when (sizeLevel) {
            0 -> 180.dp
            1 -> 230.dp
            2 -> 290.dp
            3 -> minOf(350.dp, screenWidth - 16.dp)
            else -> screenWidth
        }

        val targetHeight = when (sizeLevel) {
            0 -> 106.dp
            1 -> 135.dp
            2 -> 170.dp
            3 -> minOf(205.dp, screenHeight - 90.dp)
            else -> screenHeight
        }

        Surface(
            shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp),
            color = Color.Black,
            shadowElevation = if (isFullScreen) 0.dp else 18.dp,
            border = if (isFullScreen) null else androidx.compose.foundation.BorderStroke(1.5.dp, VlcOrange.copy(alpha = 0.75f)),
            modifier = if (isFullScreen) {
                Modifier
                    .fillMaxSize()
                    .testTag("floating_pip_window")
            } else {
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(end = 12.dp, bottom = 80.dp)
                    .width(targetWidth)
                    .height(targetHeight)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .testTag("floating_pip_window")
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showControls = !showControls }
            ) {
                // Live ExoPlayer Video Surface
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = playerManager.getOrCreatePlayer()
                            useController = false
                            resizeMode = if (isFullScreen) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        view.player = playerManager.getOrCreatePlayer()
                        view.resizeMode = if (isFullScreen) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    },
                    onReset = { view ->
                        view.player = null
                    },
                    onRelease = { view ->
                        view.player = null
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Controls (VLC / PLAYit Style Pop-up)
                if (showControls) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = if (isFullScreen) 0.45f else 0.52f))
                    ) {
                        // Top Header Bar: Title, Dynamic Size (+ / -), Fullscreen, Dock, Close
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.90f), Color.Transparent)
                                    )
                                )
                                .padding(
                                    horizontal = if (isFullScreen) 12.dp else 6.dp,
                                    vertical = if (isFullScreen) 8.dp else 3.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = videoTitle,
                                color = Color.White,
                                fontSize = if (isFullScreen) 14.sp else if (sizeLevel == 0) 9.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(if (isFullScreen) 4.dp else 1.dp)
                            ) {
                                // Dynamic Size Decrease (-)
                                IconButton(
                                    onClick = {
                                        if (sizeLevel > 0) sizeLevel--
                                    },
                                    enabled = sizeLevel > 0,
                                    modifier = Modifier.size(if (isFullScreen) 30.dp else 22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Decrease Floating Size",
                                        tint = if (sizeLevel > 0) Color.White else Color.Gray,
                                        modifier = Modifier.size(if (isFullScreen) 18.dp else 13.dp)
                                    )
                                }

                                // Size Badge Indicator (Tap to cycle XS -> S -> M -> L -> FULL)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isFullScreen) VlcOrange else VlcOrange.copy(alpha = 0.85f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(
                                            horizontal = if (isFullScreen) 6.dp else 4.dp,
                                            vertical = if (isFullScreen) 2.dp else 1.dp
                                        )
                                        .clickable {
                                            sizeLevel = (sizeLevel + 1) % 5
                                        }
                                ) {
                                    Text(
                                        text = sizeLabel,
                                        color = Color.Black,
                                        fontSize = if (isFullScreen) 10.sp else 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // Dynamic Size Increase (+)
                                IconButton(
                                    onClick = {
                                        if (sizeLevel < 4) sizeLevel++
                                    },
                                    enabled = sizeLevel < 4,
                                    modifier = Modifier.size(if (isFullScreen) 30.dp else 22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Increase Floating Size",
                                        tint = if (sizeLevel < 4) Color.White else Color.Gray,
                                        modifier = Modifier.size(if (isFullScreen) 18.dp else 13.dp)
                                    )
                                }

                                // Dock to Bottom Mini Player
                                IconButton(
                                    onClick = onDock,
                                    modifier = Modifier.size(if (isFullScreen) 30.dp else 22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ViewStream,
                                        contentDescription = "Dock to bottom bar",
                                        tint = Color.White,
                                        modifier = Modifier.size(if (isFullScreen) 18.dp else 13.dp)
                                    )
                                }

                                // Fullscreen Toggle / Expand
                                IconButton(
                                    onClick = {
                                        if (!isFullScreen) {
                                            sizeLevel = 4
                                        } else {
                                            onExpand()
                                        }
                                    },
                                    modifier = Modifier.size(if (isFullScreen) 30.dp else 22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInFull,
                                        contentDescription = "Expand Fullscreen",
                                        tint = VlcOrange,
                                        modifier = Modifier.size(if (isFullScreen) 18.dp else 13.dp)
                                    )
                                }

                                // Cross / Close
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier.size(if (isFullScreen) 30.dp else 22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(if (isFullScreen) 18.dp else 14.dp)
                                    )
                                }
                            }
                        }

                        // Center Controls: Previous | Seek -10 | Play/Pause | Seek +10 | Next
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Video
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier.size(if (isFullScreen) 44.dp else if (sizeLevel == 0) 24.dp else 28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Previous Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isFullScreen) 28.dp else if (sizeLevel == 0) 16.dp else 20.dp)
                                )
                            }

                            // Seek -10s
                            if (sizeLevel >= 1) {
                                IconButton(
                                    onClick = { onSeekRelative(-10000L) },
                                    modifier = Modifier.size(if (isFullScreen) 40.dp else 26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Replay10,
                                        contentDescription = "Seek -10s",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(if (isFullScreen) 24.dp else 18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(if (isFullScreen) 12.dp else 4.dp))

                            // Play / Pause Button
                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier
                                    .size(if (isFullScreen) 56.dp else if (sizeLevel == 0) 30.dp else 36.dp)
                                    .background(VlcOrange.copy(alpha = 0.9f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(if (isFullScreen) 32.dp else if (sizeLevel == 0) 18.dp else 22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(if (isFullScreen) 12.dp else 4.dp))

                            // Seek +10s
                            if (sizeLevel >= 1) {
                                IconButton(
                                    onClick = { onSeekRelative(10000L) },
                                    modifier = Modifier.size(if (isFullScreen) 40.dp else 26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Forward10,
                                        contentDescription = "Seek +10s",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(if (isFullScreen) 24.dp else 18.dp)
                                    )
                                }
                            }

                            // Next Video
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(if (isFullScreen) 44.dp else if (sizeLevel == 0) 24.dp else 28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isFullScreen) 28.dp else if (sizeLevel == 0) 16.dp else 20.dp)
                                )
                            }
                        }

                        // Bottom: Time indicator + Progress Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f))
                                    )
                                )
                                .padding(bottom = if (isFullScreen) 12.dp else 0.dp)
                        ) {
                            if (durationMs > 0L) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = if (isFullScreen) 12.dp else 6.dp,
                                            vertical = if (isFullScreen) 4.dp else 2.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatPipTime(currentPositionMs),
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = if (isFullScreen) 11.sp else 8.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formatPipTime(durationMs),
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = if (isFullScreen) 11.sp else 8.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isFullScreen) 5.dp else 3.dp)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                        .background(VlcOrange)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
