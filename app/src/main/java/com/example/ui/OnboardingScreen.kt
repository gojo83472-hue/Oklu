package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (assistantName: String, nickname: String, language: String, voiceGender: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var assistantName by remember { mutableStateOf("NOVA") }
    var userNickname by remember { mutableStateOf("Boss") }
    var selectedLanguage by remember { mutableStateOf("auto") }
    var selectedGender by remember { mutableStateOf("female") }

    val assistantPresets = listOf("NOVA", "JARVIS", "AURA", "DK", "TITAN", "CYRA")
    val nicknamePresets = listOf("Boss", "Captain", "Sir", "Commander", "Chief", "Friend")

    val infiniteTransition = rememberInfiniteTransition(label = "arc_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val neonCyan = Color(0xFF00E5FF)
    val deepNavy = Color(0xFF070B14)
    val cardBackground = Color(0xFF0E1524)
    val accentBlue = Color(0xFF2979FF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(deepNavy, Color(0xFF030712), Color(0xFF02040A))
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Human & AI Connection App Logo Emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .border(2.dp, neonCyan.copy(alpha = 0.85f), CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(neonCyan.copy(alpha = 0.3f), Color(0xFF0F172A))
                        ),
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ai_human_logo),
                    contentDescription = "AI Assistant Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(76.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "INITIALIZE ASSISTANT",
                    color = neonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Configure your personalized AI companion & interface",
                    color = Color(0xFF90A4AE),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Card 1: Assistant Name
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = neonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Assistant Name",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "What would you like to call your AI companion?",
                        color = Color(0xFF78909C),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = assistantName,
                        onValueChange = { assistantName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. NOVA, JARVIS, AURA", color = Color(0xFF546E7A)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = Color(0xFF263238),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_assistant_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assistantPresets.forEach { preset ->
                            val isSelected = assistantName.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) neonCyan.copy(alpha = 0.2f) else Color(0xFF151D2C),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) neonCyan else Color(0xFF263238)
                                ),
                                modifier = Modifier.clickable { assistantName = preset }
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) neonCyan else Color(0xFFB0BEC5),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: User Nickname
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "What Should I Call You?",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Your assistant will address you by this name or title in all responses.",
                        color = Color(0xFF78909C),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = userNickname,
                        onValueChange = { userNickname = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Boss, Captain, Sir, your name", color = Color(0xFF546E7A)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = Color(0xFF263238),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_user_nickname_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        nicknamePresets.forEach { preset ->
                            val isSelected = userNickname.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) accentBlue.copy(alpha = 0.25f) else Color(0xFF151D2C),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) accentBlue else Color(0xFF263238)
                                ),
                                modifier = Modifier.clickable { userNickname = preset }
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) Color(0xFF82B1FF) else Color(0xFFB0BEC5),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Language & Voice Model
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Language & Voice",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Language Mode:",
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val languages = listOf(
                            "auto" to "Auto (Tamil/Eng/Thanglish)",
                            "en" to "English Only",
                            "ta" to "தமிழ் (Tamil)"
                        )
                        languages.forEach { (code, label) ->
                            val isSel = selectedLanguage == code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF151D2C),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFFFB300) else Color(0xFF263238)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLanguage = code }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color(0xFFFFD54F) else Color(0xFF90A4AE),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Voice Gender:",
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val genders = listOf(
                            "female" to "Female (Crisp & Melodic)",
                            "male" to "Male (Deep & Resonant)"
                        )
                        genders.forEach { (gen, title) ->
                            val isSel = selectedGender == gen
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) neonCyan.copy(alpha = 0.2f) else Color(0xFF151D2C),
                                border = BorderStroke(1.dp, if (isSel) neonCyan else Color(0xFF263238)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGender = gen }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = neonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = title,
                                        color = if (isSel) neonCyan else Color(0xFFB0BEC5),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Finish Setup Button
            Button(
                onClick = {
                    onComplete(
                        assistantName.ifBlank { "NOVA" },
                        userNickname.ifBlank { "Boss" },
                        selectedLanguage,
                        selectedGender
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_complete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INITIALIZE CORE SYSTEMS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
