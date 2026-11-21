package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SortOrder
import com.example.ui.theme.VlcOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsBottomSheet(
    isListView: Boolean,
    onToggleListView: (Boolean) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavoritesOnly: (Boolean) -> Unit,
    sortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.outline
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // 1. "Display settings" Section Header (Screenshots 2 & 3)
            Text(
                text = "Display settings",
                color = VlcOrange,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Display in list / grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleListView(!isListView) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isListView) Icons.Filled.GridView else Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = if (isListView) "Display in grid" else "Display in list",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }

            // Show only favorites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleFavoritesOnly(!showOnlyFavorites) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Show only favorites",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }
                Checkbox(
                    checked = showOnlyFavorites,
                    onCheckedChange = { onToggleFavoritesOnly(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VlcOrange,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkmarkColor = Color.Black
                    )
                )
            }

            // Group videos...
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Toggle Grouping */ }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VideoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Group videos...",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Group by fo..",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Playback audio...
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Toggle Playback mode */ }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "Playback as...",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Videos",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Play",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. "Sort by..." Section Header (Screenshots 2 & 3)
            Text(
                text = "Sort by...",
                color = VlcOrange,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Name: A 'n Z / Z 'n A
            SortGroupRow(
                icon = Icons.AutoMirrored.Filled.Sort,
                categoryTitle = "Name",
                firstOption = SortOptionData(
                    label = "A 'n  Z",
                    isSelected = sortOrder == SortOrder.NAME_ASC,
                    onSelect = { onSortOrderSelected(SortOrder.NAME_ASC) }
                ),
                secondOption = SortOptionData(
                    label = "Z 'n  A",
                    isSelected = sortOrder == SortOrder.NAME_DESC,
                    onSelect = { onSortOrderSelected(SortOrder.NAME_DESC) }
                )
            )

            // File name: A 'n Z / Z 'n A
            SortGroupRow(
                icon = Icons.Filled.Description,
                categoryTitle = "File name",
                firstOption = SortOptionData(
                    label = "A 'n  Z",
                    isSelected = sortOrder == SortOrder.FILENAME_ASC,
                    onSelect = { onSortOrderSelected(SortOrder.FILENAME_ASC) }
                ),
                secondOption = SortOptionData(
                    label = "Z 'n  A",
                    isSelected = sortOrder == SortOrder.FILENAME_DESC,
                    onSelect = { onSortOrderSelected(SortOrder.FILENAME_DESC) }
                )
            )

            // Length: Shortest first / Longest first
            SortGroupRow(
                icon = Icons.Filled.Schedule,
                categoryTitle = "Length",
                firstOption = SortOptionData(
                    label = "Shortest first",
                    isSelected = sortOrder == SortOrder.DURATION_SHORT,
                    onSelect = { onSortOrderSelected(SortOrder.DURATION_SHORT) }
                ),
                secondOption = SortOptionData(
                    label = "Longest first",
                    isSelected = sortOrder == SortOrder.DURATION_LONG,
                    onSelect = { onSortOrderSelected(SortOrder.DURATION_LONG) }
                )
            )

            // Insertion date: Oldest first / Newest first (Screenshot 3 checkmark)
            SortGroupRow(
                icon = Icons.Filled.CalendarToday,
                categoryTitle = "Insertion date",
                firstOption = SortOptionData(
                    label = "Oldest first",
                    isSelected = sortOrder == SortOrder.DATE_OLD,
                    onSelect = { onSortOrderSelected(SortOrder.DATE_OLD) }
                ),
                secondOption = SortOptionData(
                    label = "Newest first",
                    isSelected = sortOrder == SortOrder.DATE_NEW,
                    onSelect = { onSortOrderSelected(SortOrder.DATE_NEW) }
                )
            )
        }
    }
}

private data class SortOptionData(
    val label: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit
)

@Composable
private fun SortGroupRow(
    icon: ImageVector,
    categoryTitle: String,
    firstOption: SortOptionData,
    secondOption: SortOptionData
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))

        // Title column
        Text(
            text = categoryTitle,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.width(110.dp)
        )

        // Options column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First sub-option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { firstOption.onSelect() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = firstOption.label,
                    color = if (firstOption.isSelected) VlcOrange else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (firstOption.isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
                if (firstOption.isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = VlcOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Second sub-option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { secondOption.onSelect() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secondOption.label,
                    color = if (secondOption.isSelected) VlcOrange else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (secondOption.isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
                if (secondOption.isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = VlcOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}