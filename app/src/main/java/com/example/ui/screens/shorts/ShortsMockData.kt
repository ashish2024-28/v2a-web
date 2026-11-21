package com.example.ui.screens.shorts

import androidx.compose.ui.graphics.Color
import com.example.data.model.VideoItem
import kotlin.math.abs

/**
 * Realistic in-memory dummy data model for YouTube Shorts & Instagram Reels experience.
 * Fully interactive (working UI state, like counts increment/decrement, dynamic comments)
 * without requiring persistent database storage.
 */
data class ShortsComment(
    val id: String,
    val username: String,
    val handle: String,
    val avatarColor: Color,
    val avatarInitial: String,
    val text: String,
    val timeAgo: String,
    var likesCount: Int,
    var isLiked: Boolean = false,
    val isVerified: Boolean = false,
    val repliesCount: Int = 0
)

data class ShortsMockProfile(
    val channelName: String,
    val handle: String,
    val avatarInitial: String,
    val avatarColor: Color,
    val isVerified: Boolean,
    val subscriberCountText: String
)

object ShortsMockData {

    private val AVATAR_COLORS = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFF44336)  // Red
    )

    private val CHANNEL_NAMES = listOf(
        "NeonCraft Studio" to "@neoncraft",
        "Cinematic Motion" to "@cinematic_cuts",
        "Urban Beat Lab" to "@urbanbeat",
        "Hyperlapse Daily" to "@hyperlapsedaily",
        "Visual Wonders" to "@visualwonders",
        "Pulse Creations" to "@pulsecreations",
        "Sound & Vision" to "@soundandvision",
        "Pixel Drift" to "@pixeldrift",
        "Vibe Horizon" to "@vibehorizon",
        "Tech Wave" to "@techwave"
    )

    fun getProfileForVideo(video: VideoItem): ShortsMockProfile {
        val hash = abs(video.title.hashCode() + video.id.hashCode())
        val namePair = CHANNEL_NAMES[hash % CHANNEL_NAMES.size]
        val color = AVATAR_COLORS[hash % AVATAR_COLORS.size]
        val subscribers = "${(hash % 900 + 100) / 10.0}K subscribers"
        val folderBasedName = if (video.parentFolderName.isNotBlank() && video.parentFolderName != "0" && video.parentFolderName != "emulated") {
            video.parentFolderName
        } else {
            namePair.first
        }
        val handle = "@${folderBasedName.lowercase().replace(" ", "_").take(15)}"

        return ShortsMockProfile(
            channelName = folderBasedName,
            handle = handle,
            avatarInitial = folderBasedName.take(1).uppercase().ifEmpty { "V" },
            avatarColor = color,
            isVerified = (hash % 3 == 0),
            subscriberCountText = subscribers
        )
    }

    fun getBaseLikesCount(video: VideoItem): Int {
        val hash = abs(video.title.hashCode() + video.id.hashCode())
        return 15_000 + (hash % 850_000)
    }

    fun getBaseCommentsCount(video: VideoItem): Int {
        val hash = abs(video.title.hashCode() + video.id.hashCode() + 101)
        return 450 + (hash % 18_500)
    }

    fun getBaseSharesCount(video: VideoItem): Int {
        val hash = abs(video.title.hashCode() + video.id.hashCode() + 202)
        return 320 + (hash % 42_000)
    }

    fun getBaseRemixCount(video: VideoItem): Int {
        val hash = abs(video.title.hashCode() + video.id.hashCode() + 303)
        return 120 + (hash % 12_000)
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val value = count / 1_000_000.0
                if (value >= 10.0) "${value.toInt()}M" else String.format(java.util.Locale.getDefault(), "%.1fM", value)
            }
            count >= 1_000 -> {
                val value = count / 1_000.0
                if (value >= 10.0) "${value.toInt()}K" else String.format(java.util.Locale.getDefault(), "%.1fK", value)
            }
            else -> count.toString()
        }
    }

    fun generateInitialComments(video: VideoItem): List<ShortsComment> {
        val hash = abs(video.title.hashCode() + video.id.hashCode())
        return listOf(
            ShortsComment(
                id = "${video.id}_c1",
                username = "Alex Turner",
                handle = "@alex_t",
                avatarColor = AVATAR_COLORS[(hash) % AVATAR_COLORS.size],
                avatarInitial = "A",
                text = "The color grading and sound design on this is absolutely insane! 🔥🔥",
                timeAgo = "2h ago",
                likesCount = 1420 + (hash % 800),
                isLiked = false,
                isVerified = true,
                repliesCount = 14
            ),
            ShortsComment(
                id = "${video.id}_c2",
                username = "Elena Rostova",
                handle = "@elena_vibe",
                avatarColor = AVATAR_COLORS[(hash + 1) % AVATAR_COLORS.size],
                avatarInitial = "E",
                text = "Watched this at least 10 times in a row! What camera did you use for this? 🎥",
                timeAgo = "5h ago",
                likesCount = 852 + (hash % 400),
                isLiked = false,
                isVerified = false,
                repliesCount = 6
            ),
            ShortsComment(
                id = "${video.id}_c3",
                username = "Devin Brooks",
                handle = "@devin_cuts",
                avatarColor = AVATAR_COLORS[(hash + 2) % AVATAR_COLORS.size],
                avatarInitial = "D",
                text = "That seamless loop transition caught me off guard 💯 super clean edit",
                timeAgo = "1d ago",
                likesCount = 421 + (hash % 200),
                isLiked = false,
                isVerified = false,
                repliesCount = 2
            ),
            ShortsComment(
                id = "${video.id}_c4",
                username = "Maya Lin",
                handle = "@maya_arts",
                avatarColor = AVATAR_COLORS[(hash + 3) % AVATAR_COLORS.size],
                avatarInitial = "M",
                text = "Track ID please!! This beat hits different in headphones 🎧✨",
                timeAgo = "2d ago",
                likesCount = 189 + (hash % 100),
                isLiked = false,
                isVerified = true,
                repliesCount = 8
            ),
            ShortsComment(
                id = "${video.id}_c5",
                username = "RetroWave",
                handle = "@retrowave99",
                avatarColor = AVATAR_COLORS[(hash + 4) % AVATAR_COLORS.size],
                avatarInitial = "R",
                text = "Algorithm brought me here and I am definitely not disappointed ⚡",
                timeAgo = "3d ago",
                likesCount = 94,
                isLiked = false,
                isVerified = false,
                repliesCount = 0
            )
        )
    }
}
