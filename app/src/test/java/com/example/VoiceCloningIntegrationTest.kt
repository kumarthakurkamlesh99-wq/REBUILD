package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.speech.AudioRecorderHelper
import com.example.speech.VoiceCloningService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoiceCloningIntegrationTest {

    private lateinit var context: Context
    private lateinit var repository: FutureSelfSettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FutureSelfSettingsRepository.getInstance(context)
    }

    @Test
    fun testVoiceCloningSettingsPersistence() {
        val initialSettings = repository.getSettingsSync()

        val updatedSettings = initialSettings.copy(
            isVoiceCloningEnabled = true,
            voiceCloneProvider = com.example.data.model.futureself.VoiceCloneProvider.OMNI_VOICE,
            omniVoiceEndpointUrl = "https://k2-fsa-omnivoice.hf.space",
            voiceCloneSampleUri = "/data/user/0/com.example/files/voice_samples/sample.m4a",
            voiceCloneSampleName = "Recorded Sample (32s)",
            voiceCloneId = "clone_voice_abc_123",
            voiceCloneName = "My Future Self",
            elevenLabsApiKey = "test_api_key_xyz",
            voiceCloneStability = 0.65f,
            voiceCloneSimilarity = 0.85f
        )

        repository.updateSettings(updatedSettings)

        val retrievedSettings = repository.getSettingsSync()
        assertTrue(retrievedSettings.isVoiceCloningEnabled)
        assertEquals(com.example.data.model.futureself.VoiceCloneProvider.OMNI_VOICE, retrievedSettings.voiceCloneProvider)
        assertEquals("https://k2-fsa-omnivoice.hf.space", retrievedSettings.omniVoiceEndpointUrl)
        assertEquals("/data/user/0/com.example/files/voice_samples/sample.m4a", retrievedSettings.voiceCloneSampleUri)
        assertEquals("Recorded Sample (32s)", retrievedSettings.voiceCloneSampleName)
        assertEquals("clone_voice_abc_123", retrievedSettings.voiceCloneId)
        assertEquals("My Future Self", retrievedSettings.voiceCloneName)
        assertEquals("test_api_key_xyz", retrievedSettings.elevenLabsApiKey)
        assertEquals(0.65f, retrievedSettings.voiceCloneStability, 0.01f)
        assertEquals(0.85f, retrievedSettings.voiceCloneSimilarity, 0.01f)
    }

    @Test
    fun testVoiceCloningServicePresetVoices() {
        val presetVoices = VoiceCloningService.PRESET_VOICES
        assertFalse("Preset voices should not be empty", presetVoices.isEmpty())

        val deepAlpha = presetVoices.find { it.name.contains("Deep Alpha") }
        assertNotNull("Deep Alpha preset should be present", deepAlpha)
        assertTrue(deepAlpha!!.id.isNotBlank())
    }

    @Test
    fun testVoiceCloningServiceApiKeyResolution() {
        val service = VoiceCloningService.getInstance(context)

        // User explicit key should take precedence
        val resolvedUserKey = service.resolveApiKey("user_custom_key_999")
        assertEquals("user_custom_key_999", resolvedUserKey)
    }

    @Test
    fun testAudioRecorderHelperInit() {
        val recorderHelper = AudioRecorderHelper(context)
        assertFalse("Should not be recording initially", recorderHelper.isRecording())
        assertEquals(0L, recorderHelper.getCurrentDurationMs())
        assertEquals(0, recorderHelper.getMaxAmplitude())
    }
}
