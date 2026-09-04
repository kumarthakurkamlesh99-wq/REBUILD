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
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.viewmodel.CertificateUiState
import com.example.viewmodel.CertificateViewModel
import com.example.viewmodel.ExportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    viewModel: CertificateViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

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
                    onSelectLevel = { viewModel.selectLevel(it) }
                )
            }

            // Gating Banner: Level Locked OR Mint Required
            if (!isLevelUnlocked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF140D1E),
                        border = BorderStroke(1.dp, Color(0x66FF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DangerRed,
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
                                text = "Unlock Level ${currentRank.level} in the Rank Hierarchy before claiming its certificate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassWhiteMuted,
                                textAlign = TextAlign.Center
                            )
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
                    data = state.certificateData
                )
            }

            // 4. Export & Print Hub (Disabled if unminted)
            if (isCertificateMinted) {
                item {
                    ExportActionPanel(
                        exportStatus = state.exportStatus,
                        onExportPdf = { viewModel.exportPdf(context) },
                        onExportPng = { viewModel.exportPng(context) },
                        onExportJpg = { viewModel.exportJpg(context) },
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
    onSelectLevel: (Int) -> Unit
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
                    modifier = Modifier.clickable { onSelectLevel(lvl) }
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
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (isMinted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Minted",
                                tint = if (isSelected) DarkNavy else SuccessGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = "L$lvl",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) DarkNavy else if (isMinted) SuccessGreen else if (isUnlocked) IceCyanPrimary else GlassWhiteMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkNavy else if (isUnlocked) Color.White else GlassWhiteMuted,
                            fontSize = 11.sp
                        )
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
    onEvaluationChange: (String) -> Unit,
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
                value = data.dateAchieved,
                onValueChange = onDateChange,
                label = { Text("Date Achieved") },
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
                value = data.aiEvaluation,
                onValueChange = onEvaluationChange,
                label = { Text("AI Protocol Evaluation") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
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
 * Strictly uses R.drawable.rebuild_certificate_template as the base background
 * with precise typography matching official government-style certificates.
 */
@Composable
private fun CertificateMasterPreview(
    data: CertificateData
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

                // Master Template Base Image (Guilloche border, Laurel wreath, CERTIFICATE header, Seal, Signatures)
                Image(
                    painter = painterResource(id = R.drawable.rebuild_certificate_template),
                    contentDescription = "Master Certificate Template",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // Dynamic Overlaid Data Elements in exact coordinates
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = boxWidth * 0.12f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Spacing to clear top laurel wreath and CERTIFICATE banner (approx 39% height)
                    Spacer(modifier = Modifier.height(boxHeight * 0.38f))

                    // 1. Subtitle Header
                    Text(
                        text = "THIS CERTIFICATE IS PROUDLY PRESENTED TO",
                        color = Color(0xFF1B365D),
                        fontSize = (boxWidth.value * 0.024f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(boxHeight * 0.008f))

                    // 2. Student Name
                    Text(
                        text = data.studentName,
                        color = Color(0xFF0A192F),
                        fontSize = (boxWidth.value * 0.052f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Gold divider line
                    Box(
                        modifier = Modifier
                            .width(boxWidth * 0.62f)
                            .height(1.5.dp)
                            .background(Color(0xFFC69214))
                    )

                    Spacer(modifier = Modifier.height(boxHeight * 0.006f))

                    // 3. Class and Cohort
                    Text(
                        text = data.studentClass,
                        color = Color(0xFF203A63),
                        fontSize = (boxWidth.value * 0.026f).sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(boxHeight * 0.012f))

                    // 4. Achievement Text
                    Text(
                        text = data.getAchievementText(),
                        color = Color(0xFF1B2A4A),
                        fontSize = (boxWidth.value * 0.023f).sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        lineHeight = (boxWidth.value * 0.033f).sp,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )

                    Spacer(modifier = Modifier.height(boxHeight * 0.014f))

                    // 5. Protocol Metrics Badge Row
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x0CF0F4F8),
                        border = BorderStroke(0.8.dp, Color(0x40C69214))
                    ) {
                        Text(
                            text = "Level ${data.level} (${data.rankTitle})  •  ${String.format("%,d", data.xp)} XP  •  ${data.streak}D Streak  •  Arc Day ${data.winterArcDay}",
                            color = Color(0xFF0B2545),
                            fontSize = (boxWidth.value * 0.022f).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(boxHeight * 0.012f))

                    // 6. AI Evaluation
                    Text(
                        text = "AI PROTOCOL EVALUATION",
                        color = Color(0xFFC69214),
                        fontSize = (boxWidth.value * 0.018f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "\"${data.aiEvaluation}\"",
                        color = Color(0xFF2D3748),
                        fontSize = (boxWidth.value * 0.021f).sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = (boxWidth.value * 0.028f).sp,
                        modifier = Modifier.fillMaxWidth(0.88f)
                    )

                    Spacer(modifier = Modifier.height(boxHeight * 0.012f))

                    // 7. Date Achieved
                    Text(
                        text = "DATE OF ISSUANCE: ${data.dateAchieved.uppercase()}",
                        color = Color(0xFF1B365D),
                        fontSize = (boxWidth.value * 0.020f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 0.5.sp
                    )

                    // Spacing down to signature lines above the bottom seal
                    Spacer(modifier = Modifier.height(boxHeight * 0.065f))

                    // 8. Signatures: REBUILD Neural Engine (Left) & REBUILD Achievement Authority (Right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "REBUILD Neural Engine",
                                color = Color(0xFF0A192F),
                                fontSize = (boxWidth.value * 0.022f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "[DIGITALLY SIGNED]",
                                color = Color(0xFF718096),
                                fontSize = (boxWidth.value * 0.016f).sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "REBUILD Achievement Authority",
                                color = Color(0xFF0A192F),
                                fontSize = (boxWidth.value * 0.022f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "[VERIFIED AUTHORITY]",
                                color = Color(0xFF718096),
                                fontSize = (boxWidth.value * 0.016f).sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(boxHeight * 0.028f))

                    // 9. Footer ID & Hash
                    Text(
                        text = "ID: ${data.certificateId}   •   HASH: ${data.verificationHash.take(16)}...",
                        color = Color(0xFF4A5568),
                        fontSize = (boxWidth.value * 0.017f).sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Export Toolbar: High-Resolution A4 PDF, PNG, JPG, Print, Share
 */
@Composable
private fun ExportActionPanel(
    exportStatus: ExportStatus,
    onExportPdf: () -> Unit,
    onExportPng: () -> Unit,
    onExportJpg: () -> Unit,
    onPrint: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DeepNavySurface,
        border = BorderStroke(1.dp, Color(0x337C8CFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    text = "HIGH-RESOLUTION EXPORT ENGINE",
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
                        text = "Rendering High-Resolution Master Certificate...",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PDF Button (Primary A4)
                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = DarkNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF (A4)",
                            color = DarkNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // PNG Button
                    Button(
                        onClick = onExportPng,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "PNG",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // JPG Button
                    Button(
                        onClick = onExportJpg,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "JPG",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Print Button
                    Button(
                        onClick = onPrint,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Print",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "✓ Print-Ready (300 DPI A4 Portrait)   ✓ Gallery-Ready (Lossless PNG)   ✓ Share-Ready",
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
