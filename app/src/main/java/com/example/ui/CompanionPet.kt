package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPulse
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

enum class PetMood {
    IDLE,
    HAPPY,
    LISTENING,
    ALERT
}

enum class PetModeType {
    EMOJI,
    DROID
}

data class FloatingParticle(
    val id: Long,
    val text: String,
    val offsetX: Float,
    val offsetY: Float
)

@Composable
fun CompanionPet(
    mood: PetMood,
    assistantName: String,
    onPetClicked: () -> Unit,
    modifier: Modifier = Modifier,
    userNickname: String = "Boss",
    petMode: PetModeType = PetModeType.EMOJI,
    persona: String = "robot"
) {
    val coroutineScope = rememberCoroutineScope()
    var temporaryHappy by remember { mutableStateOf(false) }
    var speechBubbleText by remember { mutableStateOf<String?>(null) }
    val particles = remember { mutableStateListOf<FloatingParticle>() }

    // Tap scale bounce
    val tapScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pet_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Gentle rotation wiggle for emoji
    val wiggleAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    val currentMood = if (temporaryHappy) PetMood.HAPPY else mood

    val glowColor = when (currentMood) {
        PetMood.HAPPY -> AccentGreen
        PetMood.LISTENING -> SecondaryCyan
        PetMood.ALERT -> AccentPulse
        PetMood.IDLE -> TertiaryViolet
    }

    // Interactive Tap Reaction
    fun onPetTapped() {
        temporaryHappy = true
        coroutineScope.launch {
            // Spring scale effect
            tapScale.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            tapScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }

        // Emit celebratory floating emojis
        val particleOptions = listOf("✨", "❤️", "⭐", "🎉", "⚡", "💖")
        val randomEmoji = particleOptions.random()
        val newParticle = FloatingParticle(
            id = System.currentTimeMillis(),
            text = randomEmoji,
            offsetX = Random.nextFloat() * 40f - 20f,
            offsetY = -20f
        )
        particles.add(newParticle)

        // Show brief speech bubble
        val greetings = listOf(
            "Hey, $userNickname!",
            "$assistantName ready!",
            "At your service! ✨",
            "Listening! 👂",
            "Always here! 💖"
        )
        speechBubbleText = greetings.random()

        coroutineScope.launch {
            delay(1800)
            temporaryHappy = false
            speechBubbleText = null
            particles.remove(newParticle)
        }

        onPetClicked()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Floating speech bubble
        AnimatedVisibility(
            visible = speechBubbleText != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, glowColor.copy(alpha = 0.5f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .testTag("pet_speech_bubble")
            ) {
                Text(
                    text = speechBubbleText ?: "",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, floatOffset.roundToInt()) }
                .scale(tapScale.value)
                .size(56.dp)
                .shadow(12.dp, CircleShape, spotColor = glowColor)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(2.dp, glowColor.copy(alpha = 0.85f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onPetTapped()
                }
                .testTag("companion_pet")
        ) {
            if (petMode == PetModeType.EMOJI) {
                // ==========================================
                // EMOJI MODE COMPANION TYPE
                // ==========================================
                EmojiPetDisplay(
                    mood = currentMood,
                    persona = persona,
                    wiggle = wiggleAngle
                )
            } else {
                // ==========================================
                // CYBER DROID VISOR MODE
                // ==========================================
                CyberDroidVisor(
                    currentMood = currentMood,
                    glowColor = glowColor
                )
            }
        }
    }
}

/**
 * Adorable, expressive Emoji-Mode Pet Display
 */
@Composable
private fun EmojiPetDisplay(
    mood: PetMood,
    persona: String,
    wiggle: Float
) {
    // Select emoji based on persona and mood
    val emojiChar = when (persona) {
        "cat" -> when (mood) {
            PetMood.IDLE -> "😺"
            PetMood.HAPPY -> "😻"
            PetMood.LISTENING -> "😸"
            PetMood.ALERT -> "🙀"
        }
        "fox" -> when (mood) {
            PetMood.IDLE -> "🦊"
            PetMood.HAPPY -> "🥰"
            PetMood.LISTENING -> "🤩"
            PetMood.ALERT -> "🧐"
        }
        "pup" -> when (mood) {
            PetMood.IDLE -> "🐶"
            PetMood.HAPPY -> "🐾"
            PetMood.LISTENING -> "🦻"
            PetMood.ALERT -> "🐕"
        }
        "spark" -> when (mood) {
            PetMood.IDLE -> "💫"
            PetMood.HAPPY -> "💖"
            PetMood.LISTENING -> "✨"
            PetMood.ALERT -> "⚡"
        }
        else -> {
            // Default "robot" persona
            when (mood) {
                PetMood.IDLE -> "🤖"
                PetMood.HAPPY -> "🥰"
                PetMood.LISTENING -> "🤩"
                PetMood.ALERT -> "⚡"
            }
        }
    }

    val moodKaomoji = when (mood) {
        PetMood.IDLE -> "•‿•"
        PetMood.HAPPY -> "≧◡≦"
        PetMood.LISTENING -> "⊙_⊙"
        PetMood.ALERT -> "•̀_•́"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (mood) {
                        PetMood.HAPPY -> Color(0x3310B981)
                        PetMood.LISTENING -> Color(0x3300E5FF)
                        PetMood.ALERT -> Color(0x33F59E0B)
                        PetMood.IDLE -> Color(0x22818CF8)
                    }
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emojiChar,
                fontSize = 26.sp,
                modifier = Modifier
                    .offset(y = (-1).dp)
                    .scale(if (mood == PetMood.LISTENING || mood == PetMood.HAPPY) 1.15f else 1.0f)
            )

            // Micro kaomoji mouth/eyes below for extra personality
            Text(
                text = moodKaomoji,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = when (mood) {
                    PetMood.HAPPY -> Color(0xFF6EE7B7)
                    PetMood.LISTENING -> Color(0xFF7DD3FC)
                    PetMood.ALERT -> Color(0xFFFDE68A)
                    PetMood.IDLE -> Color(0xFFC7D2FE)
                },
                modifier = Modifier.offset(y = (-3).dp)
            )
        }
    }
}

/**
 * High-tech Cyber Droid Visor Display
 */
@Composable
private fun CyberDroidVisor(
    currentMood: PetMood,
    glowColor: Color
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

        // Expressive Visor graphics
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
