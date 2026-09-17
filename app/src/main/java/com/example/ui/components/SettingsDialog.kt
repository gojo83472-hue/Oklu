package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsBottomSheet(
    isTtsEnabled: Boolean,
    ttsPitch: Float,
    ttsRate: Float,
    ttsLanguage: String,
    isCallConfirmationRequired: Boolean,
    isHapticEnabled: Boolean,
    customApiKey: String,
    geminiModel: String,
    assistantName: String,
    userNickname: String,
    voiceGender: String,
    uiLanguage: String,
    showCompanionPet: Boolean,
    screenTimeReminderMinutes: Int,
    isSmsConfirmationRequired: Boolean,
    petMode: String = "emoji",
    petEmojiPersona: String = "robot",
    onSaveSettings: (
        isTtsEnabled: Boolean,
        ttsPitch: Float,
        ttsRate: Float,
        ttsLanguage: String,
        isCallConfirmationRequired: Boolean,
        isHapticEnabled: Boolean,
        customApiKey: String,
        geminiModel: String,
        assistantName: String,
        userNickname: String,
        voiceGender: String,
        uiLanguage: String,
        showCompanionPet: Boolean,
        screenTimeReminderMinutes: Int,
        isSmsConfirmationRequired: Boolean,
        petMode: String,
        petEmojiPersona: String
    ) -> Unit,
    onTestVoice: (pitch: Float, rate: Float, lang: String, gender: String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentTtsEnabled by remember { mutableStateOf(isTtsEnabled) }
    var currentPitch by remember { mutableFloatStateOf(ttsPitch) }
    var currentRate by remember { mutableFloatStateOf(ttsRate) }
    var currentLanguage by remember { mutableStateOf(ttsLanguage) }
    var currentCallConfirmation by remember { mutableStateOf(isCallConfirmationRequired) }
    var currentHaptic by remember { mutableStateOf(isHapticEnabled) }
    var currentApiKey by remember { mutableStateOf(customApiKey) }
    var currentModel by remember { mutableStateOf(geminiModel) }

    var currentAssistantName by remember { mutableStateOf(assistantName) }
    var currentUserNickname by remember { mutableStateOf(userNickname) }
    var currentVoiceGender by remember { mutableStateOf(voiceGender) }
    var currentUiLanguage by remember { mutableStateOf(uiLanguage) }
    var currentShowPet by remember { mutableStateOf(showCompanionPet) }
    var currentPetMode by remember { mutableStateOf(petMode) }
    var currentPetPersona by remember { mutableStateOf(petEmojiPersona) }
    var currentScreenTime by remember { mutableIntStateOf(screenTimeReminderMinutes) }
    var currentSmsConfirmation by remember { mutableStateOf(isSmsConfirmationRequired) }

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var langMenuExpanded by remember { mutableStateOf(false) }

    val assistantPresets = listOf("NOVA", "JARVIS", "AURA", "DK", "TITAN", "CYRA")
    val nicknamePresets = listOf("Boss", "Captain", "Sir", "Commander", "Chief")

    val ttsLanguages = listOf(
        "en_US" to "English (United States)",
        "en_GB" to "English (United Kingdom)",
        "ta_IN" to "தமிழ் (Tamil India)",
        "es_ES" to "Spanish (Spain)",
        "hi_IN" to "Hindi (India)"
    )

    fun saveAll() {
        onSaveSettings(
            currentTtsEnabled,
            currentPitch,
            currentRate,
            currentLanguage,
            currentCallConfirmation,
            currentHaptic,
            currentApiKey,
            currentModel,
            currentAssistantName,
            currentUserNickname,
            currentVoiceGender,
            currentUiLanguage,
            currentShowPet,
            currentScreenTime,
            currentSmsConfirmation,
            currentPetMode,
            currentPetPersona
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            saveAll()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ai_human_logo),
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Assistant Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Human & AI Collaboration Hub",
                            fontSize = 11.sp,
                            color = SecondaryCyan
                        )
                    }
                }
                IconButton(
                    onClick = {
                        saveAll()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("settings_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: IDENTITY & PERSONALIZATION
            Text(
                text = "IDENTITY & PERSONALIZATION",
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Assistant Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App / Assistant Name", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = currentAssistantName,
                        onValueChange = { currentAssistantName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_assistant_name_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        assistantPresets.forEach { preset ->
                            val sel = currentAssistantName.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (sel) SecondaryCyan.copy(alpha = 0.2f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (sel) SecondaryCyan else Color(0xFF334155)),
                                modifier = Modifier.clickable { currentAssistantName = preset }
                            ) {
                                Text(
                                    text = preset,
                                    color = if (sel) SecondaryCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // User Nickname
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Address Me As (Nickname)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = currentUserNickname,
                        onValueChange = { currentUserNickname = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_user_nickname_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        nicknamePresets.forEach { preset ->
                            val sel = currentUserNickname.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (sel) PrimaryIndigo.copy(alpha = 0.25f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (sel) PrimaryIndigo else Color(0xFF334155)),
                                modifier = Modifier.clickable { currentUserNickname = preset }
                            ) {
                                Text(
                                    text = preset,
                                    color = if (sel) Color(0xFF82B1FF) else TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // Voice Gender
                    Text("Voice Gender", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("female" to "Female (Melodic)", "male" to "Male (Resonant)").forEach { (gen, lbl) ->
                            val sel = currentVoiceGender == gen
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (sel) SecondaryCyan.copy(alpha = 0.2f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (sel) SecondaryCyan else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).clickable { currentVoiceGender = gen }
                            ) {
                                Text(
                                    text = lbl,
                                    color = if (sel) SecondaryCyan else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: COMPANION & DIGITAL HEALTH
            Text(
                text = "COMPANION & USAGE REMINDER",
                style = MaterialTheme.typography.labelMedium,
                color = TertiaryViolet,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Floating Pet Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = TertiaryViolet, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Floating Companion Pet", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                            Text("Interactive animated assistant companion", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = currentShowPet,
                            onCheckedChange = { currentShowPet = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TertiaryViolet
                            ),
                            modifier = Modifier.testTag("floating_pet_switch")
                        )
                    }

                    if (currentShowPet) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Pet Mode Selection: Emoji Mode vs Cyber Droid
                        Text(
                            text = "Pet Style",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isEmojiMode = currentPetMode == "emoji"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isEmojiMode) TertiaryViolet.copy(alpha = 0.25f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (isEmojiMode) TertiaryViolet else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { currentPetMode = "emoji" }
                                    .testTag("pet_mode_emoji_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("😊", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Emoji Mode",
                                        color = if (isEmojiMode) Color.White else TextSecondary,
                                        fontWeight = if (isEmojiMode) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            val isDroidMode = currentPetMode == "droid"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDroidMode) SecondaryCyan.copy(alpha = 0.25f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (isDroidMode) SecondaryCyan else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { currentPetMode = "droid" }
                                    .testTag("pet_mode_droid_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🤖", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cyber Droid",
                                        color = if (isDroidMode) Color.White else TextSecondary,
                                        fontWeight = if (isDroidMode) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // If Emoji Mode, show Emoji Persona choices
                        if (currentPetMode == "emoji") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Emoji Character",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val personas = listOf(
                                "robot" to "🤖 Robo",
                                "cat" to "😺 Cat",
                                "fox" to "🦊 Fox",
                                "pup" to "🐶 Pup",
                                "spark" to "💫 Spark"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                personas.forEach { (id, label) ->
                                    val isSelected = currentPetPersona == id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) TertiaryViolet.copy(alpha = 0.3f) else Color(0xFF131C2E),
                                        border = BorderStroke(1.dp, if (isSelected) TertiaryViolet else Color(0xFF334155)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { currentPetPersona = id }
                                            .testTag("pet_persona_$id")
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // Screen-time Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Screen-Time Reminder", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                            Text(
                                text = if (currentScreenTime > 0) "Alerts every $currentScreenTime minutes of activity" else "Reminder disabled",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(0 to "Off", 15 to "15m", 30 to "30m", 60 to "60m").forEach { (mins, label) ->
                            val sel = currentScreenTime == mins
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (sel) SecondaryCyan.copy(alpha = 0.2f) else Color(0xFF131C2E),
                                border = BorderStroke(1.dp, if (sel) SecondaryCyan else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).clickable { currentScreenTime = mins }
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) SecondaryCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 3: VOICE & TTS CUSTOMIZATION
            Text(
                text = "VOICE & SPEECH (TTS)",
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auto-read TTS toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Speak Responses Automatically", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Assistant reads its reply aloud", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = currentTtsEnabled,
                            onCheckedChange = { currentTtsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("tts_enabled_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // Speech Rate Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate", color = TextPrimary, fontSize = 13.sp)
                        Text(
                            text = String.format("%.1fx", currentRate),
                            color = SecondaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = currentRate,
                        onValueChange = { currentRate = it },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = SecondaryCyan,
                            activeTrackColor = SecondaryCyan,
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("tts_rate_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", color = TextPrimary, fontSize = 13.sp)
                        Text(
                            text = String.format("%.1fx", currentPitch),
                            color = TertiaryViolet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = currentPitch,
                        onValueChange = { currentPitch = it },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = TertiaryViolet,
                            activeTrackColor = TertiaryViolet,
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("tts_pitch_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Selector
                    Text("Voice Language", color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { langMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131C2E),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("language_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val selectedName = ttsLanguages.find { it.first == currentLanguage }?.second ?: currentLanguage
                                Text(selectedName, color = TextPrimary, fontSize = 13.sp)
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            ttsLanguages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = if (code == currentLanguage) SecondaryCyan else TextPrimary) },
                                    onClick = {
                                        currentLanguage = code
                                        langMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Voice button
                    OutlinedButton(
                        onClick = { onTestVoice(currentPitch, currentRate, currentLanguage, currentVoiceGender) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PrimaryIndigo),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth().testTag("test_voice_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview Current Voice")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: SAFETY & CONFIRMATIONS
            Text(
                text = "SAFETY & CONFIRMATIONS",
                style = MaterialTheme.typography.labelMedium,
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Call Confirmation switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Call Confirmation Dialog", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Always prompt before placing a phone call", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = currentCallConfirmation,
                            onCheckedChange = { currentCallConfirmation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentGreen
                            ),
                            modifier = Modifier.testTag("call_confirm_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // SMS Confirmation switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SMS Confirmation Dialog", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Review text and recipient before opening messaging app", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = currentSmsConfirmation,
                            onCheckedChange = { currentSmsConfirmation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentGreen
                            ),
                            modifier = Modifier.testTag("sms_confirm_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E3D52))

                    // Haptic switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Feedback", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Vibrate on voice start and actions", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = currentHaptic,
                            onCheckedChange = { currentHaptic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 5: GEMINI AI MODEL
            Text(
                text = "AI INTELLIGENCE",
                style = MaterialTheme.typography.labelMedium,
                color = TertiaryViolet,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Model: $currentModel", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Fast, responsive AI reasoning for Android voice interactions", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = currentApiKey,
                        onValueChange = { currentApiKey = it },
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("Uses default environment key if blank") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TertiaryViolet,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = TertiaryViolet,
                            unfocusedLabelColor = TextMuted,
                            cursorColor = TertiaryViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 6: WINDOWS COMPANION
            Text(
                text = "ECOSYSTEM COMPANIONS",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF0284C7),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Windows Companion Ready", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Sync commands, clipboard, and notifications between your PC and Android assistant seamlessly.", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 7: DATA & HISTORY
            Text(
                text = "DATA MANAGEMENT",
                style = MaterialTheme.typography.labelMedium,
                color = AccentRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showClearConfirmDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                modifier = Modifier.fillMaxWidth().testTag("clear_history_button")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Chat History")
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Clear Chat History?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all stored conversation messages and action logs from your local device database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
