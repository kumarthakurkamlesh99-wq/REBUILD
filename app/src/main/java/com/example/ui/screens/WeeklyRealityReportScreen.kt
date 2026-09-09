package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.WeeklyRealityReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRealityReportScreen(
    viewModel: WeeklyRealityReportViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val report = uiState.reportData

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Weekly Reality Report", fontWeight = FontWeight.Bold, color = GlassWhite, fontSize = 18.sp)
                        Text("Sunday Honest Academic Audit", color = IceCyanPrimary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy),
                actions = {
                    IconButton(onClick = { viewModel.exportReportToPdf(context) }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = IceCyanPrimary)
                    }
                }
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (report == null || uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IceCyanPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Date Range
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("AUDIT PERIOD", color = GlassWhiteMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(report.dateRange, color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { viewModel.exportReportToPdf(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export A4 PDF", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.loadWeeklyReport() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2642)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = IceCyanPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recompute", color = IceCyanPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Key Reality Metrics
                    item {
                        Text("Executive Reality Metrics", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryCard,
                                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("REAL DEEP WORK & FOCUS", color = IceCyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${report.studyHours}h", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryCard,
                                border = BorderStroke(1.dp, PurpleArc.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("EXECUTION RATE", color = PurpleArc, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${report.completionPercentage}%", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryCard,
                                border = BorderStroke(1.dp, FireOrange.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("STREAK", color = FireOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${report.currentStreak} Days", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryCard,
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("MISSED TARGETS", color = WarningAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${report.missedTasksCount}", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // AI Honest Verdict
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = IceCyanPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Honest Reality Assessment", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val verdict = if (report.studyHours >= 25f && report.completionPercentage >= 75) {
                                    "ELITE PERFORMANCE: You met your weekly benchmark. Academic momentum is on track for Class 12 board dominance. Continue spacing difficult concepts."
                                } else if (report.studyHours >= 15f) {
                                    "MODERATE CONSISTENCY: Effort observed, but output drops during evening slots. Eliminate micro-distractions and safeguard morning study hours."
                                } else {
                                    "DISCIPLINE LEAK DETECTED: Real logged study time fell short of the 25-hour weekly threshold. High exam competition requires strict non-negotiable execution."
                                }
                                Text(verdict, color = GlassWhiteMuted, fontSize = 13.sp, lineHeight = 20.sp)
                            }
                        }
                    }

                    // Subject Distribution Breakdown
                    item {
                        Text("Subject Allocation", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    items(report.subjectBreakdown.size) { index ->
                        val sub = report.subjectBreakdown[index]
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sub.subjectName, color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Tasks Done: ${sub.completedTasks} • Chapters: ${sub.completedChapters}/${sub.totalChapters}", color = GlassWhiteMuted, fontSize = 11.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x33102A45),
                                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text("${sub.studyHours} hrs", color = IceCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
