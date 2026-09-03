package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExecutiveReportData
import com.example.data.model.ReportPeriod
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.screens.AnalyticsMetricBox
import com.example.ui.screens.parseHexColor
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
import com.example.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
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
                        tint = GlassWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Analytics & Reports",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Telemetry, habits & performance trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Hero Study & Discipline Overview Card
        item {
            HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Executive Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        GlowPill(
                            text = if (uiState.averageDisciplineScore >= 75) "Optimal Velocity" else "Standard Flow",
                            color = if (uiState.averageDisciplineScore >= 75) SuccessGreen else IceCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMetricBox(
                            label = "Weekly Study",
                            value = "${uiState.weeklyStudyHours}h",
                            accentColor = IceCyanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            label = "Monthly Study",
                            value = "${uiState.monthlyStudyHours}h",
                            accentColor = FrostBlueAccent,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            label = "Avg Discipline",
                            value = "${uiState.averageDisciplineScore}/100",
                            accentColor = PurpleArc,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMetricBox(
                            label = "Habit Consistency",
                            value = "${uiState.habitCompletionRate}%",
                            accentColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            label = "School Attendance",
                            value = "${uiState.attendanceRate}%",
                            accentColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Daily Study Activity Visualizer
        if (uiState.dailyStudyGraph.isNotEmpty()) {
            item {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "7-Day Study Activity (Hours)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxHours = (uiState.dailyStudyGraph.maxOfOrNull { it.hours } ?: 1f).coerceAtLeast(1f)
                            uiState.dailyStudyGraph.forEach { point ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${point.hours}h",
                                        fontSize = 10.sp,
                                        color = GlassWhiteMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val barHeight = ((point.hours / maxHours) * 60).dp.coerceIn(6.dp, 60.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (point.hours > 0) IceCyanPrimary else FrostedNavyCard
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = point.dayLabel,
                                        fontSize = 11.sp,
                                        color = GlassWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Executive Report Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Executive Audit Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    GlowPill(text = "Verified Telemetry", color = IceCyanPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Report Period Selector (Daily / Weekly / Monthly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        val isSelected = uiState.selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricBlue else FrostedNavyCard)
                                .clickable { viewModel.selectReportPeriod(period) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (period) {
                                    ReportPeriod.DAILY -> "Daily"
                                    ReportPeriod.WEEKLY -> "Weekly"
                                    ReportPeriod.MONTHLY -> "Monthly"
                                },
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GlassWhite else GlassWhiteMuted
                            )
                        }
                    }
                }
            }
        }

        // Active Report Card
        uiState.executiveReport?.let { report ->
            item {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassWhite
                                )
                                Text(
                                    text = report.dateRange,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val reportText = buildReportExportText(report)
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, report.title)
                                        putExtra(Intent.EXTRA_TEXT, reportText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Report"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = GlassWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("export_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export Report",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Report Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalyticsMetricBox(
                                label = "Deep Study",
                                value = "${report.studyHours}h",
                                accentColor = IceCyanPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Tasks Done",
                                value = "${report.tasksCompleted}/${report.totalTasks}",
                                accentColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Completion",
                                value = "${report.completionPercentage}%",
                                accentColor = PurpleArc,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalyticsMetricBox(
                                label = "Streak",
                                value = "${report.currentStreak} Days",
                                accentColor = FireOrange,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "XP Gained",
                                value = "+${report.xpEarned}",
                                accentColor = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Missed Tasks",
                                value = "${report.missedTasksCount}",
                                accentColor = if (report.missedTasksCount > 0) FireOrange else SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Subject Breakdown inside Report
                        if (report.subjectBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Subject Progression Breakdown",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassWhite
                            )
                            report.subjectBreakdown.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(FrostedNavyCard.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subjectName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = "${sub.studyHours}h study • ${sub.completedTasks} tasks • ${sub.completedChapters}/${sub.totalChapters} chaps",
                                        fontSize = 11.sp,
                                        color = GlassWhiteMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Subject Mastery Breakdown
        item {
            Text(
                text = "Subject Curriculum Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
        }

        items(uiState.subjects, key = { it.id }) { subject ->
            val color = parseHexColor(subject.colorHex)
            val perc = if (subject.totalChapters > 0) ((subject.completedChapters.toFloat() / subject.totalChapters) * 100).toInt() else 0

            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = "${subject.completedChapters} of ${subject.totalChapters} Chapters Finished",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                    }

                    GlowPill(text = "$perc%", color = color)
                }
            }
        }
    }
}

private fun buildReportExportText(report: ExecutiveReportData): String {
    val sb = StringBuilder()
    sb.appendLine("========================================")
    sb.appendLine("REBUILD EXECUTIVE PROTOCOL REPORT")
    sb.appendLine("${report.title.uppercase()} (${report.dateRange})")
    sb.appendLine("========================================")
    sb.appendLine("Total Deep Study Hours: ${report.studyHours}h")
    sb.appendLine("Task Execution: ${report.tasksCompleted}/${report.totalTasks} (${report.completionPercentage}%)")
    sb.appendLine("Current Consecutive Streak: ${report.currentStreak} Days")
    sb.appendLine("Experience Points (XP) Earned: +${report.xpEarned} XP")
    sb.appendLine("Missed Protocol Items: ${report.missedTasksCount}")
    sb.appendLine("----------------------------------------")
    sb.appendLine("ACADEMIC & SUBJECT BREAKDOWN:")
    report.subjectBreakdown.forEach { sub ->
        sb.appendLine("- ${sub.subjectName}: ${sub.studyHours}h study | ${sub.completedTasks} tasks done | ${sub.completedChapters}/${sub.totalChapters} chapters completed")
    }
    sb.appendLine("========================================")
    sb.appendLine("Generated by REBUILD: The Anti-Mediocrity OS")
    return sb.toString()
}
