package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen

/**
 * Bottom sheet matching Screenshot 4 (Options displayed when clicking 3-dots on a video).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoOptionsBottomSheet(
    video: VideoItem,
    onDismiss: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onPlayInShorts: ((VideoItem) -> Unit)? = null,
    onPlayAsPip: ((VideoItem) -> Unit)? = null,
    onPlayFromStart: (VideoItem) -> Unit,
    onPlayAll: () -> Unit,
    onPlayAsAudio: (VideoItem) -> Unit,
    onAddToQueue: (VideoItem) -> Unit,
    onInsertNext: (VideoItem) -> Unit,
    onDownloadSubtitles: (VideoItem) -> Unit,
    onInformation: (VideoItem) -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit,
    onSetAsRingtone: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onDelete: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    onCreateShortcut: (VideoItem) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E20),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
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
                .padding(bottom = 30.dp)
        ) {
            // 1. Header: Video Thumbnail + Title (Matching Screenshot 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C2C2E))
                ) {
                    VideoThumbnail(
                        video = video,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Options List in the Exact Order of Screenshot 4:
            // - Play
            VideoOptionRow(
                icon = Icons.Filled.PlayArrow,
                title = "Play",
                onClick = {
                    onDismiss()
                    onPlay(video)
                }
            )

            // - Play in Shorts
            if (onPlayInShorts != null) {
                VideoOptionRow(
                    icon = Icons.Filled.Bolt,
                    title = "Play in Shorts Feed",
                    onClick = {
                        onDismiss()
                        onPlayInShorts(video)
                    }
                )
            }

            // - Play from start
            VideoOptionRow(
                icon = Icons.Filled.Replay,
                title = "Play from start",
                onClick = {
                    onDismiss()
                    onPlayFromStart(video)
                }
            )

            // - Play in Pop-up (Floating Video PiP)
            if (onPlayAsPip != null) {
                VideoOptionRow(
                    icon = Icons.Filled.PictureInPictureAlt,
                    title = "Play in Pop-up (Floating Video)",
                    onClick = {
                        onDismiss()
                        onPlayAsPip(video)
                    }
                )
            }

            // - Play all
            VideoOptionRow(
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                title = "Play all",
                onClick = {
                    onDismiss()
                    onPlayAll()
                }
            )

            // - Play as audio
            VideoOptionRow(
                icon = Icons.Filled.GraphicEq,
                title = "Play as audio",
                onClick = {
                    onDismiss()
                    onPlayAsAudio(video)
                }
            )

            // - Add to play queue
            VideoOptionRow(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                title = "Add to play queue",
                onClick = {
                    onDismiss()
                    onAddToQueue(video)
                }
            )

            // - Insert next
            VideoOptionRow(
                icon = Icons.Filled.SkipNext,
                title = "Insert next",
                onClick = {
                    onDismiss()
                    onInsertNext(video)
                }
            )

            // - Download subtitles
            VideoOptionRow(
                icon = Icons.Filled.Download,
                title = "Download subtitles",
                onClick = {
                    onDismiss()
                    onDownloadSubtitles(video)
                }
            )

            // - Information
            VideoOptionRow(
                icon = Icons.Outlined.Info,
                title = "Information",
                onClick = {
                    onDismiss()
                    onInformation(video)
                }
            )

            // - Add to playlist
            VideoOptionRow(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                title = "Add to playlist",
                onClick = {
                    onDismiss()
                    onAddToPlaylist(video)
                }
            )

            // - Set as ringtone
            VideoOptionRow(
                icon = Icons.Filled.Phone,
                title = "Set as ringtone",
                onClick = {
                    onDismiss()
                    onSetAsRingtone(video)
                }
            )

            // - Add to favorites (or Remove from favorites)
            VideoOptionRow(
                icon = if (video.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                title = if (video.isFavorite) "Remove from favorites" else "Add to favorites",
                onClick = {
                    onDismiss()
                    onToggleFavorite(video)
                }
            )

            // - Delete
            VideoOptionRow(
                icon = Icons.Outlined.Delete,
                title = "Delete",
                onClick = {
                    onDismiss()
                    onDelete(video)
                }
            )

            // - Share
            VideoOptionRow(
                icon = Icons.Filled.Share,
                title = "Share",
                onClick = {
                    onDismiss()
                    onShare(video)
                }
            )

            // - Create a launcher shortcut
            VideoOptionRow(
                icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
                title = "Create a launcher shortcut",
                onClick = {
                    onDismiss()
                    onCreateShortcut(video)
                }
            )
        }
    }
}

@Composable
private fun VideoOptionRow(
    icon: ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            color = tint,
            fontWeight = FontWeight.Normal
        )
    }
}
