package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VlcOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Modern touch side fast scroller placed on the right side of any scrollable container.
 * Supports LazyListState (LazyColumn) and optional index letter/label bubble.
 */
@Composable
fun FastScroller(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier,
    thumbColor: Color = VlcOrange,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    indexLabel: ((index: Int) -> String)? = null,
    content: @Composable () -> Unit
) {
    if (itemCount <= 0) {
        content()
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var hideJobTrigger by remember { mutableStateOf(0) }
    var dragNormalizedY by remember { mutableFloatStateOf(0f) }

    // Track scroll activity to show handle when scrolling normally
    val isScrollInProgress = listState.isScrollInProgress

    // Auto-hide delay after scrolling or dragging stops
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            isVisible = true
        } else {
            delay(1800)
            isVisible = false
        }
    }

    // Compute current scroll fraction from firstVisibleItemIndex
    val listFraction by remember(listState, itemCount) {
        derivedStateOf {
            if (itemCount <= 1) 0f
            else {
                val index = listState.firstVisibleItemIndex.toFloat()
                val offset = listState.firstVisibleItemScrollOffset.toFloat()
                // approximate estimate
                (index / (itemCount - 1).toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    val currentFraction = if (isDragging) dragNormalizedY else listFraction

    val targetIndex by remember(currentFraction, itemCount) {
        derivedStateOf {
            ((currentFraction * (itemCount - 1)).roundToInt()).coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        }
    }

    val bubbleText = remember(targetIndex, indexLabel) {
        if (indexLabel != null && itemCount > 0) {
            indexLabel(targetIndex)
        } else ""
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        content()

        val maxHeightPx = constraints.maxHeight.toFloat()
        val thumbHeightDp = 48.dp
        val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
        val maxScrollableHeightPx = (maxHeightPx - thumbHeightPx).coerceAtLeast(1f)

        val thumbOffsetY = (currentFraction * maxScrollableHeightPx).coerceIn(0f, maxScrollableHeightPx)

        // Only display if there are enough items to warrant fast-scrolling (e.g. > 6)
        if (itemCount > 6) {
            AnimatedVisibility(
                visible = isVisible || isDragging,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(400)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(maxHeightPx, maxScrollableHeightPx, itemCount) {
                            detectTapGestures(
                                onPress = { offset ->
                                    isDragging = true
                                    val clampedY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (clampedY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        listState.scrollToItem(idx)
                                    }
                                    tryAwaitRelease()
                                    isDragging = false
                                }
                            )
                        }
                        .pointerInput(maxHeightPx, maxScrollableHeightPx, itemCount) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    val clampedY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (clampedY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        listState.scrollToItem(idx)
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newY = (thumbOffsetY + dragAmount.y).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (newY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        listState.scrollToItem(idx)
                                    }
                                }
                            )
                        }
                ) {
                    // Slim background track on edge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(trackColor, RoundedCornerShape(2.dp))
                    )

                    // Letter Preview Bubble when dragging
                    if (isDragging && bubbleText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 6.dp, bottomEnd = 6.dp),
                            color = thumbColor,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset {
                                    IntOffset(
                                        x = -with(density) { 52.dp.roundToPx() },
                                        y = (thumbOffsetY - with(density) { 8.dp.roundToPx() }).roundToInt()
                                    )
                                }
                                .size(50.dp, 44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = bubbleText,
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Draggable Scroller Pill Handle
                    val handleWidth = if (isDragging) 8.dp else 5.dp
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = thumbColor,
                        shadowElevation = if (isDragging) 6.dp else 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset { IntOffset(x = -with(density) { 3.dp.roundToPx() }, y = thumbOffsetY.roundToInt()) }
                            .width(handleWidth)
                            .size(width = handleWidth, height = thumbHeightDp)
                    ) {}
                }
            }
        }
    }
}

/**
 * Grid-based Fast Scroller wrapper for LazyVerticalGrid.
 */
@Composable
fun FastScrollerGrid(
    gridState: LazyGridState,
    itemCount: Int,
    columns: Int = 2,
    modifier: Modifier = Modifier,
    thumbColor: Color = VlcOrange,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    indexLabel: ((index: Int) -> String)? = null,
    content: @Composable () -> Unit
) {
    if (itemCount <= 0) {
        content()
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var dragNormalizedY by remember { mutableFloatStateOf(0f) }

    val isScrollInProgress = gridState.isScrollInProgress
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            isVisible = true
        } else {
            delay(1800)
            isVisible = false
        }
    }

    val totalRows = (itemCount + columns - 1) / columns
    val gridFraction by remember(gridState, totalRows) {
        derivedStateOf {
            if (totalRows <= 1) 0f
            else {
                val rowIndex = gridState.firstVisibleItemIndex / columns
                (rowIndex.toFloat() / (totalRows - 1).toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    val currentFraction = if (isDragging) dragNormalizedY else gridFraction

    val targetIndex by remember(currentFraction, itemCount) {
        derivedStateOf {
            ((currentFraction * (itemCount - 1)).roundToInt()).coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        }
    }

    val bubbleText = remember(targetIndex, indexLabel) {
        if (indexLabel != null && itemCount > 0) {
            indexLabel(targetIndex)
        } else ""
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        content()

        val maxHeightPx = constraints.maxHeight.toFloat()
        val thumbHeightDp = 48.dp
        val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
        val maxScrollableHeightPx = (maxHeightPx - thumbHeightPx).coerceAtLeast(1f)

        val thumbOffsetY = (currentFraction * maxScrollableHeightPx).coerceIn(0f, maxScrollableHeightPx)

        if (itemCount > 8) {
            AnimatedVisibility(
                visible = isVisible || isDragging,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(400)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(maxHeightPx, maxScrollableHeightPx, itemCount) {
                            detectTapGestures(
                                onPress = { offset ->
                                    isDragging = true
                                    val clampedY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (clampedY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        gridState.scrollToItem(idx)
                                    }
                                    tryAwaitRelease()
                                    isDragging = false
                                }
                            )
                        }
                        .pointerInput(maxHeightPx, maxScrollableHeightPx, itemCount) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    val clampedY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (clampedY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        gridState.scrollToItem(idx)
                                    }
                                },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newY = (thumbOffsetY + dragAmount.y).coerceIn(0f, maxScrollableHeightPx)
                                    val frac = (newY / maxScrollableHeightPx).coerceIn(0f, 1f)
                                    dragNormalizedY = frac
                                    val idx = ((frac * (itemCount - 1)).roundToInt()).coerceIn(0, itemCount - 1)
                                    coroutineScope.launch {
                                        gridState.scrollToItem(idx)
                                    }
                                }
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(trackColor, RoundedCornerShape(2.dp))
                    )

                    if (isDragging && bubbleText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 6.dp, bottomEnd = 6.dp),
                            color = thumbColor,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset {
                                    IntOffset(
                                        x = -with(density) { 52.dp.roundToPx() },
                                        y = (thumbOffsetY - with(density) { 8.dp.roundToPx() }).roundToInt()
                                    )
                                }
                                .size(50.dp, 44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = bubbleText,
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    val handleWidth = if (isDragging) 8.dp else 5.dp
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = thumbColor,
                        shadowElevation = if (isDragging) 6.dp else 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset { IntOffset(x = -with(density) { 3.dp.roundToPx() }, y = thumbOffsetY.roundToInt()) }
                            .width(handleWidth)
                            .size(width = handleWidth, height = thumbHeightDp)
                    ) {}
                }
            }
        }
    }
}
