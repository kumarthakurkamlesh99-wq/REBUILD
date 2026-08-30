package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.HolidayEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.BoardExamViewModel

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun BoardExamScreen(
    viewModel: BoardExamViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddHolidayDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHolidayDialog = true },
                containerColor = IceCyanPrimary,
                contentColor = DarkNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .testTag("add_holiday_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Festival / Holiday")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
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
                            tint = GlassWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACADEMIC MASTERY",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Board Exam 2027 Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = GlassWhite
                        )
                    }
                }
            }

            // Countdown & Velocity Hero Card
            item {
                HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAYS REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FrostBlueAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.remainingDays} Days",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = GlassWhite
                                )
                                Text(
                                    text = "Target: ${uiState.config.targetPercentage}% in Board Exam",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted
                                )
                            }

                            // Dynamic Status Badge
                            if (uiState.isAheadOfSchedule) {
                                GlowPill(
                                    text = "Ahead Of Schedule",
                                    color = SuccessGreen,
                                    icon = Icons.Default.TrendingUp
                                )
                            } else {
                                GlowPill(
                                    text = "Behind Schedule",
                                    color = WarningAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Syllabus Completion Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Syllabus Coverage: ${uiState.config.completedChapters}/${uiState.config.totalSyllabusChapters} Chapters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted
                                )
                                Text(
                                    text = "${uiState.completionPercentage}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IceCyanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { uiState.completionPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = IceCyanPrimary,
                                trackColor = Color(0x331F3A60),
                                strokeCap = StrokeCap.Round
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Key Calculated Rates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalyticsMetricBox(
                                label = "Remaining Ch",
                                value = "${uiState.remainingChapters}",
                                accentColor = IceCyanPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Required Rate",
                                value = "1 Ch / 4 Days",
                                accentColor = FrostBlueAccent,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Daily Revisions",
                                value = "${uiState.config.dailyTargetRevisions}",
                                accentColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Indian Festivals & Holidays Adjustment Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indian Festivals & Workload Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    GlowPill(text = "Auto Calibrated", color = PurpleArc)
                }
            }

            items(uiState.holidays, key = { it.id }) { holiday ->
                HolidayCardItem(holiday = holiday)
            }
        }
    }

    if (showAddHolidayDialog) {
        AddHolidayDialog(
            onDismiss = { showAddHolidayDialog = false },
            onConfirm = { name, date, reduction ->
                viewModel.addCustomHoliday(name, date, reduction)
                showAddHolidayDialog = false
            }
        )
    }
}

@Composable
fun HolidayCardItem(
    holiday: HolidayEntity,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = if (holiday.isIndianFestival) WarningAmber else FrostBlueAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = holiday.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Text(
                    text = "Workload Reduction: ${holiday.workloadReductionPercent}% • ${holiday.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )
            }

            GlowPill(
                text = holiday.date,
                color = WarningAmber
            )
        }
    }
}

@Composable
fun AddHolidayDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, date: String, reduction: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var reduction by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1A33),
        title = {
            Text("Add Festival / Holiday", color = GlassWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Festival Name (e.g. Diwali)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhiteMuted,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (MM-DD or YYYY-MM-DD)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhiteMuted,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reduction,
                    onValueChange = { reduction = it },
                    label = { Text("Workload Reduction % (e.g. 50)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhiteMuted,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, date, reduction.toIntOrNull() ?: 50)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
            ) {
                Text("Save", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GlassWhiteMuted)
            }
        }
    )
}
