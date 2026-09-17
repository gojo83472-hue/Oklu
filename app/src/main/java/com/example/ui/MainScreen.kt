package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.action.AndroidAction
import com.example.ui.components.CallConfirmDialog
import com.example.ui.components.MessageBubble
import com.example.ui.components.QuickActionChips
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.SmsConfirmDialog
import com.example.ui.components.VisualizerBars
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPulse
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val assistantName by viewModel.assistantName.collectAsStateWithLifecycle()
    val userNickname by viewModel.userNickname.collectAsStateWithLifecycle()
    val voiceGender by viewModel.voiceGender.collectAsStateWithLifecycle()
    val uiLanguage by viewModel.uiLanguage.collectAsStateWithLifecycle()
    val showCompanionPet by viewModel.showCompanionPet.collectAsStateWithLifecycle()
    val screenTimeReminderMinutes by viewModel.screenTimeReminderMinutes.collectAsStateWithLifecycle()
    val isSmsConfirmationRequired by viewModel.isSmsConfirmationRequired.collectAsStateWithLifecycle()
    val smsPendingAction by viewModel.smsPendingAction.collectAsStateWithLifecycle()
    val screenTimeAlert by viewModel.screenTimeAlert.collectAsStateWithLifecycle()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.rmsLevel.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by viewModel.speakingMessageId.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val callPendingAction by viewModel.callPendingAction.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // First Launch: Show full-screen onboarding if not completed
    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            onComplete = { chosenName, chosenNick, chosenLang, chosenGender ->
                viewModel.completeOnboarding(chosenName, chosenNick, chosenLang, chosenGender)
            }
        )
        return
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle incoming notifications / toasts
    LaunchedEffect(Unit) {
        viewModel.userNotifications.collect { note ->
            Toast.makeText(context, note, Toast.LENGTH_SHORT).show()
        }
    }

    // Audio recording permission launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, context.getString(R.string.mic_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    fun triggerVoiceInput() {
        if (isListening) {
            viewModel.stopListening()
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                viewModel.startListening()
            } else {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val currentCoreState = when {
        isListening -> AssistantCoreState.LISTENING
        isSpeaking -> AssistantCoreState.SPEAKING
        isProcessing -> AssistantCoreState.PROCESSING
        else -> AssistantCoreState.IDLE
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Glowing status indicator orb
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val orbScale by infiniteTransition.animateFloat(
                            initialValue = 0.85f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "orbScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(if (isListening || isSpeaking) orbScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListening -> AccentPulse
                                        isSpeaking -> TertiaryViolet
                                        isProcessing -> SecondaryCyan
                                        else -> AccentGreen
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = assistantName.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (userNickname.isNotBlank()) "$statusText • $userNickname" else statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isListening -> AccentPulse
                                    isSpeaking -> TertiaryViolet
                                    isProcessing -> SecondaryCyan
                                    else -> TextSecondary
                                },
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                ),
                actions = {
                    // Quick Mute/Unmute toggle
                    IconButton(
                        onClick = {
                            val newTts = !viewModel.preferences.isTtsEnabled
                            viewModel.preferences.isTtsEnabled = newTts
                            if (!newTts) {
                                viewModel.stopSpeaking()
                            }
                            Toast.makeText(
                                context,
                                if (newTts) "Voice responses enabled" else "Voice responses muted",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("tts_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (viewModel.preferences.isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle TTS",
                            tint = if (viewModel.preferences.isTtsEnabled) SecondaryCyan else TextMuted
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Main Chat / Arc Reactor Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        // Empty state: Futuristic Arc Reactor Core with holographic invitation
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CORE ONLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Arc Reactor Core
                            ArcReactorCore(
                                state = currentCoreState,
                                rmsLevel = rmsLevel,
                                onClick = { triggerVoiceInput() },
                                size = 160.dp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Ready, $userNickname",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tap the arc reactor to give a voice command in English, தமிழ் (Tamil), or Thanglish.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        // Active Chat Conversation
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("chat_messages_list"),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            // Mini reactor status header inside the scroll when active
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ArcReactorCore(
                                        state = currentCoreState,
                                        rmsLevel = rmsLevel,
                                        onClick = { triggerVoiceInput() },
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$assistantName is active • $userNickname",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            items(messages, key = { it.id }) { msg ->
                                MessageBubble(
                                    message = msg,
                                    isSpeakingThisMessage = isSpeaking && speakingMessageId == msg.id,
                                    onSpeak = { text -> viewModel.speakText(text, msg.id) },
                                    onStopSpeak = { viewModel.stopSpeaking() },
                                    onExecuteAction = { action -> viewModel.executeAction(action, msg.id) }
                                )
                            }
                        }
                    }
                }

                // Audio visualizer waveform when listening or speaking
                AnimatedVisibility(
                    visible = isListening || isSpeaking,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VisualizerBars(
                            isActive = isListening || isSpeaking,
                            rmsLevel = if (isListening) rmsLevel else 0.5f
                        )
                    }
                }

                // Quick Action Suggestion Chips
                QuickActionChips(
                    onActionSelected = { action ->
                        when (action) {
                            is AndroidAction.MakeCall -> {
                                viewModel.executeAction(action)
                            }
                            is AndroidAction.SendMessage -> {
                                viewModel.processUserMessage("Send message")
                            }
                            is AndroidAction.OpenCamera -> {
                                viewModel.processUserMessage("Open camera")
                            }
                            is AndroidAction.OpenGallery -> {
                                viewModel.processUserMessage("Open gallery")
                            }
                            is AndroidAction.OpenSettings -> {
                                viewModel.processUserMessage("Open settings")
                            }
                            is AndroidAction.OpenChrome -> {
                                viewModel.processUserMessage("Open Chrome")
                            }
                            is AndroidAction.OpenMusic -> {
                                viewModel.processUserMessage("Play music")
                            }
                            is AndroidAction.OpenYouTube -> {
                                viewModel.processUserMessage("Open YouTube")
                            }
                            is AndroidAction.RecordVideo -> {
                                viewModel.processUserMessage("Record video")
                            }
                            is AndroidAction.PlayVideo -> {
                                viewModel.processUserMessage("Play video")
                            }
                            is AndroidAction.ToggleFlashlight -> {
                                viewModel.executeAction(action)
                            }
                            is AndroidAction.ControlVolume -> {
                                viewModel.executeAction(action)
                            }
                            else -> {
                                viewModel.executeAction(action)
                            }
                        }
                    },
                    onQuerySelected = { query ->
                        viewModel.processUserMessage(query)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Input & Voice Control Panel
                Surface(
                    color = DarkSurface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Text Input field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (isListening) stringResource(R.string.listening_prompt) else "Ask anything or command $assistantName…",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryCyan,
                                unfocusedBorderColor = Color(0xFF2E3D52),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = SecondaryCyan,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            ),
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        viewModel.processUserMessage(inputText)
                                        inputText = ""
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (inputText.isNotBlank()) {
                            // Send text button
                            IconButton(
                                onClick = {
                                    viewModel.processUserMessage(inputText)
                                    inputText = ""
                                    keyboardController?.hide()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryCyan)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Prominent Animated Glowing Microphone FAB
                            val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
                            val micGlowScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "mic_glow_scale"
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(56.dp)
                            ) {
                                if (isListening) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .scale(micGlowScale)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        AccentPulse.copy(alpha = 0.5f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }

                                FloatingActionButton(
                                    onClick = { triggerVoiceInput() },
                                    shape = CircleShape,
                                    containerColor = if (isListening) AccentPulse else SecondaryCyan,
                                    contentColor = Color.Black,
                                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                    modifier = Modifier
                                        .size(50.dp)
                                        .testTag("voice_mic_fab")
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Companion Pet (if enabled in settings)
            if (showCompanionPet) {
                val petMood = when {
                    isListening -> PetMood.LISTENING
                    isSpeaking -> PetMood.HAPPY
                    isProcessing -> PetMood.ALERT
                    else -> PetMood.IDLE
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp, end = 16.dp)
                ) {
                    CompanionPet(
                        mood = petMood,
                        assistantName = assistantName,
                        onPetClicked = { triggerVoiceInput() }
                    )
                }
            }
        }
    }

    // Call Confirmation Dialog
    if (callPendingAction != null) {
        CallConfirmDialog(
            initialPhoneNumber = callPendingAction?.phoneNumber ?: "",
            onConfirm = { confirmedNumber ->
                viewModel.confirmCall(confirmedNumber)
            },
            onDismiss = {
                viewModel.dismissCallDialog()
            }
        )
    }

    // SMS Confirmation Dialog
    if (smsPendingAction != null) {
        SmsConfirmDialog(
            initialRecipient = smsPendingAction?.phoneNumber ?: "",
            initialMessage = smsPendingAction?.messageText ?: "",
            onConfirm = { recipient, message ->
                viewModel.confirmSms(recipient, message)
            },
            onDismiss = {
                viewModel.dismissSmsDialog()
            }
        )
    }

    // Screen Time Alert Dialog
    if (screenTimeAlert != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissScreenTimeAlert() },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Usage Checkpoint",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = screenTimeAlert ?: "",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissScreenTimeAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got it, $assistantName", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        SettingsBottomSheet(
            isTtsEnabled = viewModel.preferences.isTtsEnabled,
            ttsPitch = viewModel.preferences.ttsPitch,
            ttsRate = viewModel.preferences.ttsRate,
            ttsLanguage = viewModel.preferences.ttsLanguage,
            isCallConfirmationRequired = viewModel.preferences.isCallConfirmationRequired,
            isHapticEnabled = viewModel.preferences.isHapticEnabled,
            customApiKey = viewModel.preferences.customApiKey,
            geminiModel = viewModel.preferences.geminiModel,
            assistantName = assistantName,
            userNickname = userNickname,
            voiceGender = voiceGender,
            uiLanguage = uiLanguage,
            showCompanionPet = showCompanionPet,
            screenTimeReminderMinutes = screenTimeReminderMinutes,
            isSmsConfirmationRequired = isSmsConfirmationRequired,
            onSaveSettings = { isTtsEnabled, ttsPitch, ttsRate, ttsLanguage, isCallConf, isHaptic, customKey, model, aName, uNick, vGen, uiLang, pet, screenTime, smsConf ->
                viewModel.saveSettings(
                    isTtsEnabled = isTtsEnabled,
                    ttsPitch = ttsPitch,
                    ttsRate = ttsRate,
                    ttsLanguage = ttsLanguage,
                    isCallConfirmationRequired = isCallConf,
                    isHapticEnabled = isHaptic,
                    customApiKey = customKey,
                    geminiModel = model,
                    assistantName = aName,
                    userNickname = uNick,
                    voiceGender = vGen,
                    uiLanguage = uiLang,
                    showCompanionPet = pet,
                    screenTimeReminderMinutes = screenTime,
                    isSmsConfirmationRequired = smsConf
                )
            },
            onTestVoice = { pitch, rate, lang, gender ->
                viewModel.testVoice(pitch, rate, lang, gender)
            },
            onClearHistory = {
                viewModel.clearHistory()
            },
            onDismiss = {
                showSettingsSheet = false
            }
        )
    }
}
