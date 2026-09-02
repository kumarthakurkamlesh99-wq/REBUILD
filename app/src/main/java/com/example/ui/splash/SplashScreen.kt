package com.example.ui.splash

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val AccentBlue = Color(0xFF4FB8FF)
private val DeepIce = Color(0xFF00E5FF)
private val BgBlack = Color(0xFF000000)
private val TextGray = Color(0xFF5A6572)

@Composable
fun RebuildSplashScreen(
    onFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val sweepProgress = remember { Animatable(0f) }
    var showStatus by remember { mutableStateOf(false) }

    // Continuous "breathing" glow
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheAlpha"
    )

    // Dots blink animation
    val dotBlink by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotBlink"
    )

    LaunchedEffect(Unit) {
        // Parallel entrance animations for instant responsive speed
        coroutineScope {
            launch {
                logoAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
            }
            launch {
                logoScale.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
            }
            launch {
                glowAlpha.animateTo(1f, tween(450))
            }
            launch {
                delay(200)
                sweepProgress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
        }
        showStatus = true
        // Brief hold to view the brand emblem before transitioning seamlessly
        delay(400)
        onFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .testTag("rebuild_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Subtle ambient cyber particle points
        val particles = remember {
            List(16) {
                Triple(
                    Random.nextFloat(),
                    Random.nextFloat() * 0.6f + 0.4f,
                    Random.nextFloat() * 3f + 1.5f
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { (xRatio, yRatio, radius) ->
                val particleAlpha = (0.25f + 0.35f * breathe).coerceIn(0f, 1f)
                drawCircle(
                    color = AccentBlue.copy(alpha = particleAlpha),
                    radius = radius,
                    center = Offset(xRatio * widthPx, (yRatio * heightPx + breathe * 15) % heightPx)
                )
            }
        }

        // Center Logo container
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            // Soft atmospheric radial aura
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentBlue.copy(alpha = (0.45f * breathe * glowAlpha.value).coerceIn(0f, 1f)),
                                DeepIce.copy(alpha = (0.18f * breathe * glowAlpha.value).coerceIn(0f, 1f)),
                                Color.Transparent
                            )
                        )
                    )
            )

            // High-resolution brand logo asset
            Image(
                painter = painterResource(id = R.drawable.ic_rebuild_logo),
                contentDescription = "REBUILD OS",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Light sheen sweep overlay
            if (sweepProgress.value in 0.01f..0.99f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFB4E1FF).copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                startX = sweepProgress.value * 800f - 200f,
                                endX = sweepProgress.value * 800f + 200f
                            )
                        )
                )
            }
        }

        // Bottom Loading status
        if (showStatus) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOADING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "● ● ●",
                        fontSize = 8.sp,
                        color = AccentBlue.copy(alpha = dotBlink),
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AI MONK MODE PROTOCOL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
