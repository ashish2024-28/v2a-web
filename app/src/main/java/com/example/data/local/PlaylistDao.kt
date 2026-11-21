package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt ASC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("UPDATE playlists SET title = :newTitle WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: String, newTitle: String)

    @Query("SELECT * FROM playlist_items ORDER BY sortOrder ASC, addedAt ASC")
    fun getAllPlaylistItemsFlow(): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY sortOrder ASC, addedAt ASC")
    fun getItemsForPlaylistFlow(playlistId: String): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaType = :mediaType AND mediaId = :mediaId")
    suspend fun removeItem(playlistId: String, mediaType: String, mediaId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylistItems(playlistId: String)

    @Query("DELETE FROM playlist_items WHERE mediaId = :mediaId")
    suspend fun removeAllItemsWithMediaId(mediaId: Long)

    @Query("DELETE FROM playlist_items WHERE mediaId IN (:mediaIds)")
    suspend fun removeAllItemsWithMediaIds(mediaIds: List<Long>)
}
