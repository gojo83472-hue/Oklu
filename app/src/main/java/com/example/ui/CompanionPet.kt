package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class PetMood {
    IDLE,
    HAPPY,
    LISTENING,
    ALERT
}

@Composable
fun CompanionPet(
    mood: PetMood,
    assistantName: String,
    onPetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var temporaryHappy by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pet_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    val currentMood = if (temporaryHappy) PetMood.HAPPY else mood

    val neonCyan = Color(0xFF00E5FF)
    val neonAmber = Color(0xFFFFB300)
    val bodyColor = Color(0xFF0F172A)
    val glowColor = when (currentMood) {
        PetMood.HAPPY -> Color(0xFF00E676)
        PetMood.LISTENING -> neonCyan
        PetMood.ALERT -> neonAmber
        PetMood.IDLE -> Color(0xFF38BDF8)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset { IntOffset(0, floatOffset.roundToInt()) }
            .size(54.dp)
            .shadow(10.dp, CircleShape, spotColor = glowColor)
            .clip(CircleShape)
            .background(bodyColor)
            .border(2.dp, glowColor.copy(alpha = 0.8f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                temporaryHappy = true
                onPetClicked()
            }
            .testTag("companion_pet")
    ) {
        Canvas(modifier = Modifier.size(42.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Holographic visor glass
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                ),
                topLeft = Offset(center.x - 14.dp.toPx(), center.y - 8.dp.toPx()),
                size = Size(28.dp.toPx(), 16.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Eyes / Expressive Visor graphics
            when (currentMood) {
                PetMood.HAPPY -> {
                    // Curved happy eyes (^_^)
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - 10.dp.toPx(), center.y - 5.dp.toPx()),
                        size = Size(7.dp.toPx(), 7.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x + 3.dp.toPx(), center.y - 5.dp.toPx()),
                        size = Size(7.dp.toPx(), 7.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                PetMood.LISTENING -> {
                    // Big glowing listening eyes
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(center.x - 6.dp.toPx(), center.y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(center.x + 6.dp.toPx(), center.y)
                    )
                    // Antenna pulse line on top
                    drawLine(
                        color = glowColor,
                        start = Offset(center.x, center.y - 12.dp.toPx()),
                        end = Offset(center.x, center.y - 16.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                PetMood.ALERT -> {
                    // Focused slant eyes
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x - 9.dp.toPx(), center.y - 3.dp.toPx()),
                        end = Offset(center.x - 3.dp.toPx(), center.y + 1.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x + 9.dp.toPx(), center.y - 3.dp.toPx()),
                        end = Offset(center.x + 3.dp.toPx(), center.y + 1.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                PetMood.IDLE -> {
                    // Calm oval eyes
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(center.x - 8.dp.toPx(), center.y - 3.dp.toPx()),
                        size = Size(5.dp.toPx(), 7.dp.toPx())
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(center.x + 3.dp.toPx(), center.y - 3.dp.toPx()),
                        size = Size(5.dp.toPx(), 7.dp.toPx())
                    )
                }
            }
        }
    }
}
