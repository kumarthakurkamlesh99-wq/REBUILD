package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import com.example.util.DateTimeUtils
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.TaskType
import com.example.data.scheduler.DynamicStudyScheduler
import com.example.data.scheduler.DynamicTaskScheduleItem
import com.example.data.scheduler.TaskDifficulty
import com.example.data.scheduler.UserEnergyLevel
import com.example.ui.components.RebuildDialog
import com.example.ui.components.RebuildSelectorChip
import com.example.ui.components.RebuildTextField
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.PlannerViewModel

@Composable
fun TasksScreen(
    viewModel: PlannerViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Physics", "Chemistry", "Biology", "English", "Hindi", "Workout", "General")

    // Map tasks to their dynamic schedule item metadata
    val dynamicMap = remember(state.dynamicScheduleItems) {
        state.dynamicScheduleItems.associateBy { it.task.id }
    }

    val filteredTasks = state.todayTasks.filter { task ->
        if (selectedFilter == "All") true else task.subject.equals(selectedFilter, ignoreCase = true)
    }

    val completedCount = state.todayTasks.count { it.isCompleted }
    val totalCount = state.todayTasks.size
    val progressPerc = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LuxuryCard)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dynamic Study Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IceCyanPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "AI CALIBRATED",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IceCyanPrimary
                            )
                        }
                    }
                    Text(
                        text = "$completedCount/$totalCount Done • $progressPerc% Progress • Dynamic Time Blocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Energy Level Check-in Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = FrostedNavyCard,
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Energy",
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Energy Level",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = state.energyLevel.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (state.energyLevel) {
                                UserEnergyLevel.HIGH -> SuccessGreen
                                UserEnergyLevel.MEDIUM -> LuxuryAccent
                                UserEnergyLevel.LOW -> WarningAmber
                            },
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserEnergyLevel.values().forEach { level ->
                            val isSelected = state.energyLevel == level
                            val activeColor = when (level) {
                                UserEnergyLevel.HIGH -> SuccessGreen
                                UserEnergyLevel.MEDIUM -> LuxuryAccent
                                UserEnergyLevel.LOW -> WarningAmber
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setEnergyLevel(level) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) activeColor.copy(alpha = 0.2f) else LuxuryCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) activeColor else GlassWhiteMuted.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = level.emoji,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = level.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = if (isSelected) GlassWhite else GlassWhiteMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // JARVIS AI Deep Work Predictor Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = LuxuryCard,
                border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "JARVIS AI",
                                tint = LuxuryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JARVIS Deep Work Window",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryAccent,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.optimizeWithJarvis() },
                            enabled = !state.isOptimizing,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            if (state.isOptimizing) {
                                CircularProgressIndicator(
                                    color = DarkNavy,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Re-Schedule",
                                    tint = DarkNavy,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Re-Schedule",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = state.deepWorkPrediction.optimalWindows.firstOrNull() ?: "05:30 PM - 07:45 PM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Peak Cognitive Alertness Window",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontSize = 10.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF142B47),
                            border = BorderStroke(0.5.dp, IceCyanPrimary)
                        ) {
                            Text(
                                text = "${state.deepWorkPrediction.confidencePercentage}% AI CONFIDENCE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = IceCyanPrimary
                            )
                        }
                    }

                    state.optimizationNotice?.let { notice ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = FrostBlueAccent,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Category Filter Chips
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedFilter == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LuxuryAccent else LuxuryCard,
                            border = BorderStroke(0.5.dp, if (isSelected) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { selectedFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkNavy else GlassWhite,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Task List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    val dynamicInfo = dynamicMap[task.id]
                    DynamicTaskItemRow(
                        task = task,
                        dynamicInfo = dynamicInfo,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onDelay = { mins -> viewModel.delayTask(task, mins) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = LuxuryAccent,
            contentColor = DarkNavy,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_task_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { subject, title, targetMins, details, remHour, remMin ->
                viewModel.addNewTask(
                    subject = subject,
                    title = title,
                    type = TaskType.CUSTOM,
                    targetMins = targetMins,
                    details = details,
                    reminderHour = remHour,
                    reminderMinute = remMin
                )
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun DynamicTaskItemRow(
    task: DailyPlanTaskEntity,
    dynamicInfo: DynamicTaskScheduleItem?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onDelay: (mins: Int) -> Unit
) {
    val difficulty = dynamicInfo?.difficulty ?: DynamicStudyScheduler.evaluateDifficulty(task)
    val timeSlot = dynamicInfo?.predictedTimeSlot

    val diffColor = when (difficulty) {
        TaskDifficulty.HARD -> WarningAmber
        TaskDifficulty.MEDIUM -> IceCyanPrimary
        TaskDifficulty.EASY -> SuccessGreen
    }

    val accentColor = when (task.subject) {
        "Physics" -> LuxuryAccent
        "Chemistry" -> WarningAmber
        "Biology" -> PurpleArc
        "Workout" -> FireOrange
        else -> FrostBlueAccent
    }

    val formattedTimeRange = remember(task.startTime, task.endTime, task.reminderHour, task.reminderMinute, task.targetMinutes, task.delayMinutes) {
        DateTimeUtils.formatTaskTimeRange(
            startTime = task.startTime,
            endTime = task.endTime,
            reminderHour = task.reminderHour,
            reminderMinute = task.reminderMinute,
            durationMinutes = task.targetMinutes,
            delayMinutes = task.delayMinutes
        )
    }.ifBlank { timeSlot ?: "${task.targetMinutes}m" }

    val isOverdue = remember(task.date, task.startTime, task.endTime, task.reminderHour, task.reminderMinute, task.targetMinutes, task.delayMinutes, task.isCompleted) {
        !task.isCompleted && DateTimeUtils.isTaskOverdue(
            taskDate = task.date,
            startTime = task.startTime,
            endTime = task.endTime,
            reminderHour = task.reminderHour,
            reminderMinute = task.reminderMinute,
            durationMinutes = task.targetMinutes,
            delayMinutes = task.delayMinutes
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        color = LuxuryCard,
        border = BorderStroke(
            1.dp,
            when {
                task.isCompleted -> SuccessGreen.copy(alpha = 0.5f)
                isOverdue -> WarningAmber.copy(alpha = 0.7f)
                task.isDelayed -> IceCyanPrimary.copy(alpha = 0.5f)
                else -> accentColor.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SuccessGreen,
                        checkmarkColor = DarkNavy,
                        uncheckedColor = GlassWhiteMuted
                    ),
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Top Meta: Subject, Difficulty badge, Time slot / Start-End range, Overdue badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = task.subject.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontSize = 9.sp
                            )
                        }

                        // Difficulty Tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = diffColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, diffColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = difficulty.label,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = diffColor,
                                fontSize = 8.sp
                            )
                        }

                        // Time Range Badge (12-hour format: e.g. 09:00 AM – 10:30 AM)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isOverdue) WarningAmber.copy(alpha = 0.2f) else IceCyanPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, if (isOverdue) WarningAmber.copy(alpha = 0.6f) else IceCyanPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time slot",
                                    tint = if (isOverdue) WarningAmber else IceCyanPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formattedTimeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverdue) WarningAmber else IceCyanPrimary,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Overdue or Delayed Badge
                        if (isOverdue) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WarningAmber.copy(alpha = 0.25f),
                                border = BorderStroke(0.5.dp, WarningAmber)
                            ) {
                                Text(
                                    text = "NOT PERFORMED",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = WarningAmber,
                                    fontSize = 8.sp
                                )
                            }
                        } else if (task.isDelayed) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FrostBlueAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "DELAYED (+${task.delayMinutes}m)",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostBlueAccent,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) GlassWhiteMuted else GlassWhite,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 14.sp
                    )

                    if (task.details.isNotBlank()) {
                        Text(
                            text = task.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }

                    // Dynamic Adjustment Reason
                    dynamicInfo?.adjustmentReason?.let { reason ->
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "• $reason",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = GlassWhiteMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Delay / Postpone Actions for Uncompleted or Overdue Tasks
            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF142B47),
                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onDelay(15) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = "Delay 15 mins",
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+15m Delay",
                                    color = IceCyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF142B47),
                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onDelay(30) }
                        ) {
                            Text(
                                text = "+30m",
                                color = GlassWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF142B47),
                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onDelay(60) }
                        ) {
                            Text(
                                text = "+1h",
                                color = GlassWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isOverdue) {
                        Text(
                            text = "Unperformed • Delay Now",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (subject: String, title: String, targetMins: Int, details: String, remHour: Int?, remMin: Int?) -> Unit
) {
    val subjects = listOf("Physics", "Chemistry", "Biology", "English", "Hindi", "Workout", "General")
    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var title by remember { mutableStateOf("") }
    var targetMinsStr by remember { mutableStateOf("45") }
    var details by remember { mutableStateOf("") }

    var enableReminder by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(17) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    RebuildDialog(
        onDismiss = onDismiss,
        title = "Add Dynamic Task",
        confirmButtonText = "Schedule Task",
        onConfirm = {
            val mins = targetMinsStr.toIntOrNull() ?: 45
            if (title.isNotBlank()) {
                onAdd(
                    selectedSubject,
                    title.trim(),
                    mins,
                    details.trim(),
                    if (enableReminder) reminderHour else null,
                    if (enableReminder) reminderMinute else null
                )
            }
        },
        dismissButtonText = "Cancel",
        headerAccentColor = LuxuryAccent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Subject",
                style = MaterialTheme.typography.labelSmall,
                color = GlassWhiteMuted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subjects.forEach { sub ->
                    RebuildSelectorChip(
                        text = sub,
                        isSelected = selectedSubject == sub,
                        onClick = { selectedSubject = sub }
                    )
                }
            }

            RebuildTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task Mission / Chapter",
                placeholder = "e.g. Electromagnetic Induction Numericals",
                testTag = "task_title_input"
            )

            RebuildTextField(
                value = details,
                onValueChange = { details = it },
                label = "Details / Focus Notes (Optional)",
                placeholder = "NCERT PYQ 2018-2024 problems",
                testTag = "task_details_input"
            )

            RebuildTextField(
                value = targetMinsStr,
                onValueChange = { targetMinsStr = it.filter { ch -> ch.isDigit() } },
                label = "Target Duration (Minutes)",
                placeholder = "45",
                testTag = "task_duration_input"
            )
        }
    }
}
