package com.example.data.local

import androidx.room.Entity

@Entity(tableName = "audio_metadata")
data class AudioMetadataEntity(
    @androidx.room.PrimaryKey val audioId: Long,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val playCount: Int = 0
)
