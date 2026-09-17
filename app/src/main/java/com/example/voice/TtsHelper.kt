package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TtsHelper(
    context: Context,
    onReady: (() -> Unit)? = null
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentPitch: Float = 1.0f
    private var currentRate: Float = 1.0f
    private var currentLocale: Locale = Locale.US
    private var voiceGender: String = "female" // "female", "male", "default"

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                applySettings()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
                onReady?.invoke()
            }
        }
    }

    private fun applySettings() {
        val targetPitch = when (voiceGender.lowercase(Locale.ROOT)) {
            "male" -> (currentPitch * 0.85f).coerceIn(0.5f, 2.0f)
            "female" -> (currentPitch * 1.15f).coerceIn(0.5f, 2.0f)
            else -> currentPitch
        }
        tts?.setPitch(targetPitch)
        tts?.setSpeechRate(currentRate)
        tts?.language = currentLocale

        // Try selecting matching voice if available
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val matching = voices.firstOrNull { v ->
                    val isTargetLang = v.locale.language == currentLocale.language
                    val matchesGender = when (voiceGender.lowercase(Locale.ROOT)) {
                        "male" -> v.name.contains("male", ignoreCase = true) && !v.name.contains("female", ignoreCase = true)
                        "female" -> v.name.contains("female", ignoreCase = true)
                        else -> true
                    }
                    isTargetLang && matchesGender
                }
                if (matching != null) {
                    tts?.voice = matching
                }
            }
        } catch (_: Exception) {}
    }

    fun setVoiceGender(gender: String) {
        voiceGender = gender
        if (isInitialized) {
            applySettings()
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            applySettings()
        }
    }

    fun setRate(rate: Float) {
        currentRate = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(currentRate)
        }
    }

    fun setLanguage(locale: Locale) {
        currentLocale = locale
        if (isInitialized) {
            tts?.language = currentLocale
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return
        stop()

        // Auto-detect Tamil characters to switch TTS engine voice dynamically
        val hasTamil = text.any { it in '\u0B80'..'\u0BFF' }
        if (hasTamil) {
            val tamilLocale = Locale("ta", "IN")
            val available = tts?.isLanguageAvailable(tamilLocale)
            if (available != TextToSpeech.LANG_MISSING_DATA && available != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = tamilLocale
            }
        } else {
            tts?.language = currentLocale
        }

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
