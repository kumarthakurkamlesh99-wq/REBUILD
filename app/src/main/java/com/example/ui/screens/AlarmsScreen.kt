package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AlarmChallengeType
import com.example.data.local.entity.AlarmDifficulty
import com.example.data.local.entity.AlarmEntity
import com.example.notification.AlarmDismissActivity
import com.example.notification.AlarmScheduler
import com.example.ui.components.RebuildTopAppBar
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
import com.example.viewmodel.AlarmsViewModel

@Composable
fun AlarmsScreen(
    viewModel: AlarmsViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission tracking states for Android 12+ & Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(AlarmScheduler.hasNotificationPermission(context))
    }
    var canScheduleExactAlarms by remember {
        mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context))
    }

    // Refresh permissions on resume / interaction
    DisposableEffect(Unit) {
        hasNotificationPermission = AlarmScheduler.hasNotificationPermission(context)
        canScheduleExactAlarms = AlarmScheduler.canScheduleExactAlarms(context)
        onDispose { }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            RebuildTopAppBar(
                title = "Smart Alarm Engine",
                subtitle = "Cognitive & Physical Challenge Dismissal • Anti-Slumber",
                onMenuClick = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Permission Warning Card for Exact Alarms (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FireOrange.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, FireOrange)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = FireOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Exact Alarm Permission Required",
                                        fontWeight = FontWeight.Bold,
                                        color = FireOrange,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Android 12+ requires explicit permission to trigger exact wake-up alarms on time when the device is locked or asleep.",
                                    fontSize = 12.sp,
                                    color = GlassWhiteMuted
                                )
                                Button(
                                    onClick = { AlarmScheduler.openExactAlarmSettings(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FireOrange,
                                        contentColor = DarkNavy
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Grant Exact Alarm Permission", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Permission Warning Card for Notifications (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, WarningAmber)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Notification Permission Required",
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Android 13+ requires notification permissions to alert you and trigger the foreground ringing service.",
                                    fontSize = 12.sp,
                                    color = GlassWhiteMuted
                                )
                                Button(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarningAmber,
                                        contentColor = DarkNavy
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Allow Notifications", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Challenge Testing Hero Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Anti-Slumber Protocol Verification",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassWhite
                                )
                            }

                            Text(
                                text = "Traditional swipe to dismiss is disabled. Experience and test challenges immediately:",
                                fontSize = 12.sp,
                                color = GlassWhiteMuted
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        launchTestAlarmChallenge(
                                            context = context,
                                            type = AlarmChallengeType.MATH,
                                            difficulty = AlarmDifficulty.MEDIUM
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricBlue,
                                        contentColor = DarkNavy
                                    )
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Math", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        launchTestAlarmChallenge(
                                            context = context,
                                            type = AlarmChallengeType.CAPTCHA,
                                            difficulty = AlarmDifficulty.EASY
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IceCyanPrimary,
                                        contentColor = DarkNavy
                                    )
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Captcha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        launchTestAlarmChallenge(
                                            context = context,
                                            type = AlarmChallengeType.PHYSICAL_SHAKE,
                                            difficulty = AlarmDifficulty.MEDIUM
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FireOrange,
                                        contentColor = DarkNavy
                                    )
                                ) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Shake", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Header for Alarms List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE PROTOCOL ALARMS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostBlueAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${uiState.alarms.count { it.isEnabled }} Enabled",
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Alarms List Items
                items(uiState.alarms, key = { "alarm_${it.id}" }) { alarm ->
                    AlarmCardItem(
                        alarm = alarm,
                        onToggle = {
                            if (!alarm.isEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !AlarmScheduler.hasNotificationPermission(context)) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !AlarmScheduler.canScheduleExactAlarms(context)) {
                                    AlarmScheduler.openExactAlarmSettings(context)
                                }
                            }
                            viewModel.toggleAlarm(alarm)
                        },
                        onEdit = { viewModel.openEditDialog(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onTestTrigger = {
                            launchTestAlarmChallenge(
                                context = context,
                                type = alarm.challengeType,
                                difficulty = alarm.challengeDifficulty
                            )
                        }
                    )
                }

                // Dismissal History Logs
                if (uiState.alarmLogs.isNotEmpty()) {
                    item {
                        Text(
                            text = "RECENT ALARM DISMISSAL LOGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostBlueAccent,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.alarmLogs.take(5), key = { "log_${it.id}_${it.timestamp}" }) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B1424))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.alarmTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassWhite
                                )
                                Text(
                                    text = "${log.date} • Triggered: ${log.triggeredTime} • Solved: ${log.dismissedTime}",
                                    fontSize = 11.sp,
                                    color = GlassWhiteMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${log.snoozesUsed} Snoozes",
                                    fontSize = 11.sp,
                                    color = if (log.snoozesUsed == 0) SuccessGreen else WarningAmber
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB to create new alarm
        FloatingActionButton(
            onClick = { viewModel.openCreateDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_alarm_fab"),
            containerColor = ElectricBlue,
            contentColor = DarkNavy
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm")
        }
    }

    // Add / Edit Alarm Dialog
    if (uiState.isCreatingOrEditing) {
        AlarmEditDialog(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
fun AlarmCardItem(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestTrigger: () -> Unit
) {
    val hour12 = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
    val amPm = if (alarm.hour >= 12) "PM" else "AM"
    val timeFormatted = String.format("%02d:%02d", hour12, alarm.minute)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) FrostedNavyCard else Color(0xFF0A101D)
        ),
        border = BorderStroke(
            1.dp,
            if (alarm.isEnabled) ElectricBlue.copy(alpha = 0.35f) else FrostBlueAccent.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (alarm.isEnabled) IceCyanPrimary else GlassWhiteMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = amPm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) FrostBlueAccent else GlassWhiteMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Text(
                        text = alarm.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) GlassWhite else GlassWhiteMuted
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkNavy,
                        checkedTrackColor = IceCyanPrimary,
                        uncheckedThumbColor = GlassWhiteMuted,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alarm attributes row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Challenge: ${alarm.challengeType.label} (${alarm.challengeDifficulty.label})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF14223D))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Vol ${alarm.volumePercent}%",
                            fontSize = 11.sp,
                            color = FrostBlueAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTestTrigger, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Trigger",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Alarm",
                            tint = FrostBlueAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Alarm",
                            tint = FireOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmEditDialog(
    uiState: com.example.viewmodel.AlarmsUiState,
    viewModel: AlarmsViewModel
) {
    val context = LocalContext.current
    var challengeMenuExpanded by remember { mutableStateOf(false) }
    var diffMenuExpanded by remember { mutableStateOf(false) }
    var soundMenuExpanded by remember { mutableStateOf(false) }
    var snoozeSoundMenuExpanded by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                viewModel.setInputRingtonePreset(uri.toString())
            }
        }
    }

    val snoozeRingtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                viewModel.setInputSnoozeRingtonePreset(uri.toString())
            }
        }
    }

    val presetRingtones = listOf(
        "CYBER_SIREN" to "Cyber Siren (High Alert)",
        "APEX_HORNS" to "Apex Horns (Deep Impact)",
        "ZEN_CHIME" to "Zen Chime (Gentle Awakening)",
        "BELL" to "Classic Metal Bell",
        "MILITARY" to "Military Bugle Reveille",
        "SYSTEM_DEFAULT" to "System Alarm Default"
    )

    val presetSnoozeSounds = listOf(
        "TICK_TOCK" to "Tick Tock Warning",
        "BELL" to "Single Soft Bell",
        "ZEN_CHIME" to "Zen Ripple",
        "SYSTEM_DEFAULT" to "System Notification Default"
    )

    Dialog(onDismissRequest = { viewModel.dismissDialog() }) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.editingAlarm == null) "New Protocol Alarm" else "Edit Alarm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary
                    )
                    IconButton(onClick = { viewModel.dismissDialog() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GlassWhite)
                    }
                }

                // Title
                OutlinedTextField(
                    value = uiState.inputTitle,
                    onValueChange = { viewModel.setInputTitle(it) },
                    label = { Text("Alarm Label / Protocol", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Pickers (Hour & Minute)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hour (0-23)", fontSize = 12.sp, color = GlassWhiteMuted)
                        OutlinedTextField(
                            value = "${uiState.inputHour}",
                            onValueChange = {
                                val h = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                                viewModel.setInputTime(h, uiState.inputMinute)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                                focusedContainerColor = DarkNavy,
                                unfocusedContainerColor = DarkNavy
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Minute (0-59)", fontSize = 12.sp, color = GlassWhiteMuted)
                        OutlinedTextField(
                            value = "${uiState.inputMinute}",
                            onValueChange = {
                                val m = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                viewModel.setInputTime(uiState.inputHour, m)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                                focusedContainerColor = DarkNavy,
                                unfocusedContainerColor = DarkNavy
                            )
                        )
                    }
                }

                // Sound / Ringtone Selection
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alarm Sound & Ringtone", fontSize = 12.sp, color = GlassWhiteMuted)
                        if (uiState.isPreviewingSound) {
                            Text(
                                text = "Playing Preview...",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavy)
                                .border(1.dp, FrostBlueAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { soundMenuExpanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val displaySoundName = presetRingtones.find { it.first == uiState.inputRingtonePreset }?.second
                                    ?: if (uiState.inputRingtonePreset.startsWith("content://")) "Custom Android Ringtone" else uiState.inputRingtonePreset
                                Text(
                                    text = displaySoundName,
                                    color = GlassWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text("Change ▼", fontSize = 11.sp, color = FrostBlueAccent)
                        }

                        DropdownMenu(
                            expanded = soundMenuExpanded,
                            onDismissRequest = { soundMenuExpanded = false },
                            modifier = Modifier.background(FrostedNavyCard)
                        ) {
                            presetRingtones.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = GlassWhite, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setInputRingtonePreset(key)
                                        soundMenuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Choose From Android Ringtones...", color = IceCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                onClick = {
                                    soundMenuExpanded = false
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    }
                                    ringtonePickerLauncher.launch(intent)
                                }
                            )
                        }
                    }

                    // Sound Preview Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isPreviewingSound) {
                            OutlinedButton(
                                onClick = { viewModel.stopSoundPreview() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FireOrange)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Sound", fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.previewSound(context, uiState.inputRingtonePreset) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = IceCyanPrimary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Alarm Sound", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Snooze Sound Selection
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Snooze Sound / Tone", fontSize = 12.sp, color = GlassWhiteMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavy)
                                .border(1.dp, FrostBlueAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { snoozeSoundMenuExpanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val displaySnoozeName = presetSnoozeSounds.find { it.first == uiState.inputSnoozeRingtonePreset }?.second
                                    ?: viewModel.getRingtoneDisplayName(context, uiState.inputSnoozeRingtonePreset)
                                Text(
                                    text = displaySnoozeName,
                                    color = GlassWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text("Change ▼", fontSize = 11.sp, color = FrostBlueAccent)
                        }

                        DropdownMenu(
                            expanded = snoozeSoundMenuExpanded,
                            onDismissRequest = { snoozeSoundMenuExpanded = false },
                            modifier = Modifier.background(FrostedNavyCard)
                        ) {
                            presetSnoozeSounds.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = GlassWhite, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setInputSnoozeRingtonePreset(key)
                                        snoozeSoundMenuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Choose Custom Snooze Tone...", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                onClick = {
                                    snoozeSoundMenuExpanded = false
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_ALARM)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Snooze Tone")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    }
                                    snoozeRingtonePickerLauncher.launch(intent)
                                }
                            )
                        }
                    }

                    // Snooze Sound Preview Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previewSound(context, uiState.inputSnoozeRingtonePreset) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Snooze Sound", fontSize = 11.sp)
                        }
                    }
                }

                // Challenge Selection
                Column {
                    Text("Dismissal Challenge Type", fontSize = 12.sp, color = GlassWhiteMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavy)
                                .border(1.dp, FrostBlueAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { challengeMenuExpanded = true }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.inputChallengeType.label,
                                color = GlassWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Change ▼", fontSize = 11.sp, color = FrostBlueAccent)
                        }

                        DropdownMenu(
                            expanded = challengeMenuExpanded,
                            onDismissRequest = { challengeMenuExpanded = false },
                            modifier = Modifier.background(FrostedNavyCard)
                        ) {
                            AlarmChallengeType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label, color = GlassWhite) },
                                    onClick = {
                                        viewModel.setInputChallengeType(type)
                                        challengeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Difficulty Selection
                Column {
                    Text("Challenge Difficulty", fontSize = 12.sp, color = GlassWhiteMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavy)
                                .border(1.dp, FrostBlueAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { diffMenuExpanded = true }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.inputDifficulty.label,
                                color = GlassWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Change ▼", fontSize = 11.sp, color = FrostBlueAccent)
                        }

                        DropdownMenu(
                            expanded = diffMenuExpanded,
                            onDismissRequest = { diffMenuExpanded = false },
                            modifier = Modifier.background(FrostedNavyCard)
                        ) {
                            AlarmDifficulty.values().forEach { diff ->
                                DropdownMenuItem(
                                    text = { Text(diff.label, color = GlassWhite) },
                                    onClick = {
                                        viewModel.setInputDifficulty(diff)
                                        diffMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Snooze Settings (Max Snoozes & Duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max Snoozes", fontSize = 12.sp, color = GlassWhiteMuted)
                        OutlinedTextField(
                            value = "${uiState.inputMaxSnoozes}",
                            onValueChange = {
                                val s = it.toIntOrNull()?.coerceIn(0, 10) ?: 0
                                viewModel.setInputMaxSnoozes(s)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                                focusedContainerColor = DarkNavy,
                                unfocusedContainerColor = DarkNavy
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Snooze (Mins)", fontSize = 12.sp, color = GlassWhiteMuted)
                        OutlinedTextField(
                            value = "${uiState.inputSnoozeDuration}",
                            onValueChange = {
                                val d = it.toIntOrNull()?.coerceIn(1, 30) ?: 5
                                viewModel.setInputSnoozeDuration(d)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassWhite,
                                unfocusedTextColor = GlassWhite,
                                focusedBorderColor = IceCyanPrimary,
                                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.4f),
                                focusedContainerColor = DarkNavy,
                                unfocusedContainerColor = DarkNavy
                            )
                        )
                    }
                }

                // Volume Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alarm Volume", fontSize = 12.sp, color = GlassWhiteMuted)
                        Text("${uiState.inputVolume}%", fontSize = 12.sp, color = IceCyanPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = uiState.inputVolume.toFloat(),
                        onValueChange = { viewModel.setInputVolume(it.toInt()) },
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = IceCyanPrimary,
                            activeTrackColor = ElectricBlue
                        )
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.dismissDialog() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = GlassWhiteMuted)
                    }

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !AlarmScheduler.hasNotificationPermission(context)) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !AlarmScheduler.canScheduleExactAlarms(context)) {
                                AlarmScheduler.openExactAlarmSettings(context)
                            }
                            viewModel.saveAlarm()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = DarkNavy
                        )
                    ) {
                        Text("Save Alarm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun launchTestAlarmChallenge(
    context: Context,
    type: AlarmChallengeType,
    difficulty: AlarmDifficulty
) {
    val intent = Intent(context, AlarmDismissActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(AlarmDismissActivity.EXTRA_ALARM_ID, 999L)
        putExtra(AlarmDismissActivity.EXTRA_ALARM_TITLE, "Test Awakening Protocol")
        putExtra(AlarmDismissActivity.EXTRA_CHALLENGE_TYPE, type.name)
        putExtra(AlarmDismissActivity.EXTRA_DIFFICULTY, difficulty.name)
        putExtra(AlarmDismissActivity.EXTRA_VOLUME, 80)
        putExtra(AlarmDismissActivity.EXTRA_VIBRATE, true)
        putExtra(AlarmDismissActivity.EXTRA_MAX_SNOOZES, 3)
        putExtra(AlarmDismissActivity.EXTRA_SNOOZE_DURATION, 5)
    }
    context.startActivity(intent)
}
