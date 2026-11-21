package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: String,
    val mediaType: String, // "VIDEO" or "AUDIO"
    val mediaId: Long,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
