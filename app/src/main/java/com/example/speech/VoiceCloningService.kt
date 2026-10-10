package com.example.speech

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class VoiceCloneResult(
    val voiceId: String,
    val voiceName: String
)

data class PresetVoiceProfile(
    val id: String,
    val name: String,
    val description: String,
    val sampleGender: String
)

class VoiceCloningService private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "VoiceCloningService"
        private const val BASE_URL = "https://api.elevenlabs.io/v1"

        @Volatile
        private var instance: VoiceCloningService? = null

        fun getInstance(context: Context): VoiceCloningService {
            return instance ?: synchronized(this) {
                instance ?: VoiceCloningService(context.applicationContext).also { instance = it }
            }
        }

        val PRESET_VOICES = listOf(
            PresetVoiceProfile(
                id = "onwK4e9ZLuTAKqWW03F9",
                name = "Future Self - Deep Alpha",
                description = "Authoritative, calm, resonant future self voice",
                sampleGender = "Male"
            ),
            PresetVoiceProfile(
                id = "EXAVITQu4vr4xnSDxMaL",
                name = "Future Self - Strict Commander",
                description = "No-nonsense military clarity and urgency",
                sampleGender = "Female"
            ),
            PresetVoiceProfile(
                id = "CwhRBWXzGAHq8TQ4Fs17",
                name = "Future Self - Calm Sage",
                description = "Deeply centered, grounded mentor presence",
                sampleGender = "Male"
            ),
            PresetVoiceProfile(
                id = "FGY2WhTYpPnrIDTdsKH5",
                name = "Future Self - High Octane",
                description = "Relentless energy, athletic coach persona",
                sampleGender = "Female"
            )
        )
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()

    private val cacheDir = File(appContext.cacheDir, "voice_clones").apply { mkdirs() }

    fun resolveApiKey(userKey: String): String {
        if (userKey.isNotBlank()) return userKey.trim()
        return try {
            val buildKey = BuildConfig.ELEVENLABS_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_ELEVENLABS_API_KEY") buildKey else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun computeTextHash(text: String, voiceId: String): String {
        val input = "${voiceId}_${text.trim()}"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getCachedAudio(text: String, voiceId: String): File? {
        val hash = computeTextHash(text, voiceId)
        val file = File(cacheDir, "$hash.mp3")
        return if (file.exists() && file.length() > 500) file else null
    }

    /**
     * Clones voice from a sample audio file (.m4a, .mp3, .wav) using ElevenLabs Instant Voice Cloning.
     */
    suspend fun cloneVoiceFromAudioFile(
        audioFile: File,
        voiceName: String,
        apiKey: String
    ): Result<VoiceCloneResult> = withContext(Dispatchers.IO) {
        val resolvedKey = resolveApiKey(apiKey)
        if (resolvedKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("ElevenLabs API Key is required. Please enter your API key in Settings.")
            )
        }

        if (!audioFile.exists() || audioFile.length() < 1000) {
            return@withContext Result.failure(
                IllegalArgumentException("Voice sample audio file is missing or too short. Please record or pick a voice sample.")
            )
        }

        try {
            val mediaType = when {
                audioFile.name.endsWith(".m4a", true) -> "audio/mp4".toMediaType()
                audioFile.name.endsWith(".wav", true) -> "audio/wav".toMediaType()
                else -> "audio/mpeg".toMediaType()
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", voiceName.ifBlank { "Future Self Voice Clone" })
                .addFormDataPart("description", "Rebuild App Future Self digital clone profile")
                .addFormDataPart(
                    "files",
                    audioFile.name,
                    audioFile.asRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/voices/add")
                .addHeader("xi-api-key", resolvedKey)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("detail")?.optString("message")
                        ?: json.optString("detail", response.message)
                } catch (_: Exception) {
                    response.message
                }
                return@withContext Result.failure(
                    RuntimeException("Voice cloning failed (${response.code}): $errorMsg")
                )
            }

            val json = JSONObject(responseBody)
            val voiceId = json.getString("voice_id")
            Log.i(TAG, "Voice cloned successfully! Voice ID: $voiceId")

            Result.success(VoiceCloneResult(voiceId = voiceId, voiceName = voiceName))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during voice cloning", e)
            Result.failure(e)
        }
    }

    /**
     * Synthesizes dynamic speech text into an MP3 file using the specified cloned voice.
     */
    suspend fun synthesizeSpeech(
        text: String,
        voiceId: String,
        apiKey: String,
        stability: Float = 0.5f,
        similarity: Float = 0.8f
    ): Result<File> = withContext(Dispatchers.IO) {
        val cached = getCachedAudio(text, voiceId)
        if (cached != null) {
            Log.d(TAG, "Cache hit for speech: ${cached.name}")
            return@withContext Result.success(cached)
        }

        val resolvedKey = resolveApiKey(apiKey)
        if (resolvedKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Missing ElevenLabs API key for voice synthesis.")
            )
        }

        if (voiceId.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Missing voice ID for voice synthesis.")
            )
        }

        try {
            val payload = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", stability.coerceIn(0.1f, 1.0f).toDouble())
                    put("similarity_boost", similarity.coerceIn(0.1f, 1.0f).toDouble())
                }
                put("voice_settings", voiceSettings)
            }

            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$BASE_URL/text-to-speech/$voiceId")
                .addHeader("xi-api-key", resolvedKey)
                .addHeader("Accept", "audio/mpeg")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "ElevenLabs TTS failed (${response.code}): $errorBody")
                return@withContext Result.failure(
                    RuntimeException("TTS failed with code ${response.code}: ${response.message}")
                )
            }

            val hash = computeTextHash(text, voiceId)
            val outputFile = File(cacheDir, "$hash.mp3")

            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (outputFile.exists() && outputFile.length() > 500) {
                Log.d(TAG, "Cloned voice speech synthesized and cached: ${outputFile.length()} bytes")
                Result.success(outputFile)
            } else {
                Result.failure(RuntimeException("Synthesized audio file was empty or corrupted"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during voice synthesis", e)
            Result.failure(e)
        }
    }

    /**
     * Synthesizes dynamic speech using k2-fsa OmniVoice open-source zero-shot voice cloning.
     * Takes the user's reference audio sample and clones their voice without requiring any API key or subscription.
     */
    suspend fun synthesizeWithOmniVoice(
        text: String,
        sampleAudioFile: File,
        endpointUrl: String = "https://k2-fsa-omnivoice.hf.space",
        languageCode: String = "en"
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!sampleAudioFile.exists() || sampleAudioFile.length() < 500) {
            return@withContext Result.failure(
                IllegalArgumentException("Voice sample audio is missing. Please record or upload a sample.")
            )
        }

        val baseEndpoint = endpointUrl.trim().removeSuffix("/")
        val hash = computeTextHash("OMNIVOICE_${baseEndpoint}_${sampleAudioFile.name}_${text}", "omnivoice")
        val cachedFile = File(cacheDir, "$hash.mp3")
        if (cachedFile.exists() && cachedFile.length() > 500) {
            Log.d(TAG, "Cache hit for OmniVoice speech: ${cachedFile.name}")
            return@withContext Result.success(cachedFile)
        }

        try {
            val mediaType = when {
                sampleAudioFile.name.endsWith(".m4a", true) -> "audio/mp4".toMediaType()
                sampleAudioFile.name.endsWith(".wav", true) -> "audio/wav".toMediaType()
                else -> "audio/mpeg".toMediaType()
            }

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", text)
                .addFormDataPart("language", languageCode)
                .addFormDataPart(
                    "audio",
                    sampleAudioFile.name,
                    sampleAudioFile.asRequestBody(mediaType)
                )
                .build()

            val candidatePaths = listOf(
                "/api/clone",
                "/clone",
                "/generate",
                "/api/generate",
                "/predict"
            )

            var successfulAudioStream: java.io.InputStream? = null
            var lastError = "Unable to connect to OmniVoice server"

            for (path in candidatePaths) {
                try {
                    val request = Request.Builder()
                        .url("$baseEndpoint$path")
                        .post(multipartBody)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful && response.body != null) {
                        val contentType = response.header("Content-Type") ?: ""
                        if (contentType.contains("audio") || contentType.contains("octet-stream")) {
                            successfulAudioStream = response.body?.byteStream()
                            break
                        } else {
                            val bodyStr = response.body?.string() ?: ""
                            val json = try { JSONObject(bodyStr) } catch (_: Exception) { null }
                            val audioUrl = json?.optString("audio_url") ?: json?.optString("url") ?: ""
                            if (audioUrl.isNotBlank()) {
                                val targetUrl = if (audioUrl.startsWith("http")) audioUrl else "$baseEndpoint/$audioUrl"
                                val dlReq = Request.Builder().url(targetUrl).build()
                                val dlResp = httpClient.newCall(dlReq).execute()
                                if (dlResp.isSuccessful && dlResp.body != null) {
                                    successfulAudioStream = dlResp.body?.byteStream()
                                    break
                                }
                            }
                        }
                    } else {
                        lastError = "OmniVoice returned code ${response.code}: ${response.message}"
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Connection failed"
                }
            }

            if (successfulAudioStream != null) {
                FileOutputStream(cachedFile).use { output ->
                    successfulAudioStream.copyTo(output)
                }
                if (cachedFile.exists() && cachedFile.length() > 500) {
                    Log.d(TAG, "OmniVoice speech synthesized successfully: ${cachedFile.length()} bytes")
                    return@withContext Result.success(cachedFile)
                }
            }

            Result.failure(RuntimeException("OmniVoice cloning synthesis failed: $lastError"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OmniVoice synthesis", e)
            Result.failure(e)
        }
    }

    /**
     * Pre-synthesizes audio in the background before call connection so it plays instantaneously.
     */
    fun preSynthesize(
        text: String,
        voiceId: String,
        apiKey: String,
        stability: Float = 0.5f,
        similarity: Float = 0.8f
    ) {
        if (voiceId.isBlank()) return
        if (getCachedAudio(text, voiceId) != null) return

        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                synthesizeSpeech(text, voiceId, apiKey, stability, similarity)
            } catch (e: Exception) {
                Log.w(TAG, "Background pre-synthesis skipped: ${e.message}")
            }
        }
    }
}
