package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Automatically adjusts font size so text comfortably fits its bounding container
 * without awkward truncation or clipping.
 */
@Composable
fun AutoResizedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    targetFontSize: TextUnit = style.fontSize,
    minFontSize: TextUnit = 10.sp,
    fontWeight: FontWeight? = style.fontWeight,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    var resizedFontSize by remember(text, targetFontSize) { mutableStateOf(targetFontSize) }
    var shouldDraw by remember(text, targetFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        color = if (color != Color.Unspecified) color else style.color,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        textAlign = textAlign,
        fontSize = resizedFontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = { result ->
            if (result.didOverflowWidth || result.didOverflowHeight) {
                if (resizedFontSize.value > minFontSize.value) {
                    resizedFontSize = (resizedFontSize.value - 1f).sp
                } else {
                    shouldDraw = true
                }
            } else {
                shouldDraw = true
            }
        },
        style = style
    )
}
