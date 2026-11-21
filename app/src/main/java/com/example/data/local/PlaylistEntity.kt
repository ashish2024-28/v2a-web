package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val isCustom: Boolean = true,
    val category: String = "ALL", // "ALL", "SONGS", "VIDEOS"
    val createdAt: Long = System.currentTimeMillis()
)

