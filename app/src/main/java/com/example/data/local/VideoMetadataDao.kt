package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoMetadataDao {
    @Query("SELECT * FROM video_metadata")
    fun getAllMetadataFlow(): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE videoId = :videoId LIMIT 1")
    fun getMetadataFlow(videoId: Long): Flow<VideoMetadataEntity?>

    @Query("SELECT * FROM video_metadata WHERE videoId = :videoId LIMIT 1")
    suspend fun getMetadata(videoId: Long): VideoMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: VideoMetadataEntity)

    @Query("UPDATE video_metadata SET isFavorite = :isFavorite WHERE videoId = :videoId")
    suspend fun setFavorite(videoId: Long, isFavorite: Boolean)

    @Query("UPDATE video_metadata SET lastPositionMs = :positionMs, lastPlayedTimestamp = :timestamp WHERE videoId = :videoId")
    suspend fun updateProgress(videoId: Long, positionMs: Long, timestamp: Long)

    @Query("UPDATE video_metadata SET lastPositionMs = :positionMs, lastPlayedTimestamp = :timestamp, playCount = playCount + 1 WHERE videoId = :videoId")
    suspend fun recordPlaybackStart(videoId: Long, positionMs: Long, timestamp: Long)

    @Query("UPDATE video_metadata SET lastPlayedTimestamp = 0, lastPositionMs = 0 WHERE videoId = :videoId")
    suspend fun clearVideoHistory(videoId: Long)

    @Query("UPDATE video_metadata SET lastPlayedTimestamp = 0, lastPositionMs = 0")
    suspend fun clearAllVideoHistory()

    @Query("DELETE FROM video_metadata WHERE videoId = :videoId")
    suspend fun deleteMetadata(videoId: Long)

    @Query("DELETE FROM video_metadata WHERE videoId IN (:videoIds)")
    suspend fun deleteMultipleMetadata(videoIds: List<Long>)
}
