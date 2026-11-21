package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VlcOrange
import kotlinx.coroutines.delay

enum class V2SAlertPosition {
    TOP,
    BOTTOM
}

/**
 * Fixed top in-app alert banner.
 * Positioned safely below the status bar with smooth top entrance/exit animations.
 * All alerts are strictly fixed to the top of the screen.
 */
@Composable
fun V2SAlertHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    // Optional compatibility parameters defaulted for backward compatibility
    position: V2SAlertPosition = V2SAlertPosition.TOP,
    onTogglePosition: () -> Unit = {},
    isWideScreen: Boolean = false
) {
    val isVisible = !message.isNullOrBlank()

    // Auto-dismiss top alert after 3.2 seconds
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            delay(3200L)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp))
                    .testTag("top_notification_banner"),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xF5161820),
                border = BorderStroke(1.dp, VlcOrange.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VlcOrange.copy(alpha = 0.18f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = VlcOrange,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Text(
                            text = message.orEmpty(),
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("top_notification_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF9E9EA8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
