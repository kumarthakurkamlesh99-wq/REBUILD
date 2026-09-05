package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RankLevel
import com.example.ui.components.LevelUnlockCelebrationModal
import com.example.ui.components.LevelUnlockPurchaseModal
import com.example.ui.theme.DarkLuxuryBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.HeroCardGradient
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.RankReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankReportScreen(
    viewModel: RankReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCertificate: () -> Unit = {},
    onNavigateToCertificateWithLevel: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    // Scroll to current rank on load
    LaunchedEffect(state.currentLevel) {
        val targetIndex = (state.currentLevel - 1).coerceAtLeast(0)
        // Add offset for header items
        listState.animateScrollToItem((targetIndex + 2).coerceIn(0, state.allRanks.size + 1))
    }

    Scaffold(
        modifier = Modifier.testTag("rank_report_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkLuxuryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rank Intelligence Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = "Operational Reality & Hierarchy Audit",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("rank_report_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCertificate) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Certificate Engine",
                            tint = IceCyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkLuxuryBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Current Rank Hero Card
            item {
                CurrentRankHeroCard(
                    level = state.currentLevel,
                    title = state.currentRank.title,
                    currentXp = state.currentXp,
                    xpRequiredForNext = state.xpRequiredForNext,
                    progress = state.progress
                )
            }

            // 1B. Official Certificate CTA Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToCertificate() },
                    shape = RoundedCornerShape(14.dp),
                    color = LuxuryCard,
                    border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f))
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(IceCyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = IceCyanPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Official Achievement Certificate",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Export A4 Print-Ready PDF, JPG & PNG with verification hash",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhiteMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToCertificate,
                            colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("View", color = DarkLuxuryBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Dynamic Level Analysis Engine Card
            item {
                DynamicAnalysisEngineCard(
                    level = state.currentLevel,
                    rankTitle = state.currentRank.title,
                    dynamicAnalysis = state.dynamicAnalysis,
                    daysUntilExam = state.daysUntilExam
                )
            }

            // 3. Section Header: All 25 Levels
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "APEX HIERARCHY (ALL 25 LEVELS)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhiteMuted,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "Level ${state.currentLevel}/25 Unlocked",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary
                    )
                }
            }

            // 4. List of 25 Ranks with XP-purchase locking
            items(state.allRanks, key = { it.level }) { rankItem ->
                val isUnlocked = state.unlockedLevels.contains(rankItem.level)
                val isCurrent = rankItem.level == state.currentLevel

                RankListItemCard(
                    rank = rankItem,
                    isCurrent = isCurrent,
                    isUnlocked = isUnlocked,
                    currentXp = state.currentXp,
                    daysUntilExam = state.daysUntilExam,
                    onLockedLevelClick = {
                        viewModel.openPurchaseModal(rankItem)
                    },
                    onOpenCertificate = {
                        onNavigateToCertificateWithLevel(rankItem.level)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Level Purchase Dialog
    state.selectedLockedRankForPurchase?.let { lockedRank ->
        LevelUnlockPurchaseModal(
            rank = lockedRank,
            currentXpBalance = state.currentXp,
            isPurchasing = state.isPurchasing,
            onConfirmUnlock = { viewModel.confirmLevelPurchase(lockedRank) },
            onDismiss = { viewModel.dismissPurchaseModal() }
        )
    }

    // Modal: Celebration Dialog
    state.lastUnlockedPurchase?.let { (purchase, rank) ->
        LevelUnlockCelebrationModal(
            purchase = purchase,
            rank = rank,
            onViewDetails = {
                viewModel.dismissCelebration()
                onNavigateToCertificateWithLevel(rank.level)
            },
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}

@Composable
fun CurrentRankHeroCard(
    level: Int,
    title: String,
    currentXp: Int,
    xpRequiredForNext: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "rank_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlowBorderBrush, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroCardGradient)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Apex Level Badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(FrostedNavyCard)
                        .border(2.dp, IceCyanPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "LVL $level",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GlassWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rank Title
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = GlassWhite,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "CURRENT REBUILD STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = IceCyanPrimary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // XP Metrics Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF070B17))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL DISCIPLINE XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassWhiteMuted
                        )
                        Text(
                            text = "$currentXp XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (level >= 25) "MAX RANK" else "TO LEVEL ${level + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassWhiteMuted
                        )
                        Text(
                            text = if (level >= 25) "Apex Achieved" else "+$xpRequiredForNext XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (level >= 25) SuccessGreen else FireOrange,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level Progression",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = IceCyanPrimary,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicAnalysisEngineCard(
    level: Int,
    rankTitle: String,
    dynamicAnalysis: String,
    daysUntilExam: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IceCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IceCyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "LEVEL ANALYSIS ENGINE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    Text(
                        text = "Brutal & Constructive Operational Reality",
                        style = MaterialTheme.typography.labelSmall,
                        color = IceCyanGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic generated text box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF060913))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = dynamicAnalysis.ifBlank {
                        "Analyzing discipline telemetry for Level $level ($rankTitle)...\n\nGood news:\nYou have $daysUntilExam days left.\nStart stacking study sessions and consistency.\nLevel ${level + 1} is waiting."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassWhite.copy(alpha = 0.95f),
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun RankListItemCard(
    rank: RankLevel,
    isCurrent: Boolean,
    isUnlocked: Boolean,
    currentXp: Int,
    daysUntilExam: Long,
    onLockedLevelClick: () -> Unit = {},
    onOpenCertificate: () -> Unit = {}
) {
    var expanded by remember(isCurrent) { mutableStateOf(isCurrent) }

    val borderColor = when {
        isCurrent -> IceCyanPrimary
        isUnlocked -> PurpleArc.copy(alpha = 0.5f)
        else -> GlassBorder
    }

    val cardBg = when {
        isCurrent -> Color(0xFF131D38)
        isUnlocked -> LuxuryCard
        else -> Color(0xFF090D18)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                if (isUnlocked || isCurrent) {
                    expanded = !expanded
                } else {
                    onLockedLevelClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Level Icon/Number Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> IceCyanPrimary.copy(alpha = 0.25f)
                                    isUnlocked -> PurpleArc.copy(alpha = 0.15f)
                                    else -> Color(0xFF141926)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isCurrent -> IceCyanPrimary
                                    isUnlocked -> PurpleArc
                                    else -> Color.DarkGray
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rank.level}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCurrent -> IceCyanPrimary
                                isUnlocked -> PurpleArc
                                else -> GlassWhiteMuted
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isUnlocked || isCurrent) rank.title else "🔒 ${rank.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isCurrent) GlassWhite else if (isUnlocked) GlassWhite.copy(alpha = 0.9f) else GlassWhiteMuted
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IceCyanPrimary.copy(alpha = 0.2f))
                                        .border(1.dp, IceCyanPrimary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CURRENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IceCyanPrimary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        if (isUnlocked || isCurrent) {
                            Text(
                                text = "${rank.minXp} - ${if (rank.level == 25) "∞" else "${rank.maxXp}"} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = "Cost: ${String.format("%,d", rank.unlockXpCost)} XP to Unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = IceCyanPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Status Indicator & Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        isCurrent -> {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Active",
                                tint = IceCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        isUnlocked -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Unlocked",
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        else -> {
                            Button(
                                onClick = onLockedLevelClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = DarkLuxuryBackground,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Unlock",
                                        color = DarkLuxuryBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isUnlocked || isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = GlassWhiteMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Teaser line & placeholder for locked levels
            if (!isUnlocked && !isCurrent) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rank.teaserLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unlock this level to reveal full details.",
                    style = MaterialTheme.typography.labelSmall,
                    color = IceCyanPrimary.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }

            // Expanded Level Briefing & Reality Audit (ONLY when unlocked/current)
            if (isUnlocked || isCurrent) {
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF060913))
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "OPERATIONAL REALITY:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IceCyanPrimary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rank.brutalReality,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhite.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "TACTICAL DIRECTIVE:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rank.baseDirective,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassWhite.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rank.nextRankGoal,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuxuryAccent
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onOpenCertificate,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = DarkLuxuryBackground,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "View & Mint Level ${rank.level} Certificate",
                                            color = DarkLuxuryBackground,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
