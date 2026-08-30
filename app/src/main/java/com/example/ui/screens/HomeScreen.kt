package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.TaskType
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. REBUILD BRAND HEADER
        item {
            BrandHeader(
                xp = uiState.winterArcState.xp,
                level = uiState.winterArcState.level,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 2. LARGE HERO CARD
        item {
            HeroArcCard(
                dayNumber = uiState.winterArcState.currentDay,
                totalDays = uiState.winterArcState.targetDays,
                disciplineScore = uiState.disciplineScore.totalScore,
                boardExamDaysLeft = uiState.daysUntilExam,
                streakDays = uiState.winterArcState.streak,
                progressPercentage = uiState.progressPercentage,
                onWinterArcClick = onNavigateToWinterArc,
                onBoardExamClick = onNavigateToBoardExam
            )
        }

        // 3. SCHOOL STATUS SYSTEM (4 Action Buttons & Instant State Flow)
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

        // 4. SMART DAILY PLANNER SECTION
        item {
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
                        text = "Today's Protocol",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x33102A45),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { viewModel.generateTodayPlan() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Goal: 6h Deep Work",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tasks items
        if (uiState.todayTasks.isEmpty()) {
            item {
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = FrostBlueAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Tasks Active Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = GlassWhite
                        )
                        Text(
                            text = "Press 'ARRIVED HOME' above or Generate to start today's plan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.generateTodayPlan() },
                            colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("generate_plan_btn")
                        ) {
                            Text("Generate Protocol", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(uiState.todayTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task) }
                )
            }
        }

        // 5. QUICK ACTIONS FOR FOCUS & STUDY
        item {
            QuickActionsRow(
                onStartPomodoro = onNavigateToPomodoro,
                onViewFullPlanner = onNavigateToPlanner
            )
        }
    }
}

@Composable
fun BrandHeader(
    xp: Int,
    level: Int,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    tint = GlassWhite
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.rebuild_logo),
                contentDescription = "REBUILD Logo",
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, IceCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "REBUILD",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = GlassWhite
                )
                Text(
                    text = "DISCIPLINE • FOCUS • TRANSFORM",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GlowPill(
                text = "LVL $level",
                color = PurpleArc,
                icon = Icons.Default.Bolt
            )
            GlowPill(
                text = "$xp XP",
                color = IceCyanPrimary,
                icon = Icons.Default.AutoAwesome
            )
        }
    }
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
                        text = "WINTER ARC",
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

            // 4 ACTION BUTTONS GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchoolActionButton(
                    text = "1. Dispatch\nSchool",
                    icon = Icons.Default.DirectionsWalk,
                    isActive = currentState == SchoolState.TRAVELLING_TO_SCHOOL,
                    isCompleted = currentState == SchoolState.IN_SCHOOL || currentState == SchoolState.TRAVELLING_HOME || currentState == SchoolState.ARRIVED_HOME,
                    onClick = onDispatchSchool,
                    modifier = Modifier.weight(1f)
                )

                SchoolActionButton(
                    text = "2. Arrived\nSchool",
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
                    text = "3. Dispatch\nHome",
                    icon = Icons.Default.DirectionsBus,
                    isActive = currentState == SchoolState.TRAVELLING_HOME,
                    isCompleted = currentState == SchoolState.ARRIVED_HOME,
                    onClick = onDispatchHome,
                    modifier = Modifier.weight(1f)
                )

                SchoolActionButton(
                    text = "4. Arrived\nHome",
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
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TaskItemCard(
    task: DailyPlanTaskEntity,
    onToggle: () -> Unit,
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
        }
    }
}

@Composable
fun QuickActionsRow(
    onStartPomodoro: () -> Unit,
    onViewFullPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onStartPomodoro() },
            shape = RoundedCornerShape(16.dp),
            color = Color(0x33102D5A),
            border = androidx.compose.foundation.BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pomodoro Focus",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    Text(
                        text = "25m / 50m Engine",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onViewFullPlanner() },
            shape = RoundedCornerShape(16.dp),
            color = Color(0x33102D5A),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = FrostBlueAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Full Timetable",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    Text(
                        text = "Manage Protocols",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }
            }
        }
    }
}
