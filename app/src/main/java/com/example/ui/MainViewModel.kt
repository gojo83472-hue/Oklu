package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.action.ActionDispatcher
import com.example.action.AndroidAction
import com.example.api.AssistantEngine
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRepository
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.TtsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(db.messageDao())
    val preferences = PreferencesManager(application)
    private val assistantEngine = AssistantEngine()

    private val speechHelper = SpeechRecognizerHelper(application, viewModelScope)
    private val ttsHelper = TtsHelper(application) {
        applyTtsPreferences()
    }

    val messages: StateFlow<List<MessageEntity>> = repository.messages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isListening: StateFlow<Boolean> = speechHelper.isListening
    val rmsLevel: StateFlow<Float> = speechHelper.rmsLevel
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _statusText = MutableStateFlow("Ready")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<Long?>(null)
    val speakingMessageId: StateFlow<Long?> = _speakingMessageId.asStateFlow()

    private val _callPendingAction = MutableStateFlow<AndroidAction.MakeCall?>(null)
    val callPendingAction: StateFlow<AndroidAction.MakeCall?> = _callPendingAction.asStateFlow()

    private val _userNotifications = MutableSharedFlow<String>()
    val userNotifications: SharedFlow<String> = _userNotifications.asSharedFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(preferences.hasCompletedOnboarding)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _assistantName = MutableStateFlow(preferences.assistantName)
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _userNickname = MutableStateFlow(preferences.userNickname)
    val userNickname: StateFlow<String> = _userNickname.asStateFlow()

    private val _uiLanguage = MutableStateFlow(preferences.uiLanguage)
    val uiLanguage: StateFlow<String> = _uiLanguage.asStateFlow()

    private val _voiceGender = MutableStateFlow(preferences.voiceGender)
    val voiceGender: StateFlow<String> = _voiceGender.asStateFlow()

    private val _showCompanionPet = MutableStateFlow(preferences.showCompanionPet)
    val showCompanionPet: StateFlow<Boolean> = _showCompanionPet.asStateFlow()

    private val _petMode = MutableStateFlow(preferences.petMode)
    val petMode: StateFlow<String> = _petMode.asStateFlow()

    private val _petEmojiPersona = MutableStateFlow(preferences.petEmojiPersona)
    val petEmojiPersona: StateFlow<String> = _petEmojiPersona.asStateFlow()

    private val _screenTimeReminderMinutes = MutableStateFlow(preferences.screenTimeReminderMinutes)
    val screenTimeReminderMinutes: StateFlow<Int> = _screenTimeReminderMinutes.asStateFlow()

    private val _isSmsConfirmationRequired = MutableStateFlow(preferences.isSmsConfirmationRequired)
    val isSmsConfirmationRequired: StateFlow<Boolean> = _isSmsConfirmationRequired.asStateFlow()

    private val _screenTimeAlert = MutableStateFlow<String?>(null)
    val screenTimeAlert: StateFlow<String?> = _screenTimeAlert.asStateFlow()

    private val _smsPendingAction = MutableStateFlow<AndroidAction.SendMessage?>(null)
    val smsPendingAction: StateFlow<AndroidAction.SendMessage?> = _smsPendingAction.asStateFlow()

    private var appActiveMinutes = 0

    init {
        // Apply TTS preferences
        applyTtsPreferences()

        // Observe speech recognition results
        viewModelScope.launch {
            speechHelper.speechResults.collect { spokenText ->
                if (spokenText.isNotBlank()) {
                    triggerHaptic(50)
                    processUserMessage(spokenText)
                }
            }
        }

        // Observe speech errors
        viewModelScope.launch {
            speechHelper.speechErrors.collect { error ->
                _statusText.value = error
                _userNotifications.emit(error)
            }
        }

        // Observe TTS status
        viewModelScope.launch {
            ttsHelper.isSpeaking.collect { speaking ->
                if (!speaking) {
                    _speakingMessageId.value = null
                    if (!_isProcessing.value && !speechHelper.isListening.value) {
                        _statusText.value = "Ready"
                    }
                } else {
                    _statusText.value = "Speaking…"
                }
            }
        }

        // Observe listening state for status text
        viewModelScope.launch {
            speechHelper.isListening.collect { listening ->
                if (listening) {
                    _statusText.value = "Listening…"
                } else if (!_isProcessing.value && !ttsHelper.isSpeaking.value) {
                    _statusText.value = "Ready"
                }
            }
        }

        // Screen time reminder loop
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000) // 1 minute
                appActiveMinutes++
                val reminderLimit = preferences.screenTimeReminderMinutes
                if (reminderLimit > 0 && appActiveMinutes % reminderLimit == 0) {
                    val alertText = "Screen-time reminder, ${_userNickname.value}: You have been active for $appActiveMinutes minutes. Consider taking a short break!"
                    _screenTimeAlert.value = alertText
                    _userNotifications.emit(alertText)
                    if (preferences.isTtsEnabled) {
                        ttsHelper.speak(alertText)
                    }
                }
            }
        }

        // If history is completely empty, insert friendly welcome message
        viewModelScope.launch(Dispatchers.IO) {
            val recent = repository.getRecentMessages(1)
            if (recent.isEmpty()) {
                val aName = preferences.assistantName
                val uNick = preferences.userNickname
                repository.insertMessage(
                    MessageEntity(
                        role = "assistant",
                        content = "Systems online, $uNick! I am $aName, your personalized AI assistant. You can give me voice commands in Tamil, English, or Thanglish, launch apps, manage offline controls, and ask anything."
                    )
                )
            }
        }
    }

    fun completeOnboarding(chosenAssistantName: String, chosenNickname: String, chosenLang: String, chosenGender: String) {
        val safeAssistant = if (chosenAssistantName.isNotBlank()) chosenAssistantName.trim() else "NOVA"
        val safeNickname = if (chosenNickname.isNotBlank()) chosenNickname.trim() else "Boss"
        preferences.assistantName = safeAssistant
        preferences.userNickname = safeNickname
        preferences.uiLanguage = chosenLang
        preferences.voiceGender = chosenGender
        preferences.hasCompletedOnboarding = true

        _assistantName.value = safeAssistant
        _userNickname.value = safeNickname
        _uiLanguage.value = chosenLang
        _voiceGender.value = chosenGender
        _hasCompletedOnboarding.value = true

        applyTtsPreferences()

        val welcome = "All systems calibrated, $safeNickname! I am $safeAssistant. How may I serve you today?"
        viewModelScope.launch {
            repository.insertMessage(
                MessageEntity(
                    role = "assistant",
                    content = welcome
                )
            )
            if (preferences.isTtsEnabled) {
                ttsHelper.speak(welcome)
            }
        }
    }

    fun updateAssistantName(name: String) {
        val safe = if (name.isNotBlank()) name.trim() else "NOVA"
        preferences.assistantName = safe
        _assistantName.value = safe
    }

    fun updateUserNickname(nickname: String) {
        val safe = if (nickname.isNotBlank()) nickname.trim() else "Boss"
        preferences.userNickname = safe
        _userNickname.value = safe
    }

    fun updateShowCompanionPet(show: Boolean) {
        preferences.showCompanionPet = show
        _showCompanionPet.value = show
    }

    fun updateScreenTimeReminderMinutes(minutes: Int) {
        preferences.screenTimeReminderMinutes = minutes
        _screenTimeReminderMinutes.value = minutes
    }

    fun updateVoiceGender(gender: String) {
        preferences.voiceGender = gender
        _voiceGender.value = gender
        applyTtsPreferences()
    }

    fun updateUiLanguage(lang: String) {
        preferences.uiLanguage = lang
        _uiLanguage.value = lang
    }

    private fun applyTtsPreferences() {
        ttsHelper.setPitch(preferences.ttsPitch)
        ttsHelper.setRate(preferences.ttsRate)
        ttsHelper.setVoiceGender(preferences.voiceGender)
        val localeParts = preferences.ttsLanguage.split("_")
        val locale = if (localeParts.size == 2) {
            Locale(localeParts[0], localeParts[1])
        } else {
            Locale(preferences.ttsLanguage)
        }
        ttsHelper.setLanguage(locale)
    }

    fun startListening() {
        ttsHelper.stop()
        triggerHaptic(40)
        val langCode = preferences.ttsLanguage.replace("_", "-")
        speechHelper.startListening(langCode)
    }

    fun stopListening() {
        speechHelper.stopListening()
        triggerHaptic(20)
    }

    fun processUserMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _isProcessing.value = true
            _statusText.value = "Thinking…"

            // 1. Insert user message into local Room history
            val userMsg = MessageEntity(
                role = "user",
                content = trimmed,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)

            // 2. Query assistant engine
            val apiKey = if (preferences.customApiKey.isNotBlank()) {
                preferences.customApiKey
            } else {
                try {
                    BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }
            }

            val recentHistory = repository.getRecentMessages(8).reversed()
            val result = assistantEngine.processQuery(
                query = trimmed,
                apiKey = apiKey,
                modelName = preferences.geminiModel,
                history = recentHistory,
                assistantName = _assistantName.value,
                userNickname = _userNickname.value,
                uiLanguage = _uiLanguage.value
            )

            // 3. Insert assistant response into local Room history
            val assistantMsg = MessageEntity(
                role = "assistant",
                content = result.responseText,
                timestamp = System.currentTimeMillis(),
                actionType = result.action?.type,
                actionPayload = when (val act = result.action) {
                    is AndroidAction.MakeCall -> act.phoneNumber
                    is AndroidAction.SendMessage -> "${act.phoneNumber}|||${act.messageText}"
                    is AndroidAction.OpenSettings -> act.subSetting
                    is AndroidAction.OpenChrome -> act.queryOrUrl
                    is AndroidAction.OpenMusic -> act.query
                    is AndroidAction.OpenYouTube -> act.query
                    is AndroidAction.PlayVideo -> act.query
                    is AndroidAction.ToggleFlashlight -> act.enable.toString()
                    is AndroidAction.ControlVolume -> act.action.name
                    is AndroidAction.Dictation -> act.transcribedText
                    is AndroidAction.OpenApp -> act.appName
                    else -> null
                },
                actionLabel = result.action?.title,
                actionStatus = if (result.action != null) "pending" else "none"
            )
            val insertedId = repository.insertMessage(assistantMsg)

            _isProcessing.value = false
            _statusText.value = "Ready"

            // 4. If TTS enabled, speak assistant response
            if (preferences.isTtsEnabled) {
                _speakingMessageId.value = insertedId
                ttsHelper.speak(result.responseText)
            }

            // 5. If an action was detected:
            if (result.action != null) {
                if (result.action is AndroidAction.MakeCall && preferences.isCallConfirmationRequired) {
                    // Show call confirmation dialog
                    _callPendingAction.value = result.action
                } else if (result.action is AndroidAction.SendMessage && preferences.isSmsConfirmationRequired) {
                    // Show SMS confirmation dialog
                    _smsPendingAction.value = result.action
                } else if (result.action !is AndroidAction.MakeCall && result.action !is AndroidAction.SendMessage) {
                    // Automatically execute non-destructive actions
                    executeAction(result.action, insertedId)
                }
            }
        }
    }

    fun executeAction(action: AndroidAction, messageId: Long? = null) {
        if (action is AndroidAction.MakeCall && preferences.isCallConfirmationRequired) {
            _callPendingAction.value = action
            return
        }
        if (action is AndroidAction.SendMessage && preferences.isSmsConfirmationRequired) {
            _smsPendingAction.value = action
            return
        }

        triggerHaptic(50)
        val context = getApplication<Application>().applicationContext
        val res = ActionDispatcher.executeAction(context, action)

        viewModelScope.launch {
            if (res.isSuccess) {
                _userNotifications.emit(res.getOrNull() ?: "Action launched")
                if (messageId != null) {
                    val current = repository.getRecentMessages(20).find { it.id == messageId }
                    if (current != null) {
                        repository.updateMessage(current.copy(actionStatus = "executed"))
                    }
                }
            } else {
                val err = res.exceptionOrNull()?.message ?: "Failed to execute action"
                _userNotifications.emit(err)
            }
        }
    }

    fun confirmSms(phoneNumber: String, messageText: String) {
        _smsPendingAction.value = null
        val action = AndroidAction.SendMessage(phoneNumber, messageText)
        triggerHaptic(60)
        val context = getApplication<Application>().applicationContext
        val res = ActionDispatcher.executeAction(context, action)
        viewModelScope.launch {
            if (res.isSuccess) {
                _userNotifications.emit("Opening SMS to $phoneNumber…")
            } else {
                _userNotifications.emit(res.exceptionOrNull()?.message ?: "Unable to send SMS")
            }
        }
    }

    fun dismissSmsDialog() {
        _smsPendingAction.value = null
    }

    fun confirmCall(phoneNumber: String) {
        _callPendingAction.value = null
        val action = AndroidAction.MakeCall(phoneNumber)
        triggerHaptic(60)
        val context = getApplication<Application>().applicationContext
        val res = ActionDispatcher.executeAction(context, action)
        viewModelScope.launch {
            if (res.isSuccess) {
                _userNotifications.emit("Calling $phoneNumber…")
            } else {
                _userNotifications.emit(res.exceptionOrNull()?.message ?: "Unable to call")
            }
        }
    }

    fun dismissCallDialog() {
        _callPendingAction.value = null
    }

    fun speakText(text: String, messageId: Long? = null) {
        _speakingMessageId.value = messageId
        ttsHelper.speak(text)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
        _speakingMessageId.value = null
        _statusText.value = "Ready"
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userNotifications.emit("Chat history cleared")
        }
    }

    fun saveSettings(
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
        petEmojiPersona: String = "robot"
    ) {
        preferences.isTtsEnabled = isTtsEnabled
        preferences.ttsPitch = ttsPitch
        preferences.ttsRate = ttsRate
        preferences.ttsLanguage = ttsLanguage
        preferences.isCallConfirmationRequired = isCallConfirmationRequired
        preferences.isHapticEnabled = isHapticEnabled
        preferences.customApiKey = customApiKey
        preferences.geminiModel = geminiModel

        val safeName = if (assistantName.isNotBlank()) assistantName.trim() else "NOVA"
        val safeNick = if (userNickname.isNotBlank()) userNickname.trim() else "Boss"
        preferences.assistantName = safeName
        preferences.userNickname = safeNick
        preferences.voiceGender = voiceGender
        preferences.uiLanguage = uiLanguage
        preferences.showCompanionPet = showCompanionPet
        preferences.screenTimeReminderMinutes = screenTimeReminderMinutes
        preferences.isSmsConfirmationRequired = isSmsConfirmationRequired
        preferences.petMode = petMode
        preferences.petEmojiPersona = petEmojiPersona

        _assistantName.value = safeName
        _userNickname.value = safeNick
        _voiceGender.value = voiceGender
        _uiLanguage.value = uiLanguage
        _showCompanionPet.value = showCompanionPet
        _screenTimeReminderMinutes.value = screenTimeReminderMinutes
        _isSmsConfirmationRequired.value = isSmsConfirmationRequired
        _petMode.value = petMode
        _petEmojiPersona.value = petEmojiPersona

        applyTtsPreferences()
    }

    fun dismissScreenTimeAlert() {
        _screenTimeAlert.value = null
    }

    fun testVoice(pitch: Float, rate: Float, language: String, gender: String = "female") {
        ttsHelper.setVoiceGender(gender)
        ttsHelper.setPitch(pitch)
        ttsHelper.setRate(rate)
        val parts = language.split("_")
        val locale = if (parts.size == 2) Locale(parts[0], parts[1]) else Locale(language)
        ttsHelper.setLanguage(locale)
        val testGreeting = if (language.startsWith("ta")) {
            "வணக்கம் ${_userNickname.value}! நான் உங்கள் AI உதவியாளர் ${_assistantName.value}."
        } else {
            "Hello ${_userNickname.value}, I am ${_assistantName.value}, your voice assistant. Systems are online."
        }
        ttsHelper.speak(testGreeting)
    }

    private fun triggerHaptic(milliseconds: Long) {
        if (!preferences.isHapticEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(milliseconds)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.destroy()
        ttsHelper.shutdown()
    }
}
