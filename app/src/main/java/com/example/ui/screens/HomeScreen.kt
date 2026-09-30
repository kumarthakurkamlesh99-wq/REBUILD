package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.TaskType
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.RebuildDialog
import com.example.ui.components.RebuildSelectorChip
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.DashboardAiRecommendation
import com.example.viewmodel.DashboardScheduleItem
import com.example.viewmodel.HabitWithStatus
import com.example.viewmodel.HomeUiState
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.SyllabusSubjectSummary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToPlanner: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToWinterArc: () -> Unit = {},
    onNavigateToRankReport: () -> Unit = {},
    onStartFocusWithPreset: (subject: String, chapter: String, durationMins: Int) -> Unit = { _, _, _ -> onNavigateToPomodoro() },
    onNavigateToMistakeNotebook: () -> Unit = {},
    onNavigateToFlashcards: () -> Unit = {},
    onNavigateToSyllabus: () -> Unit = {},
    onNavigateToJarvis: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP APP BAR / BRAND HEADER
        item {
            CommandCenterHeader(
                currentDay = uiState.winterArcCurrentDay,
                daysUntilExam = uiState.daysUntilExam,
                onOpenDrawer = onOpenDrawer,
                onNavigateToJarvis = onNavigateToJarvis
            )
        }

        // SECTION 1: HERO STATUS CARD
        item {
            HeroStatusCard(
                uiState = uiState,
                onSyllabusClick = onNavigateToSyllabus
            )
        }

        // SECTION 3: CONTINUE BUTTON (Top priority action)
        val nextMissionTask = uiState.todayTasks.firstOrNull { !it.isCompleted }
        item {
            ContinueActionButton(
                currentTask = nextMissionTask,
                onStartTask = { task ->
                    onStartFocusWithPreset(task.subject, task.title, task.targetMinutes)
                },
                onStartGeneralFocus = onNavigateToPomodoro
            )
        }

        // SECTION 2: TODAY'S MISSION
        item {
            TodaysMissionSection(
                tasks = uiState.todayTasks,
                completedCount = uiState.completedTasksCount,
                totalCount = uiState.totalTasksCount,
                onToggleTask = { viewModel.toggleTask(it) },
                onStartTaskFocus = { task ->
                    onStartFocusWithPreset(task.subject, task.title, task.targetMinutes)
                },
                onGeneratePlan = { viewModel.generateTodayPlan() },
                onAddTaskClick = { showAddTaskDialog = true },
                onNavigateToPlanner = onNavigateToPlanner
            )
        }

        // SECTION 4: STUDY PROGRESS
        item {
            StudyProgressSection(
                studyMinutes = uiState.todayStudyMinutes,
                targetStudyMinutes = uiState.targetStudyMinutes,
                completedTasks = uiState.completedTasksCount,
                totalTasks = uiState.totalTasksCount,
                completedHabits = uiState.completedHabitsCount,
                totalHabits = uiState.totalHabitsCount
            )
        }

        // SECTION 5: TODAY'S SCHEDULE
        if (uiState.scheduleTimeline.isNotEmpty()) {
            item {
                TodayScheduleTimelineSection(
                    timeline = uiState.scheduleTimeline
                )
            }
        }

        // SECTION 6: HABITS
        if (uiState.habits.isNotEmpty()) {
            item {
                HabitsQuickCompletionSection(
                    habits = uiState.habits,
                    onToggleHabit = { viewModel.toggleHabit(it.habit) }
                )
            }
        }

        // SECTION 7: SUBJECT PROGRESS
        if (uiState.subjectSummaries.isNotEmpty()) {
            item {
                SubjectProgressSection(
                    summaries = uiState.subjectSummaries,
                    onSubjectClick = onNavigateToSyllabus
                )
            }
        }

        // SECTION 8: AI COACH
        if (uiState.aiRecommendation != null) {
            item {
                AiCoachSingleCard(
                    recommendation = uiState.aiRecommendation!!,
                    onExecuteRecommendation = {
                        val sub = uiState.aiRecommendation?.subject ?: "Physics"
                        val ch = uiState.aiRecommendation?.chapter ?: "Revision"
                        onStartFocusWithPreset(sub, ch, 45)
                    },
                    onOpenJarvis = onNavigateToJarvis
                )
            }
        }

        // SECTION 9: QUICK ACTIONS
        item {
            QuickActionsSection(
                onAddTask = { showAddTaskDialog = true },
                onAddNote = onNavigateToNotes,
                onGeneratePlan = { viewModel.generateTodayPlan() },
                onStartFocus = onNavigateToPomodoro
            )
        }
    }

    if (showAddTaskDialog) {
        QuickAddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, subject, mins ->
                viewModel.addNewTask(title, subject, mins)
                showAddTaskDialog = false
            }
        )
    }
}

// =========================================================================
// HEADER
// =========================================================================

@Composable
private fun CommandCenterHeader(
    currentDay: Int,
    daysUntilExam: Long,
    onOpenDrawer: () -> Unit,
    onNavigateToJarvis: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(40.dp)
                    .background(FrostedNavyCard.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .testTag("drawer_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer",
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "REBUILD",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "STUDENT COMMAND CENTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Live Target Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FrostedNavyCard,
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${daysUntilExam}d Left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onNavigateToJarvis,
                modifier = Modifier
                    .size(40.dp)
                    .background(FrostedNavyCard.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .testTag("open_jarvis_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "JARVIS AI",
                    tint = LuxuryAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =========================================================================
// SECTION 1: HERO STATUS CARD
// =========================================================================

@Composable
private fun HeroStatusCard(
    uiState: HomeUiState,
    onSyllabusClick: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_status_card")
            .clickable { onSyllabusClick() },
        borderBrush = Brush.linearGradient(
            listOf(IceCyanPrimary.copy(alpha = 0.5f), ElectricBlue.copy(alpha = 0.2f))
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Top Primary Target Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WINTER ARC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhiteMuted,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Day ${uiState.winterArcCurrentDay} / ${uiState.winterArcTotalDays}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassWhite
                    )
                }

                // Countdown Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = IceCyanPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BOARD EXAM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = IceCyanPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${uiState.daysUntilExam} Days Left",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Metric Bottom Bar: Streak, Discipline, Readiness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak
                MetricPill(
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = FireOrange,
                    label = "STREAK",
                    value = "${uiState.realStreak} Days",
                    modifier = Modifier.weight(1f)
                )

                // Discipline
                MetricPill(
                    icon = Icons.Default.Bolt,
                    iconTint = WarningAmber,
                    label = "DISCIPLINE",
                    value = "${uiState.disciplineScore.totalScore}%",
                    modifier = Modifier.weight(1f)
                )

                // Readiness
                MetricPill(
                    icon = Icons.Default.Star,
                    iconTint = SuccessGreen,
                    label = "READINESS",
                    value = "${uiState.readinessScore}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkNavy.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, GlassWhite.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhiteMuted,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlassWhite
            )
        }
    }
}

// =========================================================================
// SECTION 3: CONTINUE BUTTON (Top Execution Action)
// =========================================================================

@Composable
private fun ContinueActionButton(
    currentTask: DailyPlanTaskEntity?,
    onStartTask: (DailyPlanTaskEntity) -> Unit,
    onStartGeneralFocus: () -> Unit
) {
    if (currentTask != null) {
        Button(
            onClick = { onStartTask(currentTask) },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("continue_studying_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = IceCyanPrimary,
                contentColor = DarkNavy
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkNavy.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = DarkNavy,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "START FOCUS SESSION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${currentTask.subject}: ${currentTask.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkNavy.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${currentTask.targetMinutes}m",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    } else {
        Button(
            onClick = onStartGeneralFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("continue_studying_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FrostedNavyCard,
                contentColor = IceCyanPrimary
            ),
            border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ALL MISSIONS DONE • START FREE FOCUS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// =========================================================================
// SECTION 2: TODAY'S MISSION
// =========================================================================

@Composable
private fun TodaysMissionSection(
    tasks: List<DailyPlanTaskEntity>,
    completedCount: Int,
    totalCount: Int,
    onToggleTask: (DailyPlanTaskEntity) -> Unit,
    onStartTaskFocus: (DailyPlanTaskEntity) -> Unit,
    onGeneratePlan: () -> Unit,
    onAddTaskClick: () -> Unit,
    onNavigateToPlanner: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("todays_mission_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S MISSION",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Core Daily Execution Queue",
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (totalCount > 0 && completedCount == totalCount) SuccessGreen.copy(alpha = 0.15f) else IceCyanPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (totalCount > 0 && completedCount == totalCount) SuccessGreen.copy(alpha = 0.4f) else IceCyanPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$completedCount / $totalCount Completed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalCount > 0 && completedCount == totalCount) SuccessGreen else IceCyanPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = IceCyanPrimary,
                trackColor = DarkNavy.copy(alpha = 0.6f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (tasks.isEmpty()) {
                // Empty state design (as strictly requested)
                MissionEmptyState(
                    onGeneratePlan = onGeneratePlan,
                    onAddTask = onAddTaskClick
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { task ->
                        MissionTaskRow(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onStartFocus = { onStartTaskFocus(task) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddTaskClick,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IceCyanPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Task", fontSize = 11.sp)
                    }

                    Text(
                        text = "View Planner →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary,
                        modifier = Modifier
                            .clickable { onNavigateToPlanner() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionTaskRow(
    task: DailyPlanTaskEntity,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit
) {
    val priority = when {
        task.type == TaskType.PYQ || task.targetMinutes >= 60 -> "HIGH"
        task.type == TaskType.REVISION -> "NORMAL"
        else -> "MEDIUM"
    }

    val priorityColor = when (priority) {
        "HIGH" -> FireOrange
        "MEDIUM" -> WarningAmber
        else -> FrostBlueAccent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        color = if (task.isCompleted) DarkNavy.copy(alpha = 0.35f) else FrostedNavyCard.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, if (task.isCompleted) SuccessGreen.copy(alpha = 0.25f) else GlassWhite.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox toggle
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Task",
                        tint = if (task.isCompleted) SuccessGreen else GlassWhiteMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Subject tag
                        Text(
                            text = task.subject.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = IceCyanPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Priority Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = priority,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = task.title,
                        fontSize = 13.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (task.isCompleted) GlassWhiteMuted else GlassWhite,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${task.targetMinutes}m",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhiteMuted
                )

                if (!task.isCompleted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onStartFocus,
                        modifier = Modifier
                            .size(28.dp)
                            .background(IceCyanPrimary.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Focus",
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionEmptyState(
    onGeneratePlan: () -> Unit,
    onAddTask: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.TaskAlt,
            contentDescription = null,
            tint = IceCyanPrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No tasks available.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GlassWhite
        )
        Text(
            text = "Generate today's exam plan or create custom tasks.",
            fontSize = 11.sp,
            color = GlassWhiteMuted
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onGeneratePlan,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary, contentColor = DarkNavy)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate Today's Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onAddTask,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassWhite)
            ) {
                Text("Create Task", fontSize = 11.sp)
            }
        }
    }
}

// =========================================================================
// SECTION 4: STUDY PROGRESS
// =========================================================================

@Composable
private fun StudyProgressSection(
    studyMinutes: Int,
    targetStudyMinutes: Int,
    completedTasks: Int,
    totalTasks: Int,
    completedHabits: Int,
    totalHabits: Int
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_progress_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "STUDY PROGRESS",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlassWhite,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Execution Metrics for Today",
                fontSize = 11.sp,
                color = GlassWhiteMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Study Time
                val hours = studyMinutes / 60
                val mins = studyMinutes % 60
                val targetHours = targetStudyMinutes / 60
                val studyFraction = if (targetStudyMinutes > 0) (studyMinutes.toFloat() / targetStudyMinutes).coerceIn(0f, 1f) else 0f

                ProgressMetricCard(
                    title = "Study Time",
                    primaryValue = "${hours}h ${mins}m",
                    targetValue = "/ ${targetHours}h",
                    fraction = studyFraction,
                    barColor = IceCyanPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Tasks
                val taskFraction = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks).coerceIn(0f, 1f) else 0f
                ProgressMetricCard(
                    title = "Tasks",
                    primaryValue = "$completedTasks",
                    targetValue = "/ $totalTasks",
                    fraction = taskFraction,
                    barColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )

                // Habits
                val habitFraction = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits).coerceIn(0f, 1f) else 0f
                ProgressMetricCard(
                    title = "Habits",
                    primaryValue = "$completedHabits",
                    targetValue = "/ $totalHabits",
                    fraction = habitFraction,
                    barColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProgressMetricCard(
    title: String,
    primaryValue: String,
    targetValue: String,
    fraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkNavy.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, GlassWhite.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GlassWhiteMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = primaryValue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassWhite
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = targetValue,
                    fontSize = 10.sp,
                    color = GlassWhiteMuted,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = barColor,
                trackColor = DarkNavy,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// =========================================================================
// SECTION 5: TODAY'S SCHEDULE (Timeline)
// =========================================================================

@Composable
private fun TodayScheduleTimelineSection(
    timeline: List<DashboardScheduleItem>
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_schedule_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Automated Day Timeline",
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                timeline.forEachIndexed { index, item ->
                    ScheduleTimelineRow(
                        item = item,
                        isLast = index == timeline.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimelineRow(
    item: DashboardScheduleItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time badge
        Surface(
            modifier = Modifier.width(68.dp),
            shape = RoundedCornerShape(8.dp),
            color = DarkNavy.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.2f))
        ) {
            Text(
                text = item.timeDisplay,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = IceCyanPrimary,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (item.isCompleted) SuccessGreen else IceCyanPrimary.copy(alpha = 0.5f),
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isCompleted) GlassWhiteMuted else GlassWhite,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    fontSize = 10.sp,
                    color = GlassWhiteMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Category Tag
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = FrostedNavyCard.copy(alpha = 0.7f)
        ) {
            Text(
                text = item.category,
                fontSize = 9.sp,
                color = GlassWhiteMuted,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// =========================================================================
// SECTION 6: HABITS
// =========================================================================

@Composable
private fun HabitsQuickCompletionSection(
    habits: List<HabitWithStatus>,
    onToggleHabit: (HabitWithStatus) -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habits_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABITS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "One-Tap Daily Consistency",
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }

                val completed = habits.count { it.isCompletedToday }
                Text(
                    text = "$completed / ${habits.size} Done",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (completed == habits.size) SuccessGreen else IceCyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                habits.forEach { habitStatus ->
                    HabitQuickRow(
                        habitStatus = habitStatus,
                        onToggle = { onToggleHabit(habitStatus) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitQuickRow(
    habitStatus: HabitWithStatus,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(10.dp),
        color = if (habitStatus.isCompletedToday) DarkNavy.copy(alpha = 0.4f) else FrostedNavyCard.copy(alpha = 0.7f),
        border = BorderStroke(
            1.dp,
            if (habitStatus.isCompletedToday) SuccessGreen.copy(alpha = 0.35f) else GlassWhite.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (habitStatus.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (habitStatus.isCompletedToday) SuccessGreen else GlassWhiteMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = habitStatus.habit.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habitStatus.isCompletedToday) GlassWhite else GlassWhiteMuted,
                    textDecoration = if (habitStatus.isCompletedToday) TextDecoration.None else TextDecoration.None
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (habitStatus.habit.streak > 0) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = FireOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${habitStatus.habit.streak}d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FireOrange
                    )
                }
            }
        }
    }
}

// =========================================================================
// SECTION 7: SUBJECT PROGRESS
// =========================================================================

@Composable
private fun SubjectProgressSection(
    summaries: List<SyllabusSubjectSummary>,
    onSubjectClick: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_progress_section")
            .clickable { onSubjectClick() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUBJECT PROGRESS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Class 12 Curriculum Tracking",
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }

                Text(
                    text = "Full Syllabus →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                summaries.take(4).forEach { sub ->
                    val color = when (sub.code) {
                        "PHYSICS" -> ElectricBlue
                        "CHEMISTRY" -> LuxuryAccent
                        "BIOLOGY" -> SuccessGreen
                        "HINDI" -> WarningAmber
                        else -> FrostBlueAccent
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sub.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Text(
                                text = "${sub.percentage}% (${sub.completedChapters}/${sub.totalChapters} ch)",
                                fontSize = 11.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (sub.percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = DarkNavy.copy(alpha = 0.6f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 8: AI COACH (Only One Recommendation)
// =========================================================================

@Composable
private fun AiCoachSingleCard(
    recommendation: DashboardAiRecommendation,
    onExecuteRecommendation: () -> Unit,
    onOpenJarvis: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_coach_card"),
        borderBrush = Brush.linearGradient(
            listOf(LuxuryAccent.copy(alpha = 0.45f), IceCyanPrimary.copy(alpha = 0.2f))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(LuxuryAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = LuxuryAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI COACH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LuxuryAccent,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Open JARVIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary,
                    modifier = Modifier.clickable { onOpenJarvis() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recommendation.headline,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendation.recommendation,
                fontSize = 12.sp,
                color = GlassWhiteMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onExecuteRecommendation,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuxuryAccent.copy(alpha = 0.2f),
                    contentColor = LuxuryAccent
                ),
                border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Execute Recommended Focus Block",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================
// SECTION 9: QUICK ACTIONS
// =========================================================================

@Composable
private fun QuickActionsSection(
    onAddTask: () -> Unit,
    onAddNote: () -> Unit,
    onGeneratePlan: () -> Unit,
    onStartFocus: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlassWhite,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Task",
                    onClick = onAddTask,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.NoteAdd,
                    label = "Add Note",
                    onClick = onAddNote,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.AutoAwesome,
                    label = "Gen Plan",
                    onClick = onGeneratePlan,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Timer,
                    label = "Start Focus",
                    onClick = onStartFocus,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = DarkNavy.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IceCyanPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
        }
    }
}

// =========================================================================
// QUICK ADD TASK DIALOG
// =========================================================================

@Composable
private fun QuickAddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, subject: String, targetMins: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedMinutes by remember { mutableIntStateOf(45) }

    val subjects = listOf("Physics", "Chemistry", "Biology", "English", "Hindi", "Revision")
    val minutePresets = listOf(15, 30, 45, 60, 90)

    RebuildDialog(
        onDismiss = onDismiss,
        title = "Create Today's Task",
        confirmButtonText = "Schedule Task",
        onConfirm = {
            if (title.isNotBlank()) {
                onAddTask(title, selectedSubject, selectedMinutes)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title / Topic") },
                placeholder = { Text("e.g. Electromagnetic Induction Numericals") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IceCyanPrimary,
                    unfocusedBorderColor = GlassWhite.copy(alpha = 0.2f),
                    focusedTextColor = GlassWhite,
                    unfocusedTextColor = GlassWhite
                )
            )

            Text("Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlassWhiteMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subjects.take(3).forEach { sub ->
                    RebuildSelectorChip(
                        text = sub,
                        isSelected = selectedSubject == sub,
                        onClick = { selectedSubject = sub }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subjects.drop(3).forEach { sub ->
                    RebuildSelectorChip(
                        text = sub,
                        isSelected = selectedSubject == sub,
                        onClick = { selectedSubject = sub }
                    )
                }
            }

            Text("Target Minutes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlassWhiteMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                minutePresets.forEach { mins ->
                    RebuildSelectorChip(
                        text = "${mins}m",
                        isSelected = selectedMinutes == mins,
                        onClick = { selectedMinutes = mins }
                    )
                }
            }
        }
    }
}
