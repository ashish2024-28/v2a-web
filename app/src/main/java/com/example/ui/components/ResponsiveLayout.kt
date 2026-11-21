package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class V2SWindowSize {
    COMPACT, // Phones
    MEDIUM,  // Small tablets / foldables
    EXPANDED // Large tablets / desktop
}

/**
 * Responsive Screen dimensions and breakpoints for V2S media player.
 * Narrow phone (< 360dp width) gets tailored tighter padding and balanced icons.
 * Normal phone (360dp - 600dp width) gets balanced standard UI.
 * Tablet / Landscape (>= 600dp) expands smoothly.
 */
data class V2SScreenDimensions(
    val windowSize: V2SWindowSize,
    val screenWidthDp: Dp,
    val isNarrowPhone: Boolean,
    val horizontalPadding: Dp,
    val contentSpacing: Dp,
    val iconSize: Dp,
    val touchTargetSize: Dp
)

/**
 * Helper to compute responsive column counts based on V2SWindowSize and available width:
 * Phone/Compact: compactColumns (defaults to 2)
 * Tablet/Medium: mediumColumns (defaults to 3)
 * Desktop/Expanded: expandedColumns (defaults to 4)
 */
fun responsiveGridCells(
    windowSize: V2SWindowSize,
    compactColumns: Int = 2,
    mediumColumns: Int = 3,
    expandedColumns: Int = 4
): GridCells {
    val count = when (windowSize) {
        V2SWindowSize.COMPACT -> compactColumns
        V2SWindowSize.MEDIUM -> mediumColumns
        V2SWindowSize.EXPANDED -> expandedColumns
    }
    return GridCells.Fixed(count)
}

/**
 * Adaptive column count for card grids based on exact available width.
 * Prevents cards from getting excessively squished on small screens or huge on tablets.
 */
fun adaptiveGridCells(
    minCardWidth: Dp = 150.dp
): GridCells {
    return GridCells.Adaptive(minSize = minCardWidth)
}

/**
 * Clamp helper for responsive Dp values:
 * Keeps value between [min] and [max].
 */
fun clampDp(preferred: Dp, min: Dp, max: Dp): Dp {
    return when {
        preferred < min -> min
        preferred > max -> max
        else -> preferred
    }
}

/**
 * Clamp helper for responsive TextUnit (sp) values:
 * Keeps font size between [min] and [max].
 */
fun clampSp(preferred: TextUnit, min: TextUnit, max: TextUnit): TextUnit {
    return when {
        preferred.value < min.value -> min
        preferred.value > max.value -> max
        else -> preferred
    }
}

@Composable
fun rememberV2SWindowSize(): V2SWindowSize {
    val config = LocalConfiguration.current
    val width = config.screenWidthDp.dp
    return when {
        width < 600.dp -> V2SWindowSize.COMPACT
        width < 840.dp -> V2SWindowSize.MEDIUM
        else -> V2SWindowSize.EXPANDED
    }
}

/**
 * Top-level responsive wrapper supplying screen dimensions, window size,
 * and adaptive horizontal padding to any screen.
 */
@Composable
fun ResponsiveScreen(
    modifier: Modifier = Modifier,
    content: @Composable (V2SWindowSize, Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val windowSize = when {
            maxWidth < 600.dp -> V2SWindowSize.COMPACT
            maxWidth < 840.dp -> V2SWindowSize.MEDIUM
            else -> V2SWindowSize.EXPANDED
        }

        val horizontalPadding = when {
            maxWidth < 360.dp -> 10.dp
            maxWidth < 600.dp -> 14.dp
            maxWidth < 840.dp -> 20.dp
            else -> 28.dp
        }

        content(
            windowSize,
            horizontalPadding
        )
    }
}
