package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.DarkLuxuryBackground
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.ProfileSettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: ProfileSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateAvatarUri(uri.toString())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collectLatest {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    val classOptions = listOf("Class 12", "Class 11", "Class 10", "Dropper / JEE / NEET", "College")
    val boardOptions = listOf("Bihar Board", "CBSE", "ICSE", "State Board", "Other")
    val streamOptions = listOf("Science (PCM)", "Science (PCB)", "Science (PCMB)", "Commerce", "Arts / Humanities")

    Scaffold(
        modifier = Modifier.testTag("profile_settings_screen"),
        containerColor = DarkLuxuryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profile Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = "Identity & Academic Calibration",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkLuxuryBackground
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IceCyanPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Avatar Section
                item {
                    AvatarSelectionCard(
                        avatarUri = state.avatarUri,
                        name = state.name,
                        onSelectPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearPhoto = { viewModel.updateAvatarUri("") }
                    )
                }

                // 2. Personal & Academic Profile
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SectionLabel(title = "PERSONAL & ACADEMICS", icon = Icons.Default.Person)

                            // Name
                            FieldInput(
                                label = "Full Name",
                                value = state.name,
                                onValueChange = { viewModel.updateName(it) },
                                testTag = "profile_name_input"
                            )

                            // Class Chips
                            ChipSelector(
                                title = "Class",
                                options = classOptions,
                                selected = state.studentClass,
                                onSelect = { viewModel.updateStudentClass(it) }
                            )

                            // Board Chips
                            ChipSelector(
                                title = "Board",
                                options = boardOptions,
                                selected = state.board,
                                onSelect = { viewModel.updateBoard(it) }
                            )

                            // Stream Chips
                            ChipSelector(
                                title = "Academic Stream",
                                options = streamOptions,
                                selected = state.stream,
                                onSelect = { viewModel.updateStream(it) }
                            )

                            // Target Percentage
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Percentage",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GlassWhiteMuted
                                    )
                                    Text(
                                        text = "${state.targetPercentage}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = IceCyanPrimary
                                    )
                                }
                                Slider(
                                    value = state.targetPercentage.toFloat(),
                                    onValueChange = { viewModel.updateTargetPercentage(it.toInt()) },
                                    valueRange = 60f..100f,
                                    steps = 39,
                                    colors = SliderDefaults.colors(
                                        thumbColor = IceCyanPrimary,
                                        activeTrackColor = IceCyanPrimary,
                                        inactiveTrackColor = Color(0xFF1E293B)
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Routine Schedule (Wake & Sleep Time)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SectionLabel(title = "DAILY PROTOCOL TIMINGS", icon = Icons.Default.Alarm)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FieldInput(
                                        label = "Wake Time (HH:mm)",
                                        value = state.wakeUpTime,
                                        onValueChange = { viewModel.updateWakeUpTime(it) },
                                        testTag = "profile_wake_time_input"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    FieldInput(
                                        label = "Sleep Time (HH:mm)",
                                        value = state.sleepTime,
                                        onValueChange = { viewModel.updateSleepTime(it) },
                                        testTag = "profile_sleep_time_input"
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Milestone Dates & Goals
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SectionLabel(title = "DATES & MISSION OBJECTIVE", icon = Icons.Default.CalendarMonth)

                            FieldInput(
                                label = "Winter Arc Start Date (yyyy-MM-dd)",
                                value = state.winterArcStartDate,
                                onValueChange = { viewModel.updateWinterArcStartDate(it) },
                                testTag = "profile_arc_start_input"
                            )

                            FieldInput(
                                label = "Exam Date (yyyy-MM-dd)",
                                value = state.targetExamDate,
                                onValueChange = { viewModel.updateTargetExamDate(it) },
                                testTag = "profile_exam_date_input"
                            )

                            FieldInput(
                                label = "Primary Academic Goal",
                                value = state.goal,
                                onValueChange = { viewModel.updateGoal(it) },
                                singleLine = false,
                                minLines = 2,
                                testTag = "profile_goal_input"
                            )
                        }
                    }
                }

                // 5. Save Button
                item {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_profile_button"),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyanPrimary,
                            disabledContainerColor = IceCyanPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                color = Color(0xFF050816),
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Saving Profile...", color = Color(0xFF050816), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color(0xFF050816)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAVE PROFILE SETTINGS",
                                color = Color(0xFF050816),
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AvatarSelectionCard(
    avatarUri: String,
    name: String,
    onSelectPhoto: () -> Unit,
    onClearPhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlowBorderBrush, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Display
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131D38))
                    .border(2.dp, IceCyanPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri.isNotBlank()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = if (name.isNotBlank()) name.take(2).uppercase() else "KT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IceCyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = "Profile Avatar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Text(
                    text = "Choose an avatar or photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSelectPhoto,
                        colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color(0xFF050816), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Image", color = Color(0xFF050816), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (avatarUri.isNotBlank()) {
                        Button(
                            onClick = onClearPhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263352)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Reset", color = GlassWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IceCyanPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = IceCyanGlow,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FieldInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = GlassWhiteMuted) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IceCyanPrimary,
            unfocusedBorderColor = GlassBorder,
            focusedContainerColor = Color(0xFF070B16),
            unfocusedContainerColor = Color(0xFF070B16),
            focusedTextColor = GlassWhite,
            unfocusedTextColor = GlassWhite
        )
    )
}

@Composable
fun ChipSelector(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = title,
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
            options.forEach { option ->
                val isSelected = selected.equals(option, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) IceCyanPrimary else Color(0xFF0A0F1D))
                        .border(1.dp, if (isSelected) IceCyanPrimary else GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelect(option) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF050816) else GlassWhiteMuted
                    )
                }
            }
        }
    }
}
