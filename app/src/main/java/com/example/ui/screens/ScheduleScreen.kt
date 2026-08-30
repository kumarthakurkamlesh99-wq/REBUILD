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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
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

    val scheduleBlocks = listOf(
        ScheduleTimeBlock("06:00 AM – 06:30 AM", "Wake Up & Cold Reset", "Hydrate 500ml water, zero screen time, deep breathing.", Icons.Default.WbSunny, WarningAmber),
        ScheduleTimeBlock("06:30 AM – 08:30 AM", "Deep Study Block 1 • Physics", "Modern Physics & Nuclei formulas, 10 numerical problems.", Icons.Default.MenuBook, LuxuryAccent),
        ScheduleTimeBlock("08:30 AM – 09:45 AM", "Breakfast & School Prep", "High-protein breakfast, bag check, quick flashcard review.", Icons.Default.Home, FrostBlueAccent),
        ScheduleTimeBlock("09:45 AM – 12:00 PM", "School Dispatch & Attendance", "Departure at 09:45 AM. Core school practicals & lectures.", Icons.Default.School, IceCyanPrimary, isSchoolBlock = true),
        ScheduleTimeBlock("12:00 PM – 01:00 PM", "School Dispersal & Return", "Dispatch home at 12:00 PM, reach home by ~01:00 PM.", Icons.Default.DirectionsRun, IceCyanPrimary, isSchoolBlock = true),
        ScheduleTimeBlock("01:00 PM – 02:00 PM", "Post-Commute Recovery & Lunch", "Warm lunch, hydration (1L), 20 min cognitive reset.", Icons.Default.Home, SuccessGreen),
        ScheduleTimeBlock("02:00 PM – 04:30 PM", "Deep Study Block 2 • Chemistry", "P-Block Elements & Coordination Compounds (2x50m Pomodoro).", Icons.Default.MenuBook, LuxuryAccent),
        ScheduleTimeBlock("04:30 PM – 05:30 PM", "Calisthenics & Cardio", "Running 20m + 3x15 Pushups + 3x20 Squats.", Icons.Default.DirectionsRun, FireOrange),
        ScheduleTimeBlock("06:30 PM – 08:45 PM", "Deep Study Block 3 • Biology & PYQs", "Genetics Mendelian inheritance + 15 past year questions.", Icons.Default.MenuBook, PurpleArc),
        ScheduleTimeBlock("09:30 PM – 10:15 PM", "Daily Reflection & Language Review", "Evening journal audit in REBUILD Notes + English/Hindi review.", Icons.Default.MenuBook, FrostBlueAccent),
        ScheduleTimeBlock("10:30 PM", "Sleep & Recovery Protocol", "Phone on airplane mode, dark room, 7.5 hours neural rest.", Icons.Default.Bedtime, GlassWhiteMuted)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(top = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LuxuryCard)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = GlassWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SCHEDULE & TIMETABLE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = GlassWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Time-blocked daily operating schedule & school flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive School Flow Action Card
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
