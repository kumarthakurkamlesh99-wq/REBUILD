package com.example.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmForegroundService : Service() {

    companion object {
        const val TAG = "AlarmForegroundService"
        const val NOTIFICATION_ID = 99999

        const val ACTION_START_ALARM = "com.example.notification.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.notification.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.notification.ACTION_SNOOZE_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_CHALLENGE_TYPE = "extra_challenge_type"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"
        const val EXTRA_MAX_SNOOZES = "extra_max_snoozes"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"
        const val EXTRA_CURRENT_SNOOZES = "extra_current_snoozes"
        const val EXTRA_RINGTONE_PRESET = "extra_ringtone_preset"
        const val EXTRA_SNOOZE_RINGTONE_PRESET = "extra_snooze_ringtone_preset"
        const val EXTRA_IS_SNOOZE_TRIGGER = "extra_is_snooze_trigger"

        fun startAlarm(
            context: Context,
            alarmId: Long,
            title: String,
            challengeType: String = "MATH",
            difficulty: String = "MEDIUM",
            volume: Int = 90,
            isVibrate: Boolean = true,
            maxSnoozes: Int = 3,
            snoozeDuration: Int = 5,
            currentSnoozeCount: Int = 0,
            ringtonePreset: String = "CYBER_SIREN",
            snoozeRingtonePreset: String = "TICK_TOCK",
            isSnoozeTrigger: Boolean = false
        ) {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_TITLE, title)
                putExtra(EXTRA_CHALLENGE_TYPE, challengeType)
                putExtra(EXTRA_DIFFICULTY, difficulty)
                putExtra(EXTRA_VOLUME, volume)
                putExtra(EXTRA_VIBRATE, isVibrate)
                putExtra(EXTRA_MAX_SNOOZES, maxSnoozes)
                putExtra(EXTRA_SNOOZE_DURATION, snoozeDuration)
                putExtra(EXTRA_CURRENT_SNOOZES, currentSnoozeCount)
                putExtra(EXTRA_RINGTONE_PRESET, ringtonePreset)
                putExtra(EXTRA_SNOOZE_RINGTONE_PRESET, snoozeRingtonePreset)
                putExtra(EXTRA_IS_SNOOZE_TRIGGER, isSnoozeTrigger)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AlarmForegroundService", e)
            }
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send stop to AlarmForegroundService", e)
            }
        }

        fun snoozeAlarm(context: Context) {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                action = ACTION_SNOOZE_ALARM
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send snooze to AlarmForegroundService", e)
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentAlarmId: Long = 0L
    private var currentTitle: String = "Apex Protocol Wake-Up"
    private var currentChallengeType: String = "MATH"
    private var currentDifficulty: String = "MEDIUM"
    private var currentVolume: Int = 90
    private var isVibrateEnabled: Boolean = true
    private var maxSnoozes: Int = 3
    private var snoozeDuration: Int = 5
    private var currentSnoozesUsed: Int = 0
    private var ringtoneUriString: String = "CYBER_SIREN"
    private var snoozeRingtoneUriString: String = "TICK_TOCK"
    private var isSnoozeTrigger: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START_ALARM -> {
                currentAlarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L)
                currentTitle = intent.getStringExtra(EXTRA_ALARM_TITLE) ?: "Apex Protocol Wake-Up"
                currentChallengeType = intent.getStringExtra(EXTRA_CHALLENGE_TYPE) ?: "MATH"
                currentDifficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"
                currentVolume = intent.getIntExtra(EXTRA_VOLUME, 90)
                isVibrateEnabled = intent.getBooleanExtra(EXTRA_VIBRATE, true)
                maxSnoozes = intent.getIntExtra(EXTRA_MAX_SNOOZES, 3)
                snoozeDuration = intent.getIntExtra(EXTRA_SNOOZE_DURATION, 5)
                currentSnoozesUsed = intent.getIntExtra(EXTRA_CURRENT_SNOOZES, 0)
                ringtoneUriString = intent.getStringExtra(EXTRA_RINGTONE_PRESET) ?: "CYBER_SIREN"
                snoozeRingtoneUriString = intent.getStringExtra(EXTRA_SNOOZE_RINGTONE_PRESET) ?: "TICK_TOCK"
                isSnoozeTrigger = intent.getBooleanExtra(EXTRA_IS_SNOOZE_TRIGGER, false)

                startForegroundNotification()
                requestAudioFocus()
                startSoundPlayback()
                if (isVibrateEnabled) {
                    startVibration()
                }
            }
            ACTION_STOP_ALARM -> {
                stopAlarmAndSelf()
            }
            ACTION_SNOOZE_ALARM -> {
                handleSnoozeAction()
            }
            else -> {
                stopAlarmAndSelf()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null || !wakeLock!!.isHeld) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "REBUILD:AlarmForegroundWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(15 * 60 * 1000L) // Max 15 minutes safety hold
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }

    private fun startForegroundNotification() {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(AlarmDismissActivity.EXTRA_ALARM_ID, currentAlarmId)
            putExtra(AlarmDismissActivity.EXTRA_ALARM_TITLE, currentTitle)
            putExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE, currentChallengeType)
            putExtra(AlarmDismissActivity.EXTRA_DIFFICULTY, currentDifficulty)
            putExtra(AlarmDismissActivity.EXTRA_VOLUME, currentVolume)
            putExtra(AlarmDismissActivity.EXTRA_VIBRATE, isVibrateEnabled)
            putExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, maxSnoozes)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, snoozeDuration)
            putExtra(AlarmDismissActivity.EXTRA_RINGTONE_PRESET, ringtoneUriString)
            putExtra(AlarmDismissActivity.EXTRA_SNOOZE_RINGTONE_PRESET, snoozeRingtoneUriString)
            putExtra(AlarmDismissActivity.EXTRA_IS_SNOOZE_TRIGGER, isSnoozeTrigger)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            currentAlarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeServiceIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            (currentAlarmId + 1).toInt(),
            snoozeServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Stop / Dismiss
        val stopServiceIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            (currentAlarmId + 2).toInt(),
            stopServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_RINGING_ALARM)
            .setSmallIcon(R.drawable.ic_rebuild_logo)
            .setContentTitle("🚨 $currentTitle")
            .setContentText("Discipline Alert active • Tap or solve challenge to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setColor(0xFF00E5FF.toInt())
            .addAction(R.drawable.ic_rebuild_logo, "SNOOZE (${snoozeDuration}m)", snoozePendingIntent)
            .addAction(R.drawable.ic_rebuild_logo, "DISMISS CHALLENGE", fullScreenPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun requestAudioFocus() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* maintain continuous ringing */ }
                .build()

            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    private fun startSoundPlayback() {
        try {
            stopMediaPlayer()

            // Resolve Tone Uri based on whether this is a snooze trigger or primary alarm
            val chosenUriString = if (isSnoozeTrigger && snoozeRingtoneUriString.isNotBlank() && snoozeRingtoneUriString != "TICK_TOCK") {
                snoozeRingtoneUriString
            } else {
                ringtoneUriString
            }

            var alertUri: Uri? = null
            if (chosenUriString.startsWith("content://") || chosenUriString.startsWith("file://") || chosenUriString.startsWith("android.resource://")) {
                try {
                    alertUri = Uri.parse(chosenUriString)
                } catch (e: Exception) {
                    alertUri = null
                }
            }

            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alertUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                isLooping = true

                val volumeFactor = (currentVolume.coerceIn(10, 100)) / 100f
                setVolume(volumeFactor, volumeFactor)

                prepare()
                start()
            }
            Log.d(TAG, "Playing alarm audio uri: $alertUri, looping=true, volume=$currentVolume")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start media player with primary uri, falling back to default ringtone", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, fallbackUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Critical fallback media player error", fallbackEx)
            }
        }
    }

    private fun stopMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Continuous intense alarm pulse pattern: 0ms delay, 800ms on, 400ms off, 800ms on, 400ms off, 1200ms on
            val pattern = longArrayOf(0, 800, 400, 800, 400, 1200, 500)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, 0) // repeat from index 0
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                vibrator?.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start continuous vibration", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling vibration", e)
        }
    }

    private fun handleSnoozeAction() {
        if (currentSnoozesUsed < maxSnoozes) {
            AlarmScheduler.scheduleSnooze(
                context = applicationContext,
                alarmId = currentAlarmId,
                title = currentTitle,
                challengeType = currentChallengeType,
                difficulty = currentDifficulty,
                volume = currentVolume,
                isVibrate = isVibrateEnabled,
                maxSnoozes = maxSnoozes,
                snoozeMinutes = snoozeDuration,
                snoozesUsedSoFar = currentSnoozesUsed + 1,
                ringtonePreset = ringtoneUriString,
                snoozeRingtonePreset = snoozeRingtoneUriString
            )
        }
        stopAlarmAndSelf()
    }

    private fun stopAlarmAndSelf() {
        stopMediaPlayer()
        stopVibration()
        abandonAudioFocus()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMediaPlayer()
        stopVibration()
        abandonAudioFocus()
        releaseWakeLock()
        serviceScope.cancel()
    }
}
