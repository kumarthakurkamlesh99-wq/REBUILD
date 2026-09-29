package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object RingtoneStorageManager {

    private const val TAG = "RingtoneStorageManager"
    private const val PREFS_NAME = "rebuild_ringtone_metadata"

    data class RingtoneResult(
        val storedUriOrPath: String,
        val displayName: String
    )

    fun saveCustomRingtone(context: Context, sourceUri: Uri): RingtoneResult {
        val displayName = resolveRingtoneTitle(context, sourceUri)

        // Attempt persistable permission
        try {
            context.contentResolver.takePersistableUriPermission(
                sourceUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Expected for non-persistable content URIs
        }

        // Copy audio content to app internal storage for 100% reliable offline/background playback
        val copyResult = copyUriToInternalStorage(context, sourceUri, displayName)
        if (copyResult != null) {
            saveDisplayName(context, copyResult.absolutePath, displayName)
            return RingtoneResult(copyResult.absolutePath, displayName)
        }

        // If direct copy failed (e.g. symbolic system setting URI), try resolving actual default uri
        val actualDefault = try {
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        } catch (_: Exception) {
            null
        }

        if (actualDefault != null && actualDefault != sourceUri) {
            val fallbackCopy = copyUriToInternalStorage(context, actualDefault, displayName)
            if (fallbackCopy != null) {
                saveDisplayName(context, fallbackCopy.absolutePath, displayName)
                return RingtoneResult(fallbackCopy.absolutePath, displayName)
            }
        }

        // Store original URI string as fallback
        val uriStr = sourceUri.toString()
        saveDisplayName(context, uriStr, displayName)
        return RingtoneResult(uriStr, displayName)
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri, title: String): File? {
        return try {
            val dir = File(context.filesDir, "custom_ringtones")
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val sanitizedTitle = title
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .take(30)
                .ifBlank { "tone" }

            val targetFile = File(dir, "alarm_tone_${System.currentTimeMillis()}_$sanitizedTitle.audio")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

            if (targetFile.exists() && targetFile.length() > 500) {
                Log.d(TAG, "Successfully copied custom ringtone to: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
                targetFile
            } else {
                targetFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to copy ringtone uri $uri to internal storage", e)
            null
        }
    }

    private fun resolveRingtoneTitle(context: Context, uri: Uri): String {
        // 1. Try OpenableColumns (for document and content pickers)
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            return name.substringBeforeLast(".")
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // 2. Try RingtoneManager
        try {
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone?.getTitle(context)
            if (!title.isNullOrBlank()) {
                return title
            }
        } catch (_: Exception) { }

        // 3. Fallback to path segment or generic
        return uri.lastPathSegment?.substringBeforeLast(".")?.ifBlank { "Device Ringtone" } ?: "Device Ringtone"
    }

    fun getDisplayName(context: Context, presetOrPathOrUri: String): String {
        return when (presetOrPathOrUri.uppercase()) {
            "CYBER_SIREN" -> "Cyber Siren (High Alert)"
            "APEX_HORNS" -> "Apex Horns (Brass Wake)"
            "QUANTUM_PULSE" -> "Quantum Pulse (Electronic)"
            "ZEN_CHIME" -> "Zen Chime (Gentle Acoustic)"
            "BELL" -> "Classic Alarm Bell"
            "MILITARY" -> "Military Bugle Reveille"
            "TICK_TOCK" -> "Urgent Tick-Tock"
            "SYSTEM_DEFAULT" -> "System Default Alarm"
            else -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val savedName = prefs.getString(presetOrPathOrUri, null)
                if (!savedName.isNullOrBlank()) {
                    return savedName
                }

                if (presetOrPathOrUri.startsWith("/") || presetOrPathOrUri.startsWith("file://")) {
                    val file = if (presetOrPathOrUri.startsWith("file://")) {
                        File(Uri.parse(presetOrPathOrUri).path ?: "")
                    } else {
                        File(presetOrPathOrUri)
                    }
                    val fileName = file.nameWithoutExtension
                    if (fileName.startsWith("alarm_tone_")) {
                        val parts = fileName.split("_", limit = 4)
                        if (parts.size >= 4) {
                            return parts[3].replace("_", " ")
                        }
                    }
                    return if (fileName.isNotBlank()) fileName.replace("_", " ") else "Custom Device Ringtone"
                }

                if (presetOrPathOrUri.startsWith("content://") || presetOrPathOrUri.startsWith("android.resource://")) {
                    try {
                        val ringtone = RingtoneManager.getRingtone(context, Uri.parse(presetOrPathOrUri))
                        val title = ringtone?.getTitle(context)
                        if (!title.isNullOrBlank()) return title
                    } catch (_: Exception) { }
                    return "Custom Device Ringtone"
                }

                presetOrPathOrUri.replace("_", " ")
            }
        }
    }

    private fun saveDisplayName(context: Context, key: String, name: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(key, name).apply()
        } catch (_: Exception) { }
    }

    /**
     * Prepares and starts MediaPlayer with the selected tone, applying robust multi-tiered fallbacks.
     */
    fun setupAndPlayAudio(
        context: Context,
        mediaPlayer: MediaPlayer,
        presetOrPathOrUri: String,
        volumePercent: Int = 90,
        isLooping: Boolean = true
    ): Boolean {
        var initialized = false

        // 1. Try local file path (Primary for copied device ringtones)
        if (presetOrPathOrUri.startsWith("/") || presetOrPathOrUri.startsWith("file://")) {
            val filePath = if (presetOrPathOrUri.startsWith("file://")) {
                Uri.parse(presetOrPathOrUri).path ?: ""
            } else {
                presetOrPathOrUri
            }
            val localFile = File(filePath)
            if (localFile.exists() && localFile.length() > 0) {
                try {
                    FileInputStream(localFile).use { fis ->
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(fis.fd)
                        initialized = true
                        Log.d(TAG, "Playing local ringtone file: ${localFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing local ringtone file", e)
                }
            }
        }

        // 2. Try preset tones (synthesizes PCM wave if needed)
        if (!initialized) {
            val presetFile = PresetAudioGenerator.getPresetFile(context, presetOrPathOrUri)
            if (presetFile != null && presetFile.exists() && presetFile.length() > 0) {
                try {
                    FileInputStream(presetFile).use { fis ->
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(fis.fd)
                        initialized = true
                        Log.d(TAG, "Playing preset ringtone: $presetOrPathOrUri from ${presetFile.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing preset tone", e)
                }
            }
        }

        // 3. Try content URI
        if (!initialized && presetOrPathOrUri.startsWith("content://")) {
            val uri = Uri.parse(presetOrPathOrUri)
            // Attempt to copy on-the-fly to internal storage
            val copied = copyUriToInternalStorage(context, uri, "cached_alarm_ringtone")
            if (copied != null && copied.exists()) {
                try {
                    FileInputStream(copied).use { fis ->
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(fis.fd)
                        initialized = true
                        Log.d(TAG, "Playing cached ringtone from content uri")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing newly cached ringtone", e)
                }
            }

            // If on-the-fly copy failed, attempt openAssetFileDescriptor
            if (!initialized) {
                try {
                    context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        initialized = true
                        Log.d(TAG, "Playing ringtone via asset file descriptor: $uri")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Asset file descriptor failed for $uri", e)
                }
            }

            // Direct setDataSource
            if (!initialized) {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context.applicationContext, uri)
                    initialized = true
                    Log.d(TAG, "Playing ringtone directly from URI: $uri")
                } catch (e: Exception) {
                    Log.w(TAG, "Direct URI setDataSource failed for $uri", e)
                }
            }
        }

        // 4. Fallback to system default alarm or ringtone
        if (!initialized) {
            val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if (defaultAlarmUri != null) {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context.applicationContext, defaultAlarmUri)
                    initialized = true
                    Log.d(TAG, "Playing system default fallback ringtone: $defaultAlarmUri")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load default ringtone", e)
                }
            }
        }

        if (!initialized) {
            return false
        }

        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()
            )
            mediaPlayer.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mediaPlayer.isLooping = isLooping

            val factor = (volumePercent.coerceIn(10, 100)) / 100f
            mediaPlayer.setVolume(factor, factor)

            mediaPlayer.prepare()
            mediaPlayer.start()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed during prepare/start of MediaPlayer", e)
            return false
        }
    }
}
