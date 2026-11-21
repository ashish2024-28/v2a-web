
package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.V2SWindowSize
import com.example.ui.components.VideoThumbnail
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.V2SHomeLogo
import com.example.ui.components.V2SHeroBannerLogo
import com.example.ui.theme.VyloCoralAccent
import com.example.ui.theme.VyloCyanAccent
import com.example.ui.theme.VyloVioletDark
import com.example.ui.theme.VyloVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    totalVideosCount: Int,
    continueWatchingList: List<VideoItem>,
    recentlyAddedList: List<VideoItem>,
    recentlyWatchedList: List<VideoItem>,
    favoriteList: List<VideoItem>,
    isScanning: Boolean,
    onEnterShorts: () -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onResumeVideo: (VideoItem) -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    ResponsiveScreen(
    modifier = modifier
) { windowSize, horizontalPadding ->

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            HomeTopBar(
                totalVideosCount = totalVideosCount,
                isScanning = isScanning,
                onOpenSettings = onOpenSettings,
                onRefreshScan = onRefreshScan,
                horizontalPadding = horizontalPadding
            )
        }

        // Official V2S 3D Hero Banner
        item {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding , vertical = 6.dp)) {
                V2SHeroBannerLogo(
                    height = when (windowSize) {
                        V2SWindowSize.COMPACT -> 150.dp
                        V2SWindowSize.MEDIUM -> 190.dp
                        V2SWindowSize.EXPANDED -> 230.dp
                    },
                    onClick = onEnterShorts
                )
            }
        }

        // Hero Shorts Entry Banner
        item {
            HeroShortsBanner(
                totalVideos = totalVideosCount,
                onEnterShorts = onEnterShorts
            )
        }

        // Continue Watching Section
        if (continueWatchingList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Continue Watching",
                    subtitle = "Resume from where you left off",
                    icon = Icons.Filled.History,
                    iconTint = VyloCyanAccent
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueWatchingList, key = { "cw_${it.id}" }) { video ->
                        ContinueWatchingCard(
                            video = video,
                            onClick = { onResumeVideo(video) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Recently Added Videos Section
        if (recentlyAddedList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Added",
                    subtitle = "Latest videos discovered on device",
                    icon = Icons.Filled.VideoLibrary,
                    iconTint = VyloVioletPrimary
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyAddedList, key = { "ra_${it.id}" }) { video ->
                        VideoCardSmall(
                            video = video,
                            onClick = { onPlayVideo(video) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Recently Watched Section
        if (recentlyWatchedList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Watched",
                    subtitle = "Your playback history",
                    icon = Icons.Filled.PlayCircle,
                    iconTint = VyloCoralAccent
                )
            }
            items(recentlyWatchedList, key = { "rw_${it.id}" }) { video ->
                RecentlyWatchedItemRow(
                    video = video,
                    onClick = { onPlayVideo(video) }
                )
            }
        }

        // Quick Stats Footer Card
        item {
            QuickStatsCard(
                totalCount = totalVideosCount,
                favoritesCount = favoriteList.size,
                continueCount = continueWatchingList.size
            )
        }
    }
}
    }
    

@Composable
private fun HomeTopBar(
    totalVideosCount: Int,
    isScanning: Boolean,
    onOpenSettings: () -> Unit,
    onRefreshScan: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            V2SBrandLogo(
                iconSize = 46.dp,
                showTagline = true
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "$totalVideosCount",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onRefreshScan,
                modifier = Modifier.testTag("home_refresh_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Scan videos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("home_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } 
}

@Composable
private fun HeroShortsBanner(
    totalVideos: Int,
    onEnterShorts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp))
            .clickable(onClick = onEnterShorts)
            .testTag("hero_shorts_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            VyloVioletDark.copy(alpha = 0.85f),
                            Color(0xFF2E1065).copy(alpha = 0.95f),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = null,
                        tint = VyloCoralAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SMART SHORTS FEED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VyloCoralAccent,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Swipe through your videos like Shorts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Smart non-repeating algorithm · Duration filters · 100% On-device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onEnterShorts,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VyloVioletPrimary
                    ),
                    modifier = Modifier.testTag("enter_shorts_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Shorts ($totalVideos videos)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("continue_watching_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                VideoThumbnail(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { video.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = VyloCoralAccent,
                    trackColor = Color.Black.copy(alpha = 0.5f)
                )

                // Resume badge
                val resumeSec = video.lastPositionMs / 1000
                val resumeFormatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", (resumeSec % 3600) / 60, resumeSec % 60)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = "Resume $resumeFormatted",
                        color = VyloCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.displayName} • ${video.formattedDuration}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun VideoCardSmall(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("video_card_small_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                VideoThumbnail(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = video.formattedDuration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = video.title,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun RecentlyWatchedItemRow(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("recently_watched_row_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 55.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                VideoThumbnail(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.formattedDuration} · ${video.formattedSize}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (video.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = VyloCoralAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    totalCount: Int,
    favoritesCount: Int,
    continueCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Total Videos", value = totalCount.toString())
            StatItem(label = "Favorites", value = favoritesCount.toString())
            StatItem(label = "In Progress", value = continueCount.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
