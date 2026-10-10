package com.example.notification

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.RebuildApplication
import com.example.data.local.AppDatabase
import com.example.data.model.futureself.VoiceLanguage
import com.example.data.model.futureself.VoiceTone
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.speech.FutureSelfMessageEngine
import com.example.speech.FutureSelfSpeechManager
import com.example.ui.navigation.Screen
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.math.roundToInt
import android.util.Log

class FutureSelfCallActivity : ComponentActivity() {

    companion object {
        private const val TAG = "FutureSelfCallActivity"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_SUBJECT = "extra_task_subject"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_XP_REWARD = "extra_xp_reward"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
        const val EXTRA_CUSTOM_QUOTE = "extra_custom_quote"
        const val EXTRA_DELAY_MINUTES = "extra_delay_minutes"
        const val EXTRA_IS_MAX_DELAY = "extra_is_max_delay"
    }

    private lateinit var speechManager: FutureSelfSpeechManager
    private lateinit var settingsRepo: FutureSelfSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce lockscreen wakeup & screen turn-on like a real phone dialer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager?.requestDismissKeyguard(this, null)
            }
        } catch (_: Exception) {}

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechManager = FutureSelfSpeechManager.getInstance(applicationContext)
        settingsRepo = FutureSelfSettingsRepository.getInstance(applicationContext)

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Physics Electrostatics"
        val taskSubject = intent.getStringExtra(EXTRA_TASK_SUBJECT) ?: "Physics"
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: "05:00 AM"
        val endTime = intent.getStringExtra(EXTRA_END_TIME) ?: "06:30 AM"
        val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 90)
        val xpReward = intent.getIntExtra(EXTRA_XP_REWARD, 150)
        val studentName = intent.getStringExtra(EXTRA_STUDENT_NAME) ?: "Rudra"
        val customQuote = intent.getStringExtra(EXTRA_CUSTOM_QUOTE) ?: ""
        val delayMinutesSoFar = intent.getIntExtra(EXTRA_DELAY_MINUTES, 0)
        val isMaxDelayFromExtra = intent.getBooleanExtra(EXTRA_IS_MAX_DELAY, false)
        val occurrenceId = intent.getStringExtra(FutureSelfCallEngine.EXTRA_CALL_OCCURRENCE_ID) ?: ""
        var callHandled = false

        val settings = settingsRepo.getSettingsSync()
        val isMaxDelay = isMaxDelayFromExtra || (delayMinutesSoFar >= settings.maxAllowedDelayMinutes)

        fun executeMaxDelayAutoSpeech() {
            callHandled = true
            lifecycleScope.launch(Dispatchers.IO) {
                FutureSelfCallEngine.markAnswered(applicationContext, occurrenceId, taskId)
            }
            speechManager.stopRingtoneAndVibrate()
            val maxDelayText = FutureSelfMessageEngine.buildMaxDelaySpeechText(
                subject = taskSubject,
                title = taskTitle,
                durationMinutes = durationMinutes,
                language = settings.language
            )

            val customAudio = when {
                settings.customMaxDelayAudioUri.isNotBlank() && File(settings.customMaxDelayAudioUri).let { it.exists() && it.length() > 200 } -> settings.customMaxDelayAudioUri
                settings.useCustomCallAudio && settings.customCallAudioUri.isNotBlank() && File(settings.customCallAudioUri).let { it.exists() && it.length() > 200 } -> settings.customCallAudioUri
                else -> ""
            }

            if (customAudio.isNotBlank()) {
                speechManager.playCustomAudioOrSpeech(
                    customAudioPath = customAudio,
                    fallbackText = maxDelayText,
                    tone = settings.tone,
                    language = settings.language,
                    gender = settings.voiceGender,
                    speedMultiplier = settings.speechSpeed,
                    volume = settings.volume,
                    onDone = {
                        launchFocusMode(taskId, taskSubject, taskTitle, durationMinutes)
                    }
                )
            } else {
                speechManager.speak(
                    text = maxDelayText,
                    tone = settings.tone,
                    language = settings.language,
                    gender = settings.voiceGender,
                    speedMultiplier = settings.speechSpeed,
                    onDone = {
                        launchFocusMode(taskId, taskSubject, taskTitle, durationMinutes)
                    }
                )
            }
        }

        if (isMaxDelay && settings.autoStartOnMaxDelay) {
            // Max delay reached: auto-play audio directive immediately
            executeMaxDelayAutoSpeech()
        } else {
            // Normal call: play realistic incoming ringtone and vibration pattern
            speechManager.startRingtoneAndVibrate(
                presetKey = settings.ringtonePreset,
                customRingtoneUri = settings.customRingtoneUri,
                volume = settings.volume,
                enableVibration = settings.vibrationEnabled
            )
        }

        setContent {
            val motivationalQuote = remember {
                FutureSelfMessageEngine.getCategoryQuote(taskSubject, taskTitle, customQuote)
            }

            RealWorldCallScreen(
                studentName = studentName,
                taskSubject = taskSubject,
                taskTitle = taskTitle,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                xpReward = xpReward,
                motivationalQuote = motivationalQuote,
                isMaxDelay = isMaxDelay,
                delayMinutesSoFar = delayMinutesSoFar,
                maxAllowedDelayMinutes = settings.maxAllowedDelayMinutes,
                callTimeoutSeconds = settings.callTimeoutSeconds,
                onAnswerCall = {
                    callHandled = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        FutureSelfCallEngine.markAnswered(applicationContext, occurrenceId, taskId)
                    }
                    speechManager.stopRingtoneAndVibrate()
                    val ignitionText = FutureSelfMessageEngine.buildIgnitionSpeechText(
                        subject = taskSubject,
                        title = taskTitle,
                        durationMinutes = durationMinutes,
                        language = settings.language
                    )

                    val hasCustomCallAudio = settings.useCustomCallAudio &&
                            settings.customCallAudioUri.isNotBlank() &&
                            File(settings.customCallAudioUri).let { it.exists() && it.length() > 200 }

                    if (hasCustomCallAudio) {
                        Log.i(TAG, "Playing user's uploaded custom audio on call answer: ${settings.customCallAudioName}")
                        speechManager.playCustomAudioOrSpeech(
                            customAudioPath = settings.customCallAudioUri,
                            fallbackText = ignitionText,
                            tone = settings.tone,
                            language = settings.language,
                            gender = settings.voiceGender,
                            speedMultiplier = settings.speechSpeed,
                            volume = settings.volume,
                            onDone = {
                                launchFocusMode(taskId, taskSubject, taskTitle, durationMinutes)
                            }
                        )
                    } else {
                        speechManager.speak(
                            text = ignitionText,
                            tone = settings.tone,
                            language = settings.language,
                            gender = settings.voiceGender,
                            speedMultiplier = settings.speechSpeed,
                            onDone = {
                                launchFocusMode(taskId, taskSubject, taskTitle, durationMinutes)
                            }
                        )
                    }
                },
                onMaxDelayAutoIgnite = {
                    executeMaxDelayAutoSpeech()
                },
                onEndCall = {
                    callHandled = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        FutureSelfCallEngine.markDismissed(applicationContext, occurrenceId, taskId, "USER_DECLINED")
                    }
                    speechManager.stopRingtoneAndVibrate()
                    speechManager.stopSpeaking()
                    launchFocusMode(taskId, taskSubject, taskTitle, durationMinutes)
                },
                onDelayTaskConfirmed = { delayMinutes ->
                    callHandled = true
                    speechManager.stopRingtoneAndVibrate()

                    val app = application as? RebuildApplication
                    val repo = app?.repository

                    // Compute new start time
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.MINUTE, delayMinutes)
                    }
                    val newHour = cal.get(Calendar.HOUR_OF_DAY)
                    val newMinute = cal.get(Calendar.MINUTE)
                    val newFormattedTime = DateTimeUtils.formatTo12Hour(
                        String.format(java.util.Locale.US, "%02d:%02d", newHour, newMinute)
                    )

                    val updatedTotalDelay = delayMinutesSoFar + delayMinutes

                    if (repo != null && taskId > 0L) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val task = repo.getTaskById(taskId)
                            if (task != null) {
                                repo.delayTask(task, delayMinutes)
                            }
                            FutureSelfCallEngine.snoozeCall(
                                context = applicationContext,
                                occurrenceId = occurrenceId,
                                taskId = taskId,
                                snoozeMinutes = delayMinutes,
                                taskTitle = taskTitle,
                                taskSubject = taskSubject,
                                durationMinutes = durationMinutes,
                                xpReward = xpReward
                            )
                        }
                    }

                    if (updatedTotalDelay >= settings.maxAllowedDelayMinutes) {
                        executeMaxDelayAutoSpeech()
                    } else {
                        val delayText = FutureSelfMessageEngine.buildDelaySpeechText(
                            delayMinutes = delayMinutes,
                            newStartTime = newFormattedTime,
                            language = settings.language
                        )

                        speechManager.speak(
                            text = delayText,
                            tone = settings.tone,
                            language = settings.language,
                            gender = settings.voiceGender,
                            speedMultiplier = settings.speechSpeed,
                            onDone = {
                                finish()
                            }
                        )
                    }
                }
            )
        }
    }

    private fun launchFocusMode(taskId: Long, subject: String, title: String, durationMinutes: Int) {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_START_DESTINATION", Screen.Focus.route)
            putExtra("EXTRA_FOCUS_TASK_ID", taskId)
            putExtra("EXTRA_FOCUS_SUBJECT", subject)
            putExtra("EXTRA_FOCUS_TITLE", title)
            putExtra("EXTRA_FOCUS_MINUTES", durationMinutes)
        }
        startActivity(launchIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.stopRingtoneAndVibrate()
        speechManager.stopSpeaking()
        val occId = intent?.getStringExtra(FutureSelfCallEngine.EXTRA_CALL_OCCURRENCE_ID) ?: ""
        val tId = intent?.getLongExtra(EXTRA_TASK_ID, 0L) ?: 0L
        if (tId > 0L && occId.isNotBlank()) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext, this)
                    val record = db.futureSelfCallDao().getRecordByOccurrenceId(occId)
                    if (record?.state == com.example.data.local.entity.FutureSelfCallState.RINGING) {
                        FutureSelfCallEngine.markDismissed(applicationContext, occId, tId, "ACTIVITY_DESTROYED")
                    }
                } catch (_: Exception) {}
            }
        }
    }
}

enum class CallScreenState {
    RINGING,
    CONNECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealWorldCallScreen(
    studentName: String,
    taskSubject: String,
    taskTitle: String,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    xpReward: Int,
    motivationalQuote: String,
    isMaxDelay: Boolean = false,
    delayMinutesSoFar: Int = 0,
    maxAllowedDelayMinutes: Int = 60,
    callTimeoutSeconds: Int = 40,
    onAnswerCall: () -> Unit,
    onMaxDelayAutoIgnite: () -> Unit,
    onEndCall: () -> Unit,
    onDelayTaskConfirmed: (delayMinutes: Int) -> Unit
) {
    val context = LocalContext.current
    var callState by remember {
        mutableStateOf(if (isMaxDelay) CallScreenState.CONNECTED else CallScreenState.RINGING)
    }

    // Call duration timer (seconds) for the in-call screen (00:01, 00:02...)
    var callDurationSeconds by remember { mutableIntStateOf(0) }

    // Live speech transcript / subtitle
    var currentSpeechSubtitle by remember {
        mutableStateOf(
            if (isMaxDelay)
                "Maximum delay threshold reached. No more excuses. Directive initiated for $taskSubject $taskTitle. Focus now."
            else
                "Connecting with Future $studentName..."
        )
    }

    // Sheets
    var showDelaySheet by remember { mutableStateOf(false) }
    var showQuickMessageSheet by remember { mutableStateOf(false) }
    var showKeypadSheet by remember { mutableStateOf(false) }
    var showDossierDialog by remember { mutableStateOf(false) }

    // In-call toggles
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    // In-call duration timer
    LaunchedEffect(callState) {
        if (callState == CallScreenState.CONNECTED) {
            while (true) {
                delay(1000L)
                callDurationSeconds++
            }
        }
    }

    // Auto-timeout for unanswered ringing call
    LaunchedEffect(callState, isMaxDelay) {
        if (callState == CallScreenState.RINGING && !isMaxDelay) {
            delay(callTimeoutSeconds * 1000L)
            if (callState == CallScreenState.RINGING) {
                callState = CallScreenState.CONNECTED
                currentSpeechSubtitle = "Temporal call auto-connected. Directive initiated."
                onAnswerCall()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030712),
                        Color(0xFF0B132B),
                        Color(0xFF02040A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (callState == CallScreenState.RINGING) {
            // =========================================================================
            // 1. INCOMING PHONE CALL SCREEN (100% REALISTIC SMARTPHONE DIALER)
            // =========================================================================
            IncomingRealCallView(
                studentName = studentName,
                taskSubject = taskSubject,
                taskTitle = taskTitle,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                xpReward = xpReward,
                motivationalQuote = motivationalQuote,
                isMaxDelay = isMaxDelay,
                delayMinutesSoFar = delayMinutesSoFar,
                maxAllowedDelayMinutes = maxAllowedDelayMinutes,
                onAnswer = {
                    callState = CallScreenState.CONNECTED
                    currentSpeechSubtitle = "Message from Future $studentName: $taskSubject $taskTitle session has started. Focus now."
                    onAnswerCall()
                },
                onDeclineOrDelay = {
                    if (isMaxDelay) {
                        onMaxDelayAutoIgnite()
                    } else {
                        showDelaySheet = true
                    }
                },
                onRemindMeClick = {
                    if (isMaxDelay) {
                        Toast.makeText(context, "Maximum delay reached. Deferral blocked.", Toast.LENGTH_SHORT).show()
                    } else {
                        showDelaySheet = true
                    }
                },
                onQuickMessageClick = {
                    showQuickMessageSheet = true
                }
            )
        } else {
            // =========================================================================
            // 2. ACTIVE IN-CALL SCREEN (AUTHENTIC CONNECTED PHONE CALL WITH WAVEFORM)
            // =========================================================================
            ActiveInCallView(
                studentName = studentName,
                taskSubject = taskSubject,
                taskTitle = taskTitle,
                durationMinutes = durationMinutes,
                xpReward = xpReward,
                callDurationSeconds = callDurationSeconds,
                currentSpeechSubtitle = currentSpeechSubtitle,
                isMaxDelay = isMaxDelay,
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                onToggleMute = { isMuted = !isMuted },
                onToggleSpeaker = { isSpeakerOn = !isSpeakerOn },
                onKeypadClick = { showKeypadSheet = true },
                onDossierClick = { showDossierDialog = true },
                onEndCall = onEndCall
            )
        }
    }

    // -------------------------------------------------------------------------
    // Quick Reply Message Bottom Sheet (Like Real Android Phone)
    // -------------------------------------------------------------------------
    if (showQuickMessageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickMessageSheet = false },
            containerColor = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "QUICK RESPONSE TO FUTURE SELF",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Send an immediate commitment protocol:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val quickReplies = listOf(
                    "Starting right now! Entering focus mode." to 0,
                    "Need 10 minutes to reach my desk." to 10,
                    "Preparing notes. Starting in 15 minutes." to 15,
                    "Rescheduling 30 minutes forward." to 30
                )

                quickReplies.forEach { (text, delayMin) ->
                    val willExceed = (delayMinutesSoFar + delayMin) > maxAllowedDelayMinutes && delayMin > 0
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                showQuickMessageSheet = false
                                if (delayMin == 0) {
                                    callState = CallScreenState.CONNECTED
                                    onAnswerCall()
                                } else {
                                    if (willExceed) {
                                        onMaxDelayAutoIgnite()
                                    } else {
                                        onDelayTaskConfirmed(delayMin)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (willExceed) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (willExceed) Color(0xFFEF4444) else Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = text,
                                color = if (willExceed) Color(0xFFFCA5A5) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (delayMin > 0) {
                                Text(
                                    text = if (willExceed) "Exceeds Max" else "+$delayMin m",
                                    color = if (willExceed) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Delay / Reschedule Sheet (Remind Me)
    // -------------------------------------------------------------------------
    if (showDelaySheet) {
        ModalBottomSheet(
            onDismissRequest = { showDelaySheet = false },
            containerColor = Color(0xFF0B132B),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "TACTICAL RESCHEDULING",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Select Deferral Window",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val remainingDelay = maxOf(0, maxAllowedDelayMinutes - delayMinutesSoFar)
                Text(
                    text = "Accumulated delay: $delayMinutesSoFar min / $maxAllowedDelayMinutes min max allowance ($remainingDelay min remaining).",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                val options = listOf(
                    Pair(15, "15 Minutes (Brief Setup)"),
                    Pair(30, "30 Minutes (Short Break)"),
                    Pair(60, "1 Hour (Later Block)"),
                    Pair(180, "3 Hours (Evening Shift)"),
                    Pair(1440, "Tomorrow Morning")
                )

                options.forEach { (minutes, title) ->
                    val willExceedMax = (delayMinutesSoFar + minutes) > maxAllowedDelayMinutes
                    OutlinedButton(
                        onClick = {
                            showDelaySheet = false
                            if (willExceedMax) {
                                onMaxDelayAutoIgnite()
                            } else {
                                onDelayTaskConfirmed(minutes)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (willExceedMax) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, if (willExceedMax) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (willExceedMax) "$title (Hits Max Limit)" else title,
                                color = if (willExceedMax) Color(0xFFFCA5A5) else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "+$minutes min",
                                color = if (willExceedMax) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Interactive In-Call Dialpad Sheet (Real Phone Keypad)
    // -------------------------------------------------------------------------
    if (showKeypadSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKeypadSheet = false },
            containerColor = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TEMPORAL FREQUENCY KEYPAD",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val keypadButtons = listOf(
                    "1" to "", "2" to "ABC", "3" to "DEF",
                    "4" to "GHI", "5" to "JKL", "6" to "MNO",
                    "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
                    "*" to "", "0" to "+", "#" to ""
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(keypadButtons) { (digit, letters) ->
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable {
                                    Toast.makeText(context, "DTMF Tone: $digit", Toast.LENGTH_SHORT).show()
                                },
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = digit,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (letters.isNotBlank()) {
                                    Text(
                                        text = letters,
                                        color = Color(0xFF64748B),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showKeypadSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hide Keypad", color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Task Dossier Dialog
    // -------------------------------------------------------------------------
    if (showDossierDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDossierDialog = false },
            containerColor = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "SESSION PROTOCOL DOSSIER",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$taskSubject: $taskTitle",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Scheduled: $startTime - $endTime ($durationMinutes minutes)",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
                Text(
                    text = "Completion Reward: +$xpReward XP",
                    color = Color(0xFFFCD34D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "\"$motivationalQuote\"",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// =============================================================================
// INCOMING REAL CALL VIEW (Looks 100% like real smartphone incoming call)
// =============================================================================
@Composable
fun IncomingRealCallView(
    studentName: String,
    taskSubject: String,
    taskTitle: String,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    xpReward: Int,
    motivationalQuote: String,
    isMaxDelay: Boolean,
    delayMinutesSoFar: Int,
    maxAllowedDelayMinutes: Int,
    onAnswer: () -> Unit,
    onDeclineOrDelay: () -> Unit,
    onRemindMeClick: () -> Unit,
    onQuickMessageClick: () -> Unit
) {
    // Pulsing acoustic sound ripples for incoming call
    val infiniteTransition = rememberInfiniteTransition(label = "realCallPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )
    val chevronBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chevronBounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: Real-world carrier & protocol bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Surface(
                color = if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMaxDelay) "MAX DELAY EXCEEDED • DIRECTIVE READY" else "INCOMING TEMPORAL AUDIO CALL",
                        color = if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Realistic Caller Name (Huge, Crisp, Authoritative)
            Text(
                text = "Future $studentName (2030)",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Caller Number & Protocol
            Text(
                text = "+91 99999 02030 • Encrypted Line",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Pill
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Text(
                    text = "TARGET: ${taskSubject.uppercase()} • $durationMinutes MIN",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Center: Holographic Caller Photo / Avatar with Acoustic Wave Rings
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Outermost Acoustic Ripple Ring
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        Brush.sweepGradient(
                            if (isMaxDelay) listOf(
                                Color(0xFFEF4444).copy(alpha = ringAlpha),
                                Color(0xFFF59E0B).copy(alpha = ringAlpha),
                                Color(0xFFEF4444).copy(alpha = ringAlpha)
                            ) else listOf(
                                Color(0xFF10B981).copy(alpha = ringAlpha),
                                Color(0xFF00F0FF).copy(alpha = ringAlpha),
                                Color(0xFF10B981).copy(alpha = ringAlpha)
                            )
                        ),
                        CircleShape
                    )
            )

            // Inner Ring
            Box(
                modifier = Modifier
                    .size(145.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        2.dp,
                        if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Future Self Avatar",
                    tint = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF38BDF8),
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        // Mid-Card: Task Title & Quote Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = taskTitle,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$startTime - $endTime  •  +$xpReward XP",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (motivationalQuote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$motivationalQuote\"",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }

        // Quick Real-World Call Actions (Remind Me & Message)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Remind Me Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRemindMeClick() }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Remind Me",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Remind Me",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Message Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onQuickMessageClick() }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Message",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Message",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Real-World Dialpad Controls: Dual Red & Green Swipe/Tap Buttons
        RealisticCallButtonsPad(
            isMaxDelay = isMaxDelay,
            chevronBounce = chevronBounce,
            onAnswer = onAnswer,
            onDeclineOrDelay = onDeclineOrDelay
        )
    }
}

// =============================================================================
// REALISTIC CALL BUTTONS PAD (UPWARD & HORIZONTAL SWIPE GESTURES + LABELS)
// =============================================================================
@Composable
fun RealisticCallButtonsPad(
    isMaxDelay: Boolean,
    chevronBounce: Float,
    onAnswer: () -> Unit,
    onDeclineOrDelay: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btnRipple")
    val buttonRippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btnRippleScale"
    )
    val buttonRippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btnRippleAlpha"
    )

    var redOffsetY by remember { mutableFloatStateOf(0f) }
    var redOffsetX by remember { mutableFloatStateOf(0f) }
    var greenOffsetY by remember { mutableFloatStateOf(0f) }
    var greenOffsetX by remember { mutableFloatStateOf(0f) }

    val dragThresholdPx = with(LocalDensity.current) { 40.dp.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // ---------------------------------------------------------------------
        // LEFT: DECLINE / DELAY (RED)
        // ---------------------------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            if (isMaxDelay) {
                Surface(
                    color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MAX DELAY HIT",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Animated upward chevrons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset { IntOffset(0, (chevronBounce + redOffsetY).roundToInt()) }
                        .padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444).copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                if (!isMaxDelay) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(buttonRippleScale)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = buttonRippleAlpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(redOffsetX.roundToInt(), redOffsetY.roundToInt()) }
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMaxDelay) {
                                Brush.verticalGradient(listOf(Color(0xFF450A0A), Color(0xFF1E293B)))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                            }
                        )
                        .border(2.dp, if (isMaxDelay) Color(0xFF7F1D1D) else Color(0xFFFCA5A5), CircleShape)
                        .pointerInput(isMaxDelay) {
                            if (!isMaxDelay) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (redOffsetY <= -dragThresholdPx) onDeclineOrDelay()
                                        redOffsetY = 0f
                                    },
                                    onDragCancel = { redOffsetY = 0f },
                                    onVerticalDrag = { _, amount ->
                                        val new = redOffsetY + amount
                                        if (new <= 0f && new >= -dragThresholdPx * 2.2f) redOffsetY = new
                                    }
                                )
                            }
                        }
                        .pointerInput(isMaxDelay) {
                            if (!isMaxDelay) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (redOffsetX <= -dragThresholdPx) onDeclineOrDelay()
                                        redOffsetX = 0f
                                    },
                                    onDragCancel = { redOffsetX = 0f },
                                    onHorizontalDrag = { _, amount ->
                                        val new = redOffsetX + amount
                                        if (new <= 0f && new >= -dragThresholdPx * 2.2f) redOffsetX = new
                                    }
                                )
                            }
                        }
                        .clickable { onDeclineOrDelay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMaxDelay) Icons.Default.Lock else Icons.Default.CallEnd,
                        contentDescription = "Decline or Delay Task",
                        tint = if (isMaxDelay) Color(0xFF94A3B8) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isMaxDelay) "LOCKED • MAX DELAY" else "↑ SWIPE UP TO DECLINE",
                color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFFF87171),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isMaxDelay) "Limit Exceeded" else "DECLINE / DELAY (or ←)",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Center Status Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF334155))
            ) {
                Text(
                    text = if (isMaxDelay) "ENFORCING" else "TAP OR SWIPE",
                    color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // ---------------------------------------------------------------------
        // RIGHT: ANSWER / ACCEPT (GREEN)
        // ---------------------------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            // Animated upward chevrons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(0, (chevronBounce + greenOffsetY).roundToInt()) }
                    .padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF10B981).copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(buttonRippleScale)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = buttonRippleAlpha))
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(greenOffsetX.roundToInt(), greenOffsetY.roundToInt()) }
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                        )
                        .border(2.dp, Color(0xFF6EE7B7), CircleShape)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (greenOffsetY <= -dragThresholdPx) onAnswer()
                                    greenOffsetY = 0f
                                },
                                onDragCancel = { greenOffsetY = 0f },
                                onVerticalDrag = { _, amount ->
                                    val new = greenOffsetY + amount
                                    if (new <= 0f && new >= -dragThresholdPx * 2.2f) greenOffsetY = new
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (greenOffsetX >= dragThresholdPx) onAnswer()
                                    greenOffsetX = 0f
                                },
                                onDragCancel = { greenOffsetX = 0f },
                                onHorizontalDrag = { _, amount ->
                                    val new = greenOffsetX + amount
                                    if (new >= 0f && new <= dragThresholdPx * 2.2f) greenOffsetX = new
                                }
                            )
                        }
                        .clickable { onAnswer() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Swipe up to Answer",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "↑ SWIPE UP TO ANSWER",
                color = Color(0xFF6EE7B7),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "START TASK (or swipe →)",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================================
// ACTIVE IN-CALL VIEW (100% REALISTIC ACTIVE PHONE CALL WITH VOICE WAVEFORM & 6-BUTTON GRID)
// =============================================================================
@Composable
fun ActiveInCallView(
    studentName: String,
    taskSubject: String,
    taskTitle: String,
    durationMinutes: Int,
    xpReward: Int,
    callDurationSeconds: Int,
    currentSpeechSubtitle: String,
    isMaxDelay: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onKeypadClick: () -> Unit,
    onDossierClick: () -> Unit,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val formattedTime = remember(callDurationSeconds) {
        val mins = callDurationSeconds / 60
        val secs = callDurationSeconds % 60
        String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }

    // Dynamic wave animation for speech visualizer
    val infiniteTransition = rememberInfiniteTransition(label = "inCallWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top In-Call Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMaxDelay) "MAX DELAY ACTIVE • LIVE DIRECTIVE" else "CALL CONNECTED • HD AUDIO",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Future $studentName (2030)",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Real Call Duration Timer (00:03, 00:04...)
            Text(
                text = formattedTime,
                color = Color(0xFF38BDF8),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        // Center Voice Equalizer / Waveform Visualizer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Holographic Connected Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Active",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic 14-Bar Equalizer Waveform
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(44.dp)
            ) {
                val heights = listOf(14, 26, 38, 22, 42, 30, 20, 36, 44, 28, 18, 34, 24, 16)
                heights.forEachIndexed { i, baseH ->
                    val waveMultiplier = (kotlin.math.sin((wavePhase * 6.28f) + (i * 0.45f)) + 1f) / 2f
                    val barHeight = (baseH * (0.35f + waveMultiplier * 0.65f)).dp
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF00F0FF), Color(0xFF10B981))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Speech Subtitle Card (Speech to Text Transcription)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FUTURE SELF LIVE VOICE DIRECTIVE",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"$currentSpeechSubtitle\"",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // In-Call 6-Button Matrix (Authentic Real Phone Call Grid)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mute Button
                InCallActionButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) "Muted" else "Mute",
                    isActive = isMuted,
                    onClick = onToggleMute
                )

                // Keypad Button
                InCallActionButton(
                    icon = Icons.Default.Dialpad,
                    label = "Keypad",
                    isActive = false,
                    onClick = onKeypadClick
                )

                // Speaker Button
                InCallActionButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label = "Speaker",
                    isActive = isSpeakerOn,
                    onClick = onToggleSpeaker
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Add Call Button
                InCallActionButton(
                    icon = Icons.Default.PersonAdd,
                    label = "Add Call",
                    isActive = false,
                    onClick = {
                        Toast.makeText(context, "Conference restricted in temporal link", Toast.LENGTH_SHORT).show()
                    }
                )

                // Video Call Button
                InCallActionButton(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    isActive = false,
                    onClick = {
                        Toast.makeText(context, "Holographic transmission only", Toast.LENGTH_SHORT).show()
                    }
                )

                // Task Details / Protocol Dossier
                InCallActionButton(
                    icon = Icons.Default.Assignment,
                    label = "Dossier",
                    isActive = false,
                    onClick = onDossierClick
                )
            }
        }

        // Bottom Prominent RED END CALL Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                        )
                    )
                    .border(2.dp, Color(0xFFFCA5A5), CircleShape)
                    .clickable { onEndCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call & Start Focus",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "END CALL & ENTER FOCUS MODE",
                color = Color(0xFFFCA5A5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

// =============================================================================
// REUSABLE IN-CALL ACTION BUTTON (Mute, Speaker, Keypad, etc.)
// =============================================================================
@Composable
fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = if (isActive) Color.White else Color(0xFF1E293B),
            border = BorderStroke(1.dp, if (isActive) Color.White else Color(0xFF334155))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isActive) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
