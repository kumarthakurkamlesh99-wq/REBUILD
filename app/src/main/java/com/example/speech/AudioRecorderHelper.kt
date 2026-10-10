package com.example.speech

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {

    companion object {
        private const val TAG = "AudioRecorderHelper"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var startTimeMillis: Long = 0L
    private var currentOutputFile: File? = null

    fun isRecording(): Boolean = isRecording

    fun getCurrentDurationMs(): Long {
        return if (isRecording && startTimeMillis > 0L) {
            SystemClock.elapsedRealtime() - startTimeMillis
        } else {
            0L
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) {
                mediaRecorder?.maxAmplitude ?: 0
            } else {
                0
            }
        } catch (_: Exception) {
            0
        }
    }

    fun startRecording(outputFile: File): Boolean {
        if (isRecording) {
            stopRecording()
        }

        return try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            currentOutputFile = outputFile
            startTimeMillis = SystemClock.elapsedRealtime()
            Log.d(TAG, "Audio recording started: ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            cleanup()
            false
        }
    }

    fun stopRecording(): Long {
        if (!isRecording) return 0L
        val duration = SystemClock.elapsedRealtime() - startTimeMillis
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media recorder", e)
        } finally {
            mediaRecorder = null
            isRecording = false
            startTimeMillis = 0L
        }
        return duration
    }

    fun cleanup() {
        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        isRecording = false
        startTimeMillis = 0L
    }
}
