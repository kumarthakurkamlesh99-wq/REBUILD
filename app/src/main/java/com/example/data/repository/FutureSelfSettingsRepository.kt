package com.example.data.repository

import android.content.Context
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.model.futureself.VoiceCloneProvider
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
        private const val KEY_MAX_DELAY_MINUTES = "fs_max_delay_minutes"
        private const val KEY_AUTO_START_MAX_DELAY = "fs_auto_start_max_delay"
        private const val KEY_CALL_TIMEOUT_SECONDS = "fs_call_timeout_seconds"
        private const val KEY_CUSTOM_MAX_DELAY_AUDIO_URI = "fs_custom_max_delay_audio_uri"
        private const val KEY_CUSTOM_MAX_DELAY_AUDIO_NAME = "fs_custom_max_delay_audio_name"
        private const val KEY_CUSTOM_RINGTONE_URI = "fs_custom_ringtone_uri"
        private const val KEY_CUSTOM_RINGTONE_NAME = "fs_custom_ringtone_name"
        private const val KEY_VOICE_CLONING_ENABLED = "fs_voice_cloning_enabled"
        private const val KEY_VOICE_CLONE_SAMPLE_URI = "fs_voice_clone_sample_uri"
        private const val KEY_VOICE_CLONE_SAMPLE_NAME = "fs_voice_clone_sample_name"
        private const val KEY_VOICE_CLONE_ID = "fs_voice_clone_id"
        private const val KEY_VOICE_CLONE_NAME = "fs_voice_clone_name"
        private const val KEY_ELEVEN_LABS_API_KEY = "fs_eleven_labs_api_key"
        private const val KEY_VOICE_CLONE_STABILITY = "fs_voice_clone_stability"
        private const val KEY_VOICE_CLONE_SIMILARITY = "fs_voice_clone_similarity"
        private const val KEY_VOICE_CLONE_PROVIDER = "fs_voice_clone_provider"
        private const val KEY_OMNI_VOICE_ENDPOINT_URL = "fs_omni_voice_endpoint_url"

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
            customFutureSelfQuote = prefs.getString(KEY_CUSTOM_QUOTE, "") ?: "",
            maxAllowedDelayMinutes = prefs.getInt(KEY_MAX_DELAY_MINUTES, 60),
            autoStartOnMaxDelay = prefs.getBoolean(KEY_AUTO_START_MAX_DELAY, true),
            callTimeoutSeconds = prefs.getInt(KEY_CALL_TIMEOUT_SECONDS, 40),
            customMaxDelayAudioUri = prefs.getString(KEY_CUSTOM_MAX_DELAY_AUDIO_URI, "") ?: "",
            customMaxDelayAudioName = prefs.getString(KEY_CUSTOM_MAX_DELAY_AUDIO_NAME, "") ?: "",
            customRingtoneUri = prefs.getString(KEY_CUSTOM_RINGTONE_URI, "") ?: "",
            customRingtoneName = prefs.getString(KEY_CUSTOM_RINGTONE_NAME, "") ?: "",
            isVoiceCloningEnabled = prefs.getBoolean(KEY_VOICE_CLONING_ENABLED, false),
            voiceCloneProvider = try {
                VoiceCloneProvider.valueOf(
                    prefs.getString(KEY_VOICE_CLONE_PROVIDER, VoiceCloneProvider.OMNI_VOICE.name)
                        ?: VoiceCloneProvider.OMNI_VOICE.name
                )
            } catch (_: Exception) { VoiceCloneProvider.OMNI_VOICE },
            omniVoiceEndpointUrl = prefs.getString(KEY_OMNI_VOICE_ENDPOINT_URL, "https://k2-fsa-omnivoice.hf.space")
                ?: "https://k2-fsa-omnivoice.hf.space",
            voiceCloneSampleUri = prefs.getString(KEY_VOICE_CLONE_SAMPLE_URI, "") ?: "",
            voiceCloneSampleName = prefs.getString(KEY_VOICE_CLONE_SAMPLE_NAME, "") ?: "",
            voiceCloneId = prefs.getString(KEY_VOICE_CLONE_ID, "") ?: "",
            voiceCloneName = prefs.getString(KEY_VOICE_CLONE_NAME, "") ?: "",
            elevenLabsApiKey = prefs.getString(KEY_ELEVEN_LABS_API_KEY, "") ?: "",
            voiceCloneStability = prefs.getFloat(KEY_VOICE_CLONE_STABILITY, 0.5f),
            voiceCloneSimilarity = prefs.getFloat(KEY_VOICE_CLONE_SIMILARITY, 0.8f)
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
            .putInt(KEY_MAX_DELAY_MINUTES, newSettings.maxAllowedDelayMinutes)
            .putBoolean(KEY_AUTO_START_MAX_DELAY, newSettings.autoStartOnMaxDelay)
            .putInt(KEY_CALL_TIMEOUT_SECONDS, newSettings.callTimeoutSeconds)
            .putString(KEY_CUSTOM_MAX_DELAY_AUDIO_URI, newSettings.customMaxDelayAudioUri)
            .putString(KEY_CUSTOM_MAX_DELAY_AUDIO_NAME, newSettings.customMaxDelayAudioName)
            .putString(KEY_CUSTOM_RINGTONE_URI, newSettings.customRingtoneUri)
            .putString(KEY_CUSTOM_RINGTONE_NAME, newSettings.customRingtoneName)
            .putBoolean(KEY_VOICE_CLONING_ENABLED, newSettings.isVoiceCloningEnabled)
            .putString(KEY_VOICE_CLONE_PROVIDER, newSettings.voiceCloneProvider.name)
            .putString(KEY_OMNI_VOICE_ENDPOINT_URL, newSettings.omniVoiceEndpointUrl)
            .putString(KEY_VOICE_CLONE_SAMPLE_URI, newSettings.voiceCloneSampleUri)
            .putString(KEY_VOICE_CLONE_SAMPLE_NAME, newSettings.voiceCloneSampleName)
            .putString(KEY_VOICE_CLONE_ID, newSettings.voiceCloneId)
            .putString(KEY_VOICE_CLONE_NAME, newSettings.voiceCloneName)
            .putString(KEY_ELEVEN_LABS_API_KEY, newSettings.elevenLabsApiKey)
            .putFloat(KEY_VOICE_CLONE_STABILITY, newSettings.voiceCloneStability)
            .putFloat(KEY_VOICE_CLONE_SIMILARITY, newSettings.voiceCloneSimilarity)
            .apply()

        _settingsFlow.value = newSettings
    }
}
