package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Schedule
import com.example.util.DateTimeUtils
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.TaskType
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.RebuildSelectorChip
import com.example.ui.components.RebuildTopAppBar
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.PlannerViewModel

data class ScheduleTimeBlock(
    val timeSlot: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isSchoolBlock: Boolean = false
)

@Composable
fun ScheduleScreen(
    viewModel: PlannerViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.userProfile
    var selectedTab by remember { mutableStateOf(0) } // 0 = Today's Plan, 1 = Master Routine

    val scheduleBlocks = remember(profile) {
        buildDynamicSchedule(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        RebuildTopAppBar(
            title = "Schedule",
            onMenuClick = onOpenDrawer,
            subtitle = if (profile != null) "${profile.studentClass} • ${profile.stream}" else "Time-blocked daily routine"
        )

        // Schedule Mode Toggle Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RebuildSelectorChip(
                text = "Today's Plan (${state.todayTasks.size})",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                selectedColor = IceCyanPrimary
            )
            RebuildSelectorChip(
                text = "Master Routine",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                selectedColor = WarningAmber
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedTab == 0) {
                // Today's Plan Tasks / Imported schedule items
                if (state.todayTasks.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = LuxuryCard,
                            border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "No Schedule Tasks For Today",
                                    color = GlassWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Import a study plan JSON from Settings or generate your daily smart schedule below.",
                                    color = GlassWhiteMuted,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.regeneratePlan() },
                                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("schedule_generate_plan_btn")
                                ) {
                                    Text("Generate Smart Plan", color = DarkNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(state.todayTasks, key = { "schedule_task_${it.id}" }) { task ->
                        val timeDisplay = remember(task.startTime, task.endTime, task.reminderHour, task.reminderMinute, task.targetMinutes, task.delayMinutes) {
                            DateTimeUtils.formatTaskTimeRange(
                                startTime = task.startTime,
                                endTime = task.endTime,
                                reminderHour = task.reminderHour,
                                reminderMinute = task.reminderMinute,
                                durationMinutes = task.targetMinutes,
                                delayMinutes = task.delayMinutes
                            )
                        }.ifBlank { "${task.targetMinutes} mins" }

                        val isOverdue = remember(task.date, task.startTime, task.endTime, task.reminderHour, task.reminderMinute, task.targetMinutes, task.delayMinutes, task.isCompleted) {
                            !task.isCompleted && DateTimeUtils.isTaskOverdue(
                                taskDate = task.date,
                                startTime = task.startTime,
                                endTime = task.endTime,
                                reminderHour = task.reminderHour,
                                reminderMinute = task.reminderMinute,
                                durationMinutes = task.targetMinutes,
                                delayMinutes = task.delayMinutes
                            )
                        }

                        val accentColor = when (task.type) {
                            TaskType.LECTURE -> IceCyanPrimary
                            TaskType.NOTES -> FrostBlueAccent
                            TaskType.REVISION -> WarningAmber
                            TaskType.PYQ -> FireOrange
                            TaskType.WORKOUT -> SuccessGreen
                            else -> LuxuryAccent
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleTask(task) },
                            shape = RoundedCornerShape(16.dp),
                            color = LuxuryCard,
                            border = BorderStroke(
                                0.5.dp,
                                when {
                                    task.isCompleted -> SuccessGreen.copy(alpha = 0.4f)
                                    isOverdue -> WarningAmber.copy(alpha = 0.7f)
                                    task.isDelayed -> IceCyanPrimary.copy(alpha = 0.5f)
                                    else -> accentColor.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleTask(task) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Toggle task completion",
                                            tint = if (task.isCompleted) SuccessGreen else GlassWhiteMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (task.isCompleted) GlassWhiteMuted else GlassWhite,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isOverdue) WarningAmber.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f),
                                                border = BorderStroke(0.5.dp, if (isOverdue) WarningAmber else accentColor.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = if (isOverdue) WarningAmber else accentColor,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = timeDisplay,
                                                        color = if (isOverdue) WarningAmber else accentColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0x22FFFFFF)
                                            ) {
                                                Text(
                                                    text = task.subject,
                                                    color = GlassWhiteMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (isOverdue) {
                                                Text(
                                                    text = "NOT PERFORMED",
                                                    color = WarningAmber,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            } else if (task.isDelayed) {
                                                Text(
                                                    text = "DELAYED (+${task.delayMinutes}m)",
                                                    color = FrostBlueAccent,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (task.details.isNotBlank()) {
                                                Text(
                                                    text = task.details,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = GlassWhiteMuted,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                // Quick Delay Bar for uncompleted tasks
                                if (!task.isCompleted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF142B47),
                                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                                            modifier = Modifier.clickable { viewModel.delayTask(task, 15) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Update,
                                                    contentDescription = "Delay 15m",
                                                    tint = IceCyanPrimary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "+15m Delay",
                                                    color = IceCyanPrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF142B47),
                                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                                            modifier = Modifier.clickable { viewModel.delayTask(task, 30) }
                                        ) {
                                            Text(
                                                text = "+30m",
                                                color = GlassWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Timetable Blocks (Master Routine)
                items(scheduleBlocks) { block ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = LuxuryCard,
                        border = BorderStroke(0.5.dp, block.accentColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = block.accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = block.icon,
                                        contentDescription = null,
                                        tint = block.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = block.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = block.timeSlot,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = block.accentColor,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = block.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

private fun buildDynamicSchedule(profile: UserProfileEntity?): List<ScheduleTimeBlock> {
    val wake = DateTimeUtils.formatTo12Hour(profile?.wakeUpTime?.ifBlank { "06:00" } ?: "06:00")
    val sleep = DateTimeUtils.formatTo12Hour(profile?.sleepTime?.ifBlank { "22:30" } ?: "22:30")
    val hasSchool = profile?.hasSchool ?: true
    val schoolStart = DateTimeUtils.formatTo12Hour(profile?.schoolStartTime?.ifBlank { "09:45" } ?: "09:45")
    val schoolEnd = DateTimeUtils.formatTo12Hour(profile?.schoolEndTime?.ifBlank { "13:00" } ?: "13:00")
    val workoutType = profile?.workoutType ?: "Calisthenics"
    val workoutTime = DateTimeUtils.formatTo12Hour(profile?.workoutTime?.ifBlank { "17:00" } ?: "17:00")
    val workoutDur = profile?.workoutDurationMinutes ?: 30
    val stream = profile?.stream ?: "Science (PCM)"

    val blocks = mutableListOf<ScheduleTimeBlock>()

    // 1. Wake Up
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "$wake – Wake",
            title = "Wake Up & Cold Reset",
            description = "Hydrate 500ml water, zero screen time, deep breathing & sunlight exposure.",
            icon = Icons.Default.WbSunny,
            accentColor = WarningAmber
        )
    )

    // 2. Morning Focus Block
    val morningSubject = when {
        stream.contains("PCB") -> "Biology"
        stream.contains("Commerce") -> "Accountancy"
        stream.contains("Arts") -> "History"
        else -> "Physics"
    }
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "Morning Session",
            title = "Deep Study Block 1 • $morningSubject",
            description = "High-cognition concepts, formulas, active recall & problem sets.",
            icon = Icons.Default.MenuBook,
            accentColor = LuxuryAccent
        )
    )

    // 3. School Block if applicable
    if (hasSchool) {
        blocks.add(
            ScheduleTimeBlock(
                timeSlot = "$schoolStart – $schoolEnd",
                title = "School Attendance & Commute",
                description = "Departure at $schoolStart. Core lectures, practicals and return by $schoolEnd.",
                icon = Icons.Default.School,
                accentColor = IceCyanPrimary,
                isSchoolBlock = true
            )
        )
        blocks.add(
            ScheduleTimeBlock(
                timeSlot = "Post-Commute",
                title = "Recovery & Lunch",
                description = "Nutritious meal, hydration, 20-minute mental reset for afternoon deep work.",
                icon = Icons.Default.Home,
                accentColor = SuccessGreen
            )
        )
    } else {
        blocks.add(
            ScheduleTimeBlock(
                timeSlot = "Midday Session",
                title = "Self-Study Block 2",
                description = "Dedicated independent deep focus module and problem solving.",
                icon = Icons.Default.MenuBook,
                accentColor = FrostBlueAccent
            )
        )
    }

    // 4. Afternoon Deep Study Block
    val afternoonSubject = when {
        stream.contains("PCB") -> "Chemistry"
        stream.contains("Commerce") -> "Economics"
        stream.contains("Arts") -> "Political Science"
        else -> "Chemistry"
    }
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "Afternoon Session",
            title = "Deep Study Block 2 • $afternoonSubject",
            description = "Theory review, reaction mechanisms/derivations, chapter notes.",
            icon = Icons.Default.MenuBook,
            accentColor = FrostBlueAccent
        )
    )

    // 5. Workout
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "$workoutTime (${workoutDur}m)",
            title = "Physical Power • $workoutType",
            description = "Structured training to build physical stamina and mental grit.",
            icon = Icons.Default.FitnessCenter,
            accentColor = FireOrange
        )
    )

    // 6. Evening Deep Study Block
    val eveningSubject = when {
        stream.contains("PCM") -> "Mathematics"
        stream.contains("PCB") -> "Physics"
        stream.contains("Commerce") -> "Business Studies"
        stream.contains("Arts") -> "Geography"
        else -> "Mathematics"
    }
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "Evening Session",
            title = "Deep Study Block 3 • $eveningSubject & PYQs",
            description = "Past year questions, timed mock sets, and targeted weak-spot drilling.",
            icon = Icons.Default.Timer,
            accentColor = PurpleArc
        )
    )

    // 7. Night Revision
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "Night Sweep",
            title = "Night Revision & Daily Score Reflection",
            description = "Audit completed tasks, log discipline score, and prep tomorrow's plan.",
            icon = Icons.Default.SelfImprovement,
            accentColor = SuccessGreen
        )
    )

    // 8. Sleep Protocol
    blocks.add(
        ScheduleTimeBlock(
            timeSlot = "$sleep – Sleep",
            title = "Sleep & Recovery Protocol",
            description = "Device curfew, dark room, neural rest and full circadian alignment.",
            icon = Icons.Default.Bedtime,
            accentColor = GlassWhiteMuted
        )
    )

    return blocks
}
