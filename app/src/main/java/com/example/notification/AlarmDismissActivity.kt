package com.example.notification

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.RebuildApplication
import com.example.data.local.entity.AlarmChallengeType
import com.example.data.local.entity.AlarmDifficulty
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.AlarmLogEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.REBUILDTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

open class AlarmDismissActivity : ComponentActivity(), SensorEventListener {

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_CHALLENGE_TYPE = "extra_challenge_type"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_MAX_SNOOZES = "extra_max_snoozes"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"
        const val EXTRA_CURRENT_SNOOZES = "extra_current_snoozes"
        const val EXTRA_RINGTONE_PRESET = "extra_ringtone_preset"
        const val EXTRA_SNOOZE_RINGTONE_PRESET = "extra_snooze_ringtone_preset"
        const val EXTRA_IS_SNOOZE_TRIGGER = "extra_is_snooze_trigger"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    // Shake tracking
    private var lastShakeTime = 0L
    private var shakeCountState = mutableIntStateOf(0)
    private var targetShakes = 20

    private var alarmId: Long = 0
    private var alarmTitle: String = "Apex Protocol Alarm"
    private var challengeType = AlarmChallengeType.MATH
    private var difficulty = AlarmDifficulty.MEDIUM
    private var volumePercent = 90
    private var isVibrate = true
    private var maxSnoozes = 3
    private var snoozeDurationMinutes = 5
    private var currentSnoozesUsed = 0
    private var ringtonePreset = "CYBER_SIREN"
    private var snoozeRingtonePreset = "TICK_TOCK"
    private var isSnoozeTrigger = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Configure Lockscreen & Turn Screen On Flags (Android 8.0+ / 8.1+)
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

        // 2. Dismiss Keyguard when possible
        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager?.requestDismissKeyguard(this, null)
            }
        } catch (e: Exception) {
            Log.e("AlarmDismissActivity", "Could not request keyguard dismissal", e)
        }

        // Parse extras
        alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L)
        alarmTitle = intent.getStringExtra(EXTRA_ALARM_TITLE) ?: "Apex Protocol Wake-Up"
        val challengeStr = intent.getStringExtra(EXTRA_CHALLENGE_TYPE) ?: "MATH"
        challengeType = try { AlarmChallengeType.valueOf(challengeStr) } catch (e: Exception) { AlarmChallengeType.MATH }
        val diffStr = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"
        difficulty = try { AlarmDifficulty.valueOf(diffStr) } catch (e: Exception) { AlarmDifficulty.MEDIUM }
        volumePercent = intent.getIntExtra(EXTRA_VOLUME, 90)
        isVibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)
        maxSnoozes = intent.getIntExtra(EXTRA_MAX_SNOOZES, 3)
        snoozeDurationMinutes = intent.getIntExtra(EXTRA_SNOOZE_DURATION, 5)
        currentSnoozesUsed = intent.getIntExtra(EXTRA_CURRENT_SNOOZES, 0)
        ringtonePreset = intent.getStringExtra(EXTRA_RINGTONE_PRESET) ?: "CYBER_SIREN"
        snoozeRingtonePreset = intent.getStringExtra(EXTRA_SNOOZE_RINGTONE_PRESET) ?: "TICK_TOCK"
        isSnoozeTrigger = intent.getBooleanExtra(EXTRA_IS_SNOOZE_TRIGGER, false)

        // Initialize Sensors for shake / walk challenge
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Ensure background service is running alarm playback
        AlarmForegroundService.startAlarm(
            context = this,
            alarmId = alarmId,
            title = alarmTitle,
            challengeType = challengeType.name,
            difficulty = difficulty.name,
            volume = volumePercent,
            isVibrate = isVibrate,
            maxSnoozes = maxSnoozes,
            snoozeDuration = snoozeDurationMinutes,
            currentSnoozeCount = currentSnoozesUsed,
            ringtonePreset = ringtonePreset,
            snoozeRingtonePreset = snoozeRingtonePreset,
            isSnoozeTrigger = isSnoozeTrigger
        )

        setContent {
            REBUILDTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkNavy
                ) {
                    AlarmDismissScreen(
                        alarmTitle = alarmTitle,
                        challengeType = challengeType,
                        difficulty = difficulty,
                        maxSnoozes = maxSnoozes,
                        initialSnoozesUsed = currentSnoozesUsed,
                        snoozeDurationMinutes = snoozeDurationMinutes,
                        shakeCount = shakeCountState.intValue,
                        targetShakes = targetShakes,
                        onSnoozeClicked = { updatedSnoozeCount ->
                            handleSmartSnooze(updatedSnoozeCount)
                        },
                        onChallengeSolved = { snoozesUsed ->
                            handleDismissSuccess(snoozesUsed)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (challengeType == AlarmChallengeType.PHYSICAL_SHAKE || challengeType == AlarmChallengeType.PHYSICAL_STEPS) {
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    private fun handleSmartSnooze(snoozesCount: Int) {
        // Schedule next exact snooze alarm
        AlarmScheduler.scheduleSnooze(
            context = applicationContext,
            alarmId = alarmId,
            title = alarmTitle,
            challengeType = challengeType.name,
            difficulty = difficulty.name,
            volume = volumePercent,
            isVibrate = isVibrate,
            maxSnoozes = maxSnoozes,
            snoozeMinutes = snoozeDurationMinutes,
            snoozesUsedSoFar = snoozesCount,
            ringtonePreset = ringtonePreset,
            snoozeRingtonePreset = snoozeRingtonePreset
        )

        // Stop current ringing audio & service
        AlarmForegroundService.stopAlarm(this)
        finish()
    }

    private fun handleDismissSuccess(snoozesUsed: Int) {
        // Stop background alarm service
        AlarmForegroundService.stopAlarm(this)

        // Log to Room Database
        val app = application as? RebuildApplication
        val repo = app?.repository
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()

        CoroutineScope(Dispatchers.IO).launch {
            repo?.logAlarmDismissal(
                AlarmLogEntity(
                    alarmId = alarmId,
                    alarmTitle = alarmTitle,
                    targetTime = timeFormat.format(now),
                    triggeredTime = timeFormat.format(now),
                    dismissedTime = timeFormat.format(now),
                    snoozesUsed = snoozesUsed,
                    challengeType = challengeType,
                    solvedSuccessfully = true,
                    isMissed = false,
                    date = dateFormat.format(now)
                )
            )
        }

        finish()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > 2.2f) {
                val now = System.currentTimeMillis()
                if (lastShakeTime + 300 > now) {
                    return
                }
                lastShakeTime = now
                shakeCountState.intValue += 1
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun AlarmDismissScreen(
    alarmTitle: String,
    challengeType: AlarmChallengeType,
    difficulty: AlarmDifficulty,
    maxSnoozes: Int,
    initialSnoozesUsed: Int = 0,
    snoozeDurationMinutes: Int = 5,
    shakeCount: Int,
    targetShakes: Int,
    onSnoozeClicked: (Int) -> Unit,
    onChallengeSolved: (Int) -> Unit
) {
    var snoozesUsed by remember { mutableIntStateOf(initialSnoozesUsed) }
    var isChallengeSolved by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Live clock updating every second
    var currentDateTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = Date()
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }
    val secondsFormat = remember { SimpleDateFormat(":ss", Locale.getDefault()) }
    val amPmFormat = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkNavy,
                        Color(0xFF0F172A),
                        Color(0xFF090D16)
                    )
                )
            )
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Time & Urgent Pulse Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FireOrange.copy(alpha = 0.2f))
                        .border(1.dp, FireOrange, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = FireOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "WAKE UP PROTOCOL ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FireOrange,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = dateFormat.format(currentDateTime).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FrostBlueAccent,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.scale(pulseScale)
                ) {
                    Text(
                        text = timeFormat.format(currentDateTime),
                        fontSize = 62.sp,
                        fontWeight = FontWeight.Black,
                        color = IceCyanPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = secondsFormat.format(currentDateTime),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amPmFormat.format(currentDateTime),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = alarmTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Solve awakening challenge to silence alarm.",
                    fontSize = 12.sp,
                    color = GlassWhiteMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Challenge Interactive Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
                border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (challengeType) {
                        AlarmChallengeType.MATH -> {
                            MathChallengeSolver(
                                difficulty = difficulty,
                                onSolved = {
                                    isChallengeSolved = true
                                    onChallengeSolved(snoozesUsed)
                                }
                            )
                        }
                        AlarmChallengeType.CAPTCHA -> {
                            CaptchaChallengeSolver(
                                onSolved = {
                                    isChallengeSolved = true
                                    onChallengeSolved(snoozesUsed)
                                }
                            )
                        }
                        AlarmChallengeType.PHYSICAL_SHAKE, AlarmChallengeType.PHYSICAL_STEPS -> {
                            PhysicalShakeChallengeSolver(
                                currentShakes = shakeCount,
                                targetShakes = targetShakes,
                                onSolved = {
                                    isChallengeSolved = true
                                    onChallengeSolved(snoozesUsed)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Actions: Snooze Controls & Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val snoozesRemaining = maxOf(0, maxSnoozes - snoozesUsed)

                if (snoozesRemaining > 0) {
                    OutlinedButton(
                        onClick = {
                            val newSnoozeCount = snoozesUsed + 1
                            snoozesUsed = newSnoozeCount
                            onSnoozeClicked(newSnoozeCount)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WarningAmber
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze (${snoozeDurationMinutes}m • $snoozesRemaining left)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Maximum snoozes reached ($maxSnoozes/$maxSnoozes). You must complete the challenge now.",
                        fontSize = 12.sp,
                        color = FireOrange,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apex Winter Arc Protocol • Lock Screen Armed",
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MathChallengeSolver(
    difficulty: AlarmDifficulty,
    onSolved: () -> Unit
) {
    var num1 by remember { mutableIntStateOf(0) }
    var num2 by remember { mutableIntStateOf(0) }
    var operation by remember { mutableStateOf("+") }
    var expectedAnswer by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun generateEquation() {
        userAnswer = ""
        isError = false
        when (difficulty) {
            AlarmDifficulty.EASY -> {
                num1 = Random.nextInt(12, 50)
                num2 = Random.nextInt(8, 30)
                operation = if (Random.nextBoolean()) "+" else "-"
                expectedAnswer = if (operation == "+") num1 + num2 else num1 - num2
            }
            AlarmDifficulty.MEDIUM -> {
                val ops = listOf("+", "-", "×")
                operation = ops.random()
                when (operation) {
                    "+" -> {
                        num1 = Random.nextInt(45, 99)
                        num2 = Random.nextInt(37, 88)
                        expectedAnswer = num1 + num2
                    }
                    "-" -> {
                        num1 = Random.nextInt(80, 150)
                        num2 = Random.nextInt(25, 75)
                        expectedAnswer = num1 - num2
                    }
                    "×" -> {
                        num1 = Random.nextInt(12, 38)
                        num2 = Random.nextInt(4, 9)
                        expectedAnswer = num1 * num2
                    }
                }
            }
            AlarmDifficulty.HARD -> {
                num1 = Random.nextInt(24, 68)
                num2 = Random.nextInt(6, 14)
                operation = "×"
                expectedAnswer = num1 * num2
            }
        }
    }

    LaunchedEffect(Unit) {
        generateEquation()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = IceCyanPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Math Challenge (${difficulty.label})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IceCyanPrimary
            )
        }

        Text(
            text = "Solve the equation to stop alarm:",
            fontSize = 13.sp,
            color = GlassWhiteMuted
        )

        // Math Equation Box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF060B14))
                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = "$num1 $operation $num2 = ?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = GlassWhite,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        OutlinedTextField(
            value = userAnswer,
            onValueChange = {
                userAnswer = it.filter { ch -> ch.isDigit() || ch == '-' }
                isError = false
            },
            placeholder = { Text("Enter Answer", color = GlassWhiteMuted) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    val parsed = userAnswer.toIntOrNull()
                    if (parsed == expectedAnswer) {
                        onSolved()
                    } else {
                        isError = true
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GlassWhite,
                unfocusedTextColor = GlassWhite,
                focusedBorderColor = IceCyanPrimary,
                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                errorBorderColor = FireOrange,
                focusedContainerColor = DarkNavy,
                unfocusedContainerColor = DarkNavy
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
        )

        if (isError) {
            Text(
                text = "Incorrect result! Recalculate carefully.",
                fontSize = 12.sp,
                color = FireOrange,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { generateEquation() },
                modifier = Modifier
                    .weight(0.4f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Equation",
                    tint = FrostBlueAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = {
                    val parsed = userAnswer.toIntOrNull()
                    if (parsed == expectedAnswer) {
                        onSolved()
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkNavy
                )
            ) {
                Text(
                    text = "Verify & Stop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CaptchaChallengeSolver(
    onSolved: () -> Unit
) {
    var captchaCode by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun generateCaptcha() {
        userInput = ""
        isError = false
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefhkmnprstuvwxyz"
        captchaCode = (1..6).map { chars.random() }.joinToString("")
    }

    LaunchedEffect(Unit) {
        generateCaptcha()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = IceCyanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Captcha Verification",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IceCyanPrimary
            )
        }

        Text(
            text = "Type the dynamic code exactly as shown:",
            fontSize = 13.sp,
            color = GlassWhiteMuted
        )

        // Captcha Visual Box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF050D18))
                .border(1.dp, FrostBlueAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text(
                text = captchaCode,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = ElectricBlue,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        OutlinedTextField(
            value = userInput,
            onValueChange = {
                userInput = it.trim()
                isError = false
            },
            placeholder = { Text("Enter Captcha Code", color = GlassWhiteMuted) },
            singleLine = true,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GlassWhite,
                unfocusedTextColor = GlassWhite,
                focusedBorderColor = IceCyanPrimary,
                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                errorBorderColor = FireOrange,
                focusedContainerColor = DarkNavy,
                unfocusedContainerColor = DarkNavy
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
        )

        if (isError) {
            Text(
                text = "Captcha mismatch! Case sensitive.",
                fontSize = 12.sp,
                color = FireOrange,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { generateCaptcha() },
                modifier = Modifier
                    .weight(0.4f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate Captcha",
                    tint = FrostBlueAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = {
                    if (userInput.equals(captchaCode, ignoreCase = false)) {
                        onSolved()
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkNavy
                )
            ) {
                Text(
                    text = "Verify & Stop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PhysicalShakeChallengeSolver(
    currentShakes: Int,
    targetShakes: Int,
    onSolved: () -> Unit
) {
    val progress = (currentShakes.toFloat() / targetShakes.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(currentShakes) {
        if (currentShakes >= targetShakes) {
            onSolved()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Vibration,
                contentDescription = null,
                tint = FireOrange,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Physical Shake Protocol",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FireOrange
            )
        }

        Text(
            text = "Shake device vigorously to activate nervous system:",
            fontSize = 13.sp,
            color = GlassWhiteMuted,
            textAlign = TextAlign.Center
        )

        // Circular Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(140.dp),
                color = FrostBlueAccent.copy(alpha = 0.2f),
                strokeWidth = 10.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(140.dp),
                color = if (progress >= 1f) SuccessGreen else ElectricBlue,
                strokeWidth = 10.dp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currentShakes",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = GlassWhite
                )
                Text(
                    text = "of $targetShakes Shakes",
                    fontSize = 11.sp,
                    color = FrostBlueAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SuccessGreen,
            trackColor = DarkNavy
        )

        if (currentShakes >= targetShakes) {
            Text(
                text = "Target Achieved! Dismissing alarm...",
                fontSize = 14.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
