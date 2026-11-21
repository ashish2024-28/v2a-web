package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SortOrder
import com.example.ui.theme.VlcOrange
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.Sort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VlcDisplaySettingsSheet(
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    showFavoritesOnly: Boolean,
    onToggleFavoritesOnly: (Boolean) -> Unit,
    sortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    groupByFolder: Boolean,
    onToggleGroupByFolder: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showGroupDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Main Title
            Text(
                text = "Display settings",
                color = VlcOrange,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 1. Display in list / grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleGridView() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isGridView) "Display in list" else "Display in grid",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. Show only favorites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleFavoritesOnly(!showFavoritesOnly) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = if (showFavoritesOnly) VlcOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Show only favorites",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = showFavoritesOnly,
                    onCheckedChange = onToggleFavoritesOnly,
                    colors = CheckboxDefaults.colors(checkedColor = VlcOrange)
                )
            }

            // 3. Group videos
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGroupDropdown = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Group videos...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (groupByFolder) "Group by folder" else "None",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = showGroupDropdown,
                    onDismissRequest = { showGroupDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Group by folder") },
                        onClick = {
                            onToggleGroupByFolder(true)
                            showGroupDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("None (All videos)") },
                        onClick = {
                            onToggleGroupByFolder(false)
                            showGroupDropdown = false
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Sort by section header
            Text(
                text = "Sort by...",
                color = VlcOrange,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Name
            SortOptionTwoWayRow(
                icon = Icons.AutoMirrored.Filled.Sort,
                label = "Name",
                firstOptionLabel = "A 'n  Z",
                secondOptionLabel = "Z 'n  A",
                isFirstSelected = sortOrder == SortOrder.NAME_ASC,
                isSecondSelected = sortOrder == SortOrder.NAME_DESC,
                onFirstClick = { onSortChange(SortOrder.NAME_ASC) },
                onSecondClick = { onSortChange(SortOrder.NAME_DESC) }
            )

            // File name
            SortOptionTwoWayRow(
                icon = Icons.Filled.Description,
                label = "File name",
                firstOptionLabel = "A 'n  Z",
                secondOptionLabel = "Z 'n  A",
                isFirstSelected = sortOrder == SortOrder.FILENAME_ASC,
                isSecondSelected = sortOrder == SortOrder.FILENAME_DESC,
                onFirstClick = { onSortChange(SortOrder.FILENAME_ASC) },
                onSecondClick = { onSortChange(SortOrder.FILENAME_DESC) }
            )

            // Length
            SortOptionTwoWayRow(
                icon = Icons.Filled.Timer,
                label = "Length",
                firstOptionLabel = "Shortest first",
                secondOptionLabel = "Longest first",
                isFirstSelected = sortOrder == SortOrder.DURATION_SHORT,
                isSecondSelected = sortOrder == SortOrder.DURATION_LONG,
                onFirstClick = { onSortChange(SortOrder.DURATION_SHORT) },
                onSecondClick = { onSortChange(SortOrder.DURATION_LONG) }
            )

            // Insertion date
            SortOptionTwoWayRow(
                icon = Icons.Filled.CalendarMonth,
                label = "Insertion date",
                firstOptionLabel = "Oldest first",
                secondOptionLabel = "Newest first",
                isFirstSelected = sortOrder == SortOrder.OLDEST,
                isSecondSelected = sortOrder == SortOrder.NEWEST,
                onFirstClick = { onSortChange(SortOrder.OLDEST) },
                onSecondClick = { onSortChange(SortOrder.NEWEST) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SortOptionTwoWayRow(
    icon: ImageVector,
    label: String,
    firstOptionLabel: String,
    secondOptionLabel: String,
    isFirstSelected: Boolean,
    isSecondSelected: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onFirstClick)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = firstOptionLabel,
                    color = if (isFirstSelected) VlcOrange else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isFirstSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
                if (isFirstSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = VlcOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clickable(onClick = onSecondClick)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = secondOptionLabel,
                    color = if (isSecondSelected) VlcOrange else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSecondSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
                if (isSecondSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = VlcOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
