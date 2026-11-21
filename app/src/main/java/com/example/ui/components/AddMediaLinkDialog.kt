package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.VlcOrange
import com.example.util.MediaLinkUtils

data class UserLinkEditData(
    val mediaId: Long,
    val url: String,
    val title: String,
    val mediaType: String,
    val artistOrSource: String
)

@Composable
fun AddMediaLinkDialog(
    initialMediaType: String = "VIDEO",
    editingLink: UserLinkEditData? = null,
    onDismiss: () -> Unit,
    onSaveLink: (url: String, title: String, mediaType: String, artistOrSource: String) -> Unit
) {
    val isEditMode = editingLink != null
    var urlInput by remember(editingLink) { mutableStateOf(editingLink?.url.orEmpty()) }
    var titleInput by remember(editingLink) { mutableStateOf(editingLink?.title.orEmpty()) }
    var sourceInput by remember(editingLink) { mutableStateOf(editingLink?.artistOrSource.orEmpty()) }
    var selectedType by remember(editingLink, initialMediaType) {
        mutableStateOf(editingLink?.mediaType ?: initialMediaType)
    }
    var urlError by remember(editingLink) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Filled.Edit else Icons.Filled.Link,
                    contentDescription = null,
                    tint = VlcOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isEditMode) "Fix or Edit Media Link" else "Add Video or Song Link",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paste any YouTube, YouTube Shorts, YouTube Music, SoundCloud, Vimeo, or direct MP4/MP3/M3U8 link. Saved locally on your device.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { newVal ->
                        urlInput = newVal
                        urlError = null
                        if (!isEditMode && (newVal.contains("http") || newVal.contains("youtu") || newVal.contains(".mp"))) {
                            selectedType = MediaLinkUtils.inferMediaType(newVal)
                        }
                    },
                    label = { Text("Media Link URL *") },
                    placeholder = { Text("https://youtube.com/watch?v=...") },
                    isError = urlError != null,
                    supportingText = urlError?.let { err -> { Text(err) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title (Optional)") },
                    placeholder = { Text("Auto-fetched if left blank") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sourceInput,
                    onValueChange = { sourceInput = it },
                    label = { Text("Channel / Artist (Optional)") },
                    placeholder = { Text("e.g. YouTube, Favorite Artist") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Save as",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        Triple("VIDEO", "Video", Icons.Filled.VideoLibrary),
                        Triple("SHORT", "Short", Icons.Filled.AutoAwesome),
                        Triple("SONG", "Song", Icons.Filled.MusicNote)
                    )
                    options.forEach { (typeKey, label, icon) ->
                        val selected = selectedType == typeKey
                        FilterChip(
                            selected = selected,
                            onClick = { selectedType = typeKey },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VlcOrange.copy(alpha = 0.2f),
                                selectedLabelColor = VlcOrange,
                                selectedLeadingIconColor = VlcOrange
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = MediaLinkUtils.validateMediaUrl(urlInput)
                    if (!validation.valid) {
                        urlError = validation.errorMessage ?: "Please enter a valid video or song link"
                        return@Button
                    }
                    onSaveLink(
                        validation.normalizedUrl,
                        titleInput.trim(),
                        selectedType,
                        sourceInput.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VlcOrange,
                    contentColor = Color.White
                )
            ) {
                Text(if (isEditMode) "Update Link" else "Save & Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
