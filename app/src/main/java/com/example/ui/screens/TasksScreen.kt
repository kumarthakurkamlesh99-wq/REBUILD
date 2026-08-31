package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
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
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$completedCount/$totalCount Completed • $progressPerc% Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Category Filter Chips with Fade Edge
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkNavy else GlassWhite
                            )
                        }
                    }
                }

                // Left Fade Edge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(16.dp)
                        .height(36.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkNavy, Color.Transparent)
                            )
                        )
                )

                // Right Fade Edge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(20.dp)
                        .height(36.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, DarkNavy)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Task List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
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
fun TaskItemRow(
    task: DailyPlanTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = when (task.subject) {
        "Physics" -> LuxuryAccent
        "Chemistry" -> WarningAmber
        "Biology" -> PurpleArc
        "Workout" -> FireOrange
        else -> FrostBlueAccent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        color = LuxuryCard,
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) SuccessGreen.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${task.targetMinutes} min • +${task.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassWhiteMuted,
                        fontSize = 10.sp
                    )

                    if (task.reminderHour != null && task.reminderMinute != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IceCyanPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, IceCyanPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Reminder",
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%02d:%02d", task.reminderHour, task.reminderMinute),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceCyanPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) GlassWhiteMuted else GlassWhite,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = GlassWhiteMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, Int?, Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var durationMins by remember { mutableIntStateOf(45) }
    var enableReminder by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(16) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    val subjects = listOf("Physics", "Chemistry", "Biology", "English", "Hindi", "Workout", "General")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = LuxuryCard,
            border = BorderStroke(1.dp, LuxuryAccent)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Custom Task",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Wave Optics PYQs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject row
                Text(text = "Subject", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { s ->
                        val isSelected = subject == s
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) LuxuryAccent else Color(0x227C8CFF),
                            modifier = Modifier.clickable { subject = s }
                        ) {
                            Text(
                                text = s,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) DarkNavy else GlassWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details / Micro-steps") },
                    placeholder = { Text("Solve 10 questions from 2023 CBSE paper...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder alarm toggle & time selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22102A45))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarm",
                            tint = if (enableReminder) IceCyanPrimary else GlassWhiteMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set Reminder Alarm",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enableReminder) GlassWhite else GlassWhiteMuted
                        )
                    }

                    Checkbox(
                        checked = enableReminder,
                        onCheckedChange = { enableReminder = it },
                        colors = CheckboxDefaults.colors(checkedColor = IceCyanPrimary)
                    )
                }

                if (enableReminder) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time (24h):",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x337C8CFF),
                                modifier = Modifier.clickable {
                                    reminderHour = (reminderHour + 1) % 24
                                }
                            ) {
                                Text(
                                    text = String.format("%02d", reminderHour),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IceCyanPrimary
                                )
                            }

                            Text(":", color = GlassWhite, fontWeight = FontWeight.Bold)

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x337C8CFF),
                                modifier = Modifier.clickable {
                                    reminderMinute = (reminderMinute + 15) % 60
                                }
                            ) {
                                Text(
                                    text = String.format("%02d", reminderMinute),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IceCyanPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GlassWhiteMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(
                                    subject,
                                    title,
                                    durationMins,
                                    details,
                                    if (enableReminder) reminderHour else null,
                                    if (enableReminder) reminderMinute else null
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent)
                    ) {
                        Text("Add Task", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
