package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.TaskType
import com.example.ui.components.CompactLevelXpBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.components.StatBadge
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.HomeViewModel

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToSchool: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToWinterArc: () -> Unit,
    onNavigateToBoardExam: () -> Unit,
    onNavigateToRankReport: () -> Unit = {},
    onStartFocusWithPreset: (subject: String, chapter: String, durationMins: Int) -> Unit = { _, _, _ -> onNavigateToPomodoro() },
    onNavigateToMistakeNotebook: () -> Unit = {},
    onNavigateToFlashcards: () -> Unit = {},
    onNavigateToSyllabus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. BRAND HEADER (Clean study branding, no hardcore XP badges)
        item {
            BrandHeader(
                daysUntilExam = uiState.daysUntilExam,
                onOpenDrawer = onOpenDrawer,
                onSyllabusClick = onNavigateToSyllabus
            )
        }

        // 2. SYLLABUS STUDY HUB (Replaces hardcore Winter Arc cockpit)
        val nextMissionTask = uiState.todayTasks.firstOrNull { !it.isCompleted }
        item {
            SyllabusStudyHubCard(
                currentTask = nextMissionTask,
                daysUntilExam = uiState.daysUntilExam,
                totalTasksCount = uiState.totalTasksCount,
                completedTasksCount = uiState.completedTasksCount,
                onStartFocusPreset = { subject, chapter, durationMins ->
                    onStartFocusWithPreset(subject, chapter, durationMins)
                },
                onCompleteTask = { task ->
                    viewModel.toggleTask(task)
                },
                onGeneratePlan = { viewModel.generateTodayPlan() },
                onSyllabusClick = onNavigateToSyllabus
            )
        }

        // 3. SCHOOL STATUS SYSTEM (Transit & In-School Protocol)
        item {
            SchoolStatusCard(
                currentState = uiState.schoolStatus.currentState,
                travelToSchoolMins = uiState.schoolStatus.travelToSchoolMinutes,
                travelHomeMins = uiState.schoolStatus.travelHomeMinutes,
                onDispatchSchool = { viewModel.onDispatchSchool() },
                onArrivedSchool = { viewModel.onArrivedSchool() },
                onDispatchHome = { viewModel.onDispatchHome() },
                onArrivedHome = { viewModel.onArrivedHome() },
                onViewFullSchool = onNavigateToSchool
            )
        }

        // 4. SMART DAILY STUDY SECTION
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Study Plan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33102A45),
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { viewModel.generateTodayPlan() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.completedTasksCount}/${uiState.totalTasksCount} Completed (${uiState.progressPercentage}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Mini Progress Bar for Today's tasks
                LinearProgressIndicator(
                    progress = { uiState.progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = IceCyanPrimary,
                    trackColor = Color(0x2238E1FF),
                    strokeCap = StrokeCap.Round
                )
            }
        }

        // Tasks items
        if (uiState.todayTasks.isEmpty()) {
            item {
                com.example.ui.components.RebuildEmptyState(
                    title = "No Tasks Active Yet",
                    description = "Press 'ARRIVED HOME' above or tap Generate to launch today's timetable protocol.",
                    icon = Icons.Default.School,
                    actionLabel = "Generate Protocol",
                    onAction = { viewModel.generateTodayPlan() }
                )
            }
        } else {
            items(uiState.todayTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task) },
                    onStartFocus = {
                        onStartFocusWithPreset(task.subject, task.title, task.targetMinutes)
                    }
                )
            }
        }

        // 5. STUDY LAB ACTIONS (Focus, Mistake Notebook, Flashcards, Full Timetable)
        item {
            TacticalQuickActionsRow(
                onStartPomodoro = onNavigateToPomodoro,
                onNavigateToMistakeNotebook = onNavigateToMistakeNotebook,
                onNavigateToFlashcards = onNavigateToFlashcards,
                onViewFullPlanner = onNavigateToPlanner
            )
        }
    }
}

@Composable
fun BrandHeader(
    daysUntilExam: Long = 120,
    onOpenDrawer: () -> Unit = {},
    onSyllabusClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FrostedNavyCard)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = GlassWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.rebuild_logo),
                contentDescription = "REBUILD Logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, IceCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "REBUILD",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GlassWhite,
                    fontSize = 17.sp,
                    maxLines = 1
                )
                Text(
                    text = "Syllabus Tracker",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = IceCyanPrimary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSyllabusClick() }
                .background(Color(0x33102A45))
                .border(1.dp, IceCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("dashboard_exam_badge")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$daysUntilExam Days to Exam",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Overload for backward compatibility
@Composable
fun BrandHeader(
    xp: Int,
    level: Int,
    onOpenDrawer: () -> Unit = {},
    onBadgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BrandHeader(
        daysUntilExam = 120,
        onOpenDrawer = onOpenDrawer,
        onSyllabusClick = onBadgeClick,
        modifier = modifier
    )
}

@Composable
fun HeroArcCard(
    dayNumber: Int,
    totalDays: Int,
    disciplineScore: Int,
    boardExamDaysLeft: Long,
    streakDays: Int,
    progressPercentage: Int,
    onWinterArcClick: () -> Unit,
    onBoardExamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HeroGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_card"),
        onClick = onWinterArcClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "ARC PROTOCOL",
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostBlueAccent,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Day $dayNumber / $totalDays",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite
                    )
                }

                // Progress Ring with Discipline Score
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { disciplineScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = IceCyanPrimary,
                        trackColor = Color(0x3338E1FF),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$disciplineScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = GlassWhite
                        )
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub Hero Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Board Exam Metric
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBoardExamClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x40102142),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = FrostBlueAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Board Exam",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassWhiteMuted,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$boardExamDaysLeft Days Left",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                    }
                }

                // Streak Metric
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x40102142),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FireOrange.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FireOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassWhiteMuted,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$streakDays Days",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = FireOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Today's Protocol Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassWhiteMuted
                    )
                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = IceCyanPrimary,
                    trackColor = Color(0x331F3A60),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun SchoolStatusCard(
    currentState: SchoolState,
    travelToSchoolMins: Int,
    travelHomeMins: Int,
    onDispatchSchool: () -> Unit,
    onArrivedSchool: () -> Unit,
    onDispatchHome: () -> Unit,
    onArrivedHome: () -> Unit,
    onViewFullSchool: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("school_status_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "School Status Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }

                // Current State Badge
                val stateText = when (currentState) {
                    SchoolState.HOME -> "AT HOME"
                    SchoolState.TRAVELLING_TO_SCHOOL -> "TRAVELLING TO SCHOOL"
                    SchoolState.IN_SCHOOL -> "IN SCHOOL"
                    SchoolState.TRAVELLING_HOME -> "TRAVELLING HOME"
                    SchoolState.ARRIVED_HOME -> "ARRIVED HOME"
                }
                val stateColor = when (currentState) {
                    SchoolState.HOME -> GlassWhiteMuted
                    SchoolState.TRAVELLING_TO_SCHOOL -> WarningAmber
                    SchoolState.IN_SCHOOL -> FrostBlueAccent
                    SchoolState.TRAVELLING_HOME -> PurpleArc
                    SchoolState.ARRIVED_HOME -> SuccessGreen
                }

                GlowPill(text = stateText, color = stateColor)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 ACTION BUTTONS GRID (Single-line clean labels, responsive touch targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchoolActionButton(
                    text = "Dispatch School",
                    icon = Icons.Default.DirectionsWalk,
                    isActive = currentState == SchoolState.TRAVELLING_TO_SCHOOL,
                    isCompleted = currentState == SchoolState.IN_SCHOOL || currentState == SchoolState.TRAVELLING_HOME || currentState == SchoolState.ARRIVED_HOME,
                    onClick = onDispatchSchool,
                    modifier = Modifier.weight(1f)
                )

                SchoolActionButton(
                    text = "Arrived School",
                    icon = Icons.Default.School,
                    isActive = currentState == SchoolState.IN_SCHOOL,
                    isCompleted = currentState == SchoolState.TRAVELLING_HOME || currentState == SchoolState.ARRIVED_HOME,
                    onClick = onArrivedSchool,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchoolActionButton(
                    text = "Dispatch Home",
                    icon = Icons.Default.DirectionsBus,
                    isActive = currentState == SchoolState.TRAVELLING_HOME,
                    isCompleted = currentState == SchoolState.ARRIVED_HOME,
                    onClick = onDispatchHome,
                    modifier = Modifier.weight(1f)
                )

                SchoolActionButton(
                    text = "Arrived Home",
                    icon = Icons.Default.Home,
                    isActive = currentState == SchoolState.ARRIVED_HOME,
                    isCompleted = currentState == SchoolState.ARRIVED_HOME,
                    onClick = onArrivedHome,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            if (travelToSchoolMins > 0 || travelHomeMins > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (travelToSchoolMins > 0) {
                        Text(
                            text = "To School: ${travelToSchoolMins}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                    }
                    if (travelHomeMins > 0) {
                        Text(
                            text = "Return Travel: ${travelHomeMins}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SchoolActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    accentColor: Color = IceCyanPrimary,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) accentColor.copy(alpha = 0.25f) else Color(0x33102447)
    val borderColor = if (isActive) accentColor else if (isCompleted) SuccessGreen.copy(alpha = 0.5f) else Color(0x205CE1E6)

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) accentColor else if (isCompleted) SuccessGreen else GlassWhiteMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) GlassWhite else GlassWhiteMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TaskItemCard(
    task: DailyPlanTaskEntity,
    onToggle: () -> Unit,
    onStartFocus: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val subjectColor = when (task.subject) {
        "Physics" -> IceCyanPrimary
        "Chemistry" -> FrostBlueAccent
        "Biology" -> SuccessGreen
        "English" -> WarningAmber
        "Hindi" -> PurpleArc
        "Workout" -> FireOrange
        else -> IceCyanPrimary
    }

    val typeIcon = when (task.type) {
        TaskType.LECTURE -> Icons.Default.School
        TaskType.NOTES -> Icons.Default.MenuBook
        TaskType.REVISION -> Icons.Default.AutoAwesome
        TaskType.PYQ -> Icons.Default.Science
        TaskType.WORKOUT -> Icons.Default.FitnessCenter
        TaskType.CUSTOM -> Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        color = if (task.isCompleted) Color(0x2015305B) else FrostedNavyCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) SuccessGreen.copy(alpha = 0.5f) else Color(0x304B93D8)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox icon
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (task.isCompleted) "Completed" else "Incomplete",
                tint = if (task.isCompleted) SuccessGreen else GlassWhiteMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, subjectColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = task.subject.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${task.targetMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )

                    if (task.movedFromDate != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Rolled Over",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) GlassWhiteMuted else GlassWhite,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.details.isNotEmpty()) {
                    Text(
                        text = task.details,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // XP badge
            GlowPill(
                text = "+${task.xpReward} XP",
                color = if (task.isCompleted) SuccessGreen else IceCyanPrimary
            )

            if (!task.isCompleted && onStartFocus != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = IceCyanPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onStartFocus() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Focus",
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalQuickActionsRow(
    onStartPomodoro: () -> Unit,
    onNavigateToMistakeNotebook: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onViewFullPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TACTICAL LAB TOOLS",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            color = IceCyanPrimary.copy(alpha = 0.8f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalToolCard(
                title = "Focus Cockpit",
                subtitle = "Deep Timer",
                icon = Icons.Default.Timer,
                iconTint = IceCyanPrimary,
                onClick = onStartPomodoro,
                modifier = Modifier.weight(1f)
            )

            TacticalToolCard(
                title = "Mistake Audit",
                subtitle = "Error Traps",
                icon = Icons.Default.Edit,
                iconTint = WarningAmber,
                onClick = onNavigateToMistakeNotebook,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalToolCard(
                title = "SM-2 Recall",
                subtitle = "AI Flashcards",
                icon = Icons.Default.MenuBook,
                iconTint = FrostBlueAccent,
                onClick = onNavigateToFlashcards,
                modifier = Modifier.weight(1f)
            )

            TacticalToolCard(
                title = "Timetable",
                subtitle = "Task Protocol",
                icon = Icons.Default.AutoAwesome,
                iconTint = PurpleArc,
                onClick = onViewFullPlanner,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TacticalToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0x28122A4E),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = GlassWhiteMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FocusPresetChip(
    label: String,
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) IceCyanPrimary.copy(alpha = 0.25f) else Color(0x22102142),
        border = BorderStroke(
            1.dp,
            if (isSelected) IceCyanPrimary else Color(0x2038E1FF)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GlassWhite else GlassWhiteMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SyllabusStudyHubCard(
    currentTask: DailyPlanTaskEntity?,
    daysUntilExam: Long,
    totalTasksCount: Int,
    completedTasksCount: Int,
    onStartFocusPreset: (subject: String, chapter: String, durationMins: Int) -> Unit,
    onCompleteTask: (DailyPlanTaskEntity) -> Unit,
    onGeneratePlan: () -> Unit,
    onSyllabusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPresetMinutes by remember(currentTask?.id) {
        mutableIntStateOf(currentTask?.targetMinutes?.coerceIn(15, 120) ?: 45)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("syllabus_study_hub"),
        shape = RoundedCornerShape(20.dp),
        color = LuxuryCard,
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    IceCyanPrimary.copy(alpha = 0.8f),
                    ElectricBlue.copy(alpha = 0.5f),
                    Color(0x334B93D8)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. SYLLABUS HEADER STRIP (Tappable header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSyllabusClick() }
                    .background(Color(0x330B172E))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CLASS 12 SYLLABUS TRACKER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = IceCyanPrimary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Physics • Chemistry • Biology • Hindi • English",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = GlassWhiteMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarningAmber.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$daysUntilExam Days to Exam",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. IMMEDIATE STUDY TASK / WHAT TO STUDY NEXT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEXT STUDY TOPIC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = FrostBlueAccent
                    )
                }

                Surface(
                    modifier = Modifier.clickable { onSyllabusClick() },
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x2238E1FF)
                ) {
                    Text(
                        text = "Open Syllabus →",
                        style = MaterialTheme.typography.labelSmall,
                        color = IceCyanPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (currentTask != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x350F223D),
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricBlue.copy(alpha = 0.25f),
                                border = BorderStroke(0.5.dp, ElectricBlue)
                            ) {
                                Text(
                                    text = currentTask.subject.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceCyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${currentTask.targetMinutes} mins target",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = currentTask.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite,
                            fontSize = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x250F223D),
                    border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Scheduled Topics Done!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            Text(
                                text = "Explore the Syllabus Tracker to pick your next chapter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 3. STUDY TIMER TRIGGER
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QUICK STUDY TIMER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FrostBlueAccent,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "25m Sprint" to 25,
                        "45m Session" to 45,
                        "60m Deep" to 60
                    ).forEach { (label, mins) ->
                        FocusPresetChip(
                            label = label,
                            minutes = mins,
                            isSelected = selectedPresetMinutes == mins,
                            onClick = { selectedPresetMinutes = mins },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val subj = currentTask?.subject ?: "Physics"
                            val ch = currentTask?.title ?: "Syllabus Study"
                            onStartFocusPreset(subj, ch, selectedPresetMinutes)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("launch_study_timer"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyanPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = DarkNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START FOCUS (${selectedPresetMinutes}M)",
                            color = DarkNavy,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (currentTask != null) {
                        OutlinedButton(
                            onClick = { onCompleteTask(currentTask) },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("primary_directive_done_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SuccessGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete Task",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Done",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedExecutionCockpitCard(
    currentTask: DailyPlanTaskEntity?,
    arcDay: Int,
    arcTargetDays: Int,
    streakDays: Int,
    disciplineScore: Int,
    daysUntilExam: Long,
    totalTasksCount: Int,
    completedTasksCount: Int,
    onStartFocusPreset: (subject: String, chapter: String, durationMins: Int) -> Unit,
    onCompleteTask: (DailyPlanTaskEntity) -> Unit,
    onGeneratePlan: () -> Unit,
    onWinterArcClick: () -> Unit,
    onBoardExamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPresetMinutes by remember(currentTask?.id) {
        mutableIntStateOf(currentTask?.targetMinutes?.coerceIn(15, 120) ?: 45)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unified_execution_cockpit"),
        shape = RoundedCornerShape(20.dp),
        color = LuxuryCard,
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    IceCyanPrimary.copy(alpha = 0.8f),
                    PurpleArc.copy(alpha = 0.5f),
                    Color(0x334B93D8)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. INTEGRATED WINTER ARC TELEMETRY STRIP (Tappable header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onWinterArcClick() }
                    .background(Color(0x330B172E))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = PurpleArc,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "WINTER ARC • DAY $arcDay OF $arcTargetDays",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = FrostBlueAccent,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Discipline Lock-In • Tap to View",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = GlassWhiteMuted
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FireOrange.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, FireOrange.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FireOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${streakDays}D",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FireOrange,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Exam Countdown Pill
                    Surface(
                        modifier = Modifier.clickable { onBoardExamClick() },
                        shape = RoundedCornerShape(8.dp),
                        color = WarningAmber.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, WarningAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${daysUntilExam}D EXAM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Score Gauge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IceCyanPrimary.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "$disciplineScore%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = IceCyanPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 2. CORE DIRECTIVE: "WHAT SHOULD I DO RIGHT NOW?"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WHAT TO DO RIGHT NOW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = IceCyanPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentTask != null) Color(0x33FFA726) else Color(0x334CAF50)
                ) {
                    Text(
                        text = if (currentTask != null) "MISSION ACTIVE" else "RUNWAY CLEAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (currentTask != null) WarningAmber else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (currentTask != null) {
                val subjectColor = when (currentTask.subject) {
                    "Physics" -> IceCyanPrimary
                    "Chemistry" -> FrostBlueAccent
                    "Biology" -> SuccessGreen
                    "English" -> WarningAmber
                    "Hindi" -> PurpleArc
                    "Workout" -> FireOrange
                    else -> IceCyanPrimary
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = subjectColor.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, subjectColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = currentTask.subject.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = subjectColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Target: ${currentTask.targetMinutes}m • +${currentTask.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = currentTask.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        fontSize = 18.sp
                    )

                    if (currentTask.details.isNotBlank()) {
                        Text(
                            text = currentTask.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. QUICK-ACTION POMODORO PRESETS & TRIGGER
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FOCUS PRESET TRIGGER",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhiteMuted,
                        letterSpacing = 1.sp
                    )

                    // Preset duration selection pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FocusPresetChip(
                            label = "25m Sprint",
                            minutes = 25,
                            isSelected = selectedPresetMinutes == 25,
                            onClick = { selectedPresetMinutes = 25 },
                            modifier = Modifier.weight(1f)
                        )
                        FocusPresetChip(
                            label = "${currentTask.targetMinutes}m Target",
                            minutes = currentTask.targetMinutes,
                            isSelected = selectedPresetMinutes == currentTask.targetMinutes,
                            onClick = { selectedPresetMinutes = currentTask.targetMinutes },
                            modifier = Modifier.weight(1f)
                        )
                        FocusPresetChip(
                            label = "50m Deep",
                            minutes = 50,
                            isSelected = selectedPresetMinutes == 50,
                            onClick = { selectedPresetMinutes = 50 },
                            modifier = Modifier.weight(1f)
                        )
                        FocusPresetChip(
                            label = "90m Exam",
                            minutes = 90,
                            isSelected = selectedPresetMinutes == 90,
                            onClick = { selectedPresetMinutes = 90 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Main Action Launchers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onStartFocusPreset(
                                    currentTask.subject,
                                    currentTask.title,
                                    selectedPresetMinutes
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("primary_directive_focus_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IceCyanPrimary,
                                contentColor = Color(0xFF050816)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LAUNCH FOCUS (${selectedPresetMinutes}M)",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onCompleteTask(currentTask) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("primary_directive_done_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SuccessGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete Directive",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Done",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "All scheduled directives completed for today! Your runway is clear. Review mock errors in the Mistake Notebook or calibrate tomorrow's blueprint.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassWhiteMuted,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onGeneratePlan,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E3A68))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IceCyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CALIBRATE PLAN", fontWeight = FontWeight.Bold, color = IceCyanPrimary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onStartFocusPreset("General Focus", "Deep Work Session", 45) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary, contentColor = Color(0xFF050816))
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FREE FOCUS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Backward-compatibility alias
@Composable
fun PrimaryDirectiveCard(
    currentTask: DailyPlanTaskEntity?,
    onStartFocus: () -> Unit,
    onCompleteTask: () -> Unit,
    onGeneratePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedExecutionCockpitCard(
        currentTask = currentTask,
        arcDay = 1,
        arcTargetDays = 90,
        streakDays = 1,
        disciplineScore = 80,
        daysUntilExam = 90,
        totalTasksCount = 1,
        completedTasksCount = 0,
        onStartFocusPreset = { _, _, _ -> onStartFocus() },
        onCompleteTask = { onCompleteTask() },
        onGeneratePlan = onGeneratePlan,
        onWinterArcClick = {},
        onBoardExamClick = {},
        modifier = modifier
    )
}

// Backward-compatibility alias
@Composable
fun QuickActionsRow(
    onStartPomodoro: () -> Unit,
    onViewFullPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    TacticalQuickActionsRow(
        onStartPomodoro = onStartPomodoro,
        onNavigateToMistakeNotebook = {},
        onNavigateToFlashcards = {},
        onViewFullPlanner = onViewFullPlanner,
        modifier = modifier
    )
}
