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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.example.viewmodel.AiChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

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
            title = "AI Neural Coach",
            subtitle = "Live Data Synchronized • 4 Personas",
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

        // Persona Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PersonaChip(
                title = "Board Coach",
                icon = Icons.Default.School,
                color = ElectricBlue,
                isSelected = uiState.selectedPersona == AiCoachPersona.BOARD_EXAM_COACH,
                onClick = { viewModel.selectPersona(AiCoachPersona.BOARD_EXAM_COACH) }
            )
            PersonaChip(
                title = "Winter Arc",
                icon = Icons.Default.TrendingUp,
                color = IceCyanPrimary,
                isSelected = uiState.selectedPersona == AiCoachPersona.WINTER_ARC_COACH,
                onClick = { viewModel.selectPersona(AiCoachPersona.WINTER_ARC_COACH) }
            )
            PersonaChip(
                title = "Productivity",
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

        // Live Context Synchronization Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D192E))
                .border(1.dp, FrostBlueAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = IceCyanPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Trained on your Profile, 70-Chapter Syllabus, Alarms & Habits",
                fontSize = 11.sp,
                color = FrostBlueAccent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.messages) { message ->
                ChatMessageBubble(message = message)
            }

            if (uiState.isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(FrostedNavyCard)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = IceCyanPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing personal telemetry & formulating plan...",
                            fontSize = 12.sp,
                            color = FrostBlueAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.quickPrompts.forEach { prompt ->
                QuickPromptChip(
                    text = prompt,
                    onClick = { viewModel.sendMessage(prompt) }
                )
            }
        }

        // Bottom Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.currentInput,
                onValueChange = { viewModel.setInput(it) },
                placeholder = { Text("Ask your AI Coach anything...", color = GlassWhiteMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_field"),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GlassWhite,
                    unfocusedTextColor = GlassWhite,
                    focusedBorderColor = IceCyanPrimary,
                    unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.3f),
                    focusedContainerColor = FrostedNavyCard,
                    unfocusedContainerColor = FrostedNavyCard
                )
            )

            IconButton(
                onClick = { viewModel.sendMessage() },
                enabled = uiState.currentInput.isNotBlank() && !uiState.isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.currentInput.isNotBlank() && !uiState.isSending)
                            Brush.linearGradient(listOf(ElectricBlue, IceCyanPrimary))
                        else
                            Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                    )
                    .testTag("ai_chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (uiState.currentInput.isNotBlank()) DarkNavy else GlassWhiteMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Telemetry Snapshot Dialog
    if (uiState.showContextDialog) {
        Dialog(onDismissRequest = { viewModel.toggleContextDialog(false) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
                border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Telemetry Snapshot",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary
                        )
                        IconButton(onClick = { viewModel.toggleContextDialog(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = GlassWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "This live data is fed to the Gemini AI on every query to ensure 100% personalized responses:",
                        fontSize = 12.sp,
                        color = GlassWhiteMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF070E1A))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.contextSnapshot.ifEmpty { "Generating real telemetry..." },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GlassWhite,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.toggleContextDialog(false) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = DarkNavy
                        )
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaChip(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else FrostedNavyCard)
            .border(
                1.dp,
                if (isSelected) color else FrostBlueAccent.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) color else GlassWhiteMuted,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GlassWhite else GlassWhiteMuted
        )
    }
}

@Composable
fun QuickPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131F37))
            .border(1.dp, ElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = IceCyanPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val senderName = if (isUser) "You" else message.persona.title
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = "$senderName • $timeStr",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUser) FrostBlueAccent else IceCyanPrimary
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8)))
                    else
                        Brush.linearGradient(listOf(Color(0xFF0F1E36), Color(0xFF122340)))
                )
                .border(
                    1.dp,
                    if (isUser) ElectricBlue.copy(alpha = 0.4f) else FrostBlueAccent.copy(alpha = 0.2f),
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(14.dp)
        ) {
            SelectionContainer {
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = GlassWhite,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
