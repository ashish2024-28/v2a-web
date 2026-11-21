package com.example.ui.screens.settings

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.EarphoneAction
import com.example.player.EarphoneControlSettings
import com.example.player.EarphonePreferences

@Composable
fun EarphoneControlsSection(
    onOpenEarphoneSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenEarphoneSettings() }
            .testTag("earphone_controls_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Earphone & Bluetooth",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Configure headset button actions",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EarphoneSettingsDetailScreen(
    horizontalPadding: Dp = 16.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    var settings by remember { mutableStateOf(EarphonePreferences.load(context)) }
    var selectingTarget by remember { mutableStateOf<String?>(null) } // "single", "double", "triple", "right", "left"
    
    var connectedDevice by remember { mutableStateOf<AudioDeviceInfo?>(null) }

    fun updateDevice() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        connectedDevice = devices.firstOrNull { 
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || 
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || 
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || 
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_HEARING_AID
        }
    }

    DisposableEffect(context) {
        val callback = object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                updateDevice()
            }
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                updateDevice()
            }
        }
        updateDevice()
        audioManager.registerAudioDeviceCallback(callback, null)
        onDispose {
            audioManager.unregisterAudioDeviceCallback(callback)
        }
    }

    fun updateAndSave(newSettings: EarphoneControlSettings) {
        settings = newSettings
        EarphonePreferences.save(context, newSettings)
    }

    fun resetToDefault() {
        val defaultSettings = EarphoneControlSettings()
        updateAndSave(defaultSettings)
        Toast.makeText(context, "Gestures reset to default", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Connected Device Status
        item {
            Spacer(modifier = Modifier.height(8.dp))
            val isConnected = connectedDevice != null
            val deviceName = connectedDevice?.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Unknown Audio Device"
            val deviceTypeString = when (connectedDevice?.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                AudioDeviceInfo.TYPE_BLE_HEADSET -> "Bluetooth LE"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset (with mic)"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
                AudioDeviceInfo.TYPE_HEARING_AID -> "Hearing Aid"
                else -> "Disconnected"
            }
            val icon = if (isConnected) Icons.Filled.Headphones else Icons.Filled.HeadsetOff
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConnected) "Connected Device" else "No Device Connected",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isConnected) deviceName else "Use phone speaker",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (isConnected) {
                            Text(
                                text = "Type: $deviceTypeString",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Custom Controls",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Override system default media keys",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = settings.isCustomControlsEnabled,
                            onCheckedChange = {
                                updateAndSave(settings.copy(isCustomControlsEnabled = it))
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Universal Headset Button",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
        item {
            GestureOptionItem(
                title = "Single Click",
                subtitle = "Play/Pause media or Answer/End calls",
                currentAction = settings.singleClickAction,
                enabled = settings.isCustomControlsEnabled,
                onClick = { selectingTarget = "single" }
            )
        }
        item {
            GestureOptionItem(
                title = "Double Click",
                subtitle = "Supported on wired headsets/some earbuds",
                currentAction = settings.doubleClickAction,
                enabled = settings.isCustomControlsEnabled,
                onClick = { selectingTarget = "double" }
            )
        }
        item {
            GestureOptionItem(
                title = "Triple Click",
                subtitle = "Supported on wired headsets/some earbuds",
                currentAction = settings.tripleClickAction,
                enabled = settings.isCustomControlsEnabled,
                onClick = { selectingTarget = "triple" }
            )
        }

        item {
            Text(
                text = "Device-Specific Media Keys",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
        item {
            Text(
                text = "Note: Many wireless earbuds handle gestures internally and send direct Media Next/Previous commands. Android cannot distinguish left/right earbuds directly. These settings map those media commands.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        item {
            GestureOptionItem(
                title = "Media Next Key",
                subtitle = "Often sent by right earbud or double-tap",
                currentAction = settings.doubleClickRightAction,
                enabled = settings.isCustomControlsEnabled,
                onClick = { selectingTarget = "right" }
            )
        }
        item {
            GestureOptionItem(
                title = "Media Previous Key",
                subtitle = "Often sent by left earbud or triple-tap",
                currentAction = settings.doubleClickLeftAction,
                enabled = settings.isCustomControlsEnabled,
                onClick = { selectingTarget = "left" }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { resetToDefault() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset to Default",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (selectingTarget != null) {
        val target = selectingTarget!!
        val currentSelected = when (target) {
            "single" -> settings.singleClickAction
            "double" -> settings.doubleClickAction
            "triple" -> settings.tripleClickAction
            "right" -> settings.doubleClickRightAction
            "left" -> settings.doubleClickLeftAction
            else -> EarphoneAction.PLAY_PAUSE
        }
        val targetTitle = when (target) {
            "single" -> "Single Click"
            "double" -> "Double Click"
            "triple" -> "Triple Click"
            "right" -> "Media Next Key"
            "left" -> "Media Previous Key"
            else -> "Gesture Action"
        }

        AlertDialog(
            onDismissRequest = { selectingTarget = null },
            title = {
                Text(
                    text = "Choose Action",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(EarphoneAction.values().size) { idx ->
                        val action = EarphoneAction.values()[idx]
                        val isSelected = action == currentSelected
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val updated = when (target) {
                                        "single" -> settings.copy(singleClickAction = action)
                                        "double" -> settings.copy(doubleClickAction = action)
                                        "triple" -> settings.copy(tripleClickAction = action)
                                        "right" -> settings.copy(doubleClickRightAction = action)
                                        "left" -> settings.copy(doubleClickLeftAction = action)
                                        else -> settings
                                    }
                                    updateAndSave(updated)
                                    selectingTarget = null
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = action.title,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = action.description,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectingTarget = null }) {
                    Text("Close")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun GestureOptionItem(
    title: String,
    subtitle: String,
    currentAction: EarphoneAction,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getActionIcon(currentAction),
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.6f),
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ) {
                Text(
                    text = currentAction.title,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun getActionIcon(action: EarphoneAction): ImageVector {
    return when (action) {
        EarphoneAction.PLAY_PAUSE -> Icons.Filled.PlayArrow
        EarphoneAction.NEXT_TRACK -> Icons.Filled.SkipNext
        EarphoneAction.PREVIOUS_TRACK -> Icons.Filled.SkipPrevious
        EarphoneAction.SEEK_FORWARD_10S -> Icons.Filled.FastForward
        EarphoneAction.SEEK_BACKWARD_10S -> Icons.Filled.FastRewind
        EarphoneAction.INCREASE_VOLUME -> Icons.AutoMirrored.Filled.VolumeUp
        EarphoneAction.DECREASE_VOLUME -> Icons.AutoMirrored.Filled.VolumeDown
        EarphoneAction.MUTE_UNMUTE -> Icons.AutoMirrored.Filled.VolumeMute
        EarphoneAction.TOGGLE_SPEED -> Icons.Filled.Speed
        EarphoneAction.NONE -> Icons.Filled.TouchApp
    }
}
