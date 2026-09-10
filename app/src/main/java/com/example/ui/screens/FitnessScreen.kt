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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.RebuildDialog
import com.example.ui.components.RebuildTextField
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
import com.example.data.local.entity.ExerciseType
import com.example.data.local.entity.WorkoutLevel
import com.example.data.local.entity.WorkoutLogEntity
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.FitnessViewModel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun FitnessScreen(
    viewModel: FitnessViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWorkoutDialog = true },
                containerColor = FireOrange,
                contentColor = DarkNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .testTag("add_workout_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
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
                            text = "Fitness & Calisthenics",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Workouts, cardio & physical power",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Hero Fitness Metrics Card
            item {
                HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Energy Output",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            GlowPill(
                                text = "${uiState.totalCaloriesBurnedToday} kcal",
                                color = FireOrange,
                                icon = Icons.Default.LocalFireDepartment
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalyticsMetricBox(
                                label = "Running Dist",
                                value = "${uiState.totalRunningKmToday} km",
                                accentColor = FireOrange,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Pushups Total",
                                value = "${uiState.totalPushupsToday}",
                                accentColor = IceCyanPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricBox(
                                label = "Squats Total",
                                value = "${uiState.totalSquatsToday}",
                                accentColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Intensity Level Selector (Beginner, Intermediate, Advanced)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Workout Intensity Tier",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LevelButton(
                            title = "Beginner",
                            isSelected = uiState.selectedLevel == WorkoutLevel.BEGINNER,
                            onClick = { viewModel.selectLevel(WorkoutLevel.BEGINNER) },
                            modifier = Modifier.weight(1f)
                        )
                        LevelButton(
                            title = "Intermediate",
                            isSelected = uiState.selectedLevel == WorkoutLevel.INTERMEDIATE,
                            onClick = { viewModel.selectLevel(WorkoutLevel.INTERMEDIATE) },
                            modifier = Modifier.weight(1f)
                        )
                        LevelButton(
                            title = "Advanced",
                            isSelected = uiState.selectedLevel == WorkoutLevel.ADVANCED,
                            onClick = { viewModel.selectLevel(WorkoutLevel.ADVANCED) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Workouts List Title
            item {
                Text(
                    text = "Scheduled Calisthenics & Cardio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
            }

            // Workout Cards
            if (uiState.todayWorkouts.isEmpty()) {
                item {
                    com.example.ui.components.RebuildEmptyState(
                        title = "No Workouts Logged",
                        description = "No calisthenics or cardio sessions registered today. Tap '+' below to log your training.",
                        icon = Icons.Default.FitnessCenter,
                        iconTint = FireOrange,
                        actionLabel = "Add Workout",
                        onAction = { showAddWorkoutDialog = true }
                    )
                }
            } else {
                items(uiState.todayWorkouts, key = { it.id }) { workout ->
                    WorkoutCardItem(
                        workout = workout,
                        onToggle = { viewModel.toggleWorkout(workout) }
                    )
                }
            }
        }
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onConfirm = { name, type, sets, reps, duration, dist ->
                viewModel.addCustomWorkout(name, type, sets, reps, duration, dist)
                showAddWorkoutDialog = false
            }
        )
    }
}

@Composable
fun LevelButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) FireOrange.copy(alpha = 0.25f) else Color(0x33102447),
        border = BorderStroke(
            1.dp,
            if (isSelected) FireOrange else Color(0x205CE1E6)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GlassWhite else GlassWhiteMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WorkoutCardItem(
    workout: WorkoutLogEntity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (workout.exerciseType) {
        ExerciseType.RUNNING -> Icons.Default.DirectionsRun
        ExerciseType.WALKING -> Icons.Default.DirectionsWalk
        ExerciseType.PUSHUPS -> Icons.Default.FitnessCenter
        ExerciseType.SQUATS -> Icons.Default.DirectionsWalk
        else -> Icons.Default.FitnessCenter
    }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (workout.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (workout.isCompleted) SuccessGreen else GlassWhiteMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )

                val detailsText = if (workout.exerciseType == ExerciseType.RUNNING || workout.exerciseType == ExerciseType.WALKING) {
                    "${workout.durationMinutes} min • ${workout.distanceKm} km • ${workout.caloriesBurned} kcal"
                } else {
                    "${workout.sets} sets x ${workout.reps} reps • ${workout.durationMinutes} min"
                }

                Text(
                    text = detailsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 12.sp
                )
            }

            GlowPill(
                text = "+40 XP",
                color = if (workout.isCompleted) SuccessGreen else FireOrange
            )
        }
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: ExerciseType, sets: Int, reps: Int, duration: Int, dist: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("15") }
    var duration by remember { mutableStateOf("15") }

    RebuildDialog(
        onDismiss = onDismiss,
        title = "Add Workout Exercise",
        subtitle = "Target sets, reps, and discipline duration",
        icon = Icons.Default.FitnessCenter,
        iconTint = FireOrange,
        headerAccentColor = FireOrange,
        confirmButtonText = "Add Exercise",
        confirmButtonEnabled = name.isNotBlank(),
        onConfirm = {
            if (name.isNotBlank()) {
                onConfirm(
                    name.trim(),
                    ExerciseType.PUSHUPS,
                    sets.toIntOrNull() ?: 3,
                    reps.toIntOrNull() ?: 15,
                    duration.toIntOrNull() ?: 15,
                    0f
                )
            }
        },
        testTag = "add_workout_dialog"
    ) {
        RebuildTextField(
            value = name,
            onValueChange = { name = it },
            label = "Exercise Name",
            placeholder = "e.g. Pullups, Planks, Squats",
            focusedBorderColor = FireOrange,
            testTag = "workout_name_input"
        )

        RebuildTextField(
            value = sets,
            onValueChange = { sets = it },
            label = "Target Sets",
            placeholder = "e.g. 3",
            focusedBorderColor = FireOrange,
            testTag = "workout_sets_input"
        )

        RebuildTextField(
            value = reps,
            onValueChange = { reps = it },
            label = "Reps / Seconds per Set",
            placeholder = "e.g. 15",
            focusedBorderColor = FireOrange,
            testTag = "workout_reps_input"
        )
    }
}
