package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.VideoItem
import com.example.ui.theme.VyloCoralAccent

@Composable
fun DeleteConfirmationDialog(
    video: VideoItem?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (video == null) return

    DeleteGenericConfirmationDialog(
        title = "Delete Video Permanently?",
        message = "Are you sure you want to permanently delete \"${video.title}\" from device storage memory? The file will be erased permanently from your device and cannot be recovered.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DeleteGenericConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete Permanently",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = null,
                tint = Color(0xFFFF5252)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF5252)
                ),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("delete_dialog")
    )
}

@Composable
fun DeleteMultipleConfirmationDialog(
    count: Int,
    mediaType: String = "items",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (count <= 0) return

    DeleteGenericConfirmationDialog(
        title = "Delete $count $mediaType Permanently?",
        message = "Are you sure you want to permanently delete these $count $mediaType from your device storage? This action cannot be undone.",
        confirmLabel = "Delete $count $mediaType",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
