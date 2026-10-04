package com.example.data.repository

import android.content.Context
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.model.futureself.VoiceGender
import com.example.data.model.futureself.VoiceLanguage
import com.example.data.model.futureself.VoiceTone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FutureSelfSettingsRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "future_self_voice_prefs"
        private const val KEY_ENABLED = "fs_enabled"
        private const val KEY_GENDER = "fs_gender"
        private const val KEY_TONE = "fs_tone"
        private const val KEY_LANGUAGE = "fs_language"
        private const val KEY_SPEED = "fs_speed"
        private const val KEY_VOLUME = "fs_volume"
        private const val KEY_VIBRATION = "fs_vibration"
        private const val KEY_RINGTONE = "fs_ringtone"
        private const val KEY_CUSTOM_QUOTE = "fs_custom_quote"

        @Volatile
        private var instance: FutureSelfSettingsRepository? = null

        fun getInstance(context: Context): FutureSelfSettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: FutureSelfSettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(getSettingsSync())
    val settingsFlow: StateFlow<FutureSelfVoiceSettings> = _settingsFlow.asStateFlow()

    fun getSettingsSync(): FutureSelfVoiceSettings {
        return FutureSelfVoiceSettings(
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            voiceGender = try {
                VoiceGender.valueOf(prefs.getString(KEY_GENDER, VoiceGender.MALE.name) ?: VoiceGender.MALE.name)
            } catch (_: Exception) { VoiceGender.MALE },
            tone = try {
                VoiceTone.valueOf(prefs.getString(KEY_TONE, VoiceTone.STRICT.name) ?: VoiceTone.STRICT.name)
            } catch (_: Exception) { VoiceTone.STRICT },
            language = try {
                VoiceLanguage.valueOf(prefs.getString(KEY_LANGUAGE, VoiceLanguage.ENGLISH.name) ?: VoiceLanguage.ENGLISH.name)
            } catch (_: Exception) { VoiceLanguage.ENGLISH },
            speechSpeed = prefs.getFloat(KEY_SPEED, 1.0f),
            volume = prefs.getFloat(KEY_VOLUME, 1.0f),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            ringtonePreset = prefs.getString(KEY_RINGTONE, "CYBER_SIREN") ?: "CYBER_SIREN",
            customFutureSelfQuote = prefs.getString(KEY_CUSTOM_QUOTE, "") ?: ""
        )
    }

    fun updateSettings(newSettings: FutureSelfVoiceSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, newSettings.isEnabled)
            .putString(KEY_GENDER, newSettings.voiceGender.name)
            .putString(KEY_TONE, newSettings.tone.name)
            .putString(KEY_LANGUAGE, newSettings.language.name)
            .putFloat(KEY_SPEED, newSettings.speechSpeed)
            .putFloat(KEY_VOLUME, newSettings.volume)
            .putBoolean(KEY_VIBRATION, newSettings.vibrationEnabled)
            .putString(KEY_RINGTONE, newSettings.ringtonePreset)
            .putString(KEY_CUSTOM_QUOTE, newSettings.customFutureSelfQuote)
            .apply()

        _settingsFlow.value = newSettings
    }
}
