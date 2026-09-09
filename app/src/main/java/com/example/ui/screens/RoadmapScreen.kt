package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CustomTrackerEntity
import com.example.data.local.entity.RoadmapEntity
import com.example.data.local.entity.RoadmapMilestoneEntity
import com.example.ui.components.FrostedGlassCard
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
import com.example.viewmodel.RoadmapViewModel

@Composable
fun RoadmapScreen(
    viewModel: RoadmapViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showTrackerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RebuildTopAppBar(
                title = "AI Roadmaps & Trackers",
                subtitle = "30/60/90-Day Execution Roadmaps & Custom Metric Trackers",
                onMenuClick = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Custom Trackers Matrix
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
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = IceCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CUSTOM PERFORMANCE METRICS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostBlueAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = { showTrackerDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Tracker",
                                tint = IceCyanPrimary
                            )
                        }
                    }
                }

                if (uiState.customTrackers.isEmpty()) {
                    item {
                        EmptyTrackersCard(onAdd = { showTrackerDialog = true })
                    }
                } else {
                    items(uiState.customTrackers, key = { it.id }) { tracker ->
                        TrackerItemCard(
                            tracker = tracker,
                            onIncrement = { viewModel.incrementTracker(tracker, 1) },
                            onDecrement = { viewModel.incrementTracker(tracker, -1) },
                            onDelete = { viewModel.deleteTracker(tracker.id) }
                        )
                    }
                }

                // Section 2: AI Multi-Horizon Roadmaps
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = PurpleArc,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AI HORIZON ROADMAPS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostBlueAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        Button(
                            onClick = { showGenerateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleArc),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = DarkNavy)
                                Text("New AI Roadmap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                            }
                        }
                    }
                }

                if (uiState.roadmaps.isEmpty()) {
                    item {
                        EmptyRoadmapsCard(onGenerate = { showGenerateDialog = true })
                    }
                } else {
                    item {
                        RoadmapsSelectorRow(
                            roadmaps = uiState.roadmaps,
                            selectedId = uiState.selectedRoadmapId,
                            onSelect = { viewModel.selectRoadmap(it) },
                            onDelete = { viewModel.deleteRoadmap(it) }
                        )
                    }

                    // Roadmap Milestones
                    items(uiState.activeRoadmapMilestones, key = { it.id }) { milestone ->
                        MilestoneCard(
                            milestone = milestone,
                            onToggle = { viewModel.toggleMilestone(milestone) }
                        )
                    }
                }
            }
        }

        if (showGenerateDialog) {
            GenerateRoadmapDialog(
                onDismiss = { showGenerateDialog = false },
                onGenerate = { title, cat, days ->
                    viewModel.generateAiRoadmap(title, cat, days)
                    showGenerateDialog = false
                }
            )
        }

        if (showTrackerDialog) {
            CreateTrackerDialog(
                onDismiss = { showTrackerDialog = false },
                onSave = { title, cat, target, unit ->
                    viewModel.createTracker(title, cat, target, unit)
                    showTrackerDialog = false
                }
            )
        }
    }
}

@Composable
private fun EmptyTrackersCard(onAdd: () -> Unit) {
    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No Custom Trackers Yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlassWhite)
            Text(
                "Track anything from DSA LeetCode problems, Running KMs, or Vocab cards.",
                fontSize = 12.sp,
                color = GlassWhiteMuted,
                lineHeight = 16.sp
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Build Custom Tracker", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TrackerItemCard(
    tracker: CustomTrackerEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tracker.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent
                    )
                    Text(
                        text = tracker.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = GlassWhiteMuted, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tracker.currentCount} / ${tracker.targetCount} ${tracker.unit}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = IceCyanPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33284B75),
                        modifier = Modifier.clickable { onDecrement() }
                    ) {
                        Text("-1", color = GlassWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IceCyanPrimary,
                        modifier = Modifier.clickable { onIncrement() }
                    ) {
                        Text("+1", color = DarkNavy, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            val progress = if (tracker.targetCount > 0) tracker.currentCount.toFloat() / tracker.targetCount else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = IceCyanPrimary,
                trackColor = Color(0x33284B75)
            )
        }
    }
}

@Composable
private fun EmptyRoadmapsCard(onGenerate: () -> Unit) {
    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No AI Roadmaps Generated", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlassWhite)
            Text(
                "Generate structured 30, 60, or 90-day execution roadmaps for any goal.",
                fontSize = 12.sp,
                color = GlassWhiteMuted
            )
            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleArc),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate AI Roadmap", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RoadmapsSelectorRow(
    roadmaps: List<RoadmapEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        roadmaps.forEach { r ->
            val isSelected = r.id == selectedId
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PurpleArc.copy(alpha = 0.25f) else FrostedNavyCard,
                border = BorderStroke(1.dp, if (isSelected) PurpleArc else Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().clickable { onSelect(r.id) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = PurpleArc.copy(alpha = 0.3f)) {
                                Text("${r.horizonDays} DAYS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PurpleArc, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Text(r.goalTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlassWhite)
                        }
                        if (r.summary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(r.summary, fontSize = 11.sp, color = GlassWhiteMuted, maxLines = 1)
                        }
                    }

                    IconButton(onClick = { onDelete(r.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = GlassWhiteMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: RoadmapMilestoneEntity,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
        border = BorderStroke(1.dp, if (milestone.isCompleted) SuccessGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (milestone.isCompleted) SuccessGreen else IceCyanPrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.milestoneTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (milestone.isCompleted) SuccessGreen else GlassWhite
                )
                if (milestone.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = milestone.description,
                        fontSize = 11.sp,
                        color = GlassWhiteMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GenerateRoadmapDialog(
    onDismiss: () -> Unit,
    onGenerate: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Coding") }
    var days by remember { mutableStateOf(30) }

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
                Text("Generate AI Roadmap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PurpleArc)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal or Target Title", color = FrostBlueAccent) },
                    placeholder = { Text("e.g. Master React & DSA, Crack UPSC Prelims", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = PurpleArc,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Horizon Duration", fontSize = 12.sp, color = FrostBlueAccent, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90).forEach { d ->
                        val isSel = days == d
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) PurpleArc else Color(0x33284B75),
                            modifier = Modifier.weight(1f).clickable { days = d }
                        ) {
                            Text(
                                "$d Days",
                                color = if (isSel) DarkNavy else GlassWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = GlassWhiteMuted) }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) onGenerate(title.trim(), category, days)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleArc),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Generate", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTrackerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CODING") }
    var target by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("Problems") }

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
                Text("New Custom Metric Tracker", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IceCyanPrimary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tracker Name", color = FrostBlueAccent) },
                    placeholder = { Text("e.g. LeetCode Easy/Medium, 5km Runs", color = GlassWhiteMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target Count", color = FrostBlueAccent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GlassWhite,
                            unfocusedTextColor = GlassWhite,
                            focusedBorderColor = IceCyanPrimary,
                            unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                            focusedContainerColor = DarkNavy,
                            unfocusedContainerColor = DarkNavy
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit", color = FrostBlueAccent) },
                        placeholder = { Text("Problems, km, pages", color = GlassWhiteMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GlassWhite,
                            unfocusedTextColor = GlassWhite,
                            focusedBorderColor = IceCyanPrimary,
                            unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                            focusedContainerColor = DarkNavy,
                            unfocusedContainerColor = DarkNavy
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = GlassWhiteMuted) }
                    Button(
                        onClick = {
                            val count = target.toIntOrNull() ?: 100
                            if (title.isNotBlank()) onSave(title.trim(), category, count, unit.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Tracker", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
