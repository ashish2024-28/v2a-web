
package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.R
import com.example.ui.theme.VlcOrange
import androidx.compose.material3.Icon
// VlcIcon.kt
// Required imports for the simple logo:

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow

// ═════════════════════════════════════════════════════════════════════════════
// V2S COLOR PALETTE CONSTANTS
// ═════════════════════════════════════════════════════════════════════════════
val V2SOrangeLight = Color(0xFFFFE66D)
val V2SOrangeMain = Color(0xFFFFD000)
val V2SOrangeDark = Color(0xFFC7A600)
val V2SOrangeDeep = Color(0xFF8A7200)
val V2SGoldAccent = Color(0xFFFFE66D)

// ═════════════════════════════════════════════════════════════════════════════
// PRODUCTION FIX: Safe image loader that handles fallback properly
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun SafeImageResource(
    resourceId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackContent: @Composable () -> Unit
) {
    SubcomposeAsyncImage(
        model = resourceId,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            // While loading, show fallback
            fallbackContent()
        },
        error = {
            // When error occurs, gracefully show fallback content without noise
            fallbackContent()
        },
        success = {
            SubcomposeAsyncImageContent()
        }
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// 1. V2S 3D PLAY SYMBOL (VIDEOS ICON)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SVideosSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer 3D Rounded Triangle Body
        val bodyPath = Path().apply {
            moveTo(w * 0.18f, h * 0.12f)
            quadraticTo(w * 0.26f, h * 0.05f, w * 0.38f, h * 0.12f)
            lineTo(w * 0.88f, h * 0.44f)
            quadraticTo(w * 0.98f, h * 0.50f, w * 0.88f, h * 0.56f)
            lineTo(w * 0.38f, h * 0.88f)
            quadraticTo(w * 0.26f, h * 0.95f, w * 0.18f, h * 0.88f)
            close()
        }

        // Drop shadow / base layer
        drawPath(
            bodyPath,
            brush = Brush.verticalGradient(
                colors = listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
            )
        )

        // Bevel highlight around border
        drawPath(
            bodyPath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
        )

        // Inner hollow/embossed Play triangle
        val innerPlay = Path().apply {
            moveTo(w * 0.38f, h * 0.32f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.38f, h * 0.68f)
            close()
        }
        drawPath(innerPlay, color = Color.White)

        // Top glossy flare
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = w * 0.18f,
            center = Offset(w * 0.40f, h * 0.30f)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. V2S 3D SHORTS SYMBOL (RIBBON S REEL)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SShortsSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // S-shaped glossy body with rounded ends
        val sPath = Path().apply {
            moveTo(w * 0.32f, h * 0.12f)
            lineTo(w * 0.68f, h * 0.12f)
            quadraticTo(w * 0.92f, h * 0.12f, w * 0.90f, h * 0.36f)
            quadraticTo(w * 0.88f, h * 0.52f, w * 0.60f, h * 0.52f)
            lineTo(w * 0.40f, h * 0.52f)
            quadraticTo(w * 0.10f, h * 0.52f, w * 0.10f, h * 0.74f)
            quadraticTo(w * 0.12f, h * 0.92f, w * 0.38f, h * 0.92f)
            lineTo(w * 0.72f, h * 0.92f)
        }

        drawPath(
            sPath,
            brush = Brush.verticalGradient(
                colors = listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
            ),
            style = Stroke(width = w * 0.24f, cap = StrokeCap.Round)
        )

        // Gloss highlight
        drawPath(
            sPath,
            color = Color.White.copy(alpha = 0.40f),
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )

        // Center white play mark
        val playPath = Path().apply {
            moveTo(w * 0.42f, h * 0.38f)
            lineTo(w * 0.64f, h * 0.50f)
            lineTo(w * 0.42f, h * 0.62f)
            close()
        }
        drawPath(playPath, color = Color.White)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. V2S 3D SONGS SYMBOL (DOUBLE EIGHTH NOTES)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SSongsSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val noteGradient = Brush.verticalGradient(
            colors = listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
        )

        // Left note head
        drawOval(
            brush = noteGradient,
            topLeft = Offset(w * 0.10f, h * 0.60f),
            size = Size(w * 0.34f, h * 0.28f)
        )
        // Right note head
        drawOval(
            brush = noteGradient,
            topLeft = Offset(w * 0.58f, h * 0.48f),
            size = Size(w * 0.34f, h * 0.28f)
        )

        // Note stems
        drawRoundRect(
            brush = noteGradient,
            topLeft = Offset(w * 0.34f, h * 0.15f),
            size = Size(w * 0.10f, h * 0.60f),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        drawRoundRect(
            brush = noteGradient,
            topLeft = Offset(w * 0.82f, h * 0.08f),
            size = Size(w * 0.10f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.05f)
        )

        // Connecting Beam
        val beamPath = Path().apply {
            moveTo(w * 0.34f, h * 0.15f)
            lineTo(w * 0.92f, h * 0.08f)
            lineTo(w * 0.92f, h * 0.26f)
            lineTo(w * 0.34f, h * 0.33f)
            close()
        }
        drawPath(beamPath, brush = noteGradient)

        // Highlights for 3D gloss
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = w * 0.07f,
            center = Offset(w * 0.24f, h * 0.68f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = w * 0.07f,
            center = Offset(w * 0.72f, h * 0.56f)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. V2S 3D PLAYLIST SYMBOL (3 HORIZONTAL BARS WITH MUSIC STEM)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SPlaylistSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val playlistGradient = Brush.horizontalGradient(
            listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
        )

        val barHeight = h * 0.13f
        val radius = CornerRadius(barHeight / 2)

        // Top bar
        drawRoundRect(
            brush = playlistGradient,
            topLeft = Offset(w * 0.12f, h * 0.22f),
            size = Size(w * 0.44f, barHeight),
            cornerRadius = radius
        )
        // Middle bar
        drawRoundRect(
            brush = playlistGradient,
            topLeft = Offset(w * 0.12f, h * 0.44f),
            size = Size(w * 0.40f, barHeight),
            cornerRadius = radius
        )
        // Bottom bar
        drawRoundRect(
            brush = playlistGradient,
            topLeft = Offset(w * 0.12f, h * 0.66f),
            size = Size(w * 0.32f, barHeight),
            cornerRadius = radius
        )

        // Musical note stem and head on the right
        // Stem
        drawRoundRect(
            brush = playlistGradient,
            topLeft = Offset(w * 0.72f, h * 0.18f),
            size = Size(w * 0.10f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        // Note head oval
        drawOval(
            brush = playlistGradient,
            topLeft = Offset(w * 0.52f, h * 0.56f),
            size = Size(w * 0.28f, h * 0.24f)
        )
        // Upper flag
        val flagPath = Path().apply {
            moveTo(w * 0.72f, h * 0.18f)
            quadraticTo(w * 0.88f, h * 0.22f, w * 0.90f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.38f)
            quadraticTo(w * 0.80f, h * 0.28f, w * 0.72f, h * 0.28f)
            close()
        }
        drawPath(flagPath, brush = playlistGradient)

        // Highlights for gloss
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = w * 0.05f,
            center = Offset(w * 0.64f, h * 0.64f)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(w * 0.16f, h * 0.24f),
            end = Offset(w * 0.48f, h * 0.24f),
            strokeWidth = h * 0.03f,
            cap = StrokeCap.Round
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4B. V2S 3D FOLDER SYMBOL
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SFolderSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val folderGradient = Brush.verticalGradient(
            listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
        )

        // Back tab
        val tabPath = Path().apply {
            moveTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.44f, h * 0.22f)
            lineTo(w * 0.54f, h * 0.34f)
            lineTo(w * 0.88f, h * 0.34f)
            lineTo(w * 0.88f, h * 0.78f)
            lineTo(w * 0.12f, h * 0.78f)
            close()
        }
        drawPath(tabPath, brush = Brush.verticalGradient(listOf(V2SOrangeDark, V2SOrangeDeep)))

        // Front folder body
        val frontPath = Path().apply {
            moveTo(w * 0.08f, h * 0.35f)
            lineTo(w * 0.92f, h * 0.35f)
            lineTo(w * 0.88f, h * 0.84f)
            lineTo(w * 0.12f, h * 0.84f)
            close()
        }
        drawPath(frontPath, brush = folderGradient)

        // Gloss highlight line across top
        drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = Offset(w * 0.10f, h * 0.36f),
            end = Offset(w * 0.90f, h * 0.36f),
            strokeWidth = h * 0.04f,
            cap = StrokeCap.Round
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. V2S 3D EQUALIZER SYMBOL
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SEqualizerSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val barWidth = w * 0.12f
        val heights = listOf(0.45f, 0.80f, 0.60f, 0.95f, 0.50f)
        val xPositions = listOf(0.12f, 0.30f, 0.48f, 0.66f, 0.84f)

        val gradient = Brush.verticalGradient(
            listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
        )

        for (i in heights.indices) {
            val barH = h * heights[i]
            val top = h * 0.90f - barH
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(w * xPositions[i] - barWidth / 2, top),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2)
            )
            // Top highlight dot
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = barWidth * 0.3f,
                center = Offset(w * xPositions[i], top + barWidth / 2)
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. V2S 3D CONE SYMBOL (V2S HYBRID CONE) - RADIANT YELLOW LUXURY EMBLEM
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SConeSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    withGlow: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (withGlow) 8.dp else 3.dp,
                shape = RoundedCornerShape(size * 0.28f),
                spotColor = Color(0xFFFFD600).copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF176),
                        Color(0xFFFFD600),
                        Color(0xFFFFB300)
                    )
                )
            )
            .then(
                if (withGlow) {
                    Modifier.border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFFE0),
                                Color(0xFFFFE082),
                                Color(0xFFFFA000).copy(alpha = 0.8f)
                            )
                        ),
                        RoundedCornerShape(size * 0.28f)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.v2s_yellow_logo_1787594287970),
            contentDescription = "V2S logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. V2S COMPLETE 3D LARGE LOGO (CONE + 3D V2S SHIELD WITH PULSE ANIMATION)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SLarge3DLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_breathing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFFD600).copy(alpha = 0.7f))
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF59D),
                        Color(0xFFFFD600),
                        Color(0xFFFFB300)
                    )
                )
            )
            .border(
                2.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFFFE0),
                        Color(0xFFFFD600),
                        Color(0xFFFF9100)
                    )
                ),
                RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.v2s_yellow_logo_1787594287970),
            contentDescription = "V2S logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 8. V2S HORIZONTAL BRAND LOGO (CLEAN TEXT-ONLY BRANDING, COMPACT & NO LOGO ICON)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SBrandLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    showTagline: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "V",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Text(
                text = "2",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9E00),
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Text(
                text = "S",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }

        if (showTagline) {
            Text(
                text = "VIDEO • SHORTS • SONGS",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp,
                color = Color(0xFFFFD600),
                maxLines = 1
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 8A. V2S HOME LOGO (COMPACT TEXT-ONLY BRANDING)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SHomeLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    showTagline: Boolean = true
) {
    V2SBrandLogo(
        modifier = modifier,
        iconSize = iconSize,
        showTagline = showTagline
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// 8B. V2S HERO BANNER LOGO (FEATURING FULL 3D HERO ARTWORK & SHIMMER LASER)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SHeroBannerLogo(
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = VlcOrange.copy(alpha = 0.6f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0F0A06))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFE66D).copy(alpha = 0.9f),
                        VlcOrange,
                        Color(0xFFFFD000).copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.v2s_hero_black_cone_1787004603846),
            contentDescription = "V2S Hero Brand",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Animated laser reflection sweep
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val heightPx = size.height
            val startX = shimmerProgress * width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFFFFE66D).copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + width * 0.35f, heightPx)
                )
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 9. V2S 3D CIRCULAR PLAYBACK CONTROL BUTTON
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SCircleControlButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    highlight: Boolean = false
) { 
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (highlight) 12.dp else 5.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    if (highlight) {
                        listOf(Color(0xFFFFE66D), Color(0xFFFFD000), Color(0xFFC92A00))
                    } else {
                        listOf(Color(0xFFFF8500), Color(0xFFFFD000), Color(0xFFC7A600))
                    }
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 10. V2S CATEGORY PILL (VIDEOS • SHORTS • SONGS)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SCategoryPill(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(V2SOrangeLight, V2SOrangeMain, V2SOrangeDark)
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF241A12), Color(0xFF19120C))
                    )
                }
            )
            .border(
                1.2.dp,
                if (selected) Color.White.copy(alpha = 0.5f) else V2SOrangeMain.copy(alpha = 0.35f),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = if (selected) Color.White else V2SOrangeLight
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 11. V2S COMPACT IMAGE LOGO
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun V2SImageLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    V2SVideosSymbol(modifier = modifier, size = size)
}

// ═════════════════════════════════════════════════════════════════════════════
// 12. VLC CONE ICON (BACKWARD COMPATIBILITY)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun VlcConeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 45.dp
) {
    V2SConeSymbol(modifier = modifier, size = size)
}

@Composable
fun VlcPlayitBrandHeader(
    modifier: Modifier = Modifier,
    title: String = "V2S"
) {
    V2SBrandLogo(modifier = modifier, iconSize = 30.dp, showTagline = true)
} 
// VlcIcon.kt
// Add this simple professional icon and use it wherever the old
// V2SConeSymbol/VlcConeIcon was being used.

@Composable
fun V2SSimpleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Icon(
        imageVector = Icons.Filled.PlayArrow,
        contentDescription = "V2S",
        tint = VlcOrange,
        modifier = modifier.size(size)
    )
}