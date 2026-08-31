package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AiPlanType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LiquidArcGradient
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AiCoachUiState
import com.example.viewmodel.AiCoachViewModel
import com.example.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    viewModel: AiCoachViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Blueprint Planner, 1: Live Coach Chat, 2: Telemetry Snapshot
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var enteredKey by remember { mutableStateOf(state.apiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(top = 8.dp)
    ) {
        // Top Header
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
                    text = "AI Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Gemini",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = FrostBlueAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Key button
            IconButton(
                onClick = { showApiKeyDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (state.hasApiKey) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                    .testTag("api_key_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Configure API Key",
                    tint = if (state.hasApiKey) SuccessGreen else WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Action feedback snackbar
        state.actionFeedback?.let { msg ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = LuxuryAccent.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, LuxuryAccent)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhite,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearFeedback() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GlassWhiteMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = GlassWhite,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = LuxuryAccent,
                    height = 3.dp
                )
            },
            divider = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "9 AI Plans",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) IceCyanPrimary else GlassWhiteMuted,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Live Coach Chat",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) IceCyanPrimary else GlassWhiteMuted,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    viewModel.refreshContextSnapshot()
                },
                text = {
                    Text(
                        text = "AI Context View",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 2) IceCyanPrimary else GlassWhiteMuted,
                        fontSize = 13.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> AiPlansSection(viewModel = viewModel, state = state)
            1 -> LiveCoachChatSection(viewModel = viewModel, state = state)
            2 -> AiContextTelemetrySection(viewModel = viewModel, state = state)
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showApiKeyDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = LuxuryCard,
                border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gemini API Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your personal Gemini API key to activate high-precision coaching. (Or leave blank to use the pre-configured environment key).",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassWhiteMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredKey,
                        onValueChange = { enteredKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryAccent,
                            unfocusedBorderColor = GlassWhiteMuted.copy(alpha = 0.4f),
                            focusedTextColor = GlassWhite,
                            unfocusedTextColor = GlassWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showApiKeyDialog = false }) {
                            Text("Cancel", color = GlassWhiteMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateApiKey(enteredKey)
                                showApiKeyDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                            modifier = Modifier.testTag("save_api_key_btn")
                        ) {
                            Text("Save Key", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiPlansSection(viewModel: AiCoachViewModel, state: AiCoachUiState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Plan Type Horizontal Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiPlanType.values().forEach { type ->
                val isSelected = state.selectedPlanType == type
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectPlanType(type) }
                        .testTag("plan_chip_${type.key.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuxuryAccent else LuxuryCard,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) IceCyanPrimary else GlassWhiteMuted.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = type.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DarkNavy else GlassWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = LuxuryCard,
            border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isGenerating) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = IceCyanPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Synthesizing ${state.selectedPlanType.title}...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyzing school schedule, Physics/Chem/Bio chapters, calisthenics logs, and 148-day countdown.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        MarkdownText(
                            text = state.currentPlanContent.ifBlank { "Tap Generate to produce this plan." }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.generatePlan(state.selectedPlanType) },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryCard),
                border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("regenerate_plan_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Regenerate",
                    color = GlassWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { viewModel.applyPlanToLocalSchedule() },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isApplyingPlan,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("apply_plan_btn")
            ) {
                if (state.isApplyingPlan) {
                    CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(18.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DarkNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply to Schedule",
                        color = DarkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LiveCoachChatSection(viewModel: AiCoachViewModel, state: AiCoachUiState) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.chatMessages) { message ->
                val isUser = message.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) LuxuryAccent.copy(alpha = 0.25f) else LuxuryCard,
                        border = BorderStroke(
                            1.dp,
                            if (isUser) LuxuryAccent else LuxuryAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Star else Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (isUser) IceCyanPrimary else PurpleArc,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isUser) "You" else "AI Coach",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) IceCyanPrimary else PurpleArc,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            MarkdownText(
                                text = message.text,
                                baseColor = GlassWhite
                            )
                        }
                    }
                }
            }

            if (state.isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LuxuryCard,
                            border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = IceCyanPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Coach is thinking...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick query chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "How to recover after school?",
                "Physics Nuclei strategy",
                "I'm feeling fatigued",
                "Adjust my schedule"
            ).forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LuxuryCard,
                    border = BorderStroke(0.5.dp, LuxuryAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        viewModel.setChatInput(prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostBlueAccent,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.chatInput,
                onValueChange = { viewModel.setChatInput(it) },
                placeholder = { Text("Ask Coach about syllabus, fatigue, schedule...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryAccent,
                    unfocusedBorderColor = LuxuryAccent.copy(alpha = 0.3f),
                    focusedContainerColor = LuxuryCard,
                    unfocusedContainerColor = LuxuryCard,
                    focusedTextColor = GlassWhite,
                    unfocusedTextColor = GlassWhite
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.sendChatMessage() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LuxuryAccent)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = DarkNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AiContextTelemetrySection(viewModel: AiCoachViewModel, state: AiCoachUiState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Data Fed Into Gemini AI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GlassWhite
        )
        Text(
            text = "The AI Coach receives this exact real-time local telemetry snapshot to tailor every schedule, study session, and motivation prompt.",
            style = MaterialTheme.typography.bodySmall,
            color = GlassWhiteMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = LuxuryCard,
            border = BorderStroke(1.dp, LuxuryAccent.copy(alpha = 0.4f))
        ) {
            Text(
                text = state.contextSnapshot.ifBlank { "Loading telemetry snapshot..." },
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = IceCyanPrimary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.refreshContextSnapshot() },
            colors = ButtonDefaults.buttonColors(containerColor = LuxuryCard),
            border = BorderStroke(1.dp, LuxuryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = LuxuryAccent)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Refresh Telemetry View", color = GlassWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = GlassWhite
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = buildAnnotatedMarkdown(trimmed.removePrefix("### ").trim(), IceCyanPrimary),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary,
                        fontSize = 14.sp
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = buildAnnotatedMarkdown(trimmed.removePrefix("## ").trim(), LuxuryAccent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = LuxuryAccent,
                        fontSize = 15.sp
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = buildAnnotatedMarkdown(trimmed.removePrefix("# ").trim(), GlassWhite),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite,
                        fontSize = 17.sp
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val content = trimmed.removePrefix("- ").removePrefix("* ").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = IceCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildAnnotatedMarkdown(content, baseColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = baseColor,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\..*")) -> {
                    val match = Regex("^(\\d+\\.)\\s*(.*)").find(trimmed)
                    val prefix = match?.groupValues?.getOrNull(1) ?: "1."
                    val content = match?.groupValues?.getOrNull(2) ?: trimmed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = prefix,
                            color = LuxuryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildAnnotatedMarkdown(content, baseColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = baseColor,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    Text(
                        text = buildAnnotatedMarkdown(trimmed, baseColor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = baseColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

private fun buildAnnotatedMarkdown(
    text: String,
    baseColor: Color
) = buildAnnotatedString {
    var currentIndex = 0
    // Regex matches: **bold**, *italic*, `code`
    val regex = Regex("(\\*{1,2})(.*?)\\1|(`)(.*?)\\3")
    val matches = regex.findAll(text)

    for (match in matches) {
        // Append text before match
        if (match.range.first > currentIndex) {
            append(text.substring(currentIndex, match.range.first))
        }

        val fullMatch = match.value
        when {
            fullMatch.startsWith("**") && fullMatch.endsWith("**") && fullMatch.length >= 4 -> {
                val inner = fullMatch.substring(2, fullMatch.length - 2)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = IceCyanPrimary)) {
                    append(inner)
                }
            }
            fullMatch.startsWith("*") && fullMatch.endsWith("*") && fullMatch.length >= 2 -> {
                val inner = fullMatch.substring(1, fullMatch.length - 1)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(inner)
                }
            }
            fullMatch.startsWith("`") && fullMatch.endsWith("`") && fullMatch.length >= 2 -> {
                val inner = fullMatch.substring(1, fullMatch.length - 1)
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = FrostBlueAccent,
                        background = Color(0x33000000)
                    )
                ) {
                    append(inner)
                }
            }
            else -> {
                append(fullMatch)
            }
        }
        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}
