package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AiCoachPersona
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.FlashcardEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.RebuildDialog
import com.example.ui.components.RebuildSelectorChip
import com.example.ui.components.RebuildTextField
import com.example.ui.components.RebuildTopAppBar
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
import com.example.viewmodel.AiChatViewModel
import com.example.viewmodel.FlashcardsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    flashcardsViewModel: FlashcardsViewModel? = null,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var activeTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .imePadding()
    ) {
        // Top Navigation Bar
        RebuildTopAppBar(
            title = "JARVIS AI Assistant",
            subtitle = "Cognitive Study Copilot • Neural Chat & SM-2 Recall",
            onMenuClick = onOpenDrawer,
            actions = {
                IconButton(
                    onClick = { viewModel.toggleContextDialog(true) },
                    modifier = Modifier.testTag("ai_chat_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Telemetry Info",
                        tint = IceCyanPrimary
                    )
                }
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.testTag("ai_chat_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = GlassWhiteMuted
                    )
                }
            }
        )

        // Unified JARVIS Tabs: Neural Chat vs Flashcards
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = GlassWhite,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = IceCyanPrimary,
                    height = 3.dp
                )
            },
            divider = {},
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (activeTab == 0) IceCyanPrimary else GlassWhiteMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JARVIS Chat",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 0) IceCyanPrimary else GlassWhiteMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = if (activeTab == 1) LuxuryAccent else GlassWhiteMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JARVIS Flashcards",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) LuxuryAccent else GlassWhiteMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (activeTab == 0) {
            // JARVIS Chat Content
            JarvisChatContent(
                viewModel = viewModel,
                uiState = uiState,
                listState = listState
            )
        } else {
            // JARVIS Flashcards Content
            if (flashcardsViewModel != null) {
                JarvisFlashcardsContent(viewModel = flashcardsViewModel)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Flashcard Engine Initializing...",
                        color = GlassWhiteMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (uiState.showContextDialog) {
        TelemetryContextDialog(
            snapshot = uiState.contextSnapshot,
            onDismiss = { viewModel.toggleContextDialog(false) }
        )
    }
}

@Composable
private fun JarvisChatContent(
    viewModel: AiChatViewModel,
    uiState: com.example.viewmodel.AiChatUiState,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Persona Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PersonaChip(
                title = "JARVIS Lead",
                icon = Icons.Default.Psychology,
                color = IceCyanPrimary,
                isSelected = uiState.selectedPersona == AiCoachPersona.BOARD_EXAM_COACH,
                onClick = { viewModel.selectPersona(AiCoachPersona.BOARD_EXAM_COACH) }
            )
            PersonaChip(
                title = "Discipline Master",
                icon = Icons.Default.TrendingUp,
                color = ElectricBlue,
                isSelected = uiState.selectedPersona == AiCoachPersona.WINTER_ARC_COACH,
                onClick = { viewModel.selectPersona(AiCoachPersona.WINTER_ARC_COACH) }
            )
            PersonaChip(
                title = "Deep Work Strategist",
                icon = Icons.Default.Speed,
                color = WarningAmber,
                isSelected = uiState.selectedPersona == AiCoachPersona.PRODUCTIVITY_MENTOR,
                onClick = { viewModel.selectPersona(AiCoachPersona.PRODUCTIVITY_MENTOR) }
            )
            PersonaChip(
                title = "Accountability",
                icon = Icons.Default.Security,
                color = SuccessGreen,
                isSelected = uiState.selectedPersona == AiCoachPersona.ACCOUNTABILITY_PARTNER,
                onClick = { viewModel.selectPersona(AiCoachPersona.ACCOUNTABILITY_PARTNER) }
            )
        }

        // Quick Prompt Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            uiState.quickPrompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FrostedNavyCard,
                    border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable { viewModel.sendMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = GlassWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.messages) { message ->
                ChatMessageBubble(message = message)
            }

            if (uiState.isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = IceCyanPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JARVIS is analyzing telemetry & calculating response...",
                            fontSize = 12.sp,
                            color = FrostBlueAccent
                        )
                    }
                }
            }
        }

        // Message Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FrostedNavyCard,
            border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.currentInput,
                    onValueChange = { viewModel.setInput(it) },
                    placeholder = {
                        Text("Ask JARVIS anything...", color = GlassWhiteMuted, fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite,
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = uiState.currentInput.isNotBlank() && !uiState.isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (uiState.currentInput.isNotBlank() && !uiState.isSending) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f))
                        .testTag("ai_chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (uiState.currentInput.isNotBlank() && !uiState.isSending) DarkNavy else GlassWhiteMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JarvisFlashcardsContent(
    viewModel: FlashcardsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newDeckTitle by remember { mutableStateOf("") }
    var newDeckSubject by remember { mutableStateOf("PHYSICS") }
    var newDeckChapter by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
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
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
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
                                    Text("${uiState.dueCards.size} Cards Scheduled", color = GlassWhite, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                                }
                                Button(
                                    onClick = { viewModel.startReview(null) },
                                    enabled = uiState.dueCards.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("START RECALL", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LuxuryAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("JARVIS High-Yield Flashcard Generator", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Generate core formulas, derivations, definitions, and NCERT concepts on demand.",
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                    border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Chemistry: Solutions", color = LuxuryAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Section Title + Create Deck Action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Decks (${uiState.decks.size})", color = GlassWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        TextButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = IceCyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Deck", color = IceCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (uiState.decks.isEmpty()) {
                    item {
                        com.example.ui.components.RebuildEmptyState(
                            title = "No Flashcard Decks Yet",
                            description = "Active recall & spaced repetition power retention. Tap 'New Deck' or generate with JARVIS above.",
                            icon = Icons.Default.AutoAwesome,
                            iconTint = LuxuryAccent,
                            actionLabel = "Create Deck",
                            onAction = { showCreateDialog = true }
                        )
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
            val subjects = listOf("PHYSICS", "CHEMISTRY", "BIOLOGY", "MATHEMATICS", "ENGLISH", "GENERAL")
            RebuildDialog(
                onDismiss = { showCreateDialog = false },
                title = "Create Flashcard Deck",
                subtitle = "Organize high-yield cards for SuperMemo SM-2 spaced repetition",
                icon = Icons.Default.Style,
                iconTint = IceCyanPrimary,
                headerAccentColor = IceCyanPrimary,
                confirmButtonText = "Create Deck",
                confirmButtonEnabled = newDeckTitle.isNotBlank(),
                onConfirm = {
                    if (newDeckTitle.isNotBlank()) {
                        viewModel.createCustomDeck(newDeckTitle.trim(), newDeckSubject.trim(), newDeckChapter.trim())
                        showCreateDialog = false
                        newDeckTitle = ""
                    }
                },
                testTag = "create_deck_dialog"
            ) {
                RebuildTextField(
                    value = newDeckTitle,
                    onValueChange = { newDeckTitle = it },
                    label = "Deck Title",
                    placeholder = "e.g. Electromagnetism Formulas & Derivations",
                    singleLine = true,
                    focusedBorderColor = IceCyanPrimary,
                    testTag = "deck_title_input"
                )

                Text("Subject", fontSize = 12.sp, color = GlassWhiteMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { s ->
                        RebuildSelectorChip(
                            text = s,
                            isSelected = newDeckSubject == s,
                            onClick = { newDeckSubject = s },
                            selectedColor = IceCyanPrimary
                        )
                    }
                }

                RebuildTextField(
                    value = newDeckChapter,
                    onValueChange = { newDeckChapter = it },
                    label = "Chapter / Unit (Optional)",
                    placeholder = "e.g. Optics, Thermodynamics",
                    singleLine = true,
                    focusedBorderColor = IceCyanPrimary,
                    testTag = "deck_chapter_input"
                )
            }
        }
    }
}

@Composable
private fun PersonaChip(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else FrostedNavyCard,
        border = BorderStroke(1.dp, if (isSelected) color else FrostBlueAccent.copy(alpha = 0.2f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) color else GlassWhiteMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) GlassWhite else GlassWhiteMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                color = Color(0xFF131D38),
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "JARVIS",
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Color(0xFF16325C) else FrostedNavyCard,
            border = BorderStroke(
                1.dp,
                if (isUser) ElectricBlue.copy(alpha = 0.4f) else FrostBlueAccent.copy(alpha = 0.2f)
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text(
                        text = "JARVIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = IceCyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                SelectionContainer {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassWhiteMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun TelemetryContextDialog(snapshot: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkNavy,
            border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JARVIS Live Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GlassWhiteMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF060B18),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                ) {
                    Text(
                        text = snapshot.ifBlank { "Synchronizing live database snapshot..." },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = FrostBlueAccent,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
                ) {
                    Text("Dismiss", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
