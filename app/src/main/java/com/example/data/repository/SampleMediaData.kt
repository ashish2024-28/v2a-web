package com.example.data.repository

import com.example.data.model.SongItem
import com.example.data.model.VideoItem

object SampleMediaData {
    /** No pre-loaded default videos — only user-added links and device storage media appear. */
    val sampleVideos: List<VideoItem> = emptyList()

    /** No pre-loaded default songs — only user-added links and device storage audio appear. */
    val sampleSongs: List<SongItem> = emptyList()
}
