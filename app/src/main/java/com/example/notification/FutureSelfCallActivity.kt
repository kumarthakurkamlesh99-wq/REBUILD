package com.example.notification

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.RebuildApplication
import com.example.data.model.futureself.VoiceLanguage
import com.example.data.model.futureself.VoiceTone
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.speech.FutureSelfMessageEngine
import com.example.speech.FutureSelfSpeechManager
import com.example.ui.navigation.Screen
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.roundToInt

class FutureSelfCallActivity : ComponentActivity() {

    companion object {
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
        // Enforce lockscreen wakeup & screen turn-on
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

        val settings = settingsRepo.getSettingsSync()
        val isMaxDelay = isMaxDelayFromExtra || (delayMinutesSoFar >= settings.maxAllowedDelayMinutes)

        fun executeMaxDelayAutoSpeech() {
            speechManager.stopRingtoneAndVibrate()
            val maxDelayText = FutureSelfMessageEngine.buildMaxDelaySpeechText(
                subject = taskSubject,
                title = taskTitle,
                durationMinutes = durationMinutes,
                language = settings.language
            )

            speechManager.playCustomAudioOrSpeech(
                customAudioPath = settings.customMaxDelayAudioUri,
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
        }

        if (isMaxDelay) {
            // Maximum delay hit! Do not wait for swipe: start audio directives immediately
            executeMaxDelayAutoSpeech()
        } else {
            // Normal call: Ring and vibrate immediately
            speechManager.startRingtoneAndVibrate(
                presetKey = settings.ringtonePreset,
                volume = settings.volume,
                enableVibration = settings.vibrationEnabled
            )
        }

        setContent {
            val motivationalQuote = remember {
                FutureSelfMessageEngine.getCategoryQuote(taskSubject, taskTitle, customQuote)
            }

            FutureSelfCallContainer(
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
                onStartTaskConfirmed = {
                    speechManager.stopRingtoneAndVibrate()
                    val ignitionText = FutureSelfMessageEngine.buildIgnitionSpeechText(
                        subject = taskSubject,
                        title = taskTitle,
                        durationMinutes = durationMinutes,
                        language = settings.language
                    )

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
                },
                onMaxDelayAutoIgnite = {
                    executeMaxDelayAutoSpeech()
                },
                onDelayTaskConfirmed = { delayMinutes ->
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
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            val task = repo.getTaskById(taskId)
                            if (task != null) {
                                repo.delayTask(task, delayMinutes)
                            }
                        }
                    }

                    if (updatedTotalDelay >= settings.maxAllowedDelayMinutes) {
                        // Delay reached maximum allowed threshold!
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
    }
}

enum class CallScreenState {
    RINGING,
    SPEAKING,
    SHOW_DELAY_OPTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureSelfCallContainer(
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
    onStartTaskConfirmed: () -> Unit,
    onMaxDelayAutoIgnite: () -> Unit = {},
    onDelayTaskConfirmed: (delayMinutes: Int) -> Unit
) {
    var screenState by remember {
        mutableStateOf(if (isMaxDelay) CallScreenState.SPEAKING else CallScreenState.RINGING)
    }
    var currentSpeechSubtitle by remember {
        mutableStateOf(
            if (isMaxDelay)
                "Maximum delay threshold reached. No more excuses. Message from your future self. $taskSubject $taskTitle session has started now. Focus now."
            else ""
        )
    }
    var showDelaySheet by remember { mutableStateOf(false) }

    // If max delay reached, immediately lock into speaking state
    LaunchedEffect(isMaxDelay) {
        if (isMaxDelay) {
            screenState = CallScreenState.SPEAKING
            currentSpeechSubtitle = "Maximum delay threshold reached. No more excuses. Directive initiated. Focus now."
        }
    }

    // Auto-timeout if call rings with no response for callTimeoutSeconds
    LaunchedEffect(Unit) {
        if (!isMaxDelay) {
            kotlinx.coroutines.delay(callTimeoutSeconds * 1000L)
            if (screenState == CallScreenState.RINGING) {
                screenState = CallScreenState.SPEAKING
                currentSpeechSubtitle = "Temporal call auto-connected. Directive initiated."
                onStartTaskConfirmed()
            }
        }
    }

    // Pulsing animations for holographic avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Protocol status badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Surface(
                    color = if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF10B981).copy(alpha = 0.5f))
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
                            text = if (isMaxDelay) "MAXIMUM DELAY HIT • AUDIO AUTO-PLAYING" else "TEMPORAL TRANSMISSION LIVE",
                            color = if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        isMaxDelay -> "Future Self Taking Control..."
                        screenState == CallScreenState.SPEAKING -> "Future Self Speaking..."
                        else -> "Future You is Calling..."
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            }

            // Central Visualizer: Holographic Transmission Ring & Wave
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
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
                                    Color(0xFF00F0FF).copy(alpha = ringAlpha),
                                    Color(0xFF8B5CF6).copy(alpha = ringAlpha),
                                    Color(0xFF00F0FF).copy(alpha = ringAlpha)
                                )
                            ),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            1.dp,
                            if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.7f) else Color(0xFF38BDF8).copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (screenState == CallScreenState.SPEAKING) Icons.Default.GraphicEq else Icons.Default.Psychology,
                        contentDescription = "Future Self Hologram",
                        tint = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF38BDF8),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Task Dossier Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.88f)),
                border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.6f) else Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = taskSubject.uppercase(),
                        color = if (isMaxDelay) Color(0xFFF87171) else Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = taskTitle,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$startTime - $endTime",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+$xpReward XP",
                                    color = Color(0xFFFCD34D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isMaxDelay) "Directive From Future $studentName (MAX DELAY HIT):" else "Message From Future $studentName:",
                        color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentSpeechSubtitle.isNotBlank()) "\"$currentSpeechSubtitle\"" else "\"$motivationalQuote\"",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Bottom Actions: Bidirectional Slider or Speaking Status
            if (screenState == CallScreenState.SPEAKING) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isMaxDelay) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isMaxDelay) "Max Delay Hit • Audio Directive Playing..." else "Synthesizing Directives...",
                            color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                RealWorldCallActionPad(
                    isMaxDelay = isMaxDelay,
                    delayMinutesSoFar = delayMinutesSoFar,
                    maxAllowedDelayMinutes = maxAllowedDelayMinutes,
                    onStartTask = {
                        screenState = CallScreenState.SPEAKING
                        currentSpeechSubtitle = "Message from your future self. $taskSubject $taskTitle session has started. Focus now."
                        onStartTaskConfirmed()
                    },
                    onDelayTask = {
                        if (!isMaxDelay) {
                            showDelaySheet = true
                        } else {
                            onMaxDelayAutoIgnite()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }

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

                Spacer(modifier = Modifier.height(20.dp))

                val options = listOf(
                    Pair(15, "15 Minutes"),
                    Pair(30, "30 Minutes"),
                    Pair(60, "1 Hour"),
                    Pair(180, "Later Today (+3h)"),
                    Pair(1440, "Tomorrow Morning")
                )

                options.forEach { (minutes, title) ->
                    val willExceedMax = (delayMinutesSoFar + minutes) > maxAllowedDelayMinutes
                    OutlinedButton(
                        onClick = {
                            showDelaySheet = false
                            if (willExceedMax) {
                                // Exceeds max delay: trigger auto-ignition
                                onMaxDelayAutoIgnite()
                            } else {
                                screenState = CallScreenState.SPEAKING
                                currentSpeechSubtitle = "Task delayed by $minutes minutes. Do not let delay become avoidance."
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
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "+$minutes min",
                                color = if (willExceedMax) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RealWorldCallActionPad(
    isMaxDelay: Boolean = false,
    delayMinutesSoFar: Int = 0,
    maxAllowedDelayMinutes: Int = 60,
    onStartTask: () -> Unit,
    onDelayTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "callPadArrows")
    val arrowBounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowBounceY"
    )
    val arrowBounceX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowBounceX"
    )
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    var greenOffsetY by remember { mutableFloatStateOf(0f) }
    var greenOffsetX by remember { mutableFloatStateOf(0f) }
    var redOffsetY by remember { mutableFloatStateOf(0f) }
    var redOffsetX by remember { mutableFloatStateOf(0f) }

    val dragThresholdPx = with(LocalDensity.current) { 42.dp.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // -------------------------------------------------------------
        // LEFT: RED / DECLINE (SWIPE UP TO DELAY)
        // -------------------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            if (isMaxDelay) {
                // Delay Locked Indicator
                Surface(
                    color = Color(0xFF7F1D1D).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
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
                // Animated upward chevrons showing swipe direction
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset { IntOffset(0, (arrowBounceY + redOffsetY).roundToInt()) }
                        .padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444).copy(alpha = 0.45f),
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

            // Draggable Red Call Button
            Box(contentAlignment = Alignment.Center) {
                if (!isMaxDelay) {
                    // Expanding ripple
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(rippleScale)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = rippleAlpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(redOffsetX.roundToInt(), redOffsetY.roundToInt()) }
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMaxDelay) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF450A0A), Color(0xFF1E293B))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                )
                            }
                        )
                        .border(
                            2.dp,
                            if (isMaxDelay) Color(0xFF7F1D1D) else Color(0xFFFCA5A5),
                            CircleShape
                        )
                        .pointerInput(isMaxDelay) {
                            if (!isMaxDelay) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (redOffsetY <= -dragThresholdPx) {
                                            onDelayTask()
                                        }
                                        redOffsetY = 0f
                                    },
                                    onDragCancel = { redOffsetY = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        val newOffset = redOffsetY + dragAmount
                                        if (newOffset <= 0f && newOffset >= -dragThresholdPx * 2.2f) {
                                            redOffsetY = newOffset
                                        }
                                    }
                                )
                            }
                        }
                        .pointerInput(isMaxDelay) {
                            if (!isMaxDelay) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (redOffsetX <= -dragThresholdPx) {
                                            onDelayTask()
                                        }
                                        redOffsetX = 0f
                                    },
                                    onDragCancel = { redOffsetX = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        val newOffset = redOffsetX + dragAmount
                                        if (newOffset <= 0f && newOffset >= -dragThresholdPx * 2.2f) {
                                            redOffsetX = newOffset
                                        }
                                    }
                                )
                            }
                        }
                        .clickable { onDelayTask() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMaxDelay) Icons.Default.Lock else Icons.Default.CallEnd,
                        contentDescription = if (isMaxDelay) "Delay Locked" else "Swipe up to Delay Task",
                        tint = if (isMaxDelay) Color(0xFF94A3B8) else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text indicating Direction + Purpose
            Text(
                text = if (isMaxDelay) "LOCKED • MAX DELAY" else "↑ SWIPE UP TO DECLINE",
                color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFFF87171),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isMaxDelay) "Deferral Limit Hit" else "DELAY TASK (or swipe ←)",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Center separator / temporal call label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isMaxDelay) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF334155))
            ) {
                Text(
                    text = if (isMaxDelay) "AUTOPLAY ACTIVE" else "DISCIPLINE OS",
                    color = if (isMaxDelay) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // RIGHT: GREEN / ANSWER (SWIPE UP TO START)
        // -------------------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            // Animated upward chevrons showing swipe direction
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(0, (arrowBounceY + greenOffsetY).roundToInt()) }
                    .padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF10B981).copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Draggable Green Call Button
            Box(contentAlignment = Alignment.Center) {
                // Expanding ripple
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(rippleScale)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = rippleAlpha))
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(greenOffsetX.roundToInt(), greenOffsetY.roundToInt()) }
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF10B981), Color(0xFF047857))
                            )
                        )
                        .border(2.dp, Color(0xFF6EE7B7), CircleShape)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (greenOffsetY <= -dragThresholdPx) {
                                        onStartTask()
                                    }
                                    greenOffsetY = 0f
                                },
                                onDragCancel = { greenOffsetY = 0f },
                                onVerticalDrag = { _, dragAmount ->
                                    val newOffset = greenOffsetY + dragAmount
                                    if (newOffset <= 0f && newOffset >= -dragThresholdPx * 2.2f) {
                                        greenOffsetY = newOffset
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (greenOffsetX >= dragThresholdPx) {
                                        onStartTask()
                                    }
                                    greenOffsetX = 0f
                                },
                                onDragCancel = { greenOffsetX = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    val newOffset = greenOffsetX + dragAmount
                                    if (newOffset >= 0f && newOffset <= dragThresholdPx * 2.2f) {
                                        greenOffsetX = newOffset
                                    }
                                }
                            )
                        }
                        .clickable { onStartTask() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Swipe up to Start Task",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text indicating Direction + Purpose
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
