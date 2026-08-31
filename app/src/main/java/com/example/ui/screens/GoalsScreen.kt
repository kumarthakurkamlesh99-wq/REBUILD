package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.StatBadge
import com.example.ui.theme.DarkNavy
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
import com.example.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var isCompletedExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
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
                        text = "Apex Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${uiState.completedCount}/${uiState.totalCount} Accomplished • ${uiState.overallProgress}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                GlowPill(
                    text = "TARGETS",
                    glowColor = IceCyanPrimary,
                    textColor = DarkNavy,
                    backgroundColor = IceCyanPrimary
                )
            }

            // Summary Telemetry Card
            PaddingBox(horizontal = 16.dp, vertical = 6.dp) {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatBadge(
                                label = "TOTAL",
                                value = "${uiState.totalCount}",
                                valueColor = GlassWhite
                            )
                            StatBadge(
                                label = "ACTIVE",
                                value = "${uiState.activeCount}",
                                valueColor = IceCyanPrimary
                            )
                            StatBadge(
                                label = "COMPLETED",
                                value = "${uiState.completedCount}",
                                valueColor = SuccessGreen
                            )
                            StatBadge(
                                label = "AVG VELOCITY",
                                value = "${uiState.overallProgress}%",
                                valueColor = LuxuryAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (uiState.overallProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = IceCyanPrimary,
                            trackColor = Color(0x331F3A60),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Category Filter Chips with Fade Edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected = uiState.selectedCategory == null
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (allSelected) LuxuryAccent else LuxuryCard,
                        border = BorderStroke(
                            0.5.dp,
                            if (allSelected) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.clickable { viewModel.setCategoryFilter(null) }
                    ) {
                        Text(
                            text = "All Goals (${uiState.allGoals.size})",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (allSelected) DarkNavy else GlassWhite
                        )
                    }

                    GoalCategory.values().forEach { category ->
                        val isSelected = uiState.selectedCategory == category
                        val catCount = uiState.allGoals.count { it.category == category }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LuxuryAccent else LuxuryCard,
                            border = BorderStroke(
                                0.5.dp,
                                if (isSelected) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { viewModel.setCategoryFilter(category) }
                        ) {
                            Text(
                                text = "${category.name.lowercase().replaceFirstChar { it.uppercase() }} ($catCount)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkNavy else GlassWhite
                            )
                        }
                    }
                }

                // Subtle right-side fade gradient cue
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(width = 24.dp, height = 36.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, DarkNavy)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Goals List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.activeGoals.isEmpty() && uiState.completedGoals.isEmpty()) {
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Goals Established Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GlassWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Define targets for Board Exams, Fitness, or Monk Mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showAddGoalDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = DarkNavy)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Set Your First Goal", color = DarkNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Active Goals Section Header
                    if (uiState.activeGoals.isNotEmpty()) {
                        item {
                            Text(
                                text = "ACTIVE OBJECTIVES (${uiState.activeGoals.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = IceCyanPrimary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(uiState.activeGoals, key = { it.id }) { goal ->
                            GoalCardItem(
                                goal = goal,
                                onToggle = { viewModel.toggleGoal(goal) },
                                onProgressChange = { p -> viewModel.updateProgress(goal, p) },
                                onDelete = { viewModel.deleteGoal(goal) }
                            )
                        }
                    }

                    // Completed Goals Section
                    if (uiState.completedGoals.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCompletedExpanded = !isCompletedExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMPLETED ACHIEVEMENTS (${uiState.completedGoals.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SuccessGreen,
                                    letterSpacing = 1.2.sp
                                )
                                Icon(
                                    imageVector = if (isCompletedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand/Collapse",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (isCompletedExpanded) {
                            items(uiState.completedGoals, key = { it.id }) { goal ->
                                GoalCardItem(
                                    goal = goal,
                                    onToggle = { viewModel.toggleGoal(goal) },
                                    onProgressChange = { p -> viewModel.updateProgress(goal, p) },
                                    onDelete = { viewModel.deleteGoal(goal) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddGoalDialog = true },
            containerColor = LuxuryAccent,
            contentColor = DarkNavy,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_goal_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Goal",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAdd = { title, desc, cat, date, reminder, hour, min ->
                viewModel.addGoal(title, desc, cat, date, reminder, hour, min)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
private fun PaddingBox(
    horizontal: androidx.compose.ui.unit.Dp,
    vertical: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = horizontal, vertical = vertical)) {
        content()
    }
}

@Composable
fun GoalCardItem(
    goal: GoalEntity,
    onToggle: () -> Unit,
    onProgressChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (goal.category) {
        GoalCategory.ACADEMIC -> IceCyanPrimary
        GoalCategory.FITNESS -> FireOrange
        GoalCategory.PERSONAL -> FrostBlueAccent
        GoalCategory.DISCIPLINE -> PurpleArc
    }

    val categoryIcon = when (goal.category) {
        GoalCategory.ACADEMIC -> Icons.Default.School
        GoalCategory.FITNESS -> Icons.Default.FitnessCenter
        GoalCategory.PERSONAL -> Icons.Default.Star
        GoalCategory.DISCIPLINE -> Icons.Default.Psychology
    }

    val daysText = remember(goal.targetDate) {
        if (goal.targetDate.isNullOrBlank()) {
            "Open-Ended"
        } else {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val target = format.parse(goal.targetDate)
                val today = format.parse(format.format(Date()))
                if (target != null && today != null) {
                    val diff = target.time - today.time
                    val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    if (days > 0) "$days days left" else if (days == 0L) "Target Today" else "${-days}d overdue"
                } else {
                    goal.targetDate
                }
            } catch (e: Exception) {
                goal.targetDate
            }
        }
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox toggle
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (goal.isCompleted) "Completed" else "Incomplete",
                        tint = if (goal.isCompleted) SuccessGreen else GlassWhiteMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, categoryColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = goal.category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor
                                )
                            }
                        }

                        // Target Date Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x331E3A5F),
                            border = BorderStroke(0.5.dp, FrostBlueAccent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = FrostBlueAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = daysText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = FrostBlueAccent
                                )
                            }
                        }

                        // Reminder Pill
                        if (goal.reminderEnabled && goal.reminderHour != null && goal.reminderMinute != null) {
                            val timeStr = String.format(
                                Locale.getDefault(),
                                "%02d:%02d %s",
                                if (goal.reminderHour % 12 == 0) 12 else goal.reminderHour % 12,
                                goal.reminderMinute,
                                if (goal.reminderHour >= 12) "PM" else "AM"
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LuxuryAccent.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, LuxuryAccent.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = LuxuryAccent,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = LuxuryAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) GlassWhiteMuted else GlassWhite,
                        fontSize = 14.sp
                    )

                    if (goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = GlassWhiteMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Quick Adjusters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stepper down
                Surface(
                    shape = CircleShape,
                    color = Color(0x337C8CFF),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onProgressChange(goal.progressPercentage - 10) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "-10", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = GlassWhite)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = GlassWhiteMuted
                        )
                        Text(
                            text = "${goal.progressPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (goal.progressPercentage >= 100) SuccessGreen else IceCyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (goal.progressPercentage >= 100) SuccessGreen else categoryColor,
                        trackColor = Color(0x221E3A5F),
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stepper up
                Surface(
                    shape = CircleShape,
                    color = Color(0x337C8CFF),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onProgressChange(goal.progressPercentage + 10) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "+10", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = GlassWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        description: String,
        category: GoalCategory,
        targetDate: String?,
        reminderEnabled: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.ACADEMIC) }
    var targetDate by remember { mutableStateOf("2026-02-15") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderHour by remember { mutableIntStateOf(7) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = LuxuryCard,
            border = BorderStroke(1.dp, LuxuryAccent)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Set Apex Target Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Class 12 Boards 95%+, 50 Pushups") },
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

                // Category selector
                Text(text = "Category", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoalCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) LuxuryAccent else Color(0x227C8CFF),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) DarkNavy else GlassWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Key Milestones / Notes") },
                    placeholder = { Text("Master 14 Physics chapters with NCERT pyqs...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Date
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Deadline (yyyy-MM-dd)") },
                    placeholder = { Text("2026-02-15") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Reminder Alarm Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Reminder Alarm",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = "Receive scheduled notification",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkNavy,
                            checkedTrackColor = LuxuryAccent
                        )
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reminder Time: ${String.format(Locale.getDefault(), "%02d:%02d %s", if (reminderHour % 12 == 0) 12 else reminderHour % 12, reminderMinute, if (reminderHour >= 12) "PM" else "AM")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair(6, 0), Pair(7, 0), Pair(8, 30),
                            Pair(13, 0), Pair(17, 30), Pair(20, 0), Pair(22, 0)
                        ).forEach { (h, m) ->
                            val isSelected = reminderHour == h && reminderMinute == m
                            val label = String.format(
                                Locale.getDefault(),
                                "%02d:%02d %s",
                                if (h % 12 == 0) 12 else h % 12,
                                m,
                                if (h >= 12) "PM" else "AM"
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) LuxuryAccent else Color(0x227C8CFF),
                                modifier = Modifier.clickable {
                                    reminderHour = h
                                    reminderMinute = m
                                }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isSelected) DarkNavy else GlassWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                                    title,
                                    description,
                                    selectedCategory,
                                    targetDate.ifBlank { null },
                                    reminderEnabled,
                                    if (reminderEnabled) reminderHour else null,
                                    if (reminderEnabled) reminderMinute else null
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent)
                    ) {
                        Text("Save Target", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GlowPill(
    text: String,
    glowColor: Color = IceCyanPrimary,
    textColor: Color = DarkNavy,
    backgroundColor: Color = IceCyanPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, glowColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    valueColor: Color = GlassWhite,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontSize = 16.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GlassWhiteMuted,
            fontSize = 9.sp
        )
    }
}

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = FrostedNavyCard,
        border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.3f))
    ) {
        content()
    }
}
