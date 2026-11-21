package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val SUPPORT_RECIPIENT_EMAIL = "derp.digital.erp@gmail.com"

enum class SupportTab {
    REPORT_AND_SUGGEST,
    FAQ_AND_TROUBLESHOOTING
}

enum class FeedbackCategory(
    val title: String,
    val emoji: String,
    val icon: ImageVector
) {
    BUG_REPORT("Bug / Defect Report", "🐞", Icons.Filled.BugReport),
    FEATURE_REQUEST("Feature Request", "💡", Icons.Filled.Lightbulb),
    PLAYBACK_ISSUE("Audio & Video Playback", "🎧", Icons.Filled.Headphones),
    TECHNICAL_ISSUE("Technical / Device Issue", "⚙️", Icons.Filled.Settings),
    GENERAL_FEEDBACK("General Feedback & Rating", "💬", Icons.Filled.Feedback)
}

data class AttachedMediaItem(
    val uri: Uri,
    val name: String,
    val sizeText: String
)

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val icon: ImageVector,
    val category: String = "General"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelpSupportScreenContent(
    horizontalPadding: Dp,
    initialTab: SupportTab = SupportTab.REPORT_AND_SUGGEST,
    initialCategory: FeedbackCategory = FeedbackCategory.BUG_REPORT,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    // Report form state
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var subjectText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var userEmailText by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }
    var showDiagnosticLogPreview by remember { mutableStateOf(false) }

    val attachedFiles = remember { mutableStateListOf<AttachedMediaItem>() }

    // Thank you dialog state
    var showThankYouDialog by remember { mutableStateOf(false) }
    var submittedCategoryName by remember { mutableStateOf("") }
    var submittedSubject by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val fileName = getFileNameFromUri(context, uri) ?: "media_${System.currentTimeMillis()}"
            attachedFiles.add(
                AttachedMediaItem(
                    uri = uri,
                    name = fileName,
                    sizeText = "Attachment"
                )
            )
        }
    }

    val systemDiagnosticInfo = remember {
        buildString {
            appendLine("--- System & App Diagnostics ---")
            appendLine("App: V2S Media Player v2.4.0 (Build 2400)")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Android OS: Android ${Build.VERSION.RELEASE} (API Level ${Build.VERSION.SDK_INT})")
            appendLine("Board / Hardware: ${Build.BOARD} / ${Build.HARDWARE}")
            appendLine("Fingerprint: ${Build.FINGERPRINT}")
            appendLine("Playback Engine: ExoPlayer / Media3 Hardware Accelerated")
            appendLine("Supported Codecs: H.264, HEVC/H.265, AV1, VP9, AAC, MP3, FLAC, OPUS, WAV")
            appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
        }
    }

    val topicSuggestions = remember {
        listOf(
            "+ Song History not updating",
            "+ Clear song/video history",
            "+ Background playback stops",
            "+ Video scale & zoom issue",
            "+ Audio / Video out of sync",
            "+ Subtitle / Lyrics request",
            "+ Request new format/codec",
            "+ Gesture & Brightness control"
        )
    }

    val faqList = remember {
        listOf(
            FaqItem(
                id = "faq_song_history",
                question = "How does Song History & Playback History work in V2S?",
                answer = "Every song and video you play is automatically logged to your History with its last played timestamp and resume position. You can access your full song history under the 'History' tab in Songs or under More > Playback History. From there you can play songs directly, add them to playlists, or clear history.",
                icon = Icons.Filled.MusicNote,
                category = "Song & Playback History"
            ),
            FaqItem(
                id = "faq_clear_history",
                question = "How to clear or remove items from Song History?",
                answer = "In More > Playback History or in the Songs > History tab, tap 'Clear History' to wipe all past records. To remove a single song or video, tap its 3-dot action menu and select 'Remove from History'.",
                icon = Icons.Filled.Refresh,
                category = "Song & Playback History"
            ),
            FaqItem(
                id = "faq_bg_playback",
                question = "How to play video in background or as audio only?",
                answer = "Tap the 3-dot menu on any video in the library or playlist and choose 'Play as Audio'. Alternatively, tap the headphones icon in the player top/controls bar. The media will play via the persistent mini-player and continue in the background even when turning off the screen or switching apps.",
                icon = Icons.Filled.Headphones,
                category = "Audio & Playback"
            ),
            FaqItem(
                id = "faq_pinch_zoom",
                question = "How to use 2-Finger Zoom, Pan & Aspect Ratio modes?",
                answer = "Inside the Video Player, pinch open with two fingers to zoom in up to 400%, and pinch close to zoom out. Drag with two fingers while zoomed to pan across the video frame. Tap the Aspect Ratio button (Scale icon) to instantly cycle between Fit (Original), Fill Screen, and Zoom/Crop modes.",
                icon = Icons.Filled.ZoomIn,
                category = "Video Controls"
            ),
            FaqItem(
                id = "faq_playlists",
                question = "How to create and manage mixed video & audio playlists?",
                answer = "Go to the Playlists tab and tap '+ Create Playlist'. You can add both videos and songs to the same playlist. Use the multi-select checkboxes in Videos or Songs to batch-add multiple items directly to any playlist.",
                icon = Icons.Filled.VideoLibrary,
                category = "Playlists"
            ),
            FaqItem(
                id = "faq_supported_formats",
                question = "What audio & video codecs and formats are supported?",
                answer = "V2S provides built-in hardware decoding for MP4, MKV, AVI, WEBM, MOV, FLV, 3GP, TS, M4V video containers, and MP3, FLAC, WAV, AAC, M4A, OGG, OPUS audio codecs. 4K/UHD and high bitrate videos run at 60fps on supported hardware.",
                icon = Icons.Filled.PlayCircle,
                category = "Codecs & Formats"
            ),
            FaqItem(
                id = "faq_delete_files",
                question = "How to safely delete media files from the device?",
                answer = "Long-press any video or song to enter Selection Mode, choose items to delete, and tap the trash icon. A confirmation dialog will always ask for your explicit confirmation before permanently removing files from device storage.",
                icon = Icons.Filled.Settings,
                category = "Storage & Files"
            ),
            FaqItem(
                id = "faq_gestures",
                question = "How do the PLAYit-style swipe gestures work?",
                answer = "• Left Side Vertical Swipe: Slide up/down on the left half to adjust screen brightness.\n• Right Side Vertical Swipe: Slide up/down on the right half to change media volume.\n• Horizontal Swipe: Slide left/right anywhere to seek forward or backward with live timecode preview.",
                icon = Icons.Filled.Speed,
                category = "Player Gestures"
            ),
            FaqItem(
                id = "faq_rescan",
                question = "How to rescan storage for newly downloaded songs or videos?",
                answer = "Go to the More / Settings screen and tap 'Refresh Media Scan' in the top 3-dot menu or 'Rescan Media Library' under Storage & Cache. V2S will query Android MediaStore to discover newly added files.",
                icon = Icons.Filled.Refresh,
                category = "Library & Sync"
            ),
            FaqItem(
                id = "faq_audio_delay",
                question = "What to do if audio/video is out of sync or stuttering?",
                answer = "1. Ensure power saving mode is disabled for high-resolution 4K files.\n2. Switch between HW (Hardware) and SW decoding in Settings.\n3. If using Bluetooth headphones with high latency, select 'Play as Audio' for lightweight processing.",
                icon = Icons.Filled.BugReport,
                category = "Troubleshooting"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("help_support_screen"),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Card (V2S themed)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141A22),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Headphones,
                                contentDescription = "Support",
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HELP & SUPPORT / REPORT & SUGGEST",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Report errors, request features & message derp.digital.erp@gmail.com",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 2. Two-Tab Switcher: [ ✍️ Report & Suggest ] [ ❓ FAQ & Troubleshooting ]
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141416), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 1: Report & Suggest
                val isTab1 = selectedTab == SupportTab.REPORT_AND_SUGGEST
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { selectedTab = SupportTab.REPORT_AND_SUGGEST }
                        .testTag("tab_report_suggest"),
                    shape = RoundedCornerShape(9.dp),
                    color = if (isTab1) primaryColor else Color.Transparent,
                    border = if (!isTab1) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✍️ Report & Suggest",
                            color = if (isTab1) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Tab 2: FAQ & Troubleshooting
                val isTab2 = selectedTab == SupportTab.FAQ_AND_TROUBLESHOOTING
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { selectedTab = SupportTab.FAQ_AND_TROUBLESHOOTING }
                        .testTag("tab_faq_troubleshooting"),
                    shape = RoundedCornerShape(9.dp),
                    color = if (isTab2) primaryColor else Color.Transparent,
                    border = if (!isTab2) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❓ FAQ & Troubleshooting",
                            color = if (isTab2) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // TAB 1 CONTENT: REPORT & SUGGEST
        if (selectedTab == SupportTab.REPORT_AND_SUGGEST) {
            // Recipient Card with Copy Action
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF161B22),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Reports & Feedback Recipient",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B949E),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = SUPPORT_RECIPIENT_EMAIL,
                                    fontSize = 14.sp,
                                    color = Color(0xFF58A6FF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Support Email", SUPPORT_RECIPIENT_EMAIL)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied $SUPPORT_RECIPIENT_EMAIL to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_recipient_email_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = Color(0xFF8B949E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Category Selection Header & Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "What would you like to report or request?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeedbackCategory.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.22f) else Color(0xFF161B22),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { selectedCategory = category }
                                    .testTag("category_chip_${category.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = category.emoji,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = category.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) primaryColor else Color(0xFFC9D1D9)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Subject / Title Input
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectText,
                        onValueChange = { subjectText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_input"),
                        placeholder = {
                            Text(
                                text = "Subject / Issue / Feature Title",
                                color = Color(0xFF8B949E),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF141416),
                            unfocusedContainerColor = Color(0xFF141416)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Topic Suggestion Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topicSuggestions.forEach { suggestion ->
                            val chipText = suggestion.removePrefix("+ ")
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1C2128),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable {
                                    subjectText = chipText
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = Color(0xFF58A6FF),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Description & Suggestions
            item {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("description_input"),
                    placeholder = {
                        Text(
                            text = "Detailed Description & Suggestions\nProvide details on what happened, steps to reproduce, or feature ideas...",
                            color = Color(0xFF8B949E),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141416),
                        unfocusedContainerColor = Color(0xFF141416)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 4,
                    maxLines = 6
                )
            }

            // User Email Input (for response)
            item {
                OutlinedTextField(
                    value = userEmailText,
                    onValueChange = { userEmailText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_email_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Your Email (for response)",
                            color = Color(0xFF8B949E),
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141416),
                        unfocusedContainerColor = Color(0xFF141416)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Attach Media (Screenshots & Videos)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF161B22),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachFile,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Attach Media (Screenshots & Videos)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${attachedFiles.size} file(s) attached",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8B949E)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("pick_files_button")
                            ) {
                                Text(
                                    text = "+ Pick Files",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // List of attached items
                        if (attachedFiles.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                attachedFiles.forEachIndexed { index, item ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0D1117),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AttachFile,
                                                    contentDescription = null,
                                                    tint = Color(0xFF58A6FF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = item.name,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFC9D1D9),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            IconButton(
                                                onClick = { attachedFiles.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Remove file",
                                                    tint = Color(0xFFF85149),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Include System & App Diagnostics Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF161B22),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    color = Color(0xFF238636).copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF3FB950),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Include System & App Diagnostics",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8B949E)
                                    )
                                }
                            }

                            Switch(
                                checked = includeDiagnostics,
                                onCheckedChange = { includeDiagnostics = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Color(0xFF3FB950),
                                    uncheckedThumbColor = Color(0xFF8B949E),
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.testTag("diagnostics_switch")
                            )
                        }

                        // Expandable "View System Diagnostic Log" link
                        Row(
                            modifier = Modifier
                                .clickable { showDiagnosticLogPreview = !showDiagnosticLogPreview }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (showDiagnosticLogPreview) "▲ Hide System Diagnostic Log" else "▼ View System Diagnostic Log",
                                color = Color(0xFF58A6FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        AnimatedVisibility(
                            visible = showDiagnosticLogPreview,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0D1117),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = systemDiagnosticInfo,
                                    color = Color(0xFF7EE787),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Direct Dispatch Button: "Send Report / Feature Request via Email"
            item {
                Button(
                    onClick = {
                        val finalSubject = if (subjectText.isNotBlank()) subjectText else "${selectedCategory.title} from V2S user"
                        submittedCategoryName = selectedCategory.title
                        submittedSubject = finalSubject

                        val emailBody = buildString {
                            appendLine("=== V2S PLAYER SUPPORT & FEEDBACK DISPATCH ===")
                            appendLine("Category: ${selectedCategory.title} ${selectedCategory.emoji}")
                            appendLine("Subject: $finalSubject")
                            if (userEmailText.isNotBlank()) {
                                appendLine("Sender Email: $userEmailText")
                            }
                            appendLine()
                            appendLine("--- Detailed Description / Suggestions ---")
                            appendLine(if (descriptionText.isNotBlank()) descriptionText else "(No extra notes provided)")
                            appendLine()
                            if (includeDiagnostics) {
                                appendLine(systemDiagnosticInfo)
                            }
                        }

                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_RECIPIENT_EMAIL")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_RECIPIENT_EMAIL))
                                putExtra(Intent.EXTRA_SUBJECT, "[V2S ${selectedCategory.title}] $finalSubject")
                                putExtra(Intent.EXTRA_TEXT, emailBody)
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email via..."))
                        } catch (e: Exception) {
                            // Fallback intent if mailto handler is specific
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "message/rfc822"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_RECIPIENT_EMAIL))
                                    putExtra(Intent.EXTRA_SUBJECT, "[V2S ${selectedCategory.title}] $finalSubject")
                                    putExtra(Intent.EXTRA_TEXT, emailBody)
                                }
                                context.startActivity(Intent.createChooser(fallbackIntent, "Send Email via..."))
                            } catch (e2: Exception) {
                                Toast.makeText(context, "Could not open email client: ${e2.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Show Warm Appreciation & Thank-You Dialog immediately
                        showThankYouDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_email_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Report / Feature Request via Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // TAB 2 CONTENT: FAQ & TROUBLESHOOTING
        if (selectedTab == SupportTab.FAQ_AND_TROUBLESHOOTING) {
            items(faqList, key = { it.id }) { faq ->
                FaqAccordionCard(faq = faq)
            }
        }
    }

    // Warm Appreciation & Thank-You Dialog
    if (showThankYouDialog) {
        AlertDialog(
            onDismissRequest = { showThankYouDialog = false },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFC9D1D9),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF4D6D).copy(alpha = 0.2f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart",
                            tint = Color(0xFFFF4D6D),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Thank You! ❤️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Thank you so much for reporting errors and requesting new features to help make V2S Player better for everyone! Your suggestions and bug reports directly help us improve media playback and features for all users.",
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFC9D1D9)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0D1117),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Category:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B949E)
                                )
                                Text(
                                    text = submittedCategoryName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subject / Topic:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B949E)
                                )
                                Text(
                                    text = submittedSubject.take(28) + if (submittedSubject.length > 28) "..." else "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Recipient Inbox:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B949E)
                                )
                                Text(
                                    text = SUPPORT_RECIPIENT_EMAIL,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF58A6FF)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showThankYouDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("thank_you_done_button")
                ) {
                    Text(
                        text = "Done",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun FaqAccordionCard(
    faq: FaqItem,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(animationSpec = tween(durationMillis = 200)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF161B22),
        border = BorderStroke(
            1.dp,
            if (expanded) primaryColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                imageVector = faq.icon,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = faq.question,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = faq.category,
                            fontSize = 10.5.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) primaryColor else Color(0xFF8B949E),
                    modifier = Modifier.size(22.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = faq.answer,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFC9D1D9)
                )
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
    }
    if (name == null) {
        name = uri.path?.substringAfterLast('/')
    }
    return name
}
