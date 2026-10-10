package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.speech.AudioRecorderHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CustomCallAudioIntegrationTest {

    private lateinit var context: Context
    private lateinit var repository: FutureSelfSettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FutureSelfSettingsRepository.getInstance(context)
    }

    @Test
    fun testCustomCallAudioSettingsPersistence() {
        val initialSettings = repository.getSettingsSync()

        val updatedSettings = initialSettings.copy(
            useCustomCallAudio = true,
            customCallAudioUri = "/data/user/0/com.example/files/custom_ringtones/my_custom_voice.audio",
            customCallAudioName = "My Motivating Audio.mp3"
        )

        repository.updateSettings(updatedSettings)

        val retrievedSettings = repository.getSettingsSync()
        assertTrue(retrievedSettings.useCustomCallAudio)
        assertEquals(
            "/data/user/0/com.example/files/custom_ringtones/my_custom_voice.audio",
            retrievedSettings.customCallAudioUri
        )
        assertEquals("My Motivating Audio.mp3", retrievedSettings.customCallAudioName)
    }

    @Test
    fun testDisableCustomCallAudioToggle() {
        val initial = repository.getSettingsSync()
        val disabled = initial.copy(useCustomCallAudio = false)
        repository.updateSettings(disabled)

        val retrieved = repository.getSettingsSync()
        assertFalse(retrieved.useCustomCallAudio)
    }

    @Test
    fun testAudioRecorderHelperInit() {
        val recorderHelper = AudioRecorderHelper(context)
        assertFalse("Should not be recording initially", recorderHelper.isRecording())
        assertEquals(0L, recorderHelper.getCurrentDurationMs())
        assertEquals(0, recorderHelper.getMaxAmplitude())
    }
}
