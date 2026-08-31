package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SchoolState
import com.example.data.local.entity.UserProfileEntity
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive School Flow Action Card
            if (profile?.hasSchool != false) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = LuxuryCard,
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = IceCyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "School Status: ${state.schoolStatus.currentState.name.replace("_", " ")}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (state.schoolStatus.isPresent) SuccessGreen.copy(alpha = 0.2f) else LuxuryAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (state.schoolStatus.isPresent) "PRESENT" else "COMMUTE READY",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.schoolStatus.isPresent) SuccessGreen else IceCyanPrimary,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 interactive action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.dispatchSchool() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.schoolStatus.currentState == SchoolState.TRAVELLING_TO_SCHOOL) LuxuryAccent else Color(0x227C8CFF)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("dispatch_school_btn")
                                ) {
                                    Text("Dispatch\nSchool", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = GlassWhite)
                                }

                                Button(
                                    onClick = { viewModel.arrivedSchool() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.schoolStatus.currentState == SchoolState.IN_SCHOOL) SuccessGreen else Color(0x227C8CFF)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("arrived_school_btn")
                                ) {
                                    Text("Arrived\nSchool", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (state.schoolStatus.currentState == SchoolState.IN_SCHOOL) DarkNavy else GlassWhite)
                                }

                                Button(
                                    onClick = { viewModel.dispatchHome() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.schoolStatus.currentState == SchoolState.TRAVELLING_HOME) WarningAmber else Color(0x227C8CFF)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("dispatch_home_btn")
                                ) {
                                    Text("Dispatch\nHome", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (state.schoolStatus.currentState == SchoolState.TRAVELLING_HOME) DarkNavy else GlassWhite)
                                }

                                Button(
                                    onClick = { viewModel.arrivedHome() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.schoolStatus.currentState == SchoolState.ARRIVED_HOME) SuccessGreen else Color(0x227C8CFF)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("arrived_home_btn")
                                ) {
                                    Text("Arrived\nHome", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (state.schoolStatus.currentState == SchoolState.ARRIVED_HOME) DarkNavy else GlassWhite)
                                }
                            }
                        }
                    }
                }
            }

            // Timetable Blocks
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

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

private fun buildDynamicSchedule(profile: UserProfileEntity?): List<ScheduleTimeBlock> {
    val wake = profile?.wakeUpTime ?: "06:00"
    val sleep = profile?.sleepTime ?: "22:30"
    val hasSchool = profile?.hasSchool ?: true
    val schoolStart = profile?.schoolStartTime ?: "09:45"
    val schoolEnd = profile?.schoolEndTime ?: "13:00"
    val workoutType = profile?.workoutType ?: "Calisthenics"
    val workoutTime = profile?.workoutTime ?: "17:00"
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
