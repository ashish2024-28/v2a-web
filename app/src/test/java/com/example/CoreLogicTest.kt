package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DurationFilter
import com.example.data.model.VideoItem
import com.example.domain.feed.SmartFeedEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoreLogicTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("V2S", appName)
  }

  @Test
  fun `duration filter correctly filters videos`() {
    val shortVideo = VideoItem(
        id = 1L,
        uri = Uri.parse("content://video/1"),
        title = "Short Video",
        displayName = "short.mp4",
        durationMs = 45_000L,
        sizeBytes = 10_000_000L,
        dateAddedSec = 1000L,
        dateModifiedSec = 1000L
    )

    val mediumVideo = VideoItem(
        id = 2L,
        uri = Uri.parse("content://video/2"),
        title = "Medium Video",
        displayName = "medium.mp4",
        durationMs = 180_000L, // 3 min
        sizeBytes = 50_000_000L,
        dateAddedSec = 1000L,
        dateModifiedSec = 1000L
    )

    val longVideo = VideoItem(
        id = 3L,
        uri = Uri.parse("content://video/3"),
        title = "Long Video",
        displayName = "long.mp4",
        durationMs = 700_000L, // 11 min 40s
        sizeBytes = 200_000_000L,
        dateAddedSec = 1000L,
        dateModifiedSec = 1000L
    )

    assertTrue(DurationFilter.LESS_THAN_1_MIN.matches(shortVideo.durationMs))
    assertTrue(!DurationFilter.LESS_THAN_1_MIN.matches(mediumVideo.durationMs))
    assertTrue(!DurationFilter.LESS_THAN_1_MIN.matches(longVideo.durationMs))

    assertTrue(DurationFilter.LESS_THAN_5_MIN.matches(shortVideo.durationMs))
    assertTrue(DurationFilter.LESS_THAN_5_MIN.matches(mediumVideo.durationMs))
    assertTrue(!DurationFilter.LESS_THAN_5_MIN.matches(longVideo.durationMs))

    assertTrue(DurationFilter.ALL.matches(longVideo.durationMs))
  }

  @Test
  fun `smart feed engine prioritizes without instant duplicate`() {
    val engine = SmartFeedEngine()
    val videos = (1L..10L).map { id ->
        VideoItem(
            id = id,
            uri = Uri.parse("content://video/$id"),
            title = "Video $id",
            displayName = "video_$id.mp4",
            durationMs = 30_000L,
            sizeBytes = 10_000_000L,
            dateAddedSec = 1000L + id,
            dateModifiedSec = 1000L + id
        )
    }

    val feed = engine.generateFeed(videos, DurationFilter.ALL)
    assertEquals(10, feed.size)

    // Verify all IDs are unique in the generated feed
    val uniqueIds = feed.map { it.id }.toSet()
    assertEquals(10, uniqueIds.size)
  }
}
