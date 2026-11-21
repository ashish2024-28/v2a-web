package com.example.data.model

import android.net.Uri
import java.util.Locale

data class SongItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAddedSec: Long = 0L,
    val path: String? = null,
    val albumArtUri: Uri? = null,
    val mimeType: String = "audio/*",
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val isDemoSample: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSec = durationMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val displaySubtitle: String
        get() {
            val artistPart = if (artist.isNotBlank() && artist != "<unknown>") artist else "Unknown Artist"
            val albumPart = if (album.isNotBlank() && album != "<unknown>") album else ""
            return if (albumPart.isNotBlank()) "$artistPart • $albumPart" else artistPart
        }
}
