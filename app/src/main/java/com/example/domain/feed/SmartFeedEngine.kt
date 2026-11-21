package com.example.domain.feed

import com.example.data.model.DurationFilter
import com.example.data.model.VideoItem
import kotlin.math.max
import kotlin.random.Random

class SmartFeedEngine {
    private val sessionHistoryIds = mutableSetOf<Long>()
    private val recentQueue = ArrayDeque<Long>()
    private val maxRecentMemory = 30

    fun generateFeed(
        allVideos: List<VideoItem>,
        filter: DurationFilter,
        targetSize: Int = 50
    ): List<VideoItem> {
        val eligible = allVideos.filter { filter.matches(it.durationMs) }
        if (eligible.isEmpty()) return emptyList()

        if (eligible.size <= 3) {
            // For tiny libraries, shuffle without immediate adjacent duplicate
            return eligible.shuffled()
        }

        val now = System.currentTimeMillis()
        val oldestAdded = eligible.minOfOrNull { it.dateAddedSec } ?: (now / 1000)
        val newestAdded = eligible.maxOfOrNull { it.dateAddedSec } ?: (now / 1000)
        val addedSpan = max(1L, newestAdded - oldestAdded)

        // Split into unseen in this session vs already seen
        val unseenCandidates = eligible.filter { it.id !in sessionHistoryIds }
        val pool = if (unseenCandidates.isNotEmpty()) unseenCandidates else {
            // Reset session history when all have been viewed, but keep the last 3 from repeating immediately
            val lastSeen = recentQueue.takeLast(3).toSet()
            sessionHistoryIds.clear()
            sessionHistoryIds.addAll(lastSeen)
            eligible.filter { it.id !in lastSeen }.ifEmpty { eligible }
        }

        // Score each candidate:
        // 1. Recency of addition (0..30 pts)
        // 2. Not recently watched (0..35 pts)
        // 3. Favorite status (0..20 pts)
        // 4. Random variation (0..25 pts)
        val scoredList = pool.map { video ->
            var score = 0.0

            // 1. Recently added
            val recencyRatio = (video.dateAddedSec - oldestAdded).toDouble() / addedSpan.toDouble()
            score += (recencyRatio * 30.0)

            // 2. Not recently watched
            if (video.lastPlayedTimestamp == 0L) {
                score += 35.0 // Never watched yet
            } else {
                val hoursSinceWatched = (now - video.lastPlayedTimestamp) / (1000 * 3600.0)
                // The longer ago it was watched, the higher the score
                val watchAgeBonus = (hoursSinceWatched / 72.0).coerceIn(0.0, 1.0) * 25.0
                score += watchAgeBonus
            }

            // 3. Favorite status bonus
            if (video.isFavorite) {
                score += 20.0
            }

            // 4. Random variation to avoid deterministic monotony
            score += Random.nextDouble(0.0, 25.0)

            Pair(video, score)
        }.sortedByDescending { it.second }
            .map { it.first }

        return scoredList
    }

    fun markVideoSeen(videoId: Long) {
        sessionHistoryIds.add(videoId)
        recentQueue.addLast(videoId)
        if (recentQueue.size > maxRecentMemory) {
            recentQueue.removeFirst()
        }
    }

    fun resetSession() {
        sessionHistoryIds.clear()
        recentQueue.clear()
    }
}
