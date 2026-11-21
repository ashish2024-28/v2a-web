package com.example

import com.example.player.MediaPlaybackUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaPlaybackUtilsTest {
    @Test
    fun `content uri uses declared MediaStore mime`() {
        assertEquals(
            "video/mp4",
            MediaPlaybackUtils.resolveMimeType(
                declaredMimeType = "video/mp4",
                path = "/storage/emulated/0/Movies/demo.mp4",
                uriString = "content://media/external/video/media/123",
                isAudio = false
            )
        )
    }

    @Test
    fun `numeric content uri falls back to file extension`() {
        assertEquals(
            "audio/mpeg",
            MediaPlaybackUtils.resolveMimeType(
                declaredMimeType = null,
                path = "/storage/emulated/0/Music/song.mp3",
                uriString = "content://media/external/audio/media/123",
                isAudio = true
            )
        )
    }

    @Test
    fun `matroska and webm receive explicit container hints`() {
        assertEquals(
            "application/x-matroska",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/movie.mkv", "content://media/1", false)
        )
        assertEquals(
            "video/webm",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/clip.webm", "content://media/2", false)
        )
    }

    @Test
    fun `generic wildcard does not override extension detection`() {
        assertEquals(
            "audio/flac",
            MediaPlaybackUtils.resolveMimeType("audio/*", "/Music/song.flac", "content://media/3", true)
        )
    }

    @Test
    fun `generic octet stream does not hide a known extension`() {
        assertEquals(
            "video/mp4",
            MediaPlaybackUtils.resolveMimeType("application/octet-stream", "/Movies/video.mp4", "content://media/4", false)
        )
    }

    @Test
    fun `playit file format and alternate extensions are resolved properly`() {
        assertEquals(
            "video/mp4",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/video.playit", "content://media/5", false)
        )
        assertEquals(
            "application/x-matroska",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/movie.mkv.playit", "content://media/6", false)
        )
        assertEquals(
            "video/x-msvideo",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/old_movie.avi", "content://media/7", false)
        )
        assertEquals(
            "video/x-flv",
            MediaPlaybackUtils.resolveMimeType(null, "/Movies/stream.flv", "content://media/8", false)
        )
    }

    @Test
    fun `unknown media remains sniffable`() {
        assertNull(
            MediaPlaybackUtils.resolveMimeType(null, null, "content://media/external/123", false)
        )
    }
}
