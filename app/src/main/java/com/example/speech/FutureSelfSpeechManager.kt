package com.example.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.model.futureself.VoiceGender
import com.example.data.model.futureself.VoiceLanguage
import com.example.data.model.futureself.VoiceTone
import com.example.util.PresetAudioGenerator
import java.io.File
import java.util.Locale

class FutureSelfSpeechManager private constructor(private val appContext: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "FutureSelfSpeechMgr"

        @Volatile
        private var instance: FutureSelfSpeechManager? = null

        fun getInstance(context: Context): FutureSelfSpeechManager {
            return instance ?: synchronized(this) {
                instance ?: FutureSelfSpeechManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var activeCompletionCallback: (() -> Unit)? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    init {
        initTts()
        initVibrator()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set audio attributes on TTS", e)
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "TTS utterance start: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "TTS utterance done: $utteranceId")
                    val cb = activeCompletionCallback
                    activeCompletionCallback = null
                    cb?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TTS utterance error: $utteranceId")
                    val cb = activeCompletionCallback
                    activeCompletionCallback = null
                    cb?.invoke()
                }
            })
        } else {
            Log.e(TAG, "TextToSpeech init failed with status: $status")
        }
    }

    /**
     * Speaks the specified text with selected tone, language, and speed.
     * Invokes [onDone] when speech concludes or fails.
     */
    fun speak(
        text: String,
        tone: VoiceTone = VoiceTone.STRICT,
        language: VoiceLanguage = VoiceLanguage.ENGLISH,
        gender: VoiceGender = VoiceGender.MALE,
        speedMultiplier: Float = 1.0f,
        onDone: () -> Unit
    ) {
        if (!isTtsInitialized || tts == null) {
            Log.w(TAG, "TTS not ready, invoking callback immediately")
            onDone()
            return
        }

        this.activeCompletionCallback = onDone

        try {
            // Set locale
            val targetLocale = when (language) {
                VoiceLanguage.ENGLISH -> Locale("en", "IN")
                VoiceLanguage.HINDI -> Locale("hi", "IN")
                VoiceLanguage.HINGLISH -> Locale("en", "IN")
            }

            val langResult = tts?.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }

            // Select Voice by gender if available
            try {
                val voices = tts?.voices
                if (!voices.isNullOrEmpty()) {
                    val matchingVoice = voices.find { v ->
                        val nameLower = v.name.lowercase()
                        when (gender) {
                            VoiceGender.MALE -> nameLower.contains("male") && !nameLower.contains("female")
                            VoiceGender.FEMALE -> nameLower.contains("female")
                        }
                    }
                    if (matchingVoice != null) {
                        tts?.voice = matchingVoice
                    }
                }
            } catch (_: Exception) {
                // Ignore voice selection fallback
            }

            // Tune pitch and rate according to tone
            val (basePitch, baseRate) = when (tone) {
                VoiceTone.CALM -> Pair(0.95f, 0.90f)
                VoiceTone.STRICT -> Pair(0.85f, 1.05f)
                VoiceTone.COACH -> Pair(1.05f, 1.15f)
                VoiceTone.MILITARY -> Pair(0.80f, 1.15f)
            }

            val finalRate = (baseRate * speedMultiplier).coerceIn(0.6f, 2.0f)
            tts?.setPitch(basePitch)
            tts?.setSpeechRate(finalRate)

            val utteranceId = "FUTURE_SELF_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak text", e)
            val cb = activeCompletionCallback
            activeCompletionCallback = null
            cb?.invoke()
        }
    }

    /**
     * Starts looping ringtone audio and vibration for the incoming future self call.
     */
    fun startRingtoneAndVibrate(
        presetKey: String = "CYBER_SIREN",
        volume: Float = 1.0f,
        enableVibration: Boolean = true
    ) {
        stopRingtoneAndVibrate()

        // 1. Audio Playback
        try {
            val presetFile: File? = PresetAudioGenerator.getPresetFile(appContext, presetKey)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                if (presetFile != null && presetFile.exists()) {
                    setDataSource(presetFile.absolutePath)
                } else {
                    // Fallback to system default alarm
                    val defaultUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    setDataSource(appContext, defaultUri)
                }
                isLooping = true
                setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ringtone media player", e)
        }

        // 2. Vibration
        if (enableVibration && vibrator != null && vibrator?.hasVibrator() == true) {
            try {
                val timings = longArrayOf(0, 450, 250, 450, 250, 800)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 200)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(timings, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger ringtone vibration", e)
            }
        }
    }

    /**
     * Halts ringtone and vibration.
     */
    fun stopRingtoneAndVibrate() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator", e)
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        activeCompletionCallback = null
    }

    fun release() {
        stopRingtoneAndVibrate()
        stopSpeaking()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
