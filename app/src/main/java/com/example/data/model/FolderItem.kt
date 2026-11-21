package com.example.data.model

import android.net.Uri
import java.util.Locale

data class FolderItem(
    val name: String,
    val path: String,
    val videoCount: Int = 0,
    val songCount: Int = 0,
    val totalSizeBytes: Long = 0L,
    val latestTimestamp: Long = 0L,
    val previewUri: Uri? = null,
    val previewVideo: VideoItem? = null
) {
    val totalItemsCount: Int
        get() = videoCount + songCount

    val formattedSize: String
        get() {
            if (totalSizeBytes <= 0) return "--"
            val kb = totalSizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                else -> String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }

    val formattedTotalCount: String
        get() {
            val parts = mutableListOf<String>()
            if (videoCount > 0) parts.add("$videoCount ${if (videoCount == 1) "video" else "videos"}")
            if (songCount > 0) parts.add("$songCount ${if (songCount == 1) "song" else "songs"}")
            return if (parts.isNotEmpty()) parts.joinToString(", ") else "0 files"
        }

    val formattedTotalSize: String
        get() = formattedSize
}
