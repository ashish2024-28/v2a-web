package com.example.ui.screens.settings

import com.example.ui.theme.AccentColor
import com.example.ui.theme.VlcOrange
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.saveable.rememberSaveable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.VideoItem
import com.example.data.model.SongItem
import com.example.ui.components.ResponsiveScreen
import com.example.ui.components.V2SBrandLogo
import com.example.ui.components.VideoThumbnail
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MediaMode
import com.example.ui.viewmodel.ThemeMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Save
import kotlin.math.roundToInt
import androidx.compose.material.icons.automirrored.filled.VolumeUp

data class StreamItem(
    val id: String,
    val title: String,
    val url: String,
    val subtitle: String = url
)

enum class MoreSubView {
    MAIN,
    SETTINGS_DETAIL,
    ABOUT_DETAIL,
    HELP_SUPPORT,
    EARPHONE_CONTROLS,
    HISTORY_ALL,
    STREAMS_ALL
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerManager.playerState.collectAsState()
    val recentlyWatched by viewModel.recentlyWatchedVideos.collectAsState()
    val recentlyWatchedSongs by viewModel.recentlyWatchedSongs.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var currentSubView by remember { mutableStateOf(MoreSubView.MAIN) }
    var helpSupportInitialTab by remember { mutableStateOf(SupportTab.REPORT_AND_SUGGEST) }
    var helpSupportInitialCategory by remember { mutableStateOf(FeedbackCategory.BUG_REPORT) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showNewStreamDialog by remember { mutableStateOf(false) }
    var streamUrlInput by remember { mutableStateOf("") }
    var streamTitleInput by remember { mutableStateOf("") }
    var streamUrlError by remember { mutableStateOf<String?>(null) }
    var isNoticeBannerDismissed by rememberSaveable { mutableStateOf(false) }

    var customStreams by remember {
        mutableStateOf<List<StreamItem>>(emptyList())
    }

    BackHandler(enabled = currentSubView != MoreSubView.MAIN) {
        currentSubView = MoreSubView.MAIN
    }

    ResponsiveScreen(
        modifier = modifier
    ) { _windowSize, horizontalPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentSubView) {
                MoreSubView.MAIN -> {
                    // More screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = horizontalPadding,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
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
                                        text = "${allVideos.size + allSongs.size}",
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

                            Box {
                                IconButton(onClick = { showTopMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showTopMenu,
                                    onDismissRequest = { showTopMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Settings,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            currentSubView = MoreSubView.SETTINGS_DETAIL
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Help & Support") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Headphones,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            currentSubView = MoreSubView.HELP_SUPPORT
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("About") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Info,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            currentSubView = MoreSubView.ABOUT_DETAIL
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Refresh Media Scan") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Refresh,
                                                null
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.refreshMediaScan()
                                        }
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = horizontalPadding,
                                vertical = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 1. Top Action Buttons (Settings, Support, About)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable {
                                                currentSubView =
                                                    MoreSubView.SETTINGS_DETAIL
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF141416),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SETTINGS",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable {
                                                currentSubView =
                                                    MoreSubView.HELP_SUPPORT
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF141A22),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Headphones,
                                                contentDescription = "Support",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SUPPORT",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable {
                                                currentSubView =
                                                    MoreSubView.ABOUT_DETAIL
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF141416),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Info,
                                                contentDescription = "About",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ABOUT",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                 }
                            }

                            // 2. Streams Section
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentSubView =
                                                    MoreSubView.STREAMS_ALL
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Streams",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 19.sp
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "View all streams",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(125.dp)
                                                .clickable {
                                                    showNewStreamDialog = true
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF141416),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = "Add",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(38.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "New stream",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        val firstStream = customStreams.firstOrNull()

                                        if (firstStream != null) {
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(125.dp)
                                                    .clickable {
                                                        val dummyVid = VideoItem(
                                                            id = 999990L,
                                                            uri = Uri.parse(firstStream.url),
                                                            title = firstStream.title,
                                                            displayName = firstStream.title,
                                                            durationMs = 0L,
                                                            sizeBytes = 0L,
                                                            width = 1920,
                                                            height = 1080,
                                                            dateAddedSec =
                                                                System.currentTimeMillis() / 1000,
                                                            dateModifiedSec =
                                                                System.currentTimeMillis() / 1000,
                                                            isDemoSample = false
                                                        )
                                                        viewModel.openNormalPlayer(dummyVid)
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF141416),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(12.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize(),
                                                        horizontalAlignment =
                                                            Alignment.CenterHorizontally,
                                                        verticalArrangement =
                                                            Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Cast,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Spacer(
                                                            modifier = Modifier.height(8.dp)
                                                        )
                                                        Text(
                                                            text = firstStream.title,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = firstStream.subtitle,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Separate playback history sections
                            item {
                                HistorySection(
                                    title = "Video History",
                                    icon = Icons.Filled.History,
                                    videos = recentlyWatched.take(15),
                                    totalCount = recentlyWatched.size,
                                    onPlay = { viewModel.openNormalPlayer(it) }
                                ) { currentSubView = MoreSubView.HISTORY_ALL }
                            }
                            item {
                                SongHistorySection(
                                    title = "Song History",
                                    songs = recentlyWatchedSongs.take(15),
                                    totalCount = recentlyWatchedSongs.size,
                                    onPlay = { viewModel.playSong(it) }
                                ) { currentSubView = MoreSubView.HISTORY_ALL }
                            }

                            // 4. Help & Support Section (Below Songs History)
                            item {
                                HelpAndSupportSection(
                                    onOpenHelpSupport = { currentSubView = MoreSubView.HELP_SUPPORT }
                                )
                            }

                            // 5. Earphone & Earbuds Controls Section (Below Help & Support)
                            item {
                                EarphoneControlsSection(
                                    onOpenEarphoneSettings = { currentSubView = MoreSubView.EARPHONE_CONTROLS }
                                )
                            }
                        }
                    }
                }

            MoreSubView.SETTINGS_DETAIL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    SettingsContent(
                        uiState = uiState,
                        playerState = playerState,
                        viewModel = viewModel,
                        horizontalPadding = horizontalPadding,
                        onOpenHelpSupport = { currentSubView = MoreSubView.HELP_SUPPORT }
                    )
                }
            }

            MoreSubView.ABOUT_DETAIL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("about_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "About V2S",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .clickable { currentSubView = MoreSubView.MAIN }
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 12.dp)
                        )
                    }

                    val shortsCount = if (uiState.shortsFeed.isNotEmpty()) uiState.shortsFeed.size else allVideos.size
                    AboutAppContent(
                        totalVideos = allVideos.size,
                        totalShorts = shortsCount,
                        totalSongs = allSongs.size,
                        totalFolders = folders.size,
                        hasStoragePermission = uiState.hasStoragePermission,
                        onNavigateToHelpSupport = { tab, category ->
                            helpSupportInitialTab = tab
                            helpSupportInitialCategory = category
                            currentSubView = MoreSubView.HELP_SUPPORT
                        },
                        onBackToMain = { currentSubView = MoreSubView.MAIN }
                    )
                }
            }

            MoreSubView.HELP_SUPPORT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("help_support_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Help & Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .clickable { currentSubView = MoreSubView.MAIN }
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 12.dp)
                        )
                    }

                    HelpSupportScreenContent(
                        horizontalPadding = horizontalPadding,
                        initialTab = helpSupportInitialTab,
                        initialCategory = helpSupportInitialCategory
                    )
                }
            }

            MoreSubView.EARPHONE_CONTROLS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("earphone_controls_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Earphone & Earbuds Controls",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .clickable { currentSubView = MoreSubView.MAIN }
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 12.dp)
                        )
                    }

                    EarphoneSettingsDetailScreen(
                        horizontalPadding = horizontalPadding,
                        onBack = { currentSubView = MoreSubView.MAIN }
                    )
                }
            }

            MoreSubView.HISTORY_ALL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Playback History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    AllHistoryContent(
                        videos = recentlyWatched,
                        songs = recentlyWatchedSongs,
                        onPlayVideo = { viewModel.openNormalPlayer(it) },
                        onPlaySong = { viewModel.playSong(it) },
                        onRemoveVideoFromHistory = { viewModel.removeVideoFromHistory(it) },
                        onRemoveSongFromHistory = { viewModel.removeSongFromHistory(it) },
                        onClearAllHistory = { viewModel.clearAllPlaybackHistory() },
                        onClearSongHistory = { viewModel.clearAllSongHistory() },
                        onClearVideoHistory = { viewModel.clearAllVideoHistory() },
                        onOpenHelpSupport = { currentSubView = MoreSubView.HELP_SUPPORT }
                    )
                }
            }

            MoreSubView.STREAMS_ALL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentSubView = MoreSubView.MAIN
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Network Streams",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = horizontalPadding,
                            vertical = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Button(
                                onClick = {
                                    showNewStreamDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Add, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Add New Stream",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(customStreams) { stream ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF141416),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val dummyVid = VideoItem(
                                            id = 999991L,
                                            uri = Uri.parse(stream.url),
                                            title = stream.title,
                                            displayName = stream.title,
                                            durationMs = 0L,
                                            sizeBytes = 0L,
                                            width = 1920,
                                            height = 1080,
                                            dateAddedSec =
                                                System.currentTimeMillis() / 1000,
                                            dateModifiedSec =
                                                System.currentTimeMillis() / 1000,
                                            isDemoSample = false
                                        )
                                        viewModel.openNormalPlayer(dummyVid)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Cast,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            stream.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            stream.url,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // New Network Stream Dialog
        if (showNewStreamDialog) {
            AlertDialog(
                onDismissRequest = {
                    streamUrlError = null
                    showNewStreamDialog = false
                },
                containerColor = Color(0xFF1C1C1E),
                title = {
                    Text(
                        text = "Open Network Stream",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Enter any RTSP, RTMP, HLS, YouTube, or HTTP stream link:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = streamTitleInput,
                            onValueChange = {
                                streamTitleInput = it
                            },
                            placeholder = {
                                Text(
                                    "Stream Title (Optional)",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF3A3A3C)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = streamUrlInput,
                            onValueChange = {
                                streamUrlInput = it
                                streamUrlError = null
                            },
                            placeholder = {
                                Text(
                                    "https://, rtsp://, rtmp://",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            isError = streamUrlError != null,
                            supportingText = streamUrlError?.let { err ->
                                { Text(err, color = Color(0xFFFF6B6B)) }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF3A3A3C)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val validation = com.example.util.MediaLinkUtils.validateMediaUrl(streamUrlInput)
                            if (!validation.valid) {
                                streamUrlError = validation.errorMessage ?: "Please enter a valid video or stream link."
                                return@Button
                            }
                            val urlClean = validation.normalizedUrl
                            val inferredType = com.example.util.MediaLinkUtils.inferMediaType(urlClean)
                            val title = streamTitleInput.trim().ifBlank {
                                com.example.util.MediaLinkUtils.inferDefaultTitle(urlClean, inferredType)
                            }

                            val newStream = StreamItem(
                                id = "stream_${System.currentTimeMillis()}",
                                title = title,
                                url = urlClean,
                                subtitle = urlClean
                            )

                            customStreams = listOf(newStream) + customStreams
                            streamUrlError = null
                            showNewStreamDialog = false
                            streamUrlInput = ""
                            streamTitleInput = ""

                            viewModel.addUserMediaLink(
                                url = urlClean,
                                title = title,
                                mediaType = inferredType,
                                artistOrSource = ""
                            )

                            val dummyVid = VideoItem(
                                id = -System.currentTimeMillis(),
                                uri = Uri.parse(newStream.url),
                                title = newStream.title,
                                displayName = newStream.title,
                                durationMs = 0L,
                                sizeBytes = 0L,
                                width = 1920,
                                height = 1080,
                                dateAddedSec = System.currentTimeMillis() / 1000,
                                dateModifiedSec = System.currentTimeMillis() / 1000,
                                isDemoSample = false
                            )

                            viewModel.openNormalPlayer(dummyVid)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            "Play",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            streamUrlError = null
                            showNewStreamDialog = false
                        }
                    ) {
                        Text(
                            "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}
}

@Composable
private fun SettingsContent(
    uiState: com.example.ui.viewmodel.VyloUiState,
    playerState: com.example.player.PlayerUiState,
    viewModel: MainViewModel,
    horizontalPadding: Dp,
    onOpenHelpSupport: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Appearance / Theme
        item {
            SettingsCard(title = "Appearance & Theme") {
                ThemeOptionRow(
                    title = "Dark Theme (Midnight)",
                    subtitle = "Deep dark contrast for video playback",
                    icon = Icons.Filled.Nightlight,
                    selected = uiState.themeMode == ThemeMode.DARK,
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.DARK)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                ThemeOptionRow(
                    title = "Light Theme (Amber)",
                    subtitle = "Clean light interface with yellow accents",
                    icon = Icons.Filled.WbSunny,
                    selected = uiState.themeMode == ThemeMode.LIGHT,
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.LIGHT)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                ThemeOptionRow(
                    title = "Follow System",
                    subtitle = "Sync with Android device settings",
                    icon = Icons.Filled.BrightnessMedium,
                    selected = uiState.themeMode == ThemeMode.SYSTEM,
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.SYSTEM)
                    }
                )
            }
        }

        // 2. Accent color
        item {
            SettingsCard(title = "Accent Color") {
                Text(
                    text = "Choose your accent color. It updates buttons, sliders, navigation and player controls.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(AccentColor.values().toList(), key = { it.name }) { accent ->
                        val selected = uiState.accentColor == accent
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(accent.primary)
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(accent) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected ${accent.label}",
                                        tint = Color.White
                                    )
                                }
                            }
                            Text(
                                text = accent.label,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                Text(
                    text = uiState.accentColor.label,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 3. Text & Font Size
        item {
            FontSizeSettingsCard(
                fontScale = uiState.fontScale,
                onFontScaleChanged = viewModel::setFontScale,
                onReset = viewModel::resetFontScale
            )
        }

        // 3. Playback Engine
        item {
            SettingsCard(title = "Global Playback Settings") {
                SwitchSettingRow(
                    title = "Hardware Acceleration (HW+)",
                    subtitle = "One shared player setting for Videos, Songs and Shorts",
                    icon = Icons.Filled.Memory,
                    checked = uiState.hardwareAcceleration,
                    onCheckedChange = {
                        viewModel.toggleHardwareAcceleration()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                SwitchSettingRow(
                    title = "Background Media Playback",
                    subtitle = "Keep audio and video playing with VLC-style background controls",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    checked = playerState.isBackgroundPlayEnabled,
                    onCheckedChange = {
                        viewModel.playerManager.toggleBackgroundPlay()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                SwitchSettingRow(
                    title = "Automatic Open with Shorts",
                    subtitle = "Always open clicked videos in Shorts vertical reel view",
                    icon = Icons.Filled.Bolt,
                    checked = uiState.autoOpenWithShorts,
                    onCheckedChange = {
                        viewModel.toggleAutoOpenWithShorts()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                SwitchSettingRow(
                    title = "Auto-Advance Shorts Feed",
                    subtitle = "Automatically scroll to next short when finished",
                    icon = Icons.Filled.PlayCircle,
                    checked = uiState.autoAdvanceShorts,
                    onCheckedChange = {
                        viewModel.toggleAutoAdvanceShorts()
                    }
                )
            }
        }

        // Useful player defaults already supported by the media engine.
        item {
            SettingsCard(title = "Player Defaults") {
                SettingChoiceRow(
                    title = "Playback speed",
                    subtitle = "Default speed for new playback",
                    icon = Icons.Filled.Speed,
                    options = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f),
                    selected = playerState.playbackSpeed,
                    label = { "${it}x" },
                    onSelected = viewModel.playerManager::setPlaybackSpeed
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
                SwitchSettingRow(
                    title = "Shuffle by default",
                    subtitle = "Keep shuffle enabled for playlist playback",
                    icon = Icons.Filled.Shuffle,
                    checked = playerState.isShuffle,
                    onCheckedChange = { viewModel.playerManager.toggleShuffle() }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
                SettingChoiceRow(
                    title = "Repeat mode",
                    subtitle = "Choose how the current queue repeats",
                    icon = Icons.Filled.Repeat,
                    options = listOf(
                        androidx.media3.common.Player.REPEAT_MODE_OFF,
                        androidx.media3.common.Player.REPEAT_MODE_ONE,
                        androidx.media3.common.Player.REPEAT_MODE_ALL
                    ),
                    selected = playerState.repeatMode,
                    label = { mode ->
                        when (mode) {
                            androidx.media3.common.Player.REPEAT_MODE_ONE -> "One"
                            androidx.media3.common.Player.REPEAT_MODE_ALL -> "All"
                            else -> "Off"
                        }
                    },
                    onSelected = viewModel.playerManager::setRepeatMode
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
                SwitchSettingRow(
                    title = "Allow multiple playback",
                    subtitle = if (uiState.multiplePlaybackEnabled)
                        "New audio/video can play while existing media continues"
                    else
                        "Recommended: only one audio/video plays at a time",
                    icon = Icons.Filled.PlayCircle,
                    checked = uiState.multiplePlaybackEnabled,
                    onCheckedChange = viewModel::setMultiplePlaybackEnabled
                )
            }
        }

        // Lyrics
        item {
            SettingsCard(title = "Lyrics") {
                SwitchSettingRow(
                    title = "Show Lyrics",
                    subtitle = "Open predefined lyrics for supported songs",
                    icon = Icons.Filled.GraphicEq,
                    checked = uiState.lyricsEnabled,
                    onCheckedChange = viewModel::setLyricsEnabled
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
                SwitchSettingRow(
                    title = "Sync Lyrics",
                    subtitle = "Highlight timestamped predefined lyrics while the song plays",
                    icon = Icons.Filled.PlayArrow,
                    checked = uiState.syncLyrics,
                    onCheckedChange = viewModel::setSyncLyrics
                )
            }
        }

        // 3. Gesture Guide
        item {
            SettingsCard(title = "PLAYit-style Gesture Controls") {
                GestureInfoRow(
                    title = "Left Screen Vertical Swipe",
                    description = "Slide up/down on left side to adjust screen brightness (0 - 100%)",
                    icon = Icons.Filled.WbSunny
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                GestureInfoRow(
                    title = "Right Screen Vertical Swipe",
                    description = "Slide up/down on right side to change the Android phone media volume",
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                GestureInfoRow(
                    title = "Horizontal Swipe",
                    description = "Slide left/right anywhere to seek forward/backward with real-time target timestamp",
                    icon = Icons.Filled.Speed
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                GestureInfoRow(
                    title = "Pinch to Zoom",
                    description = "Pinch open or close on player screen to switch between Best Fit, Fill Screen, and 100% Stretch",
                    icon = Icons.Filled.GraphicEq
                )
            }
        }

        // 4. Storage & Cache
        item {
            SettingsCard(title = "Library Sources") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Phone Memory & Saved Links",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Scans videos and audio directly from phone storage and plays your locally saved links. No default sample media is loaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.refreshMediaScan() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Rescan Phone Memory", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOpenHelpSupport()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Column {
                            Text(
                                text = "Help & Support / Report & Suggest",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Report errors, request features, or view FAQs & Troubleshooting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Help & Support",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSizeSettingsCard(
    fontScale: Float,
    onFontScaleChanged: (Float) -> Unit,
    onReset: () -> Unit
) {
    SettingsCard(title = "Text & Font Size") {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FormatSize,
                            contentDescription = "Text size",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Font size",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Adjust text size across the entire app",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Text(
                    text = "${(fontScale * 100).roundToInt()}%",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // Preset Options: Small, Normal, Large, Extra Large
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "Small" to 0.85f,
                    "Normal" to 1.00f,
                    "Large" to 1.15f,
                    "X-Large" to 1.30f
                )

                presets.forEach { (label, scale) ->
                    val isSelected = kotlin.math.abs(fontScale - scale) < 0.04f
                    Surface(
                        onClick = { onFontScaleChanged(scale) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Continuous font-size slider. 50% = minimum, 100% = normal, 150% = maximum.
            Slider(
                value = fontScale.coerceIn(0.50f, 1.50f),
                onValueChange = onFontScaleChanged,
                valueRange = 0.50f..1.50f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "50%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "100% Normal",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "150%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Controlled exclusively inside app • Ignores phone OS scaling",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Preview",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "V2S — Videos, Shorts & Songs",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "This preview changes with your selected font size.",
                        color = Color(0xFFB0B0B5),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Reset to Normal (100%)",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Recently"
    val diffMs = System.currentTimeMillis() - timestamp
    val diffSec = diffMs / 1000
    val diffMin = diffSec / 60
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24
    return when {
        diffSec < 60 -> "Just now"
        diffMin < 60 -> "${diffMin}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@Composable
private fun HistorySection(
    title: String,
    icon: ImageVector,
    videos: List<VideoItem>,
    totalCount: Int = videos.size,
    onPlay: (VideoItem) -> Unit,
    onViewAll: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAll)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (totalCount > 0) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$totalCount",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View all",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "View all", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        if (videos.isEmpty()) {
            Text("No video history yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(videos, key = { it.id }) { video ->
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onPlay(video) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))) {
                                VideoThumbnail(video, Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = formatDuration(video.durationMs),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = video.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatRelativeTime(video.lastPlayedTimestamp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongHistorySection(
    title: String,
    songs: List<SongItem>,
    totalCount: Int = songs.size,
    onPlay: (SongItem) -> Unit,
    onViewAll: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAll)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GraphicEq, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (totalCount > 0) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$totalCount",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View all",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "View all", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        if (songs.isEmpty()) {
            Text("No song history yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(songs, key = { it.id }) { song ->
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onPlay(song) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    .background(Color(0xFF1C1917))
                            ) {
                                Icon(
                                    Icons.Filled.GraphicEq,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp).align(Alignment.Center)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = formatDuration(song.durationMs),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = song.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatRelativeTime(song.lastPlayedTimestamp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpAndSupportSection(
    onOpenHelpSupport: () -> Unit
) {
    val context = LocalContext.current
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenHelpSupport)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Help & Support",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "HELP DESK",
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Open desk",
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open Help Desk",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Action Cards Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Report a Bug
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenHelpSupport),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF141A22),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE63946).copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.BugReport,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Report Bug",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Report playback, video, or audio glitches",
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E),
                        lineHeight = 15.sp
                    )
                }
            }

            // Feature Suggestion
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenHelpSupport),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF141A22),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFB703).copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Suggest Feature",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Request new tools, codecs, or UI enhancements",
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Contact Support Email Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F1720),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Direct Developer Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = SUPPORT_RECIPIENT_EMAIL,
                            fontSize = 11.sp,
                            color = Color(0xFF58A6FF)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_RECIPIENT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "[V2S Support] Inquiry / Assistance")
                            }
                            context.startActivity(Intent.createChooser(intent, "Contact Support..."))
                        } catch (e: Exception) {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Support Email", SUPPORT_RECIPIENT_EMAIL))
                            Toast.makeText(context, "Email copied: $SUPPORT_RECIPIENT_EMAIL", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1E2630),
                        contentColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick FAQs Card
        val quickFaqs = listOf(
            "How do I play audio/video in the background?" to "Tap the 3-dot menu on any song or video and choose 'Play as Audio'. Media will continue playing via the persistent mini-player and notification even when screen is locked.",
            "How to manage Song History and clear records?" to "Songs you play are automatically logged in Song History. You can clear them from Songs > History tab or More > Playback History, or remove individual tracks via their 3-dot menus.",
            "Supported formats & codec decoding?" to "V2S supports hardware decoding for MP4, MKV, AVI, WEBM, MOV, FLV, MP3, FLAC, WAV, AAC, M4A, OGG, OPUS with up to 4K resolution."
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF141416),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QuestionMark,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Quick FAQs & Troubleshooting",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "FAQs",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onOpenHelpSupport)
                    )
                }

                Spacer(Modifier.height(8.dp))

                quickFaqs.forEachIndexed { index, (question, answer) ->
                    val isExpanded = expandedFaqIndex == index
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpanded) Color(0xFF1A222C) else Color(0xFF1C1C1E))
                            .clickable {
                                expandedFaqIndex = if (isExpanded) -1 else index
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = question,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isExpanded) primaryColor else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = if (isExpanded) primaryColor else Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (isExpanded) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = answer,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFFC9D1D9)
                            )
                        }
                    }
                }
            }
        }

        // Full Help Desk Button
        Button(
            onClick = onOpenHelpSupport,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Headphones,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Open Full Help & Support Desk",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AllHistoryContent(
    videos: List<VideoItem>,
    songs: List<SongItem>,
    onPlayVideo: (VideoItem) -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onRemoveVideoFromHistory: (VideoItem) -> Unit = {},
    onRemoveSongFromHistory: (SongItem) -> Unit = {},
    onClearAllHistory: () -> Unit = {},
    onClearSongHistory: () -> Unit = {},
    onClearVideoHistory: () -> Unit = {},
    onOpenHelpSupport: () -> Unit = {}
) {
    var selectedHistoryTab by remember { mutableIntStateOf(0) } // 0: All, 1: Songs, 2: Videos
    var searchQuery by remember { mutableStateOf("") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) songs
        else songs.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    val filteredVideos = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) videos
        else videos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Playback History", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select which history you would like to clear:", color = Color(0xFFC9D1D9), fontSize = 14.sp)
                    Button(
                        onClick = {
                            onClearSongHistory()
                            showClearHistoryDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2630), contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Songs History (${songs.size})", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            onClearVideoHistory()
                            showClearHistoryDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2630), contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Videos History (${videos.size})", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            onClearAllHistory()
                            showClearHistoryDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1818), contentColor = Color(0xFFFF6B6B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear All Playback History (${songs.size + videos.size})", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = Color(0xFF8B949E))
                }
            },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(14.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Search in playback history...", color = Color(0xFF8B949E), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF8B949E)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = Color(0xFF8B949E))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF141416),
                unfocusedContainerColor = Color(0xFF141416)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Tab Row
        val historyTabs = listOf(
            "All (${filteredSongs.size + filteredVideos.size})",
            "Songs (${filteredSongs.size})",
            "Videos (${filteredVideos.size})"
        )

        TabRow(
            selectedTabIndex = selectedHistoryTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedHistoryTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            historyTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedHistoryTab == index,
                    onClick = { selectedHistoryTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedHistoryTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selectedHistoryTab == index) MaterialTheme.colorScheme.primary else Color(0xFF8B949E)
                        )
                    }
                )
            }
        }

        // Action header (Clear history, item summary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedHistoryTab) {
                    1 -> "${filteredSongs.size} Songs Played"
                    2 -> "${filteredVideos.size} Videos Watched"
                    else -> "${filteredSongs.size + filteredVideos.size} Recent Items"
                },
                color = Color(0xFF8B949E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            if (filteredSongs.isNotEmpty() || filteredVideos.isNotEmpty()) {
                TextButton(
                    onClick = { showClearHistoryDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear History", tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear History", color = Color(0xFFFF6B6B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // History Content Lists
        if (filteredSongs.isEmpty() && filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No history matches \"$searchQuery\"" else "No Playback History Yet",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Songs and videos you play in V2S will be automatically recorded here.",
                        color = Color(0xFF8B949E),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onOpenHelpSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141922), contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Help & Support", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // When Tab is ALL or SONGS
                if (selectedHistoryTab == 0 || selectedHistoryTab == 1) {
                    if (selectedHistoryTab == 0 && filteredSongs.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Songs History (${filteredSongs.size})", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    items(filteredSongs, key = { "song_${it.id}" }) { song ->
                        HistorySongRowItem(
                            song = song,
                            onPlay = { onPlaySong(song) },
                            onRemove = { onRemoveSongFromHistory(song) },
                            onOpenSupport = onOpenHelpSupport
                        )
                    }
                }

                // When Tab is ALL or VIDEOS
                if (selectedHistoryTab == 0 || selectedHistoryTab == 2) {
                    if (selectedHistoryTab == 0 && filteredVideos.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.VideoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Video History (${filteredVideos.size})", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    items(filteredVideos, key = { "video_${it.id}" }) { video ->
                        HistoryVideoRowItem(
                            video = video,
                            onPlay = { onPlayVideo(video) },
                            onRemove = { onRemoveVideoFromHistory(video) },
                            onOpenSupport = onOpenHelpSupport
                        )
                    }
                }

                // Help & Support banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clickable { onOpenHelpSupport() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF101922),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Questions about History or Background Play?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Visit Help & Support for FAQs & Support", color = Color(0xFF8B949E), fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun HistorySongRowItem(
    song: SongItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onOpenSupport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF161B22),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D1B2A),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = song.artist.ifBlank { "Unknown Artist" },
                            color = Color(0xFF8B949E),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•", color = Color(0xFF484F58), fontSize = 12.sp)
                        Text(
                            text = formatDuration(song.durationMs),
                            color = Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                        Text("•", color = Color(0xFF484F58), fontSize = 12.sp)
                        Text(
                            text = formatRelativeTime(song.lastPlayedTimestamp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlay) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1C2128))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Song", color = Color.White) },
                            leadingIcon = { Icon(Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onPlay()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from History", color = Color(0xFFFF6B6B)) },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFFF6B6B)) },
                            onClick = {
                                showMenu = false
                                onRemove()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Help & Support", color = Color.White) },
                            leadingIcon = { Icon(Icons.Filled.Headphones, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onOpenSupport()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryVideoRowItem(
    video: VideoItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onOpenSupport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF161B22),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 44.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    VideoThumbnail(video = video, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = formatDuration(video.durationMs),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (video.width > 0 && video.height > 0) {
                            Text(
                                text = "${video.width}x${video.height}",
                                color = Color(0xFF8B949E),
                                fontSize = 11.sp
                            )
                            Text("•", color = Color(0xFF484F58), fontSize = 11.sp)
                        }
                        Text(
                            text = formatRelativeTime(video.lastPlayedTimestamp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlay) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1C2128))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Video", color = Color.White) },
                            leadingIcon = { Icon(Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onPlay()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from History", color = Color(0xFFFF6B6B)) },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFFF6B6B)) },
                            onClick = {
                                showMenu = false
                                onRemove()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Help & Support", color = Color.White) },
                            leadingIcon = { Icon(Icons.Filled.Headphones, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onOpenSupport()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class V2SLogoItem(
    val title: String,
    val subtitle: String,
    val tag: String,
    val drawableRes: Int,
    val tagColor: Color,
    val isSquareAspect: Boolean = false
)

@Composable
private fun AboutAppContent(
    totalVideos: Int,
    totalShorts: Int,
    totalSongs: Int,
    totalFolders: Int,
    hasStoragePermission: Boolean,
    onNavigateToHelpSupport: (SupportTab, FeedbackCategory) -> Unit = { _, _ -> },
    onBackToMain: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLibrariesDialog by remember { mutableStateOf(false) }
    var showAuthorsDialog by remember { mutableStateOf(false) }
    var showEngineeredFeaturesDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTrademarkDisclaimerDialog by remember { mutableStateOf(false) }
    var showDmcaDialog by remember { mutableStateOf(false) }
    var showSourceCodeDialog by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    val accentPrimary = MaterialTheme.colorScheme.primary

    val logoItems = remember(accentPrimary) {
        listOf(
            V2SLogoItem(
                title = "V2S Signature Cone",
                subtitle = "Glossy orange cone with bold black V2S typography & black base",
                tag = "BLACK V2S CONE",
                drawableRes = R.drawable.v2s_cone_black_text_1787004587055,
                tagColor = Color(0xFFFFB703),
                isSquareAspect = true
            ),
            V2SLogoItem(
                title = "3D Cinema Master Banner",
                subtitle = "Metallic 3D V2S logo with chrome filmstrip & black-text cone",
                tag = "OFFICIAL BANNER",
                drawableRes = R.drawable.v2s_hero_black_cone_1787004603846,
                tagColor = accentPrimary,
                isSquareAspect = false
            ),
            V2SLogoItem(
                title = "Futuristic Neon & Gold Emblem",
                subtitle = "Luminous soundwave rings & laser glow emblem badge",
                tag = "SPECIAL EDITION",
                drawableRes = R.drawable.v2s_emblem_logo_1787004615786,
                tagColor = Color(0xFFFFC300),
                isSquareAspect = false
            ),
            V2SLogoItem(
                title = "3D Studio Cinematic Artwork",
                subtitle = "Ultra high-resolution widescreen player visual poster",
                tag = "STUDIO RENDER",
                drawableRes = R.drawable.v2s_about_hero_1787004202141,
                tagColor = Color(0xFFFF8500),
                isSquareAspect = false
            ),
            V2SLogoItem(
                title = "Studio 3D Cone Edition",
                subtitle = "Classic glossy traffic cone studio lighting render",
                tag = "3D CONE",
                drawableRes = R.drawable.v2s_cone_glossy_1787004181216,
                tagColor = Color(0xFFFF9E00),
                isSquareAspect = true
            )
        )
    }

    var selectedLogoIndex by remember { mutableIntStateOf(0) }
    val logoListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. MULTIPLE LOGOS INTERACTIVE CAROUSEL (SWIPE / SCROLL LEFT & RIGHT)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gallery Header with Counter & Navigation Arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "V2S Brand Gallery",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Left & Right Quick Navigation Buttons & Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF22170F),
                            border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${selectedLogoIndex + 1} / ${logoItems.size}",
                                color = Color(0xFFFFB703),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedLogoIndex > 0) {
                                    selectedLogoIndex--
                                    coroutineScope.launch {
                                        logoListState.animateScrollToItem(selectedLogoIndex)
                                    }
                                }
                            },
                            enabled = selectedLogoIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Logo",
                                tint = if (selectedLogoIndex > 0) Color.White else Color(0xFF555555),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedLogoIndex < logoItems.size - 1) {
                                    selectedLogoIndex++
                                    coroutineScope.launch {
                                        logoListState.animateScrollToItem(selectedLogoIndex)
                                    }
                                }
                            },
                            enabled = selectedLogoIndex < logoItems.size - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Logo",
                                tint = if (selectedLogoIndex < logoItems.size - 1) Color.White else Color(0xFF555555),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scrollable Horizontal Carousel of Logos
                LazyRow(
                    state = logoListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                        count = logoItems.size,
                        key = { index -> logoItems[index].title }
                    ) { index ->
                        val item = logoItems[index]
                        val isSelected = selectedLogoIndex == index

                        Column(
                            modifier = Modifier
                                .width(300.dp)
                                .clickable {
                                    selectedLogoIndex = index
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(175.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF14100C))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) item.tagColor else Color(0xFF2C2219),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(item.drawableRes),
                                    contentDescription = item.title,
                                    contentScale = if (item.isSquareAspect) ContentScale.Fit else ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Badge Tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(1.dp, item.tagColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFF00E676) else item.tagColor)
                                        )
                                        Text(
                                            text = item.tag,
                                            color = item.tagColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.title,
                                color = if (isSelected) Color(0xFFFFB703) else Color(0xFFCCCCCC),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = item.subtitle,
                                color = Color(0xFF888888),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    logoItems.indices.forEach { index ->
                        val active = selectedLogoIndex == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (active) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) Color(0xFFFFB703) else Color(0xFF33251A))
                                .clickable {
                                    selectedLogoIndex = index
                                    coroutineScope.launch {
                                        logoListState.animateScrollToItem(index)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Capability Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "🎬 HD Video" to MaterialTheme.colorScheme.primary,
                        "⚡ Shorts" to Color(0xFFFFB703),
                        "🎵 Hi-Res Audio" to Color(0xFFFF8500),
                        "📡 Streams" to Color(0xFFFFC300)
                    ).forEach { (label, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E1712))
                                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "V2S for Android is a high-performance multimedia engine for Videos, Shorts, and Songs. Featuring hardware acceleration, custom gestures, and seamless playback.",
                    color = Color(0xFF9E9EA4),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // 2. VERSION & BUILD STATUS CARD
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF181512),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 18.dp,
                            vertical = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Version 3.6.5",
                            color = Color(0xFFFFB703),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO BUILD",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "2026-08-17",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2B. MEDIA LIBRARY & CONTENT TOTALS
        item {
            SettingsCard(title = "Media Library & Content Totals") {
                StatRow(
                    "Total Videos",
                    "$totalVideos videos"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Shorts",
                    "$totalShorts shorts"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Songs (Audio)",
                    "$totalSongs songs"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Folders",
                    "$totalFolders directories"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Indexed Items",
                    "${totalVideos + totalSongs} files"
                )
            }
        }

        // 3. STORAGE, DOWNLOAD SIZE & MEMORY FOOTPRINT SPECS
        item {
            var liveMemoryMb by remember {
                val rt = Runtime.getRuntime()
                val usedBytes = rt.totalMemory() - rt.freeMemory()
                mutableStateOf(String.format(java.util.Locale.US, "%.1f MB", usedBytes / (1024.0 * 1024.0)))
            }
            var liveCacheMb by remember {
                var size = 0L
                try {
                    context.cacheDir?.walkTopDown()?.forEach { if (it.isFile) size += it.length() }
                } catch (_: Exception) {}
                mutableStateOf(String.format(java.util.Locale.US, "%.2f MB", (size.coerceAtLeast(1024 * 350)) / (1024.0 * 1024.0)))
            }

            SettingsCard(title = "App Size & Memory Footprint") {
                StatRow(
                    "Download Size (APK)",
                    "18.2 MB"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Installed App Size",
                    "28.4 MB"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Active RAM Usage",
                    liveMemoryMb
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Cache & Temp Storage",
                    liveCacheMb
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Required Storage to Download",
                    "~35 MB Free Space"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Performance Mode",
                    "Hardware Accel • Eco RAM"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        try {
                            context.cacheDir?.deleteRecursively()
                        } catch (_: Exception) {}
                        System.gc()
                        val rt = Runtime.getRuntime()
                        val usedBytes = rt.totalMemory() - rt.freeMemory()
                        liveMemoryMb = String.format(java.util.Locale.US, "%.1f MB", usedBytes / (1024.0 * 1024.0))
                        liveCacheMb = "0.15 MB"
                        Toast.makeText(context, "⚡ RAM and Cache Optimized Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22170F),
                        contentColor = Color(0xFFFFB703)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clean Cache & Free Memory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // 4. ABOUT LINKS & NAVIGATION
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Highlight Banner for Custom Engineered Features
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showEngineeredFeaturesDialog = true },
                    color = Color(0xFF1F1A14),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Engineered Features in V2S",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "EXCLUSIVE",
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Shorts player, LRC lyrics, 5-band EQ, dual gesture HUD",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AboutLinkRow(
                    icon = Icons.Filled.Feedback,
                    title = "Send Feedback & Suggestions",
                    onClick = {
                        onNavigateToHelpSupport(
                            SupportTab.REPORT_AND_SUGGEST,
                            FeedbackCategory.GENERAL_FEEDBACK
                        )
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.BugReport,
                    title = "Report Bug or Glitch",
                    onClick = {
                        onNavigateToHelpSupport(
                            SupportTab.REPORT_AND_SUGGEST,
                            FeedbackCategory.BUG_REPORT
                        )
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Lightbulb,
                    title = "Suggest Feature / Enhancement",
                    onClick = {
                        onNavigateToHelpSupport(
                            SupportTab.REPORT_AND_SUGGEST,
                            FeedbackCategory.FEATURE_REQUEST
                        )
                    }
                )

                AboutLinkRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help Desk & Troubleshooting FAQs",
                    onClick = {
                        onNavigateToHelpSupport(
                            SupportTab.FAQ_AND_TROUBLESHOOTING,
                            FeedbackCategory.BUG_REPORT
                        )
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Lock,
                    title = "Privacy Policy (100% On-Device)",
                    onClick = {
                        showPrivacyPolicyDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Info,
                    title = "Trademark & Legal Licensing Notice",
                    onClick = {
                        showTrademarkDisclaimerDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Email,
                    title = "Designated DMCA Compliance Agent",
                    onClick = {
                        showDmcaDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Code,
                    title = "Source Code Availability (GPL/LGPL)",
                    onClick = {
                        showSourceCodeDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Code,
                    title = "Open Source Libraries",
                    onClick = {
                        showLibrariesDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Lightbulb,
                    title = "Inspirations & Architectural Credits",
                    onClick = {
                        showCreditsDialog = true
                    }
                )

                AboutLinkRow(
                    icon = Icons.Filled.Group,
                    title = "Authors & Contributors",
                    onClick = {
                        showAuthorsDialog = true
                    }
                )
            }
        }

        // 4. MEDIA ENGINE SUMMARY
        item {
            SettingsCard(title = "Media Engine Summary") {
                StatRow(
                    "Total Videos",
                    "$totalVideos files"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Shorts",
                    "$totalShorts clips"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Total Songs / Audio",
                    "$totalSongs tracks"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Media Folders",
                    "$totalFolders directories"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )

                StatRow(
                    "Storage Access",
                    if (hasStoragePermission) {
                        "Granted (Read External Storage)"
                    } else {
                        "Demo Mode"
                    }
                )
            }
        }

        // 5. COPYRIGHT & TRADEMARK COMPLIANCE NOTICE
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF181512),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "V2S Media Player",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Copyright © 2024–2026 V2S Media Player Authors & Open Source Contributors.\n" +
                            "Licensed under GNU General Public License (GPL) v2.0 / v3.0, LGPL v2.1 & Apache License 2.0.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Text(
                        text = "Trademark & Reference Disclaimer: VLC® and VideoLAN are registered trademarks of VideoLAN non-profit organization. PLAYit is a trademark/product of its respective owner. V2S is an independent Android application built with AndroidX Media3 / ExoPlayer, Jetpack Compose, and Room, engineered with inspirations and reference workflows from VLC media player and PLAYit. V2S is not affiliated with, sponsored by, or endorsed by VideoLAN or PLAYit.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "Offline Player Utility: V2S does not host, upload, or distribute copyrighted media. For IP inquiries, contact derp.digital.erp@gmail.com.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBackToMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9E00),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to More Menu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DIALOG: Engineered Features in V2S
    if (showEngineeredFeaturesDialog) {
        AlertDialog(
            onDismissRequest = { showEngineeredFeaturesDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Engineered Features in V2S",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureHighlightItem(
                        icon = Icons.Filled.PlayCircle,
                        title = "Vertical Shorts & Reels Feed",
                        desc = "Continuous full-screen vertical swipe video feed with auto-loop replay, fast gesture navigation, and instant audio extraction."
                    )

                    FeatureHighlightItem(
                        icon = Icons.Filled.MusicNote,
                        title = "Synchronized LRC Lyrics Engine",
                        desc = "Real-time syllable and line-by-line karaoke lyrics display synchronized with audio playback and manual timing offset calibration."
                    )

                    FeatureHighlightItem(
                        icon = Icons.Filled.GraphicEq,
                        title = "5-Band Studio Equalizer",
                        desc = "Hardware-accelerated DSP audio engine with 10 presets, Bass Boost, 3D Surround Virtualizer, and customized frequency band tuning."
                    )

                    FeatureHighlightItem(
                        icon = Icons.Filled.BrightnessMedium,
                        title = "PLAYit-Style Dual Gesture HUD",
                        desc = "Left vertical swipe for brightness, right vertical swipe for volume with instant on-screen meters, and horizontal gesture seeking."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEngineeredFeaturesDialog = false }) {
                    Text(
                        "Got It",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DIALOG: Privacy Policy & 100% On-Device Guarantee
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Privacy Policy & Guarantee",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "🔒 100% On-Device Architecture",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "V2S operates exclusively on your device. We do not collect, transmit, store, or sell any personal data, playback logs, or media metadata.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📊 Zero Telemetry & Analytics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "No tracking SDKs, ad networks, or background telemetry services are included. Your viewing and listening habits remain 100% private.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📁 Storage Permission Usage",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Permissions are requested solely to scan and play video and audio files stored on your local device.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(
                        "Understood",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DIALOG: Trademark & Legal Licensing Notice
    if (showTrademarkDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showTrademarkDisclaimerDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    "Trademark & Legal Notice",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "⚖️ Trademark & Attribution Notice",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "VLC® and VideoLAN are registered trademarks of VideoLAN non-profit organization. PLAYit is the trademark/product of its respective owner.\n\n" +
                            "V2S Media Player is an independently engineered Android application built on modern AndroidX Media3 / ExoPlayer, Jetpack Compose, and Room. It is not affiliated with, sponsored by, or endorsed by VideoLAN or PLAYit.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📜 Licensing Disclosures",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Licensed under GNU General Public License (GPL) v2.0 / v3.0, LGPL v2.1, and Apache License 2.0. In compliance with open source licensing, source code components are accessible to the public.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🛡️ Content Disclaimer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "V2S is an offline player utility for media files stored on the user's device. V2S does not host, stream, scrape, or distribute copyrighted media.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrademarkDisclaimerDialog = false }) {
                    Text(
                        "Close",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DIALOG: Designated DMCA & IP Compliance Agent
    if (showDmcaDialog) {
        AlertDialog(
            onDismissRequest = { showDmcaDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    "DMCA & IP Compliance",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Designated Compliance Agent",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "For intellectual property notices, licensing inquiries, or DMCA compliance requests, please reach out to our designated compliance desk:\n\n" +
                            "Email: derp.digital.erp@gmail.com\n" +
                            "Subject Tag: [V2S DMCA / IP Compliance Inquiry]\n\n" +
                            "All verified inquiries are processed promptly in accordance with applicable copyright laws.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:derp.digital.erp@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "[V2S DMCA / IP Compliance Inquiry]")
                                }
                                context.startActivity(Intent.createChooser(intent, "Email Compliance Agent"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Email: derp.digital.erp@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text(
                            "Send Email",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { showDmcaDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            }
        )
    }

    // DIALOG: Source Code Availability (GPL/LGPL)
    if (showSourceCodeDialog) {
        AlertDialog(
            onDismissRequest = { showSourceCodeDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    "Source Code Availability",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "GPL / LGPL Compliance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "In full compliance with GNU General Public License (GPL) and LGPL requirements, the source code and modifications for open-source modules are available upon request.\n\n" +
                            "You can request source archives or build instructions by emailing:\n" +
                            "derp.digital.erp@gmail.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:derp.digital.erp@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "[V2S Source Code Request under GPL/LGPL]")
                                    putExtra(Intent.EXTRA_TEXT, "Please provide source code instructions for V2S Media Player.")
                                }
                                context.startActivity(Intent.createChooser(intent, "Request Source Code"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Email: derp.digital.erp@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text(
                            "Request via Email",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { showSourceCodeDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            }
        )
    }

    // DIALOG: Open Source Libraries
    if (showLibrariesDialog) {
        AlertDialog(
            onDismissRequest = {
                showLibrariesDialog = false
            },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    "Open Source Libraries",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "• AndroidX Media3 / ExoPlayer 1.3.1 (Apache 2.0)\n" +
                            "• Jetpack Compose & Material 3 (Apache 2.0)\n" +
                            "• Kotlin Coroutines & StateFlow (Apache 2.0)\n" +
                            "• Coil-Compose Image Loader 2.6.0 (Apache 2.0)\n" +
                            "• AndroidX Room Database & KSP (Apache 2.0)\n" +
                            "• Navigation Compose (Apache 2.0)\n" +
                            "• AndroidX Activity & Core KTX (Apache 2.0)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLibrariesDialog = false
                    }
                ) {
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DIALOG: Authors & Contributors
    if (showAuthorsDialog) {
        AlertDialog(
            onDismissRequest = {
                showAuthorsDialog = false
            },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    "Authors & Contributors",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "V2S Media Player for Android is developed by V2S contributors and the global open source multimedia community.\n\n" +
                            "Special thanks to all open-source contributors and developers whose work powers modern mobile playback.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAuthorsDialog = false
                    }
                ) {
                    Text(
                        "Close",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DIALOG: Inspirations & Architectural Credits
    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Inspirations & Credits",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "V2S Media Player is crafted with inspiration and workflow references from two pioneer media applications:",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Surface(
                        color = Color(0xFF252528),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "🎬 VLC media player (VideoLAN)",
                                color = Color(0xFFFF9E00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Honoring the open-source media player by VideoLAN. V2S takes inspiration from VLC's versatile playback philosophy, multi-format media decoding standard, and robust audio-video pipeline.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF252528),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "⚡ PLAYit Video & Music Player",
                                color = Color(0xFFFF9E00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Honoring PLAYit's mobile-first user experience. V2S takes inspiration from intuitive dual-side vertical swipe gestures (brightness & volume HUDs), horizontal gesture seeking, floating PiP, and seamless video-to-audio transitions.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF252528),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "🚀 Original Engineering & Additions in V2S",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Built from scratch in Kotlin & Jetpack Compose on AndroidX Media3 / ExoPlayer, adding exclusive native features including vertical Shorts/Reels feeds, synchronized LRC syllable lyrics, customizable 5-band studio equalizer, and 100% on-device privacy.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Text(
                        "Disclaimer: VLC® is a registered trademark of VideoLAN. PLAYit is the trademark of its respective owner. V2S is an independent clean-room application developed from the ground up.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreditsDialog = false }) {
                    Text(
                        "Close",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = 8.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 6.dp
                )
            )

            content()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun <T> SettingChoiceRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { onSelected(option) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        label(option),
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureInfoRow(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun FeatureHighlightItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF25211B))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = desc,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
