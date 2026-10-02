package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.*
import com.example.notification.AlarmScheduler
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.components.RebuildTopAppBar
import com.example.ui.components.RebuildDialog
import com.example.ui.components.RebuildSelectorChip
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userProfile: UserProfileEntity?,
    onOpenDrawer: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfileSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val planImportPreview by viewModel.planImportPreview.collectAsStateWithLifecycle()
    val validationErrors by viewModel.validationErrors.collectAsStateWithLifecycle()
    val importReport by viewModel.importReport.collectAsStateWithLifecycle()
    val importError by viewModel.importError.collectAsStateWithLifecycle()
    val importSuccess by viewModel.importSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.parsePlanFile(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        RebuildTopAppBar(
            title = "Settings",
            onMenuClick = onOpenDrawer,
            subtitle = "Preferences, profile & system calibrations"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Calibration Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProfileSettings() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = IceCyanPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = IceCyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = userProfile?.name ?: "Student Profile",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = "${userProfile?.studentClass ?: "Class 12"} • ${userProfile?.stream ?: "Science PCM"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF131D38),
                                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "TAP TO EDIT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceCyanPrimary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Target: ${userProfile?.targetExamName ?: "Board Exam"} • ${userProfile?.targetPercentage ?: 95}% Target • Wake ${userProfile?.wakeUpTime ?: "06:00"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Notification Engine Diagnostics Hub Link
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FrostedNavyCard,
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToNotifications() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Testing Hub",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Text(
                                text = "Inspect & trigger all 9 background alarms instantly",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Offline Architecture Card
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
                            GlowPill(text = "Local Storage", color = SuccessGreen)
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

            // Notification & Audio Toggles
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

            // Timetable Scheduled Notifications Breakdown removed to support zero-hardcoded alarms constraint

            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleSmall,
                    color = IceCyanPrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { filePickerLauncher.launch("application/json") }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Import",
                                    tint = FrostBlueAccent
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Import Plan",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = "Load external JSON configuration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = GlassWhiteMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Restore Backup Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.restoreBackup() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Restore",
                                    tint = WarningAmber
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Restore Previous Plan",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassWhite
                                    )
                                    Text(
                                        text = "Revert to auto-backup state",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val restoreSuccess by viewModel.restoreSuccess.collectAsStateWithLifecycle()
    if (restoreSuccess) {
        RebuildDialog(
            onDismiss = { viewModel.dismissRestoreSuccess() },
            title = "Restore Successful",
            subtitle = "Your previous plan has been restored.",
            icon = Icons.Default.Sync,
            iconTint = SuccessGreen,
            headerAccentColor = SuccessGreen,
            confirmButtonText = "Done",
            onConfirm = { viewModel.dismissRestoreSuccess() }
        ) {
            Text("Your goals, tasks, habits and alarms have been successfully reverted.", color = GlassWhite, fontSize = 14.sp)
        }
    }

    // Import Modals

    // 1. Import Preview Screen
    planImportPreview?.let { plan ->
        var replaceMode by androidx.compose.runtime.mutableStateOf(true)
        
        RebuildDialog(
            onDismiss = { viewModel.cancelImport() },
            title = "Preview Plan Import",
            subtitle = "Review plan details before applying to your database.",
            icon = Icons.Default.Sync,
            iconTint = IceCyanPrimary,
            headerAccentColor = IceCyanPrimary,
            confirmButtonText = "Import Plan",
            dismissButtonText = "Cancel",
            onConfirm = { viewModel.confirmImport(replaceMode) },
            testTag = "import_plan_dialog"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Plan Name", color = GlassWhiteMuted, fontSize = 13.sp)
                            Text(
                                text = plan.planName ?: "Imported Plan",
                                color = GlassWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(GlassBorder.copy(alpha = 0.3f))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Goals Count", color = GlassWhiteMuted, fontSize = 13.sp)
                            Text(
                                text = "${plan.goals?.size ?: 0}",
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tasks Count", color = GlassWhiteMuted, fontSize = 13.sp)
                            Text(
                                text = "${plan.tasks?.size ?: 0}",
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if ((plan.schedule?.size ?: 0) > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Schedule Slots", color = GlassWhiteMuted, fontSize = 13.sp)
                                Text(
                                    text = "${plan.schedule?.size ?: 0}",
                                    color = IceCyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Habits Count", color = GlassWhiteMuted, fontSize = 13.sp)
                            Text(
                                text = "${plan.habits?.size ?: 0}",
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Alarms Count", color = GlassWhiteMuted, fontSize = 13.sp)
                            Text(
                                text = "${plan.alarms?.size ?: 0}",
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text("Import Mode", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RebuildSelectorChip(
                        text = "Replace Existing",
                        isSelected = replaceMode,
                        onClick = { replaceMode = true },
                        selectedColor = WarningAmber
                    )
                    RebuildSelectorChip(
                        text = "Merge",
                        isSelected = !replaceMode,
                        onClick = { replaceMode = false },
                        selectedColor = SuccessGreen
                    )
                }
            }
        }
    }

    // 2. Validation Errors Dialog (Human-readable, all issues at once, developer technical details toggle)
    if (validationErrors.isNotEmpty()) {
        var showTechDetails by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        RebuildDialog(
            onDismiss = { viewModel.dismissValidationErrors() },
            title = "Plan Validation Issues",
            subtitle = "Found ${validationErrors.size} issue${if (validationErrors.size > 1) "s" else ""} in the plan file.",
            icon = Icons.Default.Warning,
            iconTint = WarningAmber,
            headerAccentColor = WarningAmber,
            confirmButtonText = "Dismiss",
            onConfirm = { viewModel.dismissValidationErrors() }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Found ${validationErrors.size} issue${if (validationErrors.size > 1) "s" else ""}:",
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        for (err in validationErrors) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(err.summaryBullet, color = GlassWhite, fontSize = 13.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(GlassBorder.copy(alpha = 0.3f))
                        )

                        Text(
                            text = if (validationErrors.size == 1) {
                                validationErrors.first().friendlyMessage
                            } else {
                                "Please provide the required fields for these items and try again."
                            },
                            color = GlassWhiteMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Debug Mode: Show Technical Details for developers
                TextButton(
                    onClick = { showTechDetails = !showTechDetails },
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showTechDetails) "Hide Technical Details" else "Show Technical Details",
                            color = IceCyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showTechDetails) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Technical Details (Developer Mode):",
                                color = IceCyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            for (err in validationErrors) {
                                Text(
                                    text = err.technicalDetails,
                                    color = GlassWhiteMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Import Report Dialog (Requirement 7)
    importReport?.let { report ->
        RebuildDialog(
            onDismiss = { viewModel.dismissImportReport() },
            title = "Plan Applied Successfully",
            subtitle = "Your plan components have been saved.",
            icon = Icons.Default.CheckCircle,
            iconTint = SuccessGreen,
            headerAccentColor = SuccessGreen,
            confirmButtonText = "Done",
            onConfirm = { viewModel.dismissImportReport() }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Plan: ${report.planName}",
                            color = GlassWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(GlassBorder.copy(alpha = 0.3f))
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${report.goalsCount} Goals Imported", color = GlassWhite, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${report.tasksCount} Tasks Imported", color = GlassWhite, fontSize = 14.sp)
                        }
                        if (report.scheduleCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${report.scheduleCount} Schedule Blocks Imported", color = GlassWhite, fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${report.habitsCount} Habits Imported", color = GlassWhite, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${report.alarmsCount} Alarms Imported", color = GlassWhite, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Plan applied successfully.",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
    
    // Generic import failure dialog (if not validation error)
    if (importError != null && validationErrors.isEmpty() && importReport == null) {
        RebuildDialog(
            onDismiss = { viewModel.dismissError() },
            title = "Import Failed",
            subtitle = "The JSON configuration could not be processed.",
            icon = Icons.Default.Warning,
            iconTint = WarningAmber,
            headerAccentColor = WarningAmber,
            confirmButtonText = "Dismiss",
            onConfirm = { viewModel.dismissError() }
        ) {
            Text(importError ?: "Unknown error", color = GlassWhite, fontSize = 14.sp)
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
