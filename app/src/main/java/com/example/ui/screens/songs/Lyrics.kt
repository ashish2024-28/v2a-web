package com.example.ui.screens.songs

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SongItem
import com.example.ui.theme.PlayitCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** One timed lyric line from an LRC file. */
data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * Parses normal LRC timestamps such as:
 * [00:12.30]First line
 * [01:05.500]Another line
 *
 * Multiple timestamps on one line are also supported.
 */
fun parseLrc(text: String): List<LyricLine> {
    val result = mutableListOf<LyricLine>()

    val regex = Regex(
        """\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?\]([^\r\n]*)"""
    )

    text.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()

        regex.findAll(line).forEach { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: return@forEach
            val seconds = match.groupValues[2].toLongOrNull() ?: return@forEach

            val fractionText = match.groupValues[3]
                .ifBlank { "0" }
                .padEnd(3, '0')
                .take(3)

            val fractionMs = fractionText.toLongOrNull() ?: 0L
            val lyric = match.groupValues[4].trim()

            if (lyric.isNotBlank()) {
                result += LyricLine(
                    timeMs = minutes * 60_000L + seconds * 1_000L + fractionMs,
                    text = lyric
                )
            }
        }
    }

    return result.sortedBy { it.timeMs }
}

/**
 * Gets the real filesystem path for a MediaStore/content Uri when Android
 * exposes it. This lets us look for a sidecar LRC file beside the media file.
 */
private fun resolveMediaPath(
    context: Context,
    uri: Uri
): String? {
    if (uri.scheme.equals("file", ignoreCase = true)) {
        return uri.path
    }

    if (!uri.scheme.equals("content", ignoreCase = true)) {
        return null
    }

    val projection = arrayOf(MediaStore.MediaColumns.DATA)

    return runCatching {
        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)
            } else {
                null
            }
        }
    }.getOrNull()
}

/** Loads Song.lrc from the same directory as the selected media file. */
suspend fun loadLyrics(
    context: Context,
    song: SongItem
): List<LyricLine> = withContext(Dispatchers.IO) {
    val mediaUri = song.uri
    val mediaPath = resolveMediaPath(context, mediaUri)
        ?: return@withContext emptyList()

    val audioFile = File(mediaPath)
    val parent = audioFile.parentFile
        ?: return@withContext emptyList()

    val lrcFile = File(
        parent,
        audioFile.nameWithoutExtension + ".lrc"
    )

    if (!lrcFile.isFile || !lrcFile.canRead()) {
        return@withContext emptyList()
    }

    runCatching {
        parseLrc(lrcFile.readText(Charsets.UTF_8))
    }.getOrDefault(emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    song: SongItem,
    currentPositionMs: Long,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var lyrics by remember(song.id) {
        mutableStateOf<List<LyricLine>>(emptyList())
    }

    val listState = rememberLazyListState()

    LaunchedEffect(song.id) {
        lyrics = loadLyrics(context, song)
    }

    val activeIndex = remember(lyrics, currentPositionMs) {
        lyrics.indexOfLast { it.timeMs <= currentPositionMs }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyrics.size) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -220
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        containerColor = Color(0xFF101116),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = PlayitCyan,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Text(
                    text = "Lyrics",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close lyrics",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            if (lyrics.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E2130),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E344A)),
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Lyrics Not Found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "No synchronized or embedded lyrics found for \"${song.title}\".",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF161822),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252838)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = PlayitCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tip: You can add synchronized lyrics by placing an .lrc file in the same folder (${song.title}.lrc).",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF272B3C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    itemsIndexed(
                        items = lyrics,
                        key = { index, line -> "${line.timeMs}_$index" }
                    ) { index, line ->
                        val active = index == activeIndex

                        Text(
                            text = line.text,
                            color = if (active) {
                                PlayitCyan
                            } else {
                                Color.White.copy(alpha = 0.55f)
                            },
                            fontSize = if (active) 20.sp else 16.sp,
                            fontWeight = if (active) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}