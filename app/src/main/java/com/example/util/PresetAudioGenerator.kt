package com.example.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object PresetAudioGenerator {

    private const val TAG = "PresetAudioGenerator"
    private const val SAMPLE_RATE = 44100

    fun getPresetFile(context: Context, presetKey: String): File? {
        val dir = File(context.filesDir, "preset_tones")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val fileName = when (presetKey.uppercase()) {
            "CYBER_SIREN" -> "cyber_siren.wav"
            "APEX_HORNS" -> "apex_horns.wav"
            "ZEN_CHIME" -> "zen_chime.wav"
            "BELL" -> "classic_bell.wav"
            "MILITARY" -> "military_reveille.wav"
            "TICK_TOCK" -> "tick_tock.wav"
            else -> null
        } ?: return null

        val file = File(dir, fileName)
        if (file.exists() && file.length() > 1000) {
            return file
        }

        try {
            generatePresetWave(presetKey.uppercase(), file)
            if (file.exists() && file.length() > 1000) {
                return file
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate preset audio for $presetKey", e)
        }
        return null
    }

    private fun generatePresetWave(presetKey: String, targetFile: File) {
        val durationSeconds = when (presetKey) {
            "CYBER_SIREN" -> 3.0
            "APEX_HORNS" -> 4.0
            "ZEN_CHIME" -> 3.5
            "BELL" -> 2.5
            "MILITARY" -> 4.0
            "TICK_TOCK" -> 2.0
            else -> 2.0
        }

        val totalSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val pcmData = ShortArray(totalSamples)

        when (presetKey) {
            "CYBER_SIREN" -> {
                // High alert two-tone modulated frequency sweep (800Hz to 1600Hz)
                var phase = 0.0
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val sweep = sin(2 * PI * 1.5 * t) // 1.5 Hz modulation cycle
                    val freq = 1000.0 + 500.0 * sweep
                    phase += 2 * PI * freq / SAMPLE_RATE
                    val sample = sin(phase) * 0.9 + 0.3 * sin(phase * 2)
                    pcmData[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                }
            }
            "APEX_HORNS" -> {
                // Brass fanfare: Major triad (A3, C#4, E4, A4) with brass harmonic richness
                val f1 = 220.0
                val f2 = 277.18
                val f3 = 329.63
                val f4 = 440.0
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val pulse = (1.0 + 0.2 * sin(2 * PI * 4.0 * t))
                    val wave = (
                        0.40 * sin(2 * PI * f1 * t) +
                        0.25 * sin(2 * PI * f2 * t) +
                        0.25 * sin(2 * PI * f3 * t) +
                        0.20 * sin(2 * PI * f4 * t) +
                        0.15 * sin(2 * PI * f1 * 2 * t) +
                        0.10 * sin(2 * PI * f4 * 2 * t)
                    ) * pulse
                    pcmData[i] = (wave.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                }
            }
            "ZEN_CHIME" -> {
                // Harmonic acoustic chime with gentle strikes and long soothing decay
                val strikes = doubleArrayOf(0.0, 0.9, 1.8, 2.6)
                val baseFreqs = doubleArrayOf(528.0, 660.0, 792.0, 1056.0)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var wave = 0.0
                    for (s in strikes.indices) {
                        val dt = t - strikes[s]
                        if (dt >= 0) {
                            val decay = exp(-3.0 * dt)
                            val f = baseFreqs[s]
                            wave += (sin(2 * PI * f * dt) + 0.4 * sin(2 * PI * (f * 2.02) * dt)) * decay * 0.45
                        }
                    }
                    pcmData[i] = (wave.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                }
            }
            "BELL" -> {
                // Classic twin-frequency mechanical bell ringing with rapid beating (900Hz + 915Hz)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val cycleTime = t % 0.6
                    val decay = exp(-4.5 * cycleTime)
                    val strike = (sin(2 * PI * 920.0 * cycleTime) + sin(2 * PI * 936.0 * cycleTime) + 0.3 * sin(2 * PI * 1840.0 * cycleTime)) * decay
                    pcmData[i] = (strike.coerceIn(-1.0, 1.0) * 32767 * 0.9).toInt().toShort()
                }
            }
            "MILITARY" -> {
                // Reveille bugle notes: G3, C4, E4, G4 in classic morning sequence
                val notes = doubleArrayOf(196.0, 261.63, 329.63, 392.0, 329.63, 261.63, 392.0, 392.0)
                val noteDuration = 0.5
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val noteIndex = ((t / noteDuration).toInt()).coerceIn(0, notes.lastIndex)
                    val freq = notes[noteIndex]
                    val noteT = t % noteDuration
                    val envelope = (1.0 - exp(-30.0 * noteT)) * (1.0 - (noteT / noteDuration) * 0.3)
                    val wave = (sin(2 * PI * freq * t) + 0.4 * sin(2 * PI * freq * 2 * t) + 0.2 * sin(2 * PI * freq * 3 * t)) * envelope * 0.8
                    pcmData[i] = (wave.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                }
            }
            "TICK_TOCK" -> {
                // Crisp mechanical ticking clock impulses
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val step = (t * 2.0).toInt()
                    val dt = t - (step * 0.5)
                    val freq = if (step % 2 == 0) 1400.0 else 1050.0
                    val tick = if (dt in 0.0..0.08) {
                        sin(2 * PI * freq * dt) * exp(-50.0 * dt)
                    } else 0.0
                    pcmData[i] = (tick.coerceIn(-1.0, 1.0) * 32767 * 0.9).toInt().toShort()
                }
            }
        }

        writeWavFile(targetFile, pcmData, SAMPLE_RATE)
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalSize))
            out.write("WAVE".toByteArray())

            // "fmt " chunk
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size for PCM
            out.write(shortToByteArray(1)) // AudioFormat (1 = PCM)
            out.write(shortToByteArray(numChannels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(blockAlign.toShort()))
            out.write(shortToByteArray(bitsPerSample.toShort()))

            // "data" chunk
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            // PCM short array to bytes (little-endian)
            val byteBuffer = ByteArray(pcmData.size * 2)
            var bi = 0
            for (sample in pcmData) {
                byteBuffer[bi++] = (sample.toInt() and 0x00FF).toByte()
                byteBuffer[bi++] = ((sample.toInt() shr 8) and 0x00FF).toByte()
            }
            out.write(byteBuffer)
            out.flush()
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}
