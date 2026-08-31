package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SessionType
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
import com.example.viewmodel.PomodoroViewModel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val minutes = uiState.remainingSeconds / 60
    val seconds = uiState.remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val progress = if (uiState.totalSeconds > 0) {
        1f - (uiState.remainingSeconds.toFloat() / uiState.totalSeconds)
    } else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                        text = "Focus & Pomodoro",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Deep work sessions & timer protocols",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Mode Selector Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    ModePill(
                        title = "25 / 5 Standard",
                        isSelected = uiState.sessionType == SessionType.POMODORO_25_5,
                        onClick = { viewModel.setMode(SessionType.POMODORO_25_5) }
                    )
                }
                item {
                    ModePill(
                        title = "50 / 10 Deep Work",
                        isSelected = uiState.sessionType == SessionType.POMODORO_50_10,
                        onClick = { viewModel.setMode(SessionType.POMODORO_50_10) }
                    )
                }
                item {
                    ModePill(
                        title = "45m Custom",
                        isSelected = uiState.sessionType == SessionType.CUSTOM_FOCUS,
                        onClick = { viewModel.setMode(SessionType.CUSTOM_FOCUS) }
                    )
                }
                item {
                    ModePill(
                        title = "90m Exam Sim",
                        isSelected = uiState.sessionType == SessionType.DEEP_WORK,
                        onClick = { viewModel.setMode(SessionType.DEEP_WORK) }
                    )
                }
                item {
                    ModePill(
                        title = "30m Revision",
                        isSelected = uiState.sessionType == SessionType.REVISION,
                        onClick = { viewModel.setMode(SessionType.REVISION) }
                    )
                }
            }
        }

        // Circular Timer Visualizer
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(270.dp)
                    .padding(12.dp)
            ) {
                // Background Track Glow
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0x2038E1FF),
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                // Active Progress Arc
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (uiState.isBreak) SuccessGreen else IceCyanPrimary,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                // Center Information
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isBreak) "BREAK TIME" else "DEEP WORK FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isBreak) SuccessGreen else FrostBlueAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        color = GlassWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.selectedSubject} • ${uiState.selectedChapter}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Timer Controls: Start / Pause / Reset
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.resetTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E355B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.5f)),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = GlassWhite
                    )
                }

                Button(
                    onClick = {
                        if (uiState.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRunning) FireOrange else IceCyanPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .width(160.dp)
                        .testTag("pomodoro_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = DarkNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isRunning) "PAUSE" else "START FOCUS",
                        color = DarkNavy,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Focus Analytics Card
        item {
            HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Focus Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        GlowPill(text = "+100 XP / Session", color = IceCyanPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusMetricBox(
                            label = "Today's Focus",
                            value = "${uiState.todayFocusMinutes} Mins",
                            accentColor = IceCyanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        FocusMetricBox(
                            label = "Sessions Done",
                            value = "${uiState.completedSessionsCount}",
                            accentColor = FrostBlueAccent,
                            modifier = Modifier.weight(1f)
                        )
                        FocusMetricBox(
                            label = "Weekly Hours",
                            value = "${uiState.weeklyFocusHours}h",
                            accentColor = PurpleArc,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) IceCyanPrimary.copy(alpha = 0.25f) else Color(0x33102447),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) IceCyanPrimary else Color(0x205CE1E6)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GlassWhite else GlassWhiteMuted,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FocusMetricBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x33132B4F),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GlassWhiteMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
        }
    }
}
