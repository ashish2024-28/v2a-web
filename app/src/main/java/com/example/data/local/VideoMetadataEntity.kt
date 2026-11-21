package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_metadata")
data class VideoMetadataEntity(
    @PrimaryKey val videoId: Long,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val playCount: Int = 0,
    val userNotes: String = ""
)
