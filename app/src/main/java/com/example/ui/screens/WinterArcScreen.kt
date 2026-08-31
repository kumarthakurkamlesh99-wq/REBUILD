package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
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
import com.example.viewmodel.WinterArcViewModel

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun WinterArcScreen(
    viewModel: WinterArcViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                        text = "Winter Arc",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "90-day transformation protocol • Day ${uiState.state.currentDay} / ${uiState.state.targetDays}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Hero 90-Day Status Card
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
                                text = "TRANSFORMATION PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = FrostBlueAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Day ${uiState.state.currentDay} / ${uiState.state.targetDays}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = GlassWhite
                            )
                            Text(
                                text = "${uiState.daysRemaining} Days Until Full Transcendence",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted
                            )
                        }

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                            CircularProgressIndicator(
                                progress = { uiState.progressPercentage / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = IceCyanPrimary,
                                trackColor = Color(0x3338E1FF),
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${uiState.progressPercentage}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = GlassWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { uiState.progressPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = IceCyanPrimary,
                        trackColor = Color(0x331F3A60),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMetricBox(
                            label = "Arc Rank",
                            value = uiState.rankTitle,
                            accentColor = PurpleArc,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            label = "Discipline Score",
                            value = "${uiState.state.transformationScore}/100",
                            accentColor = IceCyanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            label = "Current Streak",
                            value = "${uiState.state.streak} Days",
                            accentColor = FireOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Winter Arc 3-Phase Roadmap
        item {
            Text(
                text = "Transformation Phases",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
        }

        item {
            PhaseCard(
                phaseNumber = 1,
                title = "Phase 1: Foundation (Days 1 - 30)",
                details = "Establish waking habits, eliminate doomscrolling, calibrate 6 hours daily deep study.",
                isCompleted = true,
                isActive = true
            )
        }

        item {
            PhaseCard(
                phaseNumber = 2,
                title = "Phase 2: High Intensity (Days 31 - 60)",
                details = "Peak focus velocity. Complete Physics & Chemistry syllabus chapters, elevate calisthenics.",
                isCompleted = false,
                isActive = false
            )
        }

        item {
            PhaseCard(
                phaseNumber = 3,
                title = "Phase 3: Board Mastery (Days 61 - 90)",
                details = "100% PYQ coverage, daily active recall, peak physical endurance, exam dominance.",
                isCompleted = false,
                isActive = false
            )
        }
    }
}

@Composable
fun PhaseCard(
    phaseNumber: Int,
    title: String,
    details: String,
    isCompleted: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isActive) IceCyanPrimary else if (isCompleted) SuccessGreen else Color(0x303E6D9C)
    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, borderColor, CircleShape)
            ) {
                Text(
                    text = "$phaseNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isActive) IceCyanPrimary else GlassWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
