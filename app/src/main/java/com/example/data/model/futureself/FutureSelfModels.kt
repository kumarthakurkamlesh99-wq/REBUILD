package com.example.data.model.futureself

import java.io.Serializable

enum class VoiceTone(val displayName: String, val description: String) {
    CALM("Calm", "Composed, mindful, and centered"),
    STRICT("Strict", "Direct, uncompromising, and firm"),
    COACH("Coach", "High-energy athletic motivator"),
    MILITARY("Military", "Unflinching operational discipline")
}

enum class VoiceLanguage(val displayName: String, val localeCode: String) {
    ENGLISH("English", "en"),
    HINDI("Hindi", "hi"),
    HINGLISH("Hinglish", "hi-en")
}

enum class VoiceGender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female")
}

enum class VoiceCloneProvider(val displayName: String, val badge: String, val description: String) {
    OMNI_VOICE("OmniVoice (k2-fsa)", "100% FREE", "Zero-shot open-source AI voice cloning (600+ languages)"),
    ELEVEN_LABS("ElevenLabs AI", "NEURAL API", "Cloud voice clone with API key")
}

data class FutureSelfVoiceSettings(
    val isEnabled: Boolean = true,
    val voiceGender: VoiceGender = VoiceGender.MALE,
    val tone: VoiceTone = VoiceTone.STRICT,
    val language: VoiceLanguage = VoiceLanguage.ENGLISH,
    val speechSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val vibrationEnabled: Boolean = true,
    val ringtonePreset: String = "CYBER_SIREN",
    val customFutureSelfQuote: String = "",
    val maxAllowedDelayMinutes: Int = 60,
    val autoStartOnMaxDelay: Boolean = true,
    val callTimeoutSeconds: Int = 40,
    val customMaxDelayAudioUri: String = "",
    val customMaxDelayAudioName: String = "",
    val customRingtoneUri: String = "",
    val customRingtoneName: String = "",
    val isVoiceCloningEnabled: Boolean = false,
    val voiceCloneProvider: VoiceCloneProvider = VoiceCloneProvider.OMNI_VOICE,
    val omniVoiceEndpointUrl: String = "https://k2-fsa-omnivoice.hf.space",
    val voiceCloneSampleUri: String = "",
    val voiceCloneSampleName: String = "",
    val voiceCloneId: String = "",
    val voiceCloneName: String = "",
    val elevenLabsApiKey: String = "",
    val voiceCloneStability: Float = 0.5f,
    val voiceCloneSimilarity: Float = 0.8f
)

data class FutureSelfCallPayload(
    val taskId: Long,
    val taskTitle: String,
    val taskSubject: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val motivationalQuote: String,
    val studentName: String
) : Serializable
