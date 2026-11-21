package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.VlcOrange
import kotlinx.coroutines.delay

/**
 * Modern, high-end startup opening animation featuring the 3D V2S Logo
 * with pulsing energy rings, neon glow, and cinematic reveal transitions.
 */
@Composable
fun V2SSplashOpenAnimation(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubtitle by remember { mutableStateOf(false) }
    var showLoadingBar by remember { mutableStateOf(false) }

    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val loadProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    val shimmerSweep by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    LaunchedEffect(Unit) {
        // Step 1: Pop in the 3D logo with spring physics
        alpha.animateTo(1f, animationSpec = tween(300))
        glowAlpha.animateTo(1f, animationSpec = tween(400))
        scale.animateTo(
            targetValue = 1.06f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )

        delay(150)
        showSubtitle = true
        showLoadingBar = true

        // Step 2: Animate progress bar
        loadProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )

        delay(200)

        // Step 3: Smooth cinematic exit
        alpha.animateTo(0f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF241508),
                        Color(0xFF150C05),
                        Color(0xFF090604)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic expanding energy rings behind logo
        Canvas(modifier = Modifier.size(280.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width / 2f

            drawCircle(
                color = VlcOrange.copy(alpha = (ringAlpha * glowAlpha.value).coerceIn(0f, 1f)),
                radius = maxRadius * (pulseRing * 0.7f).coerceIn(0.2f, 1f),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFB703).copy(alpha = (ringAlpha * 0.7f * glowAlpha.value).coerceIn(0f, 1f)),
                radius = maxRadius * (pulseRing * 0.9f).coerceIn(0.2f, 1f),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Soft ambient radial glow
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(scale.value * 1.15f)
                .alpha(glowAlpha.value * 0.45f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            VlcOrange.copy(alpha = 0.55f),
                            Color(0xFFFFB703).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Main 3D Cone Artwork Logo (Seamless dark glass container with glowing border)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale.value)
                    .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = VlcOrange)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF140D07))
                    .border(
                        2.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFFFB703),
                                VlcOrange,
                                Color(0xFFC92A00)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.v2s_cone_glossy_1787004181216),
                    contentDescription = "V2S App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Shimmer laser light reflection across logo
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val heightPx = size.height
                    val startX = shimmerSweep * width
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color(0xFFFFB703).copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(startX + width * 0.35f, heightPx)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // V2S Brand Title with Smooth Staggered Entrance
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(350)) + scaleIn(tween(350, easing = FastOutSlowInEasing), initialScale = 0.85f),
                exit = fadeOut(tween(150))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "V2",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "S",
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = VlcOrange,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "VIDEO • SHORTS • SONGS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        color = Color(0xFFFFB703),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Modern glowing progress line
            if (showLoadingBar) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(loadProgress.value)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFB703), VlcOrange, Color(0xFFFF5400))
                                )
                            )
                    )
                }
            }
        }
    }
}

