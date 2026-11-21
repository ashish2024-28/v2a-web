package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMediaLinkDao {
    @Query("SELECT * FROM user_media_links ORDER BY createdAtSec DESC, id DESC")
    fun getAllLinksFlow(): Flow<List<UserMediaLinkEntity>>

    @Query("SELECT * FROM user_media_links WHERE id = :id LIMIT 1")
    suspend fun getLinkById(id: Long): UserMediaLinkEntity?

    @Query("SELECT * FROM user_media_links WHERE LOWER(url) = LOWER(:url) LIMIT 1")
    suspend fun findByUrl(url: String): UserMediaLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: UserMediaLinkEntity): Long

    @Query("""
        UPDATE user_media_links
        SET url = :url,
            title = :title,
            mediaType = :mediaType,
            sourceName = :sourceName,
            thumbnailUrl = :thumbnailUrl
        WHERE id = :id
    """)
    suspend fun updateLink(
        id: Long,
        url: String,
        title: String,
        mediaType: String,
        sourceName: String,
        thumbnailUrl: String?
    )

    @Query("DELETE FROM user_media_links WHERE id = :id")
    suspend fun deleteLinkById(id: Long)

    @Query("DELETE FROM user_media_links WHERE id IN (:ids)")
    suspend fun deleteLinksByIds(ids: List<Long>)
}
