package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioMetadataDao {
    @Query("SELECT * FROM audio_metadata")
    fun getAllMetadataFlow(): Flow<List<AudioMetadataEntity>>

    @Query("SELECT * FROM audio_metadata WHERE audioId = :audioId LIMIT 1")
    suspend fun getMetadata(audioId: Long): AudioMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: AudioMetadataEntity)

    @Query("UPDATE audio_metadata SET isFavorite = :isFavorite WHERE audioId = :audioId")
    suspend fun setFavorite(audioId: Long, isFavorite: Boolean)

    @Query("UPDATE audio_metadata SET lastPositionMs = :positionMs, lastPlayedTimestamp = :timestamp WHERE audioId = :audioId")
    suspend fun updateProgress(audioId: Long, positionMs: Long, timestamp: Long)

    @Query("UPDATE audio_metadata SET lastPositionMs = :positionMs, lastPlayedTimestamp = :timestamp, playCount = playCount + 1 WHERE audioId = :audioId")
    suspend fun recordPlaybackStart(audioId: Long, positionMs: Long, timestamp: Long)

    @Query("UPDATE audio_metadata SET lastPlayedTimestamp = 0, lastPositionMs = 0 WHERE audioId = :audioId")
    suspend fun clearSongHistory(audioId: Long)

    @Query("UPDATE audio_metadata SET lastPlayedTimestamp = 0, lastPositionMs = 0")
    suspend fun clearAllSongHistory()

    @Query("DELETE FROM audio_metadata WHERE audioId = :audioId")
    suspend fun deleteMetadata(audioId: Long)

    @Query("DELETE FROM audio_metadata WHERE audioId IN (:audioIds)")
    suspend fun deleteMultipleMetadata(audioIds: List<Long>)
}
