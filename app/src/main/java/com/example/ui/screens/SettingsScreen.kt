package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.AlarmScheduler
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.SettingsViewModel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nameInput by remember(uiState.userName) { mutableStateOf(uiState.userName) }
    var goalHoursInput by remember(uiState.dailyGoalHours) { mutableStateOf(uiState.dailyGoalHours) }

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
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Preferences, profile & configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Offline Engine Guarantee Card
        item {
            HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "100% Offline-First Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        GlowPill(text = "Zero Cloud", color = SuccessGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All study sessions, school status logs, habits, and exam configs are strictly stored locally on this device via Room Database and AlarmManager.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Notification & Toggles
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Notifications & Audio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )

                    SettingsToggleRow(
                        title = "Local Notifications",
                        subtitle = "Enable AlarmManager daily alerts",
                        icon = Icons.Default.Notifications,
                        checked = uiState.isNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )

                    SettingsToggleRow(
                        title = "Sound Effects",
                        subtitle = "Timer bells & study alarms",
                        icon = Icons.Default.VolumeUp,
                        checked = uiState.isSoundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) }
                    )

                    SettingsToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback on actions",
                        icon = Icons.Default.Vibration,
                        checked = uiState.isVibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) }
                    )
                }
            }
        }

        // Timetable Scheduled Notifications Breakdown
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automated Daily Alarms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )

                        Button(
                            onClick = { viewModel.resyncAlarms() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338E1FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IceCyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = IceCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resync", color = GlassWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AlarmScheduler.DEFAULT_DAILY_ALARMS.forEach { alarm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = FrostBlueAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = alarm.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = alarm.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            val timeStr = String.format("%02d:%02d", alarm.hour, alarm.minute)
                            GlowPill(text = timeStr, color = IceCyanPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FrostBlueAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkNavy,
                checkedTrackColor = IceCyanPrimary,
                uncheckedThumbColor = GlassWhiteMuted,
                uncheckedTrackColor = Color(0x331E355B)
            )
        )
    }
}
