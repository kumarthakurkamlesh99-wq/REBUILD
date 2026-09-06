package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.components.MintCertificateModal
import com.example.ui.components.UnlockLevelModal
import com.example.ui.components.CertificateCelebrationModal
import com.example.ui.components.SaveCertificateBottomSheet
import androidx.compose.ui.platform.testTag
import com.example.data.model.RankLevelSystem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CertificateData
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.CertificateUiState
import com.example.viewmodel.CertificateViewModel
import com.example.viewmodel.ExportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    viewModel: CertificateViewModel,
    initialLevel: Int? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialLevel) {
        if (initialLevel != null && initialLevel in 1..25) {
            viewModel.selectLevel(initialLevel)
        }
    }

    LaunchedEffect(state.exportStatus) {
        when (val status = state.exportStatus) {
            is ExportStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.dismissExportStatus()
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.messageSnackbar) {
        state.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(state.exportSuccessToast) {
        state.exportSuccessToast?.let { toastMsg ->
            snackbarHostState.showSnackbar(toastMsg)
            viewModel.clearExportSuccessToast()
        }
    }

    val selectedLvl = state.certificateData.level
    val isLevelUnlocked = state.unlockedLevels.contains(selectedLvl)
    val isCertificateMinted = state.mintedCertificates.contains(selectedLvl)
    val currentRank = RankLevelSystem.RANKS.find { it.level == selectedLvl } ?: RankLevelSystem.RANKS[0]

    Scaffold(
        containerColor = DarkNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "REBUILD Certificate Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = IceCyanPrimary.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, IceCyanPrimary)
                            ) {
                                Text(
                                    text = if (isCertificateMinted) "MINTED & VERIFIED" else if (isLevelUnlocked) "MINT REQUIRED" else "LOCKED",
                                    color = if (isCertificateMinted) SuccessGreen else if (isLevelUnlocked) Color(0xFFFFD700) else GlassWhiteMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "A4 High-Resolution Neural Achievement Authority",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }, enabled = isCertificateMinted) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Toggle Edit Fields",
                            tint = if (state.isEditMode) IceCyanPrimary else if (isCertificateMinted) Color.White else GlassWhiteMuted.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = { viewModel.loadLiveProfileData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Profile",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavySurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Level Quick-Selector Strip (Levels 1 to 25 with Lock & Mint status)
            item {
                LevelSelectorRow(
                    levels = state.availableLevels,
                    currentLevel = state.certificateData.level,
                    unlockedLevels = state.unlockedLevels,
                    mintedCertificates = state.mintedCertificates,
                    onSelectLevel = { viewModel.selectLevel(it) },
                    onLockedLevelClick = { lvl ->
                        RankLevelSystem.RANKS.find { it.level == lvl }?.let { rank ->
                            viewModel.openUnlockModal(rank)
                        }
                    }
                )
            }

            // Gating Banner: Level Locked OR Mint Required
            if (!isLevelUnlocked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF140D1E),
                        border = BorderStroke(1.dp, PurpleArc.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PurpleArc,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LEVEL ${currentRank.level} IS LOCKED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔒 Unlock previous levels first or unlock with XP.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.openUnlockModal(currentRank) },
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleArc),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GlassWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Unlock Level ${currentRank.level}",
                                    color = GlassWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else if (!isCertificateMinted) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1A1608),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CERTIFICATE UNMINTED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Level ${currentRank.level} is unlocked! Mint this official certificate for ${String.format("%,d", currentRank.certificateCost)} XP to generate cryptographic authority and export in A4 PDF, PNG, and JPG.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhite.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.openMintModal(currentRank) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Text(
                                    text = "Mint Certificate (${String.format("%,d", currentRank.certificateCost)} XP)",
                                    color = DarkNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Expandable Custom Fields Editor (Only available after minting)
            if (isCertificateMinted) {
                item {
                    AnimatedVisibility(visible = state.isEditMode) {
                        CertificateFieldsEditorCard(
                            data = state.certificateData,
                            onNameChange = { viewModel.updateStudentName(it) },
                            onClassChange = { viewModel.updateStudentClass(it) },
                            onDateChange = { viewModel.updateDateAchieved(it) },
                            onEvaluationChange = { viewModel.updateAiEvaluation(it) },
                            onResetToLive = { viewModel.loadLiveProfileData() }
                        )
                    }
                }
            }

            // 3. Official Master Certificate Preview (Exact Template)
            item {
                CertificateMasterPreview(
                    data = state.certificateData,
                    isUnlocked = isLevelUnlocked,
                    isMinted = isCertificateMinted,
                    onMintClick = { viewModel.openMintModal(currentRank) }
                )
            }

            // 4. Export & Print Hub (Disabled if unminted)
            if (isCertificateMinted) {
                item {
                    ExportActionPanel(
                        exportStatus = state.exportStatus,
                        onOpenExportSheet = { viewModel.openExportSheet() },
                        onPrint = { activity?.let { viewModel.printCertificate(it) } }
                    )
                }
            }

            // 5. Verification & Security Footnote
            item {
                SecurityVerificationCard(data = state.certificateData)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Unlock Level
    state.selectedRankForUnlock?.let { rank ->
        UnlockLevelModal(
            rank = rank,
            currentXpBalance = state.currentXpBalance,
            isUnlocking = state.isUnlocking,
            onConfirmUnlock = { viewModel.confirmUnlockLevel(rank) },
            onDismiss = { viewModel.dismissUnlockModal() }
        )
    }

    // Modal: Mint Certificate
    state.selectedRankForMint?.let { rank ->
        MintCertificateModal(
            rank = rank,
            currentXpBalance = state.currentXpBalance,
            isMinting = state.isMinting,
            onConfirmMint = { viewModel.confirmMintCertificate(rank) },
            onDismiss = { viewModel.dismissMintModal() }
        )
    }

    // Modal: Certificate Mint Celebration
    state.mintSuccessData?.let { data ->
        CertificateCelebrationModal(
            level = data.level,
            rankTitle = data.rankTitle,
            xpDeducted = data.xpDeducted,
            onViewCertificate = { viewModel.dismissMintSuccessData() },
            onSaveCertificate = {
                viewModel.dismissMintSuccessData()
                viewModel.openExportSheet()
            },
            onDismiss = { viewModel.dismissMintSuccessData() }
        )
    }

    // Bottom Sheet: Save Certificate format chooser
    if (state.showExportBottomSheet) {
        SaveCertificateBottomSheet(
            exportStatus = state.exportStatus,
            onSelectFormat = { format ->
                viewModel.exportFormat(context, format)
            },
            onDismiss = { viewModel.dismissExportSheet() }
        )
    }

    // Success Dialog with instant Share action
    (state.exportStatus as? ExportStatus.Success)?.let { success ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportStatus() },
            containerColor = DeepNavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IceCyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Ready", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = success.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LuxuryCard,
                        border = BorderStroke(0.5.dp, Color(0x337C8CFF))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "File: ${success.file.name}",
                                color = IceCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Location: Internal Certificates Directory (${success.file.length() / 1024} KB)",
                                color = GlassWhiteMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.shareCertificate(context, success.file, success.mimeType)
                        viewModel.dismissExportStatus()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Now", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportStatus() }) {
                    Text("Dismiss", color = GlassWhiteMuted)
                }
            }
        )
    }
}

/**
 * 25-Level Horizontal Selector Bar
 */
@Composable
private fun LevelSelectorRow(
    levels: List<Pair<Int, String>>,
    currentLevel: Int,
    unlockedLevels: Set<Int>,
    mintedCertificates: Set<Int>,
    onSelectLevel: (Int) -> Unit,
    onLockedLevelClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepNavySurface)
            .border(BorderStroke(1.dp, Color(0x227C8CFF)), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = PurpleArc,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SELECT LEVEL (1 TO 25)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "Active: L$currentLevel",
                color = IceCyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            levels.forEach { (lvl, title) ->
                val isSelected = lvl == currentLevel
                val isUnlocked = unlockedLevels.contains(lvl)
                val isMinted = mintedCertificates.contains(lvl)

                val cardBg = when {
                    isSelected -> IceCyanPrimary
                    isMinted -> Color(0xFF132A26)
                    isUnlocked -> LuxuryCard
                    else -> Color(0xFF090D18)
                }

                val borderStroke = when {
                    isSelected -> BorderStroke(1.dp, IceCyanPrimary)
                    isMinted -> BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f))
                    isUnlocked -> BorderStroke(1.dp, Color(0x337C8CFF))
                    else -> BorderStroke(1.dp, GlassBorder)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cardBg,
                    border = borderStroke,
                    modifier = Modifier.clickable {
                        onSelectLevel(lvl)
                        if (!isUnlocked) {
                            onLockedLevelClick(lvl)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = if (isSelected) DarkNavy else GlassWhiteMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Level $lvl",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) DarkNavy else GlassWhiteMuted,
                                fontSize = 12.sp
                            )
                        } else {
                            if (isMinted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Minted",
                                    tint = if (isSelected) DarkNavy else SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "L$lvl",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) DarkNavy else if (isMinted) SuccessGreen else IceCyanPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkNavy else Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Collapsible Field Editor
 */
@Composable
private fun CertificateFieldsEditorCard(
    data: CertificateData,
    onNameChange: (String) -> Unit,
    onClassChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onEvaluationChange: (String) -> Unit = {},
    onResetToLive: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DeepNavySurface,
        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CUSTOMIZE DYNAMIC FIELDS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IceCyanPrimary,
                    letterSpacing = 0.5.sp
                )
                TextButton(onClick = onResetToLive) {
                    Text("Reset to Live", color = IceCyanPrimary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = data.studentName,
                onValueChange = onNameChange,
                label = { Text("Student Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IceCyanPrimary,
                    unfocusedBorderColor = Color(0x337C8CFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = data.studentClass,
                onValueChange = onClassChange,
                label = { Text("Class / Stream") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IceCyanPrimary,
                    unfocusedBorderColor = Color(0x337C8CFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = data.issueDate,
                onValueChange = onDateChange,
                label = { Text("Issue Date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IceCyanPrimary,
                    unfocusedBorderColor = Color(0x337C8CFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

/**
 * The Master Certificate Layout Preview
 * Strictly uses R.drawable.rebuild_certificate_template as the base background.
 * Overlays dynamic data strictly in designated safe areas matching the 7 mandated sections:
 * 1. Student Name
 * 2. Class Information
 * 3. Achievement Statement
 * 4. Level Information Bar
 * 5. Quote Section
 * 6. Date Section
 * 7. Certificate ID
 */
@Composable
private fun CertificateMasterPreview(
    data: CertificateData,
    isUnlocked: Boolean = true,
    isMinted: Boolean = true,
    onMintClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF070E1A),
        border = BorderStroke(1.5.dp, Color(0xFFC69214).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(896f / 1200f) // Exact Master Template Aspect Ratio
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight
                val maxSafeWidth = boxWidth * 0.74f

                if (!isUnlocked) {
                    // LOCKED CERTIFICATE: Heavy dark blur & overlay. No readable content visible!
                    Image(
                        painter = painterResource(id = R.drawable.rebuild_certificate_template),
                        contentDescription = "Locked Certificate Template",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xF8060B16)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF140D26))
                                    .border(1.5.dp, PurpleArc, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PurpleArc,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "LOCKED CERTIFICATE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔒 Unlock previous levels first.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassWhiteMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // 1. Master Template Base Image (exact, un-modified background)
                    Image(
                        painter = painterResource(id = R.drawable.rebuild_certificate_template),
                        contentDescription = "Master Certificate Template",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Dynamic Data Overlays strictly positioned within safe area
                    // 1. Student Name (Large bold serif font, Center aligned, Single line only, Auto shrink)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (422f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.studentName,
                            color = Color(0xFF0A192F),
                            fontSize = (boxWidth.value * 0.045f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        )
                    }

                    // 2. Class Information (Directly below name, Medium size)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (468f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.studentClass,
                            color = Color(0xFF203A63),
                            fontSize = (boxWidth.value * 0.024f).sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        )
                    }

                    // 3. Achievement Statement (Maximum 4 lines, Perfect line spacing, No overlap)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (522f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        ) {
                            data.getAchievementLines().take(4).forEach { line ->
                                Text(
                                    text = line,
                                    color = Color(0xFF1B2A4A),
                                    fontSize = (boxWidth.value * 0.020f).sp,
                                    fontFamily = FontFamily.Serif,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(boxHeight * (4f / 1200f)))
                            }
                        }
                    }

                    // 4. Level Information Bar (Single horizontal line)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (646f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.levelInfoLine,
                            color = Color(0xFF0B2545),
                            fontSize = (boxWidth.value * 0.018f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        )
                    }

                    // 5. Quote Section ("The protocol rewards action,\nnot intention.", Center aligned)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (698f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        ) {
                            data.quoteLines.forEach { quoteLine ->
                                Text(
                                    text = quoteLine,
                                    color = Color(0xFF2D3748),
                                    fontSize = (boxWidth.value * 0.019f).sp,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = FontFamily.Serif,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(boxHeight * (2f / 1200f)))
                            }
                        }
                    }

                    // 6. Date Section (Issue Date:\n{issueDate})
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (972f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.widthIn(max = maxSafeWidth)
                        ) {
                            Text(
                                text = "Issue Date:",
                                color = Color(0xFF1B365D),
                                fontSize = (boxWidth.value * 0.017f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(boxHeight * (2f / 1200f)))
                            Text(
                                text = data.issueDate,
                                color = Color(0xFF203A63),
                                fontSize = (boxWidth.value * 0.018f).sp,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 7. Certificate ID (ID: {certificateId}, Small text near bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = boxHeight * (1044f / 1200f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ID: ${data.certificateId}",
                            color = Color(0xFF4A5568),
                            fontSize = (boxWidth.value * 0.015f).sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Export Toolbar: SAVE CERTIFICATE (Triggers Format Bottom Sheet) & Print
 */
@Composable
private fun ExportActionPanel(
    exportStatus: ExportStatus,
    onOpenExportSheet: () -> Unit,
    onPrint: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DeepNavySurface,
        border = BorderStroke(1.dp, Color(0x337C8CFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CERTIFICATE EXPORT ENGINE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (exportStatus is ExportStatus.Exporting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = IceCyanPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Saving certificate to device storage...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenExportSheet,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_certificate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = DarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE CERTIFICATE",
                            color = DarkNavy,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onPrint,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x447C8CFF)),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("print_certificate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "✓ Save as PDF, PNG, or JPG • Automatically stored on device • Print-ready A4",
                style = MaterialTheme.typography.bodySmall,
                color = GlassWhiteMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Security & Verification Details
 */
@Composable
private fun SecurityVerificationCard(data: CertificateData) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LuxuryCard,
        border = BorderStroke(0.5.dp, Color(0x227C8CFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = IceCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REBUILD PROTOCOL VERIFICATION AUTHORITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = IceCyanPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Certificate ID: ${data.certificateId}",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Verification Hash: ${data.verificationHash}",
                color = GlassWhiteMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Issuing Engine: REBUILD Neural Engine • Authority: REBUILD Achievement Authority",
                color = GlassWhiteMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
