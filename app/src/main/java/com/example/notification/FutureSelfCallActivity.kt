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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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

        val settings = settingsRepo.getSettingsSync()

        // Ring and vibrate immediately
        speechManager.startRingtoneAndVibrate(
            presetKey = settings.ringtonePreset,
            volume = settings.volume,
            enableVibration = settings.vibrationEnabled
        )

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

                    if (repo != null && taskId > 0L) {
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            val task = repo.getTaskById(taskId)
                            if (task != null) {
                                repo.delayTask(task, delayMinutes)
                            }
                        }
                    }

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
    onStartTaskConfirmed: () -> Unit,
    onDelayTaskConfirmed: (delayMinutes: Int) -> Unit
) {
    var screenState by remember { mutableStateOf(CallScreenState.RINGING) }
    var currentSpeechSubtitle by remember { mutableStateOf("") }
    var showDelaySheet by remember { mutableStateOf(false) }

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
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
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
                            text = "TEMPORAL TRANSMISSION LIVE",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (screenState == CallScreenState.SPEAKING) "Future Self Speaking..." else "Future You is Calling...",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
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
                                listOf(
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
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (screenState == CallScreenState.SPEAKING) Icons.Default.GraphicEq else Icons.Default.Psychology,
                        contentDescription = "Future Self Hologram",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Task Dossier Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.88f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = taskSubject.uppercase(),
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = taskTitle,
                        color = Color.White,
                        fontSize = 21.sp,
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

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Message From Future $studentName:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentSpeechSubtitle.isNotBlank()) "\"$currentSpeechSubtitle\"" else "\"$motivationalQuote\"",
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                }
            }

            // Bottom Actions: Bidirectional Slider or Speaking Status
            if (screenState == CallScreenState.SPEAKING) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
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
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Synthesizing Directives...",
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                FutureSelfSwipeControl(
                    onSwipeRight = {
                        screenState = CallScreenState.SPEAKING
                        currentSpeechSubtitle = "Message from your future self. $taskSubject $taskTitle session has started. Focus now."
                        onStartTaskConfirmed()
                    },
                    onSwipeLeft = {
                        showDelaySheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
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
                Text(
                    text = "Your future self will call back at the updated time. Delaying tasks introduces future pressure.",
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
                    OutlinedButton(
                        onClick = {
                            showDelaySheet = false
                            screenState = CallScreenState.SPEAKING
                            currentSpeechSubtitle = "Task delayed by $minutes minutes. Do not let delay become avoidance."
                            onDelayTaskConfirmed(minutes)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = "+$minutes min", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FutureSelfSwipeControl(
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipeDistancePx = with(LocalDensity.current) { 130.dp.toPx() }

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(36.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DELAY",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "START",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF10B981).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when {
                            offsetX > 40f -> listOf(Color(0xFF10B981), Color(0xFF059669))
                            offsetX < -40f -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            else -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        }
                    )
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= maxSwipeDistancePx * 0.70f) {
                                onSwipeRight()
                            } else if (offsetX <= -maxSwipeDistancePx * 0.70f) {
                                onSwipeLeft()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            if (newOffset in -maxSwipeDistancePx..maxSwipeDistancePx) {
                                offsetX = newOffset
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Swipe Handle",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
