package com.example.data.model

import android.net.Uri
import java.util.Locale

enum class PlaylistMediaType {
    VIDEO,
    AUDIO
}

data class PlaylistMediaItem(
    val id: String,
    val mediaId: Long,
    val type: PlaylistMediaType,
    val title: String,
    val subtitle: String,
    val durationMs: Long,
    val formattedDuration: String,
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val isFavorite: Boolean = false,
    val videoItem: VideoItem? = null,
    val songItem: SongItem? = null,
    val resolutionLabel: String = "",
    val formattedSize: String = ""
)

data class PlaylistData(
    val id: String,
    val title: String,
    val isSystem: Boolean = false,
    val isFavorites: Boolean = false,
    val category: String = "ALL", // "ALL", "SONGS", "VIDEOS"
    val items: List<PlaylistMediaItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isVideoPlaylist: Boolean
        get() = !category.equals("SONGS", ignoreCase = true) &&
                !id.startsWith("pl_songs_") &&
                (category.equals("VIDEOS", ignoreCase = true) ||
                    id.startsWith("pl_videos_") ||
                    id == "all_videos")

    val isSongPlaylist: Boolean
        get() = !category.equals("VIDEOS", ignoreCase = true) &&
                !id.startsWith("pl_videos_") &&
                (category.equals("SONGS", ignoreCase = true) ||
                    id.startsWith("pl_songs_") ||
                    id == "all_songs")

    val totalTracks: Int
        get() = items.size

    val totalDurationMs: Long
        get() = items.sumOf { it.durationMs }

    val formattedDuration: String
        get() {
            val totalSec = totalDurationMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            return when {
                hours > 0 -> String.format(Locale.getDefault(), "%dh %dmin %ds", hours, minutes, seconds)
                minutes > 0 -> String.format(Locale.getDefault(), "%dmin %ds", minutes, seconds)
                else -> String.format(Locale.getDefault(), "%ds", seconds)
            }
        }

    val formattedShortDuration: String
        get() {
            val totalSec = totalDurationMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val sectionHeader: String
        get() {
            if (isFavorites || isSystem) return "#"
            val first = title.trim().firstOrNull() ?: return "#"
            return if (first.isDigit() || !first.isLetter()) "#" else first.uppercaseChar().toString()
        }
}
