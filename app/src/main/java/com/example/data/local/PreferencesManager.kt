package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("voice_assistant_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ASSISTANT_NAME = "assistant_name"
        private const val KEY_USER_NICKNAME = "user_nickname"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_UI_LANGUAGE = "ui_language"
        private const val KEY_VOICE_GENDER = "voice_gender"
        private const val KEY_SHOW_COMPANION = "show_companion"
        private const val KEY_SCREEN_TIME_MINS = "screen_time_mins"
        private const val KEY_SAFETY_SMS = "safety_sms"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_TTS_RATE = "tts_rate"
        private const val KEY_TTS_LANGUAGE = "tts_language"
        private const val KEY_CALL_CONFIRMATION = "call_confirmation"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_MODEL = "gemini_model"
    }

    var assistantName: String
        get() = prefs.getString(KEY_ASSISTANT_NAME, "NOVA") ?: "NOVA"
        set(value) = prefs.edit().putString(KEY_ASSISTANT_NAME, value.trim().ifBlank { "NOVA" }).apply()

    var userNickname: String
        get() = prefs.getString(KEY_USER_NICKNAME, "Boss") ?: "Boss"
        set(value) = prefs.edit().putString(KEY_USER_NICKNAME, value.trim().ifBlank { "Boss" }).apply()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var uiLanguage: String
        get() = prefs.getString(KEY_UI_LANGUAGE, "en") ?: "en" // "en" or "ta"
        set(value) = prefs.edit().putString(KEY_UI_LANGUAGE, value).apply()

    var voiceGender: String
        get() = prefs.getString(KEY_VOICE_GENDER, "female") ?: "female" // "female" or "male"
        set(value) = prefs.edit().putString(KEY_VOICE_GENDER, value).apply()

    var showCompanionPet: Boolean
        get() = prefs.getBoolean(KEY_SHOW_COMPANION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_COMPANION, value).apply()

    var screenTimeReminderMinutes: Int
        get() = prefs.getInt(KEY_SCREEN_TIME_MINS, 0) // 0 = off, 20, 30, 45, 60
        set(value) = prefs.edit().putInt(KEY_SCREEN_TIME_MINS, value).apply()

    var isSmsConfirmationRequired: Boolean
        get() = prefs.getBoolean(KEY_SAFETY_SMS, true)
        set(value) = prefs.edit().putBoolean(KEY_SAFETY_SMS, value).apply()

    var isTtsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TTS_ENABLED, value).apply()

    var ttsPitch: Float
        get() = prefs.getFloat(KEY_TTS_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TTS_PITCH, value).apply()

    var ttsRate: Float
        get() = prefs.getFloat(KEY_TTS_RATE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TTS_RATE, value).apply()

    var ttsLanguage: String
        get() = prefs.getString(KEY_TTS_LANGUAGE, "en_US") ?: "en_US"
        set(value) = prefs.edit().putString(KEY_TTS_LANGUAGE, value).apply()

    var isCallConfirmationRequired: Boolean
        get() = prefs.getBoolean(KEY_CALL_CONFIRMATION, true)
        set(value) = prefs.edit().putBoolean(KEY_CALL_CONFIRMATION, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value).apply()

    var geminiModel: String
        get() = prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()
}
