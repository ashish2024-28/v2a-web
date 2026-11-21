package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoMetadataEntity::class,
        AudioMetadataEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        UserMediaLinkEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class VyloDatabase : RoomDatabase() {
    abstract fun videoMetadataDao(): VideoMetadataDao
    abstract fun audioMetadataDao(): AudioMetadataDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun userMediaLinkDao(): UserMediaLinkDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS audio_metadata (
                        audioId INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        lastPositionMs INTEGER NOT NULL DEFAULT 0,
                        lastPlayedTimestamp INTEGER NOT NULL DEFAULT 0,
                        playCount INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(audioId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN category TEXT NOT NULL DEFAULT 'ALL'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_media_links (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        url TEXT NOT NULL,
                        title TEXT NOT NULL,
                        mediaType TEXT NOT NULL,
                        sourceName TEXT NOT NULL DEFAULT '',
                        thumbnailUrl TEXT,
                        createdAtSec INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: VyloDatabase? = null

        fun getInstance(context: Context): VyloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VyloDatabase::class.java,
                    "vylo_database.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



















