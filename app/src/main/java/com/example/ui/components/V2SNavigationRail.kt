package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VlcOrange
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ThemeMode

private data class RailNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun V2SNavigationRail(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    deviceType: V2SDeviceType,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onOpenWelcomeGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        RailNavItem(AppScreen.VIDEOS, "Videos", Icons.Filled.VideoLibrary),
        RailNavItem(AppScreen.SHORTS, "Shorts", Icons.Filled.Whatshot),
        RailNavItem(AppScreen.SONGS, "Songs", Icons.Filled.MusicNote),
        RailNavItem(AppScreen.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.QueueMusic),
        RailNavItem(AppScreen.SETTINGS, "Settings", Icons.Filled.MoreHoriz)
    )

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                V2SBrandLogo(iconSize = 36.dp, showTagline = false)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                navItems.forEach { item ->
                    val selected = currentScreen == item.screen
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavigate(item.screen) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = VlcOrange,
                            indicatorColor = VlcOrange,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (themeMode == ThemeMode.DARK) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = VlcOrange
                    )
                }
                IconButton(onClick = onOpenWelcomeGuide) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Device Guide",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
