package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import androidx.compose.material.icons.automirrored.outlined.QueueMusic

@Composable
fun VyloBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                modifier = Modifier.weight(1f),
                title = "Videos",
                icon = Icons.Outlined.VideoLibrary,
                selected = currentScreen == AppScreen.VIDEOS,
                testTag = "bottom_nav_video",
                onClick = { onNavigate(AppScreen.VIDEOS) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                title = "Songs",
                icon = Icons.Outlined.MusicNote,
                selected = currentScreen == AppScreen.SONGS,
                testTag = "bottom_nav_audio",
                onClick = { onNavigate(AppScreen.SONGS) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                title = "Shorts",
                icon = Icons.Outlined.AutoAwesome,
                selected = currentScreen == AppScreen.SHORTS,
                testTag = "bottom_nav_shorts",
                onClick = { onNavigate(AppScreen.SHORTS) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                title = "Playlists",
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                selected = currentScreen == AppScreen.PLAYLISTS,
                testTag = "bottom_nav_playlists",
                onClick = { onNavigate(AppScreen.PLAYLISTS) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                title = "More",
                icon = Icons.Outlined.MoreHoriz,
                selected = currentScreen == AppScreen.SETTINGS,
                testTag = "bottom_nav_more",
                onClick = { onNavigate(AppScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetContentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val color by animateColorAsState(
        targetValue = targetContentColor,
        label = "bottom_nav_color"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                } else Modifier
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 3.dp,
                vertical = 2.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = title,
                color = color,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
