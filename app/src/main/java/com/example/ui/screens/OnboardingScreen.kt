package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.OnboardingUiState
import com.example.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF040817),
                        DarkNavy,
                        Color(0xFF02040A)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header with steps indicator
            OnboardingHeader(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps,
                onStepClick = { step -> viewModel.setStep(step) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "OnboardingStepTransition"
                ) { step ->
                    when (step) {
                        1 -> StepPersonalAndTarget(state = state, viewModel = viewModel)
                        2 -> StepSchoolSchedule(state = state, viewModel = viewModel)
                        3 -> StepDailyClock(state = state, viewModel = viewModel)
                        4 -> StepFitnessAndAi(state = state, viewModel = viewModel)
                        5 -> StepNotificationsAndReview(
                            state = state,
                            viewModel = viewModel,
                            hasPermission = hasNotificationPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation Actions
            OnboardingBottomBar(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps,
                isSaving = state.isSaving,
                onBack = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onComplete = {
                    viewModel.completeOnboarding(onSuccess = onComplete)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = FrostedNavyCard,
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.rebuild_logo),
                        contentDescription = "REBUILD Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "REBUILD INITIALIZATION",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Step $currentStep of $totalSteps • Personal Profile Calibration",
                    style = MaterialTheme.typography.bodySmall,
                    color = IceCyanPrimary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-step progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..totalSteps) {
                val isDone = step < currentStep
                val isCurrent = step == currentStep
                val barColor = when {
                    isCurrent -> IceCyanPrimary
                    isDone -> SuccessGreen
                    else -> Color(0x33284B75)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(barColor)
                        .clickable { if (step < currentStep) onStepClick(step) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// STEP 1: PERSONAL & ACADEMIC TARGET
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepPersonalAndTarget(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            StepHeaderCard(
                title = "Who are you?",
                subtitle = "Set up your academic profile and exam target to calibrate syllabus & schedules.",
                icon = Icons.Default.Person,
                accentColor = IceCyanPrimary
            )
        }

        // Student Name
        item {
            SectionContainer(title = "YOUR NAME") {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    placeholder = { Text("e.g. Kamlesh Kumar", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_input_name")
                )
            }
        }

        // Class Selection
        item {
            SectionContainer(title = "CLASS / LEVEL") {
                val classes = listOf("Class 10", "Class 11", "Class 12", "Dropper", "College")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    classes.forEach { cls ->
                        ChoiceChip(
                            text = cls,
                            isSelected = state.studentClass == cls,
                            onClick = { viewModel.updateStudentClass(cls) }
                        )
                    }
                }
            }
        }

        // Board Selection
        item {
            SectionContainer(title = "EXAM BOARD") {
                val boards = listOf("CBSE", "ICSE / ISC", "State Board", "JEE / NEET Target", "Other")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    boards.forEach { board ->
                        ChoiceChip(
                            text = board,
                            isSelected = state.board == board,
                            onClick = { viewModel.updateBoard(board) }
                        )
                    }
                }
            }
        }

        // Stream Selection
        item {
            SectionContainer(title = "ACADEMIC STREAM") {
                val streams = listOf(
                    "Science (PCM)",
                    "Science (PCB)",
                    "Science (PCMB)",
                    "Commerce",
                    "Arts / Humanities",
                    "General"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    streams.forEach { stream ->
                        ChoiceChip(
                            text = stream,
                            isSelected = state.stream == stream,
                            onClick = { viewModel.updateStream(stream) }
                        )
                    }
                }
            }
        }

        // Target Percentage Slider
        item {
            SectionContainer(title = "TARGET PERCENTAGE: ${state.targetPercentage}%") {
                Slider(
                    value = state.targetPercentage.toFloat(),
                    onValueChange = { viewModel.updateTargetPercentage(it.toInt()) },
                    valueRange = 75f..100f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = IceCyanPrimary,
                        activeTrackColor = IceCyanPrimary,
                        inactiveTrackColor = Color(0x33284B75)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("75%", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                    Text("90% (Distinction)", style = MaterialTheme.typography.bodySmall, color = FrostBlueAccent)
                    Text("95%+ (Apex Rank)", style = MaterialTheme.typography.bodySmall, color = IceCyanPrimary, fontWeight = FontWeight.Bold)
                    Text("100%", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                }
            }
        }

        // Target Exam Name & Date
        item {
            SectionContainer(title = "TARGET EXAM & DATE") {
                OutlinedTextField(
                    value = state.targetExamName,
                    onValueChange = { viewModel.updateTargetExamName(it) },
                    label = { Text("Exam Name", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.targetExamDate,
                    onValueChange = { viewModel.updateTargetExamDate(it) },
                    label = { Text("Target Exam Date (YYYY-MM-DD)", color = GlassWhiteMuted) },
                    placeholder = { Text("2027-02-15", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ----------------------------------------------------
// STEP 2: SCHOOL / COLLEGE ROUTINE
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepSchoolSchedule(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            StepHeaderCard(
                title = "School & Daily Routine",
                subtitle = "REBUILD adjusts deep work study windows around your real school attendance and commute.",
                icon = Icons.Default.School,
                accentColor = LuxuryAccent
            )
        }

        // Has School Toggle
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LuxuryCard,
                border = BorderStroke(1.dp, Color(0x337C8CFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Regular School / College",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = if (state.hasSchool) "App creates dispatch & arrival schedule" else "Full-day self study schedule generated",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                    }
                    Switch(
                        checked = state.hasSchool,
                        onCheckedChange = { viewModel.updateHasSchool(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IceCyanPrimary,
                            checkedTrackColor = Color(0x6638E1FF)
                        )
                    )
                }
            }
        }

        if (state.hasSchool) {
            // School Timings
            item {
                SectionContainer(title = "SCHOOL HOURS") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.schoolStartTime,
                            onValueChange = { viewModel.updateSchoolStartTime(it) },
                            label = { Text("Departure (e.g. 09:45)", color = GlassWhiteMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = FrostedNavyCard,
                                unfocusedContainerColor = FrostedNavyCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.schoolEndTime,
                            onValueChange = { viewModel.updateSchoolEndTime(it) },
                            label = { Text("Arrival Home (13:00)", color = GlassWhiteMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = FrostedNavyCard,
                                unfocusedContainerColor = FrostedNavyCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Travel Duration
            item {
                SectionContainer(title = "ONE-WAY COMMUTE: ${state.travelTimeMinutes} MIN") {
                    val commuteTimes = listOf(15, 25, 35, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commuteTimes.forEach { mins ->
                            ChoiceChip(
                                text = "$mins min",
                                isSelected = state.travelTimeMinutes == mins,
                                onClick = { viewModel.updateTravelTime(mins) }
                            )
                        }
                    }
                }
            }

            // Weekly Off Days
            item {
                SectionContainer(title = "WEEKLY OFF DAYS") {
                    val offDays = listOf("Sunday", "Saturday & Sunday", "None (7 Days)")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        offDays.forEach { opt ->
                            ChoiceChip(
                                text = opt,
                                isSelected = state.weeklyOffDays == opt,
                                onClick = { viewModel.updateWeeklyOffDays(opt) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// STEP 3: DAILY CLOCK & TARGETS
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepDailyClock(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            StepHeaderCard(
                title = "Circadian Clock & Study Target",
                subtitle = "Set your biological wake/sleep times and daily focus volume.",
                icon = Icons.Default.Timer,
                accentColor = WarningAmber
            )
        }

        // Wake Up Time
        item {
            SectionContainer(title = "WAKE UP TIME") {
                val wakePresets = listOf("05:00", "05:30", "06:00", "06:30", "07:00")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wakePresets.forEach { time ->
                        ChoiceChip(
                            text = time,
                            isSelected = state.wakeUpTime == time,
                            onClick = { viewModel.updateWakeUpTime(time) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.wakeUpTime,
                    onValueChange = { viewModel.updateWakeUpTime(it) },
                    label = { Text("Custom Wake Time (HH:MM)", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Sleep Time
        item {
            SectionContainer(title = "SLEEP TIME") {
                val sleepPresets = listOf("22:00", "22:30", "23:00", "23:30", "00:00")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sleepPresets.forEach { time ->
                        ChoiceChip(
                            text = time,
                            isSelected = state.sleepTime == time,
                            onClick = { viewModel.updateSleepTime(time) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.sleepTime,
                    onValueChange = { viewModel.updateSleepTime(it) },
                    label = { Text("Custom Sleep Time (HH:MM)", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Daily Study Goal Hours
        item {
            SectionContainer(title = "DAILY STUDY TARGET: ${String.format("%.1f", state.dailyStudyGoalHours)} HOURS") {
                Slider(
                    value = state.dailyStudyGoalHours,
                    onValueChange = { viewModel.updateDailyStudyGoalHours(it) },
                    valueRange = 3f..12f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = IceCyanPrimary,
                        activeTrackColor = IceCyanPrimary,
                        inactiveTrackColor = Color(0x33284B75)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3h (Light)", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                    Text("6h (Standard)", style = MaterialTheme.typography.bodySmall, color = FrostBlueAccent)
                    Text("8h+ (Hardcore)", style = MaterialTheme.typography.bodySmall, color = IceCyanPrimary, fontWeight = FontWeight.Bold)
                    Text("12h (Max)", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                }
            }
        }

        // Focus Block Duration
        item {
            SectionContainer(title = "PREFERRED POMODORO DURATION") {
                val durations = listOf(25, 45, 50, 60, 90)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { dur ->
                        ChoiceChip(
                            text = "$dur min focus",
                            isSelected = state.preferredSessionDurationMinutes == dur,
                            onClick = { viewModel.updateSessionDuration(dur) }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// STEP 4: FITNESS & AI COACH PERSONA
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepFitnessAndAi(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            StepHeaderCard(
                title = "Physical Power & AI Coach",
                subtitle = "Winter Arc demands mental stamina and physical grit. Personalize your workout & coach tone.",
                icon = Icons.Default.FitnessCenter,
                accentColor = FireOrange
            )
        }

        // Workout Type
        item {
            SectionContainer(title = "WORKOUT DISCIPLINE") {
                val types = listOf("Calisthenics", "Running / Cardio", "Gym / Weights", "Home HIIT", "Yoga & Mobility")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        ChoiceChip(
                            text = type,
                            isSelected = state.workoutType == type,
                            onClick = { viewModel.updateWorkoutType(type) }
                        )
                    }
                }
            }
        }

        // Workout Time & Duration
        item {
            SectionContainer(title = "WORKOUT TIMING & DURATION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.workoutTime,
                        onValueChange = { viewModel.updateWorkoutTime(it) },
                        label = { Text("Time (e.g. 17:00)", color = GlassWhiteMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GlassWhite,
                            unfocusedTextColor = GlassWhite,
                            focusedBorderColor = IceCyanPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = FrostedNavyCard,
                            unfocusedContainerColor = FrostedNavyCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    val durations = listOf(20, 30, 45, 60)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duration", style = MaterialTheme.typography.labelSmall, color = GlassWhiteMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            durations.take(2).forEach { d ->
                                ChoiceChip(
                                    text = "${d}m",
                                    isSelected = state.workoutDurationMinutes == d,
                                    onClick = { viewModel.updateWorkoutDuration(d) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Coaching Persona
        item {
            SectionContainer(title = "AI COACHING STYLE") {
                val styles = listOf(
                    "Monk Mode (Strict Discipline)",
                    "Balanced Mentor",
                    "Encouraging Guide"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    styles.forEach { style ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.coachingStyle == style) Color(0x337C8CFF) else FrostedNavyCard,
                            border = BorderStroke(
                                1.dp,
                                if (state.coachingStyle == style) LuxuryAccent else GlassBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateCoachingStyle(style) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (state.coachingStyle == style) Icons.Default.CheckCircle else Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (state.coachingStyle == style) LuxuryAccent else GlassWhiteMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = style,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = when (style) {
                                            "Monk Mode (Strict Discipline)" -> "Zero excuses, uncompromising accountability, and military precision."
                                            "Balanced Mentor" -> "Actionable advice with strategic pacing and recovery."
                                            else -> "Empathetic motivation, positive reinforcement, and gradual building."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Optional Gemini API Key
        item {
            SectionContainer(title = "GEMINI API KEY (OPTIONAL)") {
                OutlinedTextField(
                    value = state.geminiApiKey,
                    onValueChange = { viewModel.updateGeminiApiKey(it) },
                    placeholder = { Text("Paste your Gemini API key here...", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = FrostedNavyCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If left empty, REBUILD uses built-in smart AI algorithms and offline schedules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// STEP 5: NOTIFICATION CENTER & REVIEW
// ----------------------------------------------------
@Composable
private fun StepNotificationsAndReview(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            StepHeaderCard(
                title = "Alarms & Notification Engine",
                subtitle = "REBUILD delivers precise daily reminders that work even when the app is closed.",
                icon = Icons.Default.NotificationsActive,
                accentColor = SuccessGreen
            )
        }

        // Permission Banner if not granted
        if (!hasPermission) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x33FFB300),
                    border = BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Text(
                                text = "Allow notifications so your daily routine alarms fire on time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Grant", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Notification Items Toggles
        item {
            SectionContainer(title = "SCHEDULED ALARMS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotificationToggleItem(
                        title = "Wake Up Alarm",
                        time = state.wakeUpTime,
                        isChecked = state.notifyWakeUp,
                        onCheckedChange = { viewModel.toggleNotifyWakeUp() }
                    )
                    if (state.hasSchool) {
                        NotificationToggleItem(
                            title = "School Departure Reminder",
                            time = state.schoolStartTime,
                            isChecked = state.notifySchoolDeparture,
                            onCheckedChange = { viewModel.toggleNotifySchoolDeparture() }
                        )
                        NotificationToggleItem(
                            title = "School Arrival Attendance",
                            time = state.schoolStartTime,
                            isChecked = state.notifySchoolArrival,
                            onCheckedChange = { viewModel.toggleNotifySchoolArrival() }
                        )
                        NotificationToggleItem(
                            title = "Return Home & Plan Trigger",
                            time = state.schoolEndTime,
                            isChecked = state.notifyReturnHome,
                            onCheckedChange = { viewModel.toggleNotifyReturnHome() }
                        )
                    }
                    NotificationToggleItem(
                        title = "Deep Study Sessions",
                        time = "Post-Commute",
                        isChecked = state.notifyStudySessions,
                        onCheckedChange = { viewModel.toggleNotifyStudySessions() }
                    )
                    NotificationToggleItem(
                        title = "Workout Session",
                        time = state.workoutTime,
                        isChecked = state.notifyWorkout,
                        onCheckedChange = { viewModel.toggleNotifyWorkout() }
                    )
                    NotificationToggleItem(
                        title = "Night Revision Sweep",
                        time = "Before Sleep",
                        isChecked = state.notifyRevision,
                        onCheckedChange = { viewModel.toggleNotifyRevision() }
                    )
                    NotificationToggleItem(
                        title = "Daily Score Reflection",
                        time = "Night Curfew",
                        isChecked = state.notifyReflection,
                        onCheckedChange = { viewModel.toggleNotifyReflection() }
                    )
                    NotificationToggleItem(
                        title = "Sleep & Recovery Protocol",
                        time = state.sleepTime,
                        isChecked = state.notifySleep,
                        onCheckedChange = { viewModel.toggleNotifySleep() }
                    )
                }
            }
        }

        // Profile Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LuxuryCard,
                border = BorderStroke(1.dp, Color(0x337C8CFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONFIGURATION SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Student: ${if (state.name.isBlank()) "Student" else state.name} (${state.studentClass} ${state.board} • ${state.stream})",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhite
                    )
                    Text(
                        text = "• Target: ${state.targetExamName} (${state.targetPercentage}%+ Target)",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhite
                    )
                    Text(
                        text = "• Routine: Wake ${state.wakeUpTime} | Sleep ${state.sleepTime} | Study ${state.dailyStudyGoalHours.toInt()}h daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhite
                    )
                    Text(
                        text = "• Fitness: ${state.workoutType} (${state.workoutDurationMinutes}m at ${state.workoutTime})",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    time: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = FrostedNavyCard,
        border = BorderStroke(0.5.dp, if (isChecked) Color(0x4438E1FF) else GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassWhite
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = IceCyanPrimary,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IceCyanPrimary,
                    checkedTrackColor = Color(0x6638E1FF)
                )
            )
        }
    }
}

@Composable
private fun StepHeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FrostedNavyCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = IceCyanPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) IceCyanPrimary.copy(alpha = 0.2f) else FrostedNavyCard,
        border = BorderStroke(
            1.dp,
            if (isSelected) IceCyanPrimary else GlassBorder
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) IceCyanPrimary else GlassWhite,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GlassWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back", color = GlassWhite, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = {
                if (currentStep < totalSteps) {
                    onNext()
                } else {
                    onComplete()
                }
            },
            enabled = !isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentStep == totalSteps) SuccessGreen else IceCyanPrimary
            ),
            modifier = Modifier
                .weight(if (currentStep > 1) 1.5f else 1f)
                .height(50.dp)
                .testTag("onboarding_action_next")
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DarkNavy,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Initializing...", color = DarkNavy, fontWeight = FontWeight.Bold)
            } else if (currentStep == totalSteps) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = DarkNavy,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LAUNCH REBUILD OS",
                    color = DarkNavy,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            } else {
                Text(
                    text = "Continue",
                    color = DarkNavy,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = DarkNavy,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
