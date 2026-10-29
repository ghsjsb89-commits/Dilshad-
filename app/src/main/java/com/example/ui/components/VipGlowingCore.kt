package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.JarvisStatus
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VipGlowingCore(
    status: JarvisStatus,
    soundWaveLevel: Float, // 0f to 1f
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_animation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Dynamic scale driven by speech audio level
    val audioResponsiveScale = pulseScale + (soundWaveLevel * 0.15f)

    val isListening = status == JarvisStatus.LISTENING
    val isSpeaking = status == JarvisStatus.SPEAKING
    val isReasoning = status == JarvisStatus.REASONING

    val primaryGlowColor = when {
        status == JarvisStatus.ERROR -> JarvisRed
        isListening -> JarvisGoldBright
        isSpeaking -> JarvisCyan
        isReasoning -> JarvisEmerald
        else -> JarvisGold
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("vip_ai_core"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Canvas for rotating energetic rings & Islamic geometric symmetry
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.width / 2 - 8.dp.toPx()
            val middleRadius = size.width / 2 - 28.dp.toPx()
            val innerRingRadius = size.width / 2 - 48.dp.toPx()

            // 1. Ambient Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = if (isListening || isSpeaking) 0.35f else 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius + 20.dp.toPx()
                ),
                radius = outerRadius + 20.dp.toPx(),
                center = center
            )

            // 2. Outer Rotating Dashed Ring
            drawCircle(
                color = JarvisGold.copy(alpha = 0.4f),
                radius = outerRadius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), rotationAngle)
                )
            )

            // 3. Middle Counter-Rotating Emerald Ring
            drawCircle(
                color = JarvisEmerald.copy(alpha = 0.5f),
                radius = middleRadius,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), counterRotation)
                )
            )

            // 4. Islamic Star Geometric Accent Nodes (8-fold symmetry)
            val nodes = 8
            for (i in 0 until nodes) {
                val angleRad = Math.toRadians((i * (360.0 / nodes) + rotationAngle).toDouble())
                val nx = center.x + (middleRadius * cos(angleRad)).toFloat()
                val ny = center.y + (middleRadius * sin(angleRad)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) JarvisGold else JarvisEmerald,
                    radius = 3.dp.toPx(),
                    center = Offset(nx, ny)
                )
            }

            // 5. Inner Core Border
            drawCircle(
                color = primaryGlowColor.copy(alpha = 0.8f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Inner Core Sphere (Tappable Microphone / Action Center)
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(audioResponsiveScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            JarvisSurfaceHighlight,
                            JarvisSurfaceDark,
                            JarvisBlack
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryGlowColor, JarvisGoldDark, JarvisEmerald)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = JarvisGold)
                ) {
                    onMicClick()
                }
                .testTag("core_microphone_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isListening || isSpeaking) {
                    // Real-time animated sound-wave bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(36.dp)
                    ) {
                        val baseLevels = listOf(0.4f, 0.8f, 1.0f, 0.7f, 0.5f)
                        baseLevels.forEachIndexed { idx, weight ->
                            val heightRatio = (weight * (0.3f + soundWaveLevel * 0.7f)).coerceIn(0.15f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(heightRatio)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isListening) JarvisGoldBright else JarvisCyan
                                    )
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (status == JarvisStatus.ERROR) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Activate Voice Command",
                        tint = primaryGlowColor,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }
    }
}
