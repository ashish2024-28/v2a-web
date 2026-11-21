package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_media_links")
data class UserMediaLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val title: String,
    val mediaType: String, // "VIDEO", "SHORT", "SONG"
    val sourceName: String = "",
    val thumbnailUrl: String? = null,
    val createdAtSec: Long = System.currentTimeMillis() / 1000L
)
