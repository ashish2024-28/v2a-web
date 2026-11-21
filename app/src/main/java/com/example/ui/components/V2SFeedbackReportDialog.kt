package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VlcOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object V2SSupportHelper {
    const val SUPPORT_EMAIL = "derp.digital.erp@gmail.com"

    fun launchEmailIntent(
        context: Context,
        type: String = "Feedback",
        userSubject: String = "",
        userMessage: String = "",
        userEmail: String = "",
        includeDiagnostics: Boolean = true
    ) {
        val subject = "[V2S $type] ${if (userSubject.isNotBlank()) userSubject else "User Message / Report"}"

        val diagnostics = if (includeDiagnostics) {
            """
            
            ------------------------------------
            System & Device Diagnostics:
            App: V2S Media Player
            Target Support: $SUPPORT_EMAIL
            Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})
            Android OS: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}
            ${if (userEmail.isNotBlank()) "Sender Contact: $userEmail" else ""}
            ------------------------------------
            """.trimIndent()
        } else {
            if (userEmail.isNotBlank()) "\n\nSender Contact: $userEmail" else ""
        }

        val fullBody = if (userMessage.isNotBlank()) {
            "$userMessage\n$diagnostics"
        } else {
            "Please describe your message, complaint, or bug report below:\n\n\n$diagnostics"
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$SUPPORT_EMAIL")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, fullBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Send $type to $SUPPORT_EMAIL"))
        } catch (e: Exception) {
            copyEmailToClipboard(context, showToast = true)
        }
    }

    fun copyEmailToClipboard(context: Context, showToast: Boolean = true) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("V2S Support Email", SUPPORT_EMAIL)
        clipboard?.setPrimaryClip(clip)
        if (showToast) {
            Toast.makeText(context, "Copied support email: $SUPPORT_EMAIL", Toast.LENGTH_LONG).show()
        }
    }
}

enum class SupportRequestType(
    val label: String,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    FEEDBACK("Feedback", "Send Feedback & Suggestions", Icons.Filled.Feedback, Color(0xFFFFD600)),
    REPORT("Bug Report", "Report a Bug or Issue", Icons.Filled.BugReport, Color(0xFFFF6D00)),
    COMPLAINT("Complaint", "Submit a Complaint", Icons.Filled.ReportProblem, Color(0xFFFF3D00)),
    INQUIRY("Message", "General Inquiry / Message", Icons.Filled.Email, Color(0xFF00E5FF))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun V2SFeedbackReportDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    initialType: SupportRequestType = SupportRequestType.FEEDBACK,
    initialSubject: String = "",
    initialMessage: String = ""
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var subject by remember(initialSubject) { mutableStateOf(initialSubject) }
    var message by remember(initialMessage) { mutableStateOf(initialMessage) }
    var userContactEmail by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = selectedType.accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = selectedType.icon,
                            contentDescription = null,
                            tint = selectedType.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = selectedType.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recipient: ${V2SSupportHelper.SUPPORT_EMAIL}",
                        fontSize = 12.sp,
                        color = VlcOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector Chips
                Text(
                    text = "Select Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportRequestType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) type.accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) type.accentColor else Color.Transparent
                            ),
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) type.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = type.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Recipient Email Card with Quick Copy
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, VlcOrange.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = VlcOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Send To Support:",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = V2SSupportHelper.SUPPORT_EMAIL,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                V2SSupportHelper.copyEmailToClipboard(context, showToast = true)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = VlcOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Subject Field
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g., Video playback error, feature suggestion)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    )
                )

                // Message Field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Detailed Message, Complaint or Bug Description") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    )
                )

                // Optional contact email
                OutlinedTextField(
                    value = userContactEmail,
                    onValueChange = { userContactEmail = it },
                    label = { Text("Your Email (Optional, for reply)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VlcOrange,
                        focusedLabelColor = VlcOrange
                    )
                )

                // Include diagnostics toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeDiagnostics = !includeDiagnostics }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeDiagnostics,
                        onCheckedChange = { includeDiagnostics = it },
                        colors = CheckboxDefaults.colors(checkedColor = VlcOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Include device & OS diagnostics",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    V2SSupportHelper.launchEmailIntent(
                        context = context,
                        type = selectedType.label,
                        userSubject = subject,
                        userMessage = message,
                        userEmail = userContactEmail,
                        includeDiagnostics = includeDiagnostics
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VlcOrange,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send via Email", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
