package com.example.data.model

import android.net.Uri
import java.util.Locale

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "video/*",
    val path: String? = null,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val playCount: Int = 0,
    val isDemoSample: Boolean = false
) {
    val durationSeconds: Long
        get() = durationMs / 1000

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

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "--"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                else -> String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }

    val progressFraction: Float
        get() {
            if (durationMs <= 0 || lastPositionMs <= 0) return 0f
            return (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val isPartiallyWatched: Boolean
        get() = lastPositionMs > 3000 && (lastPositionMs < durationMs - 5000)

    val resolutionLabel: String
        get() {
            val maxDim = maxOf(width, height)
            return when {
                maxDim >= 3840 || width >= 3840 || height >= 2160 -> "4K UHD"
                maxDim >= 2560 || width >= 2560 || height >= 1440 -> "2K QHD"
                maxDim >= 1920 || width >= 1920 || height >= 1080 -> "1080p FHD"
                maxDim >= 1280 || width >= 1280 || height >= 720 -> "720p HD"
                maxDim > 0 -> "SD"
                else -> if (isDemoSample) "4K" else "HD"
            }
        }

    val is4kUltraHd: Boolean
        get() = width >= 3840 || height >= 2160 || resolutionLabel.contains("4K", ignoreCase = true) || isDemoSample

    val formatLabel: String
        get() {
            val ext = displayName.substringAfterLast('.', "").uppercase()
            if (ext.isNotBlank() && ext.length in 2..5) return ext
            return when {
                mimeType.contains("matroska", ignoreCase = true) -> "MKV"
                mimeType.contains("mp4", ignoreCase = true) -> "MP4"
                mimeType.contains("webm", ignoreCase = true) -> "WEBM"
                mimeType.contains("x-flv", ignoreCase = true) -> "FLV"
                mimeType.contains("3gpp", ignoreCase = true) -> "3GP"
                mimeType.contains("avi", ignoreCase = true) -> "AVI"
                else -> "MP4"
            }
        }

    val parentFolderName: String
        get() {
            if (path != null) {
                val segments = path.split("/").filter { it.isNotBlank() }
                if (segments.size >= 2) {
                    return segments[segments.size - 2]
                }
            }
            return if (isDemoSample) "Featured" else "Videos"
        }
}

enum class DurationFilter(val label: String, val maxDurationMs: Long?) {
    ALL("ALL", null),
    LESS_THAN_1_MIN("<1 MIN", 60_000L),
    LESS_THAN_5_MIN("<5 MIN", 300_000L),
    LESS_THAN_10_MIN("<10 MIN", 600_000L);

    fun matches(durationMs: Long): Boolean {
        return when (this) {
            ALL -> true
            LESS_THAN_1_MIN -> durationMs in 1..60_000L
            LESS_THAN_5_MIN -> durationMs in 1..300_000L
            LESS_THAN_10_MIN -> durationMs in 1..600_000L
        }
    }
}

enum class SortOrder(val label: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    FILENAME_ASC("File name (A to Z)"),
    FILENAME_DESC("File name (Z to A)"),
    DURATION_SHORT("Shortest first"),
    DURATION_LONG("Longest first"),
    DATE_OLD("Oldest first"),
    DATE_NEW("Newest first");

    companion object {
        val NEWEST = DATE_NEW
        val OLDEST = DATE_OLD
    }
}

enum class ShortsCompletionMode(val label: String, val badge: String, val description: String) {
    REPEAT_ONE("Repeat 1 (Loop)", "1x Loop", "Replays the same short continuously"),
    CONTINUOUS("Continuous Auto Play", "Continuous", "Automatically advances to the next short"),
    RANDOM_SHUFFLE("Random Shuffle", "Shuffle", "Jumps to a random short when ended"),
    PROFESSIONAL_END("Professional End Screen", "Pro End", "Shows replay, next short preview and countdown");

    companion object {
        val DEFAULT = CONTINUOUS
    }
}