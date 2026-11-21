package com.example.ui.components

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMicros
import coil.request.videoFrameOption
import com.example.data.model.VideoItem
import com.example.ui.theme.VlcOrange
import com.example.util.MediaLinkUtils
import java.io.File

private val GRADIENT_PALETTES = listOf(
    listOf(Color(0xFF2C1B10), Color(0xFF131118)),
    listOf(Color(0xFF16232D), Color(0xFF0F141C)),
    listOf(Color(0xFF2B1626), Color(0xFF110E17)),
    listOf(Color(0xFF1C281F), Color(0xFF101612)),
    listOf(Color(0xFF282516), Color(0xFF151410)),
    listOf(Color(0xFF1F1E33), Color(0xFF11111E))
)

@Composable
fun VideoThumbnail(
    video: VideoItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showBadge: Boolean = true
) {
    val context = LocalContext.current

    val isLocalVideo = remember(video.id, video.path, video.uri, video.isDemoSample) {
        !video.isDemoSample && (
            (!video.path.isNullOrBlank() && File(video.path).exists()) ||
            video.uri.scheme == "content" ||
            (video.uri.scheme == "file" && File(video.uri.path ?: "").exists())
        )
    }

    val remoteThumbnailUrl = remember(video.uri) {
        MediaLinkUtils.getYouTubeThumbnailUrl(video.uri.toString())
    }

    val paletteIndex = remember(video.id) {
        (kotlin.math.abs(video.id) % GRADIENT_PALETTES.size).toInt()
    }
    val gradientColors = GRADIENT_PALETTES[paletteIndex]

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = gradientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLocalVideo) {
            val targetData: Any = if (!video.path.isNullOrBlank() && File(video.path).exists()) {
                video.path
            } else {
                video.uri
            }
            val targetMicros = if (video.durationMs > 2000L) 1_000_000L else 0L

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(targetData)
                    .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                    .videoFrameMicros(targetMicros)
                    .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    .allowHardware(false)
                    .memoryCacheKey("video_thumb_${video.id}_${video.durationMs}")
                    .diskCacheKey("video_thumb_${video.id}_${video.durationMs}")
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else if (remoteThumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(remoteThumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Gradient poster with icon for added stream links
            Icon(
                imageVector = Icons.Filled.VideoLibrary,
                contentDescription = null,
                tint = VlcOrange.copy(alpha = 0.35f),
                modifier = Modifier.size(36.dp)
            )
        }

        val badgeLabel = remember(video.id, video.uri, video.resolutionLabel) {
            if (MediaLinkUtils.isUserLinkId(video.id)) {
                MediaLinkUtils.inferSourceName(video.uri.toString())
            } else {
                video.resolutionLabel
            }
        }

        // Quality/Source Badge at top-end
        if (showBadge && badgeLabel.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = badgeLabel,
                    color = Color(0xFFFFB52E),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Play icon overlay in center
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.50f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
