package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentPulse
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet

@Composable
fun VisualizerBars(
    isActive: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp)
    ) {
        val totalWidth = size.width
        val barWidth = (totalWidth / barCount) * 0.55f
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1)
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            val wave = if (isActive) {
                val sinVal = kotlin.math.sin(phase + (progress * 4.5f)).toFloat()
                val heightFactor = 0.25f + (0.75f * kotlin.math.abs(sinVal)) * (0.4f + 0.6f * rmsLevel)
                (size.height * 0.85f * heightFactor).coerceIn(6f, size.height)
            } else {
                6f
            }

            val left = i * (barWidth + spacing)
            val top = centerY - (wave / 2f)

            val brush = Brush.verticalGradient(
                colors = listOf(
                    SecondaryCyan,
                    PrimaryIndigo,
                    TertiaryViolet
                ),
                startY = top,
                endY = top + wave
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, wave),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun OrbVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scale = if (isListening) pulse + (rmsLevel * 0.2f) else if (isSpeaking) pulse else 1f

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing ripple rings
        if (isListening || isSpeaking) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2f) * scale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isListening) AccentPulse.copy(alpha = 0.45f) else TertiaryViolet.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.3f
                    ),
                    radius = radius * 1.25f,
                    center = center
                )
            }
        }

        // Inner core
        Box(
            modifier = Modifier
                .size((64 * scale).dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isListening) {
                            listOf(SecondaryCyan, PrimaryIndigo)
                        } else if (isSpeaking) {
                            listOf(TertiaryViolet, PrimaryIndigo)
                        } else {
                            listOf(PrimaryIndigo, Color(0xFF1E1B4B))
                        }
                    )
                )
        )
    }
}
