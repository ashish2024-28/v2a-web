package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AudioMetadataEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.VideoMetadataEntity
import com.example.data.local.VyloDatabase
import com.example.data.lyrics.LyricLine
import com.example.data.lyrics.LyricsRepository
import com.example.data.model.DurationFilter
import com.example.data.model.SortOrder
import com.example.data.model.VideoItem
import com.example.domain.feed.SmartFeedEngine
import com.example.player.DecoderMode
import com.example.player.VideoScaleMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VyloFeaturesIntegrationTest {

    private lateinit var database: VyloDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, VyloDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    // ==========================================
    // 1. Room Database Integration Tests
    // ==========================================

    @Test
    fun testPlaylistCreationAndItemOperations() = runTest {
        val playlistDao = database.playlistDao()

        // 1. Insert playlist
        val playlist = PlaylistEntity(
            id = "pl_1",
            title = "My Favorites",
            createdAt = 1000L
        )
        playlistDao.insertPlaylist(playlist)

        val playlists = playlistDao.getAllPlaylistsFlow().first()
        assertEquals(1, playlists.size)
        assertEquals("My Favorites", playlists[0].title)

        // 2. Insert items
        val item1 = PlaylistItemEntity(
            playlistId = "pl_1",
            mediaType = "video",
            mediaId = 101L,
            addedAt = 1000L,
            sortOrder = 0
        )
        val item2 = PlaylistItemEntity(
            playlistId = "pl_1",
            mediaType = "audio",
            mediaId = 202L,
            addedAt = 1001L,
            sortOrder = 1
        )
        playlistDao.insertPlaylistItem(item1)
        playlistDao.insertPlaylistItem(item2)

        val items = playlistDao.getItemsForPlaylistFlow("pl_1").first()
        assertEquals(2, items.size)
        assertEquals(101L, items[0].mediaId)
        assertEquals(202L, items[1].mediaId)

        // 3. Rename playlist
        playlistDao.renamePlaylist("pl_1", "Chill Playlist")
        val updatedPlaylists = playlistDao.getAllPlaylistsFlow().first()
        assertEquals("Chill Playlist", updatedPlaylists[0].title)

        // 4. Remove single item
        playlistDao.removeItem("pl_1", "video", 101L)
        val remainingItems = playlistDao.getItemsForPlaylistFlow("pl_1").first()
        assertEquals(1, remainingItems.size)
        assertEquals(202L, remainingItems[0].mediaId)

        // 5. Delete playlist
        playlistDao.deletePlaylist("pl_1")
        playlistDao.clearPlaylistItems("pl_1")
        assertTrue(playlistDao.getAllPlaylistsFlow().first().isEmpty())
        assertTrue(playlistDao.getItemsForPlaylistFlow("pl_1").first().isEmpty())
    }

    @Test
    fun testVideoMetadataPersistenceAndProgress() = runTest {
        val videoDao = database.videoMetadataDao()

        // 1. Upsert metadata
        val metadata = VideoMetadataEntity(
            videoId = 500L,
            isFavorite = false,
            lastPositionMs = 0L,
            lastPlayedTimestamp = 0L,
            playCount = 0
        )
        videoDao.upsert(metadata)

        val retrieved = videoDao.getMetadata(500L)
        assertNotNull(retrieved)
        assertFalse(retrieved!!.isFavorite)
        assertEquals(0L, retrieved.lastPositionMs)

        // 2. Favorite toggle
        videoDao.setFavorite(500L, true)
        val favorited = videoDao.getMetadata(500L)
        assertTrue(favorited!!.isFavorite)

        // 3. Update playback progress
        videoDao.updateProgress(500L, 45_000L, 1700000000L)
        val progressUpdated = videoDao.getMetadata(500L)
        assertEquals(45_000L, progressUpdated!!.lastPositionMs)
        assertEquals(1700000000L, progressUpdated.lastPlayedTimestamp)

        // 4. Record playback start (increments playCount)
        videoDao.recordPlaybackStart(500L, 45_000L, 1700000005L)
        val countUpdated = videoDao.getMetadata(500L)
        assertEquals(1, countUpdated!!.playCount)

        // 5. Clear history
        videoDao.clearVideoHistory(500L)
        val historyCleared = videoDao.getMetadata(500L)
        assertEquals(0L, historyCleared!!.lastPositionMs)
        assertEquals(0L, historyCleared.lastPlayedTimestamp)

        // 6. Delete metadata
        videoDao.deleteMetadata(500L)
        assertNull(videoDao.getMetadata(500L))
    }

    @Test
    fun testAudioMetadataDaoOperations() = runTest {
        val audioDao = database.audioMetadataDao()

        val audio = AudioMetadataEntity(
            audioId = 999L,
            isFavorite = false,
            lastPositionMs = 120_000L,
            lastPlayedTimestamp = 1690000000L,
            playCount = 3
        )
        audioDao.upsert(audio)

        val retrieved = audioDao.getMetadata(999L)
        assertNotNull(retrieved)
        assertEquals(3, retrieved!!.playCount)

        audioDao.setFavorite(999L, true)
        assertTrue(audioDao.getMetadata(999L)!!.isFavorite)

        audioDao.deleteMetadata(999L)
        assertNull(audioDao.getMetadata(999L))
    }

    // ==========================================
    // 2. Lyrics Parsing & Sync Engine Tests
    // ==========================================

    @Test
    fun testLrcParserWithStandardTimestamps() {
        val sampleLrc = """
            [00:05.50]Hello world
            [00:12.00]This is line two
            [01:03.25]This is one minute in
        """.trimIndent()

        val lines = LyricsRepository.parseLrc(sampleLrc)
        assertEquals(3, lines.size)
        assertEquals(5500L, lines[0].timeMs)
        assertEquals("Hello world", lines[0].text)
        assertEquals(12000L, lines[1].timeMs)
        assertEquals("This is line two", lines[1].text)
        assertEquals(63250L, lines[2].timeMs)
        assertEquals("This is one minute in", lines[2].text)
    }

    @Test
    fun testLrcParserWithMultiTimestampLines() {
        val multiTimestampLrc = """
            [00:02.00][00:10.00]Repeated chorus line
        """.trimIndent()

        val lines = LyricsRepository.parseLrc(multiTimestampLrc)
        assertEquals(2, lines.size)
        assertEquals(2000L, lines[0].timeMs)
        assertEquals("Repeated chorus line", lines[0].text)
        assertEquals(10000L, lines[1].timeMs)
        assertEquals("Repeated chorus line", lines[1].text)
    }

    @Test
    fun testLyricsActiveIndexCalculation() {
        val lines = listOf(
            LyricLine(2000L, "First"),
            LyricLine(5000L, "Second"),
            LyricLine(8000L, "Third")
        )

        // Before first lyric
        assertEquals(-1, LyricsRepository.activeIndex(lines, 1000L))
        // Exactly on first lyric
        assertEquals(0, LyricsRepository.activeIndex(lines, 2000L))
        // Between first and second lyric
        assertEquals(0, LyricsRepository.activeIndex(lines, 4000L))
        // On second lyric
        assertEquals(1, LyricsRepository.activeIndex(lines, 5000L))
        // After all lyrics
        assertEquals(2, LyricsRepository.activeIndex(lines, 10000L))
    }

    // ==========================================
    // 3. Sorting & Filtering Tests
    // ==========================================

    @Test
    fun testVideoSortingByNameDateAndDuration() {
        val v1 = VideoItem(
            id = 1L,
            uri = Uri.parse("content://video/1"),
            title = "Banana",
            displayName = "banana.mp4",
            durationMs = 60_000L,
            sizeBytes = 100_000L,
            dateAddedSec = 100L,
            dateModifiedSec = 100L
        )
        val v2 = VideoItem(
            id = 2L,
            uri = Uri.parse("content://video/2"),
            title = "Apple",
            displayName = "apple.mp4",
            durationMs = 120_000L,
            sizeBytes = 50_000L,
            dateAddedSec = 200L,
            dateModifiedSec = 200L
        )
        val v3 = VideoItem(
            id = 3L,
            uri = Uri.parse("content://video/3"),
            title = "Cherry",
            displayName = "cherry.mp4",
            durationMs = 30_000L,
            sizeBytes = 200_000L,
            dateAddedSec = 50L,
            dateModifiedSec = 50L
        )

        val list = listOf(v1, v2, v3)

        // Sort by Name Ascending
        val sortedByNameAsc = list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        assertEquals("Apple", sortedByNameAsc[0].title)
        assertEquals("Banana", sortedByNameAsc[1].title)
        assertEquals("Cherry", sortedByNameAsc[2].title)

        // Sort by Duration Descending
        val sortedByDurationDesc = list.sortedByDescending { it.durationMs }
        assertEquals(120_000L, sortedByDurationDesc[0].durationMs)
        assertEquals(60_000L, sortedByDurationDesc[1].durationMs)
        assertEquals(30_000L, sortedByDurationDesc[2].durationMs)

        // Sort by Date Descending (Newest first)
        val sortedByDateDesc = list.sortedByDescending { it.dateModifiedSec }
        assertEquals(2L, sortedByDateDesc[0].id)
        assertEquals(1L, sortedByDateDesc[1].id)
        assertEquals(3L, sortedByDateDesc[2].id)
    }

    // ==========================================
    // 4. Player Modes & Aspect Ratio Enums
    // ==========================================

    @Test
    fun testDecoderModeAndAspectRatioEnums() {
        assertEquals(DecoderMode.HW, DecoderMode.valueOf("HW"))
        assertEquals(DecoderMode.SW, DecoderMode.valueOf("SW"))

        val modes = VideoScaleMode.values()
        assertTrue(modes.contains(VideoScaleMode.FIT))
        assertTrue(modes.contains(VideoScaleMode.FILL))
        assertTrue(modes.contains(VideoScaleMode.CROP))
        assertTrue(modes.contains(VideoScaleMode.ORIGINAL))
        assertTrue(modes.contains(VideoScaleMode.ULTRAWIDE_18_9))
        assertTrue(modes.contains(VideoScaleMode.CINEMATIC_21_9))
    }

    // ==========================================
    // 5. SmartFeedEngine Edge Cases
    // ==========================================

    @Test
    fun testSmartFeedEngineEmptyListAndFilters() {
        val engine = SmartFeedEngine()
        val emptyFeed = engine.generateFeed(emptyList(), DurationFilter.ALL)
        assertTrue(emptyFeed.isEmpty())

        val video = VideoItem(
            id = 10L,
            uri = Uri.parse("content://video/10"),
            title = "Test Short",
            displayName = "test.mp4",
            durationMs = 25_000L,
            sizeBytes = 5_000_000L,
            dateAddedSec = 1000L,
            dateModifiedSec = 1000L
        )

        val feed = engine.generateFeed(listOf(video), DurationFilter.LESS_THAN_1_MIN)
        assertEquals(1, feed.size)
        assertEquals(10L, feed[0].id)

        // If filter does not match, result is empty (video with 0ms or duration beyond range)
        val longVideo = VideoItem(
            id = 11L,
            uri = Uri.parse("content://video/11"),
            title = "Long Movie",
            displayName = "long.mp4",
            durationMs = 900_000L, // 15 min
            sizeBytes = 500_000_000L,
            dateAddedSec = 1000L,
            dateModifiedSec = 1000L
        )
        val notMatching = engine.generateFeed(listOf(longVideo), DurationFilter.LESS_THAN_1_MIN)
        assertTrue(notMatching.isEmpty())
    }
}
