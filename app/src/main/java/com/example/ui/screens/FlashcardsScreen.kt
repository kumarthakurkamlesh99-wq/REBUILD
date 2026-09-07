package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.FlashcardEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.FlashcardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newDeckTitle by remember { mutableStateOf("") }
    var newDeckSubject by remember { mutableStateOf("PHYSICS") }
    var newDeckChapter by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Flashcards & Spaced Recall", fontWeight = FontWeight.Bold, color = GlassWhite, fontSize = 18.sp)
                        Text("SuperMemo SM-2 Active Recall Engine", color = IceCyanPrimary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Deck", tint = IceCyanPrimary)
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
            if (uiState.isReviewMode) {
                // REVIEW MODE UI
                ActiveReviewCard(
                    cards = if (uiState.currentDeck != null) uiState.cardsInDeck else uiState.dueCards,
                    currentIndex = uiState.currentReviewCardIndex,
                    isShowingAnswer = uiState.isShowingAnswer,
                    onToggleAnswer = { viewModel.toggleShowAnswer() },
                    onSubmitResult = { knew -> viewModel.submitReviewResult(knew) },
                    onCloseReview = { viewModel.closeReview() }
                )
            } else {
                // DECK LIST & RECALL SUMMARY
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Due Review Banner
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("CARDS DUE FOR REVIEW", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("${uiState.dueCards.size} Cards Scheduled", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.startReview(null) },
                                        enabled = uiState.dueCards.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("START RECALL", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // AI Quick Deck Generator Banner
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurpleArc)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI High-Yield Deck Generator", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Instantly generate core definitions, derivation trap points, and formula applications for any Class 12 chapter.",
                                    color = GlassWhiteMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.generateAiFlashcards("PHYSICS", "Electrostatics") },
                                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Physics: Electrostatics", color = IceCyanPrimary, fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.generateAiFlashcards("CHEMISTRY", "Solutions") },
                                        border = BorderStroke(1.dp, PurpleArc.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Chemistry: Solutions", color = PurpleArc, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text("Active Decks (${uiState.decks.size})", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (uiState.decks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No flashcard decks yet. Tap '+' or generate one above to begin active recall.", color = GlassWhiteMuted, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(uiState.decks, key = { it.id }) { deck ->
                            DeckListItem(
                                deck = deck,
                                onSelect = {
                                    viewModel.selectDeck(deck)
                                    viewModel.startReview(deck)
                                }
                            )
                        }
                    }
                }
            }

            // Create Deck Dialog
            if (showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    title = { Text("Create Flashcard Deck", color = GlassWhite, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newDeckTitle,
                                onValueChange = { newDeckTitle = it },
                                label = { Text("Deck Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newDeckSubject,
                                onValueChange = { newDeckSubject = it },
                                label = { Text("Subject (e.g., PHYSICS, CHEMISTRY)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newDeckChapter,
                                onValueChange = { newDeckChapter = it },
                                label = { Text("Chapter Title (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newDeckTitle.isNotBlank()) {
                                    viewModel.createCustomDeck(newDeckTitle.trim(), newDeckSubject.trim(), newDeckChapter.trim())
                                    showCreateDialog = false
                                    newDeckTitle = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
                        ) {
                            Text("Create", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
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
fun ActiveReviewCard(
    cards: List<FlashcardEntity>,
    currentIndex: Int,
    isShowingAnswer: Boolean,
    onToggleAnswer: () -> Unit,
    onSubmitResult: (Boolean) -> Unit,
    onCloseReview: () -> Unit
) {
    if (cards.isEmpty() || currentIndex !in cards.indices) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Review Session Finished!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCloseReview) {
                    Text("Return to Decks")
                }
            }
        }
        return
    }

    val card = cards[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Meta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Card ${currentIndex + 1} of ${cards.size}", color = GlassWhiteMuted, fontSize = 13.sp)
            IconButton(onClick = onCloseReview) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = GlassWhiteMuted)
            }
        }

        // Center Flashcard
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onToggleAnswer() }
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF111D38),
            border = BorderStroke(1.5.dp, if (isShowingAnswer) IceCyanPrimary else Color(0x337C8CFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isShowingAnswer) "ANSWER / FORMULA" else "QUESTION / CONCEPT",
                        color = if (isShowingAnswer) IceCyanPrimary else PurpleArc,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isShowingAnswer) card.answer else card.question,
                        color = GlassWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isShowingAnswer) "Tap card to flip back" else "Tap card to reveal answer",
                        color = GlassWhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = { onSubmitResult(false) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1522))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF5252))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Forgot (Repeat)", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onSubmitResult(true) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF103A2B))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Got It (Mastered)", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeckListItem(
    deck: FlashcardDeckEntity,
    onSelect: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(deck.title, color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${deck.subjectCode} • ${deck.chapterTitle.ifBlank { "General" }}", color = IceCyanPrimary, fontSize = 12.sp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33102A45),
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f))
            ) {
                Text("PRACTICE", color = IceCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }
    }
}
