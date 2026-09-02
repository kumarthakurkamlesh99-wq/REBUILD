package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.ArcGoalPlanItemEntity
import com.example.data.local.entity.ObjectiveCategory
import com.example.data.local.entity.WinterArcObjectiveEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
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
import com.example.viewmodel.WinterArcViewModel

@Composable
fun WinterArcScreen(
    viewModel: WinterArcViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddObjectiveDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RebuildTopAppBar(
                title = "Winter Arc Mission Control",
                subtitle = "90-Day Relentless Metamorphosis Protocol",
                onMenuClick = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mission Control Hero Card
                item {
                    MissionControlHeroCard(
                        currentDay = uiState.state.currentDay,
                        totalDays = uiState.state.targetDays,
                        daysRemaining = uiState.daysRemaining,
                        progressPercentage = uiState.progressPercentage,
                        streak = uiState.state.streak,
                        level = uiState.state.level,
                        xp = uiState.state.xp,
                        rankTitle = uiState.rankTitle
                    )
                }

                // Core Arc Objectives Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IceCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "NON-NEGOTIABLE ARC OBJECTIVES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostBlueAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = { showAddObjectiveDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Objective",
                                tint = IceCyanPrimary
                            )
                        }
                    }
                }

                items(uiState.objectives, key = { "obj_${it.id}" }) { objective ->
                    ObjectiveCardItem(
                        objective = objective,
                        onToggle = { viewModel.toggleObjective(objective) },
                        onDelete = { viewModel.deleteObjective(objective) }
                    )
                }

                // Tactical & Strategic Goals Horizon Tabs
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = PurpleArc,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "TACTICAL GOALS MATRIX",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostBlueAccent,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = { showAddGoalDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Goal",
                                    tint = PurpleArc
                                )
                            }
                        }

                        // Horizon Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedNavyCard)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val horizons = listOf("DAILY" to "Daily Tasks", "WEEKLY" to "Weekly Sprints", "MONTHLY" to "Monthly Milestones")
                            horizons.forEach { (key, label) ->
                                val isSel = uiState.selectedTimeHorizon == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) ElectricBlue else Color.Transparent)
                                        .clickable { viewModel.selectHorizon(key) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) DarkNavy else GlassWhiteMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Goals for selected horizon
                val currentGoals = when (uiState.selectedTimeHorizon) {
                    "WEEKLY" -> uiState.weeklyGoals
                    "MONTHLY" -> uiState.monthlyGoals
                    else -> uiState.dailyGoals
                }

                if (currentGoals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C1628))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No goals added yet for this time horizon. Tap + to add one.",
                                fontSize = 12.sp,
                                color = GlassWhiteMuted
                            )
                        }
                    }
                } else {
                    items(currentGoals, key = { "goal_${it.id}" }) { goal ->
                        GoalPlanRowItem(
                            goal = goal,
                            onToggle = { viewModel.toggleGoal(goal) },
                            onDelete = { viewModel.deleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }

    // Add Objective Dialog
    if (showAddObjectiveDialog) {
        AddObjectiveDialog(
            onDismiss = { showAddObjectiveDialog = false },
            onSave = { title, desc, cat, target ->
                viewModel.addNewObjective(title, desc, cat, target)
                showAddObjectiveDialog = false
            }
        )
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            defaultHorizon = uiState.selectedTimeHorizon,
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, desc, horizon, priority, xp ->
                viewModel.addNewGoal(title, desc, horizon, priority, xp)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun MissionControlHeroCard(
    currentDay: Int,
    totalDays: Int,
    daysRemaining: Int,
    progressPercentage: Int,
    streak: Int,
    level: Int,
    xp: Int,
    rankTitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DAY $currentDay OF $totalDays",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = IceCyanPrimary,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FireOrange.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 $streak DAY STREAK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FireOrange
                            )
                        }
                    }

                    Text(
                        text = rankTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { (progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = IceCyanPrimary,
                        trackColor = Color(0xFF0F1B30),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$progressPercentage%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }
            }

            // XP and Level Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level $level Vanguard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent
                    )
                    Text(
                        text = "$xp XP Accumulated",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary
                    )
                }

                val levelProgress = ((xp % 500) / 500f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricBlue,
                    trackColor = Color(0xFF0D182E)
                )
            }

            // Metrics Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF091120))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DAYS LEFT", fontSize = 10.sp, color = GlassWhiteMuted, fontWeight = FontWeight.Bold)
                    Text("$daysRemaining", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GlassWhite)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DISCIPLINE", fontSize = 10.sp, color = GlassWhiteMuted, fontWeight = FontWeight.Bold)
                    Text("Apex 98%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MODE", fontSize = 10.sp, color = GlassWhiteMuted, fontWeight = FontWeight.Bold)
                    Text("Monk Mode", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PurpleArc)
                }
            }
        }
    }
}

@Composable
fun ObjectiveCardItem(
    objective: WinterArcObjectiveEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (objective.category) {
        ObjectiveCategory.ACADEMIC -> ElectricBlue
        ObjectiveCategory.FITNESS -> FireOrange
        ObjectiveCategory.DISCIPLINE -> IceCyanPrimary
        ObjectiveCategory.RESTORATION -> PurpleArc
        ObjectiveCategory.HABIT -> WarningAmber
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (objective.isCompleted) Color(0xFF091424) else FrostedNavyCard
        ),
        border = BorderStroke(
            1.dp,
            if (objective.isCompleted) SuccessGreen.copy(alpha = 0.4f) else categoryColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (objective.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Objective",
                        tint = if (objective.isCompleted) SuccessGreen else GlassWhiteMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoryColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = objective.category.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }

                        Text(
                            text = "Target: ${objective.targetValue}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostBlueAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = objective.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (objective.isCompleted) GlassWhiteMuted else GlassWhite
                    )

                    if (objective.description.isNotBlank()) {
                        Text(
                            text = objective.description,
                            fontSize = 11.sp,
                            color = GlassWhiteMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
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
fun GoalPlanRowItem(
    goal: ArcGoalPlanItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (goal.priority) {
        "HIGH" -> FireOrange
        "CRITICAL" -> PurpleArc
        else -> ElectricBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D172A))
            .border(1.dp, if (goal.isCompleted) SuccessGreen.copy(alpha = 0.3f) else FrostBlueAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (goal.isCompleted) SuccessGreen else GlassWhiteMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = goal.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (goal.isCompleted) GlassWhiteMuted else GlassWhite
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        text = goal.description,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(priorityColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "+${goal.xpReward} XP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = priorityColor
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Goal",
                    tint = GlassWhiteMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun AddObjectiveDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, ObjectiveCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ObjectiveCategory.ACADEMIC) }
    var catMenuOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
            border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("New Winter Arc Objective", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IceCyanPrimary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Objective Title", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Daily Benchmark / Details", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target (e.g. 95%+, 6h Daily)", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = GlassWhiteMuted)
                    }
                    Button(
                        onClick = { onSave(title, desc, category, target) },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkNavy)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    defaultHorizon: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var horizon by remember { mutableStateOf(defaultHorizon) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
            border = BorderStroke(1.dp, PurpleArc.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add Goal to Matrix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PurpleArc)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = PurpleArc,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Notes / Target", color = FrostBlueAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = PurpleArc,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = GlassWhiteMuted)
                    }
                    Button(
                        onClick = { onSave(title, desc, horizon, "HIGH", 50) },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleArc, contentColor = GlassWhite)
                    ) {
                        Text("Add Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
