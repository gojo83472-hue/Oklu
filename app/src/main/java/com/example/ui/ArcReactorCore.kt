package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class AssistantCoreState {
    IDLE,
    LISTENING,
    THINKING,
    PROCESSING,
    SPEAKING
}

@Composable
fun ArcReactorCore(
    state: AssistantCoreState,
    rmsLevel: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_animations")

    // Rotation angle for outer mechanical ring (clockwise)
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantCoreState.THINKING -> 2000
                    AssistantCoreState.PROCESSING -> 3000
                    AssistantCoreState.LISTENING -> 6000
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Rotation angle for inner segmented ring (counter-clockwise)
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantCoreState.THINKING -> 1500
                    AssistantCoreState.PROCESSING -> 2500
                    AssistantCoreState.SPEAKING -> 4000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Ambient breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantCoreState.LISTENING -> 600
                    AssistantCoreState.SPEAKING -> 450
                    AssistantCoreState.THINKING -> 800
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Wave ripple pulse for speaking / processing
    val ripplePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_pulse"
    )

    // Color theme based on core state
    val (primaryColor, secondaryColor, coreGlow) = when (state) {
        AssistantCoreState.LISTENING -> Triple(
            Color(0xFF00E5FF), // Cyan
            Color(0xFF00E676), // Emerald
            Color(0x8000E5FF)
        )
        AssistantCoreState.THINKING -> Triple(
            Color(0xFFFF9100), // Amber
            Color(0xFFFF1744), // Crimson
            Color(0x80FF9100)
        )
        AssistantCoreState.PROCESSING -> Triple(
            Color(0xFF7C4DFF), // Purple
            Color(0xFF00E5FF), // Cyan
            Color(0x807C4DFF)
        )
        AssistantCoreState.SPEAKING -> Triple(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFFD600), // Gold
            Color(0x9000E5FF)
        )
        AssistantCoreState.IDLE -> Triple(
            Color(0xFF00B0FF), // Soft Sky Cyan
            Color(0xFF0D47A1), // Deep Navy
            Color(0x4000B0FF)
        )
    }

    // Interactive Core container
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("arc_reactor_core")
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val maxRadius = size.toPx() / 2f - 6.dp.toPx()

            // 1. Ambient Background Glow Halo
            val dynamicScale = if (state == AssistantCoreState.LISTENING) {
                1f + (rmsLevel * 0.4f)
            } else {
                breathingPulse
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreGlow, Color.Transparent),
                    center = center,
                    radius = maxRadius * dynamicScale
                ),
                radius = maxRadius * dynamicScale,
                center = center
            )

            // 2. Outermost Static Containment Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Rotating Segmented Outer Ring (DashEffect)
            rotate(outerRotation, center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.8f),
                    radius = maxRadius - 5.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f),
                        cap = StrokeCap.Round
                    )
                )

                // 4 Orbiting Power Nodes
                for (i in 0 until 4) {
                    val angleRad = Math.toRadians((i * 90.0))
                    val nodeRadius = maxRadius - 5.dp.toPx()
                    val nodeX = center.x + (nodeRadius * cos(angleRad)).toFloat()
                    val nodeY = center.y + (nodeRadius * sin(angleRad)).toFloat()
                    drawCircle(
                        color = secondaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 4. Counter-rotating Inner Ring
            rotate(innerRotation, center) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.7f),
                    radius = maxRadius - 16.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 2.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 5. If speaking or listening, draw active concentric wave ripples
            if (state == AssistantCoreState.LISTENING || state == AssistantCoreState.SPEAKING || state == AssistantCoreState.PROCESSING) {
                val rippleRadius = (maxRadius * 0.4f) + (maxRadius * 0.5f * (ripplePulse % 1f))
                val rippleAlpha = (1f - (ripplePulse % 1f)).coerceIn(0f, 1f) * 0.6f
                drawCircle(
                    color = primaryColor.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 6. Central Arc Reactor Core Orb
            val coreOrbRadius = (maxRadius * 0.38f) * dynamicScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreOrbRadius
                ),
                radius = coreOrbRadius,
                center = center
            )

            // 7. Core Inner Pupil
            drawCircle(
                color = Color.White,
                radius = coreOrbRadius * 0.35f,
                center = center
            )
        }
    }
}
