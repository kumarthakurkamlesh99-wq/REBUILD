package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.UserProfileEntity
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import com.example.notification.ScheduledAlarmInfo
import com.example.ui.components.RebuildTopAppBar
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationCenterScreen(
    userProfile: UserProfileEntity?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val defaultProfile = userProfile ?: UserProfileEntity(name = "Student")
    val alarmsList = remember(userProfile) {
        AlarmScheduler.getProfileAlarmsList(defaultProfile)
    }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isRescheduling by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RebuildTopAppBar(
                title = "Notification Engine",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            isRescheduling = true
                            coroutineScope.launch {
                                AlarmScheduler.scheduleProfileAlarms(context, defaultProfile)
                                delay(400)
                                isRescheduling = false
                                testStatusMessage = "All ${alarmsList.count { it.isEnabled }} active alarms resynchronized!"
                            }
                        }
                    ) {
                        if (isRescheduling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = IceCyanPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Resync",
                                tint = IceCyanPrimary
                            )
                        }
                    }
                }
            )
        },
        containerColor = DarkNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Permission Status Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (hasNotificationPermission) Color(0x2200E676) else Color(0x33FFB300),
                    border = BorderStroke(1.dp, if (hasNotificationPermission) SuccessGreen else WarningAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (hasNotificationPermission) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasNotificationPermission) "System Notification Ready" else "Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Text(
                                text = if (hasNotificationPermission) "Local alarms & channels active (Survives reboot)" else "Grant notification permission for alarm alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (!hasNotificationPermission) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Grant", color = DarkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Diagnostics Header
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, Color(0x337C8CFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DIAGNOSTIC TEST CENTER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Test Trigger' next to any alarm to instantly simulate the notification payload.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )

                        testStatusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IceCyanPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IceCyanPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Alarms List Header
            item {
                Text(
                    text = "SCHEDULED PROTOCOL ALARMS (${alarmsList.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Items
            items(alarmsList) { alarm ->
                AlarmDiagnosticCard(
                    alarm = alarm,
                    onTestClick = {
                        AlarmScheduler.triggerTestNotification(context, alarm)
                        testStatusMessage = "Fired test alert: '${alarm.title}'"
                    }
                )
            }
        }
    }
}

@Composable
private fun AlarmDiagnosticCard(
    alarm: ScheduledAlarmInfo,
    onTestClick: () -> Unit
) {
    val icon = when (alarm.id) {
        AlarmScheduler.ID_WAKE_UP -> Icons.Default.WbSunny
        AlarmScheduler.ID_SCHOOL_DEPARTURE -> Icons.Default.School
        AlarmScheduler.ID_SCHOOL_ARRIVAL -> Icons.Default.School
        AlarmScheduler.ID_RETURN_HOME -> Icons.Default.Home
        AlarmScheduler.ID_STUDY_SESSION -> Icons.Default.MenuBook
        AlarmScheduler.ID_WORKOUT -> Icons.Default.FitnessCenter
        AlarmScheduler.ID_REVISION -> Icons.Default.Timer
        AlarmScheduler.ID_REFLECTION -> Icons.Default.SelfImprovement
        AlarmScheduler.ID_SLEEP -> Icons.Default.NightlightRound
        else -> Icons.Default.Alarm
    }

    val iconColor = when (alarm.id) {
        AlarmScheduler.ID_WAKE_UP -> WarningAmber
        AlarmScheduler.ID_SCHOOL_DEPARTURE -> LuxuryAccent
        AlarmScheduler.ID_SCHOOL_ARRIVAL -> LuxuryAccent
        AlarmScheduler.ID_RETURN_HOME -> IceCyanPrimary
        AlarmScheduler.ID_STUDY_SESSION -> FrostBlueAccent
        AlarmScheduler.ID_WORKOUT -> FireOrange
        AlarmScheduler.ID_REVISION -> PurpleArc
        AlarmScheduler.ID_REFLECTION -> SuccessGreen
        AlarmScheduler.ID_SLEEP -> Color(0xFF70B8FF)
        else -> IceCyanPrimary
    }

    val timeFormatted = String.format("%02d:%02d", alarm.hour, alarm.minute)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = FrostedNavyCard,
        border = BorderStroke(1.dp, if (alarm.isEnabled) iconColor.copy(alpha = 0.4f) else GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = iconColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, iconColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = alarm.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (alarm.isEnabled) SuccessGreen.copy(alpha = 0.2f) else Color(0x33777777)
                        ) {
                            Text(
                                text = if (alarm.isEnabled) "ACTIVE" else "MUTED",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alarm.isEnabled) SuccessGreen else GlassWhiteMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Firing daily at $timeFormatted (${alarm.category})",
                        style = MaterialTheme.typography.bodySmall,
                        color = iconColor,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = onTestClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, iconColor.copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("test_alarm_${alarm.id}")
                ) {
                    Text(
                        text = "Test Trigger",
                        color = iconColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x22050B1B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = alarm.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
