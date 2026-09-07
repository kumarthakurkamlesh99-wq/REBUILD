package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.MistakeSeverity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.MistakeNotebookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeNotebookScreen(
    viewModel: MistakeNotebookViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var inputSubject by remember { mutableStateOf("PHYSICS") }
    var inputChapter by remember { mutableStateOf("") }
    var inputQuestion by remember { mutableStateOf("") }
    var inputMistake by remember { mutableStateOf("") }
    var inputSolution by remember { mutableStateOf("") }
    var inputConcept by remember { mutableStateOf("") }
    var inputWhy by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mistake Notebook", fontWeight = FontWeight.Bold, color = GlassWhite, fontSize = 18.sp)
                        Text("Analyze & Eliminate Error Patterns", color = WarningAmber, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Log Mistake", tint = IceCyanPrimary)
                    }
                }
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Overview Banner
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TOTAL MISTAKES LOGGED", color = GlassWhiteMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${uiState.mistakes.size} Logged", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            val unresolvedCount = uiState.mistakes.count { !it.isResolved }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (unresolvedCount > 0) Color(0x33FF5252) else Color(0x334CAF50),
                                border = BorderStroke(1.dp, if (unresolvedCount > 0) Color(0xFFFF5252) else SuccessGreen)
                            ) {
                                Text(
                                    "$unresolvedCount Unresolved",
                                    color = if (unresolvedCount > 0) Color(0xFFFF5252) else SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Buttons Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !uiState.showOnlyUnresolved,
                            onClick = { if (uiState.showOnlyUnresolved) viewModel.toggleShowOnlyUnresolved() },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = uiState.showOnlyUnresolved,
                            onClick = { if (!uiState.showOnlyUnresolved) viewModel.toggleShowOnlyUnresolved() },
                            label = { Text("Pending Only") }
                        )
                    }
                }

                val filteredMistakes = uiState.mistakes.filter {
                    (!uiState.showOnlyUnresolved || !it.isResolved) &&
                    (uiState.filterSubject == null || it.subjectCode.equals(uiState.filterSubject, ignoreCase = true))
                }

                if (filteredMistakes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No mistakes found. Log errors immediately after test reviews!", color = GlassWhiteMuted, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredMistakes, key = { it.id }) { mistake ->
                        MistakeCard(
                            mistake = mistake,
                            onToggleResolve = { viewModel.toggleResolve(mistake) },
                            onDelete = { viewModel.deleteMistake(mistake.id) }
                        )
                    }
                }
            }

            // Log Mistake Dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Log New Exam/Test Mistake", color = GlassWhite, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputSubject,
                                onValueChange = { inputSubject = it },
                                label = { Text("Subject") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputChapter,
                                onValueChange = { inputChapter = it },
                                label = { Text("Chapter Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputQuestion,
                                onValueChange = { inputQuestion = it },
                                label = { Text("Question / Problem Statement") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputMistake,
                                onValueChange = { inputMistake = it },
                                label = { Text("What mistake did you make?") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputSolution,
                                onValueChange = { inputSolution = it },
                                label = { Text("Correct Solution & Formula") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputConcept,
                                onValueChange = { inputConcept = it },
                                label = { Text("Core Concept to Remember") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inputWhy,
                                onValueChange = { inputWhy = it },
                                label = { Text("Root Cause (e.g., Calculation, Misread, Forgot)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (inputQuestion.isNotBlank() && inputMistake.isNotBlank()) {
                                    viewModel.addMistake(
                                        subjectCode = inputSubject,
                                        chapterTitle = inputChapter,
                                        questionOrContext = inputQuestion,
                                        studentMistake = inputMistake,
                                        correctSolution = inputSolution,
                                        coreConcept = inputConcept,
                                        whyMade = inputWhy
                                    )
                                    showAddDialog = false
                                    inputQuestion = ""
                                    inputMistake = ""
                                    inputSolution = ""
                                    inputConcept = ""
                                    inputWhy = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
                        ) {
                            Text("Save Mistake", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = GlassWhiteMuted)
                        }
                    },
                    containerColor = Color(0xFF131D38)
                )
            }
        }
    }
}

@Composable
fun MistakeCard(
    mistake: MistakeEntity,
    onToggleResolve: () -> Unit,
    onDelete: () -> Unit
) {
    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1F2942)
                    ) {
                        Text(
                            mistake.subjectCode,
                            color = IceCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(mistake.chapterTitle, color = GlassWhiteMuted, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleResolve) {
                        Icon(
                            imageVector = if (mistake.isResolved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle resolved",
                            tint = if (mistake.isResolved) SuccessGreen else GlassWhiteMuted
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFF5252))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Problem:", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(mistake.questionOrContext, color = GlassWhite, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Your Mistake:", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(mistake.studentMistake, color = GlassWhiteMuted, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Correct Solution & Rule:", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(mistake.correctSolution, color = GlassWhite, fontSize = 13.sp)

            if (mistake.whyMade.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Root Cause: ${mistake.whyMade}", color = PurpleArc, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
