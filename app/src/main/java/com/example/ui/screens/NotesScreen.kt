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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.NoteEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
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
import com.example.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showAddNoteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LuxuryCard)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = GlassWhite
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NOTES & REFLECTIONS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Daily audit, high-yield formulas & board strategies",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = state.activeTab,
                containerColor = Color.Transparent,
                contentColor = GlassWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.activeTab]),
                        color = LuxuryAccent,
                        height = 3.dp
                    )
                },
                divider = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = {
                        Text(
                            text = "Daily Reflection",
                            fontWeight = if (state.activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.activeTab == 0) IceCyanPrimary else GlassWhiteMuted,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = {
                        Text(
                            text = "Study Notes & Cheats",
                            fontWeight = if (state.activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.activeTab == 1) IceCyanPrimary else GlassWhiteMuted,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (state.activeTab) {
                0 -> DailyReflectionSection(viewModel = viewModel, state = state)
                1 -> StudyNotesSection(viewModel = viewModel, state = state, onAddNote = { showAddNoteDialog = true })
            }
        }

        // FAB for Study Notes tab
        if (state.activeTab == 1) {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = LuxuryAccent,
                contentColor = DarkNavy,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, content, tag, isPinned ->
                viewModel.saveNote(title = title, content = content, subjectTag = tag, isPinned = isPinned)
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun DailyReflectionSection(viewModel: NotesViewModel, state: com.example.viewmodel.NotesUiState) {
    val reflection = state.todayReflection ?: return
    val scrollState = rememberScrollState()

    var score by remember(reflection.dailyScore) { mutableFloatStateOf(reflection.dailyScore.toFloat()) }
    var whatWentWell by remember(reflection.whatWentWell) { mutableStateOf(reflection.whatWentWell) }
    var whatHeldMeBack by remember(reflection.whatHeldMeBack) { mutableStateOf(reflection.whatHeldMeBack) }
    var gratitude by remember(reflection.gratitude) { mutableStateOf(reflection.gratitude) }
    var tomorrowGoal by remember(reflection.tomorrowGoal) { mutableStateOf(reflection.tomorrowGoal) }
    var mood by remember(reflection.mood) { mutableStateOf(reflection.mood) }
    var savedSuccess by remember { mutableStateOf(false) }

    val moodOptions = listOf("Focused", "Victorious", "Fatigued", "Unstoppable", "Recovering")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = LuxuryCard,
            border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Evening Discipline Audit (${reflection.date})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Performance Rating",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassWhiteMuted
                    )
                    Text(
                        text = "${score.toInt()}/10",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (score >= 8) SuccessGreen else if (score >= 5) WarningAmber else LuxuryAccent
                    )
                }
                Slider(
                    value = score,
                    onValueChange = { score = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = LuxuryAccent,
                        activeTrackColor = LuxuryAccent,
                        inactiveTrackColor = GlassWhiteMuted.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("score_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mood Selector
                Text(
                    text = "State of Mind",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodOptions.forEach { m ->
                        val isSelected = mood == m
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LuxuryAccent else Color(0x227C8CFF),
                            border = BorderStroke(0.5.dp, if (isSelected) IceCyanPrimary else Color.Transparent),
                            modifier = Modifier.clickable { mood = m }
                        ) {
                            Text(
                                text = m,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkNavy else GlassWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // What went well
                OutlinedTextField(
                    value = whatWentWell,
                    onValueChange = { whatWentWell = it },
                    label = { Text("What went exceptionally well today?") },
                    placeholder = { Text("Finished Nuclei chapter, locked in 50m Pomodoro...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("what_went_well_input"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // What held me back
                OutlinedTextField(
                    value = whatHeldMeBack,
                    onValueChange = { whatHeldMeBack = it },
                    label = { Text("What held me back or caused friction?") },
                    placeholder = { Text("Post-commute fatigue at 1 PM, delayed start...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("what_held_back_input"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gratitude
                OutlinedTextField(
                    value = gratitude,
                    onValueChange = { gratitude = it },
                    label = { Text("Gratitude & Grounding") },
                    placeholder = { Text("Grateful for health, mental focus, supportive family...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tomorrow's #1 Goal
                OutlinedTextField(
                    value = tomorrowGoal,
                    onValueChange = { tomorrowGoal = it },
                    label = { Text("Tomorrow's #1 Non-Negotiable Mission") },
                    placeholder = { Text("Solve 15 Genetics PYQs and 20 min running...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.saveDailyReflection(
                            score = score.toInt(),
                            whatWentWell = whatWentWell,
                            whatHeldMeBack = whatHeldMeBack,
                            gratitude = gratitude,
                            tomorrowGoal = tomorrowGoal,
                            mood = mood
                        )
                        savedSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_reflection_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DarkNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (savedSuccess) "Saved & +25 XP Awarded!" else "Save Daily Reflection",
                        color = DarkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent reflections history
        if (state.recentReflections.isNotEmpty()) {
            Text(
                text = "Past Reflection Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.recentReflections.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = LuxuryCard,
                    border = BorderStroke(0.5.dp, GlassWhiteMuted.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.date,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = IceCyanPrimary
                            )
                            Text(
                                text = "${item.dailyScore}/10 • ${item.mood}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (item.whatWentWell.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Win: ${item.whatWentWell}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StudyNotesSection(
    viewModel: NotesViewModel,
    state: com.example.viewmodel.NotesUiState,
    onAddNote: () -> Unit
) {
    val subjectTags = listOf("All", "Physics", "Chemistry", "Biology", "English", "Hindi", "Strategy", "General")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search formulas, notes, named reactions...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GlassWhiteMuted)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_notes_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LuxuryAccent,
                unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.2f),
                focusedContainerColor = LuxuryCard,
                unfocusedContainerColor = LuxuryCard,
                focusedTextColor = GlassWhite,
                unfocusedTextColor = GlassWhite
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subject filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subjectTags.forEach { tag ->
                val isSelected = state.selectedSubjectTag == tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) LuxuryAccent else LuxuryCard,
                    border = BorderStroke(0.5.dp, if (isSelected) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable { viewModel.setSelectedTag(tag) }
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) DarkNavy else GlassWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notes list
        if (state.notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = GlassWhiteMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No notes found in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassWhiteMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.notes, key = { it.id }) { note ->
                    NoteCard(note = note, onDelete = { viewModel.deleteNote(note) })
                }
                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteEntity, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        color = LuxuryCard,
        border = BorderStroke(
            1.dp,
            if (note.isPinned) LuxuryAccent else GlassWhiteMuted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LuxuryAccent.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, LuxuryAccent.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = note.subjectTag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary,
                        fontSize = 10.sp
                    )
                }

                if (note.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = LuxuryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = note.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassWhiteMuted,
                    fontSize = 11.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = GlassWhiteMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassWhite.copy(alpha = 0.9f),
                lineHeight = 20.sp,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subjectTag by remember { mutableStateOf("Physics") }
    var isPinned by remember { mutableStateOf(false) }

    val tags = listOf("Physics", "Chemistry", "Biology", "English", "Hindi", "Strategy", "General")

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
                    text = "New Note / Cheatsheet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Wave Optics High-Yield Derivations") },
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

                // Tag selector
                Text(text = "Subject Tag", style = MaterialTheme.typography.bodySmall, color = GlassWhiteMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { t ->
                        val isSelected = subjectTag == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) LuxuryAccent else Color(0x227C8CFF),
                            modifier = Modifier.clickable { subjectTag = t }
                        ) {
                            Text(
                                text = t,
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
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Formulas / Mechanisms") },
                    placeholder = { Text("Write formulas, equations, or key steps...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAccent,
                        unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.3f),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    )
                )

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
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSave(title, content, subjectTag, isPinned)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent)
                    ) {
                        Text("Save Note", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
